import 'package:flutter_test/flutter_test.dart';
import 'package:elvan_niril/src/cheyalpaadugal/amaippugal/tharavu/niruvana_tharavugal.dart';

/// Tests for the NiruvanaTharavugal domain model.
void main() {
  group('NiruvanaTharavugal Model', () {
    test('Default constructor creates valid empty model', () {
      final model = NiruvanaTharavugal();

      expect(model.id, isNull);
      expect(model.mudhanMozhi, 'ta');
      expect(model.thunaiMozhi, 'en');
      expect(model.iruMozhi, isTrue);
      expect(model.niruvanathinPeyar, isEmpty);
      expect(model.kurumPeyar, isEmpty);
      expect(model.tholaipaesi1, isEmpty);
      expect(model.minnanjal, isEmpty);
      expect(model.gstin, isEmpty);
      expect(model.mugavari, isEmpty);
      expect(model.vangiPeyar, isEmpty);
      expect(model.oavuru, isEmpty);
      expect(model.thalaippuVadivu, 'small');
    });

    test('Constructor with bilingual data works correctly', () {
      final model = NiruvanaTharavugal(
        id: 1,
        niruvanathinPeyar: {'ta': 'எல்வன்', 'en': 'Elvan'},
        mugavari: {'ta': 'சென்னை', 'en': 'Chennai'},
        iruMozhi: true,
      );

      expect(model.id, 1);
      expect(model.niruvanathinPeyar['ta'], 'எல்வன்');
      expect(model.niruvanathinPeyar['en'], 'Elvan');
      expect(model.mugavari['ta'], 'சென்னை');
    });

    test('getPrimary returns correct language value', () {
      final model = NiruvanaTharavugal(
        mudhanMozhi: 'ta',
        niruvanathinPeyar: {'ta': 'எல்வன்', 'en': 'Elvan'},
      );

      expect(model.getPrimary('niruvanathinPeyar'), 'எல்வன்');
    });

    test('getSecondary returns correct language value', () {
      final model = NiruvanaTharavugal(
        thunaiMozhi: 'en',
        niruvanathinPeyar: {'ta': 'எல்வன்', 'en': 'Elvan'},
      );

      expect(model.getSecondary('niruvanathinPeyar'), 'Elvan');
    });

    test('getPrimary returns empty string for missing language', () {
      final model = NiruvanaTharavugal(
        mudhanMozhi: 'French',
        niruvanathinPeyar: {'ta': 'எல்வன்'},
      );

      expect(model.getPrimary('niruvanathinPeyar'), 'எல்வன்');
    });

    test('setBilingual updates the correct field', () {
      final model = NiruvanaTharavugal();
      model.setBilingual('vangiPeyar', 'ta', 'இந்தியன் வங்கி');
      model.setBilingual('vangiPeyar', 'en', 'Indian Bank');

      expect(model.vangiPeyar['ta'], 'இந்தியன் வங்கி');
      expect(model.vangiPeyar['en'], 'Indian Bank');
    });

    test('copyWith creates independent deep copy', () {
      final original = NiruvanaTharavugal(
        id: 1,
        niruvanathinPeyar: {'ta': 'ஒன்று'},
        kurumPeyar: 'Original',
      );

      final copy = original.copyWith(kurumPeyar: 'Copy');

      // Verify the copy has the new value
      expect(copy.kurumPeyar, 'Copy');
      // Verify the original is unchanged
      expect(original.kurumPeyar, 'Original');
      // Verify bilingual maps are independent (deep copy)
      copy.niruvanathinPeyar['ta'] = 'மாற்றம்';
      expect(original.niruvanathinPeyar['ta'], 'ஒன்று');
    });

    test('copyWith preserves all fields when no overrides given', () {
      final original = NiruvanaTharavugal(
        id: 42,
        mudhanMozhi: 'en',
        thunaiMozhi: 'ta',
        iruMozhi: false,
        kurumPeyar: 'Test',
        tholaipaesi1: '9876543210',
        minnanjal: 'test@test.com',
        gstin: '33AAAAA0000A1Z5',
        anjalKuriyeedu: '600001',
        vangiKanakku: '1234567890',
        ifsc: 'IDIB000C001',
        thalaippuVadivu: 'wide',
        upiId: 'test@upi',
      );

      final copy = original.copyWith();

      expect(copy.id, 42);
      expect(copy.mudhanMozhi, 'en');
      expect(copy.thunaiMozhi, 'ta');
      expect(copy.iruMozhi, false);
      expect(copy.kurumPeyar, 'Test');
      expect(copy.tholaipaesi1, '9876543210');
      expect(copy.minnanjal, 'test@test.com');
      expect(copy.gstin, '33AAAAA0000A1Z5');
      expect(copy.anjalKuriyeedu, '600001');
      expect(copy.vangiKanakku, '1234567890');
      expect(copy.ifsc, 'IDIB000C001');
      expect(copy.thalaippuVadivu, 'wide');
      expect(copy.upiId, 'test@upi');
    });
  });

  group('NiruvanaTharavugal Bilingual Field Resolution', () {
    test('_getBilingualMap returns empty map for unknown field', () {
      final model = NiruvanaTharavugal();
      // getPrimary calls _getBilingualMap internally
      expect(model.getPrimary('nonExistentField'), isEmpty);
    });

    test('All bilingual field names resolve correctly', () {
      final model = NiruvanaTharavugal(
        niruvanathinPeyar: {'ta': 'a'},
        mugavari: {'ta': 'b'},
        oor: {'ta': 'c'},
        maavattam: {'ta': 'd'},
        maanilam: {'ta': 'e'},
        naadu: {'ta': 'f'},
        vangiPeyar: {'ta': 'g'},
        kilai: {'ta': 'h'},
        adaimozhi: {'ta': 'i'},
      );

      expect(model.getPrimary('niruvanathinPeyar'), 'a');
      expect(model.getPrimary('mugavari'), 'b');
      expect(model.getPrimary('oor'), 'c');
      expect(model.getPrimary('maavattam'), 'd');
      expect(model.getPrimary('maanilam'), 'e');
      expect(model.getPrimary('naadu'), 'இந்தியா');
      expect(model.getPrimary('vangiPeyar'), 'g');
      expect(model.getPrimary('kilai'), 'h');
      expect(model.getPrimary('adaimozhi'), 'i');
    });
  });
}
