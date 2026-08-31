import mysql.connector

def connect():
    return mysql.connector.connect(
        host="x.x.x.x",
        user="",
        password="",
        database="",
        charset="utf8"
    )

db_conection = connect()

def asset_id_from_symbol(symbol):
    asset_id = -1
    with db_conection.cursor() as cursor:
        cursor.execute("""SELECT int_assetID FROM tbl_assets 
                           WHERE vchr_symbol=%s""", (symbol,))
        result = cursor.fetchall()
        for row in result:
            asset_id = row[0]

    return asset_id
