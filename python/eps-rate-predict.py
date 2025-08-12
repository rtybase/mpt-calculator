import sys
import pandas as pd
import joblib
import numpy

MAX_DEGREE = 11
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


numpy.set_printoptions(suppress = True, threshold = sys.maxsize)

if len(sys.argv) > 1:
    file = sys.argv[1]
    dataset = pd.read_csv(file)
    X = dataset[['sector','industry','month','prev_pred_eps','prev_eps','pred_eps','eps','prev_rate','rate']].values

    print("linear prediction=%s" % (predict_l(X)))
    for degree in range(2, MAX_DEGREE + 1):
        print("polynomial d=%s prediction=%s" % (degree, predict_p(degree, X)))

else:
    print("Specify the file with data for prediction!")
