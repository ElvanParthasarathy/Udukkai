import 'package:elvan_niril/src/adippadai/oru_mozhi/oru_mozhi_vazhanguthigal.dart';
import 'package:elvan_niril/src/cheyalpaadugal/amaippugal/tharavu/kooli_niruvana_tharavugal_provider.dart';
import 'package:elvan_niril/src/adippadai/tharavuru/uruvugal.dart';
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
import '../../../niril_podhu/kalanjiyam/patru_nilaimai.dart';
import '../../../niril_podhu/tharavuru/seluthi_vagai.dart';
import 'package:elvan_niril/src/koorugal/podhu_koorugal/elvan_pothu_attai.dart';
import '../thiruthi/patrucheettu/niril_kooli_patrucheettu_thiruthi.dart';
import '../koorugal/elvan_kooli_tharavu_pattiyal.dart';
import '../../../niril_podhu/kaatchi/paarvai/patrucheettu_paarvai.dart';

/// Coolie Receipt List — real DB-backed view showing payment receipts.
/// Completely separate from invoice list.
class CoolieReceiptsPage extends ConsumerWidget {
  const CoolieReceiptsPage({super.key});

  static final _dateFormat = DateFormat('dd/MM/yyyy');
  static final _currencyFormat =
      NumberFormat.currency(locale: 'en_IN', symbol: '₹');

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final query = ref.watch(coolieReceiptsSearchQueryProvider).toLowerCase();
    final patrugalAsync = ref.watch(patrugalProvider);
    final profilesAsync = ref.watch(kooliNiruvanaTharavugalListProvider);
    final isDark = Theme.of(context).brightness == Brightness.dark;

    final primaryLang = ref.watch(kooliAchuMozhiProvider);
    final secondaryLang = primaryLang == 'ta' ? 'en' : 'ta';

    return ElvanKooliTharavuPattiyal<PatrugalTharavuru>(
      dataAsync: patrugalAsync,
      searchQuery: query,
      onFilter: (p, q) {
        final en = p.patruEn.toLowerCase();
        final peyarPrimary = (p.vaangunarPeyar[primaryLang] ?? '').toLowerCase();
        final peyarSecondary = (p.vaangunarPeyar[secondaryLang] ?? '').toLowerCase();
        final vagai = p.seluthumMurai.toLowerCase();
        return en.contains(q) ||
            peyarPrimary.contains(q) ||
            peyarSecondary.contains(q) ||
            vagai.contains(q);
      },
      emptyIcon: CupertinoIcons.doc_text,
      emptyTitle: K.patrucheettugalIllai.tr(context, ref),
      emptySubtitle: K.pudhiyaPatrucheettuPtn.tr(context, ref),
      itemId: (p) => p.id,
      selectionModeProvider: patruSelectionModeProvider,
      selectedIdsProvider: selectedPatruIdsProvider,
      onItemTap: (context, p) {
        final profile = profilesAsync.where((prof) => prof.id == p.niruvanamId).firstOrNull ?? 
            (profilesAsync.isNotEmpty ? profilesAsync.first : null);
        Navigator.of(context).push(
          MaterialPageRoute(
            builder: (_) => PatrucheettuPaarvai(
              patru: p,
              achuMozhi: primaryLang,
              profile: profile,
              onEdit: () {
                Navigator.of(context).pushReplacement(
                  MaterialPageRoute(
                    builder: (_) => CoolieReceiptEditor(editingEntry: p),
                  ),
                );
              },
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
        return Padding(
          padding: const EdgeInsets.only(bottom: 8),
          child: _PatruCard(
            patru: p,
            isDark: isDark,
            isSelecting: isSelecting,
            isSelected: isSelected,
            dateFormat: _dateFormat,
            currencyFormat: _currencyFormat,
            primaryLang: primaryLang,
            secondaryLang: secondaryLang,
            onTap: onTap,
            onLongPress: onLongPress,
          ),
        );
      },
    );
  }
}

// ── Receipt Card ────────────────────────────────────────────────────────────

class _PatruCard extends ConsumerWidget {
  const _PatruCard({
    required this.patru,
    required this.isDark,
    required this.isSelecting,
    required this.isSelected,
    required this.dateFormat,
    required this.currencyFormat,
    required this.primaryLang,
    required this.secondaryLang,
    required this.onTap,
    required this.onLongPress,
  });

  final PatrugalTharavuru patru;
  final bool isDark;
  final bool isSelecting;
  final bool isSelected;
  final DateFormat dateFormat;
  final NumberFormat currencyFormat;
  final String primaryLang;
  final String secondaryLang;
  final VoidCallback onTap;
  final VoidCallback onLongPress;

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final mode = SeluthiVagaiX.fromStored(patru.seluthumMurai);

    return ElvanPothuAttai(
      onTap: onTap,
      onLongPress: onLongPress,
      isSelected: isSelected,
      padding: const EdgeInsets.symmetric(horizontal: 14, vertical: 12),
      borderRadius: 14.0,
      child: Row(
        children: [
            // Selection checkbox
            if (isSelecting) ...[
              Icon(
                isSelected
                    ? CupertinoIcons.checkmark_circle_fill
                    : CupertinoIcons.circle,
                size: 22,
                color: isSelected
                    ? (isDark ? Colors.white : Colors.black)
                    : (isDark ? Colors.white30 : Colors.black26),
              ),
              const SizedBox(width: 12),
            ],
            // Content
            Expanded(
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                mainAxisAlignment: MainAxisAlignment.center,
                children: [
                  // Row 1: Customer name + date
                  Row(
                    children: [
                      Expanded(
                        child: Text(
                          patru.vaangunarPeyar[primaryLang]?.isNotEmpty == true
                              ? patru.vaangunarPeyar[primaryLang]!
                              : patru.vaangunarPeyar[secondaryLang] ?? patru.patruEn,
                          style: const TextStyle(
                            fontSize: 15,
                            fontWeight: FontWeight.w600,
                          ),
                          maxLines: 1,
                          overflow: TextOverflow.ellipsis,
                        ),
                      ),
                      const SizedBox(width: 8),
                      Text(
                        dateFormat.format(patru.patruNaal),
                        style: TextStyle(
                          fontSize: 12,
                          color: isDark ? Colors.white38 : Colors.black38,
                        ),
                      ),
                    ],
                  ),
                  const SizedBox(height: 4),
                  // Row 2: Receipt number + payment mode badge + amount
                  Row(
                    children: [
                      Text(
                        patru.patruEn,
                        style: TextStyle(
                          fontSize: 12,
                          color: isDark ? Colors.white38 : Colors.black45,
                        ),
                      ),
                      if (mode != null) ...[
                        const SizedBox(width: 8),
                        Container(
                          padding: const EdgeInsets.symmetric(
                              horizontal: 8, vertical: 2),
                          decoration: BoxDecoration(
                            color: mode
                                .badgeColor(isDark)
                                .withValues(alpha: 0.15),
                            borderRadius: BorderRadius.circular(6),
                          ),
                          child: Text(
                            mode.label(context, ref),
                            style: TextStyle(
                              fontSize: 10,
                              fontWeight: FontWeight.w600,
                              color: mode.badgeColor(isDark),
                            ),
                          ),
                        ),
                      ],
                      const Spacer(),
                      Text(
                        currencyFormat.format(patru.thogai),
                        style: TextStyle(
                          fontSize: 14,
                          fontWeight: FontWeight.w600,
                          color: isDark
                              ? Colors.green.shade300
                              : Colors.green.shade700,
                        ),
                      ),
                    ],
                  ),
                ],
              ),
            ),
            // Chevron
            if (!isSelecting) ...[
              const SizedBox(width: 8),
              Icon(
                CupertinoIcons.chevron_right,
                size: 14,
                color: isDark ? Colors.white24 : Colors.black26,
              ),
            ],
          ],
        ),
      );
  }
}
