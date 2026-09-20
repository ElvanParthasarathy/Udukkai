import 'package:flutter/material.dart';
import 'package:flutter/services.dart';

import '../../../../../../koorugal/podhu_koorugal/elvan_thiruthi_attai_kooru.dart';
import '../../../../../../koorugal/ulleedugal/elvan_ulleedu_vadivamaippigal.dart';
import '../../../../../../koorugal/ulleedugal/elvan_thiruthi_ulleedu.dart';

/// Extra-charge field using ElvanThiruthiUlleedu.
Widget kooliChargeField(
    String label, TextEditingController ctrl, ValueChanged<String> onChanged) {
  return ElvanThiruthiUlleedu(
    controller: ctrl,
    label: label,
    keyboardType: const TextInputType.numberWithOptions(decimal: true),
    inputFormatters: ElvanVadivamaippigal.thasamamEnngal,
    onChanged: onChanged,
  );
}

/// Bank detail key-value row.
Widget kooliBankRow(BuildContext context, String label, dynamic value) {
  String text = '';
  if (value is Map) {
    text = (value['ta'] ?? value['en'] ?? '').toString();
  } else {
    text = value?.toString() ?? '';
  }
  if (text.isEmpty) return const SizedBox.shrink();
  return Padding(
    padding: const EdgeInsets.symmetric(vertical: 4),
    child: Row(
      children: [
        SizedBox(
          width: 80,
          child: Text(label,
              style: TextStyle(
                color: Theme.of(context).colorScheme.onSurfaceVariant,
                fontSize: 13,
              )),
        ),
        Expanded(
            child: Text(text,
                style: const TextStyle(fontWeight: FontWeight.w500))),
      ],
    ),
  );
}

/// Pill-shaped text button (borderRadius 24).
Widget kooliPillButton(
  BuildContext context, {
  required IconData icon,
  required String label,
  required VoidCallback onPressed,
}) {
  final cs = Theme.of(context).colorScheme;
  final isDark = Theme.of(context).brightness == Brightness.dark;
  return TextButton.icon(
    onPressed: onPressed,
    icon: Icon(icon, size: 20),
    label: Text(label, style: const TextStyle(fontWeight: FontWeight.w600)),
    style: TextButton.styleFrom(
      foregroundColor: cs.onSurface,
      backgroundColor: isDark ? Colors.white.withValues(alpha: 0.08) : Colors.white,
    ),
  );
}

/// Totals summary row (label | value).
Widget kooliTotalsRow(
  String label,
  String value, {
  FontWeight? labelWeight,
  FontWeight? valueWeight,
  Color? labelColor,
}) {
  return LayoutBuilder(
    builder: (context, constraints) {
      return FittedBox(
        fit: BoxFit.scaleDown,
        child: ConstrainedBox(
          constraints: BoxConstraints(minWidth: constraints.maxWidth),
          child: Row(
            mainAxisAlignment: MainAxisAlignment.spaceBetween,
            children: [
              Text(label,
                  style: TextStyle(
                    fontWeight: labelWeight ?? FontWeight.w500,
                    color: labelColor,
                  )),
              const SizedBox(width: 12),
              Text(value,
                  style: TextStyle(
                    fontWeight: valueWeight ?? FontWeight.w600,
                  )),
            ],
          ),
        ),
      );
    },
  );
}
