package org.rty.portfolio.core.utils;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class DatesAndSetUtil {
	private static final Logger LOGGER = LoggerFactory.getLogger(DatesAndSetUtil.class.getSimpleName());

	public static final DateTimeFormatter CSV_SCAN_DATE_FORMAT_WRITE = DateTimeFormatter.ofPattern("yyyy-MM-dd");

	public static final DateTimeFormatter CSV_SCAN_DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-M-d");
	public static final DateTimeFormatter EPS_DATE_FORMAT = DateTimeFormatter.ofPattern("M/d/yyyy");
	public static final DateTimeFormatter SCAN_INPUT_DATE_FORMAT = DateTimeFormatter.ofPattern("d-M-yyyy");

	private static final int MIN_COMMON_DATE = 5;

	private DatesAndSetUtil() {

	}

	public static <T> boolean hasSufficientContent(Set<T> dates) {
		return dates.size() >= MIN_COMMON_DATE;
	}

	public static <T> SortedSet<T> computeCommonValues(List<Set<T>> sets) {
		if (sets.isEmpty()) {
			return Collections.emptySortedSet();
		}

		if (sets.size() == 1) {
			return new TreeSet<>(sets.get(0));
		}

		Set<T> result = new HashSet<>(sets.get(0));
		for (int i = 1; i < sets.size(); i++) {
			result.retainAll(sets.get(i));

			if (result.isEmpty()) {
				return Collections.emptySortedSet();
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
		final Instant instant = toJavaDate(value).toInstant();
		return CSV_SCAN_DATE_FORMAT_WRITE.format(instant.atZone(ZoneId.systemDefault()));
	}

	public static Date toDate(String value) {
		return toDate(value, DatesAndSetUtil.CSV_SCAN_DATE_FORMAT);
	}

	public static Date toDate(String value, DateTimeFormatter format) {
		try {
			final LocalDate localDate = LocalDate.parse(value, format);
			return Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
		} catch (DateTimeParseException ex) {
			LOGGER.error("Failed to parse '{}' value, returning null!", value, ex);
		}

		return null;
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
		final long daysDiffFromPrevious = daysDiff(toDate, previous);

		if (daysDiffToNext <= daysDiffFromPrevious) {
			return valueIfTrue(daysDiffToNext <= maxDaysToTolerate, next);
		}

		return valueIfTrue(daysDiffFromPrevious <= maxDaysToTolerate, previous);
	}

	public static <T> T oneOrTheOther(boolean condition, T one, T theOther) {
		if (condition) {
			return one;
		}
		return theOther;
	}

	public static Date toJavaDate(Date date) {
		if (date instanceof java.sql.Date) {
			return new Date(date.getTime());
		}

		return date;
	}

	public static long daysDiff(Date date1, Date date2) {
		if (date1 != null && date2 != null) {
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
