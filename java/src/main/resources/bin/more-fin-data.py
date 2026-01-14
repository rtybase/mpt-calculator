import requests
import sys
import pandas as pd
import util.flow

URL = "https://api.nasdaq.com/api/company/%s/financials?frequency=2"

HEADERS = {
    "User-Agent": "Mozilla/5.0",
    "Accept": "application/json",
    "Origin": "https://www.nasdaq.com",
    "Referer": "https://www.nasdaq.com/"
}

REPORT_VALUES = {
    "balanceSheetTable": ["Total Current Assets", "Total Current Liabilities",
                          "Total Liabilities", "Total Equity", "Total Assets"],
    "cashFlowTable": ["Net Cash Flow-Operating", "Capital Expenditures"]
}

def add_colums_with_data_from_dict(from_dict, to_list):
    if from_dict:
        index = 0
        for k in from_dict:
            if (index >= len(to_list)):
                to_list.append([])

            to_list[index].append(from_dict[k])
            index += 1

def add_colums_with_data_from_list(from_list, to_list, col_name):
    if from_list:
        for dct in from_list:
            if dct["value1"] == col_name:
                add_colums_with_data_from_dict(dct, to_list)

def load_fin_for(symbol, rows):
    final_url = URL % (symbol,)
    response = requests.get(final_url, headers=HEADERS)

    if response.status_code == 200:
        data = response.json()
        try:
            symbol_dct = {"value1": "Symbol", "value2": symbol, 
                          "value3": symbol, "value4": symbol, "value5": symbol}

            add_colums_with_data_from_dict(symbol_dct, rows)
            add_colums_with_data_from_dict(data['data']['balanceSheetTable']['headers'],\
                rows)

            for k in REPORT_VALUES:
                for v in REPORT_VALUES[k]:
                    add_colums_with_data_from_list(data['data'][k]['rows'],\
                        rows, v)

        except KeyError:
            print(data, flush=True)
            util.flow.error("Unexpected JSON structure!")
    else:
        util.flow.error(f"Request for {final_url} failed with status code {response.status_code}")


def print_data_as_csv(lst):
    df = pd.DataFrame(lst)
    df.to_csv(sys.stdout, index=False, header=False, encoding='utf-8')


if len(sys.argv) > 1:
    symbol = sys.argv[1]

    rows = []
    load_fin_for(symbol, rows)
    print_data_as_csv(rows)

else:
    util.flow.error("Specify the symbol!")
