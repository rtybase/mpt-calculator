package org.rty.portfolio.engine.impl.dbtask;

import java.io.File;
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
 * A general purpose CSV loader. The format must be assetName, price, change,
 * rate, date
 * 
 * date is in format of yyyy-MM-dd
 *
 */
public class LoadCsvToDbTask extends AbstractDbTask {
	private static final int DATE_COLUMN = 4;
	private static final int RATE_OF_CHANGE_COLUMN = 3;
	private static final int PRICE_CHANGE_COLUMN = 2;
	private static final int PRICE_COLUMN = 1;
	private static final int NAME_COLUMN = 0;

	private static final SimpleDateFormat REP_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	private static final int NO_OF_COLUMNS = 5;

	private final HashSet<String> errorAssets = new HashSet<String>();

	public LoadCsvToDbTask(DbManager dbManager) {
		super(dbManager);
	}

	@Override
	public void execute(Map<String, String> parameters) throws Exception {
		String inputFile = getValidParameterValue(parameters, INPUT_FILE_PARAM);

		final File toLoadFrom = new File(inputFile);
		if (toLoadFrom.isFile()) {
			loadFromOneFiles(inputFile);
		} else if (toLoadFrom.isDirectory()) {
			File[] folderContent = toLoadFrom.listFiles();
			
			for (File file : folderContent) {
				if (file.isFile()) {
					loadFromOneFiles(file.getPath());
				}
			}
		}

		reportErrors();
		say(DONE);
	}

	private void loadFromOneFiles(String inputFile) throws Exception {
		CSVReader reader = new CSVReader(new FileReader(inputFile));
		String[] nextLine;

		say("---------------------------------------------------");
		say("Load data from '{}' ... ", inputFile);
		int total = 0;
		int failed = 0;

		while ((nextLine = reader.readNext()) != null) {
			if (nextLine.length == NO_OF_COLUMNS) {
				boolean res = false;
				++total;
				String assetName = nextLine[NAME_COLUMN].trim();

				try {
					double price = Double.parseDouble(nextLine[PRICE_COLUMN].trim());
					double change = Double.parseDouble(nextLine[PRICE_CHANGE_COLUMN].trim());
					double rate = Double.parseDouble(nextLine[RATE_OF_CHANGE_COLUMN].trim());
					Date date = CsvWriter.SCAN_DATE_FORMAT.parse(nextLine[DATE_COLUMN].trim(), new ParsePosition(0));
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

		say("File: '{}'. Total processed {}", inputFile, total);
		say("File: '{}'. Operations failed {}", inputFile, failed);
	}

	private void reportErrors() throws IOException {
		say("Number of failed assets {}", errorAssets.size());

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
