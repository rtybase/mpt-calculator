import requests
import sys
import pandas as pd
import util.dates
import util.db

URL = "https://api.nasdaq.com/api/quote/list-type-extended/daily_earnings_surprise?queryString=date=%s"

HEADERS = {
    "User-Agent": "Mozilla/5.0",
    "Accept": "application/json",
    "Origin": "https://www.nasdaq.com",
    "Referer": "https://www.nasdaq.com/"
}

REQUIRED_COLUMNS = ['symbol','eps','consensusEPSForecast','estPercent','surprisePercent']

def filter_to_required_colums_with_date(data, date):
    result = {k:v for k,v in data.items() if k in REQUIRED_COLUMNS}
    result['date'] = date
    return result


def add_with_required_colums_with_date(from_list, to_list, date):
    if from_list:
        for v in from_list:
            to_list.append(filter_to_required_colums_with_date(v, date))


def load_eps_for(date, rows):
    str_date = util.dates.date_to_string(date)
    response = requests.get(URL % (str_date,), headers=HEADERS)

    if response.status_code == 200:
        data = response.json()
        try:
            add_with_required_colums_with_date(data['data']['exceed']['table']['rows'],\
                rows, str_date)

            add_with_required_colums_with_date(data['data']['meet']['table']['rows'],\
                rows, str_date)

            add_with_required_colums_with_date(data['data']['fail']['table']['rows'],\
                rows, str_date)

        except KeyError:
            print("Unexpected JSON structure:", data)
    else:
        print(f"Request failed with status code {response.status_code}")


def print_list_as_csv(lst):
    df = pd.json_normalize(lst)
    df.to_csv(sys.stdout, index=False, encoding='utf-8')


def save_to_db(asset_id, eps_detail):
    with util.db.db_conection.cursor() as cursor:
        cursor.execute("""INSERT INTO tbl_eps (fk_assetID,dbl_eps,dbl_prd_eps,int_no_of_analysts,dtm_date)
                           VALUES (%s,%s,%s,%s,%s)
                           ON DUPLICATE KEY UPDATE
                           dbl_eps=VALUES(dbl_eps),
                           dbl_prd_eps=VALUES(dbl_prd_eps)""",\
            (asset_id, eps_detail['eps'], eps_detail['consensusEPSForecast'],\
                eps_detail['estPercent'], eps_detail['date']))
    util.db.db_conection.commit()


def try_save_to_db(lst):
    saved = []
    not_saved = []

    for eps_detail in lst:
        asset_id = util.db.asset_id_from_symbol(eps_detail['symbol'])

        if asset_id >= 0:
            save_to_db(asset_id, eps_detail)
            saved.append(eps_detail)
        else:
            not_saved.append(eps_detail)

    return saved, not_saved


if len(sys.argv) > 2:
    start_date = util.dates.string_to_date(sys.argv[1])
    max_date_back = int(sys.argv[2])

    rows = []

    for days_back in range(0, max_date_back + 1):
        date = util.dates.subtract_days(start_date, days_back)

        if not util.dates.is_weekend(date):
            load_eps_for(date, rows)

    saved, not_saved = try_save_to_db(rows)

    print("--------- Saved records ---------")
    print_list_as_csv(saved)

    print("--------- Not saved records -----")
    print_list_as_csv(not_saved)

else:
    print("Specify the start date (YYYY-MM-DD) and the number of days to go back!")