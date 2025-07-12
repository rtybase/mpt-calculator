package org.rty.portfolio.engine.impl.dbtask;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

import org.rty.portfolio.core.utils.ConcurrentTaskExecutorWithBatching;
import org.rty.portfolio.core.utils.DatesAndSetUtil;
import org.rty.portfolio.db.DbManager;
import org.rty.portfolio.engine.AbstractDbTask;
import org.rty.portfolio.engine.impl.dbtask.TwoAssetsStatsCalculationTask.AssetsStatsCalculationResult;

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
		final ConcurrentTaskExecutorWithBatching<AssetsStatsCalculationResult> taskExecutor = new ConcurrentTaskExecutorWithBatching<>(
				8, 4096, 3072, listOfFutures -> {
					final List<AssetsStatsCalculationResult> listOfResults = new ArrayList<>(listOfFutures.size());

					for (Future<AssetsStatsCalculationResult> futureResult : listOfFutures) {
						AssetsStatsCalculationResult calculationResult = futureResult.get();
						if (calculationResult.hasSufficientContent) {
							listOfResults.add(calculationResult);
						} else {
							say(calculationResult.assetIds.get(0)
									+ ":" + calculationResult.assetIds.get(1)
									+ " - have insufficient common dates.");
							totalFail.incrementAndGet();
						}
					}

					if (!listOfResults.isEmpty()) {
						saveResults(listOfResults, totalFail);
					}
				});

		for (int i = 0; i < indexes.length; ++i) {
			for (int j = i + 1; j < indexes.length; ++j) {
				taskExecutor.addTask(new TwoAssetsStatsCalculationTask(storage,
						List.of(indexes[i], indexes[j])));
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

	private void saveResults(List<AssetsStatsCalculationResult> resultsToSave, AtomicInteger totalFail)
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
