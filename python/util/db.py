import mysql.connector

db_conection = mysql.connector.connect(
  host="x.x.x.x",
  user="",
  password="",
  database="",
  charset="utf8"
)

def asset_id_from_symbol(symbol):
    asset_id = -1
    with db_conection.cursor() as cursor:
        cursor.execute("""SELECT int_assetID FROM tbl_assets 
                           WHERE vchr_symbol=%s""", (symbol,))
        result = cursor.fetchall()
        for row in result:
            asset_id = row[0]

    return asset_id

