#!/bin/bash
set -o pipefail
set -ue

source ./all_configs.sh

mkdir -p ${FOLDER_FOR_PRICE_FILES}
mkdir -p ${FOLDER_FOR_DIVIDEND_FILES}

python lists.py L > "inputs/yf-inputs-l.txt"

./download_all_yf.sh "inputs/yf-inputs-l.txt" 1m

