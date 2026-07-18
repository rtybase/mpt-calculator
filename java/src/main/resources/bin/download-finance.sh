#!/bin/bash
set -o pipefail
set -ue

FOLDER_FOR_FINANCE_FILES=${FOLDER_FOR_FINANCE_FILES:-"./data_to_load_finance"}

load_finance () {
	ticker=$1
	echo "---------------------------------------------------"
	echo "Finance data for: ${ticker}"

	out_file_name=`echo "$1" | sed -e 's/[\.\%]/-/g' | tr '[:upper:]' '[:lower:]'`;
	finance_out_file1="${FOLDER_FOR_FINANCE_FILES}/${out_file_name}-1.csv"
	finance_out_file2="${FOLDER_FOR_FINANCE_FILES}/${out_file_name}-2.csv"

	if [ -f $finance_out_file1 ]; then
		echo "${finance_out_file1} already exists."
	else
		python more-fin-data-yf.py ${ticker} 1>>${finance_out_file1} 2>/dev/null
	fi

	if [ -f $finance_out_file2 ]; then
		echo "${finance_out_file2} already exists."
	else
		python more-fin-data.py ${ticker} 1>>${finance_out_file2} 2>/dev/null
	fi
}

input_file=$1
echo "Loading definitions from ${input_file}"

mkdir -p ${FOLDER_FOR_FINANCE_FILES}

dos2unix ${input_file}

while IFS='=' read -r key value
do
#    echo "Key: [$key]"
#    echo "Value: [$value]"
	
	load_finance "$key" "$value" || true
done < "${input_file}"