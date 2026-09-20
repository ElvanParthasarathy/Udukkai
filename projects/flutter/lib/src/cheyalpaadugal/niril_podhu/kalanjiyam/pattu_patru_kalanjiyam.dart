import 'package:drift/drift.dart';
import '../../../adippadai/tharavuthalam/pattu_tharavuthalam.dart';
import '../../../adippadai/tharavuru/uruvugal.dart';
import 'patru_kalanjiyam.dart';

class PattuPatruKalanjiyam implements PatruKalanjiyam {
  final PattuDatabase _db;

  PattuPatruKalanjiyam(this._db);

  PatrugalTharavuru _mapToDomain(PattuPatrugalEntry entry) {
    return PatrugalTharavuru(
      id: entry.id,
      niruvanamId: entry.niruvanamId,
      patruEn: entry.patruEn,
      vanakkam: entry.vanakkam,
      finYear: int.tryParse(entry.finYear.toString()) ?? DateTime.now().year,
      vaangunarId: entry.vaangunarId,
      vaangunarPeyar: Map<String, String>.from(entry.vaangunarPeyar as Map),
      patruNaal: entry.patruNaal,
      thogai: entry.thogai,
      seluthumMurai: entry.seluthumMurai,
      vangiPeyar: entry.vangiPeyar ?? '',
      parivarthanaiEn: entry.parivarthanaiEn ?? '',
      ullkurippu: entry.ullkurippu ?? '',
      createdAt: entry.createdAt,
      updatedAt: entry.updatedAt,
      isDeleted: entry.isDeleted,
      deletedAt: entry.deletedAt,
    );
  }

  PattiyalTharavuru _mapPattiyalToDomain(PattuPattiyalEntry entry) {
    return PattiyalTharavuru(
      id: entry.id,
      niruvanamId: entry.niruvanamId,
      patrucheettuEn: entry.patrucheettuEn,
      finYear: int.tryParse(entry.finYear.toString()) ?? DateTime.now().year,
      vanakkam: entry.vanakkam,
      pattiyalVagai: entry.pattiyalVagai,
      vaangunarId: entry.vaangunarId,
      vaangunarPeyar: Map<String, String>.from(entry.vaangunarPeyar as Map),
      vaangunarMunvari: Map<String, String>.from(entry.vaangunarMunvari as Map),
      pattiyalNaal: entry.pattiyalNaal,
      tharavugal: entry.tharavugal,
      mothaThogai: entry.mothaThogai,
      thallupadi: entry.thallupadi,
      podhuThallupadiMathippu: entry.podhuThallupadiMathippu,
      podhuThallupadiVagai: entry.podhuThallupadiVagai,
      podhuThallupadiThogai: entry.podhuThallupadiThogai,
      variThogai: entry.variThogai,
      variTharavugal: entry.variTharavugal,
      mothaEdai: 0.0,
      setharamGrams: 0.0,
      thabaalThogai: 0.0,
      ahimsaPattuThogai: 0.0,
      piravariVugal: '[]',
      sonthaViruppangal: entry.sonthaViruppangal,
      nibandhanaigal: entry.nibandhanaigal,
      ullkurippu: entry.ullkurippu ?? '',
      vangiTharavugal: '{}',
      createdAt: entry.createdAt,
      updatedAt: entry.updatedAt,
      isDeleted: entry.isDeleted,
      deletedAt: entry.deletedAt,
    );
  }

  PattuPatrugalTableCompanion _mapToCompanion(PatrugalTharavuru data) {
    return PattuPatrugalTableCompanion(
      niruvanamId: Value(data.niruvanamId),
      patruEn: Value(data.patruEn),
      vanakkam: Value(data.vanakkam),
      finYear: Value(data.finYear.toString()),
      vaangunarId: Value(data.vaangunarId),
      vaangunarPeyar: Value(data.vaangunarPeyar.cast<String, String>()),
      patruNaal: Value(data.patruNaal),
      thogai: Value(data.thogai),
      seluthumMurai: Value(data.seluthumMurai),
      vangiPeyar: Value(data.vangiPeyar),
      parivarthanaiEn: Value(data.parivarthanaiEn),
      ullkurippu: Value(data.ullkurippu),
      updatedAt: Value(DateTime.now()),
    );
  }

  @override
  Stream<List<PatrugalTharavuru>> watchPatrugal() {
    return (_db.select(_db.pattuPatrugalTable)
          ..where((t) => t.isDeleted.equals(false))
          ..orderBy([
            (t) => OrderingTerm.desc(t.patruNaal),
            (t) => OrderingTerm.desc(t.vanakkam),
          ]))
        .watch()
        .map((list) => list.map(_mapToDomain).toList());
  }

  @override
  Future<List<PatrugalTharavuru>> getPatrugal() async {
    final list = await (_db.select(_db.pattuPatrugalTable)
          ..where((t) => t.isDeleted.equals(false))
          ..orderBy([
            (t) => OrderingTerm.desc(t.patruNaal),
            (t) => OrderingTerm.desc(t.vanakkam),
          ]))
        .get();
    return list.map(_mapToDomain).toList();
  }

  @override
  Future<PatrugalTharavuru?> getById(int id) async {
    final entry = await (_db.select(_db.pattuPatrugalTable)
          ..where((t) => t.id.equals(id))
          ..where((t) => t.isDeleted.equals(false)))
        .getSingleOrNull();
    return entry != null ? _mapToDomain(entry) : null;
  }

  @override
  Future<List<PatruPattiyalInaippuTharavuru>> getLinksForPatru(int patruId) async {
    final links = await (_db.select(_db.pattuPatruPattiyalTable)
          ..where((t) => t.patruId.equals(patruId)))
        .get();
    return links.map((e) => PatruPattiyalInaippuTharavuru(
      patruId: e.patruId,
      pattiyalId: e.pattiyalId,
      poruthiyaThogai: e.poruthiyaThogai,
    )).toList();
  }

  @override
  Future<int> insertPatru(PatrugalTharavuru data, List<PatruPattiyalInaippuTharavuru> links) {
    return _db.transaction(() async {
      final patruId = await _db.into(_db.pattuPatrugalTable).insert(_mapToCompanion(data));

      for (final link in links) {
        await _db.into(_db.pattuPatruPattiyalTable).insert(
              PattuPatruPattiyalTableCompanion.insert(
                patruId: patruId,
                pattiyalId: link.pattiyalId,
                poruthiyaThogai: Value(link.poruthiyaThogai),
              ),
            );
      }
      return patruId;
    });
  }

  @override
  Future<void> updatePatru(int id, PatrugalTharavuru data, List<PatruPattiyalInaippuTharavuru> links) {
    return _db.transaction(() async {
      await (_db.update(_db.pattuPatrugalTable)..where((t) => t.id.equals(id)))
          .write(_mapToCompanion(data));

      await (_db.delete(_db.pattuPatruPattiyalTable)..where((t) => t.patruId.equals(id))).go();

      for (final link in links) {
        await _db.into(_db.pattuPatruPattiyalTable).insert(
              PattuPatruPattiyalTableCompanion.insert(
                patruId: id,
                pattiyalId: link.pattiyalId,
                poruthiyaThogai: Value(link.poruthiyaThogai),
              ),
            );
      }
    });
  }

  @override
  Future<void> deletePatru(int id) {
    return _db.transaction(() async {
      await (_db.update(_db.pattuPatrugalTable)..where((t) => t.id.equals(id)))
          .write(PattuPatrugalTableCompanion(
        isDeleted: const Value(true),
        updatedAt: Value(DateTime.now()),
      ));

      await (_db.delete(_db.pattuPatruPattiyalTable)..where((t) => t.patruId.equals(id))).go();
    });
  }

  @override
  Future<void> bulkDeletePatrugal(List<int> ids) {
    return _db.transaction(() async {
      await (_db.update(_db.pattuPatrugalTable)..where((t) => t.id.isIn(ids)))
          .write(const PattuPatrugalTableCompanion(
        isDeleted: Value(true),
      ));

      await (_db.delete(_db.pattuPatruPattiyalTable)..where((t) => t.patruId.isIn(ids))).go();
    });
  }

  @override
  Future<void> deleteAllPatrugal() {
    return _db.transaction(() async {
      await _db.delete(_db.pattuPatruPattiyalTable).go();
      await _db.delete(_db.pattuPatrugalTable).go();
    });
  }

  @override
  Stream<double> watchPaidAmount(int pattiyalId) {
    final query = _db.selectOnly(_db.pattuPatruPattiyalTable)
      ..addColumns([_db.pattuPatruPattiyalTable.poruthiyaThogai.sum()])
      ..where(_db.pattuPatruPattiyalTable.pattiyalId.equals(pattiyalId));

    return query.watchSingle().map((row) {
      return row.read(_db.pattuPatruPattiyalTable.poruthiyaThogai.sum()) ?? 0.0;
    });
  }

  @override
  Future<double> getPaidAmount(int pattiyalId) async {
    final query = _db.selectOnly(_db.pattuPatruPattiyalTable)
      ..addColumns([_db.pattuPatruPattiyalTable.poruthiyaThogai.sum()])
      ..where(_db.pattuPatruPattiyalTable.pattiyalId.equals(pattiyalId));

    final row = await query.getSingle();
    return row.read(_db.pattuPatruPattiyalTable.poruthiyaThogai.sum()) ?? 0.0;
  }

  @override
  Stream<List<PattiyalTharavuru>> watchUnpaidInvoices() {
    return (_db.select(_db.pattuPattiyalTable)
          ..where((t) => t.isDeleted.equals(false))
          ..orderBy([
            (t) => OrderingTerm.desc(t.pattiyalNaal),
          ]))
        .watch()
        .map((list) => list.map(_mapPattiyalToDomain).toList());
  }

  @override
  Future<double> getPendingBalance(int pattiyalId) async {
    final invoice = await (_db.select(_db.pattuPattiyalTable)
          ..where((t) => t.id.equals(pattiyalId)))
        .getSingleOrNull();
    if (invoice == null) return 0.0;

    final paid = await getPaidAmount(pattiyalId);
    return (invoice.mothaThogai - paid).clamp(0.0, double.infinity);
  }

  @override
  Future<Map<int, double>> getPaidAmountsForInvoices(List<int> pattiyalIds) async {
    if (pattiyalIds.isEmpty) return {};

    final query = _db.selectOnly(_db.pattuPatruPattiyalTable)
      ..addColumns([
        _db.pattuPatruPattiyalTable.pattiyalId,
        _db.pattuPatruPattiyalTable.poruthiyaThogai.sum(),
      ])
      ..where(_db.pattuPatruPattiyalTable.pattiyalId.isIn(pattiyalIds))
      ..groupBy([_db.pattuPatruPattiyalTable.pattiyalId]);

    final rows = await query.get();
    final result = <int, double>{};
    for (final row in rows) {
      final id = row.read(_db.pattuPatruPattiyalTable.pattiyalId)!;
      final sum = row.read(_db.pattuPatruPattiyalTable.poruthiyaThogai.sum()) ?? 0.0;
      result[id] = sum;
    }
    return result;
  }

  @override
  Future<int> getNextVanakkam(int? niruvanamId) async {
    final query = _db.selectOnly(_db.pattuPatrugalTable)
      ..addColumns([_db.pattuPatrugalTable.vanakkam.max()]);

    if (niruvanamId != null) {
      query.where(_db.pattuPatrugalTable.niruvanamId.equals(niruvanamId));
    } else {
      query.where(_db.pattuPatrugalTable.niruvanamId.isNull());
    }

    final result = await query.getSingle();
    final maxVal = result.read(_db.pattuPatrugalTable.vanakkam.max());
    return (maxVal ?? 0) + 1;
  }

  @override
  String formatPatruEn(String bizShort, int vanakkam) {
    final padded = vanakkam < 10
        ? vanakkam.toString().padLeft(2, '0')
        : vanakkam.toString();
    return 'RCP/$bizShort/$padded';
  }

  @override
  Future<bool> isPatruEnDuplicate(
    int? niruvanamId,
    String patruEn, {
    int? excludeId,
  }) async {
    final query = _db.select(_db.pattuPatrugalTable)
      ..where((t) => t.patruEn.upper().equals(patruEn.toUpperCase()));

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

  @override
  Future<String?> validateLinks(
    List<PatruPattiyalInaippuTharavuru> links, {
    int? excludePatruId,
  }) async {
    for (final link in links) {
      final invoice = await (_db.select(_db.pattuPattiyalTable)
            ..where((t) => t.id.equals(link.pattiyalId)))
          .getSingleOrNull();
      if (invoice == null) {
        return 'Invoice #${link.pattiyalId} not found';
      }

      double alreadyPaid = await getPaidAmount(link.pattiyalId);
      if (excludePatruId != null) {
        final existingLinks = await getLinksForPatru(excludePatruId);
        for (final existing in existingLinks) {
          if (existing.pattiyalId == link.pattiyalId) {
            alreadyPaid -= existing.poruthiyaThogai;
          }
        }
      }

      final remaining = invoice.mothaThogai - alreadyPaid;
      if (link.poruthiyaThogai > remaining + 0.01) {
        return 'Amount ₹${link.poruthiyaThogai.toStringAsFixed(2)} exceeds '
            'remaining balance ₹${remaining.toStringAsFixed(2)} for '
            '${invoice.patrucheettuEn}';
      }
    }
    return null;
  }
}
