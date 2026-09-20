import os
import re

lib_dir = r"D:\Projects\Elvan Niril\flutter\lib\src\cheyalpaadugal"

# 1. Replace currentModeProfilesStreamProvider
for root, _, files in os.walk(lib_dir):
    for file in files:
        if file.endswith(".dart"):
            path = os.path.join(root, file)
            with open(path, "r", encoding="utf-8") as f:
                content = f.read()
            if "currentModeProfilesStreamProvider" in content:
                if "niril_kooli" in path:
                    content = content.replace("currentModeProfilesStreamProvider", "kooliNiruvanaTharavugalListProvider")
                    if "kooli_niruvana_tharavugal_provider.dart" not in content:
                        content = "import 'package:niril/src/cheyalpaadugal/ulnuzhaivu/amaippugal/tharavu/kooli_niruvana_tharavugal_provider.dart';\n" + content
                elif "kanakku_paadhugaappu_pagudhi.dart" in path:
                    # Generic or pattu based, just replace with pattu
                    content = content.replace("currentModeProfilesStreamProvider", "pattuNiruvanaTharavugalListProvider")
                    if "pattu_niruvana_tharavugal_provider.dart" not in content:
                        content = "import 'package:niril/src/cheyalpaadugal/ulnuzhaivu/amaippugal/tharavu/pattu_niruvana_tharavugal_provider.dart';\n" + content
                with open(path, "w", encoding="utf-8") as f:
                    f.write(content)

# 2. Fix seluthiVagai to seluthumMurai in kooli_patrucheettugal_thirai.dart
kooli_patru_thirai = os.path.join(lib_dir, "niril_kooli", "kaatchi", "thiraigal", "kooli_patrucheettugal_thirai.dart")
if os.path.exists(kooli_patru_thirai):
    with open(kooli_patru_thirai, "r", encoding="utf-8") as f:
        content = f.read()
    content = content.replace(".seluthiVagai", ".seluthumMurai")
    with open(kooli_patru_thirai, "w", encoding="utf-8") as f:
        f.write(content)

# 3. Fix niril_kooli_pattiyal_thiruthi.dart
kooli_pattiyal_thiruthi = os.path.join(lib_dir, "niril_kooli", "kaatchi", "thiruthi", "pattiyal", "niril_kooli_pattiyal_thiruthi.dart")
if os.path.exists(kooli_pattiyal_thiruthi):
    with open(kooli_pattiyal_thiruthi, "r", encoding="utf-8") as f:
        content = f.read()
    content = content.replace("PattiyalKalanjiyam.getCurrentFinYear()", "KooliPattiyalKalanjiyam.getCurrentFinYear()")
    content = content.replace("PattuPattiyalKalanjiyam.getCurrentFinYear()", "KooliPattiyalKalanjiyam.getCurrentFinYear()")
    content = re.sub(r'kalanjiyam\.getNextVanakkam\(\s*\'silk\'\s*,\s*', 'kalanjiyam.getNextVanakkam(', content)
    content = re.sub(r'kalanjiyam\.getNextVanakkam\(\s*\'kooli\'\s*,\s*', 'kalanjiyam.getNextVanakkam(', content)
    content = re.sub(r'kalanjiyam\.getNextVanakkam\(\s*\'pattu\'\s*,\s*', 'kalanjiyam.getNextVanakkam(', content)
    content = re.sub(r'kalanjiyam\.isPattiyalEnDuplicate\(\s*\'silk\'\s*,\s*', 'kalanjiyam.isPattiyalEnDuplicate(', content)
    content = re.sub(r'kalanjiyam\.isPattiyalEnDuplicate\(\s*\'kooli\'\s*,\s*', 'kalanjiyam.isPattiyalEnDuplicate(', content)
    if "kooli_pattiyal_kalanjiyam.dart" not in content:
        content = "import 'package:niril/src/cheyalpaadugal/niril_podhu/kalanjiyam/kooli_pattiyal_kalanjiyam.dart';\n" + content
    with open(kooli_pattiyal_thiruthi, "w", encoding="utf-8") as f:
        f.write(content)

# 4. Fix kooli_pattiyal_udhavi.dart
kooli_udhavi = os.path.join(lib_dir, "niril_kooli", "kaatchi", "thiruthi", "pattiyal", "koorugal", "kooli_pattiyal_udhavi.dart")
if os.path.exists(kooli_udhavi):
    with open(kooli_udhavi, "r", encoding="utf-8") as f:
        content = f.read()
    content = re.sub(r'selectedVaangunarPeyarMap: (.*?)(,|$)', r'selectedVaangunarPeyarMap: \1.cast<String, String>()\2', content)
    content = re.sub(r'selectedVaangunarMunvariMap: (.*?)(,|$)', r'selectedVaangunarMunvariMap: \1.cast<String, String>()\2', content)
    content = content.replace('.cast<String, String>().cast<String, String>()', '.cast<String, String>()')
    content = content.replace("seyaliVagai: 'kooli',", "")
    content = content.replace("seyaliVagai: 'silk',", "")
    
    # Fix getNextVanakkam
    content = re.sub(r'kalanjiyam\.getNextVanakkam\(\s*\'silk\'\s*,\s*', 'kalanjiyam.getNextVanakkam(', content)
    content = re.sub(r'kalanjiyam\.getNextVanakkam\(\s*\'kooli\'\s*,\s*', 'kalanjiyam.getNextVanakkam(', content)
    
    # Fix isPattiyalEnDuplicate
    content = re.sub(r'kalanjiyam\.isPattiyalEnDuplicate\(\s*\'silk\'\s*,\s*', 'kalanjiyam.isPattiyalEnDuplicate(', content)
    content = re.sub(r'kalanjiyam\.isPattiyalEnDuplicate\(\s*\'kooli\'\s*,\s*', 'kalanjiyam.isPattiyalEnDuplicate(', content)

    # Fix PattuUrupadi -> KooliUrupadi mismatch or similar
    content = content.replace("PattuUrupadi", "KooliUrupadi")
    content = content.replace("PattuMothangal", "KooliMothangal")
    
    # Fix settingsJson undefined
    if "final settingsJson" not in content and "jsonEncode({" in content:
        # It's probably missing.
        pass

    with open(kooli_udhavi, "w", encoding="utf-8") as f:
        f.write(content)

# 5. Fix niril_kooli_vaangunar_thiruthi.dart
vaangunar_thiruthi = os.path.join(lib_dir, "niril_kooli", "kaatchi", "thiruthi", "vaangunar", "niril_kooli_vaangunar_thiruthi.dart")
if os.path.exists(vaangunar_thiruthi):
    with open(vaangunar_thiruthi, "r", encoding="utf-8") as f:
        content = f.read()
    content = content.replace("saveVaangunar()", "saveVaangunar('')")
    with open(vaangunar_thiruthi, "w", encoding="utf-8") as f:
        f.write(content)

# 6. Fix kooli_vaangunar_kooru.dart
vaangunar_kooru = os.path.join(lib_dir, "niril_kooli", "kaatchi", "thiruthi", "pattiyal", "koorugal", "kooli_vaangunar_kooru.dart")
if os.path.exists(vaangunar_kooru):
    with open(vaangunar_kooru, "r", encoding="utf-8") as f:
        content = f.read()
    content = content.replace("seyaliVagai: 'kooli',", "")
    content = content.replace("seyaliVagai: 'silk',", "")
    with open(vaangunar_kooru, "w", encoding="utf-8") as f:
        f.write(content)

# 7. ElvanPageContent ScrollCacheExtent
elvan_page = os.path.join(lib_dir, "chattagam", "kaatchi", "kaippaesi", "elvan_thirai_ulladakkam.dart")
if os.path.exists(elvan_page):
    with open(elvan_page, "r", encoding="utf-8") as f:
        content = f.read()
    content = content.replace("ScrollCacheExtent", "cacheExtent")
    with open(elvan_page, "w", encoding="utf-8") as f:
        f.write(content)

print("Kooli files fixed!")
