package org.rty.portfolio.engine.impl.transform;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;

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
import org.rty.portfolio.math.ZScoreCalculator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * EPS and Prices loader, to prepare data from ML training.
 */
public class TransformEpsDataForTrainingTask extends AbstractDbTask {
	private static final Logger LOGGER = LoggerFactory
			.getLogger(TransformEpsDataForTrainingTask.class.getSimpleName());

	public static final String INPUT_FILE_WITH_PRICES_PARAM = "-prices";
	public static final String INPUT_FILE_WITH_EPS_PARAM = "-eps";
	public static final String INPUT_FILE_WITH_NON_GAAP_EPS_PARAM = "-n-gaap-eps";

	private static final int YEARS_BACK = 5;

	public TransformEpsDataForTrainingTask(DbManager dbManager) {
		super(dbManager);
	}

	@Override
	public void execute(Map<String, String> parameters) throws Throwable {
		final String pricesInputFile = getValidParameterValue(parameters, INPUT_FILE_WITH_PRICES_PARAM);
		final String epsInputFile = getValidParameterValue(parameters, INPUT_FILE_WITH_EPS_PARAM);
		final String outputFile = getValidParameterValue(parameters, OUTPUT_FILE_PARAM);
		final String nonGaapEpsInputFile = getValidParameterValue(parameters, INPUT_FILE_WITH_NON_GAAP_EPS_PARAM);

		final Map<String, NavigableMap<Date, AssetEpsInfo>> epsStore = new HashMap<>();
		final Map<String, NavigableMap<Date, AssetPriceInfo>> priceStore = new HashMap<>();
		final Map<String, NavigableMap<Date, AssetNonGaapEpsInfo>> nonGaapEpsStore = new HashMap<>();
		final Map<String, Pair<Integer, Integer>> stocksAndsectorsStore = new HashMap<>();

		loadEpsAndPricesFromFiles(epsInputFile, epsStore, pricesInputFile, priceStore);
		loadEpsAndPricesFromDb(epsStore, priceStore, stocksAndsectorsStore);
		loadNonGaapEpsFromFilesAndDb(nonGaapEpsInputFile, epsStore, nonGaapEpsStore);

		final List<AssetEpsHistoricalInfo> dataForTraining = new ArrayList<>(1024);
		final List<AssetEpsHistoricalInfo> dataFor2DPrediction = new ArrayList<>(1024);
		final List<AssetEpsHistoricalInfo> dataFor1DPrediction = new ArrayList<>(1024);

		final ZScoreCalculator zScoreCalculatorForSector = new ZScoreCalculator();
		final ZScoreCalculator zScoreCalculatorForIndustry = new ZScoreCalculator();
		final ZScoreCalculator zScoreCalculatorForMoth = new ZScoreCalculator();
		final ZScoreCalculator zScoreCalculatorForPreviousAfterMarketClose = new ZScoreCalculator();
		final ZScoreCalculator zScoreCalculatorForPreviousPredictedEps = new ZScoreCalculator();
		final ZScoreCalculator zScoreCalculatorForPreviousEps = new ZScoreCalculator();
		final ZScoreCalculator zScoreCalculatorForPreviousNonGaapPredictedEps = new ZScoreCalculator();
		final ZScoreCalculator zScoreCalculatorForPreviousNonGaapEps = new ZScoreCalculator();
		final ZScoreCalculator zScoreCalculatorForPreviousPOverE = new ZScoreCalculator();
		final ZScoreCalculator zScoreCalculatorForCurrentAfterMarketClose = new ZScoreCalculator();
		final ZScoreCalculator zScoreCalculatorForCurrentPredictedEps = new ZScoreCalculator();
		final ZScoreCalculator zScoreCalculatorForCurrentEps = new ZScoreCalculator();
		final ZScoreCalculator zScoreCalculatorForCurrentNonGaapPredictedEps = new ZScoreCalculator();
		final ZScoreCalculator zScoreCalculatorForCurrentNonGaapEps = new ZScoreCalculator();
		final ZScoreCalculator zScoreCalculatorForCurrentPOverE = new ZScoreCalculator();


		epsStore.entrySet().forEach(entry -> {
			final String assetName = entry.getKey();
			final NavigableMap<Date, AssetEpsInfo> epsData = entry.getValue();

			epsData.entrySet().forEach(epsEntry -> {
				final Date currentDate = epsEntry.getKey();

				final AssetEpsInfo currentEps = epsEntry.getValue();
				final AssetNonGaapEpsInfo currentNonGaapEps = DataHandlingUtil.getCurrentEntry(nonGaapEpsStore,
						assetName, currentDate);

				final AssetEpsInfo previousEps = DataHandlingUtil.getPreviousEntry(epsData, currentDate);
				final AssetNonGaapEpsInfo previousNonGaapEps = DataHandlingUtil.getPreviousEntry(nonGaapEpsStore,
						assetName, currentDate);

				final Pair<Integer, Integer> sectorIndustryPair = getSectorIndustryPairFrom(assetName,
						stocksAndsectorsStore);

				final AssetPriceInfo priceAtCurrentEps = DataHandlingUtil.getCurrentEntryOrNext(priceStore, assetName,
						currentDate);

				if (DataHandlingUtil.allNotNull(sectorIndustryPair, currentEps, previousEps, priceAtCurrentEps)) {
					final AssetPriceInfo priceAtPreviousEps = DataHandlingUtil.getCurrentEntryOrNext(priceStore,
							assetName, previousEps.date);

					final AssetPriceInfo price2DaysBeforeCurrentEps = DataHandlingUtil.get2DaysPreviousEntry(priceStore,
							assetName, priceAtCurrentEps.date);
					final AssetPriceInfo priceBeforeCurrentEps = DataHandlingUtil.getPreviousEntry(priceStore,
							assetName, priceAtCurrentEps.date);
					final AssetPriceInfo priceAfterCurrentEps = DataHandlingUtil.getNextEntry(priceStore, assetName,
							priceAtCurrentEps.date);
					final AssetPriceInfo price2DaysAfterCurrentEps = DataHandlingUtil.get2DaysNextEntry(priceStore,
							assetName, priceAtCurrentEps.date);

					if (DataHandlingUtil.allNotNull(priceAtPreviousEps, price2DaysBeforeCurrentEps,
							priceBeforeCurrentEps, priceAfterCurrentEps, price2DaysAfterCurrentEps)) {
						dataForTraining.add(new AssetEpsHistoricalInfo(assetName,
								sectorIndustryPair.getKey(),
								sectorIndustryPair.getValue(),
								currentEps,
								currentNonGaapEps,
								previousEps,
								previousNonGaapEps,
								priceAtPreviousEps,
								price2DaysBeforeCurrentEps,
								priceBeforeCurrentEps,
								priceAtCurrentEps,
								priceAfterCurrentEps,
								price2DaysAfterCurrentEps,
								zScoreCalculatorForSector,
								zScoreCalculatorForIndustry,
								zScoreCalculatorForMoth,
								zScoreCalculatorForPreviousAfterMarketClose,
								zScoreCalculatorForPreviousPredictedEps,
								zScoreCalculatorForPreviousEps,
								zScoreCalculatorForPreviousNonGaapPredictedEps,
								zScoreCalculatorForPreviousNonGaapEps,
								zScoreCalculatorForPreviousPOverE,
								zScoreCalculatorForCurrentAfterMarketClose,
								zScoreCalculatorForCurrentPredictedEps,
								zScoreCalculatorForCurrentEps,
								zScoreCalculatorForCurrentNonGaapPredictedEps,
								zScoreCalculatorForCurrentNonGaapEps,
								zScoreCalculatorForCurrentPOverE));
					} else if (DataHandlingUtil.allNotNull(priceAtPreviousEps, price2DaysBeforeCurrentEps,
							priceBeforeCurrentEps, priceAfterCurrentEps)) {
						dataFor2DPrediction.add(new AssetEpsHistoricalInfo(assetName,
								sectorIndustryPair.getKey(),
								sectorIndustryPair.getValue(),
								currentEps,
								currentNonGaapEps,
								previousEps,
								previousNonGaapEps,
								priceAtPreviousEps,
								price2DaysBeforeCurrentEps,
								priceBeforeCurrentEps,
								priceAtCurrentEps,
								priceAfterCurrentEps,
								null,
								zScoreCalculatorForSector,
								zScoreCalculatorForIndustry,
								zScoreCalculatorForMoth,
								zScoreCalculatorForPreviousAfterMarketClose,
								zScoreCalculatorForPreviousPredictedEps,
								zScoreCalculatorForPreviousEps,
								zScoreCalculatorForPreviousNonGaapPredictedEps,
								zScoreCalculatorForPreviousNonGaapEps,
								zScoreCalculatorForPreviousPOverE,
								zScoreCalculatorForCurrentAfterMarketClose,
								zScoreCalculatorForCurrentPredictedEps,
								zScoreCalculatorForCurrentEps,
								zScoreCalculatorForCurrentNonGaapPredictedEps,
								zScoreCalculatorForCurrentNonGaapEps,
								zScoreCalculatorForCurrentPOverE));
					} else if (DataHandlingUtil.allNotNull(priceAtPreviousEps, price2DaysBeforeCurrentEps,
							priceBeforeCurrentEps)) {
						dataFor1DPrediction.add(new AssetEpsHistoricalInfo(assetName,
								sectorIndustryPair.getKey(),
								sectorIndustryPair.getValue(),
								currentEps,
								currentNonGaapEps,
								previousEps,
								previousNonGaapEps,
								priceAtPreviousEps,
								price2DaysBeforeCurrentEps,
								priceBeforeCurrentEps,
								priceAtCurrentEps,
								null,
								null,
								zScoreCalculatorForSector,
								zScoreCalculatorForIndustry,
								zScoreCalculatorForMoth,
								zScoreCalculatorForPreviousAfterMarketClose,
								zScoreCalculatorForPreviousPredictedEps,
								zScoreCalculatorForPreviousEps,
								zScoreCalculatorForPreviousNonGaapPredictedEps,
								zScoreCalculatorForPreviousNonGaapEps,
								zScoreCalculatorForPreviousPOverE,
								zScoreCalculatorForCurrentAfterMarketClose,
								zScoreCalculatorForCurrentPredictedEps,
								zScoreCalculatorForCurrentEps,
								zScoreCalculatorForCurrentNonGaapPredictedEps,
								zScoreCalculatorForCurrentNonGaapEps,
								zScoreCalculatorForCurrentPOverE));
					} else {
						LOGGER.info("Not all the details are available for '{}' on the '{}' and '{}' dates ", assetName,
								DatesAndSetUtil.dateToStr(currentDate),
								DatesAndSetUtil.dateToStr(previousEps.date));
					}
				} else {
					if (previousEps != null) {
						LOGGER.info("Not all the details are available for '{}' on the '{}' date.", assetName,
								DatesAndSetUtil.dateToStr(currentDate));
					}
				}
			});
		});

		zScoreCalculatorForSector.calculateMeanAndStdDev();
		zScoreCalculatorForIndustry.calculateMeanAndStdDev();
		zScoreCalculatorForMoth.calculateMeanAndStdDev();
		zScoreCalculatorForPreviousAfterMarketClose.calculateMeanAndStdDev();
		zScoreCalculatorForPreviousPredictedEps.calculateMeanAndStdDev();
		zScoreCalculatorForPreviousEps.calculateMeanAndStdDev();
		zScoreCalculatorForPreviousNonGaapPredictedEps.calculateMeanAndStdDev();
		zScoreCalculatorForPreviousNonGaapEps.calculateMeanAndStdDev();
		zScoreCalculatorForPreviousPOverE.calculateMeanAndStdDev();
		zScoreCalculatorForCurrentAfterMarketClose.calculateMeanAndStdDev();
		zScoreCalculatorForCurrentPredictedEps.calculateMeanAndStdDev();
		zScoreCalculatorForCurrentEps.calculateMeanAndStdDev();
		zScoreCalculatorForCurrentNonGaapPredictedEps.calculateMeanAndStdDev();
		zScoreCalculatorForCurrentNonGaapEps.calculateMeanAndStdDev();
		zScoreCalculatorForCurrentPOverE.calculateMeanAndStdDev();

		writeData(FileNameUtil.adjustOutputFileName(outputFile, "training-for-2d"), dataForTraining);
		writeData(FileNameUtil.adjustOutputFileName(outputFile, "training-for-1d"),
				DataHandlingUtil.addLists(dataForTraining, dataFor2DPrediction));

		writeData(FileNameUtil.adjustOutputFileName(outputFile, "pred-2d-after-eps"), dataFor2DPrediction);
		writeData(FileNameUtil.adjustOutputFileName(outputFile, "pred-1d-after-eps"), dataFor1DPrediction);
	}

	private void loadNonGaapEpsFromFilesAndDb(final String nonGaapEpsInputFile,
			final Map<String, NavigableMap<Date, AssetEpsInfo>> epsStore,
			final Map<String, NavigableMap<Date, AssetNonGaapEpsInfo>> nonGaapEpsStore) throws Exception {
		final ToAssetNonGaapEpsInfoEntityConvertor convertor = new ToAssetNonGaapEpsInfoEntityConvertor(epsStore);
		final BulkCsvLoader<AssetNonGaapEpsInfo> nonGaapEpsLoader = nonGaapEpsLoader(nonGaapEpsStore, convertor);

		say("Loading non-GAAP-EPS data from '{}' ...", nonGaapEpsInputFile);
		nonGaapEpsLoader.load(nonGaapEpsInputFile);
		say("Loading non-GAAP-EPS data from DB ...");
		DataHandlingUtil.addDataToMapByNameAndDate(dbManager.getAllStocksNonGaapEpsInfo(YEARS_BACK), nonGaapEpsStore);
	}

	private void loadEpsAndPricesFromDb(final Map<String, NavigableMap<Date, AssetEpsInfo>> epsStore,
			final Map<String, NavigableMap<Date, AssetPriceInfo>> priceStore,
			final Map<String, Pair<Integer, Integer>> stocksAndsectorsStore) throws Exception {
		say("Loading Stocks and Sectors data from DB ...");
		addStocksSectors(stocksAndsectorsStore, dbManager.getAllStocks());

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

	private Pair<Integer, Integer> getSectorIndustryPairFrom(final String assetName,
			final Map<String, Pair<Integer, Integer>> stocksAndsectorsStore) {
		final Pair<Integer, Integer> sectorIndustryPair = stocksAndsectorsStore.get(assetName);

		if (sectorIndustryPair == null) {
			say("No stock details found for the asset '{}' (sector/industry missing) ...", assetName);
		}
		return sectorIndustryPair;
	}

	private void loadEpsAndPricesFromFiles(String epsInputFile, Map<String, NavigableMap<Date, AssetEpsInfo>> epsStore,
			String pricesInputFile, Map<String, NavigableMap<Date, AssetPriceInfo>> priceStore) throws Exception {
		final BulkCsvLoader<AssetEpsInfo> epsLoader = epsLoader(epsStore);
		final BulkCsvLoader<AssetPriceInfo> priceLoader = priceLoader(priceStore);

		say("Loading EPS data from '{}' ...", epsInputFile);
		epsLoader.load(epsInputFile);

		say("Loading prices data from '{}' ...", pricesInputFile);
		priceLoader.load(pricesInputFile);
	}

	private static BulkCsvLoader<AssetPriceInfo> priceLoader(
			Map<String, NavigableMap<Date, AssetPriceInfo>> priceStore) {
		return new BulkCsvLoader<>(LoadPricesToDbTask.NO_OF_COLUMNS - 2, false) { // TODO: set to LoadPricesToDbTask.NO_OF_COLUMNS once migration is completed! 

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
}
