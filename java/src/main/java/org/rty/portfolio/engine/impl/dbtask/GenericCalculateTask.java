package org.rty.portfolio.engine.impl.dbtask;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.rty.portfolio.core.utils.ConcurrentTaskExecutorWithBatching;
import org.rty.portfolio.core.utils.ConcurrentTaskExecutorWithBatching.ExceptionThrowingConsumer;
import org.rty.portfolio.db.DbManager;
import org.rty.portfolio.engine.AbstractDbTask;

import com.mysql.jdbc.Statement;

public abstract class GenericCalculateTask<T> extends AbstractDbTask {
	private static final int NUMBER_OF_THREADS = computeNoOfThreads();

	public GenericCalculateTask(DbManager dbManager) {
		super(dbManager);
	}

	protected final ConcurrentTaskExecutorWithBatching<T> createExecutor(final AtomicInteger totalFail) {
		final ConcurrentTaskExecutorWithBatching<T> taskExecutor = new ConcurrentTaskExecutorWithBatching<>(NUMBER_OF_THREADS,
				NUMBER_OF_THREADS,
				(NUMBER_OF_THREADS + 1) * 1024,
				newResultProcessingConsumer(totalFail));
		return taskExecutor;
	}

	protected abstract int[] saveResults(List<T> resultsToSave) throws Exception;

	protected abstract boolean validateResult(T result);

	private ExceptionThrowingConsumer<List<T>> newResultProcessingConsumer(final AtomicInteger totalFail) {
		return listOfInputResults -> {
			final List<T> listOfResults = new ArrayList<>(listOfInputResults.size());

			for (T calculationResult : listOfInputResults) {
				if (validateResult(calculationResult)) {
					listOfResults.add(calculationResult);
				} else {
					totalFail.incrementAndGet();
				}
			}

			if (!listOfResults.isEmpty()) {
				saveResultsAndReportErrors(listOfResults, totalFail);
				dbManager.commit();
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

	private static int computeNoOfThreads() {
		int noOfCpus = Runtime.getRuntime().availableProcessors();

		if (noOfCpus <= 3) {
			return 3;
		}

		return noOfCpus - 1; // leave one for the system needs!
	}
}
