import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../../../adippadai/tharavuru/seyali_murai.dart';
import 'package:flutter_riverpod/legacy.dart';

import '../../../adippadai/nilaimai/seyali_nilaimai.dart';
import '../../../adippadai/tharavuru/uruvugal.dart';
import '../../amaippugal/tharavu/kooli_niruvana_tharavugal_provider.dart';
import '../../amaippugal/tharavu/pattu_niruvana_tharavugal_provider.dart';
import 'patru_kalanjiyam.dart';
import 'kooli_patru_kalanjiyam.dart';
import 'pattu_patru_kalanjiyam.dart';

// ── Repository Provider ─────────────────────────────────────────────────────

final patruKalanjiyamProvider = Provider<PatruKalanjiyam>((ref) {
  final mode = ref.watch(appModeProvider);
  if (mode == AppMode.coolie) {
    final db = ref.watch(kooliDatabaseProvider);
    return KooliPatruKalanjiyam(db);
  } else {
    final db = ref.watch(pattuDatabaseProvider);
    return PattuPatruKalanjiyam(db);
  }
});

// ── Mode-Aware Receipt List (one-shot, pull-to-refresh) ─────────────────────

/// Fetches all receipts once for the current app mode.
/// Call `ref.invalidate(patrugalProvider)` after any CRUD to refresh.
final patrugalProvider = FutureProvider<List<PatrugalTharavuru>>((ref) async {
  await Future.delayed(Duration.zero);
  final kalanjiyam = ref.watch(patruKalanjiyamProvider);
  return kalanjiyam.getPatrugal();
});

// ── Unpaid Invoices Stream ──────────────────────────────────────────────────

/// Provides a live stream of unpaid invoices for the current mode.
final unpaidInvoicesProvider = StreamProvider<List<PattiyalTharavuru>>((ref) async* {
  await Future.delayed(Duration.zero);
  final kalanjiyam = ref.watch(patruKalanjiyamProvider);
  yield* kalanjiyam.watchUnpaidInvoices();
});

// ── Editing State ───────────────────────────────────────────────────────────

/// Holds the receipt currently being edited (null = creating new).
final editingPatruProvider = StateProvider<PatrugalTharavuru?>((ref) => null);

// ── Selection Mode ──────────────────────────────────────────────────────────

/// Whether the receipt list is in selection mode.
final patruSelectionModeProvider = StateProvider<bool>((ref) => false);

/// Set of currently selected receipt IDs.
final selectedPatruIdsProvider = StateProvider<Set<int>>((ref) => {});
