import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../../../adippadai/tharavuru/seyali_murai.dart';
import 'package:flutter_riverpod/legacy.dart';

import '../../../adippadai/nilaimai/seyali_nilaimai.dart';
import '../../../adippadai/tharavuru/uruvugal.dart';
import '../../amaippugal/tharavu/kooli_niruvana_tharavugal_provider.dart';
import '../../amaippugal/tharavu/pattu_niruvana_tharavugal_provider.dart';
import 'porul_kalanjiyam.dart';
import 'kooli_porul_kalanjiyam.dart';
import 'pattu_porul_kalanjiyam.dart';

// ── Repository Provider ─────────────────────────────────────────────────────

final porulKalanjiyamProvider = Provider<PorulKalanjiyam>((ref) {
  final mode = ref.watch(appModeProvider);
  if (mode == AppMode.coolie) {
    final db = ref.watch(kooliDatabaseProvider);
    return KooliPorulKalanjiyam(db);
  } else {
    final db = ref.watch(pattuDatabaseProvider);
    return PattuPorulKalanjiyam(db);
  }
});

// ── Mode-Aware Item List (one-shot, pull-to-refresh) ────────────────────────

/// Fetches all items once for the current app mode.
/// Call `ref.invalidate(porulgalProvider)` after any CRUD to refresh.
final porulgalProvider = FutureProvider<List<PorulTharavuru>>((ref) async {
  await Future.delayed(Duration.zero);
  final kalanjiyam = ref.watch(porulKalanjiyamProvider);
  return kalanjiyam.getAllPorulgal();
});

// ── Editing State ───────────────────────────────────────────────────────────

/// Holds the item currently being edited (null = creating new).
final editingPorulProvider = StateProvider<PorulTharavuru?>((ref) => null);

// ── Selection Mode ──────────────────────────────────────────────────────────

/// Whether the item list is in selection mode.
final porulSelectionModeProvider = StateProvider<bool>((ref) => false);

/// Set of currently selected item IDs.
final selectedPorulIdsProvider = StateProvider<Set<int>>((ref) => {});

// ── Recycle Bin (Meetpagam) ──────────────────────────────────────────────────

/// Fetches all soft-deleted items once for the current app mode.
/// Call `ref.invalidate(deletedPorulgalProvider)` after restore/purge.
final deletedPorulgalProvider = FutureProvider<List<PorulTharavuru>>((ref) async {
  await Future.delayed(Duration.zero);
  final kalanjiyam = ref.watch(porulKalanjiyamProvider);
  return kalanjiyam.watchDeletedPorulgal().first;
});
