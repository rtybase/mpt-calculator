package org.rty.portfolio.core;

public interface CsvWritable {
	String[] toCsvLine();

	static String emptyIfNull(Double value) {
		if (value == null) {
			return "";
		}

		return "" + value;
	}
}
