package org.rty.portfolio.engine.impl.dbtask;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Arrays;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.rty.portfolio.engine.impl.dbtask.TwoAssetsStatsCalculationTask.TwoAssetsStatsCalculationResult;

class TwoAssetsStatsCalculationTaskTest {
	private static final double ERROR_TOLERANCE = 0.000001D;

	@Test
	void testCalculationResults() throws Exception {
		final TwoAssetsStatsCalculationTask task = new TwoAssetsStatsCalculationTask(1, 2,
				Set.of("1", "2", "3"),
				Map.of("1", 1D, "2", 2D, "3", 3D),
				Map.of("1", 2D, "2", 4D, "3", 6D));

		TwoAssetsStatsCalculationResult result = task.call();

		Arrays.sort(result.asset1Values);
		Arrays.sort(result.asset2Values);
		assertArrayEquals(new double[] { 1D, 2D, 3D }, result.asset1Values);
		assertArrayEquals(new double[] { 2D, 4D, 6D }, result.asset2Values);

		assertEquals(1, result.asset1Id);
		assertEquals(2, result.asset2Id);

		assertEquals(2D, result.asset1Mean, ERROR_TOLERANCE);
		assertEquals(4D, result.asset2Mean, ERROR_TOLERANCE);

		assertEquals(0.666666D, result.asset1Variance, ERROR_TOLERANCE);
		assertEquals(2.666666D, result.asset2Variance, ERROR_TOLERANCE);

		assertEquals(1.333333D, result.covariance, ERROR_TOLERANCE);
		assertEquals(1D, result.correlation, ERROR_TOLERANCE);
		
		assertEquals(3.333333D, result.portflioStats.getPortfolioReturn(), ERROR_TOLERANCE);
		assertEquals(1.851851D, result.portflioStats.getPorfolioVariance(), ERROR_TOLERANCE);

		assertEquals(0.333333D, result.portflioStats.getPortfolioWeights()[0], ERROR_TOLERANCE);
		assertEquals(0.666666D, result.portflioStats.getPortfolioWeights()[1], ERROR_TOLERANCE);

		assertEquals("asset1=1 (size=3), asset2=2 (size=3);"
				+ " mean1=2.00000, mean2=4.00000; var1=0.66667, var2=2.66667;"
				+ " cov=1.33333; cor=1.00000; w1=0.33333, w2=0.66667,"
				+ " portRet=3.33333, portVar=1.85185", result.toString());
	}
}
