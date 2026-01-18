import json
import sys

try:
    with open('src/main/resources/data/stocks.json', 'r') as f:
        json.load(f)
    print("JSON is valid.")
except json.JSONDecodeError as e:
    print(f"JSON Error: {e}")
    sys.exit(1)
except Exception as e:
    print(f"Error: {e}")
    sys.exit(1)
