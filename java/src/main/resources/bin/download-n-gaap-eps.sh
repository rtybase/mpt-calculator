#!/bin/bash
set -o pipefail
set -ue

FOLDER_FOR_N_GAAP_EPS_FILES=${FOLDER_FOR_N_GAAP_EPS_FILES:-"./data_to_load_n_gaap_eps"}

load_n_gaap_eps () {
	ticker=$1
	asset_name=$2
	echo "---------------------------------------------------"
	echo "Non-GAAP EPS data for: ${asset_name}"

	out_file_name=`echo "$1" | sed -e 's/[\.\%]/-/g' | tr '[:upper:]' '[:lower:]'`;
	eps_out_file="${FOLDER_FOR_N_GAAP_EPS_FILES}/${out_file_name}.csv"

	if [ -f $eps_out_file ]; then
		echo "${eps_out_file} already exists."
	else 
		java -Duse-http2=true -jar portfolio-0.0.1-SNAPSHOT.jar DownloadTask \
			"-url=https://api.investing.com/api/search/v2/search?q=$ticker" \
			-outfile=s-id.json

		if [ -s s-id.json ]; then
			symbol_id=`cat s-id.json | python -m json.tool | grep -iE "exchange.*(NASDAQ|NYSE)" -A 3 -B 5 | grep -iE "symbol.*\"$ticker\"" -A 3 -B 5 | sed -e 's/,//g'  | grep -iE "\"id\":" | awk -F ' ' '{ print $2}'`

			java -Duse-http2=true -jar portfolio-0.0.1-SNAPSHOT.jar DownloadTask \
				"-url=https://endpoints.investing.com/earnings/v1/instruments/$symbol_id/earnings?limit=15" \
				-outfile=n-gaap-eps.json

			if [ -s n-gaap-eps.json ]; then
				python to_csv.py n-gaap-eps.json | \
#					grep -iE "OFFICIAL|earning_date_type" | \
					sed -e "s/,${symbol_id},/,\"${asset_name//&/\\&}\",/g" 1>${eps_out_file} 2>/dev/null
			else
				echo "No data for ${asset_name}"
			fi

			rm -rf n-gaap-eps.json
		else
			echo "No ID for ${asset_name}"
		fi

		rm -rf s-id.json
	fi
}

input_file=$1
echo "Loading definitions from ${input_file}"

mkdir -p ${FOLDER_FOR_N_GAAP_EPS_FILES}

dos2unix ${input_file}

while IFS='=' read -r key value
do
#    echo "Key: [$key]"
#    echo "Value: [$value]"
	
	load_n_gaap_eps "$key" "$value" || true
done < "${input_file}"