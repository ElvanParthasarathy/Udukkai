import 'package:elvan_niril/src/adippadai/tharavuru/uruvugal.dart';
import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:elvan_niril/src/adippadai/mozhiyaakkam/k.dart';
import '../../../../adippadai/mozhiyaakkam/mozhi_vazhanguthi.dart';
import '../../kalanjiyam/vaangunar_nilaimai.dart';
import '../../../../koorugal/ulleedugal/elvan_thiruthi_ulleedu.dart';
import '../../../../koorugal/podhu_koorugal/elvan_kavanam.dart';

// ─────────────────────────────────────────────────────────────────────────────
// வணிகர் தேடு கூறு — Customer Picker Autocomplete
// ─────────────────────────────────────────────────────────────────────────────
// Reusable autocomplete widget that searches VaangunarTable entries by bilingual
// name (Tamil & English). Used by both Silk and Coolie invoice editors.
// ─────────────────────────────────────────────────────────────────────────────

/// Autocomplete search widget for selecting a [VaangunarTharavuru].
///
/// Watches [vaangunargalProvider] and filters the list by matching the
/// query against both Tamil and English name fields from the `peyar` map.
class VaangunarThaeduKooru extends ConsumerStatefulWidget {
  /// Callback fired when the user picks a customer from the list.
  final ValueChanged<VaangunarTharavuru> onSelected;

  /// Called when the user clears the current selection via the X button.
  final VoidCallback? onCleared;

  /// Called when the user taps the "Add New Customer" action.
  final VoidCallback? onRequestAddNew;

  /// If set, pre-fills the display text with this customer's name.
  final int? selectedId;

  /// The app mode identifier ('silk' or 'coolie').
  
  const VaangunarThaeduKooru({
    super.key,
    required this.onSelected,
    this.onCleared,
    this.onRequestAddNew,
    this.selectedId,
    
  });

  @override
  ConsumerState<VaangunarThaeduKooru> createState() =>
      _VaangunarThaeduKooruState();
}

class _VaangunarThaeduKooruState extends ConsumerState<VaangunarThaeduKooru> {
  late TextEditingController _textController;

  @override
  void initState() {
    super.initState();
    _textController = TextEditingController();
  }

  @override
  void dispose() {
    _textController.dispose();
    super.dispose();
  }

  // ── Helpers ────────────────────────────────────────────────────────────────

  /// Extracts the display name from a [VaangunarTharavuru], preferring Tamil.
  String _getDisplayName(VaangunarTharavuru entry) {
    final peyar = entry.peyar; // Map<String, String>
    return peyar['ta'] ?? peyar['en'] ?? '';
  }

  /// Extracts the secondary (alternate-language) name, or empty string.
  String _getSecondaryName(VaangunarTharavuru entry) {
    final peyar = entry.peyar;
    // If Tamil is the primary, show English as secondary (and vice versa).
    if (peyar.containsKey('ta') && peyar['ta']!.isNotEmpty) {
      return peyar['en'] ?? '';
    }
    return peyar['ta'] ?? '';
  }

  /// Extracts the city display text from the bilingual `oor` map.
  String _getOorDisplay(VaangunarTharavuru entry) {
    final oor = entry.oor;
    return oor['ta'] ?? oor['en'] ?? '';
  }

  /// Returns `true` if the entry matches the search [query] (case-insensitive).
  bool _matchesQuery(VaangunarTharavuru entry, String query) {
    final q = query.toLowerCase();
    final peyar = entry.peyar;
    final tamilMatch =
        (peyar['ta'] ?? '').toLowerCase().contains(q);
    final englishMatch =
        (peyar['en'] ?? '').toLowerCase().contains(q);
    return tamilMatch || englishMatch;
  }

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final colorScheme = theme.colorScheme;
    final vaangunargalAsync = ref.watch(vaangunargalProvider);

    return vaangunargalAsync.when(
      loading: () => _buildTextField(
        context,
        enabled: false,
        hintText: '...',
      ),
      error: (err, _) => _buildTextField(
        context,
        enabled: false,
        hintText: K.pizhai.tr(context, ref), // Error
      ),
      data: (vaangunargal) {
        // Pre-fill text if selectedId is provided.
        if (widget.selectedId != null && _textController.text.isEmpty) {
          final selected = vaangunargal.cast<VaangunarTharavuru?>().firstWhere(
                (v) => v!.id == widget.selectedId,
                orElse: () => null,
              );
          if (selected != null) {
            _textController.text = _getDisplayName(selected);
          }
        }

        return Autocomplete<VaangunarTharavuru>(
          displayStringForOption: _getDisplayName,
          optionsBuilder: (textEditingValue) {
            final query = textEditingValue.text.trim();
            if (query.isEmpty) return const Iterable.empty();
            return vaangunargal.where((v) => _matchesQuery(v, query));
          },
          onSelected: (entry) {
            _textController.text = _getDisplayName(entry);
            widget.onSelected(entry);
          },
          fieldViewBuilder: (
            context,
            fieldController,
            focusNode,
            onFieldSubmitted,
          ) {
            // Sync the external controller with the field controller.
            if (widget.selectedId != null && fieldController.text.isEmpty) {
              fieldController.text = _textController.text;
            }

            return TextField(
              controller: fieldController,
              focusNode: focusNode,
              style: theme.textTheme.bodyLarge,
              decoration: InputDecoration(
                isDense: true,
                constraints: const BoxConstraints(minHeight: 45, maxHeight: 45),
                labelText: K.vaangunargal.tr(context, ref),
                hintText: K.vaangunargal.tr(context, ref),
                prefixIcon: Icon(
                  Icons.person_search_rounded,
                  size: 20,
                  color: colorScheme.onSurfaceVariant,
                ),
                suffixIcon: fieldController.text.isNotEmpty
                    ? IconButton(
                        icon: Icon(
                          Icons.close,
                          size: 20,
                          color: colorScheme.onSurfaceVariant,
                        ),
                        onPressed: () {
                          fieldController.clear();
                          _textController.clear();
                          widget.onCleared?.call();
                        },
                      )
                    : null,
                contentPadding: const EdgeInsets.only(
                  left: 20,
                  right: 20,
                  top: 16,
                  bottom: 16,
                ),
                filled: true,
                fillColor: WidgetStateColor.resolveWith((states) {
                  final isLight = colorScheme.brightness == Brightness.light;
                  if (states.contains(WidgetState.focused) ||
                      states.contains(WidgetState.hovered)) {
                    return isLight ? Colors.white : colorScheme.onSurface.withValues(alpha: 0.12);
                  }
                  return isLight ? Colors.white : colorScheme.onSurface.withValues(alpha: 0.08);
                }),
                border: OutlineInputBorder(
                  borderRadius: BorderRadius.circular(100),
                  borderSide: BorderSide.none,
                ),
                enabledBorder: OutlineInputBorder(
                  borderRadius: BorderRadius.circular(100),
                  borderSide: BorderSide.none,
                ),
                focusedBorder: OutlineInputBorder(
                  borderRadius: BorderRadius.circular(100),
                  borderSide: BorderSide.none,
                ),
              ),
            );
          },
          optionsViewBuilder: (context, onSelected, options) {
            return Align(
              alignment: Alignment.topLeft,
              child: Material(
                elevation: 4,
                borderRadius: BorderRadius.circular(12),
                clipBehavior: Clip.antiAlias,
                surfaceTintColor: colorScheme.surfaceTint,
                color: colorScheme.surfaceContainerLow,
                child: ConstrainedBox(
                  constraints: const BoxConstraints(
                    maxHeight: 280,
                    maxWidth: 400,
                  ),
                  child: ListView.builder(
                    keyboardDismissBehavior: ElvanKavanam.surulNadathai,
                    padding: const EdgeInsets.symmetric(vertical: 4),
                    shrinkWrap: true,
                    itemCount: options.length +
                        (widget.onRequestAddNew != null ? 1 : 0),
                    itemBuilder: (context, index) {
                      // ── "Add New Customer" action tile ──
                      if (index == options.length) {
                        return Column(
                          mainAxisSize: MainAxisSize.min,
                          children: [
                            Divider(
                              height: 1,
                              color: colorScheme.outlineVariant,
                            ),
                            ListTile(
                              dense: true,
                              leading: Icon(
                                Icons.add_circle_outline,
                                color: colorScheme.primary,
                              ),
                              title: Text(
                                K.pudhiyaChaerkkai.tr(context, ref),
                                style: theme.textTheme.bodyMedium?.copyWith(
                                  color: colorScheme.primary,
                                  fontWeight: FontWeight.w600,
                                ),
                              ),
                              onTap: () {
                                widget.onRequestAddNew?.call();
                                FocusScope.of(context).unfocus();
                              },
                            ),
                          ],
                        );
                      }

                      // ── Regular customer option tile ──
                      final entry = options.elementAt(index);
                      final primary = _getDisplayName(entry);
                      final secondary = _getSecondaryName(entry);
                      final oor = _getOorDisplay(entry);

                      return Column(
                        mainAxisSize: MainAxisSize.min,
                        children: [
                          if (index > 0)
                            Divider(
                              height: 1,
                              color: colorScheme.outlineVariant,
                            ),
                          ListTile(
                            dense: true,
                            title: Text(
                              primary,
                              style: theme.textTheme.bodyLarge?.copyWith(
                                fontWeight: FontWeight.w600,
                              ),
                            ),
                            subtitle: Column(
                              crossAxisAlignment: CrossAxisAlignment.start,
                              children: [
                                if (secondary.isNotEmpty)
                                  Text(
                                    secondary,
                                    style:
                                        theme.textTheme.bodySmall?.copyWith(
                                      color: colorScheme.onSurfaceVariant,
                                    ),
                                  ),
                                if (oor.isNotEmpty)
                                  Text(
                                    oor,
                                    style:
                                        theme.textTheme.labelSmall?.copyWith(
                                      color: colorScheme.outline,
                                    ),
                                  ),
                              ],
                            ),
                            onTap: () => onSelected(entry),
                          ),
                        ],
                      );
                    },
                  ),
                ),
              ),
            );
          },
        );
      },
    );
  }

  /// Builds a disabled text field for loading / error states.
  Widget _buildTextField(
    BuildContext context, {
    required bool enabled,
    required String hintText,
  }) {
    return ElvanThiruthiUlleedu(
      label: '',
      hintText: hintText,
      controller: _textController,
      enabled: enabled,
      prefixIcon: Icon(
        Icons.person_search_rounded,
        color: Theme.of(context).colorScheme.onSurfaceVariant,
      ),
    );
  }
}
