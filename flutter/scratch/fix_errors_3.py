import os
import re

base_dir = r"D:\Projects\Elvan Niril\flutter\lib"
migration = os.path.join(base_dir, "src", "adippadai", "tharavuthalam", "migration_udhavi.dart")

with open(migration, "r", encoding="utf-8") as f:
    content = f.read()

# Fix Patrugal mapping errors
content = content.replace("finYear: Value(entry.finYear),", "finYear: Value(entry.patruNaal.year),")
content = content.replace("seluthumMurai: Value(entry.seluthumMurai),", "seluthumMurai: Value(entry.seluthiVagai),")
content = content.replace("vangiPeyar: Value(entry.vangiPeyar),", "")
content = content.replace("parivarthanaiEn: Value(entry.parivarthanaiEn),", "parivarthanaiEn: Value(entry.suttruEn),")
content = content.replace("deletedAt: Value(entry.deletedAt),", "") # removed from Patrugal since it doesn't exist

with open(migration, "w", encoding="utf-8") as f:
    f.write(content)

# Fix Map casts in all kalanjiyam files
kalanjiyam_dir = os.path.join(base_dir, "src", "cheyalpaadugal", "niril_podhu", "kalanjiyam")

def fix_map_casts(file_path):
    with open(file_path, "r", encoding="utf-8") as f:
        content = f.read()
    
    # Example: peyar: entry.peyar as Map<String, String>,
    # Replace with: peyar: Map<String, String>.from(entry.peyar as Map),
    fields = ["peyar", "mugavari", "oor", "maavattam", "maanilam", "naadu", "velinaadMugavari", "vaangunarPeyar", "vaangunarMunvari"]
    for field in fields:
        content = re.sub(
            rf"{field}:\s*entry\.{field}\s+as\s+Map<String,\s*String>,",
            f"{field}: Map<String, String>.from(entry.{field} as Map),",
            content
        )
        content = re.sub(
            rf"{field}:\s*Map<String,\s*dynamic>\.from\(entry\.{field}\),",
            f"{field}: Map<String, String>.from(entry.{field} as Map),",
            content
        )
        content = re.sub(
            rf"{field}:\s*entry\.{field},",
            f"{field}: Map<String, String>.from(entry.{field} as Map),",
            content
        )
        # fix the double cast if any
        content = content.replace(f"Map<String, String>.from(Map<String, String>.from(entry.{field} as Map) as Map)", f"Map<String, String>.from(entry.{field} as Map)")

    with open(file_path, "w", encoding="utf-8") as f:
        f.write(content)

for kalanjiyam_file in os.listdir(kalanjiyam_dir):
    if kalanjiyam_file.endswith(".dart"):
        fix_map_casts(os.path.join(kalanjiyam_dir, kalanjiyam_file))

# Fix currentModeProfilesStreamProvider imports in thiraigal
thiraigal_dirs = [
    os.path.join(base_dir, "src", "cheyalpaadugal", "niril_pattu", "kaatchi", "thiraigal"),
    os.path.join(base_dir, "src", "cheyalpaadugal", "niril_kooli", "kaatchi", "thiraigal")
]

for d in thiraigal_dirs:
    if not os.path.exists(d):
        continue
    for filename in os.listdir(d):
        if filename.endswith(".dart"):
            filepath = os.path.join(d, filename)
            with open(filepath, "r", encoding="utf-8") as f:
                content = f.read()
            if "currentModeProfilesStreamProvider" in content and "niruvana_tharavugal_provider.dart" not in content:
                # Add import depending on depth
                content = "import '../../../amaippugal/tharavu/niruvana_tharavugal_provider.dart';\n" + content
            with open(filepath, "w", encoding="utf-8") as f:
                f.write(content)
                
# And the same for kanakku_paadhugaappu_pagudhi.dart
filepath = os.path.join(base_dir, "src", "cheyalpaadugal", "amaippugal", "kaatchi", "koorugal", "kanakku_paadhugaappu_pagudhi.dart")
if os.path.exists(filepath):
    with open(filepath, "r", encoding="utf-8") as f:
        content = f.read()
    if "currentModeProfilesStreamProvider" in content and "niruvana_tharavugal_provider.dart" not in content:
        content = "import '../../tharavu/niruvana_tharavugal_provider.dart';\n" + content
    with open(filepath, "w", encoding="utf-8") as f:
        f.write(content)

print("Batch 3 completed.")
