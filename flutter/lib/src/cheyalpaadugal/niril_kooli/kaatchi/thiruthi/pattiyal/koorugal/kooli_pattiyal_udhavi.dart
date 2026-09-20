import 'package:elvan_niril/src/adippadai/tharavuru/uruvugal.dart';
import 'dart:convert';

import 'package:flutter/material.dart';
import 'package:shared_preferences/shared_preferences.dart';
import 'package:drift/drift.dart' show Value;
import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../../../../../adippadai/mozhiyaakkam/k.dart';
import '../../../../../../adippadai/mozhiyaakkam/mozhi_vazhanguthi.dart';

import '../../../../../../adippadai/tharavuthalam/seyali_tharavuthalam.dart';
import '../../../../../niril_podhu/tharavuru/pattiyal_tharavuru.dart';
import '../../../../../niril_podhu/kalanjiyam/pattiyal_kalanjiyam.dart';
import '../../../../../amaippugal/tharavu/niruvana_tharavugal.dart';
import '../../../../../../koorugal/maeladukkugal/elvan_cheyal_maeladukku.dart';

// ─────────────────────────────────────────────────────────────────────────────
// கூலிப் பட்டியல் உதவி — Helper for load / save logic
// ─────────────────────────────────────────────────────────────────────────────

/// Immutable snapshot of all coolie editor state fields.
/// Used to transfer data between helper ↔ widget without tight coupling.
class KooliThiruththiNilaimai {
  const KooliThiruththiNilaimai({
    this.selectedNiruvanamId,
    this.selectedProfile,
    this.selectedVaangunarId,
    this.selectedVaangunarPeyarMap = const {},
    this.selectedVaangunarMunvariMap = const {},
    required this.pattiyalNaal,
    this.items = const [],
    this.setharamGrams = 0,
    this.thabaalThogai = 0,
    this.ahimsaPattuThogai = 0,
    this.piraVarivugal = const [],
    this.showBankDetails = true,
    this.invoiceNumberOverride = '',
  });

  final int? selectedNiruvanamId;
  final NiruvanaTharavugal? selectedProfile;
  final int? selectedVaangunarId;
  final Map<String, String> selectedVaangunarPeyarMap;
  final Map<String, String> selectedVaangunarMunvariMap;
  final DateTime pattiyalNaal;
  final List<KooliUrupadi> items;
  final double setharamGrams;
  final double thabaalThogai;
  final double ahimsaPattuThogai;
  final List<PiraVarivu> piraVarivugal;
  final bool showBankDetails;

  /// Manual override for the bill number (e.g. "CB-42").
  /// Empty string means auto-generate.
  final String invoiceNumberOverride;
}

/// Pure data helper — no widget dependency. Handles:
///   • Loading from an existing PattiyalTharavuru (edit)
///   • Saving (create + update) via PattiyalKalanjiyam
class KooliPattiyalUthavi {
  KooliPattiyalUthavi._();

  static const _draftKey = 'niril_draft_coolie_invoice';

  // ── Load from entry (edit) ────────────────────────────────────────────────

  /// Parses a [PattiyalTharavuru] into a state snapshot for editing.
  static KooliThiruththiNilaimai loadFromEntry(PattiyalTharavuru entry) {
    // Parse items
    var items = PattiyalUthavigal.kooliListFromJson(entry.tharavugal);
    if (items.isEmpty) items = [const KooliUrupadi()];

    // Parse other charges
    final piraVarivugal =
        PattiyalUthavigal.piraVarivuListFromJson(entry.piravariVugal);

    return KooliThiruththiNilaimai(
      selectedNiruvanamId: entry.niruvanamId,
      selectedVaangunarId: entry.vaangunarId,
      selectedVaangunarPeyarMap: entry.vaangunarPeyar.cast<String, String>(),
      selectedVaangunarMunvariMap: entry.vaangunarMunvari.cast<String, String>(),
      pattiyalNaal: entry.pattiyalNaal,
      items: items,
      setharamGrams: entry.setharamGrams,
      thabaalThogai: entry.thabaalThogai,
      ahimsaPattuThogai: entry.ahimsaPattuThogai,
      piraVarivugal: piraVarivugal,
    );
  }

  // ── Save ─────────────────────────────────────────────────────────────────

  /// Saves the coolie invoice (create or update). Returns error string if any.
  static Future<dynamic> save({
    required PattiyalKalanjiyam kalanjiyam,
    required KooliThiruththiNilaimai state,
    required KooliMothangal totals,
    required String profilePrefix,
    required NiruvanaTharavugal profile,
    PattiyalTharavuru? editingEntry,
  }) async {
    final now = DateTime.now();
    final finYear = now.month >= 4 ? now.year : now.year - 1;
    final validItems =
        state.items.where((i) => i.edai > 0 && i.vilai > 0).toList();

    // Bank snapshot from profile
    final bankSnapshot = jsonEncode({
      'vangiPeyar': profile.vangiPeyar,
      'kilai': profile.kilai,
      'vangiKanakku': profile.vangiKanakku,
      'ifsc': profile.ifsc,
      'upiId': profile.upiId,
    });

    // Determine final bill number and vanakkam
    late final String finalBillNumber;
    late final int vanakkam;

    if (state.invoiceNumberOverride.isNotEmpty) {
      // Manual override — user typed a custom number
      finalBillNumber = state.invoiceNumberOverride;
      final parts = state.invoiceNumberOverride.split('-');
      vanakkam = int.tryParse(parts.last) ?? 1;
    } else if (editingEntry != null) {
      // Editing without override — keep existing number
      finalBillNumber = editingEntry.patrucheettuEn;
      vanakkam = editingEntry.vanakkam;
    } else {
      // New invoice — auto-generate
      vanakkam = await kalanjiyam.getNextVanakkam(state.selectedNiruvanamId, finYear);
      finalBillNumber =
          kalanjiyam.formatPattiyalEn(profilePrefix, vanakkam);
    }

    // Duplicate check
    final isDuplicate = await kalanjiyam.isPattiyalEnDuplicate(state.selectedNiruvanamId,
      finYear,
      finalBillNumber,
      excludeId: editingEntry?.id,
    );

    if (isDuplicate) {
      return 'Invoice number $finalBillNumber already exists!';
    }

    if (editingEntry != null) {
      // ── Update ──
      final updatedPattiyal = PattiyalTharavuru(
        id: editingEntry.id,
        vanakkam: editingEntry.vanakkam,
        finYear: editingEntry.finYear,
        patrucheettuEn: finalBillNumber,
        niruvanamId: state.selectedNiruvanamId,
        vaangunarId: state.selectedVaangunarId,
        vaangunarPeyar: state.selectedVaangunarPeyarMap,
        vaangunarMunvari: state.selectedVaangunarMunvariMap,
        pattiyalVagai: '',
        pattiyalNaal: state.pattiyalNaal,
        tharavugal: PattiyalUthavigal.kooliListToJson(validItems),
        mothaThogai: totals.perumMothangal,
        thallupadi: 0.0,
        podhuThallupadiThogai: 0.0,
        podhuThallupadiVagai: '%',
        podhuThallupadiMathippu: 0.0,
        variThogai: 0.0,
        variTharavugal: '{}',
        sonthaViruppangal: '{}',
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
        vaangunarPeyar: state.selectedVaangunarPeyarMap,
        vaangunarMunvari: state.selectedVaangunarMunvariMap,
        pattiyalVagai: '',
        pattiyalNaal: state.pattiyalNaal,
        tharavugal: PattiyalUthavigal.kooliListToJson(validItems),
        mothaThogai: totals.perumMothangal,
        thallupadi: 0.0,
        podhuThallupadiThogai: 0.0,
        podhuThallupadiVagai: '%',
        podhuThallupadiMathippu: 0.0,
        variThogai: 0.0,
        variTharavugal: '{}',
        sonthaViruppangal: '{}',
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
      
      // Return a copy with the newly assigned ID
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
  static Future<void> saveDraft(KooliThiruththiNilaimai state) async {
    try {
      final prefs = await SharedPreferences.getInstance();
      final draft = jsonEncode({
        'vaangunarId': state.selectedVaangunarId,
        'vaangunarPeyarMap': state.selectedVaangunarPeyarMap,
        'vaangunarMunvariMap': state.selectedVaangunarMunvariMap,
        'niruvanamId': state.selectedNiruvanamId,
        'pattiyalNaal': state.pattiyalNaal.toIso8601String(),
        'items': PattiyalUthavigal.kooliListToJson(state.items),
        'setharamGrams': state.setharamGrams,
        'thabaalThogai': state.thabaalThogai,
        'ahimsaPattuThogai': state.ahimsaPattuThogai,
        'piraVarivugal':
            PattiyalUthavigal.piraVarivuListToJson(state.piraVarivugal),
        'showBankDetails': state.showBankDetails,
        'invoiceNumberOverride': state.invoiceNumberOverride,
      });
      await prefs.setString(_draftKey, draft);
    } catch (_) {}
  }

  /// Attempts to restore a draft. Returns null if no valid draft exists
  /// or if the user declines. Shows a dialog for confirmation.
  static Future<KooliThiruththiNilaimai?> tryRestoreDraft(
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
        final parsedItems = PattiyalUthavigal.kooliListFromJson(items);
        final piraVarivugal = PattiyalUthavigal.piraVarivuListFromJson(
            draft['piraVarivugal'] as String? ?? '[]');
        return KooliThiruththiNilaimai(
          selectedVaangunarId: draft['vaangunarId'] as int?,
          selectedVaangunarPeyarMap: nameMap.cast<String, String>(),
          selectedVaangunarMunvariMap: addrMap.cast<String, String>(),
          selectedNiruvanamId: draft['niruvanamId'] as int?,
          pattiyalNaal: DateTime.tryParse(
                  draft['pattiyalNaal'] as String? ?? '') ??
              DateTime.now(),
          items:
              parsedItems.isEmpty ? [const KooliUrupadi()] : parsedItems,
          setharamGrams:
              (draft['setharamGrams'] as num?)?.toDouble() ?? 0,
          thabaalThogai:
              (draft['thabaalThogai'] as num?)?.toDouble() ?? 0,
          ahimsaPattuThogai:
              (draft['ahimsaPattuThogai'] as num?)?.toDouble() ?? 0,
          piraVarivugal: piraVarivugal,
          showBankDetails: draft['showBankDetails'] as bool? ?? true,
          invoiceNumberOverride:
              draft['invoiceNumberOverride'] as String? ?? '',
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
