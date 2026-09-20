import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'dart:convert';
import 'dart:io';

import 'package:drift/drift.dart';
import 'package:drift/native.dart';
import 'package:path_provider/path_provider.dart';
import 'package:path/path.dart' as p;
import 'package:sqlite3/sqlite3.dart';

part 'seyali_tharavuthalam.g.dart';

// ── Type Converter: Map<String, String> ↔ JSON text ──
// Used for all bilingual fields (நிறுவனத்தின் பெயர், முகவரி, etc.)
class MozhiMapConverter extends TypeConverter<Map<String, String>, String> {
  const MozhiMapConverter();

  @override
  Map<String, String> fromSql(String fromDb) {
    if (fromDb.isEmpty || fromDb == '{}') return {};
    try {
      final decoded = jsonDecode(fromDb) as Map<String, dynamic>;
      return decoded.map((k, v) => MapEntry(k, v.toString()));
    } catch (_) {
      return {};
    }
  }

  @override
  String toSql(Map<String, String> value) {
    return jsonEncode(value);
  }
}

// ── Table: நிறுவனத் தரவுகள் (Business Data) ──
@DataClassName('NiruvanaTharavugalEntry')
class NiruvanaTharavugalTable extends Table {
  // Primary key
  IntColumn get id => integer().autoIncrement()();

  // App mode: 'silk' or 'coolie'
  TextColumn get seyaliVagai => text()();

  // ── மொழி அமைப்பு (Language Config) ──
  TextColumn get mudhanMozhi => text().withDefault(const Constant('ta'))();
  TextColumn get thunaiMozhi => text().withDefault(const Constant('en'))();
  BoolColumn get iruMozhi => boolean().withDefault(const Constant(false))();

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
      text().map(const MozhiMapConverter()).withDefault(const Constant('{}'))();
  TextColumn get anjalKuriyeedu => text().withDefault(const Constant(''))();
  TextColumn get velinaadMugavari =>
      text().map(const MozhiMapConverter()).withDefault(const Constant('{}'))();

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
@DataClassName('VaangunarEntry')
class VaangunarTable extends Table {
  IntColumn get id => integer().autoIncrement()();
  TextColumn get seyaliVagai => text()(); // 'silk' or 'coolie'

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
      text().map(const MozhiMapConverter()).withDefault(const Constant('{}'))();

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
@DataClassName('PorulEntry')
class PorulTable extends Table {
  IntColumn get id => integer().autoIncrement()();
  TextColumn get seyaliVagai => text()();
  
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
@DataClassName('PatrucheettuEntry')
class PatrucheettuTable extends Table {
  // ── Identity ──
  IntColumn get id => integer().autoIncrement()();
  TextColumn get seyaliVagai => text()(); // 'silk' or 'coolie'
  IntColumn get niruvanamId => integer().nullable()(); // FK → NiruvanaTharavugalTable

  // ── Invoice Header ──
  TextColumn get patrucheettuEn => text()(); // Display: SJS/2026-27/001
  IntColumn get finYear => integer()(); // e.g. 2026
  IntColumn get vanakkam =>
      integer().withDefault(const Constant(1))(); // Seq # within FY+company
  TextColumn get pattiyalVagai =>
      text().withDefault(const Constant('tax-invoice'))(); // Silk: tax-invoice / proforma

  // ── Customer (FK — not snapshot) ──
  IntColumn get vaangunarId => integer().nullable()(); // FK → VaangunarTable
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
  RealColumn get variThogai =>
      real().withDefault(const Constant(0.0))(); // Total tax
  TextColumn get variTharavugal =>
      text().withDefault(const Constant('{}'))(); // {cgst, sgst, igst}

  // ── Coolie-specific ──
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
  BoolColumn get isDeleted =>
      boolean().withDefault(const Constant(false))();
  DateTimeColumn get deletedAt => dateTime().nullable()();
}

// ── Receipt Table ──
/// Stores payment receipts — simple flat records of "Client paid ₹X via Y".
/// Completely separate from PatrucheettuTable (invoices).
@DataClassName('PatrugalEntry')
class PatrugalTable extends Table {
  IntColumn get id => integer().autoIncrement()();
  TextColumn get seyaliVagai => text()(); // 'silk' | 'coolie'
  IntColumn get niruvanamId =>
      integer().nullable()(); // FK → NiruvanaTharavugalTable

  // ── Receipt Identity ──
  TextColumn get patruEn => text()(); // Display: 'RCP/SJS/01'
  IntColumn get vanakkam =>
      integer().withDefault(const Constant(1))(); // Seq # for auto-numbering

  // ── Customer (FK-first, fallback snapshot) ──
  IntColumn get vaangunarId =>
      integer().nullable()(); // FK → VaangunarTable
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
  TextColumn get seluthiVagai =>
      text().withDefault(const Constant(''))(); // 'cash'|'upi'|'bank_transfer'|'cheque'|'card'
  TextColumn get suttruEn =>
      text().withDefault(const Constant(''))(); // Reference/Transaction ID

  // ── Note ──
  TextColumn get ullkurippu =>
      text().withDefault(const Constant(''))();

  // ── Audit ──
  DateTimeColumn get createdAt =>
      dateTime().withDefault(currentDateAndTime)();
  DateTimeColumn get updatedAt =>
      dateTime().withDefault(currentDateAndTime)();
  BoolColumn get isDeleted =>
      boolean().withDefault(const Constant(false))();
}

// ── Receipt ↔ Invoice Junction Table ──
/// Links each receipt to one or more invoices with the exact amount applied.
/// Enables partial payments and accurate balance tracking (like Tally/Zoho).
@DataClassName('PatruPattiyalEntry')
class PatruPattiyalTable extends Table {
  IntColumn get id => integer().autoIncrement()();
  IntColumn get patruId => integer()(); // FK → PatrugalTable
  IntColumn get pattiyalId => integer()(); // FK → PatrucheettuTable
  RealColumn get poruthiyaThogai =>
      real().withDefault(const Constant(0.0))(); // Amount applied to this invoice
}

// ── Database ──
@DriftDatabase(tables: [
  NiruvanaTharavugalTable,
  VaangunarTable,
  PorulTable,
  PatrucheettuTable,
  PatrugalTable,
  PatruPattiyalTable,
])
class AppDatabase extends _$AppDatabase {
  AppDatabase(super.e);

  @override
  int get schemaVersion => 14;

  @override
  MigrationStrategy get migration => MigrationStrategy(
        onCreate: (Migrator m) async {
          await m.createAll();
        },
        onUpgrade: (Migrator m, int from, int to) async {
          if (from < 3) {
            // Add new audit columns to existing business table
            await m.addColumn(niruvanaTharavugalTable, niruvanaTharavugalTable.createdAt);
            await m.addColumn(niruvanaTharavugalTable, niruvanaTharavugalTable.updatedAt);
            await m.addColumn(niruvanaTharavugalTable, niruvanaTharavugalTable.isDeleted);
            await m.addColumn(niruvanaTharavugalTable, niruvanaTharavugalTable.deletedAt);
            
            // Create new tables added in version 3
            await m.createTable(vaangunarTable);
            await m.createTable(porulTable);
            await m.createTable(patrucheettuTable);
          }
          if (from < 4) {
            // v4: Add measureType + unit columns to PorulTable
            await m.addColumn(porulTable, porulTable.alavuVagai);
            await m.addColumn(porulTable, porulTable.alagu);
          }
          if (from < 5) {
            // v5: Expand VaangunarTable — bilingual fields + new columns
            // peyar was plain text, now MozhiMap — migration: wrap old value
            // New columns with defaults:
            await m.addColumn(vaangunarTable, vaangunarTable.oor);
            await m.addColumn(vaangunarTable, vaangunarTable.maavattam);
            await m.addColumn(vaangunarTable, vaangunarTable.maanilam);
            await m.addColumn(vaangunarTable, vaangunarTable.naadu);
            await m.addColumn(vaangunarTable, vaangunarTable.velinaadMugavari);
            await m.addColumn(vaangunarTable, vaangunarTable.anjalKuriyeedu);
            await m.addColumn(vaangunarTable, vaangunarTable.minnanjal);

            // Migrate old plain-text peyar & mugavari to JSON map format
            await customStatement(
              "UPDATE vaangunar_table SET peyar = '{\"en\":' || '\"' || peyar || '\"' || '}' "
              "WHERE peyar NOT LIKE '{%}'",
            );
            await customStatement(
              "UPDATE vaangunar_table SET mugavari = '{\"en\":' || '\"' || mugavari || '\"' || '}' "
              "WHERE mugavari NOT LIKE '{%}' AND mugavari != ''",
            );
            // Set empty mugavari to default JSON
            await customStatement(
              "UPDATE vaangunar_table SET mugavari = '{}' WHERE mugavari = ''",
            );
          }
          if (from < 6) {
            // v6: Expand PatrucheettuTable for full invoice support
            await m.addColumn(patrucheettuTable, patrucheettuTable.niruvanamId);
            await m.addColumn(patrucheettuTable, patrucheettuTable.vanakkam);
            await m.addColumn(patrucheettuTable, patrucheettuTable.pattiyalVagai);
            await m.addColumn(patrucheettuTable, patrucheettuTable.pattiyalNaal);
            await m.addColumn(patrucheettuTable, patrucheettuTable.thallupadi);
            await m.addColumn(patrucheettuTable, patrucheettuTable.variThogai);
            await m.addColumn(patrucheettuTable, patrucheettuTable.variTharavugal);
            await m.addColumn(patrucheettuTable, patrucheettuTable.mothaEdai);
            await m.addColumn(patrucheettuTable, patrucheettuTable.setharamGrams);
            await m.addColumn(patrucheettuTable, patrucheettuTable.thabaalThogai);
            await m.addColumn(patrucheettuTable, patrucheettuTable.ahimsaPattuThogai);
            await m.addColumn(patrucheettuTable, patrucheettuTable.piravariVugal);
            await m.addColumn(patrucheettuTable, patrucheettuTable.sonthaViruppangal);
            await m.addColumn(patrucheettuTable, patrucheettuTable.nibandhanaigal);
            await m.addColumn(patrucheettuTable, patrucheettuTable.ullkurippu);
            await m.addColumn(patrucheettuTable, patrucheettuTable.vangiTharavugal);

            // Drop deprecated column
            await customStatement(
              'ALTER TABLE patrucheettu_table DROP COLUMN IF EXISTS vaangunar_tholaipaesi',
            );
          }
          if (from < 7) {
            // v7: Receipt system — new tables
            await m.createTable(patrugalTable);
            await m.createTable(patruPattiyalTable);
          }
          if (from < 8) {
            // v8: Rename vaniga_tharavugal_table → niruvana_tharavugal_table
            await customStatement(
              'ALTER TABLE vaniga_tharavugal_table RENAME TO niruvana_tharavugal_table',
            );
          }
          if (from < 9) {
            // v9: Add English fallback snapshot fields to patrugal_table
            await customStatement(
                'ALTER TABLE patrugal_table ADD COLUMN vaangunar_peyar_en TEXT DEFAULT ""');
            await customStatement(
                'ALTER TABLE patrugal_table ADD COLUMN vaangunar_munvari_en TEXT DEFAULT ""');
          }
          if (from < 10) {
            await customStatement(
                'ALTER TABLE patrucheettu_table ADD COLUMN vaangunar_peyar_en TEXT DEFAULT ""');
            await m.addColumn(patrucheettuTable, patrucheettuTable.vaangunarMunvari);
            await customStatement(
                'ALTER TABLE patrucheettu_table ADD COLUMN vaangunar_munvari_en TEXT DEFAULT ""');
          }
          if (from < 11) {
            // Drop _en columns
            await customStatement(
              'ALTER TABLE patrucheettu_table DROP COLUMN IF EXISTS vaangunar_peyar_en',
            );
            await customStatement(
              'ALTER TABLE patrucheettu_table DROP COLUMN IF EXISTS vaangunar_munvari_en',
            );
            await customStatement(
              'ALTER TABLE patrugal_table DROP COLUMN IF EXISTS vaangunar_peyar_en',
            );
            await customStatement(
              'ALTER TABLE patrugal_table DROP COLUMN IF EXISTS vaangunar_munvari_en',
            );

            // Migrate flat text to JSON map (e.g. {"Tamil":"..."})
            await customStatement(
              "UPDATE patrucheettu_table SET vaangunar_peyar = '{\"Tamil\":' || '\"' || vaangunar_peyar || '\"' || '}' "
              "WHERE vaangunar_peyar NOT LIKE '{%}'",
            );
            await customStatement(
              "UPDATE patrucheettu_table SET vaangunar_munvari = '{\"Tamil\":' || '\"' || vaangunar_munvari || '\"' || '}' "
              "WHERE vaangunar_munvari NOT LIKE '{%}' AND vaangunar_munvari != ''",
            );
            await customStatement(
              "UPDATE patrucheettu_table SET vaangunar_munvari = '{}' WHERE vaangunar_munvari = ''",
            );

            await customStatement(
              "UPDATE patrugal_table SET vaangunar_peyar = '{\"Tamil\":' || '\"' || vaangunar_peyar || '\"' || '}' "
              "WHERE vaangunar_peyar NOT LIKE '{%}'",
            );
            await customStatement(
              "UPDATE patrugal_table SET vaangunar_munvari = '{\"Tamil\":' || '\"' || vaangunar_munvari || '\"' || '}' "
              "WHERE vaangunar_munvari NOT LIKE '{%}' AND vaangunar_munvari != ''",
            );
            await customStatement(
              "UPDATE patrugal_table SET vaangunar_munvari = '{}' WHERE vaangunar_munvari = ''",
            );
          }
          if (from < 12) {
            // v12: Standardize language keys (Technical Debt Migration)
            // Replace 'Tamil' -> 'ta' and 'English' -> 'en' across all JSON maps
            final tables = [
              'patrucheettu_table',
              'patrugal_table',
              'porul_table',
              'vaangunar_table',
              'niruvana_tharavugal_table'
            ];
            final columns = [
              'peyar', 'oor', 'mugavari', 'maavattam', 'maanilam', 'naadu',
              'velinaad_mugavari', 'niruvanathin_peyar', 'adaimozhi', 
              'porul_peyar', 'vaangunar_peyar', 'vaangunar_munvari'
            ];
            
            for (final table in tables) {
              // Not every table has every column, so we ignore errors silently or just check
              // Actually, sqlite3 doesn't support IF EXISTS on UPDATE. 
              // To be safe in Drift customStatement, it's better to do them specifically per table
            }
            
            // For patrucheettu_table
            await customStatement("UPDATE patrucheettu_table SET vaangunar_peyar = REPLACE(REPLACE(vaangunar_peyar, '\"Tamil\":', '\"ta\":'), '\"English\":', '\"en\":')");
            await customStatement("UPDATE patrucheettu_table SET vaangunar_munvari = REPLACE(REPLACE(vaangunar_munvari, '\"Tamil\":', '\"ta\":'), '\"English\":', '\"en\":')");
            
            // For patrugal_table
            await customStatement("UPDATE patrugal_table SET vaangunar_peyar = REPLACE(REPLACE(vaangunar_peyar, '\"Tamil\":', '\"ta\":'), '\"English\":', '\"en\":')");
            await customStatement("UPDATE patrugal_table SET vaangunar_munvari = REPLACE(REPLACE(vaangunar_munvari, '\"Tamil\":', '\"ta\":'), '\"English\":', '\"en\":')");
            
            // For porul_table
            await customStatement("UPDATE porul_table SET porul_peyar = REPLACE(REPLACE(porul_peyar, '\"Tamil\":', '\"ta\":'), '\"English\":', '\"en\":')");
            
            // For vaangunar_table
            await customStatement("UPDATE vaangunar_table SET peyar = REPLACE(REPLACE(peyar, '\"Tamil\":', '\"ta\":'), '\"English\":', '\"en\":')");
            await customStatement("UPDATE vaangunar_table SET oor = REPLACE(REPLACE(oor, '\"Tamil\":', '\"ta\":'), '\"English\":', '\"en\":')");
            await customStatement("UPDATE vaangunar_table SET mugavari = REPLACE(REPLACE(mugavari, '\"Tamil\":', '\"ta\":'), '\"English\":', '\"en\":')");
            await customStatement("UPDATE vaangunar_table SET maavattam = REPLACE(REPLACE(maavattam, '\"Tamil\":', '\"ta\":'), '\"English\":', '\"en\":')");
            await customStatement("UPDATE vaangunar_table SET maanilam = REPLACE(REPLACE(maanilam, '\"Tamil\":', '\"ta\":'), '\"English\":', '\"en\":')");
            await customStatement("UPDATE vaangunar_table SET naadu = REPLACE(REPLACE(naadu, '\"Tamil\":', '\"ta\":'), '\"English\":', '\"en\":')");
            await customStatement("UPDATE vaangunar_table SET velinaad_mugavari = REPLACE(REPLACE(velinaad_mugavari, '\"Tamil\":', '\"ta\":'), '\"English\":', '\"en\":')");
            
            // For niruvana_tharavugal_table
            await customStatement("UPDATE niruvana_tharavugal_table SET niruvanathin_peyar = REPLACE(REPLACE(niruvanathin_peyar, '\"Tamil\":', '\"ta\":'), '\"English\":', '\"en\":')");
            await customStatement("UPDATE niruvana_tharavugal_table SET oor = REPLACE(REPLACE(oor, '\"Tamil\":', '\"ta\":'), '\"English\":', '\"en\":')");
            await customStatement("UPDATE niruvana_tharavugal_table SET mugavari = REPLACE(REPLACE(mugavari, '\"Tamil\":', '\"ta\":'), '\"English\":', '\"en\":')");
            await customStatement("UPDATE niruvana_tharavugal_table SET maavattam = REPLACE(REPLACE(maavattam, '\"Tamil\":', '\"ta\":'), '\"English\":', '\"en\":')");
            await customStatement("UPDATE niruvana_tharavugal_table SET maanilam = REPLACE(REPLACE(maanilam, '\"Tamil\":', '\"ta\":'), '\"English\":', '\"en\":')");
            await customStatement("UPDATE niruvana_tharavugal_table SET naadu = REPLACE(REPLACE(naadu, '\"Tamil\":', '\"ta\":'), '\"English\":', '\"en\":')");
            await customStatement("UPDATE niruvana_tharavugal_table SET adaimozhi = REPLACE(REPLACE(adaimozhi, '\"Tamil\":', '\"ta\":'), '\"English\":', '\"en\":')");
          }
          if (from < 13) {
            // v13: Migrate the flat database columns that were missed in v12
            await customStatement("UPDATE niruvana_tharavugal_table SET mudhan_mozhi = 'ta' WHERE mudhan_mozhi = 'Tamil'");
            await customStatement("UPDATE niruvana_tharavugal_table SET mudhan_mozhi = 'en' WHERE mudhan_mozhi = 'English'");
            await customStatement("UPDATE niruvana_tharavugal_table SET thunai_mozhi = 'ta' WHERE thunai_mozhi = 'Tamil'");
            await customStatement("UPDATE niruvana_tharavugal_table SET thunai_mozhi = 'en' WHERE thunai_mozhi = 'English'");
          }
          if (from < 14) {
            // v14: Re-run v12 and v13 migrations to catch any 'Tamil' or 'English' data injected by the old seed generator

            // Re-run JSON map replacements
            await customStatement("UPDATE patrucheettu_table SET vaangunar_peyar = REPLACE(REPLACE(vaangunar_peyar, '\"Tamil\":', '\"ta\":'), '\"English\":', '\"en\":')");
            await customStatement("UPDATE patrucheettu_table SET vaangunar_munvari = REPLACE(REPLACE(vaangunar_munvari, '\"Tamil\":', '\"ta\":'), '\"English\":', '\"en\":')");
            
            await customStatement("UPDATE patrugal_table SET vaangunar_peyar = REPLACE(REPLACE(vaangunar_peyar, '\"Tamil\":', '\"ta\":'), '\"English\":', '\"en\":')");
            await customStatement("UPDATE patrugal_table SET vaangunar_munvari = REPLACE(REPLACE(vaangunar_munvari, '\"Tamil\":', '\"ta\":'), '\"English\":', '\"en\":')");
            
            await customStatement("UPDATE porul_table SET porul_peyar = REPLACE(REPLACE(porul_peyar, '\"Tamil\":', '\"ta\":'), '\"English\":', '\"en\":')");
            
            await customStatement("UPDATE vaangunar_table SET peyar = REPLACE(REPLACE(peyar, '\"Tamil\":', '\"ta\":'), '\"English\":', '\"en\":')");
            await customStatement("UPDATE vaangunar_table SET oor = REPLACE(REPLACE(oor, '\"Tamil\":', '\"ta\":'), '\"English\":', '\"en\":')");
            await customStatement("UPDATE vaangunar_table SET mugavari = REPLACE(REPLACE(mugavari, '\"Tamil\":', '\"ta\":'), '\"English\":', '\"en\":')");
            await customStatement("UPDATE vaangunar_table SET maavattam = REPLACE(REPLACE(maavattam, '\"Tamil\":', '\"ta\":'), '\"English\":', '\"en\":')");
            await customStatement("UPDATE vaangunar_table SET maanilam = REPLACE(REPLACE(maanilam, '\"Tamil\":', '\"ta\":'), '\"English\":', '\"en\":')");
            await customStatement("UPDATE vaangunar_table SET naadu = REPLACE(REPLACE(naadu, '\"Tamil\":', '\"ta\":'), '\"English\":', '\"en\":')");
            await customStatement("UPDATE vaangunar_table SET velinaad_mugavari = REPLACE(REPLACE(velinaad_mugavari, '\"Tamil\":', '\"ta\":'), '\"English\":', '\"en\":')");
            
            await customStatement("UPDATE niruvana_tharavugal_table SET niruvanathin_peyar = REPLACE(REPLACE(niruvanathin_peyar, '\"Tamil\":', '\"ta\":'), '\"English\":', '\"en\":')");
            await customStatement("UPDATE niruvana_tharavugal_table SET oor = REPLACE(REPLACE(oor, '\"Tamil\":', '\"ta\":'), '\"English\":', '\"en\":')");
            await customStatement("UPDATE niruvana_tharavugal_table SET mugavari = REPLACE(REPLACE(mugavari, '\"Tamil\":', '\"ta\":'), '\"English\":', '\"en\":')");
            await customStatement("UPDATE niruvana_tharavugal_table SET maavattam = REPLACE(REPLACE(maavattam, '\"Tamil\":', '\"ta\":'), '\"English\":', '\"en\":')");
            await customStatement("UPDATE niruvana_tharavugal_table SET maanilam = REPLACE(REPLACE(maanilam, '\"Tamil\":', '\"ta\":'), '\"English\":', '\"en\":')");
            await customStatement("UPDATE niruvana_tharavugal_table SET naadu = REPLACE(REPLACE(naadu, '\"Tamil\":', '\"ta\":'), '\"English\":', '\"en\":')");
            await customStatement("UPDATE niruvana_tharavugal_table SET adaimozhi = REPLACE(REPLACE(adaimozhi, '\"Tamil\":', '\"ta\":'), '\"English\":', '\"en\":')");

            // Re-run flat column updates
            await customStatement("UPDATE niruvana_tharavugal_table SET mudhan_mozhi = 'ta' WHERE mudhan_mozhi = 'Tamil'");
            await customStatement("UPDATE niruvana_tharavugal_table SET mudhan_mozhi = 'en' WHERE mudhan_mozhi = 'English'");
            await customStatement("UPDATE niruvana_tharavugal_table SET thunai_mozhi = 'ta' WHERE thunai_mozhi = 'Tamil'");
            await customStatement("UPDATE niruvana_tharavugal_table SET thunai_mozhi = 'en' WHERE thunai_mozhi = 'English'");
          }
        },
      );

  /// Opens the SQLite file safely on all platforms.
  static LazyDatabase openConnection(String dbName, {String? keepMode}) {
    return LazyDatabase(() async {
      // Use Support Directory instead of Documents Directory.
      // Windows Documents dir is often locked by OneDrive or permissions, preventing SQLite journaling.
      final dbFolder = await getApplicationSupportDirectory();

      if (!await dbFolder.exists()) {
        await dbFolder.create(recursive: true);
      }

      final file = File(p.join(dbFolder.path, dbName));

      // ── MIGRATION LOGIC ──
      // If the target DB does not exist, check if we need to migrate from legacy single DB
      if (!await file.exists()) {
        final legacyFile = File(p.join(dbFolder.path, 'elvan_niril.db'));
        if (await legacyFile.exists()) {
          // Copy legacy DB to the new split DB
          await legacyFile.copy(file.path);
          
          // Scrub data belonging to the OTHER mode
          if (keepMode != null) {
            // We use native SQLite driver to do a quick scrub before drift opens it
            final tempDb = sqlite3.open(file.path);
            try {
              tempDb.execute("DELETE FROM patrucheettu_table WHERE seyali_vagai != '$keepMode'");
              tempDb.execute("DELETE FROM patrugal_table WHERE seyali_vagai != '$keepMode'");
              tempDb.execute("DELETE FROM porul_table WHERE seyali_vagai != '$keepMode'");
              tempDb.execute("DELETE FROM vaangunar_table WHERE seyali_vagai != '$keepMode'");
              tempDb.execute("DELETE FROM niruvana_tharavugal_table WHERE seyali_vagai != '$keepMode'");
            } catch (e) {
              // ignore if tables don't exist yet
            } finally {
              tempDb.dispose();
            }
          }
        }
      }

      if (Platform.isWindows) {
        // Essential for Windows: tell SQLite where to store temporary files (like WAL journals)
        // Otherwise, changes might be lost when the app closes.
        sqlite3.tempDirectory = dbFolder.path;
      }

      return NativeDatabase.createInBackground(file);
    });
  }
}


final appDatabaseProvider = Provider<AppDatabase>((ref) {
  final db = AppDatabase(AppDatabase.openConnection('elvan_niril.db'));
  ref.onDispose(db.close);
  return db;
});
