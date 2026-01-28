#!/bin/bash
set -o pipefail
set -ue

FOLDER_FOR_N_GAAP_EPS_FILES=${FOLDER_FOR_N_GAAP_EPS_FILES:-"./data_to_load_n_gaap_eps"}

extract_session() {
	java -Duse-http2=true -jar portfolio-0.0.1-SNAPSHOT.jar DownloadTask \
		"-url=https://uk.investing.com/equities/microsoft-corp-earnings" \
		-outfile=msft-ss-tt-yy.html -headers=headers/investing.prop

	sesstion=`cat msft-ss-tt-yy.html | grep -Eo -i "\"accessToken\":\"[\.+-\/=A-Z_a-z0-9]{1,}\",\"" | grep -Eo -i ":\"[\.+-\/=A-Z_a-z0-9]{1,}\"" | grep -Eo -i "[\.+-\/=A-Z_a-z0-9]{2,}"`

	cp headers/investing.prop headers/investing-sess.prop
	echo "Authorization=Bearer ${sesstion}" >> headers/investing-sess.prop
	rm -rf msft-ss-tt-yy.html
}

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
			-outfile=s-id.json -headers=headers/investing-sess.prop

		if [ -s s-id.json ]; then
			symbol_id=`python to_csv_ngaap_eps.py s-id.json`

			java -Duse-http2=true -jar portfolio-0.0.1-SNAPSHOT.jar DownloadTask \
				"-url=https://endpoints.investing.com/earnings/v1/instruments/$symbol_id/earnings?limit=10" \
				-outfile=n-gaap-eps.json -headers=headers/investing-sess.prop

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

extract_session

while IFS='=' read -r key value
do
#    echo "Key: [$key]"
#    echo "Value: [$value]"
	
	load_n_gaap_eps "$key" "$value" || true
done < "${input_file}"