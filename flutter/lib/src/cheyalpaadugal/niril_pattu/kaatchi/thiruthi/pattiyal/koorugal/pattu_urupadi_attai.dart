import 'package:elvan_niril/src/adippadai/iru_mozhi/iru_mozhi_vazhanguthigal.dart';
import 'package:flutter/material.dart';
import 'package:flutter/cupertino.dart';
import 'package:flutter/services.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:intl/intl.dart';
import 'package:elvan_niril/src/adippadai/mozhiyaakkam/k.dart';
import '../../../../../../adippadai/mozhiyaakkam/mozhi_vazhanguthi.dart';
import '../../../../../../adippadai/iru_mozhi/iru_mozhi_porul_udhavi.dart';
import '../../../../../../koorugal/podhu_koorugal/elvan_thiruthi_attai_kooru.dart';
import '../../../../../../koorugal/ulleedugal/elvan_ulleedu_vadivamaippigal.dart';
import '../../../../../../koorugal/ulleedugal/elvan_thiruthi_ulleedu.dart';
import '../../../../../niril_podhu/kaatchi/koorugal/porul_thaedu_kooru.dart';
import '../../../../../niril_podhu/tharavuru/pattiyal_tharavuru.dart';
import '../../../../../../adippadai/nilaimai/seyali_nilaimai.dart';

// ─────────────────────────────────────────────────────────────────────────────
// பட்டு உருபடி அட்டை — Single Line Item Card for Silk Invoice
// ─────────────────────────────────────────────────────────────────────────────

/// Indian currency formatter: ₹1,23,456.00
final _inrFormat = NumberFormat.currency(
  locale: 'en_IN',
  symbol: '₹',
  decimalDigits: 2,
);

/// A single line item card for the Silk Invoice Editor.
///
/// Displays product search, qty/rate/discount/total fields,
/// and info line (English name · GST%).
class PattuUrupadiAttai extends ConsumerWidget {
  const PattuUrupadiAttai({
    super.key,
    required this.item,
    required this.index,
    required this.itemCount,
    required this.seyaliVagai,
    required this.onItemUpdated,
    required this.onItemDeleted,
    required this.onItemCleared,
    required this.onDirty,
    required this.onRequestAddNewProduct,
    this.onAddNewItem,
  });

  final PattuUrupadi item;
  final int index;
  final int itemCount;
  final String seyaliVagai;
  final ValueChanged<PattuUrupadi> onItemUpdated;
  final VoidCallback onItemDeleted;
  final VoidCallback onItemCleared;
  final VoidCallback onDirty;
  final VoidCallback onRequestAddNewProduct;
  final VoidCallback? onAddNewItem;

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final cs = Theme.of(context).colorScheme;
    final isDark = Theme.of(context).brightness == Brightness.dark;
    final tt = Theme.of(context).textTheme;
    final taxableAmount = item.adippadaiThogai - item.thallupadiThogai;
    final rowTax = taxableAmount * (item.variVizhukkaadu / 100);
    final rowTotal = taxableAmount + rowTax;

    return Padding(
      padding: const EdgeInsets.only(bottom: 8),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          // ── Header: "பொருள் #N" + trash icon ──
          Row(
            mainAxisAlignment: MainAxisAlignment.spaceBetween,
            crossAxisAlignment: CrossAxisAlignment.end,
            children: [
              Padding(
                padding: const EdgeInsets.only(left: 24, bottom: 0),
                child: Text(
                  '${K.porul.tr(context, ref)} #${index + 1}',
                  style: tt.labelMedium?.copyWith(
                    fontWeight: FontWeight.w600,
                    color: cs.onSurface.withValues(alpha: 0.5),
                  ),
                ),
              ),
              Opacity(
                opacity: itemCount > 1 ? 1.0 : 0.0,
                child: IgnorePointer(
                  ignoring: itemCount <= 1,
                  child: Padding(
                    padding: const EdgeInsets.only(right: 12, bottom: 12),
                    child: IconButton(
                      icon: const Icon(CupertinoIcons.delete, size: 20),
                      color: cs.onSurfaceVariant,
                      style: IconButton.styleFrom(
                        backgroundColor: Theme.of(context).brightness == Brightness.dark 
                            ? Colors.white.withValues(alpha: 0.08) 
                            : Colors.white,
                      ),
                      onPressed: onItemDeleted,
                    ),
                  ),
                ),
              ),
            ],
          ),

          // ── Item card ──
          ElvanUrupadiAttai(
            padding: const EdgeInsets.all(16),
            child: LayoutBuilder(builder: (context, constraints) {
              final isBilingual = ref.watch(bilingualProvider);
              final mudhanmaiLang = ref.watch(silkMudhanmaiMozhiProvider);
              final irandaamLang = ref.watch(silkThunaiMozhiProvider);
              final displayMudhanmai = IruMozhiPorulUdhavi.mudhanmaiPeyar(item.mozhiMap, mudhanmaiLang);
              final displayIrandaam = IruMozhiPorulUdhavi.thunaiPeyar(item.mozhiMap, irandaamLang, isBilingual);
              final fallbackMudhanmai = displayMudhanmai.isNotEmpty ? displayMudhanmai : item.porulPeyar;
              final fallbackIrandaam = isBilingual 
                  ? (displayIrandaam.isNotEmpty ? displayIrandaam : item.porulPeyarEn)
                  : '';

              final isWide = constraints.maxWidth >= 600;
              const gap = SizedBox(width: 12);
              const vGap = SizedBox(height: 12);

              // Product search row (always full width)
              final productSearch = PorulThaeduKooru(
                seyaliVagai: seyaliVagai,
                initialText: fallbackMudhanmai,

                onSelected: (p) {
                  final mudhanmaiLang = ref.read(silkMudhanmaiMozhiProvider);
                  final irandaamLang = ref.read(silkThunaiMozhiProvider);
                  final isBilingual = ref.read(bilingualProvider);
                  final primaryName = IruMozhiPorulUdhavi.mudhanmaiPeyar(p.porulPeyar, mudhanmaiLang);
                  final secondaryName = IruMozhiPorulUdhavi.thunaiPeyar(p.porulPeyar, irandaamLang, isBilingual);
                  final updated = item.copyWith(
                    porulId: p.id.toString(),
                    porulPeyar: primaryName.isNotEmpty ? primaryName : secondaryName,
                    porulPeyarEn: secondaryName,
                    mozhiMap: p.porulPeyar,
                    hsnKuriyeedu: p.hsnCode,
                    vilai: p.vilai,
                    variVizhukkaadu: p.variVeetham,
                    alagu: p.alagu,
                  );
                  onItemUpdated(updated);
                },
                onRequestAddNew: onRequestAddNewProduct,
                onCleared: onItemCleared,
              );

              // Build field widgets
              final isWeightItem = item.alagu == 'Kg';
              final qtyField = _buildItemField(
                context,
                isWeightItem ? K.edai.tr(context, ref) : K.alavu.tr(context, ref),
                item.alavu,
                (v) => onItemUpdated(
                    item.copyWith(alavu: double.tryParse(v) ?? 0)),
                isWeight: isWeightItem,
              );

              final rateField = _buildItemField(context, K.vilaiVeedham.tr(context, ref), item.vilai, (v) {
                onItemUpdated(
                    item.copyWith(vilai: double.tryParse(v) ?? 0));
              });

              final discField = _buildItemField(
                context,
                K.thallupadi.tr(context, ref), 
                item.thallupadi, 
                (v) {
                  onItemUpdated(item.copyWith(
                      thallupadi: double.tryParse(v) ?? 0));
                },
                suffixIcon: Padding(
                  padding: const EdgeInsets.only(right: 6.0),
                  child: Material(
                    color: Colors.transparent,
                    shape: const CircleBorder(),
                    clipBehavior: Clip.antiAlias,
                    child: InkWell(
                      onTap: () {
                        final newType = item.thallupadiVagai == '%'
                            ? '₹'
                            : '%';
                        onItemUpdated(
                            item.copyWith(thallupadiVagai: newType));
                      },
                      child: SizedBox(
                        width: 32,
                        height: 32,
                        child: Center(
                          child: Text(
                            item.thallupadiVagai == '%' ? '%' : '₹',
                            style: const TextStyle(fontWeight: FontWeight.bold, fontSize: 16),
                            textAlign: TextAlign.center,
                          ),
                        ),
                      ),
                    ),
                  ),
                ),
              );

              final totalDisplay = ElvanThiruthiUlleedu(
                key: ValueKey(rowTotal),
                label: K.motham.tr(context, ref),
                initialValue: _inrFormat.format(rowTotal),
                readOnly: true,

              );



              if (isWide) {
                return Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Row(
                      crossAxisAlignment: CrossAxisAlignment.start,
                      children: [
                        Expanded(flex: 3, child: productSearch),
                        gap,
                        Expanded(flex: 1, child: qtyField),
                        gap,
                        Expanded(flex: 1, child: rateField),
                        gap,
                        Expanded(flex: 1, child: discField),
                        gap,
                        Expanded(flex: 1, child: totalDisplay),
                      ],
                    ),
                    ],
                );
              }

              return Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  productSearch,
                  vGap,
                  Row(children: [
                    Expanded(child: qtyField),
                    gap,
                    Expanded(child: rateField),
                  ]),
                  vGap,
                  Row(children: [
                    Expanded(child: discField),
                    gap,
                    Expanded(child: totalDisplay),
                  ]),

                ],
              );
            }),
          ),
          if (onAddNewItem != null && index == itemCount - 1) ...[
            const SizedBox(height: 16),
            Align(
              alignment: Alignment.centerLeft,
              child: TextButton.icon(
                onPressed: onAddNewItem,
                icon: const Icon(Icons.add, size: 20),
                label: Text(K.chaerPtn.tr(context, ref), style: const TextStyle(fontWeight: FontWeight.w600)),
                style: TextButton.styleFrom(
                  foregroundColor: cs.onSurface,
                  backgroundColor: isDark ? cs.onSurface.withValues(alpha: 0.08) : Colors.white,
                  padding: EdgeInsets.symmetric(
                    horizontal: 24, 
                    vertical: MediaQuery.sizeOf(context).width >= 600 ? 18 : 12,
                  ),
                  shape: const StadiumBorder(),
                ),
              ),
            ),
          ],
        ],
      ),
    );
  }

  /// Borderless numeric field — instant math + blur formatting.
  Widget _buildItemField(
      BuildContext context, String label, double value, ValueChanged<String> onChanged,
      {bool isWeight = false, Widget? suffixIcon}) {
    // Weight: always 3 decimals matching weighing machine (24.100 = 24kg 100g).
    // Non-weight: clean integers (6 not 6.0).
    String displayText = '';
    if (value != 0) {
      if (isWeight) {
        displayText = value.toStringAsFixed(3);
      } else {
        displayText = value == value.truncateToDouble()
            ? value.toInt().toString()
            : value.toString();
      }
    }
    return ItemFieldWidget(
      label: label,
      initialText: displayText,
      isWeight: isWeight,
      onValueCommitted: onChanged,
      onChanged: onChanged,
      onDirty: onDirty,
      suffixIcon: suffixIcon,
      backgroundColor: Theme.of(context).colorScheme.onSurface.withValues(alpha: 0.08),
    );
  }
}

// ─────────────────────────────────────────────────────────────────────────────
// எண்ணிக்கைப் புலம் — Stateful numeric field (blur-based commit)
// ─────────────────────────────────────────────────────────────────────────────

/// Stateful numeric field that owns its controller + focus node.
/// Updates the parent model only on blur (not every keystroke) to avoid lag.
class ItemFieldWidget extends StatefulWidget {
  const ItemFieldWidget({
    super.key,
    required this.label,
    required this.initialText,
    required this.onValueCommitted,
    this.isWeight = false,
    this.onDirty,
    this.onChanged,
    this.suffixIcon,
    this.backgroundColor,
  });

  final String label;
  final String initialText;
  final ValueChanged<String> onValueCommitted;
  final bool isWeight;
  /// Called on first keystroke to mark form as dirty (before blur).
  final VoidCallback? onDirty;
  /// Called on every keystroke for instant calculation (optional).
  final ValueChanged<String>? onChanged;
  final Widget? suffixIcon;
  final Color? backgroundColor;

  @override
  State<ItemFieldWidget> createState() => _ItemFieldWidgetState();
}

class _ItemFieldWidgetState extends State<ItemFieldWidget> {
  late TextEditingController _controller;
  late FocusNode _focusNode;
  bool _isDirty = false;

  @override
  void initState() {
    super.initState();
    _controller = TextEditingController(text: widget.initialText);
    _focusNode = FocusNode();
    _focusNode.addListener(_onFocusChange);
  }

  @override
  void didUpdateWidget(ItemFieldWidget oldWidget) {
    super.didUpdateWidget(oldWidget);
    // Only update text if value changed externally AND field is not focused
    if (!_focusNode.hasFocus && oldWidget.initialText != widget.initialText) {
      _controller.text = widget.initialText;
    }
  }

  @override
  void dispose() {
    _focusNode.removeListener(_onFocusChange);
    _focusNode.dispose();
    _controller.dispose();
    super.dispose();
  }

  void _onFocusChange() {
    if (!_focusNode.hasFocus) {
      // Blur: commit value to parent
      final text = _controller.text.trim();
      widget.onValueCommitted(text);

      // Weight: format to 3 decimals on blur (weighing machine standard)
      if (widget.isWeight && text.isNotEmpty) {
        final v = double.tryParse(text) ?? 0;
        if (v > 0) {
          _controller.text = v.toStringAsFixed(3);
        }
      }
    }
  }

  @override
  Widget build(BuildContext context) {
    return ElvanThiruthiUlleedu(
      label: widget.label,
      controller: _controller,
      focusNode: _focusNode,
      keyboardType: const TextInputType.numberWithOptions(decimal: true),
      inputFormatters: ElvanVadivamaippigal.thasamamEnngal,
      suffixText: widget.isWeight ? 'kg' : null,
      suffixIcon: widget.suffixIcon,
      backgroundColor: widget.backgroundColor,
      onChanged: (v) {
        if (!_isDirty) {
          _isDirty = true;
          widget.onDirty?.call();
        }
        widget.onChanged?.call(v);
      },
    );
  }
}
