package org.rty.portfolio.engine.impl.dbtask.load;

import java.util.List;

import org.rty.portfolio.core.AssetPriceInfo;
import org.rty.portfolio.core.utils.ToEntityConvertorsUtil;
import org.rty.portfolio.db.DbManager;

/**
 * A general purpose price CSV loader. The format must be: 
 * assetName, price, change, rate, date, volume, volumeChangeRate
 * 
 * date is in format of yyyy-MM-dd
 *
 */
public class LoadPricesToDbTask extends GenericLoadToDbTask<AssetPriceInfo> {
	public static final int NO_OF_COLUMNS = 7;

	public LoadPricesToDbTask(DbManager dbManager) {
		super(dbManager, NO_OF_COLUMNS, false);
	}

	@Override
	protected List<String> saveResults(List<AssetPriceInfo> pricesToAdd) throws Exception {
		return dbManager.addBulkPrices(pricesToAdd);
	}

	@Override
	protected AssetPriceInfo toEntity(String assetName, String[] line) {
		return ToEntityConvertorsUtil.toAssetPriceInfoEntity(assetName, line);
	}
}
