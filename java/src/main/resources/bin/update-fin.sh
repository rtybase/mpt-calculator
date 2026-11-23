#!/bin/bash
set -o pipefail
set -ue

source ./all_configs.sh

assets_lists=$1
assets_file=$2

mkdir -p ${FOLDER_FOR_FINANCE_FILES}

> ${assets_file}

for list in ${assets_lists//,/ }
do
	echo "List: $list"
	python lists.py ${list} >> ${assets_file}
done

./download-finance.sh ${assets_file}

java -jar portfolio-0.0.1-SNAPSHOT.jar LoadAssetFinancialInfoToDbTask "-file=${FOLDER_FOR_FINANCE_FILES}"
python clean.py FIN-ONLY

rm -rf ${FOLDER_FOR_FINANCE_FILES}
