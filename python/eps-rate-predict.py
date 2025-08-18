import sys
import pandas as pd
import numpy
import util.ml

numpy.set_printoptions(suppress = True, threshold = sys.maxsize)

def predict_with_all_models(X, dataset_index):
    print("ds=%s after EPS, linear prediction=%s" % (dataset_index,\
        util.ml.predict_l(X, dataset_index)))

    for degree in range(2, util.ml.MAX_DEGREE + 1):
        print("ds=%s after EPS, polynomial d=%s prediction=%s" % (dataset_index, degree,\
            util.ml.predict_p(degree, X, dataset_index)))

    for depth in range(util.ml.MIN_DEPTH, util.ml.MAX_DEPTH + 1):
        print("ds=%s after EPS, d-tree d=%s prediction=%s" % (dataset_index, depth,\
            util.ml.predict_dtr(depth, X, dataset_index)))


if len(sys.argv) > 1:
    file = sys.argv[1]
    dataset = pd.read_csv(file)
    X = dataset[[*util.ml.CORE_COLUMNS_FOR_TRAINING, 'next_rate']].values
    predict_with_all_models(X, 2)

    print("==============================================")

    X = dataset[util.ml.CORE_COLUMNS_FOR_TRAINING].values
    predict_with_all_models(X, 1)


else:
    print("Specify the file with data for prediction!")
