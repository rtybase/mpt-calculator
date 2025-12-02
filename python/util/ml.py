import pandas as pd
#import cupy as cp
import joblib

from sklearn.linear_model import LinearRegression
from sklearn.preprocessing import PolynomialFeatures
from sklearn.tree import DecisionTreeRegressor
from sklearn.ensemble import RandomForestRegressor
from xgboost import XGBRegressor

DS1_FILE = "inputs-ml/out-training-ds-1.csv"
DS2_FILE = "inputs-ml/out-training-ds-2.csv"

MAX_DEGREE = 3
MODELS = {}

DTR_DS2_ARGS = {'max_depth': 5, 'min_samples_leaf': 14,\
        'min_samples_split': 2, 'criterion': 'absolute_error',\
        'random_state': 42}

DTR_DS1_ARGS = {'max_depth': 6, 'min_samples_leaf': 16,\
        'min_samples_split': 2, 'criterion': 'absolute_error',\
        'random_state': 42}

RFR_DS2_ARGS = {'max_depth': 5, 'min_samples_leaf': 14,\
        'min_samples_split': 2, 'n_estimators': 400,\
        'criterion': 'absolute_error', 'random_state': 42,\
        'oob_score': True, 'n_jobs': -1}

RFR_DS1_ARGS = {'max_depth': 6, 'min_samples_leaf': 16,\
        'min_samples_split': 2, 'n_estimators': 400,\
        'criterion': 'absolute_error', 'random_state': 42,\
        'oob_score': True, 'n_jobs': -1}

XGB_DS1_ARGS = {'reg_alpha': 8.54327702906688, 'reg_lambda': 7.960301462774691,\
        'colsample_bytree': 0.7853045036707974, 'subsample': 0.9342611289677315,\
        'learning_rate': 0.03187866984798271, 'max_depth': 7,\
        'n_estimators': 502, 'random_state': 42,\
        'n_jobs': -1, 'nthread': -1,\
        'objective': 'reg:squarederror', 'tree_method': 'hist',\
        'device': 'cuda'}

XGB_DS2_ARGS = {'reg_alpha': 8.54327702906688, 'reg_lambda': 7.960301462774691,\
        'colsample_bytree': 0.7853045036707974, 'subsample': 0.9342611289677315,\
        'learning_rate': 0.03187866984798271, 'max_depth': 7,\
        'n_estimators': 502, 'random_state': 42,\
        'n_jobs': -1, 'nthread': -1,\
        'objective': 'reg:squarederror', 'tree_method': 'hist',\
        'device': 'cuda'}

CORE_COLUMNS_FOR_TRAINING = ['sector','industry','month',\
    'prev_after_market_close', 'prev_pred_eps', 'prev_eps',\
    'prev_eps_spr', 'prev_no_analysts', 'prev_p_e', 'prev_f_score',\
    'prev_ngaap_pred_eps', 'prev_ngaap_eps',\
    'prev_ngaap_eps_spr', 'prev_revenue_spr',\
    'after_market_close', 'pred_eps', 'eps',\
    'eps_spr', 'no_analysts', 'p_e', 'f_score',\
    'ngaap_pred_eps', 'ngaap_eps',\
    'ngaap_eps_spr', 'revenue_spr',\
    'spr_pred_eps_prev_pred_eps', 'spr_eps_prev_eps',\
    'spr_ngaap_pred_eps_prev_ngaap_pred_eps', 'spr_ngaap_eps_prev_ngaap_eps',\
    'rate_before_m_1d', 'v_chng_before_m_1d',\
    'rate_before', 'v_chng_before']

POLY_P_MODEL_TEMPLATE = "ds{0}-m-polynomial-{1}-p"
POLY_L_MODEL_TEMPLATE = "ds{0}-m-polynomial-{1}-l"

LINEAR_MODEL_TEMPLATE = "ds{0}-m-linear"
D_TREE_MODEL_TEMPLATE = "ds{0}-m-dtr"
R_FREG_MODEL_TEMPLATE = "ds{0}-m-rfr"
XGBREG_MODEL_TEMPLATE = "ds{0}-m-xgb"

####################################################################

def save_model(model_name, model):
    model_file = "models/{0}.model".format(model_name)
    joblib.dump(model, model_file)

def load_model(model_name):
    if model_name in MODELS:
        return MODELS[model_name]

    model_file = "models/{0}.model".format(model_name)
    model = joblib.load(model_file)
    MODELS[model_name] = model
    return model

####################################################################

def train_polynomial_model(X, y, degree):
    polynomial_regression = PolynomialFeatures(degree=degree)
    X_poly = polynomial_regression.fit_transform(X)

    polynomial_model = LinearRegression()
    polynomial_model.fit(X_poly, y)

    return polynomial_model, polynomial_regression

def train_linear_model(X, y):
    linear_model = LinearRegression()
    linear_model.fit(X,y)

    return linear_model

def train_decision_tree_model(X, y, args):
    dtr_model = DecisionTreeRegressor(**args)
    dtr_model.fit(X,y)
    return dtr_model

def train_decision_tree_model_by_index(X, y, dataset_index):
    if (dataset_index == 1):
        return train_decision_tree_model(X, y, DTR_DS1_ARGS)

    return train_decision_tree_model(X, y, DTR_DS2_ARGS)

def train_random_f_regression_model(X, y, args):
    rfr_model = RandomForestRegressor(**args)
    rfr_model.fit(X,y)
    return rfr_model

def train_random_f_regression_model_by_index(X, y, dataset_index):
    if (dataset_index == 1):
        return train_random_f_regression_model(X, y, RFR_DS1_ARGS)

    return train_random_f_regression_model(X, y, RFR_DS2_ARGS)

def train_xgb_regression_model(X, y, args):
    xgb_model = XGBRegressor(**args)
    xgb_model.fit(X,y)
    return xgb_model

def train_xgb_regression_model_by_index(X, y, dataset_index):
    if (dataset_index == 1):
        return train_xgb_regression_model(X, y, XGB_DS1_ARGS)

    return train_xgb_regression_model(X, y, XGB_DS2_ARGS)

####################################################################

def train_and_save_polynomial(X, y, degree, dataset_index):
    l,p = train_polynomial_model(X, y, degree)

    l_model_name = POLY_L_MODEL_TEMPLATE.format(dataset_index, degree)
    p_model_name = POLY_P_MODEL_TEMPLATE.format(dataset_index, degree)

    save_model(l_model_name, l)
    save_model(p_model_name, p)

def train_and_save_linear(X, y, dataset_index):
    model_name = LINEAR_MODEL_TEMPLATE.format(dataset_index)

    model = train_linear_model(X, y)
    save_model(model_name, model)

def train_and_save_dtr(X, y, dataset_index):
    model_name = D_TREE_MODEL_TEMPLATE.format(dataset_index)

    model = train_decision_tree_model_by_index(X, y, dataset_index)
    save_model(model_name, model)

def train_and_save_rfr(X, y, dataset_index):
    model_name = R_FREG_MODEL_TEMPLATE.format(dataset_index)

    model = train_random_f_regression_model_by_index(X, y, dataset_index)
    save_model(model_name, model)

def train_and_save_xgb(X, y, dataset_index):
    model_name = XGBREG_MODEL_TEMPLATE.format(dataset_index)

    model = train_xgb_regression_model_by_index(X, y, dataset_index)
    save_model(model_name, model)

####################################################################

def load_polynomial_model(degree, dataset_index):
    l_model_name = POLY_L_MODEL_TEMPLATE.format(dataset_index, degree)
    p_model_name = POLY_P_MODEL_TEMPLATE.format(dataset_index, degree)

    l = load_model(l_model_name)
    p = load_model(p_model_name)
    return l,p

def load_linear_model(dataset_index):
    model_name = LINEAR_MODEL_TEMPLATE.format(dataset_index)
    return load_model(model_name)

def load_dtr_model(dataset_index):
    model_name = D_TREE_MODEL_TEMPLATE.format(dataset_index)
    return load_model(model_name)

def load_rfr_model(dataset_index):
    model_name = R_FREG_MODEL_TEMPLATE.format(dataset_index)
    return load_model(model_name)

def load_xgb_model(dataset_index):
    model_name = XGBREG_MODEL_TEMPLATE.format(dataset_index)
    return load_model(model_name)

####################################################################

def predict_p(degree, X, dataset_index):
    l,p = load_polynomial_model(degree, dataset_index)
    X_poly = p.transform(X)
    return l.predict(X_poly)

def predict_l(X, dataset_index):
    model = load_linear_model(dataset_index)
    return model.predict(X)

def predict_dtr(X, dataset_index):
    model = load_dtr_model(dataset_index)
    return model.predict(X)

def predict_rfr(X, dataset_index):
    model = load_rfr_model(dataset_index)
    return model.predict(X)

def predict_xgb(X, dataset_index):
    model = load_xgb_model(dataset_index)

#    trained_on_gpu = False
#    booster = model.get_booster()
#    config = booster.save_config()

#    if "gpu" in config.lower():
#        trained_on_gpu = True

#    if trained_on_gpu:
#        return model.predict(cp.array(X)).astype(float)

    return model.predict(X).astype(float)

####################################################################

def load_training_dataset_1(file):
    dataset = pd.read_csv(file)

    X = dataset[CORE_COLUMNS_FOR_TRAINING]
    y = dataset['rate_after']

    return X,y

def load_training_dataset_2(file):
    dataset = pd.read_csv(file)

    X = dataset[[*CORE_COLUMNS_FOR_TRAINING, 'rate_after', 'v_chng_after']]
    y = dataset['rate_after_p_1d']

    return X,y

def load_training_data_1(file):
    X_ds, y_ds = load_training_dataset_1(file)
    return X_ds.values,y_ds.values

def load_training_data_2(file):
    X_ds, y_ds = load_training_dataset_2(file)
    return X_ds.values,y_ds.values
