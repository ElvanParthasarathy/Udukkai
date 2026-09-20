import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'dart:async';

/// Global Keyboard Listener for Hardware Barcode Scanners.
/// A barcode scanner acts like a keyboard that types very fast and ends with "Enter".
class ElvanBarcodeListener extends StatefulWidget {
  final Widget child;
  final ValueChanged<String> onBarcodeScanned;
  final Duration bufferDuration;

  const ElvanBarcodeListener({
    super.key,
    required this.child,
    required this.onBarcodeScanned,
    this.bufferDuration = const Duration(milliseconds: 50),
  });

  @override
  State<ElvanBarcodeListener> createState() => _ElvanBarcodeListenerState();
}

class _ElvanBarcodeListenerState extends State<ElvanBarcodeListener> {
  final StringBuffer _buffer = StringBuffer();
  Timer? _timer;

  void _onKey(KeyEvent event) {
    if (event is KeyDownEvent) {
      if (event.logicalKey == LogicalKeyboardKey.enter) {
        if (_buffer.isNotEmpty) {
          final barcode = _buffer.toString();
          _buffer.clear();
          widget.onBarcodeScanned(barcode);
        }
      } else if (event.character != null) {
        _buffer.write(event.character);
        _timer?.cancel();
        _timer = Timer(widget.bufferDuration, () {
          _buffer.clear();
        });
      }
    }
  }

  @override
  Widget build(BuildContext context) {
    return KeyboardListener(
      focusNode: FocusNode()..requestFocus(),
      onKeyEvent: _onKey,
      autofocus: true,
      child: widget.child,
    );
  }
}
