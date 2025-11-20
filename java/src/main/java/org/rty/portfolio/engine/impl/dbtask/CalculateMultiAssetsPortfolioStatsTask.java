package org.rty.portfolio.engine.impl.dbtask;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.rty.portfolio.core.PortfolioStatistics;
import org.rty.portfolio.core.utils.ConcurrentTaskExecutorWithBatching;
import org.rty.portfolio.core.utils.JsonUtil;
import org.rty.portfolio.db.DbManager;

public class CalculateMultiAssetsPortfolioStatsTask extends GenericCalculateTask<PortfolioStatistics> {
	private static final int YEARS_BACK = 5;

	public CalculateMultiAssetsPortfolioStatsTask(DbManager dbManager) {
		super(dbManager);
	}

	@Override
	public void execute(Map<String, String> parameters) throws Exception {
		final Map<Integer, List<Integer>> portfolios = loadPortfolioDefinitions();

		say("Prepare storage... ");
		Map<Integer, Map<String, Double>> storage = dbManager.getAllDailyRates(YEARS_BACK);
		say(DONE);

		say("Running calculations... ");
		final long start = System.currentTimeMillis();

		final AtomicInteger total = new AtomicInteger(0);
		final AtomicInteger totalFail = new AtomicInteger(0);

		final ConcurrentTaskExecutorWithBatching<PortfolioStatistics> taskExecutor = createExecutor(totalFail);

		for (Map.Entry<Integer, List<Integer>> portfolio : portfolios.entrySet()) {
			taskExecutor.addTask(new PortfolioStatsCalculator(portfolio.getKey(), storage, portfolio.getValue()));
			total.incrementAndGet();
		}

		taskExecutor.close();

		long ex_time = System.currentTimeMillis() - start;
		say("{}. Execution time: {}ms. Total processed {}.", DONE, ex_time, total);
	}

	@Override
	protected boolean validateResult(PortfolioStatistics result) {
		if (!result.hasSufficientContent) {
			say("Skipping {} - have insufficient common dates.", result);
			return false;
		}

		return true;
	}

	@Override
	protected int[] saveResults(List<PortfolioStatistics> resultsToSave) throws Exception {
		return dbManager.addBulkCustomPortfolioOptimalResults(
				resultsToSave.stream().map(v -> v.portflioOptimalResults).toList());
	}

	private Map<Integer, List<Integer>> loadPortfolioDefinitions() throws Exception {
		say("Load custom portfolio definitions... ");

		final Map<Integer, String> rawCustomPortfolios = dbManager.getAllCustomPortfolios();
		final Map<Integer, List<Integer>> portfolios = new HashMap<>();

		int total = 0;
		int failed = 0;

		for (Map.Entry<Integer, String> entry : rawCustomPortfolios.entrySet()) {
			total++;
			try {
				portfolios.put(entry.getKey(), JsonUtil.toList(entry.getValue()));
			} catch (Exception ex) {
				say("JSON conversion to list failed for portfolio ID '{}'", entry.getKey());
				failed++;
			}
		}

		say("Total portfolio definitions loaded/failed {}/{}", total, failed);
		return portfolios;
	}
}
