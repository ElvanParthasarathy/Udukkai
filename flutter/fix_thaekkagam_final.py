import os
import re

file_path = r'D:\Projects\Elvan Niril\flutter\lib\src\cheyalpaadugal\amaippugal\kaatchi\koorugal\thaekkagam_pagudhi.dart'

with open(file_path, 'r', encoding='utf-8') as f:
    content = f.read()

# Fix 1: In StorageManagerSection, ensure the return statement is clean.
# Currently it is:
# return Column(children: [
#                 StorageProjectionPill(stats: stats),
#                 ElvanSettingsSection(
# ...
#               ],
#             ),
#               ],);
# Which is mostly correct but let's make sure it's perfect.
manager_pattern = r'return Column\(children: \[\s*StorageProjectionPill\(stats: stats\),\s*ElvanSettingsSection\('
if re.search(manager_pattern, content):
    print("StorageManagerSection pill insertion looks okay syntactically, wait let's verify brackets.")

# Let's completely replace StorageManagerSection and StorageActionsSection to be safe.
# Find the start of StorageManagerSection
start_manager = content.find('class StorageManagerSection extends ConsumerWidget {')
start_actions = content.find('class StorageActionsSection extends ConsumerWidget {')
end_actions = content.find('class StorageProjectionPill extends ConsumerWidget {')

if start_manager != -1 and start_actions != -1 and end_actions != -1:
    new_sections = """class StorageManagerSection extends ConsumerWidget {
  const StorageManagerSection({super.key});

  Widget _buildDataGrid(BuildContext context, int invoices, int receipts, int customers, int products) {
    final color = Theme.of(context).colorScheme.onSurface.withValues(alpha: 0.5);
    final style = TextStyle(fontSize: 12, color: color);
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        Row(
          children: [
            Expanded(child: Text('$invoices Invoices', style: style)),
            Expanded(child: Text('$receipts Receipts', style: style)),
          ],
        ),
        const SizedBox(height: 6),
        Row(
          children: [
            Expanded(child: Text('$customers Customers', style: style)),
            Expanded(child: Text('$products Products', style: style)),
          ],
        ),
      ],
    );
  }

  String _formatBytes(int bytes) {
    if (bytes <= 0) return "0 B";
    const suffixes = ["B", "KB", "MB", "GB", "TB"];
    var i = 0;
    double size = bytes.toDouble();
    while (size > 1024 && i < suffixes.length - 1) {
      size /= 1024;
      i++;
    }
    return '${size.toStringAsFixed(2)} ${suffixes[i]}';
  }

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final theme = Theme.of(context);
    final statsAsync = ref.watch(storageStatsProvider);

    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        statsAsync.when(
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
                        CupertinoIcons.folder_solid,
                        color: theme.colorScheme.onSurface,
                        size: 20,
                      ),
                      iconBgColor: theme.colorScheme.onSurface.withValues(alpha: 0.05),
                      title: 'Storage Used',
                      description: '${_formatBytes(stats['totalDbSize'])} Database  •  ${_formatBytes(stats['backupSize'])} Backup',
                      onTap: () {},
                    ),
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
          loading: () => const Padding(
            padding: EdgeInsets.all(32),
            child: Center(child: CupertinoActivityIndicator()),
          ),
          error: (e, s) => Center(child: Text('Error loading stats: $e')),
        ),
      ],
    );
  }
}

class StorageActionsSection extends ConsumerWidget {
  const StorageActionsSection({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
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
          onTap: () {
            _showBackupFlow(context, ref);
          },
        ),
      ],
    );
  }

  void _showBackupFlow(BuildContext context, WidgetRef ref) {
    showElvanActionSheet(
      context: context,
      title: K.tharavuKaappu.tr(context, ref),
      cancelText: K.kaividuPtn.tr(context, ref),
      confirmText: K.kaappuCheiPtn.tr(context, ref),
      onConfirm: () async {
        showElvanLoadingOverlay(
            context: context, text: K.chaemikkappadugiradhu.tr(context, ref));
            
        // Wait a small amount of time for UX purposes so it doesn't flash instantly
        await Future.delayed(const Duration(milliseconds: 800));
        
        final backupService = ref.read(backupServiceProvider);
        await backupService.createBackup();

        if (context.mounted) {
          // Pop the loading dialog
          Navigator.of(context, rootNavigator: true).pop();
          ElvanSnackbar.show(context, K.tharavuchaemippuvetri.tr(context, ref));
          ref.invalidate(storageStatsProvider);
        }
      },
    );
  }
}

"""
    
    content = content[:start_manager] + new_sections + content[end_actions:]

    with open(file_path, 'w', encoding='utf-8') as f:
        f.write(content)
    print("Replaced StorageManagerSection and StorageActionsSection fully.")
else:
    print("Could not find sections")
