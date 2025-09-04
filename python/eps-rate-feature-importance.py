import util.ml
import numpy
import pandas as pd
import matplotlib.pyplot as plt

from sklearn.tree import DecisionTreeRegressor
from sklearn.ensemble import RandomForestRegressor

DS1_FILE = "inputs-ml/out-training-for-1d.csv"
DS2_FILE = "inputs-ml/out-training-for-2d.csv"

def plot_feature_importance(X, model, model_name, dataset_index):
    importances = model.feature_importances_

    indices = numpy.argsort(importances)[::-1]
    features = X.columns

    plt.figure(figsize=(10, 6))
    plt.title(f"{model_name} Feature Importances for data set {dataset_index}")
    plt.bar(range(X.shape[1]), importances[indices], align="center")
    plt.xticks(range(X.shape[1]), features[indices], rotation=45)
    plt.tight_layout()
    plt.show()

def fit_and_plot_with(X_ds, y_ds, dt_args, rf_args, dataset_index):
    dt_model = util.ml.train_decision_tree_model(X_ds.values, y_ds.values, dt_args)
    plot_feature_importance(X_ds, dt_model, "Decision Tree", dataset_index)

    rf_model = util.ml.train_random_f_regression_model(X_ds.values, y_ds.values, rf_args)
    plot_feature_importance(X_ds, rf_model, "Random Forest", dataset_index)


print(f"Looking for the best params from DS=2 with {DS2_FILE}", flush=True)
X_ds, y_ds = util.ml.load_training_dataset_2_days_after_eps(DS2_FILE)
fit_and_plot_with(X_ds, y_ds, util.ml.DTR_DS2_ARGS, util.ml.RFR_DS2_ARGS, 2)

print(f"Looking for the best params from DS=1 with {DS1_FILE}", flush=True)
X_ds, y_ds = util.ml.load_training_dataset_1_day_after_eps(DS1_FILE)
fit_and_plot_with(X_ds, y_ds, util.ml.DTR_DS1_ARGS, util.ml.RFR_DS1_ARGS, 1)
