import 'package:flutter_riverpod/legacy.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../../adippadai/nilaimai/seyali_nilaimai.dart';
import '../../../adippadai/tharavuru/seyali_murai.dart';
import '../../../adippadai/tharavuru/uruvugal.dart';
import '../../amaippugal/tharavu/kooli_niruvana_tharavugal_provider.dart';
import '../../amaippugal/tharavu/pattu_niruvana_tharavugal_provider.dart';
import 'vaangunar_kalanjiyam.dart';
import 'kooli_vaangunar_kalanjiyam.dart';
import 'pattu_vaangunar_kalanjiyam.dart';

// ── Repository Provider ─────────────────────────────────────────────────────

final vaangunarKalanjiyamProvider = Provider<VaangunarKalanjiyam>((ref) {
  final mode = ref.watch(appModeProvider);
  if (mode == AppMode.coolie) {
    final db = ref.watch(kooliDatabaseProvider);
    return KooliVaangunarKalanjiyam(db);
  } else {
    final db = ref.watch(pattuDatabaseProvider);
    return PattuVaangunarKalanjiyam(db);
  }
});

// ── Mode-Aware Merchant List (one-shot, pull-to-refresh) ────────────────────

/// Fetches all merchants once for the current app mode.
/// Call `ref.invalidate(vaangunargalProvider)` after any CRUD to refresh.
final vaangunargalProvider = FutureProvider<List<VaangunarTharavuru>>((ref) async {
  await Future.delayed(Duration.zero);
  final kalanjiyam = ref.watch(vaangunarKalanjiyamProvider);
  return kalanjiyam.getAllVaangunargal();
});

// ── Editing State ───────────────────────────────────────────────────────────

/// Holds the merchant currently being edited (null = creating new).
final editingVaangunarProvider = StateProvider<VaangunarTharavuru?>((ref) => null);

// ── Selection Mode ──────────────────────────────────────────────────────────

/// Whether the merchant list is in selection mode.
final vaangunarSelectionModeProvider = StateProvider<bool>((ref) => false);

/// Set of currently selected merchant IDs.
final selectedVaangunarIdsProvider = StateProvider<Set<int>>((ref) => {});

// ── Recycle Bin (Meetpagam) ──────────────────────────────────────────────────

/// Fetches all soft-deleted merchants once for the current app mode.
/// Call `ref.invalidate(deletedVaangunargalProvider)` after restore/purge.
final deletedVaangunargalProvider =
    FutureProvider<List<VaangunarTharavuru>>((ref) async {
  await Future.delayed(Duration.zero);
  final kalanjiyam = ref.watch(vaangunarKalanjiyamProvider);
  return kalanjiyam.watchDeletedVaangunargal().first;
});
