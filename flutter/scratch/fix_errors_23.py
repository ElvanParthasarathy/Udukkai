import os
import re

thiruthi = r"D:\Projects\Elvan Niril\flutter\lib\src\cheyalpaadugal\niril_pattu\kaatchi\thiruthi\pattiyal\niril_pattu_pattiyal_thiruthi.dart"
with open(thiruthi, "r", encoding="utf-8") as f:
    content = f.read()

# Import PattuPattiyalKalanjiyam
if "pattu_pattiyal_kalanjiyam.dart" not in content:
    content = content.replace(
        "import '../../../../../adippadai/mozhiyaakkam/k.dart';",
        "import '../../../../../adippadai/mozhiyaakkam/k.dart';\nimport '../../../../niril_podhu/kalanjiyam/pattu_pattiyal_kalanjiyam.dart';"
    )

content = content.replace("PattiyalKalanjiyam.getCurrentFinYear()", "PattuPattiyalKalanjiyam.getCurrentFinYear()")
content = content.replace("kalanjiyam.getNextVanakkam(\n        'silk', _selectedNiruvanamId, finYear,\n      )", "kalanjiyam.getNextVanakkam(_selectedNiruvanamId, finYear)")
content = content.replace("kalanjiyam.getNextVanakkam(\n          'silk', _selectedNiruvanamId, finYear)", "kalanjiyam.getNextVanakkam(_selectedNiruvanamId, finYear)")

with open(thiruthi, "w", encoding="utf-8") as f:
    f.write(content)

udhavi = r"D:\Projects\Elvan Niril\flutter\lib\src\cheyalpaadugal\niril_pattu\kaatchi\thiruthi\pattiyal\koorugal\pattu_pattiyal_udhavi.dart"
with open(udhavi, "r", encoding="utf-8") as f:
    content2 = f.read()

content2 = content2.replace("kalanjiyam.getNextVanakkam(\n          'silk', state.selectedNiruvanamId, finYear)", "kalanjiyam.getNextVanakkam(state.selectedNiruvanamId, finYear)")

content2 = re.sub(
    r"kalanjiyam\.isPattiyalEnDuplicate\(\s*'silk',\s*state\.selectedNiruvanamId,\s*finYear,\s*finalBillNumber,",
    r"kalanjiyam.isPattiyalEnDuplicate(state.selectedNiruvanamId, finYear, finalBillNumber,",
    content2
)

with open(udhavi, "w", encoding="utf-8") as f:
    f.write(content2)

print("Fixed pattu_pattiyal_udhavi.dart and pattu_pattiyal_thiruthi.dart.")
