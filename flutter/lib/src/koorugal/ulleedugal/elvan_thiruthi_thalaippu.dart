import 'package:flutter/material.dart';

/// A shared, minimal label component used above all editor fields (Thiruthi).
/// Enforces standard 12px font size, 0.5 opacity, and 16px left padding.
class ElvanThiruthiThalaippu extends StatelessWidget {
  final String label;
  
  const ElvanThiruthiThalaippu({
    super.key,
    required this.label,
  });

  @override
  Widget build(BuildContext context) {
    return Padding(
      padding: const EdgeInsets.only(left: 16, bottom: 4),
      child: Text(
        label,
        style: TextStyle(
          fontSize: 12,
          fontWeight: FontWeight.w400,
          letterSpacing: 0.3,
          color: Theme.of(context).colorScheme.onSurface.withValues(alpha: 0.5),
        ),
      ),
    );
  }
}
