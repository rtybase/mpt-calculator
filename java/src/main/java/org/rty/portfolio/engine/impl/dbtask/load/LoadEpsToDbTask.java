package org.rty.portfolio.engine.impl.dbtask.load;

import java.text.SimpleDateFormat;
import java.util.List;

import org.rty.portfolio.core.AssetEpsInfo;
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
	private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("MM/dd/yyyy");

	private static final int NO_OF_COLUMNS = 4;
	private static final int EPS_COLUMN = 1;
	private static final int EPS_PREDICTED_COLUMN = 2;
	private static final int DATE_COLUMN = 3;

	public LoadEpsToDbTask(DbManager dbManager) {
		super(dbManager, NO_OF_COLUMNS);
	}

	@Override
	protected AssetEpsInfo toEntity(String assetName, String[] line) {
		return new AssetEpsInfo(assetName,
				Double.parseDouble(line[EPS_COLUMN].trim()),
				doubleFromString(line[EPS_PREDICTED_COLUMN].trim()),
				toDate(line[DATE_COLUMN].trim(), DATE_FORMAT));
	}

	@Override
	protected List<String> saveResults(List<AssetEpsInfo> dataToAdd) throws Exception {
		return dbManager.addBulkEps(dataToAdd);
	}

	private static Double doubleFromString(String value) {
		if (value.isEmpty() || "N/A".equalsIgnoreCase(value)) {
			return null;
		}
		return Double.parseDouble(value);
	}
}
