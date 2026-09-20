import 'package:flutter/material.dart';
import 'package:elvan_niril/src/cheyalpaadugal/chattagam/kaatchi/kaippaesi/elvan_chattagam.dart';
import '../../cheyalpaadugal/chattagam/kaatchi/kaippaesi/koorugal/elvan_moodu_pothan.dart';

/// A wrapper around [ElvanShell] exclusively designed for fullscreen popups (modals).
/// It inherently injects the [ElvanCloseButton] into the shell's physics engine
/// and disables the bottom navigation bar.
class ElvanFullscreenPopup extends StatelessWidget {
  const ElvanFullscreenPopup({
    super.key,
    required this.title,
    required this.slivers,
    this.navActions = const [],
    this.startCollapsed = true,
    this.backgroundColor,
  });

  final String title;
  final List<Widget> slivers;
  final List<Widget> navActions;
  final bool startCollapsed;
  final Color? backgroundColor;

  @override
  Widget build(BuildContext context) {
    return ElvanShell(
      title: title,
      slivers: slivers,
      showNavbar: false,
      leadingWidget: const ElvanCloseButton(),
      navActions: navActions,
      startCollapsed: startCollapsed,
      backgroundColor: backgroundColor,
      syncWithGlobalHeader: false,
    );
  }
}
