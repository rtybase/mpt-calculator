package org.rty.portfolio.core.utils;

public final class TimeUtils {
	private static final String YEARS_BACK_PROPERTY_NAME = "YEARS_BACK";
	private static final String DEFAULT_YEARS_BACK_VALUE = "-1";

	private TimeUtils() {

	}

	public static int yearsBack() {
		final String stringValue = System.getProperty(YEARS_BACK_PROPERTY_NAME, DEFAULT_YEARS_BACK_VALUE);
		return Integer.parseInt(stringValue);
	}
}
