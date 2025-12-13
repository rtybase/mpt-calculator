package org.rty.portfolio.core.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

import com.google.common.base.Preconditions;

public class ConcurrentTaskExecutorWithBatching<T> implements AutoCloseable {
	private final ExecutorService executor;
	private final List<Callable<T>> tasksToExecute;
	private final ExceptionThrowingConsumer<List<T>> batchCompletionRoutine;
	private final int batchSize;

	public ConcurrentTaskExecutorWithBatching(int numberOfThreads, int taskQueuSize, int batchSize,
			ExceptionThrowingConsumer<List<T>> batchCompletionRoutine) {
		Objects.requireNonNull(batchCompletionRoutine, "batchCompletionRoutine must not be null!");
		Preconditions.checkArgument(batchSize > 0, "batchSize must be > 0!");

		this.executor = ConcurrencyUtil.createExecutorService(numberOfThreads, taskQueuSize);
		this.batchCompletionRoutine = batchCompletionRoutine;
		tasksToExecute = new ArrayList<>(batchSize);

		this.batchSize = batchSize;
	}

	public synchronized void addTask(Callable<T> task) throws Exception {
		tasksToExecute.add(task);

		if (tasksToExecute.size() >= batchSize) {
			executeTasks();
		}
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
		BulkExecutor<T> bulkTask = prepareTaskToSubmit();
		tasksToExecute.clear();

		executor.submit(bulkTask);
	}

	private BulkExecutor<T> prepareTaskToSubmit() {
		final List<Callable<T>> tasks = new ArrayList<>(tasksToExecute);

		return new BulkExecutor<>(tasks, batchCompletionRoutine);
	}

	@FunctionalInterface
	public interface ExceptionThrowingConsumer<T> {
		void accept(T t) throws Exception;
	}

	private static class BulkExecutor<T> implements Callable<Void> {
		private final ExceptionThrowingConsumer<List<T>> batchCompletionRoutine;
		private final List<Callable<T>> tasks;

		private BulkExecutor(List<Callable<T>> tasks, ExceptionThrowingConsumer<List<T>> batchCompletionRoutine) {
			this.tasks = tasks;
			this.batchCompletionRoutine = batchCompletionRoutine;
		}

		@Override
		public Void call() throws Exception {
			List<T> results = new ArrayList<>(tasks.size());

			for (Callable<T> task : tasks) {
				results.add(task.call());
			}

			batchCompletionRoutine.accept(results);

			return null;
		}
	}
}
