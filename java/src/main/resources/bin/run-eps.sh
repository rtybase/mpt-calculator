#!/bin/bash
set -o pipefail
set -ue

source ./all_configs.sh

mkdir -p ${FOLDER_FOR_EPS_FILES}
mkdir -p ${FOLDER_FOR_EARNINGS_FILES}
mkdir -p ${FOLDER_FOR_N_GAAP_EPS_FILES}
mkdir -p ./inputs

input_file="inputs/eps-inputs.txt"

python lists.py STOCKS > ${input_file}

./download-eps.sh ${input_file} &
./download-earnings.sh ${input_file} &
./download-n-gaap-eps.sh ${input_file} &

wait

./all_loads.sh

rm -rf ${FOLDER_FOR_EPS_FILES}
rm -rf ${FOLDER_FOR_EARNINGS_FILES}
rm -rf ${FOLDER_FOR_N_GAAP_EPS_FILES}
