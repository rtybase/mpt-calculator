package org.rty.portfolio.db;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.Date;
import java.util.HashMap;
import java.util.Objects;

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
		CallableStatement cStmt = null;

		try {
			cStmt = connection.prepareCall("{call usp_applyavg(?)}");
			cStmt.registerOutParameter(1, Types.INTEGER);
			cStmt.execute();
			int result = cStmt.getInt(1);
			if (result >= 0) {
				ret = true;
			}
		} finally {
			if (cStmt != null) {
				try {
					cStmt.close();
				} catch (SQLException e) {
				}
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
		CallableStatement cStmt = null;

		try {
			cStmt = connection.prepareCall("{call usp_addPrice(?,?,?,?,?,?,?)}");

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
		} finally {
			if (cStmt != null) {
				try {
					cStmt.close();
				} catch (SQLException e) {
				}
			}
		}
		return ret;
	}

	/**
	 * Adds new 2 assets portfolio record with associated statistics.
	 * 
	 */
	public boolean addNew2AssetsPortfolioInfo(int asset1, int asset2, double covariance, double correlation,
			double weight1, double weight2, double portRet, double portVar) throws Exception {
		boolean ret = false;
		PreparedStatement pStmt = null;

		try {
			pStmt = connection.prepareStatement(
					"INSERT INTO tbl_correlations (fk_asset1ID, fk_asset2ID, dbl_covariance, dbl_correlation, dbl_weight1, dbl_weight2, dbl_portret, dbl_portvar) VALUES (?, ?, ?, ?, ?, ?, ?, ?)");

			pStmt.setInt(1, asset1);
			pStmt.setInt(2, asset2);
			pStmt.setDouble(3, covariance);
			pStmt.setDouble(4, correlation);
			pStmt.setDouble(5, weight1);
			pStmt.setDouble(6, weight2);
			pStmt.setDouble(7, portRet);
			pStmt.setDouble(8, portVar);

			pStmt.executeUpdate();
			ret = true;
		} finally {

			if (pStmt != null) {
				try {
					pStmt.close();
				} catch (SQLException e) {
				}
			}
		}
		return ret;
	}

	/**
	 * Returns all the daily rates for all the assets. Key is the assetId. Value is
	 * a map where key is the date and value is rate at that date.
	 * 
	 */
	public HashMap<Integer, HashMap<String, Double>> getAllDailyRates() throws Exception {
		HashMap<Integer, HashMap<String, Double>> storage = new HashMap<Integer, HashMap<String, Double>>();

		Statement stmt = connection.createStatement();
		ResultSet rs = stmt.executeQuery(
				"select fk_assetID, dtm_date, dbl_return from tbl_prices order by fk_assetID, dtm_date desc, dtm_time desc");
		while (rs.next()) {
			Integer id = rs.getInt(1);
			String date = rs.getString(2);
			Double rate = rs.getDouble(3);

			HashMap<String, Double> row = storage.get(id);
			if (row == null) {
				row = new HashMap<String, Double>();
				storage.put(id, row);
			}
			row.put(date, rate);
		}
		rs.close();
		stmt.close();
		return storage;
	}
}
