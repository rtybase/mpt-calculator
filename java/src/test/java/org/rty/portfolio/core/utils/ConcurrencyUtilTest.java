package org.rty.portfolio.core.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class ConcurrencyUtilTest {

	@Test
	void testWithBadNumberOfThread() {
		IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
				() -> ConcurrencyUtil.createExecutorService(0, 1));

		assertEquals("numberOfThread must be > 0!", ex.getMessage());
	}

	@Test
	void testWithBadQueueSize() {
		IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
				() -> ConcurrencyUtil.createExecutorService(1, 0));

		assertEquals("taskQueuSize must be > 0!", ex.getMessage());
	}
}
