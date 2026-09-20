import 'package:elvan_niril/src/adippadai/mozhiyaakkam/mozhi_vazhanguthi.dart';
import 'package:elvan_niril/src/koorugal/ulleedugal/elvan_thiruthi_ulleedu.dart';
import 'package:elvan_niril/src/adippadai/nilaimai/achu_mozhi_facade.dart';
import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

/// A bilingual text field specifically for Kooli mode editors.
/// It permanently displays both Tamil and English inputs, regardless of global settings,
/// while reading/writing to the standard `Map<String, String>` format.
class ElvanKooliIrumozhiPulan extends ConsumerWidget {
  const ElvanKooliIrumozhiPulan({
    super.key,
    required this.label,
    required this.value,
    required this.onChanged,
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
    final isDesktop = MediaQuery.sizeOf(context).width >= 800;

    final primaryKey = ref.watch(primaryLanguageProvider);
    final secondaryKey = ref.watch(secondaryLanguageProvider);

    final primaryLabel = '$label (${primaryKey.toLowerCase().tr(context, ref)})';
    final secondaryLabel = '$label (${secondaryKey.toLowerCase().tr(context, ref)})';

    final primaryValue = value[primaryKey] ?? '';
    final secondaryValue = value[secondaryKey] ?? '';

    final primaryWidget = ElvanThiruthiUlleedu(
      key: ValueKey(primaryKey),
      label: primaryLabel,
      initialValue: primaryValue,
      autofocus: autofocus,
      enabled: enabled,
      maxLines: maxLines,
      textCapitalization: textCapitalization,
      onChanged: (text) {
        final updated = Map<String, String>.from(value);
        updated[primaryKey] = text;
        onChanged(updated);
      },
    );

    final secondaryWidget = ElvanThiruthiUlleedu(
      key: ValueKey(secondaryKey),
      label: secondaryLabel,
      initialValue: secondaryValue,
      enabled: enabled,
      maxLines: maxLines,
      textCapitalization: textCapitalization,
      onChanged: (text) {
        final updated = Map<String, String>.from(value);
        updated[secondaryKey] = text;
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
}
