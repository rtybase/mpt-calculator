package org.rty.portfolio.engine.impl.transform;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;

import org.apache.commons.math3.util.Pair;
import org.rty.portfolio.core.AssetEpsHistoricalInfo;
import org.rty.portfolio.core.AssetEpsInfo;
import org.rty.portfolio.core.AssetPriceInfo;
import org.rty.portfolio.core.utils.ToEntityConvertorsUtil;
import org.rty.portfolio.db.DbManager;
import org.rty.portfolio.engine.AbstractDbTask;
import org.rty.portfolio.engine.impl.dbtask.load.LoadEpsToDbTask;
import org.rty.portfolio.engine.impl.dbtask.load.LoadPricesToDbTask;
import org.rty.portfolio.io.BulkCsvLoader;
import org.rty.portfolio.io.CsvWriter;

/**
 * EPS and Prices loader, to prepare data from ML training.
 */
public class TransformEpsDataForTrainingTask extends AbstractDbTask {
	public static final String INPUT_FILE_WITH_PRICES_PARAM = "-prices";
	public static final String INPUT_FILE_WITH_EPS_PARAM = "-eps";

	private static final int YEARS_BACK = 5;
	private static final String[] HEADER = new String[] { "asset_id", "sector", "industry", "month",
			"prev_pred_eps", "prev_eps", "prev_eps_surprize",
			"pred_eps", "eps", "eps_surprize",
			"df_prev_eps_prev_pred_eps", "df_eps_pred_eps", 
			"df_pred_eps_prev_pred_eps", "df_eps_prev_eps",
			"prev_2d_rate", "prev_rate", "rate", "next_rate", "next_2d_rate" };

	public TransformEpsDataForTrainingTask(DbManager dbManager) {
		super(dbManager);
	}

	@Override
	public void execute(Map<String, String> parameters) throws Throwable {
		final String pricesInputFile = getValidParameterValue(parameters, INPUT_FILE_WITH_PRICES_PARAM);
		final String epsInputFile = getValidParameterValue(parameters, INPUT_FILE_WITH_EPS_PARAM);
		final String outputFile = getValidParameterValue(parameters, OUTPUT_FILE_PARAM);

		final Map<String, NavigableMap<Date, AssetEpsInfo>> epsStore = new HashMap<>();
		final Map<String, NavigableMap<Date, AssetPriceInfo>> priceStore = new HashMap<>();

		final Map<String, Pair<Integer, Integer>> stocksAndsectorsStore = new HashMap<>();

		loadEpsAndPricesFromFiles(epsInputFile, epsStore,
				pricesInputFile, priceStore);

		loadEpsAndPricesFromDb(epsStore, priceStore, stocksAndsectorsStore);

		final List<AssetEpsHistoricalInfo> dataForTraining = new ArrayList<>(1024);
		final List<AssetEpsHistoricalInfo> dataFor2DPrediction = new ArrayList<>(1024);
		final List<AssetEpsHistoricalInfo> dataFor1DPrediction = new ArrayList<>(1024);

		epsStore.entrySet().forEach(entry -> {
			final String assetName = entry.getKey();
			final NavigableMap<Date, AssetEpsInfo> epsData = entry.getValue();

			epsData.entrySet().forEach(epsEntry -> {
				final Date currentDate = epsEntry.getKey();

				final AssetEpsInfo currentEps = epsEntry.getValue();
				final AssetEpsInfo previousEps = getPreviousEntry(epsData, currentDate);

				final AssetPriceInfo price2DaysBeforeCurrentEps = get2DaysPreviousEntry(priceStore, assetName,
						currentDate);
				final AssetPriceInfo priceBeforeCurrentEps = getPreviousEntry(priceStore, assetName, currentDate);
				final AssetPriceInfo priceAtCurrentEps = getCurrentEntry(priceStore, assetName, currentDate);
				final AssetPriceInfo priceAfterCurrentEps = getNextEntry(priceStore, assetName, currentDate);
				final AssetPriceInfo price2DaysAfterCurrentEps = ge2DaysNextEntry(priceStore, assetName, currentDate);
				final Pair<Integer, Integer> sectorIndustryPair = getSectorIndustryPairFrom(assetName,
						stocksAndsectorsStore);

				if (sectorIndustryPair!= null
						&& goodForTraining(currentEps)
						&& goodForTraining(previousEps)) {

					if (allNotNull(price2DaysBeforeCurrentEps, priceBeforeCurrentEps, priceAtCurrentEps,
							priceAfterCurrentEps, price2DaysAfterCurrentEps)) {
						dataForTraining.add(new AssetEpsHistoricalInfo(assetName,
								sectorIndustryPair.getKey(),
								sectorIndustryPair.getValue(),
								currentEps,
								previousEps,
								price2DaysBeforeCurrentEps,
								priceBeforeCurrentEps,
								priceAtCurrentEps,
								priceAfterCurrentEps,
								price2DaysAfterCurrentEps));
					} else if (allNotNull(price2DaysBeforeCurrentEps, priceBeforeCurrentEps, priceAtCurrentEps,
							priceAfterCurrentEps)) {
						dataFor2DPrediction.add(new AssetEpsHistoricalInfo(assetName,
								sectorIndustryPair.getKey(),
								sectorIndustryPair.getValue(),
								currentEps,
								previousEps,
								price2DaysBeforeCurrentEps,
								priceBeforeCurrentEps,
								priceAtCurrentEps,
								priceAfterCurrentEps,
								null));
					} else if (allNotNull(price2DaysBeforeCurrentEps, priceBeforeCurrentEps, priceAtCurrentEps)) {
						dataFor1DPrediction.add(new AssetEpsHistoricalInfo(assetName,
								sectorIndustryPair.getKey(),
								sectorIndustryPair.getValue(),
								currentEps,
								previousEps,
								price2DaysBeforeCurrentEps,
								priceBeforeCurrentEps,
								priceAtCurrentEps,
								null,
								null));
					}
				}
			});
		});

		CsvWriter<AssetEpsHistoricalInfo> writer = new CsvWriter<>(outputFile);
		writeData(writer, dataForTraining);
		writeData(writer, dataFor2DPrediction);
		writeData(writer, dataFor1DPrediction);
		writer.close();
	}

	private void loadEpsAndPricesFromDb(final Map<String, NavigableMap<Date, AssetEpsInfo>> epsStore,
			final Map<String, NavigableMap<Date, AssetPriceInfo>> priceStore,
			final Map<String, Pair<Integer, Integer>> stocksAndsectorsStore) throws Exception {
		say("Loading Stocks and Sectors data from DB ...");
		addStocksSectors(stocksAndsectorsStore, dbManager.getAllStocks());
		say("Loading EPS data from DB ...");
		addEpsData(epsStore, dbManager.getAllStocksEpsInfo(YEARS_BACK));
		say("Loading prices data from DB ...");
		addPricingData(priceStore, dbManager.getAllStocksPriceInfo(YEARS_BACK));
	}

	private static void writeData(CsvWriter<AssetEpsHistoricalInfo> writer, final List<AssetEpsHistoricalInfo> data) {
		if (!data.isEmpty()) {
			writer.write(HEADER);
			writer.write(data);
		}
	}

	private static boolean allNotNull(AssetPriceInfo... prices) {
		for (AssetPriceInfo price : prices) {
			if (price == null) {
				return false;
			}
		}

		return true;
	}

	private static boolean goodForTraining(AssetEpsInfo epsInfor) {
		return epsInfor != null && epsInfor.epsPredicted != null;
	}

	private Pair<Integer, Integer> getSectorIndustryPairFrom(final String assetName,
			final Map<String, Pair<Integer, Integer>> stocksAndsectorsStore) {
		final Pair<Integer, Integer> sectorIndustryPair = stocksAndsectorsStore.get(assetName);

		if (sectorIndustryPair == null) {
			say("No stock details found for the asset '{}' (sector/industry missing) ...", assetName);
		}
		return sectorIndustryPair;
	}

	private static <T> T getPreviousEntry(NavigableMap<Date, T> map, Date key) {
		final Map.Entry<Date, T> previousEntry = map.lowerEntry(key);
		if (previousEntry == null) {
			return null;
		}

		return previousEntry.getValue();
	}

	private static <T> T getNextEntry(NavigableMap<Date, T> map, Date key) {
		final Map.Entry<Date, T> nextEntry = map.higherEntry(key);
		if (nextEntry == null) {
			return null;
		}

		return nextEntry.getValue();
	}

	private static <T> T get2DaysPreviousEntry(Map<String, NavigableMap<Date, T>> map, String assetName, Date key) {
		NavigableMap<Date, T> assetEntry = map.get(assetName);
		if (assetEntry == null) {
			return null;
		}

		final Map.Entry<Date, T> previousEntry = assetEntry.lowerEntry(key);
		if (previousEntry == null) {
			return null;
		}

		return getPreviousEntry(assetEntry, previousEntry.getKey());
	}

	private static <T> T getPreviousEntry(Map<String, NavigableMap<Date, T>> map, String assetName, Date key) {
		NavigableMap<Date, T> assetEntry = map.get(assetName);
		if (assetEntry == null) {
			return null;
		}

		return getPreviousEntry(assetEntry, key);
	}

	private static <T> T ge2DaysNextEntry(Map<String, NavigableMap<Date, T>> map, String assetName, Date key) {
		NavigableMap<Date, T> assetEntry = map.get(assetName);
		if (assetEntry == null) {
			return null;
		}

		final Map.Entry<Date, T> nextEntry = assetEntry.higherEntry(key);
		if (nextEntry == null) {
			return null;
		}

		return getNextEntry(assetEntry, nextEntry.getKey());
	}

	private static <T> T getNextEntry(Map<String, NavigableMap<Date, T>> map, String assetName, Date key) {
		NavigableMap<Date, T> assetEntry = map.get(assetName);
		if (assetEntry == null) {
			return null;
		}

		return getNextEntry(assetEntry, key);
	}

	private static <T> T getCurrentEntry(Map<String, NavigableMap<Date, T>> map, String assetName, Date key) {
		NavigableMap<Date, T> firstEntry = map.get(assetName);
		if (firstEntry == null) {
			return null;
		}

		return firstEntry.get(key);
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
		return new BulkCsvLoader<>(LoadPricesToDbTask.NO_OF_COLUMNS) {

			@Override
			protected List<String> saveResults(List<AssetPriceInfo> dataToAdd) throws Exception {
				addPricingData(priceStore, dataToAdd);
				return Collections.emptyList();
			}

			@Override
			protected AssetPriceInfo toEntity(String assetName, String[] line) {
				return ToEntityConvertorsUtil.toAssetPriceInfoEntity(assetName, line);
			}
		};
	}

	private static BulkCsvLoader<AssetEpsInfo> epsLoader(Map<String, NavigableMap<Date, AssetEpsInfo>> epsStore) {
		return new BulkCsvLoader<>(LoadEpsToDbTask.NO_OF_COLUMNS) {

			@Override
			protected List<String> saveResults(List<AssetEpsInfo> dataToAdd) throws Exception {
				addEpsData(epsStore, dataToAdd);
				return Collections.emptyList();
			}

			@Override
			protected AssetEpsInfo toEntity(String assetName, String[] line) {
				return ToEntityConvertorsUtil.toAssetEpsInfoEntity(assetName, line);
			}
		};
	}

	private static void addPricingData(Map<String, NavigableMap<Date, AssetPriceInfo>> priceStore,
			List<AssetPriceInfo> dataToAdd) {
		dataToAdd.forEach(entry -> {
			NavigableMap<Date, AssetPriceInfo> assteDetails = priceStore.computeIfAbsent(entry.assetName,
					k -> new TreeMap<>());
			assteDetails.put(entry.date, entry);
		});
	}

	private static void addEpsData(Map<String, NavigableMap<Date, AssetEpsInfo>> epsStore,
			List<AssetEpsInfo> dataToAdd) {
		dataToAdd.forEach(entry -> {
			NavigableMap<Date, AssetEpsInfo> assteDetails = epsStore.computeIfAbsent(entry.assetName,
					k -> new TreeMap<>());
			assteDetails.put(entry.date, entry);
		});
	}

	private static void addStocksSectors(Map<String, Pair<Integer, Integer>> stocksAndsectorsStore,
			List<Pair<String, Pair<Integer, Integer>>> dataToAdd) {
		dataToAdd.forEach(entry -> {
			stocksAndsectorsStore.put(entry.getKey(), entry.getValue());
		});
	}
}
