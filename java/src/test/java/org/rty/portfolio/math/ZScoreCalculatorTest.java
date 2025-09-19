package org.rty.portfolio.math;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ZScoreCalculatorTest {
	private static final double ERROR_TOLERANCE = 0.00001;

	private ZScoreCalculator calculator = new ZScoreCalculator();

	@BeforeEach
	void setup() {
		calculator.reset();
	}

	@Test
	void testCalculationsWithoutData() {
		calculator.calculateMeanAndStdDev();
		final double result = calculator.calculateZScore(1D);

		assertEquals(0D, result, ERROR_TOLERANCE);
	}

	@Test
	void testCalculationsWithOneInput() {
		calculator.add(1D);
		calculator.calculateMeanAndStdDev();
		final double result = calculator.calculateZScore(1D);

		assertEquals(0D, result, ERROR_TOLERANCE);
	}

	@Test
	void testCalculationsWithZeroStdDev() {
		calculator.add(1D);
		calculator.add(1D);

		calculator.calculateMeanAndStdDev();
		final double result = calculator.calculateZScore(1D);

		assertEquals(0D, result, ERROR_TOLERANCE);
	}

	@Test
	void testCalculations() {
		calculator.add(2D);
		calculator.add(4D);

		calculator.calculateMeanAndStdDev();

		final double result1 = calculator.calculateZScore(0D);
		final double result2 = calculator.calculateZScore(1D);

		assertEquals(-3D, result1, ERROR_TOLERANCE);
		assertEquals(-2D, result2, ERROR_TOLERANCE);
		
	}
}
