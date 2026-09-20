import 'package:flutter/material.dart';
import 'package:flutter/cupertino.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:intl/intl.dart';

import '../../../../adippadai/tharavuru/uruvugal.dart';
import '../../../../adippadai/mozhiyaakkam/mozhi_vazhanguthi.dart';
import '../../../../koorugal/podhu_koorugal/elvan_pothu_attai.dart';

/// Dedicated reusable Invoice Card for Silk (Pattu) Invoice Lists
class PattuPattiyalAttai extends ConsumerWidget {
  const PattuPattiyalAttai({
    super.key,
    required this.index,
    required this.pattiyal,
    required this.onTap,
    this.isSelecting = false,
    this.isSelected = false,
    this.onLongPress,
    this.onDuplicate,
  });

  final int index;
  final PattiyalTharavuru pattiyal;
  final VoidCallback onTap;
  final bool isSelecting;
  final bool isSelected;
  final VoidCallback? onLongPress;
  final VoidCallback? onDuplicate;

  static final _dateFormat = DateFormat('dd/MM/yyyy');
  static final _currencyFormat =
      NumberFormat.currency(locale: 'en_IN', symbol: '₹');

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final isDark = Theme.of(context).brightness == Brightness.dark;
    final p = pattiyal;
    final amountStr = _currencyFormat.format(p.mothaThogai);

    final currentLocale = ref.watch(localeProvider);
    final effectiveLang =
        currentLocale?.languageCode ?? Localizations.localeOf(context).languageCode;

    final primaryLang = effectiveLang == 'ta' ? 'ta' : 'en';
    final secondaryLang = effectiveLang == 'ta' ? 'en' : 'ta';

    return ElvanPothuAttai(
      onTap: onTap,
      onLongPress: onLongPress,
      isSelected: isSelected,
      padding: const EdgeInsets.all(16),
      child: Row(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          // Index circle / Selection checkbox
          Container(
            width: 28,
            height: 28,
            margin: const EdgeInsets.only(top: 1),
            decoration: BoxDecoration(
              color: (isSelecting && isSelected)
                  ? (isDark ? Colors.white : Colors.black)
                  : (isDark
                      ? Colors.white.withValues(alpha: 0.12)
                      : Colors.black.withValues(alpha: 0.08)),
              shape: BoxShape.circle,
            ),
            child: Center(
              child: (isSelecting && isSelected)
                  ? Icon(
                      CupertinoIcons.checkmark_alt,
                      size: 16,
                      color: isDark ? Colors.black : Colors.white,
                    )
                  : Text(
                      (index + 1).toString().padLeft(2, '0'),
                      style: TextStyle(
                        fontSize: 11.2,
                        fontWeight: FontWeight.w800,
                        color: isDark ? Colors.white : Colors.black,
                        height: 1,
                      ),
                    ),
            ),
          ),
          const SizedBox(width: 12),
          // Content
          Expanded(
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              mainAxisSize: MainAxisSize.min,
              children: [
                // Row 1: Customer name + chevron
                Row(
                  children: [
                    Expanded(
                      child: Text(
                        p.vaangunarPeyar[primaryLang]?.isNotEmpty == true
                            ? p.vaangunarPeyar[primaryLang]!
                            : p.vaangunarPeyar[secondaryLang] ?? '-',
                        style: const TextStyle(
                          fontSize: 15.2,
                          fontWeight: FontWeight.w700,
                        ),
                        maxLines: 1,
                        overflow: TextOverflow.ellipsis,
                      ),
                    ),
                    Icon(
                      CupertinoIcons.chevron_right,
                      size: 18,
                      color: isDark
                          ? const Color(0xFF555555)
                          : const Color(0xFFAAAAAA),
                    ),
                  ],
                ),
                // Row 2: Invoice # + Date
                const SizedBox(height: 4),
                Text.rich(
                  TextSpan(
                    children: [
                      TextSpan(text: p.patrucheettuEn),
                      TextSpan(
                        text: '  •  ',
                        style: TextStyle(
                          color: (isDark ? Colors.white : Colors.black)
                              .withValues(alpha: 0.4),
                        ),
                      ),
                      TextSpan(text: _dateFormat.format(p.pattiyalNaal)),
                    ],
                  ),
                  style: TextStyle(
                    fontSize: 13.6,
                    color: isDark ? Colors.white54 : Colors.black54,
                  ),
                  maxLines: 1,
                  overflow: TextOverflow.ellipsis,
                ),
                // Row 3: Amount (right-aligned)
                const SizedBox(height: 2),
                Align(
                  alignment: Alignment.centerRight,
                  child: Text(
                    amountStr,
                    style: TextStyle(
                      fontSize: amountStr.length > 11 ? 12.8 : 15.2,
                      fontWeight: FontWeight.w800,
                      color: Theme.of(context).colorScheme.primary,
                    ),
                  ),
                ),
              ],
            ),
          ),
          if (onDuplicate != null && !isSelecting)
            PopupMenuButton<String>(
              icon: Icon(
                Icons.more_vert,
                size: 20,
                color: isDark ? Colors.white38 : Colors.black38,
              ),
              padding: EdgeInsets.zero,
              constraints: const BoxConstraints(),
              shape: RoundedRectangleBorder(
                borderRadius: BorderRadius.circular(12),
              ),
              itemBuilder: (_) => [
                const PopupMenuItem(
                  value: 'duplicate',
                  child: Row(
                    children: [
                      Icon(Icons.copy_outlined, size: 18),
                      SizedBox(width: 8),
                      Text('Duplicate'),
                    ],
                  ),
                ),
              ],
              onSelected: (value) {
                if (value == 'duplicate') onDuplicate!();
              },
            ),
        ],
      ),
    );
  }
}
