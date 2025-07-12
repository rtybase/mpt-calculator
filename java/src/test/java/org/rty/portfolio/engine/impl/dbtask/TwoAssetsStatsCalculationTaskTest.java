package org.rty.portfolio.engine.impl.dbtask;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.rty.portfolio.engine.impl.dbtask.TwoAssetsStatsCalculationTask.AssetsStatsCalculationResult;

class TwoAssetsStatsCalculationTaskTest {
	private static final double ERROR_TOLERANCE = 0.000001D;

	@Test
	void testCalculationResults() throws Exception {
		final TwoAssetsStatsCalculationTask task = new TwoAssetsStatsCalculationTask(
				Map.of(1, Map.of("1", 1D, "2", 2D, "3", 3D, "4", 4D, "5", 5D),
						2, Map.of("1", 2D, "2", 4D, "3", 6D, "4", 8D, "5", 10D)),
				1, 2);

		AssetsStatsCalculationResult result = task.call();

		assertTrue(result.hasSufficientContent);
		Arrays.sort(result.assetValues.get(0));
		Arrays.sort(result.assetValues.get(1));
		assertArrayEquals(new double[] { 1D, 2D, 3D, 4D, 5D }, result.assetValues.get(0));
		assertArrayEquals(new double[] { 2D, 4D, 6D, 8D, 10D }, result.assetValues.get(1));

		assertEquals(1, result.assetIds.get(0));
		assertEquals(2, result.assetIds.get(1));

		assertEquals(3D, result.assetMeans.get(0), ERROR_TOLERANCE);
		assertEquals(6D, result.assetMeans.get(1), ERROR_TOLERANCE);

		assertEquals(2D, result.assetVariances.get(0), ERROR_TOLERANCE);
		assertEquals(8D, result.assetVariances.get(1), ERROR_TOLERANCE);

		assertEquals(4D, result.covarianceMatrix[0][1], ERROR_TOLERANCE);
		assertEquals(1D, result.correlationMatrix[0][1], ERROR_TOLERANCE);
		
		assertEquals(4.999999D, result.portflioStats.getPortfolioReturn(), ERROR_TOLERANCE);
		assertEquals(5.555555D, result.portflioStats.getPorfolioVariance(), ERROR_TOLERANCE);

		assertEquals(0.333333D, result.portflioStats.getPortfolioWeights()[0], ERROR_TOLERANCE);
		assertEquals(0.666666D, result.portflioStats.getPortfolioWeights()[1], ERROR_TOLERANCE);

		assertEquals("assetIds=1 2\n"
				+ "assetMeans=3.00000 6.00000 \n"
				+ "assetVariances=2.00000 8.00000 \n"
				+ "cov=[2.00000 4.00000 \n"
				+ "4.00000 8.00000 \n"
				+ "]\n"
				+ "cor=[1.00000 1.00000 \n"
				+ "1.00000 1.00000 \n"
				+ "]\n"
				+ "assetWeights=0.33333 0.66667 \n"
				+ "portRet=5.00000, portVar=5.55556", result.toString());
	}

	@Test
	void testCalculationResultsWithInsufficientContent() throws Exception {
		final TwoAssetsStatsCalculationTask task = new TwoAssetsStatsCalculationTask(
				Map.of(1, Map.of("1", 1D, "2", 2D),
						2, Map.of("1", 2D, "2", 4D)),
				1, 2);

		AssetsStatsCalculationResult result = task.call();

		assertEquals(List.of(1, 2), result.assetIds);
		assertFalse(result.hasSufficientContent);
		assertNull(result.assetValues);
		assertNull(result.assetMeans);
		assertNull(result.assetVariances);
		assertNull(result.covarianceMatrix);
		assertNull(result.correlationMatrix);
		assertNull(result.portflioStats);

		assertEquals("assetIds=1 2\n"
				+ "assetMeans=\n"
				+ "assetVariances=\n"
				+ "cov=[]\n"
				+ "cor=[]\n"
				+ "assetWeights=\n"
				+ "portRet=, portVar=", result.toString());
	}

}
