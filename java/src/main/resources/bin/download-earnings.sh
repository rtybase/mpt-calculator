#!/bin/bash
set -o pipefail
set -ue

FOLDER_FOR_EARNINGS_FILES=${FOLDER_FOR_EARNINGS_FILES:-"./data_to_load_earnings"}
JCACHE_FOLDER=${JCACHE_FOLDER:-"./jcache"}

load_earnings () {
	ticker=$2
	echo "---------------------------------------------------"
	echo "Earnings data for: ${ticker}"

	out_file_name=`echo "$1" | sed -e 's/[\.\%]/-/g' | tr '[:upper:]' '[:lower:]'`;
	eps_out_file="${FOLDER_FOR_EARNINGS_FILES}/${out_file_name}.csv"

	if [ -f $eps_out_file ]; then
		echo "${eps_out_file} already exists."
	else 
		java -XX:+AutoCreateSharedArchive -XX:SharedArchiveFile=${JCACHE_FOLDER}/j-client.jsa \
			-Duse-http2=true -jar portfolio-0.0.1-SNAPSHOT.jar DownloadTask \
			"-url=https://api.nasdaq.com/api/company/$1/revenue?limit=1" \
			-outfile=earnings.json

		if [ -s earnings.json ]; then
			python to_csv_earnings.py earnings.json "${ticker}" 1>${eps_out_file} 2>/dev/null
		else
			echo "No data for ${ticker}"
		fi

		rm -rf earnings.json
	fi
}

input_file=$1
echo "Loading definitions from ${input_file}"

mkdir -p ${FOLDER_FOR_EARNINGS_FILES}
mkdir -p ${JCACHE_FOLDER}

dos2unix ${input_file}

while IFS='=' read -r key value
do
#    echo "Key: [$key]"
#    echo "Value: [$value]"
	
	load_earnings "$key" "$value" || true
done < "${input_file}"