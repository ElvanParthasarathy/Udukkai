import 'dart:ui';
import 'package:flutter/material.dart';

/// A scroll behavior that allows mouse and trackpad dragging for easier
/// testing and interaction on Desktop/Windows.
/// It applies iOS-style bouncing physics to Desktop (Windows/Mac/Linux)
/// while leaving Android physics perfectly native and untouched.
class ElvanScrollBehavior extends MaterialScrollBehavior {
  @override
  Set<PointerDeviceKind> get dragDevices => {
        PointerDeviceKind.touch,
        PointerDeviceKind.mouse, // Enables click-and-drag for mouse
        PointerDeviceKind.trackpad, // Enables native trackpad gestures
        PointerDeviceKind.stylus,
      };
}
