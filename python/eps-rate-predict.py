import sys
import os.path
import pandas as pd
import numpy
import util.ml
import util.db
import util.dates

DS1_FILE = "inputs-ml/out-pred-ds-1.csv"
DS2_FILE = "inputs-ml/out-pred-ds-2.csv"

def add_model_results_and_print(dictionary_to_add, model_name, model_result):
    dictionary_to_add["models"][model_name] = model_result
    print("%s=%s" % (model_name, model_result))

def predict_with_all_models(data_file, data_array, dataset_index):
    results = {}
    results["models"] = {}

    if (os.path.isfile(data_file)):
        dataset = pd.read_csv(data_file)

        results['assets'] = dataset['asset_id'].tolist()
        results['dates'] = dataset['eps_date'].tolist()
        results['amc'] = dataset['after_market_close'].tolist()
        print("assets=%s" % (results['assets']))
        print("dates=%s" % (results['dates']))
        print("after market close=%s" % (results['amc']))

        X = dataset[data_array].values
        add_model_results_and_print(results, "linear", util.ml.predict_l(X, dataset_index))
        add_model_results_and_print(results, "d-tree", util.ml.predict_dtr(X, dataset_index))
        add_model_results_and_print(results, "r-freg", util.ml.predict_rfr(X, dataset_index))

        for degree in range(2, util.ml.MAX_DEGREE + 1):
            add_model_results_and_print(results, "poly-d{0}".format(degree),\
                util.ml.predict_p(degree, X, dataset_index))

    else:
        print(f"{data_file} is missing! No predictions today.")

    return results

def next_w_date(eps_date, days_after_eps):
    next_date = util.dates.string_to_date(eps_date)
    for i in range(days_after_eps):
        next_date = util.dates.next_working_date(next_date)

    return next_date

def save_to_db(asset, eps_date, days_after_eps, model, prediction):
    with util.db.db_conection.cursor() as cursor:
        cursor.execute("""INSERT into tbl_predictions 
                           (fk_assetID, vchr_model, dtm_eps_date, int_days_after_eps, dtm_prd_date, dbl_prd_return) 
                           values (%s,%s,%s,%s,%s,%s)
                           ON DUPLICATE KEY UPDATE
                           dbl_prd_return=VALUES(dbl_prd_return)""",\
            (asset, model, eps_date, days_after_eps,\
                next_w_date(eps_date, days_after_eps),\
                prediction))
    util.db.db_conection.commit()

def save_results(model_details, dataset_index):
    if (len(model_details['models']) > 0):
        lengh = len(model_details['assets'])
        models = [k for k in model_details['models']]
        for i in range(lengh):
            symbol = model_details['assets'][i]
            asset_id = util.db.asset_id_from_symbol(symbol)

            if (asset_id >= 0):
                date = model_details['dates'][i]
                after_market_close = model_details['amc'][i]

                for m in models:
                    days_after_eps = dataset_index - (1 - after_market_close)
                    prediction = model_details['models'][m][i]
                    save_to_db(asset_id, date, days_after_eps, m, prediction)
            else:
                print("Could not find ID for %s" % (symbol))

numpy.set_printoptions(suppress=True, threshold=sys.maxsize, linewidth=500)

print("=========================<2-DS-EPS>============================")
model_details = predict_with_all_models(DS2_FILE, [*util.ml.CORE_COLUMNS_FOR_TRAINING,\
                    'rate_after', 'v_chng_after'], 2)
save_results(model_details, 2)

print("=========================<1-DS-EPS>=============================")
model_details = predict_with_all_models(DS1_FILE, util.ml.CORE_COLUMNS_FOR_TRAINING, 1)
save_results(model_details, 1)
