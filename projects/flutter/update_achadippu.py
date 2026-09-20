import os

fp = r'd:\Projects\Elvan Niril\flutter\lib\src\cheyalpaadugal\niril_pattu\vadivangal\pattu_achadippu_html_uruvakki.dart'
with open(fp, 'r', encoding='utf-8') as f:
    content = f.read()

old_str = """    // ─── Build item rows (exact same HTML structure as the template) ───
    String itemRows = '';
    int index = 1;
    double subTotal = 0;
    double totalCgst = 0;
    double totalSgst = 0;
    int totalQty = 0;
    String commonHSN = '';

    final items = PattiyalUthavigal.pattuListFromJson(pattiyal.tharavugal);
    for (final item in items) {
      final amount = item.alavu * item.vilai;
      final cgstRate = item.variVizhukkaadu / 2;
      final sgstRate = item.variVizhukkaadu / 2;
      final cgstAmt = amount * (cgstRate / 100);
      final sgstAmt = amount * (sgstRate / 100);
      final rowTotal = amount + cgstAmt + sgstAmt;

      subTotal += amount;
      totalCgst += cgstAmt;
      totalSgst += sgstAmt;
      totalQty += item.alavu.toInt();

      if (commonHSN.isEmpty && item.hsnKuriyeedu.isNotEmpty) {
        commonHSN = item.hsnKuriyeedu;
      }

      // Hot-swappable Bilingual Logic
      final primaryName = item.mozhiMap.isNotEmpty 
          ? IruMozhiPorulUdhavi.mudhanmaiPeyar(item.mozhiMap.cast<String, dynamic>(), mudhanmaiLang)
          : item.porulPeyar;
          
      final secondaryName = item.mozhiMap.isNotEmpty 
          ? IruMozhiPorulUdhavi.thunaiPeyar(item.mozhiMap.cast<String, dynamic>(), irandaamLang, isBilingual)
          : (isBilingual ? item.porulPeyarEn : '');
          
      final displayPrimary = primaryName.isNotEmpty ? primaryName : item.porulPeyar;
      final displaySecondary = secondaryName;

      // Build row using the EXACT same class names / structure as the template
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
            <td class="inv-td inv-td-right">₹ ${_fmt(rowTotal)}</td>
          </tr>''';
      }
      index++;
    }

    final grandTotal = subTotal + totalCgst + totalSgst - pattiyal.thallupadi;"""

new_str = """    // ─── Build item rows (exact same HTML structure as the template) ───
    String itemRows = '';
    int index = 1;
    int totalQty = 0;
    String commonHSN = '';

    final items = PattiyalUthavigal.pattuListFromJson(pattiyal.tharavugal);
    
    bool hasAnyDiscount = pattiyal.thallupadi > 0;
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
      globalDiscountValue: pattiyal.thallupadi,
      globalDiscountType: pattiyal.thallupadiVagai,
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
          ? '<td class="inv-td inv-td-right">${itemDiscountAmt > 0 ? ' + "'₹ '" + ' + _fmt(itemDiscountAmt) : ' + "'-'" + '}</td>' 
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
              <div style="font-weight: 600; font-size: 0.85rem; margin-bottom: 2px; line-height: 1.2; text-align: right; text-transform: none;">${isBilingual ? (mudhanmaiLang == 'ta' ? 'தள்ளுபடி' : 'Discount') : (mudhanmaiLang == 'ta' ? 'தள்ளுபடி' : 'Discount')}</div>
              <div style="font-weight: 400; font-size: 0.7rem; opacity: 0.85; line-height: 1; text-align: right; text-transform: none;">Discount</div>
            </th>''' : '';
            
    final discountThSimpleHtml = hasAnyDiscount ? '''<th class="inv-th inv-th-right">
              <div style="font-weight: 600; font-size: 0.85rem; margin-bottom: 2px; line-height: 1.2; text-align: right; text-transform: none;">${isBilingual ? (mudhanmaiLang == 'ta' ? 'தள்ளுபடி' : 'Discount') : (mudhanmaiLang == 'ta' ? 'தள்ளுபடி' : 'Discount')}</div>
              <div style="font-weight: 400; font-size: 0.7rem; opacity: 0.85; line-height: 1; text-align: right; text-transform: none;">Discount</div>
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
    final double grandTotal = totals.mothaMothangal;"""

content = content.replace(old_str, new_str)
if old_str in content:
    print('Failed to replace old string!')
else:
    with open(fp, 'w', encoding='utf-8') as f:
        f.write(content)
    print('Successfully updated logic in pattu_achadippu_html_uruvakki.dart')
