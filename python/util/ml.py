import pandas as pd
import joblib

from sklearn.linear_model import LinearRegression
from sklearn.preprocessing import PolynomialFeatures
from sklearn.tree import DecisionTreeRegressor

MAX_DEGREE = 7
MIN_DEPTH = 15
MAX_DEPTH = 40
MODELS = {}

CORE_COLUMNS_FOR_TRAINING = ['sector','industry','month','prev_pred_eps','prev_eps','pred_eps','eps','prev_2d_rate','prev_rate','rate']

LINEAR_MODEL_TEMPLATE = "ds{0}-m-linear"
D_TREE_MODEL_TEMPLATE = "ds{0}-m-dtr-{1}"
POLY_P_MODEL_TEMPLATE = "ds{0}-m-polynomial-{1}-p"
POLY_L_MODEL_TEMPLATE = "ds{0}-m-polynomial-{1}-l"

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

def train_linear_model(X, y):
    linear_model = LinearRegression()
    linear_model.fit(X,y)

    return linear_model

def train_decision_tree_model(X, y, depth):
    dtr_model = DecisionTreeRegressor(max_depth=depth)
    dtr_model.fit(X,y)

    return dtr_model

def train_polynomial_model(X, y, degree):
    polynomial_regression = PolynomialFeatures(degree=degree)
    X_poly = polynomial_regression.fit_transform(X)

    polynomial_model = LinearRegression()
    polynomial_model.fit(X_poly, y)

    return polynomial_model, polynomial_regression

def train_and_save_linear(X, y, dataset_index):
    model_name = LINEAR_MODEL_TEMPLATE.format(dataset_index)

    model = train_linear_model(X, y)
    save_model(model_name, model)

def train_and_save_polynomial(X, y, degree, dataset_index):
    l,p = train_polynomial_model(X, y, degree)

    l_model_name = POLY_L_MODEL_TEMPLATE.format(dataset_index, degree)
    p_model_name = POLY_P_MODEL_TEMPLATE.format(dataset_index, degree)

    save_model(l_model_name, l)
    save_model(p_model_name, p)

def train_and_save_dtr(X, y, depth, dataset_index):
    model = train_decision_tree_model(X, y, depth)
    model_name = D_TREE_MODEL_TEMPLATE.format(dataset_index, depth)
    save_model(model_name, model)

def load_linear_model(dataset_index):
    model_name = LINEAR_MODEL_TEMPLATE.format(dataset_index)
    return load_model(model_name)

def load_polynomial_model(degree, dataset_index):
    l_model_name = POLY_L_MODEL_TEMPLATE.format(dataset_index, degree)
    p_model_name = POLY_P_MODEL_TEMPLATE.format(dataset_index, degree)

    l = load_model(l_model_name)
    p = load_model(p_model_name)
    return l,p

def load_dtr_model(depth, dataset_index):
    model_name = D_TREE_MODEL_TEMPLATE.format(dataset_index, depth)
    return load_model(model_name)

def predict_l(X, dataset_index):
    model = load_linear_model(dataset_index)
    return model.predict(X)

def predict_p(degree, X, dataset_index):
    l,p = load_polynomial_model(degree, dataset_index)
    X_poly = p.transform(X)
    return l.predict(X_poly)

def predict_dtr(depth, X, dataset_index):
    model = load_dtr_model(depth, dataset_index)
    return model.predict(X)

def load_training_data_1_day_after_eps(file):
    dataset = pd.read_csv(file)

    X = dataset[CORE_COLUMNS_FOR_TRAINING].values
    y = dataset['next_rate'].values

    return X,y

def load_training_data_2_days_after_eps(file):
    dataset = pd.read_csv(file)

    X = dataset[[*CORE_COLUMNS_FOR_TRAINING, 'next_rate']].values
    y = dataset['next_2d_rate'].values

    return X,y
