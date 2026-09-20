import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import 'package:elvan_niril/src/adippadai/mozhiyaakkam/k.dart';
import '../../../../adippadai/mozhiyaakkam/mozhi_vazhanguthi.dart';
import '../../../../adippadai/tharavuru/uruvugal.dart';
import '../../../amaippugal/tharavu/pattu_niruvana_tharavugal_provider.dart';
import '../thiruthi/pattiyal/niril_pattu_pattiyal_thiruthi.dart';
import '../../../niril_podhu/kalanjiyam/vaangunar_nilaimai.dart';
import 'package:flutter/services.dart';
import 'package:flutter/foundation.dart';
import '../../../../adippadai/achadippu/achadippu_html_uruvakki.dart';
import '../../vadivangal/pattu_achadippu_html_uruvakki.dart';
import '../../../../adippadai/iru_mozhi/iru_mozhi_vazhanguthigal.dart';
import '../../../../adippadai/oru_mozhi/oru_mozhi_vaangunar_udhavi.dart';
import '../../../niril_podhu/kaatchi/paarvai/elvan_paarvai_oadu.dart';
import 'package:intl/intl.dart';

import 'package:flutter/services.dart';
import 'dart:convert';
import 'package:elvan_niril/src/cheyalpaadugal/niril_podhu/kaatchi/paarvai/paarvai_udhavi.dart';
import 'package:elvan_niril/src/cheyalpaadugal/niril_podhu/kaatchi/paarvai/react_palam_maatri.dart';

const _printChannel = MethodChannel('com.elvan.niril/print');

Future<void> _handlePrint(dynamic pattiyal, dynamic profile, WidgetRef ref, bool isBilingual, String mudhanmaiLang, String irandaamLang, bool isDark) async {
  try {
    // Fetch full client details from the database if vaangunarId exists
    VaangunarTharavuru? client;
    if (pattiyal.vaangunarId != null) {
      final kalanjiyam = ref.read(vaangunarKalanjiyamProvider);
      client = await kalanjiyam.getVaangunarById(pattiyal.vaangunarId!);
    }

    final pattiyalJson = <String, dynamic>{
      ...pattiyal.toMap(),
      '_client': client != null ? {
        'name': client.peyar,
        'name_Tamil': client.peyar['ta'] ?? '',
        'name_English': client.peyar['en'] ?? '',
        'mugavari': client.mugavari['ta'] ?? client.mugavari['en'] ?? '',
        'mugavari_Tamil': client.mugavari['ta'] ?? '',
        'mugavari_English': client.mugavari['en'] ?? '',
        'oor': client.oor['ta'] ?? client.oor['en'] ?? '',
        'oor_Tamil': client.oor['ta'] ?? '',
        'oor_English': client.oor['en'] ?? '',
        'maavattam': client.maavattam['ta'] ?? client.maavattam['en'] ?? '',
        'maavattam_Tamil': client.maavattam['ta'] ?? '',
        'maavattam_English': client.maavattam['en'] ?? '',
        'maanilam': client.maanilam['ta'] ?? client.maanilam['en'] ?? '',
        'maanilam_Tamil': client.maanilam['ta'] ?? '',
        'maanilam_English': client.maanilam['en'] ?? '',
        'country': client.naadu['ta'] ?? client.naadu['en'] ?? '',
        'country_Tamil': client.naadu['ta'] ?? '',
        'country_English': client.naadu['en'] ?? '',
        'pin': client.anjalKuriyeedu,
        'gstin': client.gstin,
        'email': client.minnanjal,
        'tholaipesi': client.tholaipaesi,
      } : null,
    };

    final convertedProfile = profile != null 
        ? await PaarvaiUdhavi.convertProfileImagesToBase64(profile) 
        : null;

    final profileJsonConverted = convertedProfile != null 
        ? ReactPalamMaatri.profileToReact(convertedProfile)
        : <String, dynamic>{};
    await _printChannel.invokeMethod('printInvoice', {
      'invoiceJson': jsonEncode(pattiyalJson),
      'profileJson': jsonEncode(profileJsonConverted),
      'isDark': isDark,
      'invoiceType': 'SILK',
    });
  } catch (e) {
    debugPrint("Failed to launch invoice: $e");
  }
}

/// பட்டு பட்டியல் பார்வை — Silk Invoice View Screen
///
/// Shows a PDF preview of the invoice.
/// The preview IS the final PDF — what you see is what gets exported.
class PattuPattiyalPaarvai extends ConsumerWidget {
  const PattuPattiyalPaarvai({
    super.key,
    required this.pattiyal,
  });

  final PattiyalTharavuru pattiyal;

  static final _dateFormat = DateFormat('dd/MM/yyyy');
  static final _currencyFormat = NumberFormat.currency(locale: 'en_IN', symbol: '₹');

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    // Get profile data specific to this bill, or fallback to active if missing
    final profile = pattiyal.niruvanamId != null 
        ? ref.watch(pattuNiruvanaTharavugalByIdProvider(pattiyal.niruvanamId))
        : ref.watch(pattuNiruvanaTharavugalProvider);
        
    final isDark = Theme.of(context).brightness == Brightness.dark;
    final mudhanmaiLang = ref.watch(silkMudhanmaiMozhiProvider);
    final isBilingual = ref.watch(bilingualProvider);
    final irandaamLang = ref.watch(silkThunaiMozhiProvider);

    final p = pattiyal;
    final mudhanmaiPeyar = OruMozhiVaangunarUdhavi.mudhanmaiPeyarFromMap(
      p.vaangunarPeyar.cast<String, dynamic>(), 
      mudhanmaiLang
    );

    return ElvanPaarvaiOadu(
      title: '${K.pattiyal.tr(context, ref)} #${pattiyal.patrucheettuEn}',
      onEdit: () {
        Navigator.of(context).pushReplacement(
          MaterialPageRoute(
            builder: (_) => SilkInvoiceEditor(
              editingEntry: pattiyal,
            ),
          ),
        );
      },
      onPrint: () async {
        if (profile != null) {
          await _handlePrint(
            pattiyal, 
            profile, 
            ref,
            isBilingual,
            mudhanmaiLang,
            irandaamLang,
            isDark,
          );
        } else {
          ScaffoldMessenger.of(context).showSnackBar(
            const SnackBar(content: Text('Profile not found')),
          );
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
                      _dateFormat.format(p.pattiyalNaal),
                      style: const TextStyle(
                        fontSize: 16,
                        fontWeight: FontWeight.w600,
                      ),
                    ),
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
                color: isDark ? Colors.blue.withValues(alpha: 0.1) : Colors.blue.withValues(alpha: 0.05),
                borderRadius: BorderRadius.circular(16),
                border: Border.all(
                  color: isDark ? Colors.blue.withValues(alpha: 0.3) : Colors.blue.withValues(alpha: 0.2),
                )
              ),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.center,
                children: [
                  Text(
                    K.perumMotham.tr(context, ref),
                    style: TextStyle(
                      fontSize: 16,
                      color: isDark ? Colors.blue.shade200 : Colors.blue.shade800,
                    ),
                  ),
                  const SizedBox(height: 8),
                  Text(
                    _currencyFormat.format(p.mothaThogai),
                    style: TextStyle(
                      fontSize: 36,
                      fontWeight: FontWeight.w800,
                      color: isDark ? Colors.blue.shade300 : Colors.blue.shade700,
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
