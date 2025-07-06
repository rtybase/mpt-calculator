package org.rty.portfolio.engine.impl.dbtask;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

import org.rty.portfolio.core.PortflioStats;
import org.rty.portfolio.core.utils.ConcurrentTaskExecutorWithBatching;
import org.rty.portfolio.core.utils.DatesAndSetUtil;
import org.rty.portfolio.db.DbManager;
import org.rty.portfolio.engine.AbstractDbTask;
import org.rty.portfolio.engine.impl.dbtask.TwoAssetsStatsCalculationTask.TwoAssetsStatsCalculationResult;

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
		final ConcurrentTaskExecutorWithBatching<TwoAssetsStatsCalculationResult> taskExecutor = new ConcurrentTaskExecutorWithBatching<>(4, 16, 128,
				listOfFutures -> {
					for (Future<TwoAssetsStatsCalculationResult> futureResult : listOfFutures) {
						final TwoAssetsStatsCalculationResult result = futureResult.get();

						boolean res = saveResults(result.asset1Id,
								result.asset2Id,
								result.covariance,
								result.correlation,
								result.portflioStats);

						if (!res) {
							say(result.toString());
							totalFail.incrementAndGet();
						}
						
					}
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

	private boolean saveResults(int asset1, int asset2, double covariance, double correlation, PortflioStats portStats)
			throws Exception {
		boolean res = false;
		try {
			res = dbManager.addNew2AssetsPortfolioInfo(
					asset1,
					asset2,
					covariance,
					correlation,
					portStats.getPortfolioWeights()[0],
					portStats.getPortfolioWeights()[1],
					portStats.getPortfolioReturn(),
					portStats.getPorfolioVariance());
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return res;
	}
}
