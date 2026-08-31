import util.db
import util.dates

def save_to_db(conn, predictor, predictand, last_common_date, forecast_date, correlation, min_rate_forecast, max_rate_forecast):
    with conn.cursor() as insert_cursor:
        insert_cursor.execute("""INSERT into  tbl_shift_correlations_history 
		(fk_predictor_assetID, fk_predictand_assetID,
			dtm_last_common_date, dtm_forecast_date,
			dbl_correlation, dbl_min_rate_forecast, dbl_max_rate_forecast) 
		values (%s,%s,%s,%s,%s,%s,%s)
			ON DUPLICATE KEY UPDATE
			dtm_forecast_date=VALUES(dtm_forecast_date),
			dbl_correlation=VALUES(dbl_correlation),
			dbl_min_rate_forecast=VALUES(dbl_min_rate_forecast),
			dbl_max_rate_forecast=VALUES(dbl_max_rate_forecast)""",\
            (predictor, predictand,\
                last_common_date, forecast_date,\
                correlation, min_rate_forecast,\
                max_rate_forecast))


print("Starting ...", flush=True)

select_query = """SELECT fk_predictor_assetID, fk_predictand_assetID,
	dtm_last_common_date, dbl_correlation,
	dbl_min_rate_forecast, dbl_max_rate_forecast
	FROM  tbl_shift_correlations
	WHERE int_shift=1 
	AND dbl_min_rate_forecast IS NOT NULL
	AND dtm_last_common_date IS NOT NULL"""

count = 0
select_conn = util.db.db_conection
insert_conn = util.db.connect()

with select_conn.cursor() as select_cursor:
    select_cursor.execute(select_query)
    for row in select_cursor:
        save_to_db(insert_conn, row[0], row[1],\
            row[2], util.dates.next_working_date(row[2]),\
            row[3], row[4], row[5])

        count += 1

insert_conn.commit()
util.db.db_conection.commit()

print(f"Copied {count} entries!", flush=True)