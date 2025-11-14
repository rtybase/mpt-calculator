#!/bin/bash
set -o pipefail
set -ue

java -Xmx512m -jar portfolio-0.0.1-SNAPSHOT.jar CalculateAssetStatsTask

java -Xmx1024m \
	-XX:+UnlockExperimentalVMOptions -XX:+EnableJVMCI -XX:+UseJVMCICompiler \
	-jar portfolio-0.0.1-SNAPSHOT.jar Calculate2AssetsPortfolioStatsTask

java -Xmx1024m \
	-XX:+UnlockExperimentalVMOptions -XX:+EnableJVMCI -XX:+UseJVMCICompiler \
	-jar portfolio-0.0.1-SNAPSHOT.jar CalculateAssetsShiftCorrelationTask

java -Xmx1024m -jar portfolio-0.0.1-SNAPSHOT.jar CalculateMultiAssetsPortfolioStatsTask

rm -rf inputs-ml/*

java -Xmx2048m \
	-XX:+UnlockExperimentalVMOptions -XX:+EnableJVMCI -XX:+UseJVMCICompiler \
	-jar portfolio-0.0.1-SNAPSHOT.jar TransformEpsDataForTrainingTask \
	"-prices=D:\data_to_load_prices" \
	"-eps=D:\data_to_load_eps" \
	"-eps-with-analysts=D:\data_to_load_eps_with_analysts" \
	"-n-gaap-eps=D:\data_to_load_n_gaap_eps" \
	"-outfile=inputs-ml/out.csv"

python eps-rate-predict.py
