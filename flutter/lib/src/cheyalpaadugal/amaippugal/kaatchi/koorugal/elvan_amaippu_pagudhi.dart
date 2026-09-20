import 'package:flutter/material.dart';

// ─────────────────────────────────────────────────────────────────────────────
// ElvanSettingsSection — Groups rows inside a single rounded card with dividers
// Mirrors the React ElvanSettingsSection SettingsSection component exactly.
// ─────────────────────────────────────────────────────────────────────────────
class ElvanSettingsSection extends StatelessWidget {
  const ElvanSettingsSection({
    super.key,
    required this.children,
    this.title,
    this.borderRadius = 24.0,
    this.dividerIndent = 16.0,
  });

  final List<Widget> children;
  final String? title;
  final double borderRadius;
  final double dividerIndent;

  @override
  Widget build(BuildContext context) {
    final isDark = Theme.of(context).brightness == Brightness.dark;
    final cardColor = isDark ? const Color(0xFF111111) : Colors.white;
    final dividerColor = isDark
        ? Colors.white.withValues(alpha: 0.04)
        : Colors.black.withValues(alpha: 0.04);

    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        if (title != null)
          Padding(
            padding: const EdgeInsets.only(left: 20, bottom: 8),
            child: Text(
              title!.toUpperCase(),
              style: TextStyle(
                fontSize: 12,
                fontWeight: FontWeight.w600,
                letterSpacing: 0.5,
                color: Theme.of(context)
                    .colorScheme
                    .onSurface
                    .withValues(alpha: 0.5),
              ),
            ),
          ),
        ClipRRect(
          borderRadius: BorderRadius.circular(borderRadius),
          child: Material(
            color: cardColor,
            child: Column(
              children: [
                for (int i = 0; i < children.length; i++) ...[
                  children[i],
                  if (i < children.length - 1)
                    Divider(
                      height: 1,
                      thickness: 1,
                      indent: dividerIndent,
                      endIndent: 20,
                      color: dividerColor,
                    ),
                ],
              ],
            ),
          ),
        ),
      ],
    );
  }
}

// ─────────────────────────────────────────────────────────────────────────────
// ElvanSettingsRow — A single settings item with circular icon, title, description
// Mirrors the React ElvanSettingsSection SettingsRow component exactly.
// Padding: 16px vertical, 20px horizontal (matches React's p: '16px 20px')
// Icon: 36px circle with monochrome background
// ─────────────────────────────────────────────────────────────────────────────
class ElvanSettingsRow extends StatelessWidget {
  const ElvanSettingsRow({
    super.key,
    this.icon,
    this.iconWidget,
    required this.iconBgColor,
    required this.title,
    this.description,
    this.customDescription,
    this.crossAxisAlignment,
    this.onTap,
  }) : assert(icon != null || iconWidget != null);

  final IconData? icon;
  final Widget? iconWidget;
  final Color iconBgColor;
  final String title;
  final String? description;
  final Widget? customDescription;
  final CrossAxisAlignment? crossAxisAlignment;
  final VoidCallback? onTap;

  @override
  Widget build(BuildContext context) {
    return InkWell(
      onTap: onTap,
      child: Padding(
        padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 16),
        child: Row(
          crossAxisAlignment: crossAxisAlignment ?? CrossAxisAlignment.center,
          children: [
            // Circular icon container — 36px, monochrome background
            Container(
              width: 36,
              height: 36,
              decoration: BoxDecoration(
                shape: BoxShape.circle,
                color: iconBgColor,
              ),
              child: Center(
                child: iconWidget ??
                    Icon(icon,
                        size: 20,
                        color: Theme.of(context).colorScheme.onSurface),
              ),
            ),
            const SizedBox(width: 16),
            // Title + Description
            Expanded(
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Text(
                    title,
                    style: const TextStyle(
                      fontSize: 15,
                      fontWeight: FontWeight.w500,
                      height: 1.3,
                    ),
                  ),
                  if (description != null)
                    Padding(
                      padding: const EdgeInsets.only(top: 2),
                      child: Text(
                        description!,
                        style: TextStyle(
                          fontSize: 12,
                          color: Theme.of(context)
                              .colorScheme
                              .onSurface
                              .withValues(alpha: 0.5),
                        ),
                      ),
                    ),
                  if (customDescription != null)
                    Padding(
                      padding: const EdgeInsets.only(top: 8),
                      child: customDescription!,
                    ),
                ],
              ),
            ),
          ],
        ),
      ),
    );
  }
}

// ─────────────────────────────────────────────────────────────────────────────
// ElvanRadioSettingsRow — A row for radio buttons matching ElvanSettingsRow
// ─────────────────────────────────────────────────────────────────────────────
class ElvanRadioSettingsRow<T> extends StatelessWidget {
  const ElvanRadioSettingsRow({
    super.key,
    required this.title,
    required this.value,
    required this.groupValue,
    required this.onChanged,
  });

  final String title;
  final T value;
  final T? groupValue;
  final ValueChanged<T?> onChanged;

  @override
  Widget build(BuildContext context) {
    final isSelected = value == groupValue;

    return GestureDetector(
      behavior: HitTestBehavior.opaque,
      onTap: () => onChanged(value),
      child: Padding(
        padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 16),
        child: Row(
          children: [
            Expanded(
              child: Text(
                title,
                style: TextStyle(
                  fontSize: 16,
                  fontWeight: isSelected ? FontWeight.w600 : FontWeight.w400,
                  height: 1.3,
                ),
              ),
            ),
            if (isSelected)
              Icon(
                Icons
                    .check, // Using standard check to prevent missing glyphs on Windows
                color: Theme.of(context).colorScheme.onSurface,
                size: 24, // M3 standard size
              )
            else
              const SizedBox(width: 24),
          ],
        ),
      ),
    );
  }
}

// ─────────────────────────────────────────────────────────────────────────────
// ElvanSimpleSettingsRow — A generic row with title, description, and trailing widget
// ─────────────────────────────────────────────────────────────────────────────
class ElvanSimpleSettingsRow extends StatelessWidget {
  const ElvanSimpleSettingsRow({
    super.key,
    required this.title,
    this.description,
    this.trailing,
    this.onTap,
  });

  final String title;
  final String? description;
  final Widget? trailing;
  final VoidCallback? onTap;

  @override
  Widget build(BuildContext context) {
    return InkWell(
      onTap: onTap,
      child: Padding(
        padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 12),
        child: Row(
          children: [
            Expanded(
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Text(
                    title,
                    style: const TextStyle(
                      fontSize: 15,
                      fontWeight: FontWeight.w500,
                      height: 1.3,
                    ),
                  ),
                  if (description != null)
                    Padding(
                      padding: const EdgeInsets.only(top: 2),
                      child: Text(
                        description!,
                        style: TextStyle(
                          fontSize: 13,
                          fontWeight: FontWeight.w400,
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
            if (trailing != null) ...[
              const SizedBox(width: 16),
              trailing!,
            ],
          ],
        ),
      ),
    );
  }
}
