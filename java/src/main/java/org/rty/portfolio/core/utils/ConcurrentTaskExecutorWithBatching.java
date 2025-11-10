package org.rty.portfolio.core.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

public class ConcurrentTaskExecutorWithBatching<T> implements AutoCloseable {
	private final ExecutorService executor;
	private final List<Callable<T>> tasksToExecute;
	private final ExceptionThrowingConsumer<List<T>> batchCompletionRoutine;

	private final int numberOfThreads;
	private final int batchSize;
	private final int entriesPerPartition;

	public ConcurrentTaskExecutorWithBatching(int numberOfThreads, int taskQueuSize, int batchSize,
			ExceptionThrowingConsumer<List<T>> batchCompletionRoutine) {
		Objects.requireNonNull(batchCompletionRoutine, "batchCompletionRoutine must not be null!");
		Preconditions.checkArgument(batchSize > 0, "batchSize must be > 0!");

		this.executor = ConcurrencyUtil.createExecutorService(numberOfThreads, taskQueuSize);
		this.batchCompletionRoutine = batchCompletionRoutine;
		tasksToExecute = new ArrayList<>(batchSize);

		this.batchSize = batchSize;
		this.numberOfThreads = numberOfThreads;
		this.entriesPerPartition = batchSize / numberOfThreads;
	}

	/**
	 * Returns true if adding tasks triggered calculations, otherwise (task was only
	 * scheduled for the execution) - false. It could be an useful indicator to do a
	 * (for example) DB commit.
	 */
	public synchronized boolean addTask(Callable<T> task) throws Exception {
		tasksToExecute.add(task);

		if (tasksToExecute.size() >= batchSize) {
			executeTasks();

			return true;
		}

		return false;
	}

	@Override
	public synchronized void close() throws Exception {
		if (!tasksToExecute.isEmpty()) {
			executeTasks();
		}

		executor.shutdown();
		executor.awaitTermination(Long.MAX_VALUE, TimeUnit.SECONDS);
	}

	private void executeTasks() throws Exception {
		if (tasksToExecute.size() <= numberOfThreads) {
			final List<Future<T>> futureResults = executor.invokeAll(tasksToExecute);
			batchCompletionRoutine.accept(futuresToResults(futureResults));
		} else {
			List<BulkExecutor<T>> bulkTasks = partition();
			final List<Future<List<T>>> futureResults = executor.invokeAll(bulkTasks);

			final List<T> allResults = new ArrayList<>(batchSize);

			for (Future<List<T>> futureResult : futureResults) {
				allResults.addAll(futureResult.get());
			}

			batchCompletionRoutine.accept(allResults);
		}
		tasksToExecute.clear();
	}

	private List<BulkExecutor<T>> partition() {
		List<List<Callable<T>>> partitions = Lists.partition(tasksToExecute, entriesPerPartition);
		List<BulkExecutor<T>> result = new ArrayList<>(partitions.size());

		for (List<Callable<T>> partition : partitions) {
			result.add(new BulkExecutor<>(partition));
		}

		return result;
	}

	private List<T> futuresToResults(List<Future<T>> futures) throws Exception {
		List<T> results = new ArrayList<>(futures.size());

		for (Future<T> future : futures) {
			results.add(future.get());
		}

		return results;
	}

	@FunctionalInterface
	public interface ExceptionThrowingConsumer<T> {
		void accept(T t) throws Exception;
	}

	private static class BulkExecutor<T> implements Callable<List<T>> {
		private final List<Callable<T>> tasks;

		private BulkExecutor(List<Callable<T>> tasks) {
			this.tasks = tasks;
		}

		@Override
		public List<T> call() throws Exception {
			List<T> results = new ArrayList<>(tasks.size());

			for (Callable<T> task : tasks) {
				results.add(task.call());
			}

			return results;
		}
	}
}
