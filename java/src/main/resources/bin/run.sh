#!/bin/bash
set -o pipefail
set -ue

FOLDER_FOR_FILES="./files_to_load"

load_yf () {
	echo "---------------------------------------------------"
	echo "Yahoo data for: $1"
	./ParseTable.exe "-link=https://finance.yahoo.com/quote/$2/history/" "-format=CSV"
	java -jar portfolio-0.0.1-SNAPSHOT.jar TransformSeriesDataTask "-file=out.csv" "-out_symbol=$3" "-outfile=${FOLDER_FOR_FILES}/$4" "-date_value_index=0" "-price_value_index=4" "-date_format=MMM d, yyyy"
	rm -rf out.csv
	rm -rf request.tmp
}

mkdir -p ${FOLDER_FOR_FILES}

echo "---------------------------------------------------"
echo "Standard Life data"
java -jar portfolio-0.0.1-SNAPSHOT.jar DownloadTask "-url=https://secure.standardlife.co.uk/secure/fundfilter/rest/results/funds/GROUP_PENSIONS/GPP/null/existingcustomer?_=1601483195897" -outfile=std-life.json
java -jar portfolio-0.0.1-SNAPSHOT.jar TransformStdLifeJsonDataTask "-file=std-life.json" "-outfile=${FOLDER_FOR_FILES}/std-life.csv"
rm -rf std-life.json

echo "---------------------------------------------------"
echo "European Central Bank data"
java -jar portfolio-0.0.1-SNAPSHOT.jar DownloadTask "-url=http://www.ecb.europa.eu/stats/eurofxref/eurofxref-hist-90d.xml" -outfile=ecb_rates.xml
java -jar portfolio-0.0.1-SNAPSHOT.jar TransformEcbRatesTask "-file=ecb_rates.xml" "-outfile=${FOLDER_FOR_FILES}/ecb.csv"
rm -rf ecb_rates.xml

load_yf "GBP/USD" "GBPUSD%3DX" "GBP/USD" "gbp.csv"
load_yf "Gold" "GC%3DF" "GOLD" "gold.csv"

load_yf "FTSE100" "%5EFTSE" "FTSE100" "ftse100.csv"
load_yf "S&P500" "%5EGSPC" "S&P500" "sp500.csv"
load_yf "NASDAQ100" "%5ENDX" "NASDAQ100" "nasdaq.csv"
load_yf "CAC40" "%5EFCHI" "CAC40" "cac40.csv"

load_yf "Microsoft" "MSFT" "Microsoft" "microsof.csv"
load_yf "Iron Mountain" "IRM" "Iron Mountain" "irm.csv"
load_yf "BlackRock" "BLK" "BlackRock" "blk.csv"
load_yf "Coca-Cola" "KO" "Coca-Cola" "ko.csv"
load_yf "McDonalds" "MCD" "McDonalds" "mcd.csv"
load_yf "AT&T" "T" "AT&T" "t.csv"
load_yf "Jamf Holding" "JAMF" "Jamf Holding" "jamf.csv"
load_yf "Apple" "AAPL" "Apple" "aapl.csv"
load_yf "Tesla" "TSLA" "Tesla" "tsla.csv"
load_yf "Intel" "INTC" "Intel" "intc.csv"
load_yf "AMD" "AMD" "AMD" "amd.csv"
load_yf "Snap" "SNAP" "Snap" "snap.csv"
load_yf "Cloudflare" "NET" "Cloudflare" "net.csv"
load_yf "Fortinet" "FTNT" "Fortinet" "ftnt.csv"
load_yf "Nintendo" "NTDOF" "Nintendo" "ntdof.csv"
load_yf "Zoom" "ZM" "Zoom" "zm.csv"
load_yf "Atlassian" "TEAM" "Atlassian" "team.csv"
load_yf "Ebay" "EBAY" "Ebay" "ebay.csv"
load_yf "Cisco" "CSCO" "Cisco" "csco.csv"
load_yf "Netflix" "NFLX" "Netflix" "nflx.csv"
load_yf "PayPal" "PYPL" "PayPal" "pypl.csv"
load_yf "NVIDIA" "NVDA" "NVIDIA" "nvda.csv"
load_yf "Zscaler" "ZS" "Zscaler" "zs.csv"
load_yf "Facebook" "META" "Facebook" "meta.csv"
load_yf "Alphabet" "GOOG" "Alphabet" "goog.csv"
load_yf "H&R Block" "HRB" "H&R Block" "hrb.csv"

load_yf "HSBC" "HSBA.L" "HSBC" "hsbc.csv"
load_yf "Standard Life Aberdeen" "ABDN.L" "Standard Life Aberdeen" "abdn-l.csv"
load_yf "Persimmon" "PSN.L" "Persimmon" "psn-l.csv"
load_yf "Aviva" "AV.L" "Aviva" "av-l.csv"
load_yf "BT Group" "BT-A.L" "BT Group" "bt-a-l.csv"
load_yf "Deliveroo" "ROO.L" "Deliveroo" "roo-l.csv"
load_yf "Unilever" "ULVR.L" "Unilever" "ulvr-l.csv"
load_yf "Lloyds Banking" "LLOY.L" "Lloyds Banking" "lloy-l.csv"
load_yf "Rio Tinto" "RIO.L" "Rio Tinto" "rio-l.csv"
load_yf "BP" "BP.L" "BP" "bp-l.csv"
load_yf "National Grid" "NG.L" "National Grid" "ng-l.csv"
load_yf "Legal & General" "LGEN.L" "Legal & General" "lgen-l.csv"

load_yf "BITCOIN" "BTC-USD" "BITCOIN" "btc-usd.csv"
load_yf "DOGECOIN" "DOGE-USD" "DOGECOIN" "doge-usd.csv"
load_yf "LITECOIN" "LTC-USD" "LITECOIN" "ltc-usd.csv"
load_yf "CHAINLINK-COIN" "LINK-USD" "CHAINLINK-COIN" "link-usd.csv"
load_yf "CARDANO-COIN" "ADA-USD" "CARDANO-COIN" "ada-usd.csv"
load_yf "ETHEREUM-COIN" "ETH-USD" "ETHEREUM-COIN" "eth-usd.csv"

java -jar portfolio-0.0.1-SNAPSHOT.jar LoadCsvToDbTask "-file=${FOLDER_FOR_FILES}"
rm -rf ${FOLDER_FOR_FILES}

java -Xmx512m -jar portfolio-0.0.1-SNAPSHOT.jar CalculateAssetStatsTask
java -Xmx512m -jar portfolio-0.0.1-SNAPSHOT.jar Calculate2AssetsPortfolioStatsTask
