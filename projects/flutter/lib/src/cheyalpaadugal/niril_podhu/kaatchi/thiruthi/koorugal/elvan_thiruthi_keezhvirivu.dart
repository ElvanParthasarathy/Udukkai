import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:elvan_niril/src/koorugal/maeladukkugal/elvan_kizh_maeladukku/elvan_kizh_maeladukku.dart';
import 'package:elvan_niril/src/koorugal/ulleedugal/elvan_thiruthi_pothan.dart';
import 'package:elvan_niril/src/koorugal/ulleedugal/elvan_thiruthi_thalaippu.dart';

/// A dropdown component designed specifically for the Elvan Editors (Thiruthi).
/// It visually matches the standard text fields (same padding and background alpha).
class ElvanThiruthiKeezhvirivu<T> extends ConsumerWidget {
  final String label;
  final bool hideLabel;
  final T? value;
  final List<T> items;
  final ValueChanged<T> onChanged;
  final VoidCallback? onClear;
  final String Function(BuildContext, WidgetRef, T)? subtitleBuilder;
  final Widget Function(BuildContext, WidgetRef, T)? leadingBuilder;
  final String Function(BuildContext, WidgetRef, T) itemLabelBuilder;
  
  // Optional features for the bottom sheet
  final bool showSearch;
  final bool Function(T, String)? searchFilter;
  final VoidCallback? onRequestAddNew;

  const ElvanThiruthiKeezhvirivu({
    super.key,
    required this.label,
    this.hideLabel = false,
    required this.value,
    required this.items,
    required this.onChanged,
    required this.itemLabelBuilder,
    this.onClear,
    this.subtitleBuilder,
    this.leadingBuilder,
    this.showSearch = false,
    this.searchFilter,
    this.onRequestAddNew,
  });

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        if (label.isNotEmpty && !hideLabel) ElvanThiruthiThalaippu(label: label),
        ElvanThiruthiPothan(
          onTap: () {
            showElvanSelectionBottomSheet<T>(
              context: context,
              title: label,
              items: items,
              currentValue: value,
              onSelected: onChanged,
              itemLabelBuilder: itemLabelBuilder,
              subtitleBuilder: subtitleBuilder,
              leadingBuilder: leadingBuilder,
              showSearch: showSearch,
              searchFilter: searchFilter,
              onRequestAddNew: onRequestAddNew,
            );
          },
          padding: const EdgeInsets.only(left: 20, right: 6),
          child: Row(
            children: [
              if (value != null && leadingBuilder != null) ...[
                leadingBuilder!(context, ref, value as T),
                const SizedBox(width: 8),
              ],
              Expanded(
                child: Text(
                  value == null ? '' : itemLabelBuilder(context, ref, value as T),
                  style: TextStyle(
                    fontSize: 14,
                    color: Theme.of(context).colorScheme.onSurface,
                  ),
                  overflow: TextOverflow.ellipsis,
                ),
              ),
              if (value != null) ...[
                if (onClear != null)
                  IconButton(
                    icon: const Icon(Icons.close_rounded),
                    iconSize: 16,
                    color: Theme.of(context).colorScheme.onSurface,
                    style: IconButton.styleFrom(
                      padding: const EdgeInsets.all(8),
                      minimumSize: const Size(0, 0),
                    ),
                    onPressed: onClear,
                  ),
              ] else
                Padding(
                  padding: const EdgeInsets.all(8.0),
                  child: Icon(
                    Icons.keyboard_arrow_down_rounded,
                    size: 20.0,
                    color: Theme.of(context)
                        .colorScheme
                        .onSurface
                        .withValues(alpha: 0.5),
                  ),
                ),
            ],
          ),
        ),
      ],
    );
  }
}
