import 'package:flutter/cupertino.dart';
import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../../../../adippadai/nilaimai/seyali_nilaimai.dart';

/// A circular back button designed to be passed into ElvanShell as a [leadingWidget].
/// The shell's physics engine automatically handles its background, drop shadow, and scroll fading.
class ElvanBackButton extends ConsumerWidget {
  const ElvanBackButton({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    return SizedBox(
      width: 50,
      height: 50,
      child: InkResponse(
        onTap: () {
          if (Navigator.canPop(context)) {
            Navigator.maybePop(context);
          } else {
            // If we are at the root route (e.g. forced onboarding screen),
            // hitting back should take us back to the mode selector.
            final mode = ref.read(appModeProvider);
            if (mode != null) {
              ref.read(appModeProvider.notifier).setMode(null);
            }
          }
        },
        radius: 25,
        highlightShape: BoxShape.circle,
        splashFactory:
            NoSplash.splashFactory, // Instantly fills the circle, no growing!
        splashColor: Colors.transparent,
        highlightColor: Theme.of(context)
            .colorScheme
            .onSurface
            .withValues(alpha: 0.12), // Instant full circle flash
        child: const Center(
          child: Icon(
            CupertinoIcons.chevron_back,
            size: 24,
          ),
        ),
      ),
    );
  }
}

