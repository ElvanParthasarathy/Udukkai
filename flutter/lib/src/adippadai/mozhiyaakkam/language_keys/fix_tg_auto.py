file_path = 'd:/Projects/Elvan Niril/flutter/lib/src/localization/language_keys/tg.dart'
with open(file_path, 'r', encoding='utf-8') as f:
    lines = f.readlines()

new_lines = []
for line in lines:
    if '// === Auto Added Keys ===' in line and len(line.strip()) > 50:
        parts = line.split('// === Auto Added Keys ===')
        new_lines.append('  // === Auto Added Keys ===\n')
        rest = parts[1]
        rest = rest.replace(",  '", ",\n  '")
        new_lines.append(rest + '\n')
    else:
        new_lines.append(line)

with open(file_path, 'w', encoding='utf-8') as f:
    f.writelines(new_lines)
print('Fixed Auto Added Keys')
