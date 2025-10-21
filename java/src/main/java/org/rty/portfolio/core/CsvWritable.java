package org.rty.portfolio.core;

public interface CsvWritable {
	String[] toCsvLine();

	static <T extends Number> String emptyIfNull(T value) {
		if (value == null) {
			return "";
		}

		return "" + value;
	}
}
