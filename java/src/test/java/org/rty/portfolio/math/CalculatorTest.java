package org.rty.portfolio.math;

import static org.junit.jupiter.api.Assertions.assertEquals;

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
		assertEquals(rate, RATE, ERROR);
	}

	@Test
	void testChangeFromRateCalculation() {
		double change = Calculator.calculateChangeFromRate(CURRENT_PRICE, RATE);
		assertEquals(change, CHANGE, ERROR);
	}

	@Test
	void testCorrelatioCalculation() {
		double correlation = Calculator.calculateCorrelation(1.0D, 2.0D, 2.0D);
		assertEquals(correlation, 0.5D, ERROR);
	}

	@Test
	void testZeroCorrelatioCalculation() {
		double correlation = Calculator.calculateCorrelation(1.0D, 0.0D, 2.0D);
		assertEquals(correlation, 0.0D, ERROR);
	}
}
