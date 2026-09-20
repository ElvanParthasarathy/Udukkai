import 'dart:convert';
import 'package:flutter_test/flutter_test.dart';
import 'package:elvan_niril/src/adippadai/tharavuthalam/seyali_tharavuthalam.dart';

/// Tests for MozhiMapConverter (bilingual JSON ↔ Map conversion).
void main() {
  const converter = MozhiMapConverter();

  group('MozhiMapConverter', () {
    test('fromSql: valid JSON maps correctly', () {
      final json = jsonEncode({'Tamil': 'எல்வன்', 'English': 'Elvan'});
      final result = converter.fromSql(json);

      expect(result, {'Tamil': 'எல்வன்', 'English': 'Elvan'});
    });

    test('fromSql: empty string returns empty map', () {
      expect(converter.fromSql(''), isEmpty);
    });

    test('fromSql: empty JSON object returns empty map', () {
      expect(converter.fromSql('{}'), isEmpty);
    });

    test('fromSql: invalid JSON returns empty map (no crash)', () {
      expect(converter.fromSql('not json at all'), isEmpty);
    });

    test('fromSql: null values in JSON are converted to strings', () {
      final json = jsonEncode({'Tamil': null, 'English': 'test'});
      final result = converter.fromSql(json);

      expect(result['Tamil'], 'null');
      expect(result['English'], 'test');
    });

    test('fromSql: numeric values in JSON are converted to strings', () {
      final json = jsonEncode({'Tamil': 123});
      final result = converter.fromSql(json);

      expect(result['Tamil'], '123');
    });

    test('toSql: map encodes to valid JSON', () {
      final map = {'Tamil': 'எல்வன்', 'English': 'Elvan'};
      final result = converter.toSql(map);

      expect(result, jsonEncode(map));
    });

    test('toSql: empty map encodes to empty JSON object', () {
      expect(converter.toSql({}), '{}');
    });

    test('Round-trip: toSql then fromSql preserves data', () {
      final original = {'Tamil': 'சென்னை', 'English': 'Chennai'};
      final json = converter.toSql(original);
      final restored = converter.fromSql(json);

      expect(restored, original);
    });

    test('fromSql: handles Unicode Tamil text correctly', () {
      final json = jsonEncode({
        'Tamil': 'நிறுவனத்தின் பெயர்',
        'English': 'Business Name',
      });
      final result = converter.fromSql(json);

      expect(result['Tamil'], 'நிறுவனத்தின் பெயர்');
    });
  });
}
