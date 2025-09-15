package org.rty.portfolio.engine.impl.dbtask.load;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;

import org.rty.portfolio.core.AssetEpsInfo;
import org.rty.portfolio.core.AssetNonGaapEpsInfo;
import org.rty.portfolio.core.utils.DataHandlingUtil;
import org.rty.portfolio.core.utils.ToAssetNonGaapEpsInfoEntityConvertor;
import org.rty.portfolio.db.DbManager;

/**
 * A general purpose non-GAAP EPS (predicted and actual) CSV loader.
 *
 */
public class LoadNonGaapEpsToDbTask extends GenericLoadToDbTask<AssetNonGaapEpsInfo> {
	public static final int NO_OF_COLUMNS = 20;

	private static final int YEARS_BACK = 5;

	private ToAssetNonGaapEpsInfoEntityConvertor convertor;

	public LoadNonGaapEpsToDbTask(DbManager dbManager) throws Exception {
		super(dbManager, NO_OF_COLUMNS, true);
	}

	@Override
	public void execute(Map<String, String> parameters) throws Exception {
		say("Loading EPS data from DB ...");
		Map<String, NavigableMap<Date, AssetEpsInfo>> epsStore = new HashMap<>();
		DataHandlingUtil.addDataToMapByNameAndDate(dbManager.getAllStocksEpsInfo(YEARS_BACK, false), epsStore);
		convertor = new ToAssetNonGaapEpsInfoEntityConvertor(epsStore);

		super.execute(parameters);
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
