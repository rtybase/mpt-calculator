package org.rty.portfolio.engine.impl.dbtask;

import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

import org.rty.portfolio.core.AssetsStatistics;
import org.rty.portfolio.core.utils.ConcurrentTaskExecutorWithBatching;
import org.rty.portfolio.db.DbManager;
import org.rty.portfolio.engine.AbstractDbTask;

import au.com.bytecode.opencsv.CSVReader;

public class CalculateMultiAssetsPortfolioStatsTask extends AbstractDbTask {
	public CalculateMultiAssetsPortfolioStatsTask(DbManager dbManager) {
		super(dbManager);
	}

	@Override
	public void execute(Map<String, String> parameters) throws Exception {
		final String inputFile = getValidParameterValue(parameters, INPUT_FILE_PARAM);
		final String outFile = getValidParameterValue(parameters, OUTPUT_FILE_PARAM);

		final List<List<Integer>> portfolios = loadPortfolioDefinitions(inputFile);

		say("Prepare storage... ");
		Map<Integer, Map<String, Double>> storage = dbManager.getAllDailyRates();
		say(DONE);

		say("Running calculations... ");
		final long start = System.currentTimeMillis();

		final AtomicInteger total = new AtomicInteger(0);

		final ConcurrentTaskExecutorWithBatching<AssetsStatistics> taskExecutor = new ConcurrentTaskExecutorWithBatching<>(
				8, 4096, 3072, listOfFutures -> {
					try (FileWriter fw = new FileWriter(outFile, true)) {
						for (Future<AssetsStatistics> futureResult : listOfFutures) {
							final AssetsStatistics calculationResult = futureResult.get();
							fw.write(calculationResult.toString());
							fw.write("\n");
						}
						fw.flush();
					}
				});

		for (List<Integer> portfolio : portfolios) {
			taskExecutor.addTask(new AssetsStatsCalculator(storage, portfolio));
			total.incrementAndGet();
		}

		taskExecutor.close();

		long ex_time = System.currentTimeMillis() - start;
		say("{}. Execution time: {}ms,", DONE, ex_time);
		say("Total processed {}", total);
	}

	private List<List<Integer>> loadPortfolioDefinitions(String inputFile) throws Exception {
		say("Load portfolio definitions from '{}' ... ", inputFile);

		List<List<Integer>> result = new ArrayList<>();
		final CSVReader reader = new CSVReader(new FileReader(inputFile));

		int total = 0;
		int failed = 0;

		String[] nextLine;
		while ((nextLine = reader.readNext()) != null) {
			if (nextLine.length > 0) {
				total++;

				List<Integer> portfolio = stringsToInts(nextLine);
				if (portfolio.isEmpty()) {
					failed++;
				} else {
					result.add(portfolio);
				}
			}
		}

		reader.close();

		say("Total portfolio definitions loaded {}", total);
		say("Operations failed {}", failed);
		return result;
	}

	private List<Integer> stringsToInts(String[] nextLine) {
		try {
			List<Integer> portfolio = new ArrayList<>(nextLine.length);

			for (String s : nextLine) {
				portfolio.add(Integer.parseInt(s));
			}

			return portfolio;
		} catch (Exception ex) {
			say(ex.toString());
		}

		return Collections.emptyList();
	}
}
