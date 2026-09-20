import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:intl/intl.dart';

import 'package:elvan_niril/src/adippadai/mozhiyaakkam/k.dart';
import '../../../../adippadai/mozhiyaakkam/mozhi_vazhanguthi.dart';
import '../../../../adippadai/tharavuru/uruvugal.dart';
import '../../../../adippadai/oru_mozhi/oru_mozhi_vazhanguthigal.dart';
import '../../../../adippadai/oru_mozhi/oru_mozhi_vaangunar_udhavi.dart';
import '../../../niril_podhu/kaatchi/paarvai/elvan_paarvai_oadu.dart';
import '../thiruthi/pattiyal/niril_kooli_pattiyal_thiruthi.dart';
import '../../../niril_podhu/tharavuru/pattiyal_tharavuru.dart';
import 'package:flutter/services.dart';
import 'package:flutter/foundation.dart';
import '../../../amaippugal/tharavu/kooli_niruvana_tharavugal_provider.dart';

import 'dart:convert';
import 'package:elvan_niril/src/cheyalpaadugal/niril_podhu/kaatchi/paarvai/paarvai_udhavi.dart';
import 'package:elvan_niril/src/cheyalpaadugal/niril_podhu/kaatchi/paarvai/react_palam_maatri.dart';

const _printChannel = MethodChannel('com.elvan.niril/print');

Future<void> _handlePrint(dynamic pattiyal, dynamic profile, bool isDark) async {
  try {
    List<dynamic> items = [];
    try {
      items = jsonDecode(pattiyal.tharavugal);
    } catch (_) {}

    final mappedItems = items.map((it) => {
      'item_name': it['porulPeyar'] ?? '',
      'item_name_en': it['porulPeyarEn'] ?? '',
      'kg': it['edai'] ?? 0,
      'coolie': it['vilai'] ?? 0,
    }).toList();

    final pattiyalJson = <String, dynamic>{
      ...pattiyal.toMap(),
      'bill_no': pattiyal.patrucheettuEn,
      'date': DateFormat('dd/MM/yyyy').format(pattiyal.pattiyalNaal),
      'customer_name': pattiyal.vaangunarPeyar['ta'] ?? pattiyal.vaangunarPeyar['en'] ?? '',
      'customer_name_en': pattiyal.vaangunarPeyar['en'] ?? '',
      'address': pattiyal.vaangunarMunvari['ta'] ?? pattiyal.vaangunarMunvari['en'] ?? '',
      'address_en': pattiyal.vaangunarMunvari['en'] ?? '',
      'items': mappedItems,
      'setharam_grams': pattiyal.setharamGrams,
      'courier_rs': pattiyal.thabaalThogai,
      'ahimsa_silk_rs': pattiyal.ahimsaPattuThogai,
      'custom_charge_name': pattiyal.piravariVugal,
    };

    final convertedProfile = profile != null && profile is! Map
        ? await PaarvaiUdhavi.convertProfileImagesToBase64(profile) 
        : null;

    final profileJsonConverted = convertedProfile != null 
        ? ReactPalamMaatri.profileToReact(convertedProfile)
        : <String, dynamic>{};
    await _printChannel.invokeMethod('printInvoice', {
      'invoiceJson': jsonEncode(pattiyalJson),
      'profileJson': jsonEncode(profileJsonConverted),
      'isDark': isDark,
      'invoiceType': 'COOLIE',
    });
  } catch (e) {
    debugPrint("Failed to launch invoice: $e");
  }
}

class KooliPattiyalPaarvai extends ConsumerWidget {
  const KooliPattiyalPaarvai({
    super.key,
    required this.pattiyal,
  });

  final PattiyalTharavuru pattiyal;

  static final _dateFormat = DateFormat('dd/MM/yyyy');
  static final _currencyFormat =
      NumberFormat.currency(locale: 'en_IN', symbol: '₹');

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final kooliAchuMozhi = ref.watch(kooliAchuMozhiProvider);
    final p = pattiyal;

    final mudhanmaiPeyar = OruMozhiVaangunarUdhavi.mudhanmaiPeyarFromMap(
      p.vaangunarPeyar.cast<String, dynamic>(), 
      kooliAchuMozhi
    );
    final mudhanmaiOor = OruMozhiVaangunarUdhavi.mudhanmaiPeyarFromMap(
      p.vaangunarMunvari.cast<String, dynamic>(), 
      kooliAchuMozhi
    );
    
    final isDark = Theme.of(context).brightness == Brightness.dark;
    final items = PattiyalUthavigal.kooliListFromJson(p.tharavugal);

    return ElvanPaarvaiOadu(
      title: '${K.pattiyal.tr(context, ref)} #${p.patrucheettuEn}',
      onEdit: () {
        Navigator.of(context).pushReplacement(
          MaterialPageRoute(
            builder: (_) => CoolieInvoiceEditor(
              editingEntry: pattiyal,
            ),
          ),
        );
      },
      onPrint: () {
        final profiles = ref.read(kooliNiruvanaTharavugalListProvider);
        final defaultProfile = ref.read(kooliNiruvanaTharavugalProvider);
        final profile = pattiyal.niruvanamId != null && profiles.isNotEmpty
            ? profiles.firstWhere((p) => p.id == pattiyal.niruvanamId, orElse: () => defaultProfile ?? profiles.first)
            : defaultProfile;
        _handlePrint(pattiyal, profile ?? {}, isDark);
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
                      if (mudhanmaiOor.isNotEmpty) ...[
                        const SizedBox(height: 4),
                        Text(
                          mudhanmaiOor,
                          style: TextStyle(
                            fontSize: 15,
                            color: isDark ? Colors.white70 : Colors.black87,
                          ),
                        ),
                      ],
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
                    const SizedBox(height: 4),
                    Container(
                      padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 4),
                      decoration: BoxDecoration(
                        color: (isDark ? Colors.white : Colors.black).withValues(alpha: 0.1),
                        borderRadius: BorderRadius.circular(6),
                      ),
                      child: Text(
                        K.nirilKooli.tr(context, ref),
                        style: const TextStyle(
                          fontSize: 12,
                          fontWeight: FontWeight.w700,
                        ),
                      ),
                    ),
                  ],
                ),
              ],
            ),
          ),
          
          const SizedBox(height: 24),
          
          // Items Table
          Text(
            K.porutkal.tr(context, ref),
            style: const TextStyle(
              fontSize: 18,
              fontWeight: FontWeight.w700,
            ),
          ),
          const SizedBox(height: 12),
          Container(
            decoration: BoxDecoration(
              border: Border.all(
                color: isDark ? Colors.white24 : Colors.black12,
              ),
              borderRadius: BorderRadius.circular(12),
            ),
            child: ListView.separated(
              shrinkWrap: true,
              physics: const NeverScrollableScrollPhysics(),
              itemCount: items.length,
              separatorBuilder: (context, index) => Divider(
                height: 1, 
                color: isDark ? Colors.white24 : Colors.black12,
              ),
              itemBuilder: (context, index) {
                final item = items[index];
                final itemName = item.mozhiMap[kooliAchuMozhi]?.toString().isNotEmpty == true
                    ? item.mozhiMap[kooliAchuMozhi].toString()
                    : (item.porulPeyar.isNotEmpty ? item.porulPeyar : '-');
                
                return Padding(
                  padding: const EdgeInsets.all(16),
                  child: Row(
                    children: [
                      Container(
                        width: 24,
                        height: 24,
                        alignment: Alignment.center,
                        decoration: BoxDecoration(
                          color: isDark ? Colors.white12 : Colors.black12,
                          shape: BoxShape.circle,
                        ),
                        child: Text(
                          '${index + 1}',
                          style: const TextStyle(fontSize: 12, fontWeight: FontWeight.bold),
                        ),
                      ),
                      const SizedBox(width: 12),
                      Expanded(
                        child: Text(
                          itemName,
                          style: const TextStyle(
                            fontSize: 15,
                            fontWeight: FontWeight.w600,
                          ),
                        ),
                      ),
                      Text(
                        '${item.edai.toStringAsFixed(1)} Kg',
                        style: TextStyle(
                          fontSize: 14,
                          color: isDark ? Colors.white70 : Colors.black87,
                        ),
                      ),
                      const SizedBox(width: 24),
                      Text(
                        _currencyFormat.format(item.varisaiThogai),
                        style: const TextStyle(
                          fontSize: 15,
                          fontWeight: FontWeight.w600,
                        ),
                      ),
                    ],
                  ),
                );
              },
            ),
          ),

          const SizedBox(height: 24),
          
          // Totals
          Align(
            alignment: Alignment.centerRight,
            child: Container(
              width: 300,
              padding: const EdgeInsets.all(16),
              decoration: BoxDecoration(
                color: isDark ? Colors.white.withValues(alpha: 0.1) : Colors.black.withValues(alpha: 0.05),
                borderRadius: BorderRadius.circular(12),
              ),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Row(
                    mainAxisAlignment: MainAxisAlignment.spaceBetween,
                    children: [
                      Text(
                        K.perumMotham.tr(context, ref),
                        style: const TextStyle(fontSize: 16),
                      ),
                      Text(
                        _currencyFormat.format(p.mothaThogai),
                        style: const TextStyle(
                          fontSize: 22,
                          fontWeight: FontWeight.w800,
                        ),
                      ),
                    ],
                  ),
                ],
              ),
            ),
          ),
          
          const SizedBox(height: 48),
        ],
      ),
    );
  }
}
