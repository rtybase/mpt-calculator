#!/bin/bash
set -o pipefail
set -ue

source ./all_configs.sh

export FOLDER_FOR_SECTOR_FILES="./data_to_load_sectors"

mkdir -p ${FOLDER_FOR_SECTOR_FILES}
mkdir -p ${FOLDER_FOR_PRICE_FILES}
mkdir -p ${FOLDER_FOR_DIVIDEND_FILES}
mkdir -p ${FOLDER_FOR_EPS_FILES}
mkdir -p ${FOLDER_FOR_EARNINGS_FILES}
mkdir -p ${FOLDER_FOR_N_GAAP_EPS_FILES}
mkdir -p ${FOLDER_FOR_FSCORE_FILES}

./download-sectors.sh inputs/new-assets.txt
./download-f-score.sh inputs/new-assets.txt

./all_downloads.sh "inputs/new-assets.txt" "5y" "inputs/new-assets.txt"

python add-assets.py inputs/new-assets.txt
python add-stocks.py ${FOLDER_FOR_SECTOR_FILES}
python add-f-score.py ${FOLDER_FOR_FSCORE_FILES}

./all_loads.sh

rm -rf ${FOLDER_FOR_SECTOR_FILES}
rm -rf ${FOLDER_FOR_PRICE_FILES}
rm -rf ${FOLDER_FOR_DIVIDEND_FILES}
rm -rf ${FOLDER_FOR_EPS_FILES}
rm -rf ${FOLDER_FOR_EARNINGS_FILES}
rm -rf ${FOLDER_FOR_N_GAAP_EPS_FILES}
rm -rf ${FOLDER_FOR_FSCORE_FILES}
