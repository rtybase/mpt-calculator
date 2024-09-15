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

load_yf "Vanguard S&P 500 UCITS ETF" "VUSA.L" "Vanguard S&P 500 UCITS ETF" "v-sp500-etf.csv"
load_yf "Vanguard FTSE 100 UCITS ETF" "VUKE.L" "Vanguard FTSE 100 UCITS ETF" "v-ftse100-etf.csv"
load_yf "Vanguard FTSE 250 UCITS ETF" "VMID.L" "Vanguard FTSE 250 UCITS ETF" "v-ftse250-etf.csv"
load_yf "Vanguard ESG Global All Cap UCITS ETF" "V3AM.L" "Vanguard ESG Global All Cap UCITS ETF" "v-esg-global-all-c-etf.csv"
load_yf "Vanguard EUR Corporate Bond UCITS ETF" "VECP.L" "Vanguard EUR Corporate Bond UCITS ETF" "v-eur-c-bond-c-etf.csv"
load_yf "Vanguard U.K. Gilt UCITS ETF" "VGOV.L" "Vanguard U.K. Gilt UCITS ETF" "v-uk-gilt-etf.csv"
load_yf "Vanguard EUR Eurozone Government Bond UCITS ETF" "VETY.L" "Vanguard EUR Eurozone Government Bond UCITS ETF" "v-eur-gov-bond-etf.csv"
load_yf "Vanguard USD Treasury Bond UCITS ETF" "VUTY.L" "Vanguard USD Treasury Bond UCITS ETF" "v-usd-tr-bond-etf.csv"
load_yf "Vanguard FTSE Japan UCITS ETF" "VJPN.L" "Vanguard FTSE Japan UCITS ETF" "v-ftse-japan-etf.csv"
load_yf "Vanguard Global Aggregate Bond UCITS ETF" "VAGP.L" "Vanguard Global Aggregate Bond UCITS ETF" "v-glb-aggr-bond-etf.csv"
load_yf "Vanguard USD Corporate 1-3 Year Bond UCITS ETF" "VUSC.L" "Vanguard USD Corporate 1-3 Year Bond UCITS ETF" "v-usd-c13y-bond-etf.csv"
load_yf "Vanguard USD Corporate Bond UCITS ETF" "VUCP.L" "Vanguard USD Corporate Bond UCITS ETF" "v-usd-c-bond-etf.csv"
load_yf "Vanguard USD Emerging Markets Government Bond UCITS ETF" "VEMT.L" "Vanguard USD Emerging Markets Government Bond UCITS ETF" "v-usd-em-m-gov-bond-etf.csv"
load_yf "Vanguard FTSE All-World High Dividend Yield UCITS ETF" "VHYL.L" "Vanguard FTSE All-World High Dividend Yield UCITS ETF" "v-ftse-aw-hdy-etf.csv"
load_yf "Vanguard FTSE All-World UCITS ETF" "VWRL.L" "Vanguard FTSE All-World UCITS ETF" "v-ftse-aw-etf.csv"
load_yf "Vanguard FTSE Developed Asia Pacific ex Japan UCITS ETF" "VAPX.L" "Vanguard FTSE Developed Asia Pacific ex Japan UCITS ETF" "v-ftse-dap-ej-etf.csv"
load_yf "Vanguard FTSE Developed Europe ex UK UCITS ETF" "VERX.L" "Vanguard FTSE Developed Europe ex UK UCITS ETF" "v-ftse-de-euk-etf.csv"
load_yf "Vanguard FTSE Developed Europe UCITS ETF" "VEUR.L" "Vanguard FTSE Developed Europe UCITS ETF" "v-ftse-de-etf.csv"
load_yf "Vanguard FTSE Developed World UCITS ETF" "VEVE.L" "Vanguard FTSE Developed World UCITS ETF" "v-ftse-dw-etf.csv"
load_yf "Vanguard FTSE Emerging Markets UCITS ETF" "VFEM.L" "Vanguard FTSE Emerging Markets UCITS ETF" "v-ftse-ems-etf.csv"
load_yf "Vanguard FTSE North America UCITS ETF" "VNRT.L" "Vanguard FTSE North America UCITS ETF" "v-ftse-na-etf.csv"

java -jar portfolio-0.0.1-SNAPSHOT.jar LoadCsvToDbTask "-file=${FOLDER_FOR_FILES}"
rm -rf ${FOLDER_FOR_FILES}

java -Xmx512m -jar portfolio-0.0.1-SNAPSHOT.jar CalculateAssetStatsTask
java -Xmx512m -jar portfolio-0.0.1-SNAPSHOT.jar Calculate2AssetsPortfolioStatsTask
