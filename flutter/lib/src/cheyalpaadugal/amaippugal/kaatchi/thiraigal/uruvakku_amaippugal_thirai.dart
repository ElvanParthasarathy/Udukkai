import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../../../adippadai/nilaimai/seyali_nilaimai.dart';
import '../../../../adippadai/tharavuru/seyali_murai.dart';
import '../../../niril_pattu/kaatchi/thiraigal/amaippugal/pattu_uruvakku_amaippu.dart';
import '../../../niril_kooli/kaatchi/thiraigal/amaippugal/kooli_uruvakku_amaippu.dart';

class UruvakkuAmaippugalPage extends ConsumerWidget {
  const UruvakkuAmaippugalPage({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final mode = ref.watch(appModeProvider);

    if (mode == AppMode.coolie) {
      return const CoolieUruvakkuAmaippuPage();
    }
    return const SilkUruvakkuAmaippuPage();
  }
}
