#!/bin/bash
set -o pipefail
set -ue

source ./all_configs.sh

file_pattern="x-daily-symbols"
current_date=`date '+%Y-%m-%d'`

python more-eps-data.py "${current_date}" 2 | tee "${file_pattern}-result.txt"
grep -v "Request" "${file_pattern}-result.txt" | grep "=" > "${file_pattern}.txt" || true

rm "${file_pattern}-result.txt"

if [ -s "${file_pattern}.txt" ]; then
	mkdir -p ${FOLDER_FOR_SECTOR_FILES}
	mkdir -p ${FOLDER_FOR_FSCORE_FILES}
	mkdir -p ${FOLDER_FOR_N_GAAP_EPS_FILES}
	mkdir -p ${FOLDER_FOR_FINANCE_FILES}

	./download-sectors.sh "${file_pattern}.txt"
	./download-f-score.sh "${file_pattern}.txt"
	./download-n-gaap-eps.sh "${file_pattern}.txt"
	./download-finance.sh "${file_pattern}.txt"

	python add-stocks.py ${FOLDER_FOR_SECTOR_FILES}
	python add-f-score.py ${FOLDER_FOR_FSCORE_FILES}
	java -jar portfolio-0.0.1-SNAPSHOT.jar LoadNonGaapEpsToDbTask "-file=${FOLDER_FOR_N_GAAP_EPS_FILES}"
	java -jar portfolio-0.0.1-SNAPSHOT.jar LoadAssetFinancialInfoToDbTask "-file=${FOLDER_FOR_FINANCE_FILES}"

	rm -rf ${FOLDER_FOR_SECTOR_FILES}
	rm -rf ${FOLDER_FOR_FSCORE_FILES}
	rm -rf ${FOLDER_FOR_N_GAAP_EPS_FILES}
	rm -rf ${FOLDER_FOR_FINANCE_FILES}
	./run-ml.sh
fi

rm "${file_pattern}.txt"
