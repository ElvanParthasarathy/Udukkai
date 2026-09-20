import 'package:flutter/material.dart';
import '../../../../chattagam/kaatchi/kaippaesi/elvan_kizh_pattai.dart';
import '../../../../chattagam/kaatchi/kaippaesi/koorugal/elvan_maatri.dart';

/// A sleek, compact pill shifter specifically designed for editor (thiruthi) forms.
class ElvanThiruthiMaatri extends StatelessWidget {
  const ElvanThiruthiMaatri({
    super.key,
    required this.labels,
    required this.currentIndex,
    required this.onValueChanged,
    this.label,
  });

  /// The text labels for the toggle options.
  final List<String> labels;

  /// The currently selected index.
  final int currentIndex;

  /// Callback when the value changes.
  final ValueChanged<int> onValueChanged;

  /// Optional label text to display above the pill shifter.
  final String? label;

  @override
  Widget build(BuildContext context) {
    final shifter = ElvanPillShifter(
      isSmall: true,
      isFullWidth: true,
      items: labels.map((l) => CustomNavItem(label: l)).toList(),
      currentIndex: currentIndex,
      onValueChanged: onValueChanged,
    );

    if (label == null) return shifter;

    final isDark = Theme.of(context).brightness == Brightness.dark;
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      mainAxisSize: MainAxisSize.min,
      children: [
        Padding(
          padding: const EdgeInsets.only(left: 16.0, bottom: 8.0),
          child: Text(
            label!,
            style: TextStyle(
              fontSize: 12,
              fontWeight: FontWeight.w400,
              letterSpacing: 0.3,
              color: Theme.of(context)
                  .colorScheme
                  .onSurface
                  .withValues(alpha: 0.5),
            ),
          ),
        ),
        shifter,
      ],
    );
  }
}
