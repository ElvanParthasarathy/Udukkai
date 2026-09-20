import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:elvan_niril/src/adippadai/mozhiyaakkam/k.dart';
import '../../../../adippadai/mozhiyaakkam/mozhi_vazhanguthi.dart';
import '../../../../adippadai/nilaimai/seyali_nilaimai.dart';
import '../../../../adippadai/nilaimai/achu_mozhi_facade.dart';
import '../../../../adippadai/iru_mozhi/iru_mozhi_vazhanguthigal.dart';
import '../../../../adippadai/oru_mozhi/oru_mozhi_vazhanguthigal.dart';
import '../../../../adippadai/tharavuru/seyali_murai.dart';
import '../../../../adippadai/tharavuru/uruvugal.dart';
import '../../kalanjiyam/vaangunar_nilaimai.dart';
import '../../../../koorugal/maeladukkugal/elvan_kizh_maeladukku/elvan_kizh_maeladukku.dart';
import '../thiruthi/koorugal/elvan_thiruthi_keezhvirivu.dart';

class ElvanVaangunarKeezhvirivuKooru extends ConsumerWidget {
  final int? selectedVaangunarId;
  final ValueChanged<VaangunarTharavuru?> onChanged;
  final VoidCallback? onRequestAddNew;
  final bool hideLabel;
  final bool showClearButton;

  const ElvanVaangunarKeezhvirivuKooru({
    super.key,
    required this.selectedVaangunarId,
    required this.onChanged,
    this.onRequestAddNew,
    this.hideLabel = false,
    this.showClearButton = false,
  });

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final mode = ref.watch(appModeProvider);
    final vaangunargalAsync = ref.watch(vaangunargalProvider);
    
    // Silk
    final isBilingual = ref.watch(bilingualProvider);
    final mudhanmaiMozhi = ref.watch(primaryLanguageProvider);
    final irandaamMozhi = ref.watch(secondaryLanguageProvider);
    
    // Kooli
    final kooliAchuMozhi = ref.watch(kooliAchuMozhiProvider);

    // Helpers to get Vaangunar fields
    String _getDisplayString(Map<String, dynamic>? dataMap, String targetLang, {String fallbackLang = 'en'}) {
      if (dataMap == null) return '';
      if (dataMap[targetLang] != null && dataMap[targetLang].toString().trim().isNotEmpty) {
        return dataMap[targetLang].toString();
      }
      if (dataMap[fallbackLang] != null && dataMap[fallbackLang].toString().trim().isNotEmpty) {
        return dataMap[fallbackLang].toString();
      }
      if (dataMap.values.isNotEmpty) {
        return dataMap.values.first?.toString() ?? '';
      }
      return '';
    }

    String getPrimaryName(VaangunarTharavuru v) {
      if (mode == AppMode.silk) {
        return _getDisplayString(v.peyar, mudhanmaiMozhi, fallbackLang: irandaamMozhi);
      } else {
        return _getDisplayString(v.peyar, kooliAchuMozhi);
      }
    }

    String getSecondaryName(VaangunarTharavuru v) {
      if (mode == AppMode.silk && isBilingual) {
        return _getDisplayString(v.peyar, irandaamMozhi, fallbackLang: mudhanmaiMozhi);
      } else if (mode != AppMode.silk) {
        final otherKeys = v.peyar.keys.where((k) => k != kooliAchuMozhi).toList();
        return otherKeys.isNotEmpty ? (v.peyar[otherKeys.first]?.toString() ?? '') : '';
      }
      return '';
    }

    String getOor(VaangunarTharavuru v) {
      if (mode == AppMode.silk) {
        // User requested: "the oor names can stay onlt primary lang supported by irumozhi in the silk"
        return _getDisplayString(v.oor, mudhanmaiMozhi);
      } else {
        return _getDisplayString(v.oor, kooliAchuMozhi);
      }
    }

    String getSubtitle(VaangunarTharavuru v) {
      final secondary = getSecondaryName(v);
      final oor = getOor(v);
      
      if (secondary.isNotEmpty && secondary != getPrimaryName(v)) {
        if (oor.isNotEmpty) {
          return '$secondary - $oor';
        }
        return secondary;
      }
      return oor;
    }

    bool filterSearch(VaangunarTharavuru v, String query) {
      final lowerQuery = query.toLowerCase();
      // Search in peyar (Tamil and English)
      final peyarMatches = v.peyar.values.any((val) => val != null && val.toString().toLowerCase().contains(lowerQuery));
      // Search in oor
      final oorMatches = v.oor.values.any((val) => val != null && val.toString().toLowerCase().contains(lowerQuery));
      
      return peyarMatches || oorMatches;
    }

    final placeholder = K.vaangunaraiThaerodhu.tr(context, ref);
    
    return vaangunargalAsync.when(
      data: (vaangunargal) {
        final selectedVaangunar = vaangunargal.where((v) => v.id == selectedVaangunarId).firstOrNull;
        final currentValue = selectedVaangunar != null ? getPrimaryName(selectedVaangunar) : placeholder;

        return ElvanThiruthiKeezhvirivu<VaangunarTharavuru>(
          label: placeholder,
          hideLabel: hideLabel,
          value: selectedVaangunar,
          items: vaangunargal,
          itemLabelBuilder: (ctx, ref, item) => getPrimaryName(item),
          subtitleBuilder: (ctx, ref, item) => getSubtitle(item),
          searchFilter: filterSearch,
          showSearch: true,
          onRequestAddNew: onRequestAddNew,
          onChanged: (VaangunarTharavuru newValue) {
            onChanged(newValue);
          },
          onClear: (showClearButton && selectedVaangunarId != null) ? () => onChanged(null) : null,
        );
      },
      loading: () => Container(
        height: 60,
        decoration: BoxDecoration(
          color: Theme.of(context).colorScheme.onSurface.withValues(alpha: 0.03),
          borderRadius: BorderRadius.circular(16),
        ),
        alignment: Alignment.center,
        child: const CircularProgressIndicator(),
      ),
      error: (e, st) => Container(
        height: 60,
        alignment: Alignment.center,
        child: Text(K.pizhai.tr(context, ref)),
      ),
    );
  }
}
