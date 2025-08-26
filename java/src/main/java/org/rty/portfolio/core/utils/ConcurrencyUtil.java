package org.rty.portfolio.core.utils;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.google.common.base.Preconditions;

public final class ConcurrencyUtil {
	private ConcurrencyUtil() {
	}

	public static ExecutorService createExecutorService(int numberOfThread, int taskQueuSize) {
		Preconditions.checkArgument(numberOfThread > 0, "numberOfThread must be > 0!");
		Preconditions.checkArgument(taskQueuSize > 0, "taskQueuSize must be > 0!");

		return new ThreadPoolExecutor(numberOfThread,
				numberOfThread,
				0L,
				TimeUnit.MILLISECONDS,
				new ArrayBlockingQueue<>(taskQueuSize),
				new ThreadPoolExecutor.CallerRunsPolicy());
	}
}
