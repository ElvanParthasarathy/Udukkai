import os

base_dir = r"D:\Projects\Elvan Niril\flutter\lib"

# 1. Remove duplicate providers from seyali_nilaimai.dart
seyali_nilaimai_path = os.path.join(base_dir, "src", "adippadai", "nilaimai", "seyali_nilaimai.dart")
with open(seyali_nilaimai_path, "r", encoding="utf-8") as f:
    content = f.read()

content = content.replace("""final appDatabaseProvider = Provider<AppDatabase>((ref) {
  final db = AppDatabase(AppDatabase.openConnection('elvan_niril.db'));
  ref.onDispose(db.close);
  return db;
});""", "")

content = content.replace("""final pattuDatabaseProvider = Provider<PattuDatabase>((ref) {
  final db = PattuDatabase(PattuDatabase.openConnection('elvan_niril_silk.db'));
  ref.onDispose(db.close);
  return db;
});""", "")

content = content.replace("""final kooliDatabaseProvider = Provider<KooliDatabase>((ref) {
  final db = KooliDatabase(KooliDatabase.openConnection('elvan_niril_coolie.db'));
  ref.onDispose(db.close);
  return db;
});""", "")

with open(seyali_nilaimai_path, "w", encoding="utf-8") as f:
    f.write(content)

# 2. Add appDatabaseProvider to seyali_tharavuthalam.dart
seyali_tharavuthalam_path = os.path.join(base_dir, "src", "adippadai", "tharavuthalam", "seyali_tharavuthalam.dart")
with open(seyali_tharavuthalam_path, "r", encoding="utf-8") as f:
    content = f.read()

if "final appDatabaseProvider" not in content:
    content += """

import 'package:flutter_riverpod/flutter_riverpod.dart';
final appDatabaseProvider = Provider<AppDatabase>((ref) {
  final db = AppDatabase(AppDatabase.openConnection('elvan_niril.db'));
  ref.onDispose(db.close);
  return db;
});
"""
    with open(seyali_tharavuthalam_path, "w", encoding="utf-8") as f:
        f.write(content)

# 3. Add Riverpod import to vaangunar_nilaimai.dart
vaangunar_path = os.path.join(base_dir, "src", "cheyalpaadugal", "niril_podhu", "kalanjiyam", "vaangunar_nilaimai.dart")
with open(vaangunar_path, "r", encoding="utf-8") as f:
    content = f.read()
if "import 'package:flutter_riverpod/flutter_riverpod.dart';" not in content:
    content = "import 'package:flutter_riverpod/flutter_riverpod.dart';\n" + content
with open(vaangunar_path, "w", encoding="utf-8") as f:
    f.write(content)

print("Duplicates removed and missing riverpod import added.")
