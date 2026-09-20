import 'package:elvan_niril/src/adippadai/tharavuru/uruvugal.dart';
import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../../../niril_podhu/kaatchi/thiruthi/patru_thiruthi.dart';

import '../../../../niril_podhu/kaatchi/paarvai/patrucheettu_paarvai.dart';
import 'package:elvan_niril/src/adippadai/nilaimai/achu_mozhi_facade.dart';
import '../../../../amaippugal/tharavu/kooli_niruvana_tharavugal_provider.dart';

/// Coolie Receipt Editor — thin wrapper around the shared PatruThiruthi.
class CoolieReceiptEditor extends ConsumerWidget {
  final PatrugalTharavuru? editingEntry;

  const CoolieReceiptEditor({super.key, this.editingEntry});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final primaryLang = ref.watch(primaryLanguageProvider);
    final profile = ref.watch(kooliNiruvanaTharavugalProvider);

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
                    builder: (_) => CoolieReceiptEditor(editingEntry: saved),
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
