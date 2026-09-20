import 'package:flutter/material.dart';

/// A completely reusable Elvan Card component.
/// It wraps the content in a Material + InkWell to provide the native
/// Android ripple effect upon tap. It also smoothly animates background
/// color changes when `isSelected` changes.
class ElvanPothuAttai extends StatefulWidget {
  const ElvanPothuAttai({
    super.key,
    required this.child,
    this.onTap,
    this.onLongPress,
    this.isSelected = false,
    this.padding = const EdgeInsets.all(16),
    this.borderRadius = 24.0,
  });

  /// The inner content of the card
  final Widget child;

  /// Triggered on tap
  final VoidCallback? onTap;

  /// Triggered on long press
  final VoidCallback? onLongPress;

  /// Whether the card is selected (changes the background color)
  final bool isSelected;

  /// Inner padding for the content
  final EdgeInsetsGeometry padding;

  /// Corner border radius
  final double borderRadius;

  @override
  State<ElvanPothuAttai> createState() => _ElvanPothuAttaiState();
}

class _ElvanPothuAttaiState extends State<ElvanPothuAttai> {
  bool _isPressed = false;

  void _handleTapDown(TapDownDetails details) {
    setState(() => _isPressed = true);
  }

  void _handleTapUp(TapUpDetails details) {
    setState(() => _isPressed = false);
  }

  void _handleTapCancel() {
    setState(() => _isPressed = false);
  }

  @override
  Widget build(BuildContext context) {
    final isDark = Theme.of(context).brightness == Brightness.dark;

    final defaultBg = isDark ? const Color(0xFF111111) : Colors.white;
    final selectedBg = isDark
        ? const Color(0xFF1A1A1A)
        : Colors.black.withValues(alpha: 0.04);
    
    final bgColor = widget.isSelected ? selectedBg : defaultBg;

    return GestureDetector(
      onTapDown: _handleTapDown,
      onTapUp: _handleTapUp,
      onTapCancel: _handleTapCancel,
      child: AnimatedScale(
        scale: _isPressed ? 0.985 : 1.0,
        duration: _isPressed
            ? const Duration(milliseconds: 100)
            : const Duration(milliseconds: 200),
        curve: Curves.easeOut,
        child: AnimatedContainer(
          duration: const Duration(milliseconds: 200),
          decoration: BoxDecoration(
            color: bgColor,
            borderRadius: BorderRadius.circular(widget.borderRadius),
          ),
          child: Material(
            type: MaterialType.transparency,
            borderRadius: BorderRadius.circular(widget.borderRadius),
            clipBehavior: Clip.antiAlias,
            child: InkWell(
              onTap: widget.onTap,
              onLongPress: widget.onLongPress,
              child: Padding(
                padding: widget.padding,
                child: widget.child,
              ),
            ),
          ),
        ),
      ),
    );
  }
}
