import numpy
import datetime
import sys
import util.ml
import util.db
from sklearn.metrics import r2_score
from statistics import correlation

def save_to_db(dataset, metric, model, report_date, result, after_training):
    with util.db.db_conection.cursor() as cursor:
        cursor.execute("""INSERT into tbl_ml_quality
                           (int_dataset,vchr_metric,vchr_model,dtm_report_date,dbl_result,bln_after_retrain) 
                           values (%s,%s,%s,%s,%s,%s)
                           ON DUPLICATE KEY UPDATE
                           dbl_result=VALUES(dbl_result),
                           bln_after_retrain=VALUES(bln_after_retrain)""",\
            (dataset, metric, model, report_date, result, after_training))
    util.db.db_conection.commit()


def compute_matches(y_actual, y_predicted):
    return numpy.linalg.norm(y_actual - y_predicted)

def report_model_details(y_actual, y_predicted, dataset_index, model_name, date, after_training):
    r2 = r2_score(y_actual, y_predicted)
    corr = correlation(y_actual, y_predicted)
    distance = compute_matches(y_actual, y_predicted)

    print("---------------------------")
    print(f"ds={dataset_index}, {model_name} distance=%s" % (distance))
    print(f"ds={dataset_index}, {model_name} r2=%s" % (r2))
    print(f"ds={dataset_index}, {model_name} corr=%s" % (corr))

    save_to_db(dataset_index, "r2", model_name, date, r2, after_training)
    save_to_db(dataset_index, "corr", model_name, date, corr, after_training)
    save_to_db(dataset_index, "distance", model_name, date, distance, after_training)

def validate_all_models(X,y_actual, dataset_index, date, after_training):
    y_predicted = util.ml.predict_l(X, dataset_index)
    report_model_details(y_actual, y_predicted, dataset_index, 'linear', date, after_training)

    y_predicted = util.ml.predict_dtr(X, dataset_index)
    report_model_details(y_actual, y_predicted, dataset_index, 'd-tree', date, after_training)

    y_predicted = util.ml.predict_rfr(X, dataset_index)
    report_model_details(y_actual, y_predicted, dataset_index, 'r-freg', date, after_training)

    y_predicted = util.ml.predict_xgb(X, dataset_index)
    report_model_details(y_actual, y_predicted, dataset_index, 'xgb-reg', date, after_training)

    for degree in range(2, util.ml.MAX_DEGREE + 1):
        y_predicted = util.ml.predict_p(degree, X, dataset_index)
        model_name = f"polynomial d={degree}"

        report_model_details(y_actual, y_predicted, dataset_index, model_name, date, after_training)

    print("==============================================")


after_training = False

if len(sys.argv) > 1:
    after_training = sys.argv[1].strip().lower() == "true"
else:
    print("after training not set, assuming false!", flush=True)

current_date = datetime.datetime.now()

X,y_actual = util.ml.load_training_data_2(util.ml.DS2_FILE)
num_rows, num_cols = X.shape
validate_all_models(X,y_actual, 2, current_date, after_training)
save_to_db(2, "rows", "all", current_date, num_rows, after_training)

X,y_actual = util.ml.load_training_data_1(util.ml.DS1_FILE)
num_rows, num_cols = X.shape
validate_all_models(X,y_actual, 1, current_date, after_training)
save_to_db(1, "rows", "all", current_date, num_rows, after_training)
