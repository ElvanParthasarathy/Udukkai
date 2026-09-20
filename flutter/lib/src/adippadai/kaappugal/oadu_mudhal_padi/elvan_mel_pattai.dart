import 'package:flutter/material.dart';

/// A reusable, independent Top Bar component that seamlessly handles the "Visual Hand-off"
/// and white pill background physics for the One UI design language.
class ElvanTopBar extends StatelessWidget {
  const ElvanTopBar({
    super.key,
    required this.scrollController,
    required this.hideAnimation,
    required this.navActions,
    this.expandedHeight = 320.0,
  });

  /// The scroll controller attached to the page's CustomScrollView.
  /// Used to calculate hand-off thresholds and collision physics.
  final ScrollController scrollController;

  /// The animation controller that dictates when the top bar should fade out.
  /// (e.g., synchronized with the bottom navbar hiding).
  final Animation<double> hideAnimation;

  /// The list of icon buttons to display inside the pill.
  final List<Widget> navActions;

  /// The maximum physical height of the page header before scrolling.
  final double expandedHeight;

  @override
  Widget build(BuildContext context) {
    return AnimatedBuilder(
      animation: Listenable.merge([scrollController, hideAnimation]),
      builder: (context, child) {
        if (!scrollController.hasClients) return const SizedBox.shrink();

        final double shrinkOffset = scrollController.offset;
        final double statusBarHeight = MediaQuery.paddingOf(context).top;
        final double ceiling = statusBarHeight + 20.0;

        // 1. Hand-off logic: Calculate where Component A (the naked icons) is currently positioned
        final double currentTop =
            expandedHeight - shrinkOffset - 8.0 - kToolbarHeight;
        final bool isPinned = currentTop <= ceiling;

        // If the hand-off hasn't happened yet (the icons haven't reached the ceiling),
        // Component B remains completely invisible.
        if (!isPinned) return const SizedBox.shrink();

        // 2. True Pill Logic: Calculate when the gallery cards physically collide with the pill
        final double collisionOffset =
            (expandedHeight + 32.0) - (ceiling + 50.0);
        final double liftStartOffset = collisionOffset - 4.0;

        double liftProgress = 0.0;
        if (shrinkOffset > liftStartOffset) {
          liftProgress =
              ((shrinkOffset - liftStartOffset) / 12.0).clamp(0.0, 1.0);
        }

        return Positioned(
          top: ceiling,
          right: 16,
          child: Align(
            alignment: Alignment.centerRight,
            child: Opacity(
              opacity: hideAnimation
                  .value, // Fade out when the page tells us to hide
              child: Container(
                decoration: BoxDecoration(
                  color: Colors.white.withValues(alpha: 0.88 * liftProgress),
                  borderRadius: BorderRadius.circular(100),
                  boxShadow: liftProgress > 0
                      ? [
                          BoxShadow(
                            blurRadius: 16 * liftProgress,
                            offset: Offset(0, 4 * liftProgress),
                            color: Colors.black
                                .withValues(alpha: 0.05 * liftProgress),
                          )
                        ]
                      : null,
                ),
                padding: const EdgeInsets.symmetric(horizontal: 4, vertical: 5),
                child: Row(
                  mainAxisSize: MainAxisSize.min,
                  children: navActions,
                ),
              ),
            ),
          ),
        );
      },
    );
  }
}
