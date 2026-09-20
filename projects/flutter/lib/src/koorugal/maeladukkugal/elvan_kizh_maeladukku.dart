import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import 'dart:io' show Platform;
import 'package:flutter/foundation.dart' show kIsWeb;
import '../podhu_koorugal/elvan_kavanam.dart';
import '../podhu_koorugal/elvan_keezhnagar.dart';

Future<T?> showElvanBottomSheet<T>({
  required BuildContext context,
  required WidgetBuilder builder,
}) {
  ElvanKavanam.viduvi(context);

  final isDesktop =
      !kIsWeb && (Platform.isWindows || Platform.isMacOS || Platform.isLinux);

  if (isDesktop) {
    return showDialog<T>(
      context: context,
      useRootNavigator: true,
      builder: (context) {
        return Dialog(
          shape:
              RoundedRectangleBorder(borderRadius: BorderRadius.circular(16)),
          backgroundColor: Theme.of(context).brightness == Brightness.dark
              ? const Color(0xFF111111)
              : Colors.white,
          child: ConstrainedBox(
            constraints: const BoxConstraints(maxWidth: 480),
            child: builder(context),
          ),
        );
      },
    );
  }

  return showModalBottomSheet<T>(
    context: context,
    useRootNavigator: true,
    showDragHandle: true,
    isScrollControlled: true,
    useSafeArea: true,
    constraints: BoxConstraints(
      maxHeight: MediaQuery.of(context).size.height -
          MediaQuery.of(context).padding.top -
          24,
    ),
    backgroundColor: Theme.of(context).brightness == Brightness.dark
        ? const Color(0xFF111111)
        : Colors.white,
    builder: (context) => Column(
      mainAxisSize: MainAxisSize.min,
      children: [
        Flexible(child: builder(context)),
        const ElvanKeezhNagar(),
      ],
    ),
  );
}

Future<void> showElvanSelectionBottomSheet({
  required BuildContext context,
  required String title,
  required List<String> items,
  required String currentValue,
  required ValueChanged<String> onSelected,
  Map<String, String>? subtitles,
  String Function(BuildContext, WidgetRef, String)? itemLabelBuilder,
}) {
  return showElvanBottomSheet(
    context: context,
    builder: (context) {
      return ElvanSelectionBottomSheet(
        title: title,
        items: items,
        currentValue: currentValue,
        onSelected: (val) {
          Navigator.pop(context);
          onSelected(val);
        },
        subtitles: subtitles,
        itemLabelBuilder: itemLabelBuilder,
      );
    },
  );
}

class ElvanSelectionBottomSheet extends ConsumerWidget {
  final String title;
  final List<String> items;
  final String currentValue;
  final ValueChanged<String> onSelected;
  final Map<String, String>? subtitles;
  final String Function(BuildContext, WidgetRef, String)? itemLabelBuilder;

  const ElvanSelectionBottomSheet({
    super.key,
    required this.title,
    required this.items,
    required this.currentValue,
    required this.onSelected,
    this.subtitles,
    this.itemLabelBuilder,
  });

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    return SafeArea(
      child: Column(
        mainAxisSize: MainAxisSize.min,
        crossAxisAlignment: CrossAxisAlignment.stretch,
        children: [
          Padding(
            padding:
                const EdgeInsets.only(left: 24, right: 24, bottom: 8, top: 12),
            child: Text(
              title,
              style: TextStyle(
                fontSize: 13,
                fontWeight: FontWeight.w500,
                color: Theme.of(context)
                    .colorScheme
                    .onSurface
                    .withValues(alpha: 0.5),
              ),
            ),
          ),
          ...items.map((item) {
            final isSelected = item == currentValue;
            final subtitle = subtitles?[item];
            
            String labelText = item;
            if (itemLabelBuilder != null) {
              labelText = itemLabelBuilder!(context, ref, item);
            }
            
            return InkWell(
              onTap: () {
                onSelected(item);
              },
              child: Padding(
                padding:
                    const EdgeInsets.symmetric(horizontal: 24, vertical: 16),
                child: Row(
                  children: [
                    Expanded(
                      child: Column(
                        crossAxisAlignment: CrossAxisAlignment.start,
                        children: [
                          Text(
                            labelText,
                            style: TextStyle(
                              fontSize: 18, // Fixed: larger and more premium font size
                              fontWeight:
                                  isSelected ? FontWeight.w600 : FontWeight.w400,
                              color: isSelected
                                  ? Theme.of(context).colorScheme.onSurface
                                  : Theme.of(context)
                                      .colorScheme
                                      .onSurface
                                      .withValues(alpha: 0.8),
                            ),
                          ),
                          if (subtitle != null && subtitle.isNotEmpty) ...[
                            const SizedBox(height: 4),
                            Text(
                              subtitle,
                              style: TextStyle(
                                fontSize: 13,
                                fontWeight: FontWeight.w400,
                                color: Theme.of(context)
                                    .colorScheme
                                    .onSurface
                                    .withValues(alpha: 0.5),
                              ),
                            ),
                          ]
                        ],
                      ),
                    ),
                    const SizedBox(width: 16),
                    if (isSelected)
                      Icon(
                        Icons.check,
                        color: Theme.of(context).colorScheme.onSurface,
                        size: 20,
                      ),
                  ],
                ),
              ),
            );
          }),
          const SizedBox(height: 16),
        ],
      ),
    );
  }
}
