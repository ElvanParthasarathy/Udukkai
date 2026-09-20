import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../../../adippadai/tharavuru/seyali_murai.dart';
import '../../../adippadai/nilaimai/seyali_nilaimai.dart';
import '../../niril_pattu/kaatchi/thiraigal/pattu_porutkal_thirai.dart';
import '../../niril_kooli/kaatchi/thiraigal/kooli_porutkal_thirai.dart';

class PorulPage extends ConsumerWidget {
  const PorulPage({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final mode = ref.watch(appModeProvider);

    if (mode == AppMode.coolie) {
      return const CoolieItemsPage();
    }

    // Default to GST mode
    return const SilkItemsPage();
  }
}
