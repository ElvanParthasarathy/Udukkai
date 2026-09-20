import 'dart:io';

import 'package:drift/drift.dart';
import 'package:drift/native.dart';
import 'package:path_provider/path_provider.dart';
import 'package:path/path.dart' as p;
import 'package:sqlite3/sqlite3.dart';

import 'mozhi_map_converter.dart';

part 'kooli_tharavuthalam.g.dart';

// ── Table: நிறுவனத் தரவுகள் (Business Data) ──
@DataClassName('KooliNiruvanaTharavugalEntry')
class KooliNiruvanaTharavugalTable extends Table {
  IntColumn get id => integer().autoIncrement()();

  // ── நிறுவனத் தரவு (Business Details) ──
  TextColumn get niruvanathinPeyar =>
      text().map(const MozhiMapConverter()).withDefault(const Constant('{}'))();
  TextColumn get kurumPeyar => text().withDefault(const Constant(''))();
  TextColumn get tholaipaesi1 => text().withDefault(const Constant(''))();
  TextColumn get tholaipaesi2 => text().withDefault(const Constant(''))();
  TextColumn get minnanjal => text().withDefault(const Constant(''))();
  TextColumn get gstin => text().withDefault(const Constant(''))();

  // ── முகவரி (Address) ──
  TextColumn get mugavari =>
      text().map(const MozhiMapConverter()).withDefault(const Constant('{}'))();
  TextColumn get oor =>
      text().map(const MozhiMapConverter()).withDefault(const Constant('{}'))();
  TextColumn get maavattam =>
      text().map(const MozhiMapConverter()).withDefault(const Constant('{}'))();
  TextColumn get maanilam =>
      text().map(const MozhiMapConverter()).withDefault(const Constant('{}'))();
  TextColumn get naadu =>
      text().map(const MozhiMapConverter()).withDefault(const Constant('{\"en\": \"India\", \"ta\": \"இந்தியா\"}'))();
  TextColumn get anjalKuriyeedu => text().withDefault(const Constant(''))();

  // ── வங்கி (Bank Details) ──
  TextColumn get vangiPeyar =>
      text().map(const MozhiMapConverter()).withDefault(const Constant('{}'))();
  TextColumn get kilai =>
      text().map(const MozhiMapConverter()).withDefault(const Constant('{}'))();
  TextColumn get vangiKanakku => text().withDefault(const Constant(''))();
  TextColumn get ifsc => text().withDefault(const Constant(''))();

  // ── அடையாளங்கள் (Branding) ──
  TextColumn get oavuru => text().withDefault(const Constant(''))();
  TextColumn get agalaOavuru => text().withDefault(const Constant(''))();
  TextColumn get thalaippuVadivu =>
      text().withDefault(const Constant('small'))();
  TextColumn get kaiyoppam => text().withDefault(const Constant(''))();
  TextColumn get oppamPeyar => text().withDefault(const Constant(''))();

  // ── கூடுதல் (Additional) ──
  TextColumn get adaimozhi =>
      text().map(const MozhiMapConverter()).withDefault(const Constant('{}'))();
  TextColumn get upiId => text().withDefault(const Constant(''))();
  TextColumn get thoatraNiram => text().withDefault(const Constant(''))();

  // ── Audit & Soft Delete ──
  DateTimeColumn get createdAt => dateTime().withDefault(currentDateAndTime)();
  DateTimeColumn get updatedAt => dateTime().withDefault(currentDateAndTime)();
  BoolColumn get isDeleted => boolean().withDefault(const Constant(false))();
  DateTimeColumn get deletedAt => dateTime().nullable()();
}

// ── Table: வணிகர் (Customer / Merchant) ──
@DataClassName('KooliVaangunarEntry')
class KooliVaangunarTable extends Table {
  IntColumn get id => integer().autoIncrement()();

  // ── Bilingual fields (stored as JSON map via MozhiMapConverter) ──
  TextColumn get peyar =>
      text().map(const MozhiMapConverter()).withDefault(const Constant('{}'))();
  TextColumn get mugavari =>
      text().map(const MozhiMapConverter()).withDefault(const Constant('{}'))();
  TextColumn get oor =>
      text().map(const MozhiMapConverter()).withDefault(const Constant('{}'))();
  TextColumn get maavattam =>
      text().map(const MozhiMapConverter()).withDefault(const Constant('{}'))();
  TextColumn get maanilam =>
      text().map(const MozhiMapConverter()).withDefault(const Constant('{}'))();
  TextColumn get naadu =>
      text().map(const MozhiMapConverter()).withDefault(const Constant('{\"en\": \"India\", \"ta\": \"இந்தியா\"}'))();

  // ── Non-India: single multiline bilingual address ──
  TextColumn get velinaadMugavari =>
      text().map(const MozhiMapConverter()).withDefault(const Constant('{}'))();

  // ── Single-value fields ──
  TextColumn get anjalKuriyeedu => text().withDefault(const Constant(''))();
  TextColumn get gstin => text().withDefault(const Constant(''))();
  TextColumn get minnanjal => text().withDefault(const Constant(''))();
  TextColumn get tholaipaesi => text().withDefault(const Constant(''))();

  // Audit
  DateTimeColumn get createdAt => dateTime().withDefault(currentDateAndTime)();
  DateTimeColumn get updatedAt => dateTime().withDefault(currentDateAndTime)();
  BoolColumn get isDeleted => boolean().withDefault(const Constant(false))();
  DateTimeColumn get deletedAt => dateTime().nullable()();
}

// ── Table: பொருள் (Items / Products) ──
@DataClassName('KooliPorulEntry')
class KooliPorulTable extends Table {
  IntColumn get id => integer().autoIncrement()();
  
  // Bilingual name: {"Tamil": "...", "English": "..."}
  TextColumn get porulPeyar =>
      text().map(const MozhiMapConverter()).withDefault(const Constant('{}'))();
  TextColumn get hsnCode => text().withDefault(const Constant(''))();
  RealColumn get vilai => real().withDefault(const Constant(0.0))();
  RealColumn get variVeetham => real().withDefault(const Constant(0.0))(); // GST %

  // Measure: 'quantity' | 'weight'
  TextColumn get alavuVagai => text().withDefault(const Constant('quantity'))();
  // Unit: 'Nos' | 'kg'
  TextColumn get alagu => text().withDefault(const Constant('Nos'))();

  // Audit
  DateTimeColumn get createdAt => dateTime().withDefault(currentDateAndTime)();
  DateTimeColumn get updatedAt => dateTime().withDefault(currentDateAndTime)();
  BoolColumn get isDeleted => boolean().withDefault(const Constant(false))();
  DateTimeColumn get deletedAt => dateTime().nullable()();
}

// ── Table: பற்றுச்சீட்டு (Invoice) ──
@DataClassName('KooliPattiyalEntry')
class KooliPattiyalTable extends Table {
  // ── Identity ──
  IntColumn get id => integer().autoIncrement()();
  IntColumn get niruvanamId => integer().nullable()(); // FK → KooliNiruvanaTharavugalTable

  // ── Invoice Header ──
  TextColumn get patrucheettuEn => text().unique()(); // Display: SJS/2026-27/001
  IntColumn get finYear => integer()(); // e.g. 2026
  IntColumn get vanakkam =>
      integer().withDefault(const Constant(1))(); // Seq # within FY+company

  @override
  List<Set<Column>> get uniqueKeys => [
        {niruvanamId, finYear, vanakkam},
      ];

  TextColumn get pattiyalVagai =>
      text().withDefault(const Constant('tax-invoice'))(); // Silk: tax-invoice / proforma

  // ── Customer (FK — not snapshot) ──
  IntColumn get vaangunarId => integer().nullable()(); // FK → KooliVaangunarTable
  TextColumn get vaangunarPeyar =>
      text().map(const MozhiMapConverter()).withDefault(const Constant('{}'))();
  TextColumn get vaangunarMunvari =>
      text().map(const MozhiMapConverter()).withDefault(const Constant('{}'))();

  // ── Date ──
  DateTimeColumn get pattiyalNaal =>
      dateTime().withDefault(currentDateAndTime)(); // Invoice date (not created date)

  // ── Line Items (JSON array) ──
  TextColumn get tharavugal =>
      text().withDefault(const Constant('[]'))();

  // ── Financial Results (stored at save, never re-calculated) ──
  RealColumn get mothaThogai =>
      real().withDefault(const Constant(0.0))(); // Grand total
  RealColumn get thallupadi =>
      real().withDefault(const Constant(0.0))(); // Total discount
  RealColumn get podhuThallupadiMathippu =>
      real().withDefault(const Constant(0.0))(); // Global discount input value
  TextColumn get podhuThallupadiVagai =>
      text().withDefault(const Constant('%'))(); // Global discount type (% or ₹)
  RealColumn get podhuThallupadiThogai =>
      real().withDefault(const Constant(0.0))(); // Global discount calculated amount
  RealColumn get variThogai =>
      real().withDefault(const Constant(0.0))(); // Total tax
  TextColumn get variTharavugal =>
      text().withDefault(const Constant('{}'))(); // {cgst, sgst, igst}

  // ── Kooli-specific ──
  RealColumn get mothaEdai =>
      real().withDefault(const Constant(0.0))(); // Total KG
  RealColumn get setharamGrams =>
      real().withDefault(const Constant(0.0))();
  RealColumn get thabaalThogai =>
      real().named('thabaal_thogai').withDefault(const Constant(0.0))(); // Courier ₹
  RealColumn get ahimsaPattuThogai =>
      real().withDefault(const Constant(0.0))(); // Ahimsa silk ₹
  TextColumn get piravariVugal =>
      text().withDefault(const Constant('[]'))(); // [{peyar, thogai}]

  // ── Silk-specific ──
  TextColumn get sonthaViruppangal =>
      text().withDefault(const Constant('{}'))(); // Invoice settings JSON
  TextColumn get nibandhanaigal =>
      text().withDefault(const Constant(''))(); // Custom terms
  TextColumn get ullkurippu =>
      text().withDefault(const Constant(''))(); // Internal note

  // ── Bank Snapshot (coolie — from profile at save time) ──
  TextColumn get vangiTharavugal =>
      text().withDefault(const Constant('{}'))();

  // ── Audit ──
  DateTimeColumn get createdAt =>
      dateTime().withDefault(currentDateAndTime)();
  DateTimeColumn get updatedAt =>
      dateTime().withDefault(currentDateAndTime)();
  BoolColumn get isDeleted => boolean().withDefault(const Constant(false))();
  DateTimeColumn get deletedAt => dateTime().nullable()();
}

// ── Receipt Table ──
@DataClassName('KooliPatrugalEntry')
class KooliPatrugalTable extends Table {
  IntColumn get id => integer().autoIncrement()();
  IntColumn get niruvanamId =>
      integer().nullable()(); // FK → KooliNiruvanaTharavugalTable

  // ── Receipt Identity ──
  TextColumn get patruEn => text().unique()();
  TextColumn get finYear => text().withDefault(const Constant(''))();
  IntColumn get vanakkam =>
      integer().withDefault(const Constant(1))(); // Seq # for auto-numbering

  @override
  List<Set<Column>> get uniqueKeys => [
        {niruvanamId, finYear, vanakkam},
      ];

  // ── Customer (FK-first, fallback snapshot) ──
  IntColumn get vaangunarId =>
      integer().nullable()(); // FK → KooliVaangunarTable
  TextColumn get vaangunarPeyar =>
      text().map(const MozhiMapConverter()).withDefault(const Constant('{}'))();
  TextColumn get vaangunarMunvari =>
      text().map(const MozhiMapConverter()).withDefault(const Constant('{}'))();

  // ── Receipt Data ──
  DateTimeColumn get patruNaal =>
      dateTime().withDefault(currentDateAndTime)();
  RealColumn get thogai =>
      real().withDefault(const Constant(0.0))(); // Amount received

  // ── Payment ──
  TextColumn get seluthumMurai => text().withDefault(const Constant('cash'))();
  TextColumn get vangiPeyar => text().nullable()(); // 'cash'|'upi'|'bank_transfer'|'cheque'|'card'
  TextColumn get parivarthanaiEn => text().nullable()(); // Reference/Transaction ID

  // ── Note ──
  TextColumn get ullkurippu =>
      text().withDefault(const Constant(''))();

  // ── Audit ──
  DateTimeColumn get createdAt =>
      dateTime().withDefault(currentDateAndTime)();
  DateTimeColumn get updatedAt =>
      dateTime().withDefault(currentDateAndTime)();
  BoolColumn get isDeleted => boolean().withDefault(const Constant(false))();
  DateTimeColumn get deletedAt => dateTime().nullable()();
}

// ── Receipt ↔ Invoice Junction Table ──
@DataClassName('KooliPatruPattiyalEntry')
class KooliPatruPattiyalTable extends Table {
  IntColumn get id => integer().autoIncrement()();
  IntColumn get patruId => integer()(); // FK → KooliPatrugalTable
  IntColumn get pattiyalId => integer()(); // FK → KooliPattiyalTable
  RealColumn get poruthiyaThogai =>
      real().withDefault(const Constant(0.0))(); // Amount applied to this invoice
}

// ── Database ──
@DriftDatabase(tables: [
  KooliNiruvanaTharavugalTable,
  KooliVaangunarTable,
  KooliPorulTable,
  KooliPattiyalTable,
  KooliPatrugalTable,
  KooliPatruPattiyalTable,
])
class KooliDatabase extends _$KooliDatabase {
  KooliDatabase(super.e);

  @override
  int get schemaVersion => 2;

  @override
  MigrationStrategy get migration => MigrationStrategy(
        onCreate: (Migrator m) async {
          await m.createAll();
        },
        onUpgrade: (Migrator m, int from, int to) async {
          // Future migrations
        },
      );

  /// Opens the SQLite file safely on all platforms.
  static LazyDatabase openConnection(String dbName) {
    return LazyDatabase(() async {
      final dbFolder = await getApplicationSupportDirectory();

      if (!await dbFolder.exists()) {
        await dbFolder.create(recursive: true);
      }

      final file = File(p.join(dbFolder.path, dbName));

      if (Platform.isWindows) {
        sqlite3.tempDirectory = dbFolder.path;
      }

      return NativeDatabase.createInBackground(file);
    });
  }
}
