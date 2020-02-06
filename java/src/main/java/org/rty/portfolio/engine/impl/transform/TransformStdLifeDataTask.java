package org.rty.portfolio.engine.impl.transform;

import java.io.FileReader;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Map;

import org.rty.portfolio.core.AssetPriceInfo;
import org.rty.portfolio.engine.AbstractTask;
import org.rty.portfolio.io.CsvWriter;

import au.com.bytecode.opencsv.CSVReader;

public class TransformStdLifeDataTask extends AbstractTask {
	private static final SimpleDateFormat SCAN_DATE_FORMAT = new SimpleDateFormat("dd/MM/yyyy");

	@Override
	public void execute(Map<String, String> parameters) throws Exception {
		String inputFile = getValidParameterValue(parameters, INPUT_FILE_PARAM);
		String outputFile = getValidParameterValue(parameters, OUTPUT_FILE_PARAM);

		CSVReader reader = new CSVReader(new FileReader(inputFile));
		CsvWriter writer = new CsvWriter(outputFile);

		String[] nextLine;

		say("Convert data... ");
		int total = 0;
		while ((nextLine = reader.readNext()) != null) {
			if (nextLine.length == 9) {
				++total;
				writer.write(buildPriceInfo(nextLine));
			}
		}
		reader.close();
		writer.close();

		say("Total processed " + total);
		say(DONE);
	}

	private static AssetPriceInfo buildPriceInfo(String[] nextLine) {
		return new AssetPriceInfo(nextLine[0],
				Double.parseDouble(nextLine[2]),
				Double.parseDouble(nextLine[4].replace("p", "")),
				Double.parseDouble(nextLine[5].replace("%", "")),
				SCAN_DATE_FORMAT.parse(nextLine[6], new ParsePosition(0)));
	}
}
