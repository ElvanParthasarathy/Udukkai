import 'package:elvan_niril/src/adippadai/tharavuru/uruvugal.dart';
import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../../../niril_podhu/kaatchi/thiruthi/patru_thiruthi.dart';

import '../../../../niril_podhu/kaatchi/paarvai/patrucheettu_paarvai.dart';
import 'package:elvan_niril/src/adippadai/mozhiyaakkam/mozhi_vazhanguthi.dart';
import '../../../../amaippugal/tharavu/pattu_niruvana_tharavugal_provider.dart';

/// Silk Receipt Editor — thin wrapper around the shared PatruThiruthi.
class SilkReceiptEditor extends ConsumerWidget {
  final PatrugalTharavuru? editingEntry;

  const SilkReceiptEditor({super.key, this.editingEntry});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final currentLocale = ref.watch(localeProvider);
    final profile = ref.watch(pattuNiruvanaTharavugalProvider);
    final effectiveLang =
        currentLocale?.languageCode ?? Localizations.localeOf(context).languageCode;
    final primaryLang = effectiveLang == 'ta' ? 'ta' : 'en';

    return PatruThiruthi(
      editingEntry: editingEntry,
      onSaved: (context, saved) {
        Navigator.of(context).pushReplacement(
          MaterialPageRoute(
            builder: (_) => PatrucheettuPaarvai(
              patru: saved,
              achuMozhi: primaryLang,
              profile: profile,
              onEdit: () {
                Navigator.of(context).push(
                  MaterialPageRoute(
                    builder: (_) => SilkReceiptEditor(editingEntry: saved),
                  ),
                );
              },
            ),
          ),
        );
      },
    );
  }
}
