package org.rty.portfolio.core.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.junit.jupiter.api.Test;

class JsonUtilTest {
	private static final List<String> EXPECTED_RESULT = List.of("177", "192", "193", "210", "215");

	@Test
	void testToStringList() throws Exception {
		final List<String> result = JsonUtil.toList("[\"177\",\"192\",\"193\",\"210\",\"215\"]");

		assertEquals(EXPECTED_RESULT, result);
	}

	@Test
	void testToIntegerList() throws Exception {
		List<Integer> result = JsonUtil.toList("[177,192,193,210,215]");

		assertEquals(EXPECTED_RESULT.size(), result.size());

		for (int i = 0; i < EXPECTED_RESULT.size(); i++) {
			assertEquals(Integer.parseInt(EXPECTED_RESULT.get(i)), result.get(i));
		}
	}
}
