package org.rty.portfolio.engine.impl.dbtask.load;

import java.util.List;

import org.rty.portfolio.core.AssetNonGaapEpsInfo;
import org.rty.portfolio.core.utils.ToAssetNonGaapEpsInfoEntityConvertor;
import org.rty.portfolio.db.DbManager;

/**
 * A general purpose non-GAAP EPS (predicted and actual) CSV loader.
 *
 */
public class LoadNonGaapEpsToDbTask extends GenericLoadToDbTask<AssetNonGaapEpsInfo> {
	private static final int YEARS_BACK = 5;
	public static final int NO_OF_COLUMNS = 20;

	private ToAssetNonGaapEpsInfoEntityConvertor convertor;

	public LoadNonGaapEpsToDbTask(DbManager dbManager) throws Exception {
		super(dbManager, NO_OF_COLUMNS, true);

		say("Loading EPS data from DB ...");
		convertor = new ToAssetNonGaapEpsInfoEntityConvertor(dbManager.getAllStocksEpsInfo(YEARS_BACK, false));
	}

	@Override
	protected AssetNonGaapEpsInfo toEntity(String assetName, String[] line) {
		return convertor.toEntity(assetName, line);
	}

	@Override
	protected List<String> saveResults(List<AssetNonGaapEpsInfo> dataToAdd) throws Exception {
		return dbManager.addBulkNonGaapEps(dataToAdd);
	}

	@Override
	protected void announceHeaders(String inputFile, String[] headerLine) {
		convertor.updateHeadersFrom(inputFile, headerLine);
	}

	@Override
	protected String assetNameFrom(String[] line) {
		return convertor.assetNameFrom(line);
	}
}
