import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../../../adippadai/nilaimai/seyali_nilaimai.dart';
import '../../../../adippadai/tharavuru/seyali_murai.dart';
import '../../../niril_pattu/kaatchi/thiraigal/amaippugal/pattu_niruvana_amaippu.dart';
import '../../../niril_kooli/kaatchi/thiraigal/amaippugal/kooli_niruvana_amaippu.dart';

class NiruvanaAmaippugalPage extends ConsumerWidget {
  const NiruvanaAmaippugalPage({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final mode = ref.watch(appModeProvider);

    if (mode == AppMode.coolie) {
      return const CoolieNiruvanaAmaippuPage();
    }
    return const SilkNiruvanaAmaippuPage();
  }
}
