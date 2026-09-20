import 'package:flutter/material.dart';
import 'package:elvan_niril/src/adippadai/mozhiyaakkam/k.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../../../../adippadai/mozhiyaakkam/mozhi_vazhanguthi.dart';
import '../../../../adippadai/thoatra_vazhanguthi.dart';
import 'elvan_amaippu_kattupadugal.dart';

class ElvanThemeSelector extends ConsumerWidget {
  const ElvanThemeSelector({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final currentTheme = ref.watch(themeModeProvider);
    final isDark = Theme.of(context).brightness == Brightness.dark;

    void handleThemeChange(ThemeMode newMode) {
      ref.read(themeModeProvider.notifier).setThemeMode(newMode);
    }

    return Column(
      children: [
        Padding(
          padding: const EdgeInsets.symmetric(vertical: 24, horizontal: 16),
          child: Row(
            mainAxisAlignment: MainAxisAlignment.spaceEvenly,
            children: [
              // Light Mode Option
              _ThemeOptionBox(
                title: K.oliNilai.tr(context, ref),
                isSelected: currentTheme == ThemeMode.light,
                isDarkModeDesign: false,
                onTap: () => handleThemeChange(ThemeMode.light),
              ),

              // Dark Mode Option
              _ThemeOptionBox(
                title: K.irulNilai.tr(context, ref),
                isSelected: currentTheme == ThemeMode.dark,
                isDarkModeDesign: true,
                onTap: () => handleThemeChange(ThemeMode.dark),
              ),
            ],
          ),
        ),

        // Auto Mode Switch Row
        Divider(
          height: 1,
          thickness: 1,
          color: isDark
              ? Colors.white.withValues(alpha: 0.04)
              : Colors.black.withValues(alpha: 0.04),
          indent: 16,
        ),
        GestureDetector(
          behavior: HitTestBehavior.opaque,
          onTap: () {
            if (currentTheme == ThemeMode.system) {
              handleThemeChange(isDark ? ThemeMode.dark : ThemeMode.light);
            } else {
              handleThemeChange(ThemeMode.system);
            }
          },
          child: Padding(
            padding: const EdgeInsets.symmetric(horizontal: 20, vertical: 12),
            child: Row(
              children: [
                Expanded(
                  child: Text(
                    K.thaaniyangiAmaippu.tr(context, ref),
                    style: const TextStyle(
                      fontSize: 15,
                      fontWeight: FontWeight.w600,
                      height: 1.3,
                    ),
                  ),
                ),
                ElvanSettingsSwitch(
                  value: currentTheme == ThemeMode.system,
                  onChanged: (val) {
                    if (val) {
                      handleThemeChange(ThemeMode.system);
                    } else {
                      handleThemeChange(
                          isDark ? ThemeMode.dark : ThemeMode.light);
                    }
                  },
                ),
              ],
            ),
          ),
        ),
      ],
    );
  }
}

class _ThemeOptionBox extends StatelessWidget {
  final String title;
  final bool isSelected;
  final bool isDarkModeDesign;
  final VoidCallback onTap;

  const _ThemeOptionBox({
    required this.title,
    required this.isSelected,
    required this.isDarkModeDesign,
    required this.onTap,
  });

  @override
  Widget build(BuildContext context) {
    final scale = isSelected ? 1.02 : 1.0;
    final isDark = Theme.of(context).brightness == Brightness.dark;

    // Colors matching React design
    final boxBgColor =
        isDarkModeDesign ? const Color(0xFF1E1E1E) : const Color(0xFFF5F5F5);
    final borderColor = isSelected
        ? (isDarkModeDesign ? const Color(0xFF888888) : const Color(0xFF555555))
        : (isDark
            ? Colors.white.withValues(alpha: 0.08)
            : Colors.black.withValues(alpha: 0.08));

    final bar1Color = isDarkModeDesign
        ? Colors.white.withValues(alpha: 0.8)
        : Colors.black.withValues(alpha: 0.7);
    final bar2Color =
        isDarkModeDesign ? const Color(0xFF444444) : const Color(0xCFCFCFCF);

    return GestureDetector(
      onTap: onTap,
      behavior: HitTestBehavior.opaque,
      child: Column(
        children: [
          AnimatedContainer(
            duration: const Duration(milliseconds: 200),
            transform: Matrix4.identity()..scale(scale, scale),
            transformAlignment: Alignment.center,
            width: 110,
            height: 85,
            padding: const EdgeInsets.all(16),
            decoration: BoxDecoration(
              color: boxBgColor,
              borderRadius: BorderRadius.circular(24),
              border: Border.all(color: borderColor, width: 1),
            ),
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Container(
                  width: 32,
                  height: 8,
                  decoration: BoxDecoration(
                    color: bar1Color,
                    borderRadius: BorderRadius.circular(4),
                  ),
                ),
                const SizedBox(height: 10),
                Container(
                  width: double.infinity,
                  height: 6,
                  decoration: BoxDecoration(
                    color: bar2Color,
                    borderRadius: BorderRadius.circular(4),
                  ),
                ),
                const SizedBox(height: 10),
                Container(
                  width: 48,
                  height: 6,
                  decoration: BoxDecoration(
                    color: bar2Color,
                    borderRadius: BorderRadius.circular(4),
                  ),
                ),
              ],
            ),
          ),
          const SizedBox(height: 16),
          Text(
            title,
            style: TextStyle(
              fontSize: 14,
              fontWeight: isSelected ? FontWeight.w600 : FontWeight.w500,
              color: isSelected
                  ? Theme.of(context).colorScheme.onSurface
                  : Theme.of(context)
                      .colorScheme
                      .onSurface
                      .withValues(alpha: 0.5),
            ),
          ),
          const SizedBox(height: 8),
          Icon(
            isSelected
                ? Icons.check_circle_rounded
                : Icons.radio_button_unchecked_rounded,
            size: 24,
            color: isSelected
                ? (isDarkModeDesign ? Colors.white : Colors.black)
                : const Color(0xFF888888),
          ),
        ],
      ),
    );
  }
}
