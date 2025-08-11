package org.rty.portfolio.engine.impl.transform;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;

import org.rty.portfolio.core.AssetEpsHistoricalInfo;
import org.rty.portfolio.core.AssetEpsInfo;
import org.rty.portfolio.core.AssetPriceInfo;
import org.rty.portfolio.core.utils.ToEntityConvertorsUtil;
import org.rty.portfolio.engine.AbstractTask;
import org.rty.portfolio.engine.impl.dbtask.load.LoadEpsToDbTask;
import org.rty.portfolio.engine.impl.dbtask.load.LoadPricesToDbTask;
import org.rty.portfolio.io.BulkCsvLoader;
import org.rty.portfolio.io.CsvWriter;

/**
 * EPS and Prices loader, to prepare data from ML training.
 */
public class TransformEpsDataForTrainingTask extends AbstractTask {
	public static final String INPUT_FILE_WITH_PRICES_PARAM = "-prices";
	public static final String INPUT_FILE_WITH_EPS_PARAM = "-eps";

	@Override
	public void execute(Map<String, String> parameters) throws Throwable {
		final String pricesInputFile = getValidParameterValue(parameters, INPUT_FILE_WITH_PRICES_PARAM);
		final String epsInputFile = getValidParameterValue(parameters, INPUT_FILE_WITH_EPS_PARAM);
		final String outputFile = getValidParameterValue(parameters, OUTPUT_FILE_PARAM);

		final Map<String, NavigableMap<Date, AssetEpsInfo>> epsStore = new HashMap<>();
		final Map<String, NavigableMap<Date, AssetPriceInfo>> priceStore = new HashMap<>();

		loadEpsAndPrices(epsInputFile, epsStore, pricesInputFile, priceStore);

		final List<AssetEpsHistoricalInfo> dataForTraining = new ArrayList<>(1024);

		epsStore.entrySet().forEach(entry -> {
			final String assetName = entry.getKey();
			final NavigableMap<Date, AssetEpsInfo> epsData = entry.getValue();

			epsData.entrySet().forEach(epsEntry -> {
				final Date currentDate = epsEntry.getKey();

				final AssetEpsInfo currentEps = epsEntry.getValue();
				final AssetEpsInfo previousEps = getPreviousEntry(epsData, currentDate);

				final AssetPriceInfo priceBeforeCurrentEps = getPreviousEntry(priceStore, assetName, currentDate);
				final AssetPriceInfo priceAtCurrentEps = getCurrentEntry(priceStore, assetName, currentDate);
				final AssetPriceInfo priceAfterCurrentEps = getNextEntry(priceStore, assetName, currentDate);

				if (currentEps != null && previousEps != null
						&& priceBeforeCurrentEps != null
						&& priceAtCurrentEps != null
						&& priceAfterCurrentEps != null
						&& currentEps.epsPredicted != null
						&& previousEps.epsPredicted != null) {
					dataForTraining.add(new AssetEpsHistoricalInfo(assetName,
							currentEps,
							previousEps,
							priceBeforeCurrentEps,
							priceAtCurrentEps,
							priceAfterCurrentEps));
				}
			});
		});

		if (!dataForTraining.isEmpty()) {
			CsvWriter<AssetEpsHistoricalInfo> writer = new CsvWriter<>(outputFile);
			writer.write(dataForTraining);
			writer.close();
		}
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

	private static <T> T getPreviousEntry(Map<String, NavigableMap<Date, T>> map, String assetName, Date key) {
		NavigableMap<Date, T> assetEntry = map.get(assetName);
		if (assetEntry == null) {
			return null;
		}

		return getPreviousEntry(assetEntry, key);
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

	private void loadEpsAndPrices(String epsInputFile, Map<String, NavigableMap<Date, AssetEpsInfo>> epsStore,
			String pricesInputFile, Map<String, NavigableMap<Date, AssetPriceInfo>> priceStore) throws Exception {
		final BulkCsvLoader<AssetEpsInfo> epsLoader = epsLoader(epsStore);
		final BulkCsvLoader<AssetPriceInfo> priceLoader = priceLoader(priceStore);

		say("Loading EPS data ...");
		epsLoader.load(epsInputFile);
		say("Loading price data ...");
		priceLoader.load(pricesInputFile);
	}

	private static BulkCsvLoader<AssetPriceInfo> priceLoader(
			Map<String, NavigableMap<Date, AssetPriceInfo>> priceStore) {
		return new BulkCsvLoader<>(LoadPricesToDbTask.NO_OF_COLUMNS) {

			@Override
			protected List<String> saveResults(List<AssetPriceInfo> dataToAdd) throws Exception {
				dataToAdd.forEach(entry -> {
					NavigableMap<Date, AssetPriceInfo> assteDetails = priceStore.computeIfAbsent(entry.assetName,
							k -> new TreeMap<>());
					assteDetails.put(entry.date, entry);
				});
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
				dataToAdd.forEach(entry -> {
					NavigableMap<Date, AssetEpsInfo> assteDetails = epsStore.computeIfAbsent(entry.assetName,
							k -> new TreeMap<>());
					assteDetails.put(entry.date, entry);
				});
				return Collections.emptyList();
			}

			@Override
			protected AssetEpsInfo toEntity(String assetName, String[] line) {
				return ToEntityConvertorsUtil.toAssetEpsInfoEntity(assetName, line);
			}
		};
	}
}
