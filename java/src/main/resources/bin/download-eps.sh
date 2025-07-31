#!/bin/bash
set -o pipefail
set -ue

FOLDER_FOR_EPS_FILES=${FOLDER_FOR_EPS_FILES:-"./data_to_load_eps"}

load_eps () {
	ticker=$2
	echo "---------------------------------------------------"
	echo "EPS data for: ${ticker}"

	out_file_name=`echo "$1" | sed -e 's/[\.\%]/-/g' | tr '[:upper:]' '[:lower:]'`;
	eps_out_file="${FOLDER_FOR_EPS_FILES}/${out_file_name}.csv"

	if [ -f $eps_out_file ]; then
		echo "${eps_out_file} already exists."
	else 
		java -Duse-http2=true -jar portfolio-0.0.1-SNAPSHOT.jar DownloadTask "-url=https://api.nasdaq.com/api/company/$1/earnings-surprise" -outfile=eps.json

		cat eps.json | python -m json.tool | \
			grep -iE "(dateReported|eps|consensusForecast)" | \
			sed -e 's/[\",\$\(\)\:]//g' | awk -F ' ' '{ print $2}' | paste -d " " - - - | \
			tail -n +2 | \
			awk -F ' ' -v 'OFS=,' -v TICKER="${ticker}" '{ print "\"" TICKER "\"", $2, $3, $1}'  > ${eps_out_file}

		rm -rf eps.json
	fi
}

input_file=$1
echo "Loading definitions from ${input_file}"

mkdir -p ${FOLDER_FOR_EPS_FILES}

dos2unix ${input_file}

while IFS='=' read -r key value
do
#    echo "Key: [$key]"
#    echo "Value: [$value]"
	
	load_eps "$key" "$value"
done < "${input_file}"