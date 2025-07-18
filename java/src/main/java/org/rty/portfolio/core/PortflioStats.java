package org.rty.portfolio.core;

public class PortflioStats {
	public final double portfolioReturn;
	public final double porfolioVariance;
	public final double[] portfolioWeights;

	public PortflioStats(double portfolioReturn, double porfolioVariance, double[] portfolioWeights) {
		this.portfolioReturn = portfolioReturn;
		this.porfolioVariance = porfolioVariance;
		this.portfolioWeights = portfolioWeights;
	}
}
