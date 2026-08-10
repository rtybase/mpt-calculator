package org.rty.portfolio.db;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;

public class DbManager {
	private static final Logger LOGGER = LoggerFactory.getLogger(DbManager.class.getSimpleName());

	private final List<DbConnection> allConnections;
	private final BlockingQueue<DbConnection> availableConnections;

	private final Object syncObject = new Object();

	private final AtomicInteger currentConnection = new AtomicInteger(0);
	private final String connectionString;
	private final int maximumConnections;

	public DbManager(String connectionString, int maximumConnections) {
		Preconditions.checkArgument(maximumConnections > 0, "maximumConnections must be greater than 0!");
		LOGGER.info("Maximum DB connections is set to '{}'", maximumConnections);

		this.maximumConnections = maximumConnections;
		this.connectionString = Objects.requireNonNull(connectionString, "connectionString must not be null!");
		this.availableConnections = new LinkedBlockingQueue<>(maximumConnections);
		this.allConnections = new ArrayList<>(maximumConnections);
	}

	String getConnectionString() {
		return connectionString;
	}

	void free(DbConnection connection) throws Exception {
		availableConnections.put(connection);
	}

	public DbConnection get() throws Exception {
		if (currentConnection.get() == maximumConnections) {
			return availableConnections.take();
		}

		synchronized (syncObject) {
			if (currentConnection.get() < maximumConnections) {
				final DbConnection connection = new DbConnection(this);

				allConnections.add(connection);
				currentConnection.incrementAndGet();

				LOGGER.info("New DB connection created. Total so far '{}'", allConnections.size());

				return connection;
			}
		}

		return availableConnections.take();
	}

	public void close() throws Exception {
		synchronized (syncObject) {
			for (DbConnection connection : allConnections) {
				connection.shutdown();
			}
		}
	}
}
