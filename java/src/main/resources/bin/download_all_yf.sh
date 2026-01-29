#!/bin/bash
set -o pipefail
set -ue

FOLDER_FOR_PRICE_FILES=${FOLDER_FOR_PRICE_FILES:-"./data_to_load_prices"}
FOLDER_FOR_DIVIDEND_FILES=${FOLDER_FOR_DIVIDEND_FILES:-"./data_to_load_dividends"}

load_yf () {
	echo "---------------------------------------------------"
	echo "Yahoo data for: $2"

	out_file_name=`echo "$1" | sed -e 's/[\.\%]/-/g' | tr '[:upper:]' '[:lower:]'`;
	price_out_file="${FOLDER_FOR_PRICE_FILES}/${out_file_name}.csv"
	dividend_out_file="${FOLDER_FOR_DIVIDEND_FILES}/${out_file_name}.csv"

	if [ -f $price_out_file ]; then
		echo "${price_out_file} already exists."
	else 
		url="https://finance.yahoo.com/quote/$1/history/$3"
		java -Duse-http2=true -jar portfolio-0.0.1-SNAPSHOT.jar DownloadTask \
			"-url=$url" -outfile=out.html -headers=headers/yh-headers.prop

		./ParseTable.exe "-link=out.html" "-format=CSV"

		java -jar portfolio-0.0.1-SNAPSHOT.jar TransformSeriesDataTask "-file=out.csv" "-out_symbol=$2" \
			"-outfile=${price_out_file}" "-date_value_index=0" "-price_value_index=4" \
			"-volume_value_index=6" "-date_format=MMM d, yyyy"
		java -jar portfolio-0.0.1-SNAPSHOT.jar TransformDividendsDataTask "-file=out.csv" "-out_symbol=$2" \
			"-outfile=${dividend_out_file}" "-date_value_index=0" "-price_value_index=1" \
			"-date_format=MMM d, yyyy"

		rm -rf out.html
		rm -rf out.csv
		rm -rf request.tmp
	fi
}

input_file=$1
period=${2:-""}
echo "Period '${period}'"
echo "Loading definitions from ${input_file}"

mkdir -p ${FOLDER_FOR_PRICE_FILES}
mkdir -p ${FOLDER_FOR_DIVIDEND_FILES}

dos2unix ${input_file}

extra=""

if [ "${period}" == "5d" ]; then
	end=`date +%s`
	start=`date --date='5 days ago' +%s`
	extra="?period1=${start}&period2=${end}"
elif [ "${period}" == "1m" ]; then
	end=`date +%s`
	start=`date --date='30 days ago' +%s`
	extra="?period1=${start}&period2=${end}"
elif [ "${period}" == "5y" ]; then
	end=`date +%s`
	start=`date --date='5 years ago' +%s`
	extra="?period1=${start}&period2=${end}"
elif [ "${period}" == "6y" ]; then
	end=`date +%s`
	start=`date --date='6 years ago' +%s`
	extra="?period1=${start}&period2=${end}"
fi

while IFS='=' read -r key value
do
#    echo "Key: [$key]"
#    echo "Value: [$value]"
	
	load_yf "$key" "$value" "$extra"
done < "${input_file}"
