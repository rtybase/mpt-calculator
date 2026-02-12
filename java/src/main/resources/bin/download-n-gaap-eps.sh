#!/bin/bash
set -o pipefail
set -ue

FOLDER_FOR_N_GAAP_EPS_FILES=${FOLDER_FOR_N_GAAP_EPS_FILES:-"./data_to_load_n_gaap_eps"}
CACHE_FOLDER="/d/cache"
MAX_AGE_SECONDS=$((5*24*60*60))

extract_session() {
	java -Duse-http2=true -jar portfolio-0.0.1-SNAPSHOT.jar DownloadTask \
		"-url=https://uk.investing.com/equities/microsoft-corp-earnings" \
		-outfile=msft-ss-tt-yy.html -headers=headers/investing.prop

	sesstion=`cat msft-ss-tt-yy.html | grep -Eo -i "\"accessToken\":\"[\.+-\/=A-Z_a-z0-9]{1,}\",\"" | grep -Eo -i ":\"[\.+-\/=A-Z_a-z0-9]{1,}\"" | grep -Eo -i "[\.+-\/=A-Z_a-z0-9]{2,}"`

	cp headers/investing.prop headers/investing-sess.prop
	echo "Authorization=Bearer ${sesstion}" >> headers/investing-sess.prop
	rm -rf msft-ss-tt-yy.html
}

update_id() {
	ticker=$1
	out_file=$2

	java -Duse-http2=true -jar portfolio-0.0.1-SNAPSHOT.jar DownloadTask \
		"-url=https://api.investing.com/api/search/v2/search?q=$ticker" \
		-outfile=$out_file -headers=headers/investing-sess.prop

}

update_id_if_required() {
	ticker=$1
	out_file=$2

	if [ -f $out_file ]; then
		file_age_is_seconds=$(($(date +%s) - $(date -r "${out_file}" +%s)))

		if [[ $file_age_is_seconds -gt MAX_AGE_SECONDS ]]; then
			update_id $ticker "${out_file}"
		fi
	else
		update_id $ticker "${out_file}"
	fi
}

load_n_gaap_eps () {
	ticker=$1
	asset_name=$2
	echo "---------------------------------------------------"
	echo "Non-GAAP EPS data for: ${asset_name}"

	out_file_name=`echo "$ticker" | sed -e 's/[\.\%]/-/g' | tr '[:upper:]' '[:lower:]'`;
	eps_out_file="${FOLDER_FOR_N_GAAP_EPS_FILES}/${out_file_name}.csv"
	id_file="${CACHE_FOLDER}/${out_file_name}-id.json"

	if [ -f $eps_out_file ]; then
		echo "${eps_out_file} already exists."
	else
		update_id_if_required $ticker "${id_file}"

		if [ -s "${id_file}" ]; then
			symbol_id=`python to_csv_ngaap_eps.py ${id_file} ${ticker}`

			if [ -z "${symbol_id}" ]; then
				echo "No ID for ${asset_name}"
				return
			fi

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
	fi
}

input_file=$1
echo "Loading definitions from ${input_file}"

mkdir -p ${FOLDER_FOR_N_GAAP_EPS_FILES}
mkdir -p ${CACHE_FOLDER}

dos2unix ${input_file}

extract_session

while IFS='=' read -r key value
do
#    echo "Key: [$key]"
#    echo "Value: [$value]"
	
	load_n_gaap_eps "$key" "$value" || true
done < "${input_file}"