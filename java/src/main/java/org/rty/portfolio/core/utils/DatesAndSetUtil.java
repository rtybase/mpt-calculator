package org.rty.portfolio.core.utils;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public final class DatesAndSetUtil {
	private static final int MIN_COMMON_DATE = 5;

	private DatesAndSetUtil() {

	}

	public static <T> boolean hasSufficientContent(Set<T> dates) {
		return dates.size() >= MIN_COMMON_DATE;
	}

	public static <T> Set<T> computeCommonValues(Set<T> set1, Set<T> set2) {
		Set<T> result = new HashSet<>(set1);
		result.retainAll(set2);
		return result;
	}

	public static int[] getIndexesFrom(Map<Integer, ?> storage) {
		final int[] indexes = new int[storage.size()];

		int i = 0;
		for (Integer row : storage.keySet()) {
			indexes[i++] = row;
		}

		return indexes;
	}

	public static <T> double[] getValuesByIndex(Set<T> index, Map<T, Double> allValues) {
		final double[] ret = new double[index.size()];

		int i = 0;
		for (T atIndex : index) {
			ret[i++] = allValues.get(atIndex);
		}

		return ret;
	}

}
