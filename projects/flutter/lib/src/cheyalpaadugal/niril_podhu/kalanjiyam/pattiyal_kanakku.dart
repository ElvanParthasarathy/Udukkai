/// பட்டியல் கணக்கு — Pure calculation engine for invoice totals.
///
/// Contains two calculator classes:
/// - [PattuKanakku] — Silk mode (item discounts, global discount,
///   proportional tax, GST split).
/// - [KooliKanakku] — Coolie mode (truncated row totals, setharam,
///   courier, ahimsa, other charges).
///
/// No Flutter/UI imports. Pure Dart arithmetic only.
library;

import '../tharavuru/pattiyal_tharavuru.dart';

// ─────────────────────────────────────────────────────────────────────
// Helpers
// ─────────────────────────────────────────────────────────────────────

/// Rounds [n] to two decimal places using standard rounding.
double _round2(double n) => (n * 100).round() / 100;

// ─────────────────────────────────────────────────────────────────────
// PattuKanakku — Silk Invoice Calculator
// ─────────────────────────────────────────────────────────────────────

/// Silk invoice calculation engine.
///
/// Ported from React `InvoiceTotals.tsx`:
/// 1. rawSubtotal = Σ (alavu × vilai)
/// 2. itemDiscounts = Σ per-item discount amounts
/// 3. globalDiscountAmount applied on (rawSubtotal − itemDiscounts)
/// 4. Tax per item on taxable amount after proportional global discount
/// 5. GST split: CGST+SGST (intra-state) or IGST (inter-state)
/// 6. finalTotal = round(rawSubtotal − totalDiscount + taxTotal)
class PattuKanakku {
  PattuKanakku._();

  static PattuMothangal calculate({
    required List<PattuUrupadi> items,
    double globalDiscountValue = 0,
    String globalDiscountType = '%',
    String businessState = '',
    String customerState = '',
    String country = 'India',
  }) {
    // ── Pass 1: raw subtotal & item-level discounts ──────────────

    double rawSubtotal = 0;
    double itemDiscounts = 0;

    for (final item in items) {
      final double amount = item.alavu * item.vilai;
      final double rawDiscount = item.thallupadi;
      final double discountAmount = item.thallupadiVagai == '%'
          ? amount * (rawDiscount / 100)
          : rawDiscount;

      rawSubtotal += amount;
      itemDiscounts += discountAmount;
    }

    // ── Global discount ──────────────────────────────────────────

    final double afterItemDiscountSubtotal = rawSubtotal - itemDiscounts;

    final double globalDiscountAmount = globalDiscountType == '%'
        ? afterItemDiscountSubtotal * (globalDiscountValue / 100)
        : globalDiscountValue;

    final double totalDiscount = itemDiscounts + globalDiscountAmount;

    // ── Pass 2: tax with proportional global discount ────────────

    double taxTotal = 0;

    for (final item in items) {
      final double amount = item.alavu * item.vilai;
      final double rawDiscount = item.thallupadi;
      final double itemDiscount = item.thallupadiVagai == '%'
          ? amount * (rawDiscount / 100)
          : rawDiscount;

      final double afterItemDiscount = amount - itemDiscount;

      // Distribute the global discount proportionally across items.
      final double itemWeight = afterItemDiscountSubtotal > 0
          ? afterItemDiscount / afterItemDiscountSubtotal
          : 0;
      final double itemGlobalDiscount = globalDiscountAmount * itemWeight;
      final double taxableAmount = afterItemDiscount - itemGlobalDiscount;

      taxTotal +=
          ((taxableAmount > 0 ? taxableAmount : 0) * item.variVizhukkaadu) /
              100;
    }

    // ── GST split (CGST / SGST / IGST) ──────────────────────────

    final bool isIndia =
        country.toLowerCase() == 'india' || country.toLowerCase() == 'in';
    final bool isInterstate = isIndia &&
        businessState.isNotEmpty &&
        customerState.isNotEmpty &&
        businessState != customerState;

    final double cgst = isIndia ? (isInterstate ? 0 : taxTotal / 2) : 0;
    final double sgst = isIndia ? (isInterstate ? 0 : taxTotal / 2) : 0;
    final double igst =
        isIndia ? (isInterstate ? taxTotal : 0) : taxTotal;

    // ── Final total with rounding ────────────────────────────────

    final double baseTotal = rawSubtotal - totalDiscount + taxTotal;
    final int finalTotal = baseTotal.round();
    final double roundOff = _round2(finalTotal - baseTotal);

    return PattuMothangal(
      adippadaiMothangal: rawSubtotal,
      thallupadiMothangal: totalDiscount,
      cgst: cgst,
      sgst: sgst,
      igst: igst,
      variMothangal: taxTotal,
      suttruOff: roundOff,
      mothaMothangal: finalTotal.toDouble(),
    );
  }
}

// ─────────────────────────────────────────────────────────────────────
// KooliKanakku — Coolie Invoice Calculator
// ─────────────────────────────────────────────────────────────────────

/// Coolie invoice calculation engine.
///
/// Ported from React coolie logic:
/// 1. Row total = floor(edai × vilai) — truncated, not rounded
/// 2. subtotal = Σ row totals
/// 3. totalKg = Σ item edai + (setharamGrams / 1000)
/// 4. grandTotal = subtotal + courier + ahimsa + other charges
class KooliKanakku {
  KooliKanakku._();

  static KooliMothangal calculate({
    required List<KooliUrupadi> items,
    double setharamGrams = 0,
    double thabaalThogai = 0,
    double ahimsaPattuThogai = 0,
    List<PiraVarivu> piraVarivugal = const [],
  }) {
    // ── Row totals (truncated) & subtotal ────────────────────────

    double subtotal = 0;
    double totalKg = 0;

    for (final item in items) {
      // floor() truncates — matches Math.floor(kg * rate) in React.
      final int rowTotal = (item.edai * item.vilai).floor();
      subtotal += rowTotal;
      totalKg += item.edai;
    }

    // Add setharam weight (grams → kg).
    totalKg += setharamGrams / 1000;

    // ── Other charges total ──────────────────────────────────────

    double piraVarivuMoththam = 0;
    for (final varivu in piraVarivugal) {
      piraVarivuMoththam += varivu.thogai;
    }

    // ── Grand total ──────────────────────────────────────────────

    final double grandTotal =
        subtotal + thabaalThogai + ahimsaPattuThogai + piraVarivuMoththam;

    return KooliMothangal(
      adippadaiMothangal: subtotal,
      mothaEdai: _round2(totalKg),
      perumMothangal: grandTotal,
    );
  }
}
