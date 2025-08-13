package org.rty.portfolio.engine.impl.transform;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.apache.commons.math3.util.Pair;
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
	public static final String INPUT_FILE_WITH_SECTORS_PARAM = "-sector";

	@Override
	public void execute(Map<String, String> parameters) throws Throwable {
		final String pricesInputFile = getValidParameterValue(parameters, INPUT_FILE_WITH_PRICES_PARAM);
		final String epsInputFile = getValidParameterValue(parameters, INPUT_FILE_WITH_EPS_PARAM);
		final String sectorsInputFile = getValidParameterValue(parameters, INPUT_FILE_WITH_SECTORS_PARAM);
		final String outputFile = getValidParameterValue(parameters, OUTPUT_FILE_PARAM);

		final Map<String, NavigableMap<Date, AssetEpsInfo>> epsStore = new HashMap<>();
		final Map<String, NavigableMap<Date, AssetPriceInfo>> priceStore = new HashMap<>();

		final Map<String, Pair<String, String>> sectorsStore = new HashMap<>();

		loadEpsAndPrices(epsInputFile, epsStore,
				pricesInputFile, priceStore,
				sectorsInputFile, sectorsStore);

		final Map<String, Integer> sectorIndexes = indexSectors(sectorsStore);
		final Map<String, Integer> industryIndexes = indexIndustry(sectorsStore);

		final List<AssetEpsHistoricalInfo> dataForTraining = new ArrayList<>(1024);

		epsStore.entrySet().forEach(entry -> {
			final String assetName = entry.getKey();
			final NavigableMap<Date, AssetEpsInfo> epsData = entry.getValue();

			epsData.entrySet().forEach(epsEntry -> {
				final Date currentDate = epsEntry.getKey();

				final AssetEpsInfo currentEps = epsEntry.getValue();
				final AssetEpsInfo previousEps = getPreviousEntry(epsData, currentDate);

				final AssetPriceInfo price2DaysBeforeCurrentEps = get2DaysPreviousEntry(priceStore, assetName, currentDate);
				final AssetPriceInfo priceBeforeCurrentEps = getPreviousEntry(priceStore, assetName, currentDate);
				final AssetPriceInfo priceAtCurrentEps = getCurrentEntry(priceStore, assetName, currentDate);
				final AssetPriceInfo priceAfterCurrentEps = getNextEntry(priceStore, assetName, currentDate);
				final AssetPriceInfo price2DaysAfterCurrentEps = ge2DaysNextEntry(priceStore, assetName, currentDate);

				if (currentEps != null && previousEps != null
						&& price2DaysBeforeCurrentEps != null
						&& priceBeforeCurrentEps != null
						&& priceAtCurrentEps != null
						&& priceAfterCurrentEps != null
						&& price2DaysAfterCurrentEps != null
						&& currentEps.epsPredicted != null
						&& previousEps.epsPredicted != null) {

					try {
						final Pair<String, String> sectorIndustryPair = sectorsStore.get(assetName);
						final int sectorIndex = sectorIndexes.get(sectorIndustryPair.getKey());
						final int industryIndex = industryIndexes.get(sectorIndustryPair.getValue());

						dataForTraining.add(new AssetEpsHistoricalInfo(assetName,
								sectorIndex,
								sectorIndustryPair.getKey(),
								industryIndex,
								sectorIndustryPair.getValue(),
								currentEps,
								previousEps,
								price2DaysBeforeCurrentEps,
								priceBeforeCurrentEps,
								priceAtCurrentEps,
								priceAfterCurrentEps,
								price2DaysAfterCurrentEps));
					} catch (Exception ex) {
						say("Asset '{}' sector problem ...", assetName);
					}
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

	private Map<String, Integer> indexSectors(Map<String, Pair<String, String>> sectorsStore) {
		final Map<String, Integer> result = new HashMap<>();
		final Set<String> sectorsOrdered = new TreeSet<>(sectorsStore.values().stream().map(Pair::getKey).toList());

		int i = 0;
		for (String sector : sectorsOrdered) {
			result.put(sector, i);
			i++;
		}

		say("Sectors: '{}'", sectorsOrdered);
		say("Sectors map: '{}'", result);
		return result;
	}

	private Map<String, Integer> indexIndustry(Map<String, Pair<String, String>> sectorsStore) {
		final Map<String, Integer> result = new HashMap<>();
		final Set<String> industriesOrdered = new TreeSet<>(sectorsStore.values().stream().map(Pair::getValue).toList());

		int i = 0;
		for (String industry : industriesOrdered) {
			result.put(industry, i);
			i++;
		}

		say("Industries: '{}'", industriesOrdered);
		say("Industries map: '{}'", result);
		return result;
	}

	private void loadEpsAndPrices(String epsInputFile, Map<String, NavigableMap<Date, AssetEpsInfo>> epsStore,
			String pricesInputFile, Map<String, NavigableMap<Date, AssetPriceInfo>> priceStore,
			String sectorsInputFile, Map<String, Pair<String, String>> sectortorsStore) throws Exception {
		final BulkCsvLoader<AssetEpsInfo> epsLoader = epsLoader(epsStore);
		final BulkCsvLoader<AssetPriceInfo> priceLoader = priceLoader(priceStore);
		final BulkCsvLoader<Pair<String, Pair<String, String>>> sectorsLoader = sectorLoader(sectortorsStore);

		say("Loading EPS data from '{}' ...", epsInputFile);
		epsLoader.load(epsInputFile);

		say("Loading prices data from '{}' ...", pricesInputFile);
		priceLoader.load(pricesInputFile);

		say("Loading sector data '{}' ...", sectorsInputFile);
		sectorsLoader.load(sectorsInputFile);
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

	private static BulkCsvLoader<Pair<String, Pair<String, String>>> sectorLoader(Map<String, Pair<String, String>> sectorStore) {
		return new BulkCsvLoader<>(3) {

			@Override
			protected List<String> saveResults(List<Pair<String, Pair<String, String>>> dataToAdd) throws Exception {
				dataToAdd.forEach(entry -> {
					sectorStore.put(entry.getKey(), entry.getValue());
				});
				return Collections.emptyList();
			}

			@Override
			protected Pair<String, Pair<String, String>> toEntity(String assetName, String[] line) {
				return new Pair<>(assetName,
						new Pair<>(line[1].trim(), line[2].trim()));
			}
		};
	}
}
