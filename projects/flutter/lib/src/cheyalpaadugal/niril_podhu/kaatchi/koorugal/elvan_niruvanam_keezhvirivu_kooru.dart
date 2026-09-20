import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:elvan_niril/src/adippadai/mozhiyaakkam/k.dart';
import '../../../../adippadai/mozhiyaakkam/mozhi_vazhanguthi.dart';
import '../../../../adippadai/nilaimai/seyali_nilaimai.dart';
import '../../../../adippadai/nilaimai/achu_mozhi_facade.dart';
import '../../../../adippadai/iru_mozhi/iru_mozhi_vazhanguthigal.dart';
import '../../../../adippadai/iru_mozhi/iru_mozhi_niruvanam_udhavi.dart';
import '../../../../adippadai/oru_mozhi/oru_mozhi_vazhanguthigal.dart';
import '../../../../adippadai/oru_mozhi/oru_mozhi_niruvanam_udhavi.dart';
import '../../../amaippugal/tharavu/niruvana_tharavugal.dart';
import '../../../amaippugal/tharavu/niruvana_tharavugal_provider.dart';
import '../thiruthi/koorugal/elvan_thiruthi_keezhvirivu.dart';
import '../../../../adippadai/tharavuru/seyali_murai.dart';

class ElvanNiruvanamKeezhvirivuKooru extends ConsumerWidget {
  final int? selectedNiruvanamId;
  final ValueChanged<NiruvanaTharavugal?> onChanged;
  final bool hideLabel;
  final bool showClearButton;

  const ElvanNiruvanamKeezhvirivuKooru({
    super.key,
    required this.selectedNiruvanamId,
    required this.onChanged,
    this.hideLabel = false,
    this.showClearButton = false,
  });

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final profiles = ref.watch(NiruvanaTharavugalListProvider);
    final mode = ref.watch(appModeProvider);
    
    // Silk
    final isBilingual = ref.watch(bilingualProvider);
    final mudhanmaiMozhi = ref.watch(primaryLanguageProvider);
    final irandaamMozhi = ref.watch(secondaryLanguageProvider);
    
    // Kooli
    final kooliAchuMozhi = ref.watch(kooliAchuMozhiProvider);

    String getPrimary(NiruvanaTharavugal p) {
      if (mode == AppMode.silk) {
        return IruMozhiNiruvanamUdhavi.mudhanmaiPeyar(p, mudhanmaiMozhi, irandaamMozhi);
      } else {
        return OruMozhiNiruvanamUdhavi.mudhanmaiPeyar(p, kooliAchuMozhi);
      }
    }

    String getSecondary(NiruvanaTharavugal p) {
      if (mode == AppMode.silk) {
        return IruMozhiNiruvanamUdhavi.thunaiPeyar(p, isBilingual, true, irandaamMozhi);
      } else {
        return OruMozhiNiruvanamUdhavi.thunaiPeyar(p, kooliAchuMozhi);
      }
    }

    final placeholder = K.niruvanaththaithThaernhedu.tr(context, ref);
    final currentValue = selectedNiruvanamId == null
        ? null
        : profiles.firstWhere(
            (p) => p.id == selectedNiruvanamId,
            orElse: () => profiles.first,
          );

    return ElvanThiruthiKeezhvirivu<NiruvanaTharavugal>(
      label: placeholder,
      hideLabel: hideLabel,
      value: currentValue,
      items: profiles,
      itemLabelBuilder: (ctx, ref, item) => getPrimary(item),
      subtitleBuilder: (ctx, ref, item) => getSecondary(item),
      showSearch: false,
      onChanged: (NiruvanaTharavugal newValue) {
        onChanged(newValue);
      },
      onClear: (showClearButton && selectedNiruvanamId != null) ? () => onChanged(null) : null,
    );
  }
}
