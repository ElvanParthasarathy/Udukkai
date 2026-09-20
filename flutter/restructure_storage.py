import os
import re

base_path = r'D:\Projects\Elvan Niril\flutter\lib\src\cheyalpaadugal\amaippugal\kaatchi'
pagudhi_path = os.path.join(base_path, 'koorugal', 'thaekkagam_pagudhi.dart')
thirai_path = os.path.join(base_path, 'thiraigal', 'thaekkagam_amaippugal_thirai.dart')

with open(pagudhi_path, 'r', encoding='utf-8') as f:
    content = f.read()

# 1. We need to extract `_showBackupFlow` from `StorageActionsSection` and inject it into `StorageManagerSection`
backup_method_match = re.search(r'  void _showBackupFlow\(.*?\n  \}\n', content, re.MULTILINE | re.DOTALL)
backup_method = backup_method_match.group(0)

# 2. Rebuild the `Column(children: [...])` in `StorageManagerSection`
# It's better to just replace the whole `statsAsync.when(data: (stats) { ...` block

start_when = content.find('        statsAsync.when(')
end_when = content.find('          loading: () => const Padding(')

new_when = '''        statsAsync.when(
          data: (stats) {
            final DateTime? lastBackup = stats['lastBackup'];
            final backupDateStr = lastBackup != null 
                ? DateFormat('MMM d, yyyy - h:mm a').format(lastBackup)
                : 'No Backup Yet';

            return Column(
              children: [
                StorageProjectionPill(stats: stats),
                ElvanSettingsSection(
                  children: [
                    ElvanSettingsRow(
                      iconWidget: Icon(
                        CupertinoIcons.folder_solid,
                        color: theme.colorScheme.onSurface,
                        size: 20,
                      ),
                      iconBgColor: theme.colorScheme.onSurface.withValues(alpha: 0.05),
                      title: 'Storage Used',
                      description: '${_formatBytes(stats['totalDbSize'])} Database  •  ${_formatBytes(stats['backupSize'])} Backup',
                      onTap: () {},
                    ),
                  ],
                ),
                const SizedBox(height: 24),
                ElvanSettingsSection(
                  children: [
                    ElvanSettingsRow(
                      iconWidget: Icon(
                        CupertinoIcons.time,
                        color: theme.colorScheme.onSurface,
                        size: 20,
                      ),
                      iconBgColor: theme.colorScheme.onSurface.withValues(alpha: 0.05),
                      title: 'Last Auto-Backup',
                      description: backupDateStr,
                      onTap: () {},
                    ),
                    ElvanSettingsRow(
                      iconWidget: Icon(
                        CupertinoIcons.cloud_upload_fill,
                        color: theme.colorScheme.onSurface,
                        size: 20,
                      ),
                      iconBgColor: theme.colorScheme.onSurface.withValues(alpha: 0.05),
                      title: K.tharavuKaappuChei.tr(context, ref),
                      description: K.ungalTharavaiChaemikkavum.tr(context, ref),
                      onTap: () {
                        _showBackupFlow(context, ref);
                      },
                    ),
                  ],
                ),
                const SizedBox(height: 24),
                ElvanSettingsSection(
                  children: [
                    ElvanSettingsRow(
                      iconWidget: Icon(
                        CupertinoIcons.doc_text_fill,
                        color: theme.colorScheme.onSurface,
                        size: 20,
                      ),
                      iconBgColor: theme.colorScheme.onSurface.withValues(alpha: 0.05),
                      title: 'Coolie Data',
                      customDescription: _buildDataGrid(context, stats['kooliInvoices'], stats['kooliReceipts'], stats['kooliCustomers'], stats['kooliProducts']),
                      onTap: () {},
                    ),
                  ],
                ),
                const SizedBox(height: 24),
                ElvanSettingsSection(
                  children: [
                    ElvanSettingsRow(
                      iconWidget: Icon(
                        CupertinoIcons.cube_box_fill,
                        color: theme.colorScheme.onSurface,
                        size: 20,
                      ),
                      iconBgColor: theme.colorScheme.onSurface.withValues(alpha: 0.05),
                      title: 'Silk Data',
                      customDescription: _buildDataGrid(context, stats['silkInvoices'], stats['silkReceipts'], stats['silkCustomers'], stats['silkProducts']),
                      onTap: () {},
                    ),
                  ],
                ),
              ],
            );
          },
'''

content = content[:start_when] + new_when + content[end_when:]

# Insert _showBackupFlow at the end of StorageManagerSection
end_manager = content.find('class StorageActionsSection')
# find the closing brace of StorageManagerSection before end_manager
closing_brace_index = content.rfind('}', 0, end_manager)
content = content[:closing_brace_index] + '\n' + backup_method + '\n}\n\n' + content[end_manager:]

# Remove StorageActionsSection completely
start_actions = content.find('class StorageActionsSection extends ConsumerWidget {')
end_actions = content.find('class StorageProjectionPill extends ConsumerWidget {')
content = content[:start_actions] + content[end_actions:]

with open(pagudhi_path, 'w', encoding='utf-8') as f:
    f.write(content)

# 3. Remove StorageActionsSection from thaekkagam_amaippugal_thirai.dart
with open(thirai_path, 'r', encoding='utf-8') as f:
    thirai_content = f.read()

thirai_content = thirai_content.replace('              StorageActionsSection(),\n              SizedBox(height: 24),\n', '')

with open(thirai_path, 'w', encoding='utf-8') as f:
    thirai_content = thirai_content.replace('const SizedBox(height: 24),', '')
    f.write(thirai_content)

print("Restructured UI successfully.")
