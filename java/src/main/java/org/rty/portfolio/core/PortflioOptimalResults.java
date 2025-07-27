package org.rty.portfolio.core;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class PortflioOptimalResults {
	private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

	public final Integer portfolioId;

	public final double portfolioReturn;
	public final double porfolioVariance;
	public final double[] portfolioWeights;

	public PortflioOptimalResults(double portfolioReturn, double porfolioVariance, double[] portfolioWeights, Integer portfolioId) {
		this.portfolioReturn = portfolioReturn;
		this.porfolioVariance = porfolioVariance;
		this.portfolioWeights = portfolioWeights;
		this.portfolioId = portfolioId;
	}

	@Override
	public String toString() {
		try {
			return OBJECT_MAPPER.writeValueAsString(this);
		} catch (JsonProcessingException e) {
			e.printStackTrace();
			return "";
		}
	}
}
