#!/bin/bash
set -o pipefail
set -ue

source ./all_configs.sh

current_date=`date '+%Y-%m-%d'`
python more-eps-data.py "${current_date}" 5

java -jar portfolio-0.0.1-SNAPSHOT.jar LoadPricesToDbTask "-file=${FOLDER_FOR_PRICE_FILES}"
java -jar portfolio-0.0.1-SNAPSHOT.jar LoadDividendsToDbTask "-file=${FOLDER_FOR_DIVIDEND_FILES}"
java -jar portfolio-0.0.1-SNAPSHOT.jar LoadEpsToDbTask "-file=${FOLDER_FOR_EPS_FILES}"
java -jar portfolio-0.0.1-SNAPSHOT.jar LoadEarningsToDbTask "-file=${FOLDER_FOR_EARNINGS_FILES}"
java -jar portfolio-0.0.1-SNAPSHOT.jar LoadNonGaapEpsToDbTask "-file=${FOLDER_FOR_N_GAAP_EPS_FILES}"
