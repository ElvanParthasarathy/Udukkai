import os
import re

files = [
    r"lib/src/cheyalpaadugal/niril_kooli/kaatchi/thiruthi/porul/niril_kooli_porul_thiruthi.dart",
    r"lib/src/cheyalpaadugal/niril_kooli/kaatchi/thiruthi/vaangunar/niril_kooli_vaangunar_thiruthi.dart",
    r"lib/src/cheyalpaadugal/niril_pattu/kaatchi/thiruthi/porul/niril_pattu_porul_thiruthi.dart",
    r"lib/src/cheyalpaadugal/niril_pattu/kaatchi/thiruthi/vaangunar/niril_pattu_vaangunar_thiruthi.dart",
    r"lib/src/cheyalpaadugal/niril_podhu/kaatchi/thiraigal/meetpagam_thirai.dart",
]

pattern = re.compile(r"ScaffoldMessenger\.of\(context\)\.showSnackBar\(\s*SnackBar\(\s*content:\s*Text\((.*?)\),\s*behavior:\s*SnackBarBehavior\.floating,\s*\),\s*\);", re.DOTALL)

for filepath in files:
    if not os.path.exists(filepath):
        continue
    with open(filepath, 'r', encoding='utf-8') as f:
        content = f.read()
    
    new_content = pattern.sub(r"ElvanSnackbar.show(context, \1);", content)
    
    if "ElvanSnackbar" in new_content and "elvan_siruseidhi" not in new_content:
        new_content = "import 'package:elvan_niril/src/koorugal/podhu_koorugal/elvan_siruseidhi.dart';\n" + new_content

    with open(filepath, 'w', encoding='utf-8') as f:
        f.write(new_content)
print("Done")
