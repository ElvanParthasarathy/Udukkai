import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:intl/intl.dart';
import 'package:elvan_niril/src/adippadai/mozhiyaakkam/k.dart';
import '../../../../../../adippadai/mozhiyaakkam/mozhi_vazhanguthi.dart';

import '../../../../../../koorugal/podhu_koorugal/elvan_thiruthi_attai_kooru.dart';
import '../../../../../niril_podhu/tharavuru/pattiyal_tharavuru.dart';

/// Displays invoice totals — subtotal, discount, taxes, round off, and grand total.
class PattuMothangalKooru extends ConsumerWidget {
  const PattuMothangalKooru({super.key, required this.totals});

  final PattuMothangal totals;

  static final NumberFormat _inrFormat = NumberFormat.currency(
    locale: 'en_IN',
    symbol: '₹',
    decimalDigits: 2,
  );

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
              mainAxisSize: MainAxisSize.min,
              children: [
                _totalsRow(K.ulmotham.tr(context, ref), totals.adippadaiMothangal, cs, tt),
                if (totals.thallupadiMothangal > 0) ...[
                  const SizedBox(height: 6),
                  _totalsRow(K.thallupadi.tr(context, ref), -totals.thallupadiMothangal, cs, tt,
                      color: Colors.red),
                ],
                if (totals.cgst > 0) ...[
                  const SizedBox(height: 6),
                  _totalsRow('CGST', totals.cgst, cs, tt),
                ],
                if (totals.sgst > 0) ...[
                  const SizedBox(height: 6),
                  _totalsRow('SGST', totals.sgst, cs, tt),
                ],
                if (totals.igst > 0) ...[
                  const SizedBox(height: 6),
                  _totalsRow('IGST', totals.igst, cs, tt),
                ],
                if (totals.suttruOff != 0) ...[
                  const SizedBox(height: 6),
                  _totalsRow(K.chuttruOppu.tr(context, ref), totals.suttruOff, cs, tt),
                ],
                Padding(
                  padding: const EdgeInsets.symmetric(vertical: 10),
                  child: Divider(height: 1, color: cs.onSurface.withValues(alpha: 0.08)),
                ),
                LayoutBuilder(
                  builder: (context, constraints) {
                    return FittedBox(
                      fit: BoxFit.scaleDown,
                      child: ConstrainedBox(
                        constraints: BoxConstraints(minWidth: constraints.maxWidth),
                        child: Row(
                          mainAxisAlignment: MainAxisAlignment.spaceBetween,
                          children: [
                            Text(K.perumMotham.tr(context, ref),
                                style: tt.titleMedium?.copyWith(
                                    fontWeight: FontWeight.w700,
                                    color: cs.onSurface)),
                            const SizedBox(width: 8),
                            Text(_inrFormat.format(totals.mothaMothangal),
                                style: TextStyle(
                                    fontSize: 24,
                                    fontWeight: FontWeight.w800,
                                    color: cs.onSurface)),
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

  Widget _totalsRow(
      String label, double amount, ColorScheme cs, TextTheme tt,
      {Color? color}) {
    return LayoutBuilder(
      builder: (context, constraints) {
        return FittedBox(
          fit: BoxFit.scaleDown,
          child: ConstrainedBox(
            constraints: BoxConstraints(minWidth: constraints.maxWidth),
            child: Row(
              mainAxisAlignment: MainAxisAlignment.spaceBetween,
              children: [
                Text(label, style: tt.bodyMedium?.copyWith(color: cs.onSurfaceVariant)),
                const SizedBox(width: 12),
                Text(_inrFormat.format(amount),
                    style: tt.bodyMedium
                        ?.copyWith(fontWeight: FontWeight.w500, color: color ?? cs.onSurface)),
              ],
            ),
          ),
        );
      }
    );
  }
}
