import sys
import pandas as pd
from pathlib import Path
import json

if len(sys.argv) > 1:
    p = Path(sys.argv[1])

    sector_data = {}
    with p.open('r', encoding='utf-8') as f:
        data = json.loads(f.read())

    sector_data["symbol"] = data["data"]["symbol"].strip()
    sector_data["sector"] = data["data"]["summaryData"]["Sector"]["value"].strip()
    sector_data["industry"] = data["data"]["summaryData"]["Industry"]["value"].strip()

#    print(data)
    df = pd.json_normalize(sector_data)
    df.to_csv(sys.stdout, index=False, header=False, encoding='utf-8')

else:
    print("Specify the file with data!")
