package org.rty.portfolio.engine.impl.transform;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.rty.portfolio.core.AssetPriceInfoAccumulator;
import org.rty.portfolio.engine.AbstractTask;
import org.rty.portfolio.io.CsvWriter;

/**
 * General purposes history prices data loader.
 */
public class TransformSeriesDataTask extends AbstractTask {
	@Override
	public void execute(Map<String, String> parameters) throws Exception {
		final String inputFile = getValidParameterValue(parameters, INPUT_FILE_PARAM);
		final String outSymbol = getValidParameterValue(parameters, OUT_SYMBOL);
		final String outputFile = getValidParameterValue(parameters, OUTPUT_FILE_PARAM);
		final String dateValueIndex = getValidParameterValue(parameters, DATE_VALUE_INDEX_PARAM);
		final String priceValueIndex = getValidParameterValue(parameters, PRICE_VALUE_INDEX_PARAM);
		final String dateFormat = getValidParameterValue(parameters, DATE_FORMAT_PARAM);

		AssetPriceInfoAccumulator accumulator = populateRates(outSymbol,
				inputFile,
				Integer.parseInt(priceValueIndex),
				Integer.parseInt(dateValueIndex),
				DateTimeFormatter.ofPattern(dateFormat, Locale.US));

		CsvWriter writer = new CsvWriter(outputFile);
		writer.write(accumulator.getChangeHistory());
		writer.close();

		say(DONE);
	}

	private AssetPriceInfoAccumulator populateRates(String assetName, String inputFile, int closePriceColumn,
			int dateColumn, DateTimeFormatter dateFormatter) throws Exception {
		final AssetPriceInfoAccumulator accumulator = new AssetPriceInfoAccumulator(assetName);
		final int minimumColumnsToHave = Math.max(dateColumn, closePriceColumn);

		final List<String[]> lines = readAllLinesFrom(inputFile);
		if (lines.isEmpty()) {
			say("Nothing to read from " + inputFile);
			return accumulator;
		}

		int i = 0;
		for (String[] line : lines) {
			if (line.length > minimumColumnsToHave) {
				try {
					final Date date = strToDate(dateFormatter, line[dateColumn]);
					final double price = Double.parseDouble(line[closePriceColumn].replace(",", ""));
					accumulator.add(date, price);
				} catch (Exception ex) {
					say("parse error at line " + i
							+ ", date='" + line[dateColumn]
							+ "', price=" + line[closePriceColumn]);
					ex.printStackTrace();
				}
			}
			i++;
		}

		return accumulator;
	}

	private static Date strToDate(DateTimeFormatter dateFormatter, String value) {
		final LocalDate dateTime = LocalDate.parse(value, dateFormatter);
		return java.util.Date.from(dateTime
				.atStartOfDay()
				.atZone(ZoneId.systemDefault())
				.toInstant());
	}
}
