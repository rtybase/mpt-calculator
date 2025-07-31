package org.rty.portfolio.engine.impl.dbtask.load;

import java.text.SimpleDateFormat;
import java.util.List;

import org.rty.portfolio.core.AssetEpsInfo;
import org.rty.portfolio.db.DbManager;

/**
 * A general purpose EPS (reported on the relevant end of the financial period date) CSV loader. The format must be: 
 * assetName, eps, date
 * 
 * date is in format of MM/dd/yyyy
 *
 * This is the relevant end of the financial period date.
 */
public class LoadEarningsToDbTask extends GenericLoadToDbTask<AssetEpsInfo> {
	private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("MM/dd/yyyy");

	private static final int NO_OF_COLUMNS = 3;
	private static final int EPS_COLUMN = 1;
	private static final int DATE_COLUMN = 2;

	public LoadEarningsToDbTask(DbManager dbManager) {
		super(dbManager, NO_OF_COLUMNS);
	}

	@Override
	protected List<String> saveResults(List<AssetEpsInfo> dataToAdd) throws Exception {
		return dbManager.addBulkEarnings(dataToAdd);
	}

	@Override
	protected AssetEpsInfo toEntity(String assetName, String[] line) {
		return new AssetEpsInfo(assetName,
				Double.parseDouble(line[EPS_COLUMN].trim()),
				toDate(line[DATE_COLUMN].trim(), DATE_FORMAT));
	}
}
