import sys
import joblib
import util.ml

from sklearn.linear_model import LinearRegression
from sklearn.preprocessing import PolynomialFeatures
from sklearn.tree import DecisionTreeRegressor

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

def train_and_save_polynomial(X, y, degree):
    l,p = train_polynomial_model(X, y, degree)

    l_file = "models/m-polynomial-{0}-l.model".format(degree)
    p_file = "models/m-polynomial-{0}-p.model".format(degree)

    joblib.dump(l, l_file)
    joblib.dump(p, p_file)

def train_and_save_dtr(X, y, depth):
    m = train_decision_tree_model(X, y, depth)
    m_file = "models/m-dtr-{0}.model".format(depth)
    joblib.dump(m, m_file)

if len(sys.argv) > 1:
    X,y = util.ml.load_training_data(sys.argv[1])

    linear_model = train_linear_model(X, y)
    joblib.dump(linear_model, 'models/m-linear.model')

    for degree in range(2, util.ml.MAX_DEGREE + 1):
        train_and_save_polynomial(X, y, degree)

    for depth in range(2, util.ml.MAX_DEPTH + 1):
        train_and_save_dtr(X, y, depth)

else:
    print("Specify the file with training data!")

