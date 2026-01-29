import sys
from pathlib import Path
import json

def find_id_with(quotes, symbol, exchange):
    result = None
    for quote in quotes:
        if quote["symbol"] == symbol and quote["exchange"] == exchange and quote["type"] == f"Stock - {exchange}":
            result = quote
            break

    return result

if len(sys.argv) > 2:
    p = Path(sys.argv[1])
    symbol = sys.argv[2]

    with p.open('r', encoding='utf-8') as f:
        data = json.loads(f.read())

#    print(data["quotes"])
    result = find_id_with(data["quotes"], symbol, "NYSE")
    if result is None:
        result = find_id_with(data["quotes"], symbol, "NASDAQ")

#    print(result)
    print(result["id"])

else:
    print("Specify the file with data!")
