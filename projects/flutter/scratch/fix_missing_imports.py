import os

def insert_imports(filepath, imports):
    with open(filepath, 'r', encoding='utf-8') as f:
        content = f.read()
    
    # Add imports after the first import statement
    import_idx = content.find('import ')
    if import_idx != -1:
        end_of_line = content.find('\n', import_idx)
        new_content = content[:end_of_line+1] + '\n'.join(imports) + '\n' + content[end_of_line+1:]
        with open(filepath, 'w', encoding='utf-8') as f:
            f.write(new_content)
        print(f"Added {len(imports)} imports to {filepath}")
    else:
        print(f"Could not find where to insert in {filepath}")

base_dir = r"d:\Projects\Elvan Niril\flutter"

files_to_fix = {
    r"lib\src\cheyalpaadugal\niril_pattu\kaatchi\thiraigal\pattu_patrucheettugal_thirai.dart": [
        "import 'package:flutter_riverpod/flutter_riverpod.dart';",
        "import 'package:flutter/cupertino.dart';"
    ],
    r"lib\src\cheyalpaadugal\niril_pattu\kaatchi\thiruthi\pattiyal\niril_pattu_pattiyal_thiruthi.dart": [
        "import 'package:elvan_niril/src/adippadai/mozhiyaakkam/k.dart';",
        "import 'package:elvan_niril/src/cheyalpaadugal/niril_podhu/tharavuru/pattiyal_tharavuru.dart';",
        "import 'package:elvan_niril/src/cheyalpaadugal/niril_podhu/kaatchi/thiruthi/elvan_thiruthi_niruvanam_oadu.dart';"
    ]
}

for rel_path, imports in files_to_fix.items():
    insert_imports(os.path.join(base_dir, rel_path), imports)

