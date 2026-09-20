import 'dart:async';
import 'dart:io';

import 'package:flutter/foundation.dart';
import 'package:path_provider/path_provider.dart';
import 'package:path/path.dart' as p;

// ─────────────────────────────────────────────────────────────────────────────
// ELVAN பிழைப்பதிவு — Global Error Logger
// ─────────────────────────────────────────────────────────────────────────────
// Catches all uncaught Flutter framework errors, async errors, and platform
// errors. Writes them to a local log file in Documents/Elvan Niril/logs/.
// Keeps the last 5 log files and rotates older ones to save storage.

class ElvanPizhaipadhivu {
  static File? _logFile;
  static const int _maxLogFiles = 5;

  /// Initialize the logger. Call this once in main() before runApp.
  static Future<void> initialize() async {
    final logDir = await _getLogDirectory();
    if (!await logDir.exists()) {
      await logDir.create(recursive: true);
    }

    // Create a log file named with the current date
    final now = DateTime.now();
    final fileName =
        'niril_log_${now.year}-${now.month.toString().padLeft(2, '0')}-${now.day.toString().padLeft(2, '0')}.log';
    _logFile = File(p.join(logDir.path, fileName));

    // Rotate old log files
    await _rotateLogFiles(logDir);

    // Write session header
    await _write(
      '═══════════════════════════════════════════════════\n'
      '  Elvan Niril — Session Started\n'
      '  ${now.toIso8601String()}\n'
      '  Platform: ${Platform.operatingSystem} ${Platform.operatingSystemVersion}\n'
      '═══════════════════════════════════════════════════\n',
    );
  }

  /// Log an error with optional stack trace.
  static Future<void> logError(
    dynamic error, {
    StackTrace? stackTrace,
    String? context,
  }) async {
    final now = DateTime.now();
    final buffer = StringBuffer();
    buffer.writeln('──── ERROR [$now] ────');
    if (context != null) {
      buffer.writeln('Context: $context');
    }
    buffer.writeln('Error: $error');
    if (stackTrace != null) {
      buffer.writeln('Stack Trace:');
      buffer.writeln(stackTrace.toString());
    }
    buffer.writeln();

    // Always print to debug console
    if (kDebugMode) {
      debugPrint('\x1B[31m${buffer.toString()}\x1B[0m'); // Red text
    }

    await _write(buffer.toString());
  }

  /// Log a warning (non-fatal).
  static Future<void> logWarning(String message) async {
    final now = DateTime.now();
    final line = '⚠ WARNING [$now]: $message\n';

    if (kDebugMode) {
      debugPrint('\x1B[33m$line\x1B[0m'); // Yellow text
    }

    await _write(line);
  }

  /// Log general info.
  static Future<void> logInfo(String message) async {
    final now = DateTime.now();
    final line = 'ℹ INFO [$now]: $message\n';

    if (kDebugMode) {
      debugPrint(line);
    }

    await _write(line);
  }

  // ── Private Helpers ──

  static Future<void> _write(String text) async {
    try {
      await _logFile?.writeAsString(text, mode: FileMode.append);
    } catch (e) {
      // If writing to the log file itself fails, just print to console.
      // We never want the logger to crash the app.
      debugPrint('[ElvanPizhaipadhivu] Failed to write to log file: $e');
    }
  }

  static Future<Directory> _getLogDirectory() async {
    if (Platform.isWindows || Platform.isMacOS || Platform.isLinux) {
      // Desktop: Documents/Elvan Niril/logs/
      final home = Platform.environment['USERPROFILE'] ??
          Platform.environment['HOME'] ??
          '.';
      return Directory(p.join(home, 'Documents', 'Elvan Niril', 'logs'));
    } else {
      // Mobile: Application Documents/logs/
      final docsDir = await getApplicationDocumentsDirectory();
      return Directory(p.join(docsDir.path, 'Elvan Niril', 'logs'));
    }
  }

  static Future<void> _rotateLogFiles(Directory logDir) async {
    try {
      final logFiles = logDir
          .listSync()
          .whereType<File>()
          .where((f) => f.path.endsWith('.log'))
          .toList();

      // Sort by modification time (oldest first)
      logFiles.sort(
          (a, b) => a.lastModifiedSync().compareTo(b.lastModifiedSync()));

      // Delete oldest files if we have more than _maxLogFiles
      while (logFiles.length >= _maxLogFiles) {
        await logFiles.removeAt(0).delete();
      }
    } catch (_) {
      // Rotation failure is non-critical
    }
  }
}
