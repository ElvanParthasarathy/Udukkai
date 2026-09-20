import 'package:elvan_niril/src/adippadai/tharavuru/uruvugal.dart';
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:intl/intl.dart';
import 'dart:convert';
import 'package:elvan_niril/src/cheyalpaadugal/niril_podhu/kaatchi/paarvai/paarvai_udhavi.dart';
import 'package:elvan_niril/src/cheyalpaadugal/niril_podhu/kaatchi/paarvai/react_palam_maatri.dart';
import 'dart:io';

import 'package:elvan_niril/src/adippadai/mozhiyaakkam/k.dart';
import '../../../../adippadai/mozhiyaakkam/mozhi_vazhanguthi.dart';
import '../../../../adippadai/oru_mozhi/oru_mozhi_vaangunar_udhavi.dart';
import '../../../niril_podhu/tharavuru/seluthi_vagai.dart';
import '../../../amaippugal/tharavu/pattu_niruvana_tharavugal_provider.dart';
import '../../../../adippadai/elvan_navil_ezhuthen/elvan_navil_ezhuthen.dart';
import 'elvan_paarvai_oadu.dart';
import '../../../amaippugal/tharavu/niruvana_tharavugal.dart';
import '../../../../adippadai/nilaimai/seyali_nilaimai.dart';
import '../../../../adippadai/tharavuru/seyali_murai.dart';

class PatrucheettuPaarvai extends ConsumerWidget {
  const PatrucheettuPaarvai({
    super.key,
    required this.patru,
    required this.achuMozhi,
    required this.profile,
    required this.onEdit,
  });

  final PatrugalTharavuru patru;
  final String achuMozhi;
  final NiruvanaTharavugal? profile;
  final VoidCallback onEdit;

  static final _dateFormat = DateFormat('dd/MM/yyyy');
  static final _currencyFormat =
      NumberFormat.currency(locale: 'en_IN', symbol: '₹');
  
  static const MethodChannel _printChannel = MethodChannel('com.elvan.niril/print');

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final p = patru;

    final mudhanmaiPeyar = OruMozhiVaangunarUdhavi.mudhanmaiPeyarFromMap(
      p.vaangunarPeyar.cast<String, dynamic>(), 
      achuMozhi
    );
        
    final isDark = Theme.of(context).brightness == Brightness.dark;
    final mode = SeluthiVagaiX.fromStored(p.seluthumMurai);

    return ElvanPaarvaiOadu(
      title: '${K.patrucheettu.tr(context, ref)} #${p.patruEn}',
      onEdit: onEdit,
      onPrint: () async {
        if (Platform.isAndroid || Platform.isIOS) {
          final amountInWords = ElvanNavilEzhuthen.convertToMozhiMap(p.thogai);
          
          // English name for bilingual receipts
          final englishPeyar = OruMozhiVaangunarUdhavi.mudhanmaiPeyarFromMap(
            p.vaangunarPeyar.cast<String, dynamic>(), 
            'en'
          );
          
          final receiptJson = {
            'id': p.id,
            'receiptNo': p.patruEn,
            'date': p.patruNaal.toIso8601String(),
            'clientName': mudhanmaiPeyar,
            'clientNameEn': englishPeyar != mudhanmaiPeyar ? englishPeyar : null,
            'amount': p.thogai,
            'paymentMode': p.seluthumMurai,
            'referenceNo': p.parivarthanaiEn,
            'note': p.ullkurippu,
            'againstInvoice': p.vanakkam,
          };
          
          NiruvanaTharavugal? updatedProfile;
          if (profile != null) {
            updatedProfile = await PaarvaiUdhavi.convertProfileImagesToBase64(profile!);
          }
          final profileJson = updatedProfile != null
              ? ReactPalamMaatri.profileToReact(updatedProfile)
              : <String, dynamic>{};
          
          final appMode = ref.read(appModeProvider);
          final receiptType = appMode == AppMode.coolie ? 'COOLIE' : 'GST';
          
          try {
            await _printChannel.invokeMethod('printReceipt', {
              'receiptJson': jsonEncode(receiptJson),
              'profileJson': jsonEncode(profileJson),
              'isDark': isDark,
              'receiptType': receiptType,
            });
          } catch (e) {
            debugPrint("Failed to launch receipt: $e");
            if (context.mounted) {
              ScaffoldMessenger.of(context).showSnackBar(
                SnackBar(content: Text('Failed to launch receipt view: $e')),
              );
            }
          }
        }
      },
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          // Customer & Date Header
          Container(
            padding: const EdgeInsets.all(24),
            decoration: BoxDecoration(
              color: isDark 
                ? Colors.white.withValues(alpha: 0.05) 
                : Colors.black.withValues(alpha: 0.02),
              borderRadius: BorderRadius.circular(16),
            ),
            child: Row(
              mainAxisAlignment: MainAxisAlignment.spaceBetween,
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Expanded(
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      Text(
                        mudhanmaiPeyar,
                        style: const TextStyle(
                          fontSize: 20,
                          fontWeight: FontWeight.w700,
                          letterSpacing: -0.2,
                        ),
                      ),

                    ],
                  ),
                ),
                Column(
                  crossAxisAlignment: CrossAxisAlignment.end,
                  children: [
                    Text(
                      _dateFormat.format(p.patruNaal),
                      style: const TextStyle(
                        fontSize: 16,
                        fontWeight: FontWeight.w600,
                      ),
                    ),
                    if (mode != null) ...[
                      const SizedBox(height: 4),
                      Container(
                        padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 4),
                        decoration: BoxDecoration(
                          color: mode.badgeColor(isDark).withValues(alpha: 0.15),
                          borderRadius: BorderRadius.circular(6),
                        ),
                        child: Text(
                          mode.label(context, ref),
                          style: TextStyle(
                            fontSize: 12,
                            fontWeight: FontWeight.w700,
                            color: mode.badgeColor(isDark),
                          ),
                        ),
                      ),
                    ],
                  ],
                ),
              ],
            ),
          ),
          
          const SizedBox(height: 24),
          
          // Totals
          Align(
            alignment: Alignment.center,
            child: Container(
              width: double.infinity,
              padding: const EdgeInsets.all(32),
              decoration: BoxDecoration(
                color: isDark ? Colors.green.withValues(alpha: 0.1) : Colors.green.withValues(alpha: 0.05),
                borderRadius: BorderRadius.circular(16),
                border: Border.all(
                  color: isDark ? Colors.green.withValues(alpha: 0.3) : Colors.green.withValues(alpha: 0.2),
                )
              ),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.center,
                children: [
                  Text(
                    K.perumMotham.tr(context, ref),
                    style: TextStyle(
                      fontSize: 16,
                      color: isDark ? Colors.green.shade200 : Colors.green.shade800,
                    ),
                  ),
                  const SizedBox(height: 8),
                  Text(
                    _currencyFormat.format(p.thogai),
                    style: TextStyle(
                      fontSize: 36,
                      fontWeight: FontWeight.w800,
                      color: isDark ? Colors.green.shade300 : Colors.green.shade700,
                    ),
                  ),
                ],
              ),
            ),
          ),
          
          if (p.ullkurippu.isNotEmpty) ...[
            const SizedBox(height: 24),
            Text(
              K.kurippu.tr(context, ref),
              style: const TextStyle(
                fontSize: 18,
                fontWeight: FontWeight.w700,
              ),
            ),
            const SizedBox(height: 8),
            Container(
              width: double.infinity,
              padding: const EdgeInsets.all(16),
              decoration: BoxDecoration(
                border: Border.all(
                  color: isDark ? Colors.white24 : Colors.black12,
                ),
                borderRadius: BorderRadius.circular(12),
              ),
              child: Text(
                p.ullkurippu,
                style: const TextStyle(
                  fontSize: 15,
                ),
              ),
            ),
          ],
          
          const SizedBox(height: 48),
        ],
      ),
    );
  }
}
