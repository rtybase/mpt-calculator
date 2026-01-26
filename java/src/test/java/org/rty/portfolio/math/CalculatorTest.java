package org.rty.portfolio.math;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class CalculatorTest {
	private static final double CHANGE = -10.0D;
	private static final double RATE = -50.0D;
	private static final double ERROR_TOLERANCE = 0.00001;
	private static final double PREVIOUS_PRICE = 20.0D;
	private static final double CURRENT_PRICE = 10.0D;

	@Test
	void testChangeCalculation() {
		double change = Calculator.calculateChange(CURRENT_PRICE, PREVIOUS_PRICE);
		assertEquals(change, CHANGE, ERROR_TOLERANCE);
	}

	@Test
	void testRateCalculation() {
		double rate = Calculator.calculateRate(CURRENT_PRICE, PREVIOUS_PRICE);
		assertEquals(RATE, rate, ERROR_TOLERANCE);
	}

	@Test
	void testChangeFromRateCalculation() {
		double change = Calculator.calculateChangeFromRate(CURRENT_PRICE, RATE);
		assertEquals(CHANGE, change, ERROR_TOLERANCE);
	}

	@Test
	void testCorrelatioCalculation() {
		double correlation = Calculator.calculateCorrelation(1.0D, 2.0D, 2.0D);
		assertEquals(0.5D, correlation, ERROR_TOLERANCE);
	}

	@Test
	void testZeroCorrelatioCalculation() {
		double correlation = Calculator.calculateCorrelation(1.0D, 0.0D, 2.0D);
		assertEquals(0.0D, correlation, ERROR_TOLERANCE);
	}

	@Test
	void testCovarianceCalculation() {
		double covariance = Calculator.calculateCovariance(new double[] { 1D, 1D },
				new double[] { 2D, 2D });
		assertEquals(0.0D, covariance, ERROR_TOLERANCE);
	}

	@Test
	void testCorrelatioCalculationWithArrays() {
		double covariance = Calculator.calculateCorrelation(new double[] { 1D, 2D, 3D },
				new double[] { 2D, 4D, 6D });
		assertEquals(1D, covariance, ERROR_TOLERANCE);
	}

	@Test
	void testCalculateDiffWithPrecision() {
		assertEquals("0.18", "" + Calculator.calculateDiffWithPrecision(1.23, 1.05));
	}

	@Test
	void testCalculateEpsSurprise() {
		double epsSurprise = Calculator.calculateEpsSurprise(1D, 0.92D);
		assertEquals(8.695652D, epsSurprise, ERROR_TOLERANCE);

		epsSurprise = Calculator.calculateEpsSurprise(-0.92D, -1D);
		assertEquals(8D, epsSurprise, ERROR_TOLERANCE);

		epsSurprise = Calculator.calculateEpsSurprise(1D, 0D);
		assertEquals(Calculator.MAX_VALUE, epsSurprise, ERROR_TOLERANCE);

		epsSurprise = Calculator.calculateEpsSurprise(-1D, 0D);
		assertEquals(-Calculator.MAX_VALUE, epsSurprise, ERROR_TOLERANCE);
	}

	@Test
	void testCalculatePriceOverEps() {
		double pe = Calculator.calculatePriceOverEps(1D, 0.5D);
		assertEquals(2D, pe, ERROR_TOLERANCE);

		pe = Calculator.calculatePriceOverEps(1D, -0.5D);
		assertEquals(0D, pe, ERROR_TOLERANCE);

		pe = Calculator.calculatePriceOverEps(1D, 0D);
		assertEquals(0D, pe, ERROR_TOLERANCE);
	}

	@Test
	void testCalculateDividendYield() {
		double yield = Calculator.calculateDividendYield(1D, 2D);
		assertEquals(50D, yield, ERROR_TOLERANCE);

		yield = Calculator.calculateDividendYield(1D, 0D);
		assertEquals(0D, yield, ERROR_TOLERANCE);
	}
}
