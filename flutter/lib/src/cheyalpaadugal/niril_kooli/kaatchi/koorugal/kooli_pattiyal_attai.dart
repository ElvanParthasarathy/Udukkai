import 'package:flutter/material.dart';
import 'package:flutter/cupertino.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:intl/intl.dart';

import '../../../../adippadai/tharavuru/uruvugal.dart';
import '../../../../adippadai/oru_mozhi/oru_mozhi_vazhanguthigal.dart';
import '../../../../adippadai/oru_mozhi/oru_mozhi_vaangunar_udhavi.dart';
import '../../../../koorugal/podhu_koorugal/elvan_pothu_attai.dart';

/// Dedicated reusable Invoice Card for Kooli Invoice Lists
class KooliPattiyalAttai extends ConsumerWidget {
  const KooliPattiyalAttai({
    super.key,
    required this.index,
    required this.pattiyal,
    required this.onTap,
    this.isSelecting = false,
    this.isSelected = false,
    this.onLongPress,
  });

  final int index;
  final PattiyalTharavuru pattiyal;
  final VoidCallback onTap;
  final bool isSelecting;
  final bool isSelected;
  final VoidCallback? onLongPress;

  static final _dateFormat = DateFormat('dd/MM/yyyy');
  static final _currencyFormat =
      NumberFormat.currency(locale: 'en_IN', symbol: '₹');

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final isDark = Theme.of(context).brightness == Brightness.dark;
    final p = pattiyal;
    final amountStr = _currencyFormat.format(p.mothaThogai);

    final kooliAchuMozhi = ref.watch(kooliAchuMozhiProvider);
    
    final mudhanmaiPeyar = OruMozhiVaangunarUdhavi.mudhanmaiPeyarFromMap(
      p.vaangunarPeyar.cast<String, dynamic>(), 
      kooliAchuMozhi
    );
    final thunaiPeyar = OruMozhiVaangunarUdhavi.thunaiPeyarFromMap(
      p.vaangunarPeyar.cast<String, dynamic>(), 
      kooliAchuMozhi
    );
    final showThunai = thunaiPeyar.isNotEmpty && thunaiPeyar != mudhanmaiPeyar;
    
    final mudhanmaiOor = OruMozhiVaangunarUdhavi.mudhanmaiPeyarFromMap(
      p.vaangunarMunvari.cast<String, dynamic>(), 
      kooliAchuMozhi
    );

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
                Row(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Expanded(
                      child: Column(
                        crossAxisAlignment: CrossAxisAlignment.start,
                        children: [
                          Text(
                            mudhanmaiPeyar.isNotEmpty ? mudhanmaiPeyar : '-',
                            style: const TextStyle(
                              fontSize: 16.8,
                              fontWeight: FontWeight.w800,
                            ),
                          ),
                          if (showThunai) ...[
                            const SizedBox(height: 2),
                            Text(
                              thunaiPeyar,
                              style: TextStyle(
                                fontSize: 13.6,
                                color: isDark ? Colors.white54 : Colors.black54,
                              ),
                            ),
                          ],
                        ],
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
                const SizedBox(height: 8),
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
                const SizedBox(height: 4),
                Row(
                  mainAxisAlignment: MainAxisAlignment.spaceBetween,
                  crossAxisAlignment: CrossAxisAlignment.end,
                  children: [
                    Expanded(
                      child: Text(
                        mudhanmaiOor,
                        style: TextStyle(
                          fontSize: 13.6,
                          color: isDark ? Colors.white54 : Colors.black54,
                        ),
                        maxLines: 1,
                        overflow: TextOverflow.ellipsis,
                      ),
                    ),
                    Text(
                      amountStr,
                      style: TextStyle(
                        fontSize: amountStr.length > 11 ? 12.0 : 14.4,
                        fontWeight: FontWeight.w800,
                        color: isDark ? Colors.white : Colors.black,
                      ),
                    ),
                  ],
                ),
              ],
            ),
          ),
        ],
      ),
    );
  }
}
