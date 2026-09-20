import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import 'package:elvan_niril/src/adippadai/mozhiyaakkam/k.dart';
import '../../../../../../adippadai/mozhiyaakkam/mozhi_vazhanguthi.dart';

import 'kooli_thiruthi_udhavigal.dart';

/// §3.5 Extra Charges — setharam / ahimsa silk / courier fields in a
/// responsive bento grid layout.
class KooliMelthogaiKooru extends ConsumerWidget {
  final TextEditingController setharamCtrl;
  final TextEditingController ahimsaCtrl;
  final TextEditingController thabaalCtrl;
  final ValueChanged<String> onSetharamChanged;
  final ValueChanged<String> onAhimsaChanged;
  final ValueChanged<String> onThabaalChanged;

  const KooliMelthogaiKooru({
    super.key,
    required this.setharamCtrl,
    required this.ahimsaCtrl,
    required this.thabaalCtrl,
    required this.onSetharamChanged,
    required this.onAhimsaChanged,
    required this.onThabaalChanged,
  });

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    return Align(
      alignment: Alignment.centerLeft,
      child: ConstrainedBox(
        constraints: const BoxConstraints(maxWidth: 400),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.stretch,
          children: [
            kooliChargeField(
                K.chaedhaaramGiraam.tr(context, ref), setharamCtrl, onSetharamChanged),
            const SizedBox(height: 12),
            Row(
              children: [
                Expanded(
                  child: kooliChargeField(
                      K.ahimsaiPattuThogai.tr(context, ref), ahimsaCtrl, onAhimsaChanged),
                ),
                const SizedBox(width: 12),
                Expanded(
                  child: kooliChargeField(
                      K.koriyarKattanam.tr(context, ref), thabaalCtrl, onThabaalChanged),
                ),
              ],
            ),
          ],
        ),
      ),
    );
  }
}
