package org.rty.portfolio.engine.impl.dbtask;

import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicInteger;

import org.rty.portfolio.core.PortfolioStatistics;
import org.rty.portfolio.db.DbManager;

import com.mysql.jdbc.Statement;

public class Calculate2AssetsPortfolioStatsTask extends Generic2AssetsCalculateTask<PortfolioStatistics> {
	public Calculate2AssetsPortfolioStatsTask(DbManager dbManager) {
		super(dbManager);
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
	protected Callable<PortfolioStatistics> calculatorFrom(Map<Integer, Map<String, Double>> storage, int asset1Id,
			int asset2Id) {
		return new PortfolioStatsCalculator(storage, List.of(asset1Id, asset2Id));
	}

	@Override
	protected void saveResults(List<PortfolioStatistics> resultsToSave, AtomicInteger totalFail) throws Exception {
		try {
			int[] executionResults = dbManager.addNew2AssetsPortfolioInfo(resultsToSave);

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
