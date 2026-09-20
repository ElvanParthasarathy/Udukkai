import 'package:elvan_niril/src/adippadai/tharavuru/uruvugal.dart';
import '../../../cheyalpaadugal/amaippugal/tharavu/pattu_niruvana_tharavugal_provider.dart';
import '../../../cheyalpaadugal/amaippugal/tharavu/kooli_niruvana_tharavugal_provider.dart';
import 'dart:convert';

import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:intl/intl.dart';

import '../../../adippadai/tharavuru/seyali_murai.dart';
import '../../amaippugal/tharavu/niruvana_tharavugal.dart';
import '../../amaippugal/tharavu/niruvana_tharavugal_provider.dart';
import '../../niril_podhu/kalanjiyam/kooli_pattiyal_kalanjiyam.dart';
import '../../niril_podhu/kalanjiyam/pattu_pattiyal_kalanjiyam.dart';
import '../../niril_podhu/kalanjiyam/kooli_patru_kalanjiyam.dart';
import '../../niril_podhu/kalanjiyam/pattu_patru_kalanjiyam.dart';
import '../../niril_podhu/kalanjiyam/kooli_porul_kalanjiyam.dart';
import '../../niril_podhu/kalanjiyam/pattu_porul_kalanjiyam.dart';
import '../../niril_podhu/kalanjiyam/kooli_vaangunar_kalanjiyam.dart';
import '../../niril_podhu/kalanjiyam/pattu_vaangunar_kalanjiyam.dart';
import 'sodhanai_tharavugal.dart';

class SodhanaiTharavuUruvakki {
  /// Seeds business profile data for the given mode.
  static Future<void> seedData(WidgetRef ref, AppMode mode) async {
    final dynamic notifier = mode == AppMode.coolie 
        ? ref.read(kooliNiruvanaTharavugalListProvider.notifier)
        : ref.read(pattuNiruvanaTharavugalListProvider.notifier);
    
    // Clear existing data for this mode
    await notifier.clearProfile();

    final sourceList = mode == AppMode.silk ? mockSilkProfiles : mockCoolieProfiles;

    for (final data in sourceList) {
      final profile = NiruvanaTharavugal(
        mudhanMozhi: data['mudhanMozhi'] ?? 'ta',
        thunaiMozhi: data['thunaiMozhi'] ?? 'en',
        iruMozhi: data['iruMozhi'] ?? true,
        niruvanathinPeyar: {
          'ta': data['niruvanathinPeyar_ta'] ?? '',
          'en': data['niruvanathinPeyar_en'] ?? '',
        },
        kurumPeyar: data['kurumPeyar'] ?? '',
        tholaipaesi1: data['tholaipesi_1'] ?? '',
        tholaipaesi2: data['tholaipesi_2'] ?? '',
        minnanjal: data['minnanjal'] ?? '',
        gstin: data['gstin'] ?? '',
        mugavari: {
          'ta': data['mugavari_ta'] ?? '',
          'en': data['mugavari_en'] ?? '',
        },
        oor: {
          'ta': data['oor_ta'] ?? '',
          'en': data['oor_en'] ?? '',
        },
        maavattam: {
          'ta': data['maavattam_ta'] ?? '',
          'en': data['maavattam_en'] ?? '',
        },
        maanilam: {
          'ta': data['maanilam_ta'] ?? '',
          'en': data['maanilam_en'] ?? '',
        },
        naadu: {
          'ta': data['naadu_ta'] ?? '',
          'en': data['naadu_en'] ?? '',
        },
        anjalKuriyeedu: data['anjalKuriyeedu'] ?? '',
        vangiPeyar: {
          'ta': data['vangiPeyar_ta'] ?? '',
          'en': data['vangiPeyar_en'] ?? '',
        },
        kilai: {
          'ta': data['kilai_ta'] ?? '',
          'en': data['kilai_en'] ?? '',
        },
        vangiKanakku: data['vangiKanakku'] ?? '',
        ifsc: data['ifsc'] ?? '',
        oavuru: data['oavuru'] ?? '',
        agalaOavuru: data['agalaOavuru'] ?? '',
        thalaippuVadivu: data['thalaippuVadivu'] ?? 'small',
        kaiyoppam: data['kaiyoppam'] ?? '',
        oppamPeyar: data['oppamPeyar'] ?? '',
        adaimozhi: {
          'ta': data['adaimozhi_ta'] ?? '',
          'en': data['adaimozhi_en'] ?? '',
        },
        upiId: data['upiId'] ?? '',
        thoatraNiram: data['thoatraNiram'] ?? '',
      );

      await notifier.createProfile(profile);
    }
  }

  /// Seeds Porul (Product) and Vaangunar (Customer) mock data for the given mode.
  static Future<void> seedPorulAndVaangunar(WidgetRef ref, {required AppMode mode}) async {
    final porulRepo = mode == AppMode.coolie 
        ? KooliPorulKalanjiyam(ref.read(kooliDatabaseProvider)) 
        : PattuPorulKalanjiyam(ref.read(pattuDatabaseProvider));
    final vaangunarRepo = mode == AppMode.coolie 
        ? KooliVaangunarKalanjiyam(ref.read(kooliDatabaseProvider)) 
        : PattuVaangunarKalanjiyam(ref.read(pattuDatabaseProvider));

    if (mode == AppMode.coolie) {
      // ── Seed Coolie Products ──
      for (final data in mockCooliePorulgal) {
        await porulRepo.savePorul(PorulTharavuru(id: 0, porulPeyar: {
            'ta': data['peyar_ta'] ?? '',
            'en': data['peyar_en'] ?? '',
          }, hsnCode: '', vilai: 0.0, variVeetham: 0.0, alagu: '', alavuVagai: '', createdAt: DateTime.now(), updatedAt: DateTime.now(), isDeleted: false));
      }

      // ── Seed Coolie Customers ──
      for (final data in mockCoolieVaangunargal) {
        await vaangunarRepo.saveVaangunar(VaangunarTharavuru(id: 0, peyar: {
            'ta': data['peyar_ta'] ?? '',
            'en': data['peyar_en'] ?? '',
          }, oor: {
            'ta': data['oor_ta'] ?? '',
            'en': data['oor_en'] ?? '',
          }, mugavari: {'ta': '', 'en': ''}, maavattam: {'ta': '', 'en': ''}, maanilam: {'ta': '', 'en': ''}, naadu: {'ta': '', 'en': ''}, velinaadMugavari: {'ta': '', 'en': ''}, anjalKuriyeedu: '', gstin: '', minnanjal: '', tholaipaesi: '', createdAt: DateTime.now(), updatedAt: DateTime.now(), isDeleted: false));
      }
    } else if (mode == AppMode.silk) {
      // ── Seed Silk Products ──
      for (final data in mockSilkPorulgal) {
        await porulRepo.savePorul(PorulTharavuru(id: 0, porulPeyar: {
            'ta': data['peyar_ta'] ?? '',
            'en': data['peyar_en'] ?? '',
          }, hsnCode: data['hsnCode'] ?? '', vilai: (data['vilai'] as num?)?.toDouble() ?? 0.0, variVeetham: (data['variVeetham'] as num?)?.toDouble() ?? 0.0, alagu: data['alagu'] ?? 'Nos', alavuVagai: data['alavuVagai'] ?? 'quantity', createdAt: DateTime.now(), updatedAt: DateTime.now(), isDeleted: false));
      }

      // ── Seed Silk Customers ──
      for (final data in mockSilkVaangunargal) {
        await vaangunarRepo.saveVaangunar(VaangunarTharavuru(id: 0, peyar: {
            'ta': data['peyar_ta'] ?? '',
            'en': data['peyar_en'] ?? '',
          }, mugavari: {
            'ta': data['mugavari_ta'] ?? '',
            'en': data['mugavari_en'] ?? '',
          }, oor: {
            'ta': data['oor_ta'] ?? '',
            'en': data['oor_en'] ?? '',
          }, maavattam: {
            'ta': data['maavattam_ta'] ?? '',
            'en': data['maavattam_en'] ?? '',
          }, maanilam: {
            'ta': data['maanilam_ta'] ?? '',
            'en': data['maanilam_en'] ?? '',
          }, naadu: {
            'ta': data['naadu_ta'] ?? '',
            'en': data['naadu_en'] ?? '',
          }, anjalKuriyeedu: data['anjalKuriyeedu'] ?? '', gstin: data['gstin'] ?? '', velinaadMugavari: {'ta': '', 'en': ''}, minnanjal: '', tholaipaesi: '', createdAt: DateTime.now(), updatedAt: DateTime.now(), isDeleted: false));
      }
    }
  }

  /// Seeds invoice data from real Firebase export for the given mode.
  static Future<void> seedPattiyalgal(WidgetRef ref, {required AppMode mode}) async {
    final kooliDb = ref.read(kooliDatabaseProvider);
    final pattuDb = ref.read(pattuDatabaseProvider);
    final pattiyalRepo = mode == AppMode.coolie 
        ? KooliPattiyalKalanjiyam(kooliDb) 
        : PattuPattiyalKalanjiyam(pattuDb);
    final ddFormat = DateFormat('dd/MM/yyyy');

    // Helper: match customer_name (+ optional oor) against customer data
    int? matchVaangunarId(
        String customerName, List<dynamic> vaangunargal,
        {String? customerOor}) {
      if (customerOor != null && customerOor.isNotEmpty) {
        for (final v in vaangunargal) {
          if (v.peyar['ta'] == customerName &&
              v.oor['ta'] == customerOor) {
            return v.id;
          }
        }
      }
      for (final v in vaangunargal) {
        if (v.peyar['ta'] == customerName) return v.id;
      }
      return null;
    }

    // Helper: inject porulId into items JSON (as String, matching model expectation)
    String injectPorulIds(String itemsJson, List<dynamic> porulgal) {
      final items = jsonDecode(itemsJson) as List<dynamic>;
      for (final item in items) {
        final map = item as Map<String, dynamic>;
        final peyar = map['porulPeyar'] as String? ?? '';
        for (final p in porulgal) {
          if (p.porulPeyar['ta'] == peyar) {
            map['porulId'] = p.id.toString();
            map['mozhiMap'] = p.porulPeyar;
            break;
          }
        }
      }
      return jsonEncode(items);
    }

    if (mode == AppMode.coolie) {
      final coolieProfiles = await (kooliDb.select(kooliDb.kooliNiruvanaTharavugalTable)
            )
          .get();

      final Map<String, int> coolieProfileMap = {};
      for (final p in coolieProfiles) {
        if (p.kurumPeyar.isNotEmpty) {
          coolieProfileMap[p.kurumPeyar] = p.id;
        }
      }

      final coolieVaangunargal = await (kooliDb.select(kooliDb.kooliVaangunarTable)
            ..where((t) => t.isDeleted.equals(false)))
          .get();

      final cooliePorulgal = await (kooliDb.select(kooliDb.kooliPorulTable)
            ..where((t) => t.isDeleted.equals(false)))
          .get();

      // ── Coolie invoices (VRM + PVS) ──
      for (final data in mockCooliePattiyalgal) {
        final date = ddFormat.parse(data['date'] as String);
        final billNo = data['bill_no'] as String;
        final prefix = billNo.split('-').first; // 'VRM' or 'PVS'
        final vanakkamNum =
            int.tryParse(billNo.split('-').last) ?? 1;
        // Map invoice to correct profile via prefix
        final profileId = coolieProfileMap[prefix];
        final vaangunarId = matchVaangunarId(
            data['customer_name'] as String, coolieVaangunargal,
            customerOor: data['customer_oor'] as String?);
        final enrichedItems =
            injectPorulIds(data['items'] as String, cooliePorulgal);

        final vaangunar = coolieVaangunargal.where((v) => v.id == vaangunarId).firstOrNull;
        final peyarMap = vaangunar != null ? Map<String, String>.from(vaangunar.peyar) : {'ta': data['customer_name'] as String, 'en': data['customer_name_en'] as String? ?? data['customer_name'] as String};
        final munvariMap = vaangunar != null ? Map<String, String>.from(vaangunar.oor) : {'ta': data['customer_oor'] as String? ?? '', 'en': data['customer_oor'] as String? ?? ''};


        await pattiyalRepo.createPattiyal(PattiyalTharavuru(
          id: 0,
          niruvanamId: profileId,
          patrucheettuEn: billNo,
          finYear: date.year,
          vanakkam: vanakkamNum,
          pattiyalVagai: 'tax-invoice',
          vaangunarId: vaangunarId,
          vaangunarPeyar: peyarMap,
          vaangunarMunvari: munvariMap,
          pattiyalNaal: date,
          tharavugal: enrichedItems,
          mothaThogai: double.tryParse(data['grand_total'].toString()) ?? 0.0,
          thallupadi: 0.0,
          podhuThallupadiMathippu: 0.0,
          podhuThallupadiVagai: '%',
          podhuThallupadiThogai: 0.0,
          variThogai: 0.0,
          variTharavugal: '{}',
          mothaEdai: double.tryParse(data['mothaEdai'].toString()) ?? 0.0,
          setharamGrams: double.tryParse(data['setharamGrams'].toString()) ?? 0.0,
          thabaalThogai: double.tryParse(data['thabaalThogai'].toString()) ?? 0.0,
          ahimsaPattuThogai: double.tryParse(data['ahimsaPattuThogai'].toString()) ?? 0.0,
          piravariVugal: '{}',
          sonthaViruppangal: '{}',
          nibandhanaigal: '{}',
          ullkurippu: '',
          vangiTharavugal: '{}',
          createdAt: date,
          updatedAt: date,
          isDeleted: false,
        ));

      }
    } else if (mode == AppMode.silk) {
      final silkProfiles = await (pattuDb.select(pattuDb.pattuNiruvanaTharavugalTable)
            )
          .get();

      final Map<String, int> silkProfileMap = {};
      for (final p in silkProfiles) {
        if (p.kurumPeyar.isNotEmpty) {
          silkProfileMap[p.kurumPeyar] = p.id;
        }
      }

      final silkVaangunargal = await (pattuDb.select(pattuDb.pattuVaangunarTable)
            ..where((t) => t.isDeleted.equals(false)))
          .get();

      final silkPorulgal = await (pattuDb.select(pattuDb.pattuPorulTable)
            ..where((t) => t.isDeleted.equals(false)))
          .get();

      // ── Silk invoices ──
      final isoFormat = DateFormat('yyyy-MM-dd');
      for (final data in mockSilkPattiyalgal) {
        final date = isoFormat.parse(data['invoiceDate'] as String);
        final invoiceNo = data['invoiceNumber'] as String;
        final prefix = invoiceNo.split('-').first; // 'SJPS' etc.
        final vanakkamNum =
            int.tryParse(invoiceNo.split('-').last) ?? 1;
        final profileId = silkProfileMap[prefix];
        final vaangunarId = matchVaangunarId(
            data['customer_name'] as String, silkVaangunargal);
        final enrichedItems =
            injectPorulIds(data['items'] as String, silkPorulgal);

        final vaangunar = silkVaangunargal.where((v) => v.id == vaangunarId).firstOrNull;
        final peyarMap = vaangunar != null ? Map<String, String>.from(vaangunar.peyar) : {'ta': data['customer_name'] as String, 'en': data['customer_name_en'] as String? ?? data['customer_name'] as String};
        final munvariMap = vaangunar != null ? Map<String, String>.from(vaangunar.oor) : {'ta': '', 'en': ''};


        await pattiyalRepo.createPattiyal(PattiyalTharavuru(
          id: 0,
          niruvanamId: profileId,
          patrucheettuEn: invoiceNo,
          finYear: date.year,
          vanakkam: vanakkamNum,
          pattiyalVagai: 'tax-invoice',
          vaangunarId: vaangunarId,
          vaangunarPeyar: peyarMap,
          vaangunarMunvari: munvariMap,
          pattiyalNaal: date,
          tharavugal: enrichedItems,
          mothaThogai: double.tryParse(data['grandTotal'].toString()) ?? 0.0,
          thallupadi: double.tryParse(data['discountTotal'].toString()) ?? 0.0,
          podhuThallupadiMathippu: double.tryParse(data['discountTotal'].toString()) ?? 0.0,
          podhuThallupadiVagai: '%',
          podhuThallupadiThogai: double.tryParse(data['discountTotal'].toString()) ?? 0.0,
          variThogai: double.tryParse(data['taxTotal'].toString()) ?? 0.0,
          variTharavugal: '{}',
          mothaEdai: 0.0,
          setharamGrams: 0.0,
          thabaalThogai: double.tryParse(data['shippingCharge'].toString()) ?? 0.0,
          ahimsaPattuThogai: 0.0,
          piravariVugal: '{}',
          sonthaViruppangal: '{}',
          nibandhanaigal: '{}',
          ullkurippu: '',
          vangiTharavugal: '{}',
          createdAt: date,
          updatedAt: date,
          isDeleted: false,
        ));

      }
    }
  }

  static Future<void> eraseData(WidgetRef ref) async {
    // Clear business profiles
    final notifier = ref.read(niruvanaTharavugalNotifierProvider);
    await notifier.clearProfile();

    // Note: We need to clear BOTH databases.
    final kooliDb = ref.read(kooliDatabaseProvider);
    final pattuDb = ref.read(pattuDatabaseProvider);
    
    // Clear Coolie DB
    await KooliPorulKalanjiyam(kooliDb).deleteAllPorulgal();
    await KooliVaangunarKalanjiyam(kooliDb).deleteAllVaangunargal();
    await KooliPattiyalKalanjiyam(kooliDb).deleteAllPattiyalgal();
    await KooliPatruKalanjiyam(kooliDb).deleteAllPatrugal();

    // Clear Silk DB
    await PattuPorulKalanjiyam(pattuDb).deleteAllPorulgal();
    await PattuVaangunarKalanjiyam(pattuDb).deleteAllVaangunargal();
    await PattuPattiyalKalanjiyam(pattuDb).deleteAllPattiyalgal();
    await PattuPatruKalanjiyam(pattuDb).deleteAllPatrugal();
  }

  static Future<void> seedPatrugal(WidgetRef ref, {required AppMode mode}) async {
    final kooliDb = ref.read(kooliDatabaseProvider);
    final pattuDb = ref.read(pattuDatabaseProvider);
    final patruRepo = mode == AppMode.coolie 
        ? KooliPatruKalanjiyam(kooliDb) 
        : PattuPatruKalanjiyam(pattuDb);
    
    // Check if we already have receipts
    final existingPatrugal = mode == AppMode.coolie
        ? await kooliDb.select(kooliDb.kooliPatrugalTable).get()
        : await pattuDb.select(pattuDb.pattuPatrugalTable).get();
    if (existingPatrugal.isNotEmpty) return;

    final isoFormat = DateFormat('yyyy-MM-dd');

    final List<dynamic> pattiyalgal = mode == AppMode.coolie
        ? await kooliDb.select(kooliDb.kooliPattiyalTable).get()
        : await pattuDb.select(pattuDb.pattuPattiyalTable).get();
    final pattiyalEnToId = { for (var p in pattiyalgal) p.patrucheettuEn: p.id };
    final pattiyalEnToMothaThogai = { for (var p in pattiyalgal) p.patrucheettuEn: p.mothaThogai };

    List<PatruPattiyalInaippuTharavuru> buildLinks(String againstInvoice, double amount) {
      final links = <PatruPattiyalInaippuTharavuru>[];
      final invList = againstInvoice.split(',').map((e) => e.trim()).where((e) => e.isNotEmpty);
      double remaining = amount;
      for (final invEn in invList) {
        final id = pattiyalEnToId[invEn];
        if (id != null) {
          final balance = pattiyalEnToMothaThogai[invEn] ?? 0.0;
          final apply = remaining.clamp(0.0, balance);
          if (apply > 0) {
            links.add(PatruPattiyalInaippuTharavuru(pattiyalId: id, poruthiyaThogai: apply.toDouble()));
            remaining -= apply;
          }
        }
      }
      return links;
    }

    String _mapPaymentMode(String? pm) {
      if (pm == null) return 'cash';
      final lower = pm.toLowerCase();
      if (lower == 'upi') return 'upi';
      if (lower == 'bank_transfer') return 'bank_transfer';
      if (lower == 'card') return 'card';
      if (lower == 'cheque') return 'cheque';
      return 'cash';
    }

    if (mode == AppMode.coolie) {
      final profiles = await (kooliDb.select(kooliDb.kooliNiruvanaTharavugalTable))
          .get();
      final coolieProfileMap = <String, int>{};
      for (final p in profiles) {
        if (p.kurumPeyar.isNotEmpty) {
          coolieProfileMap[p.kurumPeyar] = p.id;
        }
      }

      final coolieVaangunargal = await (kooliDb.select(kooliDb.kooliVaangunarTable))
          .get();

      int? matchVaangunarId(String customerName, {String? customerOor}) {
        if (customerOor != null && customerOor.isNotEmpty) {
          for (final v in coolieVaangunargal) {
            if (v.peyar['ta'] == customerName && v.oor['ta'] == customerOor) return v.id;
          }
        }
        for (final v in coolieVaangunargal) {
          if (v.peyar['ta'] == customerName) return v.id;
        }
        return null;
      }

      // Seed Coolie Receipts
      for (final data in mockCooliePatrugal) {
        final date = isoFormat.parse(data['date'] as String);
        final patruEn = data['receiptNo'] as String; // e.g. RCP/PVS/01
        final parts = patruEn.split('/');
        final prefix = parts.length > 1 ? parts[1] : '';
        final vanakkamNum = int.tryParse(parts.last) ?? 1;

        final profileId = coolieProfileMap[prefix];
        final vaangunarId = matchVaangunarId(data['clientName'] as String);
        final links = buildLinks((data['againstInvoice'] as String?) ?? '', double.tryParse(data['amount'].toString()) ?? 0.0);
        await patruRepo.insertPatru(PatrugalTharavuru(
          id: 0,
          niruvanamId: profileId,
          patruEn: patruEn,
          vanakkam: vanakkamNum,
          finYear: date.year,
          vaangunarId: vaangunarId,
          vaangunarPeyar: {'ta': data['clientName'] as String, 'en': data['clientName'] as String},
          patruNaal: date,
          thogai: double.tryParse(data['amount'].toString()) ?? 0.0,
          seluthumMurai: _mapPaymentMode(data['paymentMode'] as String?),
          vangiPeyar: '',
          parivarthanaiEn: (data['referenceNo'] as String?) ?? '',
          ullkurippu: (data['remarks'] as String?) ?? '',
          createdAt: date,
          updatedAt: date,
          isDeleted: false,
        ), links);
      }
    } else if (mode == AppMode.silk) {
      final profiles = await (pattuDb.select(pattuDb.pattuNiruvanaTharavugalTable))
          .get();
      final silkProfileMap = <String, int>{};
      for (final p in profiles) {
        if (p.kurumPeyar.isNotEmpty) {
          silkProfileMap[p.kurumPeyar] = p.id;
        }
      }

      final silkVaangunargal = await (pattuDb.select(pattuDb.pattuVaangunarTable))
          .get();

      int? matchVaangunarId(String customerName) {
        for (final v in silkVaangunargal) {
          if (v.peyar['ta'] == customerName) return v.id;
        }
        return null;
      }

      // Seed Silk Receipts
      for (final data in mockSilkPatrugal) {
        final date = isoFormat.parse(data['date'] as String);
        final patruEn = data['receiptNo'] as String; // e.g. RCP/SJPS/04
        final parts = patruEn.split('/');
        final prefix = parts.length > 1 ? parts[1] : '';
        final vanakkamNum = int.tryParse(parts.last) ?? 1;

        final profileId = silkProfileMap[prefix];
        final vaangunarId = matchVaangunarId(data['clientName'] as String);
        final links = buildLinks((data['againstInvoice'] as String?) ?? '', double.tryParse(data['amount'].toString()) ?? 0.0);
        await patruRepo.insertPatru(PatrugalTharavuru(
          id: 0,
          niruvanamId: profileId,
          patruEn: patruEn,
          vanakkam: vanakkamNum,
          finYear: date.year,
          vaangunarId: vaangunarId,
          vaangunarPeyar: {'ta': data['clientName'] as String, 'en': data['clientName'] as String},
          patruNaal: date,
          thogai: double.tryParse(data['amount'].toString()) ?? 0.0,
          seluthumMurai: _mapPaymentMode(data['paymentMode'] as String?),
          vangiPeyar: '',
          parivarthanaiEn: (data['referenceNo'] as String?) ?? '',
          ullkurippu: (data['remarks'] as String?) ?? '',
          createdAt: date,
          updatedAt: date,
          isDeleted: false,
        ), links);
      }
    }
  }
}
