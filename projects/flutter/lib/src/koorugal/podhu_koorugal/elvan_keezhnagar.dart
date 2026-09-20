import 'package:flutter/material.dart';

/// Adds artificial bottom padding matching the keyboard height.
/// Useful in Scaffolds where `resizeToAvoidBottomInset` is false.
class ElvanKeezhNagar extends StatelessWidget {
  const ElvanKeezhNagar({super.key});

  @override
  Widget build(BuildContext context) {
    return SizedBox(height: MediaQuery.viewInsetsOf(context).bottom);
  }
}

/// Sliver version of [ElvanKeezhNagar] for use inside CustomScrollViews.
class ElvanSliverKeezhNagar extends StatelessWidget {
  const ElvanSliverKeezhNagar({super.key});

  @override
  Widget build(BuildContext context) {
    return SliverToBoxAdapter(
      child: const ElvanKeezhNagar(),
    );
  }
}
