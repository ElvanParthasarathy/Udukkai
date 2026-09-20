import os
import re

base_dir = r"D:\Projects\Elvan Niril\flutter\lib\src\cheyalpaadugal\niril_podhu\kalanjiyam"

vaangunar_files = ["pattu_vaangunar_kalanjiyam.dart", "kooli_vaangunar_kalanjiyam.dart"]

for file in vaangunar_files:
    filepath = os.path.join(base_dir, file)
    with open(filepath, "r", encoding="utf-8") as f:
        content = f.read()

    # The error is in:
    # vaangunarPeyar: entry.vaangunarPeyar as Map<String, String>,
    # vaangunarMunvari: entry.vaangunarMunvari as Map<String, String>,
    # bankDetails: entry.bankDetails as Map<String, String>,
    # We should cast them using Map<String, String>.from((entry.vaangunarPeyar as Map) ?? {})
    
    # We already did this once? Let's check the error:
    # `lib\src\cheyalpaadugal\niril_podhu\kalanjiyam\pattu_vaangunar_kalanjiyam.dart:66:23`

    content = re.sub(r"vaangunarPeyar:\s*entry\.vaangunarPeyar\s*as\s*Map<String,\s*String>,", r"vaangunarPeyar: (entry.vaangunarPeyar as Map).cast<String, String>(),", content)
    content = re.sub(r"vaangunarMunvari:\s*entry\.vaangunarMunvari\s*as\s*Map<String,\s*String>,", r"vaangunarMunvari: (entry.vaangunarMunvari as Map).cast<String, String>(),", content)
    content = re.sub(r"bankDetails:\s*entry\.bankDetails\s*as\s*Map<String,\s*String>,", r"bankDetails: (entry.bankDetails as Map).cast<String, String>(),", content)

    with open(filepath, "w", encoding="utf-8") as f:
        f.write(content)

print("Vaangunar maps casted.")

# Also the state provider error
vaangunar_nilaimai = os.path.join(base_dir, "vaangunar_nilaimai.dart")
with open(vaangunar_nilaimai, "r", encoding="utf-8") as f:
    content = f.read()

# Replace StateProvider with StateProvider from flutter_riverpod
# Ensure import 'package:flutter_riverpod/flutter_riverpod.dart'; is there.
if "import 'package:flutter_riverpod/flutter_riverpod.dart';" not in content:
    content = "import 'package:flutter_riverpod/flutter_riverpod.dart';\n" + content

with open(vaangunar_nilaimai, "w", encoding="utf-8") as f:
    f.write(content)

# And fix patru_thiruthi.dart
patru_thiruthi = r"D:\Projects\Elvan Niril\flutter\lib\src\cheyalpaadugal\niril_podhu\kaatchi\thiruthi\patru_thiruthi.dart"
with open(patru_thiruthi, "r", encoding="utf-8") as f:
    content = f.read()

# Just replace PatrugalTharavuru( id: Value(...), ... ) with PatrugalTharavuru(...) no Value wrappers
content = re.sub(r"Value\((.*?)\)", r"\1", content)
content = content.replace("const absent()", "''")

with open(patru_thiruthi, "w", encoding="utf-8") as f:
    f.write(content)

