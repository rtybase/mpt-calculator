import datetime
import holidays

nyse_holidays = holidays.NYSE()

def date_to_string(date):
    return date.strftime('%Y-%m-%d')

def subtract_days(start_date, no_of_days):
    return start_date - datetime.timedelta(days = no_of_days)

def add_days(start_date, no_of_days):
    return start_date + datetime.timedelta(days = no_of_days)

def is_weekend(date):
    week_day = date.weekday()
    return week_day == 5 or week_day == 6

def is_nyse_holiday(date):
    return date in nyse_holidays

def is_weekend_or_nyse_holiday(date):
    return is_weekend(date) or is_nyse_holiday(date)

def previous_working_date(start_date):
    date = subtract_days(start_date, 1)

    while is_weekend_or_nyse_holiday(date):
        date = subtract_days(date, 1)

    return date

def next_working_date(start_date):
    date = add_days(start_date, 1)

    while is_weekend_or_nyse_holiday(date):
        date = add_days(date, 1)

    return date

