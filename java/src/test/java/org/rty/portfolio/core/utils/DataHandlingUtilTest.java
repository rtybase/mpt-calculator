package org.rty.portfolio.core.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
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
		final Map<String, NavigableMap<Date, AssetPriceInfo>> store = new HashMap<>();

		DataHandlingUtil.addDataToMapByNameAndDate(List.of(assetPriceFrom(TEST_ASSET, "2025-07-17"),
				assetPriceFrom(TEST_ASSET, "2025-07-16"),
				assetPriceFrom(TEST_ASSET + 1, "2025-07-17")),
			store);

		assertEquals(2, store.size());

		assertEquals(2, store.get(TEST_ASSET).size());
		assertTrue(store.get(TEST_ASSET).containsKey(dateFrom(17)));
		assertTrue(store.get(TEST_ASSET).containsKey(dateFrom(16)));

		assertEquals(1, store.get(TEST_ASSET + 1).size());
		assertTrue(store.get(TEST_ASSET + 1).containsKey(dateFrom(17)));
	}
}
