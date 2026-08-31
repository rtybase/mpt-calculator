package org.rty.portfolio.engine.impl.dbtask;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;

import org.junit.jupiter.api.Test;
import org.rty.portfolio.core.AssetsCorrelationInfo;
import org.rty.portfolio.core.utils.CommonTestRoutines;
import org.rty.portfolio.engine.impl.dbtask.AssetsShiftCorrelationCalculator.ShiftCorrelationComputationResult;

class AssetsShiftCorrelationCalculatorTest extends CommonTestRoutines {
	private static final double[] TEST_ARRAY1 = new double[] { 1D, 2D, 3D };
	private static final double[] TEST_ARRAY2 = new double[] { 2D, 4D, 6D };

	private static final double ERROR_TOLERANCE = 0.000001D;

	@Test
	void testCalculateResultsWithInsufficientContent() {
		final AssetsShiftCorrelationCalculator task = new AssetsShiftCorrelationCalculator(
				Map.of(1, Map.of("1", 1D, "2", 2D),
						2, Map.of("1", 2D, "2", 4D)),
				1, 2, Integer.MAX_VALUE);

		final AssetsCorrelationInfo result = task.call();

		assertEquals(1, result.predictorId);
		assertEquals(2, result.predictandId);
		assertFalse(result.hasSufficientContent);
		assertEquals(Integer.MIN_VALUE, result.bestShift);
		assertTrue(Double.isNaN(result.bestCorrelation));
		assertNull(result.lastCommonDate);

		assertEquals("{\"predictorId\":1,\"predictandId\":2,\"hasSufficientContent\":false,"
				+ "\"bestShift\":-2147483648,\"bestCorrelation\":\"NaN\","
				+ "\"dates\":[\"1\",\"2\"]}", result.toString());
	}

	@Test
	void testCalculateResults() {
		final AssetsShiftCorrelationCalculator task = new AssetsShiftCorrelationCalculator(
				Map.of(1, Map.of("1", 1D, "2", 2D, "3", 3D, "4", 4D, "5", 5D),
						2, Map.of("1", 2D, "2", 4D, "3", 6D, "4", 8D, "5", 10D)),
				1, 2, Integer.MAX_VALUE);

		final AssetsCorrelationInfo result = task.call();

		assertEquals(2, result.predictorId);
		assertEquals(1, result.predictandId);
		assertTrue(result.hasSufficientContent);
		assertEquals(2, result.bestShift);
		assertEquals(1D, result.bestCorrelation, ERROR_TOLERANCE);
		assertNull(result.lastCommonDate);

		assertEquals("{\"predictorId\":2,\"predictandId\":1,\"hasSufficientContent\":true,"
				+ "\"bestShift\":2,\"bestCorrelation\":1.0,"
				+ "\"dates\":[\"1\",\"2\",\"3\",\"4\",\"5\"],"
				+ "\"predictorRates\":[2.0,4.0,6.0,8.0,10.0],"
				+ "\"predictandRates\":[1.0,2.0,3.0,4.0,5.0]}", result.toString());
	}

	@Test
	void testCalculateResultsWithThreshold() {
		final Map<String, Double> map1 = Map.of("2025-07-01", 1D, "2025-07-02", 2D, "2025-07-03", 3D, "2025-07-04", 4D,
				"2025-07-05", 5D);
		final Map<String, Double> map2 = Map.of("2025-07-01", 10D, "2025-07-02", 10.1D, "2025-07-03", 1D, "2025-07-04",
				1.1D, "2025-07-05", 1.2D);

		AssetsShiftCorrelationCalculator task = new AssetsShiftCorrelationCalculator(Map.of(1, map1, 2, map2), 1, 2,
				Integer.MAX_VALUE);

		AssetsCorrelationInfo result = task.call();

		assertTrue(result.hasSufficientContent);
		assertEquals(1, result.predictorId);
		assertEquals(2, result.predictandId);
		assertEquals(2, result.bestShift);
		assertEquals(dateFrom(5), result.lastCommonDate);

		final int threshold = -2;
		task = new AssetsShiftCorrelationCalculator(Map.of(1, map1, 2, map2), 1, 2, threshold);

		result = task.call();

		assertTrue(result.hasSufficientContent);
		assertEquals(2, result.predictorId);
		assertEquals(1, result.predictandId);
		assertEquals(1, result.bestShift);
		assertEquals(dateFrom(5), result.lastCommonDate);
	}

	@Test
	void testCorrelatioCalculationWithShiftTooWide() {
		final IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
				() -> AssetsShiftCorrelationCalculator.calculateCorrelationWithShift(TEST_ARRAY1,
						TEST_ARRAY2,
						2));
		assertEquals("Shift is too wide!", ex.getMessage());
	}

	@Test
	void testCorrelatioCalculationWithZeroShift() {
		final ShiftCorrelationComputationResult result = AssetsShiftCorrelationCalculator
				.calculateCorrelationWithShift(TEST_ARRAY1, TEST_ARRAY2, 0);

		assertEquals(1D, result.correlation, ERROR_TOLERANCE);
		assertEquals(1D, result.absCorrelation, ERROR_TOLERANCE);
		assertEquals(3D, result.valueForForecast, ERROR_TOLERANCE);

		assertEquals(0, result.shift);
		assertEquals(0, result.absShift);

		assertArrayEquals(TEST_ARRAY1, result.array1WithShift);
		assertArrayEquals(TEST_ARRAY2, result.array2WithShift);
	}

	@Test
	void testCorrelatioCalculationWithPositiveShift() {
		final ShiftCorrelationComputationResult result = AssetsShiftCorrelationCalculator
				.calculateCorrelationWithShift(new double[] { 1D, 2D, 3D, 22D },
						new double[] { 0D, 2D, 4D, 6D }, 1);

		assertEquals(1D, result.correlation, ERROR_TOLERANCE);
		assertEquals(1D, result.absCorrelation, ERROR_TOLERANCE);
		assertEquals(22D, result.valueForForecast, ERROR_TOLERANCE);

		assertEquals(1, result.shift);
		assertEquals(1, result.absShift);

		assertArrayEquals(TEST_ARRAY1, result.array1WithShift);
		assertArrayEquals(TEST_ARRAY2, result.array2WithShift);
	}

	@Test
	void testCorrelatioCalculationWithNegativeShift() {
		final ShiftCorrelationComputationResult result = AssetsShiftCorrelationCalculator
				.calculateCorrelationWithShift(new double[] { 0D, 1D, 2D, 3D },
						new double[] { 2D, 4D, 6D, 33D }, -1);

		assertEquals(1D, result.correlation, ERROR_TOLERANCE);
		assertEquals(1D, result.absCorrelation, ERROR_TOLERANCE);
		assertEquals(33D, result.valueForForecast, ERROR_TOLERANCE);

		assertEquals(-1, result.shift);
		assertEquals(1, result.absShift);

		assertArrayEquals(TEST_ARRAY1, result.array1WithShift);
		assertArrayEquals(TEST_ARRAY2, result.array2WithShift);
	}

	@Test
	void testNoForecastForLargeShift() {
		final ShiftCorrelationComputationResult computationResult = AssetsShiftCorrelationCalculator
				.calculateCorrelationWithShift(new double[] { 1D, 2D, 3D, 0D, 0D },
						new double[] { 0D, 0D, 2D, 4D, 6D }, 2);

		final double[] result = AssetsShiftCorrelationCalculator.calculateForecast(computationResult);
		assertNull(result);
	}

	@Test
	void testForecastForPositiveShift() {
		final ShiftCorrelationComputationResult computationResult = AssetsShiftCorrelationCalculator
				.calculateCorrelationWithShift(new double[] { 1D, 2D, 3D, 4D },
						new double[] { 0D, 2D, 4D, 6D }, 1);

		assertEquals(1D, computationResult.correlation, ERROR_TOLERANCE);
		assertEquals(4D, computationResult.valueForForecast, ERROR_TOLERANCE);

		final double[] result = AssetsShiftCorrelationCalculator.calculateForecast(computationResult);
		assertEquals(8D, result[0], ERROR_TOLERANCE);
		assertEquals(8D, result[1], ERROR_TOLERANCE);
	}

	@Test
	void testForecastForNegativeShift() {
		final ShiftCorrelationComputationResult computationResult = AssetsShiftCorrelationCalculator
				.calculateCorrelationWithShift(new double[] { 0D, 1D, 2D, 3D },
						new double[] { 2D, 4D, 6D, 8D }, -1);

		assertEquals(1D, computationResult.correlation, ERROR_TOLERANCE);
		assertEquals(8D, computationResult.valueForForecast, ERROR_TOLERANCE);

		final double[] result = AssetsShiftCorrelationCalculator.calculateForecast(computationResult);
		assertEquals(4D, result[0], ERROR_TOLERANCE);
		assertEquals(4D, result[1], ERROR_TOLERANCE);
	}

	@Test
	void testForecastForNegativeShift_1() {
		final ShiftCorrelationComputationResult computationResult = AssetsShiftCorrelationCalculator
				.calculateCorrelationWithShift(new double[] { 0D, 1D, 2D, 3D, 4D, 5D },
						new double[] { 2D, 4D, 6D, 7.5D, 10D, 12D }, -1);

		assertEquals(0.9977067946884691D, computationResult.correlation, ERROR_TOLERANCE);
		assertEquals(12D, computationResult.valueForForecast, ERROR_TOLERANCE);

		final double[] result = AssetsShiftCorrelationCalculator.calculateForecast(computationResult);
		assertEquals(5.853876344788695D, result[0], ERROR_TOLERANCE);
		assertEquals(6.42599426679744D, result[1], ERROR_TOLERANCE);
	}
}
