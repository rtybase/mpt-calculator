import sys
import pandas as pd
from pathlib import Path
import json

if len(sys.argv) > 1:
    p = Path(sys.argv[1])

    with p.open('r', encoding='utf-8') as f:
        data = json.loads(f.read())

    df = pd.json_normalize(data['earnings'])
    df.to_csv(sys.stdout, index=False, encoding='utf-8')

else:
    print("Specify the file with data!")