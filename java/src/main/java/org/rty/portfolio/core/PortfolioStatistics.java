package org.rty.portfolio.core;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class PortfolioStatistics {
	private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

	public final List<Integer> assetIds;
	public final Set<String> dates;
	public final boolean hasSufficientContent;

	public final List<double[]> assetRates;
	public final List<Double> assetMeans;
	public final List<Double> assetVariances;
	public final double[][] covarianceMatrix;
	public final double[][] correlationMatrix;
	public final PortflioOptimalResults portflioOptimalResults;

	public PortfolioStatistics(List<Integer> assetIds, Set<String> dates, boolean hasSufficientContent,
			List<double[]> assetRates, List<Double> assetMeans, List<Double> assetVariances,
			double[][] covarianceMatrix, double[][] correlationMatrix, PortflioOptimalResults portflioOptimalResults) {
		this.assetIds = Collections.unmodifiableList(assetIds);
		this.dates = dates;
		this.hasSufficientContent = hasSufficientContent;
		this.assetRates = toUnmodifiableList(assetRates);
		this.assetMeans = toUnmodifiableList(assetMeans);
		this.assetVariances = toUnmodifiableList(assetVariances);
		this.covarianceMatrix = covarianceMatrix;
		this.correlationMatrix = correlationMatrix;
		this.portflioOptimalResults = portflioOptimalResults;
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

	private static <T> List<T> toUnmodifiableList(List<T> values) {
		if (values != null) {
			return Collections.unmodifiableList(values);
		}
		return null;
	}
}
