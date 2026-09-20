import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../../chattagam/kaatchi/kaippaesi/elvan_utpakkach_chattagam.dart';
import '../koorugal/chaemippu_matrum_kaappu_pagudhi.dart';
import '../../../../adippadai/mozhiyaakkam/k.dart';
import '../../../../adippadai/mozhiyaakkam/mozhi_vazhanguthi.dart';

class ChaemippuMatrumKaappuThirai extends ConsumerWidget {
  const ChaemippuMatrumKaappuThirai({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final isDark = Theme.of(context).brightness == Brightness.dark;

    return ElvanSubpageShell(
      title: K.chaemippuMatrumKaappu.tr(context, ref),
      backgroundColor:
          isDark ? const Color(0xFF000000) : const Color(0xFFF3F4F6),
      slivers: [
        SliverPadding(
          padding: const EdgeInsets.only(
            left: 16,
            right: 16,
            top: 0,
            bottom: 32,
          ),
          sliver: SliverList.list(
            children: const [
              ChaemippuMatrumKaappuPagudhi(),
            ],
          ),
        ),
      ],
    );
  }
}
