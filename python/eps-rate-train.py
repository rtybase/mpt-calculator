import sys
import util.ml

def train_all_models(X, y, dataset_index):
    util.ml.train_and_save_linear(X, y, dataset_index)

    for degree in range(2, util.ml.MAX_DEGREE + 1):
        util.ml.train_and_save_polynomial(X, y, degree, dataset_index)

    for depth in range(util.ml.MIN_DEPTH, util.ml.MAX_DEPTH + 1):
        util.ml.train_and_save_dtr(X, y, depth, dataset_index)

    for depth in range(util.ml.MIN_DEPTH, util.ml.MAX_DEPTH + 1):
        util.ml.train_and_save_rfr(X, y, depth, dataset_index)

if len(sys.argv) > 1:
    print("Training with DS=2", flush=True)
    X,y = util.ml.load_training_data_2_days_after_eps(sys.argv[1])
    train_all_models(X, y, 2)

    print("Training with DS=1", flush=True)
    X,y = util.ml.load_training_data_1_day_after_eps(sys.argv[1])
    train_all_models(X, y, 1)

else:
    print("Specify the file with training data!")
