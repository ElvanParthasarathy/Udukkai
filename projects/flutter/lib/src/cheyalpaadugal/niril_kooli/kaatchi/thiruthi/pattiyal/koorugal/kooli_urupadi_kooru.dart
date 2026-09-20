import 'package:elvan_niril/src/adippadai/oru_mozhi/oru_mozhi_vazhanguthigal.dart';
import 'package:elvan_niril/src/adippadai/oru_mozhi/oru_mozhi_porul_udhavi.dart';
import 'package:flutter/material.dart';
import 'package:flutter/cupertino.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import 'package:elvan_niril/src/adippadai/mozhiyaakkam/k.dart';
import '../../../../../../adippadai/mozhiyaakkam/mozhi_vazhanguthi.dart';
import 'package:intl/intl.dart';

import '../../../../../../koorugal/podhu_koorugal/elvan_thiruthi_attai_kooru.dart';
import '../../../../../../koorugal/ulleedugal/elvan_thiruthi_ulleedu.dart';
import '../../../../../niril_podhu/kaatchi/koorugal/porul_thaedu_kooru.dart';
import '../../../../../niril_podhu/tharavuru/pattiyal_tharavuru.dart';
import '../../../../../niril_pattu/kaatchi/thiruthi/pattiyal/koorugal/pattu_urupadi_attai.dart';

/// Builds a single coolie line-item row: header label + delete + ElvanUrupadiAttai.
class KooliUrupadiKooru extends ConsumerWidget {
  final int index;
  final KooliUrupadi item;
  final int itemCount;
  final NumberFormat formatter;
  final ValueChanged<KooliUrupadi> onUpdated;
  final VoidCallback onDeleted;
  final VoidCallback? onRequestAddNewProduct;
  final VoidCallback? onAddNewItem;
  final VoidCallback? onAddNewCharge;

  const KooliUrupadiKooru({
    super.key,
    required this.index,
    required this.item,
    required this.itemCount,
    required this.formatter,
    required this.onUpdated,
    required this.onDeleted,
    this.onRequestAddNewProduct,
    this.onAddNewItem,
    this.onAddNewCharge,
  });

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final cs = Theme.of(context).colorScheme;
    final isDark = Theme.of(context).brightness == Brightness.dark;
    final tt = Theme.of(context).textTheme;

    final kooliLang = ref.watch(kooliAchuMozhiProvider);
    final primaryName = OruMozhiPorulUdhavi.mudhanmaiPeyar(item.mozhiMap, kooliLang);
    final secondaryName = OruMozhiPorulUdhavi.thunaiPeyar(item.mozhiMap, kooliLang);
    final displayPrimary = primaryName.isNotEmpty ? primaryName : item.porulPeyar;
    final displaySecondary = secondaryName.isNotEmpty ? secondaryName : item.porulPeyarEn;

    return Padding(
      padding: const EdgeInsets.only(bottom: 8),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          // Item #N header + trash
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
                      onPressed: onDeleted,
                    ),
                  ),
                ),
              ),
            ],
          ),
          
          ElvanUrupadiAttai(
            padding: const EdgeInsets.all(16),
            child: LayoutBuilder(builder: (context, constraints) {
              final isWide = constraints.maxWidth >= 600;
              const gap = SizedBox(width: 12);
              const vGap = SizedBox(height: 12);

              // Product search row (always full width)
              final productSearch = PorulThaeduKooru(
                seyaliVagai: 'coolie',
                initialText: displayPrimary,
                onSelected: (p) {
                  final primaryName = OruMozhiPorulUdhavi.mudhanmaiPeyar(p.porulPeyar, kooliLang);
                  final secondaryName = OruMozhiPorulUdhavi.thunaiPeyar(p.porulPeyar, kooliLang);
                  onUpdated(item.copyWith(
                    porulId: p.id.toString(),
                    porulPeyar: primaryName.isNotEmpty
                        ? primaryName
                        : secondaryName,
                    porulPeyarEn: primaryName.isNotEmpty
                        ? secondaryName
                        : '',
                    mozhiMap: p.porulPeyar,
                  ));
                },
                onRequestAddNew: onRequestAddNewProduct,
                onCleared: () {
                  onUpdated(const KooliUrupadi());
                },
              );

              // Build field widgets
              final weightField = _buildItemField(
                context,
                K.edai.tr(context, ref),
                item.edai,
                (v) => onUpdated(
                    item.copyWith(edai: double.tryParse(v) ?? 0)),
                isWeight: true,
              );

              final rateField = _buildItemField(
                context, 
                K.vilai.tr(context, ref), 
                item.vilai, 
                (v) => onUpdated(
                    item.copyWith(vilai: double.tryParse(v) ?? 0)),
              );

              final totalDisplay = ElvanThiruthiUlleedu(
                key: ValueKey(item.varisaiThogai),
                label: K.motham.tr(context, ref),
                initialValue: formatter.format(item.varisaiThogai),
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
                        Expanded(flex: 1, child: weightField),
                        gap,
                        Expanded(flex: 1, child: rateField),
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
                  const SizedBox(height: 8),
                  Row(children: [
                    Expanded(child: weightField),
                    gap,
                    Expanded(child: rateField),
                  ]),
                  vGap,
                  totalDisplay,

                ],
              );
            }),
          ),
          if ((onAddNewItem != null || onAddNewCharge != null) && index == itemCount - 1) ...[
            const SizedBox(height: 16),
            Align(
              alignment: Alignment.centerLeft,
              child: Wrap(
                spacing: 12,
                children: [
                  if (onAddNewItem != null)
                    TextButton.icon(
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
                  if (onAddNewCharge != null)
                    TextButton.icon(
                      onPressed: onAddNewCharge,
                      icon: const Icon(Icons.add, size: 20),
                      label: Text(K.piraVarivuChaer.tr(context, ref), style: const TextStyle(fontWeight: FontWeight.w600)),
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
                ],
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
      {bool isWeight = false}) {
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
      backgroundColor: Theme.of(context).colorScheme.onSurface.withValues(alpha: 0.08),
    );
  }
}
