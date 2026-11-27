#!/bin/bash
set -o pipefail

FOLDER_FOR_FINANCE_FILES=${FOLDER_FOR_FINANCE_FILES:-"./data_to_load_finance"}
JCACHE_FOLDER=${JCACHE_FOLDER:-"./jcache"}
TEMP_FOLDER=${TEMP_FOLDER:-"./temp"}

input_file=$1

download_data() {
	ticker=$1
	statement_type=$2
	out_file=$3
	url="https://seekingalpha.com/api/v3/symbols/${ticker}/fundamentals_metrics?lang=en&period_type=quarterly&statement_type=${statement_type}&target_currency=USD"
	
	java -XX:+AutoCreateSharedArchive -XX:SharedArchiveFile=${JCACHE_FOLDER}/j-client.jsa \
		-Duse-http2=true -jar portfolio-0.0.1-SNAPSHOT.jar DownloadTask \
		"-url=${url}" -headers=headers/seekingalpha.prop \
		-outfile=${out_file}
}

load_finance () {
	ticker=$1
	echo "---------------------------------------------------"
	echo "Finance data for: ${ticker}"

	out_file_name=`echo "$1" | sed -e 's/[\.\%]/-/g' | tr '[:upper:]' '[:lower:]'`;
	finance_out_file="${FOLDER_FOR_FINANCE_FILES}/${out_file_name}-3.csv"

	if [ -f $finance_out_file ]; then
		echo "${finance_out_file} already exists."
	else
		download_data "${ticker}" "income-statement" "${TEMP_FOLDER}/${out_file_name}-s-al-is.json"
		download_data "${ticker}" "balance-sheet" "${TEMP_FOLDER}/${out_file_name}-s-al-bs.json"
		download_data "${ticker}" "cash-flow-statement" "${TEMP_FOLDER}/${out_file_name}-s-al-cs.json"

		python to_csv_s_al.py "${ticker}" 1>>${finance_out_file}
	fi


	rm -rf "${TEMP_FOLDER}/${out_file_name}-s-al-is.json"
	rm -rf "${TEMP_FOLDER}/${out_file_name}-s-al-bs.json"
	rm -rf "${TEMP_FOLDER}/${out_file_name}-s-al-cs.json"
}

echo "Loading definitions from ${input_file}"

mkdir -p ${FOLDER_FOR_FINANCE_FILES}
mkdir -p ${JCACHE_FOLDER}
mkdir -p ${TEMP_FOLDER}

dos2unix ${input_file}

while IFS='=' read -r key value
do
#    echo "Key: [$key]"
#    echo "Value: [$value]"
	
	load_finance "$key" "$value"
done < "${input_file}"
