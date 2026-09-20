import 'package:flutter/material.dart';
import 'package:elvan_niril/src/adippadai/mozhiyaakkam/k.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../../../adippadai/mozhiyaakkam/mozhi_vazhanguthi.dart';
import '../../../chattagam/kaatchi/kaippaesi/elvan_utpakkach_chattagam.dart';

class SeyaliPatriPage extends ConsumerWidget {
  const SeyaliPatriPage({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    return ElvanSubpageShell(
      title: K.cheyaliPatri.tr(context, ref),
      slivers: [
        SliverFillRemaining(
          child: Center(
            child: Text(K.cheyaliPatriMaadhiri.tr(context, ref)),
          ),
        ),
      ],
    );
  }
}
