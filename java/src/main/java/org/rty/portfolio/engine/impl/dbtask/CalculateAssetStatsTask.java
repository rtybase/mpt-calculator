package org.rty.portfolio.engine.impl.dbtask;

import java.util.Map;

import org.rty.portfolio.db.DbConnection;
import org.rty.portfolio.db.DbManager;
import org.rty.portfolio.engine.AbstractDbTask;

public class CalculateAssetStatsTask extends AbstractDbTask {

	public CalculateAssetStatsTask(DbManager dbManager) {
		super(dbManager);
	}

	@Override
	public void execute(Map<String, String> params) throws Exception {
		say("Executing...");

		final DbConnection connection = dbManager.get();

		if (connection.applyAverages()) {
			say(DONE);
		} else {
			say("ERROR");
		}

		connection.close();
	}
}
