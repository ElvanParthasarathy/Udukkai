import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../../cheyalpaadugal/amaippugal/tharavu/pattu_niruvana_tharavugal_provider.dart';
import 'iru_mozhi.dart';

class BilingualNotifier extends Notifier<bool> {
  @override
  bool build() {
    final profile = ref.watch(pattuNiruvanaTharavugalProvider);
    return profile?.iruMozhi ?? false;
  }

  @override
  set state(bool value) {
    final profile = ref.read(pattuNiruvanaTharavugalProvider);
    if (profile != null) {
      final newProfile = profile.copyWith(iruMozhi: value);
      ref.read(pattuNiruvanaTharavugalListProvider.notifier).updateProfile(newProfile);
    }
  }
}

final bilingualProvider = NotifierProvider<BilingualNotifier, bool>(() {
  return BilingualNotifier();
});

class GstSplitNotifier extends Notifier<bool> {
  @override
  bool build() {
    final profile = ref.watch(pattuNiruvanaTharavugalProvider);
    return profile?.gstPirippugal ?? false;
  }

  @override
  set state(bool value) {
    final profile = ref.read(pattuNiruvanaTharavugalProvider);
    if (profile != null) {
      final newProfile = profile.copyWith(gstPirippugal: value);
      ref.read(pattuNiruvanaTharavugalListProvider.notifier).updateProfile(newProfile);
    }
  }
}

final gstSplitProvider = NotifierProvider<GstSplitNotifier, bool>(() {
  return GstSplitNotifier();
});

class SilkMudhanmaiMozhiNotifier extends Notifier<String> {
  @override
  String build() {
    final profile = ref.watch(pattuNiruvanaTharavugalProvider);
    return profile?.mudhanMozhi ?? IruMozhi.iyalbuMudhanmaiMozhi;
  }

  @override
  set state(String value) {
    final profile = ref.read(pattuNiruvanaTharavugalProvider);
    if (profile != null) {
      final newProfile = profile.copyWith(mudhanMozhi: value);
      ref.read(pattuNiruvanaTharavugalListProvider.notifier).updateProfile(newProfile);
    }
  }
}

final silkMudhanmaiMozhiProvider =
    NotifierProvider<SilkMudhanmaiMozhiNotifier, String>(() {
  return SilkMudhanmaiMozhiNotifier();
});

class SilkThunaiMozhiNotifier extends Notifier<String> {
  @override
  String build() {
    final profile = ref.watch(pattuNiruvanaTharavugalProvider);
    return profile?.thunaiMozhi ?? IruMozhi.iyalbuThunaiMozhi;
  }

  @override
  set state(String value) {
    final profile = ref.read(pattuNiruvanaTharavugalProvider);
    if (profile != null) {
      final newProfile = profile.copyWith(thunaiMozhi: value);
      ref.read(pattuNiruvanaTharavugalListProvider.notifier).updateProfile(newProfile);
    }
  }
}

final silkThunaiMozhiProvider =
    NotifierProvider<SilkThunaiMozhiNotifier, String>(() {
  return SilkThunaiMozhiNotifier();
});
