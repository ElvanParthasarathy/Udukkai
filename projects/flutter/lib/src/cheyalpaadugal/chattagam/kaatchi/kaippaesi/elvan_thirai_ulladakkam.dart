import 'package:flutter/material.dart';
import 'package:flutter/physics.dart';
import 'elvan_virindha_pattai.dart';
import '../../../../koorugal/podhu_koorugal/elvan_kavanam.dart';

import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../../../koorugal/podhu_koorugal/elvan_keezhnagar.dart';
import 'koorugal/elvan_maeladukku_pattiyal.dart';

// ─────────────────────────────────────────────────────────────────────────────
// PAGE CONTENT (Scroll View & Slivers)
// ─────────────────────────────────────────────────────────────────────────────

class ElvanPageContent extends ConsumerWidget {
  const ElvanPageContent({
    super.key,
    required this.scrollController,
    required this.title,
    required this.navActions,
    required this.slivers,
    required this.expandedHeight,
    required this.isHeaderExpandedNotifier,
    this.dynamicPillHeightNotifier,
    this.leadingWidget,
    this.showLeadingWidgetInExpandedBar = true,
    this.isSearchActiveNotifier,
  });

  final ScrollController scrollController;
  final String title;
  final List<Widget> navActions;
  final List<Widget> slivers;
  final double expandedHeight;
  final ValueNotifier<bool> isHeaderExpandedNotifier;
  final ValueNotifier<double>? dynamicPillHeightNotifier;
  final Widget? leadingWidget;
  final bool showLeadingWidgetInExpandedBar;
  final ValueNotifier<bool>? isSearchActiveNotifier;

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final isMenuOpen = ref.watch(popupMenuOpenProvider);
    final double statusBarHeight = MediaQuery.paddingOf(context).top;
    final double snapThreshold =
        expandedHeight - 8.0 - kToolbarHeight - statusBarHeight - 20.0;

    return Stack(
      children: [
        CustomScrollView(
          keyboardDismissBehavior: ElvanKavanam.surulNadathai,
          cacheExtent: 1500.0, controller: scrollController, // Pre-builds items off-screen to prevent frame drops during slow scrolling!
          physics: ElvanBrickWallPhysics(
            isHeaderExpandedNotifier: isHeaderExpandedNotifier,
            snapThreshold: snapThreshold,
            parent: const AlwaysScrollableScrollPhysics(),
          ),
          slivers: [
            SliverPersistentHeader(
              pinned: true,
              delegate: ElvanExpandedBarDelegate(
                title: title,
                navActions: navActions,
                statusBarHeight: MediaQuery.paddingOf(context).top,
                expandedHeight: expandedHeight,
                dynamicPillHeightNotifier: dynamicPillHeightNotifier,
                leadingWidget:
                    showLeadingWidgetInExpandedBar ? leadingWidget : null,
                isSearchActiveNotifier:
                    isSearchActiveNotifier ?? ValueNotifier(false),
                isMenuOpen: isMenuOpen,
              ),
            ),

            // ── Small gap so cards sit slightly below the naked icons ──
            const SliverToBoxAdapter(
              child: SizedBox(height: 12),
            ),

            // ── Body content slivers ──
            ...slivers,

            // ── Smart Bottom Padding ──
            // Only adds empty space if the cards aren't tall enough to allow the header to collapse.
            SliverLayoutBuilder(
              builder: (context, constraints) {
                final double statusBarHeight =
                    MediaQuery.paddingOf(context).top;
                final double handOffOffset = expandedHeight -
                    8.0 -
                    kToolbarHeight -
                    statusBarHeight -
                    20.0;

                // The absolute minimum total scroll height required to collapse the header
                final double requiredTotalHeight =
                    constraints.viewportMainAxisExtent + handOffOffset;
                final double currentSliversHeight =
                    constraints.precedingScrollExtent;

                final double missingHeight =
                    requiredTotalHeight - currentSliversHeight;

                return SliverToBoxAdapter(
                  child:
                      SizedBox(height: missingHeight > 0 ? missingHeight : 0),
                );
              },
            ),

            // ── Keyboard Artificial Padding ──
            // Because Scaffold has resizeToAvoidBottomInset: false, the UI doesn't squish.
            // But we still need to allow the user to scroll up past the keyboard!
            const ElvanSliverKeezhNagar(),
          ],
        ),
      ],
    );
  }
}

/// A custom ScrollPhysics that acts as a mathematical "Brick Wall".
/// When the header is collapsed (isHeaderExpandedNotifier is false),
/// this completely prevents the CustomScrollView from scrolling below the snapThreshold.
/// This guarantees zero 1-frame visual leaks because the layout engine never even
/// receives an offset below the threshold.
class ElvanBrickWallPhysics extends ScrollPhysics {
  const ElvanBrickWallPhysics({
    required this.isHeaderExpandedNotifier,
    required this.snapThreshold,
    super.parent,
  });

  final ValueNotifier<bool> isHeaderExpandedNotifier;
  final double snapThreshold;

  @override
  ElvanBrickWallPhysics applyTo(ScrollPhysics? ancestor) {
    return ElvanBrickWallPhysics(
      isHeaderExpandedNotifier: isHeaderExpandedNotifier,
      snapThreshold: snapThreshold,
      parent: buildParent(ancestor),
    );
  }

  @override
  double applyBoundaryConditions(ScrollMetrics position, double value) {
    if (!isHeaderExpandedNotifier.value) {
      // ONLY block if it's momentum/fling! Allow manual drag to bypass the wall.
      final bool isMomentum = position is ScrollPosition &&
          position.activity is BallisticScrollActivity;

      if (isMomentum) {
        // Enforce our custom hard boundary!
        if (value < snapThreshold && position.pixels <= snapThreshold) {
          return value - position.pixels; // Prevent overscroll past threshold
        }
        if (value < snapThreshold && position.pixels > snapThreshold) {
          return value - snapThreshold; // Prevent momentum past threshold
        }
      }
    }

    // Normal bounds
    return super.applyBoundaryConditions(position, value);
  }

  @override
  Simulation? createBallisticSimulation(
      ScrollMetrics position, double velocity) {
    if (!isHeaderExpandedNotifier.value) {
      // If we are at or below the threshold, and trying to go further up (velocity < 0), stop it immediately.
      if (position.pixels <= snapThreshold && velocity <= 0.0) {
        return null; // returning null kills momentum perfectly!
      }
    }
    return super.createBallisticSimulation(position, velocity);
  }
}
