package org.rty.portfolio.core.utils;

public final class FileNameUtil {
	private FileNameUtil() {

	}

	public static String adjustOutputFileName(String requestedFileName, String extraDetails) {
		final int pos = requestedFileName.lastIndexOf(".");
		if (pos > -1) {
			return String.format("%s-%s%s", requestedFileName.substring(0, pos),
					extraDetails,
					requestedFileName.substring(pos));
		}

		return String.format("%s-%s.csv", requestedFileName, extraDetails);
	}
}
