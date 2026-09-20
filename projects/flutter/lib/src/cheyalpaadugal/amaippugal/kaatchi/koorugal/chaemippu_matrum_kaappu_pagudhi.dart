import 'package:flutter/material.dart';
import 'package:flutter_svg/flutter_svg.dart';
import '../../../../adippadai/panigal/seyali_oaviyangal.dart';

import 'package:flutter/cupertino.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../../../adippadai/panigal/niril_backup_service.dart';
import '../../tharavu/kooli_niruvana_tharavugal_provider.dart';
import '../../tharavu/pattu_niruvana_tharavugal_provider.dart';
import 'elvan_amaippu_pagudhi.dart';

import 'package:elvan_niril/src/adippadai/mozhiyaakkam/k.dart';
import '../../../../adippadai/mozhiyaakkam/mozhi_vazhanguthi.dart';
import '../../../../adippadai/panigal/elvan_naatkaatti.dart';


import '../../../../koorugal/maeladukkugal/elvan_cheyal_maeladukku.dart';
import '../../../../koorugal/maeladukkugal/elvan_aetrum_maeladukku.dart';
import '../../../../koorugal/podhu_koorugal/elvan_siruseidhi.dart';


final storageStatsProvider = FutureProvider<Map<String, dynamic>>((ref) async {
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

class ChaemippuMatrumKaappuPagudhi extends ConsumerWidget {
  const ChaemippuMatrumKaappuPagudhi({super.key});

  Widget _buildDataGrid(BuildContext context, WidgetRef ref, int invoices, int receipts, int customers, int products, bool isLoading) {
    final color = Theme.of(context).colorScheme.onSurface.withValues(alpha: 0.5);
    final style = TextStyle(fontSize: 12, color: color);
    
    final invStr = isLoading ? '--' : '$invoices';
    final recStr = isLoading ? '--' : '$receipts';
    final cusStr = isLoading ? '--' : '$customers';
    final proStr = isLoading ? '--' : '$products';

    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        Row(
          children: [
            Expanded(child: Text('$invStr ${K.pattiyalgal.tr(context, ref)}', style: style)),
            Expanded(child: Text('$recStr ${K.patrucheettugal.tr(context, ref)}', style: style)),
          ],
        ),
        const SizedBox(height: 6),
        Row(
          children: [
            Expanded(child: Text('$cusStr ${K.vaangunargal.tr(context, ref)}', style: style)),
            Expanded(child: Text('$proStr ${K.porutkal.tr(context, ref)}', style: style)),
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

    final stats = statsAsync.value ?? {
      'lastBackup': null,
      'backupSize': 0,
      'totalDbSize': 0,
      'kooliInvoices': 0,
      'kooliReceipts': 0,
      'kooliProducts': 0,
      'kooliCustomers': 0,
      'silkInvoices': 0,
      'silkReceipts': 0,
      'silkProducts': 0,
      'silkCustomers': 0,
    };

    final isLoading = statsAsync.isLoading && statsAsync.value == null;

    final DateTime? lastBackup = stats['lastBackup'];
    final backupDateStr = isLoading 
        ? K.aetrugiradhu.tr(context, ref) 
        : (lastBackup != null 
            ? ElvanNaatkaatti.formatDateTime(context, ref, lastBackup)
            : K.idhuvuraiKaappuIllai.tr(context, ref));
            
    final storageUsedStr = isLoading 
        ? K.aetrugiradhu.tr(context, ref) 
        : '${_formatBytes(stats['totalDbSize'])} ${K.tharavuthalam.tr(context, ref)}\n${_formatBytes(stats['backupSize'])} ${K.kaappu.tr(context, ref)}';

    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        StorageProjectionPill(stats: stats, isLoading: isLoading),
        ElvanSettingsSection(
          children: [
            ElvanSettingsRow(
              iconWidget: Icon(
                CupertinoIcons.folder_solid,
                color: theme.colorScheme.onSurface,
                size: 20,
              ),
              iconBgColor: theme.colorScheme.onSurface.withValues(alpha: 0.05),
              title: K.payanpaduthiyaChaemippu.tr(context, ref),
              description: storageUsedStr,
              crossAxisAlignment: CrossAxisAlignment.start,
              onTap: () {},
            ),
            ElvanSettingsRow(
              iconWidget: Icon(
                CupertinoIcons.time,
                color: theme.colorScheme.onSurface,
                size: 20,
              ),
              iconBgColor: theme.colorScheme.onSurface.withValues(alpha: 0.05),
              title: K.kadaisiThaaniyakkaKaappu.tr(context, ref),
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
              iconWidget: SvgPicture.string(
                AppSvgs.coolieMode,
                width: 20,
                height: 20,
                colorFilter: ColorFilter.mode(theme.colorScheme.onSurface, BlendMode.srcIn),
              ),
              iconBgColor: theme.colorScheme.onSurface.withValues(alpha: 0.05),
              title: K.kooliTharavugal.tr(context, ref),
              crossAxisAlignment: CrossAxisAlignment.start,
              customDescription: _buildDataGrid(context, ref, stats['kooliInvoices'], stats['kooliReceipts'], stats['kooliCustomers'], stats['kooliProducts'], isLoading),
              onTap: () {},
            ),
            ElvanSettingsRow(
              iconWidget: SvgPicture.string(
                AppSvgs.silkMode,
                width: 20,
                height: 20,
                colorFilter: ColorFilter.mode(theme.colorScheme.onSurface, BlendMode.srcIn),
              ),
              iconBgColor: theme.colorScheme.onSurface.withValues(alpha: 0.05),
              title: K.pattuTharavugal.tr(context, ref),
              crossAxisAlignment: CrossAxisAlignment.start,
              customDescription: _buildDataGrid(context, ref, stats['silkInvoices'], stats['silkReceipts'], stats['silkCustomers'], stats['silkProducts'], isLoading),
              onTap: () {},
            ),
          ],
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

class StorageProjectionPill extends ConsumerWidget {
  final Map<String, dynamic> stats;
  final bool isLoading;
  
  const StorageProjectionPill({
    super.key, 
    required this.stats, 
    this.isLoading = false,
  });

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final theme = Theme.of(context);
    final isDark = theme.brightness == Brightness.dark;
    final cardColor = isDark ? const Color(0xFF111111) : Colors.white;

    final dbSize = stats['totalDbSize'] as int;
    final totalItems = (stats['kooliInvoices'] as int) + 
                       (stats['kooliReceipts'] as int) + 
                       (stats['kooliCustomers'] as int) + 
                       (stats['kooliProducts'] as int) + 
                       (stats['silkInvoices'] as int) + 
                       (stats['silkReceipts'] as int) + 
                       (stats['silkCustomers'] as int) + 
                       (stats['silkProducts'] as int);
    
    final avgSize = totalItems > 0 ? (dbSize / totalItems) : 4096.0;
    final maxSpace = 1024.0 * 1024.0 * 1024.0; // 1 GB
    final percent = dbSize / maxSpace;

    // Formatting sizes
    String formatBytes(double bytes) {
      if (bytes <= 0) return "0 B";
      const suffixes = ["B", "KB", "MB", "GB", "TB"];
      var i = 0;
      double size = bytes;
      while (size > 1024 && i < suffixes.length - 1) {
        size /= 1024;
        i++;
      }
      return '${size.toStringAsFixed(2)} ${suffixes[i]}';
    }

    return Container(
      margin: const EdgeInsets.only(bottom: 24),
      padding: const EdgeInsets.all(20),
      decoration: BoxDecoration(
        color: cardColor,
        borderRadius: BorderRadius.circular(24),
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Padding(
            padding: const EdgeInsets.symmetric(horizontal: 14),
            child: Row(
              mainAxisAlignment: MainAxisAlignment.spaceBetween,
              crossAxisAlignment: CrossAxisAlignment.end,
              children: [
                Text(
                  K.tharavuthalam.tr(context, ref),
                  style: TextStyle(
                    fontSize: 14,
                    fontWeight: FontWeight.w600,
                    color: theme.colorScheme.onSurface,
                  ),
                ),
                Text(
                  isLoading 
                      ? '--- / 1 GB' 
                      : '${formatBytes(dbSize.toDouble())} / 1 GB',
                  style: TextStyle(
                    fontSize: 12,
                    fontWeight: FontWeight.w500,
                    color: theme.colorScheme.onSurface.withValues(alpha: 0.5),
                  ),
                ),
              ],
            ),
          ),
          const SizedBox(height: 8),
          ClipRRect(
            borderRadius: BorderRadius.circular(100),
            child: TweenAnimationBuilder<double>(
              tween: Tween<double>(begin: 0.0, end: isLoading ? 0.0 : percent),
              duration: const Duration(milliseconds: 1200),
              curve: Curves.easeOutCubic,
              builder: (context, value, _) {
                return LinearProgressIndicator(
                  value: value,
                  minHeight: 28,
                  backgroundColor: theme.colorScheme.onSurface.withValues(alpha: 0.1),
                  valueColor: AlwaysStoppedAnimation<Color>(theme.colorScheme.onSurface),
                );
              },
            ),
          ),
        ],
      ),
    );
  }
}
