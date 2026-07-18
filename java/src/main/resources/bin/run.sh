#!/bin/bash
set -o pipefail
set -ue

source ./all_configs.sh

load_uk_in() {
	echo "---------------------------------------------------"
	echo "UK Investing data for: $2"

	java -XX:+AutoCreateSharedArchive -XX:SharedArchiveFile=${JCACHE_FOLDER}/j-client.jsa \
		-Duse-http2=true -jar portfolio-0.0.1-SNAPSHOT.jar DownloadTask \
		"-url=https://uk.investing.com/$1-historical-data" \
		-outfile=out.html -headers=headers/investing.prop

	./ParseTable.exe "-link=out.html" "-format=CSV"

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

load_uk_in "indices/uk-100" "FTSE100" "ftse100-1.csv"

python lists.py ALL > "inputs/yf-inputs.txt"

./download_all_yf.sh "inputs/yf-inputs.txt" "1m"

./all_loads.sh

rm -rf ${FOLDER_FOR_PRICE_FILES}
rm -rf ${FOLDER_FOR_DIVIDEND_FILES}

./run-calcs.sh
