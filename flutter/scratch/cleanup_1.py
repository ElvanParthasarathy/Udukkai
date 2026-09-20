import os
import re

# 1. Fix onPopInvoked in nalvaravu_thirai_amaippu.dart
nalvaravu = r"D:\Projects\Elvan Niril\flutter\lib\src\cheyalpaadugal\ulnuzhaivu\kaatchi\thiraigal\nalvaravu_thirai_amaippu.dart"
with open(nalvaravu, "r", encoding="utf-8") as f:
    content = f.read()

content = content.replace("onPopInvoked: (didPop) {", "onPopInvokedWithResult: (didPop, result) {")

with open(nalvaravu, "w", encoding="utf-8") as f:
    f.write(content)

# 2. Fix unused imports and variables in sodhanai_tharavu_uruvakki.dart
sodhanai = r"D:\Projects\Elvan Niril\flutter\lib\src\cheyalpaadugal\uruvakkunar_karuvigal\tharavu\sodhanai_tharavu_uruvakki.dart"
with open(sodhanai, "r", encoding="utf-8") as f:
    content = f.read()

content = re.sub(r"import\s+'\.\.\/\.\.\/\.\.\/adippadai\/nilaimai\/seyali_nilaimai\.dart';\n", "", content)
content = re.sub(r"import\s+'\.\.\/\.\.\/\.\.\/adippadai\/tharavuthalam\/seyali_tharavuthalam\.dart';\n", "", content)
# We won't remove all unused local variables automatically via regex here because they could be deeply nested or part of larger expressions. 
# But we can remove the unused imports. We can let the analyzer complain about unused locals, or we can use the `dart fix --apply` command to auto-fix unused variables/imports.

with open(sodhanai, "w", encoding="utf-8") as f:
    f.write(content)

print("Fixed onPopInvoked and basic unused imports. Next, we will run `dart fix --apply`.")
