import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../../../adippadai/nilaimai/seyali_nilaimai.dart';
import '../../../../adippadai/nilaimai/thaedal_nilaimai.dart';
import '../../../../adippadai/tharavuru/seyali_murai.dart';
import '../../../../adippadai/mozhiyaakkam/k.dart';
import '../../../../adippadai/mozhiyaakkam/mozhi_vazhanguthi.dart';

import '../../../niril_podhu/kalanjiyam/pattiyal_nilaimai.dart';
import '../../../niril_podhu/kalanjiyam/patru_nilaimai.dart';
import '../../../niril_podhu/kalanjiyam/vaangunar_nilaimai.dart';
import '../../../niril_podhu/kalanjiyam/porul_nilaimai.dart';
import '../../../../adippadai/oru_mozhi/oru_mozhi_vazhanguthigal.dart';
import '../../../../adippadai/iru_mozhi/iru_mozhi_vazhanguthigal.dart';

import '../../../../koorugal/podhu_koorugal/elvan_thervu_pattai.dart';
import '../../../../koorugal/maeladukkugal/elvan_cheyal_maeladukku.dart';
import '../../../../koorugal/podhu_koorugal/elvan_siruseidhi.dart';

import '../../../../adippadai/vazhikaattal/navigation_provider.dart';

class SelectionOverlayHelper {
  static Widget? buildOverlay(BuildContext context, WidgetRef ref, int tabIndex, NirilNavigationState navState, {bool isExpanded = true}) {
    final mode = ref.watch(appModeProvider) ?? AppMode.silk;
    
    if (tabIndex == 1) { // Invoices / Receipts
      if (navState.uruvakkuSegment == 0) { // Invoices
        final isSelecting = ref.watch(pattiyalSelectionModeProvider);
        if (!isSelecting) return null;
        
        final selectedIds = ref.watch(selectedPattiyalIdsProvider);
        
        return ElvanThervuPattai(
          key: const ValueKey('selection_bar'),
          isExpanded: isExpanded,
          selectedCount: selectedIds.length,
          onSelectAll: () {
            // Need to get filtered IDs
            final query = mode == AppMode.coolie 
                ? ref.read(coolieInvoicesSearchQueryProvider) 
                : ref.read(silkInvoicesSearchQueryProvider);
            final items = ref.read(pattiyalgalProvider).value ?? [];
            final primaryLang = ref.read(mode == AppMode.coolie ? kooliAchuMozhiProvider : silkMudhanmaiMozhiProvider);
            final secondaryLang = primaryLang == 'ta' ? 'en' : 'ta';
            
            final filtered = query.isEmpty ? items : items.where((p) {
              final en = p.patrucheettuEn.toLowerCase();
              final peyarPrimary = (p.vaangunarPeyar[primaryLang] ?? '').toLowerCase();
              final peyarSecondary = (p.vaangunarPeyar[secondaryLang] ?? '').toLowerCase();
              return en.contains(query) || peyarPrimary.contains(query) || peyarSecondary.contains(query);
            }).toList();
            
            ref.read(selectedPattiyalIdsProvider.notifier).state = filtered.map((e) => e.id).toSet();
          },
          onDelete: () => _deletePattiyalgal(context, ref, selectedIds.toList()),
          onCancel: () {
            ref.read(pattiyalSelectionModeProvider.notifier).state = false;
            ref.read(selectedPattiyalIdsProvider.notifier).state = {};
          },
        );
      } else { // Receipts
        final isSelecting = ref.watch(patruSelectionModeProvider);
        if (!isSelecting) return null;
        
        final selectedIds = ref.watch(selectedPatruIdsProvider);
        
        return ElvanThervuPattai(
          key: const ValueKey('selection_bar'),
          isExpanded: isExpanded,
          selectedCount: selectedIds.length,
          onSelectAll: () {
            final query = mode == AppMode.coolie 
                ? ref.read(coolieReceiptsSearchQueryProvider) 
                : ref.read(silkReceiptsSearchQueryProvider);
            final items = ref.read(patrugalProvider).value ?? [];
            final primaryLang = ref.read(mode == AppMode.coolie ? kooliAchuMozhiProvider : silkMudhanmaiMozhiProvider);
            final secondaryLang = primaryLang == 'ta' ? 'en' : 'ta';
            
            final filtered = query.isEmpty ? items : items.where((p) {
              final en = p.patruEn.toLowerCase();
              final peyarPrimary = (p.vaangunarPeyar[primaryLang] ?? '').toLowerCase();
              final peyarSecondary = (p.vaangunarPeyar[secondaryLang] ?? '').toLowerCase();
              return en.contains(query) || peyarPrimary.contains(query) || peyarSecondary.contains(query);
            }).toList();
            
            ref.read(selectedPatruIdsProvider.notifier).state = filtered.map((e) => e.id).toSet();
          },
          onDelete: () => _deletePatrucheettugal(context, ref, selectedIds.toList()),
          onCancel: () {
            ref.read(patruSelectionModeProvider.notifier).state = false;
            ref.read(selectedPatruIdsProvider.notifier).state = {};
          },
        );
      }
    } else if (tabIndex == 2) { // Products
      final isSelecting = ref.watch(porulSelectionModeProvider);
      if (!isSelecting) return null;
      
      final selectedIds = ref.watch(selectedPorulIdsProvider);
      
      return ElvanThervuPattai(
        key: const ValueKey('selection_bar'),
        isExpanded: isExpanded,
        selectedCount: selectedIds.length,
        onSelectAll: () {
          final query = mode == AppMode.coolie 
              ? ref.read(coolieItemsSearchQueryProvider) 
              : ref.read(silkItemsSearchQueryProvider);
          final items = ref.read(porulgalProvider).value ?? [];
          final primaryLang = ref.read(mode == AppMode.coolie ? kooliAchuMozhiProvider : silkMudhanmaiMozhiProvider);
            final secondaryLang = primaryLang == 'ta' ? 'en' : 'ta';
            
          final filtered = query.isEmpty ? items : items.where((p) {
            final peyarPrimary = (p.porulPeyar[primaryLang] ?? '').toLowerCase();
            final peyarSecondary = (p.porulPeyar[secondaryLang] ?? '').toLowerCase();
            return peyarPrimary.contains(query) || peyarSecondary.contains(query);
          }).toList();
          
          ref.read(selectedPorulIdsProvider.notifier).state = filtered.map((e) => e.id).toSet();
        },
        onDelete: () => _deletePorulgal(context, ref, selectedIds.toList()),
        onCancel: () {
          ref.read(porulSelectionModeProvider.notifier).state = false;
          ref.read(selectedPorulIdsProvider.notifier).state = {};
        },
      );
    } else if (tabIndex == 3) { // Merchants
      final isSelecting = ref.watch(vaangunarSelectionModeProvider);
      if (!isSelecting) return null;
      
      final selectedIds = ref.watch(selectedVaangunarIdsProvider);
      
      return ElvanThervuPattai(
        key: const ValueKey('selection_bar'),
        isExpanded: isExpanded,
        selectedCount: selectedIds.length,
        onSelectAll: () {
          final query = mode == AppMode.coolie 
              ? ref.read(coolieMerchantsSearchQueryProvider) 
              : ref.read(silkMerchantsSearchQueryProvider);
          final items = ref.read(vaangunargalProvider).value ?? [];
          final primaryLang = ref.read(mode == AppMode.coolie ? kooliAchuMozhiProvider : silkMudhanmaiMozhiProvider);
          final secondaryLang = primaryLang == 'ta' ? 'en' : 'ta';
            
          final filtered = query.isEmpty ? items : items.where((p) {
            final peyarPrimary = (p.peyar[primaryLang] ?? '').toLowerCase();
            final peyarSecondary = (p.peyar[secondaryLang] ?? '').toLowerCase();
            return peyarPrimary.contains(query) || peyarSecondary.contains(query);
          }).toList();
          
          ref.read(selectedVaangunarIdsProvider.notifier).state = filtered.map((e) => e.id).toSet();
        },
        onDelete: () => _deleteVaangunargal(context, ref, selectedIds.toList()),
        onCancel: () {
          ref.read(vaangunarSelectionModeProvider.notifier).state = false;
          ref.read(selectedVaangunarIdsProvider.notifier).state = {};
        },
      );
    }
    
    return null;
  }

  static void _deletePattiyalgal(BuildContext context, WidgetRef ref, List<int> ids) {
    if (ids.isEmpty) return;
    
    showElvanActionSheet(
      context: context,
      title: '${ids.length} ${K.pattiyalgal.tr(context, ref)}',
      cancelText: K.kaividuPtn.tr(context, ref),
      confirmText: K.neekkuPtn.tr(context, ref),
      confirmColor: Colors.red,
      onConfirm: () {
        ref.read(pattiyalKalanjiyamProvider).bulkDeletePattiyalgal(ids);
        ref.invalidate(pattiyalgalProvider);
        
        ref.read(pattiyalSelectionModeProvider.notifier).state = false;
        ref.read(selectedPattiyalIdsProvider.notifier).state = {};
        
        ElvanSnackbar.show(context, '${ids.length} ${K.pattiyalgal.tr(context, ref)} ${K.azhikkiradhu.tr(context, ref)}');
      },
    );
  }

  static void _deletePatrucheettugal(BuildContext context, WidgetRef ref, List<int> ids) {
    if (ids.isEmpty) return;
    
    showElvanActionSheet(
      context: context,
      title: '${ids.length} ${K.patrucheettugal.tr(context, ref)}',
      cancelText: K.kaividuPtn.tr(context, ref),
      confirmText: K.neekkuPtn.tr(context, ref),
      confirmColor: Colors.red,
      onConfirm: () {
        ref.read(patruKalanjiyamProvider).bulkDeletePatrugal(ids);
        ref.invalidate(patrugalProvider);
        
        ref.read(patruSelectionModeProvider.notifier).state = false;
        ref.read(selectedPatruIdsProvider.notifier).state = {};
        
        ElvanSnackbar.show(context, '${ids.length} ${K.patrucheettugal.tr(context, ref)} ${K.azhikkiradhu.tr(context, ref)}');
      },
    );
  }

  static void _deletePorulgal(BuildContext context, WidgetRef ref, List<int> ids) {
    if (ids.isEmpty) return;
    
    showElvanActionSheet(
      context: context,
      title: '${ids.length} ${K.porulAzhikkappattadhu.tr(context, ref)}',
      cancelText: K.kaividuPtn.tr(context, ref),
      confirmText: K.neekkuPtn.tr(context, ref),
      confirmColor: Colors.red,
      onConfirm: () {
        ref.read(porulKalanjiyamProvider).bulkDeletePorulgal(ids);
        ref.invalidate(porulgalProvider);
        
        ref.read(porulSelectionModeProvider.notifier).state = false;
        ref.read(selectedPorulIdsProvider.notifier).state = {};
        
        ElvanSnackbar.show(context, '${ids.length} ${K.porulAzhikkappattadhu.tr(context, ref)}');
      },
    );
  }

  static void _deleteVaangunargal(BuildContext context, WidgetRef ref, List<int> ids) {
    if (ids.isEmpty) return;
    
    showElvanActionSheet(
      context: context,
      title: '${ids.length} ${K.vaangunarAzhikkappattadhu.tr(context, ref)}',
      cancelText: K.kaividuPtn.tr(context, ref),
      confirmText: K.neekkuPtn.tr(context, ref),
      confirmColor: Colors.red,
      onConfirm: () {
        ref.read(vaangunarKalanjiyamProvider).bulkDeleteVaangunargal(ids);
        ref.invalidate(vaangunargalProvider);
        
        ref.read(vaangunarSelectionModeProvider.notifier).state = false;
        ref.read(selectedVaangunarIdsProvider.notifier).state = {};
        
        ElvanSnackbar.show(context, '${ids.length} ${K.vaangunarAzhikkappattadhu.tr(context, ref)}');
      },
    );
  }
}
