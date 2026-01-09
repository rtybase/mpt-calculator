package org.rty.portfolio.core.utils;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.rty.portfolio.core.AssetFinancialInfo;

public class ToAssetFinancialInfoEntityConvertor {
	private static final String ASSET_NAME_COLUMN = "Symbol";
	private static final String DATE_COLUMN = "Quarterly Ending:";
	private static final String TOTAL_CURRENT_ASSETS_COLUMN = "Total Current Assets";
	private static final String TOTAL_CURRENT_LIABILITIES_COLUMN = "Total Current Liabilities";
	private static final String TOTAL_ASSETS_COLUMN = "Total Assets";
	private static final String TOTAL_LIABILITIES_COLUMN = "Total Liabilities";
	private static final String TOTAL_EQUITY_COLUMN = "Total Equity";
	private static final String NET_CASH_FLOW_OPERATING_COLUMN = "Net Cash Flow-Operating";
	private static final String CAPITAL_EXPENDITURES_COLUMN = "Capital Expenditures";

	private int assetNameColumnIndex;
	private int dateColumnIndex;
	private int totalCurrentAssetsColumnIndex;
	private int totalCurrentLiabilitiesColumnIndex;
	private int totalAssetsColumnIndex;
	private int totalLiabilitiesColumnIndex;
	private int totalEquityColumnIndex;
	private int netCashFlowOperatingColumnIndex;
	private int capitalExpendituresColumnIndex;

	public ToAssetFinancialInfoEntityConvertor() {
		resetIndexes();
	}

	public void updateHeadersFrom(String inputFile, String[] headerLine) {
		resetIndexes();
		final List<String> headers = Arrays.asList(headerLine);

		assetNameColumnIndex = headers.indexOf(ASSET_NAME_COLUMN);
		dateColumnIndex = headers.indexOf(DATE_COLUMN);
		totalCurrentAssetsColumnIndex = headers.indexOf(TOTAL_CURRENT_ASSETS_COLUMN);
		totalCurrentLiabilitiesColumnIndex = headers.indexOf(TOTAL_CURRENT_LIABILITIES_COLUMN);
		totalAssetsColumnIndex = headers.indexOf(TOTAL_ASSETS_COLUMN);
		totalLiabilitiesColumnIndex = headers.indexOf(TOTAL_LIABILITIES_COLUMN);
		totalEquityColumnIndex = headers.indexOf(TOTAL_EQUITY_COLUMN);
		netCashFlowOperatingColumnIndex = headers.indexOf(NET_CASH_FLOW_OPERATING_COLUMN);
		capitalExpendituresColumnIndex = headers.indexOf(CAPITAL_EXPENDITURES_COLUMN);

		if (!DataHandlingUtil.allPositive(assetNameColumnIndex, dateColumnIndex, totalCurrentAssetsColumnIndex,
				totalCurrentLiabilitiesColumnIndex, totalAssetsColumnIndex, totalLiabilitiesColumnIndex,
				totalEquityColumnIndex, netCashFlowOperatingColumnIndex, capitalExpendituresColumnIndex)) {
			throw new IllegalArgumentException(String.format("Not all the headers are defined in '%s'!", inputFile));
		}
	}

	public String assetNameFrom(String[] line) {
		return line[assetNameColumnIndex].trim();
	}

	public AssetFinancialInfo toEntity(String assetName, String[] line) {
		final Date date = toDate(line);

		return new AssetFinancialInfo(assetName, date,
				ToEntityConvertorsUtil.valueOrDefaultFrom(line, totalCurrentAssetsColumnIndex, null),
				ToEntityConvertorsUtil.valueOrDefaultFrom(line, totalCurrentLiabilitiesColumnIndex, null),
				ToEntityConvertorsUtil.valueOrDefaultFrom(line, totalAssetsColumnIndex, null),
				ToEntityConvertorsUtil.valueOrDefaultFrom(line, totalLiabilitiesColumnIndex, null),
				ToEntityConvertorsUtil.valueOrDefaultFrom(line, totalEquityColumnIndex, null),
				ToEntityConvertorsUtil.valueOrDefaultFrom(line, netCashFlowOperatingColumnIndex, null),
				ToEntityConvertorsUtil.valueOrDefaultFrom(line, capitalExpendituresColumnIndex, null)) ;
	}

	private Date toDate(String[] line) {
		final String strDate = line[dateColumnIndex].trim();

		if (strDate.isEmpty()) {
			throw new IllegalArgumentException(String.format("Empty date found!"));
		}

		return ToEntityConvertorsUtil.toDate(strDate, ToEntityConvertorsUtil.EPS_DATE_FORMAT);
	}

	private void resetIndexes() {
		assetNameColumnIndex = -1;
		dateColumnIndex = -1;
		totalCurrentAssetsColumnIndex = -1;
		totalCurrentLiabilitiesColumnIndex = -1;
		totalAssetsColumnIndex = -1;
		totalLiabilitiesColumnIndex = -1;
		totalEquityColumnIndex = -1;
		netCashFlowOperatingColumnIndex = -1;
		capitalExpendituresColumnIndex = -1;
	}
}
