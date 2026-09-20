import 'package:flutter/material.dart';
import 'package:elvan_niril/src/adippadai/mozhiyaakkam/k.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../../../adippadai/mozhiyaakkam/mozhi_vazhanguthi.dart';
import '../../ulnuzhaivu/kaatchi/koorugal/ullnuzhaivu_koorugal.dart';

// ─────────────────────────────────────────────────────────────────────────────
// DEFERRED SHELL LOADER — Prevents UI Jank during complex route transitions
// ─────────────────────────────────────────────────────────────────────────────

class DeferredShellLoader extends ConsumerStatefulWidget {
  final Widget child;
  const DeferredShellLoader({super.key, required this.child});

  @override
  ConsumerState<DeferredShellLoader> createState() =>
      _DeferredShellLoaderState();
}

class _DeferredShellLoaderState extends ConsumerState<DeferredShellLoader> {
  bool _buildReal = false;
  bool _showReal = false;

  @override
  void initState() {
    super.initState();
    // 1. Wait for the main AnimatedSwitcher transition (600ms) to completely finish.
    Future.delayed(const Duration(milliseconds: 650), () {
      if (mounted) {
        // 2. Add the heavy widget to the tree (with 0 opacity).
        // This causes the "lag spike" while it builds, but the screen is static so it's invisible.
        setState(() => _buildReal = true);

        // 3. Wait a tiny fraction of a second for Flutter to finish layout/paint of the heavy widget.
        Future.delayed(const Duration(milliseconds: 150), () {
          if (mounted) {
            // 4. Trigger the actual visual fade animation. Since the widget is already built, it's 60fps!
            setState(() => _showReal = true);
          }
        });
      }
    });
  }

  @override
  Widget build(BuildContext context) {
    return Stack(
      children: [
        // The heavy dashboard
        if (_buildReal)
          AnimatedOpacity(
            duration: const Duration(milliseconds: 600),
            opacity: _showReal ? 1.0 : 0.0,
            child: widget.child,
          ),

        // The loading spinner (fades out as dashboard fades in)
        IgnorePointer(
          ignoring: _showReal,
          child: AnimatedOpacity(
            duration: const Duration(milliseconds: 600),
            opacity: _showReal ? 0.0 : 1.0,
            child: AuthLayout(
              hideLogo: true,
              showBranding: true,
              child: Center(
                child: Text(
                  K.niril.tr(context, ref),
                  style: TextStyle(
                    fontSize: 36,
                    fontWeight: FontWeight.w800,
                    letterSpacing: -1.0,
                    color: Theme.of(context).colorScheme.onSurface,
                  ),
                ),
              ),
            ),
          ),
        ),
      ],
    );
  }
}
