#!/bin/bash
set -o pipefail
set -ue

export FOLDER_FOR_SECTOR_FILES="./data_to_load_sectors"
export FOLDER_FOR_PRICE_FILES="./data_to_load_prices"
export FOLDER_FOR_DIVIDEND_FILES="./data_to_load_dividends"
export FOLDER_FOR_EPS_FILES="./data_to_load_eps"
export FOLDER_FOR_EARNINGS_FILES="./data_to_load_earnings"
export FOLDER_FOR_N_GAAP_EPS_FILES="./data_to_load_n_gaap_eps"

mkdir -p ${FOLDER_FOR_SECTOR_FILES}
mkdir -p ${FOLDER_FOR_PRICE_FILES}
mkdir -p ${FOLDER_FOR_DIVIDEND_FILES}
mkdir -p ${FOLDER_FOR_EPS_FILES}
mkdir -p ${FOLDER_FOR_EARNINGS_FILES}
mkdir -p ${FOLDER_FOR_N_GAAP_EPS_FILES}


./download-sectors.sh inputs/new-assets.txt
./download_all_yf.sh inputs/new-assets.txt
./download-eps.sh inputs/new-assets.txt
./download-earnings.sh inputs/new-assets.txt
./download-n-gaap-eps.sh inputs/new-assets.txt

python add-assets.py inputs/new-assets.txt
java -jar portfolio-0.0.1-SNAPSHOT.jar LoadPricesToDbTask "-file=${FOLDER_FOR_PRICE_FILES}"
java -jar portfolio-0.0.1-SNAPSHOT.jar LoadDividendsToDbTask "-file=${FOLDER_FOR_DIVIDEND_FILES}"
java -jar portfolio-0.0.1-SNAPSHOT.jar LoadEpsToDbTask "-file=${FOLDER_FOR_EPS_FILES}"
java -jar portfolio-0.0.1-SNAPSHOT.jar LoadEarningsToDbTask "-file=${FOLDER_FOR_EARNINGS_FILES}"
java -jar portfolio-0.0.1-SNAPSHOT.jar LoadNonGaapEpsToDbTask "-file=${FOLDER_FOR_N_GAAP_EPS_FILES}"

rm -rf ${FOLDER_FOR_SECTOR_FILES}
rm -rf ${FOLDER_FOR_PRICE_FILES}
rm -rf ${FOLDER_FOR_DIVIDEND_FILES}
rm -rf ${FOLDER_FOR_EPS_FILES}
rm -rf ${FOLDER_FOR_EARNINGS_FILES}
rm -rf ${FOLDER_FOR_N_GAAP_EPS_FILES}
