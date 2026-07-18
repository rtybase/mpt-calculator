import sys
import csv
import util.db
import util.flow

DEFAULT_LOWER_LIMIT = 1_000_000_000.0
COL_VALUATION = 7
COL_SYMBOL = 0

MULTIPLIERS = {
    'B': 1_000_000_000,
    'M': 1_000_000,
    'K': 1_000
}

def parse_human_number(value: str) -> float:
    if not value:
        return 0.0

    value = value.strip().replace(",", "").upper()

    suffix = value[-1]
    if suffix in MULTIPLIERS:
        try:
            return float(value[:-1]) * MULTIPLIERS[suffix]
        except ValueError:
            return 0.0

    try:
        return float(value)
    except ValueError:
        return 0.0

def load_data_from(path, lower_limit):
    print("- Loading from:", path, flush=True)
    print("-------", flush=True)

    rows = []

    with open(path, mode ='r') as file:
        content = csv.reader(file)
        for line in content:
            if (len(line) > COL_VALUATION):
                valuation = parse_human_number(line[COL_VALUATION])
                asset_id = util.db.asset_id_from_symbol(line[COL_SYMBOL])

                if valuation >= lower_limit and asset_id < 0:
                    rows.append(line)

    return rows

input_file = ""
lower_limit = DEFAULT_LOWER_LIMIT

if len(sys.argv) > 2:
    input_file = sys.argv[1]
    lower_limit = parse_human_number(sys.argv[2])
elif len(sys.argv) > 1:
    input_file = sys.argv[1]
else:
    util.flow.error("""Specify  
              - csv file path
              - valuation lower limit!""")

result = load_data_from(input_file, lower_limit)
writer = csv.writer(sys.stdout)

for row in result:
        writer.writerow(row)

print("-------", flush=True)

for row in result:
        print("{0}={1}".format(row[0], row[1]))
