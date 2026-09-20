import os
import re

base_dir = r"D:\Projects\Elvan Niril\flutter\lib\src\cheyalpaadugal"

replacements = {
    "PatrucheettuEntry": "PattiyalTharavuru",
    "PorulEntry": "PorulTharavuru",
    "VaangunarEntry": "VaangunarTharavuru",
    "PatrugalEntry": "PatrugalTharavuru",
    "PatrugalTableCompanion": "PatrugalTharavuru", # In the editor it was creating a companion to save
    "PatrucheettuTableCompanion": "PattiyalTharavuru",
}

for root, _, files in os.walk(base_dir):
    for filename in files:
        if filename.endswith(".dart"):
            filepath = os.path.join(root, filename)
            with open(filepath, "r", encoding="utf-8") as f:
                content = f.read()
            
            modified = False
            for old, new in replacements.items():
                if old in content:
                    # Don't replace if it's imported from seyali_tharavuthalam.dart maybe? 
                    # Wait, if we replace all occurrences, we might break some Drift specific things if it was used legitimately.
                    # But in the UI layer, it shouldn't be used legitimately anymore!
                    # The only place it's legitimate is in the DB and Kalanjiyam layers.
                    if "kaatchi" in filepath or "sodhanai_tharavu_uruvakki" in filepath:
                        content = content.replace(old, new)
                        modified = True
            
            if modified:
                # Add domain models import if not present
                if "import '../../../../adippadai/tharavuru/uruvugal.dart';" not in content and "import 'package:elvan_niril/src/adippadai/tharavuru/uruvugal.dart';" not in content:
                    content = "import 'package:elvan_niril/src/adippadai/tharavuru/uruvugal.dart';\n" + content
                with open(filepath, "w", encoding="utf-8") as f:
                    f.write(content)

print("UI type references updated.")
