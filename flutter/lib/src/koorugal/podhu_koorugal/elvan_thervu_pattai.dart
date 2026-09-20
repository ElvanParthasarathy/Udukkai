import 'package:flutter/cupertino.dart';
import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../adippadai/mozhiyaakkam/k.dart';
import '../../adippadai/mozhiyaakkam/mozhi_vazhanguthi.dart';
import '../../cheyalpaadugal/chattagam/kaatchi/kaippaesi/elvan_chattagam.dart';
import 'elvan_kizh_pattai_base.dart';

/// A shared bulk selection action bar used across list screens.
class ElvanThervuPattai extends ConsumerWidget {
  const ElvanThervuPattai({
    super.key,
    required this.selectedCount,
    required this.onSelectAll,
    required this.onDelete,
    required this.onCancel,
    this.isExpanded = true,
  });

  final int selectedCount;
  final VoidCallback onSelectAll;
  final VoidCallback onDelete;
  final VoidCallback onCancel;
  final bool isExpanded;

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final bool effectiveExpanded = ElvanOverlayState.of(context)?.isExpanded ?? isExpanded;
    final isDark = Theme.of(context).brightness == Brightness.dark;

    return ElvanKizhPattaiBase(
      horizontalPadding: 16.0,
      verticalPadding: 6.0,
      child: AnimatedOpacity(
        duration: const Duration(milliseconds: 200),
        opacity: effectiveExpanded ? 1.0 : 0.0,
        child: Row(
          mainAxisAlignment: MainAxisAlignment.spaceEvenly,
          children: [
            _buildAction(
              context: context,
              ref: ref,
              icon: selectedCount > 0
                  ? CupertinoIcons.checkmark_square_fill
                  : CupertinoIcons.square,
              label: selectedCount > 0
                  ? '$selectedCount ${K.thaerndhedu.tr(context, ref)}'
                  : K.anaithaiyumTheriPtn.tr(context, ref),
              onTap: onSelectAll,
              isDark: isDark,
              isActive: selectedCount > 0,
            ),
            _buildAction(
              context: context,
              ref: ref,
              icon: CupertinoIcons.trash,
              label: K.neekkuPtn.tr(context, ref),
              onTap: selectedCount > 0 ? onDelete : null,
              isDark: isDark,
              isDisabled: selectedCount == 0,
            ),
            _buildAction(
              context: context,
              ref: ref,
              icon: CupertinoIcons.xmark_circle,
              label: K.kaividuPtn.tr(context, ref),
              onTap: onCancel,
              isDark: isDark,
            ),
          ],
        ),
      ),
    );
  }

  Widget _buildAction({
    required BuildContext context,
    required WidgetRef ref,
    required IconData icon,
    required String label,
    required VoidCallback? onTap,
    required bool isDark,
    bool isActive = false,
    bool isDisabled = false,
  }) {
    final color = isDisabled
        ? (isDark ? Colors.white24 : Colors.black26)
        : (isDark ? Colors.white : Colors.black87);

    const double iconSize = 23.0;
    const double fontSize = 9.5;
    const double layoutWidth = 67.0; // Same as Navbar with <= 4 items

    return GestureDetector(
      onTap: onTap,
      behavior: HitTestBehavior.opaque,
      child: SizedBox(
        width: layoutWidth,
        child: Column(
          mainAxisSize: MainAxisSize.min,
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            Icon(icon, size: iconSize, color: color),
            const SizedBox(height: 2),
            Padding(
              padding: const EdgeInsets.symmetric(horizontal: 4.0),
              child: FittedBox(
                fit: BoxFit.scaleDown,
                child: Text(
                  label,
                  style: TextStyle(
                    fontSize: fontSize,
                    fontWeight: isActive ? FontWeight.w600 : FontWeight.w400,
                    color: color,
                    height: 1.2,
                  ),
                ),
              ),
            ),
          ],
        ),
      ),
    );
  }
}
