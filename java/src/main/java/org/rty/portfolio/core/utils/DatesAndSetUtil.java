package org.rty.portfolio.core.utils;

import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;

public final class DatesAndSetUtil {
	public static final SimpleDateFormat CSV_SCAN_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");
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
		return CSV_SCAN_DATE_FORMAT.format(value);
	}

	public static Optional<Date> findClosestDate(Date toDate, Collection<Date> datesToCheck, int maxDaysToTolerate) {
		Objects.requireNonNull(toDate, "toDate must not be null!");
		Objects.requireNonNull(datesToCheck, "datesToCheck must not be null!");

		final TreeSet<Date> datesToCheckAsSet = new TreeSet<>(datesToCheck);

		if (datesToCheckAsSet.contains(toDate)) {
			return Optional.of(toDate);
		}

		final Date next = datesToCheckAsSet.higher(toDate);
		final Date previous = datesToCheckAsSet.lower(toDate);

		if (next == null && previous == null) {
			return Optional.empty();
		}

		final long daysDiffToNext = daysDiff(toDate, next);
		final long daysDiffFromOrevious = daysDiff(toDate, previous);

		if (daysDiffToNext <= daysDiffFromOrevious) {
			return valueIfTrue(daysDiffToNext <= maxDaysToTolerate, next);
		}

		return valueIfTrue(daysDiffFromOrevious <= maxDaysToTolerate, previous);
	}

	private static long daysDiff(Date date1, Date date2) {
		if (date2 != null) {
			return Duration.between(date1.toInstant(), date2.toInstant()).abs().toDays();
		}

		return Long.MAX_VALUE;
	}

	private static <T> Optional<T> valueIfTrue(boolean condition, T Value) {
		if (condition) {
			return Optional.ofNullable(Value);
		}
		return Optional.empty();
	}
}
