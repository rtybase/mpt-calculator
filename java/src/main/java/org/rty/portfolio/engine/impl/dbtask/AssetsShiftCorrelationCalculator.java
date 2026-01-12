package org.rty.portfolio.engine.impl.dbtask;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;

import org.apache.commons.math3.stat.StatUtils;
import org.apache.commons.math3.stat.correlation.PearsonsCorrelation;
import org.apache.commons.math3.util.Pair;
import org.rty.portfolio.core.AssetsCorrelationInfo;
import org.rty.portfolio.core.utils.DatesAndSetUtil;
import org.rty.portfolio.math.Calculator;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;

public class AssetsShiftCorrelationCalculator implements Callable<AssetsCorrelationInfo> {
	private final int asset1Id;
	private final int asset2Id;
	private final int shiftThreshold;
	private final Map<Integer, Map<String, Double>> storage;

	AssetsShiftCorrelationCalculator(Map<Integer, Map<String, Double>> storage, int asset1Id, int asset2Id,
			int shiftThreshold) {
		this.storage = storage;
		this.asset1Id = asset1Id;
		this.asset2Id = asset2Id;
		this.shiftThreshold = shiftThreshold;
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
		double[] forecast = null;

		if (DatesAndSetUtil.hasSufficientContent(dates)) {
			asset1CommonRates = DatesAndSetUtil.getValuesByIndex(dates, asset1Rates);
			asset2CommonRates = DatesAndSetUtil.getValuesByIndex(dates, asset2Rates);

			final ShiftCorrelationComputationResult computationResult = computeBestCorrelation(asset1CommonRates,
					asset2CommonRates,
					shiftThreshold);
			result = new Pair<>(computationResult.shift, computationResult.correlation);
			hasSufficientContent = true;

			forecast = calculateForecast(computationResult);
		}

		return new AssetsCorrelationInfo(asset1Id,
				asset2Id,
				hasSufficientContent,
				result.getFirst(),
				result.getSecond(),
				dates,
				asset1CommonRates,
				asset2CommonRates,
				forecast);
	}

	private static ShiftCorrelationComputationResult computeBestCorrelation(double[] asset1Values, double[] asset2Values, int shiftThreshold) {
		final int shitRange = asset1Values.length / 2;

		double maxAbsCorrelation = Double.MIN_VALUE;
		ShiftCorrelationComputationResult bestResult = null;

		for (int i = -shitRange; i <= shitRange; i++) {
			final ShiftCorrelationComputationResult result = calculateCorrelationWithShift(asset1Values,
					asset2Values, i);

			if (result.absCorrelation > maxAbsCorrelation) {
				maxAbsCorrelation = result.absCorrelation;
				bestResult = result;

				if (bestResult.shift > shiftThreshold) {
					break;
				}
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

		double valueForForecast = array1[array1.length - 1];

		if (shift == 0) {
			return new ShiftCorrelationComputationResult(shift,
					new PearsonsCorrelation().correlation(array1, array2),
					array1,
					array2,
					valueForForecast);
		} else {
			final double[] array1WithShift = new double[resutArraySize];
			final double[] array2WithShift = new double[resutArraySize];

			if (shift > 0) {
				System.arraycopy(array1, 0, array1WithShift, 0, resutArraySize);
				System.arraycopy(array2, positiveValueForshift, array2WithShift, 0, resutArraySize);
			} else {
				System.arraycopy(array1, positiveValueForshift, array1WithShift, 0, resutArraySize);
				System.arraycopy(array2, 0, array2WithShift, 0, resutArraySize);
				valueForForecast = array2[array2.length - 1];
			}

			return new ShiftCorrelationComputationResult(shift,
					new PearsonsCorrelation().correlation(array1WithShift, array2WithShift),
					array1WithShift,
					array2WithShift,
					valueForForecast);
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

		final double valueForForecast;

		private ShiftCorrelationComputationResult(int shift, double correlation, double[] array1WithShift,
				double[] array2WithShift, double valueForForecast) {
			this.shift = shift;
			this.correlation = correlation;
			this.array1WithShift = array1WithShift;
			this.array2WithShift = array2WithShift;

			this.valueForForecast = valueForForecast;

			this.absCorrelation = Math.abs(correlation);
			this.absShift = Math.absExact(shift);
		}
	}

	@VisibleForTesting
	static double[] calculateForecast(ShiftCorrelationComputationResult correlationResults) {
		if (correlationResults.absShift == 1) {
			double[] x = correlationResults.array1WithShift;
			double[] y = correlationResults.array2WithShift;

			if (correlationResults.shift < 0) {
				x = correlationResults.array2WithShift;
				y = correlationResults.array1WithShift;
			}

			return forecast(x,
					correlationResults.valueForForecast,
					y,
					correlationResults.correlation);
		}
		return null;
	}

	private static double[] forecast(double[] x, double extraX, double[] y, double correlation) {
		final double[] fullX = append(x, extraX);
		final int n = fullX.length;

		final double meanX = StatUtils.mean(fullX);
		final double meanY = StatUtils.mean(y);

		final double varianceX = StatUtils.populationVariance(fullX);
		final double varianceY = StatUtils.populationVariance(y);

		final double c_2 = varianceX * (n - 1) * square(correlation);
		final double c = Math.sqrt(c_2);
		final double k = pretendCovariance(x, meanX, y, meanY);

		final double diffX = extraX - meanX;
		final double diffXandCSquare = square(diffX) - c_2;

		final double delta = square(k) + n * varianceY * diffXandCSquare;
		final double deltaSqrt = tryExtractSqrt(delta);

		if (Double.isNaN(deltaSqrt)) {
			return null;
		}

		final double y1 = meanY + (-k * diffX - c * deltaSqrt) / diffXandCSquare;
		final double y2 = meanY + (-k * diffX + c * deltaSqrt) / diffXandCSquare;

		return new double[] { y1, y2 };
	}

	private static double pretendCovariance(double[] x, double meanX, double[] y, double meanY) {
		double result = 0D;

		for (int i = 0; i < x.length; i++) {
			result += (x[i] - meanX) * (y[i] - meanY);
		}

		return result;
	}

	private static double[] append(double[] array, double element) {
		final int n = array.length;
		array = Arrays.copyOf(array, n + 1);
		array[n] = element;
		return array;
	}

	private static double square(double x) {
		return x * x;
	}

	private static double tryExtractSqrt(double x) {
		if (Calculator.almostZero(x)) {
			return 0D;
		}

		return Math.sqrt(x);
	}
}
