package org.rty.portfolio.engine.impl.dbtask;

import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicInteger;

import org.rty.portfolio.core.utils.ConcurrentTaskExecutorWithBatching;
import org.rty.portfolio.core.utils.DatesAndSetUtil;
import org.rty.portfolio.db.DbManager;

public abstract class Generic2AssetsCalculateTask<T> extends GenericCalculateTask<T> {
	private final int yearsBack;

	public Generic2AssetsCalculateTask(DbManager dbManager, int yearsBack) {
		super(dbManager);

		this.yearsBack = yearsBack;
	}

	@Override
	public final void execute(Map<String, String> params) throws Exception {
		say("Prepare storage... ");
		Map<Integer, Map<String, Double>> storage = dbManager.getAllDailyRates(yearsBack);
		say(DONE);
		say("Prepare indexes... ");
		int[] indexes = DatesAndSetUtil.getIndexesFrom(storage);
		say(DONE);

		say("Running calculations... ");
		final long start = System.currentTimeMillis();

		final AtomicInteger total = new AtomicInteger(0);
		final AtomicInteger totalFail = new AtomicInteger(0);

		dbManager.setAutoCommit(false);
		final ConcurrentTaskExecutorWithBatching<T> taskExecutor = createExecutor(totalFail);

		for (int i = 0; i < indexes.length; ++i) {
			for (int j = i + 1; j < indexes.length; ++j) {
				taskExecutor.addTask(calculatorFrom(storage, indexes[i], indexes[j]));

				total.incrementAndGet();
			}
		}

		taskExecutor.close();
		dbManager.commit();
		dbManager.setAutoCommit(true);

		long ex_time = System.currentTimeMillis() - start;
		say("{}. Execution time: {}ms,", DONE, ex_time);
		say("Total processed {}", total);
		say("Failed {}", totalFail);
	}

	protected abstract Callable<T> calculatorFrom(Map<Integer, Map<String, Double>> storage, int asset1Id,
			int asset2Id);
}
