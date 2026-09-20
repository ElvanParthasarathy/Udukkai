import 'package:elvan_niril/src/adippadai/tharavuru/uruvugal.dart';
import 'dart:async';
import 'dart:convert';

import 'package:flutter/material.dart';
import 'package:shared_preferences/shared_preferences.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../../../../../adippadai/mozhiyaakkam/k.dart';
import '../../../../../../adippadai/mozhiyaakkam/mozhi_vazhanguthi.dart';
import '../../../../../../adippadai/mozhiyaakkam/mozhi_vazhanguthi.dart';

import '../../../../../niril_podhu/tharavuru/pattiyal_tharavuru.dart';
import '../../../../../niril_podhu/kalanjiyam/pattiyal_kalanjiyam.dart';
import '../../../../../../adippadai/idangal_kalanjiyam/idangal_kalanjiyam.dart';
import '../../../../../../koorugal/maeladukkugal/elvan_cheyal_maeladukku.dart';

// ─────────────────────────────────────────────────────────────────────────────
// பட்டு பட்டியல் உதவி — Helper for load / save / draft logic
// ─────────────────────────────────────────────────────────────────────────────

/// Immutable snapshot of all editor state fields.
/// Used to transfer data between helper ↔ widget without tight coupling.
class PattuThiruththiNilaimai {
  const PattuThiruththiNilaimai({
    this.selectedNiruvanamId,
    this.selectedVaangunarId,
    this.selectedVaangunarPeyarMap = const {},
    this.selectedVaangunarMunvariMap = const {},
    this.customerState = '',
    this.pattiyalVagai = 'tax-invoice',
    required this.pattiyalNaal,
    this.placeOfSupply = '',
    this.placeOfSupplyTa = '',
    this.invoiceNumberOverride = '',
    this.items = const [],
    this.globalDiscountValue = 0,
    this.globalDiscountType = '%',
  });

  final int? selectedNiruvanamId;
  final int? selectedVaangunarId;
  final Map<String, String> selectedVaangunarPeyarMap;
  final Map<String, String> selectedVaangunarMunvariMap;
  final String customerState;
  final String pattiyalVagai;
  final DateTime pattiyalNaal;
  final String placeOfSupply;
  final String placeOfSupplyTa;
  final String invoiceNumberOverride;
  final List<PattuUrupadi> items;
  final double globalDiscountValue;
  final String globalDiscountType;
}

/// Pure data helper — no widget dependency. Handles:
///   • Loading from an existing PattiyalTharavuru (edit / duplicate)
///   • Saving (create + update) via PattiyalKalanjiyam
///   • Draft persistence via SharedPreferences
class PattuPattiyalUthavi {
  PattuPattiyalUthavi._();

  static const _draftKey = 'niril_draft_silk_invoice';

  // ── Load from entry (edit OR duplicate) ──────────────────────────────────

  /// Parses a [PattiyalTharavuru] into a state snapshot.
  /// When [isDuplicate] is true, the invoice number is cleared and date is
  /// set to now (creating a fresh copy).
  static PattuThiruththiNilaimai loadFromEntry(
    PattiyalTharavuru entry, {
    bool isDuplicate = false,
  }) {
    // Parse items
    var items = PattiyalUthavigal.pattuListFromJson(entry.tharavugal);
    if (items.isEmpty) items = [const PattuUrupadi()];

    // Parse settings
    double globalDiscountValue = 0;
    String globalDiscountType = '%';
    String placeOfSupply = '';
    String placeOfSupplyTa = '';
    try {
      final settings =
          jsonDecode(entry.sonthaViruppangal) as Map<String, dynamic>;
      globalDiscountValue =
          (settings['globalDiscountValue'] as num?)?.toDouble() ?? 0;
      globalDiscountType =
          settings['globalDiscountType'] as String? ?? '%';
      placeOfSupply = settings['placeOfSupply'] as String? ?? '';
      placeOfSupplyTa = settings['placeOfSupplyTa'] as String? ?? '';
    } catch (_) {}

    // Resolve Tamil for Place of Supply if missing
    if (placeOfSupply.isNotEmpty && placeOfSupplyTa.isEmpty) {
      final match = indhiyaMaanilangal.where(
        (s) => (s['en'] ?? '').toLowerCase() == placeOfSupply.toLowerCase(),
      ).firstOrNull;
      if (match != null) placeOfSupplyTa = match['ta'] ?? '';
    }

    return PattuThiruththiNilaimai(
      selectedNiruvanamId: entry.niruvanamId,
      selectedVaangunarId: entry.vaangunarId,
      selectedVaangunarPeyarMap: entry.vaangunarPeyar.cast<String, String>(),
      selectedVaangunarMunvariMap: entry.vaangunarMunvari.cast<String, String>(),
      pattiyalVagai: entry.pattiyalVagai,
      pattiyalNaal: isDuplicate ? DateTime.now() : entry.pattiyalNaal,
      placeOfSupply: placeOfSupply,
      placeOfSupplyTa: placeOfSupplyTa,
      invoiceNumberOverride: isDuplicate ? '' : entry.patrucheettuEn,
      items: items,
      globalDiscountValue: globalDiscountValue,
      globalDiscountType: globalDiscountType,
    );
  }

  // ── Save ─────────────────────────────────────────────────────────────────

  /// Saves the invoice (create or update) and returns the generated number. Returns error string if any.
  static Future<dynamic> save({
    required PattiyalKalanjiyam kalanjiyam,
    required PattuThiruththiNilaimai state,
    required PattuMothangal totals,
    required String profilePrefix,
    PattiyalTharavuru? editingEntry,
  }) async {
    final now = DateTime.now();
    final finYear = now.month >= 4 ? now.year : now.year - 1;
    final validItems =
        state.items.where((i) => i.alavu > 0 && i.vilai > 0).toList();

    final settingsJson = jsonEncode({
      'globalDiscountValue': state.globalDiscountValue,
      'globalDiscountType': state.globalDiscountType,
      'placeOfSupply': state.placeOfSupply,
      'placeOfSupplyTa': state.placeOfSupplyTa,
    });

    // Determine final bill number
    late final String finalBillNumber;
    late final int vanakkam;

    if (state.invoiceNumberOverride.isNotEmpty) {
      finalBillNumber = state.invoiceNumberOverride;
      vanakkam = await kalanjiyam.getNextVanakkam(state.selectedNiruvanamId, finYear);
    } else if (editingEntry != null) {
      finalBillNumber = editingEntry.patrucheettuEn;
      vanakkam = editingEntry.vanakkam;
    } else {
      vanakkam = await kalanjiyam.getNextVanakkam(state.selectedNiruvanamId, finYear);
      finalBillNumber =
          kalanjiyam.formatPattiyalEn(profilePrefix, vanakkam);
    }

    // Duplicate check
    final isDuplicate = await kalanjiyam.isPattiyalEnDuplicate(state.selectedNiruvanamId, finYear, finalBillNumber,
      excludeId: editingEntry?.id,
    );

    if (isDuplicate) {
      return 'Invoice number $finalBillNumber already exists!';
    }

    if (editingEntry != null) {
      // ── Update ──
      final invNumChanged =
          state.invoiceNumberOverride != editingEntry.patrucheettuEn;
      final updatedPattiyal = PattiyalTharavuru(
        id: editingEntry.id,
        vanakkam: editingEntry.vanakkam,
        finYear: editingEntry.finYear,
        patrucheettuEn: finalBillNumber,
        niruvanamId: state.selectedNiruvanamId,
        vaangunarId: state.selectedVaangunarId,
        vaangunarPeyar: state.selectedVaangunarPeyarMap.cast<String, String>(),
        vaangunarMunvari: state.selectedVaangunarMunvariMap.cast<String, String>(),
        pattiyalVagai: state.pattiyalVagai,
        pattiyalNaal: state.pattiyalNaal,
        tharavugal: PattiyalUthavigal.pattuListToJson(validItems),
        mothaThogai: totals.mothaMothangal,
        thallupadi: totals.thallupadiMothangal,
        podhuThallupadiThogai: 0.0,
        podhuThallupadiVagai: '%',
        podhuThallupadiMathippu: 0.0,
        variThogai: totals.variMothangal,
        variTharavugal: jsonEncode(totals.variToJson()),
        sonthaViruppangal: settingsJson,
        createdAt: editingEntry.createdAt,
        updatedAt: DateTime.now(),
        isDeleted: editingEntry.isDeleted,
        deletedAt: editingEntry.deletedAt,
        mothaEdai: editingEntry.mothaEdai,
        setharamGrams: editingEntry.setharamGrams,
        thabaalThogai: editingEntry.thabaalThogai,
        ahimsaPattuThogai: editingEntry.ahimsaPattuThogai,
        piravariVugal: editingEntry.piravariVugal,
        nibandhanaigal: editingEntry.nibandhanaigal,
        ullkurippu: editingEntry.ullkurippu,
        vangiTharavugal: editingEntry.vangiTharavugal,
      );
      await kalanjiyam.updatePattiyal(editingEntry.id, updatedPattiyal);
      return updatedPattiyal;
    } else {
      // ── Create ──
      final newPattiyal = PattiyalTharavuru(
        id: 0,
        patrucheettuEn: finalBillNumber,
        finYear: finYear,
        vanakkam: vanakkam,
        niruvanamId: state.selectedNiruvanamId,
        vaangunarId: state.selectedVaangunarId,
        vaangunarPeyar: state.selectedVaangunarPeyarMap.cast<String, String>(),
        vaangunarMunvari: state.selectedVaangunarMunvariMap.cast<String, String>(),
        pattiyalVagai: state.pattiyalVagai,
        pattiyalNaal: state.pattiyalNaal,
        tharavugal: PattiyalUthavigal.pattuListToJson(validItems),
        mothaThogai: totals.mothaMothangal,
        thallupadi: totals.thallupadiMothangal,
        podhuThallupadiThogai: 0.0,
        podhuThallupadiVagai: '%',
        podhuThallupadiMathippu: 0.0,
        variThogai: totals.variMothangal,
        variTharavugal: jsonEncode(totals.variToJson()),
        sonthaViruppangal: settingsJson,
        createdAt: DateTime.now(),
        updatedAt: DateTime.now(),
        isDeleted: false,
        mothaEdai: 0.0,
        setharamGrams: 0.0,
        thabaalThogai: 0.0,
        ahimsaPattuThogai: 0.0,
        piravariVugal: '[]',
        nibandhanaigal: '',
        ullkurippu: '',
        vangiTharavugal: '{}',
      );
      final newId = await kalanjiyam.createPattiyal(newPattiyal);
      
      return PattiyalTharavuru(
        id: newId,
        patrucheettuEn: newPattiyal.patrucheettuEn,
        finYear: newPattiyal.finYear,
        vanakkam: newPattiyal.vanakkam,
        niruvanamId: newPattiyal.niruvanamId,
        vaangunarId: newPattiyal.vaangunarId,
        vaangunarPeyar: newPattiyal.vaangunarPeyar,
        vaangunarMunvari: newPattiyal.vaangunarMunvari,
        pattiyalVagai: newPattiyal.pattiyalVagai,
        pattiyalNaal: newPattiyal.pattiyalNaal,
        tharavugal: newPattiyal.tharavugal,
        mothaThogai: newPattiyal.mothaThogai,
        thallupadi: newPattiyal.thallupadi,
        podhuThallupadiThogai: newPattiyal.podhuThallupadiThogai,
        podhuThallupadiVagai: newPattiyal.podhuThallupadiVagai,
        podhuThallupadiMathippu: newPattiyal.podhuThallupadiMathippu,
        variThogai: newPattiyal.variThogai,
        variTharavugal: newPattiyal.variTharavugal,
        sonthaViruppangal: newPattiyal.sonthaViruppangal,
        createdAt: newPattiyal.createdAt,
        updatedAt: newPattiyal.updatedAt,
        isDeleted: newPattiyal.isDeleted,
        mothaEdai: newPattiyal.mothaEdai,
        setharamGrams: newPattiyal.setharamGrams,
        thabaalThogai: newPattiyal.thabaalThogai,
        ahimsaPattuThogai: newPattiyal.ahimsaPattuThogai,
        piravariVugal: newPattiyal.piravariVugal,
        nibandhanaigal: newPattiyal.nibandhanaigal,
        ullkurippu: newPattiyal.ullkurippu,
        vangiTharavugal: newPattiyal.vangiTharavugal,
      );
    }
  }

  // ── Draft persistence ───────────────────────────────────────────────────

  /// Serialises current state to SharedPreferences.
  static Future<void> saveDraft(PattuThiruththiNilaimai state) async {
    try {
      final prefs = await SharedPreferences.getInstance();
      final draft = jsonEncode({
        'vaangunarId': state.selectedVaangunarId,
        'vaangunarPeyarMap': state.selectedVaangunarPeyarMap.cast<String, String>(),
        'vaangunarMunvariMap': state.selectedVaangunarMunvariMap.cast<String, String>(),
        'customerState': state.customerState,
        'niruvanamId': state.selectedNiruvanamId,
        'pattiyalVagai': state.pattiyalVagai,
        'pattiyalNaal': state.pattiyalNaal.toIso8601String(),
        'placeOfSupply': state.placeOfSupply,
        'placeOfSupplyTa': state.placeOfSupplyTa,
        'invoiceNumberOverride': state.invoiceNumberOverride,
        'items': PattiyalUthavigal.pattuListToJson(state.items),
        'globalDiscountValue': state.globalDiscountValue,
        'globalDiscountType': state.globalDiscountType,
      });
      await prefs.setString(_draftKey, draft);
    } catch (_) {}
  }

  /// Attempts to restore a draft. Returns null if no valid draft exists
  /// or if the user declines. Shows a dialog for confirmation.
  static Future<PattuThiruththiNilaimai?> tryRestoreDraft(
      BuildContext context, WidgetRef ref) async {
    try {
      final prefs = await SharedPreferences.getInstance();
      final draftJson = prefs.getString(_draftKey);
      if (draftJson == null || draftJson.isEmpty) return null;

      final draft = jsonDecode(draftJson) as Map<String, dynamic>;
      final items = draft['items'] as String? ?? '[]';
      final nameMap = (draft['vaangunarPeyarMap'] as Map?)?.cast<String, String>() ?? {};
      final addrMap = (draft['vaangunarMunvariMap'] as Map?)?.cast<String, String>() ?? {};
      if (items == '[]' && nameMap.isEmpty) {
        await prefs.remove(_draftKey);
        return null;
      }

      if (!context.mounted) return null;

      bool restore = false;
      await showElvanActionSheet(
        context: context,
        title: '${K.varaivuMeetka.tr(context, ref)}\n\n${K.chaemikkaadhaVaraivu.tr(context, ref)}',
        cancelText: K.purakkaniPtn.tr(context, ref),
        confirmText: K.meetkavum.tr(context, ref),
        isConfirmFilled: false,
        onConfirm: () {
          restore = true;
        },
      );

      if (restore) {
        final parsedItems = PattiyalUthavigal.pattuListFromJson(items);
        return PattuThiruththiNilaimai(
          selectedVaangunarId: draft['vaangunarId'] as int?,
          selectedVaangunarPeyarMap: nameMap.cast<String, String>(),
          selectedVaangunarMunvariMap: addrMap.cast<String, String>(),
          customerState: draft['customerState'] as String? ?? '',
          selectedNiruvanamId: draft['niruvanamId'] as int?,
          pattiyalVagai: draft['pattiyalVagai'] as String? ?? 'tax-invoice',
          pattiyalNaal: DateTime.tryParse(
                  draft['pattiyalNaal'] as String? ?? '') ??
              DateTime.now(),
          placeOfSupply: draft['placeOfSupply'] as String? ?? '',
          placeOfSupplyTa: draft['placeOfSupplyTa'] as String? ?? '',
          invoiceNumberOverride:
              draft['invoiceNumberOverride'] as String? ?? '',
          items: parsedItems.isEmpty ? [const PattuUrupadi()] : parsedItems,
          globalDiscountValue:
              (draft['globalDiscountValue'] as num?)?.toDouble() ?? 0,
          globalDiscountType:
              draft['globalDiscountType'] as String? ?? '%',
        );
      } else {
        await prefs.remove(_draftKey);
        return null;
      }
    } catch (_) {
      return null;
    }
  }

  /// Removes the saved draft.
  static Future<void> clearDraft() async {
    try {
      final prefs = await SharedPreferences.getInstance();
      await prefs.remove(_draftKey);
    } catch (_) {}
  }
}
