import os
import re

thiruthi = r"D:\Projects\Elvan Niril\flutter\lib\src\cheyalpaadugal\niril_podhu\kaatchi\thiruthi\patru_thiruthi.dart"
with open(thiruthi, "r", encoding="utf-8") as f:
    content = f.read()

# 1. getNextVanakkam(seyaliVagai, _selectedNiruvanamId) -> getNextVanakkam(_selectedNiruvanamId)
content = content.replace("kalanjiyam.getNextVanakkam(seyaliVagai, _selectedNiruvanamId)", "kalanjiyam.getNextVanakkam(_selectedNiruvanamId)")

# 2. remove unused seyaliVagai variable at line 193 if present (or just rely on the analyzer, it's a warning, but we can fix it)
content = re.sub(r"final seyaliVagai = .*?;\s*", "", content)

# 3. PatruPattiyalLink -> PatruPattiyalInaippuTharavuru
content = content.replace("PatruPattiyalLink", "PatruPattiyalInaippuTharavuru")

with open(thiruthi, "w", encoding="utf-8") as f:
    f.write(content)

print("Fixed getNextVanakkam and PatruPattiyalLink.")
