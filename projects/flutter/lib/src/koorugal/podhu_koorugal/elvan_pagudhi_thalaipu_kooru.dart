import 'package:flutter/material.dart';

/// பகுதித் தலைப்பு கூறு — Numbered section header matching React's
/// ElvanEditorLayout section badges.
///
/// Displays a numbered circle badge (24×24, primary bg) followed by
/// the section title text (17.6px, fontWeight 600).
class ElvanPagudhiThalaipu extends StatelessWidget {
  const ElvanPagudhiThalaipu({
    super.key,
    required this.en,
    required this.thalaipu,
  });

  /// Section number displayed in the circle badge.
  final int en;

  /// Section title text.
  final String thalaipu;

  @override
  Widget build(BuildContext context) {
    final cs = Theme.of(context).colorScheme;
    final isDark = Theme.of(context).brightness == Brightness.dark;

    return Padding(
      padding: const EdgeInsets.only(left: 16, bottom: 8),
      child: Row(
        children: [
          // ── Numbered Circle Badge ──
          Container(
            width: 24,
            height: 24,
            decoration: BoxDecoration(
              color: isDark ? Colors.white : Colors.black,
              shape: BoxShape.circle,
            ),
            alignment: Alignment.center,
            child: Text(
              '$en',
              style: TextStyle(
                color: isDark ? Colors.black : Colors.white,
                fontSize: 12,
                fontWeight: FontWeight.bold,
                height: 1,
              ),
            ),
          ),
          const SizedBox(width: 12),
          // ── Section Title ──
          Flexible(
            child: Text(
              thalaipu,
              overflow: TextOverflow.ellipsis,
              style: TextStyle(
                fontSize: 17.6, // 1.1rem
                fontWeight: FontWeight.w600,
                color: cs.onSurface,
              ),
            ),
          ),
        ],
      ),
    );
  }
}
