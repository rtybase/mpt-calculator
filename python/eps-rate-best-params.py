import util.ml

from sklearn.tree import DecisionTreeRegressor
from sklearn.ensemble import RandomForestRegressor
from sklearn.model_selection import GridSearchCV
from sklearn.model_selection import RandomizedSearchCV
from xgboost import XGBRegressor

CV_COUNT = 5
SCORING = 'r2'
N_JOBS = -1
VERBOSITY = 3

def use_grid_search(regression_model, params):
    print("Using grid search ...", flush=True)
    return GridSearchCV(regression_model, param_grid=params, cv=CV_COUNT,\
        scoring=SCORING, n_jobs=N_JOBS, verbose=VERBOSITY)

def use_random_search(regression_model, params):
    print("Using random search ...", flush=True)
    return RandomizedSearchCV(regression_model, param_distributions=params, cv=CV_COUNT,\
        scoring=SCORING, n_iter=11, n_jobs=N_JOBS, verbose=VERBOSITY)

def search_model(regression_model, params, use_grid):
    if use_grid:
        return use_grid_search(regression_model, params)

    return use_random_search(regression_model, params)

def best_model_params(X, y, regression_model, params, use_grid):
    model = search_model(regression_model, params, use_grid)
    model.fit(X, y)
    print(">>>>>>>>Best parameters:", model.best_params_)

def dtr_model_and_paramrs():
    params = {
        'max_depth': [5, 6, 7],
        'min_samples_split': [2, 4, 6, 8, 10],
        'min_samples_leaf': [4, 6, 7, 8, 10, 12, 14, 15, 16, 18],
        'criterion': ['absolute_error']
    }

    model = DecisionTreeRegressor(random_state=42)
    return model, params

def rfr_model_and_paramrs():
    params = {
        'n_estimators': [200, 300, 400],
        'max_depth': [5, 6],
        'min_samples_split': [2, 4, 6, 8, 10],
        'min_samples_leaf': [4, 6, 7, 8, 10, 12, 14, 15, 16, 18],
        'criterion': ['absolute_error']
    }

    model = RandomForestRegressor(random_state=42, oob_score=True)
    return model, params

def xgb_model_and_paramrs():
    params = {
        'n_estimators':[400, 500],
        'min_child_weight':[4, 5],
        'gamma':[i/10.0 for i in range(3, 6)],
        'subsample':[i/10.0 for i in range(6, 11)],
        'colsample_bytree':[i/10.0 for i in range(6, 11)],
        'max_depth': [4, 6, 7],
        'objective': ['reg:squarederror'],
        'booster': ['gbtree', 'gblinear'],
        'eval_metric': ['rmse'],
        'eta': [i/10.0 for i in range(3, 6)],
        'reg_alpha': [8.54327702906688],
        'reg_lambda': [7.960301462774691],
        'learning_rate': [0.03187866984798271]
    }

    model = XGBRegressor(random_state=42, nthread=-1, device='cuda',\
                tree_method='hist', early_stopping_rounds=100)
    return model, params

def best_params_for_data(X, y):
    print("For decision tree ...", flush=True)
    model, params = dtr_model_and_paramrs()
    best_model_params(X, y, model, params, True)

    print("For random forest ...", flush=True)
    model, params = rfr_model_and_paramrs()
    best_model_params(X, y, model, params, False)

    print("For xgb boost ...", flush=True)
    model, params = xgb_model_and_paramrs()
    best_model_params(X, y, model, params, True)


print(f"Looking for the best params from DS=2 with {util.ml.DS2_FILE}", flush=True)
X,y = util.ml.load_training_data_2(util.ml.DS2_FILE)
best_params_for_data(X, y)

print(f"Looking for the best params from DS=1 with {util.ml.DS1_FILE}", flush=True)
X,y = util.ml.load_training_data_1(util.ml.DS1_FILE)
best_params_for_data(X, y)
