package org.rty.portfolio.core.utils;

import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.rty.portfolio.core.AssetEpsInfo;
import org.rty.portfolio.core.AssetPriceInfo;

public final class ToEntityConvertorsUtil {
	static final String NA_VALUE = "N/A";
	static final SimpleDateFormat EPS_DATE_FORMAT = new SimpleDateFormat("MM/dd/yyyy");

	private static final int EPS_VALUE_COLUMN = 1;
	private static final int EPS_PREDICTED_COLUMN = 2;
	private static final int EPS_DATE_COLUMN = 3;

	private static final int PRICE_DATE_COLUMN = 4;
	private static final int PRICE_RATE_OF_CHANGE_COLUMN = 3;
	private static final int PRICE_CHANGE_COLUMN = 2;
	private static final int PRICE_VALUE_COLUMN = 1;

	private ToEntityConvertorsUtil() {}

	public static Date toDate(String value) {
		return toDate(value, DatesAndSetUtil.CSV_SCAN_DATE_FORMAT);
	}

	public static Date toDate(String value, SimpleDateFormat format) {
		return format.parse(value, new ParsePosition(0));
	}

	public static AssetPriceInfo toAssetPriceInfoEntity(String assetName, String[] line) {
		return new AssetPriceInfo(assetName,
				Double.parseDouble(line[PRICE_VALUE_COLUMN].trim()),
				Double.parseDouble(line[PRICE_CHANGE_COLUMN].trim()),
				Double.parseDouble(line[PRICE_RATE_OF_CHANGE_COLUMN].trim()),
				toDate(line[PRICE_DATE_COLUMN].trim()));
	}

	public static AssetEpsInfo toAssetEpsInfoEntity(String assetName, String[] line) {
		return new AssetEpsInfo(assetName,
				Double.parseDouble(line[EPS_VALUE_COLUMN].trim()),
				doubleFromString(line[EPS_PREDICTED_COLUMN].trim()),
				toDate(line[EPS_DATE_COLUMN].trim(), EPS_DATE_FORMAT));
	}

	public static Double valueOrDefaultFrom(String[] line, int valueIndex, Double defaultValue) {
		if (line == null || valueIndex < 0 || valueIndex >= line.length) {
			return defaultValue;
		}

		String value = line[valueIndex];
		if (value == null) {
			return null;
		}

		return doubleFromString(value.trim());
	}

	private static Double doubleFromString(String value) {
		if (value.isEmpty() || NA_VALUE.equalsIgnoreCase(value)) {
			return null;
		}
		return Double.parseDouble(value);
	}
}
