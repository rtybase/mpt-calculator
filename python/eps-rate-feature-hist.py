import util.ml
import matplotlib.pyplot as plt

DS2_FILE = "inputs-ml/out-training-for-2d.csv"

def plot_with(X_ds, columns):
    X_ds.hist(bins=100, column=columns, figsize=(65,25), color='skyblue', edgecolor='black')
    plt.show()

print(f"Looking for the best params from DS=2 with {DS2_FILE}", flush=True)
X_ds, y_ds = util.ml.load_training_dataset_2_days_after_eps(DS2_FILE)

all_columns = X_ds.columns
n = 6
group_of_columns = [all_columns[i:i + n] for i in range(0, len(all_columns), n)]

for columns in group_of_columns:
    plot_with(X_ds, columns)
