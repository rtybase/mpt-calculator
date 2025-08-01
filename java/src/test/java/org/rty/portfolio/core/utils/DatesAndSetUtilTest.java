package org.rty.portfolio.core.utils;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.Test;

class DatesAndSetUtilTest {
	private static final Date D_2025_07_17 = new Date(125, 6, 17);

	@Test
	void testHasSufficientContent() {
		assertTrue(DatesAndSetUtil.hasSufficientContent(Set.of("1", "2", "3", "4", "5")));
	}

	@Test
	void testHasInsufficientContent() {
		assertFalse(DatesAndSetUtil.hasSufficientContent(Set.of()));
	}

	@Test
	void testComputeCommonWithEmptyInput() {
		final Set<String> result = DatesAndSetUtil.computeCommonValues(List.of());

		assertTrue(result.isEmpty());
	}

	@Test
	void testComputeCommonWithOneInput() {
		final Set<String> result = DatesAndSetUtil.computeCommonValues(List.of(Set.of("1")));

		assertEquals(Set.of("1"), result);
	}

	@Test
	void testComputeCommonValuesIsEmpty() {
		final Set<String> result = DatesAndSetUtil.computeCommonValues(List.of(Set.of(), Set.of("1")));

		assertTrue(result.isEmpty());
	}

	@Test
	void testComputeCommonValuesWithNoCommonValues() {
		final Set<String> result = DatesAndSetUtil.computeCommonValues(List.of(Set.of("2"), Set.of("3")));

		assertTrue(result.isEmpty());
	}

	@Test
	void testComputeCommonValues() {
		final Set<String> result = DatesAndSetUtil.computeCommonValues(List.of(Set.of("2"), Set.of("2", "3")));

		assertEquals(Set.of("2"), result);
	}

	@Test
	void testComputeCommonValuesMoreInputs() {
		final Set<String> result = DatesAndSetUtil
				.computeCommonValues(List.of(Set.of("2"), Set.of("2", "4"), Set.of("2", "3")));

		assertEquals(Set.of("2"), result);
	}

	@Test
	void testGetIndexesFromEmptyStorage() {
		final int[] result = DatesAndSetUtil.getIndexesFrom(Map.of());

		assertEquals(0, result.length);
	}

	@Test
	void testGetIndexesFrom() {
		final int[] result = DatesAndSetUtil.getIndexesFrom(Map.of(1, Map.of(), 2, Map.of()));

		Arrays.sort(result);
		assertArrayEquals(new int[] { 1, 2 }, result);
	}

	@Test
	void testGetValuesByIndexWithEmptyIndex() {
		final double[] result = DatesAndSetUtil.getValuesByIndex(Set.of(), Map.of());

		assertEquals(0, result.length);
	}

	@Test
	void testGetValuesByIndex() {
		final double[] result = DatesAndSetUtil.getValuesByIndex(Set.of("1", "3"), Map.of("1", 1D, "2", 2D, "3", 3D));

		Arrays.sort(result);
		assertArrayEquals(new double[] { 1D, 3D }, result);
	}

	@Test
	void testStrToDate() {
		final Date result = DatesAndSetUtil.strToDate(DateTimeFormatter.ofPattern("MMM d, yyyy", Locale.US),
				"Jul 17, 2025");

		assertEquals(D_2025_07_17, result);
	}

	@Test
	void testDateToStr() {
		final String result = DatesAndSetUtil.dateToStr(D_2025_07_17);

		assertEquals("2025-07-17", result);
	}
}
