import sys
import os
import csv
import util.db

def symbol_from_asset(assetName):
    symbol = ""
    with util.db.db_conection.cursor() as cursor:
        cursor.execute("""SELECT vchr_symbol FROM tbl_assets 
                           WHERE vchr_name=%s""", (assetName,))
        result = cursor.fetchall()
        for row in result:
            symbol = row[0]

    return symbol

def sector_id_from_name(sectorName):
    sectorId = -1
    with util.db.db_conection.cursor() as cursor:
        cursor.execute("""SELECT int_sectorID FROM tbl_sectors 
                           WHERE vchr_name=%s""", (sectorName,))
        result = cursor.fetchall()
        for row in result:
            sectorId = row[0]

    return sectorId

def industry_id_from_name(industryName):
    industryId = -1
    with util.db.db_conection.cursor() as cursor:
        cursor.execute("""SELECT int_industryID FROM tbl_industries
                           WHERE vchr_name=%s""", (industryName,))
        result = cursor.fetchall()
        for row in result:
            industryId = row[0]

    return industryId

def save_to_db(symbol, sectorId, industryId):
    print(f"--- adding ('{symbol}', {sectorId}, {industryId})")
    with util.db.db_conection.cursor() as cursor:
        cursor.execute("""INSERT into tbl_stocks 
                           (vchr_symbol,fk_sectorID,fk_industryID) 
                           values (%s,%s,%s)
                           ON DUPLICATE KEY UPDATE
                           fk_sectorID=VALUES(fk_sectorID),
                           fk_industryID=VALUES(fk_industryID)""",\
            (symbol, sectorId, industryId))
    util.db.db_conection.commit()

def error(text):
    print(text)
    sys.exit(1)

def process_data(assetName, sector, industry):
    print(f"-- asset='{assetName}', sector='{sector}', industry='{industry}'")

    symbol = symbol_from_asset(assetName)
    if len(symbol) == 0:
        error(f"-- '{assetName}' has no symbol defined!")

    sectorId = sector_id_from_name(sector)
    if sectorId < 0:
        error(f"-- '{sector}' is not defined in DB!")

    industryId = industry_id_from_name(industry)
    if industryId < 0:
        error(f"-- '{industry}' is not defined in DB!")

    save_to_db(symbol, sectorId, industryId)

def load_stocks_from(path):
    print("- Loading from:", path)
    with open(path, mode ='r') as file:
        content = csv.reader(file)
        for line in content:
            process_data(line[0], line[1], line[2])


if len(sys.argv) > 1:
    print("Adding new stocks from:", sys.argv[1], flush=True)
    print("------------------------------------", flush=True)

    folderContent = os.scandir(sys.argv[1])
    for entry in folderContent:
        if entry.is_file() and entry.name.endswith(".csv"):
            load_stocks_from(entry.path)

else:
    error("Specify the folder with the files to load from!")
