import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:elvan_niril/src/adippadai/mozhiyaakkam/k.dart';
import '../../../../../../adippadai/mozhiyaakkam/mozhi_vazhanguthi.dart';

import '../../../../../../koorugal/podhu_koorugal/elvan_thiruthi_attai_kooru.dart';
import '../../../../../../koorugal/ulleedugal/elvan_ulleedu_vadivamaippigal.dart';
import '../../../../../../koorugal/ulleedugal/elvan_thiruthi_ulleedu.dart';

/// கழிவு கூறு — Global Discount row with a text field and
/// percentage / amount toggle (pill).
class PattuThallupadiKooru extends ConsumerWidget {
  const PattuThallupadiKooru({
    super.key,
    required this.controller,
    required this.discountType,
    required this.onValueChanged,
    required this.onTypeChanged,
  });

  /// Controller for the discount value text field.
  final TextEditingController controller;

  /// Current discount mode — `'%'` or `'₹'`.
  final String discountType;

  /// Fires when the user edits the discount value text.
  final ValueChanged<String> onValueChanged;

  /// Fires when the user toggles between % and ₹.
  final ValueChanged<String> onTypeChanged;

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final cs = Theme.of(context).colorScheme;

    return Align(
      alignment: Alignment.centerLeft,
      child: ConstrainedBox(
        constraints: const BoxConstraints(maxWidth: 400),
        child: ElvanThiruthiUlleedu(
          controller: controller,
          label: K.muzhuThallupadi.tr(context, ref),
          keyboardType: const TextInputType.numberWithOptions(decimal: true),
          inputFormatters: ElvanVadivamaippigal.thasamamEnngal,
          onChanged: onValueChanged,
          suffixIcon: Padding(
            padding: const EdgeInsets.only(right: 6.0),
            child: Material(
              color: Colors.transparent,
              shape: const CircleBorder(),
              clipBehavior: Clip.antiAlias,
              child: InkWell(
                onTap: () {
                  final newType = discountType == '%' ? '₹' : '%';
                  onTypeChanged(newType);
                },
                child: SizedBox(
                  width: 32,
                  height: 32,
                  child: Center(
                    child: Text(
                      discountType == '%' ? '%' : '₹',
                      style: const TextStyle(fontWeight: FontWeight.bold, fontSize: 16),
                      textAlign: TextAlign.center,
                    ),
                  ),
                ),
              ),
            ),
          ),
        ),
      ),
    );
  }
}
