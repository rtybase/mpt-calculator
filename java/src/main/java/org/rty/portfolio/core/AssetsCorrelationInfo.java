package org.rty.portfolio.core;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class AssetsCorrelationInfo {
	private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

	public final int asset1Id;
	public final int asset2Id;
	public final boolean hasSufficientContent;

	public final int bestShift;
	public final double bestCorrelation;

	public AssetsCorrelationInfo(int asset1Id, int asset2Id, boolean hasSufficientContent, int bestShift, double bestCorrelation) {
		this.asset1Id = asset1Id;
		this.asset2Id = asset2Id;
		this.hasSufficientContent = hasSufficientContent;
		this.bestShift = bestShift;
		this.bestCorrelation = bestCorrelation;
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
