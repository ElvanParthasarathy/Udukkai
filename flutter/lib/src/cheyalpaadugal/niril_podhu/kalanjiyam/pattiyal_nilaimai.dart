import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../../../adippadai/tharavuru/seyali_murai.dart';
import 'package:flutter_riverpod/legacy.dart';

import '../../../adippadai/nilaimai/seyali_nilaimai.dart';
import '../../../adippadai/tharavuru/uruvugal.dart';
import '../../amaippugal/tharavu/kooli_niruvana_tharavugal_provider.dart';
import '../../amaippugal/tharavu/pattu_niruvana_tharavugal_provider.dart';
import 'pattiyal_kalanjiyam.dart';
import 'kooli_pattiyal_kalanjiyam.dart';
import 'pattu_pattiyal_kalanjiyam.dart';

// ── Repository Provider ─────────────────────────────────────────────────────

final pattiyalKalanjiyamProvider = Provider<PattiyalKalanjiyam>((ref) {
  final mode = ref.watch(appModeProvider);
  if (mode == AppMode.coolie) {
    final db = ref.watch(kooliDatabaseProvider);
    return KooliPattiyalKalanjiyam(db);
  } else {
    final db = ref.watch(pattuDatabaseProvider);
    return PattuPattiyalKalanjiyam(db);
  }
});

// ── Mode-Aware Invoice List (one-shot, pull-to-refresh) ─────────────────────

/// Fetches all invoices once for the current app mode.
/// Call `ref.invalidate(pattiyalgalProvider)` after any CRUD to refresh.
final pattiyalgalProvider = FutureProvider<List<PattiyalTharavuru>>((ref) async {
  await Future.delayed(Duration.zero);
  final kalanjiyam = ref.watch(pattiyalKalanjiyamProvider);
  return kalanjiyam.getPattiyalgal();
});

// ── Editing State ───────────────────────────────────────────────────────────

/// Holds the invoice currently being edited (null = creating new).
final editingPattiyalProvider = StateProvider<PattiyalTharavuru?>((ref) => null);

// ── Selection Mode ──────────────────────────────────────────────────────────

/// Whether the invoice list is in selection mode.
final pattiyalSelectionModeProvider = StateProvider<bool>((ref) => false);

/// Set of currently selected invoice IDs.
final selectedPattiyalIdsProvider = StateProvider<Set<int>>((ref) => {});

// ── Recycle Bin (Meetpagam) ──────────────────────────────────────────────────

/// Fetches all soft-deleted invoices once for the current app mode.
/// Call `ref.invalidate(deletedPattiyalgalProvider)` after restore/purge.
final deletedPattiyalgalProvider = FutureProvider<List<PattiyalTharavuru>>((ref) async {
  await Future.delayed(Duration.zero);
  final kalanjiyam = ref.watch(pattiyalKalanjiyamProvider);
  return kalanjiyam.getDeletedPattiyalgal();
});
