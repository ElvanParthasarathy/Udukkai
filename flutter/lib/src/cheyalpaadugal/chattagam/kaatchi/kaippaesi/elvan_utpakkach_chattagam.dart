import 'package:flutter/material.dart';
import '../../../../adippadai/vazhikaattal/niril_nav.dart';

import 'elvan_chattagam.dart';
import '../kanini/elvan_kanini_utpakkach_chattagam.dart';
import 'koorugal/elvan_pin_pothan.dart';

/// A wrapper around [ElvanShell] exclusively designed for subpages (e.g. Settings, PDF Viewer).
/// It inherently injects the [ElvanBackButton] into the shell's physics engine
/// and disables the bottom navigation bar.
class ElvanSubpageShell extends StatefulWidget {
  final String title;
  final List<Widget> slivers;
  final List<Widget> navActions;
  final bool startCollapsed;
  final Color? backgroundColor;
  final bool hideHeaderOnDesktop;
  final EdgeInsetsGeometry? contentPadding;
  final double maxWidth;

  const ElvanSubpageShell({
    super.key,
    required this.title,
    required this.slivers,
    this.navActions = const [],
    this.startCollapsed = true,
    this.backgroundColor,
    this.hideHeaderOnDesktop = false,
    this.contentPadding,
    this.maxWidth = 680,
  });

  @override
  State<ElvanSubpageShell> createState() => _ElvanSubpageShellState();
}

class _ElvanSubpageShellState extends State<ElvanSubpageShell> {
  @override
  Widget build(BuildContext context) {
    // Use width-based detection combined with OS checks so that resizing
    // the window on desktop correctly locks to the desktop shell.
    final isDesktop = isDesktopLayoutContext(context);

    if (isDesktop) {
      return ElvanDesktopSubpageShell(
        title: widget.title,
        slivers: widget.slivers,
        backgroundColor: widget.backgroundColor,
        hideHeaderOnDesktop: widget.hideHeaderOnDesktop,
        contentPadding: widget.contentPadding,
        maxWidth: widget.maxWidth,
      );
    }

    // Mobile view uses the full ElvanShell with draggable physics
    return ElvanShell(
      title: widget.title,
      showNavbar: false,
      startCollapsed: widget.startCollapsed,
      navActions: widget.navActions,
      leadingWidget: const ElvanBackButton(),
      showLeadingWidgetInExpandedBar: true,
      slivers: widget.slivers,
      backgroundColor: widget.backgroundColor,
      syncWithGlobalHeader: false,
    );
  }
}
