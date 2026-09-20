import os

base_dir = r"D:\Projects\Elvan Niril\flutter\lib"

udhavi_files = [
    os.path.join(base_dir, "src", "cheyalpaadugal", "niril_pattu", "kaatchi", "thiruthi", "pattiyal", "koorugal", "pattu_pattiyal_udhavi.dart"),
    os.path.join(base_dir, "src", "cheyalpaadugal", "niril_kooli", "kaatchi", "thiruthi", "pattiyal", "koorugal", "kooli_pattiyal_udhavi.dart")
]

for file_path in udhavi_files:
    if os.path.exists(file_path):
        with open(file_path, "r", encoding="utf-8") as f:
            content = f.read()
        
        # Replace bad extensions import if any.
        # Let's just find "import '.*extensions.dart';" and replace with the correct one.
        # Actually it's probably "import '../../../../../adippadai/mozhiyaakkam/extensions.dart';" instead of 6 dirs up.
        # src/cheyalpaadugal/niril_pattu/kaatchi/thiruthi/pattiyal/koorugal -> 6 levels to src
        # ../../../../../../adippadai/mozhiyaakkam/extensions.dart
        
        import re
        content = re.sub(r"import\s+'[^']*extensions\.dart';", "import '../../../../../../adippadai/mozhiyaakkam/extensions.dart';", content)
        
        with open(file_path, "w", encoding="utf-8") as f:
            f.write(content)

print("Extensions import fixed.")
