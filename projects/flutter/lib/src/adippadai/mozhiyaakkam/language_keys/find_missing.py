import re
import os

keys_en = set()
keys_ta = set()
keys_tg = set()

def extract_keys(filename):
    keys = set()
    with open(filename, 'r', encoding='utf-8') as f:
        content = f.read()
    matches = re.findall(r"^\s*'([^']+)'\s*:", content, re.MULTILINE)
    for m in matches:
        keys.add(m)
    return keys

base_path = 'd:/Projects/Elvan Niril/flutter/lib/src/localization/language_keys/'

keys_en = extract_keys(base_path + 'en.dart')
keys_ta = extract_keys(base_path + 'ta.dart')
keys_tg = extract_keys(base_path + 'tg.dart')

all_keys = keys_en.union(keys_ta).union(keys_tg)

missing_in_en = all_keys - keys_en
missing_in_ta = all_keys - keys_ta
missing_in_tg = all_keys - keys_tg

print("Missing in English:", list(missing_in_en))
print("Missing in Tamil:", list(missing_in_ta))
print("Missing in Tanglish:", list(missing_in_tg))
