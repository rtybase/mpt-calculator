package org.rty.portfolio.engine.impl.transform;

import java.io.FileReader;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

import org.rty.portfolio.core.AssetPriceInfoAccumulator;
import org.rty.portfolio.engine.AbstractTask;
import org.rty.portfolio.io.CsvWriter;

import com.google.common.base.Strings;

import au.com.bytecode.opencsv.CSVReader;

public class TransformLbmaDataTask extends AbstractTask {
	private static final SimpleDateFormat SCAN_INPUT_DATE_FORMAT = new SimpleDateFormat("dd-MMM-yy");

	@Override
	public void execute(Map<String, String> parameters) throws Exception {
		String inputFile = getValidParameterValue(parameters, INPUT_FILE_PARAM);
		String outputFile = getValidParameterValue(parameters, OUTPUT_FILE_PARAM);
		String outSymbol = getValidParameterValue(parameters, OUT_SYMBOL);

		AssetPriceInfoAccumulator accumulator = populateRates(outSymbol, inputFile);
		CsvWriter writer = new CsvWriter(outputFile);
		writer.write(accumulator.getChangeHistory());
		writer.close();
		say(DONE);
	}

	private AssetPriceInfoAccumulator populateRates(String assetName, String file) throws Exception {
		say("Convert data... ");

		AssetPriceInfoAccumulator accumulator = new AssetPriceInfoAccumulator(assetName);

		int total = 0;
		CSVReader reader = new CSVReader(new FileReader(file));
		String[] nextLine;
		while ((nextLine = reader.readNext()) != null) {
			if (nextLine.length == 8) {
				try {
					Date date = SCAN_INPUT_DATE_FORMAT.parse(nextLine[0], new ParsePosition(0));
					double price = getPriceFrom(nextLine[4], nextLine[3]);
					accumulator.add(date, price);
					++total;
				} catch (Exception e) {
					say(e.toString());
				}
			}
		}
		reader.close();
		say("Total processed " + total);
		return accumulator;
	}

	private static double getPriceFrom(String price1, String price2) {
		String price = price1;
		if (Strings.isNullOrEmpty(price)) {
			price = price2;
		}
		return Double.parseDouble(price);
	}
}
