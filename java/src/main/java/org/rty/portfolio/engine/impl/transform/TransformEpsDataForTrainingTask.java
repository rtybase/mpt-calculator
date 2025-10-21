package org.rty.portfolio.engine.impl.transform;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NavigableMap;
import java.util.Set;

import org.apache.commons.math3.util.Pair;
import org.rty.portfolio.core.AssetEpsHistoricalInfo;
import org.rty.portfolio.core.AssetEpsInfo;
import org.rty.portfolio.core.AssetNonGaapEpsInfo;
import org.rty.portfolio.core.AssetPriceInfo;
import org.rty.portfolio.core.utils.DataHandlingUtil;
import org.rty.portfolio.core.utils.DatesAndSetUtil;
import org.rty.portfolio.core.utils.FileNameUtil;
import org.rty.portfolio.core.utils.ToAssetNonGaapEpsInfoEntityConvertor;
import org.rty.portfolio.core.utils.ToEntityConvertorsUtil;
import org.rty.portfolio.db.DbManager;
import org.rty.portfolio.engine.AbstractDbTask;
import org.rty.portfolio.engine.impl.dbtask.load.GenericLoadToDbTask;
import org.rty.portfolio.engine.impl.dbtask.load.LoadEpsToDbTask;
import org.rty.portfolio.engine.impl.dbtask.load.LoadNonGaapEpsToDbTask;
import org.rty.portfolio.engine.impl.dbtask.load.LoadPricesToDbTask;
import org.rty.portfolio.io.BulkCsvLoader;
import org.rty.portfolio.io.CsvWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.annotations.VisibleForTesting;

/**
 * EPS and Prices loader, to prepare data from ML training.
 */
public class TransformEpsDataForTrainingTask extends AbstractDbTask {
	private static final Logger LOGGER = LoggerFactory
			.getLogger(TransformEpsDataForTrainingTask.class.getSimpleName());

	public static final String INPUT_FILE_WITH_PRICES_PARAM = "-prices";
	public static final String INPUT_FILE_WITH_EPS_PARAM = "-eps";
	public static final String INPUT_FILE_WITH_EPS_WITH_ANALYSTS_PARAM = "-eps-with-analysts";
	public static final String INPUT_FILE_WITH_NON_GAAP_EPS_PARAM = "-n-gaap-eps";

	private static final int YEARS_BACK = 5;

	public TransformEpsDataForTrainingTask(DbManager dbManager) {
		super(dbManager);
	}

	@Override
	public void execute(Map<String, String> parameters) throws Throwable {
		final String pricesInputFile = getValidParameterValue(parameters, INPUT_FILE_WITH_PRICES_PARAM);
		final String epsInputFile = getValidParameterValue(parameters, INPUT_FILE_WITH_EPS_PARAM);
		final String epsWithNoOfAnalystsInputFile = getValidParameterValue(parameters, INPUT_FILE_WITH_EPS_WITH_ANALYSTS_PARAM);
		final String outputFile = getValidParameterValue(parameters, OUTPUT_FILE_PARAM);
		final String nonGaapEpsInputFile = getValidParameterValue(parameters, INPUT_FILE_WITH_NON_GAAP_EPS_PARAM);

		final Map<String, NavigableMap<Date, AssetEpsInfo>> epsStore = new HashMap<>();
		final Map<String, NavigableMap<Date, AssetPriceInfo>> priceStore = new HashMap<>();
		final Map<String, NavigableMap<Date, AssetNonGaapEpsInfo>> nonGaapEpsStore = new HashMap<>();
		final Map<String, Pair<Integer, Integer>> stocksAndsectorsStore = new HashMap<>();
		final Map<String, NavigableMap<Date, Double>> stocksAndFScoreStore = new HashMap<>();

		loadEpsAndPricesFromFiles(epsInputFile, epsWithNoOfAnalystsInputFile, epsStore, pricesInputFile, priceStore);
		loadEpsAndPricesFromDb(epsStore, priceStore, stocksAndsectorsStore, stocksAndFScoreStore);
		loadNonGaapEpsFromFilesAndDb(nonGaapEpsInputFile, epsStore, nonGaapEpsStore);

		final List<AssetEpsHistoricalInfo> dataForTraining = new ArrayList<>(1024);
		final List<AssetEpsHistoricalInfo> dataFor2DPrediction = new ArrayList<>(1024);
		final List<AssetEpsHistoricalInfo> dataFor1DPrediction = new ArrayList<>(1024);
		final Set<String> alreadyReportedAssets = new HashSet<>();

		epsStore.entrySet().forEach(entry -> {
			final String assetName = entry.getKey();
			final NavigableMap<Date, AssetEpsInfo> epsData = entry.getValue();

			epsData.entrySet().forEach(epsEntry -> collectHistoricalData(priceStore,
					nonGaapEpsStore,
					stocksAndsectorsStore,
					stocksAndFScoreStore,
					dataForTraining,
					dataFor2DPrediction,
					dataFor1DPrediction,
					assetName,
					epsData,
					epsEntry,
					alreadyReportedAssets));
		});

		writeData(FileNameUtil.adjustOutputFileName(outputFile, "training-ds-2"), dataForTraining);
		writeData(FileNameUtil.adjustOutputFileName(outputFile, "training-ds-1"),
				DataHandlingUtil.addLists(dataForTraining, dataFor2DPrediction));

		writeData(FileNameUtil.adjustOutputFileName(outputFile, "pred-ds-2"), dataFor2DPrediction);
		writeData(FileNameUtil.adjustOutputFileName(outputFile, "pred-ds-1"), dataFor1DPrediction);
	}

	@VisibleForTesting
	static void collectHistoricalData(Map<String, NavigableMap<Date, AssetPriceInfo>> priceStore,
			Map<String, NavigableMap<Date, AssetNonGaapEpsInfo>> nonGaapEpsStore,
			Map<String, Pair<Integer, Integer>> stocksAndsectorsStore,
			Map<String, NavigableMap<Date, Double>> stocksAndFScoreStore,
			List<AssetEpsHistoricalInfo> dataForTraining,
			List<AssetEpsHistoricalInfo> dataFor2DPrediction,
			List<AssetEpsHistoricalInfo> dataFor1DPrediction,
			String assetName, NavigableMap<Date, AssetEpsInfo> epsData, Entry<Date, AssetEpsInfo> epsEntry,
			Set<String> alreadyReportedAssets) {
		final Date currentDate = epsEntry.getKey();

		final AssetEpsInfo currentEps = epsEntry.getValue();
		final AssetNonGaapEpsInfo currentNonGaapEps = DataHandlingUtil.getCurrentEntry(nonGaapEpsStore, assetName,
				currentDate);
		final Double currentFScore = getClosestFScore(stocksAndFScoreStore, assetName, currentDate, alreadyReportedAssets);

		final AssetEpsInfo previousEps = DataHandlingUtil.getPreviousEntry(epsData, currentDate);
		final AssetNonGaapEpsInfo previousNonGaapEps = DataHandlingUtil.getPreviousEntry(nonGaapEpsStore, assetName,
				currentDate);
		final Double previousFScore = getClosestFScore(stocksAndFScoreStore, assetName,
				previousEps != null ? previousEps.date : null, alreadyReportedAssets);

		final Pair<Integer, Integer> sectorIndustryPair = getSectorIndustryPairFrom(assetName, stocksAndsectorsStore);
		final AssetPriceInfo priceAtCurrentEps = DataHandlingUtil.getCurrentEntryOrNext(priceStore, assetName,
				currentDate);

		final Date currentPriceDate = priceAtCurrentEps != null ? priceAtCurrentEps.date : currentDate;

		if (!DataHandlingUtil.allNotNull(sectorIndustryPair)) {
			LOGGER.warn("Sector/Industry not set for '{}'.", assetName);
			return;
		}

		if (!DataHandlingUtil.allNotNull(currentFScore, previousFScore) && previousEps != null) {
			LOGGER.warn("F-Scores are not set for '{}'.", assetName);
			return;
		}

		if (DataHandlingUtil.allNotNull(currentEps, previousEps)) {
			final AssetPriceInfo priceAtPreviousEps = DataHandlingUtil.getCurrentEntryOrNext(priceStore, assetName,
					previousEps.date);

			if (priceAtPreviousEps == null) {
				LOGGER.warn("Can't get previous price details for '{}' for EPS on '{}'.",
						assetName,
						DatesAndSetUtil.dateToStr(previousEps.date));
				return;
			}

			final AssetPriceInfo priceBeforePreviousEps = DataHandlingUtil.getPreviousEntry(priceStore, assetName,
					priceAtPreviousEps.date);

			final AssetPriceInfo price2DaysBeforeCurrentEps = DataHandlingUtil.get2DaysPreviousEntry(priceStore,
					assetName, currentPriceDate);
			final AssetPriceInfo priceBeforeCurrentEps = DataHandlingUtil.getPreviousEntry(priceStore, assetName,
					currentPriceDate);
			final AssetPriceInfo priceAfterCurrentEps = DataHandlingUtil.getNextEntry(priceStore, assetName,
					currentPriceDate);
			final AssetPriceInfo price2DaysAfterCurrentEps = DataHandlingUtil.get2DaysNextEntry(priceStore, assetName,
					currentPriceDate);

			final AssetEpsHistoricalInfo epsHistoryInfo = new AssetEpsHistoricalInfo(assetName,
					sectorIndustryPair.getKey(),
					sectorIndustryPair.getValue(),
					currentEps,
					currentNonGaapEps,
					currentFScore,
					previousEps,
					previousNonGaapEps,
					previousFScore,
					priceAtPreviousEps,
					priceBeforePreviousEps,
					price2DaysBeforeCurrentEps,
					priceBeforeCurrentEps,
					priceAtCurrentEps,
					priceAfterCurrentEps,
					price2DaysAfterCurrentEps);

			if (epsHistoryInfo.isGoodForAfterPlusOneDayEpsTraining()) {
				dataForTraining.add(epsHistoryInfo);
			} else if (epsHistoryInfo.isGoodForAfterEpsTraining()) {
				dataFor2DPrediction.add(epsHistoryInfo);
			} else if (epsHistoryInfo.isGoodForAfterEpsPrediction()) {
				dataFor1DPrediction.add(epsHistoryInfo);
			} else {
				LOGGER.info("Not all the details are available for '{}' on the '{}' and '{}' dates ", assetName,
						DatesAndSetUtil.dateToStr(currentDate),
						DatesAndSetUtil.dateToStr(previousEps.date));
			}
		} else {
			if (previousEps != null) {
				LOGGER.info("EPS values not found for '{}' on the '{}' date.", assetName,
						DatesAndSetUtil.dateToStr(currentDate));
			}
		}
	}

	private void loadNonGaapEpsFromFilesAndDb(String nonGaapEpsInputFile,
			Map<String, NavigableMap<Date, AssetEpsInfo>> epsStore,
			Map<String, NavigableMap<Date, AssetNonGaapEpsInfo>> nonGaapEpsStore) throws Exception {
		final ToAssetNonGaapEpsInfoEntityConvertor convertor = new ToAssetNonGaapEpsInfoEntityConvertor(epsStore);
		final BulkCsvLoader<AssetNonGaapEpsInfo> nonGaapEpsLoader = nonGaapEpsLoader(nonGaapEpsStore, convertor);

		say("Loading non-GAAP-EPS data from '{}' ...", nonGaapEpsInputFile);
		nonGaapEpsLoader.load(nonGaapEpsInputFile);
		say("Loading non-GAAP-EPS data from DB ...");
		DataHandlingUtil.addDataToMapByNameAndDate(dbManager.getAllStocksNonGaapEpsInfo(YEARS_BACK), nonGaapEpsStore);
	}

	private void loadEpsAndPricesFromDb(Map<String, NavigableMap<Date, AssetEpsInfo>> epsStore,
			Map<String, NavigableMap<Date, AssetPriceInfo>> priceStore,
			Map<String, Pair<Integer, Integer>> stocksAndsectorsStore,
			Map<String, NavigableMap<Date, Double>> stocksAndFScoreStore) throws Exception {
		say("Loading Stocks and Sectors data from DB ...");
		addStocksSectors(stocksAndsectorsStore, dbManager.getAllStocks());

		say("Loading Stocks and F-Scores data from DB ...");
		stocksAndFScoreStore.putAll(dbManager.getAllStockFScore(YEARS_BACK));

		say("Loading EPS data from DB ...");
		DataHandlingUtil.addDataToMapByNameAndDate(dbManager.getAllStocksEpsInfo(YEARS_BACK, true), epsStore);

		say("Loading prices data from DB ...");
		DataHandlingUtil.addDataToMapByNameAndDate(dbManager.getAllStocksPriceInfo(YEARS_BACK), priceStore);
	}

	private static void writeData(String outputFile, List<AssetEpsHistoricalInfo> data) throws Exception {
		if (!data.isEmpty()) {
			final CsvWriter<AssetEpsHistoricalInfo> writer = new CsvWriter<>(outputFile);
			writer.write(AssetEpsHistoricalInfo.HEADER);
			writer.write(data);
			writer.close();
		}
	}

	private static Pair<Integer, Integer> getSectorIndustryPairFrom(final String assetName,
			final Map<String, Pair<Integer, Integer>> stocksAndsectorsStore) {
		final Pair<Integer, Integer> sectorIndustryPair = stocksAndsectorsStore.get(assetName);

		if (sectorIndustryPair == null) {
			LOGGER.warn("No stock details found for the asset '{}' (sector/industry missing) ...", assetName);
		}
		return sectorIndustryPair;
	}

	private void loadEpsAndPricesFromFiles(String epsInputFile, String epsWithNoOfAnalystsInputFile,
			Map<String, NavigableMap<Date, AssetEpsInfo>> epsStore, String pricesInputFile,
			Map<String, NavigableMap<Date, AssetPriceInfo>> priceStore) throws Exception {
		final BulkCsvLoader<AssetEpsInfo> epsLoader = epsLoader(epsStore);
		final BulkCsvLoader<AssetEpsInfo> epsWithNoOfAnalystsLoader = epsWithNoOfAnalystsLoader(epsStore);
		final BulkCsvLoader<AssetPriceInfo> priceLoader = priceLoader(priceStore);

		say("Loading EPS data from '{}' ...", epsInputFile);
		epsLoader.load(epsInputFile);

		say("Loading EPS data with analysts details from '{}' ...", epsWithNoOfAnalystsInputFile);
		epsWithNoOfAnalystsLoader.load(epsWithNoOfAnalystsInputFile);

		say("Loading prices data from '{}' ...", pricesInputFile);
		priceLoader.load(pricesInputFile);
	}

	private static BulkCsvLoader<AssetPriceInfo> priceLoader(
			Map<String, NavigableMap<Date, AssetPriceInfo>> priceStore) {
		return new BulkCsvLoader<>(LoadPricesToDbTask.NO_OF_COLUMNS, false) {

			@Override
			protected List<String> saveResults(List<AssetPriceInfo> dataToAdd) throws Exception {
				DataHandlingUtil.addDataToMapByNameAndDate(dataToAdd, priceStore);
				return Collections.emptyList();
			}

			@Override
			protected AssetPriceInfo toEntity(String assetName, String[] line) {
				return ToEntityConvertorsUtil.toAssetPriceInfoEntity(assetName, line);
			}

			@Override
			protected void announceHeaders(String inputFile, String[] headerLine) {
				// nothing to do
			}

			@Override
			protected String assetNameFrom(String[] line) {
				return line[GenericLoadToDbTask.NAME_COLUMN].trim();
			}
		};
	}

	private static BulkCsvLoader<AssetEpsInfo> epsLoader(Map<String, NavigableMap<Date, AssetEpsInfo>> epsStore) {
		return new BulkCsvLoader<>(LoadEpsToDbTask.NO_OF_COLUMNS, false) {

			@Override
			protected List<String> saveResults(List<AssetEpsInfo> dataToAdd) throws Exception {
				DataHandlingUtil.addDataToMapByNameAndDate(dataToAdd, epsStore);
				return Collections.emptyList();
			}

			@Override
			protected AssetEpsInfo toEntity(String assetName, String[] line) {
				return ToEntityConvertorsUtil.toAssetEpsInfoEntity(assetName, line);
			}

			@Override
			protected void announceHeaders(String inputFile, String[] headerLine) {
				// nothing to do
			}

			@Override
			protected String assetNameFrom(String[] line) {
				return line[GenericLoadToDbTask.NAME_COLUMN].trim();
			}
		};
	}


	private static BulkCsvLoader<AssetEpsInfo> epsWithNoOfAnalystsLoader(Map<String, NavigableMap<Date, AssetEpsInfo>> epsStore) {
		return new BulkCsvLoader<>(6, true) {

			@Override
			protected List<String> saveResults(List<AssetEpsInfo> dataToAdd) throws Exception {
				DataHandlingUtil.addDataToMapByNameAndDate(dataToAdd, epsStore);
				return Collections.emptyList();
			}

			@Override
			protected AssetEpsInfo toEntity(String assetName, String[] line) {
				return new AssetEpsInfo(assetName,
						Double.parseDouble(line[1].trim()),
						ToEntityConvertorsUtil.possiblyDoubleFromString(line[2].trim()),
						Integer.parseInt(line[3].trim()),
						ToEntityConvertorsUtil.toDate(line[5].trim()));
			}

			@Override
			protected void announceHeaders(String inputFile, String[] headerLine) {
				// nothing to do
			}

			@Override
			protected String assetNameFrom(String[] line) {
				return line[GenericLoadToDbTask.NAME_COLUMN].trim();
			}
		};
	}

	private static BulkCsvLoader<AssetNonGaapEpsInfo> nonGaapEpsLoader(
			Map<String, NavigableMap<Date, AssetNonGaapEpsInfo>> nonGaapEpsStore,
			ToAssetNonGaapEpsInfoEntityConvertor convertor) {
		return new BulkCsvLoader<>(LoadNonGaapEpsToDbTask.NO_OF_COLUMNS, true) {

			@Override
			protected List<String> saveResults(List<AssetNonGaapEpsInfo> dataToAdd) throws Exception {
				DataHandlingUtil.addDataToMapByNameAndDate(dataToAdd, nonGaapEpsStore);
				return Collections.emptyList();
			}

			@Override
			protected AssetNonGaapEpsInfo toEntity(String assetName, String[] line) {
				return convertor.toEntity(assetName, line);
			}

			@Override
			protected void announceHeaders(String inputFile, String[] headerLine) {
				convertor.updateHeadersFrom(inputFile, headerLine);
			}

			@Override
			protected String assetNameFrom(String[] line) {
				return convertor.assetNameFrom(line);
			}
		};
	}

	private static void addStocksSectors(Map<String, Pair<Integer, Integer>> stocksAndsectorsStore,
			List<Pair<String, Pair<Integer, Integer>>> dataToAdd) {
		dataToAdd.forEach(entry -> {
			stocksAndsectorsStore.put(entry.getKey(), entry.getValue());
		});
	}

	private static Double getClosestFScore(Map<String, NavigableMap<Date, Double>> stocksAndFScoreStore,
			String assetName, Date date, Set<String> alreadyReportedAssets) {
		if (date == null) {
			return null;
		}

		Double possibleResult = DataHandlingUtil.getCurrentEntryOrPrevious(stocksAndFScoreStore, assetName, date);
		if (possibleResult != null) {
			return possibleResult;
		}

		// better than nothing case
		possibleResult = DataHandlingUtil.getCurrentEntryOrNext(stocksAndFScoreStore, assetName, date);
		if (possibleResult != null) {
			LOGGER.info("Using F-Score value after the '{}' date for the '{}'.",
					DatesAndSetUtil.dateToStr(date),
					assetName);
			return possibleResult;
		}

		if (!alreadyReportedAssets.contains(assetName)) {
			LOGGER.warn("No F-Score found for '{}', assuming 0.", assetName);
			alreadyReportedAssets.add(assetName);
		}
		return 0D;
	}
}
