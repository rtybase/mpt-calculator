import sys
import os.path
import pandas as pd
import numpy
import util.ml

DS1_FILE = "inputs-ml/out-pred-1d-after-eps.csv"
DS2_FILE = "inputs-ml/out-pred-2d-after-eps.csv"

def predict_with_all_models(X, dataset_index):
    print("ds=%s after EPS, linear prediction=%s" % (dataset_index,\
        util.ml.predict_l(X, dataset_index)))

    print("ds=%s after EPS, d-tree prediction=%s" % (dataset_index,\
        util.ml.predict_dtr(X, dataset_index)))

    print("ds=%s after EPS, r-freg prediction=%s" % (dataset_index,\
        util.ml.predict_rfr(X, dataset_index)))

    for degree in range(2, util.ml.MAX_DEGREE + 1):
        print("ds=%s after EPS, polynomial d=%s prediction=%s" % (dataset_index, degree,\
            util.ml.predict_p(degree, X, dataset_index)))

numpy.set_printoptions(suppress=True, threshold=sys.maxsize, linewidth=500)

if (os.path.isfile(DS2_FILE)):
    dataset = pd.read_csv(DS2_FILE)

    print("======================================================================")
    assets = dataset['asset_id'].tolist()
    print("assets=%s" % (assets))

    X = dataset[[*util.ml.CORE_COLUMNS_FOR_TRAINING, 'next_rate']].values
    predict_with_all_models(X, 2)
else:
    print(f"{DS2_FILE} is missing! No predictions today.")


if (os.path.isfile(DS1_FILE)):
    dataset = pd.read_csv(DS1_FILE)

    print("======================================================================")
    assets = dataset['asset_id'].tolist()
    print("assets=%s" % (assets))

    X = dataset[util.ml.CORE_COLUMNS_FOR_TRAINING].values
    predict_with_all_models(X, 1)
else:
    print(f"{DS1_FILE} is missing! No predictions today.")
