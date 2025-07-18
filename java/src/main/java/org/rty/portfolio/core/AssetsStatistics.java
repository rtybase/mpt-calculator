package org.rty.portfolio.core;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class AssetsStatistics {
	private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

	public final List<Integer> assetIds;
	public final Set<String> dates;
	public final boolean hasSufficientContent;

	public final List<double[]> assetValues;
	public final List<Double> assetMeans;
	public final List<Double> assetVariances;
	public final double[][] covarianceMatrix;
	public final double[][] correlationMatrix;
	public final PortflioStats portflioStats;

	public AssetsStatistics(List<Integer> assetIds, Set<String> dates, boolean hasSufficientContent,
			List<double[]> assetValues, List<Double> assetMeans, List<Double> assetVariances,
			double[][] covarianceMatrix, double[][] correlationMatrix, PortflioStats portflioStats) {
		this.assetIds = Collections.unmodifiableList(assetIds);
		this.dates = dates;
		this.hasSufficientContent = hasSufficientContent;
		this.assetValues = toUnmodifiableList(assetValues);
		this.assetMeans = toUnmodifiableList(assetMeans);
		this.assetVariances = toUnmodifiableList(assetVariances);
		this.covarianceMatrix = covarianceMatrix;
		this.correlationMatrix = correlationMatrix;
		this.portflioStats = portflioStats;
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
