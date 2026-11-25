#!/bin/bash
set -o pipefail

FOLDER_FOR_FINANCE_FILES=${FOLDER_FOR_FINANCE_FILES:-"./data_to_load_finance"}

input_file=$1
api_key=$2

load_finance () {
	ticker=$1
	echo "---------------------------------------------------"
	echo "Finance data for: ${ticker}"

	out_file_name=`echo "$1" | sed -e 's/[\.\%]/-/g' | tr '[:upper:]' '[:lower:]'`;
	finance_out_file="${FOLDER_FOR_FINANCE_FILES}/${out_file_name}.csv"

	if [ -f $finance_out_file ]; then
		echo "${finance_out_file} already exists."
	else
		python more-fin-data-av.py "${ticker}" "${api_key}" 1>>${finance_out_file}
		if [ $? -ne 0 ]; then
			rm -rf ${finance_out_file}
			echo "Exit..."
			exit 1
		fi
	fi

	sed -i 's/None//g' ${finance_out_file}
	sed -i 's/,0,/,,/g' ${finance_out_file}
}

echo "Loading definitions from ${input_file}"

mkdir -p ${FOLDER_FOR_FINANCE_FILES}

dos2unix ${input_file}

while IFS='=' read -r key value
do
#    echo "Key: [$key]"
#    echo "Value: [$value]"
	
	load_finance "$key" "$value"
done < "${input_file}"