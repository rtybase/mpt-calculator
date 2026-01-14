package org.rty.portfolio.core.utils;

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import org.rty.portfolio.core.AssetEpsInfo;
import org.rty.portfolio.core.AssetNonGaapEpsInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ToAssetNonGaapEpsInfoEntityConvertor {
	private static final Logger LOGGER = LoggerFactory
			.getLogger(ToAssetNonGaapEpsInfoEntityConvertor.class.getSimpleName());

	private static final int MAX_DAYS_TOLERANCE = 41;

	private static final String AFTER_MARKEE_CLOSE_INDICATOR = "AFTER_HOURS";

	private static final String ASSET_NAME_COLUMN = "instrument_id";
	private static final String EPS_DATE_COLUMN = "date";
	private static final String EPS_COLUMN = "eps_actual";
	private static final String EPS_PREDICTED_COLUMN = "eps_forecast";
	private static final String MARKET_PHASE_COLUMN = "market_phase";
	private static final String REVENUE_COLUMN = "revenue_actual";
	private static final String REVENUE_PREDICTED_COLUMN = "revenue_forecast";

	private final Map<String, NavigableMap<Date, AssetEpsInfo>> epsStore;

	private int assetNameColumnIndex;
	private int epsDateColumnIndex;
	private int epsColumnIndex;
	private int epsPredictedColumnIndex;
	private int marketPhaseColumnIndex;
	private int revenueColumnIndex;
	private int revenuePredictedColumnIndex;

	public ToAssetNonGaapEpsInfoEntityConvertor(Map<String, NavigableMap<Date, AssetEpsInfo>> epsStore) {
		this.epsStore = Objects.requireNonNull(epsStore, "epsStore must not be null!");
		resetIndexes();
	}

	public void updateHeadersFrom(String inputFile, String[] headerLine) {
		resetIndexes();
		final List<String> headers = Arrays.asList(headerLine);

		// mandatory
		assetNameColumnIndex = headers.indexOf(ASSET_NAME_COLUMN);
		epsDateColumnIndex = headers.indexOf(EPS_DATE_COLUMN);
		epsColumnIndex = headers.indexOf(EPS_COLUMN);
		marketPhaseColumnIndex = headers.indexOf(MARKET_PHASE_COLUMN);

		// optional
		revenueColumnIndex = headers.indexOf(REVENUE_COLUMN);
		revenuePredictedColumnIndex = headers.indexOf(REVENUE_PREDICTED_COLUMN);
		epsPredictedColumnIndex = headers.indexOf(EPS_PREDICTED_COLUMN);

		if (!DataHandlingUtil.allPositive(assetNameColumnIndex, epsDateColumnIndex, epsColumnIndex, marketPhaseColumnIndex)) {
			throw new IllegalArgumentException(String.format("Not all the headers are defined in '%s'!", inputFile));
		}

		warnIfColumnNotDefined(revenueColumnIndex, REVENUE_COLUMN);
		warnIfColumnNotDefined(revenuePredictedColumnIndex, REVENUE_PREDICTED_COLUMN);
		warnIfColumnNotDefined(epsPredictedColumnIndex, EPS_PREDICTED_COLUMN);
	}

	public String assetNameFrom(String[] line) {
		return line[assetNameColumnIndex].trim();
	}

	public AssetNonGaapEpsInfo toEntity(String assetName, String[] line) {
		final Date epsDate = findCorrectedEpsDate(assetName, line);

		return new AssetNonGaapEpsInfo(assetName, Double.parseDouble(line[epsColumnIndex].trim()),
				ToEntityConvertorsUtil.valueOrDefaultFrom(line, epsPredictedColumnIndex, null),
				AFTER_MARKEE_CLOSE_INDICATOR.equalsIgnoreCase(line[marketPhaseColumnIndex].trim()),
				ToEntityConvertorsUtil.valueOrDefaultFrom(line, revenueColumnIndex, null),
				ToEntityConvertorsUtil.valueOrDefaultFrom(line, revenuePredictedColumnIndex, null),
				epsDate);
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

	private static void warnIfColumnNotDefined(int columnIndex, String columnName) {
		if (columnIndex < 0) {
			LOGGER.warn("'{}' column is not defined!", columnName);
		}
	}

	private Date findCorrectedEpsDate(String assetName, String[] line) {
		final Date epsDate = ToEntityConvertorsUtil.toDate(line[epsDateColumnIndex].trim());

		final Optional<Date> epsDateCorrected = DatesAndSetUtil.findClosestDate(epsDate, getDatesFor(assetName),
				MAX_DAYS_TOLERANCE);

		if (epsDateCorrected.isPresent()) {
			if (!epsDateCorrected.get().equals(epsDate)) {
				LOGGER.info("Using corrected date '{}' instead of '{}' for '{}'.",
						DatesAndSetUtil.dateToStr(epsDateCorrected.get()), DatesAndSetUtil.dateToStr(epsDate),
						assetName);

				return epsDateCorrected.get();
			}

		} else {
			LOGGER.warn("No close date from '{}' at '{}' found.", assetName, DatesAndSetUtil.dateToStr(epsDate));
		}
		return epsDate;
	}

	private Set<Date> getDatesFor(String assetName) {
		NavigableMap<Date, AssetEpsInfo> datesAndValues = epsStore.get(assetName);
		if (datesAndValues != null) {
			return datesAndValues.keySet();
		}

		LOGGER.warn("No EPS data from '{}' to fix the dates!!!", assetName);
		return Collections.emptySet();
	}
}
