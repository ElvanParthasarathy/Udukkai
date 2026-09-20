import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import 'koorugal/elvan_maeladukku_pudhiya_pothan.dart';
import 'koorugal/elvan_maeladukku_thaedal.dart';
import 'koorugal/elvan_maeladukku_urupadi.dart';
import '../elvan_kizh_maeladukku.dart' as legacy;
import 'package:elvan_niril/src/adippadai/vazhikaattal/niril_nav.dart';

Future<void> showElvanSelectionBottomSheet<T>({
  required BuildContext context,
  required String title,
  required List<T> items,
  required T? currentValue,
  required ValueChanged<T> onSelected,
  required String Function(BuildContext, WidgetRef, T) itemLabelBuilder,
  String Function(BuildContext, WidgetRef, T)? subtitleBuilder,
  Widget Function(BuildContext, WidgetRef, T)? leadingBuilder,
  bool showSearch = false,
  bool Function(T, String)? searchFilter,
  VoidCallback? onRequestAddNew,
}) {
  return legacy.showElvanBottomSheet(
    context: context,
    builder: (context) {
      return ElvanSelectionBottomSheet<T>(
        title: title,
        items: items,
        currentValue: currentValue,
        onSelected: (val) {
          Navigator.pop(context);
          onSelected(val);
        },
        itemLabelBuilder: itemLabelBuilder,
        subtitleBuilder: subtitleBuilder,
        leadingBuilder: leadingBuilder,
        showSearch: showSearch,
        searchFilter: searchFilter,
        onRequestAddNew: onRequestAddNew,
      );
    },
  );
}

class ElvanSelectionBottomSheet<T> extends ConsumerStatefulWidget {
  final String title;
  final List<T> items;
  final T? currentValue;
  final ValueChanged<T> onSelected;
  final String Function(BuildContext, WidgetRef, T) itemLabelBuilder;
  final String Function(BuildContext, WidgetRef, T)? subtitleBuilder;
  final Widget Function(BuildContext, WidgetRef, T)? leadingBuilder;
  final bool showSearch;
  final bool Function(T, String)? searchFilter;
  final VoidCallback? onRequestAddNew;

  const ElvanSelectionBottomSheet({
    super.key,
    required this.title,
    required this.items,
    required this.currentValue,
    required this.onSelected,
    required this.itemLabelBuilder,
    this.subtitleBuilder,
    this.leadingBuilder,
    this.showSearch = false,
    this.searchFilter,
    this.onRequestAddNew,
  });

  @override
  ConsumerState<ElvanSelectionBottomSheet<T>> createState() =>
      _ElvanSelectionBottomSheetState<T>();
}

class _ElvanSelectionBottomSheetState<T>
    extends ConsumerState<ElvanSelectionBottomSheet<T>> {
  final TextEditingController _searchController = TextEditingController();
  List<T> _filteredItems = [];
  String _searchQuery = '';

  @override
  void initState() {
    super.initState();
    _filteredItems = widget.items;
  }

  @override
  void didUpdateWidget(covariant ElvanSelectionBottomSheet<T> oldWidget) {
    super.didUpdateWidget(oldWidget);
    if (widget.items != oldWidget.items) {
      _filterItems(_searchQuery);
    }
  }

  @override
  void dispose() {
    _searchController.dispose();
    super.dispose();
  }

  void _filterItems(String query) {
    setState(() {
      _searchQuery = query;
      if (query.isEmpty) {
        _filteredItems = widget.items;
      } else {
        if (widget.searchFilter != null) {
          _filteredItems = widget.items
              .where((item) => widget.searchFilter!(item, query))
              .toList();
        } else {
          // Fallback: search by title
          _filteredItems = widget.items.where((item) {
            final title =
                widget.itemLabelBuilder(context, ref, item).toLowerCase();
            return title.contains(query.toLowerCase());
          }).toList();
        }
      }
    });
  }

  Widget _buildDesktop(BuildContext context) {
    return Padding(
      padding: const EdgeInsets.symmetric(vertical: 24.0),
      child: Column(
        mainAxisSize: MainAxisSize.min,
        crossAxisAlignment: CrossAxisAlignment.stretch,
        children: [
          if (widget.showSearch) ...[
            ElvanMaeladukkuThaedal(
              controller: _searchController,
              onChanged: _filterItems,
            ),
            const SizedBox(height: 16),
          ],
          Flexible(
            child: ListView.builder(
              shrinkWrap: true,
              itemCount: _filteredItems.length,
              itemBuilder: (context, index) {
                final item = _filteredItems[index];
                final title = widget.itemLabelBuilder(context, ref, item);
                final subtitle =
                    widget.subtitleBuilder?.call(context, ref, item);
                final leading =
                    widget.leadingBuilder?.call(context, ref, item);
                final isSelected = widget.currentValue == item;

                return ElvanMaeladukkuUrupadi(
                  title: title,
                  subtitle: subtitle,
                  leading: leading,
                  isSelected: isSelected,
                  onTap: () => widget.onSelected(item),
                );
              },
            ),
          ),
          if (widget.onRequestAddNew != null) ...[
            const SizedBox(height: 24),
            Padding(
              padding: const EdgeInsets.symmetric(horizontal: 24.0),
              child: Align(
                alignment: Alignment.centerRight,
                child: SizedBox(
                  height: 40,
                  child: FilledButton.icon(
                    icon: const Icon(Icons.add_circle, size: 18),
                    label: Text('புதிய சேர்க்கை', style: const TextStyle(fontSize: 14)), // Hardcoded for now, or we can use K.pudhiyaChaerkkai.tr(context, ref)
                    style: FilledButton.styleFrom(
                      backgroundColor: Theme.of(context).colorScheme.onSurface,
                      foregroundColor: Theme.of(context).colorScheme.surface,
                      padding: const EdgeInsets.symmetric(horizontal: 24),
                      shape: RoundedRectangleBorder(
                        borderRadius: BorderRadius.circular(50),
                      ),
                    ),
                    onPressed: () {
                      Navigator.pop(context);
                      widget.onRequestAddNew!();
                    },
                  ),
                ),
              ),
            ),
          ],
        ],
      ),
    );
  }

  Widget _buildMobile(BuildContext context) {
    return ConstrainedBox(
      constraints: BoxConstraints(
        maxHeight: MediaQuery.of(context).size.height * 0.5,
      ),
      child: SafeArea(
        child: Column(
          mainAxisSize: MainAxisSize.min,
          crossAxisAlignment: CrossAxisAlignment.stretch,
          children: [
            if (widget.showSearch)
              ElvanMaeladukkuThaedal(
                controller: _searchController,
                onChanged: _filterItems,
              ),
            const SizedBox(height: 8),
            Flexible(
              child: ListView.builder(
                shrinkWrap: true,
                itemCount: _filteredItems.length,
                itemBuilder: (context, index) {
                  final item = _filteredItems[index];
                  final title = widget.itemLabelBuilder(context, ref, item);
                  final subtitle =
                      widget.subtitleBuilder?.call(context, ref, item);
                  final leading =
                      widget.leadingBuilder?.call(context, ref, item);
                  final isSelected = widget.currentValue == item;

                  return ElvanMaeladukkuUrupadi(
                    title: title,
                    subtitle: subtitle,
                    leading: leading,
                    isSelected: isSelected,
                    onTap: () => widget.onSelected(item),
                  );
                },
              ),
            ),
            if (widget.onRequestAddNew != null)
              ElvanMaeladukkuPudhiyaPothan(
                onTap: () {
                  Navigator.pop(context);
                  widget.onRequestAddNew!();
                },
              ),
            const SizedBox(height: 16),
          ],
        ),
      ),
    );
  }

  @override
  Widget build(BuildContext context) {
    if (isDesktopLayoutContext(context)) {
      return _buildDesktop(context);
    }
    return _buildMobile(context);
  }
}
