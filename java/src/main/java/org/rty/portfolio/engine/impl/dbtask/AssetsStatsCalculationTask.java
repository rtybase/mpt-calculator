package org.rty.portfolio.engine.impl.dbtask;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;

import org.apache.commons.math3.stat.StatUtils;
import org.apache.commons.math3.stat.correlation.Covariance;
import org.rty.portfolio.core.PortflioStats;
import org.rty.portfolio.core.utils.DatesAndSetUtil;
import org.rty.portfolio.engine.impl.dbtask.AssetsStatsCalculationTask.AssetsStatsCalculationResult;
import org.rty.portfolio.math.Calculator;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class AssetsStatsCalculationTask implements Callable<AssetsStatsCalculationResult> {
	private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

	private final List<Integer> assetIds;
	private final Map<Integer, Map<String, Double>> storage;

	AssetsStatsCalculationTask(Map<Integer, Map<String, Double>> storage, List<Integer> assetIds) {
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

			return new AssetsStatsCalculationResult(assetIds, dates,
					true, 
					values,
					means,
					variances,
					covarianceMatrix,
					correlationMatrix,
					portStats);
		} else {
			return new AssetsStatsCalculationResult(assetIds, dates,
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

	public static class AssetsStatsCalculationResult {
		public final List<Integer> assetIds;
		public final Set<String> dates;
		public final boolean hasSufficientContent;

		public final List<double[]> assetValues;
		public final List<Double> assetMeans;
		public final List<Double> assetVariances;
		public final double[][] covarianceMatrix;
		public final double[][] correlationMatrix;
		public final PortflioStats portflioStats;

		private AssetsStatsCalculationResult(List<Integer> assetIds,
				Set<String> dates,
				boolean hasSufficientContent,
				List<double[]> assetValues,
				List<Double> assetMeans,
				List<Double> assetVariances,
				double[][] covarianceMatrix,
				double[][] correlationMatrix,
				PortflioStats portflioStats) {
			this.assetIds = Collections.unmodifiableList(assetIds);
			this.dates = dates;
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
			try {
				return OBJECT_MAPPER.writeValueAsString(this);
			} catch (JsonProcessingException e) {
				e.printStackTrace();
				return "";
			}
		}

		private static <T> List<T> toUnmodifiableList(List<T> values) {
			if (values != null) {
				return Collections.unmodifiableList(values);
			}
			return null;
		}
	}
}
