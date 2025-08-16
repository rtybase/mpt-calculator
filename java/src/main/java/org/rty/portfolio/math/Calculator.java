package org.rty.portfolio.math;

import java.math.BigDecimal;

import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.LUDecomposition;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.SingularValueDecomposition;
import org.apache.commons.math3.stat.correlation.Covariance;
import org.apache.commons.math3.stat.correlation.PearsonsCorrelation;
import org.rty.portfolio.core.PortflioOptimalResults;

import com.google.common.base.Preconditions;

public class Calculator {
	private static final double CORRECTION = 1000000.0D;
	private static final double ERROR = 0.0000001D;

	/**
	 * @param portfolioId	can be null, ie. optional
	 */
	public static PortflioOptimalResults calculateWeights(double[] rates, double[][] covariance, Integer portfolioId) {
		RealMatrix ratesVector = new Array2DRowRealMatrix(rates);
		RealMatrix covMatrix = new Array2DRowRealMatrix(covariance);
		RealMatrix unitVector = generateUnitVector(covMatrix.getRowDimension());
		RealMatrix covMatxInverse = getInverse(covMatrix);

		double portfolioVariance = calculatePortfolioVariance(unitVector, covMatxInverse);
		double portfolioReturn = calculatePorfolioReturn(unitVector, covMatxInverse, ratesVector, portfolioVariance);
		RealMatrix weights = covMatxInverse.multiply(unitVector).scalarMultiply(portfolioVariance);

		return new PortflioOptimalResults(portfolioReturn,
				portfolioVariance,
				weights.getColumn(0),
				portfolioId);
	}

	/**
	 * Calculated rates are in percentage.
	 */
	public static double calculateRate(double currentPrice, double previousPrice) {
		double rate = (100.0D * calculateChange(currentPrice, previousPrice)) / previousPrice;
		return Math.round(rate * CORRECTION) / CORRECTION;
	}

	public static double calculateChange(double currentPrice, double previousPrice) {
		double change = currentPrice - previousPrice;
		return Math.round(change * CORRECTION) / CORRECTION;
	}

	public static double calculateChangeFromRate(double currentPrice, double rate) {
		double noPercentRate = rate / 100.0D;
		double change = (currentPrice * noPercentRate) / (1 + noPercentRate);
		return Math.round(change * CORRECTION) / CORRECTION;
	}

	public static double calculateCovariance(double[] array1, double[] array2) {
		return new Covariance().covariance(array1, array2, false);
	}

	public static double calculateCorrelation(double[] array1, double[] array2) {
		return new PearsonsCorrelation().correlation(array1, array2);
	}

	public static double calculateCorrelationWithShift(double[] array1, double[] array2, int shift) {
		Preconditions.checkArgument(array1.length == array2.length, "Arrays must have the same length!");

		final int positiveValueForshift = Math.absExact(shift);
		final int resutArraySize = array1.length - positiveValueForshift;

		Preconditions.checkArgument(resutArraySize > 1, "Shift is too wide!");

		if (shift == 0) {
			return new PearsonsCorrelation().correlation(array1, array2);
		} else {
			double[] array1WithShift = new double[resutArraySize];
			double[] array2WithShift = new double[resutArraySize];

			if (shift > 0) {
				System.arraycopy(array1, 0, array1WithShift, 0, resutArraySize);
				System.arraycopy(array2, positiveValueForshift, array2WithShift, 0, resutArraySize);
			} else {
				System.arraycopy(array1, positiveValueForshift, array1WithShift, 0, resutArraySize);
				System.arraycopy(array2, 0, array2WithShift, 0, resutArraySize);
			}

			return new PearsonsCorrelation().correlation(array1WithShift, array2WithShift);
		}
	}

	public static double calculateCorrelation(double covariance, double variance1, double variance2) {
		double tmp = Math.sqrt(variance1 * variance2);

		if (tmp < ERROR) {
			return 0D;
		}
		return covariance / tmp;
	}

	public static double calculateDiffWithPrecision(double d1, double d2) {
		BigDecimal b1 = BigDecimal.valueOf(d1);
		BigDecimal b2 = BigDecimal.valueOf(d2);
		return b1.subtract(b2).doubleValue();
	}

	public static double calculateEpsSurprise(double reportedEps, double predictedEps) {
		double rate = (100.0D * calculateChange(reportedEps, predictedEps)) / Math.abs(predictedEps);
		return Math.round(rate * CORRECTION) / CORRECTION;
	}

	private static RealMatrix generateUnitVector(int rows) {
		RealMatrix unitVector = new Array2DRowRealMatrix(rows, 1);
		return unitVector.scalarAdd(1.0d);
	}

	private static double calculatePortfolioVariance(RealMatrix unitVector, RealMatrix covMatxInverse) {
		RealMatrix A = unitVector.transpose().multiply(covMatxInverse).multiply(unitVector);
		return 1 / A.getData()[0][0];
	}

	private static double calculatePorfolioReturn(RealMatrix unitVector, RealMatrix covMatxInverse,
			RealMatrix ratesVector, double portVariance) {
		RealMatrix B = unitVector.transpose().multiply(covMatxInverse).multiply(ratesVector);
		return B.getData()[0][0] * portVariance;
	}

	private static RealMatrix getInverse(RealMatrix matrix) {
		try {
			return new LUDecomposition(matrix).getSolver().getInverse();
		} catch (Exception e) {
			return new SingularValueDecomposition(matrix).getSolver().getInverse();
		}
	}
}
