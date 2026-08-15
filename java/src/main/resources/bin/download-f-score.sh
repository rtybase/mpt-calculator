#!/bin/bash
set -o pipefail
set -ue

FOLDER_FOR_FSCORE_FILES=${FOLDER_FOR_FSCORE_FILES:-"./data_to_load_fscore"}
JCACHE_FOLDER=${JCACHE_FOLDER:-"./jcache"}
TEMP_FOLDER=${TEMP_FOLDER:-"./temp"}

load_fscore () {
	ticker=$1
	echo "---------------------------------------------------"
	echo "F-Score data for: ${ticker}"

	out_file_name=`echo "$1" | sed -e 's/[\.\%]/-/g' | tr '[:upper:]' '[:lower:]'`;
	fscore_out_file="${FOLDER_FOR_FSCORE_FILES}/${out_file_name}.csv"

	if [ -f $fscore_out_file ]; then
		echo "${fscore_out_file} already exists."
	else 
		out_file="${TEMP_FOLDER}/f-score.html"

		java -XX:+AutoCreateSharedArchive -XX:SharedArchiveFile=${JCACHE_FOLDER}/j-client.jsa \
			-Duse-http2=true -jar portfolio-0.0.1-SNAPSHOT.jar DownloadTask \
			"-url=https://www.gurufocus.com/term/fscore/$1" \
			"-to_csv=true" "-ignore_table_headers=true"\
			"-outfile=$out_file" -headers=headers/gurufocus.prop

		if [ -s "${out_file}.csv" ]; then
			echo "${ticker}" > ${fscore_out_file}
			grep -iE "Piotroski F-Score.*Get" "${out_file}.csv" -B 1 | \
				awk -F ',' -v 'OFS=,' '{ print $(NF-5), $(NF-4), $(NF-3), $(NF-2), $(NF-1), $NF}' \
				1>>${fscore_out_file} 2>/dev/null

			rm -rf "${out_file}.csv"
		else
			echo "No data for ${ticker}"
		fi

		rm -rf "$out_file"
		sleep $(( ( RANDOM % 7 )  + 15 ))
	fi
}

input_file=$1
echo "Loading definitions from ${input_file}"

mkdir -p ${FOLDER_FOR_FSCORE_FILES}
mkdir -p ${JCACHE_FOLDER}
mkdir -p ${TEMP_FOLDER}

dos2unix ${input_file}

while IFS='=' read -r key value
do
#    echo "Key: [$key]"
#    echo "Value: [$value]"
	
	load_fscore "$key" "$value" || true
done < "${input_file}"