#!/bin/bash
set -o pipefail
set -ue

FOLDER_FOR_PRICE_FILES="./data_to_load_prices"
FOLDER_FOR_DIVIDEND_FILES="./data_to_load_dividends"

load_yf () {
	echo "---------------------------------------------------"
	echo "Yahoo data for: $2"

	price_out_file="${FOLDER_FOR_PRICE_FILES}/$3"
	dividend_out_file="${FOLDER_FOR_DIVIDEND_FILES}/$3"

	if [ -f $price_out_file ]; then
		echo "${price_out_file} already exists."
	else 
		java -Duse-http2=true -jar portfolio-0.0.1-SNAPSHOT.jar DownloadTask "-url=https://finance.yahoo.com/quote/$1/history/" -outfile=out.html -headers=headers/yh-headers.prop
		./ParseTable.exe "-link=out.html" "-format=CSV"
		java -jar portfolio-0.0.1-SNAPSHOT.jar TransformSeriesDataTask "-file=out.csv" "-out_symbol=$2" "-outfile=${price_out_file}" "-date_value_index=0" "-price_value_index=4" "-date_format=MMM d, yyyy"
		java -jar portfolio-0.0.1-SNAPSHOT.jar TransformDividendsDataTask "-file=out.csv" "-out_symbol=$2" "-outfile=${dividend_out_file}" "-date_value_index=0" "-price_value_index=1" "-date_format=MMM d, yyyy"

		rm -rf out.html
		rm -rf out.csv
		rm -rf request.tmp
	fi
}

load_uk_in() {
	echo "---------------------------------------------------"
	echo "UK Investing data for: $2"
	./ParseTable.exe "-link=https://uk.investing.com/$1-historical-data" "-format=CSV"
	head -n 27 out.csv > out1.csv
	java -jar portfolio-0.0.1-SNAPSHOT.jar TransformSeriesDataTask "-file=out1.csv" "-out_symbol=$2" "-outfile=${FOLDER_FOR_PRICE_FILES}/$3" "-date_value_index=0" "-price_value_index=1" "-date_format=MMM d, yyyy"
	rm -rf out.csv
	rm -rf out1.csv
	rm -rf request.tmp
}

mkdir -p ${FOLDER_FOR_PRICE_FILES}
mkdir -p ${FOLDER_FOR_DIVIDEND_FILES}

echo "---------------------------------------------------"
echo "Standard Life data"
java -jar portfolio-0.0.1-SNAPSHOT.jar DownloadTask "-url=https://secure.standardlife.co.uk/secure/fundfilter/rest/results/funds/GROUP_PENSIONS/GPP/null/existingcustomer?_=1601483195897" -outfile=std-life.json
java -jar portfolio-0.0.1-SNAPSHOT.jar TransformStdLifeJsonDataTask "-file=std-life.json" "-outfile=${FOLDER_FOR_PRICE_FILES}/std-life.csv"
rm -rf std-life.json

echo "---------------------------------------------------"
echo "European Central Bank data"
curl "https://www.ecb.europa.eu/stats/eurofxref/eurofxref-hist-90d.xml" > ecb_rates.xml
java -jar portfolio-0.0.1-SNAPSHOT.jar TransformEcbRatesTask "-file=ecb_rates.xml" "-outfile=${FOLDER_FOR_PRICE_FILES}/ecb.csv"
rm -rf ecb_rates.xml

load_uk_in "indices/uk-100" "FTSE100" "ftse100-1.csv"
load_yf "%5EGSPC" "S&P500" "sp500.csv"
load_yf "%5EFCHI" "CAC40" "cac40.csv"
load_yf "%5ENDX" "NASDAQ100" "nasdaq.csv"

load_yf "GC%3DF" "GOLD" "gold.csv"
load_yf "SI%3DF" "SILVER" "silver.csv"
load_yf "HG%3DF" "COPPER" "copper.csv"
load_yf "PL%3DF" "PLATINUM" "platinum.csv"
load_yf "GBPUSD%3DX" "GBP/USD" "gbp.csv"

load_yf "TSLA" "Tesla" "tsla.csv"
load_yf "ZM" "Zoom" "zm.csv"
load_yf "IRM" "Iron Mountain" "irm.csv"
load_yf "BLK" "BlackRock" "blk.csv"
load_yf "JAMF" "Jamf Holding" "jamf.csv"
load_yf "NVDA" "NVIDIA" "nvda.csv"
load_yf "PLTR" "Palantir" "pltr.csv"
load_yf "AMZN" "Amazon" "amzn.csv"
load_yf "MSFT" "Microsoft" "msft.csv"
load_yf "KO" "Coca-Cola" "ko.csv"
load_yf "PEP" "Pepsi-Cola" "pep.csv"
load_yf "MCD" "McDonalds" "mcd.csv"
load_yf "T" "AT&T" "t.csv"
load_yf "AAPL" "Apple" "aapl.csv"
load_yf "INTC" "Intel" "intc.csv"
load_yf "AMD" "AMD" "amd.csv"
load_yf "SNAP" "Snap" "snap.csv"
load_yf "NET" "Cloudflare" "net.csv"
load_yf "FTNT" "Fortinet" "ftnt.csv"
load_yf "NTDOF" "Nintendo" "ntdof.csv"
load_yf "TEAM" "Atlassian" "team.csv"
load_yf "EBAY" "Ebay" "ebay.csv"
load_yf "CSCO" "Cisco" "csco.csv"
load_yf "NFLX" "Netflix" "nflx.csv"
load_yf "PYPL" "PayPal" "pypl.csv"
load_yf "ZS" "Zscaler" "zs.csv"
load_yf "META" "Facebook" "meta.csv"
load_yf "GOOG" "Alphabet" "goog.csv"
load_yf "HRB" "H&R Block" "hrb.csv"
load_yf "CAJFF" "Canon" "cajff.csv"
load_yf "HPQ" "HP" "hp.csv"
load_yf "EA" "Electronic Arts" "ea.csv"
load_yf "SNOW" "Snowflake" "snow.csv"
load_yf "COIN" "Coinbase" "coin.csv"
load_yf "WDAY" "Workday" "wday.csv"
load_yf "SPOT" "Spotify" "spot.csv"
load_yf "CRWD" "CrowdStrike" "crwd.csv"
load_yf "DELL" "Dell" "dell.csv"
load_yf "ABNB" "Airbnb" "abnb.csv"
load_yf "ORCL" "Oracle" "orcl.csv"
load_yf "SHOP" "Shopify" "shop.csv"
load_yf "PANW" "Palo Alto Networks" "panw.csv"
load_yf "ADI" "Analog Devices" "adi.csv"
load_yf "SONY" "Sony" "sony.csv"
load_yf "BKNG" "Booking Holdings" "bkng.csv"
load_yf "ANET" "Arista Networks" "anet.csv"
load_yf "UBER" "Uber" "uber.csv"
load_yf "NOW" "ServiceNow" "now.csv"
load_yf "TXN" "Texas Instruments" "txn.csv"
load_yf "QCOM" "QUALCOMM" "qcom.csv"
load_yf "IBM" "IBM" "ibm.csv"
load_yf "ADBE" "Adobe" "adbe.csv"
load_yf "CRM" "Salesforce" "crm.csv"
load_yf "005930.KS" "Samsung" "samsung.csv"
load_yf "AVGO" "Broadcom" "avgo.csv"
load_yf "HST" "Host Hotels & Resorts, Inc." "hst.csv"
load_yf "TROW" "T. Rowe Price Group, Inc." "trow.csv"
load_yf "PAA" "Plains All American Pipeline" "paa.csv"
load_yf "ARCC" "Ares Capital Corporation" "arcc.csv"
load_yf "JNJ" "Johnson & Johnson" "jnj.csv"
load_yf "ED" "Consolidated Edison" "ed.csv"
load_yf "NEE" "NextEra Energy" "nee.csv"
load_yf "NNN" "NNN REIT" "nnn.csv"
load_yf "VICI" "VICI Properties" "vici.csv"
load_yf "AVB" "AvalonBay Communities" "avb.csv"
load_yf "EPD" "Enterprise Products Partners" "epd.csv"
load_yf "ENB" "Enbridge" "enb.csv"
load_yf "NDAQ" "Nasdaq Inc" "ndaq.csv"
load_yf "VRSN" "VeriSign" "vrsn.csv"
load_yf "NWG" "NatWest Group" "nwg.csv"
load_yf "TMUS" "T-Mobile US" "tmus.csv"
load_yf "PFE" "Pfizer" "pfe.csv"
load_yf "AZN" "AstraZeneca" "azn.csv"
load_yf "NVO" "Novo Nordisk" "nvo.csv"

load_yf "HSBA.L" "HSBC" "hsbc.csv"
load_yf "ABDN.L" "Standard Life Aberdeen" "abdn-l.csv"
load_yf "AV.L" "Aviva" "av-l.csv"
load_yf "ROO.L" "Deliveroo" "roo-l.csv"
load_yf "LLOY.L" "Lloyds Banking" "lloy-l.csv"
load_yf "RIO.L" "Rio Tinto" "rio-l.csv"
load_yf "BP.L" "BP" "bp-l.csv"
load_yf "NG.L" "National Grid" "ng-l.csv"
load_yf "LGEN.L" "Legal & General" "lgen-l.csv"
load_yf "ULVR.L" "Unilever" "ulvr-l.csv"
load_yf "BT-A.L" "BT Group" "bt-a-l.csv"
load_yf "PSN.L" "Persimmon" "psn-l.csv"

load_yf "BTC-USD" "BITCOIN" "btc-usd.csv"
load_yf "DOGE-USD" "DOGECOIN" "doge-usd.csv"
load_yf "LTC-USD" "LITECOIN" "ltc-usd.csv"
load_yf "LINK-USD" "CHAINLINK-COIN" "link-usd.csv"
load_yf "ADA-USD" "CARDANO-COIN" "ada-usd.csv"
load_yf "ETH-USD" "ETHEREUM-COIN" "eth-usd.csv"

load_yf "VUSA.L" "Vanguard S&P 500 UCITS ETF" "v-sp500-etf.csv"
load_yf "VUKE.L" "Vanguard FTSE 100 UCITS ETF" "v-ftse100-etf.csv"
load_yf "VMID.L" "Vanguard FTSE 250 UCITS ETF" "v-ftse250-etf.csv"
load_yf "V3AM.L" "Vanguard ESG Global All Cap UCITS ETF" "v-esg-global-all-c-etf.csv"
load_yf "VECP.L" "Vanguard EUR Corporate Bond UCITS ETF" "v-eur-c-bond-c-etf.csv"
load_yf "VGOV.L" "Vanguard U.K. Gilt UCITS ETF" "v-uk-gilt-etf.csv"
load_yf "VETY.L" "Vanguard EUR Eurozone Government Bond UCITS ETF" "v-eur-gov-bond-etf.csv"
load_yf "VUTY.L" "Vanguard USD Treasury Bond UCITS ETF" "v-usd-tr-bond-etf.csv"
load_yf "VJPN.L" "Vanguard FTSE Japan UCITS ETF" "v-ftse-japan-etf.csv"
load_yf "VAGP.L" "Vanguard Global Aggregate Bond UCITS ETF" "v-glb-aggr-bond-etf.csv"
load_yf "VUSC.L" "Vanguard USD Corporate 1-3 Year Bond UCITS ETF" "v-usd-c13y-bond-etf.csv"
load_yf "VUCP.L" "Vanguard USD Corporate Bond UCITS ETF" "v-usd-c-bond-etf.csv"
load_yf "VEMT.L" "Vanguard USD Emerging Markets Government Bond UCITS ETF" "v-usd-em-m-gov-bond-etf.csv"
load_yf "VHYL.L" "Vanguard FTSE All-World High Dividend Yield UCITS ETF" "v-ftse-aw-hdy-etf.csv"
load_yf "VWRL.L" "Vanguard FTSE All-World UCITS ETF" "v-ftse-aw-etf.csv"
load_yf "VAPX.L" "Vanguard FTSE Developed Asia Pacific ex Japan UCITS ETF" "v-ftse-dap-ej-etf.csv"
load_yf "VERX.L" "Vanguard FTSE Developed Europe ex UK UCITS ETF" "v-ftse-de-euk-etf.csv"
load_yf "VEUR.L" "Vanguard FTSE Developed Europe UCITS ETF" "v-ftse-de-etf.csv"
load_yf "VEVE.L" "Vanguard FTSE Developed World UCITS ETF" "v-ftse-dw-etf.csv"
load_yf "VFEM.L" "Vanguard FTSE Emerging Markets UCITS ETF" "v-ftse-ems-etf.csv"
load_yf "VNRT.L" "Vanguard FTSE North America UCITS ETF" "v-ftse-na-etf.csv"
load_yf "TDIV.L" "VanEck Morningstar Developed Markets Dividend Leaders UCITS ETF" "tdiv-l.csv"
load_yf "FLXD.L" "Franklin European Quality Dividend UCITS ETF" "flxd-l.csv"
load_yf "IUKD.L" "iShares UK Dividend UCITS ETF" "iukd-l.csv"
load_yf "UKDV.L" "SPDR S&P UK Dividend Aristocrats UCITS ETF" "ukdv-l.csv"
load_yf "EMHD.L" "Invesco FTSE Emerging Markets High Dividend Low Volatility UCITS ETF" "emhd-l.csv"
load_yf "DEM" "WisdomTree Emerging Markets High Dividend Fund" "dem.csv"

load_yf "0P0001IL90.L" "Vanguard Active U.K. Equity Fund" "v-act-uk-ef.csv"
load_yf "0P0001IU23.F" "Vanguard Emerging Markets Bond Fund" "v-em-bf.csv"
load_yf "IE00B50MZ724.IR" "Vanguard Emerging Markets Stock Index Fund" "v-em-sif.csv"
load_yf "0P0000UGLG.L" "Vanguard ESG Developed World All Cap Equity Index Fund" "v-esg-dw-ac-eif.csv"
load_yf "IE00BFRTD722.IR" "Vanguard Euro Government Bond Index Fund" "v-eg-bif.csv"
load_yf "IE00B04FFJ44.IR" "Vanguard Euro Investment Grade Bond Index Fund" "v-eu-ig-bif.csv"
load_yf "0P00018XAP.L" "Vanguard FTSE 100 Index Unit Trust" "v-ftse100-iuf.csv"
load_yf "0P0000KSP8.L" "Vanguard FTSE Developed Europe ex-U.K. Equity Index Fund" "v-ftse-de-euk-eif.csv"
load_yf "0P0000KSP6.L" "Vanguard FTSE Developed World ex-U.K. Equity Index Fund" "v-ftse-dw-euk-eif.csv"
load_yf "0P00018XAR.L" "Vanguard FTSE Global All Cap Index Fund" "v-ftse-gac-if.csv"
load_yf "0P0000SAVS.L" "Vanguard FTSE U.K. All Share Index Unit Trust" "v-ftse-uk-as-iut.csv"
load_yf "0P0000KSP4.L" "Vanguard FTSE U.K. Equity Income Index Fund" "v-ftse-uk-ei-if.csv"
load_yf "0P0000KM24.L" "Vanguard Global Bond Index Fund" "v-gb-if.csv"
load_yf "0P0001C5T6.L" "Vanguard Global Corporate Bond Index Fund" "v-gcb-if.csv"
load_yf "0P0001BLQI.L" "Vanguard Global Credit Bond Fund" "v-gc-bf.csv"
load_yf "0P000185T1.L" "Vanguard Global Emerging Markets Fund" "v-g-emf.csv"
load_yf "0P000185T3.L" "Vanguard Global Equity Fund" "v-g-ef.csv"
load_yf "0P000185SZ.L" "Vanguard Global Equity Income Fund" "v-g-eif.csv"
load_yf "IE00BH65QG55.IR" "Vanguard Global Short-Term Bond Index Fund" "v-g-st-bif.csv"
load_yf "0P0001C5T4.L" "Vanguard Global Short-Term Corporate Bond Index Fund" "v-g-st-cbif.csv"
load_yf "0P00011WUE.L" "Vanguard Japan Government Bond Index Fund" "v-j-g-bif.csv"
load_yf "0P0000KM26.L" "Vanguard U.K. Government Bond Index Fund" "v-uk-g-bif.csv"
load_yf "0P00011WUD.L" "Vanguard U.S. Government Bond Index Fund" "v-us-g-bif.csv"
load_yf "IE00B76VTL96.IR" "Vanguard SRI European Stock Fund" "v-sri-esf.csv"
load_yf "0P0000KM1Y.L" "Vanguard Japan Stock Index Fund" "v-j-sif.csv"
load_yf "0P0000KSPA.L" "Vanguard U.S. Equity Index Fund" "v-us-eif.csv"
load_yf "0P0000N47O.L" "Vanguard Global Small-Cap Index Fund" "v-gsc-if.csv"
load_yf "0P0001O2BN.L" "Vanguard Global Sustainable Equity Fund" "v-gs-ef.csv"
load_yf "0P0000KM20.L" "Vanguard Pacific ex-Japan Stock Index Fund" "v-pej-sif.csv"
load_yf "0P0001I2A0.L" "Vanguard Sterling Short-Term Money Market Fund" "v-sst-mmf.csv"
load_yf "0P00011WUC.L" "Vanguard U.S. Investment Grade Credit Index Fund" "v-us-igc-if.csv"

load_yf "0P0001CSW3.L" "BlackRock ACS LifePath 2040-2042" "blk-acs-lp-40-42.csv"
load_yf "0P0001F2HD.L" "BlackRock ACS World ESG Equity Tracker Fund" "blk-acs-w-esg-etf.csv"
load_yf "0P0001S2NG.L" "BlackRock LifePath Target Date Fund 2040" "blk-lp-tdf-40.csv"
load_yf "LIKIX" "BlackRock LifePath Index 2040 Fund" "blk-lp-i-40-f.csv"

java -jar portfolio-0.0.1-SNAPSHOT.jar LoadPricesToDbTask "-file=${FOLDER_FOR_PRICE_FILES}"
java -jar portfolio-0.0.1-SNAPSHOT.jar LoadDividendsToDbTask "-file=${FOLDER_FOR_DIVIDEND_FILES}"
rm -rf ${FOLDER_FOR_PRICE_FILES}
rm -rf ${FOLDER_FOR_DIVIDEND_FILES}

java -Xmx512m -jar portfolio-0.0.1-SNAPSHOT.jar CalculateAssetStatsTask
java -Xmx768m -jar portfolio-0.0.1-SNAPSHOT.jar Calculate2AssetsPortfolioStatsTask
java -Xmx768m -jar portfolio-0.0.1-SNAPSHOT.jar CalculateAssetsShiftCorrelationTask
