package org.rty.portfolio.math;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class CalculatorTest {
	private static final double CHANGE = -10.0D;
	private static final double RATE = -50.0D;
	private static final double ERROR = 0.00001;
	private static final double PREVIOUS_PRICE = 20.0D;
	private static final double CURRENT_PRICE = 10.0D;

	@Test
	void testChangeCalculation() {
		double change = Calculator.calculateChange(CURRENT_PRICE, PREVIOUS_PRICE);
		assertEquals(change, CHANGE, ERROR);
	}

	@Test
	void testRateCalculation() {
		double rate = Calculator.calculateRate(CURRENT_PRICE, PREVIOUS_PRICE);
		assertEquals(RATE, rate, ERROR);
	}

	@Test
	void testChangeFromRateCalculation() {
		double change = Calculator.calculateChangeFromRate(CURRENT_PRICE, RATE);
		assertEquals(CHANGE, change, ERROR);
	}

	@Test
	void testCorrelatioCalculation() {
		double correlation = Calculator.calculateCorrelation(1.0D, 2.0D, 2.0D);
		assertEquals(0.5D, correlation, ERROR);
	}

	@Test
	void testZeroCorrelatioCalculation() {
		double correlation = Calculator.calculateCorrelation(1.0D, 0.0D, 2.0D);
		assertEquals(0.0D, correlation, ERROR);
	}

	@Test
	void testCovarianceCalculation() {
		double covariance = Calculator.calculateCovariance(new double[] { 1D, 1D },
				new double[] { 2D, 2D });
		assertEquals(0.0D, covariance, ERROR);
	}

	@Test
	void testCorrelatioCalculationWithArrays() {
		double covariance = Calculator.calculateCorrelation(new double[] { 1D, 2D, 3D },
				new double[] { 2D, 4D, 6D });
		assertEquals(1D, covariance, ERROR);
	}

	@Test
	void testCorrelatioCalculationWithShiftTooWide() {
		final IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
				() -> Calculator.calculateCorrelationWithShift(new double[] { 1D, 2D, 3D },
						new double[] { 2D, 4D, 6D },
						2));
		assertEquals("Shift is too wide!", ex.getMessage());
	}

	@Test
	void testCorrelatioCalculationWithZeroShift() {
		double covariance = Calculator.calculateCorrelationWithShift(new double[] { 1D, 2D, 3D },
				new double[] { 2D, 4D, 6D },
				0);
		assertEquals(1D, covariance, ERROR);
	}

	@Test
	void testCorrelatioCalculationWithPositiveShift() {
		double covariance = Calculator.calculateCorrelationWithShift(new double[] { 1D, 2D, 3D, 0D },
				new double[] { 0D, 2D, 4D, 6D },
				1);
		assertEquals(1D, covariance, ERROR);
	}

	@Test
	void testCorrelatioCalculationWithNegativeShift() {
		double covariance = Calculator.calculateCorrelationWithShift(new double[] { 0D, 1D, 2D, 3D },
				new double[] { 2D, 4D, 6D, 0D },
				-1);
		assertEquals(1D, covariance, ERROR);
	}
}
