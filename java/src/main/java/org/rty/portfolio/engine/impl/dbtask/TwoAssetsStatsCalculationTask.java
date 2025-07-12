package org.rty.portfolio.engine.impl.dbtask;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.math3.stat.StatUtils;
import org.apache.commons.math3.stat.correlation.Covariance;
import org.rty.portfolio.core.PortflioStats;
import org.rty.portfolio.core.utils.DatesAndSetUtil;
import org.rty.portfolio.engine.impl.dbtask.TwoAssetsStatsCalculationTask.AssetsStatsCalculationResult;
import org.rty.portfolio.math.Calculator;

public class TwoAssetsStatsCalculationTask implements Callable<AssetsStatsCalculationResult> {
	private final List<Integer> assetIds;
	private final Map<Integer, Map<String, Double>> storage;

	TwoAssetsStatsCalculationTask(Map<Integer, Map<String, Double>> storage, List<Integer> assetIds) {
		this.storage = storage;
		this.assetIds = assetIds;
	}

	@Override
	public AssetsStatsCalculationResult call() throws Exception {
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
					final double covariance = new Covariance().covariance(values.get(i),
							values.get(j),
							false);

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

			return new AssetsStatsCalculationResult(assetIds, true, 
					values,
					means,
					variances,
					covarianceMatrix,
					correlationMatrix,
					portStats);
		} else {
			return new AssetsStatsCalculationResult(assetIds, false,
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

	public static class AssetsStatsCalculationResult {
		public final List<Integer> assetIds;
		public final List<double[]> assetValues;
		public final List<Double> assetMeans;
		public final List<Double> assetVariances;
		public final double[][] covarianceMatrix;
		public final double[][] correlationMatrix;
		public final PortflioStats portflioStats;
		public final boolean hasSufficientContent;

		private AssetsStatsCalculationResult(List<Integer> assetIds,
				boolean hasSufficientContent,
				List<double[]> assetValues,
				List<Double> assetMeans,
				List<Double> assetVariances,
				double[][] covarianceMatrix,
				double[][] correlationMatrix,
				PortflioStats portflioStats) {
			this.assetIds = Collections.unmodifiableList(assetIds);
			this.hasSufficientContent = hasSufficientContent;
			this.assetValues = toUnmodifiableList(assetValues);
			this.assetMeans = toUnmodifiableList(assetMeans);
			this.assetVariances = toUnmodifiableList(assetVariances);
			this.covarianceMatrix = covarianceMatrix;
			this.correlationMatrix = correlationMatrix;
			this.portflioStats = portflioStats;
		}

		@Override
		public String toString() {
			if (hasSufficientContent) {
				return String.format(
						"assetIds=%s\n"
						+ "assetMeans=%s\n"
						+ "assetVariances=%s\n"
						+ "cov=[%s]\n"
						+ "cor=[%s]\n"
						+ "assetWeights=%s\n"
						+ "portRet=%.5f, portVar=%.5f",
						StringUtils.join(assetIds, " "),
						join(assetMeans),
						join(assetVariances),
						join(covarianceMatrix),
						join(correlationMatrix),
						join(portflioStats.getPortfolioWeights()),
						portflioStats.getPortfolioReturn(),
						portflioStats.getPorfolioVariance());
			} else {
				return String.format(
						"assetIds=%s\n"
						+ "assetMeans=\n"
						+ "assetVariances=\n"
						+ "cov=[]\n"
						+ "cor=[]\n"
						+ "assetWeights=\n"
						+ "portRet=, portVar=",
						StringUtils.join(assetIds, " "));
			}
		}

		private static <T> List<T> toUnmodifiableList(List<T> values) {
			if (values != null) {
				return Collections.unmodifiableList(values);
			}
			return null;
		}

		private static String join(List<Double> values) {
			final StringBuilder sb = new StringBuilder();
			values.forEach(value -> sb.append(String.format("%.5f ", value)));
			return sb.toString();
		}

		private static String join(double[] values) {
			final StringBuilder sb = new StringBuilder();
			for (double value : values) {
				sb.append(String.format("%.5f ", value));
			}
			return sb.toString();
		}

		private static String join(double[][] values) {
			final StringBuilder sb = new StringBuilder();
			for (double[] value : values) {
				sb.append(join(value));
				sb.append("\n");
			}
			return sb.toString();
		}
	}
}
