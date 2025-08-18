import sys
import numpy
import util.ml
from sklearn.metrics import r2_score

def compute_matches(y_actual, y_predicted):
    return numpy.linalg.norm(y_actual - y_predicted)

if len(sys.argv) > 1:
    X,y_actual = util.ml.load_training_data(sys.argv[1])

    y_predicted = util.ml.predict_l(X)
    r2 = r2_score(y_actual, y_predicted)
    print("---------------------------")
    print("linear distance=%s" % (compute_matches(y_actual, y_predicted)))
    print("linear r2=%s" % (r2))

    for degree in range(2, util.ml.MAX_DEGREE + 1):
        print("---------------------------")

        y_predicted = util.ml.predict_p(degree, X)
        r2 = r2_score(y_actual, y_predicted)

        print("polynomial d=%s distance=%s" % (degree,\
            compute_matches(y_actual, y_predicted)))
        print("polynomial d=%s r2=%s" % (degree, r2))

    for depth in range(2, util.ml.MAX_DEPTH + 1):
        print("---------------------------")

        y_predicted = util.ml.predict_dtr(depth, X)
        r2 = r2_score(y_actual, y_predicted)

        print("d-tree-r d=%s distance=%s" % (depth,\
            compute_matches(y_actual, y_predicted)))
        print("d-tree-r d=%s r2=%s" % (depth, r2))

else:
    print("Specify the file with data for prediction!")
