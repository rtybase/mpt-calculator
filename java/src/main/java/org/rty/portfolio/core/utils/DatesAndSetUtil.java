package org.rty.portfolio.core.utils;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

public final class DatesAndSetUtil {
	public static final SimpleDateFormat SCAN_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");
	private static final int MIN_COMMON_DATE = 5;

	private DatesAndSetUtil() {

	}

	public static <T> boolean hasSufficientContent(Set<T> dates) {
		return dates.size() >= MIN_COMMON_DATE;
	}

	public static <T> Set<T> computeCommonValues(List<Set<T>> sets) {
		if (sets.isEmpty()) {
			return Collections.emptySet();
		}

		if (sets.size() == 1) {
			return sets.get(0);
		}

		Set<T> result = new HashSet<>(sets.get(0));
		for (int i = 1; i < sets.size(); i++) {
			result.retainAll(sets.get(i));

			if (result.isEmpty()) {
				return Collections.emptySet();
			}
		}

		return new TreeSet<>(result);
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

	public static Date strToDate(DateTimeFormatter dateFormatter, String value) {
		final LocalDate dateTime = LocalDate.parse(value, dateFormatter);
		return java.util.Date.from(dateTime
				.atStartOfDay()
				.atZone(ZoneId.systemDefault())
				.toInstant());
	}

	public static String dateToStr(Date value) {
		return SCAN_DATE_FORMAT.format(value);
	}
}
