import 'package:elvan_niril/src/adippadai/nilaimai/achu_mozhi_facade.dart';
import 'package:elvan_niril/src/adippadai/iru_mozhi/iru_mozhi_vazhanguthigal.dart';
import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../adippadai/mozhiyaakkam/mozhi_vazhanguthi.dart';
import 'package:elvan_niril/src/koorugal/ulleedugal/elvan_thiruthi_ulleedu.dart';

/// A reusable bilingual text field widget.
/// Renders 1 or 2 TextFields based on the app's bilingual setting.
/// Reads/writes data as a `Map<String, String>` compatible with `MozhiMapConverter`.
class ElvanIrumozhiPulan extends ConsumerWidget {
  const ElvanIrumozhiPulan({
    super.key,
    required this.label,
    required this.value,
    required this.onChanged,
    this.primaryController,
    this.secondaryController,
    this.autofocus = false,
    this.textCapitalization = TextCapitalization.words,
    this.enabled = true,
    this.maxLines = 1,
  });

  /// Label displayed above the fields (e.g. 'Product Name').
  final String label;

  /// Current value as a language map: {"Tamil": "...", "English": "..."}.
  final Map<String, String> value;

  /// Called whenever either field changes.
  final ValueChanged<Map<String, String>> onChanged;

  /// Optional controllers for external control.
  final TextEditingController? primaryController;
  final TextEditingController? secondaryController;

  /// Whether the primary field should autofocus.
  final bool autofocus;

  /// Text capitalization for both fields.
  final TextCapitalization textCapitalization;

  /// Whether the fields are enabled.
  final bool enabled;

  /// The maximum number of lines for the text fields.
  final int maxLines;

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final isBilingual = ref.watch(bilingualProvider);
    final primaryLang = ref.watch(primaryLanguageProvider);
    final secondaryLang = ref.watch(secondaryLanguageProvider);

    final translatedPrimaryLang = primaryLang.toLowerCase().tr(context, ref);
    final translatedSecondaryLang = secondaryLang.toLowerCase().tr(context, ref);

    final primaryValue = value[primaryLang] ?? '';
    final secondaryValue = value[secondaryLang] ?? '';

    final isDark = Theme.of(context).brightness == Brightness.dark;

    final isDesktop = MediaQuery.sizeOf(context).width >= 800;

    final primaryWidget = _buildTextField(
      key: ValueKey(primaryLang),
      context: context,
      isDark: isDark,
      label: '$label ($translatedPrimaryLang)',
      initialValue: primaryValue,
      controller: primaryController,
      autofocus: autofocus,
      enabled: enabled,
      maxLines: maxLines,
      onChanged: (text) {
        final updated = Map<String, String>.from(value);
        updated[primaryLang] = text;
        onChanged(updated);
      },
    );

    if (!isBilingual) {
      return primaryWidget;
    }

    final secondaryWidget = _buildTextField(
      key: ValueKey(secondaryLang),
      context: context,
      isDark: isDark,
      label: '$label ($translatedSecondaryLang)',
      initialValue: secondaryValue,
      controller: secondaryController,
      enabled: enabled,
      maxLines: maxLines,
      onChanged: (text) {
        final updated = Map<String, String>.from(value);
        updated[secondaryLang] = text;
        onChanged(updated);
      },
    );

    if (isDesktop) {
      return Row(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Expanded(child: primaryWidget),
          const SizedBox(width: 16),
          Expanded(child: secondaryWidget),
        ],
      );
    }

    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        primaryWidget,
        const SizedBox(height: 12),
        secondaryWidget,
      ],
    );
  }

  Widget _buildTextField({
    Key? key,
    required BuildContext context,
    required bool isDark,
    required String label,
    required String initialValue,
    TextEditingController? controller,
    bool autofocus = false,
    bool enabled = true,
    int maxLines = 1,
    required ValueChanged<String> onChanged,
  }) {
    return ElvanThiruthiUlleedu(
      key: key,
      label: label,
      initialValue: initialValue,
      controller: controller,
      autofocus: autofocus,
      enabled: enabled,
      maxLines: maxLines,
      textCapitalization: textCapitalization,
      onChanged: onChanged,
    );
  }
}
