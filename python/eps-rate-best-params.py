import util.ml

from sklearn.tree import DecisionTreeRegressor
from sklearn.ensemble import RandomForestRegressor
from sklearn.model_selection import GridSearchCV

def best_model_params(X, y, regression_model, params):
    grid = GridSearchCV(regression_model, param_grid=params, cv=5, scoring='r2', n_jobs=-1, verbose=3)
    grid.fit(X, y)
    print(">>>>>>>>Best parameters:", grid.best_params_)

def dtr_model_and_paramrs():
    params = {
        'max_depth': [5, 6, 7, 8, 9, 10],
        'min_samples_split': [2, 4, 6, 8, 10, 12, 14, 16],
        'min_samples_leaf': [1, 2, 4, 6, 8, 10, 12, 14],
        'criterion': ['absolute_error']
    }

    model = DecisionTreeRegressor(random_state=42)
    return model, params

def rfr_model_and_paramrs():
    params = {
        'n_estimators': [200, 300, 400],
        'max_depth': [5, 6],
        'min_samples_split': [2, 4, 5, 10, 12, 14, 16],
        'min_samples_leaf': [1, 2, 4, 6, 8],
        'criterion': ['absolute_error']
    }

    model = RandomForestRegressor(random_state=42, oob_score=True)
    return model, params

def best_params_for_data(X, y):
    print("For decision tree ...", flush=True)
    model, params = dtr_model_and_paramrs()
    best_model_params(X, y, model, params)

    print("For random forest ...", flush=True)
    model, params = rfr_model_and_paramrs()
    best_model_params(X, y, model, params)


print(f"Looking for the best params from DS=2 with {util.ml.DS2_FILE}", flush=True)
X,y = util.ml.load_training_data_2(util.ml.DS2_FILE)
best_params_for_data(X, y)

print(f"Looking for the best params from DS=1 with {util.ml.DS1_FILE}", flush=True)
X,y = util.ml.load_training_data_1(util.ml.DS1_FILE)
best_params_for_data(X, y)
