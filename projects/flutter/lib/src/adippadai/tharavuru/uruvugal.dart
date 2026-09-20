// Core Domain Models (Pure Dart) that replace Drift generated entities in the UI.

class VaangunarTharavuru {
  final int id;
  final Map<String, dynamic> peyar;
  final Map<String, dynamic> mugavari;
  final Map<String, dynamic> oor;
  final Map<String, dynamic> maavattam;
  final Map<String, dynamic> maanilam;
  final Map<String, dynamic> naadu;
  final Map<String, dynamic> velinaadMugavari;
  final String anjalKuriyeedu;
  final String gstin;
  final String minnanjal;
  final String tholaipaesi;
  final DateTime createdAt;
  final DateTime updatedAt;
  final bool isDeleted;
  final DateTime? deletedAt;

  VaangunarTharavuru({
    required this.id,
    required this.peyar,
    required this.mugavari,
    required this.oor,
    required this.maavattam,
    required this.maanilam,
    required this.naadu,
    required this.velinaadMugavari,
    required this.anjalKuriyeedu,
    required this.gstin,
    required this.minnanjal,
    required this.tholaipaesi,
    required this.createdAt,
    required this.updatedAt,
    required this.isDeleted,
    this.deletedAt,
  });
}

class PorulTharavuru {
  final int id;
  final Map<String, dynamic> porulPeyar;
  final String hsnCode;
  final double vilai;
  final double variVeetham;
  final String alavuVagai;
  final String alagu;
  final DateTime createdAt;
  final DateTime updatedAt;
  final bool isDeleted;
  final DateTime? deletedAt;

  PorulTharavuru({
    required this.id,
    required this.porulPeyar,
    required this.hsnCode,
    required this.vilai,
    required this.variVeetham,
    required this.alavuVagai,
    required this.alagu,
    required this.createdAt,
    required this.updatedAt,
    required this.isDeleted,
    this.deletedAt,
  });
}

class PattiyalTharavuru {
  final int id;
  final int? niruvanamId;
  final String patrucheettuEn;
  final int finYear;
  final int vanakkam;
  final String pattiyalVagai;

  final int? vaangunarId;
  final Map<String, dynamic> vaangunarPeyar;
  final Map<String, dynamic> vaangunarMunvari;

  final DateTime pattiyalNaal;
  final String tharavugal;

  final double mothaThogai;
  final double thallupadi;
  final double variThogai;
  final String variTharavugal;

  // Global Discount (Explicit Database Columns)
  final double podhuThallupadiMathippu;
  final String podhuThallupadiVagai;
  final double podhuThallupadiThogai;

  // Kooli-specific
  final double mothaEdai;
  final double setharamGrams;
  final double thabaalThogai;
  final double ahimsaPattuThogai;
  final String piravariVugal;

  // Silk-specific
  final String sonthaViruppangal;
  final String nibandhanaigal;
  final String ullkurippu;

  // Bank
  final String vangiTharavugal;

  final DateTime createdAt;
  final DateTime updatedAt;
  final bool isDeleted;
  final DateTime? deletedAt;

  PattiyalTharavuru({
    required this.id,
    this.niruvanamId,
    required this.patrucheettuEn,
    required this.finYear,
    required this.vanakkam,
    required this.pattiyalVagai,
    this.vaangunarId,
    required this.vaangunarPeyar,
    required this.vaangunarMunvari,
    required this.pattiyalNaal,
    required this.tharavugal,
    required this.mothaThogai,
    required this.thallupadi,
    required this.variThogai,
    required this.variTharavugal,
    required this.podhuThallupadiMathippu,
    required this.podhuThallupadiVagai,
    required this.podhuThallupadiThogai,
    required this.mothaEdai,
    required this.setharamGrams,
    required this.thabaalThogai,
    required this.ahimsaPattuThogai,
    required this.piravariVugal,
    required this.sonthaViruppangal,
    required this.nibandhanaigal,
    required this.ullkurippu,
    required this.vangiTharavugal,
    required this.createdAt,
    required this.updatedAt,
    required this.isDeleted,
    this.deletedAt,
  });

  // Helper method for legacy compatibility with UI (PatrucheettuEntry fallback)
  int get patrucheettuId => id;

  Map<String, dynamic> toMap() {
    return {
      'id': id,
      'niruvanamId': niruvanamId,
      'patrucheettuEn': patrucheettuEn,
      'finYear': finYear,
      'vanakkam': vanakkam,
      'pattiyalVagai': pattiyalVagai,
      'vaangunarId': vaangunarId,
      'vaangunarPeyar': vaangunarPeyar,
      'vaangunarMunvari': vaangunarMunvari,
      'pattiyalNaal': pattiyalNaal.toIso8601String(),
      'tharavugal': tharavugal,
      'mothaThogai': mothaThogai,
      'thallupadi': thallupadi,
      'variThogai': variThogai,
      'variTharavugal': variTharavugal,
      'podhuThallupadiMathippu': podhuThallupadiMathippu,
      'podhuThallupadiVagai': podhuThallupadiVagai,
      'podhuThallupadiThogai': podhuThallupadiThogai,
      'mothaEdai': mothaEdai,
      'setharamGrams': setharamGrams,
      'thabaalThogai': thabaalThogai,
      'ahimsaPattuThogai': ahimsaPattuThogai,
      'piravariVugal': piravariVugal,
      'sonthaViruppangal': sonthaViruppangal,
      'nibandhanaigal': nibandhanaigal,
      'ullkurippu': ullkurippu,
      'vangiTharavugal': vangiTharavugal,
      'createdAt': createdAt.toIso8601String(),
      'updatedAt': updatedAt.toIso8601String(),
      'isDeleted': isDeleted,
      'deletedAt': deletedAt?.toIso8601String(),
    };
  }
}

class PatrugalTharavuru {
  final int id;
  final int? niruvanamId;
  final String patruEn;
  final int vanakkam;
  final int finYear;
  final int? vaangunarId;
  final Map<String, dynamic> vaangunarPeyar;
  final DateTime patruNaal;
  final double thogai;
  final String seluthumMurai;
  final String vangiPeyar;
  final String parivarthanaiEn;
  final String ullkurippu;
  final DateTime createdAt;
  final DateTime updatedAt;
  final bool isDeleted;
  final DateTime? deletedAt;

  PatrugalTharavuru({
    required this.id,
    this.niruvanamId,
    required this.patruEn,
    required this.vanakkam,
    required this.finYear,
    this.vaangunarId,
    required this.vaangunarPeyar,
    required this.patruNaal,
    required this.thogai,
    required this.seluthumMurai,
    required this.vangiPeyar,
    required this.parivarthanaiEn,
    required this.ullkurippu,
    required this.createdAt,
    required this.updatedAt,
    required this.isDeleted,
    this.deletedAt,
  });
}

class PatruPattiyalInaippuTharavuru {
  final int patruId;
  final int pattiyalId;
  final double poruthiyaThogai;

  const PatruPattiyalInaippuTharavuru({
    this.patruId = 0,
    required this.pattiyalId,
    required this.poruthiyaThogai,
  });
}
