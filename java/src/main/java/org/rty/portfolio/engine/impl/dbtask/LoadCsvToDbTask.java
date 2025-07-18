package org.rty.portfolio.engine.impl.dbtask;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import org.rty.portfolio.core.AssetPriceInfo;
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

	private final Set<String> errorAssets = new HashSet<String>();

	public LoadCsvToDbTask(DbManager dbManager) {
		super(dbManager);
	}

	@Override
	public void execute(Map<String, String> parameters) throws Exception {
		String inputFile = getValidParameterValue(parameters, INPUT_FILE_PARAM);

		final File toLoadFrom = new File(inputFile);
		if (toLoadFrom.isFile()) {
			loadFromOneFile(inputFile);
		} else if (toLoadFrom.isDirectory()) {
			File[] folderContent = toLoadFrom.listFiles();
			
			for (File file : folderContent) {
				if (file.isFile()) {
					loadFromOneFile(file.getPath());
				}
			}
		}

		reportErrors();
		say(DONE);
	}

	private void loadFromOneFile(String inputFile) throws Exception {
		say("---------------------------------------------------");
		say("Load data from '{}' ... ", inputFile);

		final AtomicInteger total = new AtomicInteger(0);
		final AtomicInteger totalFail  = new AtomicInteger(0);

		final List<AssetPriceInfo> pricesToAdd = loadDataFromFile(inputFile, total, totalFail);

		if (!pricesToAdd.isEmpty()) {
			dbManager.setAutoCommit(false);
			saveResults(pricesToAdd, totalFail);
			dbManager.commit();
			dbManager.setAutoCommit(true);
		}

		say("File: '{}'. Total processed {}", inputFile, total);
		say("File: '{}'. Operations failed {}", inputFile, totalFail);
	}

	private List<AssetPriceInfo> loadDataFromFile(String inputFile, final AtomicInteger total,
			final AtomicInteger totalFail) throws Exception {
		final List<AssetPriceInfo> pricesToAdd = new ArrayList<>(1024);
		final CSVReader reader = new CSVReader(new FileReader(inputFile));

		String[] nextLine;
		while ((nextLine = reader.readNext()) != null) {
			if (nextLine.length == NO_OF_COLUMNS) {
				total.incrementAndGet();

				final String assetName = nextLine[NAME_COLUMN].trim();

				try {
					pricesToAdd.add(toPriceInfo(assetName, nextLine));
				} catch (Exception e) {
					say(e.toString());

					errorAssets.add(assetName);
					totalFail.incrementAndGet();
				}
			}
		}
		reader.close();
		return pricesToAdd;
	}

	private void saveResults(List<AssetPriceInfo> pricesToAdd, AtomicInteger totalFail) throws Exception {
		List<String> executionResults = dbManager.addBulkPrices(pricesToAdd);

		for (String failedAsset : executionResults) {
			errorAssets.add(failedAsset);
			totalFail.incrementAndGet();
		}

	}

	private AssetPriceInfo toPriceInfo(String assetName, String[] line) {
		return new AssetPriceInfo(assetName,
				Double.parseDouble(line[PRICE_COLUMN].trim()),
				Double.parseDouble(line[PRICE_CHANGE_COLUMN].trim()),
				Double.parseDouble(line[RATE_OF_CHANGE_COLUMN].trim()),
				CsvWriter.SCAN_DATE_FORMAT.parse(line[DATE_COLUMN].trim(), new ParsePosition(0)));
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
