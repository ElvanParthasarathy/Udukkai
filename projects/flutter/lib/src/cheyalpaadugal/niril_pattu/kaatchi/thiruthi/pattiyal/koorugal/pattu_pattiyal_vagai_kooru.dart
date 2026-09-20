import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:elvan_niril/src/adippadai/mozhiyaakkam/k.dart';
import '../../../../../../adippadai/mozhiyaakkam/mozhi_vazhanguthi.dart';
import '../../../../../niril_podhu/kaatchi/thiruthi/koorugal/elvan_thiruthi_keezhvirivu.dart';

/// பட்டியல் வகை கூறு — Invoice Type dropdown (Tax Invoice / Proforma).
class PattuPattiyalVagaiKooru extends ConsumerWidget {
  const PattuPattiyalVagaiKooru({
    super.key,
    required this.pattiyalVagai,
    required this.onChanged,
  });

  /// Current invoice type — `'tax-invoice'` or `'proforma'`.
  final String pattiyalVagai;

  /// Fires when the user picks a different invoice type.
  final ValueChanged<String?> onChanged;

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final types = ['tax-invoice', 'proforma'];
    return Align(
      alignment: Alignment.centerLeft,
      child: ConstrainedBox(
        constraints: const BoxConstraints(maxWidth: 400),
      child: ElvanThiruthiKeezhvirivu<String>(
        label: K.pattiyalVagai.tr(context, ref),
        hideLabel: true,
        value: types.contains(pattiyalVagai) ? pattiyalVagai : 'tax-invoice',
        items: types,
        itemLabelBuilder: (ctx, ref, item) {
          if (item == 'tax-invoice') return K.varipPattiyal.tr(context, ref);
          return K.munvaraivu.tr(context, ref);
        },
        showSearch: false,
        onChanged: (newValue) {
          if (newValue != null) {
            onChanged(newValue);
          }
        },
      ),
    ));
  }
}
