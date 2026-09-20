import 'package:elvan_niril/src/adippadai/tharavuru/uruvugal.dart';
import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:elvan_niril/src/adippadai/mozhiyaakkam/k.dart';
import 'package:intl/intl.dart';
import '../../../../adippadai/mozhiyaakkam/mozhi_vazhanguthi.dart';
import '../../../../koorugal/podhu_koorugal/elvan_kavanam.dart';
import '../../kalanjiyam/porul_nilaimai.dart';
import '../../../../koorugal/maeladukkugal/elvan_kizh_maeladukku/elvan_kizh_maeladukku.dart';
import '../../../../adippadai/nilaimai/achu_mozhi_facade.dart';
import '../../../../adippadai/iru_mozhi/iru_mozhi_vazhanguthigal.dart';
import '../../../../adippadai/iru_mozhi/iru_mozhi_porul_udhavi.dart';
import '../../../../adippadai/oru_mozhi/oru_mozhi_vazhanguthigal.dart';
import '../../../../adippadai/oru_mozhi/oru_mozhi_porul_udhavi.dart';
import '../../../../adippadai/nilaimai/seyali_nilaimai.dart';
import '../../../../koorugal/ulleedugal/elvan_thiruthi_ulleedu.dart';
import '../../../../koorugal/ulleedugal/elvan_thiruthi_pothan.dart';
import '../../../../koorugal/ulleedugal/elvan_thiruthi_thalaippu.dart';

// ─────────────────────────────────────────────────────────────────────────────
// பொருள் தேடு கூறு — Product Picker Selection
// ─────────────────────────────────────────────────────────────────────────────

/// Indian currency formatter: ₹1,23,456.00
final _inrFormat = NumberFormat.currency(
  locale: 'en_IN',
  symbol: '₹',
  decimalDigits: 2,
);

/// Selection widget for picking a [PorulTharavuru].
class PorulThaeduKooru extends ConsumerStatefulWidget {
  /// Callback fired when the user picks a product from the list.
  final ValueChanged<PorulTharavuru> onSelected;

  /// Called when the user taps the "Add New Product" action.
  final VoidCallback? onRequestAddNew;

  /// Optional text to pre-fill the search field.
  final String? initialText;

  /// The app mode identifier ('silk' or 'coolie').
  final String seyaliVagai;

  /// Called when the user clears the selected product (X button).
  final VoidCallback? onCleared;

  /// Optional background color for the pill
  final Color? backgroundColor;

  const PorulThaeduKooru({
    super.key,
    required this.onSelected,
    this.onRequestAddNew,
    this.onCleared,
    this.initialText,
    required this.seyaliVagai,
    this.backgroundColor,
  });

  @override
  ConsumerState<PorulThaeduKooru> createState() => _PorulThaeduKooruState();
}

class _PorulThaeduKooruState extends ConsumerState<PorulThaeduKooru> {
  late String _currentText;
  PorulTharavuru? _selectedItem;

  @override
  void initState() {
    super.initState();
    _currentText = widget.initialText ?? '';
  }

  @override
  void didUpdateWidget(covariant PorulThaeduKooru oldWidget) {
    super.didUpdateWidget(oldWidget);
    if (widget.initialText != oldWidget.initialText) {
      _currentText = widget.initialText ?? '';
    }
  }

  // ── Helpers ────────────────────────────────────────────────────────────────

  String _getDisplayName(
      BuildContext context, WidgetRef ref, PorulTharavuru entry) {
    if (_isSilk) {
      final mudhanmaiLang = ref.watch(silkMudhanmaiMozhiProvider);
      return IruMozhiPorulUdhavi.mudhanmaiPeyar(entry.porulPeyar, mudhanmaiLang);
    } else {
      final kooliLang = ref.watch(kooliAchuMozhiProvider);
      return OruMozhiPorulUdhavi.mudhanmaiPeyar(entry.porulPeyar, kooliLang);
    }
  }

  String _getSecondaryName(
      BuildContext context, WidgetRef ref, PorulTharavuru entry) {
    if (_isSilk) {
      final thunaiLang = ref.watch(silkThunaiMozhiProvider);
      final isBilingual = ref.watch(bilingualProvider);
      return IruMozhiPorulUdhavi.thunaiPeyar(entry.porulPeyar, thunaiLang, isBilingual);
    } else {
      final kooliLang = ref.watch(kooliAchuMozhiProvider);
      return OruMozhiPorulUdhavi.thunaiPeyar(entry.porulPeyar, kooliLang);
    }
  }

  bool _matchesQuery(PorulTharavuru entry, String query) {
    final q = query.toLowerCase();
    final peyar = entry.porulPeyar;
    final tamilMatch = (peyar['ta'] ?? '').toLowerCase().contains(q);
    final englishMatch = (peyar['en'] ?? '').toLowerCase().contains(q);
    final hsnMatch = entry.hsnCode.toLowerCase().contains(q);
    return tamilMatch || englishMatch || hsnMatch;
  }

  bool get _isSilk => widget.seyaliVagai == 'silk';

  void _openSelectionSheet(List<PorulTharavuru> items) {
    final isBilingual = ref.read(bilingualProvider);

    showElvanSelectionBottomSheet<PorulTharavuru>(
      context: context,
      title: K.porutkal.tr(context, ref),
      items: items,
      currentValue: _selectedItem,
      showSearch: true,
      searchFilter: _matchesQuery,
      onRequestAddNew: widget.onRequestAddNew,
      onSelected: (val) {
        setState(() {
          _selectedItem = val;
          _currentText = _getDisplayName(context, ref, val);
        });
        widget.onSelected(val);
      },
      itemLabelBuilder: (ctx, ref, item) => _getDisplayName(ctx, ref, item),
      subtitleBuilder: (ctx, ref, item) {
        final secondary = isBilingual ? _getSecondaryName(ctx, ref, item) : '';
        final parts = <String>[];
        
        if (secondary.isNotEmpty && secondary != _getDisplayName(ctx, ref, item)) {
          parts.add(secondary);
        }
        if (_isSilk && item.vilai > 0) {
          parts.add(_inrFormat.format(item.vilai));
        }
        
        if (parts.isEmpty) return '';
        return parts.join('  •  ');
      },
    );
  }

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final colorScheme = theme.colorScheme;
    final porulgalAsync = ref.watch(porulgalProvider);

    return porulgalAsync.when(
      loading: () => _buildFakeField(
        context: context,
        labelText: K.porutkal.tr(context, ref),
        text: '...',
        enabled: false,
      ),
      error: (err, _) => _buildFakeField(
        context: context,
        labelText: K.porutkal.tr(context, ref),
        text: K.pizhai.tr(context, ref),
        enabled: false,
      ),
      data: (porulgal) {
        return _buildFakeField(
          context: context,
          labelText: K.porutkal.tr(context, ref),
          text:
              _currentText.isEmpty ? K.porutkal.tr(context, ref) : _currentText,
          isHint: _currentText.isEmpty,
          enabled: true,
          backgroundColor: widget.backgroundColor,
          onTap: () {
            ElvanKavanam.viduvi(context);
            _openSelectionSheet(porulgal);
          },
          onClear: _currentText.isNotEmpty
              ? () {
                  setState(() {
                    _currentText = '';
                    _selectedItem = null;
                  });
                  widget.onCleared?.call();
                }
              : null,
        );
      },
    );
  }

  Widget _buildFakeField({
    required BuildContext context,
    required String text,
    bool isHint = false,
    required bool enabled,
    String? labelText,
    VoidCallback? onTap,
    VoidCallback? onClear,
    Color? backgroundColor,
  }) {
    final colorScheme = Theme.of(context).colorScheme;

    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        if (labelText != null && labelText.isNotEmpty) 
          ElvanThiruthiThalaippu(label: labelText),
        ElvanThiruthiPothan(
          onTap: enabled ? onTap : null,
          backgroundColor: backgroundColor,
          padding: const EdgeInsets.only(left: 20, right: 6),
          child: Row(
            children: [
              Expanded(
                child: Text(
                  text,
                  style: TextStyle(
                    fontSize: 14,
                    color: isHint ? colorScheme.onSurfaceVariant : colorScheme.onSurface,
                  ),
                  overflow: TextOverflow.ellipsis,
                ),
              ),
              if (onClear != null && !isHint)
                IconButton(
                  icon: const Icon(Icons.close_rounded),
                  iconSize: 16,
                  color: colorScheme.onSurface,
                  style: IconButton.styleFrom(
                    padding: const EdgeInsets.all(8),
                    minimumSize: const Size(0, 0),
                  ),
                  onPressed: onClear,
                )
              else
                Padding(
                  padding: const EdgeInsets.all(8.0),
                  child: Icon(
                    Icons.keyboard_arrow_down_rounded,
                    size: 20.0,
                    color: colorScheme.onSurface.withValues(alpha: 0.5),
                  ),
                ),
            ],
          ),
        ),
      ],
    );
  }
}
