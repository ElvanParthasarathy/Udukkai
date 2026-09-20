import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../../cheyalpaadugal/amaippugal/tharavu/kooli_niruvana_tharavugal_provider.dart';
import 'oru_mozhi.dart';

class KooliAchuMozhiNotifier extends Notifier<String> {
  @override
  String build() {
    final profile = ref.watch(kooliNiruvanaTharavugalProvider);
    return profile?.mudhanMozhi ?? OruMozhi.iyalbuMozhi;
  }

  @override
  set state(String value) {
    final profile = ref.read(kooliNiruvanaTharavugalProvider);
    if (profile != null) {
      final newProfile = profile.copyWith(mudhanMozhi: value);
      ref.read(kooliNiruvanaTharavugalListProvider.notifier).updateProfile(newProfile);
    }
  }
}

final kooliAchuMozhiProvider =
    NotifierProvider<KooliAchuMozhiNotifier, String>(() {
  return KooliAchuMozhiNotifier();
});
