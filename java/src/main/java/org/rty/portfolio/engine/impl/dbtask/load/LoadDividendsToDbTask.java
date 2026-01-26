package org.rty.portfolio.engine.impl.dbtask.load;

import java.util.List;

import org.rty.portfolio.core.AssetDividendInfo;
import org.rty.portfolio.core.utils.ToEntityConvertorsUtil;
import org.rty.portfolio.db.DbManager;

/**
 * A general purpose dividends CSV loader. The format must be: 
 * assetName, pay, date
 * 
 * date is in format of yyyy-MM-dd
 *
 */
public class LoadDividendsToDbTask extends GenericLoadToDbTask<AssetDividendInfo> {
	public static final int NO_OF_COLUMNS = 3;

	public LoadDividendsToDbTask(DbManager dbManager) {
		super(dbManager, NO_OF_COLUMNS, false);
	}

	@Override
	protected List<String> saveResults(List<AssetDividendInfo> dataToAdd) throws Exception {
		return dbManager.addBulkDividends(dataToAdd);
	}

	@Override
	protected AssetDividendInfo toEntity(String assetName, String[] line) {
		return ToEntityConvertorsUtil.toAssetDividendInfo(assetName, line);
	}
}
