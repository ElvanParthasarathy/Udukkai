import 'package:flutter/cupertino.dart';
import 'package:flutter/foundation.dart';

class ElvanPageRoute<T> extends CupertinoPageRoute<T> {
  ElvanPageRoute({
    required super.builder,
    super.title,
    super.settings,
    super.maintainState,
    super.fullscreenDialog,
  });

  @override
  bool get popGestureEnabled {
    // Disable edge-swipe-to-pop on Android, but keep the Cupertino transition animation.
    if (defaultTargetPlatform == TargetPlatform.android) {
      return false;
    }
    return super.popGestureEnabled;
  }
}
