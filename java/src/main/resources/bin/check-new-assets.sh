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
	"-outfile=${TEMP_FOLDER}/out.html" \
	-headers=headers/yh-headers.prop

./ParseTable.exe "-link=${TEMP_FOLDER}/out.html" "-format=CSV"
rm -rf ${TEMP_FOLDER}/out.html

python check-new-assets.py out.csv ${valuation}
rm -rf out.csv