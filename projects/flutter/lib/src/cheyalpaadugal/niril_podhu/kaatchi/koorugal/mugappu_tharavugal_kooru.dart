import 'package:elvan_niril/src/adippadai/tharavuru/uruvugal.dart';
import 'package:flutter/cupertino.dart';
import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:intl/intl.dart';

import 'package:elvan_niril/src/adippadai/mozhiyaakkam/k.dart';
import '../../../../adippadai/mozhiyaakkam/mozhi_vazhanguthi.dart';
import '../../../../adippadai/oru_mozhi/oru_mozhi_vazhanguthigal.dart';

import '../../../../koorugal/podhu_koorugal/elvan_pothu_attai.dart';

// ── Stats Card (Bento Grid Item) ────────────────────────────────────────────

/// A flat, monochrome stats card matching React's MugappuLayout design.
/// Used in the bento grid on the home screen.
class ElvanStatsCard extends StatelessWidget {
  const ElvanStatsCard({
    super.key,
    required this.icon,
    required this.label,
    required this.value,
    this.isLoading = false,
    this.onTap,
  });

  final IconData icon;
  final String label;
  final String value;
  final bool isLoading;
  final VoidCallback? onTap;

  @override
  Widget build(BuildContext context) {
    final isDark = Theme.of(context).brightness == Brightness.dark;

    return ElvanPothuAttai(
      padding: EdgeInsets.zero,
      onTap: onTap,
      child: LayoutBuilder(
            builder: (context, constraints) {
              final isNarrow = constraints.maxWidth < 160;
              final cardPadding = isNarrow ? 16.0 : 20.0;

              final iconSize = isNarrow ? 40.0 : 48.0;

              final iconBox = Container(
                width: iconSize,
                height: iconSize,
                decoration: BoxDecoration(
                  color: isDark
                      ? Colors.white.withValues(alpha: 0.05)
                      : const Color(0xFFF5F5F5),
                  borderRadius: BorderRadius.circular(isNarrow ? 12 : 16),
                ),
                child: Icon(icon, size: isNarrow ? 20 : 24,
                    color: isDark ? Colors.white : Colors.black),
              );

              final textColumn = Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                mainAxisAlignment: MainAxisAlignment.center,
                mainAxisSize: MainAxisSize.min,
                children: [
                  Text(
                    label,
                    style: TextStyle(
                      fontSize: isNarrow ? 12 : 14,
                      fontWeight: FontWeight.w600,
                      color: isDark ? Colors.white54 : Colors.black54,
                    ),
                  ),
                  const SizedBox(height: 4),
                  if (isLoading)
                    Container(
                      width: 80,
                      height: 20,
                      decoration: BoxDecoration(
                        color: isDark
                            ? Colors.white.withValues(alpha: 0.08)
                            : Colors.black.withValues(alpha: 0.06),
                        borderRadius: BorderRadius.circular(6),
                      ),
                    )
                  else
                    Text(
                      value,
                      style: TextStyle(
                        fontSize: isNarrow
                            ? _adaptiveFontSize(value) * 0.8
                            : _adaptiveFontSize(value),
                        fontWeight: FontWeight.w800,
                        color: isDark ? Colors.white : Colors.black,
                        height: 1.2,
                      ),
                    ),
                ],
              );

              // Column layout on narrow mobile, Row on wider screens
              if (isNarrow) {
                return SizedBox(
                  height: 128,
                  child: Padding(
                    padding: EdgeInsets.all(cardPadding),
                    child: Column(
                      crossAxisAlignment: CrossAxisAlignment.start,
                      mainAxisAlignment: MainAxisAlignment.spaceBetween,
                      children: [
                        iconBox,
                        FittedBox(
                          fit: BoxFit.scaleDown,
                          alignment: Alignment.centerLeft,
                          child: textColumn,
                        ),
                      ],
                    ),
                  ),
                );
              }

              return SizedBox(
                height: 96,
                child: Padding(
                  padding: EdgeInsets.all(cardPadding),
                  child: Row(
                    crossAxisAlignment: CrossAxisAlignment.center,
                    children: [
                      iconBox,
                      const SizedBox(width: 16),
                      Expanded(
                        child: Container(
                          height: iconSize, // Constrain text to icon's vertical boundary
                          alignment: Alignment.centerLeft,
                          child: FittedBox(
                            fit: BoxFit.scaleDown,
                            alignment: Alignment.centerLeft,
                            child: textColumn,
                          ),
                        ),
                      ),
                    ],
                  ),
                ),
              );
            },
          ),
    );
  }

  /// Adaptive font size based on value length (matches React logic)
  double _adaptiveFontSize(String val) {
    if (val.length > 14) return 15;
    if (val.length > 11) return 18;
    return 24;
  }
}

// ── Recent Activity Header ──────────────────────────────────────────────────

class RecentActivityHeader extends StatelessWidget {
  const RecentActivityHeader({
    super.key,
    required this.onSeeAll,
  });

  final VoidCallback onSeeAll;

  @override
  Widget build(BuildContext context) {
    final isDark = Theme.of(context).brightness == Brightness.dark;

    return Padding(
      padding: const EdgeInsets.only(left: 24, right: 24, top: 8, bottom: 8),
      child: Row(
        mainAxisAlignment: MainAxisAlignment.spaceBetween,
        children: [
          Consumer(
            builder: (context, ref, _) => Text(
              K.arugilSeyalgal.tr(context, ref),
              style: TextStyle(
                fontSize: 16,
                fontWeight: FontWeight.w700,
                color: isDark ? Colors.white : Colors.black,
              ),
            ),
          ),
          Consumer(
            builder: (context, ref, _) => GestureDetector(
              onTap: onSeeAll,
              child: Row(
                mainAxisSize: MainAxisSize.min,
                children: [
                  Text(
                    K.anaiththaiyumPaarPtn.tr(context, ref),
                    style: TextStyle(
                      fontSize: 14,
                      fontWeight: FontWeight.w600,
                      color: isDark ? Colors.white54 : Colors.black54,
                    ),
                  ),
                  const SizedBox(width: 4),
                  Icon(
                    CupertinoIcons.chevron_right,
                    size: 16,
                    color: isDark ? Colors.white54 : Colors.black54,
                  ),
                ],
              ),
            ),
          ),
        ],
      ),
    );
  }
}


// ── Empty State ─────────────────────────────────────────────────────────────

class MugappuEmptyState extends StatelessWidget {
  const MugappuEmptyState({super.key});

  @override
  Widget build(BuildContext context) {
    final isDark = Theme.of(context).brightness == Brightness.dark;

    return Padding(
      padding: const EdgeInsets.symmetric(vertical: 80),
      child: Center(
        child: Column(
          mainAxisSize: MainAxisSize.min,
          children: [
            Icon(
              CupertinoIcons.doc_text,
              size: 48,
              color: isDark
                  ? Colors.white24
                  : Colors.black26,
            ),
            const SizedBox(height: 16),
            Consumer(
              builder: (context, ref, _) => Text(
                K.pattiyalgalIllai.tr(context, ref),
                style: TextStyle(
                  fontSize: 15,
                  color: isDark ? Colors.white38 : Colors.black38,
                ),
              ),
            ),
          ],
        ),
      ),
    );
  }
}

// ── Loading Skeleton ────────────────────────────────────────────────────────

class MugappuLoadingSkeleton extends StatelessWidget {
  const MugappuLoadingSkeleton({super.key});

  @override
  Widget build(BuildContext context) {
    final isDark = Theme.of(context).brightness == Brightness.dark;

    return Column(
      children: List.generate(4, (i) {
        return Padding(
          padding: const EdgeInsets.only(bottom: 16),
          child: Container(
            padding: const EdgeInsets.all(16),
            decoration: BoxDecoration(
               color: isDark
                  ? const Color(0xFF111111)
                  : Colors.white,
              borderRadius: BorderRadius.circular(24),
            ),
            child: Row(
              children: [
                // Circle skeleton
                Container(
                  width: 28,
                  height: 28,
                  decoration: BoxDecoration(
                    color: isDark
                        ? Colors.white.withValues(alpha: 0.08)
                        : Colors.black.withValues(alpha: 0.06),
                    shape: BoxShape.circle,
                  ),
                ),
                const SizedBox(width: 12),
                // Text skeleton
                Expanded(
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      Container(
                        width: 180,
                        height: 16,
                        decoration: BoxDecoration(
                          color: isDark
                              ? Colors.white.withValues(alpha: 0.08)
                              : Colors.black.withValues(alpha: 0.06),
                          borderRadius: BorderRadius.circular(4),
                        ),
                      ),
                      const SizedBox(height: 8),
                      Container(
                        width: 100,
                        height: 12,
                        decoration: BoxDecoration(
                          color: isDark
                              ? Colors.white.withValues(alpha: 0.05)
                              : Colors.black.withValues(alpha: 0.04),
                          borderRadius: BorderRadius.circular(4),
                        ),
                      ),
                    ],
                  ),
                ),
                const SizedBox(width: 12),
                // Amount skeleton
                Container(
                  width: 60,
                  height: 18,
                  decoration: BoxDecoration(
                    color: isDark
                        ? Colors.white.withValues(alpha: 0.08)
                        : Colors.black.withValues(alpha: 0.06),
                    borderRadius: BorderRadius.circular(4),
                  ),
                ),
              ],
            ),
          ),
        );
      }),
    );
  }
}
