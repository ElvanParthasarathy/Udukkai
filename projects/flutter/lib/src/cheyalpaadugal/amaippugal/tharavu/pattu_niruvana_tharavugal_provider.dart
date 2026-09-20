import 'package:drift/drift.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../../adippadai/tharavuthalam/pattu_tharavuthalam.dart';
import '../../../adippadai/panigal/niril_backup_service.dart';
import '../../../adippadai/viruppangal_paniyagam.dart';
import 'niruvana_tharavugal.dart';

/// Provider for the Drift database instance.
final pattuDatabaseProvider = Provider<PattuDatabase>((ref) {
  final db = PattuDatabase(PattuDatabase.openConnection('elvan_niril_silk.db'));
  ref.onDispose(() {
    db.close();
  });
  return db;
});

const int maxPattuProfiles = 5;

final pattuNiruvanaTharavugalListProvider =
    NotifierProvider<PattuNiruvanaTharavugalListNotifier, List<NiruvanaTharavugal>>(() {
  return PattuNiruvanaTharavugalListNotifier();
});

final pattuNiruvanaTharavugalProvider = Provider<NiruvanaTharavugal?>((ref) {
  final profiles = ref.watch(pattuNiruvanaTharavugalListProvider);
  final prefs = ref.watch(sharedPreferencesProvider);

  if (profiles.isEmpty) return null;

  final activeId = prefs.getInt('silk_active_profile_id');

  if (activeId != null) {
    final match = profiles.where((p) => p.id == activeId);
    if (match.isNotEmpty) return match.first;
  }

  return profiles.first;
});

final pattuNiruvanaTharavugalByIdProvider = Provider.family<NiruvanaTharavugal?, int?>((ref, id) {
  if (id == null) return null;
  final profiles = ref.watch(pattuNiruvanaTharavugalListProvider);
  final match = profiles.where((p) => p.id == id);
  if (match.isNotEmpty) return match.first;
  // Fallback to active profile if the specific one was hard-deleted or not found
  return ref.read(pattuNiruvanaTharavugalProvider);
});

class PattuNiruvanaTharavugalListNotifier extends Notifier<List<NiruvanaTharavugal>> {
  late PattuDatabase _db;
  late NirilBackupService _backupService;
  late dynamic _prefs; 

  @override
  List<NiruvanaTharavugal> build() {
    _db = ref.watch(pattuDatabaseProvider);
    _backupService = ref.watch(backupServiceProvider);
    _prefs = ref.watch(sharedPreferencesProvider);
    _loadAllProfiles();
    return [];
  }

  Future<void> hideProfile(int id) async {
    // Hidden profiles are just deleted in this implementation, or we can use isDeleted
    await (_db.update(_db.pattuNiruvanaTharavugalTable)..where((t) => t.id.equals(id)))
        .write(PattuNiruvanaTharavugalTableCompanion(isDeleted: const Value(true)));
    await _loadAllProfiles();
  }

  Future<bool> restoreProfile(String kurumPeyar) async {
    final hiddenProfile = await (_db.select(_db.pattuNiruvanaTharavugalTable)
          ..where((t) => t.isDeleted.equals(true))
          ..where((t) => t.kurumPeyar.equals(kurumPeyar))
          ..limit(1))
        .getSingleOrNull();

    if (hiddenProfile != null) {
      await (_db.update(_db.pattuNiruvanaTharavugalTable)..where((t) => t.id.equals(hiddenProfile.id)))
          .write(PattuNiruvanaTharavugalTableCompanion(isDeleted: const Value(false)));
      await _loadAllProfiles();
      return true;
    }
    return false;
  }

  Future<void> _loadAllProfiles() async {
    // Yield to the event loop to prevent 'setState() called during build' 
    // because Drift NativeDatabase might return results synchronously.
    await Future.delayed(Duration.zero);
    try {
      final entries = await (_db.select(_db.pattuNiruvanaTharavugalTable)
            ..where((t) => t.isDeleted.equals(false)))
          .get();
      state = entries.map(_entryToModel).toList();
    } catch (e) {
      print('Error loading Pattu profiles: $e');
      state = [];
    }
  }

  Future<int?> createProfile(NiruvanaTharavugal profile) async {
    if (state.length >= maxPattuProfiles) return null;

    final id = await _db.into(_db.pattuNiruvanaTharavugalTable).insert(
          _modelToCompanion(profile),
        );

    profile.id = id;
    state = [...state, profile];
    _prefs.setInt('silk_active_profile_id', id);
    _backupService.createBackup();
    return id;
  }

  Future<void> updateProfile(NiruvanaTharavugal profile) async {
    if (profile.id != null) {
      await (_db.update(_db.pattuNiruvanaTharavugalTable)
            ..where((t) => t.id.equals(profile.id!)))
          .write(_modelToCompanion(profile));

      state = [
        for (final p in state)
          if (p.id == profile.id) profile else p,
      ];
    } else {
      await createProfile(profile);
      return;
    }
    _backupService.createBackup();
  }

  void setActiveProfile(int id) {
    _prefs.setInt('silk_active_profile_id', id);
    state = [...state];
  }

  Future<void> deleteProfile(int id) async {
    await (_db.delete(_db.pattuNiruvanaTharavugalTable)
          ..where((t) => t.id.equals(id)))
        .go();
    state = state.where((p) => p.id != id).toList();

    final activeId = _prefs.getInt('silk_active_profile_id');
    if (activeId == id) {
      if (state.isNotEmpty) {
        _prefs.setInt('silk_active_profile_id', state.first.id!);
      } else {
        _prefs.remove('silk_active_profile_id');
      }
    }
    _backupService.createBackup();
  }

  Future<void> clearProfile() async {
    await _db.delete(_db.pattuNiruvanaTharavugalTable).go();
    state = [];
    _prefs.remove('silk_active_profile_id');
  }

  NiruvanaTharavugal _entryToModel(PattuNiruvanaTharavugalEntry entry) {
    return NiruvanaTharavugal(
      id: entry.id,
      mudhanMozhi: entry.mudhanMozhi,
      thunaiMozhi: entry.thunaiMozhi,
      iruMozhi: entry.iruMozhi,
      gstPirippugal: entry.gstPirippugal,
      niruvanathinPeyar: entry.niruvanathinPeyar,
      kurumPeyar: entry.kurumPeyar,
      tholaipaesi1: entry.tholaipaesi1,
      tholaipaesi2: entry.tholaipaesi2,
      minnanjal: entry.minnanjal,
      gstin: entry.gstin,
      mugavari: entry.mugavari,
      oor: entry.oor,
      maavattam: entry.maavattam,
      maanilam: entry.maanilam,
      naadu: entry.naadu,
      anjalKuriyeedu: entry.anjalKuriyeedu,
      vangiPeyar: entry.vangiPeyar,
      kilai: entry.kilai,
      vangiKanakku: entry.vangiKanakku,
      ifsc: entry.ifsc,
      oavuru: entry.oavuru,
      agalaOavuru: entry.agalaOavuru,
      thalaippuVadivu: entry.thalaippuVadivu,
      kaiyoppam: entry.kaiyoppam,
      oppamPeyar: entry.oppamPeyar,
      adaimozhi: entry.adaimozhi,
      upiId: entry.upiId,
      thoatraNiram: '', // Pattu doesn't use this
    );
  }

  PattuNiruvanaTharavugalTableCompanion _modelToCompanion(NiruvanaTharavugal model) {
    return PattuNiruvanaTharavugalTableCompanion(
      mudhanMozhi: Value(model.mudhanMozhi),
      thunaiMozhi: Value(model.thunaiMozhi),
      iruMozhi: Value(model.iruMozhi),
      gstPirippugal: Value(model.gstPirippugal),
      niruvanathinPeyar: Value(model.niruvanathinPeyar),
      kurumPeyar: Value(model.kurumPeyar),
      tholaipaesi1: Value(model.tholaipaesi1),
      tholaipaesi2: Value(model.tholaipaesi2),
      minnanjal: Value(model.minnanjal),
      gstin: Value(model.gstin),
      mugavari: Value(model.mugavari),
      oor: Value(model.oor),
      maavattam: Value(model.maavattam),
      maanilam: Value(model.maanilam),
      naadu: Value(model.naadu),
      anjalKuriyeedu: Value(model.anjalKuriyeedu),
      vangiPeyar: Value(model.vangiPeyar),
      kilai: Value(model.kilai),
      vangiKanakku: Value(model.vangiKanakku),
      ifsc: Value(model.ifsc),
      oavuru: Value(model.oavuru),
      agalaOavuru: Value(model.agalaOavuru),
      thalaippuVadivu: Value(model.thalaippuVadivu),
      kaiyoppam: Value(model.kaiyoppam),
      oppamPeyar: Value(model.oppamPeyar),
      adaimozhi: Value(model.adaimozhi),
      upiId: Value(model.upiId),
    );
  }
}
