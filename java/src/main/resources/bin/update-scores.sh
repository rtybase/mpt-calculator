#!/bin/bash
set -o pipefail
set -ue

source ./all_configs.sh

mkdir -p ${FOLDER_FOR_FSCORE_FILES}

python lists.py SCORE > inputs/score-assets.txt
./download-f-score.sh inputs/score-assets.txt

python add-f-score.py ${FOLDER_FOR_FSCORE_FILES}

rm -rf ${FOLDER_FOR_FSCORE_FILES}