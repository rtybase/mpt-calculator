#!/bin/bash
set -o pipefail
set -ue

source ./all_configs.sh

load_uk_in() {
	echo "---------------------------------------------------"
	echo "UK Investing data for: $2"
	./ParseTable.exe "-link=https://uk.investing.com/$1-historical-data" "-format=CSV"
	head -n 27 out.csv > out1.csv
	java -jar portfolio-0.0.1-SNAPSHOT.jar TransformSeriesDataTask "-file=out1.csv" "-out_symbol=$2" \
		"-outfile=${FOLDER_FOR_PRICE_FILES}/$3" "-date_value_index=0" "-price_value_index=1" \
		"-volume_value_index=5" "-date_format=dd/MM/yyyy"
	rm -rf out.csv
	rm -rf out1.csv
	rm -rf request.tmp
}

mkdir -p ${FOLDER_FOR_PRICE_FILES}
mkdir -p ${FOLDER_FOR_DIVIDEND_FILES}
mkdir -p ${FOLDER_FOR_EPS_FILES}
mkdir -p ${FOLDER_FOR_EARNINGS_FILES}
mkdir -p ${FOLDER_FOR_N_GAAP_EPS_FILES}

echo "---------------------------------------------------"
echo "Standard Life data"
java -jar portfolio-0.0.1-SNAPSHOT.jar DownloadTask "-url=https://secure.standardlife.co.uk/secure/fundfilter/rest/results/funds/GROUP_PENSIONS/GPP/null/existingcustomer?_=1601483195897" -outfile=std-life.json
java -jar portfolio-0.0.1-SNAPSHOT.jar TransformStdLifeJsonDataTask "-file=std-life.json" "-outfile=${FOLDER_FOR_PRICE_FILES}/std-life1.csv"
rm -rf std-life.json

java -jar portfolio-0.0.1-SNAPSHOT.jar DownloadTask "-url=https://secure.standardlife.co.uk/secure/fundfilter/rest/results/funds/INDIVIDUAL_PENSIONS/PPP/null/existingcustomer?_=1601483195897" -outfile=std-life.json
java -jar portfolio-0.0.1-SNAPSHOT.jar TransformStdLifeJsonDataTask "-file=std-life.json" "-outfile=${FOLDER_FOR_PRICE_FILES}/std-life2.csv"
rm -rf std-life.json

echo "---------------------------------------------------"
echo "European Central Bank data"
curl "https://www.ecb.europa.eu/stats/eurofxref/eurofxref-hist-90d.xml" > ecb_rates.xml
java -jar portfolio-0.0.1-SNAPSHOT.jar TransformEcbRatesTask "-file=ecb_rates.xml" "-outfile=${FOLDER_FOR_PRICE_FILES}/ecb.csv"
rm -rf ecb_rates.xml

load_uk_in "indices/uk-100" "FTSE100" "ftse100-1.csv"

python lists.py STOCKS > "inputs/eps-inputs.txt"
python lists.py ALL > "inputs/yf-inputs.txt"

./all_downloads.sh "inputs/yf-inputs.txt" "1m" "inputs/eps-inputs.txt"

./all_loads.sh

rm -rf ${FOLDER_FOR_PRICE_FILES}
rm -rf ${FOLDER_FOR_DIVIDEND_FILES}
rm -rf ${FOLDER_FOR_EPS_FILES}
rm -rf ${FOLDER_FOR_EARNINGS_FILES}
rm -rf ${FOLDER_FOR_N_GAAP_EPS_FILES}

./run-calcs.sh
