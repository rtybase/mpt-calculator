package org.rty.portfolio.engine.impl.dbtask.load;

import java.util.Arrays;
import java.util.List;

import org.rty.portfolio.core.AssetNonGaapEpsInfo;
import org.rty.portfolio.core.utils.ToEntityConvertorsUtil;
import org.rty.portfolio.db.DbManager;

/**
 * A general purpose non-GAAP EPS (predicted and actual) CSV loader.
 *
 */
public class LoadNonGaapEpsToDbTask extends GenericLoadToDbTask<AssetNonGaapEpsInfo> {
	public static final int NO_OF_COLUMNS = 20;

	private static final String AFTER_MARKEE_CLOSE_INDICATOR = "AFTER_HOURS";

	private static final String ASSET_NAME_COLUMN = "instrument_id";
	private static final String EPS_DATE_COLUMN = "date";
	private static final String EPS_COLUMN = "eps_actual";
	private static final String EPS_PREDICTED_COLUMN = "eps_forecast";
	private static final String MARKET_PHASE_COLUMN = "market_phase";
	private static final String REVENUE_COLUMN = "revenue_actual";
	private static final String REVENUE_PREDICTED_COLUMN = "revenue_forecast";

	private int assetNameColumnIndex;
	private int epsDateColumnIndex;
	private int epsColumnIndex;
	private int epsPredictedColumnIndex;
	private int marketPhaseColumnIndex;
	private int revenueColumnIndex;
	private int revenuePredictedColumnIndex;

	public LoadNonGaapEpsToDbTask(DbManager dbManager) {
		super(dbManager, NO_OF_COLUMNS, true);
		resetIndexes();
	}

	@Override
	protected AssetNonGaapEpsInfo toEntity(String assetName, String[] line) {
		return new AssetNonGaapEpsInfo(assetName,
				Double.parseDouble(line[epsColumnIndex].trim()),
				ToEntityConvertorsUtil.doubleFromString(line[epsPredictedColumnIndex].trim()),
				AFTER_MARKEE_CLOSE_INDICATOR.equalsIgnoreCase(line[marketPhaseColumnIndex].trim()),
				Double.parseDouble(line[revenueColumnIndex].trim()),
				ToEntityConvertorsUtil.doubleFromString(line[revenuePredictedColumnIndex].trim()),
				ToEntityConvertorsUtil.toDate(line[epsDateColumnIndex].trim()));
	}

	@Override
	protected List<String> saveResults(List<AssetNonGaapEpsInfo> dataToAdd) throws Exception {
		return dbManager.addBulkNonGaapEps(dataToAdd);
	}

	@Override
	protected void announceHeaders(String inputFile, String[] headerLine) {
		resetIndexes();
		final List<String> headers = Arrays.asList(headerLine);

		assetNameColumnIndex = headers.indexOf(ASSET_NAME_COLUMN);
		epsDateColumnIndex = headers.indexOf(EPS_DATE_COLUMN);
		epsColumnIndex = headers.indexOf(EPS_COLUMN);
		epsPredictedColumnIndex = headers.indexOf(EPS_PREDICTED_COLUMN);
		marketPhaseColumnIndex = headers.indexOf(MARKET_PHASE_COLUMN);
		revenueColumnIndex = headers.indexOf(REVENUE_COLUMN);
		revenuePredictedColumnIndex = headers.indexOf(REVENUE_PREDICTED_COLUMN);

		if (!allPositive(assetNameColumnIndex, epsDateColumnIndex, epsColumnIndex, epsPredictedColumnIndex,
				marketPhaseColumnIndex, revenueColumnIndex, revenuePredictedColumnIndex)) {
			throw new IllegalArgumentException(String.format("Not all the headers are defined in '%s'!", inputFile));
		}
	}

	@Override
	protected String assetNameFrom(String[] line) {
		return line[assetNameColumnIndex].trim();
	}

	private void resetIndexes() {
		assetNameColumnIndex = -1;
		epsDateColumnIndex = -1;
		epsColumnIndex = -1;
		epsPredictedColumnIndex = -1;
		marketPhaseColumnIndex = -1;
		revenueColumnIndex = -1;
		revenuePredictedColumnIndex = -1;
	}

	private static boolean allPositive(int... indexes) {
		for (int index : indexes) {
			if (index < 0) {
				return false;
			}
		}
		return true;
	}
}
