#!/bin/bash
set -o pipefail
set -ue

source ./all_configs.sh

url=$1
valuation=${2:-"1B"}

mkdir -p ${TEMP_FOLDER}

java -XX:+AutoCreateSharedArchive -XX:SharedArchiveFile=${JCACHE_FOLDER}/j-client.jsa \
	-Duse-http2=false \
	-jar portfolio-0.0.1-SNAPSHOT.jar DownloadTask \
	"-url=$url" \
	"-outfile=${TEMP_FOLDER}/ch-a-out.html" \
	"-to_csv=true" "-ignore_table_headers=true" \
	-headers=headers/yh-headers.prop

rm -rf "${TEMP_FOLDER}/ch-a-out.html"

python check-new-assets.py "${TEMP_FOLDER}/ch-a-out.html.csv" ${valuation}
rm -rf "${TEMP_FOLDER}/ch-a-out.html.csv"