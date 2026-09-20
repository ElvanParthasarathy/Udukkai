import 'package:elvan_niril/src/adippadai/nilaimai/achu_mozhi_facade.dart';
import 'package:elvan_niril/src/adippadai/tharavuru/uruvugal.dart';
import 'package:flutter/cupertino.dart';
import 'package:flutter/material.dart';
import 'package:flutter_svg/flutter_svg.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../../../adippadai/mozhiyaakkam/k.dart';
import '../../../../adippadai/mozhiyaakkam/mozhi_vazhanguthi.dart';
import '../../../../adippadai/nilaimai/seyali_nilaimai.dart';
import '../../../../adippadai/nilaimai/thaedal_nilaimai.dart';
import '../../../../adippadai/vazhikaattal/niril_nav.dart';
import '../../../../koorugal/maeladukkugal/elvan_cheyal_maeladukku.dart';
import '../../../chattagam/kaatchi/koorugal/elvan_uyir_valai.dart';
import '../../../niril_podhu/kalanjiyam/vaangunar_nilaimai.dart';
import 'package:elvan_niril/src/koorugal/podhu_koorugal/elvan_pothu_attai.dart';
import '../thiruthi/vaangunar/niril_pattu_vaangunar_thiruthi.dart';
import '../koorugal/elvan_pattu_tharavu_pattiyal.dart';
import '../../../niril_podhu/kaatchi/paarvai/vaangunar_paarvai.dart';
class SilkMerchantsPage extends ConsumerWidget {
  const SilkMerchantsPage({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final query = ref.watch(silkMerchantsSearchQueryProvider).toLowerCase();
    final vaangunargalAsync = ref.watch(vaangunargalProvider);
    final primaryLang = ref.watch(primaryLanguageProvider);
    final secondaryLang = ref.watch(secondaryLanguageProvider);
    final isDark = Theme.of(context).brightness == Brightness.dark;


    return ElvanPattuTharavuPattiyal<VaangunarTharavuru>(
      dataAsync: vaangunargalAsync,
      searchQuery: query,
      onFilter: (v, q) {
        final pName = (v.peyar[primaryLang] ?? '').toLowerCase();
        final sName = (v.peyar[secondaryLang] ?? '').toLowerCase();
        final pCity = (v.oor[primaryLang] ?? '').toLowerCase();
        final gstin = v.gstin.toLowerCase();
        final phone = v.tholaipaesi.toLowerCase();
        final email = v.minnanjal.toLowerCase();
        return pName.contains(q) ||
            sName.contains(q) ||
            pCity.contains(q) ||
            gstin.contains(q) ||
            phone.contains(q) ||
            email.contains(q);
      },
      emptyIcon: CupertinoIcons.person_2,
      emptyTitle: K.vaangunargalIllai.tr(context, ref),
      emptySubtitle: K.vaangunaraiChaerkkavum.tr(context, ref),
      itemId: (v) => v.id,
      selectionModeProvider: vaangunarSelectionModeProvider,
      selectedIdsProvider: selectedVaangunarIdsProvider,
      onItemTap: (context, vaangunar) {
        Navigator.of(context).push(
          MaterialPageRoute(
            builder: (_) => VaangunarPaarvai(
              vaangunar: vaangunar,
              achuMozhi: primaryLang,
              onEdit: () {
                Navigator.of(context).pushReplacement(
                  MaterialPageRoute(
                    builder: (_) => SilkMerchantEditor(vaangunar: vaangunar),
                  ),
                );
              },
            ),
          ),
        );
      },
      childAspectRatio: 2.8,
      mobileItemHeight: 100,
      cardBuilder: (context, ref, vaangunar, index, isSelecting, isSelected, onTap, onLongPress) {
        return _SilkVaangunarCard(
          index: index,
          vaangunar: vaangunar,
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


// ── Silk Merchant Card ──────────────────────────────────────────────────────

class _SilkVaangunarCard extends StatelessWidget {
  const _SilkVaangunarCard({
    required this.index,
    required this.vaangunar,
    required this.primaryLang,
    required this.secondaryLang,
    required this.isDark,
    required this.isSelecting,
    required this.isSelected,
    required this.onTap,
    required this.onLongPress,
  });

  final int index;
  final VaangunarTharavuru vaangunar;
  final String primaryLang;
  final String secondaryLang;
  final bool isDark;
  final bool isSelecting;
  final bool isSelected;
  final VoidCallback onTap;
  final VoidCallback onLongPress;

  @override
  Widget build(BuildContext context) {
    final primaryName = vaangunar.peyar[primaryLang] ?? '';
    final secondaryName = vaangunar.peyar[secondaryLang] ?? '';
    final primaryCity = vaangunar.oor[primaryLang] ?? '';
    final secondaryCity = vaangunar.oor[secondaryLang] ?? '';
    final gstin = vaangunar.gstin;
    final phone = vaangunar.tholaipaesi;

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
            // Content
            Expanded(
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Text(
                    primaryName,
                    style: const TextStyle(
                      fontSize: 15.2,
                      fontWeight: FontWeight.w700,
                    ),
                    maxLines: 1,
                    overflow: TextOverflow.ellipsis,
                  ),
                  if (secondaryName.isNotEmpty &&
                      secondaryName != primaryName) ...[
                    const SizedBox(height: 2),
                    Text(
                      secondaryName,
                      style: TextStyle(
                        fontSize: 13,
                        fontWeight: FontWeight.w500,
                        color: isDark ? Colors.white54 : Colors.black54,
                      ),
                      maxLines: 1,
                      overflow: TextOverflow.ellipsis,
                    ),
                  ],
                  if (primaryCity.isNotEmpty) ...[
                    const SizedBox(height: 3),
                    Text(
                      primaryCity,
                      style: TextStyle(
                        fontSize: 13.6,
                        color: isDark ? Colors.white38 : Colors.black38,
                      ),
                      maxLines: 1,
                      overflow: TextOverflow.ellipsis,
                    ),
                  ],
                  if (secondaryCity.isNotEmpty &&
                      secondaryCity != primaryCity) ...[
                    const SizedBox(height: 2),
                    Text(
                      secondaryCity,
                      style: TextStyle(
                        fontSize: 12.8,
                        color: (isDark ? Colors.white30 : Colors.black.withValues(alpha: 0.30))
                            .withValues(alpha: 0.8),
                      ),
                      maxLines: 1,
                      overflow: TextOverflow.ellipsis,
                    ),
                  ],
                  if (gstin.isNotEmpty || phone.isNotEmpty) ...[
                    const SizedBox(height: 4),
                    Text(
                      [if (gstin.isNotEmpty) gstin, if (phone.isNotEmpty) phone]
                          .join(' · '),
                      style: TextStyle(
                        fontSize: 13.6,
                        color: isDark ? Colors.white38 : Colors.black38,
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
