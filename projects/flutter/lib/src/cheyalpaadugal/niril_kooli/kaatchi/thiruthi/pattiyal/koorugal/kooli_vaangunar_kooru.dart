import 'package:elvan_niril/src/adippadai/oru_mozhi/oru_mozhi_vazhanguthigal.dart';
import 'package:elvan_niril/src/adippadai/oru_mozhi/oru_mozhi_vaangunar_udhavi.dart';
import 'package:elvan_niril/src/adippadai/tharavuru/uruvugal.dart';
import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import 'package:elvan_niril/src/adippadai/mozhiyaakkam/k.dart';
import '../../../../../../adippadai/mozhiyaakkam/mozhi_vazhanguthi.dart';

import '../../../../../../adippadai/nilaimai/seyali_nilaimai.dart';
import '../../../../../niril_podhu/kaatchi/thiruthi/koorugal/elvan_thiruthi_keezhvirivu.dart';
import '../../../../../../koorugal/podhu_koorugal/elvan_thiruthi_attai_kooru.dart';
import '../../../../../niril_podhu/kaatchi/koorugal/elvan_vaangunar_keezhvirivu_kooru.dart';
import 'package:elvan_niril/src/koorugal/ulleedugal/elvan_thiruthi_thalaippu.dart';

/// §1 Customer — client search + company dropdown + saved-details card.
class KooliVaangunarKooru extends ConsumerWidget {
  final int? selectedVaangunarId;
  final Map<String, String> selectedVaangunarPeyarMap;
  final VaangunarTharavuru? selectedVaangunar;
  final ValueChanged<VaangunarTharavuru> onVaangunarSelected;
  final VoidCallback onVaangunarCleared;

  const KooliVaangunarKooru({
    super.key,
    required this.selectedVaangunarId,
    required this.selectedVaangunarPeyarMap,
    required this.selectedVaangunar,
    required this.onVaangunarSelected,
    required this.onVaangunarCleared,
  });

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final cs = Theme.of(context).colorScheme;
    final tt = Theme.of(context).textTheme;

    return LayoutBuilder(builder: (context, constraints) {
      final customerColumn = Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          ElvanThiruthiThalaippu(label: K.vaangunarPeyarThaedu.tr(context, ref)),
          ElvanVaangunarKeezhvirivuKooru(
            selectedVaangunarId: selectedVaangunarId,
            hideLabel: true,
            showClearButton: true,
            onChanged: (vaangunar) {
              if (vaangunar == null) {
                onVaangunarCleared();
              } else {
                onVaangunarSelected(vaangunar);
              }
            },
          ),
          if (selectedVaangunar != null) ...[
            const SizedBox(height: 24),
            SizedBox(
              width: double.infinity,
              child: ElvanThiruthiAttai(
                color: cs.brightness == Brightness.light ? Colors.white : cs.onSurface.withValues(alpha: 0.08),
                borderRadius: 16,
                padding: const EdgeInsets.all(16),
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Text(K.chaemiththaTharavugal.tr(context, ref).toUpperCase(),
                      style: tt.labelSmall?.copyWith(
                        color: cs.onSurfaceVariant,
                        letterSpacing: 1.2,
                      )),
                  const SizedBox(height: 8),
                  Builder(
                    builder: (context) {
                      final kooliLang = ref.watch(kooliAchuMozhiProvider);
                      final thunaiLang = kooliLang == 'ta' ? 'en' : 'ta';
                      
                      final primaryName = OruMozhiVaangunarUdhavi.mudhanmaiPeyar(selectedVaangunar!, kooliLang).isNotEmpty 
                              ? OruMozhiVaangunarUdhavi.mudhanmaiPeyar(selectedVaangunar!, kooliLang) 
                              : OruMozhiVaangunarUdhavi.mudhanmaiPeyarFromMap(selectedVaangunarPeyarMap, kooliLang);
                              
                      final secondaryName = OruMozhiVaangunarUdhavi.thunaiPeyar(selectedVaangunar!, kooliLang).isNotEmpty 
                              ? OruMozhiVaangunarUdhavi.thunaiPeyar(selectedVaangunar!, kooliLang) 
                              : OruMozhiVaangunarUdhavi.thunaiPeyarFromMap(selectedVaangunarPeyarMap, kooliLang);

                      final titleName = secondaryName.isNotEmpty ? secondaryName : primaryName;
                      final primaryAddress = _buildAddressLines(selectedVaangunar!, kooliLang, tt, cs);
                      final secondaryAddress = _buildAddressLines(selectedVaangunar!, thunaiLang, tt, cs);

                      return Column(
                        crossAxisAlignment: CrossAxisAlignment.start,
                        children: [
                          Text(
                            titleName,
                            style: tt.bodyLarge?.copyWith(
                              fontWeight: FontWeight.w600,
                            ),
                          ),
                          ...primaryAddress,
                          
                          // Add a small spacing if there are secondary address lines
                          if (secondaryAddress.isNotEmpty) ...[
                            const SizedBox(height: 6),
                            ...secondaryAddress,
                          ],
                        ],
                      );
                    },
                  ),
                  // GSTIN
                  if (selectedVaangunar!.gstin.trim().isNotEmpty) ...[
                    const SizedBox(height: 6),
                    Text(
                      'GSTIN: ${selectedVaangunar!.gstin.trim()}',
                      style: tt.bodySmall?.copyWith(
                        color: cs.primary,
                        fontWeight: FontWeight.w600,
                      ),
                    ),
                  ],
                ],
              ),
            ),
          ),
          ],
        ],
      );

      final isDesktop = MediaQuery.sizeOf(context).width >= 800;
      if (isDesktop) {
        return Align(
          alignment: Alignment.centerLeft,
          child: ConstrainedBox(
            constraints: const BoxConstraints(maxWidth: 360),
            child: customerColumn,
          ),
        );
      }
      return customerColumn;
    });
  }

  /// Builds combined address lines for a given language key.
  List<Widget> _buildAddressLines(
    VaangunarTharavuru v,
    String key,
    TextTheme tt,
    ColorScheme cs,
  ) {
    final parts = <String>[];
    // Fallback to Tamil if requested key is empty
    final oor = (v.oor[key]?.isNotEmpty == true ? v.oor[key] : v.oor['ta'] ?? '').trim();
    final maavattam = (v.maavattam[key]?.isNotEmpty == true ? v.maavattam[key] : v.maavattam['ta'] ?? '').trim();
    final combined = [
      if (oor.isNotEmpty) oor,
      if (maavattam.isNotEmpty) maavattam,
    ].join(', ');
    if (combined.isNotEmpty) parts.add(combined);

    final maanilam = (v.maanilam[key]?.isNotEmpty == true ? v.maanilam[key] : v.maanilam['ta'] ?? '').trim();
    final naadu = (v.naadu[key]?.isNotEmpty == true ? v.naadu[key] : v.naadu['ta'] ?? '').trim();
    final stateLine = [
      if (maanilam.isNotEmpty) maanilam,
      if (naadu.isNotEmpty) naadu,
    ].join(', ');
    if (stateLine.isNotEmpty) parts.add(stateLine);

    if (parts.isEmpty) return [];

    final style = tt.bodySmall?.copyWith(
      color: cs.onSurfaceVariant,
      height: 1.5,
    );

    return [
      const SizedBox(height: 6),
      ...parts.map((line) => Text(line, style: style)),
    ];
  }
}
