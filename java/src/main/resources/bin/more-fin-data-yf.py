import sys
import yfinance as yf
import pandas as pd
import util.flow
import time

def val_if_aval(obj, what):
    result = None

    if what in obj:
        result = obj[what]
        if pd.isna(result):
            result = None
    
    return result

def add_empty_dict_to(dct, key):
    if key not in dct:
        dct[key] = {}

def current_Liabilities(dct):
    liabilities = val_if_aval(dct,"Current Liabilities")
    if liabilities is None:
        liabilities = val_if_aval(dct,"Current Debt And Capital Lease Obligation")

    return liabilities

def div_by(val, div):
    if val is not None:
        return val / div
    return val

def load_fin_data(symbol):
    result = {}
    fin_data = yf.Ticker(symbol)
#    print(vars(fin_data.quarterly_income_stmt))
#    print(vars(fin_data.quarterly_balance_sheet))
#    print(vars(fin_data.quarterly_cash_flow))

    for d in fin_data.quarterly_balance_sheet:
        add_empty_dict_to(result, d)

        result[d]["Symbol"] = symbol
        result[d]["Quarterly Ending:"] = d.strftime('%m/%d/%Y')

        val = val_if_aval(fin_data.quarterly_balance_sheet[d],"Current Assets")
        result[d]["Total Current Assets"] = div_by(val,1000)

        val = current_Liabilities(fin_data.quarterly_balance_sheet[d])
        result[d]["Total Current Liabilities"] = div_by(val,1000)

        val = val_if_aval(fin_data.quarterly_balance_sheet[d],"Total Liabilities Net Minority Interest")
        result[d]["Total Liabilities"] = div_by(val,1000)

        val = val_if_aval(fin_data.quarterly_balance_sheet[d],"Stockholders Equity")
        result[d]["Total Equity"] = div_by(val,1000)

        val = val_if_aval(fin_data.quarterly_balance_sheet[d],"Total Assets")
        result[d]["Total Assets"] = div_by(val,1000)

        val = val_if_aval(fin_data.quarterly_balance_sheet[d],"Share Issued")
        result[d]["Share Issued"] = val

        result[d]["Net Cash Flow-Operating"] = None
        result[d]["Capital Expenditures"] = None

    for d in fin_data.quarterly_cash_flow:
        add_empty_dict_to(result, d)

        result[d]["Symbol"] = symbol
        result[d]["Quarterly Ending:"] = d.strftime('%m/%d/%Y')

        val = val_if_aval(fin_data.quarterly_cash_flow[d],"Operating Cash Flow")
        result[d]["Net Cash Flow-Operating"] = div_by(val,1000)

        val = val_if_aval(fin_data.quarterly_cash_flow[d],"Capital Expenditure")
        result[d]["Capital Expenditures"] = div_by(val,1000)

    return result

def print_list_as_csv(lst):
    df = pd.DataFrame(lst)
    df.transpose().to_csv(sys.stdout, index=False, encoding='utf-8')

yf.config.network.retries = 2
yf.config.network.timeout = 10

if len(sys.argv) > 1:
    symbol = sys.argv[1]

    data = load_fin_data(symbol)
    print_list_as_csv(data)
    time.sleep(0.5)
else:
    util.flow.error("Specify the symbol!")
