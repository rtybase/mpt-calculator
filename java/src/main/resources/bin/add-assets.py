import sys
import configparser
import util.db

def max_id():
    total_count = 0
    with util.db.db_conection.cursor() as cursor:
        cursor.execute("SELECT max(int_assetID) FROM tbl_assets")
        result = cursor.fetchall()
        for row in result:
            total_count = row[0]

    return total_count

def save_to_db(assetId, assetName, symbol):
    with util.db.db_conection.cursor() as cursor:
        cursor.execute("""INSERT into tbl_assets 
                           (int_assetID,vchr_name,vchr_symbol,vchr_price_symbol) 
                           values (%s,%s,%s,%s)
                           ON DUPLICATE KEY UPDATE
                           vchr_symbol=VALUES(vchr_symbol),
                           vchr_price_symbol=VALUES(vchr_price_symbol)""",\
            (assetId, assetName, symbol, symbol))
    util.db.db_conection.commit()


if len(sys.argv) > 1:
    print("Adding new assets from:", sys.argv[1], flush=True)
    print("------------------------------------", flush=True)
    config = configparser.ConfigParser(allow_unnamed_section=True)
    config.read(sys.argv[1])
    items = config.items(configparser.UNNAMED_SECTION)

    assetId = max_id() + 1
    for key,value in items:
        symbol = key.upper()
        print("%s. %s=%s" % (assetId, symbol, value))
        save_to_db(assetId, value, symbol)
        assetId += 1

else:
    print("Specify the file with assets!")
    sys.exit(1)