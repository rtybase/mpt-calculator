#!/bin/bash
set -o pipefail
set -ue

FOLDER_FOR_SECTOR_FILES=${FOLDER_FOR_SECTOR_FILES:-"./data_to_load_sectors"}

load_sector () {
	ticker=$2
	echo "---------------------------------------------------"
	echo "Sector data for: ${ticker}"

	out_file_name=`echo "$1" | sed -e 's/[\.\%]/-/g' | tr '[:upper:]' '[:lower:]'`;
	sector_out_file="${FOLDER_FOR_SECTOR_FILES}/${out_file_name}.csv"

	if [ -f $sector_out_file ]; then
		echo "${sector_out_file} already exists."
	else 
		java -Duse-http2=true -jar portfolio-0.0.1-SNAPSHOT.jar DownloadTask "-url=https://api.nasdaq.com/api/quote/$1/summary?assetclass=stocks" -outfile=sector.json

		cat sector.json | python -m json.tool | \
			grep -iE -A 1 "label\":.*(Sector|Industry)" | \
			grep -iE "value" | \
			sed -e 's/\"value\"://g' | \
			tr -s '[:blank:]' | \
			sed -e 's/ \"/\"/g' | \
			paste -d "," - - | \
			awk -F ',' -v 'OFS=,' -v TICKER="${ticker}" '{ print "\"" TICKER "\"", $1, $2}' > ${sector_out_file}

		rm -rf sector.json
	fi
}

input_file=$1
echo "Loading definitions from ${input_file}"

mkdir -p ${FOLDER_FOR_SECTOR_FILES}

dos2unix ${input_file}

while IFS='=' read -r key value
do
#    echo "Key: [$key]"
#    echo "Value: [$value]"
	
	load_sector "$key" "$value"
done < "${input_file}"