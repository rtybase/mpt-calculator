package org.rty.portfolio.engine.impl.dbtask;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.math3.stat.StatUtils;
import org.apache.commons.math3.stat.correlation.Covariance;
import org.rty.portfolio.core.PortflioStats;
import org.rty.portfolio.db.DbManager;
import org.rty.portfolio.engine.AbstractDbTask;
import org.rty.portfolio.math.Calculator;

public class Calculate2AssetsPortfolioStatsTask extends AbstractDbTask {
	private static final int MIN_COMMON_DATE = 5;

	public Calculate2AssetsPortfolioStatsTask(DbManager dbManager) {
		super(dbManager);
	}

	@Override
	public void execute(Map<String, String> params) throws Exception {
		say("Prepare storage... ");
		HashMap<Integer, HashMap<String, Double>> storage = dbManager.getAllDailyRates();
		say(DONE);
		say("Prepare indexes... ");
		int[] indexes = getIndexes(storage);
		say(DONE);

		say("Running calculations... ");
		long start = System.currentTimeMillis();

		int total = 0;
		int totalFail = 0;

		dbManager.setAutoCommit(false);
		for (int i = 0; i < indexes.length; ++i) {
			HashMap<String, Double> rates1 = storage.get(indexes[i]);
			for (int j = i + 1; j < indexes.length; ++j) {
				HashMap<String, Double> rates2 = storage.get(indexes[j]);

				HashSet<String> dates = computeCommonDates(rates1, rates2);

				if (hasSufficientCommonDates(dates)) {
					// get the rates for the common dates
					double[] values1 = getValues(dates, rates1);
					double[] values2 = getValues(dates, rates2);

					double avg_r1 = StatUtils.mean(values1);
					double avg_r2 = StatUtils.mean(values2);

					double variance1 = StatUtils.populationVariance(values1);
					double variance2 = StatUtils.populationVariance(values2);

					double covariance = new Covariance().covariance(values1, values2, false);
					double correlation = Calculator.calculateCorrelation(covariance, variance1, variance2);

					double[][] covMatrix = new double[][] { { variance1, covariance }, { covariance, variance2 } };
					double[] rates = new double[] { avg_r1, avg_r2 };
					PortflioStats portStats = Calculator.calculateWeights(rates, covMatrix);

					boolean res = saveResults(indexes[i], indexes[j], covariance, correlation, portStats);
					if (!res) {
						say(indexes[i]
								+ "(" + values1.length + "),"
								+ indexes[j] + "(" + values2.length + "),r1=" + avg_r1
								+ ",r2=" + avg_r2
								+ ",var1=" + variance1
								+ ",var2=" + variance2
								+ ",cov=" + covariance
								+ ",cor=" + correlation
								+ ",w1=" + portStats.getPortfolioWeights()[0]
								+ ",w2=" + portStats.getPortfolioWeights()[1]
								+ ",PortR=" + portStats.getPortfolioReturn()
								+ ",PortVar=" + portStats.getPorfolioVariance());
						++totalFail;
					}
				} else {
					say(indexes[i] + ":" + indexes[j] + " - have insufficient common dates.");
					++totalFail;
				}

				++total;
			}
			dbManager.commit();
		}
		dbManager.commit();
		dbManager.setAutoCommit(true);

		long ex_time = System.currentTimeMillis() - start;
		say(DONE + ". Execution time: " + ex_time + "ms.");
		say("Total processed " + total);
		say("Failed " + totalFail);

	}

	private boolean hasSufficientCommonDates(HashSet<String> dates) {
		return dates.size() >= MIN_COMMON_DATE;
	}

	private HashSet<String> computeCommonDates(HashMap<String, Double> rates1, HashMap<String, Double> rates2) {
		HashSet<String> dates1 = new HashSet<String>(rates1.keySet());
		HashSet<String> dates2 = new HashSet<String>(rates2.keySet());
		dates1.retainAll(dates2);
		return dates1;
	}

	private boolean saveResults(int asset1, int asset2, double covariance, double correlation, PortflioStats portStats)
			throws Exception {
		boolean res = false;
		try {
			res = dbManager.addNew2AssetsPortfolioInfo(
					asset1,
					asset2,
					covariance,
					correlation,
					portStats.getPortfolioWeights()[0],
					portStats.getPortfolioWeights()[1],
					portStats.getPortfolioReturn(),
					portStats.getPorfolioVariance());
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return res;
	}

	public int[] getIndexes(HashMap<Integer, HashMap<String, Double>> storage) {
		int[] indexes = new int[storage.size()];
		int i = 0;
		Set<Integer> rows = storage.keySet();
		for (Integer row : rows) {
			indexes[i++] = row;
		}
		return indexes;
	}

	private double[] getValues(HashSet<String> dateSet, HashMap<String, Double> rates) {
		double[] ret = new double[dateSet.size()];
		int i = 0;
		for (String s : dateSet) {
			ret[i++] = rates.get(s);
		}
		return ret;
	}
}
