import 'package:flutter/material.dart';

/// The purely structural base component for bottom floating pills.
/// Handles only the shape, border, shadow, background color, and fixed height.
class ElvanKizhPattaiBase extends StatelessWidget {
  const ElvanKizhPattaiBase({
    super.key,
    required this.child,
    this.horizontalPadding = 8.0,
    this.verticalPadding = 4.0,
  });

  final Widget child;
  final double horizontalPadding;
  final double verticalPadding;

  @override
  Widget build(BuildContext context) {
    final bool isDark = Theme.of(context).brightness == Brightness.dark;

    return Container(
      height: 60,
      decoration: BoxDecoration(
        // ── Translucent background — content is clearly visible beneath ──
        color: isDark
            ? const Color(0xFF1E1E1E).withValues(alpha: 0.88)
            : const Color(0xFFFFFFFF).withValues(alpha: 0.88),
        borderRadius: BorderRadius.circular(100),
        border: Border.all(
          color: isDark
              ? const Color(0xFF333333).withValues(alpha: 0.15)
              : const Color(0xFFFFFFFF).withValues(alpha: 0.6),
          width: 0.5,
        ),
        boxShadow: [
          BoxShadow(
            blurRadius: 16,
            spreadRadius: 0,
            offset: const Offset(0, 4),
            color: Colors.black.withValues(alpha: 0.05),
          ),
        ],
      ),
      child: Padding(
        padding: EdgeInsets.symmetric(
            horizontal: horizontalPadding, vertical: verticalPadding),
        child: child,
      ),
    );
  }
}
