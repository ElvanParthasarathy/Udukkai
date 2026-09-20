import json, re
data = open('d:/Projects/Elvan Niril/flutter/assets/data/db.json', encoding='utf-8').read()
matches = re.findall(r'data:image/svg\+xml[^"\']*', data)
for i, m in enumerate(list(set(matches))):
    with open(f'd:/Projects/Elvan Niril/flutter/svg_{i}.txt', 'w', encoding='utf-8') as f:
        f.write(m)
