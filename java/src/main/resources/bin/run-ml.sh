#!/bin/bash
set -o pipefail
set -ue

rm -rf inputs-ml/*

"/c/Program Files/Java/graalvm-jdk-21.0.9+7.1/bin/java" -Xmx4096m \
	-XX:+UnlockExperimentalVMOptions -XX:+EnableJVMCI -XX:+UseJVMCICompiler \
	-jar portfolio-0.0.1-SNAPSHOT.jar TransformEpsDataForTrainingTask \
	"-prices=D:\data_to_load_prices" \
	"-eps=D:\data_to_load_eps" \
	"-eps-with-analysts=D:\data_to_load_eps_with_analysts" \
	"-n-gaap-eps=D:\data_to_load_n_gaap_eps" \
	"-dividends=D:\data_to_load_dividends" \
	"-outfile=inputs-ml/out.csv"

python eps-rate-predict.py
