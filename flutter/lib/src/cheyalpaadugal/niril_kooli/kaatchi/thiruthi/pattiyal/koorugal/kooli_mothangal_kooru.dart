import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:intl/intl.dart';

import 'package:elvan_niril/src/adippadai/mozhiyaakkam/k.dart';
import '../../../../../../adippadai/mozhiyaakkam/mozhi_vazhanguthi.dart';
import '../../../../../../koorugal/podhu_koorugal/elvan_thiruthi_attai_kooru.dart';
import '../../../../../niril_podhu/tharavuru/pattiyal_tharavuru.dart';
import 'kooli_thiruthi_udhavigal.dart';

/// §5 Totals — sub-totals, weight, setharam, ahimsa, courier, other charges,
/// grand total.
class KooliMothangalKooru extends ConsumerWidget {
  final KooliMothangal totals;
  final double setharamGrams;
  final double ahimsaPattuThogai;
  final double thabaalThogai;
  final List<PiraVarivu> piraVarivugal;
  final NumberFormat formatter;

  const KooliMothangalKooru({
    super.key,
    required this.totals,
    required this.setharamGrams,
    required this.ahimsaPattuThogai,
    required this.thabaalThogai,
    required this.piraVarivugal,
    required this.formatter,
  });

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final cs = Theme.of(context).colorScheme;
    final tt = Theme.of(context).textTheme;

    return Align(
      alignment: Alignment.centerLeft,
      child: ConstrainedBox(
        constraints: const BoxConstraints(maxWidth: 400),
        child: ElvanThiruthiAttai(
          color: cs.brightness == Brightness.light ? Colors.white : cs.onSurface.withValues(alpha: 0.08),
          padding: const EdgeInsets.all(24),
          child: Column(
            children: [
              kooliTotalsRow(
                K.ulmotham.tr(context, ref),
                '₹${formatter.format(totals.adippadaiMothangal)}',
                labelWeight: FontWeight.w600,
                labelColor: cs.onSurfaceVariant,
                valueWeight: FontWeight.w700,
              ),
              const SizedBox(height: 12),
              if (ahimsaPattuThogai > 0) ...[
                kooliTotalsRow(K.ahimsaiPattu.tr(context, ref),
                    '₹${formatter.format(ahimsaPattuThogai)}'),
                const SizedBox(height: 12),
              ],
              if (thabaalThogai > 0) ...[
                kooliTotalsRow(
                    K.koriyar.tr(context, ref), '₹${formatter.format(thabaalThogai)}'),
                const SizedBox(height: 12),
              ],
              for (final charge in piraVarivugal)
                if (charge.thogai > 0) ...[
                  kooliTotalsRow(
                    charge.peyar.isNotEmpty ? charge.peyar : K.pira.tr(context, ref),
                    '₹${formatter.format(charge.thogai)}',
                  ),
                  const SizedBox(height: 12),
                ],
              kooliTotalsRow(
                K.mothaEdai.tr(context, ref),
                '${totals.mothaEdai.toStringAsFixed(3)} Kg',
                labelWeight: FontWeight.w600,
                valueWeight: FontWeight.w700,
              ),
              const SizedBox(height: 12),
              if (setharamGrams > 0) ...[
                kooliTotalsRow('+ ${K.chaedhaaram.tr(context, ref)}',
                    '${setharamGrams == setharamGrams.truncateToDouble() ? setharamGrams.toInt() : setharamGrams} g'),
                const SizedBox(height: 12),
              ],
              Divider(height: 1, color: cs.onSurface.withValues(alpha: 0.08)),
              const SizedBox(height: 12),
              // Grand Total
              LayoutBuilder(
                  builder: (context, constraints) {
                    return FittedBox(
                      fit: BoxFit.scaleDown,
                      child: ConstrainedBox(
                        constraints: BoxConstraints(minWidth: constraints.maxWidth),
                        child: Row(
                          mainAxisAlignment: MainAxisAlignment.spaceBetween,
                          children: [
                            Text(K.motham.tr(context, ref),
                                style: tt.titleLarge?.copyWith(
                                  fontWeight: FontWeight.w700,
                                  color: cs.onSurface,
                                )),
                            const SizedBox(width: 12),
                            Text(
                              '₹${formatter.format(totals.perumMothangal)}',
                              style: TextStyle(
                                fontSize: 24,
                                fontWeight: FontWeight.w800,
                                color: cs.onSurface,
                              ),
                            ),
                          ],
                        ),
                      ),
                    );
                  }
              ),
            ],
          ),
        ),
      ),
    );
  }
}
