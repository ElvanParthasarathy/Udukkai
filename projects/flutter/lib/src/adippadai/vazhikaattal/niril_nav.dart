import 'package:flutter/material.dart';
import 'package:flutter/foundation.dart';
import '../../cheyalpaadugal/chattagam/kaatchi/kanini/elvan_kanini_chattagam.dart';
import '../../cheyalpaadugal/chattagam/kaatchi/kaippaesi/koorugal/elvan_thirai_vazhi.dart';

/// True if the current operating system is a desktop OS (Windows, macOS, Linux).
bool get isDesktopOS {
  if (kIsWeb) return false;
  return defaultTargetPlatform == TargetPlatform.windows ||
         defaultTargetPlatform == TargetPlatform.macOS ||
         defaultTargetPlatform == TargetPlatform.linux;
}

/// True if we should render the Desktop layout (either because it's a Desktop OS, 
/// or because the given width is >= 800).
bool isDesktopLayout(double width) {
  return isDesktopOS || width >= 800;
}

/// True if we should render the Desktop layout using the given context's width.
bool isDesktopLayoutContext(BuildContext context) {
  return isDesktopLayout(MediaQuery.sizeOf(context).width);
}

final GlobalKey<NavigatorState> globalRootNavigatorKey = GlobalKey<NavigatorState>();

/// Industry-standard navigation helper that automatically routes page pushes
/// through the correct navigator based on the current layout.
///
/// On **desktop** (width >= 800): Pushes into the nested `desktopNavigatorKey`
/// navigator so the sidebar stays visible and the subpage only covers the
/// content area.
///
/// On **mobile**: Pushes onto the root navigator with a Cupertino slide
/// transition (`ElvanPageRoute`).
///
/// Usage:
/// ```dart
/// NirilNav.push(context, const SilkInvoiceEditor());
/// ```
class NirilNav {
  NirilNav._(); // Private constructor — static-only class

  /// Push a page using the correct navigator for the current layout.
  ///
  /// Returns the [Future] from the push so callers can await results.
  static Future<T?> push<T>(BuildContext context, Widget page) {
    final width = MediaQuery.sizeOf(context).width;

    if (isDesktopLayout(width) && desktopNavigatorKey.currentState != null) {
      // Desktop: push into the nested navigator (content area only)
      return desktopNavigatorKey.currentState!.push<T>(
        MaterialPageRoute(builder: (_) => page),
      );
    } else {
      // Mobile: push onto the root navigator with Cupertino transition
      return Navigator.of(context, rootNavigator: true).push<T>(
        ElvanPageRoute(builder: (_) => page),
      );
    }
  }

  /// Pop the topmost route from the correct navigator.
  static void pop<T>(BuildContext context, [T? result]) {
    final width = MediaQuery.sizeOf(context).width;

    if (width >= 800 && desktopNavigatorKey.currentState != null) {
      desktopNavigatorKey.currentState!.pop<T>(result);
    } else {
      Navigator.of(context, rootNavigator: true).pop<T>(result);
    }
  }
}
