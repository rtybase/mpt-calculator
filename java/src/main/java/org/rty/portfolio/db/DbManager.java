package org.rty.portfolio.db;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.rty.portfolio.engine.impl.dbtask.AssetsStatsCalculationTask.AssetsStatsCalculationResult;

public class DbManager {
	private final Connection connection;

	public DbManager(Connection connection) {
		this.connection = Objects.requireNonNull(connection, "connection must not be null.");
	}

	public void setAutoCommit(boolean autoCommitFlag) throws Exception {
		connection.setAutoCommit(autoCommitFlag);
	}

	public void commit() throws Exception {
		connection.commit();
	}

	public void close() throws Exception {
		connection.close();
	}

	/**
	 * Calculate averages, variance for each individual asset in DB.
	 * 
	 */
	public boolean applyAverages() throws Exception {
		boolean ret = false;

		try (CallableStatement cStmt = connection.prepareCall("{call usp_applyavg(?)}")) {
			cStmt.registerOutParameter(1, Types.INTEGER);
			cStmt.execute();

			int result = cStmt.getInt(1);
			if (result >= 0) {
				ret = true;
			}
		}

		return ret;
	}

	/**
	 * Adds a new price record to DB.
	 * 
	 */
	public boolean addNewPrice(String assetName, double price, double change, double rate, Date date) throws Exception {
		boolean ret = false;

		try (CallableStatement cStmt = connection.prepareCall("{call usp_addPrice(?,?,?,?,?,?,?)}")) {

			cStmt.setString(1, assetName);
			cStmt.setDouble(2, price);
			cStmt.setDouble(3, change);
			cStmt.setDouble(4, rate);
			cStmt.setDate(5, new java.sql.Date(date.getTime()));
			cStmt.setTime(6, new java.sql.Time(date.getTime()));
			cStmt.registerOutParameter(7, Types.INTEGER);

			cStmt.execute();
			int result = cStmt.getInt(7);

			if (result >= 0) {
				ret = true;
			}
		}

		return ret;
	}

	/**
	 * Adds new 2 assets portfolio record with associated statistics.
	 * 
	 */
	public int[] addNew2AssetsPortfolioInfo(List<AssetsStatsCalculationResult> results) throws Exception {
		try (PreparedStatement pStmt = connection.prepareStatement(
				"INSERT INTO tbl_correlations (fk_asset1ID, fk_asset2ID, dbl_covariance, dbl_correlation, dbl_weight1, dbl_weight2, dbl_portret, dbl_portvar) VALUES (?, ?, ?, ?, ?, ?, ?, ?)");) {

			for (AssetsStatsCalculationResult result : results) {
				pStmt.setInt(1, result.assetIds.get(0));
				pStmt.setInt(2, result.assetIds.get(1));
				pStmt.setDouble(3, result.covarianceMatrix[0][1]);
				pStmt.setDouble(4, result.correlationMatrix[0][1]);
				pStmt.setDouble(5, result.portflioStats.getPortfolioWeights()[0]);
				pStmt.setDouble(6, result.portflioStats.getPortfolioWeights()[1]);
				pStmt.setDouble(7, result.portflioStats.getPortfolioReturn());
				pStmt.setDouble(8, result.portflioStats.getPorfolioVariance());

				pStmt.addBatch();
			}

			return pStmt.executeBatch();
		}
	}

	/**
	 * Returns all the daily rates for all the assets. Key is the assetId. Value is
	 * a map where key is the date and value is rate at that date.
	 * 
	 */
	public Map<Integer, Map<String, Double>> getAllDailyRates() throws Exception {
		final Map<Integer, HashMap<String, Double>> storage = new HashMap<>();

		try (Statement stmt = connection.createStatement();
				ResultSet rs = stmt.executeQuery(
						"select fk_assetID, dtm_date, dbl_return from tbl_prices order by fk_assetID, dtm_date desc, dtm_time desc")) {

			while (rs.next()) {
				addResultToStorage(storage, rs);
			}
		}

		return Collections.unmodifiableMap(storage);
	}

	private final void addResultToStorage(final Map<Integer, HashMap<String, Double>> storage, ResultSet rs)
			throws SQLException {
		final Integer id = rs.getInt(1);
		final String date = rs.getString(2);
		final Double rate = rs.getDouble(3);

		final Map<String, Double> row = storage.computeIfAbsent(id, key -> new HashMap<>());
		row.put(date, rate);
	}
}
