package org.rty.portfolio.engine.impl.dbtask;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

import org.rty.portfolio.core.utils.ConcurrentTaskExecutorWithBatching;
import org.rty.portfolio.core.utils.ConcurrentTaskExecutorWithBatching.ExceptionThrowingConsumer;
import org.rty.portfolio.db.DbManager;
import org.rty.portfolio.engine.AbstractDbTask;

import com.mysql.jdbc.Statement;

public abstract class GenericCalculateTask<T> extends AbstractDbTask {
	public GenericCalculateTask(DbManager dbManager) {
		super(dbManager);
	}

	protected final ConcurrentTaskExecutorWithBatching<T> createExecutor(final AtomicInteger totalFail) {
		final ConcurrentTaskExecutorWithBatching<T> taskExecutor = new ConcurrentTaskExecutorWithBatching<>(8, 4096,
				3072, newResultProcessingConsumer(totalFail));
		return taskExecutor;
	}

	protected abstract int[] saveResults(List<T> resultsToSave) throws Exception;

	protected abstract boolean validateResult(T result);

	private ExceptionThrowingConsumer<List<Future<T>>> newResultProcessingConsumer(final AtomicInteger totalFail) {
		return listOfFutures -> {
			final List<T> listOfResults = new ArrayList<>(listOfFutures.size());

			for (Future<T> futureResult : listOfFutures) {
				final T calculationResult = futureResult.get();

				if (validateResult(calculationResult)) {
					listOfResults.add(calculationResult);
				} else {
					totalFail.incrementAndGet();
				}
			}

			if (!listOfResults.isEmpty()) {
				saveResultsAndReportErrors(listOfResults, totalFail);
			}
		};
	}

	private void saveResultsAndReportErrors(List<T> resultsToSave, AtomicInteger totalFail) throws Exception {
		try {
			final int[] executionResults = saveResults(resultsToSave);

			for (int result : executionResults) {
				if (result == Statement.EXECUTE_FAILED) {
					say("Failed: {}", resultsToSave.get(result));
					totalFail.incrementAndGet();
				}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
}
