package org.rty.portfolio.engine.impl.transform;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.rty.portfolio.core.AssetDividendInfo;
import org.rty.portfolio.core.utils.DatesAndSetUtil;
import org.rty.portfolio.core.utils.ToEntityConvertorsUtil;
import org.rty.portfolio.engine.AbstractTask;
import org.rty.portfolio.io.CsvWriter;

/**
 * General purposes dividends history data transformer.
 */
public class TransformDividendsDataTask extends AbstractTask {
	private static final int DIVIDEND_LINE_SIZE = 2;
	private static final String DIVIDEND_SIGN = "Dividend";

	@Override
	public void execute(Map<String, String> parameters) throws Exception {
		final String inputFile = getValidParameterValue(parameters, INPUT_FILE_PARAM);
		final String outSymbol = getValidParameterValue(parameters, OUT_SYMBOL);
		final String outputFile = getValidParameterValue(parameters, OUTPUT_FILE_PARAM);
		final String dateValueIndex = getValidParameterValue(parameters, DATE_VALUE_INDEX_PARAM);
		final String payValueIndex = getValidParameterValue(parameters, PRICE_VALUE_INDEX_PARAM);
		final String dateFormat = getValidParameterValue(parameters, DATE_FORMAT_PARAM);

		final List<AssetDividendInfo> dividends = loadDividends(outSymbol,
				inputFile,
				Integer.parseInt(payValueIndex),
				Integer.parseInt(dateValueIndex),
				DateTimeFormatter.ofPattern(dateFormat, Locale.US));

		if (!dividends.isEmpty()) {
			CsvWriter<AssetDividendInfo> writer = new CsvWriter<>(outputFile);
			writer.write(dividends);
			writer.close();
		}

		say(DONE);
	}

	private List<AssetDividendInfo> loadDividends(String assetName, String inputFile, int payValueIndex, int dateColumn,
			DateTimeFormatter dateFormatter) throws Exception {
		final List<String[]> lines = readAllLinesFrom(inputFile);

		if (lines.isEmpty()) {
			say("Nothing to read from '{}'", inputFile);
			return Collections.emptyList();
		}

		final List<AssetDividendInfo> result = new ArrayList<>(1000);
		final int minimumColumnsToHave = Math.max(dateColumn, payValueIndex);

		int i = 0;
		for (String[] line : lines) {
			if (isDividendLine(minimumColumnsToHave, line)) {
				try {
					result.add(toDividendInfo(assetName, payValueIndex, dateColumn, dateFormatter, line));

				} catch (Exception ex) {
					say("parse error at line {}, date='{}', pay='{}'",
							i,
							line[dateColumn],
							line[payValueIndex]);
					ex.printStackTrace();
				}
			}
			i++;
		}

		return result;
	}

	private AssetDividendInfo toDividendInfo(String assetName, int payValueIndex, int dateColumn,
			DateTimeFormatter dateFormatter, String[] line) {
		final Date date = DatesAndSetUtil.strToDate(dateFormatter, line[dateColumn]);
		final double price = ToEntityConvertorsUtil.doubleFromString(line[payValueIndex].replace(DIVIDEND_SIGN, ""));

		return new AssetDividendInfo(assetName, price, date);
	}

	private static boolean isDividendLine(int minimumColumnsToHave, String[] line) {
		return line.length > minimumColumnsToHave && line.length >= DIVIDEND_LINE_SIZE && hasDividendText(line);
	}

	private static boolean hasDividendText(String[] line) {
		for (String value : line) {
			if (value != null && value.endsWith(DIVIDEND_SIGN)) {
				return true;
			}
		}
		return false;
	}
}
