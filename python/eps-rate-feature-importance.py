import sys
import util.ml
import numpy
import pandas as pd
import matplotlib.pyplot as plt

from sklearn.tree import DecisionTreeRegressor
from sklearn.ensemble import RandomForestRegressor

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

if len(sys.argv) > 1:
    print("Looking for the best params from DS=2", flush=True)
    X_ds, y_ds = util.ml.load_training_dataset_2_days_after_eps(sys.argv[1])
    fit_and_plot_with(X_ds, y_ds, util.ml.DTR_DS2_ARGS, util.ml.RFR_DS2_ARGS, 2)

    print("Looking for the best params from DS=1", flush=True)
    X_ds, y_ds = util.ml.load_training_dataset_1_day_after_eps(sys.argv[1])
    fit_and_plot_with(X_ds, y_ds, util.ml.DTR_DS1_ARGS, util.ml.RFR_DS1_ARGS, 1)

else:
    print("Specify the file with training data!")

