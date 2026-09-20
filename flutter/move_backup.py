import os
import re

base_path = r'D:\Projects\Elvan Niril\flutter\lib\src\cheyalpaadugal\amaippugal\kaatchi'
pagudhi_path = os.path.join(base_path, 'koorugal', 'kanakku_paadhugaappu_pagudhi.dart')
thaekkagam_path = os.path.join(base_path, 'koorugal', 'thaekkagam_pagudhi.dart')
thirai_path = os.path.join(base_path, 'thiraigal', 'thaekkagam_amaippugal_thirai.dart')

# 1. Read kanakku_paadhugaappu_pagudhi.dart
with open(pagudhi_path, 'r', encoding='utf-8') as f:
    pagudhi_content = f.read()

# Extract the Backup Data row and the method
backup_row_match = re.search(r'        ElvanSettingsRow\(\s*iconWidget: Icon\(\s*CupertinoIcons\.cloud_upload_fill.*?_showBackupFlow.*?,\s*\),', pagudhi_content, re.MULTILINE | re.DOTALL)
backup_method_match = re.search(r'  void _showBackupFlow\(.*?\n  \}\n', pagudhi_content, re.MULTILINE | re.DOTALL)

if backup_row_match and backup_method_match:
    backup_row = backup_row_match.group(0)
    backup_method = backup_method_match.group(0)

    # Remove from kanakku
    pagudhi_content = pagudhi_content.replace(backup_row, '')
    pagudhi_content = pagudhi_content.replace(backup_method, '')

    with open(pagudhi_path, 'w', encoding='utf-8') as f:
        f.write(pagudhi_content)

    # 2. Add to thaekkagam_pagudhi.dart
    with open(thaekkagam_path, 'r', encoding='utf-8') as f:
        thaekkagam_content = f.read()

    # Add necessary imports
    imports_to_add = """
import '../../../../adippadai/mozhiyaakkam/k.dart';
import '../../../../koorugal/ulleedugal/elvan_ulleedu.dart';
import '../../../../koorugal/maeladukkugal/elvan_cheyal_maeladukku.dart';
import '../../../../koorugal/maeladukkugal/elvan_aetrum_maeladukku.dart';
import '../../../../koorugal/podhu_koorugal/elvan_siruseidhi.dart';
"""
    if 'k.dart' not in thaekkagam_content:
        thaekkagam_content = thaekkagam_content.replace("import 'elvan_amaippu_pagudhi.dart';", "import 'elvan_amaippu_pagudhi.dart';\n" + imports_to_add)

    actions_section = f'''
class StorageActionsSection extends ConsumerWidget {{
  const StorageActionsSection({{super.key}});

  @override
  Widget build(BuildContext context, WidgetRef ref) {{
    final theme = Theme.of(context);
    final iconBgColor = theme.colorScheme.onSurface.withValues(alpha: 0.05);

    return ElvanSettingsSection(
      children: [
        ElvanSettingsRow(
          iconWidget: Icon(
            CupertinoIcons.cloud_upload_fill,
            color: theme.colorScheme.onSurface,
            size: 20,
          ),
          iconBgColor: iconBgColor,
          title: K.tharavuKaappuChei.tr(context, ref),
          description: K.ungalTharavaiChaemikkavum.tr(context, ref),
          onTap: () {{
            _showBackupFlow(context, ref);
          }},
        ),
      ],
    );
  }}

{backup_method}
}}
'''
    thaekkagam_content += actions_section
    with open(thaekkagam_path, 'w', encoding='utf-8') as f:
        f.write(thaekkagam_content)

    # 3. Add to thaekkagam_amaippugal_thirai.dart
    with open(thirai_path, 'r', encoding='utf-8') as f:
        thirai_content = f.read()

    if 'const StorageActionsSection()' not in thirai_content:
        thirai_content = thirai_content.replace(
            'const StorageManagerSection(),',
            'const StorageManagerSection(),\n              const SizedBox(height: 24),\n              const StorageActionsSection(),'
        )

    with open(thirai_path, 'w', encoding='utf-8') as f:
        f.write(thirai_content)

    print("Backup Data moved successfully.")
else:
    print("Could not find Backup Data row or method in kanakku.")
