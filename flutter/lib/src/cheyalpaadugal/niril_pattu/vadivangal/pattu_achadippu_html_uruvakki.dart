import 'dart:convert';
import 'package:flutter/services.dart';
import 'package:intl/intl.dart';
import '../../../adippadai/achadippu/achadippu_html_uruvakki.dart';
import '../../../adippadai/tharavuru/uruvugal.dart';

import '../../niril_podhu/tharavuru/pattiyal_tharavuru.dart';
import '../../niril_podhu/kalanjiyam/pattiyal_kanakku.dart';
import '../../../adippadai/oru_mozhi/oru_mozhi_vaangunar_udhavi.dart';
import '../../../adippadai/iru_mozhi/iru_mozhi_porul_udhavi.dart';
import '../../../adippadai/elvan_navil_ezhuthen/elvan_navil_ezhuthen.dart';

/// பட்டு அச்சடிப்பு HTML உருவாக்கி — Silk Invoice HTML Generator
///
/// Loads the exact sjs_gst_intra.html or sjs_simple.html template from assets (unmodified copy
/// of Elvan Niril Achugal) and replaces only the sample content with real
/// invoice data at runtime. The HTML/CSS structure is never altered.
class PattuAchadippuHtmlUruvakki {

  static String? _b64Regular;
  static String? _b64Medium;
  static String? _b64SemiBold;
  static String? _b64Bold;

  static Future<void> _initWindowsFonts() async {
    if (_b64Regular == null) {
      _b64Regular = base64Encode((await rootBundle.load('assets/templates/silk/fonts/ElvanSans-Regular.woff')).buffer.asUint8List());
      _b64Medium = base64Encode((await rootBundle.load('assets/templates/silk/fonts/ElvanSans-Medium.woff')).buffer.asUint8List());
      _b64SemiBold = base64Encode((await rootBundle.load('assets/templates/silk/fonts/ElvanSans-SemiBold.woff')).buffer.asUint8List());
      _b64Bold = base64Encode((await rootBundle.load('assets/templates/silk/fonts/ElvanSans-Bold.woff')).buffer.asUint8List());
    }
  }

  static Future<String> generatePattiyalHtml({
    required PattiyalTharavuru pattiyal,
    required dynamic profile,
    VaangunarTharavuru? client,
    required bool isWindows,
    required bool isBilingual,
    required String mudhanmaiLang,
    required String irandaamLang,
  }) async {
    final bool showGstSplits = profile.gstPirippugal ?? false;
    final String templateName = showGstSplits ? 'sjs_gst_intra.html' : 'sjs_simple.html';

    // 1. Load the EXACT template + CSS from assets (identical to Achugal folder)
    String html = await rootBundle.loadString('assets/templates/silk/$templateName');
    final invoiceCss = await rootBundle.loadString('assets/templates/silk/invoice.css');
    final printCss = await rootBundle.loadString('assets/templates/silk/print.css');



    // 2. Resolve font paths and inline CSS
    String finalCss = invoiceCss;
    if (isWindows) {
      await _initWindowsFonts();
      finalCss = finalCss.replaceAll(
        "url('fonts/ElvanSans-Regular.woff')",
        "url(data:font/woff;charset=utf-8;base64,$_b64Regular)"
      ).replaceAll(
        "url('fonts/ElvanSans-Medium.woff')",
        "url(data:font/woff;charset=utf-8;base64,$_b64Medium)"
      ).replaceAll(
        "url('fonts/ElvanSans-SemiBold.woff')",
        "url(data:font/woff;charset=utf-8;base64,$_b64SemiBold)"
      ).replaceAll(
        "url('fonts/ElvanSans-Bold.woff')",
        "url(data:font/woff;charset=utf-8;base64,$_b64Bold)"
      );
    } else {
      finalCss = finalCss.replaceAll(
        "url('fonts/",
        "url('file:///android_asset/flutter_assets/assets/templates/silk/fonts/"
      );
    }
    
    // Fix Footer to absolute bottom in print using Option 1
    finalCss += '''\n
    @media print {
      @page {
        size: A4;
        margin: 0 !important;
      }
      html, body {
        margin: 0 !important;
        padding: 0 !important;
        height: 100% !important;
        background-color: white !important;
      }
      .invoice-preview-container {
        width: 210mm !important;
        margin: 0 auto !important;
        box-shadow: none !important;
        border: none !important;
        min-height: 100% !important;
        height: auto !important;
        position: relative !important;
      }
      .inv-contact-block {
        position: fixed !important;
        bottom: 0 !important;
        left: 0 !important;
        width: 100% !important;
        background: var(--accent-bg-light) !important;
        z-index: 999 !important;
      }
      .unified-table-box {
        margin-bottom: 80px !important; /* Prevent overlapping with fixed footer */
      }
    }
    ''';

    html = html.replaceFirst(
      '<link rel="stylesheet" href="invoice.css">',
      '<style>\n$finalCss\n</style>',
    );
    html = html.replaceFirst(
      '<link rel="stylesheet" href="print.css">',
      '<style>\n$printCss\n</style>',
    );

    // ─── Extract real data from database ───
    final df = DateFormat('dd/MM/yyyy');
    final dateStr = df.format(pattiyal.pattiyalNaal);

    String resolveHybridValue(dynamic field) {
      if (field == null) return '';
      Map<String, dynamic> parsedMap = {};
      try {
        parsedMap = Map<String, dynamic>.from(field);
      } catch (e) {
        return field.toString();
      }
      if (parsedMap.isEmpty) return '';
      final langToUse = isBilingual ? irandaamLang : mudhanmaiLang;
      return OruMozhiVaangunarUdhavi.mudhanmaiPeyarFromMap(parsedMap, langToUse);
    }

    // Profile (business) details
    final companyNameTa = profile.niruvanathinPeyar != null
        ? OruMozhiVaangunarUdhavi.mudhanmaiPeyarFromMap(
            profile.niruvanathinPeyar.cast<String, dynamic>(), mudhanmaiLang)
        : '';
    final companyNameEn = (isBilingual && profile.niruvanathinPeyar != null)
        ? OruMozhiVaangunarUdhavi.mudhanmaiPeyarFromMap(
            profile.niruvanathinPeyar.cast<String, dynamic>(), irandaamLang)
        : '';
    final signatureName = isBilingual ? (companyNameEn.isNotEmpty ? companyNameEn : companyNameTa) : companyNameTa;

    final bizAddress1 = resolveHybridValue(profile.mugavari);
    final bizAddress2 = resolveHybridValue(profile.oor);
    final bizPhone = profile.tholaipaesi1 ?? '';
    final bizEmail = profile.minnanjal ?? '';
    final bizGstin = profile.gstin ?? '';

    // Client details
    final clientNameStr = client != null
        ? resolveHybridValue(client.peyar)
        : resolveHybridValue(pattiyal.vaangunarPeyar);
    
    final clientAddr1 = client != null
        ? resolveHybridValue(client.mugavari)
        : resolveHybridValue(pattiyal.vaangunarMunvari);
        
    final clientAddr2 = client != null
        ? [
            resolveHybridValue(client.oor),
            resolveHybridValue(client.maavattam),
            resolveHybridValue(client.maanilam),
            client.anjalKuriyeedu,
          ].where((e) => e != null && e.toString().isNotEmpty).join(', ')
        : '';
        
    final clientGstin = client?.gstin ?? '';
    final clientPhone = client?.tholaipaesi ?? '';
    final clientState = client != null
        ? resolveHybridValue(client.maanilam)
        : (isBilingual ? (irandaamLang == 'ta' ? 'தமிழ்நாடு' : 'Tamil Nadu') : (mudhanmaiLang == 'ta' ? 'தமிழ்நாடு' : 'Tamil Nadu'));

    // Bank
    final bankName = resolveHybridValue(profile.vangiPeyar);
    final accountNo = profile.vangiKanakku ?? '';
    final ifsc = profile.ifsc ?? '';

    // ─── Build item rows (exact same HTML structure as the template) ───
    String itemRows = '';
    int index = 1;
    int totalQty = 0;
    String commonHSN = '';

    final items = PattiyalUthavigal.pattuListFromJson(pattiyal.tharavugal);
    
    bool hasAnyDiscount = pattiyal.podhuThallupadiMathippu > 0 || pattiyal.thallupadi > 0;
    if (!hasAnyDiscount) {
      for (final item in items) {
        if (item.thallupadi > 0) {
          hasAnyDiscount = true;
          break;
        }
      }
    }

    final totals = PattuKanakku.calculate(
      items: items,
      globalDiscountValue: pattiyal.podhuThallupadiMathippu,
      globalDiscountType: pattiyal.podhuThallupadiVagai,
      businessState: resolveHybridValue(profile.maanilam),
      customerState: clientState,
      country: resolveHybridValue(profile.naadu),
    );

    double afterItemDiscountSubtotal = 0;
    for (final item in items) {
       afterItemDiscountSubtotal += item.alavu * item.vilai - item.thallupadiThogai;
    }

    for (final item in items) {
      final amount = item.alavu * item.vilai;
      final cgstRate = item.variVizhukkaadu / 2;
      final sgstRate = item.variVizhukkaadu / 2;
      
      final double itemDiscountAmt = item.thallupadiThogai;
      final double afterItemDiscount = amount - itemDiscountAmt;
      
      final double globalDiscountAmount = totals.thallupadiMothangal - (items.fold(0.0, (sum, i) => sum + i.thallupadiThogai));
      final double itemWeight = afterItemDiscountSubtotal > 0 ? afterItemDiscount / afterItemDiscountSubtotal : 0;
      final double itemGlobalDiscount = globalDiscountAmount * itemWeight;
      final double taxableAmount = afterItemDiscount - itemGlobalDiscount;
      
      final double cgstAmt = (taxableAmount > 0 ? taxableAmount : 0) * (cgstRate / 100);
      final double sgstAmt = (taxableAmount > 0 ? taxableAmount : 0) * (sgstRate / 100);
      final rowTotal = taxableAmount + cgstAmt + sgstAmt;

      totalQty += item.alavu.toInt();

      if (commonHSN.isEmpty && item.hsnKuriyeedu.isNotEmpty) {
        commonHSN = item.hsnKuriyeedu;
      }

      final primaryName = item.mozhiMap.isNotEmpty 
          ? IruMozhiPorulUdhavi.mudhanmaiPeyar(item.mozhiMap.cast<String, dynamic>(), mudhanmaiLang)
          : item.porulPeyar;
          
      final secondaryName = item.mozhiMap.isNotEmpty 
          ? IruMozhiPorulUdhavi.thunaiPeyar(item.mozhiMap.cast<String, dynamic>(), irandaamLang, isBilingual)
          : (isBilingual ? item.porulPeyarEn : '');
          
      final displayPrimary = primaryName.isNotEmpty ? primaryName : item.porulPeyar;
      final displaySecondary = secondaryName;
      
      final discountCell = hasAnyDiscount 
          ? '<td class="inv-td inv-td-right">${itemDiscountAmt > 0 ? '₹ ${_fmt(itemDiscountAmt)}' : '-'}</td>' 
          : '';

      if (showGstSplits) {
        itemRows += '''
          <tr>
            <td class="inv-td inv-td-muted">$index</td>
            <td class="inv-td">
              <div class="item-name">$displayPrimary</div>
              ${displaySecondary.isNotEmpty ? '<div class="item-name-sec">$displaySecondary</div>' : ''}
            </td>
            <td class="inv-td inv-td-center inv-td-muted">${item.hsnKuriyeedu}</td>
            <td class="inv-td inv-td-center">${item.alavu.toInt()}</td>
            <td class="inv-td inv-td-right">₹ ${_fmt(item.vilai)}</td>
            $discountCell
            <td class="inv-td inv-td-center">${cgstRate.toStringAsFixed(1)}%</td>
            <td class="inv-td inv-td-right">₹ ${_fmt(cgstAmt)}</td>
            <td class="inv-td inv-td-center">${sgstRate.toStringAsFixed(1)}%</td>
            <td class="inv-td inv-td-right">₹ ${_fmt(sgstAmt)}</td>
            <td class="inv-td inv-td-right">₹ ${_fmt(rowTotal)}</td>
          </tr>''';
      } else {
        itemRows += '''
          <tr>
            <td class="inv-td inv-td-muted">$index</td>
            <td class="inv-td inv-td-name">
              <div class="item-name">$displayPrimary</div>
              ${displaySecondary.isNotEmpty ? '<div class="item-name-sec">$displaySecondary</div>' : ''}
            </td>
            <td class="inv-td inv-td-center inv-td-muted">${item.hsnKuriyeedu}</td>
            <td class="inv-td inv-td-center">${item.alavu.toInt()}</td>
            <td class="inv-td inv-td-right">₹ ${_fmt(item.vilai)}</td>
            $discountCell
            <td class="inv-td inv-td-right">₹ ${_fmt(rowTotal)}</td>
          </tr>''';
      }
      index++;
    }

    final discountThHtml = hasAnyDiscount ? '''<th class="inv-th inv-th-right" rowspan="2">
              <div style="font-weight: 300; font-size: 0.6rem; margin-bottom: 2px; line-height: 1.2; text-align: right; text-transform: none;">${isBilingual ? (mudhanmaiLang == 'ta' ? 'தள்ளுபடி' : 'Discount') : (mudhanmaiLang == 'ta' ? 'தள்ளுபடி' : 'Discount')}</div>
              <div style="font-weight: 300; font-size: 0.65rem; opacity: 0.65; line-height: 1; text-align: right; text-transform: none;">Discount</div>
            </th>''' : '';
            
    final discountThSimpleHtml = hasAnyDiscount ? '''<th class="inv-th inv-th-right">
              <div style="font-weight: 300; font-size: 0.6rem; margin-bottom: 2px; line-height: 1.2; text-align: right; text-transform: none;">${isBilingual ? (mudhanmaiLang == 'ta' ? 'தள்ளுபடி' : 'Discount') : (mudhanmaiLang == 'ta' ? 'தள்ளுபடி' : 'Discount')}</div>
              <div style="font-weight: 300; font-size: 0.65rem; opacity: 0.65; line-height: 1; text-align: right; text-transform: none;">Discount</div>
            </th>''' : '';

    final discountRowHtml = totals.thallupadiMothangal > 0 ? '''<div class="inv-total-row">
            <span>${mudhanmaiLang == 'ta' ? 'தள்ளுபடி' : 'Discount'}</span>
            <strong>- ₹ ${_fmt(totals.thallupadiMothangal)}</strong>
          </div>''' : '';

    html = html.replaceFirst('<!-- DISCOUNT_TH_PLACEHOLDER -->', showGstSplits ? discountThHtml : discountThSimpleHtml);
    html = html.replaceFirst('<!-- DISCOUNT_ROW_PLACEHOLDER -->', discountRowHtml);

    final double subTotal = totals.adippadaiMothangal;
    final double totalCgst = totals.cgst;
    final double totalSgst = totals.sgst;
    final double grandTotal = totals.mothaMothangal;

    // ─── 3. Runtime replacements on the EXACT template content ───
    // 4. Fix viewport zooming on mobile and print page overflow
    html = html.replaceFirst(
      '<meta name="viewport" content="width=device-width, initial-scale=1.0">',
      '<meta name="viewport" content="width=800, user-scalable=yes">',
    );
    
    // Header — business name
    html = html.replaceFirst('ஸ்ரீ சிவராம் சில்க் சாரீஸ்', companyNameTa);
    // Second occurrence of company name (signature block) — uses English
    html = html.replaceFirst('Sri Sivaram Silk Sarees', companyNameEn);
    // Signature block still has the English name
    html = html.replaceFirst('Sri Sivaram Silk Sarees', signatureName);

    // GSTIN (business)
    html = html.replaceFirst('33AABCS1234H1Z5', bizGstin);

    // Invoice number (appears in title and in meta box)
    if (showGstSplits) {
      html = html.replaceAll('SJPS-29', pattiyal.patrucheettuEn);
      html = html.replaceFirst('15/06/2026', dateStr);
      html = html.replaceFirst('லட்சுமி டெக்ஸ்டைல்ஸ்', clientNameStr);
      html = html.replaceFirst('12, நேரு தெரு, சேலம் - 636001', clientAddr1);
      html = html.replaceFirst('சேலம் District, தமிழ்நாடு / Tamil Nadu', clientAddr2);
      html = html.replaceFirst('33AABLM9876Z1K4', clientGstin);
      html = html.replaceFirst('+91 94432 11223', clientPhone);
    } else {
      html = html.replaceAll('SJPS-31', pattiyal.patrucheettuEn);
      html = html.replaceFirst('28/06/2026', dateStr);
      html = html.replaceFirst('முருகன் டெக்ஸ்டைல்ஸ்', clientNameStr);
      html = html.replaceFirst('45, காந்தி நகர், கரூர் - 639001', clientAddr1);
      html = html.replaceFirst('கரூர் District, தமிழ்நாடு / Tamil Nadu', clientAddr2);
      html = html.replaceFirst('33AABCM5678K1Z2', clientGstin);
      // In simple template, phone is replaced first
      html = html.replaceFirst('+91 98765 43210', clientPhone);
    }

    // Place of supply
    html = html.replaceFirst(
      '<div class="pos-primary">தமிழ்நாடு</div>',
      '<div class="pos-primary">$clientState</div>',
    );

    // Replace entire <tbody>…</tbody> with generated rows
    html = html.replaceFirst(
      RegExp(r'<tbody>.*?</tbody>', dotAll: true),
      '<tbody>\n$itemRows\n        </tbody>',
    );

    // Totals section
    html = html.replaceFirst(
      '<span class="font-semibold" style="color: #334155;">6</span>',
      '<span class="font-semibold" style="color: #334155;">$totalQty</span>',
    );
    html = html.replaceFirst(
      '<span class="font-semibold" style="color: #334155;">5007</span>',
      '<span class="font-semibold" style="color: #334155;">$commonHSN</span>',
    );

    // Amount in words
    final mozhiMapAmt = ElvanNavilEzhuthen.convertToMozhiMap(grandTotal);
    String displayAmtTa = '';
    String displayAmtEn = '';
    
    if (isBilingual) {
      displayAmtTa = mozhiMapAmt[mudhanmaiLang] ?? '';
      displayAmtEn = mozhiMapAmt[irandaamLang] ?? '';
    } else {
      displayAmtTa = mozhiMapAmt[mudhanmaiLang] ?? '';
      displayAmtEn = '';
    }

    if (showGstSplits) {
      html = html.replaceFirst('ஆறுபத்தொன்பதாயிரத்து எண்ணூற்றிருபத்தைந்து ரூபாய் மட்டும்', displayAmtTa);
      html = html.replaceFirst('Sixty Nine Thousand Eight Hundred Twenty Five Rupees Only', displayAmtEn);
      
      html = html.replaceFirst('₹ 66,500.00', '₹ ${_fmt(subTotal)}');
      html = html.replaceFirst('₹ 1,662.50', '₹ ${_fmt(totalCgst)}');
      html = html.replaceFirst('₹ 1,662.50', '₹ ${_fmt(totalSgst)}');
      html = html.replaceFirst('₹ 69,825.00', '₹ ${_fmt(grandTotal)}');
    } else {
      html = html.replaceFirst('அறுபத்தாறு ஆயிரத்து ஐநூறு ரூபாய் மட்டும்', displayAmtTa);
      html = html.replaceFirst('Sixty Six Thousand Five Hundred Rupees Only', displayAmtEn);
      
      html = html.replaceFirst('₹ 56,355.93', '₹ ${_fmt(subTotal)}');
      html = html.replaceFirst('₹ 1,412.50', '₹ ${_fmt(totalCgst)}');
      html = html.replaceFirst('₹ 1,412.50', '₹ ${_fmt(totalSgst)}');
      html = html.replaceFirst('-₹ 0.93', '${totals.suttruOff < 0 ? '-' : ''}₹ ${_fmt(totals.suttruOff.abs())}'); // Round off
      html = html.replaceFirst('₹ 66,500.00', '₹ ${_fmt(grandTotal)}');
    }

    // Bank details
    html = html.replaceFirst('Indian Bank, கரூர் கிளை', bankName);
    html = html.replaceFirst('6789012345', accountNo);
    html = html.replaceFirst('IDIB000K123', ifsc);

    // Contact block
    html = html.replaceFirst('45, ராஜாஜி தெரு, கரூர் - 639001', bizAddress1);
    html = html.replaceFirst('கரூர், தமிழ்நாடு / Tamil Nadu, இந்தியா', bizAddress2);
    if (showGstSplits) {
      html = html.replaceFirst('+91 98765 43210', bizPhone);
    } else {
      // It has two phones in contact block, let's replace both with bizPhone
      html = html.replaceFirst('+91 98765 43210', bizPhone);
      html = html.replaceFirst('+91 87654 32109', '');
    }
    html = html.replaceFirst('sales@srisilks.com', bizEmail);

    return html;
  }

  /// Format a number as Indian currency style (e.g. 1,662.50)
  static String _fmt(double val) {
    final f = NumberFormat('#,##,##0.00', 'en_IN');
    return f.format(val);
  }
}
