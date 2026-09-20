import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../iru_mozhi/iru_mozhi_vazhanguthigal.dart';
import '../oru_mozhi/oru_mozhi_vazhanguthigal.dart';
import 'seyali_nilaimai.dart';
import '../tharavuru/seyali_murai.dart';

final primaryLanguageProvider = Provider<String>((ref) {
  final mode = ref.watch(appModeProvider);
  if (mode == AppMode.coolie) {
    return ref.watch(kooliAchuMozhiProvider);
  }
  return ref.watch(silkMudhanmaiMozhiProvider);
});

final secondaryLanguageProvider = Provider<String>((ref) {
  final mode = ref.watch(appModeProvider);
  if (mode == AppMode.coolie) {
    final kooliLang = ref.watch(kooliAchuMozhiProvider);
    return kooliLang == 'ta' ? 'en' : 'ta';
  }
  return ref.watch(silkThunaiMozhiProvider);
});
