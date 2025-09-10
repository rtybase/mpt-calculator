package org.rty.portfolio.core.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;

import org.junit.jupiter.api.Test;
import org.rty.portfolio.core.AssetPriceInfo;

class DataHandlingUtilTest extends CommonTestRoutines {
	@Test
	void testAddDataToMapByNameAndDate() {
		final Map<String, NavigableMap<Date, AssetPriceInfo>> store = newStoreWithContent();

		assertEquals(2, store.size());

		assertEquals(2, store.get(TEST_ASSET).size());
		assertTrue(store.get(TEST_ASSET).containsKey(dateFrom(17)));
		assertTrue(store.get(TEST_ASSET).containsKey(dateFrom(16)));

		assertEquals(1, store.get(TEST_ASSET + 1).size());
		assertTrue(store.get(TEST_ASSET + 1).containsKey(dateFrom(17)));
	}

	@Test
	void testAddLists() {
		assertEquals(3, DataHandlingUtil.addLists(List.of("1"), List.of("1", "1")).size());
	}

	@Test
	void testAllNotNull() {
		assertTrue(DataHandlingUtil.allNotNull());
		assertTrue(DataHandlingUtil.allNotNull(1D, 2D));
	}

	@Test
	void testAllNotNullReturnsFalse() {
		assertFalse(DataHandlingUtil.allNotNull(1D, null));
	}

	@Test
	void testGetCurrentEntry() {
		final Map<String, NavigableMap<Date, AssetPriceInfo>> store = newStoreWithContent();
		assertNotNull(DataHandlingUtil.getCurrentEntry(store, TEST_ASSET, D_2025_07_17));
	}

	@Test
	void testGetCurrentEntryReturnsNull() {
		final Map<String, NavigableMap<Date, AssetPriceInfo>> store = newStoreWithContent();
		assertNull(DataHandlingUtil.getCurrentEntry(store, "DOES'T EXIST", D_2025_07_17));
	}

	@Test
	void testGetPreviousEntry() {
		final Map<String, NavigableMap<Date, AssetPriceInfo>> store = newStoreWithContent();
		assertNotNull(DataHandlingUtil.getPreviousEntry(store, TEST_ASSET, D_2025_07_17));
	}

	@Test
	void testGetPreviousEntryReturnsNull() {
		final Map<String, NavigableMap<Date, AssetPriceInfo>> store = newStoreWithContent();
		assertNull(DataHandlingUtil.getPreviousEntry(store, "DOES'T EXIST", D_2025_07_17));
		assertNull(DataHandlingUtil.getPreviousEntry(store, TEST_ASSET, dateFrom(16)));
	}

	@Test
	void testGet2DaysPreviousEntryReturnsNull() {
		final Map<String, NavigableMap<Date, AssetPriceInfo>> store = newStoreWithContent();
		assertNull(DataHandlingUtil.get2DaysPreviousEntry(store, "DOES'T EXIST", D_2025_07_17));
		assertNull(DataHandlingUtil.get2DaysPreviousEntry(store, TEST_ASSET, dateFrom(16)));
		assertNull(DataHandlingUtil.get2DaysPreviousEntry(store, TEST_ASSET, D_2025_07_17));
	}

	@Test
	void testGetNetxEntry() {
		final Map<String, NavigableMap<Date, AssetPriceInfo>> store = newStoreWithContent();
		assertNotNull(DataHandlingUtil.getNextEntry(store, TEST_ASSET, dateFrom(16)));
	}

	@Test
	void testGetNextEntryReturnsNull() {
		final Map<String, NavigableMap<Date, AssetPriceInfo>> store = newStoreWithContent();
		assertNull(DataHandlingUtil.getNextEntry(store, "DOES'T EXIST", D_2025_07_17));
		assertNull(DataHandlingUtil.getNextEntry(store, TEST_ASSET, D_2025_07_17));
	}

	@Test
	void testGet2DaysNextEntryReturnsNull() {
		final Map<String, NavigableMap<Date, AssetPriceInfo>> store = newStoreWithContent();
		assertNull(DataHandlingUtil.get2DaysNextEntry(store, "DOES'T EXIST", D_2025_07_17));
		assertNull(DataHandlingUtil.get2DaysNextEntry(store, TEST_ASSET, D_2025_07_17));
		assertNull(DataHandlingUtil.get2DaysNextEntry(store, TEST_ASSET, dateFrom(16)));
	}

	private Map<String, NavigableMap<Date, AssetPriceInfo>> newStoreWithContent() {
		final Map<String, NavigableMap<Date, AssetPriceInfo>> store = new HashMap<>();

		DataHandlingUtil.addDataToMapByNameAndDate(List.of(assetPriceFrom(TEST_ASSET, "2025-07-17"),
				assetPriceFrom(TEST_ASSET, "2025-07-16"),
				assetPriceFrom(TEST_ASSET + 1, "2025-07-17")),
			store);
		return store;
	}
}
