import os
import re

base_dir = r"D:\Projects\Elvan Niril\flutter\lib"

# 1. seyali_tharavuthalam.dart directives order
seyali_tharavu = os.path.join(base_dir, "src", "adippadai", "tharavuthalam", "seyali_tharavuthalam.dart")
with open(seyali_tharavu, "r", encoding="utf-8") as f:
    content = f.read()
if "import 'package:flutter_riverpod/flutter_riverpod.dart';" in content:
    content = content.replace("\nimport 'package:flutter_riverpod/flutter_riverpod.dart';", "")
    content = "import 'package:flutter_riverpod/flutter_riverpod.dart';\n" + content
with open(seyali_tharavu, "w", encoding="utf-8") as f:
    f.write(content)

# 2. restore_thirai.dart imports
restore = os.path.join(base_dir, "src", "cheyalpaadugal", "ulnuzhaivu", "kaatchi", "thiraigal", "restore_thirai.dart")
with open(restore, "r", encoding="utf-8") as f:
    content = f.read()
content = content.replace("import '../../amaippugal/tharavu/pattu_niruvana_tharavugal_provider.dart';", "import '../../../amaippugal/tharavu/pattu_niruvana_tharavugal_provider.dart';")
content = content.replace("import '../../amaippugal/tharavu/kooli_niruvana_tharavugal_provider.dart';", "import '../../../amaippugal/tharavu/kooli_niruvana_tharavugal_provider.dart';")
with open(restore, "w", encoding="utf-8") as f:
    f.write(content)

# 3. pattu_tharavuthalam.dart & kooli_tharavuthalam.dart duplicate deletedAt
for db_file in ["pattu_tharavuthalam.dart", "kooli_tharavuthalam.dart"]:
    path = os.path.join(base_dir, "src", "adippadai", "tharavuthalam", db_file)
    with open(path, "r", encoding="utf-8") as f:
        content = f.read()
    
    # We want to keep the FIRST occurrence in each table class, but remove subsequent ones.
    # Actually, deletedAt should only appear ONCE per table class.
    # Let's just blindly remove exact duplicates within the file? No, it's defined once in each class.
    # Oh, wait. The error says "deletedAt' is already declared in this scope". This means within the SAME class, deletedAt is written multiple times.
    # Let's fix this using regex.
    # Find all class definitions and remove duplicate deletedAt lines inside them.
    classes = content.split("class ")
    for i in range(1, len(classes)):
        lines = classes[i].split("\n")
        new_lines = []
        seen_deleted = False
        for line in lines:
            if "DateTimeColumn get deletedAt => dateTime().nullable()();" in line:
                if not seen_deleted:
                    new_lines.append(line)
                    seen_deleted = True
            else:
                new_lines.append(line)
        classes[i] = "\n".join(new_lines)
    
    with open(path, "w", encoding="utf-8") as f:
        f.write("class ".join(classes))

# 4. Add flutter_riverpod to providers
for prov_file in ["pattu_niruvana_tharavugal_provider.dart", "kooli_niruvana_tharavugal_provider.dart", "niruvana_tharavugal_provider.dart"]:
    path = os.path.join(base_dir, "src", "cheyalpaadugal", "amaippugal", "tharavu", prov_file)
    with open(path, "r", encoding="utf-8") as f:
        content = f.read()
    if "import 'package:flutter_riverpod/flutter_riverpod.dart';" not in content:
        content = "import 'package:flutter_riverpod/flutter_riverpod.dart';\n" + content
    # For niruvana_tharavugal_provider.dart, add seyali_murai.dart
    if prov_file == "niruvana_tharavugal_provider.dart":
        if "import '../../../adippadai/tharavuru/seyali_murai.dart';" not in content:
            content = "import '../../../adippadai/tharavuru/seyali_murai.dart';\n" + content
    with open(path, "w", encoding="utf-8") as f:
        f.write(content)

# 5. Fix vaangunar_nilaimai.dart
vaangunar = os.path.join(base_dir, "src", "cheyalpaadugal", "niril_podhu", "kalanjiyam", "vaangunar_nilaimai.dart")
with open(vaangunar, "r", encoding="utf-8") as f:
    content = f.read()
if "import 'package:flutter_riverpod/flutter_riverpod.dart';" not in content:
    content = "import 'package:flutter_riverpod/flutter_riverpod.dart';\n" + content
with open(vaangunar, "w", encoding="utf-8") as f:
    f.write(content)

print("Batch 1 completed.")
