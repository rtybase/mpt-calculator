#!/bin/bash
set -o pipefail
set -ue

source ./all_configs.sh

mkdir -p ${JCACHE_FOLDER}
mkdir -p ${TEMP_FOLDER}
mkdir -p ${FOLDER_FOR_PRICE_FILES}

load_st () {
	url=$1
	tmp_file="${TEMP_FOLDER}/std-life-${2}.json"
	out_file="${FOLDER_FOR_PRICE_FILES}/std-life-${2}.csv"

	java -XX:+AutoCreateSharedArchive -XX:SharedArchiveFile=${JCACHE_FOLDER}/j-client.jsa \
		-jar portfolio-0.0.1-SNAPSHOT.jar DownloadTask \
		"-url=${url}" \
		"-outfile=${tmp_file}"

	java -XX:+AutoCreateSharedArchive -XX:SharedArchiveFile=${JCACHE_FOLDER}/j-client.jsa \
		-jar portfolio-0.0.1-SNAPSHOT.jar TransformStdLifeJsonDataTask \
		"-file=${tmp_file}" \
		"-outfile=${out_file}"

	rm -rf "${tmp_file}"
}

echo "---------------------------------------------------"
echo "Standard Life data"

load_st "https://secure.standardlife.co.uk/secure/fundfilter/rest/results/funds/GROUP_PENSIONS/GPP/null/existingcustomer?_=1601483195897" 1
load_st "https://secure.standardlife.co.uk/secure/fundfilter/rest/results/funds/INDIVIDUAL_PENSIONS/PPP/null/existingcustomer?_=1601483195897" 2

echo "---------------------------------------------------"
echo "European Central Bank data"
curl "https://www.ecb.europa.eu/stats/eurofxref/eurofxref-hist-90d.xml" > "${TEMP_FOLDER}/ecb_rates.xml"
java -jar portfolio-0.0.1-SNAPSHOT.jar TransformEcbRatesTask \
	"-file=${TEMP_FOLDER}/ecb_rates.xml" \
	"-outfile=${FOLDER_FOR_PRICE_FILES}/ecb.csv"

rm -rf "${TEMP_FOLDER}/ecb_rates.xml"

./all_loads.sh

rm -rf ${FOLDER_FOR_PRICE_FILES}

