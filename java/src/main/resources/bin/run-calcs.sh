#!/bin/bash
set -o pipefail
set -ue

java -Xmx512m -jar portfolio-0.0.1-SNAPSHOT.jar CalculateAssetStatsTask

"/c/Program Files/Java/graalvm-jdk-21.0.9+7.1/bin/java" -Xmx4096m \
	-XX:+UnlockExperimentalVMOptions -XX:+EnableJVMCI -XX:+UseJVMCICompiler \
	-jar portfolio-0.0.1-SNAPSHOT.jar Calculate2AssetsPortfolioStatsTask

#	-XX:+UseZGC -XX:+ZGenerational \
"/c/Program Files/Java/graalvm-jdk-21.0.9+7.1/bin/java" -Xmx4096m \
	-XX:+UnlockExperimentalVMOptions -XX:+EnableJVMCI -XX:+UseJVMCICompiler \
	-jar portfolio-0.0.1-SNAPSHOT.jar CalculateAssetsShiftCorrelationTask

python clean.py ALL

java -Xmx2048m -jar portfolio-0.0.1-SNAPSHOT.jar CalculateMultiAssetsPortfolioStatsTask

./run-ml.sh
