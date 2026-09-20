import os
import re

base_dir = r"D:\Projects\Elvan Niril\flutter\lib\src\cheyalpaadugal\niril_podhu\kalanjiyam"

map_fields = [
    "peyar", "mugavari", "oor", "maavattam", "maanilam", "naadu", 
    "velinaadMugavari", "porulPeyar", "vaangunarPeyar", "vaangunarMunvari"
]

for filename in os.listdir(base_dir):
    if filename.endswith("_kalanjiyam.dart") and ("pattu_" in filename or "kooli_" in filename):
        filepath = os.path.join(base_dir, filename)
        with open(filepath, "r", encoding="utf-8") as f:
            content = f.read()
        
        modified = False
        for field in map_fields:
            # Look for `Value(tharavuru.field)` and replace with `Value(tharavuru.field.cast<String, String>())`
            # Need to be careful not to replace it if it's already casted
            pattern = rf"Value\(tharavuru\.{field}\)"
            replacement = f"Value(tharavuru.{field}.cast<String, String>())"
            if re.search(pattern, content):
                content = re.sub(pattern, replacement, content)
                modified = True
                
            # Wait, what if it's `Value(tharavuru.peyar as Map<String, String>)`? Let's just blindly cast.
            
        if modified:
            with open(filepath, "w", encoding="utf-8") as f:
                f.write(content)

print("Added cast<String, String>() to Kalanjiyam files.")

# Let's also double check vaangunar_nilaimai.dart state provider error.
# The error was: `The function 'StateProvider' isn't defined.`
# We added `import 'package:flutter_riverpod/flutter_riverpod.dart';`
# But maybe we need `import 'package:flutter_riverpod/legacy.dart';`?
vaangunar_nilaimai = os.path.join(base_dir, "vaangunar_nilaimai.dart")
with open(vaangunar_nilaimai, "r", encoding="utf-8") as f:
    vcontent = f.read()

if "import 'package:flutter_riverpod/legacy.dart';" not in vcontent:
    vcontent = "import 'package:flutter_riverpod/legacy.dart';\n" + vcontent
    with open(vaangunar_nilaimai, "w", encoding="utf-8") as f:
        f.write(vcontent)
    print("Added legacy.dart to vaangunar_nilaimai.dart")

