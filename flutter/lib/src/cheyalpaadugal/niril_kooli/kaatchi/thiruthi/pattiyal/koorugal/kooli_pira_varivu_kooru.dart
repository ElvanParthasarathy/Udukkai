import 'package:flutter/material.dart';
import 'package:flutter/cupertino.dart';
import 'package:flutter/services.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import 'package:elvan_niril/src/adippadai/mozhiyaakkam/k.dart';
import '../../../../../../adippadai/mozhiyaakkam/mozhi_vazhanguthi.dart';

import '../../../../../../koorugal/podhu_koorugal/elvan_thiruthi_attai_kooru.dart';
import '../../../../../../koorugal/ulleedugal/elvan_ulleedu_vadivamaippigal.dart';
import '../../../../../../koorugal/ulleedugal/elvan_thiruthi_ulleedu.dart';
import '../../../../../niril_podhu/tharavuru/pattiyal_tharavuru.dart';

/// Builds a dynamic "other charge" row inside an ElvanUrupadiAttai.
class KooliPiraVarivuKooru extends ConsumerWidget {
  final int index;
  final PiraVarivu charge;
  final ValueChanged<PiraVarivu> onUpdated;
  final VoidCallback onDeleted;
  final VoidCallback onRecalculate;

  const KooliPiraVarivuKooru({
    super.key,
    required this.index,
    required this.charge,
    required this.onUpdated,
    required this.onDeleted,
    required this.onRecalculate,
  });

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final cs = Theme.of(context).colorScheme;
    final tt = Theme.of(context).textTheme;

    return Padding(
      padding: const EdgeInsets.only(bottom: 12),
      child: Column(
        children: [
          Row(
            mainAxisAlignment: MainAxisAlignment.spaceBetween,
            crossAxisAlignment: CrossAxisAlignment.end,
            children: [
              Padding(
                padding: const EdgeInsets.only(left: 24, bottom: 0),
                child: Text(
                  '${K.pira.tr(context, ref)} #${index + 1}',
                  style: tt.labelMedium?.copyWith(
                    fontWeight: FontWeight.w600,
                    color: cs.onSurface.withValues(alpha: 0.5),
                  ),
                ),
              ),
              Padding(
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
            ],
          ),
          ElvanUrupadiAttai(
            padding: const EdgeInsets.all(16),
            child: Row(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Expanded(
                  flex: 2,
                  child: ElvanThiruthiUlleedu(
                    label: K.kattanaPeyar.tr(context, ref),
                    initialValue: charge.peyar,
                    onChanged: (v) {
                      onUpdated(charge.copyWith(peyar: v));
                    },
                  ),
                ),
                const SizedBox(width: 12),
                Expanded(
                  child: ElvanThiruthiUlleedu(
                    label: '${K.motham.tr(context, ref)} (₹)',
                    initialValue: charge.thogai > 0 ? charge.thogai.toString() : '',
                    keyboardType: const TextInputType.numberWithOptions(decimal: true),
                    inputFormatters: ElvanVadivamaippigal.thasamamEnngal,
                    onChanged: (v) {
                      onUpdated(
                          charge.copyWith(thogai: double.tryParse(v) ?? 0));
                      onRecalculate();
                    },
                  ),
                ),
              ],
            ),
          ),
        ],
      ),
    );
  }
}
