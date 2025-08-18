import sys
import numpy
import util.ml
from sklearn.metrics import r2_score

def compute_matches(y_actual, y_predicted):
    return numpy.linalg.norm(y_actual - y_predicted)

def validate_all_models(X,y_actual, dataset_index):
    y_predicted = util.ml.predict_l(X, dataset_index)
    r2 = r2_score(y_actual, y_predicted)
    print("---------------------------")
    print("ds=%s after EPS, linear distance=%s" % (dataset_index, compute_matches(y_actual, y_predicted)))
    print("ds=%s after EPS, linear r2=%s" % (dataset_index, r2))

    for degree in range(2, util.ml.MAX_DEGREE + 1):
        print("---------------------------")

        y_predicted = util.ml.predict_p(degree, X, dataset_index)
        r2 = r2_score(y_actual, y_predicted)

        print("ds=%s after EPS, polynomial d=%s distance=%s" % (dataset_index, degree,\
            compute_matches(y_actual, y_predicted)))
        print("ds=%s after EPS, polynomial d=%s r2=%s" % (dataset_index, degree, r2))

    for depth in range(util.ml.MIN_DEPTH, util.ml.MAX_DEPTH + 1):
        print("---------------------------")

        y_predicted = util.ml.predict_dtr(depth, X, dataset_index)
        r2 = r2_score(y_actual, y_predicted)

        print("ds=%s after EPS, d-tree-r d=%s distance=%s" % (dataset_index, depth,\
            compute_matches(y_actual, y_predicted)))
        print("ds=%s after EPS, d-tree-r d=%s r2=%s" % (dataset_index, depth, r2))

    print("==============================================")

if len(sys.argv) > 1:
    X,y_actual = util.ml.load_training_data_2_days_after_eps(sys.argv[1])
    validate_all_models(X,y_actual, 2)

    X,y_actual = util.ml.load_training_data_1_day_after_eps(sys.argv[1])
    validate_all_models(X,y_actual, 1)

else:
    print("Specify the file with data for prediction!")
