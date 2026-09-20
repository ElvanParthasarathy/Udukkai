import os
import re

thiruthi = r"D:\Projects\Elvan Niril\flutter\lib\src\cheyalpaadugal\niril_podhu\kaatchi\thiruthi\patru_thiruthi.dart"
with open(thiruthi, "r", encoding="utf-8") as f:
    content = f.read()

replacement = """    final entry = widget.editingEntry;
    if (entry != null) {
      _selectedNiruvanamId = entry.niruvanamId;
      _selectedVaangunarId = entry.vaangunarId;
      _vaangunarPeyarMap = entry.vaangunarPeyar.cast<String, String>();
      _patruNaal = entry.patruNaal;
      _patruEn = entry.patruEn;
      _vanakkam = entry.vanakkam;
      _thogai = entry.thogai;
      _seluthiVagai = SeluthiVagaiX.fromStored(entry.seluthumMurai);
      _suttruEn = entry.parivarthanaiEn; // Map parivarthanaiEn to UI's suttruEn
      _ullkurippu = entry.ullkurippu;

      _thogaiCtrl.text = _thogai > 0 ? _thogai.toStringAsFixed(2) : '';
      _suttruEnCtrl.text = _suttruEn;
      _ullkurippuCtrl.text = _ullkurippu;
      _patruEnCtrl.text = _patruEn;
    } else {"""

content = re.sub(
    r"    final entry = widget\.editingEntry;\s*if \(entry != null\) \{.*?\n    \} else \{",
    replacement,
    content,
    flags=re.DOTALL
)

with open(thiruthi, "w", encoding="utf-8") as f:
    f.write(content)

print("Fixed PatrugalTharavuru mapping in patru_thiruthi.dart.")
