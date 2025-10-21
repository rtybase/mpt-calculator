package org.rty.portfolio.core.utils;

import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.rty.portfolio.core.AssetEpsInfo;
import org.rty.portfolio.core.AssetPriceInfo;

public final class ToEntityConvertorsUtil {
	static final String NA_VALUE = "N/A";
	static final String NO_VALUE = "-";
	static final SimpleDateFormat EPS_DATE_FORMAT = new SimpleDateFormat("MM/dd/yyyy");

	private static final int EPS_VALUE_COLUMN = 1;
	private static final int EPS_PREDICTED_COLUMN = 2;
	private static final int EPS_DATE_COLUMN = 3;

	private static final int PRICE_VALUE_COLUMN = 1;
	private static final int PRICE_CHANGE_COLUMN = 2;
	private static final int PRICE_RATE_OF_CHANGE_COLUMN = 3;
	private static final int PRICE_DATE_COLUMN = 4;
	private static final int PRICE_VOLUME_COLUMN = 5;
	private static final int PRICE_VOLUME_CHANGE_RATE_COLUMN = 6;

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
				valueOrDefaultFrom(line, PRICE_VOLUME_COLUMN, null),
				valueOrDefaultFrom(line, PRICE_VOLUME_CHANGE_RATE_COLUMN, null),
				toDate(line[PRICE_DATE_COLUMN].trim()));
	}

	public static AssetEpsInfo toAssetEpsInfoEntity(String assetName, String[] line) {
		return new AssetEpsInfo(assetName,
				Double.parseDouble(line[EPS_VALUE_COLUMN].trim()),
				possiblyDoubleFromString(line[EPS_PREDICTED_COLUMN].trim()),
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

		return possiblyDoubleFromString(value.trim());
	}

	public static double doubleFromString(String value) {
		final String adjustedValue = value.replace(",", "");

		if (adjustedValue.toLowerCase().endsWith("b")) {
			return Double.parseDouble(adjustedValue.replace("b", "").replace("B", "")) * 1_000_000_000.D;
		} else if (adjustedValue.toLowerCase().endsWith("m")) {
			return Double.parseDouble(adjustedValue.replace("m", "").replace("M", "")) * 1_000_000.D;
		}

		return Double.parseDouble(adjustedValue);
	}

	public static Double possiblyDoubleFromString(String value) {
		if (value.isEmpty() || NA_VALUE.equalsIgnoreCase(value) || NO_VALUE.equals(value)) {
			return null;
		}
		return doubleFromString(value);
	}
}
