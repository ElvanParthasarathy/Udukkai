import os
import re

thiruthi = r"D:\Projects\Elvan Niril\flutter\lib\src\cheyalpaadugal\niril_pattu\kaatchi\thiruthi\porul\niril_pattu_porul_thiruthi.dart"
with open(thiruthi, "r", encoding="utf-8") as f:
    content = f.read()

replacement = """    kalanjiyam.savePorul(PorulTharavuru(
      id: _isEditing ? widget.product!.id : -1,
      porulPeyar: _porulPeyar,
      hsnCode: _hsnController.text.trim(),
      vilai: double.tryParse(_vilaiController.text) ?? 0.0,
      variVeetham: double.tryParse(_variController.text) ?? 0.0,
      alavuVagai: _alavuVagai,
      alagu: alagu,
      createdAt: widget.product?.createdAt ?? DateTime.now(),
      updatedAt: DateTime.now(),
      isDeleted: widget.product?.isDeleted ?? false,
    )).then((_) {"""

content = re.sub(
    r"    kalanjiyam\.savePorul\(\s*id:.*?alagu: alagu,\s*\)\.then\(\(\_\) \{",
    replacement,
    content,
    flags=re.DOTALL
)

with open(thiruthi, "w", encoding="utf-8") as f:
    f.write(content)

print("Fixed niril_pattu_porul_thiruthi.dart.")
