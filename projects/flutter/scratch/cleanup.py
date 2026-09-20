import os

base_dir = r"D:\Projects\Elvan Niril\flutter\lib"
path = os.path.join(base_dir, "src", "cheyalpaadugal", "amaippugal", "kaatchi", "koorugal", "kanakku_paadhugaappu_pagudhi.dart")

with open(path, "r", encoding="utf-8") as f:
    content = f.read()

# Replace the wipe logic
old_wipe_logic = """              // 1. Wipe Silk Database
              ref.read(appModeProvider.notifier).setMode(AppMode.silk);
              await Future.delayed(const Duration(milliseconds: 100));
              var db = ref.read(appDatabaseProvider);
              await db.delete(db.niruvanaTharavugalTable).go();
              await db.delete(db.porulTable).go();
              await db.delete(db.vaangunarTable).go();
              await db.delete(db.patrucheettuTable).go();
              await db.delete(db.patrugalTable).go();
              await db.delete(db.patruPattiyalTable).go();

              // 2. Wipe Coolie Database
              ref.read(appModeProvider.notifier).setMode(AppMode.coolie);
              await Future.delayed(const Duration(milliseconds: 100));
              db = ref.read(appDatabaseProvider);
              await db.delete(db.niruvanaTharavugalTable).go();
              await db.delete(db.porulTable).go();
              await db.delete(db.vaangunarTable).go();
              await db.delete(db.patrucheettuTable).go();
              await db.delete(db.patrugalTable).go();
              await db.delete(db.patruPattiyalTable).go();"""

new_wipe_logic = """              // 1. Wipe Silk Database
              final pattuDb = ref.read(pattuDatabaseProvider);
              await pattuDb.delete(pattuDb.pattuNiruvanaTharavugalTable).go();
              await pattuDb.delete(pattuDb.pattuPorulTable).go();
              await pattuDb.delete(pattuDb.pattuVaangunarTable).go();
              await pattuDb.delete(pattuDb.pattuPatrucheettuTable).go();
              await pattuDb.delete(pattuDb.pattuPatrugalTable).go();
              await pattuDb.delete(pattuDb.pattuPatruPattiyalTable).go();

              // 2. Wipe Coolie Database
              final kooliDb = ref.read(kooliDatabaseProvider);
              await kooliDb.delete(kooliDb.kooliNiruvanaTharavugalTable).go();
              await kooliDb.delete(kooliDb.kooliPorulTable).go();
              await kooliDb.delete(kooliDb.kooliVaangunarTable).go();
              await kooliDb.delete(kooliDb.kooliPatrucheettuTable).go();
              await kooliDb.delete(kooliDb.kooliPatrugalTable).go();
              await kooliDb.delete(kooliDb.kooliPatruPattiyalTable).go();"""

if old_wipe_logic in content:
    content = content.replace(old_wipe_logic, new_wipe_logic)
    
    # Add imports if needed
    imports = "import '../../tharavu/pattu_niruvana_tharavugal_provider.dart';\nimport '../../tharavu/kooli_niruvana_tharavugal_provider.dart';\n"
    if "pattu_niruvana_tharavugal_provider.dart" not in content:
        content = content.replace("import '../../tharavu/niruvana_tharavugal_provider.dart';", "import '../../tharavu/niruvana_tharavugal_provider.dart';\n" + imports)
else:
    print("WARNING: Wipe logic not found exactly as string")

with open(path, "w", encoding="utf-8") as f:
    f.write(content)

# Update elvan_uruvakkunar_menu.dart to remove appDatabaseProvider comment
menu_path = os.path.join(base_dir, "src", "cheyalpaadugal", "uruvakkunar_karuvigal", "kaatchi", "elvan_uruvakkunar_menu.dart")
with open(menu_path, "r", encoding="utf-8") as f:
    menu_content = f.read()

menu_content = menu_content.replace("// Yield to let providers (especially appDatabaseProvider) rebuild", "// Yield to let providers rebuild")
with open(menu_path, "w", encoding="utf-8") as f:
    f.write(menu_content)

print("Cleanup script ready")
