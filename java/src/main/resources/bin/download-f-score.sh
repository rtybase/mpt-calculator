#!/bin/bash
set -o pipefail
set -ue

FOLDER_FOR_FSCORE_FILES=${FOLDER_FOR_FSCORE_FILES:-"./data_to_load_fscore"}

load_fscore () {
	ticker=$1
	echo "---------------------------------------------------"
	echo "F-Score data for: ${ticker}"

	out_file_name=`echo "$1" | sed -e 's/[\.\%]/-/g' | tr '[:upper:]' '[:lower:]'`;
	fscore_out_file="${FOLDER_FOR_FSCORE_FILES}/${out_file_name}.csv"

	if [ -f $fscore_out_file ]; then
		echo "${fscore_out_file} already exists."
	else 
		java -Duse-http2=true -jar portfolio-0.0.1-SNAPSHOT.jar DownloadTask \
			"-url=https://www.gurufocus.com/term/fscore/$1" \
			-outfile=fscore.html -headers=headers/gurufocus.prop

		if [ -s fscore.html ]; then
			./ParseTable.exe "-link=fscore.html" "-format=CSV"
			echo "${ticker}" > ${fscore_out_file}
			grep -iE "Piotroski F-Score.*Get" out.csv -B 1 | \
				awk -F ',' -v 'OFS=,' '{ print $(NF-5), $(NF-4), $(NF-3), $(NF-2), $(NF-1), $NF}' \
				1>>${fscore_out_file} 2>/dev/null

			rm -rf out.csv
		else
			echo "No data for ${ticker}"
		fi

		rm -rf fscore.html
	fi
}

input_file=$1
echo "Loading definitions from ${input_file}"

mkdir -p ${FOLDER_FOR_FSCORE_FILES}

dos2unix ${input_file}

while IFS='=' read -r key value
do
#    echo "Key: [$key]"
#    echo "Value: [$value]"
	
	load_fscore "$key" "$value" || true
done < "${input_file}"