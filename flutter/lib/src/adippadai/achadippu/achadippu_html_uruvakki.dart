import 'dart:convert';
import 'package:flutter/services.dart';
import 'package:intl/intl.dart';

import '../tharavuru/uruvugal.dart';
import '../../cheyalpaadugal/niril_podhu/tharavuru/pattiyal_tharavuru.dart';
import '../oru_mozhi/oru_mozhi_vaangunar_udhavi.dart';

/// அச்சு HTML உருவாக்கி — Universal HTML generator for Invoices and Receipts
class AchadippuHtmlUruvakki {
  // Shared font base64 cache (used on Windows only, Android uses direct paths)
  static String? _b64Regular;
  static String? _b64Bold;

  static Future<void> initFonts() async {
    if (_b64Regular == null || _b64Bold == null) {
      final regularData =
          await rootBundle.load('assets/fonts/ElvanSans-Regular.woff');
      final boldData =
          await rootBundle.load('assets/fonts/ElvanSans-Bold.woff');
      _b64Regular = base64Encode(regularData.buffer.asUint8List());
      _b64Bold = base64Encode(boldData.buffer.asUint8List());
    }
  }

  static String _buildBaseStyle(bool isWindows) {
    final fontRegularSrc = isWindows
        ? "url(data:font/woff;charset=utf-8;base64,$_b64Regular) format('woff')"
        : "url('file:///android_asset/flutter_assets/assets/fonts/ElvanSans-Regular.woff') format('woff')";

    final fontBoldSrc = isWindows
        ? "url(data:font/woff;charset=utf-8;base64,$_b64Bold) format('woff')"
        : "url('file:///android_asset/flutter_assets/assets/fonts/ElvanSans-Bold.woff') format('woff')";

    return '''
      @font-face {
        font-family: 'ElvanSans';
        src: $fontRegularSrc;
        font-weight: 400;
      }
      @font-face {
        font-family: 'ElvanSans';
        src: $fontBoldSrc;
        font-weight: 700;
      }
      * { box-sizing: border-box; margin: 0; padding: 0; }
      html, body {
        width: 794px;
        min-height: 1123px;
        font-family: 'ElvanSans', sans-serif;
        background-color: #ffffff;
      }
      .page {
        width: 794px;
        min-height: 1123px;
        padding: 40px;
        background-color: #ffffff;
        display: flex;
        flex-direction: column;
      }
      .header {
        text-align: center;
        margin-bottom: 20px;
        border-bottom: 2px solid #333;
        padding-bottom: 10px;
      }
      .header h1 { font-size: 28px; margin-bottom: 4px; }
      .header p { font-size: 14px; color: #555; }
      
      .info-row {
        display: flex;
        justify-content: space-between;
        margin-bottom: 20px;
        font-size: 14px;
      }
      
      table {
        width: 100%;
        border-collapse: collapse;
        margin-bottom: 20px;
      }
      th, td {
        border: 1px solid #ddd;
        padding: 8px;
        text-align: left;
        font-size: 14px;
      }
      th { background-color: #f2f2f2; font-weight: 700; }
      .right { text-align: right; }
      .center { text-align: center; }
      
      .totals {
        width: 50%;
        float: right;
      }
      .totals table th, .totals table td { border: none; padding: 4px; }
      
      .footer {
        margin-top: auto;
        text-align: center;
        font-size: 12px;
        color: #777;
        border-top: 1px solid #ddd;
        padding-top: 10px;
      }
    ''';
  }

  /// Generates HTML for an Invoice (Pattiyal)
  static Future<String> generatePattiyalHtml({
    required PattiyalTharavuru pattiyal,
    required dynamic profile,
    required bool isWindows,
    bool isKooli = false,
  }) async {
    if (isWindows) await initFonts();

    final df = DateFormat('dd/MM/yyyy');
    final dateStr = df.format(pattiyal.pattiyalNaal);
    
    // Fallbacks for profile
    final companyName = profile.niruvanathinPeyar != null 
        ? OruMozhiVaangunarUdhavi.mudhanmaiPeyarFromMap(profile.niruvanathinPeyar.cast<String, dynamic>(), 'ta')
        : 'Elvan Niril';
    
    final address = profile.mugavari != null 
        ? OruMozhiVaangunarUdhavi.mudhanmaiPeyarFromMap(profile.mugavari.cast<String, dynamic>(), 'ta')
        : '';
        
    final phone = profile.tholaipaesi1 != null && profile.tholaipaesi1.isNotEmpty 
        ? 'Phone: \${profile.tholaipaesi1}' 
        : '';

    final vaangunarStr = OruMozhiVaangunarUdhavi.mudhanmaiPeyarFromMap(
      pattiyal.vaangunarPeyar.cast<String, dynamic>(),
      'ta',
    );
    final vaangunarMugavariStr = OruMozhiVaangunarUdhavi.mudhanmaiPeyarFromMap(
      pattiyal.vaangunarMunvari.cast<String, dynamic>(),
      'ta',
    );

    String rowsHtml = '';
    int index = 1;
    double subTotal = 0;

    if (!isKooli) {
      final items = PattiyalUthavigal.pattuListFromJson(pattiyal.tharavugal);
      for (final item in items) {
        final amount = item.alavu * item.vilai;
        subTotal += amount;
        rowsHtml += '''
          <tr>
            <td class="center">\$index</td>
            <td>\${item.porulPeyar}</td>
            <td class="right">\${item.alavu} \${item.alagu}</td>
            <td class="right">₹\${item.vilai.toStringAsFixed(2)}</td>
            <td class="right">₹\${amount.toStringAsFixed(2)}</td>
          </tr>
        ''';
        index++;
      }
    } else {
      final items = PattiyalUthavigal.kooliListFromJson(pattiyal.tharavugal);
      for (final item in items) {
        final amount = item.edai * item.vilai;
        subTotal += amount;
        rowsHtml += '''
          <tr>
            <td class="center">\$index</td>
            <td>\${item.porulPeyar}</td>
            <td class="right">\${item.edai} Kg</td>
            <td class="right">₹\${item.vilai.toStringAsFixed(2)}</td>
            <td class="right">₹\${amount.toStringAsFixed(2)}</td>
          </tr>
        ''';
        index++;
      }
    }

    final total = subTotal; // simplified for now

    return '''
<!DOCTYPE html>
<html>
  <head>
    <meta charset="utf-8">
    <meta name="viewport" content="width=794, initial-scale=1.0, user-scalable=no">
    <style>\${_buildBaseStyle(isWindows)}</style>
  </head>
  <body>
    <div class="page">
      <div class="header">
        <h1>\$companyName</h1>
        <p>\$address</p>
        <p>\$phone</p>
        <h2>பட்டியல் / INVOICE</h2>
      </div>
      
      <div class="info-row">
        <div>
          <strong>பெறுநர் (Billed To):</strong><br>
          \$vaangunarStr<br>
          \$vaangunarMugavariStr
        </div>
        <div class="right">
          <strong>பட்டியல் எண் (Inv No):</strong> \${pattiyal.patrucheettuEn}<br>
          <strong>தேதி (Date):</strong> \$dateStr
        </div>
      </div>
      
      <table>
        <thead>
          <tr>
            <th class="center" style="width: 50px;">#</th>
            <th>விவரம் (Description)</th>
            <th class="right">அளவு (Qty)</th>
            <th class="right">விலை (Rate)</th>
            <th class="right">தொகை (Amount)</th>
          </tr>
        </thead>
        <tbody>
          \$rowsHtml
        </tbody>
      </table>
      
      <div class="totals">
        <table>
          <tr>
            <th>மொத்தம் (Sub Total):</th>
            <td class="right">₹\${subTotal.toStringAsFixed(2)}</td>
          </tr>
          <tr>
            <th><strong>இறுதித் தொகை (Grand Total):</strong></th>
            <td class="right"><strong>₹\${total.toStringAsFixed(2)}</strong></td>
          </tr>
        </table>
      </div>
      
      <div style="clear: both;"></div>
      
      <div class="footer">
        Thank you for your business! | Created with Elvan Niril
      </div>
    </div>
  </body>
</html>
''';
  }
}
