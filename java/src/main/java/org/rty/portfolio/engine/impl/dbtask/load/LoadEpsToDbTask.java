package org.rty.portfolio.engine.impl.dbtask.load;

import java.util.List;

import org.rty.portfolio.core.AssetEpsInfo;
import org.rty.portfolio.core.utils.ToEntityConvertorsUtil;
import org.rty.portfolio.db.DbManager;

/**
 * A general purpose EPS (predicted and actual) CSV loader. The format must be: 
 * assetName, eps, predicted_eps, date
 * 
 * date is in format of MM/dd/yyyy
 *
 * This is actual reporting date, not the relevant end of the financial period date.  
 */
public class LoadEpsToDbTask extends GenericLoadToDbTask<AssetEpsInfo> {
	public static final int NO_OF_COLUMNS = 4;

	public LoadEpsToDbTask(DbManager dbManager) {
		super(dbManager, NO_OF_COLUMNS, false);
	}

	@Override
	protected AssetEpsInfo toEntity(String assetName, String[] line) {
		return ToEntityConvertorsUtil.toAssetEpsInfoEntity(assetName, line);
	}

	@Override
	protected List<String> saveResults(List<AssetEpsInfo> dataToAdd) throws Exception {
		return dbManager.addBulkEps(dataToAdd);
	}
}
