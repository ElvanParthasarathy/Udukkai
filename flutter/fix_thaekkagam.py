import os
import re

base_path = r'D:\Projects\Elvan Niril\flutter\lib\src\cheyalpaadugal\amaippugal\koorugal'
file_path = r'D:\Projects\Elvan Niril\flutter\lib\src\cheyalpaadugal\amaippugal\kaatchi\koorugal\thaekkagam_pagudhi.dart'

with open(file_path, 'r', encoding='utf-8') as f:
    content = f.read()

# 1. Add missing imports
imports = """
import 'package:elvan_niril/src/adippadai/mozhiyaakkam/k.dart';
import '../../../../adippadai/mozhiyaakkam/mozhi_vazhanguthi.dart';
import '../../../../adippadai/thoatra_vazhanguthi.dart';
"""
content = content.replace("import 'elvan_amaippu_pagudhi.dart';", "import 'elvan_amaippu_pagudhi.dart';\n" + imports)

# 2. Fix 'const' issues on lines 115 and 126 (StorageManagerSection)
content = re.sub(r'iconWidget:\s*const\s*Icon\(', 'iconWidget: Icon(', content)
content = content.replace('iconBgColor: Colors.blue.withValues(alpha: 0.05)', 'iconBgColor: theme.colorScheme.onSurface.withValues(alpha: 0.05)')
content = content.replace('iconBgColor: Colors.purple.withValues(alpha: 0.05)', 'iconBgColor: theme.colorScheme.onSurface.withValues(alpha: 0.05)')
content = content.replace('iconBgColor: theme.colorScheme.onSurface.withValues(alpha: 0.05),', 'iconBgColor: theme.colorScheme.onSurface.withValues(alpha: 0.05),')

with open(file_path, 'w', encoding='utf-8') as f:
    f.write(content)

print("Fixed thaekkagam_pagudhi.dart")
