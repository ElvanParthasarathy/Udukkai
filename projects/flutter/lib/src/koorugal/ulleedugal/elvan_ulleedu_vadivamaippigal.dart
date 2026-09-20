import 'package:flutter/services.dart';

/// Allows digits and at most ONE decimal point.
/// Industry-standard for currency/price fields (Google Pay, Razorpay).
/// Name: ஒன்றே புள்ளி (Only One Dot)
class OndraePulli extends TextInputFormatter {
  @override
  TextEditingValue formatEditUpdate(
    TextEditingValue oldValue,
    TextEditingValue newValue,
  ) {
    // Allow empty
    if (newValue.text.isEmpty) return newValue;
    // Block if more than one dot
    if ('.'.allMatches(newValue.text).length > 1) return oldValue;
    // Block if contains non-numeric, non-dot chars
    if (!RegExp(r'^[0-9.]*$').hasMatch(newValue.text)) return oldValue;
    return newValue;
  }
}

/// Forces text to UPPERCASE (for GSTIN, IFSC, PAN).
/// Name: பெரிய எழுத்து (Capital Letters)
class PeriyaEzhuthu extends TextInputFormatter {
  @override
  TextEditingValue formatEditUpdate(
    TextEditingValue oldValue,
    TextEditingValue newValue,
  ) {
    return TextEditingValue(
      text: newValue.text.toUpperCase(),
      selection: newValue.selection,
    );
  }
}

/// Standard formatters for quick reuse across the app.
/// Name: எல்வன் வடிவமைப்பிகள் (Elvan Formatters)
class ElvanVadivamaippigal {
  /// Digits only — Pincode, Bank Account, Invoice #, HSN
  static List<TextInputFormatter> get enngalMattum => [
        FilteringTextInputFormatter.digitsOnly,
      ];

  /// Decimal (single dot) — Price, Qty, Rate, Discount, Tax %
  static List<TextInputFormatter> get thasamamEnngal => [
        FilteringTextInputFormatter.allow(RegExp(r'[0-9.]')),
        OndraePulli(),
      ];

  /// Phone — digits, +, -, space
  static List<TextInputFormatter> get tholaippaesi => [
        FilteringTextInputFormatter.allow(RegExp(r'[0-9+\- ]')),
      ];

  /// Uppercase alphanumeric — GSTIN, IFSC
  static List<TextInputFormatter> get periyaEzhuthuEnngal => [
        FilteringTextInputFormatter.allow(RegExp(r'[a-zA-Z0-9]')),
        PeriyaEzhuthu(),
      ];
}
