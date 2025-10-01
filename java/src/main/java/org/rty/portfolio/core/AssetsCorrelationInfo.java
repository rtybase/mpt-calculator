package org.rty.portfolio.core;

import java.util.Set;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class AssetsCorrelationInfo {
	private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

	static {
		OBJECT_MAPPER.setSerializationInclusion(Include.NON_NULL);
	}

	public final int asset1Id;
	public final int asset2Id;
	public final boolean hasSufficientContent;
	public final int bestShift;
	public final double bestCorrelation;

	public final Set<String> dates;
	public final double[] asset1Rates;
	public final double[] asset2Rates;

	public final double[] forecast;

	public AssetsCorrelationInfo(int asset1Id, int asset2Id, boolean hasSufficientContent, int bestShift,
			double bestCorrelation, Set<String> date, double[] asset1Rates, double[] asset2Rates, double[] forecast) {
		this.asset1Id = asset1Id;
		this.asset2Id = asset2Id;
		this.hasSufficientContent = hasSufficientContent;
		this.bestShift = bestShift;
		this.bestCorrelation = bestCorrelation;

		this.dates = date;
		this.asset1Rates = asset1Rates;
		this.asset2Rates = asset2Rates;

		this.forecast = forecast;
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
