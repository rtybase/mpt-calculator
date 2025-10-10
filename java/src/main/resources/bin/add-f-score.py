import sys
import os
import csv
import datetime
import util.db
import util.dates

BAD_VALUES = ["-", "N/A"]

def save_to_db(symbol, fscore, date):
    print(f"-- adding ('{symbol}', {fscore}, {date})", flush=True)
    with util.db.db_conection.cursor() as cursor:
        cursor.execute("""INSERT into tbl_fscores
                           (vchr_symbol,dbl_fscore,dtm_date) 
                           values (%s,%s,%s)
                           ON DUPLICATE KEY UPDATE
                           dbl_fscore=VALUES(dbl_fscore)""",\
            (symbol, fscore, date))
    util.db.db_conection.commit()

def convert_to_db_date(str_value):
    date = datetime.datetime.strptime(str_value, '%b%y')
    return util.dates.date_to_string(date)

def load_f_score_from(path):
    print("- Loading from:", path, flush=True)
    with open(path, mode ='r') as file:
        content = csv.reader(file)

        symbol = next(content)
        dates = next(content)
        scores = next(content)
        for date, score in zip(dates, scores):
            if (len(date) > 0) and (score not in BAD_VALUES):
                save_to_db(symbol[0], score, convert_to_db_date(date))


if len(sys.argv) > 1:
    print("Adding f-scores from:", sys.argv[1], flush=True)
    print("------------------------------------", flush=True)

    folderContent = os.scandir(sys.argv[1])
    for entry in folderContent:
        if entry.is_file() and entry.name.endswith(".csv"):
            load_f_score_from(entry.path)

else:
    error("Specify the folder with the files to load from!")
