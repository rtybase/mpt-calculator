import sys
import util.db
import util.flow


EMPTY_FIN_DATA = {
    "WHAT": "EMPTY-FIN-DATA",
    "SQL": """delete FROM tbl_finances_quarter WHERE 
	dbl_total_current_assets is null and
	dbl_total_current_liabilities is null and
	dbl_total_assets is null and
	dbl_total_liabilities is null and
	dbl_total_equity is null and
	dbl_net_cash_flow_operating is null and
	dbl_capital_expenditures is null and
	dbl_share_issued is null"""
}

SHIFT_CORR_DATA = {
    "WHAT": "OUTDATED-SHIFT-CORR-DATA",
    "SQL": """delete FROM tbl_shift_correlations WHERE 
	dtm_last_update_date <= (NOW() - INTERVAL 1 DAY)"""
}

OPTIMISE = {
    "WHAT": "OPTIMISE-BIG-TABLES",
    "SQL": """OPTIMIZE TABLE
	tbl_prices, tbl_shift_correlations, tbl_correlations"""
}

SCOPES = {
    "ALL": [EMPTY_FIN_DATA, SHIFT_CORR_DATA],
    "FIN-ONLY": [EMPTY_FIN_DATA],
    "OPT-ONLY": [OPTIMISE]
}

def execute_sql(sql):
    affected_rows = 0
    with util.db.db_conection.cursor(buffered=True) as cursor:
        cursor.execute(sql)
        affected_rows = cursor.rowcount
    util.db.db_conection.commit()
    return affected_rows

if len(sys.argv) > 1:
    scope = sys.argv[1]

    if scope not in SCOPES:
        util.flow.error("Scope not found!")

    sql_lst = SCOPES[scope]

    for sql in sql_lst:
        print("%s start" % (sql["WHAT"],), flush=True)

        affected_rows = execute_sql(sql["SQL"])

        print("%s done, affected rows=%s" % (sql["WHAT"], affected_rows), flush=True)

    util.db.db_conection.close()

else:
    util.flow.error("Specify the clean scope!")
