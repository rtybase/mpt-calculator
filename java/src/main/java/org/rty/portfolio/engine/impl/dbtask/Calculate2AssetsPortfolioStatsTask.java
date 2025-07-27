package org.rty.portfolio.engine.impl.dbtask;

import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import org.rty.portfolio.core.PortfolioStatistics;
import org.rty.portfolio.db.DbManager;

public class Calculate2AssetsPortfolioStatsTask extends Generic2AssetsCalculateTask<PortfolioStatistics> {
	private static final int YEARS_BACK = 5;

	public Calculate2AssetsPortfolioStatsTask(DbManager dbManager) {
		super(dbManager, YEARS_BACK);
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
		return new PortfolioStatsCalculator(null, storage, List.of(asset1Id, asset2Id));
	}

	@Override
	protected int[] saveResults(List<PortfolioStatistics> resultsToSave) throws Exception {
		return dbManager.addNew2AssetsPortfolioInfo(resultsToSave);
	}
}
