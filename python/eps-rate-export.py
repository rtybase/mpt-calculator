import mysql.connector
import datetime
import json
import sys

mydb = mysql.connector.connect(
  host="x.x.x.x",
  user="",
  password="",
  database="",
  charset="utf8"
)

def date_to_string(date):
    return date.strftime('%Y-%m-%d')

def subtract_days(start_date, no_of_days):
    return start_date - datetime.timedelta(days = no_of_days)

def add_days(start_date, no_of_days):
    return start_date + datetime.timedelta(days = no_of_days)

def previous_non_weekend_date(start_date):
    week_day = start_date.weekday()

    if week_day == 6:
        return subtract_days(start_date, 2)
    elif week_day == 5:
        return subtract_days(start_date, 1)

    return start_date

def next_non_weekend_date(start_date):
    week_day = start_date.weekday()

    if week_day == 6:
        return add_days(start_date, 1)
    elif week_day == 5:
        return add_days(start_date, 2)

    return start_date

def get_last_return_rates(asset_id, around_date):
    prices = {}
    start_date = previous_non_weekend_date(subtract_days(around_date, 1))
    with mydb.cursor() as cursor:
        cursor.execute("""SELECT dtm_date, dbl_return FROM tbl_prices 
                           WHERE fk_assetID = %s AND dtm_date >= %s 
                           ORDER BY dtm_date LIMIT 0,3""",\
            (asset_id, date_to_string(start_date)))
        result = cursor.fetchall()
        for row in result:
            prices[row[0]] = row[1]

    return prices

def check_if_dates_are_consecutive(dates_to_check, around_date):
    if (len(dates_to_check) == 3):
        expected_dates = [previous_non_weekend_date(subtract_days(around_date, 1)),\
                          around_date,\
                          next_non_weekend_date(add_days(around_date, 1))]
        return set(expected_dates) == set(dates_to_check)

    if (len(dates_to_check) == 2):
        expected_dates = [previous_non_weekend_date(subtract_days(around_date, 1)),\
                          around_date]
        return set(expected_dates) == set(dates_to_check)

    return False

def date_to_string_key_dictionary_from(date_indexed_dictionary):
    result = {}

    for k, v in date_indexed_dictionary.items():
        result[date_to_string(k)] = v

    return result

def print_as_csv(args):
    if len(args) > 1:
         return args[1].lower() == "csv"

    return False

def print_training_data(eps_entries):
    print("asset_id,prev_pred_eps,prev_eps,pred_eps,eps,prev_rate,rate,next_rate")

    for key in eps_entries:
        for d in eps_entries[key]:
            values_only = [v for d, v in eps_entries[key][d]["return_rates"].items()]

            if (len(values_only) == 3):
                print("%s,%s,%s,%s,%s,%s,%s,%s" % (key,\
                    eps_entries[key][d]["previous"]["prdicted_eps"],\
                    eps_entries[key][d]["previous"]["eps"],
                    eps_entries[key][d]["current"]["prdicted_eps"],\
                    eps_entries[key][d]["current"]["eps"],\
                    values_only[0],\
                    values_only[1],\
                    values_only[2]), flush = True)

def print_data_for_prediction(eps_entries):
    print("asset_id,prev_pred_eps,prev_eps,pred_eps,eps,prev_rate,rate")

    for key in eps_entries:
        for d in eps_entries[key]:
            values_only = [v for d, v in eps_entries[key][d]["return_rates"].items()]

            if (len(values_only) == 2):
                print("%s,%s,%s,%s,%s,%s,%s" % (key,\
                    eps_entries[key][d]["previous"]["prdicted_eps"],\
                    eps_entries[key][d]["previous"]["eps"],
                    eps_entries[key][d]["current"]["prdicted_eps"],\
                    eps_entries[key][d]["current"]["eps"],\
                    values_only[0],\
                    values_only[1]), flush = True)


select_eps_query = """SELECT fk_assetID, dtm_date, dbl_eps, dbl_prd_eps 
    FROM tbl_eps 
    WHERE dbl_prd_eps is not NULL
    ORDER BY fk_assetID, dtm_date"""

eps_entries = {}

with mydb.cursor() as cursor:
    cursor.execute(select_eps_query)
    result = cursor.fetchall()

    current_id = -1
    prev_eps = {}

    for row in result:
        key = row[0]
        if (key not in eps_entries):
            eps_entries[key] = {}

        current_eps = {"eps": row[2], "prdicted_eps": row[3]}

        if (current_id != key):
            current_id = key
            prev_eps = {}

        if (len(prev_eps) > 0):
            three_days_returns = get_last_return_rates(key, row[1])
            dates_only = [d for d in three_days_returns]

            if (check_if_dates_are_consecutive(dates_only, row[1])):
                eps_entries[key][date_to_string(row[1])] = {"current": current_eps,\
                    "previous": prev_eps,\
                    "return_rates": date_to_string_key_dictionary_from(three_days_returns)}

        prev_eps = current_eps


if (print_as_csv(sys.argv)):
    print_training_data(eps_entries)
    print_data_for_prediction(eps_entries)

else:
    print(json.dumps(eps_entries, indent=4))

#print(len(eps_entries))

