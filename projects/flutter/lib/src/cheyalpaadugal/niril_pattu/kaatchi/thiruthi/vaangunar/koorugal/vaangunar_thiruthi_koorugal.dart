import 'package:flutter/cupertino.dart';
import 'package:flutter/material.dart';

/// Section title with subtle styling — used in merchant editors.
class VaangunarThiruthiPaguthiThalaipu extends StatelessWidget {
  const VaangunarThiruthiPaguthiThalaipu({
    super.key,
    required this.label,
    this.stepNumber,
    this.isActive = false,
  });

  final String label;
  final int? stepNumber;
  final bool isActive;

  @override
  Widget build(BuildContext context) {
    final isDark = Theme.of(context).brightness == Brightness.dark;
    
    // Using a subtle transition to show active state
    final textColor = isActive 
        ? (isDark ? Colors.white : Colors.black)
        : (isDark ? Colors.white70 : Colors.black87);

    return Row(
      children: [
        if (stepNumber != null) ...[
          Container(
            width: 28,
            height: 28,
            alignment: Alignment.center,
            decoration: BoxDecoration(
              shape: BoxShape.circle,
              color: isActive 
                  ? (isDark ? Colors.white : Colors.black) 
                  : (isDark ? Colors.white24 : Colors.black12),
            ),
            child: Text(
              '$stepNumber',
              style: TextStyle(
                fontWeight: FontWeight.bold,
                color: isActive 
                    ? (isDark ? Colors.black : Colors.white)
                    : (isDark ? Colors.white : Colors.black),
                fontSize: 14,
              ),
            ),
          ),
          const SizedBox(width: 12),
        ],
        Expanded(
          child: Text(
            label,
            style: Theme.of(context).textTheme.titleMedium?.copyWith(
                  fontWeight: FontWeight.w600,
                  color: textColor,
                ),
          ),
        ),
      ],
    );
  }
}

/// A tappable dropdown-like field that shows current value + edit icon.
/// Used for state/country pickers in merchant editors.
class VaangunarIzhivaruPulan extends StatelessWidget {
  const VaangunarIzhivaruPulan({
    super.key,
    required this.label,
    required this.primaryValue,
    this.secondaryValue,
    required this.isDark,
    required this.onTap,
  });

  final String label;
  final String primaryValue;
  final String? secondaryValue;
  final bool isDark;
  final VoidCallback onTap;

  @override
  Widget build(BuildContext context) {
    return GestureDetector(
      onTap: onTap,
      child: Container(
        padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 14),
        decoration: BoxDecoration(
          color: isDark
              ? Colors.white.withValues(alpha: 0.06)
              : Colors.black.withValues(alpha: 0.04),
          borderRadius: BorderRadius.circular(14),
        ),
        child: Row(
          children: [
            Expanded(
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Text(
                    label,
                    style: TextStyle(
                      fontSize: 12,
                      color: isDark
                          ? Colors.white.withValues(alpha: 0.5)
                          : Colors.black.withValues(alpha: 0.4),
                    ),
                  ),
                  const SizedBox(height: 4),
                  Text(
                    primaryValue.isEmpty ? '—' : primaryValue,
                    style: TextStyle(
                      fontSize: 16,
                      color: primaryValue.isEmpty
                          ? (isDark ? Colors.white30 : Colors.black26)
                          : null,
                    ),
                  ),
                  if (secondaryValue != null && secondaryValue!.isNotEmpty)
                    Text(
                      secondaryValue!,
                      style: TextStyle(
                        fontSize: 13,
                        color: isDark ? Colors.white38 : Colors.black38,
                      ),
                    ),
                ],
              ),
            ),
            Icon(
              CupertinoIcons.chevron_down,
              size: 16,
              color: isDark ? Colors.white38 : Colors.black38,
            ),
          ],
        ),
      ),
    );
  }
}

/// Bottom sheet picker for states / countries with search filtering.
class NilaiyamThaeraviPattai extends StatefulWidget {
  const NilaiyamThaeraviPattai({
    super.key,
    required this.states,
    required this.primaryKey,
    required this.secondaryKey,
    required this.onSelected,
    required this.onCustom,
  });

  final List<Map<String, String>> states;
  final String primaryKey;
  final String secondaryKey;
  final void Function(String primary, String secondary) onSelected;
  final VoidCallback onCustom;

  @override
  State<NilaiyamThaeraviPattai> createState() => _NilaiyamThaeraviPattaiState();
}

class _NilaiyamThaeraviPattaiState extends State<NilaiyamThaeraviPattai> {
  String _searchQuery = '';

  List<Map<String, String>> get _filtered {
    if (_searchQuery.isEmpty) return widget.states;
    final q = _searchQuery.toLowerCase();
    return widget.states.where((s) {
      if (s['custom'] == 'true') return true; // Always show custom
      return (s['en']?.toLowerCase().contains(q) ?? false) ||
          (s['ta']?.toLowerCase().contains(q) ?? false);
    }).toList();
  }

  @override
  Widget build(BuildContext context) {
    final isDark = Theme.of(context).brightness == Brightness.dark;

    return Container(
      constraints: BoxConstraints(
        maxHeight: MediaQuery.of(context).size.height * 0.6,
      ),
      decoration: BoxDecoration(
        color: isDark ? const Color(0xFF1E1E1E) : Colors.white,
        borderRadius: const BorderRadius.vertical(top: Radius.circular(20)),
      ),
      child: Column(
        mainAxisSize: MainAxisSize.min,
        children: [
          // Handle bar
          Container(
            margin: const EdgeInsets.only(top: 12),
            width: 40,
            height: 4,
            decoration: BoxDecoration(
              color: isDark ? Colors.white24 : Colors.black12,
              borderRadius: BorderRadius.circular(2),
            ),
          ),
          // Search
          Padding(
            padding: const EdgeInsets.all(16),
            child: CupertinoSearchTextField(
              onChanged: (q) => setState(() => _searchQuery = q),
              style: TextStyle(
                color: isDark ? Colors.white : Colors.black,
              ),
            ),
          ),
          // List
          Flexible(
            child: ListView.builder(
              itemCount: _filtered.length,
              itemBuilder: (ctx, i) {
                final item = _filtered[i];
                final isCustom = item['custom'] == 'true';

                if (isCustom) {
                  return ListTile(
                    leading: const Icon(CupertinoIcons.pencil, size: 18),
                    title: Text(item[widget.primaryKey] ?? ''),
                    onTap: widget.onCustom,
                  );
                }

                return ListTile(
                  title: Text(item[widget.primaryKey] ?? ''),
                  subtitle: widget.primaryKey != widget.secondaryKey
                      ? Text(
                          item[widget.secondaryKey] ?? '',
                          style: TextStyle(
                            fontSize: 13,
                            color: isDark ? Colors.white38 : Colors.black38,
                          ),
                        )
                      : null,
                  onTap: () => widget.onSelected(
                    item[widget.primaryKey] ?? '',
                    item[widget.secondaryKey] ?? '',
                  ),
                );
              },
            ),
          ),
        ],
      ),
    );
  }
}
