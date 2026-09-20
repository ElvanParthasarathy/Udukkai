import 'package:flutter/material.dart';

/// திருத்தி அட்டை கூறு — Editor card matching React's ElvanCard component.
///
/// Features:
/// - `borderRadius: 24` (default, customizable)
/// - No shadow, no border
/// - Light: white bg, Dark: `rgba(255,255,255,0.03)`
/// - Optional tap with scale animation
/// InheritedWidget to let descendants (like text fields) know they are inside a card.
class ElvanAttaiSoolal extends InheritedWidget {
  final bool isInsideCard;

  const ElvanAttaiSoolal({
    super.key,
    required this.isInsideCard,
    required super.child,
  });

  static bool check(BuildContext context) {
    return context.dependOnInheritedWidgetOfExactType<ElvanAttaiSoolal>()?.isInsideCard ?? false;
  }

  @override
  bool updateShouldNotify(ElvanAttaiSoolal oldWidget) {
    return isInsideCard != oldWidget.isInsideCard;
  }
}

class ElvanThiruthiAttai extends StatelessWidget {
  const ElvanThiruthiAttai({
    super.key,
    required this.child,
    this.borderRadius = 24,
    this.padding,
    this.onTap,
    this.margin,
    this.color,
  });

  final Widget child;
  final double borderRadius;
  final EdgeInsetsGeometry? padding;
  final VoidCallback? onTap;
  final EdgeInsetsGeometry? margin;
  final Color? color;

  @override
  Widget build(BuildContext context) {
    final isDark = Theme.of(context).brightness == Brightness.dark;

    final container = Material(
      type: MaterialType.card,
      color: color ?? (isDark
          ? Colors.white.withValues(alpha: 0.03)
          : Colors.white),
      borderRadius: BorderRadius.circular(borderRadius),
      animationDuration: const Duration(milliseconds: 200),
      child: Container(
        margin: margin,
        padding: padding ??
            const EdgeInsets.symmetric(horizontal: 16, vertical: 16),
        child: ElvanAttaiSoolal(
          isInsideCard: true,
          child: child,
        ),
      ),
    );

    if (onTap != null) {
      return GestureDetector(
        onTap: onTap,
        child: container,
      );
    }

    return container;
  }
}

/// Item card (used inside line items) — smaller radius, different bg.
///
/// Matches React's item row: `borderRadius: 16px`,
/// `bgcolor: dark ? 'action.hover' : 'background.paper'`
class ElvanUrupadiAttai extends StatelessWidget {
  const ElvanUrupadiAttai({
    super.key,
    required this.child,
    this.padding,
    this.margin,
  });

  final Widget child;
  final EdgeInsetsGeometry? padding;
  final EdgeInsetsGeometry? margin;

  @override
  Widget build(BuildContext context) {
    final cs = Theme.of(context).colorScheme;
    final isDark = Theme.of(context).brightness == Brightness.dark;

    return Container(
      decoration: BoxDecoration(
        color: isDark 
            ? cs.onSurface.withValues(alpha: 0.08) 
            : Colors.white,
        borderRadius: BorderRadius.circular(24),
      ),
      clipBehavior: Clip.antiAlias,
      margin: margin,
      padding: padding ?? const EdgeInsets.symmetric(vertical: 8),
      child: Material(
        type: MaterialType.transparency,
        child: ElvanAttaiSoolal(
          isInsideCard: true,
          child: child,
        ),
      ),
    );
  }
}
