#!/bin/bash
set -o pipefail
set -ue

source ./all_configs.sh

mkdir -p ${FOLDER_FOR_FINANCE_FILES}

python lists.py FIN-Q > inputs/finance-assets.txt
./download-finance.sh inputs/finance-assets.txt

java -jar portfolio-0.0.1-SNAPSHOT.jar LoadAssetFinancialInfoToDbTask "-file=${FOLDER_FOR_FINANCE_FILES}"

rm -rf ${FOLDER_FOR_FINANCE_FILES}
