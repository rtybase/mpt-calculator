package org.rty.portfolio.core.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import com.google.common.base.Preconditions;

public class ConcurrentTaskExecutorWithBatching<T> implements AutoCloseable {
	private final ExecutorService executor;
	private final List<Callable<T>> tasksToExecute;
	private final ExceptionThrowingConsumer<List<Future<T>>> batchCompletionRoutine;
	private final int batchSize;

	public ConcurrentTaskExecutorWithBatching(int numberOfThread, int taskQueuSize, int batchSize,
			ExceptionThrowingConsumer<List<Future<T>>> batchCompletionRoutine) {
		Objects.requireNonNull(batchCompletionRoutine, "batchCompletionRoutine must not be null!");
		Preconditions.checkArgument(batchSize > 0, "batchSize must be > 0!");

		this.executor = ConcurrencyUtil.createExecutorService(numberOfThread, taskQueuSize);
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
		final List<Future<T>> result = executor.invokeAll(tasksToExecute);
		batchCompletionRoutine.accept(result);
		tasksToExecute.clear();
	}

	@FunctionalInterface
	public interface ExceptionThrowingConsumer<T> {
	    void accept(T t) throws Exception;
	}
}
