package org.rty.portfolio.io;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.nio.charset.StandardCharsets;

import com.google.common.annotations.VisibleForTesting;

public final class TableParser {
	private TableParser() {
	}

	public static void parseToCsv(String inputFile, String outputFile, boolean includeHeaders) throws Exception {
		try (BufferedReader reader = new BufferedReader(new FileReader(inputFile, StandardCharsets.UTF_8));
				BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile, StandardCharsets.UTF_8))) {

			boolean inRow = false;
			boolean inCell = false;

			StringBuilder cell = new StringBuilder(256);
			StringBuilder tag = new StringBuilder(64);

			int ch;
			boolean insideTag = false;

			while ((ch = reader.read()) != -1) {
				char c = (char) ch;

				if (c == '<') {
					insideTag = true;
					tag.setLength(0);
					tag.append(c);
					continue;
				}

				if (insideTag) {
					tag.append(c);

					if (c == '>') {
						insideTag = false;
						String tagValue = tag.toString().toUpperCase();

						if (tagValue.startsWith("<TABLE")) {
							writer.write("TABLE\n");
						} else if (tagValue.startsWith("</TABLE")) {
							if (inCell) {
								writeTo(writer, cell);
								inCell = false;
							}
							if (inRow) {
								writer.write("\n");
								inRow = false;
							}
							writer.write("\n");
						} else if (tagValue.startsWith("<TR")) {
							if (inCell) {
								writeTo(writer, cell);
								inCell = false;
							}
							if (inRow) {
								writer.write("\n");
							}
							inRow = true;
						} else if (tagValue.startsWith("</TR")) {
							if (inCell) {
								writeTo(writer, cell);
								inCell = false;
							}
							writer.write("\n");
							inRow = false;
						} else if (tagValue.startsWith("<TD")
								|| includeHeaders(tagValue, "<TH", "<THEAD", includeHeaders)) {
							if (inCell) {
								writeTo(writer, cell);
							}
							inCell = true;
						} else if (tagValue.startsWith("</TD")
								|| includeHeaders(tagValue, "</TH", "</THEAD", includeHeaders)) {
							writeTo(writer, cell);
							inCell = false;
						} else {
							if (inCell) {
								cell.append(tagValue);
							}
						}
					}
					continue;
				}

				if (inCell) {
					cell.append(c);
				}
			}
		}
	}

	private static boolean includeHeaders(String tagValue, String startWithContent, String doesnStartWithContent,
			boolean includeHeaders) {
		return includeHeaders
				&& !tagValue.startsWith(doesnStartWithContent)
				&& tagValue.startsWith(startWithContent);
	}

	private static void writeTo(BufferedWriter writer, StringBuilder cell) throws Exception {
		String content = cell.toString();

		content = deleteContent(content, "<SCRIPT", "</SCRIPT>");
		content = deleteContent(content, "<CAPTION", "</CAPTION>");
		content = deleteContent(content, "<!--", "-->");
		content = deleteContent(content, "<", ">");

		writer.write("\"" + content.replace("&nbsp;", " ")
			.replace("\t", " ")
			.replace("&amp;", "&")
			.replace("  ", " ")
			.replace("\n", "")
			.replace("\r", "")
			.trim() + "\",");
		cell.setLength(0);
	}

	@VisibleForTesting
	static String deleteContent(String content, String fromWhat, String toWhat) {
		final int n = content.length();
		final int fromWhatLen = fromWhat.length();
		final int toWhatLen = toWhat.length();

		StringBuilder out = new StringBuilder(n);

		int i = 0;
		while (i < n) {

			int start = content.indexOf(fromWhat, i);

			if (start == -1) {
				out.append(content, i, n);
				break;
			}

			out.append(content, i, start);

			int end = content.indexOf(toWhat, start + fromWhatLen);
			if (end == -1) {
				break;
			}

			i = end + toWhatLen;
		}

		return out.toString();
	}
}
