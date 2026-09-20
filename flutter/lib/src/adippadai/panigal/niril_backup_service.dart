import 'dart:io';
import 'dart:typed_data';

import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:path_provider/path_provider.dart';
import 'package:path/path.dart' as p;
import 'package:sqlite3/sqlite3.dart';

// ─────────────────────────────────────────────────────────────────────────────
// NIRIL BACKUP SERVICE — தரவு பாதுகாப்பு (Data Safety)
// ─────────────────────────────────────────────────────────────────────────────
// Keeps a raw copy of the SQLite database in a safe location outside the app's
// internal data directory. This backup survives app uninstall + reinstall.

import '../../cheyalpaadugal/amaippugal/tharavu/kooli_niruvana_tharavugal_provider.dart';
import '../../cheyalpaadugal/amaippugal/tharavu/pattu_niruvana_tharavugal_provider.dart';
import 'dart:async';

final backupServiceProvider = Provider<NirilBackupService>((ref) {
  throw UnimplementedError('backupServiceProvider must be overridden in ProviderScope');
});

final hasBackupProvider = FutureProvider<bool>((ref) async {
  final backupService = ref.watch(backupServiceProvider);
  return backupService.hasBackup();
});

/// Listens to all database table updates and triggers a debounced auto-backup.
final autoBackupProvider = Provider<void>((ref) {
  final backupService = ref.watch(backupServiceProvider);
  final coolieDb = ref.watch(kooliDatabaseProvider);
  final pattuDb = ref.watch(pattuDatabaseProvider);

  Timer? debounceTimer;
  void triggerAutoBackup() {
    debounceTimer?.cancel();
    debounceTimer = Timer(const Duration(seconds: 2), () {
      backupService.createBackup();
    });
  }

  final sub1 = coolieDb.tableUpdates().listen((_) => triggerAutoBackup());
  final sub2 = pattuDb.tableUpdates().listen((_) => triggerAutoBackup());

  ref.onDispose(() {
    sub1.cancel();
    sub2.cancel();
    debounceTimer?.cancel();
  });
});

class NirilBackupService {
  final String _coolieDbPath;
  final String _silkDbPath;
  final String _backupDbPath;

  NirilBackupService._({
    required String coolieDbPath,
    required String silkDbPath,
    required String backupDbPath,
  })  : _coolieDbPath = coolieDbPath,
        _silkDbPath = silkDbPath,
        _backupDbPath = backupDbPath;

  static Future<NirilBackupService> initialize() async {
    final appSupportDir = await getApplicationSupportDirectory();
    final coolieDbPath = p.join(appSupportDir.path, 'elvan_niril_coolie.db');
    final silkDbPath = p.join(appSupportDir.path, 'elvan_niril_silk.db');

    final backupDir = await _getBackupDirectory();
    final backupDbPath = p.join(backupDir, 'elvan_niril_backup.db');

    return NirilBackupService._(
      coolieDbPath: coolieDbPath,
      silkDbPath: silkDbPath,
      backupDbPath: backupDbPath,
    );
  }

  static Future<String> _getBackupDirectory() async {
    String basePath;
    if (Platform.isAndroid) {
      basePath = '/storage/emulated/0/Documents';
    } else {
      final dir = await getApplicationDocumentsDirectory();
      basePath = dir.path;
    }
    final backupDir = p.join(basePath, 'Elvan Niril', 'backup');
    final dir = Directory(backupDir);
    if (!await dir.exists()) {
      await dir.create(recursive: true);
    }
    return backupDir;
  }

  Future<void> createBackup() async {
    try {
      // Safely flush WAL to the main DB before copying
      try {
        final cDb = sqlite3.open(_coolieDbPath);
        cDb.execute('PRAGMA wal_checkpoint(TRUNCATE)');
        cDb.dispose();
      } catch (e) {
        print('Coolie checkpoint failed: $e');
      }
      
      try {
        final sDb = sqlite3.open(_silkDbPath);
        sDb.execute('PRAGMA wal_checkpoint(TRUNCATE)');
        sDb.dispose();
      } catch (e) {
        print('Silk checkpoint failed: $e');
      }

      final backupFile = File(_backupDbPath);
      final sink = backupFile.openWrite();

      final filesToPack = [
        _coolieDbPath,
        '$_coolieDbPath-wal',
        '$_coolieDbPath-shm',
        _silkDbPath,
        '$_silkDbPath-wal',
        '$_silkDbPath-shm',
      ];

      final header = ByteData(48); // 6 files * 8 bytes
      final fileBytes = <List<int>>[];

      for (int i = 0; i < filesToPack.length; i++) {
        final file = File(filesToPack[i]);
        if (await file.exists()) {
          final bytes = await file.readAsBytes();
          header.setInt64(i * 8, bytes.length, Endian.little);
          fileBytes.add(bytes);
        } else {
          header.setInt64(i * 8, 0, Endian.little);
          fileBytes.add([]);
        }
      }

      sink.add(header.buffer.asUint8List());
      for (final bytes in fileBytes) {
        sink.add(bytes);
      }
      await sink.close();
    } catch (e) {
      print('Backup failed: $e');
    }
  }

  Future<bool> restoreFromBackup() async {
    try {
      final backupFile = File(_backupDbPath);
      if (!await backupFile.exists()) return false;

      final bytes = await backupFile.readAsBytes();
      if (bytes.length < 48) return false;

      final header = ByteData.view(bytes.buffer, 0, 48);
      int offset = 48;

      final filesToUnpack = [
        _coolieDbPath,
        '$_coolieDbPath-wal',
        '$_coolieDbPath-shm',
        _silkDbPath,
        '$_silkDbPath-wal',
        '$_silkDbPath-shm',
      ];

      for (int i = 0; i < filesToUnpack.length; i++) {
        final size = header.getInt64(i * 8, Endian.little);
        final file = File(filesToUnpack[i]);
        if (size > 0) {
          // Sublist creates a view, copy it if needed, but writeAsBytes handles it
          final fileContent = bytes.sublist(offset, offset + size);
          await file.writeAsBytes(fileContent, flush: true);
          offset += size;
        } else {
          if (await file.exists()) await file.delete();
        }
      }
      return true;
    } catch (e) {
      print('Restore failed: $e');
      return false;
    }
  }

  Future<bool> hasBackup() async {
    try {
      final backupFile = File(_backupDbPath);
      if (await backupFile.exists()) {
        final length = await backupFile.length();
        if (length > 0) {
          return true;
        } else {
          try { await backupFile.delete(); } catch (_) {}
          return false;
        }
      }
      return false;
    } catch (e) {
      return false;
    }
  }

  Future<void> deleteBackup() async {
    try {
      final backupFile = File(_backupDbPath);
      if (await backupFile.exists()) {
        await backupFile.delete();
      }
    } catch (e) {
      print('Delete backup failed: $e');
    }
  }

  Future<Map<String, dynamic>?> getBackupStats() async {
    try {
      final backupFile = File(_backupDbPath);
      if (await backupFile.exists()) {
        final stat = await backupFile.stat();
        return {
          'lastModified': stat.modified,
          'sizeBytes': stat.size,
        };
      }
      return null;
    } catch (e) {
      return null;
    }
  }

  Future<int> getTotalDatabaseSize() async {
    try {
      int totalSize = 0;
      final filesToCheck = [
        _coolieDbPath,
        '$_coolieDbPath-wal',
        '$_coolieDbPath-shm',
        _silkDbPath,
        '$_silkDbPath-wal',
        '$_silkDbPath-shm',
      ];
      for (final path in filesToCheck) {
        final file = File(path);
        if (await file.exists()) {
          totalSize += await file.length();
        }
      }
      return totalSize;
    } catch (e) {
      return 0;
    }
  }
}

