import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import 'package:elvan_niril/src/adippadai/mozhiyaakkam/k.dart';
import '../../../../adippadai/mozhiyaakkam/mozhi_vazhanguthi.dart';
import '../../../../adippadai/tharavuru/uruvugal.dart';
import 'elvan_paarvai_oadu.dart';

class PorulPaarvai extends ConsumerWidget {
  const PorulPaarvai({
    super.key,
    required this.porul,
    required this.achuMozhi,
    required this.onEdit,
  });

  final PorulTharavuru porul;
  final String achuMozhi;
  final VoidCallback onEdit;

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final isDark = Theme.of(context).brightness == Brightness.dark;

    final primaryLang = achuMozhi;
    final secondaryLang = primaryLang == 'ta' ? 'en' : 'ta';
    
    // Support both specific maps
    final String p1 = porul.porulPeyar[primaryLang] ?? porul.porulPeyar['ta'] ?? '';
    final String p2 = porul.porulPeyar[secondaryLang] ?? porul.porulPeyar['en'] ?? '';
    
    final primaryName = p1.isNotEmpty ? p1 : (p2.isNotEmpty ? p2 : '-');
    final secondaryName = p1.isNotEmpty && p2.isNotEmpty && p1 != p2 ? p2 : '';

    return ElvanPaarvaiOadu(
      title: K.porulTharavugal.tr(context, ref),
      onEdit: onEdit,
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          // Header Card
          Container(
            padding: const EdgeInsets.all(32),
            decoration: BoxDecoration(
              color: isDark 
                ? Colors.purple.withValues(alpha: 0.1) 
                : Colors.purple.withValues(alpha: 0.05),
              borderRadius: BorderRadius.circular(16),
              border: Border.all(
                color: isDark ? Colors.purple.withValues(alpha: 0.2) : Colors.purple.withValues(alpha: 0.1),
              )
            ),
            child: Row(
              children: [
                Container(
                  width: 64,
                  height: 64,
                  decoration: BoxDecoration(
                    color: isDark ? Colors.purple.withValues(alpha: 0.2) : Colors.purple.withValues(alpha: 0.1),
                    shape: BoxShape.circle,
                  ),
                  child: Center(
                    child: Icon(
                      Icons.inventory_2,
                      size: 28,
                      color: isDark ? Colors.purple.shade200 : Colors.purple.shade800,
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
                if (porul.hsnCode.isNotEmpty)
                  Container(
                    padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 6),
                    decoration: BoxDecoration(
                      color: isDark ? Colors.white12 : Colors.black12,
                      borderRadius: BorderRadius.circular(8),
                    ),
                    child: Text(
                      'HSN: ${porul.hsnCode}',
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
                    title: K.vilai.tr(context, ref),
                    value: '₹${porul.vilai}',
                    icon: Icons.currency_rupee,
                    isWide: isWide,
                  ),
                  _buildDetailCard(
                    context, 
                    title: K.gstVeedham.tr(context, ref),
                    value: '${porul.variVeetham}%',
                    icon: Icons.percent,
                    isWide: isWide,
                  ),
                  _buildDetailCard(
                    context, 
                    title: K.alavuVagai.tr(context, ref),
                    value: porul.alavuVagai,
                    icon: Icons.scale,
                    isWide: isWide,
                  ),
                  _buildDetailCard(
                    context, 
                    title: 'Unit',
                    value: porul.alagu,
                    icon: Icons.format_list_numbered,
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
