import 'package:flutter/cupertino.dart';
import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:flutter_riverpod/legacy.dart';

import '../../../chattagam/kaatchi/koorugal/elvan_uyir_valai.dart';

/// A generic list component for Pattu data that handles AsyncValue states,
/// filtering, grouping, empty states, and selection toggle logic.
class ElvanPattuTharavuPattiyal<T> extends ConsumerWidget {
  const ElvanPattuTharavuPattiyal({
    super.key,
    required this.dataAsync,
    required this.searchQuery,
    required this.onFilter,
    required this.emptyIcon,
    required this.emptyTitle,
    required this.emptySubtitle,
    required this.itemId,
    required this.selectionModeProvider,
    required this.selectedIdsProvider,
    required this.onItemTap,
    required this.cardBuilder,
    this.groupBy,
    this.groupHeaderBuilder,
    this.desktopCrossAxisCount = 2,
    this.childAspectRatio = 3.0,
    this.mobileItemHeight = 96,
  });

  final AsyncValue<List<T>> dataAsync;
  final String searchQuery;
  final bool Function(T item, String query) onFilter;
  
  final IconData emptyIcon;
  final String emptyTitle;
  final String emptySubtitle;
  
  final int Function(T item) itemId;
  final StateProvider<bool> selectionModeProvider;
  final StateProvider<Set<int>> selectedIdsProvider;
  
  final void Function(BuildContext context, T item) onItemTap;
  final Widget Function(
    BuildContext context, 
    WidgetRef ref,
    T item, 
    int index, 
    bool isSelecting, 
    bool isSelected, 
    VoidCallback onTap, 
    VoidCallback onLongPress,
  ) cardBuilder;

  /// Optional: Function to group items. If null, items are displayed in a flat list.
  final int? Function(T item)? groupBy;
  
  /// Optional: Builder for the section header if `groupBy` is used.
  final Widget Function(BuildContext context, int? groupId)? groupHeaderBuilder;

  final int desktopCrossAxisCount;
  final double childAspectRatio;
  final double mobileItemHeight;

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final isDark = Theme.of(context).brightness == Brightness.dark;

    return dataAsync.when(
      loading: () => const SliverFillRemaining(
        child: Center(child: CupertinoActivityIndicator()),
      ),
      error: (e, _) => SliverFillRemaining(
        child: Center(child: Text('Error: $e')),
      ),
      data: (items) {
        // Filter
        final filtered = searchQuery.isEmpty
            ? items
            : items.where((item) => onFilter(item, searchQuery)).toList();

        // Empty state
        if (filtered.isEmpty) {
          return SliverFillRemaining(
            child: Center(
              child: Column(
                mainAxisSize: MainAxisSize.min,
                children: [
                  Icon(
                    emptyIcon,
                    size: 48,
                    color: isDark ? Colors.white24 : Colors.black26,
                  ),
                  const SizedBox(height: 12),
                  Text(
                    emptyTitle,
                    style: TextStyle(
                      fontSize: 16,
                      fontWeight: FontWeight.w500,
                      color: isDark ? Colors.white38 : Colors.black38,
                    ),
                  ),
                  const SizedBox(height: 4),
                  Text(
                    emptySubtitle,
                    style: TextStyle(
                      fontSize: 13,
                      color: isDark ? Colors.white24 : Colors.black26,
                    ),
                  ),
                ],
              ),
            ),
          );
        }

        // Grouping
        final slivers = <Widget>[];

        if (groupBy != null && groupHeaderBuilder != null) {
          final grouped = <int?, List<T>>{};
          for (final item in filtered) {
            grouped.putIfAbsent(groupBy!(item), () => []).add(item);
          }

          for (final entry in grouped.entries) {
            final groupId = entry.key;
            final groupItems = entry.value;

            if (grouped.length > 1) {
              slivers.add(groupHeaderBuilder!(context, groupId));
            }

            slivers.add(_buildGrid(groupItems));
          }
        } else {
          // Flat list
          slivers.add(_buildGrid(filtered));
        }

        return SliverPadding(
          padding: const EdgeInsets.only(left: 16, right: 16, bottom: 120),
          sliver: SliverMainAxisGroup(slivers: slivers),
        );
      },
    );
  }

  Widget _buildGrid(List<T> items) {
    return ElvanResponsiveGrid(
      itemCount: items.length,
      desktopCrossAxisCount: desktopCrossAxisCount,
      childAspectRatio: childAspectRatio,
      mobileItemHeight: mobileItemHeight,
      itemBuilder: (context, index) {
        final item = items[index];
        final id = itemId(item);

        return Consumer(
          builder: (context, ref, _) {
            final isSelecting = ref.watch(selectionModeProvider);
            final isSelected = ref.watch(
              selectedIdsProvider.select((ids) => ids.contains(id)),
            );

            return cardBuilder(
              context,
              ref,
              item,
              index,
              isSelecting,
              isSelected,
              () {
                if (isSelecting) {
                  _toggleSelection(ref, id);
                } else {
                  onItemTap(context, item);
                }
              },
              () {
                if (!isSelecting) {
                  ref.read(selectionModeProvider.notifier).state = true;
                  ref.read(selectedIdsProvider.notifier).state = {id};
                }
              },
            );
          },
        );
      },
    );
  }

  void _toggleSelection(WidgetRef ref, int id) {
    final current = ref.read(selectedIdsProvider);
    final updated = Set<int>.from(current);
    if (updated.contains(id)) {
      updated.remove(id);
    } else {
      updated.add(id);
    }
    ref.read(selectedIdsProvider.notifier).state = updated;

    if (updated.isEmpty) {
      ref.read(selectionModeProvider.notifier).state = false;
    }
  }
}
