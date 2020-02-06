package org.rty.portfolio.engine.impl.dbtask;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.Map;

import org.rty.portfolio.db.DbManager;
import org.rty.portfolio.engine.AbstractDbTask;
import org.rty.portfolio.io.CsvWriter;

import au.com.bytecode.opencsv.CSVReader;

/**
 * A general purpose CSV loader. The format must be 
 * assetName, price, change, rate, date
 * 
 * date is in format of yyyy-MM-dd
 *
 */
public class LoadCsvToDbTask extends AbstractDbTask {
	protected static final SimpleDateFormat REP_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	private static final int NO_OF_COLUMNS = 5;

	protected final HashSet<String> errorAssets = new HashSet<String>();

	public LoadCsvToDbTask(DbManager dbManager) {
		super(dbManager);
	}

	@Override
	public void execute(Map<String, String> parameters) throws Exception {
		String inputFile = getValidParameterValue(parameters, INPUT_FILE_PARAM);

		CSVReader reader = new CSVReader(new FileReader(inputFile));
		String[] nextLine;

		say("Load data... ");
		int total = 0;
		int failed = 0;
		while ((nextLine = reader.readNext()) != null) {
			if (nextLine.length == NO_OF_COLUMNS) {
				boolean res = false;
				++total;
				String assetName = nextLine[0].trim();
				try {
					double price = Double.parseDouble(nextLine[1].trim());
					double change = Double.parseDouble(nextLine[2].trim());
					double rate = Double.parseDouble(nextLine[3].trim());
					Date date = CsvWriter.SCAN_DATE_FORMAT.parse(nextLine[4].trim(), new ParsePosition(0));
					res = dbManager.addNewPrice(assetName, price, change, rate, date);
				} catch (Exception e) {
					say(e.toString());
				}
				if (!res) {
					errorAssets.add(assetName);
					++failed;
				}
			}
		}
		reader.close();

		say("Total processed " + total);
		say("Operations failed " + failed);
		reportErrors();
		say(DONE);
	}

	private void reportErrors() throws IOException {
		say("Number of failed assets " + errorAssets.size());
		if (errorAssets.size() > 0) {
			Calendar cal = Calendar.getInstance();
			FileWriter fw = new FileWriter(ERROR_REPORT_FILE, true);

			fw.write(REP_DATE_FORMAT.format(cal.getTime()) + " Check the following assets:\n");
			for (String s : errorAssets) {
				fw.write(s);
				fw.write("\n");
			}
			fw.close();
		}
	}
}
