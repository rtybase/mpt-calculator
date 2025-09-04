import util.ml

DS1_FILE = "inputs-ml/out-training-for-1d.csv"
DS2_FILE = "inputs-ml/out-training-for-2d.csv"

def train_all_models(X, y, dataset_index):
    util.ml.train_and_save_linear(X, y, dataset_index)
    util.ml.train_and_save_dtr(X, y, dataset_index)
    util.ml.train_and_save_rfr(X, y, dataset_index)

    for degree in range(2, util.ml.MAX_DEGREE + 1):
        util.ml.train_and_save_polynomial(X, y, degree, dataset_index)


print(f"Training with DS=2 with {DS2_FILE}", flush=True)
X,y = util.ml.load_training_data_2_days_after_eps(DS2_FILE)
train_all_models(X, y, 2)

print(f"Training with DS=1 with {DS1_FILE}", flush=True)
X,y = util.ml.load_training_data_1_day_after_eps(DS1_FILE)
train_all_models(X, y, 1)

