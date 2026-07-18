#!/bin/bash
set -o pipefail
set -ue

source ./all_configs.sh

mkdir -p ${FOLDER_FOR_SECTOR_FILES}
mkdir -p ${FOLDER_FOR_PRICE_FILES}
mkdir -p ${FOLDER_FOR_DIVIDEND_FILES}
mkdir -p ${FOLDER_FOR_EPS_FILES}
mkdir -p ${FOLDER_FOR_EARNINGS_FILES}
mkdir -p ${FOLDER_FOR_N_GAAP_EPS_FILES}
mkdir -p ${FOLDER_FOR_FSCORE_FILES}
mkdir -p ${FOLDER_FOR_FINANCE_FILES}

./download-sectors.sh inputs/new-assets.txt
./download-f-score.sh inputs/new-assets.txt
./download-finance.sh inputs/new-assets.txt

./download_all_yf.sh "inputs/new-assets.txt" "5y" &
./download-eps.sh "inputs/new-assets.txt" &
./download-earnings.sh "inputs/new-assets.txt" &
./download-n-gaap-eps.sh "inputs/new-assets.txt" &

wait

python add-assets.py inputs/new-assets.txt
python add-stocks.py ${FOLDER_FOR_SECTOR_FILES}
python add-f-score.py ${FOLDER_FOR_FSCORE_FILES}

java -jar portfolio-0.0.1-SNAPSHOT.jar LoadAssetFinancialInfoToDbTask "-file=${FOLDER_FOR_FINANCE_FILES}"
python clean.py FIN-ONLY

./all_loads.sh

rm -rf ${FOLDER_FOR_SECTOR_FILES}
rm -rf ${FOLDER_FOR_PRICE_FILES}
rm -rf ${FOLDER_FOR_DIVIDEND_FILES}
rm -rf ${FOLDER_FOR_EPS_FILES}
rm -rf ${FOLDER_FOR_EARNINGS_FILES}
rm -rf ${FOLDER_FOR_N_GAAP_EPS_FILES}
rm -rf ${FOLDER_FOR_FSCORE_FILES}
rm -rf ${FOLDER_FOR_FINANCE_FILES}
