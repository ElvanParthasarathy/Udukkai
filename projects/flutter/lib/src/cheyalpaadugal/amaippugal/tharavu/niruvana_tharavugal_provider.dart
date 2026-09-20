import '../../../adippadai/tharavuru/seyali_murai.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../../../adippadai/nilaimai/seyali_nilaimai.dart';
import 'niruvana_tharavugal.dart';

import 'pattu_niruvana_tharavugal_provider.dart';
import 'kooli_niruvana_tharavugal_provider.dart';

// Facade provider to serve the correct database based on app mode.
final NiruvanaTharavugalListProvider = Provider<List<NiruvanaTharavugal>>((ref) {
  final mode = ref.watch(appModeProvider);
  if (mode == AppMode.coolie) {
    return ref.watch(kooliNiruvanaTharavugalListProvider);
  } else {
    return ref.watch(pattuNiruvanaTharavugalListProvider);
  }
});

// Facade provider to serve the currently active profile.
final NiruvanaTharavugalProvider = Provider<NiruvanaTharavugal?>((ref) {
  final mode = ref.watch(appModeProvider);
  if (mode == AppMode.coolie) {
    return ref.watch(kooliNiruvanaTharavugalProvider);
  } else {
    return ref.watch(pattuNiruvanaTharavugalProvider);
  }
});

// Provides access to the underlying notifier based on the current mode
// This allows UI to call methods like createProfile() without knowing which DB it uses.
final niruvanaTharavugalNotifierProvider = Provider<dynamic>((ref) {
  final mode = ref.watch(appModeProvider);
  if (mode == AppMode.coolie) {
    return ref.watch(kooliNiruvanaTharavugalListProvider.notifier);
  } else {
    return ref.watch(pattuNiruvanaTharavugalListProvider.notifier);
  }
});
