package org.rty.portfolio.engine.impl.dbtask;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;

import org.junit.jupiter.api.Test;
import org.rty.portfolio.core.AssetsCorrelationInfo;
import org.rty.portfolio.engine.impl.dbtask.AssetsShiftCorrelationCalculator.ShiftCorrelationComputationResult;

class AssetsShiftCorrelationCalculatorTest {
	private static final double[] TEST_ARRAY2 = new double[] { 2D, 4D, 6D };
	private static final double[] TEST_ARRAY1 = new double[] { 1D, 2D, 3D };

	private static final double ERROR_TOLERANCE = 0.000001D;

	@Test
	void testCalculateResultsWithInsufficientContent() {
		final AssetsShiftCorrelationCalculator task = new AssetsShiftCorrelationCalculator(
				Map.of(1, Map.of("1", 1D, "2", 2D),
						2, Map.of("1", 2D, "2", 4D)),
				1, 2);

		final AssetsCorrelationInfo result = task.call();

		assertEquals(1, result.asset1Id);
		assertEquals(2, result.asset2Id);
		assertFalse(result.hasSufficientContent);
		assertEquals(Integer.MIN_VALUE, result.bestShift);
		assertTrue(Double.isNaN(result.bestCorrelation));

		assertEquals("{\"asset1Id\":1,\"asset2Id\":2,\"hasSufficientContent\":false,"
				+ "\"bestShift\":-2147483648,\"bestCorrelation\":\"NaN\","
				+ "\"dates\":[\"1\",\"2\"],"
				+ "\"asset1Rates\":null,\"asset2Rates\":null}", result.toString());
	}

	@Test
	void testCalculateResults() {
		final AssetsShiftCorrelationCalculator task = new AssetsShiftCorrelationCalculator(
				Map.of(1, Map.of("1", 1D, "2", 2D, "3", 3D, "4", 4D, "5", 5D),
						2, Map.of("1", 2D, "2", 4D, "3", 6D, "4", 8D, "5", 10D)),
				1, 2);

		final AssetsCorrelationInfo result = task.call();

		assertEquals(1, result.asset1Id);
		assertEquals(2, result.asset2Id);
		assertTrue(result.hasSufficientContent);
		assertEquals(-2, result.bestShift);
		assertEquals(1D, result.bestCorrelation, ERROR_TOLERANCE);

		assertEquals("{\"asset1Id\":1,\"asset2Id\":2,\"hasSufficientContent\":true,"
				+ "\"bestShift\":-2,\"bestCorrelation\":1.0,"
				+ "\"dates\":[\"1\",\"2\",\"3\",\"4\",\"5\"],"
				+ "\"asset1Rates\":[1.0,2.0,3.0,4.0,5.0],"
				+ "\"asset2Rates\":[2.0,4.0,6.0,8.0,10.0]}", result.toString());
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

		assertEquals(0, result.shift);
		assertEquals(0, result.absShift);

		assertArrayEquals(TEST_ARRAY1, result.array1WithShift);
		assertArrayEquals(TEST_ARRAY2, result.array2WithShift);
	}

	@Test
	void testCorrelatioCalculationWithPositiveShift() {
		final ShiftCorrelationComputationResult result = AssetsShiftCorrelationCalculator
				.calculateCorrelationWithShift(new double[] { 1D, 2D, 3D, 0D },
						new double[] { 0D, 2D, 4D, 6D }, 1);

		assertEquals(1D, result.correlation, ERROR_TOLERANCE);
		assertEquals(1D, result.absCorrelation, ERROR_TOLERANCE);

		assertEquals(1, result.shift);
		assertEquals(1, result.absShift);

		assertArrayEquals(TEST_ARRAY1, result.array1WithShift);
		assertArrayEquals(TEST_ARRAY2, result.array2WithShift);
	}

	@Test
	void testCorrelatioCalculationWithNegativeShift() {
		final ShiftCorrelationComputationResult result = AssetsShiftCorrelationCalculator
				.calculateCorrelationWithShift(new double[] { 0D, 1D, 2D, 3D },
						new double[] { 2D, 4D, 6D, 0D }, -1);

		assertEquals(1D, result.correlation, ERROR_TOLERANCE);
		assertEquals(1D, result.absCorrelation, ERROR_TOLERANCE);

		assertEquals(-1, result.shift);
		assertEquals(1, result.absShift);

		assertArrayEquals(TEST_ARRAY1, result.array1WithShift);
		assertArrayEquals(TEST_ARRAY2, result.array2WithShift);
	}
}
