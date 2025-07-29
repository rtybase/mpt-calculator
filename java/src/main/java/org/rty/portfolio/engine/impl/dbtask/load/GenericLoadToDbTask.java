package org.rty.portfolio.engine.impl.dbtask.load;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import org.rty.portfolio.db.DbManager;
import org.rty.portfolio.engine.AbstractDbTask;
import org.rty.portfolio.io.CsvWriter;

import au.com.bytecode.opencsv.CSVReader;

public abstract class GenericLoadToDbTask<T> extends AbstractDbTask {
	private static final int NAME_COLUMN = 0;
	private static final SimpleDateFormat REP_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	protected final Set<String> errorAssets = new HashSet<String>();
	private final int expectedNumberOfColumns;

	public GenericLoadToDbTask(DbManager dbManager, int expectedNumberOfColumns) {
		super(dbManager);
		this.expectedNumberOfColumns = expectedNumberOfColumns;
	}

	@Override
	public final void execute(Map<String, String> parameters) throws Exception {
		final String inputFile = getValidParameterValue(parameters, INPUT_FILE_PARAM);

		final File toLoadFrom = new File(inputFile);
		if (toLoadFrom.isFile()) {
			loadFromOneFile(inputFile);
		} else if (toLoadFrom.isDirectory()) {
			final File[] folderContent = toLoadFrom.listFiles();

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
		final AtomicInteger totalFail = new AtomicInteger(0);

		final List<T> dataToAdd = loadDataFromFile(inputFile, total, totalFail);

		if (!dataToAdd.isEmpty()) {
			dbManager.setAutoCommit(false);
			saveResultsAndCollectErrors(dataToAdd, totalFail);
			dbManager.commit();
			dbManager.setAutoCommit(true);
		}

		say("File: '{}'. Total processed {}", inputFile, total);
		say("File: '{}'. Operations failed {}", inputFile, totalFail);
	}

	private List<T> loadDataFromFile(String inputFile, AtomicInteger total, AtomicInteger totalFail)
			throws Exception {
		final List<T> dataToAdd = new ArrayList<>(1024);
		final CSVReader reader = new CSVReader(new FileReader(inputFile));

		String[] nextLine;
		while ((nextLine = reader.readNext()) != null) {
			if (nextLine.length == expectedNumberOfColumns) {
				total.incrementAndGet();

				final String assetName = nextLine[NAME_COLUMN].trim();

				try {
					dataToAdd.add(toEntity(assetName, nextLine));
				} catch (Exception e) {
					say(e.toString());

					errorAssets.add(assetName);
					totalFail.incrementAndGet();
				}
			}
		}
		reader.close();
		return dataToAdd;
	}

	private void reportErrors() throws IOException {
		say("Number of failed assets {}", errorAssets.size());

		if (errorAssets.size() > 0) {
			Calendar cal = Calendar.getInstance();
			FileWriter fw = new FileWriter(ERROR_REPORT_FILE, true);

			fw.write(REP_DATE_FORMAT.format(cal.getTime())
					+ " "
					+ this.getClass().getSimpleName()
					+ ". Check the following assets:\n");

			for (String s : errorAssets) {
				fw.write(s);
				fw.write("\n");
			}

			fw.close();
		}
	}

	private void saveResultsAndCollectErrors(List<T> dataToAdd, AtomicInteger totalFail) throws Exception {
		List<String> executionResults = saveResults(dataToAdd);

		for (String failedAsset : executionResults) {
			errorAssets.add(failedAsset);
			totalFail.incrementAndGet();
		}
	}

	protected final Date toDate(String value) {
		return toDate(value, CsvWriter.SCAN_DATE_FORMAT);
	}

	protected final Date toDate(String value, SimpleDateFormat format) {
		return format.parse(value, new ParsePosition(0));
	}

	/**
	 * Returns a list of assets that failed to save.
	 */
	protected abstract List<String> saveResults(List<T> dataToAdd) throws Exception;

	protected abstract T toEntity(String assetName, String[] line);
}
