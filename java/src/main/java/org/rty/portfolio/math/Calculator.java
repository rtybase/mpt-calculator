package org.rty.portfolio.math;

import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.LUDecomposition;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.SingularValueDecomposition;
import org.rty.portfolio.core.PortflioStats;

public class Calculator {
	private static final double CORRECTION = 1000000.0D;
	private static final double ERROR = 0.0000001D;

	public static PortflioStats calculateWeights(double[] rates, double[][] covariance) {
		RealMatrix ratesVector = new Array2DRowRealMatrix(rates);
		RealMatrix covMatrix = new Array2DRowRealMatrix(covariance);
		RealMatrix unitVector = generateUnitVector(covMatrix.getRowDimension());
		RealMatrix covMatxInverse = getInverse(covMatrix);

		double portfolioVariance = calculatePortfolioVariance(unitVector, covMatxInverse);
		double portfolioReturn = calculatePorfolioReturn(unitVector, covMatxInverse, ratesVector, portfolioVariance);
		RealMatrix weights = covMatxInverse.multiply(unitVector).scalarMultiply(portfolioVariance);

		return new PortflioStats(portfolioReturn, portfolioVariance, weights.getColumn(0));
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

	public static double calculateCorrelation(double covariance, double variance1, double variance2) {
		double tmp = Math.sqrt(variance1 * variance2);

		if (tmp < ERROR) {
			return 0D;
		}
		return covariance / tmp;
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
