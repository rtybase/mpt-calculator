import sys
from pathlib import Path
import json
import pandas as pd

def good_to_process(data):
    if "data" in data:
        if (data["data"] is not None) and ("revenueTable" in data["data"]):
            if "rows" in data["data"]["revenueTable"]:
                return True
    return False

def create_record(symbol, value):
    values = value.split(" ")
    values[0] = values[0].replace("$","").replace("(","-").replace(")","")
    if (len(values) > 1):
        values[1] = values[1].replace("(","").replace(")","")

    return [symbol, *values]

def print_data_as_csv(lst):
    df = pd.DataFrame(lst)
    df.to_csv(sys.stdout, index=False, header=False, encoding='utf-8')

if len(sys.argv) > 2:
    p = Path(sys.argv[1])
    symbol = sys.argv[2]

    with p.open('r', encoding='utf-8') as f:
        data = json.loads(f.read())

    records = []
    if good_to_process(data):
        #print(data["data"]["revenueTable"]["rows"])
        for r in data["data"]["revenueTable"]["rows"]:
            if r["value1"] == "EPS":
                #print(r)
                for pos in range(2, 5):
                    v_pos = f"value{pos}"
                    values = create_record(symbol, r[v_pos])
                    if len(values) > 2 and len(values[2]) > 0:
                        records.append(values)

    #print(records)
    print_data_as_csv(records)

else:
    print("Specify the file with data!")
