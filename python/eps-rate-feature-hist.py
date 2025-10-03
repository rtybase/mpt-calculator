import sys
import util.ml
import numpy
import pandas as pd
import matplotlib.pyplot as plt

def plot_with(X_ds, columns):
    X_ds.hist(bins=500, column=columns, figsize=(65,25), color='skyblue', edgecolor='black')
    plt.show()

numpy.set_printoptions(suppress=True, threshold=sys.maxsize, linewidth=500)

print(f"Looking for the best params from DS=2 with {util.ml.DS2_FILE}", flush=True)

dataset = pd.read_csv(util.ml.DS2_FILE)

ALL = dataset[[*util.ml.CORE_COLUMNS_FOR_TRAINING,\
    'rate_after', 'v_chng_after', 'rate_after_p_1d', 'v_chng_after_p_1d']]

X = ALL
#X = ALL.query('eps_spr >= 30')
#X = ALL.query('ngaap_eps_spr >= 100')

#Y = X[['pred_eps','eps', 'ngaap_pred_eps', 'ngaap_eps',\
#    'rate_before_m_1d', 'rate_before', 'rate_after', 'rate_after_p_1d']]

all_columns = X.columns
n = 6
group_of_columns = [all_columns[i:i + n] for i in range(0, len(all_columns), n)]

#print(Y.corr(method='pearson'), flush=True)

for columns in group_of_columns:
    plot_with(X, columns)

#columns_to_compare = ['rate_before', 'rate_after', 'rate_after_p_1d']

#for column in columns_to_compare:
#    for start_pos in range(0, 101):
#        qry1 = f"eps_spr >= {start_pos}"
#        X = ALL.query(qry1)
#        total_rows = len(X.index)

#        qry2 = f"{column} >= 0"
#        P = X.query(qry2)
#        rows = len(P.index)

#        distr = rows / total_rows
#        print(f"for '{qry1}' and '{qry2}' value is: {distr}")




