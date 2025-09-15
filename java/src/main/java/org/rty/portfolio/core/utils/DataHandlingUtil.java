package org.rty.portfolio.core.utils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Objects;
import java.util.TreeMap;

import org.rty.portfolio.core.EntryWithAssetNameAndDate;

public final class DataHandlingUtil {
	private DataHandlingUtil() {
	}

	public static <T extends EntryWithAssetNameAndDate> void addDataToMapByNameAndDate(List<T> dataToAdd,
			Map<String, NavigableMap<Date, T>> store) {
		Objects.requireNonNull(dataToAdd, "dataToAdd must not be null!!");
		Objects.requireNonNull(store, "store must not be null!!");

		dataToAdd.forEach(entry -> {
			NavigableMap<Date, T> assteDetails = store.computeIfAbsent(entry.getAssetName(), k -> new TreeMap<>());
			assteDetails.put(entry.getDate(), entry);
		});
	}

	public static <T> List<T> addLists(List<T> l1, List<T> l2) {
		final List<T> result = new ArrayList<>();
		result.addAll(l1);
		result.addAll(l2);
		return result;
	}

	public static boolean allNotNull(Object... objects) {
		for (Object object : objects) {
			if (object == null) {
				return false;
			}
		}

		return true;
	}

	public static <T> T getCurrentEntry(Map<String, NavigableMap<Date, T>> map, String assetName, Date key) {
		NavigableMap<Date, T> firstEntry = map.get(assetName);
		if (firstEntry == null) {
			return null;
		}

		return firstEntry.get(key);
	}

	// as-of value or next (e.g.if 'key' is a holiday)
	public static <T> T getCurrentEntryOrNext(Map<String, NavigableMap<Date, T>> map, String assetName, Date key) {
		final T currentEntry = getCurrentEntry(map, assetName, key);

		if (currentEntry != null) {
			return currentEntry;
		}

		return getNextEntry(map, assetName, key);
	}

	public static <T> T getPreviousEntry(Map<String, NavigableMap<Date, T>> map, String assetName, Date key) {
		NavigableMap<Date, T> assetEntry = map.get(assetName);
		if (assetEntry == null) {
			return null;
		}

		return getPreviousEntry(assetEntry, key);
	}

	public static <T> T getNextEntry(Map<String, NavigableMap<Date, T>> map, String assetName, Date key) {
		NavigableMap<Date, T> assetEntry = map.get(assetName);
		if (assetEntry == null) {
			return null;
		}

		return getNextEntry(assetEntry, key);
	}

	public static <T> T get2DaysPreviousEntry(Map<String, NavigableMap<Date, T>> map, String assetName, Date key) {
		NavigableMap<Date, T> assetEntry = map.get(assetName);
		if (assetEntry == null) {
			return null;
		}

		final Map.Entry<Date, T> previousEntry = assetEntry.lowerEntry(key);
		if (previousEntry == null) {
			return null;
		}

		return getPreviousEntry(assetEntry, previousEntry.getKey());
	}

	public static <T> T get2DaysNextEntry(Map<String, NavigableMap<Date, T>> map, String assetName, Date key) {
		NavigableMap<Date, T> assetEntry = map.get(assetName);
		if (assetEntry == null) {
			return null;
		}

		final Map.Entry<Date, T> nextEntry = assetEntry.higherEntry(key);
		if (nextEntry == null) {
			return null;
		}

		return getNextEntry(assetEntry, nextEntry.getKey());
	}

	public static <T> T getPreviousEntry(NavigableMap<Date, T> map, Date key) {
		final Map.Entry<Date, T> previousEntry = map.lowerEntry(key);
		if (previousEntry == null) {
			return null;
		}

		return previousEntry.getValue();
	}

	public static <T> T getNextEntry(NavigableMap<Date, T> map, Date key) {
		final Map.Entry<Date, T> nextEntry = map.higherEntry(key);
		if (nextEntry == null) {
			return null;
		}

		return nextEntry.getValue();
	}
}
