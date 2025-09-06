package org.rty.portfolio.io;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import au.com.bytecode.opencsv.CSVReader;

public abstract class BulkCsvLoader<T> {
	private static final Logger LOGGER = LoggerFactory.getLogger(BulkCsvLoader.class.getSimpleName());

	private static final String ERROR_REPORT_FILE = "assets.err";
	private static final SimpleDateFormat REPORT_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	protected final Set<String> errorAssets = new HashSet<String>();
	private final int expectedNumberOfColumns;
	private final boolean hasHeader;

	public BulkCsvLoader(int expectedNumberOfColumns, boolean hasHeader) {
		this.expectedNumberOfColumns = expectedNumberOfColumns;
		this.hasHeader = hasHeader;
	}

	public final void load(String inputFile) throws Exception {
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
	}

	private void loadFromOneFile(String inputFile) throws Exception {
		LOGGER.info("---------------------------------------------------");
		LOGGER.info("Load data from '{}' ... ", inputFile);

		final AtomicInteger total = new AtomicInteger(0);
		final AtomicInteger totalFail = new AtomicInteger(0);

		final List<T> dataToAdd = loadDataFromFile(inputFile, total, totalFail);

		if (!dataToAdd.isEmpty()) {
			saveResultsAndCollectErrors(dataToAdd, totalFail);
		}

		LOGGER.info("File: '{}'. Total processed {}", inputFile, total);
		LOGGER.info("File: '{}'. Operations failed {}", inputFile, totalFail);
	}

	private List<T> loadDataFromFile(String inputFile, AtomicInteger total, AtomicInteger totalFail) throws Exception {
		final List<T> dataToAdd = new ArrayList<>(1024);
		final CSVReader reader = new CSVReader(new FileReader(inputFile));

		announceHeadersIfAny(inputFile, reader);

		String[] nextLine;
		while ((nextLine = reader.readNext()) != null) {
			if (nextLine.length == expectedNumberOfColumns) {
				total.incrementAndGet();

				final String assetName = assetNameFrom(nextLine);

				try {
					dataToAdd.add(toEntity(assetName, nextLine));
				} catch (Exception e) {
					LOGGER.error("Error converting to entity", e);

					errorAssets.add(assetName);
					totalFail.incrementAndGet();
				}
			}
		}
		reader.close();
		return dataToAdd;
	}

	private void announceHeadersIfAny(String inputFile, CSVReader reader) throws IOException {
		if (hasHeader) {
			final String[] headers = reader.readNext();
			if (headers != null) {
				announceHeaders(inputFile, headers);
			}
		}
	}

	private void reportErrors() throws IOException {
		LOGGER.info("Number of failed assets {}", errorAssets.size());

		if (errorAssets.size() > 0) {
			Calendar cal = Calendar.getInstance();
			FileWriter fw = new FileWriter(ERROR_REPORT_FILE, true);

			fw.write(REPORT_DATE_FORMAT.format(cal.getTime()) + " " + this.getClass().getSimpleName()
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

	/**
	 * Returns a list of assets that failed to save.
	 */
	protected abstract List<String> saveResults(List<T> dataToAdd) throws Exception;

	protected abstract T toEntity(String assetName, String[] line);

	protected abstract void announceHeaders(String inputFile, String[] headerLine);

	protected abstract String assetNameFrom(String[] line);
}
