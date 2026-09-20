import os
import re

thiruthi = r"D:\Projects\Elvan Niril\flutter\lib\src\cheyalpaadugal\niril_pattu\kaatchi\thiruthi\vaangunar\niril_pattu_vaangunar_thiruthi.dart"
with open(thiruthi, "r", encoding="utf-8") as f:
    content = f.read()

replacement = """    kalanjiyam.saveVaangunar(VaangunarTharavuru(
      id: _isEditing ? widget.vaangunar!.id : -1,
      peyar: _peyar,
      mugavari: _mugavari,
      oor: _oor,
      maavattam: _maavattam,
      maanilam: _maanilam,
      naadu: _naadu,
      velinaadMugavari: _velinaadMugavari,
      anjalKuriyeedu: _anjalKuriyeeduController.text.trim(),
      gstin: gstin.toUpperCase(),
      minnanjal: _minnanjalController.text.trim(),
      tholaipaesi: _tholaipaesiController.text.trim(),
      createdAt: widget.vaangunar?.createdAt ?? DateTime.now(),
      updatedAt: DateTime.now(),
      isDeleted: widget.vaangunar?.isDeleted ?? false,
    )).then((_) {"""

content = re.sub(
    r"    kalanjiyam\.saveVaangunar\(\s*id:.*?tholaipaesi:.*?\n    \)\.then\(\(\_\) \{",
    replacement,
    content,
    flags=re.DOTALL
)

with open(thiruthi, "w", encoding="utf-8") as f:
    f.write(content)

print("Fixed niril_pattu_vaangunar_thiruthi.dart.")
