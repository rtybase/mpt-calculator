import sys
import pandas as pd
import joblib

from sklearn.linear_model import LinearRegression
from sklearn.preprocessing import PolynomialFeatures

MAX_DEGREE = 11

def train_linear_model(X, y):
    linear_model = LinearRegression()
    linear_model.fit(X,y)

    return linear_model

def train_polynomial_model(X, y, degree):
    polynomial_regression = PolynomialFeatures(degree=degree)
    X_poly = polynomial_regression.fit_transform(X)

    polynomial_model = LinearRegression()
    polynomial_model.fit(X_poly, y)

    return polynomial_model, polynomial_regression

def train_and_save_polynomial(X, y, degree):
    l,p = train_polynomial_model(X, y, degree)

    l_file = "models/m-polynomial-{0}-l.model".format(degree)
    p_file = "models/m-polynomial-{0}-p.model".format(degree)

    joblib.dump(l, l_file)
    joblib.dump(p, p_file)

if len(sys.argv) > 1:
    file = sys.argv[1]
    dataset = pd.read_csv(file)
    X = dataset[['sector','month','prev_pred_eps','prev_eps','pred_eps','eps','prev_rate','rate']].values
    y = dataset['next_rate'].values

    linear_model = train_linear_model(X, y)
    joblib.dump(linear_model, 'models/m-linear.model')

    for degree in range(2, MAX_DEGREE + 1):
        train_and_save_polynomial(X, y, degree)

else:
    print("Specify the file with training data!")

