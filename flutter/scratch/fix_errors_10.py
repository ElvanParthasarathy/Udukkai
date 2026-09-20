import os
import re

thiruthi = r"D:\Projects\Elvan Niril\flutter\lib\src\cheyalpaadugal\niril_podhu\kaatchi\thiruthi\patru_thiruthi.dart"
with open(thiruthi, "r", encoding="utf-8") as f:
    content = f.read()

# 1. Cast first.vaangunarPeyar
content = re.sub(r"=\s*first\.vaangunarPeyar;", r"= first.vaangunarPeyar.cast<String, String>();", content)
content = re.sub(r"=\s*first\.vaangunarMunvari;", r"= first.vaangunarMunvari.cast<String, String>();", content)

# 2. Remove suttruEn
# Find the ElvanThedalPetti invocation around line 255
# `suttruEn: p.suttruEn,`
# Wait, let's just find and remove any line containing `suttruEn:`
lines = content.split('\n')
new_lines = []
for line in lines:
    if 'suttruEn:' in line:
        continue
    new_lines.append(line)

with open(thiruthi, "w", encoding="utf-8") as f:
    f.write('\n'.join(new_lines))

print("Fixed patru_thiruthi.dart")
