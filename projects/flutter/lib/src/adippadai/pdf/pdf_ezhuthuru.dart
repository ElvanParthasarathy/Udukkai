import 'package:flutter/services.dart' show rootBundle;
import 'package:pdf/pdf.dart';
import 'package:pdf/widgets.dart' as pw;

/// எழுத்துரு — PDF font loader & cache for ElvanSans Tamil font.
///
/// Loads TTF font data from bundled assets and provides `pw.Font`
/// instances for use in PDF documents. Fonts are cached after first load.
class PdfEzhuthuru {
  PdfEzhuthuru._();

  static pw.Font? _regular;
  static pw.Font? _medium;
  static pw.Font? _semiBold;
  static pw.Font? _bold;

  /// Load all font weights. Call once before building PDFs.
  static Future<void> load() async {
    if (_regular != null) return; // Already loaded

    final regularData = await rootBundle.load('assets/fonts/ElvanSans-Regular.ttf');
    final mediumData = await rootBundle.load('assets/fonts/ElvanSans-Medium.ttf');
    final semiBoldData = await rootBundle.load('assets/fonts/ElvanSans-SemiBold.ttf');
    final boldData = await rootBundle.load('assets/fonts/ElvanSans-Bold.ttf');

    _regular = pw.Font.ttf(regularData);
    _medium = pw.Font.ttf(mediumData);
    _semiBold = pw.Font.ttf(semiBoldData);
    _bold = pw.Font.ttf(boldData);
  }

  static pw.Font get regular {
    assert(_regular != null, 'Call PdfEzhuthuru.load() before accessing fonts');
    return _regular!;
  }

  static pw.Font get medium {
    assert(_medium != null, 'Call PdfEzhuthuru.load() before accessing fonts');
    return _medium!;
  }

  static pw.Font get semiBold {
    assert(_semiBold != null, 'Call PdfEzhuthuru.load() before accessing fonts');
    return _semiBold!;
  }

  static pw.Font get bold {
    assert(_bold != null, 'Call PdfEzhuthuru.load() before accessing fonts');
    return _bold!;
  }

  /// Get a pw.TextStyle with the appropriate font weight.
  static pw.TextStyle style({
    double fontSize = 12,
    pw.FontWeight fontWeight = pw.FontWeight.normal,
    PdfColor? color,
    double? letterSpacing,
    double? lineSpacing,
    pw.FontStyle fontStyle = pw.FontStyle.normal,
  }) {
    pw.Font font;
    switch (fontWeight) {
      case pw.FontWeight.bold:
        font = bold;
        break;
      default:
        font = regular;
        break;
    }

    return pw.TextStyle(
      font: font,
      fontBold: bold,
      fontSize: fontSize,
      fontWeight: fontWeight,
      color: color,
      letterSpacing: letterSpacing,
      lineSpacing: lineSpacing,
      fontStyle: fontStyle,
    );
  }

  /// Get the correct font for a specific weight value (400, 500, 600, 700).
  static pw.Font fontForWeight(int weight) {
    if (weight >= 700) return bold;
    if (weight >= 600) return semiBold;
    if (weight >= 500) return medium;
    return regular;
  }

  /// Build a pw.TextStyle with a specific numeric weight.
  static pw.TextStyle weightedStyle({
    double fontSize = 12,
    int weight = 400,
    PdfColor? color,
    double? letterSpacing,
    double? lineSpacing,
  }) {
    return pw.TextStyle(
      font: fontForWeight(weight),
      fontBold: bold,
      fontSize: fontSize,
      color: color,
      letterSpacing: letterSpacing,
      lineSpacing: lineSpacing,
    );
  }
}
