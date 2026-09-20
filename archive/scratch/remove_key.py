import sys

filepath = r"d:\Projects\Elvan Niril\android\app\google-services.json"

with open(filepath, 'r', encoding='utf-8') as f:
    content = f.read()

# Replace the key with a dummy key
content = content.replace('"current_key": "AIzaSyD8AYlOTBXisWwTMUfixn2YB1vxf5OGHpM"', '"current_key": "YOUR_API_KEY_HERE_FOR_SECURITY_REASONS"')

with open(filepath, 'w', encoding='utf-8') as f:
    f.write(content)

print("Key removed from google-services.json")
