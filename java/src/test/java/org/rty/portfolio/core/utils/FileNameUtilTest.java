package org.rty.portfolio.core.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class FileNameUtilTest {
	@Test
	void testWithoutExtention() {
		assertEquals("test-extra.csv", FileNameUtil.adjustOutputFileName("test", "extra"));
	}

	@Test
	void testWithExtention() {
		assertEquals("test.v1-extra.csv", FileNameUtil.adjustOutputFileName("test.v1.csv", "extra"));
	}

}
