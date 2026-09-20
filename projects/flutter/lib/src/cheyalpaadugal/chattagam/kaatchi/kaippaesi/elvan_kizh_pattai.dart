import 'package:flutter/material.dart';
import 'package:flutter_svg/flutter_svg.dart';

import '../../../../koorugal/podhu_koorugal/elvan_kizh_pattai_base.dart';

// ─────────────────────────────────────────────────────────────────────────────
// DATA MODEL — Fully decoupled nav item descriptor
// ─────────────────────────────────────────────────────────────────────────────

/// Describes a single tab entry for the custom floating pill navbar.
///
/// Each [CustomNavItem] carries its own [icon], [activeIcon], and [label]
/// so the shell remains completely agnostic of domain logic.
class CustomNavItem {
  const CustomNavItem({
    this.icon,
    required this.label,
    this.headerLabel,
    this.activeIcon,
    this.svgString,
    this.activeSvgString,
  });

  /// Icon rendered when this tab is **not** selected.
  final IconData? icon;

  /// Optional override icon rendered when this tab **is** selected.
  final IconData? activeIcon;

  /// The label rendered in the bottom navigation bar.
  final String label;

  /// Optional label rendered in the top header. Defaults to [label] if not provided.
  final String? headerLabel;

  /// Optional SVG string rendered when this tab is **not** selected.
  final String? svgString;

  /// Optional override SVG string rendered when this tab **is** selected.
  final String? activeSvgString;
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
    this.hideContent = false,
  });

  final List<CustomNavItem> items;
  final int currentIndex;
  final ValueChanged<int> onTabSelected;
  final bool hideContent;

  @override
  State<ElvanNavbar> createState() => _ElvanNavbarState();
}

class _ElvanNavbarState extends State<ElvanNavbar> {
  bool _isInteracting = false;
  double? _dragOffset;
  double _touchOffsetFromCenter = 0.0;
  int? _hoverIndex;
  int? _localLockedIndex;
  bool _snapNextFrame = false;

  @override
  void didUpdateWidget(covariant ElvanNavbar oldWidget) {
    super.didUpdateWidget(oldWidget);
    // Clear the local visual lock once the parent finally updates the actual screen
    if (widget.currentIndex != oldWidget.currentIndex) {
      _localLockedIndex = null;
      // The tab just switched (which means this shell might have just become visible).
      // Instantly snap the pill so it doesn't replay an old animation and cause a visual glitch.
      _snapNextFrame = true;
      WidgetsBinding.instance.addPostFrameCallback((_) {
        if (mounted && _snapNextFrame) {
          setState(() {
            _snapNextFrame = false;
          });
        }
      });
    }
  }

  @override
  Widget build(BuildContext context) {
    final bool isDark = Theme.of(context).brightness == Brightness.dark;

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
      child: ElvanKizhPattaiBase(
        horizontalPadding: horizontalPadding,
        verticalPadding: verticalPadding,
        child: AnimatedOpacity(
            duration: const Duration(milliseconds: 150),
            opacity: widget.hideContent ? 0.0 : 1.0,
            child: GestureDetector(
              behavior: HitTestBehavior.opaque,
              onTapUp: (details) {
                int index = (details.localPosition.dx / layoutWidth)
                    .floor()
                    .clamp(0, itemCount - 1);
                setState(() {
                  _localLockedIndex =
                      index; // Visually snap the pill immediately
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
                  _touchOffsetFromCenter =
                      details.localPosition.dx - slotCenter;

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
                      duration: _snapNextFrame ||
                              (_isInteracting && _dragOffset != null)
                          ? Duration
                              .zero // Track finger instantly with zero lag while sliding
                          : const Duration(
                              milliseconds:
                                  150), // Fast snap to lock on release
                      curve: _snapNextFrame ||
                              (_isInteracting && _dragOffset != null)
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
                            color: isDark
                                ? const Color(0xFF333333)
                                    .withValues(alpha: 0.95)
                                : const Color(0xFFE5E5E5)
                                    .withValues(alpha: 0.95),
                            borderRadius: BorderRadius.circular(100),
                            boxShadow: isDark
                                ? null
                                : [
                                    BoxShadow(
                                      blurRadius: 4,
                                      offset: const Offset(0, 1),
                                      color:
                                          Colors.black.withValues(alpha: 0.04),
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
    final bool isDark = Theme.of(context).brightness == Brightness.dark;

    final icon = isActive ? (item.activeIcon ?? item.icon) : item.icon;
    final color = isActive
        ? (isDark ? Colors.white : const Color(0xFF1A1A1A))
        : (isDark ? Colors.grey.shade500 : const Color(0xFF7C7C80));

    // Uniform Apple pattern: large icons, small labels
    const double iconSize = 23.0;
    const double fontSize = 9.5;

    return SizedBox(
      width: layoutWidth,
      child: Column(
        mainAxisAlignment: MainAxisAlignment.center,
        mainAxisSize: MainAxisSize.min,
        children: [
          if (item.svgString != null)
            SvgPicture.string(
              isActive
                  ? (item.activeSvgString ?? item.svgString!)
                  : item.svgString!,
              width: iconSize,
              height: iconSize,
              colorFilter: ColorFilter.mode(color, BlendMode.srcIn),
            )
          else
            Icon(
              isActive ? (item.activeIcon ?? item.icon) : item.icon,
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
