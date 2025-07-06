package org.rty.portfolio.core.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Future;

import org.junit.jupiter.api.Test;
import org.rty.portfolio.core.utils.ConcurrentTaskExecutorWithBatching.ExceptionThrowingConsumer;

class ConcurrentTaskExecutorWithBatchingTest {

	@Test
	void testCtorWithNullBatchCompletionRoutine() {
		NullPointerException ex = assertThrows(NullPointerException.class,
				() -> new ConcurrentTaskExecutorWithBatching<Integer>(1, 1, 1, null));

		assertEquals("batchCompletionRoutine must not be null!", ex.getMessage());
	}

	@Test
	void testCtorWithBadBatchSize() {
		IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
				() -> new ConcurrentTaskExecutorWithBatching<Integer>(1, 1, 0, createSupplier(new HashSet<>())));

		assertEquals("batchSize must be > 0!", ex.getMessage());
	}

	@Test
	void testTaskExecutionAtClose() throws Exception {
		final Set<Integer> result = new HashSet<>();
		ConcurrentTaskExecutorWithBatching<Integer> executor = new ConcurrentTaskExecutorWithBatching<Integer>(1, 1, 10,
				createSupplier(result));

		executor.addTask(() -> {
			return 1;
		});

		executor.close();

		assertEquals(Set.of(1), result);
	}

	@Test
	void testTaskExecutionAtAdding() throws Exception {
		final Set<Integer> result = new HashSet<>();
		ConcurrentTaskExecutorWithBatching<Integer> executor = new ConcurrentTaskExecutorWithBatching<Integer>(1, 1, 1,
				createSupplier(result));

		executor.addTask(() -> {
			return 1;
		});

		executor.close();

		assertEquals(Set.of(1), result);
	}

	@Test
	void testTaskExecution() throws Exception {
		final Set<Integer> result = new HashSet<>();
		ConcurrentTaskExecutorWithBatching<Integer> executor = new ConcurrentTaskExecutorWithBatching<Integer>(1, 1, 2,
				createSupplier(result));

		final Set<Integer> expected = Set.of(1, 2, 3);

		expected.forEach(val -> {
			try {
				executor.addTask(() -> {
					return val;
				});
			} catch (Exception ex) {
			}
		});

		executor.close();

		assertEquals(expected, result);
	}

	private static ExceptionThrowingConsumer<List<Future<Integer>>> createSupplier(Set<Integer> result)
			throws Exception {
		return listOfFutures -> {
			for (Future<Integer> f : listOfFutures) {
				result.add(f.get());
			}
		};
	}
}
