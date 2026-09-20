import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import '../../../../koorugal/ulleedugal/elvan_thiruthi_ulleedu.dart';

// ─────────────────────────────────────────────────────────────────────────────
// ElvanSettingsAnimatedExpand — Smoothly expands and fades between view and edit
// ─────────────────────────────────────────────────────────────────────────────
class ElvanSettingsAnimatedExpand extends StatelessWidget {
  final bool isEditing;
  final Widget displayChild;
  final Widget editChild;
  final String keyPrefix;

  const ElvanSettingsAnimatedExpand({
    super.key,
    required this.isEditing,
    required this.displayChild,
    required this.editChild,
    required this.keyPrefix,
  });

  @override
  Widget build(BuildContext context) {
    return Column(
      mainAxisSize: MainAxisSize.min,
      children: [
        AnimatedSwitcher(
          duration: const Duration(milliseconds: 300),
          switchInCurve: Curves.easeInOut,
          switchOutCurve: Curves.easeInOut,
          transitionBuilder: (child, animation) {
            return SizeTransition(
              sizeFactor: animation,
              axisAlignment: -1.0,
              child: FadeTransition(
                opacity: animation,
                child: child,
              ),
            );
          },
          child: !isEditing
              ? SizedBox(
                  key: ValueKey('${keyPrefix}_display'),
                  width: double.infinity,
                  child: displayChild,
                )
              : const SizedBox.shrink(key: ValueKey('empty_display')),
        ),
        AnimatedSwitcher(
          duration: const Duration(milliseconds: 300),
          switchInCurve: Curves.easeInOut,
          switchOutCurve: Curves.easeInOut,
          transitionBuilder: (child, animation) {
            return SizeTransition(
              sizeFactor: animation,
              axisAlignment: -1.0,
              child: FadeTransition(
                opacity: animation,
                child: child,
              ),
            );
          },
          child: isEditing
              ? SizedBox(
                  key: ValueKey('${keyPrefix}_edit'),
                  width: double.infinity,
                  child: editChild,
                )
              : const SizedBox.shrink(key: ValueKey('empty_edit')),
        ),
      ],
    );
  }
}

// ─────────────────────────────────────────────────────────────────────────────
// ElvanSettingsTextField — Pill-shaped text field with dynamic fill color
// ─────────────────────────────────────────────────────────────────────────────
class ElvanSettingsTextField extends StatelessWidget {
  final String label;
  final String initialValue;
  final ValueChanged<String> onChanged;
  final int maxLines;
  final TextInputType? keyboardType;
  final Widget? suffixIcon;
  final List<TextInputFormatter>? inputFormatters;
  final int? maxLength;

  const ElvanSettingsTextField({
    super.key,
    required this.label,
    required this.initialValue,
    required this.onChanged,
    this.maxLines = 1,
    this.keyboardType,
    this.suffixIcon,
    this.inputFormatters,
    this.maxLength,
  });

  @override
  Widget build(BuildContext context) {
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        Padding(
          padding: const EdgeInsets.only(left: 16, bottom: 8),
          child: Text(
            label,
            style: TextStyle(
              fontSize: 12,
              fontWeight: FontWeight.w400,
              letterSpacing: 0.3,
              color: Theme.of(context)
                  .colorScheme
                  .onSurface
                  .withValues(alpha: 0.5),
            ),
          ),
        ),
        ElvanThiruthiUlleedu(
          initialValue: initialValue,
          onChanged: onChanged,
          keyboardType: keyboardType,
          inputFormatters: inputFormatters,
          maxLength: maxLength,
          maxLines: maxLines,
          suffixIcon: suffixIcon,
        ),
      ],
    );
  }
}

// ─────────────────────────────────────────────────────────────────────────────
// ElvanSettingsEditContainer — Tinted background for edit mode text fields
// ─────────────────────────────────────────────────────────────────────────────
class ElvanSettingsEditContainer extends StatelessWidget {
  final Widget child;

  const ElvanSettingsEditContainer({super.key, required this.child});

  @override
  Widget build(BuildContext context) {
    return Container(
      width: double.infinity,
      padding: const EdgeInsets.all(16),
      child: child,
    );
  }
}

// ─────────────────────────────────────────────────────────────────────────────
// ElvanSettingsDisplayRow — Read-only state showing primary/secondary values
// ─────────────────────────────────────────────────────────────────────────────
class ElvanSettingsDisplayRow extends StatelessWidget {
  final String title;
  final String primaryValue;
  final String? secondaryValue;
  final VoidCallback? onEdit;
  final VoidCallback? onTap;
  final IconData icon;
  final Color? iconColor;
  final Widget? primaryWidget;

  const ElvanSettingsDisplayRow({
    super.key,
    required this.title,
    required this.primaryValue,
    this.secondaryValue,
    this.onEdit,
    this.onTap,
    this.icon = Icons.edit_rounded,
    this.iconColor,
    this.primaryWidget,
  });

  @override
  Widget build(BuildContext context) {
    return InkWell(
      onTap: onTap,
      borderRadius: BorderRadius.circular(16), // Match inner rounded corners
      child: Padding(
        padding: const EdgeInsets.symmetric(horizontal: 16.0, vertical: 12.0),
        child: Row(
          crossAxisAlignment: CrossAxisAlignment.center,
          children: [
            Expanded(
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Text(
                    title,
                    style: TextStyle(
                      fontSize: 14,
                      color: Theme.of(context)
                          .colorScheme
                          .onSurface
                          .withValues(alpha: 0.6),
                    ),
                  ),
                  const SizedBox(height: 6),
                  if (primaryWidget != null)
                    Padding(
                      padding: const EdgeInsets.only(bottom: 6.0),
                      child: primaryWidget!,
                    ),
                  if (primaryWidget == null || primaryValue.isNotEmpty)
                    Text(
                      primaryWidget != null && primaryValue.isEmpty
                          ? ''
                          : (primaryValue.isEmpty ? '-' : primaryValue),
                      maxLines: 1,
                      overflow: TextOverflow.ellipsis,
                      style: const TextStyle(
                        fontSize: 14,
                        fontWeight: FontWeight.w500,
                      ),
                    ),
                  if (secondaryValue != null && secondaryValue!.isNotEmpty)
                    Padding(
                      padding: const EdgeInsets.only(top: 2.0),
                      child: Text(
                        secondaryValue!,
                        maxLines: 1,
                        overflow: TextOverflow.ellipsis,
                        style: TextStyle(
                          fontSize: 12,
                          color: Theme.of(context)
                              .colorScheme
                              .onSurface
                              .withValues(alpha: 0.6),
                        ),
                      ),
                    ),
                ],
              ),
            ),
            if (onEdit != null)
              IconButton(
                onPressed: onEdit,
              style: IconButton.styleFrom(
                backgroundColor: iconColor != null
                    ? iconColor!.withValues(alpha: 0.1)
                    : Theme.of(context)
                        .colorScheme
                        .onSurface
                        .withValues(alpha: 0.05),
                fixedSize: const Size(40, 40),
              ),
              icon: Icon(
                icon,
                size: 20,
                color: iconColor ??
                    Theme.of(context)
                        .colorScheme
                        .onSurface
                        .withValues(alpha: 0.6),
              ),
            ),
          ],
        ),
      ),
    );
  }
}
