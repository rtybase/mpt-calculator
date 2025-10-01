package org.rty.portfolio.math;

import java.math.BigDecimal;

import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.LUDecomposition;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.SingularValueDecomposition;
import org.apache.commons.math3.stat.correlation.Covariance;
import org.apache.commons.math3.stat.correlation.PearsonsCorrelation;
import org.rty.portfolio.core.PortflioOptimalResults;

public class Calculator {
	static final double MAX_VALUE = Double.MAX_VALUE - 1D;
	static final double ERROR = 0.0000001D;

	private static final double CORRECTION = 1000000.0D;

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
		return round(rate, CORRECTION);
	}

	public static double calculateChange(double currentPrice, double previousPrice) {
		double change = currentPrice - previousPrice;
		return round(change, CORRECTION);
	}

	public static double calculateChangeFromRate(double currentPrice, double rate) {
		double noPercentRate = rate / 100.0D;
		double change = (currentPrice * noPercentRate) / (1 + noPercentRate);
		return round(change, CORRECTION);
	}

	public static double calculateCovariance(double[] array1, double[] array2) {
		return new Covariance().covariance(array1, array2, false);
	}

	public static double calculateCorrelation(double[] array1, double[] array2) {
		return new PearsonsCorrelation().correlation(array1, array2);
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
		final double absValue = Math.abs(predictedEps);
		final double diff = calculateChange(reportedEps, predictedEps);

		if (absValue < ERROR) {
			if (diff < 0D) {
				return -MAX_VALUE;
			}
			return MAX_VALUE;
		}

		double rate = (100.0D * diff) / absValue;
		return round(rate, CORRECTION);
	}

	public static double calculatePriceOverEps(double price, double eps) {
		final double absValue = Math.abs(eps);

		if (eps <= 0D) {
			return 0D;
		}

		double pe = price / absValue;
		return round(pe, CORRECTION);
	}

	public static double round(double value, double correction) {
		return Math.round(value * correction) / correction;
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
