package org.rty.portfolio.engine.impl.dbtask;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

import org.rty.portfolio.core.utils.ConcurrentTaskExecutorWithBatching;
import org.rty.portfolio.core.utils.DatesAndSetUtil;
import org.rty.portfolio.db.DbManager;
import org.rty.portfolio.engine.AbstractDbTask;
import org.rty.portfolio.engine.impl.dbtask.TwoAssetsStatsCalculationTask.TwoAssetsStatsCalculationResult;

import com.mysql.jdbc.Statement;

public class Calculate2AssetsPortfolioStatsTask extends AbstractDbTask {
	public Calculate2AssetsPortfolioStatsTask(DbManager dbManager) {
		super(dbManager);
	}

	@Override
	public void execute(Map<String, String> params) throws Exception {
		say("Prepare storage... ");
		Map<Integer, Map<String, Double>> storage = dbManager.getAllDailyRates();
		say(DONE);
		say("Prepare indexes... ");
		int[] indexes = DatesAndSetUtil.getIndexesFrom(storage);
		say(DONE);

		say("Running calculations... ");
		final long start = System.currentTimeMillis();

		final AtomicInteger total = new AtomicInteger(0);
		final AtomicInteger totalFail  = new AtomicInteger(0);

		dbManager.setAutoCommit(false);
		final ConcurrentTaskExecutorWithBatching<TwoAssetsStatsCalculationResult> taskExecutor = new ConcurrentTaskExecutorWithBatching<>(8, 4096, 4096,
				listOfFutures -> {
					final List<TwoAssetsStatsCalculationResult> listOfResults = new ArrayList<>(listOfFutures.size());

					for (Future<TwoAssetsStatsCalculationResult> futureResult : listOfFutures) {
						listOfResults.add(futureResult.get());
					}

					saveResults(listOfResults, totalFail);
				});

		for (int i = 0; i < indexes.length; ++i) {
			final Map<String, Double> rates1 = storage.get(indexes[i]);

			for (int j = i + 1; j < indexes.length; ++j) {
				final Map<String, Double> rates2 = storage.get(indexes[j]);

				Set<String> dates = DatesAndSetUtil.computeCommonValues(rates1.keySet(), rates2.keySet());

				if (DatesAndSetUtil.hasSufficientContent(dates)) {
					taskExecutor.addTask(new TwoAssetsStatsCalculationTask(indexes[i], indexes[j], dates, rates1, rates2));
				} else {
					say(indexes[i] + ":" + indexes[j] + " - have insufficient common dates.");
					totalFail.incrementAndGet();
				}

				total.incrementAndGet();
			}
			dbManager.commit();
		}
		taskExecutor.close();
		dbManager.commit();
		dbManager.setAutoCommit(true);

		long ex_time = System.currentTimeMillis() - start;
		say(DONE + ". Execution time: " + ex_time + "ms.");
		say("Total processed " + total);
		say("Failed " + totalFail);

	}

	private void saveResults(List<TwoAssetsStatsCalculationResult> resultsToSave, AtomicInteger totalFail)
			throws Exception {
		try {
			int[] executionResults = dbManager.addNew2AssetsPortfolioInfo(resultsToSave);

			for (int result : executionResults) {
				if (result == Statement.EXECUTE_FAILED) {
					say(resultsToSave.get(result).toString());
					totalFail.incrementAndGet();
				}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
}
