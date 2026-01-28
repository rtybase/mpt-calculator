import sys
from pathlib import Path
import json

def find_id_with(quotes, exchange):
    result = None
    for quote in quotes:
        if quote["exchange"] == exchange and quote["type"] == f"Stock - {exchange}":
            result = quote
            break

    return result

if len(sys.argv) > 1:
    p = Path(sys.argv[1])

    with p.open('r', encoding='utf-8') as f:
        data = json.loads(f.read())

#    print(data["quotes"])
    result = find_id_with(data["quotes"], "NYSE")
    if result is None:
        result = find_id_with(data["quotes"], "NASDAQ")

#    print(result)
    print(result["id"])

else:
    print("Specify the file with data!")
