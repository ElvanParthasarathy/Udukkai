import 'package:elvan_niril/src/adippadai/oru_mozhi/oru_mozhi_vazhanguthigal.dart';
import 'package:elvan_niril/src/cheyalpaadugal/amaippugal/tharavu/kooli_niruvana_tharavugal_provider.dart';
import 'package:elvan_niril/src/adippadai/tharavuru/uruvugal.dart';
import '../../../amaippugal/tharavu/niruvana_tharavugal_provider.dart';
import 'package:flutter/cupertino.dart';
import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:intl/intl.dart';

import '../../../../adippadai/mozhiyaakkam/k.dart';
import '../../../../adippadai/mozhiyaakkam/mozhi_vazhanguthi.dart';
import '../../../../adippadai/nilaimai/seyali_nilaimai.dart';
import '../../../../adippadai/nilaimai/thaedal_nilaimai.dart';
import '../../../../adippadai/tharavuthalam/seyali_tharavuthalam.dart';
import '../../../../koorugal/maeladukkugal/elvan_cheyal_maeladukku.dart';
import '../../../chattagam/kaatchi/koorugal/elvan_uyir_valai.dart';
import '../../../niril_podhu/kalanjiyam/pattiyal_nilaimai.dart';

import 'package:elvan_niril/src/koorugal/podhu_koorugal/elvan_pothu_attai.dart';
import '../thiruthi/pattiyal/niril_kooli_pattiyal_thiruthi.dart';
import '../koorugal/elvan_kooli_tharavu_pattiyal.dart';
import '../koorugal/kooli_pattiyal_attai.dart';
import '../paarvai/kooli_pattiyal_paarvai.dart';

/// Coolie invoice list — real DB-backed view.
/// Shows invoices grouped by business profile with search, selection, and
/// tap-to-edit navigation.
class CoolieInvoicesPage extends ConsumerWidget {
  const CoolieInvoicesPage({super.key});

  static final _dateFormat = DateFormat('dd/MM/yyyy');
  static final _currencyFormat =
      NumberFormat.currency(locale: 'en_IN', symbol: '₹');

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final query = ref.watch(coolieInvoicesSearchQueryProvider).toLowerCase();
    final pattiyalgalAsync = ref.watch(pattiyalgalProvider);
    final profilesAsync = ref.watch(kooliNiruvanaTharavugalListProvider);
    final isDark = Theme.of(context).brightness == Brightness.dark;

    final primaryLang = ref.watch(kooliAchuMozhiProvider);
    final secondaryLang = primaryLang == 'ta' ? 'en' : 'ta';

    return ElvanKooliTharavuPattiyal<PattiyalTharavuru>(
      dataAsync: pattiyalgalAsync,
      searchQuery: query,
      onFilter: (p, q) {
        final en = p.patrucheettuEn.toLowerCase();
        final peyarPrimary = (p.vaangunarPeyar[primaryLang] ?? '').toLowerCase();
        final peyarSecondary = (p.vaangunarPeyar[secondaryLang] ?? '').toLowerCase();
        return en.contains(q) || 
               peyarPrimary.contains(q) || 
               peyarSecondary.contains(q);
      },
      emptyIcon: CupertinoIcons.doc_text,
      emptyTitle: K.pattiyalgalIllai.tr(context, ref),
      emptySubtitle: K.pudhiyaPattiyalPtn.tr(context, ref),
      itemId: (p) => p.id,
      selectionModeProvider: pattiyalSelectionModeProvider,
      selectedIdsProvider: selectedPattiyalIdsProvider,
      onItemTap: (context, p) {
        Navigator.of(context).push(
          MaterialPageRoute(
            builder: (_) => KooliPattiyalPaarvai(
              pattiyal: p,
            ),
          ),
        );
      },
      groupBy: (p) => p.niruvanamId,
      groupHeaderBuilder: (context, niruvanamId) {
        final profiles = profilesAsync;
        String sectionName;
        if (niruvanamId == null) {
          sectionName = 'General';
        } else {
          final profile = profiles
              .where((p) => p.id == niruvanamId)
              .firstOrNull;
          sectionName = profile?.kurumPeyar ?? 'General';
        }
        return SliverToBoxAdapter(
          child: Padding(
            padding: const EdgeInsets.only(top: 16, bottom: 8),
            child: Text(
              sectionName,
              style: TextStyle(
                fontSize: 13,
                fontWeight: FontWeight.w700,
                letterSpacing: 0.5,
                color: isDark
                    ? Colors.white.withValues(alpha: 0.5)
                    : Colors.black.withValues(alpha: 0.4),
              ),
            ),
          ),
        );
      },
      cardBuilder: (context, ref, p, index, isSelecting, isSelected, onTap, onLongPress) {
        return KooliPattiyalAttai(
          index: index,
          pattiyal: p,
          isSelecting: isSelecting,
          isSelected: isSelected,
          onTap: onTap,
          onLongPress: onLongPress,
        );
      },
    );
  }
}

