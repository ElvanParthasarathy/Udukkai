import os

base_dir = r"D:\Projects\Elvan Niril\flutter\lib"

# Fix restore_thirai.dart
restore_path = os.path.join(base_dir, "src", "cheyalpaadugal", "ulnuzhaivu", "kaatchi", "thiraigal", "restore_thirai.dart")
with open(restore_path, "r", encoding="utf-8") as f:
    content = f.read()

imports = """import '../../../amaippugal/tharavu/pattu_niruvana_tharavugal_provider.dart';
import '../../../amaippugal/tharavu/kooli_niruvana_tharavugal_provider.dart';
"""
if "pattu_niruvana_tharavugal_provider.dart" not in content:
    content = imports + content

with open(restore_path, "w", encoding="utf-8") as f:
    f.write(content)


# Fix sodhanai_tharavu_uruvakki.dart
sodhanai_path = os.path.join(base_dir, "src", "cheyalpaadugal", "uruvakkunar_karuvigal", "tharavu", "sodhanai_tharavu_uruvakki.dart")
with open(sodhanai_path, "r", encoding="utf-8") as f:
    content = f.read()

imports2 = """import '../../amaippugal/tharavu/pattu_niruvana_tharavugal_provider.dart';
import '../../amaippugal/tharavu/kooli_niruvana_tharavugal_provider.dart';
"""
if "pattu_niruvana_tharavugal_provider.dart" not in content:
    content = imports2 + content

with open(sodhanai_path, "w", encoding="utf-8") as f:
    f.write(content)

# Fix vaangunar_nilaimai.dart missing StateProvider
vaangunar_path = os.path.join(base_dir, "src", "cheyalpaadugal", "niril_podhu", "kalanjiyam", "vaangunar_nilaimai.dart")
with open(vaangunar_path, "r", encoding="utf-8") as f:
    content = f.read()
if "import 'package:flutter_riverpod/flutter_riverpod.dart';" not in content:
    content = "import 'package:flutter_riverpod/flutter_riverpod.dart';\n" + content
with open(vaangunar_path, "w", encoding="utf-8") as f:
    f.write(content)

print("Done")
