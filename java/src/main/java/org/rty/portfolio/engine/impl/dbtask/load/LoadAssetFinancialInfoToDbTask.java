package org.rty.portfolio.engine.impl.dbtask.load;

import java.util.List;
import java.util.Map;

import org.rty.portfolio.core.AssetFinancialInfo;
import org.rty.portfolio.core.utils.ToAssetFinancialInfoEntityConvertor;
import org.rty.portfolio.db.DbManager;

/**
 * A general purpose CSV loader for financial data (balance sheet, cash flow,
 * etc).
 *
 */
public class LoadAssetFinancialInfoToDbTask extends GenericLoadToDbTask<AssetFinancialInfo> {
	public static final int NO_OF_COLUMNS = 9;

	private ToAssetFinancialInfoEntityConvertor convertor;

	public LoadAssetFinancialInfoToDbTask(DbManager dbManager) throws Exception {
		super(dbManager, NO_OF_COLUMNS, true);
	}

	@Override
	public void execute(Map<String, String> parameters) throws Exception {
		convertor = new ToAssetFinancialInfoEntityConvertor();

		super.execute(parameters);
	}

	@Override
	protected List<String> saveResults(List<AssetFinancialInfo> dataToAdd) throws Exception {
		return dbManager.addBulkFinancialInfo(dataToAdd);
	}

	@Override
	protected AssetFinancialInfo toEntity(String assetName, String[] line) {
		return convertor.toEntity(assetName, line);
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
