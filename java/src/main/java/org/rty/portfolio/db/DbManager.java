package org.rty.portfolio.db;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.rty.portfolio.core.AssetDividendInfo;
import org.rty.portfolio.core.AssetPriceInfo;
import org.rty.portfolio.core.AssetsCorrelationInfo;
import org.rty.portfolio.core.PortflioOptimalResults;
import org.rty.portfolio.core.PortfolioStatistics;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import com.google.common.base.Preconditions;

public class DbManager {
	private final Connection connection;
	private final LoadingCache<String, Integer> cache;

	public DbManager(Connection connection) {
		this.connection = Objects.requireNonNull(connection, "connection must not be null.");

		this.cache = Caffeine.newBuilder()
				  .maximumSize(1000)
				  .build(this::queryDbForAssetIdFromName);
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
	 * Attempts to resolve asset name to ID.
	 * 
	 */
	public Integer resolveAssetNameToId(String assetName) throws Exception {
		return cache.get(assetName);
	}

	/**
	 * Adds price records to DB.
	 * 
	 */
	public 	List<String> addBulkPrices(List<AssetPriceInfo> prices) throws Exception {
		final List<String> failedResults = new ArrayList<>(prices.size());
		final List<String> possiblyGoodResults = new ArrayList<>(prices.size());

		try (PreparedStatement pStmt = connection.prepareStatement(
				"INSERT INTO tbl_prices (fk_assetID, dbl_price, dbl_change,dbl_return, dtm_date, dtm_time)"
						+ " VALUES (?,?,?,?,?,?)"
						+ " ON DUPLICATE KEY UPDATE"
						+ "	 dbl_price=VALUES(dbl_price),"
						+ "	 dbl_change=VALUES(dbl_change),"
						+ "	 dbl_return=VALUES(dbl_return)")) {

			for (AssetPriceInfo price : prices) {
				Integer assetId = resolveAssetNameToId(price.assetName);

				if (assetId < 0) {
					failedResults.add(price.assetName);
				} else {
					possiblyGoodResults.add(price.assetName);

					pStmt.setInt(1, assetId);
					pStmt.setDouble(2, price.price);
					pStmt.setDouble(3, price.change);
					pStmt.setDouble(4, price.rate);
					pStmt.setDate(5, new java.sql.Date(price.date.getTime()));
					pStmt.setTime(6, new java.sql.Time(price.date.getTime()));

					pStmt.addBatch();
				}
			}

			executeAndProcessResult(pStmt, possiblyGoodResults, failedResults);
		}

		return failedResults;
	}

	/**
	 * Adds price records to DB.
	 * 
	 */
	public 	List<String> addBulkDividends(List<AssetDividendInfo> dividends) throws Exception {
		final List<String> failedResults = new ArrayList<>(dividends.size());
		final List<String> possiblyGoodResults = new ArrayList<>(dividends.size());

		try (PreparedStatement pStmt = connection.prepareStatement(
				"INSERT INTO tbl_dividends (fk_assetID, dbl_pay, dtm_date)"
						+ " VALUES (?,?,?)"
						+ " ON DUPLICATE KEY UPDATE"
						+ "	 dbl_pay=VALUES(dbl_pay)")) {

			for (AssetDividendInfo dividend : dividends) {
				Integer assetId = resolveAssetNameToId(dividend.assetName);

				if (assetId < 0) {
					failedResults.add(dividend.assetName);
				} else {
					possiblyGoodResults.add(dividend.assetName);

					pStmt.setInt(1, assetId);
					pStmt.setDouble(2, dividend.pay);
					pStmt.setDate(3, new java.sql.Date(dividend.date.getTime()));

					pStmt.addBatch();
				}
			}

			executeAndProcessResult(pStmt, possiblyGoodResults, failedResults);
		}

		return failedResults;
	}

	/**
	 * Adds time shift correlation records to DB.
	 * 
	 */
	public 	int[] addBulkShiftCorrelations(List<AssetsCorrelationInfo> assetsShiftCorrelations) throws Exception {
		try (PreparedStatement pStmt = connection.prepareStatement(
				"INSERT INTO tbl_shift_correlations (fk_asset1ID, fk_asset2ID, int_shift, dbl_correlation, txt_json)"
						+ " VALUES (?,?,?,?,?)")) {

			for (AssetsCorrelationInfo assetsShiftCorrelation : assetsShiftCorrelations) {
				pStmt.setInt(1, assetsShiftCorrelation.asset1Id);
				pStmt.setInt(2, assetsShiftCorrelation.asset2Id);
				pStmt.setInt(3, assetsShiftCorrelation.bestShift);
				pStmt.setDouble(4, assetsShiftCorrelation.bestCorrelation);
				pStmt.setString(5, assetsShiftCorrelation.toString());

				pStmt.addBatch();
			}

			return pStmt.executeBatch();
		}
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
	public int[] addNew2AssetsPortfolioInfo(List<PortfolioStatistics> results) throws Exception {
		try (PreparedStatement pStmt = connection.prepareStatement(
				"INSERT INTO tbl_correlations (fk_asset1ID, fk_asset2ID, dbl_covariance, dbl_correlation, dbl_weight1, dbl_weight2, dbl_portret, dbl_portvar)"
						+ " VALUES (?, ?, ?, ?, ?, ?, ?, ?)")) {

			for (PortfolioStatistics result : results) {
				pStmt.setInt(1, result.assetIds.get(0));
				pStmt.setInt(2, result.assetIds.get(1));
				pStmt.setDouble(3, result.covarianceMatrix[0][1]);
				pStmt.setDouble(4, result.correlationMatrix[0][1]);
				pStmt.setDouble(5, result.portflioOptimalResults.portfolioWeights[0]);
				pStmt.setDouble(6, result.portflioOptimalResults.portfolioWeights[1]);
				pStmt.setDouble(7, result.portflioOptimalResults.portfolioReturn);
				pStmt.setDouble(8, result.portflioOptimalResults.porfolioVariance);

				pStmt.addBatch();
			}

			return pStmt.executeBatch();
		}
	}

	/**
	 * Returns all the daily rates for all the assets. Key is the assetId. Value is
	 * a map where key is the date and value is rate on that date.
	 */
	public Map<Integer, Map<String, Double>> getAllDailyRates(int yearsBack) throws Exception {
		Preconditions.checkArgument(yearsBack > 0, "yearsBack must be > 0!");

		final Map<Integer, HashMap<String, Double>> storage = new HashMap<>();

		try (PreparedStatement pStmt = connection.prepareStatement("select fk_assetID, dtm_date, dbl_return"
				+ " from tbl_prices"
				+ " where dtm_date between (NOW() - INTERVAL ? YEAR) and NOW()"
				+ " order by fk_assetID, dtm_date desc, dtm_time desc")) {
			pStmt.setInt(1, yearsBack);

			try (ResultSet rs = pStmt.executeQuery()) {

				while (rs.next()) {
					addResultToStorage(storage, rs);
				}
			}
		}

		return Collections.unmodifiableMap(storage);
	}

	/**
	 * Returns all custom portfolios. Key is the portfolioId. Value is portfolio
	 * composition, json array of int's.
	 */
	public Map<Integer, String> getAllCustomPortfolios() throws Exception {
		final Map<Integer, String> portfolios = new HashMap<>();

		try (Statement stmt = connection.createStatement();
				ResultSet rs = stmt.executeQuery(
						"select int_portfolioID, txt_json_composition from tbl_custom_portfolios"
						+ " order by int_portfolioID")) {
			while (rs.next()) {
				portfolios.put(rs.getInt(1), rs.getString(2));
			}

		}

		return Collections.unmodifiableMap(portfolios);
	}

	/**
	 * Adds optimal custom portfolio results to DB.
	 * 
	 */
	public int[] addBulkCustomPortfolioOptimalResults(List<PortflioOptimalResults> results) throws Exception {
		try (PreparedStatement pStmt = connection.prepareStatement(
				"INSERT INTO tbl_custom_portfolios_data (fk_portfolioID, dtm_date, txt_json_stats)"
						+ " VALUES (?, now(), ?)"
						+ " ON DUPLICATE KEY UPDATE"
						+ "	 txt_json_stats=VALUES(txt_json_stats)")) {

			for (PortflioOptimalResults result : results) {
				pStmt.setInt(1, result.portfolioId);
				pStmt.setString(2, result.toString());

				pStmt.addBatch();
			}

			return pStmt.executeBatch();
		}
	}

	private final void addResultToStorage(final Map<Integer, HashMap<String, Double>> storage, ResultSet rs)
			throws SQLException {
		final Integer id = rs.getInt(1);
		final String date = rs.getString(2);
		final Double rate = rs.getDouble(3);

		final Map<String, Double> row = storage.computeIfAbsent(id, key -> new HashMap<>());
		row.put(date, rate);
	}

	private Integer queryDbForAssetIdFromName(String assetName) throws Exception {
		try (PreparedStatement pStmt = connection
				.prepareStatement("SELECT int_assetID FROM tbl_assets WHERE UPPER(vchr_name)=UPPER(?)")) {
			pStmt.setString(1, assetName);

			try (ResultSet rs = pStmt.executeQuery()) {
				while (rs.next()) {
					return rs.getInt(1);
				}
			}
		}

		return -1;
	}

	private static void executeAndProcessResult(PreparedStatement pStmt, final List<String> possiblyGoodResults,
			final List<String> failedResults) throws Exception {
		if (!possiblyGoodResults.isEmpty()) {
			final int[] executionResults = pStmt.executeBatch();

			for (int result : executionResults) {
				if (result == Statement.EXECUTE_FAILED) {
					failedResults.add(possiblyGoodResults.get(result));
				}
			}
		}
	}
}
