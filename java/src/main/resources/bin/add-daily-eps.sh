#!/bin/bash
set -o pipefail
set -ue

source ./all_configs.sh

file_pattern="x-daily-symbols"
current_date=`date '+%Y-%m-%d'`

python more-eps-data.py "${current_date}" 1 | tee "${file_pattern}-result.txt"
grep -v "Request" "${file_pattern}-result.txt" | grep "=" > "${file_pattern}.txt"

rm "${file_pattern}-result.txt"

mkdir -p ${FOLDER_FOR_N_GAAP_EPS_FILES}

./download-n-gaap-eps.sh "${file_pattern}.txt"
java -jar portfolio-0.0.1-SNAPSHOT.jar LoadNonGaapEpsToDbTask "-file=${FOLDER_FOR_N_GAAP_EPS_FILES}"

rm -rf ${FOLDER_FOR_N_GAAP_EPS_FILES}
rm "${file_pattern}.txt"