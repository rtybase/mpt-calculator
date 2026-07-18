#!/bin/bash
set -o pipefail
set -ue

FOLDER_FOR_SECTOR_FILES=${FOLDER_FOR_SECTOR_FILES:-"./data_to_load_sectors"}
JCACHE_FOLDER=${JCACHE_FOLDER:-"./jcache"}
TEMP_FOLDER=${TEMP_FOLDER:-"./temp"}

load_sector () {
	ticker=$1
	echo "---------------------------------------------------"
	echo "Sector data for: ${ticker}"

	out_file_name=`echo "$1" | sed -e 's/[\.\%]/-/g' | tr '[:upper:]' '[:lower:]'`;
	sector_out_file="${FOLDER_FOR_SECTOR_FILES}/${out_file_name}.csv"
	tmp_out_file="${TEMP_FOLDER}/sector-${out_file_name}.json"

	if [ -f $sector_out_file ]; then
		echo "${sector_out_file} already exists."
	else 
		java -XX:+AutoCreateSharedArchive -XX:SharedArchiveFile=${JCACHE_FOLDER}/j-client.jsa \
			-Duse-http2=true -jar portfolio-0.0.1-SNAPSHOT.jar DownloadTask \
			"-url=https://api.nasdaq.com/api/quote/$1/summary?assetclass=stocks" \
			"-outfile=${tmp_out_file}"

		if [ -s ${tmp_out_file} ]; then
			python to_csv_sector.py ${tmp_out_file} 1>${sector_out_file} 2>/dev/null
		else
			echo "No data for ${ticker}"
		fi

		rm -rf ${tmp_out_file}
	fi
}

input_file=$1
echo "Loading definitions from ${input_file}"

mkdir -p ${FOLDER_FOR_SECTOR_FILES}
mkdir -p ${JCACHE_FOLDER}
mkdir -p ${TEMP_FOLDER}

dos2unix ${input_file}

while IFS='=' read -r key value
do
#    echo "Key: [$key]"
#    echo "Value: [$value]"
	
	load_sector "$key" "$value" || true
done < "${input_file}"