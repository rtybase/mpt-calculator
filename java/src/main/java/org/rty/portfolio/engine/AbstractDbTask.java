package org.rty.portfolio.engine;

import java.util.Map;
import java.util.Objects;

import org.rty.portfolio.db.DbConnection;
import org.rty.portfolio.db.DbManager;

public abstract class AbstractDbTask extends AbstractTask {
	protected final DbManager dbManager;

	public AbstractDbTask(DbManager dbManager) {
		super();
		this.dbManager = Objects.requireNonNull(dbManager, "dbManager must not be null.");
	}

	protected Map<Integer, Map<String, Double>> loadAllDailyRates(int yearsBack) throws Exception {
		say("Prepare storage... ");

		final DbConnection connection = dbManager.get();
		final Map<Integer, Map<String, Double>> result = connection.getAllDailyRates(yearsBack);
		connection.close();

		say(DONE);

		return result;
	}
}
