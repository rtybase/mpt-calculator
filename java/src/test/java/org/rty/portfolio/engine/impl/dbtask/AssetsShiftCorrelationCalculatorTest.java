package org.rty.portfolio.engine.impl.dbtask;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;

import org.junit.jupiter.api.Test;
import org.rty.portfolio.core.AssetsCorrelationInfo;

class AssetsShiftCorrelationCalculatorTest {
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

		assertEquals("{\"asset1Id\":1,\"asset2Id\":2,"
				+ "\"hasSufficientContent\":false,\"bestShift\":-2147483648,"
				+ "\"bestCorrelation\":\"NaN\"}", result.toString());
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

		assertEquals("{\"asset1Id\":1,\"asset2Id\":2,"
				+ "\"hasSufficientContent\":true,\"bestShift\":-2,"
				+ "\"bestCorrelation\":1.0}", result.toString());
	}
}
