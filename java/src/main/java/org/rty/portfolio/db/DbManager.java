package org.rty.portfolio.db;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;

public class DbManager {
	private static final Logger LOGGER = LoggerFactory.getLogger(DbManager.class.getSimpleName());
	private static final boolean NOT_FREE = false;
	private static final boolean FREE = true;

	private final Map<Integer, DbConnectionHolder> allConnections;
	private final BlockingQueue<DbConnection> freeConnections;

	private final Object syncObject = new Object();

	private final AtomicInteger currentConnection = new AtomicInteger(0);
	private final String connectionString;
	private final int maximumConnections;

	public DbManager(String connectionString, int maximumConnections) {
		Preconditions.checkArgument(maximumConnections > 0, "maximumConnections must be greater than 0!");
		LOGGER.info("Maximum DB connections is set to '{}'", maximumConnections);

		this.maximumConnections = maximumConnections;
		this.connectionString = Objects.requireNonNull(connectionString, "connectionString must not be null!");

		this.freeConnections = new LinkedBlockingQueue<>(maximumConnections);
		this.allConnections = new HashMap<>();
	}

	String getConnectionString() {
		return connectionString;
	}

	void free(DbConnection connection) throws Exception {
		final int internalId = connection.getInternalId();
		final DbConnectionHolder dbConnectionHolder = allConnections.get(internalId);

		if (dbConnectionHolder != null) {
			if (dbConnectionHolder.freeFlag().compareAndSet(NOT_FREE, FREE)) {
				freeConnections.put(connection);
			}
		} else {
			LOGGER.warn("DB connection with internalId '{}' was never registered before!", internalId);
		}
	}

	public DbConnection get() throws Exception {
		if (currentConnection.get() == maximumConnections) {
			return getFreeConnection();
		}

		synchronized (syncObject) {
			final int internalId = currentConnection.get();

			if (internalId < maximumConnections) {
				currentConnection.incrementAndGet();

				return addNewConnection(internalId);
			}
		}

		return getFreeConnection();
	}

	public void close() throws Exception {
		synchronized (syncObject) {
			for (Map.Entry<Integer, DbConnectionHolder> entry : allConnections.entrySet()) {
				entry.getValue().dbConnection().shutdown();
			}
		}
	}

	private DbConnection addNewConnection(int internalId) throws Exception {
		final DbConnection connection = new DbConnection(this, internalId);

		allConnections.put(internalId, new DbConnectionHolder(connection, new AtomicBoolean(NOT_FREE)));

		LOGGER.info("New DB connection created. Total so far '{}'", allConnections.size());

		return connection;
	}

	private DbConnection getFreeConnection() throws Exception {
		final DbConnection connection = freeConnections.take(); // will block waiting for free entry
		allConnections.get(connection.getInternalId()).freeFlag().set(NOT_FREE);

		return connection;
	}

	private record DbConnectionHolder(DbConnection dbConnection, AtomicBoolean freeFlag) {
	}
}
