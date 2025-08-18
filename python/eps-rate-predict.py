import sys
import pandas as pd
import numpy
import util.ml

numpy.set_printoptions(suppress = True, threshold = sys.maxsize)

if len(sys.argv) > 1:
    file = sys.argv[1]
    dataset = pd.read_csv(file)
    X = dataset[['sector','industry','month','prev_pred_eps','prev_eps','pred_eps','eps','prev_2d_rate','prev_rate','rate','next_rate']].values

    print("linear prediction=%s" % (util.ml.predict_l(X)))
    for degree in range(2, util.ml.MAX_DEGREE + 1):
        print("polynomial d=%s prediction=%s" % (degree, util.ml.predict_p(degree, X)))

    for depth in range(2, util.ml.MAX_DEPTH + 1):
        print("d-tree d=%s prediction=%s" % (depth, util.ml.predict_dtr(depth, X)))

else:
    print("Specify the file with data for prediction!")
