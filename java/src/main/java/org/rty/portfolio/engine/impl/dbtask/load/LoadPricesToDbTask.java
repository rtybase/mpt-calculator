package org.rty.portfolio.engine.impl.dbtask.load;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.rty.portfolio.core.AssetPriceInfo;
import org.rty.portfolio.db.DbManager;

/**
 * A general purpose price CSV loader. The format must be: 
 * assetName, price, change, rate, date
 * 
 * date is in format of yyyy-MM-dd
 *
 */
public class LoadPricesToDbTask extends GenericLoadToDbTask<AssetPriceInfo> {
	private static final int DATE_COLUMN = 4;
	private static final int RATE_OF_CHANGE_COLUMN = 3;
	private static final int PRICE_CHANGE_COLUMN = 2;
	private static final int PRICE_COLUMN = 1;
	private static final int NO_OF_COLUMNS = 5;

	public LoadPricesToDbTask(DbManager dbManager) {
		super(dbManager, NO_OF_COLUMNS);
	}

	@Override
	protected void saveResults(List<AssetPriceInfo> pricesToAdd, AtomicInteger totalFail) throws Exception {
		List<String> executionResults = dbManager.addBulkPrices(pricesToAdd);

		for (String failedAsset : executionResults) {
			errorAssets.add(failedAsset);
			totalFail.incrementAndGet();
		}
	}

	@Override
	protected AssetPriceInfo toEntity(String assetName, String[] line) {
		return new AssetPriceInfo(assetName,
				Double.parseDouble(line[PRICE_COLUMN].trim()),
				Double.parseDouble(line[PRICE_CHANGE_COLUMN].trim()),
				Double.parseDouble(line[RATE_OF_CHANGE_COLUMN].trim()),
				toDate(line[DATE_COLUMN].trim()));
	}
}
