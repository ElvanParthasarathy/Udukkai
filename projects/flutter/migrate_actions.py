import os
import re

base_path = r'D:\Projects\Elvan Niril\flutter\lib\src\cheyalpaadugal\amaippugal\kaatchi'

pagudhi_path = os.path.join(base_path, 'koorugal', 'kanakku_paadhugaappu_pagudhi.dart')
with open(pagudhi_path, 'r', encoding='utf-8') as f:
    pagudhi_content = f.read()

# 1. Extract the methods from kanakku_paadhugaappu_pagudhi.dart
# We need _showBackupFlow, _showEraseDataFlow, _showSyncFlow
methods_code = ""

backup_match = re.search(r'void _showBackupFlow\(.*?^  }', pagudhi_content, re.MULTILINE | re.DOTALL)
if backup_match:
    methods_code += backup_match.group(0) + "\n\n"
    
erase_match = re.search(r'void _showEraseDataFlow\(.*?^  }', pagudhi_content, re.MULTILINE | re.DOTALL)
if erase_match:
    methods_code += erase_match.group(0) + "\n\n"
    
sync_match = re.search(r'void _showSyncFlow\(.*?^  }', pagudhi_content, re.MULTILINE | re.DOTALL)
if sync_match:
    methods_code += sync_match.group(0) + "\n\n"

# Remove those methods
new_pagudhi_content = re.sub(r'  void _showBackupFlow\(.*?^  }', '', pagudhi_content, flags=re.MULTILINE | re.DOTALL)
new_pagudhi_content = re.sub(r'  void _showEraseDataFlow\(.*?^  }', '', new_pagudhi_content, flags=re.MULTILINE | re.DOTALL)
new_pagudhi_content = re.sub(r'  void _showSyncFlow\(.*?^  }', '', new_pagudhi_content, flags=re.MULTILINE | re.DOTALL)

# Remove the ElvanSettingsRow entries
# Backup Row
new_pagudhi_content = re.sub(r'        ElvanSettingsRow\(\s*iconWidget: Icon\(\s*CupertinoIcons\.cloud_upload_fill.*?_showBackupFlow.*?,\s*\),', '', new_pagudhi_content, flags=re.MULTILINE | re.DOTALL)
# Sync Row
new_pagudhi_content = re.sub(r'        ElvanSettingsRow\(\s*iconWidget: Icon\(\s*CupertinoIcons\.arrow_2_circlepath.*?_showSyncFlow.*?,\s*\),', '', new_pagudhi_content, flags=re.MULTILINE | re.DOTALL)
# Erase Row
new_pagudhi_content = re.sub(r'        ElvanSettingsRow\(\s*iconWidget: Icon\(\s*CupertinoIcons\.delete_solid.*?_showEraseDataFlow.*?,\s*\),', '', new_pagudhi_content, flags=re.MULTILINE | re.DOTALL)

with open(pagudhi_path, 'w', encoding='utf-8') as f:
    f.write(new_pagudhi_content)

# 2. Add StorageActionsSection to thaekkagam_pagudhi.dart
thaekkagam_path = os.path.join(base_path, 'koorugal', 'thaekkagam_pagudhi.dart')
with open(thaekkagam_path, 'r', encoding='utf-8') as f:
    thaekkagam_content = f.read()

# Make StorageManagerSection icons monochrome
thaekkagam_content = re.sub(r'theme\.colorScheme\.primary\.withOpacity\(0\.1\)', 'theme.colorScheme.onSurface.withValues(alpha: 0.05)', thaekkagam_content)
thaekkagam_content = re.sub(r'theme\.colorScheme\.primary', 'theme.colorScheme.onSurface', thaekkagam_content)

thaekkagam_content = re.sub(r'theme\.colorScheme\.secondary\.withOpacity\(0\.1\)', 'theme.colorScheme.onSurface.withValues(alpha: 0.05)', thaekkagam_content)
thaekkagam_content = re.sub(r'theme\.colorScheme\.secondary', 'theme.colorScheme.onSurface', thaekkagam_content)

thaekkagam_content = re.sub(r'Colors\.blue\.withOpacity\(0\.1\)', 'theme.colorScheme.onSurface.withValues(alpha: 0.05)', thaekkagam_content)
thaekkagam_content = re.sub(r'Colors\.blue', 'theme.colorScheme.onSurface', thaekkagam_content)

thaekkagam_content = re.sub(r'Colors\.purple\.withOpacity\(0\.1\)', 'theme.colorScheme.onSurface.withValues(alpha: 0.05)', thaekkagam_content)
thaekkagam_content = re.sub(r'Colors\.purple', 'theme.colorScheme.onSurface', thaekkagam_content)

# Add imports for dialogs
imports_to_add = """
import '../../../../adippadai/mozhiyaakkam/k.dart';
import '../../../../koorugal/ulleedugal/elvan_ulleedu.dart';
import '../../../../koorugal/maeladukkugal/elvan_cheyal_maeladukku.dart';
import '../../../../koorugal/maeladukkugal/elvan_aetrum_maeladukku.dart';
import '../../../../koorugal/podhu_koorugal/elvan_siruseidhi.dart';
import '../../../../adippadai/viruppangal_paniyagam.dart';
import '../../../../adippadai/nilaimai/seyali_nilaimai.dart';
import '../../../../adippadai/vazhikaattal/navigation_provider.dart';
import '../../../niril_podhu/kalanjiyam/pattiyal_nilaimai.dart';
import '../../../niril_podhu/kalanjiyam/patru_nilaimai.dart';
import '../../../niril_podhu/kalanjiyam/porul_nilaimai.dart';
import '../../../niril_podhu/kalanjiyam/vaangunar_nilaimai.dart';
"""
if 'k.dart' not in thaekkagam_content:
    thaekkagam_content = thaekkagam_content.replace("import 'elvan_amaippu_pagudhi.dart';", "import 'elvan_amaippu_pagudhi.dart';\n" + imports_to_add)

# Create StorageActionsSection class
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
        ElvanSettingsRow(
          iconWidget: Icon(
            CupertinoIcons.arrow_2_circlepath,
            color: theme.colorScheme.onSurface,
            size: 20,
          ),
          iconBgColor: iconBgColor,
          title: K.orunginaiCheyaliPtn.tr(context, ref),
          onTap: () {{
            _showSyncFlow(context, ref);
          }},
        ),
        ElvanSettingsRow(
          iconWidget: Icon(
            CupertinoIcons.delete_solid,
            color: theme.colorScheme.onSurface,
            size: 20,
          ),
          iconBgColor: iconBgColor,
          title: K.cheyalithTharavaiAzhi.tr(context, ref),
          description: K.tharavaiazhi.tr(context, ref),
          onTap: () {{
            _showEraseDataFlow(context, ref);
          }},
        ),
      ],
    );
  }}

{methods_code}
}}
'''

thaekkagam_content += actions_section

with open(thaekkagam_path, 'w', encoding='utf-8') as f:
    f.write(thaekkagam_content)


# 3. Add StorageActionsSection to thaekkagam_amaippugal_thirai.dart
thirai_path = os.path.join(base_path, 'thiraigal', 'thaekkagam_amaippugal_thirai.dart')
with open(thirai_path, 'r', encoding='utf-8') as f:
    thirai_content = f.read()

if 'const StorageActionsSection()' not in thirai_content:
    thirai_content = thirai_content.replace(
        'const StorageManagerSection(),',
        'const StorageManagerSection(),\n              const SizedBox(height: 24),\n              const StorageActionsSection(),'
    )

with open(thirai_path, 'w', encoding='utf-8') as f:
    thirai_content = thirai_content.replace("import '../../../../adippadai/mozhiyaakkam/mozhi_vazhanguthi.dart';", '') # if any
    f.write(thirai_content)

print("Migration completed.")
