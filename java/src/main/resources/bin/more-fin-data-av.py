import requests
import csv
import json
import os
import sys
import time
from datetime import datetime

CACHE_DIR = "temp"
API_KEY = "YOUR_API_KEY_HERE"
BASE_URL = "https://www.alphavantage.co/query"

def cache_filename(function, symbol):
    symbol = symbol.upper()
    os.makedirs(CACHE_DIR, exist_ok=True)
    return os.path.join(CACHE_DIR, f"{symbol}_{function}.json")

def fetch_av(function, symbol):
    """
    Fetch Alpha Vantage data.

    1. Check cache first.
    2. If not cached, call Alpha Vantage.
    3. Fail fast if quota/rate limit is hit.
    4. Cache successful responses.
    """

    cache_file = cache_filename(function, symbol)

    # Cache hit
    if os.path.exists(cache_file):
        with open(cache_file, "r", encoding="utf-8") as f:
            return json.load(f)

    params = {
        "function": function,
        "symbol": symbol,
        "apikey": API_KEY
    }

    response = requests.get(BASE_URL, params=params)

    try:
        response.raise_for_status()
        data = response.json()
    finally:
        response.close()

    #
    # Fail fast on Alpha Vantage quota/rate limiting
    #
    if "Note" in data:
        print(f"ERROR: Alpha Vantage quota/rate limit hit:\n{data['Note']}")
        sys.exit(1)

    if "Information" in data:
        print(f"ERROR: Alpha Vantage returned:\n{data['Information']}")
        sys.exit(1)

    if "Error Message" in data:
        print(f"ERROR: Alpha Vantage returned:\n{data['Error Message']}")
        sys.exit(1)

    # Cache successful response
    with open(cache_file, "w", encoding="utf-8") as f:
        json.dump(data, f, indent=2)

    time.sleep(12)
    return data

def extract_quarterly(data, key="quarterlyReports"):
    """Extract quarterly reports safely."""
    return data.get(key, [])

def format_date(date_str):
    """Convert YYYY-MM-DD  MM/dd/yyyy."""
    try:
        dt = datetime.strptime(date_str, "%Y-%m-%d")
        return dt.strftime("%m/%d/%Y")
    except Exception:
        return date_str  # fallback if unexpected format

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
balance_data = fetch_av("BALANCE_SHEET", symbol)
shares_data = fetch_av("SHARES_OUTSTANDING", symbol)

shares_q = extract_quarterly(shares_data, "data")
cashflow_q = extract_quarterly(cashflow_data)
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
    row["Quarterly Ending:"] = format_date(date)
    writer.writerow(row)
