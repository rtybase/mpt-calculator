package org.rty.portfolio.math;

import java.util.LinkedList;
import java.util.List;

import org.apache.commons.math3.stat.StatUtils;

public class ZScoreCalculator {
	private final List<Double> values = new LinkedList<>();

	private double mean = 0D;
	private double standardDeviation = 0D;

	public void add(double value) {
		values.add(value);
	}

	public void reset() {
		values.clear();
		mean = 0D;
		standardDeviation = 0D;
	}

	public void calculateMeanAndStdDev() {
		if (values.size() > 0) {
			final double[] valuesArray = values.stream().mapToDouble(Double::doubleValue).toArray();

			final double variance = StatUtils.populationVariance(valuesArray);
			standardDeviation = Math.sqrt(variance);

			mean = StatUtils.mean(valuesArray);
		}
	}

	public double calculateZScore(double value) {
		if (standardDeviation < Calculator.ERROR) {
			return 0D;
		}

		return (value - mean) / standardDeviation;
	}
}
