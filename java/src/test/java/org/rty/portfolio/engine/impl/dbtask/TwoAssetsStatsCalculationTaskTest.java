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
	void testCalculationResultsWith2Assets() throws Exception {
		final TwoAssetsStatsCalculationTask task = new TwoAssetsStatsCalculationTask(
				Map.of(1, Map.of("1", 1D, "2", 2D, "3", 3D, "4", 4D, "5", 5D),
						2, Map.of("1", 2D, "2", 4D, "3", 6D, "4", 8D, "5", 10D)),
				List.of(1, 2));

		AssetsStatsCalculationResult result = task.call();

		assertTrue(result.hasSufficientContent);
		sortValuesForChecking(result);
		assertArrayEquals(new double[] { 1D, 2D, 3D, 4D, 5D }, result.assetValues.get(0));
		assertArrayEquals(new double[] { 2D, 4D, 6D, 8D, 10D }, result.assetValues.get(1));

		verifyAssetIds(result);
		verifyListsOfDoublesAreEqual(List.of(3D, 6D), result.assetMeans);
		verifyListsOfDoublesAreEqual(List.of(2D, 8D), result.assetVariances);

		assertEquals(4D, result.covarianceMatrix[0][1], ERROR_TOLERANCE);
		assertEquals(1D, result.correlationMatrix[0][1], ERROR_TOLERANCE);

		assertEquals(4.999999D, result.portflioStats.getPortfolioReturn(), ERROR_TOLERANCE);
		assertEquals(5.555555D, result.portflioStats.getPorfolioVariance(), ERROR_TOLERANCE);

		verifyListsOfDoublesAreEqual(List.of(0.333333D, 0.666666D), result.portflioStats.getPortfolioWeights());

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
	void testCalculationResultsWith3Assets() throws Exception {
		final TwoAssetsStatsCalculationTask task = new TwoAssetsStatsCalculationTask(
				Map.of(1, Map.of("1", 1D, "2", 2D, "3", 3D, "4", 4D, "5", 5D),
						2, Map.of("1", 2D, "2", 4D, "3", 6D, "4", 8D, "5", 10D),
						3, Map.of("1", 3D, "2", 6D, "3", 9D, "4", 12D, "5", 15D)),
				List.of(1, 2, 3));

		AssetsStatsCalculationResult result = task.call();

		assertTrue(result.hasSufficientContent);
		sortValuesForChecking(result);
		assertArrayEquals(new double[] { 1D, 2D, 3D, 4D, 5D }, result.assetValues.get(0));
		assertArrayEquals(new double[] { 2D, 4D, 6D, 8D, 10D }, result.assetValues.get(1));
		assertArrayEquals(new double[] { 3D, 6D, 9D, 12D, 15D }, result.assetValues.get(2));

		verifyAssetIds(result);
		verifyListsOfDoublesAreEqual(List.of(3D, 6D, 9D), result.assetMeans);
		verifyListsOfDoublesAreEqual(List.of(2D, 8D, 18D), result.assetVariances);

		assertEquals(4D, result.covarianceMatrix[0][1], ERROR_TOLERANCE);
		assertEquals(6D, result.covarianceMatrix[0][2], ERROR_TOLERANCE);
		assertEquals(1D, result.correlationMatrix[0][1], ERROR_TOLERANCE);
		assertEquals(1D, result.correlationMatrix[0][2], ERROR_TOLERANCE);

		assertEquals(7D, result.portflioStats.getPortfolioReturn(), ERROR_TOLERANCE);
		assertEquals(10.888888D, result.portflioStats.getPorfolioVariance(), ERROR_TOLERANCE);

		verifyListsOfDoublesAreEqual(List.of(0.166666D, 0.333333D, 0.5D), result.portflioStats.getPortfolioWeights());

		assertEquals("assetIds=1 2 3\n"
				+ "assetMeans=3.00000 6.00000 9.00000 \n"
				+ "assetVariances=2.00000 8.00000 18.00000 \n"
				+ "cov=[2.00000 4.00000 6.00000 \n"
				+ "4.00000 8.00000 12.00000 \n"
				+ "6.00000 12.00000 18.00000 \n"
				+ "]\n"
				+ "cor=[1.00000 1.00000 1.00000 \n"
				+ "1.00000 1.00000 1.00000 \n"
				+ "1.00000 1.00000 1.00000 \n"
				+ "]\n"
				+ "assetWeights=0.16667 0.33333 0.50000 \n"
				+ "portRet=7.00000, portVar=10.88889", result.toString());
	}

	@Test
	void testCalculationResultsWithInsufficientContent() throws Exception {
		final TwoAssetsStatsCalculationTask task = new TwoAssetsStatsCalculationTask(
				Map.of(1, Map.of("1", 1D, "2", 2D),
						2, Map.of("1", 2D, "2", 4D)),
				List.of(1, 2));

		AssetsStatsCalculationResult result = task.call();

		verifyAssetIds(result);
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

	private static void sortValuesForChecking(AssetsStatsCalculationResult result) {
		for (int i = 0; i < result.assetValues.size(); i++) {
			Arrays.sort(result.assetValues.get(i));
		}
	}

	private static void verifyAssetIds(AssetsStatsCalculationResult result) {
		for (int i = 0; i < result.assetIds.size(); i++) {
			assertEquals(i + 1, result.assetIds.get(i));
		}
	}

	private static void verifyListsOfDoublesAreEqual(List<Double> expected, List<Double> actual) {
		assertEquals(expected.size(), actual.size());

		for (int i = 0; i < actual.size(); i++) {
			assertEquals(expected.get(i), actual.get(i), ERROR_TOLERANCE);
		}
	}

	private static void verifyListsOfDoublesAreEqual(List<Double> expected, double[] actual) {
		assertEquals(expected.size(), actual.length);

		for (int i = 0; i < actual.length; i++) {
			assertEquals(expected.get(i), actual[i], ERROR_TOLERANCE);
		}
	}
}
