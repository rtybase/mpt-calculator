import csv
import json
import os
import sys
import time
from datetime import datetime, timedelta

CACHE_DIR = "temp"

def error(msg):
    print(msg, file=sys.stderr)
    sys.exit(1)

def filename(function, symbol):
    symbol = symbol.lower()
    return os.path.join(CACHE_DIR, f"{symbol}-{function}.json")

def load_s_al(function, symbol):
    file_name = filename(function, symbol)

    if os.path.exists(file_name):
        with open(file_name, "r", encoding="utf-8") as f:
            return json.load(f)

    error(f"File {file_name} doesn't exist!")

def extract_from(data, check_attribute, check_value, return_attribute):
    for record in data:
        if (check_attribute in record) and (return_attribute in record):
            if record[check_attribute] == check_value:
                return record[return_attribute]

    return []

def format_date(date_str):
    try:
        dt = datetime.strptime(date_str, "%Y-%m-%d")
        return dt.strftime("%m/%d/%Y")
    except Exception:
        return date_str  # fallback if unexpected format

def key_date(date_str):
    try:
        dt = datetime.strptime(date_str, "%b %Y")

        if dt.month == 12:
            next_month = dt.replace(year=dt.year + 1, month=1)
        else:
            next_month = dt.replace(month=dt.month + 1)

        last_day = next_month - timedelta(days=1)
        return last_day.strftime("%Y-%m-%d")
    except Exception:
        return date_str  # fallback if unexpected format

def set_values_from(merged_data, specific_data, column):
    for row in specific_data:
        if "raw_value" not in row:
            continue

        date = row.get("name")
        if not date:
            continue

        k_date = key_date(date)
        merged_data.setdefault(k_date, {})
        merged_data[k_date][column] = row.get("raw_value")

def merge_by_fiscal_date(shares_outstanding, net_cash_flow, cap_ex,
    total_current_assets, total_assets,
    total_current_liabilities, total_liabilities,
    total_equity):

    merged = {}

    set_values_from(merged, shares_outstanding, "Share Issued")
    set_values_from(merged, net_cash_flow, "Net Cash Flow-Operating")
    set_values_from(merged, cap_ex, "Capital Expenditures")
    set_values_from(merged, total_current_assets, "Total Current Assets")
    set_values_from(merged, total_assets, "Total Assets")
    set_values_from(merged, total_current_liabilities, "Total Current Liabilities")
    set_values_from(merged, total_liabilities, "Total Liabilities")
    set_values_from(merged, total_equity, "Total Equity")

    return merged


if len(sys.argv) < 2:
    error("Usage: python script.py SYMBOL")

symbol = sys.argv[1].upper()

cashflow_data = load_s_al("s-al-cs", symbol)
balance_data = load_s_al("s-al-bs", symbol)
shares_data = load_s_al("s-al-is", symbol)

supplemental_items = extract_from(shares_data, "title", "Supplemental Items", "rows")
shares_outstanding = extract_from(supplemental_items, "name", "weighted_average_diluted_shares_outstanding", "cells")
if len(shares_outstanding) == 0:
    shares_outstanding = extract_from(supplemental_items, "name", "weighted_average_basic_shares_outstanding", "cells")

cash_flow_op = extract_from(cashflow_data, "title", "Cash Flow From Operating Activities", "rows")
net_cash_flow = extract_from(cash_flow_op, "name", "nocf", "cells")

cash_flow_in = extract_from(cashflow_data, "title", "Cash Flow From Investing Activities", "rows")
cap_ex = extract_from(cash_flow_in, "name", "capital_expenditures", "cells")

current_assets = extract_from(balance_data, "title", "Current Assets", "rows")
total_current_assets = extract_from(current_assets, "name", "total_current_assets", "cells")

long_term_assets = extract_from(balance_data, "title", "Long-Term Assets", "rows")
total_assets = extract_from(long_term_assets, "name", "total_assets", "cells")

current_liabilities = extract_from(balance_data, "title", "Current Liabilities", "rows")
total_current_liabilities = extract_from(current_liabilities, "name", "total_current_liabilities", "cells")

long_term_liabilities = extract_from(balance_data, "title", "Long-Term Liabilities", "rows")
total_liabilities = extract_from(long_term_liabilities, "name", "total_liabilities", "cells")

equity = extract_from(balance_data, "title", "Stockholders' Equity", "rows")
total_equity = extract_from(equity, "name", "total_equity", "cells")


merged = merge_by_fiscal_date(shares_outstanding, net_cash_flow, cap_ex,\
    total_current_assets, total_assets,\
    total_current_liabilities, total_liabilities,\
    total_equity)

# CSV output
fieldnames = [
    "Symbol",
    "Quarterly Ending:",
    "Net Cash Flow-Operating",
    "Capital Expenditures",
    "Share Issued",
    "Total Assets",
    "Total Current Assets",
    "Total Liabilities",
    "Total Current Liabilities",
    "Total Equity"
]

writer = csv.DictWriter(sys.stdout, fieldnames=fieldnames)
writer.writeheader()

for date in sorted(merged.keys(), reverse=True):
    row = merged[date]
    row["Symbol"] = symbol
    row["Quarterly Ending:"] = format_date(date)
    writer.writerow(row)
