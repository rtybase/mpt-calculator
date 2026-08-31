package org.rty.portfolio.core;

import java.util.Date;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class AssetsCorrelationInfo {
	private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

	static {
		OBJECT_MAPPER.setDefaultPropertyInclusion(Include.NON_NULL);
	}

	public final int predictorId;
	public final int predictandId;
	public final boolean hasSufficientContent;
	public final int bestShift;
	public final double bestCorrelation;
	public final Double minRateForecast;
	public final Double maxRateForecast;
	public final Date lastCommonDate;

	public final Set<String> dates;
	public final double[] predictorRates;
	public final double[] predictandRates;

	public AssetsCorrelationInfo(int predictorId, int predictandId, boolean hasSufficientContent, int bestShift,
			double bestCorrelation, Set<String> date, double[] predictorRates, double[] predictandRates,
			Double minRateForecast, Double maxRateForecast, Date lastCommonDate) {
		this.predictorId = predictorId;
		this.predictandId = predictandId;
		this.hasSufficientContent = hasSufficientContent;
		this.bestShift = bestShift;
		this.bestCorrelation = bestCorrelation;

		this.dates = date;
		this.predictorRates = predictorRates;
		this.predictandRates = predictandRates;

		this.minRateForecast = minRateForecast;
		this.maxRateForecast = maxRateForecast;
		this.lastCommonDate = lastCommonDate;
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
