import 'package:flutter/cupertino.dart';
import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../../adippadai/mozhiyaakkam/k.dart';
import '../../../adippadai/mozhiyaakkam/mozhi_vazhanguthi.dart';

/// Type-safe payment mode enum with localized display names.
/// Stored as lowercase string in DB (e.g. 'cash', 'upi', 'bank_transfer').
enum SeluthiVagai {
  vangiMaatram, // Bank Transfer — வங்கி மாற்றம்
  upi,          // UPI
  panam,        // Cash — பணம்
  kaasoalai,    // Cheque — காசோலை
  attai,        // Card — அட்டை
}

extension SeluthiVagaiX on SeluthiVagai {
  /// The stored value in the database column.
  String get storedValue {
    switch (this) {
      case SeluthiVagai.panam:
        return 'cash';
      case SeluthiVagai.upi:
        return 'upi';
      case SeluthiVagai.vangiMaatram:
        return 'bank_transfer';
      case SeluthiVagai.kaasoalai:
        return 'cheque';
      case SeluthiVagai.attai:
        return 'card';
    }
  }

  /// Parse from stored string. Returns null for unknown values.
  static SeluthiVagai? fromStored(String? value) {
    if (value == null || value.isEmpty) return null;
    switch (value) {
      case 'cash':
        return SeluthiVagai.panam;
      case 'upi':
        return SeluthiVagai.upi;
      case 'bank_transfer':
        return SeluthiVagai.vangiMaatram;
      case 'cheque':
        return SeluthiVagai.kaasoalai;
      case 'card':
        return SeluthiVagai.attai;
      default:
        return null;
    }
  }

  /// Localized display name.
  String label(BuildContext context, WidgetRef ref) {
    switch (this) {
      case SeluthiVagai.panam:
        return K.kaasu.tr(context, ref);
      case SeluthiVagai.upi:
        return 'UPI';
      case SeluthiVagai.vangiMaatram:
        return K.vangiParimaatram.tr(context, ref);
      case SeluthiVagai.kaasoalai:
        return K.kaasoalai.tr(context, ref);
      case SeluthiVagai.attai:
        return K.attai.tr(context, ref);
    }
  }

  /// Whether this payment mode requires a reference/transaction ID.
  bool get needsReference => this != SeluthiVagai.panam;

  /// Cupertino icon for each mode.
  IconData get icon {
    switch (this) {
      case SeluthiVagai.panam:
        return CupertinoIcons.money_dollar_circle;
      case SeluthiVagai.upi:
        return CupertinoIcons.device_phone_portrait;
      case SeluthiVagai.vangiMaatram:
        return CupertinoIcons.building_2_fill;
      case SeluthiVagai.kaasoalai:
        return CupertinoIcons.doc_text;
      case SeluthiVagai.attai:
        return CupertinoIcons.creditcard;
    }
  }

  /// Color for pill badge.
  Color badgeColor(bool isDark) {
    switch (this) {
      case SeluthiVagai.panam:
        return isDark ? Colors.green.shade300 : Colors.green.shade700;
      case SeluthiVagai.upi:
        return isDark ? Colors.purple.shade200 : Colors.purple.shade600;
      case SeluthiVagai.vangiMaatram:
        return isDark ? Colors.blue.shade200 : Colors.blue.shade700;
      case SeluthiVagai.kaasoalai:
        return isDark ? Colors.orange.shade200 : Colors.orange.shade700;
      case SeluthiVagai.attai:
        return isDark ? Colors.teal.shade200 : Colors.teal.shade700;
    }
  }
}
