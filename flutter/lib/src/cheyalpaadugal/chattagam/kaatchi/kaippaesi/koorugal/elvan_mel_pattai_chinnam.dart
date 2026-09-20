import 'package:flutter/material.dart';

/// A highly optimized custom icon button for the Elvan Top Bar.
/// It uses InkResponse directly to draw a perfect circular ripple
/// without any of the forced padding or stadium shape restrictions of Material 3's IconButton.
class ElvanTopBarIcon extends StatelessWidget {
  const ElvanTopBarIcon({
    super.key,
    required this.icon,
    required this.onTap,
  });

  final IconData icon;
  final VoidCallback onTap;

  @override
  Widget build(BuildContext context) {
    return SizedBox(
      width: 26,
      height: 26, // Exact tap target size to prevent layout overlap
      child: OverflowBox(
        maxWidth: 40,
        maxHeight: 40,
        child: SizedBox(
          width: 40,
          height:
              40, // Reduced from 54 to 40 to mathematically prevent overlapping
          child: Material(
            type: MaterialType.transparency,
            shape: const CircleBorder(),
            clipBehavior: Clip
                .hardEdge, // Strictly confine highlight and splash to this 40px circle
            child: InkResponse(
              onTap: onTap,
              radius: 20, // Expanding circle radius (40px diameter)
              highlightShape: BoxShape.circle,
              splashFactory: NoSplash
                  .splashFactory, // Instantly fills the circle, no growing!
              splashColor: Colors.transparent,
              highlightColor: Theme.of(context)
                  .colorScheme
                  .onSurface
                  .withValues(alpha: 0.12), // Instant full circle flash
              child: Icon(icon, size: 22),
            ),
          ),
        ),
      ),
    );
  }
}
