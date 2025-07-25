package org.rty.portfolio.engine.impl.dbtask;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;

import org.apache.commons.math3.util.Pair;
import org.rty.portfolio.core.AssetsCorrelationInfo;
import org.rty.portfolio.core.utils.DatesAndSetUtil;
import org.rty.portfolio.math.Calculator;

public class AssetsShiftCorrelationCalculator implements Callable<AssetsCorrelationInfo> {
	private final int asset1Id;
	private final int asset2Id;
	private final Map<Integer, Map<String, Double>> storage;

	AssetsShiftCorrelationCalculator(Map<Integer, Map<String, Double>> storage, int asset1Id, int asset2Id) {
		this.storage = storage;
		this.asset1Id = asset1Id;
		this.asset2Id = asset2Id;
	}

	@Override
	public AssetsCorrelationInfo call() {
		final Map<String, Double> asset1Rates = storage.get(asset1Id);
		final Map<String, Double> asset2Rates = storage.get(asset2Id);

		final Set<String> dates = DatesAndSetUtil
				.computeCommonValues(List.of(asset1Rates.keySet(), asset2Rates.keySet()));

		Pair<Integer, Double> result = new Pair<>(Integer.MIN_VALUE, Double.NaN);
		boolean hasSufficientContent = false;
		double[] asset1CommonRates = null;
		double[] asset2CommonRates = null;

		if (DatesAndSetUtil.hasSufficientContent(dates)) {
			asset1CommonRates = DatesAndSetUtil.getValuesByIndex(dates, asset1Rates);
			asset2CommonRates = DatesAndSetUtil.getValuesByIndex(dates, asset2Rates);

			result = computeBestCorrelation(asset1CommonRates, asset2CommonRates);
			hasSufficientContent = true;
		}

		return new AssetsCorrelationInfo(asset1Id,
				asset2Id,
				hasSufficientContent,
				result.getFirst(),
				result.getSecond(),
				dates,
				asset1CommonRates,
				asset2CommonRates);
	}

	private static Pair<Integer, Double> computeBestCorrelation(double[] asset1Values, double[] asset2Values) {
		final int shitRange = asset1Values.length / 2;

		int bestShift = Integer.MIN_VALUE;
		double bestCorrelation = Double.MIN_VALUE;
		double maxAbsCorrelation = Double.MIN_VALUE;

		for (int i = -shitRange; i <= shitRange; i++) {
			final double correlation = Calculator.calculateCorrelationWithShift(asset1Values, asset2Values, i);
			final double absCorrelation = Math.abs(correlation);

			if (absCorrelation > maxAbsCorrelation) {
				maxAbsCorrelation = absCorrelation;

				bestCorrelation = correlation;
				bestShift = i;
			}
		}

		return new Pair<>(bestShift, bestCorrelation);
	}
}
