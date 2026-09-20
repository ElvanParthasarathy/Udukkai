import os
import re

base_dir = r"D:\Projects\Elvan Niril\flutter\lib"
sodhanai_path = os.path.join(base_dir, "src", "cheyalpaadugal", "uruvakkunar_karuvigal", "tharavu", "sodhanai_tharavu_uruvakki.dart")

with open(sodhanai_path, "r", encoding="utf-8") as f:
    content = f.read()

# Fix velinaadMugavari
content = content.replace("velinaadMugavari: ''", "velinaadMugavari: {'Tamil': '', 'English': ''}")

# Fix apply in clamp
content = content.replace("poruthiyaThogai: apply", "poruthiyaThogai: apply.toDouble()")

# Remove unused imports in sodhanai
content = content.replace("import 'package:drift/drift.dart';\n", "")
content = content.replace("import '../../../adippadai/tharavuthalam/pattu_tharavuthalam.dart';\n", "")
content = content.replace("import '../../../adippadai/tharavuthalam/kooli_tharavuthalam.dart';\n", "")
content = content.replace("import '../../niril_podhu/kalanjiyam/patru_kalanjiyam.dart';\n", "")

with open(sodhanai_path, "w", encoding="utf-8") as f:
    f.write(content)

# Fix restore_thirai.dart unused imports
restore_path = os.path.join(base_dir, "src", "cheyalpaadugal", "ulnuzhaivu", "kaatchi", "thiraigal", "restore_thirai.dart")
with open(restore_path, "r", encoding="utf-8") as f:
    content = f.read()

content = content.replace("import '../../../../adippadai/tharavuthalam/pattu_tharavuthalam.dart';\n", "")
content = content.replace("import '../../../../adippadai/tharavuthalam/kooli_tharavuthalam.dart';\n", "")
content = content.replace("import '../../../amaippugal/tharavu/niruvana_tharavugal_provider.dart';\n", "")

with open(restore_path, "w", encoding="utf-8") as f:
    f.write(content)

print("Done")
