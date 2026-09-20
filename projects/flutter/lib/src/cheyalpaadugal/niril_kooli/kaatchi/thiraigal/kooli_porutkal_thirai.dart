import 'package:elvan_niril/src/adippadai/nilaimai/achu_mozhi_facade.dart';
import 'package:elvan_niril/src/adippadai/tharavuru/uruvugal.dart';
import 'package:flutter/cupertino.dart';
import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../../../adippadai/mozhiyaakkam/k.dart';
import '../../../../adippadai/mozhiyaakkam/mozhi_vazhanguthi.dart';
import '../../../../adippadai/nilaimai/seyali_nilaimai.dart';
import '../../../../adippadai/nilaimai/thaedal_nilaimai.dart';
import '../../../../adippadai/vazhikaattal/niril_nav.dart';
import '../../../../koorugal/maeladukkugal/elvan_cheyal_maeladukku.dart';
import '../../../chattagam/kaatchi/koorugal/elvan_uyir_valai.dart';
import '../../../niril_podhu/kalanjiyam/porul_nilaimai.dart';
import 'package:elvan_niril/src/koorugal/podhu_koorugal/elvan_pothu_attai.dart';
import '../thiruthi/porul/niril_kooli_porul_thiruthi.dart';
import '../koorugal/elvan_kooli_tharavu_pattiyal.dart';
import '../../../niril_podhu/kaatchi/paarvai/porul_paarvai.dart';

class CoolieItemsPage extends ConsumerWidget {
  const CoolieItemsPage({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final query = ref.watch(coolieItemsSearchQueryProvider).toLowerCase();
    final porulgalAsync = ref.watch(porulgalProvider);
    final primaryLang = ref.watch(primaryLanguageProvider);
    final secondaryLang = ref.watch(secondaryLanguageProvider);
    final isDark = Theme.of(context).brightness == Brightness.dark;

    return ElvanKooliTharavuPattiyal<PorulTharavuru>(
      dataAsync: porulgalAsync,
      searchQuery: query,
      onFilter: (p, q) {
        final primary = (p.porulPeyar[primaryLang] ?? '').toLowerCase();
        final secondary = (p.porulPeyar[secondaryLang] ?? '').toLowerCase();
        return primary.contains(q) || secondary.contains(q);
      },
      emptyIcon: CupertinoIcons.cube_box,
      emptyTitle: K.porulgalIllai.tr(context, ref),
      emptySubtitle: K.porulaiChaerkkavum.tr(context, ref),
      itemId: (p) => p.id,
      selectionModeProvider: porulSelectionModeProvider,
      selectedIdsProvider: selectedPorulIdsProvider,
      onItemTap: (context, p) {
        Navigator.of(context).push(
          MaterialPageRoute(
            builder: (_) => PorulPaarvai(
              porul: p,
              achuMozhi: primaryLang,
              onEdit: () {
                Navigator.of(context).pushReplacement(
                  MaterialPageRoute(
                    builder: (_) => CoolieItemEditor(product: p),
                  ),
                );
              },
            ),
          ),
        );
      },
      childAspectRatio: 3.5,
      mobileItemHeight: 72,
      cardBuilder: (context, ref, p, index, isSelecting, isSelected, onTap, onLongPress) {
        return _CooliePorulCard(
          index: index,
          porul: p,
          primaryLang: primaryLang,
          secondaryLang: secondaryLang,
          isDark: isDark,
          isSelecting: isSelecting,
          isSelected: isSelected,
          onTap: onTap,
          onLongPress: onLongPress,
        );
      },
    );
  }
}


// ── Product Card Widget ─────────────────────────────────────────────────────

class _CooliePorulCard extends StatelessWidget {
  const _CooliePorulCard({
    required this.index,
    required this.porul,
    required this.primaryLang,
    required this.secondaryLang,
    required this.isDark,
    required this.isSelecting,
    required this.isSelected,
    required this.onTap,
    required this.onLongPress,
  });

  final int index;
  final PorulTharavuru porul;
  final String primaryLang;
  final String secondaryLang;
  final bool isDark;
  final bool isSelecting;
  final bool isSelected;
  final VoidCallback onTap;
  final VoidCallback onLongPress;

  @override
  Widget build(BuildContext context) {
    final primary = porul.porulPeyar[primaryLang] ?? '';
    final secondary = porul.porulPeyar[secondaryLang] ?? '';

    return ElvanPothuAttai(
      onTap: onTap,
      onLongPress: onLongPress,
      isSelected: isSelected,
      padding: const EdgeInsets.all(16),
      borderRadius: 24.0,
      child: Row(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
            // Index circle / selection checkbox
            Container(
              width: 28,
              height: 28,
              decoration: BoxDecoration(
                shape: BoxShape.circle,
                color: (isSelecting && isSelected)
                    ? (isDark ? Colors.white : Colors.black)
                    : (isDark
                        ? Colors.white.withValues(alpha: 0.12)
                        : Colors.black.withValues(alpha: 0.08)),
              ),
              alignment: Alignment.center,
              child: (isSelecting && isSelected)
                  ? Icon(
                      CupertinoIcons.checkmark_alt,
                      size: 16,
                      color: isDark ? Colors.black : Colors.white,
                    )
                  : Text(
                      (index + 1).toString().padLeft(2, '0'),
                      style: TextStyle(
                        fontWeight: FontWeight.w800,
                        fontSize: 11.2,
                        color: isDark ? Colors.white : Colors.black,
                        height: 1,
                      ),
                    ),
            ),

            const SizedBox(width: 12),

            // Product name
            Expanded(
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                mainAxisAlignment: MainAxisAlignment.center,
                children: [
                  Text(
                    primary,
                    style: const TextStyle(
                      fontSize: 15.2,
                      fontWeight: FontWeight.w700,
                    ),
                    maxLines: 1,
                    overflow: TextOverflow.ellipsis,
                  ),
                  if (secondary.isNotEmpty) ...[
                    const SizedBox(height: 2),
                    Text(
                      secondary,
                      style: TextStyle(
                        fontSize: 13,
                        fontWeight: FontWeight.w500,
                        color: isDark ? Colors.white54 : Colors.black54,
                      ),
                      maxLines: 1,
                      overflow: TextOverflow.ellipsis,
                    ),
                  ],
                ],
              ),
            ),
          ],
        ),
      );
  }
}
