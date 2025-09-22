package org.rty.portfolio.core;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class CsvWritableTest {
	@Test
	void testEmptyIfNullWithNull() {
		assertEquals("", CsvWritable.emptyIfNull(null));
	}

	@Test
	void testEmptyIfNull() {
		assertEquals("1.0", CsvWritable.emptyIfNull(1D));
	}
}
