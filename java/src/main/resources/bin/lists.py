import sys
import util.db
import util.flow

LISTS = {
    "L": """SELECT vchr_price_symbol, vchr_name FROM tbl_assets 
		WHERE vchr_price_symbol like "%.L" 
		OR vchr_price_symbol like "%.IR" 
		OR vchr_price_symbol like "%.RO"
		OR vchr_price_symbol in ('GBPUSD%3DX')
		ORDER by vchr_name""",
    "ALL": """SELECT vchr_price_symbol, vchr_name FROM tbl_assets 
		WHERE vchr_price_symbol is not null
		ORDER by vchr_name""",
    "STOCKS": """SELECT vchr_symbol, vchr_name FROM tbl_assets 
		WHERE vchr_type="Stock" 
		AND vchr_symbol is not null
		ORDER by vchr_name"""
}

if len(sys.argv) > 1:
    lst = sys.argv[1]

    if lst not in LISTS:
        util.flow.error("List not found!")

    sql = LISTS[lst]

    with util.db.db_conection.cursor() as cursor:
        cursor.execute(sql)
        result = cursor.fetchall()

        for row in result:
            print("%s=%s" % (row[0], row[1]), flush=True)

else:
    util.flow.error("Specify the list name!")
