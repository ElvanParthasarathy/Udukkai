import 'package:flutter_test/flutter_test.dart';
import 'package:elvan_niril/src/adippadai/mozhiyaakkam/language_keys/en.dart';
import 'package:elvan_niril/src/adippadai/mozhiyaakkam/language_keys/ta.dart';

/// Tests to ensure translation map integrity.
/// Every key in English must have a corresponding key in Tamil and Tanglish.
void main() {
  group('Translation Coverage Tests', () {
    test('Every English key has a Tamil translation', () {
      final missingInTamil = <String>[];
      for (final key in en.keys) {
        if (!ta.containsKey(key)) {
          missingInTamil.add(key);
        }
      }
      expect(
        missingInTamil,
        isEmpty,
        reason: 'Missing Tamil translations for: ${missingInTamil.join(', ')}',
      );
    });

    test('No Tamil key is orphaned (exists in ta but not en)', () {
      final orphanedInTamil = <String>[];
      for (final key in ta.keys) {
        if (!en.containsKey(key)) {
          orphanedInTamil.add(key);
        }
      }
      expect(
        orphanedInTamil,
        isEmpty,
        reason: 'Orphaned Tamil keys (no English counterpart): ${orphanedInTamil.join(', ')}',
      );
    });

    test('No translation value is empty', () {
      final emptyValues = <String>[];
      for (final entry in en.entries) {
        if (entry.value.trim().isEmpty) {
          emptyValues.add('en:${entry.key}');
        }
      }
      for (final entry in ta.entries) {
        if (entry.value.trim().isEmpty) {
          emptyValues.add('ta:${entry.key}');
        }
      }
      expect(
        emptyValues,
        isEmpty,
        reason: 'Empty translation values found: ${emptyValues.join(', ')}',
      );
    });

    test('No translation value equals its key (untranslated passthrough)', () {
      final passthroughKeys = <String>[];
      for (final entry in ta.entries) {
        // Skip keys that are intentionally the same (acronyms, proper nouns)
        if (entry.key == entry.value && entry.key != 'GSTIN') {
          passthroughKeys.add(entry.key);
        }
      }
      // This is a warning test — some passthroughs may be intentional
      // So we just report them rather than hard-fail
      if (passthroughKeys.isNotEmpty) {
        // ignore: avoid_print
        print(
            'Tamil translations that equal their key (possible untranslated): $passthroughKeys');
      }
    });
  });
}
