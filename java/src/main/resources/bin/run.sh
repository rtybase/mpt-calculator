#!/bin/bash
set -o pipefail
set -ue

export FOLDER_FOR_PRICE_FILES="./data_to_load_prices"
export FOLDER_FOR_DIVIDEND_FILES="./data_to_load_dividends"
export FOLDER_FOR_EPS_FILES="./data_to_load_eps"
export FOLDER_FOR_EARNINGS_FILES="./data_to_load_earnings"
export FOLDER_FOR_N_GAAP_EPS_FILES="./data_to_load_n_gaap_eps"

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
mkdir -p ${FOLDER_FOR_EPS_FILES}
mkdir -p ${FOLDER_FOR_EARNINGS_FILES}
mkdir -p ${FOLDER_FOR_N_GAAP_EPS_FILES}

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

./download_all_yf.sh inputs/yf-inputs.txt
./download-eps.sh inputs/eps-inputs.txt
./download-earnings.sh inputs/eps-inputs.txt
./download-n-gaap-eps.sh inputs/eps-inputs.txt

java -jar portfolio-0.0.1-SNAPSHOT.jar LoadPricesToDbTask "-file=${FOLDER_FOR_PRICE_FILES}"
java -jar portfolio-0.0.1-SNAPSHOT.jar LoadDividendsToDbTask "-file=${FOLDER_FOR_DIVIDEND_FILES}"
java -jar portfolio-0.0.1-SNAPSHOT.jar LoadEpsToDbTask "-file=${FOLDER_FOR_EPS_FILES}"
java -jar portfolio-0.0.1-SNAPSHOT.jar LoadEarningsToDbTask "-file=${FOLDER_FOR_EARNINGS_FILES}"
java -jar portfolio-0.0.1-SNAPSHOT.jar LoadNonGaapEpsToDbTask "-file=${FOLDER_FOR_N_GAAP_EPS_FILES}"

rm -rf ${FOLDER_FOR_PRICE_FILES}
rm -rf ${FOLDER_FOR_DIVIDEND_FILES}
rm -rf ${FOLDER_FOR_EPS_FILES}
rm -rf ${FOLDER_FOR_EARNINGS_FILES}
rm -rf ${FOLDER_FOR_N_GAAP_EPS_FILES}

java -Xmx512m -jar portfolio-0.0.1-SNAPSHOT.jar CalculateAssetStatsTask
java -Xmx768m -jar portfolio-0.0.1-SNAPSHOT.jar Calculate2AssetsPortfolioStatsTask
java -Xmx768m -jar portfolio-0.0.1-SNAPSHOT.jar CalculateAssetsShiftCorrelationTask
java -Xmx768m -jar portfolio-0.0.1-SNAPSHOT.jar CalculateMultiAssetsPortfolioStatsTask

rm -rf inputs-ml/*

java -jar portfolio-0.0.1-SNAPSHOT.jar TransformEpsDataForTrainingTask \
	"-prices=D:\data_to_load_prices" \
	"-eps=D:\data_to_load_eps" \
	"-outfile=inputs-ml/out.csv"

python eps-rate-predict.py
