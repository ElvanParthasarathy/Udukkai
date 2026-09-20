import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../../../../adippadai/tharavuru/seyali_murai.dart';
import '../../../../adippadai/nilaimai/seyali_nilaimai.dart';
import '../kaippaesi/elvan_kizh_pattai.dart'; // For CustomNavItem
import 'elvan_kanini_pakkapattai.dart';
import '../../../../koorugal/podhu_koorugal/elvan_menmai_nagarvu.dart';
import '../../../../adippadai/viruppangal_paniyagam.dart';

class ElvanDesktopShell extends ConsumerStatefulWidget {
  final int currentIndex;
  final ValueChanged<int> onTabSelected;
  final VoidCallback onSettingsPressed;
  final List<CustomNavItem> navItems;
  final List<Widget> slivers; // The pages (SliverOffstage)
  final Widget? customContent;
  final String? title;
  final Widget? toolbar; // Desktop toolbar (search, edit, add)

  const ElvanDesktopShell({
    super.key,
    required this.currentIndex,
    required this.onTabSelected,
    required this.onSettingsPressed,
    required this.navItems,
    required this.slivers,
    this.customContent,
    this.title,
    this.toolbar,
  });

  @override
  ConsumerState<ElvanDesktopShell> createState() => _ElvanDesktopShellState();
}

final GlobalKey<NavigatorState> desktopNavigatorKey = GlobalKey<NavigatorState>();

class _ElvanDesktopShellState extends ConsumerState<ElvanDesktopShell> {
  bool isCollapsed = false;
  final double _sidebarOpacity = 1.0;
  final bool _isAnimatingSidebar = false;
  final ScrollController _scrollController = ScrollController();

  @override
  void initState() {
    super.initState();
    isCollapsed = ref.read(preferencesServiceProvider).getIsSidebarCollapsed();
  }

  @override
  void dispose() {
    _scrollController.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    final mode = ref.watch(appModeProvider);
    final isDark = Theme.of(context).brightness == Brightness.dark;

    return Scaffold(
      backgroundColor:
          isDark ? const Color(0xFF111111) : const Color(0xFFFFFFFF),
      body: Row(
        children: [
          // Sidebar
          AnimatedContainer(
            duration: const Duration(milliseconds: 400),
            curve: Curves.easeInOutCubicEmphasized,
            width: isCollapsed ? 80 : 260,
            decoration: BoxDecoration(
              color: isDark ? const Color(0xFF111111) : const Color(0xFFFFFFFF),
              border: Border(
                right: BorderSide(
                  color: isDark ? Colors.transparent : const Color(0xFFF3F4F6),
                  width: 1,
                ),
              ),
            ),
            child: ElvanDesktopSidebar(
              isCollapsed: isCollapsed,
              onToggleCollapse: () {
                setState(() => isCollapsed = !isCollapsed);
                ref
                    .read(preferencesServiceProvider)
                    .setIsSidebarCollapsed(isCollapsed);
              },
              currentIndex: widget.currentIndex,
              onTabSelected: (index) {
                desktopNavigatorKey.currentState?.popUntil((route) => route.isFirst);
                if (_scrollController.hasClients) {
                  if (widget.currentIndex == index) {
                    _scrollController.animateTo(0,
                        duration: const Duration(milliseconds: 300),
                        curve: Curves.easeOut);
                  } else {
                    _scrollController.jumpTo(0);
                  }
                }
                widget.onTabSelected(index);
              },
              onSettingsPressed: () {
                desktopNavigatorKey.currentState?.popUntil((route) => route.isFirst);
                widget.onSettingsPressed();
              },
              navItems: widget.navItems,
              appMode: mode ?? AppMode.silk,
            ),
          ),

          // Main Content Area
          Expanded(
            child: Navigator(
              key: desktopNavigatorKey,
              onPopPage: (route, result) => route.didPop(result),
              pages: [
                MaterialPage(
                  key: const ValueKey('desktop_root'),
                  child: Container(
                    color: Theme.of(context).scaffoldBackgroundColor,
                    child: widget.customContent ?? ElvanSmoothScroll(
                            controller: _scrollController,
                            child: CustomScrollView(
                              controller: _scrollController,
                              slivers: [
                                if (widget.title != null)
                                  SliverToBoxAdapter(
                                    child: Padding(
                                      padding: const EdgeInsets.only(
                                          top: 32, left: 60, bottom: 24),
                                      child: Text(
                                        widget.title!,
                                        style: TextStyle(
                                          fontSize: 24,
                                          fontWeight: FontWeight.w700,
                                          color:
                                              Theme.of(context).colorScheme.onSurface,
                                        ),
                                      ),
                                    ),
                                  ),
                                if (widget.toolbar != null)
                                  SliverPadding(
                                    padding:
                                        const EdgeInsets.symmetric(horizontal: 24),
                                    sliver: SliverToBoxAdapter(
                                      child: widget.toolbar!,
                                    ),
                                  ),
                                ...widget.slivers.map((sliver) => SliverPadding(
                                      padding:
                                          const EdgeInsets.symmetric(horizontal: 24),
                                      sliver: sliver,
                                    )),
                              ],
                            ),
                          ),
                  ),
                ),
              ],
            ),
          ),
        ],
      ),
    );
  }
}
