package org.rty.portfolio.core;

public class PortflioStats {
	private final double portfolioReturn;
	private final double porfolioVariance;
	private final double[] portfolioWeights;

	public PortflioStats(double portfolioReturn, double porfolioVariance, double[] portfolioWeights) {
		this.portfolioReturn = portfolioReturn;
		this.porfolioVariance = porfolioVariance;
		this.portfolioWeights = portfolioWeights;
	}

	public double getPortfolioReturn() {
		return portfolioReturn;
	}

	public double getPorfolioVariance() {
		return porfolioVariance;
	}

	public double[] getPortfolioWeights() {
		return portfolioWeights;
	}
}
