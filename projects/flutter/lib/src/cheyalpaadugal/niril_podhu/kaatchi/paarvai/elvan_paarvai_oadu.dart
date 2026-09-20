import 'package:elvan_niril/src/adippadai/vazhikaattal/niril_nav.dart';
import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import 'package:elvan_niril/src/adippadai/mozhiyaakkam/k.dart';
import '../../../../adippadai/mozhiyaakkam/mozhi_vazhanguthi.dart';
import '../../../chattagam/kaatchi/kaippaesi/elvan_utpakkach_chattagam.dart';
import '../../../chattagam/kaatchi/kaippaesi/koorugal/elvan_cheyal_pothan.dart';

/// The Universal View Shell — wraps all read-only views (Paarvai).
///
/// Mobile: existing navbar with text edit button.
/// Desktop: Edit button at the bottom of the form.
/// Content is constrained to maxWidth 1200 matching React's ElvanEditorLayout.
class ElvanPaarvaiOadu extends ConsumerWidget {
  const ElvanPaarvaiOadu({
    super.key,
    required this.title,
    required this.child,
    this.onEdit,
    this.onPrint,
  });

  final String title;
  final Widget child;
  
  /// Callback when the "Edit" (Thiruthi) button is pressed.
  /// If null, the edit button is not shown.
  final VoidCallback? onEdit;
  
  /// Callback when the "Print" (Achadippu) button is pressed.
  final VoidCallback? onPrint;

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final isDesktop = MediaQuery.sizeOf(context).width >= 800;
    final isDark = Theme.of(context).brightness == Brightness.dark;

    return ElvanSubpageShell(
      title: title,
      maxWidth: double.infinity,
      hideHeaderOnDesktop: true,
      navActions: [
        if (onPrint != null)
          ElvanCheyalPothan(
            label: 'அச்சிடு', // Print
            onPressed: onPrint,
          ),
        if (onEdit != null)
          ElvanCheyalPothan(
            label: K.maatriyamai.tr(context, ref), // "Edit"
            onPressed: onEdit,
          ),
      ],
      slivers: [
        SliverToBoxAdapter(
          child: Align(
            alignment: Alignment.topLeft,
            child: ConstrainedBox(
              constraints: const BoxConstraints(maxWidth: double.infinity),
              child: isDesktop
                  ? Padding(
                      padding: const EdgeInsets.symmetric(
                        horizontal: 32,
                        vertical: 32,
                      ),
                      child: Column(
                        crossAxisAlignment: CrossAxisAlignment.start,
                        children: [
                          Row(
                            mainAxisAlignment: MainAxisAlignment.spaceBetween,
                            crossAxisAlignment: CrossAxisAlignment.center,
                            children: [
                              Text(
                                title,
                                style: TextStyle(
                                  fontSize: 28,
                                  fontWeight: FontWeight.w700,
                                  letterSpacing: -0.5,
                                  color: isDark ? Colors.white : Colors.black,
                                ),
                              ),
                              if (onEdit != null)
                                FilledButton.icon(
                                  onPressed: onEdit,
                                  icon: const Icon(Icons.edit, size: 18),
                                  label: Text(
                                    K.maatriyamai.tr(context, ref),
                                    style: const TextStyle(
                                      fontWeight: FontWeight.w600,
                                      letterSpacing: 0.2,
                                    ),
                                  ),
                                  style: FilledButton.styleFrom(
                                    padding: const EdgeInsets.symmetric(
                                      horizontal: 20,
                                      vertical: 16,
                                    ),
                                    shape: RoundedRectangleBorder(
                                      borderRadius: BorderRadius.circular(12),
                                    ),
                                  ),
                                ),
                            ],
                          ),
                          const SizedBox(height: 32),
                          child,
                        ],
                      ),
                    )
                  : Padding(
                      padding: const EdgeInsets.symmetric(
                        horizontal: 16,
                        vertical: 16,
                      ),
                      child: child,
                    ),
            ),
          ),
        ),
      ],
    );
  }
}
