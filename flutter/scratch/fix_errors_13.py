import os
import re

thiruthi = r"D:\Projects\Elvan Niril\flutter\lib\src\cheyalpaadugal\niril_podhu\kaatchi\thiruthi\patru_thiruthi.dart"
with open(thiruthi, "r", encoding="utf-8") as f:
    content = f.read()

# 1. Remove `seyaliVagai` argument from `_buildReceiptDataSection` call
content = content.replace("_buildReceiptDataSection(seyaliVagai, isDark)", "_buildReceiptDataSection(isDark)")

# 2. Remove `String seyaliVagai, ` from `_buildReceiptDataSection` definition
content = content.replace("Widget _buildReceiptDataSection(String seyaliVagai, bool isDark)", "Widget _buildReceiptDataSection(bool isDark)")

# 3. Remove `seyaliVagai: seyaliVagai,` from wherever it is inside `_buildReceiptDataSection`
content = re.sub(r"seyaliVagai:\s*seyaliVagai,", "", content)

# Also let's check for any remaining `mode` unused variables 
content = re.sub(r"final mode = ref\.read\(appModeProvider\);", "", content)

with open(thiruthi, "w", encoding="utf-8") as f:
    f.write(content)

print("Fixed seyaliVagai and unused mode variables.")
