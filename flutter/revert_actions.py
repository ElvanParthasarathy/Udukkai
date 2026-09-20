import os
import re

base_path = r'D:\Projects\Elvan Niril\flutter\lib\src\cheyalpaadugal\amaippugal\kaatchi'
pagudhi_path = os.path.join(base_path, 'koorugal', 'kanakku_paadhugaappu_pagudhi.dart')
thaekkagam_path = os.path.join(base_path, 'koorugal', 'thaekkagam_pagudhi.dart')

# 1. Read thaekkagam_pagudhi.dart and extract the methods and rows
with open(thaekkagam_path, 'r', encoding='utf-8') as f:
    thaekkagam_content = f.read()

# Extract methods
backup_match = re.search(r'  void _showBackupFlow\(.*?^  }', thaekkagam_content, re.MULTILINE | re.DOTALL)
erase_match = re.search(r'  void _showEraseDataFlow\(.*?^  }', thaekkagam_content, re.MULTILINE | re.DOTALL)
sync_match = re.search(r'  void _showSyncFlow\(.*?^  }', thaekkagam_content, re.MULTILINE | re.DOTALL)

backup_method = backup_match.group(0) if backup_match else ''
erase_method = erase_match.group(0) if erase_match else ''
sync_method = sync_match.group(0) if sync_match else ''

# 2. Re-insert them into kanakku_paadhugaappu_pagudhi.dart
with open(pagudhi_path, 'r', encoding='utf-8') as f:
    pagudhi_content = f.read()

# Re-insert the rows before the Logout row
rows_to_insert = '''        ElvanSettingsRow(
          iconWidget: Icon(
            CupertinoIcons.cloud_upload_fill,
            color: theme.colorScheme.onSurface,
            size: 20,
          ),
          iconBgColor: iconBgColor,
          title: K.tharavuKaappuChei.tr(context, ref),
          description: K.ungalTharavaiChaemikkavum.tr(context, ref),
          onTap: () {
            _showBackupFlow(context, ref);
          },
        ),
        ElvanSettingsRow(
          iconWidget: Icon(
            CupertinoIcons.arrow_2_circlepath,
            color: theme.colorScheme.onSurface,
            size: 20,
          ),
          iconBgColor: iconBgColor,
          title: K.orunginaiCheyaliPtn.tr(context, ref),
          onTap: () {
            _showSyncFlow(context, ref);
          },
        ),'''

erase_row = '''        ElvanSettingsRow(
          iconWidget: Icon(
            CupertinoIcons.delete_solid,
            color: theme.colorScheme.onSurface,
            size: 20,
          ),
          iconBgColor: iconBgColor,
          title: K.cheyalithTharavaiAzhi.tr(context, ref),
          description: K.tharavaiazhi.tr(context, ref),
          onTap: () {
            _showEraseDataFlow(context, ref);
          },
        ),'''

pagudhi_content = re.sub(
    r'        ElvanSettingsRow\(\s*iconWidget: Icon\(\s*CupertinoIcons\.square_arrow_right_fill',
    rows_to_insert + '\n        ElvanSettingsRow(\n          iconWidget: Icon(\n            CupertinoIcons.square_arrow_right_fill',
    pagudhi_content
)

# Insert erase_row after Logout row
logout_end_match = re.search(r'        ElvanSettingsRow\(\s*iconWidget: Icon\(\s*CupertinoIcons\.square_arrow_right_fill.*?_showSignOutFlow.*?,\s*\),', pagudhi_content, re.MULTILINE | re.DOTALL)
if logout_end_match:
    pagudhi_content = pagudhi_content[:logout_end_match.end()] + '\n' + erase_row + pagudhi_content[logout_end_match.end():]

# Insert methods before the final closing brace of AccountSecuritySection
account_sec_end = pagudhi_content.rfind('}')
if account_sec_end != -1:
    pagudhi_content = pagudhi_content[:account_sec_end] + f"\n{backup_method}\n\n{erase_method}\n\n{sync_method}\n" + pagudhi_content[account_sec_end:]

with open(pagudhi_path, 'w', encoding='utf-8') as f:
    f.write(pagudhi_content)


# 3. Remove StorageActionsSection from thaekkagam_pagudhi.dart
thaekkagam_content = re.sub(r'class StorageActionsSection extends ConsumerWidget \{.*', '', thaekkagam_content, flags=re.MULTILINE | re.DOTALL)
with open(thaekkagam_path, 'w', encoding='utf-8') as f:
    f.write(thaekkagam_content)


# 4. Remove StorageActionsSection from thaekkagam_amaippugal_thirai.dart
thirai_path = os.path.join(base_path, 'thiraigal', 'thaekkagam_amaippugal_thirai.dart')
with open(thirai_path, 'r', encoding='utf-8') as f:
    thirai_content = f.read()

thirai_content = thirai_content.replace('const StorageManagerSection(),\n              const SizedBox(height: 24),\n              const StorageActionsSection(),', 'const StorageManagerSection(),')

with open(thirai_path, 'w', encoding='utf-8') as f:
    f.write(thirai_content)

print("Reverted actions to AccountSecuritySection")
