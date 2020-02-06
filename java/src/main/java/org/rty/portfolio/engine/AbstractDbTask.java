package org.rty.portfolio.engine;

import java.util.Objects;

import org.rty.portfolio.db.DbManager;

public abstract class AbstractDbTask extends AbstractTask {
	protected final DbManager dbManager;

	public AbstractDbTask(DbManager dbManager) {
		super();
		this.dbManager = Objects.requireNonNull(dbManager, "dbManager must not be null.");
	}
}
