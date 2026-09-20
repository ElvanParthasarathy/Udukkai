import 'package:drift/drift.dart';
import '../../../adippadai/tharavuthalam/pattu_tharavuthalam.dart';
import '../../../adippadai/tharavuru/uruvugal.dart';
import 'vaangunar_kalanjiyam.dart';

class PattuVaangunarKalanjiyam implements VaangunarKalanjiyam {
  final PattuDatabase _db;

  PattuVaangunarKalanjiyam(this._db);

  VaangunarTharavuru _mapToDomain(PattuVaangunarEntry entry) {
    return VaangunarTharavuru(
      id: entry.id,
      peyar: Map<String, String>.from((entry.peyar as Map?) ?? {}),
      mugavari: Map<String, String>.from((entry.mugavari as Map?) ?? {}),
      oor: Map<String, String>.from((entry.oor as Map?) ?? {}),
      maavattam: Map<String, String>.from((entry.maavattam as Map?) ?? {}),
      maanilam: Map<String, String>.from((entry.maanilam as Map?) ?? {}),
      naadu: Map<String, String>.from((entry.naadu as Map?) ?? {}),
      velinaadMugavari: Map<String, String>.from((entry.velinaadMugavari as Map?) ?? {}),
      anjalKuriyeedu: entry.anjalKuriyeedu,
      gstin: entry.gstin,
      minnanjal: entry.minnanjal,
      tholaipaesi: entry.tholaipaesi,
      createdAt: entry.createdAt,
      updatedAt: entry.updatedAt,
      isDeleted: entry.isDeleted,
      deletedAt: entry.deletedAt,
    );
  }

  @override
  Stream<List<VaangunarTharavuru>> watchAllVaangunargal() {
    return (_db.select(_db.pattuVaangunarTable)
          ..where((t) => t.isDeleted.equals(false))
          ..orderBy([(t) => OrderingTerm.desc(t.updatedAt)]))
        .watch()
        .map((list) => list.map(_mapToDomain).toList());
  }

  @override
  Future<List<VaangunarTharavuru>> getAllVaangunargal() async {
    final list = await (_db.select(_db.pattuVaangunarTable)
          ..where((t) => t.isDeleted.equals(false))
          ..orderBy([(t) => OrderingTerm.desc(t.updatedAt)]))
        .get();
    return list.map(_mapToDomain).toList();
  }

  @override
  Future<VaangunarTharavuru?> getVaangunarById(int id) async {
    final entry = await (_db.select(_db.pattuVaangunarTable)
          ..where((t) => t.id.equals(id))
          ..where((t) => t.isDeleted.equals(false)))
        .getSingleOrNull();
    return entry != null ? _mapToDomain(entry) : null;
  }

  @override
  Future<int> saveVaangunar(VaangunarTharavuru tharavuru) async {
    final companion = PattuVaangunarTableCompanion(
      peyar: Value(tharavuru.peyar.cast<String, String>()),
      mugavari: Value(tharavuru.mugavari.cast<String, String>()),
      oor: Value(tharavuru.oor.cast<String, String>()),
      maavattam: Value(tharavuru.maavattam.cast<String, String>()),
      maanilam: Value(tharavuru.maanilam.cast<String, String>()),
      naadu: Value(tharavuru.naadu.cast<String, String>()),
      velinaadMugavari: Value(tharavuru.velinaadMugavari.cast<String, String>()),
      anjalKuriyeedu: Value(tharavuru.anjalKuriyeedu),
      gstin: Value(tharavuru.gstin),
      minnanjal: Value(tharavuru.minnanjal),
      tholaipaesi: Value(tharavuru.tholaipaesi),
      updatedAt: Value(DateTime.now()),
    );

    if (tharavuru.id > 0) {
      await (_db.update(_db.pattuVaangunarTable)..where((t) => t.id.equals(tharavuru.id)))
          .write(companion);
      return tharavuru.id;
    } else {
      return _db.into(_db.pattuVaangunarTable).insert(companion);
    }
  }

  @override
  Future<void> deleteVaangunar(int id) async {
    await (_db.update(_db.pattuVaangunarTable)..where((t) => t.id.equals(id))).write(
      PattuVaangunarTableCompanion(
        isDeleted: const Value(true),
        deletedAt: Value(DateTime.now()),
      ),
    );
  }

  @override
  Future<void> bulkDeleteVaangunargal(List<int> ids) async {
    await (_db.update(_db.pattuVaangunarTable)..where((t) => t.id.isIn(ids))).write(
      PattuVaangunarTableCompanion(
        isDeleted: const Value(true),
        deletedAt: Value(DateTime.now()),
      ),
    );
  }

  @override
  Future<void> restoreVaangunar(int id) async {
    await (_db.update(_db.pattuVaangunarTable)..where((t) => t.id.equals(id))).write(
      const PattuVaangunarTableCompanion(
        isDeleted: Value(false),
        deletedAt: Value(null),
      ),
    );
  }

  @override
  Future<void> bulkRestoreVaangunargal(List<int> ids) async {
    await (_db.update(_db.pattuVaangunarTable)..where((t) => t.id.isIn(ids))).write(
      const PattuVaangunarTableCompanion(
        isDeleted: Value(false),
        deletedAt: Value(null),
      ),
    );
  }

  @override
  Future<void> permanentDeleteVaangunar(int id) async {
    await (_db.delete(_db.pattuVaangunarTable)..where((t) => t.id.equals(id))).go();
  }

  @override
  Stream<List<VaangunarTharavuru>> watchDeletedVaangunargal() {
    return (_db.select(_db.pattuVaangunarTable)
          ..where((t) => t.isDeleted.equals(true))
          ..orderBy([(t) => OrderingTerm.desc(t.deletedAt)]))
        .watch()
        .map((list) => list.map(_mapToDomain).toList());
  }

  @override
  Future<int> purgeExpiredVaangunargal({int days = 30}) async {
    final cutoff = DateTime.now().subtract(Duration(days: days));
    return (_db.delete(_db.pattuVaangunarTable)
          ..where((t) => t.isDeleted.equals(true))
          ..where((t) => t.deletedAt.isSmallerOrEqualValue(cutoff)))
        .go();
  }

  @override
  Future<void> deleteAllVaangunargal() async {
    await _db.delete(_db.pattuVaangunarTable).go();
  }
}
