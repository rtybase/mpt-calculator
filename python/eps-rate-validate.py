import sys
import pandas as pd
import joblib
import numpy
from sklearn.metrics import r2_score

MAX_DEGREE = 16
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

def predict_l(X):
    model = load_linear_model()
    return model.predict(X)

def predict_p(degree, X):
    l,p = load_polynomial_model(degree)
    X_poly = p.transform(X)
    return l.predict(X_poly)

def compute_matches(y_actual, y_predicted):
    return numpy.linalg.norm(y_actual - y_predicted)

if len(sys.argv) > 1:
    file = sys.argv[1]
    dataset = pd.read_csv(file)
    X = dataset[['asset_id','prev_pred_eps','prev_eps','pred_eps','eps','prev_rate','rate']].values
    y_actual = dataset['next_rate'].values

    print("linear prediction=%s" % (compute_matches(y_actual, predict_l(X))))
    for degree in range(2, MAX_DEGREE + 1):
        print("---------------------------")

        y_predicted = predict_p(degree, X)
        r2 = r2_score(y_actual, y_predicted)

        print("polynomial d=%s distance=%s" % (degree,\
            compute_matches(y_actual, y_predicted)))
        print("polynomial d=%s r2=%s" % (degree, r2))

else:
    print("Specify the file with data for prediction!")
