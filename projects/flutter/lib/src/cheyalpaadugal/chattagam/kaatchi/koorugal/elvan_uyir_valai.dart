import 'package:flutter/material.dart';

/// A wrapper that dynamically switches between a vertical SliverList on Mobile
/// and a multi-column SliverGrid on Desktop.
/// It uses a standard 2x2 grid approach by default for desktop.
class ElvanResponsiveGrid extends StatelessWidget {
  const ElvanResponsiveGrid({
    super.key,
    required this.itemCount,
    required this.itemBuilder,
    this.desktopCrossAxisCount = 2,
    this.mainAxisSpacing = 16.0,
    this.crossAxisSpacing = 16.0,
    this.childAspectRatio = 1.0,
    this.breakpoint = 800.0,
    this.mobileItemHeight = 160.0,
  });

  final int itemCount;
  final NullableIndexedWidgetBuilder itemBuilder;
  final int desktopCrossAxisCount;
  final double mainAxisSpacing;
  final double crossAxisSpacing;
  final double childAspectRatio;
  final double breakpoint;
  final double mobileItemHeight;

  @override
  Widget build(BuildContext context) {
    return SliverLayoutBuilder(
      builder: (context, constraints) {
        if (constraints.crossAxisExtent >= breakpoint) {
          // Desktop Grid Layout
          return SliverGrid(
            gridDelegate: SliverGridDelegateWithFixedCrossAxisCount(
              crossAxisCount: desktopCrossAxisCount,
              mainAxisSpacing: mainAxisSpacing,
              crossAxisSpacing: crossAxisSpacing,
              childAspectRatio: childAspectRatio,
            ),
            delegate: SliverChildBuilderDelegate(
              itemBuilder,
              childCount: itemCount,
            ),
          );
        } else {
          // Mobile List Layout
          return SliverList(
            delegate: SliverChildBuilderDelegate(
              (context, index) {
                return Padding(
                  padding: EdgeInsets.only(bottom: mainAxisSpacing),
                  child: itemBuilder(context, index),
                );
              },
              childCount: itemCount,
            ),
          );
        }
      },
    );
  }
}
