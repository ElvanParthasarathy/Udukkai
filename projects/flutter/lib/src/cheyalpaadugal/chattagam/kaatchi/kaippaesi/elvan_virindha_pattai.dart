import 'dart:math' as math;
import 'package:flutter/material.dart';

// ─────────────────────────────────────────────────────────────────────────────
// EXPANDED BAR DELEGATE (Massive Title Header)
// ─────────────────────────────────────────────────────────────────────────────

class ElvanExpandedBarDelegate extends SliverPersistentHeaderDelegate {
  ElvanExpandedBarDelegate({
    required this.title,
    required this.navActions,
    required this.statusBarHeight,
    required this.expandedHeight,
    this.dynamicPillHeightNotifier,
    this.leadingWidget,
    required this.isSearchActiveNotifier,
    required this.isMenuOpen,
  });

  final String title;
  final List<Widget> navActions;
  final double statusBarHeight;
  final double expandedHeight;
  final ValueNotifier<double>? dynamicPillHeightNotifier;
  final Widget? leadingWidget;
  final ValueNotifier<bool> isSearchActiveNotifier;
  final bool isMenuOpen;

  @override
  double get maxExtent => expandedHeight;

  @override
  double get minExtent => statusBarHeight + 72.0;

  @override
  Widget build(
      BuildContext context, double shrinkOffset, bool overlapsContent) {
    return ValueListenableBuilder<bool>(
        valueListenable: isSearchActiveNotifier,
        builder: (context, isSearchActive, child) {
          return LayoutBuilder(
            builder: (context, constraints) {
              final double currentHeight = constraints.maxHeight;
              final double maxShrink = maxExtent - minExtent;
              final double shrinkProgress =
                  (shrinkOffset / maxShrink).clamp(0.0, 1.0);
              final double titleOpacity =
                  (1.0 - (shrinkProgress * 1.5)).clamp(0.0, 1.0);

              final double expandedButtonsBottom = 8.0;
              double currentTop =
                  currentHeight - expandedButtonsBottom - kToolbarHeight;
              final double ceiling = statusBarHeight + 20.0;

              bool isPinned = currentTop <= ceiling;
              if (isPinned) {
                currentTop = ceiling;
              }

              // Calculate the exact mathematical progress where the handoff triggers
              final double handoffHeight =
                  ceiling + expandedButtonsBottom + kToolbarHeight;
              final double handoffShrinkOffset = maxExtent - handoffHeight;
              final double handoffProgress =
                  (handoffShrinkOffset / maxShrink).clamp(0.001, 1.0);

              // This progress hits exactly 1.0 at the precise millisecond of the hand-off.
              final double normalizedProgress =
                  (shrinkProgress / handoffProgress).clamp(0.0, 1.0);

              // ── PURE PIXEL POSITION MATH (v2) ──────────────────────────────────
              // Instead of fighting Alignment.lerp against Transform.translate,
              // we measure the text's exact pixel width, compute its centered X
              // and its collapsed-bar target X, then linearly lerp between them.
              // Zero quadratic forces. Zero jitter. Works for ANY text width.

              // MICRO-NUDGES for sub-pixel font-hinting alignment:
              const double xNudge =
                  1.5; // Horizontal tweak (scaled 34px vs native 20px side-bearings)
              const double yNudge =
                  0.6; // Vertical tweak (positive = down at handoff)

              final double t =
                  normalizedProgress; // Hits 1.0 at the exact handoff frame

              // 1. Measure the title's intrinsic width at 34px so we can center it mathematically.
              //    We inherit from DefaultTextStyle to pick up the theme's fontFamily,
              //    and apply the MediaQuery textScaler so the measurement matches the render exactly.
              final titleStyle = DefaultTextStyle.of(context).style.copyWith(
                    fontSize: 34,
                    fontWeight: leadingWidget != null
                        ? FontWeight.w700
                        : FontWeight.bold,
                    letterSpacing: -0.5,
                    height: 1.15,
                  );
              final textPainter = TextPainter(
                text: TextSpan(text: title, style: titleStyle),
                textDirection: TextDirection.ltr,
                textScaler: MediaQuery.textScalerOf(context),
              )..layout();
              final double textWidth = textPainter.width;
              final double screenWidth = constraints.maxWidth;

              // 2. Compute the two endpoints
              // START (t=0): text visually centered on screen (but clamped to left margin if too long)
              final double centeredLeft =
                  math.max(16.0, (screenWidth - textWidth) / 2);
              // END (t=1): text lands further to the right
              //   Subpage:    Positioned(left:16) + Chevron(50) + Gap(12) = 78px
              final double targetLeft =
                  leadingWidget != null ? 78.0 + xNudge : 32.0;

              // Pure linear interpolation — one formula, zero fighting forces
              final double currentLeft =
                  centeredLeft + (targetLeft - centeredLeft) * t;
              final double currentScale = 1.0 - (1.0 - (20.0 / 34.0)) * t;

              // ── PERFECT DIAGONAL TRAJECTORY & PINNING ──
              // 1. Where does the text start relative to the top of the container?
              final double startTextBottomFromTop = maxExtent - 128.0;

              // 2. Where should the text end exactly at t=1?
              // We want its visual center to align with the icons' center.
              // Icons top = ceiling. Icons center = ceiling + 24.0.
              // Scaled text height is ~23px. We add an optical adjustment to push it down
              // because font bounding boxes usually have empty space at the top.
              // We use 12.5 for subpages to perfectly center with the chevron, and 14.5 elsewhere.
              final double targetTextBottomFromTop =
                  ceiling + 24.0 + (leadingWidget != null ? 12.5 : 14.5);

              // 3. Linearly interpolate the absolute distance from the top of the container
              final double currentTextBottomFromTop = startTextBottomFromTop +
                  (targetTextBottomFromTop - startTextBottomFromTop) * t;

              // 4. Convert back to "bottom" coordinate so Positioned and Transform.scale work natively.
              // Since currentHeight shrinks during the final 12px (when t=1), subtracting from currentHeight
              // mathematically glues the text to the ceiling with the icons, completely eliminating the overshoot!
              final double currentBottom =
                  currentHeight - currentTextBottomFromTop;

              // ── MATCH THE PILL FADE TIMING ──
              // The text will stay perfectly visible and locked into place right up until
              // the list cards collide with the icons. At the exact moment the pill background
              // starts to fade IN to protect the icons, the text will synchronously fade OUT!
              double liftProgress = 0.0;
              final double currentPillHeight =
                  dynamicPillHeightNotifier?.value ?? 48.0;
              const double cardPadding =
                  21.0; // Matches ElvanCollapsedBar default
              final double collisionOffset = (expandedHeight + cardPadding) -
                  (ceiling + currentPillHeight);
              final double liftStartOffset = collisionOffset - 4.0;

              if (shrinkOffset > liftStartOffset) {
                liftProgress =
                    ((shrinkOffset - liftStartOffset) / 12.0).clamp(0.0, 1.0);
              }

              return Container(
                color: Colors.transparent,
                child: Stack(
                  clipBehavior: Clip.none,
                  fit: StackFit.expand,
                  children: [
                    // ── The Dynamic Scale-and-Move Title ──
                    // Positioned at exact pixel coordinates. Transform.scale shrinks
                    // the text around its bottom-left anchor so the left edge stays pinned.
                    Positioned(
                      left: currentLeft,
                      bottom: currentBottom,
                      child: Opacity(
                        opacity: 1.0 -
                            liftProgress, // Fade OUT exactly when pill fades IN!
                        child: Transform.scale(
                          scale: currentScale,
                          alignment: Alignment.bottomLeft,
                          child: ConstrainedBox(
                            constraints: BoxConstraints(
                              maxWidth: math.max(
                                  0.0,
                                  (screenWidth - currentLeft - 16.0) /
                                      currentScale),
                            ),
                            child: Text(
                              title,
                              textAlign: TextAlign.left,
                              maxLines: 1,
                              overflow: TextOverflow.ellipsis,
                              style: titleStyle.copyWith(
                                color: Theme.of(context).brightness ==
                                        Brightness.dark
                                    ? Colors.white
                                    : Colors.black87,
                              ),
                            ),
                          ),
                        ),
                      ),
                    ),

                    // ── The Page Header Leading Icon (Left) ──
                    if (leadingWidget != null)
                      Positioned(
                        top: currentTop,
                        left: 16,
                        child: Align(
                          alignment: Alignment.centerLeft,
                          child: Opacity(
                            opacity: isPinned
                                ? 0.0
                                : 1.0, // Hand off to Collapsed Bar when pinned!
                            child: leadingWidget,
                          ),
                        ),
                      ),
                    // ── The Page Header Icons (Right) ──
                    if (navActions.isNotEmpty)
                      Positioned(
                        top: currentTop,
                        right: 16,
                        child: Align(
                          alignment: Alignment.centerRight,
                          child: AnimatedOpacity(
                            duration: const Duration(milliseconds: 200),
                            opacity: (isSearchActive || isMenuOpen) ? 0.0 : 1.0,
                            child: IgnorePointer(
                              ignoring: isSearchActive || isMenuOpen,
                              child: Opacity(
                                opacity: isPinned
                                    ? 0.0
                                    : 1.0, // Hand off to Collapsed Bar when pinned!
                                child: Container(
                                  height: 50, // Must match ElvanCollapsedBar's 50px exactly!
                                  alignment: Alignment.center,
                                  child: Row(
                                    mainAxisSize: MainAxisSize.min,
                                    children: navActions,
                                  ),
                                ),
                              ),
                            ),
                          ),
                        ),
                      ),
                  ],
                ),
              );
            },
          );
        });
  }

  @override
  bool shouldRebuild(covariant ElvanExpandedBarDelegate oldDelegate) {
    return title != oldDelegate.title ||
        navActions != oldDelegate.navActions ||
        expandedHeight != oldDelegate.expandedHeight ||
        leadingWidget != oldDelegate.leadingWidget ||
        isSearchActiveNotifier != oldDelegate.isSearchActiveNotifier ||
        isMenuOpen != oldDelegate.isMenuOpen;
  }
}
