import 'package:flutter/material.dart';
import '../../../../koorugal/podhu_koorugal/elvan_menmai_nagarvu.dart';

class ElvanSubpagePadding extends InheritedWidget {
  final EdgeInsetsGeometry padding;
  const ElvanSubpagePadding(
      {super.key, required this.padding, required super.child});

  static EdgeInsetsGeometry? of(BuildContext context) {
    return context
        .dependOnInheritedWidgetOfExactType<ElvanSubpagePadding>()
        ?.padding;
  }

  @override
  bool updateShouldNotify(ElvanSubpagePadding oldWidget) =>
      padding != oldWidget.padding;
}

class ElvanDesktopSubpageShell extends StatefulWidget {
  const ElvanDesktopSubpageShell({
    super.key,
    required this.title,
    required this.slivers,
    this.backgroundColor,
    this.hideHeaderOnDesktop = false,
    this.contentPadding,
    this.maxWidth = 680,
  });

  final String title;
  final List<Widget> slivers;
  final Color? backgroundColor;
  final bool hideHeaderOnDesktop;
  final EdgeInsetsGeometry? contentPadding;
  final double maxWidth;

  @override
  State<ElvanDesktopSubpageShell> createState() =>
      _ElvanDesktopSubpageShellState();
}

class _ElvanDesktopSubpageShellState extends State<ElvanDesktopSubpageShell> {
  final ScrollController _scrollController = ScrollController();

  @override
  void dispose() {
    _scrollController.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    final canPop = Navigator.canPop(context);

    bool isInsideSettingsSplitView = false;
    context.visitAncestorElements((element) {
      final typeName = element.widget.runtimeType.toString();
      if (typeName == 'SettingsScreen' || typeName == '_SettingsScreenState') {
        isInsideSettingsSplitView = true;
        return false;
      }
      return true;
    });

    final isWideDesktop = MediaQuery.sizeOf(context).width >= 1100;
    final isSplitView = isInsideSettingsSplitView && isWideDesktop;
    final showBackButton = canPop && !isSplitView;

    return Material(
      color:
          widget.backgroundColor ?? Theme.of(context).scaffoldBackgroundColor,
      child: LayoutBuilder(builder: (context, constraints) {
        final double availableWidth = constraints.maxWidth;
        final double horizontalPadding = isSplitView
            ? 0.0
            : ((availableWidth > widget.maxWidth) ? (availableWidth - widget.maxWidth) / 2 : 0.0);

        return ElvanSmoothScroll(
          controller: _scrollController,
          child: CustomScrollView(
            controller: _scrollController,
            slivers: [
              if (!widget.hideHeaderOnDesktop)
                SliverToBoxAdapter(
                  child: Padding(
                    padding: EdgeInsets.only(
                        top: isSplitView ? 56 : 32,
                        left: isSplitView ? 40 : (showBackButton ? 12 : 60),
                        right: isSplitView ? 0 : 24,
                        bottom: 24),
                    child: Row(
                      children: [
                        if (showBackButton) ...[
                          Container(
                            decoration: BoxDecoration(
                              color: Theme.of(context).colorScheme.surface,
                              shape: BoxShape.circle,
                            ),
                            child: IconButton(
                              icon: const Icon(Icons.chevron_left_rounded),
                              onPressed: () => Navigator.maybePop(context),
                            ),
                          ),
                          const SizedBox(width: 16),
                        ] else if (isSplitView) ...[
                          const SizedBox(width: 20),
                        ],
                        Text(
                          widget.title,
                          style: TextStyle(
                            fontSize: 24,
                            fontWeight: FontWeight.w700,
                            color: Theme.of(context).colorScheme.onSurface,
                          ),
                        ),
                      ],
                    ),
                  ),
                )
              else
                SliverToBoxAdapter(
                  child: SizedBox(height: isSplitView ? 56 : 32),
                ),
              ...widget.slivers.map((sliver) => SliverPadding(
                    padding:
                        EdgeInsets.symmetric(horizontal: horizontalPadding),
                    sliver: SliverPadding(
                      padding: widget.contentPadding ??
                          ElvanSubpagePadding.of(context) ??
                          const EdgeInsets.symmetric(
                              horizontal: 24),
                      sliver: sliver,
                    ),
                  )),
            ],
          ),
        );
      }),
    );
  }
}
