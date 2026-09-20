import os
import re

base_path = r'D:\Projects\Elvan Niril\flutter\lib\src\cheyalpaadugal\amaippugal\kaatchi\koorugal'
amaippu_pagudhi_path = os.path.join(base_path, 'elvan_amaippu_pagudhi.dart')
thaekkagam_path = os.path.join(base_path, 'thaekkagam_pagudhi.dart')

# 1. Update elvan_amaippu_pagudhi.dart
with open(amaippu_pagudhi_path, 'r', encoding='utf-8') as f:
    amaippu_content = f.read()

amaippu_content = amaippu_content.replace(
    'this.customDescription,\n    this.onTap,\n  })',
    'this.customDescription,\n    this.crossAxisAlignment,\n    this.onTap,\n  })'
)

amaippu_content = amaippu_content.replace(
    'final Widget? customDescription;\n  final VoidCallback? onTap;',
    'final Widget? customDescription;\n  final CrossAxisAlignment? crossAxisAlignment;\n  final VoidCallback? onTap;'
)

amaippu_content = amaippu_content.replace(
    '        child: Row(\n          children: [',
    '        child: Row(\n          crossAxisAlignment: crossAxisAlignment ?? CrossAxisAlignment.center,\n          children: ['
)

with open(amaippu_pagudhi_path, 'w', encoding='utf-8') as f:
    f.write(amaippu_content)

# 2. Update thaekkagam_pagudhi.dart
with open(thaekkagam_path, 'r', encoding='utf-8') as f:
    thaekkagam_content = f.read()

thaekkagam_content = thaekkagam_content.replace(
    "title: 'Coolie Data',",
    "title: 'Coolie Data',\n                      crossAxisAlignment: CrossAxisAlignment.start,"
)

thaekkagam_content = thaekkagam_content.replace(
    "title: 'Silk Data',",
    "title: 'Silk Data',\n                      crossAxisAlignment: CrossAxisAlignment.start,"
)

with open(thaekkagam_path, 'w', encoding='utf-8') as f:
    f.write(thaekkagam_content)

print("Icon alignment updated successfully.")
