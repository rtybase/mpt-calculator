import util.ml

from sklearn.tree import DecisionTreeRegressor
from sklearn.ensemble import RandomForestRegressor
from sklearn.model_selection import GridSearchCV
from xgboost import XGBRegressor

def best_model_params(X, y, regression_model, params):
    grid = GridSearchCV(regression_model, param_grid=params, cv=5, scoring='r2', n_jobs=-1, verbose=3)
    grid.fit(X, y)
    print(">>>>>>>>Best parameters:", grid.best_params_)

def dtr_model_and_paramrs():
    params = {
        'max_depth': [5, 6, 7],
        'min_samples_split': [2, 4, 6, 8, 10],
        'min_samples_leaf': [1, 2, 4, 6, 7, 8, 10, 12, 14, 15],
        'criterion': ['absolute_error']
    }

    model = DecisionTreeRegressor(random_state=42)
    return model, params

def rfr_model_and_paramrs():
    params = {
        'n_estimators': [200, 300, 400],
        'max_depth': [5, 6],
        'min_samples_split': [2, 4, 6, 8, 10],
        'min_samples_leaf': [1, 2, 4, 6, 7, 8],
        'criterion': ['absolute_error']
    }

    model = RandomForestRegressor(random_state=42, oob_score=True)
    return model, params

def xgb_model_and_paramrs():
    params = {
        'n_estimators':[500, 502],
        'min_child_weight':[4, 5],
        'gamma':[i/10.0 for i in range(3, 6)],
        'subsample':[i/10.0 for i in range(6, 11)],
        'colsample_bytree':[i/10.0 for i in range(6, 11)],
        'max_depth': [4, 6, 7],
        'objective': ['reg:squarederror', 'reg:tweedie'],
        'booster': ['gbtree', 'gblinear'],
        'eval_metric': ['rmse'],
        'eta': [i/10.0 for i in range(3, 6)],
        'reg_alpha': [8.54327702906688],
        'reg_lambda': [7.960301462774691],
        'learning_rate': [0.03187866984798271]
    }

    model = XGBRegressor(random_state=42, nthread=-1, device='cuda',\
                tree_method='hist', early_stopping_rounds=50)
    return model, params

def best_params_for_data(X, y):
    print("For decision tree ...", flush=True)
    model, params = dtr_model_and_paramrs()
    best_model_params(X, y, model, params)

    print("For random forest ...", flush=True)
    model, params = rfr_model_and_paramrs()
    best_model_params(X, y, model, params)

    print("For xgb boost ...", flush=True)
    model, params = xgb_model_and_paramrs()
    best_model_params(X, y, model, params)


print(f"Looking for the best params from DS=2 with {util.ml.DS2_FILE}", flush=True)
X,y = util.ml.load_training_data_2(util.ml.DS2_FILE)
best_params_for_data(X, y)

print(f"Looking for the best params from DS=1 with {util.ml.DS1_FILE}", flush=True)
X,y = util.ml.load_training_data_1(util.ml.DS1_FILE)
best_params_for_data(X, y)
