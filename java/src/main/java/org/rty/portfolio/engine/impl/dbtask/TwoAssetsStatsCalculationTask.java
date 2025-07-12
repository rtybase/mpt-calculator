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
	private final int asset1Id;
	private final int asset2Id;
	private final Map<Integer, Map<String, Double>> storage;

	TwoAssetsStatsCalculationTask(Map<Integer, Map<String, Double>> storage, int asset1Id, int asset2Id) {
		this.storage = storage;
		this.asset1Id = asset1Id;
		this.asset2Id = asset2Id;
	}

	@Override
	public AssetsStatsCalculationResult call() throws Exception {
		final Map<String, Double> asset1Rates = storage.get(asset1Id);
		final Map<String, Double> asset2Rates = storage.get(asset2Id);
		final Set<String> dates = DatesAndSetUtil.computeCommonValues(asset1Rates.keySet(), asset2Rates.keySet());

		if (DatesAndSetUtil.hasSufficientContent(dates)) {
			// get the rates for the common dates
			double[] values1 = DatesAndSetUtil.getValuesByIndex(dates, asset1Rates);
			double[] values2 = DatesAndSetUtil.getValuesByIndex(dates, asset2Rates);

			double avg_r1 = StatUtils.mean(values1);
			double avg_r2 = StatUtils.mean(values2);

			double variance1 = StatUtils.populationVariance(values1);
			double variance2 = StatUtils.populationVariance(values2);

			double covariance = new Covariance().covariance(values1, values2, false);
			double correlation = Calculator.calculateCorrelation(covariance, variance1, variance2);

			double[][] covarianceMatrix = new double[][] { { variance1, covariance }, { covariance, variance2 } };
			double[][] correlationMatrix = new double[][] { { 1D, correlation }, { correlation, 1D } };

			PortflioStats portStats = calculatePortfolioStats(avg_r1, avg_r2, covarianceMatrix);

			return new AssetsStatsCalculationResult(List.of(asset1Id, asset2Id), true, 
					List.of(values1, values2),
					List.of(avg_r1, avg_r2),
					List.of(variance1, variance2),
					covarianceMatrix,
					correlationMatrix,
					portStats);
		} else {
			return new AssetsStatsCalculationResult(List.of(asset1Id, asset2Id), false,
					null,
					null,
					null,
					null,
					null,
					null);
		}
	}

	private static PortflioStats calculatePortfolioStats(double avg_r1, double avg_r2, double[][] covarianceMatrix) {
		final double[] rates = new double[] { avg_r1, avg_r2 };
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
