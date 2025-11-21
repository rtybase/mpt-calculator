#!/bin/bash
set -o pipefail
set -ue

java -Xmx512m -jar portfolio-0.0.1-SNAPSHOT.jar CalculateAssetStatsTask

java -Xmx2048m \
	-XX:+UnlockExperimentalVMOptions -XX:+EnableJVMCI -XX:+UseJVMCICompiler \
	-jar portfolio-0.0.1-SNAPSHOT.jar Calculate2AssetsPortfolioStatsTask

java -Xmx2048m \
	-XX:+UnlockExperimentalVMOptions -XX:+EnableJVMCI -XX:+UseJVMCICompiler \
	-jar portfolio-0.0.1-SNAPSHOT.jar CalculateAssetsShiftCorrelationTask

java -Xmx1024m -jar portfolio-0.0.1-SNAPSHOT.jar CalculateMultiAssetsPortfolioStatsTask

./run-ml.sh
