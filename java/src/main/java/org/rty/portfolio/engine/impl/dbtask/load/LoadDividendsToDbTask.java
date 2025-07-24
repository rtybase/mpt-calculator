package org.rty.portfolio.engine.impl.dbtask.load;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.rty.portfolio.core.AssetDividendInfo;
import org.rty.portfolio.db.DbManager;

/**
 * A general purpose dividends CSV loader. The format must be: 
 * assetName, pay, date
 * 
 * date is in format of yyyy-MM-dd
 *
 */
public class LoadDividendsToDbTask extends GenericLoadToDbTask<AssetDividendInfo> {
	private static final int NO_OF_COLUMNS = 3;
	private static final int PAY_COLUMN = 1;
	private static final int DATE_COLUMN = 2;

	public LoadDividendsToDbTask(DbManager dbManager) {
		super(dbManager, NO_OF_COLUMNS);
	}

	@Override
	protected void saveResults(List<AssetDividendInfo> dataToAdd, AtomicInteger totalFail) throws Exception {
		List<String> executionResults = dbManager.addBulkDividends(dataToAdd);

		for (String failedAsset : executionResults) {
			errorAssets.add(failedAsset);
			totalFail.incrementAndGet();
		}
	}

	@Override
	protected AssetDividendInfo toEntity(String assetName, String[] line) {
		return new AssetDividendInfo(assetName,
				Double.parseDouble(line[PAY_COLUMN].trim()),
				toDate(line[DATE_COLUMN].trim()));
	}
}
