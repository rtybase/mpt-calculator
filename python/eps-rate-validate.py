import sys
import numpy
import util.ml
from sklearn.metrics import r2_score
from statistics import correlation

def compute_matches(y_actual, y_predicted):
    return numpy.linalg.norm(y_actual - y_predicted)

def report_model_details(y_actual, y_predicted, dataset_index, model_name):
    r2 = r2_score(y_actual, y_predicted)
    corr = correlation(y_actual, y_predicted)
    print("---------------------------")
    print(f"ds={dataset_index} after EPS, {model_name} distance=%s" %\
        (compute_matches(y_actual, y_predicted)))
    print(f"ds={dataset_index} after EPS, {model_name} r2=%s" % (r2))
    print(f"ds={dataset_index} after EPS, {model_name} corr=%s" % (corr))


def validate_all_models(X,y_actual, dataset_index):
    y_predicted = util.ml.predict_l(X, dataset_index)
    report_model_details(y_actual, y_predicted, dataset_index, 'linear')

    y_predicted = util.ml.predict_dtr(X, dataset_index)
    report_model_details(y_actual, y_predicted, dataset_index, 'd-tree')

    y_predicted = util.ml.predict_rfr(X, dataset_index)
    report_model_details(y_actual, y_predicted, dataset_index, 'r-freg')

    for degree in range(2, util.ml.MAX_DEGREE + 1):
        print("---------------------------")

        y_predicted = util.ml.predict_p(degree, X, dataset_index)
        r2 = r2_score(y_actual, y_predicted)
        corr = correlation(y_actual, y_predicted)

        print("ds=%s after EPS, polynomial d=%s distance=%s" % (dataset_index, degree,\
            compute_matches(y_actual, y_predicted)))
        print("ds=%s after EPS, polynomial d=%s r2=%s" % (dataset_index, degree, r2))
        print("ds=%s after EPS, polynomial d=%s corr=%s" % (dataset_index, degree, corr))

    print("==============================================")

if len(sys.argv) > 1:
    X,y_actual = util.ml.load_training_data_2_days_after_eps(sys.argv[1])
    validate_all_models(X,y_actual, 2)

    X,y_actual = util.ml.load_training_data_1_day_after_eps(sys.argv[1])
    validate_all_models(X,y_actual, 1)

else:
    print("Specify the file with data for prediction!")
