import os
import re

base_dir = r"D:\Projects\Elvan Niril\flutter\lib\src\cheyalpaadugal\niril_podhu"

# 1. Fix patru_kalanjiyam
for mode in ['kooli', 'pattu']:
    filepath = os.path.join(base_dir, "kalanjiyam", f"{mode}_patru_kalanjiyam.dart")
    with open(filepath, "r", encoding="utf-8") as f:
        content = f.read()

    # vangiPeyar: entry.vangiPeyar, -> vangiPeyar: entry.vangiPeyar ?? '',
    content = re.sub(r"vangiPeyar:\s*entry\.vangiPeyar,", r"vangiPeyar: entry.vangiPeyar ?? '',", content)
    
    with open(filepath, "w", encoding="utf-8") as f:
        f.write(content)

# 2. Fix patru_thiruthi
thiruthi = os.path.join(base_dir, "kaatchi", "thiruthi", "patru_thiruthi.dart")
with open(thiruthi, "r", encoding="utf-8") as f:
    content = f.read()

# Lines 674, 675: probably _selectedVaangunarPeyar = widget.patru!.vaangunarPeyar;
# We need to cast it: widget.patru!.vaangunarPeyar.cast<String, String>()
content = re.sub(r"=\s*widget\.patru!\.vaangunarPeyar;", r"= widget.patru!.vaangunarPeyar.cast<String, String>();", content)
content = re.sub(r"=\s*widget\.patru!\.vaangunarMunvari;", r"= widget.patru!.vaangunarMunvari.cast<String, String>();", content)

with open(thiruthi, "w", encoding="utf-8") as f:
    f.write(content)

print("Fixed vangiPeyar and patru_thiruthi map assignments.")
