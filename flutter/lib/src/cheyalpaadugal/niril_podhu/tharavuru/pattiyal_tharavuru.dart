import 'dart:convert';

import 'package:flutter/foundation.dart';

// =============================================================================
// pattiyal_tharavuru.dart
// ---------------------------------------------------------------------------
// பட்டியல் தரவுரு — Invoice line-item data models
//
// Contains immutable value classes used across Silk (பட்டு) and Coolie (கூலி)
// modes for representing individual line items, additional charges, and
// computed totals.
// =============================================================================

/// ஒரு பட்டு வரிசை உருபடி — Single line item for Silk mode invoices.
///
/// Holds product reference, quantity, rate, tax percentage and discount info.
/// Computed getters provide base amount and discount amount for the row.
class PattuUrupadi {
  /// பொருள் அடையாளம் — Product ID reference (nullable for new/unsaved rows)
  final String? porulId;

  /// பொருள் பெயர் — Product name (bilingual JSON string)
  final String porulPeyar;

  /// பொருள் பெயர் (English) — Product name in English
  final String porulPeyarEn;

  /// இருமொழி அகராதி — Full bilingual dictionary snapshot
  final Map<String, dynamic> mozhiMap;

  /// HSN குறியீடு — Harmonized System Nomenclature code
  final String hsnKuriyeedu;

  /// அளவு — Quantity
  final double alavu;

  /// அலகு — Unit of measurement (Nos, Kg, Mtr, etc.)
  final String alagu;

  /// விலை — Rate per unit
  final double vilai;

  /// வரி விழுக்காடு — Tax percentage
  final double variVizhukkaadu;

  /// தள்ளுபடி — Discount value (interpreted based on [thallupadiVagai])
  final double thallupadi;

  /// தள்ளுபடி வகை — Discount type: `'percentage'` or `'amount'`
  final String thallupadiVagai;

  const PattuUrupadi({
    this.porulId,
    this.porulPeyar = '',
    this.porulPeyarEn = '',
    this.hsnKuriyeedu = '',
    this.alavu = 1,
    this.alagu = 'Nos',
    this.vilai = 0,
    this.variVizhukkaadu = 0,
    this.thallupadi = 0,
    this.thallupadiVagai = '%',
    this.mozhiMap = const {},
  });

  /// அடிப்படைத் தொகை — Row amount before tax/discount (quantity × rate).
  double get adippadaiThogai => alavu * vilai;

  /// தள்ளுபடித் தொகை — Resolved discount amount for this row.
  ///
  /// When [thallupadiVagai] is `'percentage'`, computes discount as a
  /// percentage of [adippadaiThogai]. Otherwise returns flat [thallupadi].
  double get thallupadiThogai => thallupadiVagai == '%'
      ? adippadaiThogai * (thallupadi / 100)
      : thallupadi;

  /// Creates a copy with the given fields replaced.
  PattuUrupadi copyWith({
    String? porulId,
    String? porulPeyar,
    String? porulPeyarEn,
    String? hsnKuriyeedu,
    double? alavu,
    String? alagu,
    double? vilai,
    double? variVizhukkaadu,
    double? thallupadi,
    String? thallupadiVagai,
    Map<String, dynamic>? mozhiMap,
  }) {
    return PattuUrupadi(
      porulId: porulId ?? this.porulId,
      porulPeyar: porulPeyar ?? this.porulPeyar,
      porulPeyarEn: porulPeyarEn ?? this.porulPeyarEn,
      hsnKuriyeedu: hsnKuriyeedu ?? this.hsnKuriyeedu,
      alavu: alavu ?? this.alavu,
      alagu: alagu ?? this.alagu,
      vilai: vilai ?? this.vilai,
      variVizhukkaadu: variVizhukkaadu ?? this.variVizhukkaadu,
      thallupadi: thallupadi ?? this.thallupadi,
      thallupadiVagai: thallupadiVagai ?? this.thallupadiVagai,
      mozhiMap: mozhiMap ?? this.mozhiMap,
    );
  }

  /// Serialises this item to a JSON-compatible map.
  Map<String, dynamic> toJson() {
    return {
      'porulId': porulId,
      'porulPeyar': porulPeyar,
      'porulPeyarEn': porulPeyarEn,
      'hsnKuriyeedu': hsnKuriyeedu,
      'alavu': alavu,
      'alagu': alagu,
      'vilai': vilai,
      'variVizhukkaadu': variVizhukkaadu,
      'thallupadi': thallupadi,
      'thallupadiVagai': thallupadiVagai,
      'thallupadiThogai': thallupadiThogai,
      'mozhiMap': mozhiMap,
    };
  }

  /// Deserialises from a JSON-compatible map.
  factory PattuUrupadi.fromJson(Map<String, dynamic> json) {
    return PattuUrupadi(
      porulId: json['porulId'] as String?,
      porulPeyar: (json['porulPeyar'] as String?) ?? '',
      porulPeyarEn: (json['porulPeyarEn'] as String?) ?? '',
      hsnKuriyeedu: (json['hsnKuriyeedu'] as String?) ?? '',
      alavu: (json['alavu'] as num?)?.toDouble() ?? 1,
      alagu: (json['alagu'] as String?) ?? 'Nos',
      vilai: (json['vilai'] as num?)?.toDouble() ?? 0,
      variVizhukkaadu: (json['variVizhukkaadu'] as num?)?.toDouble() ?? 0,
      thallupadi: (json['thallupadi'] as num?)?.toDouble() ?? 0,
      thallupadiVagai: (json['thallupadiVagai'] as String?) ?? '%',
      mozhiMap: (json['mozhiMap'] as Map<String, dynamic>?) ?? {},
    );
  }

  @override
  String toString() =>
      'PattuUrupadi(porulPeyar: $porulPeyar, alavu: $alavu, vilai: $vilai)';

  @override
  bool operator ==(Object other) =>
      identical(this, other) ||
      other is PattuUrupadi &&
          runtimeType == other.runtimeType &&
          porulId == other.porulId &&
          porulPeyar == other.porulPeyar &&
          porulPeyarEn == other.porulPeyarEn &&
          hsnKuriyeedu == other.hsnKuriyeedu &&
          alavu == other.alavu &&
          alagu == other.alagu &&
          vilai == other.vilai &&
          variVizhukkaadu == other.variVizhukkaadu &&
          thallupadi == other.thallupadi &&
          thallupadiVagai == other.thallupadiVagai &&
          mapEquals(mozhiMap, other.mozhiMap);

  @override
  int get hashCode => Object.hash(
        porulId,
        porulPeyar,
        porulPeyarEn,
        hsnKuriyeedu,
        alavu,
        alagu,
        vilai,
        variVizhukkaadu,
        thallupadi,
        thallupadiVagai,
        mozhiMap,
      );
}

// =============================================================================

/// ஒரு கூலி வரிசை உருபடி — Single line item for Coolie mode invoices.
///
/// Holds product reference, weight (KG) and rate per KG.
/// The row total is **truncated** (floored), not rounded.
class KooliUrupadi {
  /// பொருள் அடையாளம் — Product ID reference (nullable for new/unsaved rows)
  final String? porulId;

  /// பொருள் பெயர் — பொருள் பெயர் (primary language, usually Tamil)
  final String porulPeyar;

  /// பொருள் பெயர் (English) — Product name in English for subtitle display
  final String porulPeyarEn;

  /// இருமொழி அகராதி — Full bilingual dictionary snapshot
  final Map<String, dynamic> mozhiMap;

  /// எடை — Weight in kilograms
  final double edai;

  /// விலை — Rate per kilogram
  final double vilai;

  const KooliUrupadi({
    this.porulId,
    this.porulPeyar = '',
    this.porulPeyarEn = '',
    this.edai = 0,
    this.vilai = 0,
    this.mozhiMap = const {},
  });

  /// வரிசைத் தொகை — Row total = floor(edai × vilai).
  ///
  /// Truncated (not rounded) per Coolie mode business rule.
  int get varisaiThogai => (edai * vilai).floor();

  /// Creates a copy with the given fields replaced.
  KooliUrupadi copyWith({
    String? porulId,
    String? porulPeyar,
    String? porulPeyarEn,
    double? edai,
    double? vilai,
    Map<String, dynamic>? mozhiMap,
  }) {
    return KooliUrupadi(
      porulId: porulId ?? this.porulId,
      porulPeyar: porulPeyar ?? this.porulPeyar,
      porulPeyarEn: porulPeyarEn ?? this.porulPeyarEn,
      edai: edai ?? this.edai,
      vilai: vilai ?? this.vilai,
      mozhiMap: mozhiMap ?? this.mozhiMap,
    );
  }

  /// Serialises this item to a JSON-compatible map.
  Map<String, dynamic> toJson() {
    return {
      'porulId': porulId,
      'porulPeyar': porulPeyar,
      'porulPeyarEn': porulPeyarEn,
      'edai': edai,
      'vilai': vilai,
      'mozhiMap': mozhiMap,
    };
  }

  /// Deserialises from a JSON-compatible map.
  factory KooliUrupadi.fromJson(Map<String, dynamic> json) {
    return KooliUrupadi(
      porulId: json['porulId'] as String?,
      porulPeyar: (json['porulPeyar'] as String?) ?? '',
      porulPeyarEn: (json['porulPeyarEn'] as String?) ?? '',
      edai: (json['edai'] as num?)?.toDouble() ?? 0,
      vilai: (json['vilai'] as num?)?.toDouble() ?? 0,
      mozhiMap: (json['mozhiMap'] as Map<String, dynamic>?) ?? {},
    );
  }

  @override
  String toString() =>
      'KooliUrupadi(porulPeyar: $porulPeyar, edai: $edai, vilai: $vilai)';

  @override
  bool operator ==(Object other) =>
      identical(this, other) ||
      other is KooliUrupadi &&
          runtimeType == other.runtimeType &&
          porulId == other.porulId &&
          porulPeyar == other.porulPeyar &&
          porulPeyarEn == other.porulPeyarEn &&
          edai == other.edai &&
          vilai == other.vilai &&
          mapEquals(mozhiMap, other.mozhiMap);

  @override
  int get hashCode => Object.hash(
        porulId,
        porulPeyar,
        porulPeyarEn,
        edai,
        vilai,
        mozhiMap,
      );
}

// =============================================================================

/// பிற வரிவு — Additional charge line for Coolie mode invoices.
///
/// A simple name + amount pair (e.g. Loading, Transport, etc.).
class PiraVarivu {
  /// பெயர் — Charge name
  final String peyar;

  /// தொகை — Charge amount
  final double thogai;

  const PiraVarivu({
    this.peyar = '',
    this.thogai = 0,
  });

  /// Creates a copy with the given fields replaced.
  PiraVarivu copyWith({
    String? peyar,
    double? thogai,
  }) {
    return PiraVarivu(
      peyar: peyar ?? this.peyar,
      thogai: thogai ?? this.thogai,
    );
  }

  /// Serialises this charge to a JSON-compatible map.
  Map<String, dynamic> toJson() {
    return {
      'peyar': peyar,
      'thogai': thogai,
    };
  }

  /// Deserialises from a JSON-compatible map.
  factory PiraVarivu.fromJson(Map<String, dynamic> json) {
    return PiraVarivu(
      peyar: (json['peyar'] as String?) ?? '',
      thogai: (json['thogai'] as num?)?.toDouble() ?? 0,
    );
  }

  @override
  String toString() => 'PiraVarivu(peyar: $peyar, thogai: $thogai)';

  @override
  bool operator ==(Object other) =>
      identical(this, other) ||
      other is PiraVarivu &&
          runtimeType == other.runtimeType &&
          peyar == other.peyar &&
          thogai == other.thogai;

  @override
  int get hashCode => Object.hash(peyar, thogai);
}

// =============================================================================

/// பட்டு மொத்தங்கள் — Computed totals result for Silk mode invoices.
///
/// Produced by the `PattuKanakku` calculator; holds subtotal, discount,
/// tax breakdown (CGST/SGST/IGST), round-off, and final grand total.
class PattuMothangal {
  /// அடிப்படை மொத்தங்கள் — Raw subtotal (sum of all row base amounts)
  final double adippadaiMothangal;

  /// தள்ளுபடி மொத்தங்கள் — Total discount across all rows
  final double thallupadiMothangal;

  /// CGST தொகை — Central GST amount
  final double cgst;

  /// SGST தொகை — State GST amount
  final double sgst;

  /// IGST தொகை — Integrated GST amount
  final double igst;

  /// வரி மொத்தங்கள் — Total tax (cgst + sgst + igst)
  final double variMothangal;

  /// சுற்று ஆஃப் — Round-off adjustment
  final double suttruOff;

  /// மொத்த மொத்தங்கள் — Final payable total
  final double mothaMothangal;

  const PattuMothangal({
    this.adippadaiMothangal = 0,
    this.thallupadiMothangal = 0,
    this.cgst = 0,
    this.sgst = 0,
    this.igst = 0,
    this.variMothangal = 0,
    this.suttruOff = 0,
    this.mothaMothangal = 0,
  });

  /// வரி JSON — Returns the tax breakdown as a JSON-compatible map.
  ///
  /// Useful for storing the tax split alongside the invoice record.
  Map<String, dynamic> variToJson() => {
        'cgst': cgst,
        'sgst': sgst,
        'igst': igst,
      };

  @override
  String toString() =>
      'PattuMothangal(subtotal: $adippadaiMothangal, '
      'discount: $thallupadiMothangal, tax: $variMothangal, '
      'roundOff: $suttruOff, total: $mothaMothangal)';

  @override
  bool operator ==(Object other) =>
      identical(this, other) ||
      other is PattuMothangal &&
          runtimeType == other.runtimeType &&
          adippadaiMothangal == other.adippadaiMothangal &&
          thallupadiMothangal == other.thallupadiMothangal &&
          cgst == other.cgst &&
          sgst == other.sgst &&
          igst == other.igst &&
          variMothangal == other.variMothangal &&
          suttruOff == other.suttruOff &&
          mothaMothangal == other.mothaMothangal;

  @override
  int get hashCode => Object.hash(
        adippadaiMothangal,
        thallupadiMothangal,
        cgst,
        sgst,
        igst,
        variMothangal,
        suttruOff,
        mothaMothangal,
      );
}

// =============================================================================

/// கூலி மொத்தங்கள் — Computed totals result for Coolie mode invoices.
///
/// Produced by the `KooliKanakku` calculator; holds items subtotal,
/// total weight, and grand total (items + additional charges).
class KooliMothangal {
  /// அடிப்படை மொத்தங்கள் — Items subtotal (sum of all row totals)
  final double adippadaiMothangal;

  /// மொத்த எடை — Total weight in kilograms across all rows
  final double mothaEdai;

  /// பெரும் மொத்தங்கள் — Grand total including additional charges
  final double perumMothangal;

  const KooliMothangal({
    this.adippadaiMothangal = 0,
    this.mothaEdai = 0,
    this.perumMothangal = 0,
  });

  @override
  String toString() =>
      'KooliMothangal(subtotal: $adippadaiMothangal, '
      'totalKg: $mothaEdai, grandTotal: $perumMothangal)';

  @override
  bool operator ==(Object other) =>
      identical(this, other) ||
      other is KooliMothangal &&
          runtimeType == other.runtimeType &&
          adippadaiMothangal == other.adippadaiMothangal &&
          mothaEdai == other.mothaEdai &&
          perumMothangal == other.perumMothangal;

  @override
  int get hashCode =>
      Object.hash(adippadaiMothangal, mothaEdai, perumMothangal);
}

// =============================================================================

/// பட்டியல் உதவிகள் — Static JSON serialisation helpers for list types.
///
/// Provides round-trip conversion between JSON strings and typed lists
/// for [PattuUrupadi], [KooliUrupadi], and [PiraVarivu].
class PattiyalUthavigal {
  PattiyalUthavigal._(); // Prevent instantiation

  // ---------------------------------------------------------------------------
  // PattuUrupadi list helpers
  // ---------------------------------------------------------------------------

  /// Decodes a JSON string into a list of [PattuUrupadi].
  ///
  /// Returns an empty list if [json] is empty or not a valid JSON array.
  static List<PattuUrupadi> pattuListFromJson(String json) {
    if (json.isEmpty) return [];
    try {
      final List<dynamic> decoded = jsonDecode(json) as List<dynamic>;
      return decoded
          .map((e) => PattuUrupadi.fromJson(e as Map<String, dynamic>))
          .toList();
    } catch (e) {
      debugPrint('⚠️ PattuUrupadi JSON pakuppaaivil pizhai: $e');
      return [];
    }
  }

  /// Encodes a list of [PattuUrupadi] into a JSON string.
  static String pattuListToJson(List<PattuUrupadi> items) {
    return jsonEncode(items.map((e) => e.toJson()).toList());
  }

  // ---------------------------------------------------------------------------
  // KooliUrupadi list helpers
  // ---------------------------------------------------------------------------

  /// Decodes a JSON string into a list of [KooliUrupadi].
  ///
  /// Returns an empty list if [json] is empty or not a valid JSON array.
  static List<KooliUrupadi> kooliListFromJson(String json) {
    if (json.isEmpty) return [];
    try {
      final List<dynamic> decoded = jsonDecode(json) as List<dynamic>;
      return decoded
          .map((e) => KooliUrupadi.fromJson(e as Map<String, dynamic>))
          .toList();
    } catch (e) {
      debugPrint('⚠️ KooliUrupadi JSON pakuppaaivil pizhai: $e');
      return [];
    }
  }

  /// Encodes a list of [KooliUrupadi] into a JSON string.
  static String kooliListToJson(List<KooliUrupadi> items) {
    return jsonEncode(items.map((e) => e.toJson()).toList());
  }

  // ---------------------------------------------------------------------------
  // PiraVarivu list helpers
  // ---------------------------------------------------------------------------

  /// Decodes a JSON string into a list of [PiraVarivu].
  ///
  /// Returns an empty list if [json] is empty or not a valid JSON array.
  static List<PiraVarivu> piraVarivuListFromJson(String json) {
    if (json.isEmpty) return [];
    try {
      final List<dynamic> decoded = jsonDecode(json) as List<dynamic>;
      return decoded
          .map((e) => PiraVarivu.fromJson(e as Map<String, dynamic>))
          .toList();
    } catch (e) {
      debugPrint('⚠️ PiraVarivu JSON pakuppaaivil pizhai: $e');
      return [];
    }
  }

  /// Encodes a list of [PiraVarivu] into a JSON string.
  static String piraVarivuListToJson(List<PiraVarivu> items) {
    return jsonEncode(items.map((e) => e.toJson()).toList());
  }
}
