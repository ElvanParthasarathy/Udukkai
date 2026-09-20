import os
import re

base_path = r'D:\Projects\Elvan Niril\flutter\lib\src\cheyalpaadugal\amaippugal\kaatchi'

# 1. Update kanakku_paadhugaappu_pagudhi.dart
pagudhi_path = os.path.join(base_path, 'koorugal', 'kanakku_paadhugaappu_pagudhi.dart')
with open(pagudhi_path, 'r', encoding='utf-8') as f:
    pagudhi_content = f.read()

# Remove storageStatsProvider and StorageManagerSection
split_pattern = r'final storageStatsProvider = FutureProvider\.autoDispose<Map<String, dynamic>>\(\(ref\) async \{'
parts = re.split(split_pattern, pagudhi_content)
if len(parts) > 1:
    new_pagudhi_content = parts[0].strip()
    with open(pagudhi_path, 'w', encoding='utf-8') as f:
        f.write(new_pagudhi_content)

# 2. Remove StorageManagerSection from paadhugaappu_amaippugal_thirai.dart
paadhu_thirai_path = os.path.join(base_path, 'thiraigal', 'paadhugaappu_amaippugal_thirai.dart')
with open(paadhu_thirai_path, 'r', encoding='utf-8') as f:
    paadhu_content = f.read()

paadhu_content = paadhu_content.replace('import \'../koorugal/kanakku_paadhugaappu_pagudhi.dart\';', 'import \'../koorugal/kanakku_paadhugaappu_pagudhi.dart\';')
paadhu_content = re.sub(r'\s*const StorageManagerSection\(\),\s*const SizedBox\(height:\s*24\),', '', paadhu_content)

with open(paadhu_thirai_path, 'w', encoding='utf-8') as f:
    f.write(paadhu_content)

# 3. Create thaekkagam_pagudhi.dart
thaekkagam_pagudhi_content = '''import 'package:flutter/material.dart';
import 'package:flutter/cupertino.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:intl/intl.dart';

import '../../../../adippadai/panigal/niril_backup_service.dart';
import '../../tharavu/kooli_niruvana_tharavugal_provider.dart';
import '../../tharavu/pattu_niruvana_tharavugal_provider.dart';
import 'elvan_amaippu_pagudhi.dart';

final storageStatsProvider = FutureProvider.autoDispose<Map<String, dynamic>>((ref) async {
  final backupService = ref.watch(backupServiceProvider);
  final kooliDb = ref.watch(kooliDatabaseProvider);
  final pattuDb = ref.watch(pattuDatabaseProvider);

  final backupStats = await backupService.getBackupStats();
  final dbSize = await backupService.getTotalDatabaseSize();
  
  final kPattiyal = await kooliDb.customSelect('SELECT COUNT(*) FROM kooli_patrucheettu_table').getSingle().then((r) => r.read<int>('COUNT(*)'));
  final kPatru = await kooliDb.customSelect('SELECT COUNT(*) FROM kooli_patrugal_table').getSingle().then((r) => r.read<int>('COUNT(*)'));
  final kPorul = await kooliDb.customSelect('SELECT COUNT(*) FROM kooli_porul_table').getSingle().then((r) => r.read<int>('COUNT(*)'));
  final kVaangunar = await kooliDb.customSelect('SELECT COUNT(*) FROM kooli_vaangunar_table').getSingle().then((r) => r.read<int>('COUNT(*)'));
  
  final pPattiyal = await pattuDb.customSelect('SELECT COUNT(*) FROM pattu_patrucheettu_table').getSingle().then((r) => r.read<int>('COUNT(*)'));
  final pPatru = await pattuDb.customSelect('SELECT COUNT(*) FROM pattu_patrugal_table').getSingle().then((r) => r.read<int>('COUNT(*)'));
  final pPorul = await pattuDb.customSelect('SELECT COUNT(*) FROM pattu_porul_table').getSingle().then((r) => r.read<int>('COUNT(*)'));
  final pVaangunar = await pattuDb.customSelect('SELECT COUNT(*) FROM pattu_vaangunar_table').getSingle().then((r) => r.read<int>('COUNT(*)'));

  return {
    'lastBackup': backupStats?['lastModified'],
    'backupSize': backupStats?['sizeBytes'] ?? 0,
    'totalDbSize': dbSize,
    'kooliInvoices': kPattiyal,
    'kooliReceipts': kPatru,
    'kooliProducts': kPorul,
    'kooliCustomers': kVaangunar,
    'silkInvoices': pPattiyal,
    'silkReceipts': pPatru,
    'silkProducts': pPorul,
    'silkCustomers': pVaangunar,
  };
});

class StorageManagerSection extends ConsumerWidget {
  const StorageManagerSection({super.key});

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

            return ElvanSettingsSection(
              children: [
                ElvanSettingsRow(
                  iconWidget: Icon(
                    CupertinoIcons.time,
                    color: theme.colorScheme.primary,
                    size: 20,
                  ),
                  iconBgColor: theme.colorScheme.primary.withOpacity(0.1),
                  title: 'Last Auto-Backup',
                  description: backupDateStr,
                  onTap: () {},
                ),
                ElvanSettingsRow(
                  iconWidget: Icon(
                    CupertinoIcons.folder_solid,
                    color: theme.colorScheme.secondary,
                    size: 20,
                  ),
                  iconBgColor: theme.colorScheme.secondary.withOpacity(0.1),
                  title: 'Storage Used',
                  description: '${_formatBytes(stats['totalDbSize'])} Database  •  ${_formatBytes(stats['backupSize'])} Backup',
                  onTap: () {},
                ),
                ElvanSettingsRow(
                  iconWidget: const Icon(
                    CupertinoIcons.doc_text_fill,
                    color: Colors.blue,
                    size: 20,
                  ),
                  iconBgColor: Colors.blue.withOpacity(0.1),
                  title: 'Coolie Data',
                  description: '${stats['kooliInvoices']} Invoices  •  ${stats['kooliReceipts']} Receipts  •  ${stats['kooliCustomers']} Customers  •  ${stats['kooliProducts']} Products',
                  onTap: () {},
                ),
                ElvanSettingsRow(
                  iconWidget: const Icon(
                    CupertinoIcons.cube_box_fill,
                    color: Colors.purple,
                    size: 20,
                  ),
                  iconBgColor: Colors.purple.withOpacity(0.1),
                  title: 'Silk Data',
                  description: '${stats['silkInvoices']} Invoices  •  ${stats['silkReceipts']} Receipts  •  ${stats['silkCustomers']} Customers  •  ${stats['silkProducts']} Products',
                  onTap: () {},
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
'''
with open(os.path.join(base_path, 'koorugal', 'thaekkagam_pagudhi.dart'), 'w', encoding='utf-8') as f:
    f.write(thaekkagam_pagudhi_content)

# 4. Create thaekkagam_amaippugal_thirai.dart
thaekkagam_thirai_content = '''import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../../chattagam/kaatchi/kaippaesi/elvan_utpakkach_chattagam.dart';
import '../koorugal/thaekkagam_pagudhi.dart';

class ThaekkagamAmaippugalPage extends ConsumerWidget {
  const ThaekkagamAmaippugalPage({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final isDark = Theme.of(context).brightness == Brightness.dark;

    return ElvanSubpageShell(
      title: 'Storage & Backup',
      backgroundColor:
          isDark ? const Color(0xFF000000) : const Color(0xFFF3F4F6),
      slivers: [
        SliverPadding(
          padding: const EdgeInsets.only(
            left: 16,
            right: 16,
            top: 24,
            bottom: 32,
          ),
          sliver: SliverList.list(
            children: const [
              StorageManagerSection(),
            ],
          ),
        ),
      ],
    );
  }
}
'''
with open(os.path.join(base_path, 'thiraigal', 'thaekkagam_amaippugal_thirai.dart'), 'w', encoding='utf-8') as f:
    f.write(thaekkagam_thirai_content)

# 5. Add to amaippugal_thirai.dart
amaippugal_thirai_path = os.path.join(base_path, 'amaippugal_thirai.dart')
with open(amaippugal_thirai_path, 'r', encoding='utf-8') as f:
    amaippugal_content = f.read()

import_statement = "import 'thiraigal/thaekkagam_amaippugal_thirai.dart';"
if import_statement not in amaippugal_content:
    amaippugal_content = amaippugal_content.replace(
        "import 'thiraigal/paadhugaappu_amaippugal_thirai.dart';",
        "import 'thiraigal/paadhugaappu_amaippugal_thirai.dart';\n" + import_statement
    )

new_row = '''                  ElvanSettingsRow(
                    icon: CupertinoIcons.folder_solid,
                    iconBgColor: iconBgColor,
                    title: 'Storage & Backup',
                    description: 'Manage databases and auto-backups',
                    onTap: () =>
                        _navigateTo(context, const ThaekkagamAmaippugalPage()),
                  ),'''

# Insert it right before paadhugaappu
paadhugaappu_row_pattern = r'ElvanSettingsRow\(\s*icon:\s*CupertinoIcons.lock_fill[\s\S]*?PathugappuAmaippugalPage\(\)\),\s*\),'
match = re.search(paadhugaappu_row_pattern, amaippugal_content)
if match:
    amaippugal_content = amaippugal_content[:match.start()] + new_row + '\n' + amaippugal_content[match.start():]
else:
    print("Could not find insertion point in amaippugal_thirai.dart")

with open(amaippugal_thirai_path, 'w', encoding='utf-8') as f:
    f.write(amaippugal_content)

print("Refactoring complete.")
