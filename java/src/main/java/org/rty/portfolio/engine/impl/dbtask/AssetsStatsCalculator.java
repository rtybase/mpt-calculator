package org.rty.portfolio.engine.impl.dbtask;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;

import org.apache.commons.math3.stat.StatUtils;
import org.rty.portfolio.core.AssetsStatistics;
import org.rty.portfolio.core.PortflioStats;
import org.rty.portfolio.core.utils.DatesAndSetUtil;
import org.rty.portfolio.math.Calculator;


public class AssetsStatsCalculator implements Callable<AssetsStatistics> {
	private final List<Integer> assetIds;
	private final Map<Integer, Map<String, Double>> storage;

	AssetsStatsCalculator(Map<Integer, Map<String, Double>> storage, List<Integer> assetIds) {
		this.storage = storage;
		this.assetIds = assetIds;
	}

	@Override
	public AssetsStatistics call() throws Exception {
		final List<Map<String, Double>> assetsRates = assetIds.stream().map(storage::get).toList();
		final Set<String> dates = DatesAndSetUtil
				.computeCommonValues(assetsRates.stream().map(assetRates -> assetRates.keySet()).toList());

		if (DatesAndSetUtil.hasSufficientContent(dates)) {
			// get the rates for the common dates
			final List<double[]> values = assetsRates.stream()
					.map(assetRates -> DatesAndSetUtil.getValuesByIndex(dates, assetRates)).toList();
			final List<Double> means = values.stream().map(StatUtils::mean).toList();
			final List<Double> variances = values.stream().map(StatUtils::populationVariance).toList();

			double[][] covarianceMatrix = new double[assetIds.size()][assetIds.size()];
			double[][] correlationMatrix = new double[assetIds.size()][assetIds.size()];

			for (int i = 0; i < assetIds.size(); i++) {
				covarianceMatrix[i][i] = variances.get(i);
				correlationMatrix[i][i] = 1D;

				for (int j = i + 1; j < assetIds.size(); j++) {
					final double covariance = Calculator.calculateCovariance(values.get(i),
							values.get(j));

					final double correlation = Calculator.calculateCorrelation(covariance,
							variances.get(i),
							variances.get(j));

					covarianceMatrix[i][j] = covariance;
					covarianceMatrix[j][i] = covariance;

					correlationMatrix[i][j] = correlation;
					correlationMatrix[j][i] = correlation;
				}
			}

			PortflioStats portStats = calculatePortfolioStats(means, covarianceMatrix);

			return new AssetsStatistics(assetIds, dates,
					true, 
					values,
					means,
					variances,
					covarianceMatrix,
					correlationMatrix,
					portStats);
		} else {
			return new AssetsStatistics(assetIds, dates,
					false,
					null,
					null,
					null,
					null,
					null,
					null);
		}
	}

	private static PortflioStats calculatePortfolioStats(List<Double> means, double[][] covarianceMatrix) {
		final double[] rates = means.stream().mapToDouble(Double::doubleValue).toArray();
		return Calculator.calculateWeights(rates, covarianceMatrix);
	}
}
