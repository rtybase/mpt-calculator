#!/bin/bash
set -o pipefail
set -ue

source ./all_configs.sh

assets_list=$1
assets_file="inputs/yf-inputs-${assets_list}.txt"

mkdir -p ${FOLDER_FOR_PRICE_FILES}
mkdir -p ${FOLDER_FOR_DIVIDEND_FILES}

python lists.py ${assets_list} > "${assets_file}"

./download_all_yf.sh "${assets_file}" 1m

./all_loads.sh

rm -rf ${FOLDER_FOR_PRICE_FILES}
rm -rf ${FOLDER_FOR_DIVIDEND_FILES}
