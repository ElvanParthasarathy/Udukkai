import os
import re

thiruthi = r"D:\Projects\Elvan Niril\flutter\lib\src\cheyalpaadugal\niril_pattu\kaatchi\thiruthi\pattiyal\niril_pattu_pattiyal_thiruthi.dart"
with open(thiruthi, "r", encoding="utf-8") as f:
    content = f.read()

content = content.replace("PattiyalKalanjiyam.getCurrentFinYear()", "PattuPattiyalKalanjiyam.getCurrentFinYear()")
content = content.replace("kalanjiyam.getNextVanakkam(\n        'silk', _selectedNiruvanamId, finYear,\n      )", "kalanjiyam.getNextVanakkam(_selectedNiruvanamId, finYear)")

with open(thiruthi, "w", encoding="utf-8") as f:
    f.write(content)

udhavi = r"D:\Projects\Elvan Niril\flutter\lib\src\cheyalpaadugal\niril_pattu\kaatchi\thiruthi\pattiyal\koorugal\pattu_pattiyal_udhavi.dart"
with open(udhavi, "r", encoding="utf-8") as f:
    content2 = f.read()

content2 = content2.replace("seyaliVagai: 'pattu',", "")

with open(udhavi, "w", encoding="utf-8") as f:
    f.write(content2)

kooru = r"D:\Projects\Elvan Niril\flutter\lib\src\cheyalpaadugal\niril_pattu\kaatchi\thiruthi\pattiyal\koorugal\pattu_vaangunargal_kooru.dart"
with open(kooru, "r", encoding="utf-8") as f:
    content3 = f.read()

content3 = content3.replace("seyaliVagai: 'silk',", "")

with open(kooru, "w", encoding="utf-8") as f:
    f.write(content3)

print("Fixed pattiyal errors.")
