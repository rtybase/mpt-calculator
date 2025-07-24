package org.rty.portfolio.engine.impl.dbtask;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

import org.rty.portfolio.core.utils.ConcurrentTaskExecutorWithBatching;
import org.rty.portfolio.core.utils.DatesAndSetUtil;
import org.rty.portfolio.db.DbManager;
import org.rty.portfolio.engine.AbstractDbTask;

public abstract class Generic2AssetsCalculateTask<T> extends AbstractDbTask {
	public Generic2AssetsCalculateTask(DbManager dbManager) {
		super(dbManager);
	}

	@Override
	public final void execute(Map<String, String> params) throws Exception {
		say("Prepare storage... ");
		Map<Integer, Map<String, Double>> storage = dbManager.getAllDailyRates();
		say(DONE);
		say("Prepare indexes... ");
		int[] indexes = DatesAndSetUtil.getIndexesFrom(storage);
		say(DONE);

		say("Running calculations... ");
		final long start = System.currentTimeMillis();

		final AtomicInteger total = new AtomicInteger(0);
		final AtomicInteger totalFail = new AtomicInteger(0);

		dbManager.setAutoCommit(false);
		final ConcurrentTaskExecutorWithBatching<T> taskExecutor = new ConcurrentTaskExecutorWithBatching<>(8, 4096,
				3072, listOfFutures -> {
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
						saveResults(listOfResults, totalFail);
					}
				});

		for (int i = 0; i < indexes.length; ++i) {
			for (int j = i + 1; j < indexes.length; ++j) {
				taskExecutor.addTask(calculatorFrom(storage, indexes[i], indexes[j]));
				total.incrementAndGet();
			}
			dbManager.commit();
		}

		taskExecutor.close();
		dbManager.commit();
		dbManager.setAutoCommit(true);

		long ex_time = System.currentTimeMillis() - start;
		say("{}. Execution time: {}ms,", DONE, ex_time);
		say("Total processed {}", total);
		say("Failed {}", totalFail);
	}

	protected abstract void saveResults(List<T> resultsToSave, AtomicInteger totalFail) throws Exception;

	protected abstract Callable<T> calculatorFrom(Map<Integer, Map<String, Double>> storage, int asset1Id,
			int asset2Id);

	protected abstract boolean validateResult(T result);
}
