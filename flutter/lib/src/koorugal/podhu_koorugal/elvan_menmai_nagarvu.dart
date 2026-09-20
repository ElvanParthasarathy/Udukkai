import 'package:flutter/gestures.dart';
import 'package:flutter/material.dart';
import 'dart:io';

/// A wrapper that adds native-feeling smooth scrolling to desktop apps
/// using an existing ScrollController, without breaking mobile touch/drag or trackpads.
class ElvanSmoothScroll extends StatefulWidget {
  final ScrollController? controller;
  final Widget child;
  final double scrollSpeed;
  final int durationMs;

  const ElvanSmoothScroll({
    super.key,
    this.controller,
    required this.child,
    this.scrollSpeed = 1.3,
    this.durationMs = 380,
  });

  @override
  State<ElvanSmoothScroll> createState() => _ElvanSmoothScrollState();
}

class _ElvanSmoothScrollState extends State<ElvanSmoothScroll> {
  double _futurePosition = 0;
  bool _isDesktop = false;
  bool _isMouseScrolling = false;
  ScrollController? _activeController;

  @override
  void didChangeDependencies() {
    super.didChangeDependencies();
    _activeController =
        widget.controller ?? PrimaryScrollController.maybeOf(context);
  }

  @override
  void initState() {
    super.initState();
    _isDesktop = Platform.isWindows || Platform.isMacOS || Platform.isLinux;
  }

  void _handlePointerSignal(PointerSignalEvent event) {
    if (!_isDesktop) return;

    if (event is PointerScrollEvent) {
      if (event.kind == PointerDeviceKind.mouse) {
        // MOUSE WHEEL DETECTED
        if (!_isMouseScrolling) {
          setState(() => _isMouseScrolling = true);
        }

        if (_activeController == null || !_activeController!.hasClients) return;

        final position = _activeController!.position;
        final maxExtent = position.maxScrollExtent;
        final minExtent = position.minScrollExtent;

        if ((_futurePosition - position.pixels).abs() >
            widget.scrollSpeed * 100) {
          _futurePosition = position.pixels;
        }

        final dy = event.scrollDelta.dy * widget.scrollSpeed;

        if (position.atEdge &&
            (position.pixels == minExtent && dy < 0 ||
                position.pixels == maxExtent && dy > 0)) {
          return;
        }

        _futurePosition += dy;
        _futurePosition = _futurePosition.clamp(minExtent, maxExtent);

        _activeController!.animateTo(
          _futurePosition,
          duration: Duration(milliseconds: widget.durationMs),
          curve: Curves.easeOutQuart,
        );
      } else {
        // TRACKPAD SCROLL DETECTED
        if (_isMouseScrolling) {
          setState(() => _isMouseScrolling = false);
        }
      }
    }
  }

  void _handlePointerDown(PointerDownEvent event) {
    if (_isDesktop && _isMouseScrolling) {
      // TOUCH/DRAG DETECTED
      setState(() => _isMouseScrolling = false);
    }
  }

  @override
  Widget build(BuildContext context) {
    if (!_isDesktop) return widget.child;

    return Listener(
      behavior: HitTestBehavior.opaque,
      onPointerSignal: _handlePointerSignal,
      onPointerDown: _handlePointerDown,
      child: ScrollConfiguration(
        behavior: _SmoothScrollBehavior(isMouseScrolling: _isMouseScrolling),
        child: widget.child,
      ),
    );
  }
}

class _SmoothScrollBehavior extends MaterialScrollBehavior {
  final bool isMouseScrolling;
  const _SmoothScrollBehavior({required this.isMouseScrolling});

  @override
  ScrollPhysics getScrollPhysics(BuildContext context) {
    if (isMouseScrolling) {
      return const NeverScrollableScrollPhysics();
    }
    return const BouncingScrollPhysics();
  }

  @override
  Set<PointerDeviceKind> get dragDevices => {
        PointerDeviceKind.touch,
        PointerDeviceKind.mouse,
        PointerDeviceKind.trackpad,
        PointerDeviceKind.stylus,
      };
}
