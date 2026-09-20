import 'package:flutter/material.dart';

import 'elvan_kizh_pattai.dart';
import 'elvan_mel_pattai.dart';

// ─────────────────────────────────────────────────────────────────────────────
// ONE UI TEMPLATE SHELL — The reusable page scaffold
// ─────────────────────────────────────────────────────────────────────────────

/// A production-grade, Samsung One UI-inspired page shell featuring:
///
/// 1. **One-handed collapsible header** — large title low on screen that
///    shrinks into a pinned centered toolbar on scroll.
/// 2. **Boundary gradient fade masks** — soft fade at top/bottom edges so
///    list items melt away before reaching hard panel borders.
/// 3. **100 % custom floating pill navbar** — capsule-shaped bottom bar
///    positioned with [AnimatedPositioned] inside a [Stack].
/// 4. **Scroll-to-hide engine** — the pill slides off-screen on downward
///    scroll and slides back on upward scroll.
///
/// This widget is **fully decoupled** — pass any [body], [title],
/// [navActions], [navItems], [currentIndex], and [onTabSelected] callback.
class ElvanShell extends StatefulWidget {
  const ElvanShell({
    super.key,
    required this.slivers,
    required this.title,
    required this.navActions,
    required this.navItems,
    required this.currentIndex,
    required this.onTabSelected,
  });

  /// The scrollable content placed inside the [CustomScrollView] as slivers.
  final List<Widget> slivers;

  /// Large title shown in the expanded header area.
  final String title;

  /// Action widgets rendered below the title in the expanded header
  /// (e.g. add, search, overflow menu icons).
  final List<Widget> navActions;

  /// Tab descriptors for the floating pill navbar.
  final List<CustomNavItem> navItems;

  /// Zero-based index of the currently active tab.
  final int currentIndex;

  /// Callback fired when the user taps a different tab.
  final ValueChanged<int> onTabSelected;

  @override
  State<ElvanShell> createState() => _ElvanShellState();
}

class _ElvanShellState extends State<ElvanShell>
    with SingleTickerProviderStateMixin {
  // ── Navbar hide / show animation ──────────────────────────────────────
  late final AnimationController _navbarController;
  late final Animation<double> _navbarOpacity;

  // ── Scroll tracking for One UI Physics ────────────────────────────────
  late final ScrollController _scrollController;

  /// Navbar total height including internal padding.
  static const double _kNavbarHeight = 60.0;

  /// Bottom margin between the pill and the screen edge.
  static const double _kNavbarBottomMargin = 28.0;

  /// Height of the gradient fade mask at each boundary.
  static const double _kFadeMaskHeight = 96.0;

  /// Expanded header height for the One UI style large title.
  static const double _kExpandedHeight = 320.0;

  bool _isNavbarVisible = true;

  @override
  void initState() {
    super.initState();
    _navbarController = AnimationController(
      vsync: this,
      duration: const Duration(milliseconds: 280),
    );
    _navbarOpacity = Tween<double>(
      begin: 1.0,
      end: 0.0,
    ).animate(CurvedAnimation(
      parent: _navbarController,
      curve: Curves.easeOutCubic,
      reverseCurve: Curves.easeInCubic,
    ));

    _scrollController = ScrollController();
  }

  @override
  void dispose() {
    _navbarController.dispose();
    _scrollController.dispose();
    super.dispose();
  }

  // ── Scroll direction listener ─────────────────────────────────────────

  bool _handleScrollNotification(ScrollNotification notification) {
    if (notification is ScrollUpdateNotification) {
      final delta = notification.scrollDelta ?? 0;

      if (delta > 0) {
        // Scrolling DOWN (finger moving up)

        // Calculate if the gallery cards have physically collided with the icons
        // to turn on the white background (the TRUE pill state!)
        final double statusBarHeight = MediaQuery.paddingOf(context).top;
        final double ceiling = statusBarHeight + 20.0;

        final double collisionOffset =
            (_kExpandedHeight + 32.0) - (ceiling + 50.0);
        final double liftStartOffset = collisionOffset - 4.0;

        final bool isTruePill = _scrollController.offset > liftStartOffset;

        // Hide ONLY if it's a momentum fling (finger off screen) AND it is a TRUE pill!
        final bool isMomentumFling = notification.dragDetails == null;
        if (isMomentumFling && delta > 2.0 && _isNavbarVisible && isTruePill) {
          _isNavbarVisible = false;
          _navbarController.forward();
        }
      } else if (delta < 0) {
        // Scrolling UP (finger moving down) -> Show immediately!
        if (!_isNavbarVisible) {
          _isNavbarVisible = true;
          _navbarController.reverse();
        }
      }
    } else if (notification is ScrollEndNotification) {
      // Show immediately when scrolling comes to a complete stop
      if (!_isNavbarVisible) {
        _isNavbarVisible = true;
        _navbarController.reverse();
      }

      // ── Samsung One UI Physics: Snapping the Header ──
      // Calculate how far the header travels before collapsing
      final double statusBarHeight = MediaQuery.paddingOf(context).top;
      final double snapThreshold =
          _kExpandedHeight - kToolbarHeight - statusBarHeight;

      final currentOffset = _scrollController.offset;
      if (currentOffset > 0 && currentOffset < snapThreshold) {
        // We are caught in the middle! Snap to the closest stage.
        final targetOffset =
            currentOffset > (snapThreshold / 2) ? snapThreshold : 0.0;

        Future.microtask(() {
          _scrollController.animateTo(
            targetOffset,
            duration: const Duration(milliseconds: 250),
            curve: Curves.easeOutCubic,
          );
        });
      }
    }
    return false; // allow notification to continue bubbling
  }

  // ── Build ─────────────────────────────────────────────────────────────

  @override
  Widget build(BuildContext context) {
    final backgroundColor = Theme.of(context).scaffoldBackgroundColor;

    return Scaffold(
      body: Stack(
        children: [
          // ─── Layer 1: Scrollable content with SliverAppBar ────────────
          NotificationListener<ScrollNotification>(
            onNotification: _handleScrollNotification,
            child: _buildScrollView(context, backgroundColor),
          ),

          // ─── Layer 2: Bottom boundary gradient fade mask ──────────────
          Positioned(
            left: 0,
            right: 0,
            bottom: 0,
            height: _kFadeMaskHeight + _kNavbarHeight + _kNavbarBottomMargin,
            child: IgnorePointer(
              child: DecoratedBox(
                decoration: BoxDecoration(
                  gradient: LinearGradient(
                    begin: Alignment.topCenter,
                    end: Alignment.bottomCenter,
                    colors: [
                      backgroundColor.withAlpha(0),
                      backgroundColor.withAlpha(40),
                      backgroundColor.withAlpha(140),
                      backgroundColor,
                    ],
                    stops: const [0.0, 0.3, 0.65, 1.0],
                  ),
                ),
              ),
            ),
          ),

          // ─── Layer 2.5: Top boundary gradient fade mask ───────────────
          Positioned(
            left: 0,
            right: 0,
            top: 0,
            height: _kFadeMaskHeight, // 96 pixels of smooth top fade
            child: IgnorePointer(
              child: DecoratedBox(
                decoration: BoxDecoration(
                  gradient: LinearGradient(
                    begin: Alignment.topCenter,
                    end: Alignment.bottomCenter,
                    colors: [
                      backgroundColor, // Solid at the very top edge
                      backgroundColor.withAlpha(140),
                      backgroundColor.withAlpha(40),
                      backgroundColor.withAlpha(0), // Fully transparent below
                    ],
                    stops: const [0.0, 0.35, 0.7, 1.0],
                  ),
                ),
              ),
            ),
          ),

          // ─── Layer 3: Floating pill navbar ────────────────────────────
          Positioned(
            left: 0,
            right: 0,
            bottom: _kNavbarBottomMargin,
            child: Center(
              child: FadeTransition(
                opacity: _navbarOpacity,
                child: AnimatedBuilder(
                  animation: _navbarOpacity,
                  builder: (context, child) {
                    return IgnorePointer(
                      ignoring: _navbarOpacity.value < 0.5,
                      child: child,
                    );
                  },
                  child: ElvanNavbar(
                    items: widget.navItems,
                    currentIndex: widget.currentIndex,
                    onTabSelected: widget.onTabSelected,
                  ),
                ),
              ),
            ),
          ),

          // ── Component B: Independent Top Bar (Pill) ──
          ElvanTopBar(
            scrollController: _scrollController,
            hideAnimation: _navbarOpacity,
            navActions: widget.navActions,
            expandedHeight: _kExpandedHeight,
          ),
        ],
      ),
    );
  }

  // ── CustomScrollView with SliverAppBar + body ─────────────────────────

  Widget _buildScrollView(BuildContext context, Color bg) {
    return CustomScrollView(
      controller: _scrollController,
      physics: const AlwaysScrollableScrollPhysics(),
      slivers: [
        SliverPersistentHeader(
          pinned: true,
          delegate: _ElvanHeaderDelegate(
            title: widget.title,
            navActions: widget.navActions,
            statusBarHeight: MediaQuery.paddingOf(context).top,
          ),
        ),

        // ── Body content slivers ──
        ...widget.slivers,
      ],
    );
  }
}

// ─────────────────────────────────────────────────────────────────────────────
// NATIVE SAMSUNG ONE UI HEADER DELEGATE
// ─────────────────────────────────────────────────────────────────────────────

class _ElvanHeaderDelegate extends SliverPersistentHeaderDelegate {
  _ElvanHeaderDelegate({
    required this.title,
    required this.navActions,
    required this.statusBarHeight,
  });

  final String title;
  final List<Widget> navActions;
  final double statusBarHeight;

  @override
  double get maxExtent => 320.0;

  @override
  double get minExtent => statusBarHeight + 72.0;

  // Crucial: Allow the header to physically stretch during Android overscroll!
  // Note: We don't need a StretchConfiguration here because returning stretch=true isn't supported directly,
  // but Flutter passes constraints.maxHeight directly anyway for stretches if physics allows it!

  @override
  Widget build(
      BuildContext context, double shrinkOffset, bool overlapsContent) {
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

        return Container(
          color: Colors.transparent,
          child: Stack(
            clipBehavior: Clip.none,
            fit: StackFit.expand,
            children: [
              // ── Massive Title ──
              Positioned(
                left: 24,
                right: 24,
                bottom: 64 + expandedButtonsBottom + kToolbarHeight,
                child: Opacity(
                  opacity: titleOpacity,
                  child: Text(
                    title,
                    textAlign: TextAlign.center,
                    style: const TextStyle(
                      fontSize: 34,
                      fontWeight: FontWeight.w700,
                      color: Colors.black87,
                      letterSpacing: -0.5,
                      height: 1.15,
                    ),
                  ),
                ),
              ),
              // ── Component A: The Page Header Icons ──
              Positioned(
                top: currentTop,
                right: 16,
                child: Align(
                  alignment: Alignment.centerRight,
                  child: Opacity(
                    opacity: isPinned
                        ? 0.0
                        : 1.0, // Hand off to Component B when pinned!
                    child: Container(
                      padding: const EdgeInsets.symmetric(
                          horizontal: 4, vertical: 5),
                      child: Row(
                        mainAxisSize: MainAxisSize.min,
                        children: navActions,
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
  }

  @override
  bool shouldRebuild(covariant _ElvanHeaderDelegate oldDelegate) {
    return title != oldDelegate.title ||
        navActions != oldDelegate.navActions ||
        statusBarHeight != oldDelegate.statusBarHeight;
  }
}
