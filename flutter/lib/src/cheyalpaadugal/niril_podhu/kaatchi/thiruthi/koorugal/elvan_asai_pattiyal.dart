import 'package:flutter/material.dart';

/// A bug-free animated list replacement.
/// Wraps a standard [Column] in an [AnimatedSize] to provide smooth
/// expansion/collapse animations without the ghost padding layout bugs
/// associated with [AnimatedList] and shrinkWrap.
class ElvanAsaiPattiyal extends StatelessWidget {
  const ElvanAsaiPattiyal({
    super.key,
    required this.itemCount,
    required this.itemBuilder,
    this.duration = const Duration(milliseconds: 300),
    this.curve = Curves.easeOutQuart,
  });

  /// The total number of items to build.
  final int itemCount;

  /// The builder function for each item.
  final Widget Function(BuildContext context, int index) itemBuilder;

  /// The duration of the size animation when an item is added or removed.
  final Duration duration;

  /// The animation curve.
  final Curve curve;

  @override
  Widget build(BuildContext context) {
    return AnimatedSize(
      duration: duration,
      curve: curve,
      alignment: Alignment.topCenter,
      child: SizedBox(
        width: double.infinity,
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.stretch,
          children: [
            for (int i = 0; i < itemCount; i++)
              itemBuilder(context, i),
          ],
        ),
      ),
    );
  }
}
