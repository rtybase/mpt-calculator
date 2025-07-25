package org.rty.portfolio.engine.impl.dbtask;

import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicInteger;

import org.rty.portfolio.core.AssetsCorrelationInfo;
import org.rty.portfolio.db.DbManager;

import com.mysql.jdbc.Statement;

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
	protected void saveResults(List<AssetsCorrelationInfo> resultsToSave, AtomicInteger totalFail) throws Exception {
		try {
			int[] executionResults = dbManager.addBulkShiftCorrelations(resultsToSave);

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
