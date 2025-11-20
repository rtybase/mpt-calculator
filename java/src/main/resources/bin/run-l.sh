#!/bin/bash
set -o pipefail
set -ue

source ./all_configs.sh

mkdir -p ${FOLDER_FOR_PRICE_FILES}
mkdir -p ${FOLDER_FOR_DIVIDEND_FILES}

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

./all_downloads.sh "inputs/yf-inputs-l.txt" 1m ""

./all_loads.sh

rm -rf ${FOLDER_FOR_PRICE_FILES}
rm -rf ${FOLDER_FOR_DIVIDEND_FILES}

./run-calcs.sh
