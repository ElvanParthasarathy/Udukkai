import os
import re

base_dir = r"D:\Projects\Elvan Niril\flutter\lib"

# 1. Add providers to seyali_nilaimai.dart
seyali_nilaimai_path = os.path.join(base_dir, "src", "adippadai", "nilaimai", "seyali_nilaimai.dart")
with open(seyali_nilaimai_path, "r", encoding="utf-8") as f:
    content = f.read()

if "final pattuDatabaseProvider" not in content:
    imports = """import '../tharavuthalam/seyali_tharavuthalam.dart';
import '../tharavuthalam/pattu_tharavuthalam.dart';
import '../tharavuthalam/kooli_tharavuthalam.dart';
"""
    content = content.replace("import 'package:flutter_riverpod/flutter_riverpod.dart';", 
                              "import 'package:flutter_riverpod/flutter_riverpod.dart';\n" + imports)
    
    providers = """

final appDatabaseProvider = Provider<AppDatabase>((ref) {
  final db = AppDatabase(AppDatabase.openConnection('elvan_niril.db'));
  ref.onDispose(db.close);
  return db;
});

final pattuDatabaseProvider = Provider<PattuDatabase>((ref) {
  final db = PattuDatabase(PattuDatabase.openConnection('elvan_niril_silk.db'));
  ref.onDispose(db.close);
  return db;
});

final kooliDatabaseProvider = Provider<KooliDatabase>((ref) {
  final db = KooliDatabase(KooliDatabase.openConnection('elvan_niril_coolie.db'));
  ref.onDispose(db.close);
  return db;
});
"""
    content += providers
    with open(seyali_nilaimai_path, "w", encoding="utf-8") as f:
        f.write(content)


# 2. Fix sodhanai_tharavu_uruvakki.dart Type mismatch (String -> Map<String,dynamic>)
sodhanai_path = os.path.join(base_dir, "src", "cheyalpaadugal", "uruvakkunar_karuvigal", "tharavu", "sodhanai_tharavu_uruvakki.dart")
with open(sodhanai_path, "r", encoding="utf-8") as f:
    content = f.read()

# Replace empty JSON string with empty Map for mugavari, peyar, etc.
# The error was: The argument type 'String' can't be assigned to the parameter type 'Map<String, dynamic>'.
content = re.sub(r'niruvanamPeyar: \'{}\'', r'niruvanamPeyar: {}', content)
content = re.sub(r'mugavari: \'{}\'', r'mugavari: {}', content)
content = re.sub(r'vangiTharavugal: \'{}\'', r'vangiTharavugal: {}', content)
content = re.sub(r'oavuruTharavugal: \'{}\'', r'oavuruTharavugal: {}', content)
content = re.sub(r'upiTharavugal: \'{}\'', r'upiTharavugal: {}', content)
content = re.sub(r'peyar: \'{}\'', r'peyar: {}', content)
content = re.sub(r'oor: \'{}\'', r'oor: {}', content)
content = re.sub(r'maavattam: \'{}\'', r'maavattam: {}', content)
content = re.sub(r'maanilam: \'{}\'', r'maanilam: {}', content)
content = re.sub(r'naadu: \'{}\'', r'naadu: {}', content)
content = re.sub(r'velinaadMugavari: \'{}\'', r'velinaadMugavari: {}', content)
content = re.sub(r'vaangunarPeyar: \'{}\'', r'vaangunarPeyar: {}', content)
content = re.sub(r'vaangunarMunvari: \'{}\'', r'vaangunarMunvari: {}', content)

# Fix double casting issue: The argument type 'num' can't be assigned to the parameter type 'double'
content = content.replace("poruthiyaThogai: item['totalAmount'] as num,", "poruthiyaThogai: (item['totalAmount'] as num).toDouble(),")

# Fix missing pattuDatabaseProvider imports
if "import '../../../adippadai/nilaimai/seyali_nilaimai.dart';" not in content:
    content = "import '../../../adippadai/nilaimai/seyali_nilaimai.dart';\n" + content

with open(sodhanai_path, "w", encoding="utf-8") as f:
    f.write(content)


# 3. Fix unused imports and variables in restore_thirai, vanakkam_thirai, etc.
vanakkam_path = os.path.join(base_dir, "src", "cheyalpaadugal", "ulnuzhaivu", "kaatchi", "thiraigal", "vanakkam_thirai.dart")
if os.path.exists(vanakkam_path):
    with open(vanakkam_path, "r", encoding="utf-8") as f:
        content = f.read()
    content = content.replace("import '../../../amaippugal/tharavu/niruvana_tharavugal_provider.dart';", "")
    content = content.replace("final language = ref.watch(localeProvider).languageCode;", "")
    with open(vanakkam_path, "w", encoding="utf-8") as f:
        f.write(content)

pattiyal_mozhi_path = os.path.join(base_dir, "src", "cheyalpaadugal", "ulnuzhaivu", "kaatchi", "thiraigal", "varavaerpu_padigal", "pattiyal_mozhi_padi.dart")
if os.path.exists(pattiyal_mozhi_path):
    with open(pattiyal_mozhi_path, "r", encoding="utf-8") as f:
        content = f.read()
    content = content.replace("final currentLang = ref.watch(primaryLanguageProvider);", "")
    with open(pattiyal_mozhi_path, "w", encoding="utf-8") as f:
        f.write(content)

menu_path = os.path.join(base_dir, "src", "cheyalpaadugal", "uruvakkunar_karuvigal", "kaatchi", "elvan_uruvakkunar_menu.dart")
if os.path.exists(menu_path):
    with open(menu_path, "r", encoding="utf-8") as f:
        content = f.read()
    content = content.replace("import '../../ulnuzhaivu/kaatchi/koorugal/ullnuzhaivu_koorugal.dart';", "")
    with open(menu_path, "w", encoding="utf-8") as f:
        f.write(content)

print("Fixes applied successfully!")
