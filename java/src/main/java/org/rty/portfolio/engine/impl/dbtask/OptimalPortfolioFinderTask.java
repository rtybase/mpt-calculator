package org.rty.portfolio.engine.impl.dbtask;

import java.io.FileReader;
import java.io.FileWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicInteger;

import org.rty.portfolio.core.PortfolioStatistics;
import org.rty.portfolio.db.DbManager;
import org.rty.portfolio.engine.AbstractDbTask;

import com.google.common.hash.Hashing;

import au.com.bytecode.opencsv.CSVReader;

public class OptimalPortfolioFinderTask extends AbstractDbTask {
	private static final int YEARS_BACK = 5;

	public OptimalPortfolioFinderTask(DbManager dbManager) {
		super(dbManager);
	}

	@Override
	public void execute(Map<String, String> parameters) throws Exception {
		final String inputFile = getValidParameterValue(parameters, INPUT_FILE_PARAM);
		final String outFile = getValidParameterValue(parameters, OUTPUT_FILE_PARAM);

		final Map<Integer, String> assetMappings = loadAssets(inputFile);
		final Queue<List<Integer>> queue = new LinkedList<>();
		final Set<String> alreadyAddedHashes = new HashSet<>();

		queue.add(toOrderedList(assetMappings.keySet()));

		say("Prepare storage... ");
		Map<Integer, Map<String, Double>> storage = dbManager.getAllDailyRates(YEARS_BACK);
		say(DONE);

		say("Running calculations... ");
		final long start = System.currentTimeMillis();

		final AtomicInteger total = new AtomicInteger(0);
		double maxReturnSoFar = Double.MIN_VALUE;

		while (!queue.isEmpty()) {
			final PortfolioStatsCalculator calculator = new PortfolioStatsCalculator(null, storage, queue.poll());
			final PortfolioStatistics resultPortfolioStatistics = calculator.call();

			total.incrementAndGet();

			if (resultPortfolioStatistics.hasSufficientContent) {
				final Set<Integer> negativeWeightsIndexes = indexesWithNegativeWeights(resultPortfolioStatistics);

				if (negativeWeightsIndexes.isEmpty()) {
					if (maxReturnSoFar <= resultPortfolioStatistics.portflioOptimalResults.portfolioReturn) {
						maxReturnSoFar = resultPortfolioStatistics.portflioOptimalResults.portfolioReturn;
						saveResults(outFile, resultPortfolioStatistics);
					}

					for (int i = 0; i < resultPortfolioStatistics.assetIds.size(); i++) {
						updateAssetIdsAndAddToQueue(queue, alreadyAddedHashes, resultPortfolioStatistics, Set.of(i));
					}

				} else {
					updateAssetIdsAndAddToQueue(queue, alreadyAddedHashes, resultPortfolioStatistics,
							negativeWeightsIndexes);
				}
			} else {
				for (int i = 0; i < resultPortfolioStatistics.assetIds.size(); i++) {
					updateAssetIdsAndAddToQueue(queue, alreadyAddedHashes, resultPortfolioStatistics, Set.of(i));
				}
			}
		}

		long ex_time = System.currentTimeMillis() - start;
		say("{}. Execution time: {}ms,", DONE, ex_time);
		say("Total processed {}", total);
	}

	private static void updateAssetIdsAndAddToQueue(final Queue<List<Integer>> queue, Set<String> alreadyAddedHashes,
			final PortfolioStatistics result, final Set<Integer> negativeWeightsIndexes) {
		final List<Integer> updatedAssetIds = removeValuesAtIndexes(result.assetIds, negativeWeightsIndexes);

		if (updatedAssetIds.size() > 1) {
			final String hash = hashListContent(updatedAssetIds);

			if (!alreadyAddedHashes.contains(hash)) {
				queue.add(updatedAssetIds);
				alreadyAddedHashes.add(hash);
			}
		}
	}

	private static String hashListContent(final List<Integer> updatedAssetIds) {
		final StringBuilder sb = new StringBuilder();
		updatedAssetIds.forEach(i -> sb.append(i).append(":"));
		return Hashing.sha256().hashString(sb.toString(), StandardCharsets.UTF_8).toString();
	}

	private static <T> List<T> removeValuesAtIndexes(List<T> values, Set<Integer> indexesToRemove) {
		final List<T> result = new ArrayList<>(values.size());

		for (int i = 0; i < values.size(); i++) {
			if (!indexesToRemove.contains(i)) {
				result.add(values.get(i));
			}
		}

		return result;
	}

	private static Set<Integer> indexesWithNegativeWeights(PortfolioStatistics stats) {
		Set<Integer> result = new HashSet<>(stats.portflioOptimalResults.portfolioWeights.length);
		for (int i = 0; i < stats.portflioOptimalResults.portfolioWeights.length; i++) {
			if (stats.portflioOptimalResults.portfolioWeights[i] < 0.0D) {
				result.add(i);
			}
		}

		return result;
	}

	private static void saveResults(final String outFile, PortfolioStatistics listOfFutures) throws Exception {
		try (FileWriter fw = new FileWriter(outFile, true)) {
			fw.write(listOfFutures.toString());
			fw.write("\n");
			fw.flush();
		}
	}

	private Map<Integer, String> loadAssets(String inputFile) throws Exception {
		say("Load assets '{}' ... ", inputFile);

		Map<Integer, String> assets = new HashMap<>();
		final CSVReader reader = new CSVReader(new FileReader(inputFile));

		int total = 0;
		int failed = 0;

		String[] nextLine;
		while ((nextLine = reader.readNext()) != null) {
			if (nextLine.length >= 2) {
				total++;

				assets.put(Integer.parseInt(nextLine[0]), nextLine[1]);
			} else {
				failed++;
			}
		}

		reader.close();

		say("Total portfolio definitions loaded {}", total);
		say("Operations failed {}", failed);
		return assets;
	}

	private <T> List<T> toOrderedList(Collection<T> values) {
		return new ArrayList<>(new TreeSet<>(values));
	}
}