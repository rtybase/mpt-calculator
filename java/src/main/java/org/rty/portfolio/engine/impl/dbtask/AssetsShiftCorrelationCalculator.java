package org.rty.portfolio.engine.impl.dbtask;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;

import org.apache.commons.math3.stat.correlation.PearsonsCorrelation;
import org.apache.commons.math3.util.Pair;
import org.rty.portfolio.core.AssetsCorrelationInfo;
import org.rty.portfolio.core.utils.DatesAndSetUtil;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;

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

			final ShiftCorrelationComputationResult computationResult = computeBestCorrelation(asset1CommonRates,
					asset2CommonRates);
			result = new Pair<>(computationResult.shift, computationResult.correlation);
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

	private static ShiftCorrelationComputationResult computeBestCorrelation(double[] asset1Values, double[] asset2Values) {
		final int shitRange = asset1Values.length / 2;

		double maxAbsCorrelation = Double.MIN_VALUE;
		ShiftCorrelationComputationResult bestResult = null;

		for (int i = -shitRange; i <= shitRange; i++) {
			final ShiftCorrelationComputationResult result = calculateCorrelationWithShift(asset1Values, asset2Values, i);

			if (result.absCorrelation > maxAbsCorrelation) {
				maxAbsCorrelation = result.absCorrelation;
				bestResult = result;
			}
		}

		return bestResult;
	}

	@VisibleForTesting
	static ShiftCorrelationComputationResult calculateCorrelationWithShift(double[] array1, double[] array2, int shift) {
		Preconditions.checkArgument(array1.length == array2.length, "Arrays must have the same length!");

		final int positiveValueForshift = Math.absExact(shift);
		final int resutArraySize = array1.length - positiveValueForshift;

		Preconditions.checkArgument(resutArraySize > 1, "Shift is too wide!");

		if (shift == 0) {
			return new ShiftCorrelationComputationResult(shift,
					new PearsonsCorrelation().correlation(array1, array2),
					array1,
					array2);
		} else {
			final double[] array1WithShift = new double[resutArraySize];
			final double[] array2WithShift = new double[resutArraySize];

			if (shift > 0) {
				System.arraycopy(array1, 0, array1WithShift, 0, resutArraySize);
				System.arraycopy(array2, positiveValueForshift, array2WithShift, 0, resutArraySize);
			} else {
				System.arraycopy(array1, positiveValueForshift, array1WithShift, 0, resutArraySize);
				System.arraycopy(array2, 0, array2WithShift, 0, resutArraySize);
			}

			return new ShiftCorrelationComputationResult(shift,
					new PearsonsCorrelation().correlation(array1WithShift, array2WithShift),
					array1WithShift,
					array2WithShift);
		}
	}

	@VisibleForTesting
	static class ShiftCorrelationComputationResult {
		final int shift;
		final int absShift;

		final double correlation;
		final double absCorrelation;

		final double[] array1WithShift;
		final double[] array2WithShift;

		private ShiftCorrelationComputationResult(int shift, double correlation, double[] array1WithShift,
				double[] array2WithShift) {
			this.shift = shift;
			this.correlation = correlation;
			this.array1WithShift = array1WithShift;
			this.array2WithShift = array2WithShift;

			this.absCorrelation = Math.abs(correlation);
			this.absShift = Math.absExact(shift);
		}
	}
}
