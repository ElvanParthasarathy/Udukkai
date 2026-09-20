import 'dart:async';
import 'package:flutter/foundation.dart';

/// A utility class for debouncing rapidly firing events.
/// Helpful for search inputs to prevent spamming the database.
class ElvanDebouncer {
  final int milliseconds;
  Timer? _timer;

  ElvanDebouncer({required this.milliseconds});

  void run(VoidCallback action) {
    _timer?.cancel();
    _timer = Timer(Duration(milliseconds: milliseconds), action);
  }

  void dispose() {
    _timer?.cancel();
  }
}
