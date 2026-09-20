import 'package:elvan_niril/src/adippadai/mozhiyaakkam/k.dart';
import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:flutter_svg/flutter_svg.dart';

import '../../../../adippadai/mozhiyaakkam/mozhi_vazhanguthi.dart';
import '../../../../adippadai/tharavuru/seyali_murai.dart';
import '../kaippaesi/elvan_kizh_pattai.dart'; // For CustomNavItem
import 'koorugal/koorugal.dart';

const String _sidebarCollapseSvg = '''
<svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.25" stroke-linecap="round" stroke-linejoin="round">
  <path d="M10 6L5 9L10 12" />
  <path d="M14 6H19" />
  <path d="M14 12H19" />
  <path d="M5 18H19" />
</svg>
''';

class ElvanDesktopSidebar extends ConsumerWidget {
  final bool isCollapsed;
  final VoidCallback onToggleCollapse;
  final int currentIndex;
  final ValueChanged<int> onTabSelected;
  final VoidCallback onSettingsPressed;
  final List<CustomNavItem> navItems;
  final AppMode appMode;

  const ElvanDesktopSidebar({
    super.key,
    required this.isCollapsed,
    required this.onToggleCollapse,
    required this.currentIndex,
    required this.onTabSelected,
    required this.onSettingsPressed,
    required this.navItems,
    required this.appMode,
  });

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final isDark = Theme.of(context).brightness == Brightness.dark;

    return ClipRect(
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          // Header (App Name + Toggle)
          _buildHeaderCrossFade(context, ref, isDark),

          // Nav Items List and Reports Section
          Expanded(
            child: ScrollConfiguration(
              behavior: ScrollConfiguration.of(context).copyWith(scrollbars: false),
              child: SingleChildScrollView(
                padding: const EdgeInsets.symmetric(vertical: 8),
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.stretch,
                  children: [
                    ...List.generate(navItems.length, (index) {
                      final item = navItems[index];
                      final isSelected = index == currentIndex;

                      return Padding(
                        padding: const EdgeInsets.symmetric(horizontal: 12),
                        child: Padding(
                          padding: EdgeInsets.only(bottom: isCollapsed ? 0 : 10),
                          child: KaniniNavKooru(
                            item: item,
                            isSelected: isSelected,
                            isDark: isDark,
                            isCollapsed: isCollapsed,
                            onTap: () => onTabSelected(index),
                          ),
                        ),
                      );
                    }),

                  ],
                ),
              ),
            ),
          ),

          const SizedBox(height: 8),

          // Settings / Profile Zone
          Padding(
            padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 16),
            child: AnimatedCrossFade(
              duration: const Duration(milliseconds: 300),
              firstCurve: const Cubic(0.2, 0.0, 0.0, 1.0),
              secondCurve: const Cubic(0.2, 0.0, 0.0, 1.0),
              alignment: Alignment.centerLeft,
              crossFadeState: isCollapsed
                  ? CrossFadeState.showSecond
                  : CrossFadeState.showFirst,
              firstChild: SizedBox(
                height: 52,
                child: OverflowBox(
                  minWidth: 236, // 260 - 24 padding
                  maxWidth: 236,
                  maxHeight: 52,
                  alignment: Alignment.centerLeft,
                  child: _buildExpandedProfile(context, isDark, ref),
                ),
              ),
              secondChild: SizedBox(
                width: 56,
                height: 48,
                child: _buildCollapsedProfile(context, isDark),
              ),
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildHeaderCrossFade(
      BuildContext context, WidgetRef ref, bool isDark) {
    return AnimatedCrossFade(
      duration: const Duration(milliseconds: 300),
      firstCurve: const Cubic(0.2, 0.0, 0.0, 1.0),
      secondCurve: const Cubic(0.2, 0.0, 0.0, 1.0),
      alignment: Alignment.centerLeft,
      crossFadeState:
          isCollapsed ? CrossFadeState.showSecond : CrossFadeState.showFirst,
      firstChild: SizedBox(
        height: 80,
        child: OverflowBox(
          minWidth: 260,
          maxWidth: 260,
          maxHeight: 80,
          alignment: Alignment.centerLeft,
          child: Container(
            width: 260,
            height: 80,
            padding:
                const EdgeInsets.only(top: 32, bottom: 8, left: 32, right: 24),
            alignment: Alignment.topLeft,
            child: Row(
              mainAxisAlignment: MainAxisAlignment.spaceBetween,
              children: [
                Text(
                  K.niril.tr(context, ref),
                  style: TextStyle(
                    fontSize: 20,
                    fontWeight: FontWeight.w800,
                    letterSpacing: -0.4,
                    color: Theme.of(context).colorScheme.onSurface,
                    height: 1.0,
                  ),
                ),
                IconButton(
                  icon: SvgPicture.string(
                    _sidebarCollapseSvg,
                    width: 20,
                    height: 20,
                    colorFilter: ColorFilter.mode(
                        Theme.of(context).colorScheme.onSurface,
                        BlendMode.srcIn),
                  ),
                  onPressed: onToggleCollapse,
                  splashRadius: 20,
                  hoverColor: isDark
                      ? Colors.white.withValues(alpha: 0.08)
                      : Colors.black.withValues(alpha: 0.04),
                ),
              ],
            ),
          ),
        ),
      ),
      secondChild: Container(
        width: 80,
        height: 80,
        padding: const EdgeInsets.only(top: 32, bottom: 8),
        alignment: Alignment.topCenter,
        child: IconButton(
          icon: Transform.scale(
            scaleX: -1,
            child: SvgPicture.string(
              _sidebarCollapseSvg,
              width: 20,
              height: 20,
              colorFilter: ColorFilter.mode(
                  Theme.of(context).colorScheme.onSurface, BlendMode.srcIn),
            ),
          ),
          onPressed: onToggleCollapse,
          splashRadius: 20,
          hoverColor: isDark
              ? Colors.white.withValues(alpha: 0.08)
              : Colors.black.withValues(alpha: 0.04),
        ),
      ),
    );
  }

  Widget _buildCollapsedProfile(BuildContext context, bool isDark) {
    return KaniniCollapsedProfileKooru(
        isDark: isDark, onSettingsPressed: onSettingsPressed);
  }

  Widget _buildExpandedProfile(
      BuildContext context, bool isDark, WidgetRef ref) {
    return KaniniExpandedProfileKooru(
      isDark: isDark,
      appMode: appMode,
      onSettingsPressed: onSettingsPressed,
      onModeSwitched: () => onTabSelected(0),
    );
  }
}
