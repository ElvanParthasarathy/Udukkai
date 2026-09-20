import os
import re

# 1. Remove seyaliVagai from VaangunarThaeduKooru
thaedu = r"D:\Projects\Elvan Niril\flutter\lib\src\cheyalpaadugal\niril_podhu\kaatchi\koorugal\vaangunar_thaedu_kooru.dart"
with open(thaedu, "r", encoding="utf-8") as f:
    content = f.read()

content = re.sub(r"final String seyaliVagai;\n", "", content)
content = re.sub(r"required this\.seyaliVagai,", "", content)

with open(thaedu, "w", encoding="utf-8") as f:
    f.write(content)

# 2. Fix suttruEn in patru_thiruthi.dart
thiruthi = r"D:\Projects\Elvan Niril\flutter\lib\src\cheyalpaadugal\niril_podhu\kaatchi\thiruthi\patru_thiruthi.dart"
with open(thiruthi, "r", encoding="utf-8") as f:
    t_content = f.read()

# Replace `p.suttruEn` or `suttruEn:` if it's there
# We want to replace `_vanakkam = widget.editingEntry?.suttruEn ?? ...` 
t_content = re.sub(r"widget\.editingEntry\?\.suttruEn", "widget.editingEntry?.vanakkam", t_content)
t_content = re.sub(r"p\.suttruEn", "p.vanakkam", t_content)

# Remove the unused mode variable again just in case
t_content = re.sub(r"final mode = ref\.watch\(appModeProvider\);", "", t_content)

with open(thiruthi, "w", encoding="utf-8") as f:
    f.write(t_content)

print("Fixed seyaliVagai in VaangunarThaeduKooru and suttruEn in PatruThiruthi.")
