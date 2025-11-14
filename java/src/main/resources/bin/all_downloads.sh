#!/bin/bash
set -o pipefail
set -ue

source ./all_configs.sh

./download_all_yf.sh $1 $2 &

if [ -n "$3" ]; then
	./download-eps.sh $3 &
	./download-earnings.sh $3 &
	./download-n-gaap-eps.sh $3 &
fi 

wait