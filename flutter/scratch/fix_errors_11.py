import os
import re

thiruthi = r"D:\Projects\Elvan Niril\flutter\lib\src\cheyalpaadugal\niril_podhu\kaatchi\thiruthi\patru_thiruthi.dart"
with open(thiruthi, "r", encoding="utf-8") as f:
    content = f.read()

# 1. PatruPattiyalLink -> PatruPattiyalInaippuTharavuru
content = content.replace("PatruPattiyalLink(", "PatruPattiyalInaippuTharavuru(")

# 2. kalanjiyam.isPatruEnDuplicate(seyaliVagai, _selectedNiruvanamId, _patruEn -> kalanjiyam.isPatruEnDuplicate(_selectedNiruvanamId, _patruEn
content = content.replace(
"""      final isDuplicate = await kalanjiyam.isPatruEnDuplicate(
        seyaliVagai,
        _selectedNiruvanamId,
        _patruEn,""",
"""      final isDuplicate = await kalanjiyam.isPatruEnDuplicate(
        _selectedNiruvanamId,
        _patruEn,""")

with open(thiruthi, "w", encoding="utf-8") as f:
    f.write(content)

print("Fixed PatruPattiyalLink and isPatruEnDuplicate arguments.")
