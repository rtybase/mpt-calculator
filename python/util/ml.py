import pandas as pd
import joblib

MAX_DEGREE = 8
MAX_DEPTH = 40
MODELS = {}

def load_model_from(file_name, model_key):
    if model_key in MODELS:
        return MODELS[model_key]

    model = joblib.load(file_name)
    MODELS[model_key] = model
    return model

def load_linear_model():
    return load_model_from("models/m-linear.model", "linear")

def load_polynomial_model(degree):
    l_model_name = "m-polynomial-{0}-l".format(degree)
    p_model_name = "m-polynomial-{0}-p".format(degree)

    l_file = "models/{0}.model".format(l_model_name)
    p_file = "models/{0}.model".format(p_model_name)

    l = load_model_from(l_file, l_model_name)
    p = load_model_from(p_file, p_model_name)
    return l,p

def load_dtr_model(depth):
    model_name = "m-dtr-{0}".format(depth)
    m_file = "models/{0}.model".format(model_name)
    return load_model_from(m_file, model_name)

def predict_l(X):
    model = load_linear_model()
    return model.predict(X)

def predict_p(degree, X):
    l,p = load_polynomial_model(degree)
    X_poly = p.transform(X)
    return l.predict(X_poly)

def predict_dtr(depth, X):
    model = load_dtr_model(depth)
    return model.predict(X)

def load_training_data(file):
    dataset = pd.read_csv(file)

    X = dataset[['sector','industry','month','prev_pred_eps','prev_eps','pred_eps','eps','prev_2d_rate','prev_rate','rate','next_rate']].values
    y = dataset['next_2d_rate'].values

    return X,y