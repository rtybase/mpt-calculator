import requests
import csv
import sys

API_KEY = "YOUR_API_KEY_HERE"
BASE_URL = "https://www.alphavantage.co/query"

def fetch_av(function, symbol):
    """Fetch Alpha Vantage JSON for a given function."""
    params = {
        "function": function,
        "symbol": symbol,
        "apikey": API_KEY
    }
    r = requests.get(BASE_URL, params=params)
    r.raise_for_status()
    return r.json()

def extract_quarterly(data, key="quarterlyReports"):
    """Extract quarterly reports safely."""
    return data.get(key, [])

def merge_by_fiscal_date(cashflow, shares, balance):
    """Merge three datasets by fiscalDateEnding."""
    merged = {}

    # Cashflow
    for row in cashflow:
        date = row.get("fiscalDateEnding")
        if not date:
            continue
        merged.setdefault(date, {})
        merged[date]["Net Cash Flow-Operating"] = row.get("operatingCashflow")
        merged[date]["Capital Expenditures"] = row.get("capitalExpenditures")

    # Shares Outstanding
    for row in shares:
        date = row.get("date")
        if not date:
            continue
        merged.setdefault(date, {})
        merged[date]["Share Issued"] = row.get("shares_outstanding_basic")

    # Balance Sheet
    for row in balance:
        date = row.get("fiscalDateEnding")
        if not date:
            continue
        merged.setdefault(date, {})
        merged[date]["Total Assets"] = row.get("totalAssets")
        merged[date]["Total Current Assets"] = row.get("totalCurrentAssets")
        merged[date]["Total Liabilities"] = row.get("totalLiabilities")
        merged[date]["Total Current Liabilities"] = row.get("totalCurrentLiabilities")
        merged[date]["Total Equity"] = row.get("totalShareholderEquity")

    return merged


if len(sys.argv) < 2:
    print("Usage: python script.py SYMBOL")
    sys.exit(1)

symbol = sys.argv[1].upper()

#print(f"Fetching Alpha Vantage data for {symbol}...")

cashflow_data = fetch_av("CASH_FLOW", symbol)
shares_data = fetch_av("SHARES_OUTSTANDING", symbol)
balance_data = fetch_av("BALANCE_SHEET", symbol)

cashflow_q = extract_quarterly(cashflow_data)
shares_q = extract_quarterly(shares_data, "data")
balance_q = extract_quarterly(balance_data)

merged = merge_by_fiscal_date(cashflow_q, shares_q, balance_q)

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
    row["Quarterly Ending:"] = date
    writer.writerow(row)
