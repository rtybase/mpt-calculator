package org.rty.portfolio.engine.impl.dbtask;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.rty.portfolio.core.AssetsStatistics;

class AssetsStatsCalculationTaskTest {
	private static final double ERROR_TOLERANCE = 0.000001D;

	@Test
	void testCalculationResultsWith2Assets() throws Exception {
		final AssetsStatsCalculationTask task = new AssetsStatsCalculationTask(
				Map.of(1, Map.of("1", 1D, "2", 2D, "3", 3D, "4", 4D, "5", 5D),
						2, Map.of("1", 2D, "2", 4D, "3", 6D, "4", 8D, "5", 10D)),
				List.of(1, 2));

		AssetsStatistics result = task.call();

		assertTrue(result.hasSufficientContent);
		assertArrayEquals(new double[] { 1D, 2D, 3D, 4D, 5D }, result.assetValues.get(0));
		assertArrayEquals(new double[] { 2D, 4D, 6D, 8D, 10D }, result.assetValues.get(1));

		verifyAssetIds(result);
		verifyListsOfDoublesAreEqual(List.of(3D, 6D), result.assetMeans);
		verifyListsOfDoublesAreEqual(List.of(2D, 8D), result.assetVariances);

		assertEquals(4D, result.covarianceMatrix[0][1], ERROR_TOLERANCE);
		assertEquals(1D, result.correlationMatrix[0][1], ERROR_TOLERANCE);

		assertEquals(4.999999D, result.portflioStats.portfolioReturn, ERROR_TOLERANCE);
		assertEquals(5.555555D, result.portflioStats.porfolioVariance, ERROR_TOLERANCE);

		verifyListsOfDoublesAreEqual(List.of(0.333333D, 0.666666D), result.portflioStats.portfolioWeights);

		assertEquals("{\"assetIds\":[1,2],\"dates\":[\"1\",\"2\",\"3\",\"4\",\"5\"],"
				+ "\"hasSufficientContent\":true,\"assetValues\":[[1.0,2.0,3.0,4.0,5.0],[2.0,4.0,6.0,8.0,10.0]],"
				+ "\"assetMeans\":[3.0,6.0],\"assetVariances\":[2.0,8.0],"
				+ "\"covarianceMatrix\":[[2.0,4.0],[4.0,8.0]],"
				+ "\"correlationMatrix\":[[1.0,1.0],[1.0,1.0]],"
				+ "\"portflioStats\":{\"portfolioReturn\":4.999999999999999,\"porfolioVariance\":5.555555555555555,"
				+ "\"portfolioWeights\":[0.3333333333333333,0.6666666666666666]}}", result.toString());
	}

	@Test
	void testCalculationResultsWith3Assets() throws Exception {
		final AssetsStatsCalculationTask task = new AssetsStatsCalculationTask(
				Map.of(1, Map.of("1", 1D, "2", 2D, "3", 3D, "4", 4D, "5", 5D),
						2, Map.of("1", 2D, "2", 4D, "3", 6D, "4", 8D, "5", 10D),
						3, Map.of("1", 3D, "2", 6D, "3", 9D, "4", 12D, "5", 15D)),
				List.of(1, 2, 3));

		AssetsStatistics result = task.call();

		assertTrue(result.hasSufficientContent);
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

		assertEquals(7D, result.portflioStats.portfolioReturn, ERROR_TOLERANCE);
		assertEquals(10.888888D, result.portflioStats.porfolioVariance, ERROR_TOLERANCE);

		verifyListsOfDoublesAreEqual(List.of(0.166666D, 0.333333D, 0.5D), result.portflioStats.portfolioWeights);

		assertEquals("{\"assetIds\":[1,2,3],\"dates\":[\"1\",\"2\",\"3\",\"4\",\"5\"],"
				+ "\"hasSufficientContent\":true,\"assetValues\":[[1.0,2.0,3.0,4.0,5.0],[2.0,4.0,6.0,8.0,10.0],[3.0,6.0,9.0,12.0,15.0]],"
				+ "\"assetMeans\":[3.0,6.0,9.0],\"assetVariances\":[2.0,8.0,18.0],"
				+ "\"covarianceMatrix\":[[2.0,4.0,6.0],[4.0,8.0,12.0],[6.0,12.0,18.0]],"
				+ "\"correlationMatrix\":[[1.0,1.0,1.0],[1.0,1.0,1.0],[1.0,1.0,1.0]],"
				+ "\"portflioStats\":{\"portfolioReturn\":7.000000000000001,\"porfolioVariance\":10.888888888888888,"
				+ "\"portfolioWeights\":[0.16666666666666669,0.33333333333333337,0.5]}}", result.toString());
	}

	@Test
	void testCalculationResultsWithInsufficientContent() throws Exception {
		final AssetsStatsCalculationTask task = new AssetsStatsCalculationTask(
				Map.of(1, Map.of("1", 1D, "2", 2D),
						2, Map.of("1", 2D, "2", 4D)),
				List.of(1, 2));

		AssetsStatistics result = task.call();

		verifyAssetIds(result);
		assertFalse(result.hasSufficientContent);
		assertNull(result.assetValues);
		assertNull(result.assetMeans);
		assertNull(result.assetVariances);
		assertNull(result.covarianceMatrix);
		assertNull(result.correlationMatrix);
		assertNull(result.portflioStats);

		assertEquals("{\"assetIds\":[1,2],\"dates\":[\"1\",\"2\"],"
				+ "\"hasSufficientContent\":false,\"assetValues\":null,"
				+ "\"assetMeans\":null,\"assetVariances\":null,"
				+ "\"covarianceMatrix\":null,\"correlationMatrix\":null,"
				+ "\"portflioStats\":null}", result.toString());
	}

	private static void verifyAssetIds(AssetsStatistics result) {
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
