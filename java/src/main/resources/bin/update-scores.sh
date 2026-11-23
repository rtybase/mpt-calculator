#!/bin/bash
set -o pipefail
set -ue

source ./all_configs.sh

assets_list=$1
assets_file="inputs/score-inputs-${assets_list}.txt"

mkdir -p ${FOLDER_FOR_FSCORE_FILES}

python lists.py ${assets_list} > "${assets_file}"

./download-f-score.sh "${assets_file}"

python add-f-score.py ${FOLDER_FOR_FSCORE_FILES}

rm -rf ${FOLDER_FOR_FSCORE_FILES}
