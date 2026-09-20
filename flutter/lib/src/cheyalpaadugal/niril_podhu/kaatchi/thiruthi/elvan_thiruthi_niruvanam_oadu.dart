import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import 'package:elvan_niril/src/adippadai/mozhiyaakkam/k.dart';
import '../../../../adippadai/mozhiyaakkam/mozhi_vazhanguthi.dart';
import '../../../../adippadai/tharavuru/seyali_murai.dart';
import '../../../../koorugal/podhu_koorugal/elvan_pagudhi_thalaipu_kooru.dart';
import 'package:elvan_niril/src/cheyalpaadugal/niril_podhu/kaatchi/thiruthi/koorugal/elvan_thiruthi_paguthi.dart';
import 'package:elvan_niril/src/koorugal/ulleedugal/elvan_thiruthi_thalaippu.dart';
import '../../../amaippugal/tharavu/niruvana_tharavugal.dart';
import '../../../amaippugal/tharavu/niruvana_tharavugal_provider.dart';
import '../koorugal/elvan_niruvanam_keezhvirivu_kooru.dart';

/// A shared wrapper that enforces Business Profile selection before 
/// allowing interaction with the editor form.
class ElvanThiruthiNiruvanamOadu extends ConsumerWidget {
  /// The currently selected business profile ID.
  final int? selectedNiruvanamId;

  /// Callback when a profile is selected or auto-selected.
  final ValueChanged<NiruvanaTharavugal?> onChanged;

  /// The inner form/editor.
  final Widget child;

  const ElvanThiruthiNiruvanamOadu({
    super.key,
    required this.selectedNiruvanamId,
    required this.onChanged,
    required this.child,
  });

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final profiles = ref.watch(NiruvanaTharavugalListProvider);

    // Auto-select if only one profile exists
    if (profiles.length == 1 && selectedNiruvanamId == null) {
      Future.microtask(() {
        onChanged(profiles.first);
      });
    }

    final isDesktop = MediaQuery.sizeOf(context).width >= 800;

    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        if (profiles.length > 1) ...[
          ElvanEditorSection(
            index: 0,
            title: K.niruvanathTharavu.tr(context, ref),
            displayChild: const SizedBox(),
            initiallyExpanded: true,
            children: [
              isDesktop
                  ? Align(
                      alignment: Alignment.centerLeft,
                      child: ConstrainedBox(
                        constraints: const BoxConstraints(maxWidth: 360),
                        child: Column(
                          crossAxisAlignment: CrossAxisAlignment.start,
                          children: [
                            ElvanThiruthiThalaippu(label: K.niruvanam.tr(context, ref)),
                            ElvanNiruvanamKeezhvirivuKooru(
                              selectedNiruvanamId: selectedNiruvanamId,
                              hideLabel: true,
                              showClearButton: true,
                              onChanged: (p) => onChanged(p),
                            ),
                          ],
                        ),
                      ),
                    )
                  : Column(
                      crossAxisAlignment: CrossAxisAlignment.start,
                      children: [
                        ElvanThiruthiThalaippu(label: K.niruvanam.tr(context, ref)),
                        ElvanNiruvanamKeezhvirivuKooru(
                          selectedNiruvanamId: selectedNiruvanamId,
                          hideLabel: true,
                          showClearButton: true,
                          onChanged: (p) => onChanged(p),
                        ),
                      ],
                    ),
            ],
          ),
        ],

        // Form Lock (Opacity + IgnorePointer)
        Opacity(
          opacity: selectedNiruvanamId == null ? 0.4 : 1.0,
          child: IgnorePointer(
            ignoring: selectedNiruvanamId == null,
            child: child,
          ),
        ),
      ],
    );
  }
}
