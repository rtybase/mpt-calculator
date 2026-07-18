#!/bin/bash
set -o pipefail
set -ue

source ./all_configs.sh

file_pattern="x-daily-symbols"
current_date=`date '+%Y-%m-%d'`

python more-eps-data.py "${current_date}" 2 | tee "${file_pattern}-result.txt"
grep -v "Request" "${file_pattern}-result.txt" | grep "=" > "${file_pattern}.txt" || true

rm "${file_pattern}-result.txt"

mkdir -p ${FOLDER_FOR_FSCORE_FILES}

if [ -s "${file_pattern}.txt" ]; then

	./download-f-score.sh "${file_pattern}.txt"

fi


python lists.py SCORE-L > inputs/new-assets.txt
./download-f-score.sh inputs/new-assets.txt
python add-f-score.py ${FOLDER_FOR_FSCORE_FILES}

rm "${file_pattern}.txt"
rm -rf ${FOLDER_FOR_FSCORE_FILES}

./run-ml.sh