package org.rty.portfolio.core.utils;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import org.rty.portfolio.core.utils.HistoricalRowGenerator.ColumnKind;
import org.rty.portfolio.core.utils.HistoricalRowGenerator.Row;
import org.rty.portfolio.core.utils.HistoricalRowGenerator.RowHeader;

class HistoricalRowGeneratorTest {
	@Test
	void testRowCreationSimpleColumns() {
		final RowHeader header = RowHeader.startWithHeader("test1", ColumnKind.CURRENT_VALUE_ONLY)
				.addHeader("test2", ColumnKind.CURRENT_VALUE_ONLY)
				.addHeader("test3", ColumnKind.CURRENT_VALUE_ONLY)
				.addHeader("test4", ColumnKind.CURRENT_VALUE_ONLY)
				.build();
		final Row row = header.newRow();

		row.add("test1", 1D);
		row.add("test2", 2D);
		row.add("test3", 3);
		row.add("test4", "t");

		assertArrayEquals(new String[] { "test1", "test2", "test3", "test4" }, header.toCsvLine());
		assertArrayEquals(new String[] { "1.0", "2.0", "3", "t" }, row.toCsvLine());
	}

	@Test
	void testRowCreationWithCurrentAndPreviousColumns() {
		final RowHeader header = RowHeader.startWithHeader("test1", ColumnKind.CURRENT_AND_PREVIOUS_VALUES)
				.addHeader("test2", ColumnKind.CURRENT_AND_PREVIOUS_VALUES).build();
		final Row row = header.newRow();

		row.add("test1", 1D, 2D);
		row.add("test2", 3D, 4D);

		assertArrayEquals(new String[] { "test1", "prev_test1", "test2", "prev_test2" }, header.toCsvLine());
		assertArrayEquals(new String[] { "1.0", "2.0", "3.0", "4.0" }, row.toCsvLine());
	}

	@Test
	void testRowCreationWithCurrentAndPreviousIntegerColumns() {
		final RowHeader header = RowHeader.startWithHeader("test1", ColumnKind.CURRENT_AND_PREVIOUS_VALUES)
				.addHeader("test2", ColumnKind.CURRENT_AND_PREVIOUS_VALUES).build();
		final Row row = header.newRow();

		row.add("test1", 1, 2);
		row.add("test2", 3, 4);

		assertArrayEquals(new String[] { "test1", "prev_test1", "test2", "prev_test2" }, header.toCsvLine());
		assertArrayEquals(new String[] { "1", "2", "3", "4" }, row.toCsvLine());
	}

	@Test
	void testRowCreationWithCurrentPreviousAndChangeRateColumns() {
		final RowHeader header = RowHeader.startWithHeader("test1", ColumnKind.CURRENT_PREVIOUS_AND_CHANGE_RATE_VALUES)
				.addHeader("test2", ColumnKind.CURRENT_PREVIOUS_AND_CHANGE_RATE_VALUES).build();
		final Row row = header.newRow();

		row.add("test1", 1D, 2D, true);
		row.add("test2", 3D, 4D, true);

		assertArrayEquals(new String[] { "test1", "prev_test1", "ch_r_test1", "test2", "prev_test2", "ch_r_test2" },
				header.toCsvLine());
		assertArrayEquals(new String[] { "1.0", "2.0", "-50.0", "3.0", "4.0", "-25.0" }, row.toCsvLine());
	}

	@Test
	void testColumnAlreadyExists() {
		final RowHeader.Builder builder = RowHeader.startWithHeader("test", ColumnKind.CURRENT_VALUE_ONLY);

		IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
				() -> builder.addHeader("test", ColumnKind.CURRENT_PREVIOUS_AND_CHANGE_RATE_VALUES));

		assertEquals("'test' already exists!", ex.getMessage());

	}

	@Test
	void testColumnAnotherAlreadyExists() {
		final RowHeader.Builder builder = RowHeader.startWithHeader("test", ColumnKind.CURRENT_AND_PREVIOUS_VALUES)
				.addHeader("prev_test", ColumnKind.CURRENT_VALUE_ONLY);

		IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> builder.build());

		assertEquals("Multiple columns with name 'prev_test' detected!", ex.getMessage());
	}

	@Test
	void testColumnsIncompatibilityPart1() {
		final RowHeader header = RowHeader.startWithHeader("test", ColumnKind.CURRENT_VALUE_ONLY).build();
		final Row row = header.newRow();

		IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> row.add("test", 1D, 2D));

		assertEquals("'test' doesn't match all columns!", ex.getMessage());
	}

	@Test
	void testColumnsIncompatibilityPart2() {
		final RowHeader header = RowHeader.startWithHeader("test", ColumnKind.CURRENT_AND_PREVIOUS_VALUES).build();
		final Row row = header.newRow();

		IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> row.add("test", 1D));

		assertEquals("'test' doesn't match all columns!", ex.getMessage());
	}

	@Test
	void testColumnsNamesMismatch() {
		final RowHeader header = RowHeader.startWithHeader("test", ColumnKind.CURRENT_AND_PREVIOUS_VALUES).build();
		final Row row = header.newRow();

		IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
				() -> row.add("another_test", 1D, 2D));

		assertEquals("'another_test' doesn't match all columns!", ex.getMessage());
	}
}
