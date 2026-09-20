import 'package:drift/drift.dart';
import '../../../adippadai/tharavuthalam/pattu_tharavuthalam.dart';
import '../../../adippadai/tharavuru/uruvugal.dart';
import 'porul_kalanjiyam.dart';

class PattuPorulKalanjiyam implements PorulKalanjiyam {
  final PattuDatabase _db;

  PattuPorulKalanjiyam(this._db);

  PorulTharavuru _mapToDomain(PattuPorulEntry entry) {
    return PorulTharavuru(
      id: entry.id,
      porulPeyar: entry.porulPeyar,
      hsnCode: entry.hsnCode,
      vilai: entry.vilai,
      variVeetham: entry.variVeetham,
      alavuVagai: entry.alavuVagai,
      alagu: entry.alagu,
      createdAt: entry.createdAt,
      updatedAt: entry.updatedAt,
      isDeleted: entry.isDeleted,
      deletedAt: entry.deletedAt,
    );
  }

  @override
  Stream<List<PorulTharavuru>> watchAllPorulgal() {
    return (_db.select(_db.pattuPorulTable)
          ..where((t) => t.isDeleted.equals(false))
          ..orderBy([(t) => OrderingTerm.desc(t.updatedAt)]))
        .watch()
        .map((list) => list.map(_mapToDomain).toList());
  }

  @override
  Future<List<PorulTharavuru>> getAllPorulgal() async {
    final list = await (_db.select(_db.pattuPorulTable)
          ..where((t) => t.isDeleted.equals(false))
          ..orderBy([(t) => OrderingTerm.desc(t.updatedAt)]))
        .get();
    return list.map(_mapToDomain).toList();
  }

  @override
  Future<PorulTharavuru?> getPorulById(int id) async {
    final entry = await (_db.select(_db.pattuPorulTable)
          ..where((t) => t.id.equals(id))
          ..where((t) => t.isDeleted.equals(false)))
        .getSingleOrNull();
    return entry != null ? _mapToDomain(entry) : null;
  }

  @override
  Future<int> savePorul(PorulTharavuru tharavuru) async {
    final companion = PattuPorulTableCompanion(
      porulPeyar: Value(tharavuru.porulPeyar.cast<String, String>()),
      hsnCode: Value(tharavuru.hsnCode),
      vilai: Value(tharavuru.vilai),
      variVeetham: Value(tharavuru.variVeetham),
      alavuVagai: Value(tharavuru.alavuVagai),
      alagu: Value(tharavuru.alagu),
      updatedAt: Value(DateTime.now()),
    );

    if (tharavuru.id > 0) {
      await (_db.update(_db.pattuPorulTable)..where((t) => t.id.equals(tharavuru.id)))
          .write(companion);
      return tharavuru.id;
    } else {
      return _db.into(_db.pattuPorulTable).insert(companion);
    }
  }

  @override
  Future<void> deletePorul(int id) async {
    await (_db.update(_db.pattuPorulTable)..where((t) => t.id.equals(id))).write(
      PattuPorulTableCompanion(
        isDeleted: const Value(true),
        deletedAt: Value(DateTime.now()),
      ),
    );
  }

  @override
  Future<void> bulkDeletePorulgal(List<int> ids) async {
    await (_db.update(_db.pattuPorulTable)..where((t) => t.id.isIn(ids))).write(
      PattuPorulTableCompanion(
        isDeleted: const Value(true),
        deletedAt: Value(DateTime.now()),
      ),
    );
  }

  @override
  Future<void> restorePorul(int id) async {
    await (_db.update(_db.pattuPorulTable)..where((t) => t.id.equals(id))).write(
      const PattuPorulTableCompanion(
        isDeleted: Value(false),
        deletedAt: Value(null),
      ),
    );
  }

  @override
  Future<void> bulkRestorePorulgal(List<int> ids) async {
    await (_db.update(_db.pattuPorulTable)..where((t) => t.id.isIn(ids))).write(
      const PattuPorulTableCompanion(
        isDeleted: Value(false),
        deletedAt: Value(null),
      ),
    );
  }

  @override
  Future<void> permanentDeletePorul(int id) async {
    await (_db.delete(_db.pattuPorulTable)..where((t) => t.id.equals(id))).go();
  }

  @override
  Stream<List<PorulTharavuru>> watchDeletedPorulgal() {
    return (_db.select(_db.pattuPorulTable)
          ..where((t) => t.isDeleted.equals(true))
          ..orderBy([(t) => OrderingTerm.desc(t.deletedAt)]))
        .watch()
        .map((list) => list.map(_mapToDomain).toList());
  }

  @override
  Future<int> purgeExpiredPorulgal({int days = 30}) async {
    final cutoff = DateTime.now().subtract(Duration(days: days));
    return (_db.delete(_db.pattuPorulTable)
          ..where((t) => t.isDeleted.equals(true))
          ..where((t) => t.deletedAt.isSmallerOrEqualValue(cutoff)))
        .go();
  }

  @override
  Future<void> deleteAllPorulgal() async {
    await _db.delete(_db.pattuPorulTable).go();
  }
}
