package org.rty.portfolio.engine.impl.dbtask;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;

import org.apache.commons.math3.stat.StatUtils;
import org.apache.commons.math3.stat.correlation.Covariance;
import org.rty.portfolio.core.PortflioStats;
import org.rty.portfolio.core.utils.DatesAndSetUtil;
import org.rty.portfolio.engine.impl.dbtask.TwoAssetsStatsCalculationTask.TwoAssetsStatsCalculationResult;
import org.rty.portfolio.math.Calculator;

public class TwoAssetsStatsCalculationTask implements Callable<TwoAssetsStatsCalculationResult> {
	private final int asset1Id;
	private final int asset2Id;
	private final Set<String> dates;
	private final Map<String, Double> asset1Rates;
	private final Map<String, Double> asset2Rates;

	TwoAssetsStatsCalculationTask(int asset1Id, int asset2Id, Set<String> dates, Map<String, Double> asset1Rates, Map<String, Double> asset2Rates) {
		this.asset1Id = asset1Id;
		this.asset2Id = asset2Id;
		this.dates = dates;
		this.asset1Rates = asset1Rates;
		this.asset2Rates = asset2Rates;
	}

	@Override
	public TwoAssetsStatsCalculationResult call() throws Exception {
		// get the rates for the common dates
		double[] values1 = DatesAndSetUtil.getValuesByIndex(dates, asset1Rates);
		double[] values2 = DatesAndSetUtil.getValuesByIndex(dates, asset2Rates);

		double avg_r1 = StatUtils.mean(values1);
		double avg_r2 = StatUtils.mean(values2);

		double variance1 = StatUtils.populationVariance(values1);
		double variance2 = StatUtils.populationVariance(values2);

		double covariance = new Covariance().covariance(values1, values2, false);
		double correlation = Calculator.calculateCorrelation(covariance, variance1, variance2);

		PortflioStats portStats = calculatePortfolioStats(avg_r1, avg_r2, variance1, variance2, covariance);

		return new TwoAssetsStatsCalculationResult(
				asset1Id,
				asset2Id,
				values1,
				values2,
				avg_r1,
				avg_r2,
				variance1,
				variance2,
				covariance,
				correlation,
				portStats);
	}

	private static PortflioStats calculatePortfolioStats(double avg_r1, double avg_r2, double variance1,
			double variance2, double covariance) {
		double[][] covMatrix = new double[][] { { variance1, covariance }, { covariance, variance2 } };
		double[] rates = new double[] { avg_r1, avg_r2 };
		PortflioStats portStats = Calculator.calculateWeights(rates, covMatrix);
		return portStats;
	}

	public static class TwoAssetsStatsCalculationResult {
		public final int asset1Id;
		public final int asset2Id;

		public final double[] asset1Values;
		public final double[] asset2Values;

		public final double asset1Mean;
		public final double asset2Mean;

		public final double asset1Variance;
		public final double asset2Variance;

		public final double covariance;
		public final double correlation;

		public final PortflioStats portflioStats;

		private TwoAssetsStatsCalculationResult(int asset1Id, int asset2Id,
				double[] asset1Values, double[] asset2Values,
				double asset1Mean, double asset2Mean,
				double asset1Variance, double asset2Variance,
				double covariance,
				double correlation,
				PortflioStats portflioStats) {
			this.asset1Id = asset1Id;
			this.asset2Id = asset2Id;

			this.asset1Values = asset1Values;
			this.asset2Values = asset2Values;

			this.asset1Mean = asset1Mean;
			this.asset2Mean = asset2Mean;

			this.asset1Variance = asset1Variance;
			this.asset2Variance = asset2Variance;

			this.covariance = covariance;
			this.correlation = correlation;
			this.portflioStats = portflioStats;
		}

		@Override
		public String toString() {
			return String.format("asset1=%d (size=%d), asset2=%d (size=%d); mean1=%.5f, mean2=%.5f;"
					+ " var1=%.5f, var2=%.5f; cov=%.5f; cor=%.5f;"
					+ " w1=%.5f, w2=%.5f, portRet=%.5f, portVar=%.5f",
					asset1Id,
					asset1Values.length,
					asset2Id,
					asset2Values.length,
					asset1Mean,
					asset2Mean,
					asset1Variance,
					asset2Variance,
					covariance,
					correlation,
					portflioStats.getPortfolioWeights()[0],
					portflioStats.getPortfolioWeights()[1],
					portflioStats.getPortfolioReturn(),
					portflioStats.getPorfolioVariance());
		}
	}
}
