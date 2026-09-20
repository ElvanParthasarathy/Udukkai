import 'dart:ui';
import 'package:flutter/cupertino.dart';
import 'package:flutter/foundation.dart';
import 'package:flutter/material.dart';

Widget buildElvanContextMenu(
    BuildContext context, EditableTextState editableTextState) {
  final List<ContextMenuButtonItem> filteredButtons =
      editableTextState.contextMenuButtonItems.where((buttonItem) {
    return buttonItem.type == ContextMenuButtonType.cut ||
        buttonItem.type == ContextMenuButtonType.copy ||
        buttonItem.type == ContextMenuButtonType.paste ||
        buttonItem.type == ContextMenuButtonType.selectAll;
  }).toList();

  final cs = Theme.of(context).colorScheme;
  final isDesktop = defaultTargetPlatform == TargetPlatform.windows ||
      defaultTargetPlatform == TargetPlatform.macOS ||
      defaultTargetPlatform == TargetPlatform.linux;

  if (filteredButtons.isEmpty) {
    return const SizedBox.shrink();
  }

  return TextSelectionToolbar(
    anchorAbove: editableTextState.contextMenuAnchors.primaryAnchor,
    anchorBelow: editableTextState.contextMenuAnchors.primaryAnchor,
    toolbarBuilder: (context, child) {
      return Padding(
        padding: const EdgeInsets.all(8.0),
        child: ClipRRect(
          borderRadius: BorderRadius.circular(16),
          child: Container(
            decoration: BoxDecoration(
              color: cs.surfaceContainerHigh,
              borderRadius: BorderRadius.circular(16),
              boxShadow: [
                BoxShadow(
                  color: Colors.black.withValues(alpha: 0.2),
                  blurRadius: 16,
                  offset: const Offset(0, 8),
                ),
              ],
            ),
            child: isDesktop
                ? IntrinsicWidth(
                    child: Column(
                      mainAxisSize: MainAxisSize.min,
                      crossAxisAlignment: CrossAxisAlignment.stretch,
                      children: _buildButtons(filteredButtons, cs, isDesktop),
                    ),
                  )
                : child, // For mobile, let TextSelectionToolbar handle horizontal wrapping
          ),
        ),
      );
    },
    // TextSelectionToolbar requires children to not be empty.
    // If desktop, we build the column in toolbarBuilder and ignore this child row.
    // If mobile, we pass the built buttons to be laid out horizontally.
    children: isDesktop ? [const SizedBox.shrink()] : _buildButtons(filteredButtons, cs, isDesktop),
  );
}

List<Widget> _buildButtons(
    List<ContextMenuButtonItem> buttons, ColorScheme cs, bool isDesktop) {
  return buttons.map((button) {
    IconData icon;
    String label = '';
    switch (button.type) {
      case ContextMenuButtonType.cut:
        icon = CupertinoIcons.scissors;
        label = 'வெட்டு';
        break;
      case ContextMenuButtonType.copy:
        icon = CupertinoIcons.doc_on_doc;
        label = 'நகலெடு';
        break;
      case ContextMenuButtonType.paste:
        icon = CupertinoIcons.doc_on_clipboard;
        label = 'ஒட்டு';
        break;
      case ContextMenuButtonType.selectAll:
        icon = CupertinoIcons.selection_pin_in_out;
        label = 'முழுதும் தேர்ந்தெடு';
        break;
      default:
        icon = CupertinoIcons.hand_draw;
    }

    return Material(
      color: Colors.transparent,
      child: InkWell(
        onTap: button.onPressed,
        hoverColor: cs.onSurface.withValues(alpha: 0.05),
        splashColor: cs.onSurface.withValues(alpha: 0.1),
        child: Padding(
          padding: EdgeInsets.symmetric(
              horizontal: isDesktop ? 16 : 16,
              vertical: isDesktop ? 10 : 12),
          child: Row(
            mainAxisSize: MainAxisSize.min,
            children: [
              Icon(icon, size: 18, color: cs.onSurfaceVariant),
              SizedBox(width: isDesktop ? 12 : 8),
              Text(
                label,
                style: TextStyle(
                  fontSize: 14,
                  fontWeight: FontWeight.w500,
                  color: cs.onSurface,
                ),
              ),
            ],
          ),
        ),
      ),
    );
  }).toList();
}
