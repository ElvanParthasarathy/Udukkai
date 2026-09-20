import os

base_dir = r"D:\Projects\Elvan Niril\flutter\lib"

# 1. Check pattu_vaangunar_kalanjiyam.dart
path_pattu_vaangunar = os.path.join(base_dir, "src", "cheyalpaadugal", "niril_podhu", "kalanjiyam", "pattu_vaangunar_kalanjiyam.dart")
with open(path_pattu_vaangunar, "r", encoding="utf-8") as f:
    content = f.read()
# Let's fix the map casts.
content = content.replace("peyar: Map<String, String>.from(entry.peyar),", "peyar: (entry.peyar as Map).cast<String, String>(),")
content = content.replace("mugavari: Map<String, String>.from(entry.mugavari),", "mugavari: (entry.mugavari as Map).cast<String, String>(),")
content = content.replace("oor: Map<String, String>.from(entry.oor),", "oor: (entry.oor as Map).cast<String, String>(),")
content = content.replace("maavattam: Map<String, String>.from(entry.maavattam),", "maavattam: (entry.maavattam as Map).cast<String, String>(),")
content = content.replace("maanilam: Map<String, String>.from(entry.maanilam),", "maanilam: (entry.maanilam as Map).cast<String, String>(),")
content = content.replace("naadu: Map<String, String>.from(entry.naadu),", "naadu: (entry.naadu as Map).cast<String, String>(),")
content = content.replace("velinaadMugavari: Map<String, String>.from(entry.velinaadMugavari),", "velinaadMugavari: (entry.velinaadMugavari as Map).cast<String, String>(),")

# Fix simpler Map cast instead of .from
content = content.replace("peyar: entry.peyar as Map<String, String>,", "peyar: Map<String, String>.from(entry.peyar as Map),")
content = content.replace("mugavari: entry.mugavari as Map<String, String>,", "mugavari: Map<String, String>.from(entry.mugavari as Map),")
content = content.replace("oor: entry.oor as Map<String, String>,", "oor: Map<String, String>.from(entry.oor as Map),")
content = content.replace("maavattam: entry.maavattam as Map<String, String>,", "maavattam: Map<String, String>.from(entry.maavattam as Map),")
content = content.replace("maanilam: entry.maanilam as Map<String, String>,", "maanilam: Map<String, String>.from(entry.maanilam as Map),")
content = content.replace("naadu: entry.naadu as Map<String, String>,", "naadu: Map<String, String>.from(entry.naadu as Map),")
content = content.replace("velinaadMugavari: entry.velinaadMugavari as Map<String, String>,", "velinaadMugavari: Map<String, String>.from(entry.velinaadMugavari as Map),")

# And what if it's just `entry.peyar` without cast?
content = content.replace("peyar: entry.peyar,", "peyar: Map<String, String>.from(entry.peyar as Map),")
content = content.replace("mugavari: entry.mugavari,", "mugavari: Map<String, String>.from(entry.mugavari as Map),")
content = content.replace("oor: entry.oor,", "oor: Map<String, String>.from(entry.oor as Map),")
content = content.replace("maavattam: entry.maavattam,", "maavattam: Map<String, String>.from(entry.maavattam as Map),")
content = content.replace("maanilam: entry.maanilam,", "maanilam: Map<String, String>.from(entry.maanilam as Map),")
content = content.replace("naadu: entry.naadu,", "naadu: Map<String, String>.from(entry.naadu as Map),")
content = content.replace("velinaadMugavari: entry.velinaadMugavari,", "velinaadMugavari: Map<String, String>.from(entry.velinaadMugavari as Map),")

# Revert any double cast
content = content.replace("Map<String, String>.from(Map<String, String>.from(entry.peyar as Map) as Map)", "Map<String, String>.from(entry.peyar as Map)")

with open(path_pattu_vaangunar, "w", encoding="utf-8") as f:
    f.write(content)

# 2. Fix sodhanai_tharavu_uruvakki.dart imports again
sodhanai_path = os.path.join(base_dir, "src", "cheyalpaadugal", "uruvakkunar_karuvigal", "tharavu", "sodhanai_tharavu_uruvakki.dart")
with open(sodhanai_path, "r", encoding="utf-8") as f:
    content = f.read()

# Add explicit paths because maybe relative path was wrong
if "import '../../amaippugal/tharavu/pattu_niruvana_tharavugal_provider.dart';" in content:
    content = content.replace("import '../../amaippugal/tharavu/pattu_niruvana_tharavugal_provider.dart';", "import '../../../cheyalpaadugal/amaippugal/tharavu/pattu_niruvana_tharavugal_provider.dart';")
    content = content.replace("import '../../amaippugal/tharavu/kooli_niruvana_tharavugal_provider.dart';", "import '../../../cheyalpaadugal/amaippugal/tharavu/kooli_niruvana_tharavugal_provider.dart';")

with open(sodhanai_path, "w", encoding="utf-8") as f:
    f.write(content)

# 3. Fix restore_thirai.dart undefined providers
restore_path = os.path.join(base_dir, "src", "cheyalpaadugal", "ulnuzhaivu", "kaatchi", "thiraigal", "restore_thirai.dart")
with open(restore_path, "r", encoding="utf-8") as f:
    content = f.read()

if "import '../../../amaippugal/tharavu/pattu_niruvana_tharavugal_provider.dart';" in content:
    content = content.replace("import '../../../amaippugal/tharavu/pattu_niruvana_tharavugal_provider.dart';", "import '../../amaippugal/tharavu/pattu_niruvana_tharavugal_provider.dart';")
    content = content.replace("import '../../../amaippugal/tharavu/kooli_niruvana_tharavugal_provider.dart';", "import '../../amaippugal/tharavu/kooli_niruvana_tharavugal_provider.dart';")

with open(restore_path, "w", encoding="utf-8") as f:
    f.write(content)

print("Done fixing minor things")
