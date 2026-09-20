import 'dart:async';
import 'package:flutter/material.dart';

const List<String> _greetings = ["வணக்கம்!", "Hello!", "നമസ്കാരം!"];

class GreetingStep extends StatefulWidget {
  final VoidCallback onComplete;

  const GreetingStep({super.key, required this.onComplete});

  @override
  State<GreetingStep> createState() => _GreetingStepState();
}

class _GreetingStepState extends State<GreetingStep> {
  int _greetingIndex = 0;
  double _greetingOpacity = 0.0;
  Timer? _greetingTimer;

  @override
  void initState() {
    super.initState();
    _startGreetingSequence();
  }

  @override
  void dispose() {
    _greetingTimer?.cancel();
    super.dispose();
  }

  Future<void> _startGreetingSequence() async {
    for (int i = 0; i < _greetings.length; i++) {
      if (!mounted) return;
      
      setState(() {
        _greetingIndex = i;
        _greetingOpacity = 1.0;
      });

      await Future.delayed(const Duration(milliseconds: 1200));

      if (!mounted) return;

      setState(() {
        _greetingOpacity = 0.0;
      });

      await Future.delayed(const Duration(milliseconds: 800));
    }

    if (!mounted) return;
    widget.onComplete();
  }

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final isDark = theme.brightness == Brightness.dark;
    final textColor = isDark ? Colors.white : const Color(0xFF1A1A1A);

    return Center(
      key: const ValueKey('greeting_step'),
      child: AnimatedOpacity(
        opacity: _greetingOpacity,
        duration: const Duration(milliseconds: 500),
        child: Text(
          _greetings[_greetingIndex],
          style: TextStyle(
            fontSize: 48,
            fontWeight: FontWeight.w300,
            color: textColor,
            letterSpacing: -1,
          ),
        ),
      ),
    );
  }
}
