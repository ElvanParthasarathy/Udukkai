import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:elvan_niril/src/adippadai/mozhiyaakkam/k.dart';
import '../../../../adippadai/mozhiyaakkam/mozhi_vazhanguthi.dart';
import '../../../../koorugal/podhu_koorugal/elvan_thiruthi_attai_kooru.dart';
import '../../../../koorugal/ulleedugal/elvan_ulleedu_vadivamaippigal.dart';
import 'package:elvan_niril/src/koorugal/ulleedugal/elvan_thiruthi_ulleedu.dart';
import 'pattiyal_naal_kooru.dart';

import '../../../../koorugal/ulleedugal/elvan_aavana_enn_kooru.dart';

// ─────────────────────────────────────────────────────────────────────────────
// பட்டியல் தரவுகள் கூறு — Invoice Details Section (Number + Date)
// ─────────────────────────────────────────────────────────────────────────────

/// Section 2/3: Invoice number (editable) + Date picker, responsive layout.
/// Used commonly across Silk and Coolie editors.
List<Widget> buildElvanPattiyalTharavugalKooru({
  required BuildContext context,
  required WidgetRef ref,
  required bool isEditing,
  required String invoiceNumberOverride,
  required String previewInvoiceNumber,
  required String profilePrefix,
  required DateTime pattiyalNaal,
  required ValueChanged<String> onInvNumberChanged,
  required ValueChanged<DateTime> onDateChanged,
  required VoidCallback onDirty,
  String? customNumberTitle,
  String? customDateTitle,
}) {
  final cs = Theme.of(context).colorScheme;
  final tt = Theme.of(context).textTheme;

  final initialFull = invoiceNumberOverride.isNotEmpty
      ? invoiceNumberOverride
      : previewInvoiceNumber;

  final invoiceNumberField = ElvanAavanaEnnKooru(
    label: customNumberTitle ?? K.pattiyalEn.tr(context, ref),
    prefix: profilePrefix,
    initialFullNumber: initialFull,
    onFullNumberChanged: onInvNumberChanged,
    onDirty: onDirty,
  );

  final dateField = PattiyalNaalKooru(
    label: customDateTitle ?? K.naal.tr(context, ref),
    selectedDate: pattiyalNaal,
    onDateChanged: onDateChanged,
  );

  return [
    invoiceNumberField,
    dateField,
  ];
}
