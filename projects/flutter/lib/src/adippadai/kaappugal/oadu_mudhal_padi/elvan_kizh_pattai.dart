import 'package:flutter/material.dart';

// ─────────────────────────────────────────────────────────────────────────────
// DATA MODEL — Fully decoupled nav item descriptor
// ─────────────────────────────────────────────────────────────────────────────

/// Describes a single tab entry for the custom floating pill navbar.
///
/// Each [CustomNavItem] carries its own [icon], [activeIcon], and [label]
/// so the shell remains completely agnostic of domain logic.
class CustomNavItem {
  const CustomNavItem({
    required this.icon,
    required this.label,
    this.activeIcon,
  });

  /// Icon rendered when this tab is **not** selected.
  final IconData icon;

  /// Optional override icon rendered when this tab **is** selected.
  /// Falls back to [icon] if null.
  final IconData? activeIcon;

  /// Human-readable label displayed beneath the icon.
  final String label;
}

// ─────────────────────────────────────────────────────────────────────────────
// ELVAN NAVBAR — Clear floating capsule that fades on scroll
// ─────────────────────────────────────────────────────────────────────────────

class ElvanNavbar extends StatefulWidget {
  const ElvanNavbar({
    super.key,
    required this.items,
    required this.currentIndex,
    required this.onTabSelected,
  });

  final List<CustomNavItem> items;
  final int currentIndex;
  final ValueChanged<int> onTabSelected;

  @override
  State<ElvanNavbar> createState() => _ElvanNavbarState();
}

class _ElvanNavbarState extends State<ElvanNavbar> {
  bool _isInteracting = false;
  double? _dragOffset;
  double _touchOffsetFromCenter = 0.0;
  int? _hoverIndex;
  int? _localLockedIndex;

  @override
  void didUpdateWidget(covariant ElvanNavbar oldWidget) {
    super.didUpdateWidget(oldWidget);
    // Clear the local visual lock once the parent finally updates the actual screen
    if (widget.currentIndex != oldWidget.currentIndex) {
      _localLockedIndex = null;
    }
  }

  @override
  Widget build(BuildContext context) {
    final int itemCount = widget.items.length;
    // The visual layout width. Increased spacing stretches the main white pill and makes room.
    final double layoutWidth = itemCount <= 4 ? 67.0 : 61.0;

    // The width of the active grey background pill. Restored to its beautiful elongated shape!
    // Overlap remains a perfectly safe 4px.
    final double bgWidth = itemCount <= 4 ? 75.0 : 69.0;

    const double horizontalPadding = 8.0;
    const double verticalPadding = 4.0;

    // Use hover index while dragging, otherwise the visually locked index, otherwise the parent's index
    int activeVisualIndex = (_isInteracting && _hoverIndex != null)
        ? _hoverIndex!
        : (_localLockedIndex ?? widget.currentIndex);

    // Calculate background pill left offset
    double targetLeft;
    if (_isInteracting && _dragOffset != null) {
      // Float freely exactly under the user's thumb without locking
      targetLeft = _dragOffset! - (bgWidth / 2);
    } else {
      // Snap cleanly to the mathematical center of the active slot ONLY on release
      double overlap = (bgWidth - layoutWidth) / 2;
      targetLeft = (activeVisualIndex * layoutWidth) - overlap;
    }

    // Constrain the background so it doesn't leave the pill boundaries
    double maxLeft =
        ((itemCount - 1) * layoutWidth) - ((bgWidth - layoutWidth) / 2);
    double minLeft = -((bgWidth - layoutWidth) / 2);
    targetLeft = targetLeft.clamp(minLeft, maxLeft);

    return AnimatedScale(
      scale: _isInteracting
          ? 1.02
          : 1.0, // Zoom effect matching Kotlin maxScale logic
      duration: const Duration(milliseconds: 150),
      curve: Curves.easeOutCubic,
      child: Container(
        height: 60,
        decoration: BoxDecoration(
          // ── Translucent white — content is clearly visible beneath ──
          color: const Color(0xFFFFFFFF).withValues(alpha: 0.88),
          borderRadius: BorderRadius.circular(100),
          border: Border.all(
            color: const Color(0xFFFFFFFF).withValues(alpha: 0.6),
            width: 0.5,
          ),
          boxShadow: [
            BoxShadow(
              blurRadius: 16,
              spreadRadius: 0,
              offset: const Offset(0, 4),
              color: Colors.black.withValues(alpha: 0.05),
            ),
          ],
        ),
        child: Padding(
          padding: const EdgeInsets.symmetric(
              horizontal: horizontalPadding, vertical: verticalPadding),
          child: GestureDetector(
            behavior: HitTestBehavior.opaque,
            onTapUp: (details) {
              int index = (details.localPosition.dx / layoutWidth)
                  .floor()
                  .clamp(0, itemCount - 1);
              setState(() {
                _localLockedIndex = index; // Visually snap the pill immediately
                _isInteracting = false;
                _dragOffset = null;
                _hoverIndex = null;
              });
              // Give the pill exactly 150ms to finish its fast-snap before the layout spike hits!
              Future.delayed(const Duration(milliseconds: 150), () {
                if (mounted) widget.onTabSelected(index);
              });
            },
            onHorizontalDragDown: (details) {
              setState(() {
                _isInteracting = true;
                _hoverIndex = (details.localPosition.dx / layoutWidth)
                    .floor()
                    .clamp(0, itemCount - 1);

                // Calculate the exact mathematical center of the slot they just touched
                double slotCenter =
                    (_hoverIndex! * layoutWidth) + (layoutWidth / 2);

                // Record exactly how far off-center their thumb is, so we can anchor the pill
                _touchOffsetFromCenter = details.localPosition.dx - slotCenter;

                _dragOffset =
                    null; // Do NOT track raw pixel yet, prevents "wiggle" on touch
              });
            },
            onHorizontalDragUpdate: (details) {
              setState(() {
                // The pill moves 1:1 with the thumb, but pushed from its original anchor point!
                double targetCenter =
                    details.localPosition.dx - _touchOffsetFromCenter;
                _dragOffset = targetCenter;
                _hoverIndex = (targetCenter / layoutWidth)
                    .floor()
                    .clamp(0, itemCount - 1);
              });
            },
            onHorizontalDragEnd: (details) {
              int? finalIndex = _hoverIndex;
              setState(() {
                if (finalIndex != null) {
                  _localLockedIndex =
                      finalIndex; // Visually snap the pill immediately
                }
                _isInteracting = false;
                _dragOffset = null;
                _hoverIndex = null;
              });
              if (finalIndex != null) {
                // Give the pill exactly 150ms to finish its fast-snap before the layout spike hits!
                Future.delayed(const Duration(milliseconds: 150), () {
                  if (mounted) widget.onTabSelected(finalIndex);
                });
              }
            },
            onHorizontalDragCancel: () {
              setState(() {
                _isInteracting = false;
                _dragOffset = null;
                _hoverIndex = null;
              });
            },
            child: SizedBox(
              width: layoutWidth * itemCount,
              child: Stack(
                clipBehavior: Clip.none,
                children: [
                  // ── Master Background Pill (Detached & Draggable) ──
                  AnimatedPositioned(
                    duration: (_isInteracting && _dragOffset != null)
                        ? Duration
                            .zero // Track finger instantly with zero lag while sliding
                        : const Duration(
                            milliseconds: 150), // Fast snap to lock on release
                    curve: (_isInteracting && _dragOffset != null)
                        ? Curves.linear
                        : Curves
                            .easeOutCubic, // Clean fast curve so it doesn't jerk
                    left: targetLeft,
                    top: 0,
                    bottom: 0,
                    width: bgWidth,
                    child: AnimatedScale(
                      scale: _isInteracting
                          ? 1.30
                          : 1.0, // Aggressive inner zoom breaking boundaries!
                      duration: const Duration(milliseconds: 150),
                      curve: Curves.easeOutCubic,
                      child: Container(
                        decoration: BoxDecoration(
                          color:
                              const Color(0xFFE5E5E5).withValues(alpha: 0.95),
                          borderRadius: BorderRadius.circular(100),
                          boxShadow: [
                            BoxShadow(
                              blurRadius: 4,
                              offset: const Offset(0, 1),
                              color: Colors.black.withValues(alpha: 0.04),
                            ),
                          ],
                        ),
                      ),
                    ),
                  ),

                  // ── Foreground Content ──
                  Row(
                    mainAxisSize: MainAxisSize.min,
                    mainAxisAlignment: MainAxisAlignment.center,
                    crossAxisAlignment: CrossAxisAlignment.stretch,
                    children: List.generate(itemCount, (index) {
                      final isActive = index == activeVisualIndex;
                      return _TranslucentNavItem(
                        item: widget.items[index],
                        isActive: isActive,
                        layoutWidth: layoutWidth,
                      );
                    }),
                  ),
                ],
              ),
            ),
          ),
        ),
      ),
    );
  }
}

// ─────────────────────────────────────────────────────────────────────────────
// TRANSLUCENT NAV ITEM — Render only the icon and text! No background or gesture
// ─────────────────────────────────────────────────────────────────────────────

class _TranslucentNavItem extends StatelessWidget {
  const _TranslucentNavItem({
    required this.item,
    required this.isActive,
    required this.layoutWidth,
  });

  final CustomNavItem item;
  final bool isActive;
  final double layoutWidth;

  @override
  Widget build(BuildContext context) {
    final icon = isActive ? (item.activeIcon ?? item.icon) : item.icon;
    final color = isActive ? const Color(0xFF1A1A1A) : const Color(0xFF7C7C80);

    // Uniform Apple pattern: large icons, small labels
    const double iconSize = 23.0;
    const double fontSize = 9.5;

    return SizedBox(
      width: layoutWidth,
      child: Column(
        mainAxisAlignment: MainAxisAlignment.center,
        mainAxisSize: MainAxisSize.min,
        children: [
          Icon(
            icon,
            size: iconSize,
            color: color,
          ),
          const SizedBox(height: 2),
          Padding(
            padding: const EdgeInsets.symmetric(horizontal: 4.0),
            child: FittedBox(
              fit: BoxFit.scaleDown,
              child: Text(
                item.label,
                style: TextStyle(
                  fontSize: fontSize,
                  fontWeight: isActive ? FontWeight.w600 : FontWeight.w400,
                  color: color,
                  height: 1.2,
                ),
              ),
            ),
          ),
        ],
      ),
    );
  }
}
