package org.rty.portfolio.db;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class DbConnectionTest {
	private static final String COMMON_INSERT_PART = "INSERT INTO tbl_finances_quarter (vchr_symbol,"
			+ "dtm_date,dbl_total_current_assets,dbl_total_current_liabilities,"
			+ "dbl_total_assets,dbl_total_liabilities,dbl_total_equity,"
			+ "dbl_net_cash_flow_operating,dbl_capital_expenditures,dbl_share_issued) "
			+ "VALUES (?,?,?,?,?,?,?,?,?,?) ON DUPLICATE KEY UPDATE ";

	private static final String COMMON_NO_OVERRIDE_PART = "dbl_total_current_assets=COALESCE(dbl_total_current_assets, VALUES(dbl_total_current_assets)), "
			+ "dbl_total_current_liabilities=COALESCE(dbl_total_current_liabilities, VALUES(dbl_total_current_liabilities)), "
			+ "dbl_total_assets=COALESCE(dbl_total_assets, VALUES(dbl_total_assets)), "
			+ "dbl_total_liabilities=COALESCE(dbl_total_liabilities, VALUES(dbl_total_liabilities)), "
			+ "dbl_total_equity=COALESCE(dbl_total_equity, VALUES(dbl_total_equity)), "
			+ "dbl_net_cash_flow_operating=COALESCE(dbl_net_cash_flow_operating, VALUES(dbl_net_cash_flow_operating)), "
			+ "dbl_capital_expenditures=COALESCE(dbl_capital_expenditures, VALUES(dbl_capital_expenditures)), ";

	private static final String OVERRIDE_SHARES_PART = "dbl_share_issued=COALESCE(VALUES(dbl_share_issued), dbl_share_issued)";

	@Test
	void testGenerateSqlForBulkFinancialInfoWithoutOverride() {
		final String expectedSql = COMMON_INSERT_PART
				+ COMMON_NO_OVERRIDE_PART
				+ "dbl_share_issued=COALESCE(dbl_share_issued, VALUES(dbl_share_issued))";

		String sql = DbConnection.generateSqlForBulkFinancialInfo(false, false);
		assertEquals(expectedSql, sql);

		sql = DbConnection.generateSqlForBulkFinancialInfo(false, true);
		assertEquals(expectedSql, sql);
	}

	@Test
	void testGenerateSqlForBulkFinancialInfoWithOverride() {
		final String sql = DbConnection.generateSqlForBulkFinancialInfo(true, false);

		assertEquals(COMMON_INSERT_PART
				+ "dbl_total_current_assets=COALESCE(VALUES(dbl_total_current_assets), dbl_total_current_assets), "
				+ "dbl_total_current_liabilities=COALESCE(VALUES(dbl_total_current_liabilities), dbl_total_current_liabilities), "
				+ "dbl_total_assets=COALESCE(VALUES(dbl_total_assets), dbl_total_assets), "
				+ "dbl_total_liabilities=COALESCE(VALUES(dbl_total_liabilities), dbl_total_liabilities), "
				+ "dbl_total_equity=COALESCE(VALUES(dbl_total_equity), dbl_total_equity), "
				+ "dbl_net_cash_flow_operating=COALESCE(VALUES(dbl_net_cash_flow_operating), dbl_net_cash_flow_operating), "
				+ "dbl_capital_expenditures=COALESCE(VALUES(dbl_capital_expenditures), dbl_capital_expenditures), "
				+ OVERRIDE_SHARES_PART, sql);
	}

	@Test
	void testGenerateSqlForBulkFinancialInfoWithSharesIssuedOverrideOnly() {
		final String sql = DbConnection.generateSqlForBulkFinancialInfo(true, true);

		assertEquals(COMMON_INSERT_PART
				+ COMMON_NO_OVERRIDE_PART
				+ OVERRIDE_SHARES_PART, sql);
	}
}
