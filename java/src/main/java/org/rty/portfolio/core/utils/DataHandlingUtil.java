package org.rty.portfolio.core.utils;

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
}
