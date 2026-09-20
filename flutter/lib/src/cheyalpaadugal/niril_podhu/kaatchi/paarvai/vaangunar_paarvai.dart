import 'package:flutter/material.dart';
import 'package:flutter/cupertino.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import 'package:elvan_niril/src/adippadai/mozhiyaakkam/k.dart';
import '../../../../adippadai/mozhiyaakkam/mozhi_vazhanguthi.dart';
import '../../../../adippadai/tharavuru/uruvugal.dart';
import 'elvan_paarvai_oadu.dart';

class VaangunarPaarvai extends ConsumerWidget {
  const VaangunarPaarvai({
    super.key,
    required this.vaangunar,
    required this.achuMozhi,
    required this.onEdit,
  });

  final VaangunarTharavuru vaangunar;
  final String achuMozhi;
  final VoidCallback onEdit;

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final v = vaangunar;
    final isDark = Theme.of(context).brightness == Brightness.dark;

    final primaryLang = achuMozhi;
    final secondaryLang = primaryLang == 'ta' ? 'en' : 'ta';
    
    // We also support 'ta' and 'en' keys if they are coming from Pattu which uses locale directly
    final String p1 = v.peyar[primaryLang] ?? v.peyar['ta'] ?? '';
    final String p2 = v.peyar[secondaryLang] ?? v.peyar['en'] ?? '';
    
    final primaryName = p1.isNotEmpty ? p1 : (p2.isNotEmpty ? p2 : '-');
    final secondaryName = p1.isNotEmpty && p2.isNotEmpty && p1 != p2 ? p2 : '';

    return ElvanPaarvaiOadu(
      title: K.vaangunarTharavugal.tr(context, ref),
      onEdit: onEdit,
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          // Header Card
          Container(
            padding: const EdgeInsets.all(32),
            decoration: BoxDecoration(
              color: isDark 
                ? Colors.blue.withValues(alpha: 0.1) 
                : Colors.blue.withValues(alpha: 0.05),
              borderRadius: BorderRadius.circular(16),
              border: Border.all(
                color: isDark ? Colors.blue.withValues(alpha: 0.2) : Colors.blue.withValues(alpha: 0.1),
              )
            ),
            child: Row(
              children: [
                Container(
                  width: 64,
                  height: 64,
                  decoration: BoxDecoration(
                    color: isDark ? Colors.blue.withValues(alpha: 0.2) : Colors.blue.withValues(alpha: 0.1),
                    shape: BoxShape.circle,
                  ),
                  child: Center(
                    child: Text(
                      primaryName.isNotEmpty ? primaryName[0].toUpperCase() : '?',
                      style: TextStyle(
                        fontSize: 28,
                        fontWeight: FontWeight.w700,
                        color: isDark ? Colors.blue.shade200 : Colors.blue.shade800,
                      ),
                    ),
                  ),
                ),
                const SizedBox(width: 24),
                Expanded(
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      Text(
                        primaryName,
                        style: const TextStyle(
                          fontSize: 24,
                          fontWeight: FontWeight.w700,
                          letterSpacing: -0.2,
                        ),
                      ),
                      if (secondaryName.isNotEmpty) ...[
                        const SizedBox(height: 4),
                        Text(
                          secondaryName,
                          style: TextStyle(
                            fontSize: 16,
                            color: isDark ? Colors.white70 : Colors.black87,
                          ),
                        ),
                      ],
                    ],
                  ),
                ),
                if (v.anjalKuriyeedu.isNotEmpty)
                  Container(
                    padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 6),
                    decoration: BoxDecoration(
                      color: isDark ? Colors.white12 : Colors.black12,
                      borderRadius: BorderRadius.circular(8),
                    ),
                    child: Text(
                      v.anjalKuriyeedu,
                      style: const TextStyle(
                        fontSize: 14,
                        fontWeight: FontWeight.w600,
                        letterSpacing: 1,
                      ),
                    ),
                  ),
              ],
            ),
          ),
          
          const SizedBox(height: 32),
          
          // Details Grid
          LayoutBuilder(
            builder: (context, constraints) {
              final isWide = constraints.maxWidth > 600;
              return Wrap(
                spacing: 24,
                runSpacing: 24,
                children: [
                  _buildDetailCard(
                    context, 
                    title: K.oor.tr(context, ref),
                    value: v.oor[primaryLang] ?? v.oor['ta'] ?? v.oor.values.firstOrNull ?? '-',
                    icon: Icons.location_city,
                    isWide: isWide,
                  ),
                  _buildDetailCard(
                    context, 
                    title: K.mugavari.tr(context, ref),
                    value: v.mugavari[primaryLang] ?? v.mugavari['ta'] ?? v.mugavari.values.firstOrNull ?? '-',
                    icon: Icons.map,
                    isWide: isWide,
                  ),
                  if (v.tholaipaesi.isNotEmpty)
                    _buildDetailCard(
                      context, 
                      title: K.tholaipaesi.tr(context, ref),
                      value: v.tholaipaesi,
                      icon: CupertinoIcons.phone,
                      isWide: isWide,
                    ),

                  if (v.gstin?.isNotEmpty == true)
                    _buildDetailCard(
                      context, 
                      title: 'GSTIN',
                      value: v.gstin!,
                      icon: Icons.account_balance,
                      isWide: isWide,
                    ),
                  if (v.minnanjal?.isNotEmpty == true)
                    _buildDetailCard(
                      context, 
                      title: K.minnanjal.tr(context, ref),
                      value: v.minnanjal!,
                      icon: Icons.email,
                      isWide: isWide,
                    ),

                ],
              );
            }
          ),
          
          const SizedBox(height: 48),
        ],
      ),
    );
  }
  
  Widget _buildDetailCard(
    BuildContext context, {
    required String title,
    required String value,
    required IconData icon,
    required bool isWide,
  }) {
    final isDark = Theme.of(context).brightness == Brightness.dark;
    return Container(
      width: isWide ? 300 : double.infinity,
      padding: const EdgeInsets.all(20),
      decoration: BoxDecoration(
        color: isDark ? Colors.white.withValues(alpha: 0.03) : Colors.black.withValues(alpha: 0.02),
        borderRadius: BorderRadius.circular(12),
        border: Border.all(
          color: isDark ? Colors.white10 : Colors.black12,
        ),
      ),
      child: Row(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Icon(
            icon,
            size: 20,
            color: isDark ? Colors.white38 : Colors.black38,
          ),
          const SizedBox(width: 16),
          Expanded(
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text(
                  title,
                  style: TextStyle(
                    fontSize: 13,
                    color: isDark ? Colors.white54 : Colors.black54,
                    fontWeight: FontWeight.w500,
                  ),
                ),
                const SizedBox(height: 4),
                Text(
                  value,
                  style: const TextStyle(
                    fontSize: 16,
                    fontWeight: FontWeight.w600,
                  ),
                ),
              ],
            ),
          ),
        ],
      ),
    );
  }
}
