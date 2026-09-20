import os
import re

thiruthi = r"D:\Projects\Elvan Niril\flutter\lib\src\cheyalpaadugal\niril_podhu\kaatchi\thiruthi\patru_thiruthi.dart"
with open(thiruthi, "r", encoding="utf-8") as f:
    t_content = f.read()

# Replace seluthiVagai with seluthumMurai for editingEntry properties
t_content = re.sub(r"widget\.editingEntry(\?|\!)\.seluthiVagai", r"widget.editingEntry\1.seluthumMurai", t_content)
t_content = re.sub(r"p\.seluthiVagai", r"p.seluthumMurai", t_content)

# Replace suttruEn with vanakkam
t_content = re.sub(r"widget\.editingEntry(\?|\!)\.suttruEn", r"widget.editingEntry\1.vanakkam", t_content)
t_content = re.sub(r"p\.suttruEn", r"p.vanakkam", t_content)

with open(thiruthi, "w", encoding="utf-8") as f:
    f.write(t_content)

print("Fixed seluthiVagai and suttruEn.")
