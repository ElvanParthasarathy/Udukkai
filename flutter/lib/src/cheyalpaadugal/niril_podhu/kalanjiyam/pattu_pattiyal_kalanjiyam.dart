import 'package:drift/drift.dart';
import '../../../adippadai/tharavuthalam/pattu_tharavuthalam.dart';
import '../../../adippadai/tharavuru/uruvugal.dart';
import 'pattiyal_kalanjiyam.dart';

class PattuPattiyalKalanjiyam implements PattiyalKalanjiyam {
  static int getCurrentFinYear() {
    final now = DateTime.now();
    if (now.month >= 4) {
      return now.year;
    }
    return now.year - 1;
  }
  final PattuDatabase _db;

  PattuPattiyalKalanjiyam(this._db);

  PattiyalTharavuru _mapToDomain(PattuPattiyalEntry entry) {
    return PattiyalTharavuru(
      id: entry.id,
      niruvanamId: entry.niruvanamId,
      patrucheettuEn: entry.patrucheettuEn,
      finYear: entry.finYear,
      vanakkam: entry.vanakkam,
      pattiyalVagai: entry.pattiyalVagai,
      vaangunarId: entry.vaangunarId,
      vaangunarPeyar: Map<String, String>.from(entry.vaangunarPeyar as Map),
      vaangunarMunvari: Map<String, String>.from(entry.vaangunarMunvari as Map),
      pattiyalNaal: entry.pattiyalNaal,
      tharavugal: entry.tharavugal,
      mothaThogai: entry.mothaThogai,
      thallupadi: entry.thallupadi,
      variThogai: entry.variThogai,
      variTharavugal: entry.variTharavugal,
      podhuThallupadiMathippu: entry.podhuThallupadiMathippu,
      podhuThallupadiVagai: entry.podhuThallupadiVagai,
      podhuThallupadiThogai: entry.podhuThallupadiThogai,
      // Default / empty values for Kooli-specific fields since Pattu doesn't have them
      mothaEdai: 0.0,
      setharamGrams: 0.0,
      thabaalThogai: 0.0,
      ahimsaPattuThogai: 0.0,
      piravariVugal: '[]',
      // Pattu-specific fields
      sonthaViruppangal: entry.sonthaViruppangal,
      nibandhanaigal: entry.nibandhanaigal,
      ullkurippu: entry.ullkurippu,
      // Default / empty for bank info (handled in profile for Pattu)
      vangiTharavugal: '{}',
      createdAt: entry.createdAt,
      updatedAt: entry.updatedAt,
      isDeleted: entry.isDeleted,
      deletedAt: entry.deletedAt,
    );
  }

  PattuPattiyalTableCompanion _mapToCompanion(PattiyalTharavuru tharavuru) {
    return PattuPattiyalTableCompanion(
      niruvanamId: Value(tharavuru.niruvanamId),
      patrucheettuEn: Value(tharavuru.patrucheettuEn),
      finYear: Value(tharavuru.finYear),
      vanakkam: Value(tharavuru.vanakkam),
      pattiyalVagai: Value(tharavuru.pattiyalVagai),
      vaangunarId: Value(tharavuru.vaangunarId),
      vaangunarPeyar: Value(tharavuru.vaangunarPeyar.cast<String, String>()),
      vaangunarMunvari: Value(tharavuru.vaangunarMunvari.cast<String, String>()),
      pattiyalNaal: Value(tharavuru.pattiyalNaal),
      tharavugal: Value(tharavuru.tharavugal),
      mothaThogai: Value(tharavuru.mothaThogai),
      thallupadi: Value(tharavuru.thallupadi),
      podhuThallupadiMathippu: Value(tharavuru.podhuThallupadiMathippu),
      podhuThallupadiVagai: Value(tharavuru.podhuThallupadiVagai),
      podhuThallupadiThogai: Value(tharavuru.podhuThallupadiThogai),
      variThogai: Value(tharavuru.variThogai),
      variTharavugal: Value(tharavuru.variTharavugal),
      sonthaViruppangal: Value(tharavuru.sonthaViruppangal),
      nibandhanaigal: Value(tharavuru.nibandhanaigal),
      ullkurippu: Value(tharavuru.ullkurippu),
      updatedAt: Value(DateTime.now()),
    );
  }

  @override
  Stream<List<PattiyalTharavuru>> watchPattiyalgal() {
    return (_db.select(_db.pattuPattiyalTable)
          ..where((t) => t.isDeleted.equals(false))
          ..orderBy([
            (t) => OrderingTerm.desc(t.pattiyalNaal),
            (t) => OrderingTerm.desc(t.vanakkam),
          ]))
        .watch()
        .map((list) => list.map(_mapToDomain).toList());
  }

  @override
  Future<List<PattiyalTharavuru>> getPattiyalgal() async {
    final list = await (_db.select(_db.pattuPattiyalTable)
          ..where((t) => t.isDeleted.equals(false))
          ..orderBy([
            (t) => OrderingTerm.desc(t.pattiyalNaal),
            (t) => OrderingTerm.desc(t.vanakkam),
          ]))
        .get();
    return list.map(_mapToDomain).toList();
  }

  @override
  Future<PattiyalTharavuru?> getById(int id) async {
    final entry = await (_db.select(_db.pattuPattiyalTable)
          ..where((t) => t.id.equals(id))
          ..where((t) => t.isDeleted.equals(false)))
        .getSingleOrNull();
    return entry != null ? _mapToDomain(entry) : null;
  }

  @override
  Future<int> createPattiyal(PattiyalTharavuru tharavuru) async {
    return _db.into(_db.pattuPattiyalTable).insert(_mapToCompanion(tharavuru));
  }

  @override
  Future<void> updatePattiyal(int id, PattiyalTharavuru tharavuru) async {
    await (_db.update(_db.pattuPattiyalTable)..where((t) => t.id.equals(id)))
        .write(_mapToCompanion(tharavuru));
  }

  @override
  Future<void> deletePattiyal(int id) async {
    await (_db.update(_db.pattuPattiyalTable)..where((t) => t.id.equals(id))).write(
      PattuPattiyalTableCompanion(
        isDeleted: const Value(true),
        deletedAt: Value(DateTime.now()),
      ),
    );
  }

  @override
  Future<void> bulkDeletePattiyalgal(List<int> ids) async {
    await (_db.update(_db.pattuPattiyalTable)..where((t) => t.id.isIn(ids))).write(
      PattuPattiyalTableCompanion(
        isDeleted: const Value(true),
        deletedAt: Value(DateTime.now()),
      ),
    );
  }

  @override
  Future<void> restorePattiyal(int id) async {
    await (_db.update(_db.pattuPattiyalTable)..where((t) => t.id.equals(id))).write(
      const PattuPattiyalTableCompanion(
        isDeleted: Value(false),
        deletedAt: Value(null),
      ),
    );
  }

  @override
  Future<void> deleteAllPattiyalgal() async {
    await _db.delete(_db.pattuPattiyalTable).go();
  }

  @override
  Stream<List<PattiyalTharavuru>> watchDeletedPattiyalgal() {
    return (_db.select(_db.pattuPattiyalTable)
          ..where((t) => t.isDeleted.equals(true))
          ..orderBy([(t) => OrderingTerm.desc(t.deletedAt)]))
        .watch()
        .map((list) => list.map(_mapToDomain).toList());
  }

  @override
  Future<List<PattiyalTharavuru>> getDeletedPattiyalgal() async {
    final list = await (_db.select(_db.pattuPattiyalTable)
          ..where((t) => t.isDeleted.equals(true))
          ..orderBy([(t) => OrderingTerm.desc(t.deletedAt)]))
        .get();
    return list.map(_mapToDomain).toList();
  }

  @override
  Future<int> purgeExpiredPattiyalgal({int days = 30}) async {
    final cutoff = DateTime.now().subtract(Duration(days: days));
    return (_db.delete(_db.pattuPattiyalTable)
          ..where((t) => t.isDeleted.equals(true))
          ..where((t) => t.deletedAt.isSmallerOrEqualValue(cutoff)))
        .go();
  }

  @override
  Future<int> getNextVanakkam(int? niruvanamId, int finYear) async {
    final query = _db.selectOnly(_db.pattuPattiyalTable)
      ..addColumns([_db.pattuPattiyalTable.vanakkam.max()])
      ..where(_db.pattuPattiyalTable.finYear.equals(finYear));

    if (niruvanamId != null) {
      query.where(_db.pattuPattiyalTable.niruvanamId.equals(niruvanamId));
    } else {
      query.where(_db.pattuPattiyalTable.niruvanamId.isNull());
    }

    final result = await query.getSingle();
    final maxVal = result.read(_db.pattuPattiyalTable.vanakkam.max());
    return (maxVal ?? 0) + 1;
  }

  @override
  String formatPattiyalEn(String prefix, int vanakkam) {
    final padded = vanakkam < 10
        ? vanakkam.toString().padLeft(2, '0')
        : vanakkam.toString();
    return '$prefix-$padded';
  }

  @override
  Future<bool> isPattiyalEnDuplicate(
    int? niruvanamId,
    int finYear,
    String pattiyalEn, {
    int? excludeId,
  }) async {
    final query = _db.select(_db.pattuPattiyalTable)
      ..where((t) => t.finYear.equals(finYear))
      ..where((t) => t.patrucheettuEn.upper().equals(pattiyalEn.toUpperCase()));

    if (niruvanamId != null) {
      query.where((t) => t.niruvanamId.equals(niruvanamId));
    } else {
      query.where((t) => t.niruvanamId.isNull());
    }

    if (excludeId != null) {
      query.where((t) => t.id.isNotValue(excludeId));
    }

    final existing = await query.get();
    return existing.isNotEmpty;
  }
}
