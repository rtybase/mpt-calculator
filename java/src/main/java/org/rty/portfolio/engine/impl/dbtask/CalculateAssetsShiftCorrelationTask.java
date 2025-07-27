package org.rty.portfolio.engine.impl.dbtask;

import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import org.rty.portfolio.core.AssetsCorrelationInfo;
import org.rty.portfolio.db.DbManager;

public class CalculateAssetsShiftCorrelationTask extends Generic2AssetsCalculateTask<AssetsCorrelationInfo> {
	private static final int YEARS_BACK = 1;

	public CalculateAssetsShiftCorrelationTask(DbManager dbManager) {
		super(dbManager, YEARS_BACK);
	}

	@Override
	protected boolean validateResult(AssetsCorrelationInfo result) {
		if (!result.hasSufficientContent) {
			say("Skipping {} - have insufficient common dates.", result);
			return false;
		}

		if (Double.isNaN(result.bestCorrelation)) {
			say("Skipping {} - correlation is NaN.", result);
			return false;
		}

		if (Integer.MIN_VALUE == result.bestShift) {
			say("Skipping {} - correlation not found.", result);
			return false;
		}

		return true;
	}

	@Override
	protected Callable<AssetsCorrelationInfo> calculatorFrom(Map<Integer, Map<String, Double>> storage, int asset1Id,
			int asset2Id) {
		return new AssetsShiftCorrelationCalculator(storage, asset1Id, asset2Id);
	}

	@Override
	protected int[] saveResults(List<AssetsCorrelationInfo> resultsToSave) throws Exception {
		return dbManager.addBulkShiftCorrelations(resultsToSave);
	}
}
