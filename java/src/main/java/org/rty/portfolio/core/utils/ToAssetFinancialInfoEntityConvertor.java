package org.rty.portfolio.core.utils;

import java.time.Year;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.rty.portfolio.core.AssetFinancialInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ToAssetFinancialInfoEntityConvertor {
	private static final Logger LOGGER = LoggerFactory
			.getLogger(ToAssetFinancialInfoEntityConvertor.class.getSimpleName());

	private static final int MAX_DAYS_TOLERANCE = 10;
	private static final Set<Date> STANDARD_QUORTER_ENDS = generateEndOfQuorterDates();

	private static final String ASSET_NAME_COLUMN = "Symbol";
	private static final String DATE_COLUMN = "Quarterly Ending:";
	private static final String TOTAL_CURRENT_ASSETS_COLUMN = "Total Current Assets";
	private static final String TOTAL_CURRENT_LIABILITIES_COLUMN = "Total Current Liabilities";
	private static final String TOTAL_ASSETS_COLUMN = "Total Assets";
	private static final String TOTAL_LIABILITIES_COLUMN = "Total Liabilities";
	private static final String TOTAL_EQUITY_COLUMN = "Total Equity";
	private static final String NET_CASH_FLOW_OPERATING_COLUMN = "Net Cash Flow-Operating";
	private static final String CAPITAL_EXPENDITURES_COLUMN = "Capital Expenditures";
	private static final String SHARE_ISSUED_COLUMN = "Share Issued";

	private final boolean shouldDivideBy1000;

	private int assetNameColumnIndex;
	private int dateColumnIndex;
	private int totalCurrentAssetsColumnIndex;
	private int totalCurrentLiabilitiesColumnIndex;
	private int totalAssetsColumnIndex;
	private int totalLiabilitiesColumnIndex;
	private int totalEquityColumnIndex;
	private int netCashFlowOperatingColumnIndex;
	private int capitalExpendituresColumnIndex;
	private int shareIssuedColumnIndex;

	public ToAssetFinancialInfoEntityConvertor(boolean shouldDivideBy1000) {
		this.shouldDivideBy1000 = shouldDivideBy1000;
		resetIndexes();
	}

	public void updateHeadersFrom(String inputFile, String[] headerLine) {
		resetIndexes();
		final List<String> headers = Arrays.asList(headerLine);

		// mandatory
		assetNameColumnIndex = headers.indexOf(ASSET_NAME_COLUMN);
		dateColumnIndex = headers.indexOf(DATE_COLUMN);
		totalCurrentAssetsColumnIndex = headers.indexOf(TOTAL_CURRENT_ASSETS_COLUMN);
		totalCurrentLiabilitiesColumnIndex = headers.indexOf(TOTAL_CURRENT_LIABILITIES_COLUMN);
		totalAssetsColumnIndex = headers.indexOf(TOTAL_ASSETS_COLUMN);
		totalLiabilitiesColumnIndex = headers.indexOf(TOTAL_LIABILITIES_COLUMN);
		totalEquityColumnIndex = headers.indexOf(TOTAL_EQUITY_COLUMN);
		netCashFlowOperatingColumnIndex = headers.indexOf(NET_CASH_FLOW_OPERATING_COLUMN);
		capitalExpendituresColumnIndex = headers.indexOf(CAPITAL_EXPENDITURES_COLUMN);

		// optional
		shareIssuedColumnIndex = headers.indexOf(SHARE_ISSUED_COLUMN);

		if (!DataHandlingUtil.allPositive(assetNameColumnIndex, dateColumnIndex, totalCurrentAssetsColumnIndex,
				totalCurrentLiabilitiesColumnIndex, totalAssetsColumnIndex, totalLiabilitiesColumnIndex,
				totalEquityColumnIndex, netCashFlowOperatingColumnIndex, capitalExpendituresColumnIndex)) {
			throw new IllegalArgumentException(String.format("Not all the headers are defined in '%s'!", inputFile));
		}

		warnIfColumnNotDefined(shareIssuedColumnIndex, SHARE_ISSUED_COLUMN);
	}

	public String assetNameFrom(String[] line) {
		return line[assetNameColumnIndex].trim();
	}

	public AssetFinancialInfo toEntity(String assetName, String[] line) {
		final Date date = toDate(assetName, line);

		return new AssetFinancialInfo(assetName, date,
				divideBy1000IfNeeded(ToEntityConvertorsUtil.valueOrDefaultFrom(line, totalCurrentAssetsColumnIndex, null)),
				divideBy1000IfNeeded(ToEntityConvertorsUtil.valueOrDefaultFrom(line, totalCurrentLiabilitiesColumnIndex, null)),
				divideBy1000IfNeeded(ToEntityConvertorsUtil.valueOrDefaultFrom(line, totalAssetsColumnIndex, null)),
				divideBy1000IfNeeded(ToEntityConvertorsUtil.valueOrDefaultFrom(line, totalLiabilitiesColumnIndex, null)),
				divideBy1000IfNeeded(ToEntityConvertorsUtil.valueOrDefaultFrom(line, totalEquityColumnIndex, null)),
				divideBy1000IfNeeded(ToEntityConvertorsUtil.valueOrDefaultFrom(line, netCashFlowOperatingColumnIndex, null)),
				divideBy1000IfNeeded(alwaysNegative(ToEntityConvertorsUtil.valueOrDefaultFrom(line, capitalExpendituresColumnIndex, null))),

				// never divided by 1000!
				ToEntityConvertorsUtil.valueOrDefaultFrom(line, shareIssuedColumnIndex, null)) ;
	}

	private Double divideBy1000IfNeeded(Double val) {
		if (val == null) {
			return null;
		}

		if (shouldDivideBy1000) {
			return val / 1000D;
		}

		return val;
	}

	private Double alwaysNegative(Double val) {
		if (val == null) {
			return null;
		}

		if (val > 0) {
			return -val;
		}

		return val;
	}

	private Date toDate(String assetName, String[] line) {
		final String strDate = line[dateColumnIndex].trim();

		if (strDate.isEmpty()) {
			throw new IllegalArgumentException(String.format("Empty date found!"));
		}

		final Date parsedDate = ToEntityConvertorsUtil.toDate(strDate, ToEntityConvertorsUtil.EPS_DATE_FORMAT);
		final Optional<Date> correctedDate = DatesAndSetUtil.findClosestDate(parsedDate, STANDARD_QUORTER_ENDS,
				MAX_DAYS_TOLERANCE);

		if (correctedDate.isPresent()) {
			if (!correctedDate.get().equals(parsedDate)) {
				LOGGER.info("Using corrected date '{}' instead of '{}' for '{}'.",
						DatesAndSetUtil.dateToStr(correctedDate.get()),
						DatesAndSetUtil.dateToStr(parsedDate),
						assetName);

				return correctedDate.get();
			}

		} else {
			LOGGER.warn("No standard quorter end date for '{}' at '{}' found.",
					assetName,
					DatesAndSetUtil.dateToStr(parsedDate));
		}

		return parsedDate;
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
		shareIssuedColumnIndex = -1;
	}

	private static void warnIfColumnNotDefined(int columnIndex, String columnName) {
		if (columnIndex < 0) {
			LOGGER.warn("'{}' column is not defined!", columnName);
		}
	}

	private static Set<Date> generateEndOfQuorterDates() {
		final Set<Date> dates = new HashSet<>();

		final int startYear = Year.now().minusYears(20L).getValue();
		final int endYear = Year.now().plusYears(1L).getValue();

		for (int year = startYear; year <= endYear; year++) {
			dates.add(dateFrom(year, 1, 31));
			dates.add(dateFrom(year, 4, 30));
			dates.add(dateFrom(year, 7, 31));
			dates.add(dateFrom(year, 10, 31));

			dates.add(dateFrom(year, 12, 31));
			dates.add(dateFrom(year, 3, 31));
			dates.add(dateFrom(year, 6, 30));
			dates.add(dateFrom(year, 9, 30));

			dates.add(dateFrom(year, 2, 28));
			dates.add(dateFrom(year, 5, 31));
			dates.add(dateFrom(year, 8, 31));
			dates.add(dateFrom(year, 11, 30));
		}

		return Set.copyOf(dates);
	}

	private static Date dateFrom(int year, int month, int day) {
		return new Date(year - 1900, month - 1, day);
	}
}
