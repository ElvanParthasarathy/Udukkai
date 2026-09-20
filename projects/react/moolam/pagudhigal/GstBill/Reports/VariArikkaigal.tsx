// @ts-nocheck
import Description from '@mui/icons-material/Description';
import Download from '@mui/icons-material/Download';
import Upload from '@mui/icons-material/Upload';
import OpenInNew from '@mui/icons-material/OpenInNew';
import CheckCircle from '@mui/icons-material/CheckCircle';
import KeyboardArrowDown from '@mui/icons-material/KeyboardArrowDown';
import KeyboardArrowRight from '@mui/icons-material/KeyboardArrowRight';
import Warning from '@mui/icons-material/Warning';
import MenuBook from '@mui/icons-material/MenuBook';
import BarChart from '@mui/icons-material/BarChart';
import { useState, useEffect, useRef } from 'react';
import { Box, Typography, Link, ButtonBase, Button, Paper, TextField, Select, MenuItem, FormControl, InputLabel, Table, TableBody, TableCell, TableContainer, TableHead, TableRow, Chip, IconButton, Dialog, DialogTitle, DialogContent, DialogActions, Stack, InputAdornment, Grid, Card, CardContent, Alert, useTheme, Pagination, Tooltip } from '@mui/material';
import { TrendUp, TrendDown, Wallet, FileText, X, MagnifyingGlass, DownloadSimple } from '@phosphor-icons/react';
import { getSearchPaperSx, searchInputStyle } from '../../commonStyles';
import ElvanCard from '../../ElvanCard';
import { getAllBills, getAllExpenses, getAllPurchases } from '../../../Avanam';
import { formatCurrency, INVOICE_TYPES, calculateLineItemTax, getStateCode, formatDateGST, getFilingPeriod, getUnitUQC, getDynamicField } from '../../../Payanpadu';
import { thagaval } from '../../Thagaval';
import { useLanguage } from '../../../mozhi/LanguageContext';
import * as XLSX from 'xlsx-js-style';

const GST_TYPES = ['tax-invoice', 'credit-note'];

const QUARTERS = [
  { id: 'Q1', label: 'Q1 (Apr–Jun)', months: [3, 4, 5] },
  { id: 'Q2', label: 'Q2 (Jul–Sep)', months: [6, 7, 8] },
  { id: 'Q3', label: 'Q3 (Oct–Dec)', months: [9, 10, 11] },
  { id: 'Q4', label: 'Q4 (Jan–Mar)', months: [0, 1, 2] },
];

function getFYOptions() {
  const now = new Date();
  const currentYear = now.getMonth() >= 3 ? now.getFullYear() : now.getFullYear() - 1;
  const options = [];
  for (let i = 0; i < 5; i++) {
    const y = currentYear - i;
    options.push({ value: `${y}-${y + 1}`, label: `FY ${y}-${String(y + 1).slice(-2)}`, from: `${y}-04-01`, to: `${y + 1}-03-31` });
  }
  return options;
}

function downloadCSV(filename, headers, rows) {
  const escape = (val) => { const s = String(val ?? ''); return s.includes(',') || s.includes('"') || s.includes('\n') ? '"' + s.replace(/"/g, '""') + '"' : s; };
  const lines = [headers.map(escape).join(',')];
  rows.forEach(row => lines.push(row.map(escape).join(',')));
  const blob = new Blob([lines.join('\n')], { type: 'text/csv;charset=utf-8;' });
  const url = URL.createObjectURL(blob);
  const a = document.createElement('a'); a.href = url; a.download = filename; a.click();
  URL.revokeObjectURL(url);
}

function round2(n) { return Math.round(n * 100) / 100; }

function computeItemTaxSplit(item, isInterState, taxInclusive = false) {
  const { afterDiscount, taxAmount } = calculateLineItemTax(item, taxInclusive);
  if (isInterState) return { taxable: afterDiscount, cgst: 0, sgst: 0, igst: taxAmount };
  const half = Math.round((taxAmount / 2) * 100) / 100;
  return { taxable: afterDiscount, cgst: half, sgst: taxAmount - half, igst: 0 };
}

function getTaxableAmount(totals) {
  return totals?.taxableAmount ?? ((totals?.subtotal || 0) - (totals?.totalDiscount || 0));
}

// ========== GSTR-2B reconciliation helpers ==========
// Normalises an invoice number for fuzzy matching: uppercase, strip non-alphanumeric.
function normInv(s) { return String(s || '').toUpperCase().replace(/[^A-Z0-9]/g, ''); }

// Given the raw GSTR-2B JSON `data` block and the user's purchase records, return
// a flat array of reconciliation rows with status: matched | amount_mismatch |
// book_only | twob_only.
function buildReconciliation(twoBData, purchases) {
  if (!twoBData) return [];
  const twoBSuppliers = twoBData.docdata?.b2b || twoBData.b2b || [];
  const rows = [];

  // Build a quick-lookup map for the user's purchases keyed on (supplier GSTIN, normalized invoice no.)
  const bookByKey = new Map();
  (purchases || []).forEach(p => {
    const key = `${(p.supplierGstin || '').toUpperCase()}::${normInv(p.invoiceNumber)}`;
    bookByKey.set(key, p);
  });

  // Track which book entries we've already matched so we can find leftover "book-only"
  const matchedBookKeys = new Set();

  twoBSuppliers.forEach(sup => {
    const ctin = (sup.ctin || '').toUpperCase();
    const supName = sup.trdnm || '';
    (sup.inv || []).forEach(inv => {
      const key = `${ctin}::${normInv(inv.inum)}`;
      const book = bookByKey.get(key);
      const twoBVal = Number(inv.val || 0);
      const twoBTaxable = (inv.items || []).reduce((s, it) => s + Number(it.txval || 0), 0);
      const twoBIgst = (inv.items || []).reduce((s, it) => s + Number(it.igst || 0), 0);
      const twoBCgst = (inv.items || []).reduce((s, it) => s + Number(it.cgst || 0), 0);
      const twoBSgst = (inv.items || []).reduce((s, it) => s + Number(it.sgst || 0), 0);

      if (!book) {
        rows.push({
          status: 'twob_only',
          supplier: supName, ctin,
          invoiceNumber: inv.inum, date: inv.dt,
          twoBVal, twoBTaxable, twoBIgst, twoBCgst, twoBSgst,
          bookVal: 0, bookTaxable: 0, bookIgst: 0, bookCgst: 0, bookSgst: 0,
          itcAvailable: inv.itcavl !== 'N',
        });
        return;
      }
      matchedBookKeys.add(key);

      const bookTotals = (book.items || []).reduce((acc, it) => {
        const amount = (it.qty || 0) * (it.rate || 0);
        const tax = amount * (it.taxPercent || 0) / 100;
        return { taxable: acc.taxable + amount, tax: acc.tax + tax, total: acc.total + amount + tax };
      }, { taxable: 0, tax: 0, total: 0 });
      const bookIgst = book.interstate ? bookTotals.tax : 0;
      const bookCgst = book.interstate ? 0 : bookTotals.tax / 2;
      const bookSgst = book.interstate ? 0 : bookTotals.tax / 2;

      const valDiff = Math.abs(twoBVal - bookTotals.total);
      const taxableDiff = Math.abs(twoBTaxable - bookTotals.taxable);
      const status = (valDiff <= 1 && taxableDiff <= 1) ? 'matched' : 'amount_mismatch';

      rows.push({
        status,
        supplier: supName || book.supplierName || '',
        ctin,
        invoiceNumber: inv.inum, date: inv.dt,
        twoBVal, twoBTaxable, twoBIgst, twoBCgst, twoBSgst,
        bookVal: bookTotals.total, bookTaxable: bookTotals.taxable,
        bookIgst, bookCgst, bookSgst,
        itcAvailable: inv.itcavl !== 'N',
        valDiff, taxableDiff,
      });
    });
  });

  // Anything in our books that didn't match a 2B entry — supplier hasn't filed yet
  (purchases || []).forEach(p => {
    const key = `${(p.supplierGstin || '').toUpperCase()}::${normInv(p.invoiceNumber)}`;
    if (matchedBookKeys.has(key)) return;
    const totals = (p.items || []).reduce((acc, it) => {
      const amount = (it.qty || 0) * (it.rate || 0);
      const tax = amount * (it.taxPercent || 0) / 100;
      return { taxable: acc.taxable + amount, tax: acc.tax + tax, total: acc.total + amount + tax };
    }, { taxable: 0, tax: 0, total: 0 });
    rows.push({
      status: 'book_only',
      supplier: p.supplierName || '', ctin: (p.supplierGstin || '').toUpperCase(),
      invoiceNumber: p.invoiceNumber, date: p.date,
      twoBVal: 0, twoBTaxable: 0, twoBIgst: 0, twoBCgst: 0, twoBSgst: 0,
      bookVal: totals.total, bookTaxable: totals.taxable,
      bookIgst: p.interstate ? totals.tax : 0,
      bookCgst: p.interstate ? 0 : totals.tax / 2,
      bookSgst: p.interstate ? 0 : totals.tax / 2,
      itcAvailable: false,
    });
  });

  return rows;
}

// Inter-maanilam status of a bill — follows place of supply when set explicitly,
// SEZ supplies always interstate, else compares seller and client maanilam.
function billIsInterstate(bill) {
  const prof = bill.data?.profile;
  const client = bill.data?.client;
  const details = bill.data?.details;
  if (client?.isSEZ) return true;
  const sellerState = (prof?.maanilam || '').trim().toLowerCase();
  const placeOfSupply = (details?.placeOfSupply || client?.maanilam || '').trim().toLowerCase();
  if (!sellerState || !placeOfSupply) return false;
  return sellerState !== placeOfSupply;
}

// ========== Filing Guide Steps ==========
const GSTR1_STEPS = [
  {
    title: 'Login to GST Portal',
    details: `1. Open gst.gov.in in your browser (Chrome/Firefox recommended).
2. Click "Login" at top-right → Enter your GSTIN or Username → Enter Password → Enter Captcha → Click "LOGIN".
3. If you have 2FA enabled, enter the OTP sent to your registered mobile.
4. After login, you'll see the Dashboard. Click "Returns" in the top menu → Click "Returns Dashboard".
IMPORTANT: Use your authorized signatory credentials. Only the primary authorized signatory or additional users with filing rights can file returns.`
  },
  {
    title: 'Select Return Period for GSTR-1',
    details: `1. On the Returns Dashboard page, select "Financial Year" (e.g., 2025-26) and "Return Filing Period" (select the month, e.g., March).
2. Click "SEARCH" button.
3. The page will show all returns for that period. Find "GSTR-1" tile.
4. Click "PREPARE ONLINE" if you have fewer than 500 invoices. Click "PREPARE OFFLINE" if you have 500+ invoices (you'll download the offline tool, import the JSON from this app, and upload).
5. For QRMP scheme users: Select the quarter end month. You file quarterly but can use IFF (Invoice Furnishing Facility) monthly for B2B invoices.
NOTE: GSTR-1 must be filed BEFORE GSTR-3B. Due date: 11th of next month (monthly filers) or 13th of month after quarter (QRMP).`
  },
  {
    title: 'Table 4A — B2B Invoices (Registered Clients)',
    details: `1. Click "4A, 4B, 4C, 6B, 6C — B2B Invoices" tile.
2. Click "+ ADD INVOICE" button.
3. For EACH B2B invoice from your GSTR-1 tab above, enter:
   • Receiver GSTIN — Enter the 15-digit GSTIN (e.g., 03AABCU9603R1ZN). Portal auto-validates.
   • Invoice Number — Must match EXACTLY as on your invoice (e.g., INV/2025-26/0001).
   • Invoice Date — DD/MM/YYYY format. Must fall within the return period.
   • Invoice Value — Total invoice amount INCLUDING tax (the "Total" column in B2B table above).
   • Place of Supply — Select the maanilam. For intra-maanilam, this is YOUR maanilam. For inter-maanilam, this is the BUYER's maanilam.
   • Reverse Charge — Select "N" (No) for normal supplies. Select "Y" only if the buyer pays GST under reverse charge (Section 9(3)/9(4)).
   • Invoice Type — "Regular" for normal invoices, "SEZ supplies with payment" / "SEZ supplies without payment" for SEZ.
   • Click "ADD" under tax details → Enter Rate (e.g., 18%), Taxable Value, IGST or CGST+SGST amounts.
4. Click "SAVE" after each invoice.
5. Repeat for ALL B2B invoices.
TIP: Use the "GSTR-1 JSON" export from this app and upload via offline tool to skip manual entry.
COMMON ERROR: "Invoice number already exists" — each invoice number must be unique within the period.`
  },
  {
    title: 'Table 5 — B2C Large (Inter-maanilam > ₹2.5 Lakh)',
    details: `This table is ONLY for inter-maanilam invoices to UNREGISTERED persons (no GSTIN) where invoice value EXCEEDS ₹2,50,000.
1. Click "5A, 5B — B2C (Large) Invoices" tile.
2. Click "+ ADD INVOICE".
3. Enter: Place of Supply (buyer's maanilam), Invoice Number, Invoice Date, Invoice Value, Taxable Value, IGST Amount, Cess (if any).
4. Only IGST applies here (never CGST/SGST) since these are inter-maanilam.
5. Click "SAVE".
NOTE: If you have no inter-maanilam B2C invoices above ₹2.5L, skip this table entirely.`
  },
  {
    title: 'Table 7 — B2C Small (All Other B2C)',
    details: `This covers ALL remaining B2C invoices: intra-maanilam B2C of any value + inter-maanilam B2C below ₹2.5 lakh.
1. Click "7 — B2C (Others)" tile.
2. Data is entered in AGGREGATE (not individual invoices). Group by: Supply Type (Intra/Inter), Place of Supply, and Tax Rate.
3. For each combination, enter: Type (Intra-maanilam/Inter-maanilam), Place of Supply, Rate (e.g., 18%), Taxable Value, CGST/SGST or IGST.
4. Use the B2C table in your GSTR-1 tab above — it's already aggregated by rate.
5. Click "SAVE".
IMPORTANT: Intra-maanilam B2C → enter CGST + SGST. Inter-maanilam B2C → enter IGST only.`
  },
  {
    title: 'Table 9B — Credit/Debit Notes',
    details: `Only if you issued Credit Notes or Debit Notes during this period.
1. Click "9B — Credit/Debit Notes (Registered)" for notes to GSTIN holders.
2. Enter: Receiver GSTIN, Note Number, Note Date, Note Type (Credit/Debit), Note Value, Place of Supply, Taxable Value, Tax Amounts.
3. For unregistered persons: Click "9B — Credit/Debit Notes (Unregistered)" and enter without GSTIN.
4. Credit Notes reduce your liability. Debit Notes increase it.
RULE: Credit note must reference the original invoice. Must be issued before September 30 following the end of the financial year of the original invoice or before filing annual return, whichever is earlier (Section 34 of CGST Act).`
  },
  {
    title: 'Table 12 — HSN-wise Summary of Outward Supplies',
    details: `Mandatory reporting based on your turnover:
• Turnover up to ₹1.5 Crore — HSN summary NOT mandatory (but recommended)
• Turnover ₹1.5 Cr to ₹5 Cr — 4-digit HSN code mandatory
• Turnover above ₹5 Crore — 6-digit HSN code mandatory
1. Click "12 — HSN-wise Summary of outward supplies".
2. For each HSN/SAC code, enter: HSN Code, Description, UQC (Unit — NOS/KGS/MTR etc.), Total Quantity, Taxable Value, IGST, CGST, SGST, Cess.
3. Use the HSN Summary from your GSTR-1 tab above.
4. Click "SAVE".
COMMON ERROR: "Invalid HSN code" — ensure HSN codes match the official HSN Master list. Services use SAC codes (starting with 99).`
  },
  {
    title: 'Table 13 — Documents Issued During the Period',
    details: `1. Click "13 — Documents Issued during the tax period".
2. Enter the serial number range for each document type:
   • Invoices for outward supply — From: INV/2025-26/0001, To: INV/2025-26/0015, Total: 15, Cancelled: 0
   • Credit Notes — From/To range, Total issued, Cancelled count
   • Debit Notes — same
   • Delivery Challans — same
3. Net Issued = Total - Cancelled (auto-calculated).
4. Use the Document Summary from your GSTR-1 tab above.
5. Click "SAVE".`
  },
  {
    title: 'Preview, Submit & File GSTR-1',
    details: `1. After filling all tables, scroll to bottom and click "PREVIEW" button.
2. Review the summary carefully. Check:
   • Total taxable value matches your records
   • B2B + B2C totals are correct
   • Tax amounts (IGST, CGST, SGST) match
   • HSN summary totals match
3. If everything looks correct, click "SUBMIT" button.
⚠️ WARNING: After clicking SUBMIT, data is FROZEN. You CANNOT edit any table after submission. Only proceed if you're sure.
4. After submission, click "FILE GSTR-1" button (appears after submit).
5. Select filing method:
   • DSC (Digital Signature Certificate) — for companies and LLPs (mandatory)
   • EVC (Electronic Verification Code) — OTP sent to registered mobile/email. Available for proprietors, partnerships, HUFs.
6. Enter OTP or sign with DSC → Click "FILE".
7. You'll see ARN (Acknowledgement Reference Number). Save this for your records.
DONE! GSTR-1 is filed. Now proceed to GSTR-3B.`
  },
];

const GSTR3B_STEPS = [
  {
    title: 'Navigate to GSTR-3B',
    details: `1. Login to gst.gov.in (if not already logged in).
2. Go to Returns → Returns Dashboard.
3. Select the same Financial Year and Period as your GSTR-1.
4. Click "SEARCH".
5. Find "GSTR-3B" tile → Click "PREPARE ONLINE".
NOTE: File GSTR-3B AFTER GSTR-1 is filed. From July 2025, Table 3 is auto-populated from your GSTR-1 data. Due date: 20th of next month (monthly) or 22nd/24th after quarter (QRMP, based on your maanilam).`
  },
  {
    title: 'Table 3.1 — Outward Supplies & Tax Liability',
    details: `This table shows your OUTPUT TAX liability. From July 2025, it's auto-populated from GSTR-1.
1. Click "3.1 — Tax on outward and reverse charge inward supplies".
2. Verify/Enter these rows:
   (a) Outward taxable supplies (other than zero-rated, nil-rated, exempted):
       • Taxable Value = Your total taxable value from GSTR-1 Summary
       • IGST = Total IGST from all invoices
       • CGST = Total CGST from all invoices
       • SGST = Total SGST from all invoices
   (b) Outward taxable supplies (zero rated): Enter if you have exports or supplies to SEZ.
   (c) Other outward supplies (nil rated, exempted): Enter exempt/nil-rated supply values.
   (d) Inward supplies (liable to reverse charge): Enter if you received services under RCM (e.g., from unregistered persons, legal services, GTA).
   (e) Non-GST outward supplies: Enter non-taxable supplies (e.g., petroleum, alcohol).
3. Click "CONFIRM" when done.
IMPORTANT: Values here MUST match your GSTR-1. Any mismatch will be flagged by the system and may trigger a notice under Section 61.`
  },
  {
    title: 'Table 3.2 — Inter-maanilam Supplies to Unregistered Persons',
    details: `Only required if you made INTER-maanilam supplies to UNREGISTERED persons or composition dealers.
1. Click "3.2 — Inter-maanilam supplies".
2. For supplies to unregistered persons:
   • Select Place of Supply (buyer's maanilam)
   • Enter Taxable Value and IGST Amount
   • Add row for each maanilam you supplied to
3. For supplies to composition dealers: Same format, separate section.
4. Click "CONFIRM".
NOTE: This data must reconcile with your GSTR-1 B2C Large (Table 5) data. If you have no inter-maanilam B2C supplies, leave blank.`
  },
  {
    title: 'Table 4 — Input Tax Credit (ITC)',
    details: `This is where you CLAIM your ITC to reduce tax liability.
1. Click "4 — Eligible ITC".
2. Section (A) — ITC Available:
   (1) Import of goods — ITC from Bill of Entry (customs)
   (2) Import of services — ITC from invoices for imported services
   (3) Inward supplies liable to reverse charge — ITC on RCM supplies you paid tax on
   (4) Inward supplies from ISD — ITC distributed by Input Service Distributor
   (5) All other ITC — THIS IS YOUR MAIN ITC. Enter IGST, CGST, SGST from eligible purchase invoices.
       Use the ITC values from your GSTR-3B tab above (from Expense Tracker).
3. Section (B) — ITC Reversed: Enter if you need to reverse ITC (Rule 42/43 — common credit reversal, exempt supplies ratio).
4. Net ITC = (A) minus (B).
5. Click "CONFIRM".
CRITICAL RULES:
• ITC is only valid if supplier has filed THEIR GSTR-1 (check GSTR-2B auto-populated statement)
• ITC must be claimed within the time limit — Section 16(4): Due date of September return or annual return filing date
• ITC cannot exceed GSTR-2B values + 5% tolerance (Rule 36(4) — now removed, but system still validates against GSTR-2B)
• Retain all invoices and proof of payment for ITC claims`
  },
  {
    title: 'Table 5 — Exempt, Nil-Rated and Non-GST Inward Supplies',
    details: `1. Click "5 — Values of exempt, nil-rated and non-GST inward supplies".
2. Enter values for:
   • Inter-maanilam inward supplies (exempt/nil/non-GST)
   • Intra-maanilam inward supplies (exempt/nil/non-GST)
3. Examples: Purchase of milk, fresh vegetables, unprocessed food grains, educational services, healthcare services.
4. Click "CONFIRM".
NOTE: This is informational only — no tax impact. But incorrect reporting can trigger scrutiny.`
  },
  {
    title: 'Table 6 — Tax Payment (Calculate Net Payable)',
    details: `This auto-calculates based on Tables 3 and 4.
1. Click "6.1 — Payment of Tax".
2. Review the auto-calculated amounts:
   • Tax Payable = Output Tax (Table 3.1) for each head (IGST, CGST, SGST)
   • ITC Claimed = From Table 4, auto-set against each tax head
   • Tax Paid through ITC = Amount of ITC utilized
   • Tax/Cess Paid in Cash = Remaining amount to pay via cash
3. ITC utilization order (mandatory as per Section 49):
   • IGST credit → First set off against IGST liability → Then CGST → Then SGST
   • CGST credit → First against CGST → Then IGST (NOT SGST)
   • SGST credit → First against SGST → Then IGST (NOT CGST)
4. If cash payment is needed:
   • Click "CREATE CHALLAN" → Select payment method (Net Banking / NEFT/RTGS / Over the Counter)
   • Pay the amount → Challan will reflect in Electronic Cash Ledger
   • Come back and click "MAKE PAYMENT / POST CREDIT TO LEDGER"
5. If ITC fully covers your liability, no cash payment needed. Click "POST CREDIT TO LEDGER" directly.`
  },
  {
    title: 'Preview, Submit, Pay & File GSTR-3B',
    details: `1. Click "PREVIEW DRAFT GSTR-3B" at the bottom.
2. Review ALL values carefully:
   • Output tax matches GSTR-1 totals
   • ITC matches your purchase records and GSTR-2B
   • Net payable amount is correct
3. Check the "I have reconciled..." declaration checkbox.
4. Click "SUBMIT" button.
⚠️ WARNING: After SUBMIT, data is FROZEN. You cannot change any values.
5. After submit:
   • If tax is payable → Click "MAKE PAYMENT / POST CREDIT TO LEDGER" → System will utilize ITC first, then debit cash ledger.
   • If no tax payable (ITC covers everything) → Click "POST CREDIT TO LEDGER".
6. Click "FILE GSTR-3B" (appears after payment/posting).
7. Select filing method: DSC or EVC (same as GSTR-1).
8. Enter OTP or sign → Click "FILE".
9. Save the ARN number.
DONE! Both GSTR-1 and GSTR-3B are filed for this period.

LATE FILING CONSEQUENCES:
• Late fee: ₹50/day (₹25 CGST + ₹25 SGST) — capped at ₹5,000 per return for taxpayers with turnover, ₹500 for nil returns (CGST Amendment Act 2023, FY 2023-24 onwards)
• Interest: 18% p.a. on outstanding tax from due date (Section 50)
• Cannot file next month's return until current month is filed
• E-way bill generation blocked after 2 months of non-filing`
  },
];

const NIL_GSTR1_STEPS = [
  {
    title: 'Login & Navigate',
    details: `1. Login to gst.gov.in → Returns → Returns Dashboard.
2. Select Financial Year and Month → Click "SEARCH".
3. Find GSTR-1 tile → Click "PREPARE ONLINE".`
  },
  {
    title: 'Verify All Tables are Empty',
    details: `1. All tables (4A, 5, 7, 9, 12, 13) should show ZERO or be empty.
2. If any table has data from a previous session, DELETE it.
3. There should be NO invoices, NO credit notes, NO HSN entries.`
  },
  {
    title: 'Submit & File NIL GSTR-1',
    details: `1. Click "GENERATE GSTR-1 SUMMARY" at the bottom.
2. Check that all values show ₹0.00.
3. Tick the declaration checkbox: "I/We hereby declare that the information..."
4. Click "FILE GSTR-1 (NIL)" button. This special button appears when all tables are empty.
5. Select EVC/DSC → Enter OTP → File.
6. Save ARN number.
IMPORTANT: Filing NIL GSTR-1 is mandatory even with zero sales. Non-filing attracts ₹20/day penalty (₹10 CGST + ₹10 SGST), max ₹500 per return.`
  },
];

const NIL_GSTR3B_STEPS = [
  {
    title: 'Login & Navigate',
    details: `1. Login to gst.gov.in → Returns → Returns Dashboard.
2. Select same period → Click "SEARCH".
3. Find GSTR-3B tile → Click "PREPARE ONLINE".`
  },
  {
    title: 'Verify All Values are Zero',
    details: `1. Table 3.1 — All outward supply values should be ₹0.
2. Table 4 — No ITC to claim (all zeros).
3. Table 5 — Exempt supplies should be ₹0 (unless you have exempt inward supplies).
4. Table 6 — Tax payable should be ₹0 across IGST, CGST, SGST.`
  },
  {
    title: 'Submit & File NIL GSTR-3B',
    details: `1. Click "SUBMIT" at the bottom.
2. Since no tax is payable, no payment step is needed.
3. Click "FILE GSTR-3B (NIL)" — this special button appears when all values are zero.
4. Tick the declaration: "I verify that to the best of my knowledge..."
5. Select EVC/DSC → Enter OTP → File.
6. Save ARN number.
NOTE: Even for NIL return, you MUST file both GSTR-1 and GSTR-3B separately. They are different returns.
PENALTY: ₹20/day late fee for NIL GSTR-3B (₹10 CGST + ₹10 SGST), capped at ₹500 per return.`
  },
];

function StepList({ steps, title }: { steps: any[], title: string }) {
  const [expanded, setExpanded] = useState<Record<number, boolean>>({});
  const [checked, setChecked] = useState<Record<number, boolean>>({});
  return (
    <Paper elevation={0} sx={{ borderRadius: 3, border: '1px solid', borderColor: 'divider', overflow: 'hidden', mb: 3 }}>
      <Box sx={{ p: 2, borderBottom: '1px solid', borderColor: 'divider', bgcolor: 'background.paper' }}>
        <Typography variant="h6" sx={{ fontWeight: "bold" ,  m: 0 }}>{title}</Typography>
      </Box>
      <Box sx={{ p: 0 }}>
        {steps.map((step, i) => (
          <Box key={i} sx={{ borderBottom: i < steps.length - 1 ? '1px solid' : 'none', borderColor: 'divider' }}>
            <Box
              onClick={() => setExpanded(p => ({ ...p, [i]: !p[i] }))}
              sx={{ display: 'flex', alignItems: 'center', gap: 1.5, p: 2, cursor: 'pointer', '@media (hover: hover)': { '&:hover': { bgcolor: 'action.hover' } } }}
            >
              <Box
                component="button"
                onClick={(e: React.MouseEvent) => { e.stopPropagation(); setChecked(p => ({ ...p, [i]: !p[i] })); }}
                sx={{
                  color: checked[i] ? 'success.main' : 'text.disabled',
                  bgcolor: checked[i] ? 'success.light' : 'transparent',
                  opacity: checked[i] ? 1 : 0.7,
                  width: 28, height: 28, borderRadius: '50%', border: 'none', cursor: 'pointer',
                  display: 'flex', alignItems: 'center', justifyContent: 'center',
                  '@media (hover: hover)': { '&:hover': { opacity: 1, bgcolor: checked[i] ? 'success.light' : 'action.hover' } }
                }}
              >
                <CheckCircle size={20} weight="fill" sx={{ fontSize: 18 }} />
              </Box>
              <Typography sx={{ flex: 1, fontWeight: 600, fontSize: '0.85rem', color: checked[i] ? 'success.main' : 'text.primary', textDecoration: checked[i] ? 'line-through' : 'none' }}>
                Step {i + 1}: {step.title}
              </Typography>
              {expanded[i] ? <CaretDown size={20} weight="fill" sx={{ fontSize: 16 }} htmlColor="gray" /> : <CaretRight size={20} weight="fill" sx={{ fontSize: 16 }} htmlColor="gray" />}
            </Box>
            {expanded[i] && (
              <Box sx={{ pl: 6.5, pr: 2, pb: 2, typography: 'body2', color: 'text.secondary', whiteSpace: 'pre-wrap' }}>
                {step.details}
              </Box>
            )}
          </Box>
        ))}
      </Box>
    </Paper>
  );
}

// ========== Main Component ==========
export default function VariArikkaigal({ profile }) {
  const theme = useTheme();
  const isDark = theme.palette.mode === 'dark';
  const { t } = useLanguage();
  const MONTHS = [t('january'), t('february'), t('march'), t('april'), t('may'), t('june'), t('july'), t('august'), t('september'), t('october'), t('november'), t('december')];


  const [bills, setBills] = useState([]);
  const [expenses, setExpenses] = useState([]);
  const [purchases, setPurchases] = useState([]);
  const [filterMode, setFilterMode] = useState('month');
  const [fyFilter, setFyFilter] = useState('');
  const [monthFilter, setMonthFilter] = useState<number | ''>('');
  const [yearFilter, setYearFilter] = useState<number | ''>('');
  const [quarterFilter, setQuarterFilter] = useState('Q1');
  const [activeTab, setActiveTab] = useState('summary');
  const [search, setSearch] = useState('');
  const [page, setPage] = useState(1);
  const listTopRef = useRef(null);
  const [gstr2bData, setGstr2bData] = useState(null); // imported 2B JSON
  const [gstr2bFilter, setGstr2bFilter] = useState('all'); // all | matched | mismatch | bookOnly | twoBOnly
  const gstr2bInputRef = useRef(null);

  const handleImport2B = async (e) => {
    const file = e.target.files?.[0];
    if (!file) return;
    try {
      const text = await file.text();
      const json = JSON.parse(text);
      // Accept both wrapped {data: ...} and bare formats
      const root = json?.data || json;
      if (!root?.docdata && !root?.b2b) {
        thagaval('That doesn\'t look like a GSTR-2B JSON. Expected docdata.b2b array.', 'error');
        return;
      }
      setGstr2bData(root);
      const supplierCount = (root.docdata?.b2b || root.b2b || []).length;
      thagaval(`Imported GSTR-2B for ${root.gstin || '?'} — ${supplierCount} suppliers`, 'success');
    } catch (err) {
      console.error(err);
      thagaval('Failed to parse GSTR-2B JSON', 'error');
    }
    if (gstr2bInputRef.current) gstr2bInputRef.current.value = '';
  };
  const [guideTab, setGuideTab] = useState('regular');
  const [filingStatus, setFilingStatus] = useState(() => {
    try { return JSON.parse(localStorage.getItem('gst_filing_status') || '{}'); } catch { return {}; }
  });

  const fyOptions = getFYOptions();
  const currentYear = new Date().getFullYear();
  const yearOptions = [];
  for (let y = currentYear; y >= currentYear - 5; y--) yearOptions.push(y);

  useEffect(() => {
    let unsubs = [];

    const now = new Date();
    const fy = fyOptions[0];
    if (fy) setFyFilter(fy.value);
    setYearFilter(now.getFullYear());
    setMonthFilter(now.getMonth());
    // Auto-detect current quarter
    const m = now.getMonth();
    const q = QUARTERS.find(q => q.months.includes(m));
    if (q) setQuarterFilter(q.id);

    const initRealtime = async () => {
      try {
        const b = await getAllBills((fresh) => setBills(fresh || []));
        if (b && b.unsubscribe) unsubs.push(b.unsubscribe);
        setBills(b || []);

        const e = await getAllExpenses();
        setExpenses(e || []);
        
        try { const pur = await getAllPurchases(); setPurchases(pur || []); } catch { /* ignore — older servers don't have this endpoint */ }
      } catch { thagaval('Failed to load data', 'error'); }
    };

    initRealtime();

    return () => {
      unsubs.forEach(unsub => unsub());
    };
  }, []);

  // ========== Period filtering ==========
  const filterByPeriod = (date) => {
    if (!date) return false;
    if (filterMode === 'fy') {
      const fy = fyOptions.find(f => f.value === fyFilter);
      return fy ? date >= fy.from && date <= fy.to : true;
    } else if (filterMode === 'quarter') {
      const d = new Date(date);
      const q = QUARTERS.find(q => q.id === quarterFilter);
      if (!q) return false;
      // Determine the year for the quarter based on FY context
      const yr = parseInt(yearFilter);
      return q.months.includes(d.getMonth()) && d.getFullYear() === yr;
    } else {
      const d = new Date(date);
      return d.getFullYear() === parseInt(yearFilter) && d.getMonth() === parseInt(monthFilter);
    }
  };

  const filteredBills = bills.filter(bill => {
    const type = bill.invoiceType || 'tax-invoice';
    if (!GST_TYPES.includes(type)) return false;
    if (!bill.data) return false;
    return filterByPeriod(bill.invoiceDate);
  });
  const allFilteredBills = bills.filter(bill => bill.data && filterByPeriod(bill.invoiceDate));
  const filteredExpenses = expenses.filter(exp => filterByPeriod(exp.date));

  // ========== Classification ==========
  const creditNotes = filteredBills.filter((b: any) => (b.invoiceType || 'tax-invoice') === 'credit-note');
  const regularBills = filteredBills.filter((b: any) => (b.invoiceType || 'tax-invoice') !== 'credit-note');
  const b2bRegular = regularBills.filter((b: any) => b.data?.client?.gstin);
  const b2cRegular = regularBills.filter((b: any) => !b.data?.client?.gstin);
  const b2cLarge = b2cRegular.filter((b: any) => {
    const isInter = billIsInterstate(b);
    return isInter && (b.totalAmount || 0) > 250000;
  });
  const b2cSmall = b2cRegular.filter((b: any) => {
    const isInter = billIsInterstate(b);
    return !(isInter && (b.totalAmount || 0) > 250000);
  });

  // ========== B2B Rows ==========
  const b2bRows = b2bRegular.map(bill => {
    const { client, totals, details } = bill.data;
    const isInterState = billIsInterstate(bill);
    const pos = getStateCode(details?.placeOfSupply || getDynamicField(client, 'maanilam', profile, true) || '');
    return {
      gstin: client.gstin, clientName: getDynamicField(client, 'name', profile, true) || bill.clientName || '',
      invoiceNo: bill.invoiceNumber || '', date: bill.invoiceDate || '', pos,
      supplyType: isInterState ? 'Inter' : 'Intra',
      taxable: getTaxableAmount(totals),
      cgst: isInterState ? 0 : (totals?.cgst || 0), sgst: isInterState ? 0 : (totals?.sgst || 0),
      igst: isInterState ? (totals?.igst || 0) : 0, total: totals?.total || 0,
    };
  });

  // ========== B2C by Rate ==========
  const b2cByRate = {};
  const b2cBills = filteredBills.filter((b: any) => !b.data?.client?.gstin);
  b2cBills.forEach(bill => {
    const { items } = bill.data;
    const isInterState = billIsInterstate(bill);
    (items || []).forEach(item => {
      const rate = item.taxPercent || 0;
      if (!b2cByRate[rate]) b2cByRate[rate] = { taxable: 0, cgst: 0, sgst: 0, igst: 0, total: 0 };
      const split = computeItemTaxSplit(item, isInterState, !!bill.data?.taxInclusive);
      b2cByRate[rate].taxable += split.taxable; b2cByRate[rate].cgst += split.cgst;
      b2cByRate[rate].sgst += split.sgst; b2cByRate[rate].igst += split.igst;
      b2cByRate[rate].total += split.taxable + split.cgst + split.sgst + split.igst;
    });
  });
  const b2cRates = Object.keys(b2cByRate).map(Number).sort((a: any, b: any) => a - b);

  // ========== HSN Summary ==========
  const hsnMap = {};
  filteredBills.forEach(bill => {
    const { items } = bill.data;
    const isInterState = billIsInterstate(bill);
    (items || []).forEach(item => {
      const hsn = item.hsn || 'N/A';
      if (!hsnMap[hsn]) hsnMap[hsn] = { hsn, description: getDynamicField(item, 'name', profile, true) || '', qty: 0, taxable: 0, cgst: 0, sgst: 0, igst: 0, totalTax: 0 };
      const split = computeItemTaxSplit(item, isInterState, !!bill.data?.taxInclusive);
      hsnMap[hsn].qty += item.qty || 0; hsnMap[hsn].taxable += split.taxable;
      hsnMap[hsn].cgst += split.cgst; hsnMap[hsn].sgst += split.sgst; hsnMap[hsn].igst += split.igst;
      hsnMap[hsn].totalTax += split.cgst + split.sgst + split.igst;
    });
  });
  const hsnRows = Object.values(hsnMap).sort((a: any, b: any) => a.hsn.localeCompare(b.hsn));

  // ========== Totals ==========
  const sumRows = (rows) => rows.reduce((acc, r) => ({ taxable: acc.taxable + (r as any).taxable, cgst: acc.cgst + r.cgst, sgst: acc.sgst + r.sgst, igst: acc.igst + (r as any).igst, total: acc.total + r.total }), { taxable: 0, cgst: 0, sgst: 0, igst: 0, total: 0 });
  const b2bTotals = sumRows(b2bRows);
  const b2cTotals = b2cRates.reduce((acc, rate) => { const d = b2cByRate[rate]; return { taxable: acc.taxable + d.taxable, cgst: acc.cgst + d.cgst, sgst: acc.sgst + d.sgst, igst: acc.igst + d.igst, total: acc.total + d.total }; }, { taxable: 0, cgst: 0, sgst: 0, igst: 0, total: 0 });
  const grandTotals = { taxable: b2bTotals.taxable + b2cTotals.taxable, cgst: b2bTotals.cgst + b2cTotals.cgst, sgst: b2bTotals.sgst + b2cTotals.sgst, igst: b2bTotals.igst + b2cTotals.igst, total: b2bTotals.total + b2cTotals.total };

  // ========== GSTR-3B ==========
  const outputTax = { cgst: grandTotals.cgst, sgst: grandTotals.sgst, igst: grandTotals.igst };
  // ITC from expenses
  const itcFromExpensesOnly = filteredExpenses.reduce((acc, e) => {
    const gst = e.gstAmount || 0;
    const half = Math.round((gst / 2) * 100) / 100;
    return { cgst: acc.cgst + half, sgst: acc.sgst + (gst - half), igst: acc.igst };
  }, { cgst: 0, sgst: 0, igst: 0 });
  // ITC from purchase bills
  const filteredPurchases = purchases.filter(p => filterByPeriod(p.date));
  // ITC from purchases — route to IGST when the purchase is interstate (supplier charged IGST),
  // otherwise split CGST/SGST. Without this, an interstate purchase incorrectly added its tax
  // to CGST+SGST in the GSTR-3B Table 4(A).
  const itcFromPurchases = filteredPurchases.reduce((acc, p) => {
    const tax = p.totalTax || (p.items || []).reduce((s, i) => s + ((i.qty || 0) * (i.rate || 0) * (i.taxPercent || 0)) / 100, 0);
    if (p.interstate) {
      return { cgst: acc.cgst, sgst: acc.sgst, igst: acc.igst + tax };
    }
    const half = Math.round((tax / 2) * 100) / 100;
    return { cgst: acc.cgst + half, sgst: acc.sgst + (tax - half), igst: acc.igst };
  }, { cgst: 0, sgst: 0, igst: 0 });
  // Combined ITC
  const itcFromExpenses = {
    cgst: itcFromExpensesOnly.cgst + itcFromPurchases.cgst,
    sgst: itcFromExpensesOnly.sgst + itcFromPurchases.sgst,
    igst: itcFromExpensesOnly.igst + itcFromPurchases.igst,
  };
  const netTax = {
    cgst: Math.max(0, outputTax.cgst - itcFromExpenses.cgst),
    sgst: Math.max(0, outputTax.sgst - itcFromExpenses.sgst),
    igst: Math.max(0, outputTax.igst - itcFromExpenses.igst),
  };

  // ========== Document Summary ==========
  const docSummary = {};
  allFilteredBills.forEach(bill => {
    const type = bill.invoiceType || 'tax-invoice';
    const prefix = INVOICE_TYPES[type]?.prefix || 'INV';
    if (!docSummary[prefix]) docSummary[prefix] = { type: INVOICE_TYPES[type]?.label || type, from: bill.invoiceNumber, to: bill.invoiceNumber, total: 0 };
    docSummary[prefix].total++;
    if (bill.invoiceNumber < docSummary[prefix].from) docSummary[prefix].from = bill.invoiceNumber;
    if (bill.invoiceNumber > docSummary[prefix].to) docSummary[prefix].to = bill.invoiceNumber;
  });

  // ========== Validation Warnings ==========
  const warnings = [];
  filteredBills.forEach(bill => {
    const { client, items } = bill.data;
    // Official GSTIN format: 2 digits maanilam code, 5 letters PAN holder, 4 digits PAN number,
    // 1 letter PAN entity, 1 digit, 1 letter (Z by default), 1 alphanumeric checksum.
    // The last char can be a letter or digit, so the final group is [A-Z\d], not \d.
    if (client?.gstin && !/^\d{2}[A-Z]{5}\d{4}[A-Z]\d[A-Z][A-Z\d]$/.test(client.gstin)) {
      warnings.push({ type: 'error', msg: `Invoice ${bill.invoiceNumber}: Invalid client GSTIN format — ${client.gstin}` });
    }
    (items || []).forEach(item => {
      if (!item.hsn || item.hsn === 'N/A') {
        warnings.push({ type: 'warning', msg: `Invoice ${bill.invoiceNumber}: Item "${getDynamicField(item, 'name', profile, true) || 'Unnamed'}" has no HSN/SAC code` });
      }
    });
    if (client?.gstin && !getDynamicField(client, 'maanilam', profile, true)) {
      warnings.push({ type: 'warning', msg: `Invoice ${bill.invoiceNumber}: Client ${getDynamicField(client, 'name', profile, true)} has GSTIN but no maanilam — Place of Supply may be wrong` });
    }
  });
  if (!profile?.gstin) {
    warnings.push({ type: 'error', msg: t('gstinNotSet') });
  }

  // ========== Filing Period Key ==========
  const getPeriodKey = () => {
    if (filterMode === 'month') return `${monthFilter}_${yearFilter}`;
    if (filterMode === 'quarter') return `${quarterFilter}_${yearFilter}`;
    return fyFilter;
  };
  const periodKey = getPeriodKey();
  const periodFiling = filingStatus[periodKey] || {};

  const markFiled = (returnType) => {
    const updated = { ...filingStatus, [periodKey]: { ...periodFiling, [returnType]: true, [`${returnType}Date`]: new Date().toISOString() } };
    setFilingStatus(updated);
    localStorage.setItem('gst_filing_status', JSON.stringify(updated));
    thagaval(`${returnType.toUpperCase()} marked as filed for this period`, 'success');
  };

  const isNilReturn = filteredBills.length === 0 && filteredExpenses.length === 0;

  // ========== CSV Exports ==========
  const exportSimpleCSV = () => {
    if (filteredBills.length === 0) { thagaval('No data to export', 'warning'); return; }
    
    const isBilingual = profile?.enableBilingual !== false;

    let primaryName = profile?.niruvanathinPeyar || 'Business Name';
    let secondaryName = profile?.niruvanathinPeyarEn || '';
    let businessTitle = primaryName;
    if (isBilingual && secondaryName && secondaryName !== primaryName) {
       businessTitle = primaryName + '\n' + secondaryName;
    }
    const reportTitle = `Sales Report - ${periodKey.replace('_', ' ')}`;

    // Create Excel worksheet data
    const wsData = [
      [businessTitle, '', '', '', '', ''],
      [reportTitle, '', '', '', '', ''],
      ['', '', '', '', '', ''],
      [
        'Invoice No',
        'Client',
        'Date',
        'Taxable Value',
        'Tax Amount',
        'Total'
      ]
    ];
    
    filteredBills.forEach(bill => {
      let cName = bill.clientName || getDynamicField(bill.data?.client, 'name', profile, true) || '';
      if (isBilingual) {
        const enName = bill.clientNameEn || getDynamicField(bill.data?.client, 'name', profile, false) || bill.data?.client?.nameEn || bill.data?.client?.peyarEn || '';
        if (enName && enName !== cName) {
           cName = cName + '\n' + enName;
        }
      }

      wsData.push([
        bill.invoiceNumber || '',
        cName,
        formatDateGST(bill.invoiceDate),
        Number(getTaxableAmount(bill.data?.totals).toFixed(2)),
        Number(((bill.data?.totals?.cgst || 0) + (bill.data?.totals?.sgst || 0) + (bill.data?.totals?.igst || 0)).toFixed(2)),
        Number((bill.data?.totals?.total || 0).toFixed(2))
      ]);
    });

    const ws = XLSX.utils.aoa_to_sheet(wsData);
    
    const borderStyle = {
      top: { style: 'thin', color: { auto: 1 } },
      bottom: { style: 'thin', color: { auto: 1 } },
      left: { style: 'thin', color: { auto: 1 } },
      right: { style: 'thin', color: { auto: 1 } }
    };

    // Add title formatting and merges
    ws['!merges'] = [
      { s: { r: 0, c: 0 }, e: { r: 0, c: 5 } }, // Business Title
      { s: { r: 1, c: 0 }, e: { r: 1, c: 5 } }, // Report Title
    ];

    if (ws[XLSX.utils.encode_cell({c: 0, r: 0})]) {
      ws[XLSX.utils.encode_cell({c: 0, r: 0})].s = {
        font: { bold: true, sz: 16, color: { rgb: "333333" } },
        alignment: { horizontal: 'center', vertical: 'center', wrapText: true }
      };
    }
    if (ws[XLSX.utils.encode_cell({c: 0, r: 1})]) {
      ws[XLSX.utils.encode_cell({c: 0, r: 1})].s = {
        font: { bold: true, sz: 13, color: { rgb: "555555" } },
        alignment: { horizontal: 'center', vertical: 'center' }
      };
    }

    // Header styling
    for (let C = 0; C <= 5; ++C) {
      const cellRef = XLSX.utils.encode_cell({c: C, r: 3});
      if (ws[cellRef]) {
        ws[cellRef].s = {
          font: { bold: true },
          fill: { fgColor: { rgb: "F1F5F9" } },
          border: borderStyle,
          alignment: { vertical: 'center', horizontal: 'center' }
        };
      }
    }

    const rowHeights = [
      { hpt: 45 }, // Row 0: Business Name
      { hpt: 25 }, // Row 1: Report Title
      { hpt: 15 }, // Row 2: Empty
      { hpt: 25 }  // Row 3: Table Header
    ];
    for (let R = 4; R < wsData.length; ++R) {
      rowHeights.push({ hpt: 35 }); // Taller row for two lines
      
      for (let C = 0; C <= 5; ++C) {
        const cellRef = XLSX.utils.encode_cell({c: C, r: R});
        if (ws[cellRef]) {
          ws[cellRef].s = {
             border: borderStyle,
             alignment: { vertical: 'center' }
          };
          
          if (C === 1) {
            ws[cellRef].s.alignment.wrapText = true;
          }
          if (C >= 3 && C <= 5) {
            ws[cellRef].z = '₹#,##0.00';
          }
        }
      }
    }
    ws['!rows'] = rowHeights;
    
    // Set column widths to make it properly spaced
    ws['!cols'] = [
      { wch: 18 }, // Invoice No
      { wch: 45 }, // Client (Tamil name needs more width)
      { wch: 15 }, // Date
      { wch: 15 }, // Taxable Value
      { wch: 15 }, // Tax Amount
      { wch: 15 }  // Total
    ];

    ws['!pageSetup'] = { 
      fitToPage: true, 
      fitToWidth: 1, 
      fitToHeight: 0, 
      orientation: 'landscape' 
    };

    const wb = XLSX.utils.book_new();
    XLSX.utils.book_append_sheet(wb, ws, "Sales Report");
    XLSX.writeFile(wb, `Sales_Report_${periodKey}.xlsx`);
    
    thagaval('Sales Excel file downloaded', 'success');
  };

  const exportB2B = () => {
    if (b2bRows.length === 0) { thagaval('No B2B data to export', 'warning'); return; }
    downloadCSV('GSTR1_B2B_Invoices.csv',
      ['GSTIN/UIN', 'Receiver Name', 'Invoice Number', 'Invoice Date', 'Invoice Value', 'Place of Supply', 'Reverse Charge', 'Invoice Type', 'Supply Type', 'Taxable Value', 'CGST Amount', 'SGST Amount', 'IGST Amount'],
      b2bRegular.map(bill => {
        const { client, totals, details } = bill.data;
        const isInter = billIsInterstate(bill);
        const pos = getStateCode(details?.placeOfSupply || client?.maanilam || '');
        return [client.gstin, client.name || bill.clientName || '', bill.invoiceNumber || '', formatDateGST(bill.invoiceDate), (totals?.total || 0).toFixed(2), pos, 'N', 'Regular', isInter ? 'Inter maanilam' : 'Intra maanilam', getTaxableAmount(totals).toFixed(2), isInter ? 0 : (totals?.cgst || 0).toFixed(2), isInter ? 0 : (totals?.sgst || 0).toFixed(2), isInter ? (totals?.igst || 0).toFixed(2) : 0];
      }));
    thagaval('B2B CSV downloaded — matches GSTR-1 Table 4A format', 'success');
  };

  const exportB2C = () => {
    if (b2cRates.length === 0 && b2cLarge.length === 0) { thagaval('No B2C data to export', 'warning'); return; }
    const b2csData = {};
    b2cSmall.forEach(bill => {
      const { profile: prof, client, items, details } = bill.data;
      const isInter = billIsInterstate(bill);
      const pos = getStateCode(details?.placeOfSupply || client?.maanilam || prof?.maanilam || '');
      const splyType = isInter ? 'INTER' : 'INTRA';
      (items || []).forEach(item => {
        const rate = item.taxPercent || 0;
        const key = `${splyType}_${pos}_${rate}`;
        if (!b2csData[key]) b2csData[key] = { splyType, pos, rate, taxable: 0, cgst: 0, sgst: 0, igst: 0 };
        const split = computeItemTaxSplit(item, isInter, !!bill.data?.taxInclusive);
        b2csData[key].taxable += split.taxable; b2csData[key].cgst += split.cgst; b2csData[key].sgst += split.sgst; b2csData[key].igst += split.igst;
      });
    });
    downloadCSV('GSTR1_B2C_Small.csv', ['Type', 'Place of Supply', 'Rate', 'Taxable Value', 'CGST Amount', 'SGST Amount', 'IGST Amount', 'Cess Amount'],
      Object.values(b2csData as any).map((d: any) => [d.splyType === 'INTER' ? 'Inter maanilam' : 'Intra maanilam', d.pos, d.rate + '%', d.taxable.toFixed(2), d.cgst.toFixed(2), d.sgst.toFixed(2), d.igst.toFixed(2), '0.00']));
    if (b2cLarge.length > 0) {
      downloadCSV('GSTR1_B2C_Large.csv', ['Invoice Number', 'Invoice Date', 'Invoice Value', 'Place of Supply', 'Taxable Value', 'IGST Amount', 'Cess Amount'],
        b2cLarge.map(bill => { const { client, totals, details } = bill.data; const pos = getStateCode(details?.placeOfSupply || client?.maanilam || ''); return [bill.invoiceNumber, formatDateGST(bill.invoiceDate), (totals?.total || 0).toFixed(2), pos, getTaxableAmount(totals).toFixed(2), (totals?.igst || 0).toFixed(2), '0.00']; }));
    }
    thagaval('B2C CSV downloaded', 'success');
  };

  const exportHSN = () => {
    if (hsnRows.length === 0) { thagaval('No HSN data', 'warning'); return; }
    const hsnDetailed = {};
    filteredBills.forEach(bill => {
      const { items } = bill.data;
      const isInter = billIsInterstate(bill);
      (items || []).forEach(item => {
        const hsn = item.hsn || 'N/A'; const rate = item.taxPercent || 0; const key = `${hsn}_${rate}`;
        if (!hsnDetailed[key]) hsnDetailed[key] = { hsn, desc: item.name?.primary || '', uqc: 'NOS', qty: 0, rate, taxable: 0, cgst: 0, sgst: 0, igst: 0, totalValue: 0 };
        const split = computeItemTaxSplit(item, isInter, !!bill.data?.taxInclusive);
        hsnDetailed[key].qty += item.qty || 0; hsnDetailed[key].taxable += split.taxable;
        hsnDetailed[key].cgst += split.cgst; hsnDetailed[key].sgst += split.sgst; hsnDetailed[key].igst += split.igst;
        hsnDetailed[key].totalValue += split.taxable + split.cgst + split.sgst + split.igst;
      });
    });
    downloadCSV('GSTR1_HSN_Summary.csv', ['HSN', 'Description', 'UQC', 'Total Quantity', 'Rate %', 'Taxable Value', 'IGST Amount', 'CGST Amount', 'SGST Amount', 'Cess Amount', 'Total Value'],
      Object.values(hsnDetailed as any).map((r: any) => [r.hsn, r.desc, r.uqc, r.qty, r.rate, (r as any).taxable.toFixed(2), (r as any).igst.toFixed(2), r.cgst.toFixed(2), r.sgst.toFixed(2), '0.00', r.totalValue.toFixed(2)]));
    thagaval('HSN CSV downloaded — GSTR-1 Table 12 format', 'success');
  };

  const exportCDNR = () => {
    const cdnrBills = creditNotes.filter((b: any) => b.data?.client?.gstin);
    const cdnurBills = creditNotes.filter((b: any) => !b.data?.client?.gstin);
    if (cdnrBills.length === 0 && cdnurBills.length === 0) { thagaval('No Credit Notes', 'warning'); return; }
    if (cdnrBills.length > 0) {
      downloadCSV('GSTR1_CDNR.csv', ['GSTIN/UIN', 'Receiver Name', 'Note Number', 'Note Date', 'Note Type', 'Place of Supply', 'Reverse Charge', 'Note Value', 'Taxable Value', 'IGST Amount', 'CGST Amount', 'SGST Amount'],
        cdnrBills.map(bill => { const { client, totals } = bill.data; const isInter = billIsInterstate(bill); const pos = getStateCode(bill.data.details?.placeOfSupply || client?.maanilam || ''); return [client.gstin, client.name || bill.clientName, bill.invoiceNumber, formatDateGST(bill.invoiceDate), 'C', pos, 'N', (totals?.total || 0).toFixed(2), getTaxableAmount(totals).toFixed(2), isInter ? (totals?.igst || 0).toFixed(2) : '0.00', isInter ? '0.00' : (totals?.cgst || 0).toFixed(2), isInter ? '0.00' : (totals?.sgst || 0).toFixed(2)]; }));
    }
    if (cdnurBills.length > 0) {
      downloadCSV('GSTR1_CDNUR.csv', ['Note Number', 'Note Date', 'Note Type', 'Place of Supply', 'Note Value', 'Taxable Value', 'IGST Amount', 'Cess Amount'],
        cdnurBills.map(bill => { const { client, totals } = bill.data; const pos = getStateCode(bill.data.details?.placeOfSupply || client?.maanilam || ''); return [bill.invoiceNumber, formatDateGST(bill.invoiceDate), 'C', pos, (totals?.total || 0).toFixed(2), getTaxableAmount(totals).toFixed(2), (totals?.igst || 0).toFixed(2), '0.00']; }));
    }
    thagaval('Credit Notes exported', 'success');
  };

  const exportDocSummary = () => {
    if (Object.keys(docSummary).length === 0) { thagaval('No documents', 'warning'); return; }
    downloadCSV('GSTR1_Doc_Summary.csv', ['Document Type', 'Sr. No. From', 'Sr. No. To', 'Total Number', 'Cancelled'],
      Object.entries(docSummary as any).map(([, d]: [any, any]) => [d.type, d.from, d.to, d.total, 0]));
    thagaval('Document Summary CSV downloaded', 'success');
  };

  const exportGSTR3B = () => {
    downloadCSV('GSTR3B_Summary.csv', ['Description', 'Taxable Value', 'IGST', 'CGST', 'SGST', 'Total'], [
      ['3.1(a) Outward taxable supplies', grandTotals.taxable.toFixed(2), grandTotals.igst.toFixed(2), grandTotals.cgst.toFixed(2), grandTotals.sgst.toFixed(2), (grandTotals.igst + grandTotals.cgst + grandTotals.sgst).toFixed(2)],
      ['4(A) ITC Available', '', itcFromExpenses.igst.toFixed(2), itcFromExpenses.cgst.toFixed(2), itcFromExpenses.sgst.toFixed(2), (itcFromExpenses.igst + itcFromExpenses.cgst + itcFromExpenses.sgst).toFixed(2)],
      ['6.1 Tax Payable', '', netTax.igst.toFixed(2), netTax.cgst.toFixed(2), netTax.sgst.toFixed(2), (netTax.igst + netTax.cgst + netTax.sgst).toFixed(2)],
    ]);
    thagaval('GSTR-3B summary CSV downloaded', 'success');
  };

  // ========== GSTR-3B JSON Export (GSTN offline tool format, schema v1.7) ==========
  // Matches the schema accepted by https://services.gst.gov.in/services/auth/dashboard
  // Upload via: GST portal → Returns → GSTR-3B → Prepare Offline → Upload JSON
  const exportGSTR3BJSON = () => {
    if (filteredBills.length === 0 && filteredExpenses.length === 0) {
      thagaval('No data to export for this period', 'warning');
      return;
    }
    const gstin = profile.gstin || '';
    const ret_period = filterMode === 'month'
      ? String(parseInt(monthFilter) + 1).padStart(2, '0') + yearFilter
      : getFilingPeriod(filteredBills[0]?.invoiceDate || filteredExpenses[0]?.date || new Date().toISOString());

    // Outward supplies — currently we only emit Table 3.1(a). Zero-rated / nil / exempt
    // require invoice-level categorization which is on the v1.5 roadmap; for now those
    // rows are zero-filled rather than omitted (the portal expects the structure even
    // if values are 0).
    const sup_details = {
      osup_det: {
        txval: round2(grandTotals.taxable),
        iamt: round2(grandTotals.igst),
        camt: round2(grandTotals.cgst),
        samt: round2(grandTotals.sgst),
        csamt: 0,
      },
      osup_zero: { txval: 0, iamt: 0, csamt: 0 },
      osup_nil_exmp: { txval: 0 },
      isup_rev: { txval: 0, iamt: 0, camt: 0, samt: 0, csamt: 0 },
      osup_nongst: { txval: 0 },
    };

    const itc_elg = {
      itc_avl: [
        { ty: 'IMPG', iamt: 0, camt: 0, samt: 0, csamt: 0 },
        { ty: 'IMPS', iamt: 0, camt: 0, samt: 0, csamt: 0 },
        { ty: 'ISRC', iamt: 0, camt: 0, samt: 0, csamt: 0 },
        { ty: 'ISD',  iamt: 0, camt: 0, samt: 0, csamt: 0 },
        { ty: 'OTH',
          iamt: round2(itcFromExpenses.igst),
          camt: round2(itcFromExpenses.cgst),
          samt: round2(itcFromExpenses.sgst),
          csamt: 0,
        },
      ],
      itc_inelg: [
        { ty: 'RUL', iamt: 0, camt: 0, samt: 0, csamt: 0 },
        { ty: 'OTH', iamt: 0, camt: 0, samt: 0, csamt: 0 },
      ],
    };

    const inward_sup = {
      isup_details: [
        { ty: 'GST',    inter: 0, intra: 0 },
        { ty: 'NONGST', inter: 0, intra: 0 },
      ],
    };

    const gstr3b = { gstin, ret_period, sup_details, itc_elg, inward_sup };
    const blob = new Blob([JSON.stringify(gstr3b, null, 2)], { type: 'application/json' });
    const url = URL.createObjectURL(blob);
    const a = document.createElement('a'); a.href = url; a.download = `GSTR3B_${gstin || 'export'}_${ret_period}.json`; a.click();
    URL.revokeObjectURL(url);
    thagaval('GSTR-3B JSON exported — upload to GST portal offline tool', 'success');
  };

  // ========== GSTR-1 JSON Export ==========
  const exportGSTR1JSON = () => {
    if (filteredBills.length === 0) { thagaval('No invoices to export', 'warning'); return; }
    const gstin = profile.gstin || '';
    const fp = filterMode === 'month'
      ? String(parseInt(monthFilter) + 1).padStart(2, '0') + yearFilter
      : getFilingPeriod(filteredBills[0]?.invoiceDate);

    const b2bMap = {};
    b2bRegular.forEach(bill => {
      const { client, totals, items, details } = bill.data;
      const ctin = client.gstin;
      if (!b2bMap[ctin]) b2bMap[ctin] = { ctin, inv: [] };
      const isInter = billIsInterstate(bill);
      const pos = getStateCode(details?.placeOfSupply || client?.maanilam || '');
      const rateMap = {};
      (items || []).forEach(item => {
        const rate = item.taxPercent || 0;
        if (!rateMap[rate]) rateMap[rate] = { txval: 0, iamt: 0, camt: 0, samt: 0 };
        const split = computeItemTaxSplit(item, isInter, !!bill.data?.taxInclusive);
        rateMap[rate].txval += split.taxable; rateMap[rate].iamt += split.igst; rateMap[rate].camt += split.cgst; rateMap[rate].samt += split.sgst;
      });
      b2bMap[ctin].inv.push({
        inum: bill.invoiceNumber, idt: formatDateGST(bill.invoiceDate), val: round2(totals?.total || 0), pos, rchrg: 'N', inv_typ: 'R',
        itms: Object.entries(rateMap as any).map(([rt, d]: [any, any], i) => ({ num: i + 1, itm_det: { rt: Number(rt), txval: round2(d.txval), iamt: round2(d.iamt), camt: round2(d.camt), samt: round2(d.samt), csamt: 0 } })),
      });
    });

    const b2csMap = {};
    b2cSmall.forEach(bill => {
      const { profile: prof, client, items, details } = bill.data;
      const isInter = billIsInterstate(bill);
      const pos = getStateCode(details?.placeOfSupply || client?.maanilam || prof?.maanilam || '');
      const splyTy = isInter ? 'INTER' : 'INTRA';
      (items || []).forEach(item => {
        const rate = item.taxPercent || 0; const key = `${splyTy}_${pos}_${rate}`;
        if (!b2csMap[key]) b2csMap[key] = { sply_ty: splyTy, pos, rt: rate, txval: 0, iamt: 0, camt: 0, samt: 0, csamt: 0 };
        const split = computeItemTaxSplit(item, isInter, !!bill.data?.taxInclusive);
        b2csMap[key].txval += split.taxable; b2csMap[key].iamt += split.igst; b2csMap[key].camt += split.cgst; b2csMap[key].samt += split.sgst;
      });
    });
    const b2csArr = Object.values(b2csMap as any).map((d: any) => ({ ...d, txval: round2(d.txval), iamt: round2(d.iamt), camt: round2(d.camt), samt: round2(d.samt) }));

    const b2clMap = {};
    b2cLarge.forEach(bill => {
      const { client, totals, items, details } = bill.data;
      const pos = getStateCode(details?.placeOfSupply || client?.maanilam || '');
      if (!b2clMap[pos]) b2clMap[pos] = { pos, inv: [] };
      const rateMap = {};
      (items || []).forEach(item => {
        const rate = item.taxPercent || 0; if (!rateMap[rate]) rateMap[rate] = { txval: 0, iamt: 0 };
        const split = computeItemTaxSplit(item, true); rateMap[rate].txval += split.taxable; rateMap[rate].iamt += split.igst;
      });
      b2clMap[pos].inv.push({ inum: bill.invoiceNumber, idt: formatDateGST(bill.invoiceDate), val: round2(totals?.total || 0), itms: Object.entries(rateMap as any).map(([rt, d]: [any, any], i) => ({ num: i + 1, itm_det: { rt: Number(rt), txval: round2(d.txval), iamt: round2(d.iamt), csamt: 0 } })) });
    });

    const cdnrMap = {};
    creditNotes.filter((b: any) => b.data?.client?.gstin).forEach(bill => {
      const { client, totals, items, details } = bill.data;
      const ctin = client.gstin; if (!cdnrMap[ctin]) cdnrMap[ctin] = { ctin, nt: [] };
      const isInter = billIsInterstate(bill);
      const pos = getStateCode(details?.placeOfSupply || client?.maanilam || '');
      const rateMap = {};
      (items || []).forEach(item => {
        const rate = item.taxPercent || 0; if (!rateMap[rate]) rateMap[rate] = { txval: 0, iamt: 0, camt: 0, samt: 0 };
        const split = computeItemTaxSplit(item, isInter, !!bill.data?.taxInclusive);
        rateMap[rate].txval += split.taxable; rateMap[rate].iamt += split.igst; rateMap[rate].camt += split.cgst; rateMap[rate].samt += split.sgst;
      });
      cdnrMap[ctin].nt.push({ ntty: 'C', nt_num: bill.invoiceNumber, nt_dt: formatDateGST(bill.invoiceDate), val: round2(totals?.total || 0), pos, rchrg: 'N', inv_typ: 'R',
        itms: Object.entries(rateMap as any).map(([rt, d]: [any, any], i) => ({ num: i + 1, itm_det: { rt: Number(rt), txval: round2(d.txval), iamt: round2(d.iamt), camt: round2(d.camt), samt: round2(d.samt), csamt: 0 } })) });
    });

    const hsnJsonMap = {};
    let unknownUnitCount = 0;
    filteredBills.forEach(bill => {
      const { items } = bill.data;
      const isInter = billIsInterstate(bill);
      (items || []).forEach(item => {
        const hsn = item.hsn || ''; const rate = item.taxPercent || 0; const key = `${hsn}_${rate}`;
        const uqc = getUnitUQC(item.unit);
        if (uqc === 'OTH' && item.unit) unknownUnitCount += 1;
        if (!hsnJsonMap[key]) hsnJsonMap[key] = { hsn_sc: hsn, desc: item.name?.primary || '', uqc, qty: 0, rt: rate, txval: 0, iamt: 0, camt: 0, samt: 0, csamt: 0 };
        const split = computeItemTaxSplit(item, isInter, !!bill.data?.taxInclusive);
        hsnJsonMap[key].qty += item.qty || 0; hsnJsonMap[key].txval += split.taxable; hsnJsonMap[key].iamt += split.igst; hsnJsonMap[key].camt += split.cgst; hsnJsonMap[key].samt += split.sgst;
      });
    });

    const docDet = Object.entries(docSummary as any).map(([, d]: [any, any], i) => ({ doc_num: i + 1, docs: [{ num: 1, from: d.from, to: d.to, totnum: d.total, cancel: 0, net_issue: d.total }] }));

    const gstr1 = {
      gstin, fp,
      b2b: Object.values(b2bMap), b2cs: b2csArr,
      ...(Object.keys(b2clMap).length > 0 ? { b2cl: Object.values(b2clMap) } : {}),
      ...(Object.keys(cdnrMap).length > 0 ? { cdnr: Object.values(cdnrMap) } : {}),
      hsn: { data: Object.values(hsnJsonMap as any).map((r: any, i) => ({ num: i + 1, ...r, txval: round2(r.txval), iamt: round2(r.iamt), camt: round2(r.camt), samt: round2(r.samt) })) },
      doc_issue: { doc_det: docDet },
    };

    const blob = new Blob([JSON.stringify(gstr1, null, 2)], { type: 'application/json' });
    const url = URL.createObjectURL(blob);
    const a = document.createElement('a'); a.href = url; a.download = `GSTR1_${gstin || 'export'}_${fp}.json`; a.click();
    URL.revokeObjectURL(url);
    if (unknownUnitCount > 0) {
      // Unknown / custom units fall through to UQC 'OTH' in the GSTR-1 HSN summary.
      // The portal accepts OTH but it's lossy — warn the user so they can map their
      // custom unit to a standard UQC if they care.
      thagaval(`GSTR-1 JSON exported. ⚠ ${unknownUnitCount} item(s) used a custom unit and were exported as UQC 'OTH'.`, 'warning');
    } else {
      thagaval('GSTR-1 JSON exported — upload to GST portal offline tool', 'success');
    }
  };

  // ========== RENDER ==========
  const totalTax = grandTotals.cgst + grandTotals.sgst + grandTotals.igst;
  const netPayable = netTax.igst + netTax.cgst + netTax.sgst;

  // Pagination & Search
  const searchedBills = filteredBills.filter(b => {
    if (!search.trim()) return true;
    const term = search.toLowerCase();
    const itemsText = b.data?.items ? b.data.items.map((i: any) => `${i.name || ''} ${i.nameEn || ''}`).join(' ') : '';
    const searchable = [
      b.invoiceNumber, b.clientName, b.clientNameEn, b.data?.client?.gstin,
      b.data?.totals?.total?.toString(), itemsText
    ].filter(Boolean).join(' ').toLowerCase();
    return searchable.includes(term);
  });
  const itemsPerPage = 8;
  const totalPages = Math.ceil(searchedBills.length / itemsPerPage);
  const safePage = Math.max(1, Math.min(page, totalPages === 0 ? 1 : totalPages));
  const paginatedBills = searchedBills.slice((safePage - 1) * itemsPerPage, safePage * itemsPerPage);

  return (
    <Box sx={{ pt: { xs: 3, md: 4 }, pb: { xs: 12, md: 4 }, px: { xs: 0, md: 4 }, maxWidth: 1200, mx: 'auto' }}>
      {/* Page Header (Hidden on Mobile) */}
      <Box sx={{ mb: 4, display: { xs: 'none', md: 'flex' }, justifyContent: 'space-between', alignItems: 'flex-start' }}>
        <Box sx={{ ml: { xs: 0, md: 2 } }}>
          <Typography variant="h4" sx={{ fontWeight: 800, mb: 1 }}>
            {t('hc_taxDataExport')}
          </Typography>
          <Typography variant="body1" color="text.secondary">
            GSTR returns, e-Way Bill, TDS/TCS, HSN summaries
          </Typography>
        </Box>
      </Box>

      {/* Period Selector - MOBILE (Unified Card) */}
      <Box sx={{ display: { xs: 'block', sm: 'none' }, mb: 4 }}>
        <ElvanCard boxSx={{ px: 3, pt: 2, pb: 2 }}>
          <Grid container spacing={2} sx={{ alignItems: 'center' }}>
            <Grid size={12}>
              <TextField 
                select 
                fullWidth
                label={t('filterByLabel') as string} 
                value={filterMode} 
                onChange={e => setFilterMode(e.target.value)}
                sx={{ '& .MuiFilledInput-root': { backgroundColor: isDark ? 'rgba(255,255,255,0.08)' : '#FFFFFF' } }}
              >
                <MenuItem value="month">{t('monthlyTab')}</MenuItem>
                <MenuItem value="quarter">Quarterly (QRMP)</MenuItem>
                <MenuItem value="fy">{t('hc_fullYear')}</MenuItem>
              </TextField>
            </Grid>
            
            {filterMode === 'fy' ? (
              <Grid size={12}>
                <TextField select fullWidth label={t('fiscalYearLabel') as string} value={fyFilter} onChange={e => setFyFilter(e.target.value)} sx={{ '& .MuiFilledInput-root': { backgroundColor: isDark ? 'rgba(255,255,255,0.08)' : '#FFFFFF' } }}>
                  {fyOptions.map(fy => <MenuItem key={fy.value} value={fy.value}>{fy.label}</MenuItem>)}
                </TextField>
              </Grid>
            ) : filterMode === 'quarter' ? (
              <>
                <Grid size={12}>
                  <TextField select fullWidth label="Quarter" value={quarterFilter} onChange={e => setQuarterFilter(e.target.value)} sx={{ '& .MuiFilledInput-root': { backgroundColor: isDark ? 'rgba(255,255,255,0.08)' : '#FFFFFF' } }}>
                    {QUARTERS.map(q => <MenuItem key={q.id} value={q.id}>{q.label}</MenuItem>)}
                  </TextField>
                </Grid>
                <Grid size={12}>
                  <TextField select fullWidth label={t('yearLabel') as string} value={yearFilter} onChange={e => setYearFilter(Number(e.target.value))} sx={{ '& .MuiFilledInput-root': { backgroundColor: isDark ? 'rgba(255,255,255,0.08)' : '#FFFFFF' } }}>
                    {yearOptions.map(y => <MenuItem key={y} value={y}>{y}</MenuItem>)}
                  </TextField>
                </Grid>
              </>
            ) : (
              <>
                <Grid size={12}>
                  <TextField select fullWidth label={t('monthLabel') as string} value={monthFilter} onChange={e => setMonthFilter(Number(e.target.value))} sx={{ '& .MuiFilledInput-root': { backgroundColor: isDark ? 'rgba(255,255,255,0.08)' : '#FFFFFF' } }}>
                    {(MONTHS as string[]).map((m, i) => <MenuItem key={i} value={i}>{m}</MenuItem>)}
                  </TextField>
                </Grid>
                <Grid size={12}>
                  <TextField select fullWidth label={t('yearLabel') as string} value={yearFilter} onChange={e => setYearFilter(Number(e.target.value))} sx={{ '& .MuiFilledInput-root': { backgroundColor: isDark ? 'rgba(255,255,255,0.08)' : '#FFFFFF' } }}>
                    {yearOptions.map(y => <MenuItem key={y} value={y}>{y}</MenuItem>)}
                  </TextField>
                </Grid>
              </>
            )}
            

          </Grid>
        </ElvanCard>
      </Box>

      {/* Period Selector - DESKTOP (Split Cards) */}
      <Box sx={{ display: { xs: 'none', sm: 'flex' }, gap: 2, mb: 4, flexWrap: 'wrap', alignItems: 'center' }}>
        <TextField select label={t('filterByLabel') as string} value={filterMode} onChange={e => setFilterMode(e.target.value)} sx={{ minWidth: 200 }}>
          <MenuItem value="month">{t('monthlyTab')}</MenuItem>
          <MenuItem value="quarter">Quarterly (QRMP)</MenuItem>
          <MenuItem value="fy">{t('hc_fullYear')}</MenuItem>
        </TextField>
        
        {filterMode === 'fy' ? (
          <TextField select label={t('fiscalYearLabel') as string} value={fyFilter} onChange={e => setFyFilter(e.target.value)} sx={{ minWidth: 200 }}>
            {fyOptions.map(fy => <MenuItem key={fy.value} value={fy.value}>{fy.label}</MenuItem>)}
          </TextField>
        ) : filterMode === 'quarter' ? (
          <Box sx={{ display: 'flex', gap: 2 }}>
            <TextField select label="Quarter" value={quarterFilter} onChange={e => setQuarterFilter(e.target.value)} sx={{ minWidth: 200 }}>
              {QUARTERS.map(q => <MenuItem key={q.id} value={q.id}>{q.label}</MenuItem>)}
            </TextField>
            <TextField select label={t('yearLabel') as string} value={yearFilter} onChange={e => setYearFilter(Number(e.target.value))} sx={{ minWidth: 200 }}>
              {yearOptions.map(y => <MenuItem key={y} value={y}>{y}</MenuItem>)}
            </TextField>
          </Box>
        ) : (
          <Box sx={{ display: 'flex', gap: 2 }}>
            <TextField select label={t('monthLabel') as string} value={monthFilter} onChange={e => setMonthFilter(Number(e.target.value))} sx={{ minWidth: 200 }}>
              {(MONTHS as string[]).map((m, i) => <MenuItem key={i} value={i}>{m}</MenuItem>)}
            </TextField>
            <TextField select label={t('yearLabel') as string} value={yearFilter} onChange={e => setYearFilter(Number(e.target.value))} sx={{ minWidth: 200 }}>
              {yearOptions.map(y => <MenuItem key={y} value={y}>{y}</MenuItem>)}
            </TextField>
          </Box>
        )}


      </Box>

      {/* Warnings & Alerts */}
      {warnings.filter(w => w.type === 'error').length > 0 && (
        <Alert severity="error" sx={{ mb: 2, borderRadius: '24px', bgcolor: isDark ? 'rgba(255,255,255,0.08)' : '#FFFFFF', color: 'text.primary', '& .MuiAlert-icon': { color: 'error.main' }, alignItems: 'center' }}>
          {warnings.filter(w => w.type === 'error').slice(0, 3).map(w => w.msg).join(' | ')}
        </Alert>
      )}

      {isNilReturn && (
        <Alert severity="warning" sx={{ mb: 2, borderRadius: '24px', bgcolor: isDark ? 'rgba(255,255,255,0.08)' : '#FFFFFF', color: 'text.primary', '& .MuiAlert-icon': { color: 'warning.main' } }}>
          No invoices or expenses found — file a NIL return on the GST portal. NIL returns are mandatory.
        </Alert>
      )}

      {/* GST Summary Cards (ElvanCard style) */}
      <Box sx={{ mb: 2, px: 2 }}>
        <Typography variant="subtitle1" sx={{ fontWeight: 700 }}>
          {t('moduleDesc_gst') || 'GST Summary'}
        </Typography>
      </Box>
      <Grid container spacing={2} sx={{ mb: 4 }}>
        <Grid size={{ xs: 12, sm: 3 }}>
          <ElvanCard sx={{ height: '100%' }} boxSx={{ display: 'flex', flexDirection: 'row', alignItems: 'center', gap: 2 }}>
            <Box sx={{ width: 48, height: 48, borderRadius: '16px', bgcolor: 'action.hover', color: 'text.primary', display: 'flex', alignItems: 'center', justifyContent: 'center', flexShrink: 0 }}>
              <FileText size={24} weight="regular" />
            </Box>
            <Box>
              <Typography variant="body2" color="text.secondary" mb={0.5} sx={{ fontWeight: 600 }}>{t('hc_totalInvoices')}</Typography>
              <Typography variant="h6" sx={{ fontWeight: 800 }}>{filteredBills.length}</Typography>
            </Box>
          </ElvanCard>
        </Grid>

        <Grid size={{ xs: 12, sm: 3 }}>
          <ElvanCard sx={{ height: '100%' }} boxSx={{ display: 'flex', flexDirection: 'row', alignItems: 'center', gap: 2 }}>
            <Box sx={{ width: 48, height: 48, borderRadius: '16px', bgcolor: 'action.hover', color: 'text.primary', display: 'flex', alignItems: 'center', justifyContent: 'center', flexShrink: 0 }}>
              <Wallet size={24} weight="regular" />
            </Box>
            <Box>
              <Typography variant="body2" color="text.secondary" mb={0.5} sx={{ fontWeight: 600 }}>Total Sales (Taxable)</Typography>
              <Typography variant="h6" sx={{ fontWeight: 800 }}>{formatCurrency(grandTotals.taxable)}</Typography>
            </Box>
          </ElvanCard>
        </Grid>

        <Grid size={{ xs: 12, sm: 3 }}>
          <ElvanCard sx={{ height: '100%' }} boxSx={{ display: 'flex', flexDirection: 'row', alignItems: 'center', gap: 2 }}>
            <Box sx={{ width: 48, height: 48, borderRadius: '16px', bgcolor: 'action.hover', color: 'text.primary', display: 'flex', alignItems: 'center', justifyContent: 'center', flexShrink: 0 }}>
              <TrendDown size={24} weight="regular" />
            </Box>
            <Box>
              <Typography variant="body2" color="text.secondary" mb={0.5} sx={{ fontWeight: 600 }}>{t('hc_taxCollected')}</Typography>
              <Typography variant="h6" sx={{ fontWeight: 800 }}>{formatCurrency(totalTax)}</Typography>
            </Box>
          </ElvanCard>
        </Grid>

        <Grid size={{ xs: 12, sm: 3 }}>
          <ElvanCard sx={{ height: '100%' }} boxSx={{ display: 'flex', flexDirection: 'row', alignItems: 'center', gap: 2 }}>
            <Box sx={{ width: 48, height: 48, borderRadius: '16px', bgcolor: 'action.hover', color: 'text.primary', display: 'flex', alignItems: 'center', justifyContent: 'center', flexShrink: 0 }}>
              <TrendUp size={24} weight="regular" />
            </Box>
            <Box>
              <Typography variant="body2" color="text.secondary" mb={0.5} sx={{ fontWeight: 600 }}>{t('hc_netPayable')}</Typography>
              <Typography variant="h6" sx={{ fontWeight: 800, color: 'primary.main' }}>{formatCurrency(netPayable)}</Typography>
            </Box>
          </ElvanCard>
        </Grid>
      </Grid>

      {/* Tabs */}
      <Stack direction="row" spacing={1} sx={{ mb: 2, display: 'none', flexWrap: 'wrap' }}>
        {[
          { id: 'gstr1', label: 'GSTR-1', icon: BarChart },
          { id: 'gstr3b', label: 'GSTR-3B', icon: Description },
          { id: 'gstr2b', label: 'GSTR-2B Reconciliation', icon: CheckCircle },
          { id: 'tds', label: 'TDS / TCS Report', icon: Description },
          { id: 'guide', label: 'Filing Guide', icon: MenuBook },
        ].map(tab => (
          <Button key={tab.id} variant={activeTab === tab.id ? 'contained' : 'outlined'} onClick={() => setActiveTab(tab.id)} startIcon={<tab.icon style={{ fontSize: 14 }} />} sx={{ borderRadius: 5, textTransform: 'none', px: 2, py: 0.5, boxShadow: 'none' }}>
            {tab.label}
          </Button>
        ))}
      </Stack>

      {/* ===================== SUMMARY TAB ===================== */}
      {activeTab === 'summary' && (
        <Stack spacing={3} ref={listTopRef} sx={{ scrollMarginTop: '100px' }}>
          <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: -1, flexWrap: 'wrap', gap: 2, px: { xs: 2, sm: 2.5 } }}>
            <Box>
              <Typography variant="h6" sx={{ fontWeight: "bold" }}>{t('hc_invoicesForThisPeriod')}</Typography>
              <Typography variant="body2" color="text.secondary">{searchedBills.length} invoices</Typography>
            </Box>
            <Box sx={{ display: 'flex', gap: 2, alignItems: 'center', width: { xs: '100%', md: 'auto' } }}>
              <Paper elevation={1} sx={{ ...getSearchPaperSx(isDark), flex: 1 }}>
                <MagnifyingGlass size={16} weight="regular" style={{ opacity: 0.5 }} />
                <input type="text" placeholder={t('search') || 'Search...'} value={search} onChange={e => { setSearch(e.target.value); setPage(1); }} style={searchInputStyle} />
                {search && <IconButton size="small" onClick={() => { setSearch(''); setPage(1); }}><X size={14} weight="regular" /></IconButton>}
              </Paper>
              <Tooltip title="Download Excel" placement="top">
                <IconButton onClick={exportSimpleCSV} sx={{ bgcolor: isDark ? 'rgba(255,255,255,0.08)' : '#FFFFFF', width: 48, height: 48, flexShrink: 0, transition: 'background 0.3s ease', '@media (hover: hover)': { '&:hover': { bgcolor: isDark ? 'rgba(255,255,255,0.12)' : '#F5F5F5' } } }}>
                  <DownloadSimple size={20} weight="fill" sx={{ fontSize: 20 }} />
                </IconButton>
              </Tooltip>
            </Box>
          </Box>
          
          {paginatedBills.length === 0 ? (
            <ElvanCard boxSx={{ p: 6, textAlign: 'center' }}>
              <Box color="text.secondary" mb={2}>
                <FileText size={48} weight="regular" style={{ opacity: 0.5 }} />
              </Box>
              <Typography color="text.secondary">No invoices match your search</Typography>
            </ElvanCard>
          ) : (
            <Box sx={{ display: 'grid', gridTemplateColumns: { xs: '1fr', md: '1fr 1fr' }, gap: 2 }}>
              {paginatedBills.map((b: any, index: number) => {
                let cNamePrimary = b.clientName || getDynamicField(b.data?.client, 'name', profile, true) || '';
                let cNameSecondary = '';
                if ((profile as any)?.enableBilingual !== false) {
                  const enName = b.clientNameEn || getDynamicField(b.data?.client, 'name', profile, false) || b.data?.client?.nameEn || b.data?.client?.peyarEn || '';
                  if (enName && enName !== cNamePrimary) cNameSecondary = enName;
                }
                const globalIndex = (safePage - 1) * itemsPerPage + index;
                return (
                  <ElvanCard key={b.id || index} sx={{ height: '100%' }}>
                    <Stack direction="row" spacing={2} sx={{ justifyContent: 'space-between', alignItems: 'center', height: '100%' }}>
                      <Box sx={{ display: 'flex', gap: 2, alignItems: 'flex-start', flex: 1, width: '100%' }}>
                        <Box sx={{ display: 'flex', alignItems: 'center', justifyContent: 'center', width: 28, height: 28, mt: 0.15, borderRadius: '50%', bgcolor: isDark ? 'rgba(255,255,255,0.12)' : 'rgba(0,0,0,0.08)', flexShrink: 0 }}>
                          <Typography variant="caption" sx={{ fontWeight: 800, color: isDark ? '#FFFFFF' : '#000000', fontSize: '0.7rem', lineHeight: 1, position: 'relative', top: '1px' }}>
                            {(globalIndex + 1).toString().padStart(2, '0')}
                          </Typography>
                        </Box>
                        <Box>
                          <Typography variant="subtitle1" sx={{ fontWeight: "bold" }}>
                            {cNamePrimary || '-'}
                          </Typography>
                          <Box sx={{ display: 'flex', flexDirection: 'column', gap: 0.5, color: 'text.secondary', mt: 0.5 }}>
                            {cNameSecondary && <Typography variant="body2" sx={{ fontSize: '0.85rem', fontWeight: 500 }}>{cNameSecondary}</Typography>}
                            <Typography variant="body2" sx={{ fontSize: '0.85rem', mt: 0.5 }}>
                              {b.invoiceNumber} <span style={{ opacity: 0.6, margin: '0 6px' }}>•</span> {b.invoiceDate ? new Date(b.invoiceDate).toLocaleDateString('en-IN') : '-'}
                            </Typography>
                            <Typography variant="body2" sx={{ fontSize: '0.85rem', mt: 0.5 }}>
                              Taxable: {formatCurrency(getTaxableAmount(b.data?.totals))} | Tax: {formatCurrency((b.data?.totals?.cgst || 0) + (b.data?.totals?.sgst || 0) + (b.data?.totals?.igst || 0))}
                            </Typography>
                          </Box>
                        </Box>
                      </Box>
                      <Box sx={{ display: 'flex', gap: 1, alignItems: 'flex-end', flexDirection: 'column', alignSelf: 'stretch', justifyContent: 'space-between' }}>
                        <Box sx={{ display: 'flex', gap: 0.5, mt: -0.5, mr: -0.5 }}></Box>
                        <Typography variant="subtitle1" color="primary.main" sx={{ fontWeight: 800 }}>
                          {formatCurrency(b.data?.totals?.total || 0)}
                        </Typography>
                      </Box>
                    </Stack>
                  </ElvanCard>
                );
              })}
            </Box>
          )}

          {totalPages > 1 && (
            <Box sx={{ display: 'flex', justifyContent: 'center', mt: 4 }}>
              <Pagination count={totalPages} page={safePage} onChange={(_e, val) => {
                setPage(val);
                if (listTopRef.current) {
                  listTopRef.current.scrollIntoView({ behavior: 'smooth', block: 'start' });
                }
              }} color="primary" size="large" sx={{ '& .MuiPaginationItem-root': { fontWeight: 600 } }} />
            </Box>
          )}
        </Stack>
      )}

      {/* ===================== GSTR-1 TAB ===================== */}
      {activeTab === 'gstr1' && (
        <>
          {/* Actions bar */}
          <Stack direction="row" spacing={1} sx={{ mb: 2, alignItems: 'center', flexWrap: 'wrap' }}>
            <Button variant="contained" onClick={exportGSTR1JSON} startIcon={<UploadSimple size={20} weight="fill" sx={{ fontSize: 14 }} />} sx={{ borderRadius: 5, textTransform: 'none', px: 2, py: 0.5, boxShadow: 'none' }}>JSON Export</Button>
            <Button variant="outlined" onClick={exportB2B} startIcon={<DownloadSimple size={20} weight="fill" sx={{ fontSize: 14 }} />} sx={{ borderRadius: 5, textTransform: 'none', px: 2, py: 0.5 }}>B2B</Button>
            <Button variant="outlined" onClick={exportB2C} startIcon={<DownloadSimple size={20} weight="fill" sx={{ fontSize: 14 }} />} sx={{ borderRadius: 5, textTransform: 'none', px: 2, py: 0.5 }}>B2C</Button>
            <Button variant="outlined" onClick={exportHSN} startIcon={<DownloadSimple size={20} weight="fill" sx={{ fontSize: 14 }} />} sx={{ borderRadius: 5, textTransform: 'none', px: 2, py: 0.5 }}>HSN</Button>
            <Button variant="outlined" onClick={exportCDNR} startIcon={<DownloadSimple size={20} weight="fill" sx={{ fontSize: 14 }} />} sx={{ borderRadius: 5, textTransform: 'none', px: 2, py: 0.5 }}>CDNR</Button>
            <Button variant="outlined" onClick={exportDocSummary} startIcon={<DownloadSimple size={20} weight="fill" sx={{ fontSize: 14 }} />} sx={{ borderRadius: 5, textTransform: 'none', px: 2, py: 0.5 }}>Docs</Button>
            {!periodFiling.gstr1 && (
              <Button variant="outlined" color="success" onClick={() => markFiled('gstr1')} startIcon={<CheckCircle size={20} weight="fill" sx={{ fontSize: 14 }} />} sx={{ ml: 'auto', borderRadius: 5, textTransform: 'none', px: 2, py: 0.5 }}>
                Mark Filed
              </Button>
            )}
          </Stack>

          {/* B2B — Table 4A */}
          <Paper elevation={0} sx={{ mb: 3, borderRadius: 3, border: '1px solid', borderColor: 'divider', overflow: 'hidden' }}>
            <Box sx={{ p: 2, borderBottom: '1px solid', borderColor: 'divider', bgcolor: 'background.paper' }}>
              <Typography variant="h6" sx={{ fontWeight: "bold" ,  m: 0 }}>{t('b2bSalesTable')}</Typography>
              <Typography variant="body2" color="text.secondary">{b2bRows.length} {b2bRows.length !== 1 ? t('invoicesWord') : t('invoiceWord')}</Typography>
            </Box>
            {b2bRows.length === 0 ? (
              <Typography sx={{ p: 3, color: 'text.secondary' }}>{t('noB2bInvoices')}</Typography>
            ) : (
              <TableContainer>
                <Table size="small">
                  <TableHead sx={{ bgcolor: 'rgba(0,0,0,0.02)' }}>
                    <TableRow>
                      <TableCell sx={{ fontWeight: 'bold' }}>GSTIN</TableCell>
                      <TableCell sx={{ fontWeight: 'bold' }}>{t('hc_client')}</TableCell>
                      <TableCell sx={{ fontWeight: 'bold' }}>{t('hc_invoiceNo')}</TableCell>
                      <TableCell sx={{ fontWeight: 'bold' }}>Date</TableCell>
                      <TableCell sx={{ fontWeight: 'bold' }}>POS</TableCell>
                      <TableCell sx={{ fontWeight: 'bold' }}>{t('hc_type')}</TableCell>
                      <TableCell align="right" sx={{ fontWeight: 'bold' }}>{t('taxableLabel')}</TableCell>
                      <TableCell align="right" sx={{ fontWeight: 'bold' }}>{t('cgstCol')}</TableCell>
                      <TableCell align="right" sx={{ fontWeight: 'bold' }}>{t('sgstCol')}</TableCell>
                      <TableCell align="right" sx={{ fontWeight: 'bold' }}>{t('igstCol')}</TableCell>
                      <TableCell align="right" sx={{ fontWeight: 'bold' }}>{t('totalCol')}</TableCell>
                    </TableRow>
                  </TableHead>
                  <TableBody>
                    {b2bRows.map((r, i) => (
                      <TableRow key={i} hover>
                        <TableCell><Chip size="small" label={r.gstin} sx={{ borderRadius: 1 }} /></TableCell>
                        <TableCell sx={{ fontWeight: 500 }}>{r.clientName}</TableCell>
                        <TableCell>{r.invoiceNo}</TableCell>
                        <TableCell sx={{ color: 'text.secondary' }}>{r.date ? new Date(r.date).toLocaleDateString('en-IN') : ''}</TableCell>
                        <TableCell sx={{ color: 'text.secondary' }}>{(r as any).pos}</TableCell>
                        <TableCell><Chip size="small" label={r.supplyType} variant="outlined" sx={{ borderRadius: 1 }} /></TableCell>
                        <TableCell align="right">{formatCurrency((r as any).taxable)}</TableCell>
                        <TableCell align="right">{formatCurrency(r.cgst)}</TableCell>
                        <TableCell align="right">{formatCurrency(r.sgst)}</TableCell>
                        <TableCell align="right">{formatCurrency((r as any).igst)}</TableCell>
                        <TableCell align="right" sx={{ fontWeight: 'bold' }}>{formatCurrency(r.total)}</TableCell>
                      </TableRow>
                    ))}
                    <TableRow sx={{ bgcolor: 'rgba(0,0,0,0.02)', '& td': { fontWeight: 'bold', borderTop: '2px solid rgba(224, 224, 224, 1)' } }}>
                      <TableCell colSpan={6}>{t('hc_b2bTotal')}</TableCell>
                      <TableCell align="right">{formatCurrency(b2bTotals.taxable)}</TableCell>
                      <TableCell align="right">{formatCurrency(b2bTotals.cgst)}</TableCell>
                      <TableCell align="right">{formatCurrency(b2bTotals.sgst)}</TableCell>
                      <TableCell align="right">{formatCurrency(b2bTotals.igst)}</TableCell>
                      <TableCell align="right">{formatCurrency(b2bTotals.total)}</TableCell>
                    </TableRow>
                  </TableBody>
                </Table>
              </TableContainer>
            )}
          </Paper>

          {/* Credit/Debit Notes — Table 9B */}
          {creditNotes.length > 0 && (
            <Paper elevation={0} sx={{ mb: 3, borderRadius: 3, border: '1px solid', borderColor: 'divider', overflow: 'hidden' }}>
              <Box sx={{ p: 2, borderBottom: '1px solid', borderColor: 'divider', bgcolor: 'background.paper' }}>
                <Typography variant="h6" sx={{ fontWeight: "bold" ,  m: 0 }}>{t('hc_creditdebitNotesTable9b')}</Typography>
                <Typography variant="body2" color="text.secondary">{creditNotes.length} note{creditNotes.length !== 1 ? 's' : ''}</Typography>
              </Box>
              <TableContainer>
                <Table size="small">
                  <TableHead sx={{ bgcolor: 'rgba(0,0,0,0.02)' }}>
                    <TableRow>
                      <TableCell sx={{ fontWeight: 'bold' }}>GSTIN</TableCell>
                      <TableCell sx={{ fontWeight: 'bold' }}>{t('hc_client')}</TableCell>
                      <TableCell sx={{ fontWeight: 'bold' }}>{t('hc_noteNo')}</TableCell>
                      <TableCell sx={{ fontWeight: 'bold' }}>Date</TableCell>
                      <TableCell align="right" sx={{ fontWeight: 'bold' }}>{t('taxableLabel')}</TableCell>
                      <TableCell align="right" sx={{ fontWeight: 'bold' }}>Tax</TableCell>
                      <TableCell align="right" sx={{ fontWeight: 'bold' }}>{t('totalCol')}</TableCell>
                    </TableRow>
                  </TableHead>
                  <TableBody>
                    {creditNotes.map((bill, i) => {
                      const { client, totals } = bill.data;
                      return (
                        <TableRow key={i} hover>
                          <TableCell><Chip size="small" label={client?.gstin || 'Unregistered'} sx={{ borderRadius: 1 }} /></TableCell>
                          <TableCell sx={{ fontWeight: 500 }}>{getDynamicField(client, 'name', profile, true) || bill.clientName}</TableCell>
                          <TableCell>{bill.invoiceNumber}</TableCell>
                          <TableCell sx={{ color: 'text.secondary' }}>{bill.invoiceDate ? new Date(bill.invoiceDate).toLocaleDateString('en-IN') : ''}</TableCell>
                          <TableCell align="right">{formatCurrency(getTaxableAmount(totals))}</TableCell>
                          <TableCell align="right">{formatCurrency((totals?.cgst || 0) + (totals?.sgst || 0) + (totals?.igst || 0))}</TableCell>
                          <TableCell align="right" sx={{ fontWeight: 'bold' }}>{formatCurrency(totals?.total || 0)}</TableCell>
                        </TableRow>
                      );
                    })}
                  </TableBody>
                </Table>
              </TableContainer>
            </Paper>
          )}

          {/* B2C — Table 7 (only show if data exists) */}
          <Paper elevation={0} sx={{ mb: 3, borderRadius: 3, border: '1px solid', borderColor: 'divider', overflow: 'hidden' }}>
            <Box sx={{ p: 2, borderBottom: '1px solid', borderColor: 'divider', bgcolor: 'background.paper' }}>
              <Typography variant="h6" sx={{ fontWeight: "bold" ,  m: 0 }}>{t('b2cSalesTable')}</Typography>
              <Typography variant="body2" color="text.secondary">
                {b2cBills.length} {b2cBills.length !== 1 ? t('invoicesWord') : t('invoiceWord')}
                {b2cLarge.length > 0 && <> ({b2cLarge.length} B2C Large)</>}
              </Typography>
            </Box>
            {b2cRates.length === 0 ? (
              <Typography sx={{ p: 3, color: 'text.secondary' }}>{t('noB2cInvoices')}</Typography>
            ) : (
              <TableContainer>
                <Table size="small">
                  <TableHead sx={{ bgcolor: 'rgba(0,0,0,0.02)' }}>
                    <TableRow>
                      <TableCell sx={{ fontWeight: 'bold' }}>{t('rateCol')}</TableCell>
                      <TableCell align="right" sx={{ fontWeight: 'bold' }}>{t('taxableLabel')}</TableCell>
                      <TableCell align="right" sx={{ fontWeight: 'bold' }}>{t('cgstCol')}</TableCell>
                      <TableCell align="right" sx={{ fontWeight: 'bold' }}>{t('sgstCol')}</TableCell>
                      <TableCell align="right" sx={{ fontWeight: 'bold' }}>{t('igstCol')}</TableCell>
                      <TableCell align="right" sx={{ fontWeight: 'bold' }}>{t('totalCol')}</TableCell>
                    </TableRow>
                  </TableHead>
                  <TableBody>
                    {b2cRates.map(rate => {
                      const d = b2cByRate[rate];
                      return (
                        <TableRow key={rate} hover>
                          <TableCell><Chip size="small" label={`${rate}%`} variant="outlined" sx={{ borderRadius: 1 }} /></TableCell>
                          <TableCell align="right">{formatCurrency(d.taxable)}</TableCell>
                          <TableCell align="right">{formatCurrency(d.cgst)}</TableCell>
                          <TableCell align="right">{formatCurrency(d.sgst)}</TableCell>
                          <TableCell align="right">{formatCurrency(d.igst)}</TableCell>
                          <TableCell align="right" sx={{ fontWeight: 'bold' }}>{formatCurrency(d.total)}</TableCell>
                        </TableRow>
                      );
                    })}
                    <TableRow sx={{ bgcolor: 'rgba(0,0,0,0.02)', '& td': { fontWeight: 'bold', borderTop: '2px solid rgba(224, 224, 224, 1)' } }}>
                      <TableCell>{t('b2cTotal')}</TableCell>
                      <TableCell align="right">{formatCurrency(b2cTotals.taxable)}</TableCell>
                      <TableCell align="right">{formatCurrency(b2cTotals.cgst)}</TableCell>
                      <TableCell align="right">{formatCurrency(b2cTotals.sgst)}</TableCell>
                      <TableCell align="right">{formatCurrency(b2cTotals.igst)}</TableCell>
                      <TableCell align="right">{formatCurrency(b2cTotals.total)}</TableCell>
                    </TableRow>
                  </TableBody>
                </Table>
              </TableContainer>
            )}
          </Paper>

          {/* HSN Summary — Table 12 */}
          <Paper elevation={0} sx={{ mb: 3, borderRadius: 3, border: '1px solid', borderColor: 'divider', overflow: 'hidden' }}>
            <Box sx={{ p: 2, borderBottom: '1px solid', borderColor: 'divider', bgcolor: 'background.paper' }}>
              <Typography variant="h6" sx={{ fontWeight: "bold" ,  m: 0 }}>{t('hsnSummaryTable')}</Typography>
              <Typography variant="body2" color="text.secondary">{hsnRows.length} {hsnRows.length !== 1 ? t('codesWord') : t('codeWord')}</Typography>
            </Box>
            {hsnRows.length === 0 ? (
              <Typography sx={{ p: 3, color: 'text.secondary' }}>{t('hc_noItemsFound')}</Typography>
            ) : (
              <TableContainer>
                <Table size="small">
                  <TableHead sx={{ bgcolor: 'rgba(0,0,0,0.02)' }}>
                    <TableRow>
                      <TableCell sx={{ fontWeight: 'bold' }}>{t('hsnCol')}</TableCell>
                      <TableCell sx={{ fontWeight: 'bold' }}>{t('descriptionCol')}</TableCell>
                      <TableCell align="right" sx={{ fontWeight: 'bold' }}>{t('qtyCol')}</TableCell>
                      <TableCell align="right" sx={{ fontWeight: 'bold' }}>{t('taxableLabel')}</TableCell>
                      <TableCell align="right" sx={{ fontWeight: 'bold' }}>{t('cgstCol')}</TableCell>
                      <TableCell align="right" sx={{ fontWeight: 'bold' }}>{t('sgstCol')}</TableCell>
                      <TableCell align="right" sx={{ fontWeight: 'bold' }}>{t('igstCol')}</TableCell>
                      <TableCell align="right" sx={{ fontWeight: 'bold' }}>{t('totalTaxCol')}</TableCell>
                    </TableRow>
                  </TableHead>
                  <TableBody>
                    {hsnRows.map((r: any, i) => (
                      <TableRow key={i} hover>
                        <TableCell><Chip size="small" label={r.hsn} sx={{ borderRadius: 1 }} /></TableCell>
                        <TableCell sx={{ fontWeight: 500 }}>{r.description}</TableCell>
                        <TableCell align="right">{r.qty}</TableCell>
                        <TableCell align="right">{formatCurrency((r as any).taxable)}</TableCell>
                        <TableCell align="right">{formatCurrency(r.cgst)}</TableCell>
                        <TableCell align="right">{formatCurrency(r.sgst)}</TableCell>
                        <TableCell align="right">{formatCurrency((r as any).igst)}</TableCell>
                        <TableCell align="right" sx={{ fontWeight: 'bold' }}>{formatCurrency(r.totalTax)}</TableCell>
                      </TableRow>
                    ))}
                    <TableRow sx={{ bgcolor: 'rgba(0,0,0,0.02)', '& td': { fontWeight: 'bold', borderTop: '2px solid rgba(224, 224, 224, 1)' } }}>
                      <TableCell colSpan={2}>{t('totalCol')}</TableCell>
                      <TableCell align="right">{Number(hsnRows.reduce((s: number, r: any) => s + (Number(r.qty) || 0), 0))}</TableCell>
                      <TableCell align="right">{formatCurrency(Number(hsnRows.reduce((s: number, r: any) => s + (Number(r.taxable) || 0), 0)))}</TableCell>
                      <TableCell align="right">{formatCurrency(Number(hsnRows.reduce((s: number, r: any) => s + (Number(r.cgst) || 0), 0)))}</TableCell>
                      <TableCell align="right">{formatCurrency(Number(hsnRows.reduce((s: number, r: any) => s + (Number(r.sgst) || 0), 0)))}</TableCell>
                      <TableCell align="right">{formatCurrency(Number(hsnRows.reduce((s: number, r: any) => s + (Number(r.igst) || 0), 0)))}</TableCell>
                      <TableCell align="right">{formatCurrency(Number(hsnRows.reduce((s: number, r: any) => s + (Number(r.totalTax) || 0), 0)))}</TableCell>
                    </TableRow>
                  </TableBody>
                </Table>
              </TableContainer>
            )}
          </Paper>

          {/* Document Summary — Table 13 */}
          <Paper elevation={0} sx={{ mb: 3, borderRadius: 3, border: '1px solid', borderColor: 'divider', overflow: 'hidden' }}>
            <Box sx={{ p: 2, borderBottom: '1px solid', borderColor: 'divider', bgcolor: 'background.paper' }}>
              <Typography variant="h6" sx={{ fontWeight: "bold" ,  m: 0 }}>{t('docSummaryTable')}</Typography>
            </Box>
            {Object.keys(docSummary).length === 0 ? (
              <Typography sx={{ p: 3, color: 'text.secondary' }}>{t('hc_noDocumentsIssued')}</Typography>
            ) : (
              <TableContainer>
                <Table size="small">
                  <TableHead sx={{ bgcolor: 'rgba(0,0,0,0.02)' }}>
                    <TableRow>
                      <TableCell sx={{ fontWeight: 'bold' }}>{t('docTypeCol')}</TableCell>
                      <TableCell sx={{ fontWeight: 'bold' }}>{t('fromCol')}</TableCell>
                      <TableCell sx={{ fontWeight: 'bold' }}>{t('toCol')}</TableCell>
                      <TableCell align="right" sx={{ fontWeight: 'bold' }}>{t('totalIssuedCol')}</TableCell>
                    </TableRow>
                  </TableHead>
                  <TableBody>
                    {Object.entries(docSummary as any).map(([prefix, d]: [string, any]) => (
                      <TableRow key={prefix} hover>
                        <TableCell sx={{ fontWeight: 500 }}>{d.type}</TableCell>
                        <TableCell sx={{ color: 'text.secondary' }}>{d.from}</TableCell>
                        <TableCell sx={{ color: 'text.secondary' }}>{d.to}</TableCell>
                        <TableCell align="right" sx={{ fontWeight: 'bold' }}>{d.total}</TableCell>
                      </TableRow>
                    ))}
                  </TableBody>
                </Table>
              </TableContainer>
            )}
          </Paper>

          {/* Grand Summary */}
          <Paper elevation={0} sx={{ borderRadius: 3, border: '1px solid', borderColor: 'divider', overflow: 'hidden' }}>
            <Box sx={{ p: 2, borderBottom: '1px solid', borderColor: 'divider', bgcolor: 'background.paper' }}>
              <Typography variant="h6" sx={{ fontWeight: "bold" ,  m: 0 }}>{t('gstr1SummaryTotals')}</Typography>
            </Box>
            <TableContainer>
              <Table size="small">
                <TableHead sx={{ bgcolor: 'rgba(0,0,0,0.02)' }}>
                  <TableRow>
                    <TableCell sx={{ fontWeight: 'bold' }}>{t('categoryCol')}</TableCell>
                    <TableCell align="right" sx={{ fontWeight: 'bold' }}>{t('taxableLabel')}</TableCell>
                    <TableCell align="right" sx={{ fontWeight: 'bold' }}>{t('cgstCol')}</TableCell>
                    <TableCell align="right" sx={{ fontWeight: 'bold' }}>{t('sgstCol')}</TableCell>
                    <TableCell align="right" sx={{ fontWeight: 'bold' }}>{t('igstCol')}</TableCell>
                    <TableCell align="right" sx={{ fontWeight: 'bold' }}>{t('totalCol')}</TableCell>
                  </TableRow>
                </TableHead>
                <TableBody>
                  <TableRow hover>
                    <TableCell sx={{ fontWeight: 500 }}>{t('b2bSalesCategory')}</TableCell>
                    <TableCell align="right">{formatCurrency(b2bTotals.taxable)}</TableCell>
                    <TableCell align="right">{formatCurrency(b2bTotals.cgst)}</TableCell>
                    <TableCell align="right">{formatCurrency(b2bTotals.sgst)}</TableCell>
                    <TableCell align="right">{formatCurrency(b2bTotals.igst)}</TableCell>
                    <TableCell align="right">{formatCurrency(b2bTotals.total)}</TableCell>
                  </TableRow>
                  <TableRow hover>
                    <TableCell sx={{ fontWeight: 500 }}>{t('b2cSalesCategory')}</TableCell>
                    <TableCell align="right">{formatCurrency(b2cTotals.taxable)}</TableCell>
                    <TableCell align="right">{formatCurrency(b2cTotals.cgst)}</TableCell>
                    <TableCell align="right">{formatCurrency(b2cTotals.sgst)}</TableCell>
                    <TableCell align="right">{formatCurrency(b2cTotals.igst)}</TableCell>
                    <TableCell align="right">{formatCurrency(b2cTotals.total)}</TableCell>
                  </TableRow>
                  <TableRow sx={{ bgcolor: 'rgba(0,0,0,0.02)', '& td': { fontWeight: 'bold', borderTop: '2px solid rgba(224, 224, 224, 1)' } }}>
                    <TableCell>{t('grandTotal')}</TableCell>
                    <TableCell align="right">{formatCurrency(grandTotals.taxable)}</TableCell>
                    <TableCell align="right">{formatCurrency(grandTotals.cgst)}</TableCell>
                    <TableCell align="right">{formatCurrency(grandTotals.sgst)}</TableCell>
                    <TableCell align="right">{formatCurrency(grandTotals.igst)}</TableCell>
                    <TableCell align="right">{formatCurrency(grandTotals.total)}</TableCell>
                  </TableRow>
                </TableBody>
              </Table>
            </TableContainer>
          </Paper>
        </>
      )}

      {/* ===================== GSTR-3B TAB ===================== */}
      {activeTab === 'gstr3b' && (
        <Stack spacing={3}>
          {/* Actions */}
          <Stack direction="row" spacing={1} sx={{ mb: 2, alignItems: 'center' }}>
            <Button variant="outlined" onClick={exportGSTR3B} startIcon={<DownloadSimple size={20} weight="fill" sx={{ fontSize: 14 }} />} sx={{ borderRadius: 5, textTransform: 'none', px: 2, py: 0.5 }}>3B CSV</Button>
            <Button variant="contained" onClick={exportGSTR3BJSON} startIcon={<UploadSimple size={20} weight="fill" sx={{ fontSize: 14 }} />} sx={{ borderRadius: 5, textTransform: 'none', px: 2, py: 0.5, boxShadow: 'none' }}>3B JSON</Button>
            {!periodFiling.gstr3b && (
              <Button variant="outlined" color="success" onClick={() => markFiled('gstr3b')} startIcon={<CheckCircle size={20} weight="fill" sx={{ fontSize: 14 }} />} sx={{ ml: 'auto', borderRadius: 5, textTransform: 'none', px: 2, py: 0.5 }}>
                Mark Filed
              </Button>
            )}
          </Stack>

          {/* Table 3.1 — Output Tax */}
          <Paper elevation={0} sx={{ borderRadius: 3, border: '1px solid', borderColor: 'divider', overflow: 'hidden' }}>
            <Box sx={{ p: 2, borderBottom: '1px solid', borderColor: 'divider', bgcolor: 'background.paper' }}>
              <Typography variant="h6" sx={{ fontWeight: "bold" ,  m: 0 }}>Table 3.1 — Outward Supplies & Tax</Typography>
            </Box>
            <TableContainer>
              <Table size="small">
                <TableHead sx={{ bgcolor: 'rgba(0,0,0,0.02)' }}>
                  <TableRow>
                    <TableCell sx={{ fontWeight: 'bold' }}>{t('hc_natureOfSupplies')}</TableCell>
                    <TableCell align="right" sx={{ fontWeight: 'bold' }}>{t('hc_taxableValue')}</TableCell>
                    <TableCell align="right" sx={{ fontWeight: 'bold' }}>{t('igstCol')}</TableCell>
                    <TableCell align="right" sx={{ fontWeight: 'bold' }}>{t('cgstCol')}</TableCell>
                    <TableCell align="right" sx={{ fontWeight: 'bold' }}>{t('sgstCol')}</TableCell>
                  </TableRow>
                </TableHead>
                <TableBody>
                  <TableRow hover>
                    <TableCell sx={{ fontWeight: 500 }}>(a) Outward taxable supplies (other than zero-rated, nil-rated and exempted)</TableCell>
                    <TableCell align="right">{formatCurrency(grandTotals.taxable)}</TableCell>
                    <TableCell align="right">{formatCurrency(grandTotals.igst)}</TableCell>
                    <TableCell align="right">{formatCurrency(grandTotals.cgst)}</TableCell>
                    <TableCell align="right">{formatCurrency(grandTotals.sgst)}</TableCell>
                  </TableRow>
                  <TableRow hover>
                    <TableCell sx={{ fontWeight: 500 }}>(b) Zero-rated supplies</TableCell>
                    <TableCell align="right">{formatCurrency(0)}</TableCell>
                    <TableCell align="right">{formatCurrency(0)}</TableCell>
                    <TableCell align="right">{formatCurrency(0)}</TableCell>
                    <TableCell align="right">{formatCurrency(0)}</TableCell>
                  </TableRow>
                  <TableRow hover>
                    <TableCell sx={{ fontWeight: 500 }}>(c) Non-GST supplies</TableCell>
                    <TableCell align="right">{formatCurrency(0)}</TableCell>
                    <TableCell colSpan={3} align="center" sx={{ color: 'text.secondary' }}>{t('hc_na')}</TableCell>
                  </TableRow>
                </TableBody>
              </Table>
            </TableContainer>
          </Paper>

          {/* Table 3.2 — Inter-maanilam Supplies */}
          {(() => {
            const interStateB2C = {};
            b2cBills.forEach(bill => {
              const { client, items, details } = bill.data;
              const isInter = billIsInterstate(bill);
              if (!isInter) return;
              const pos = getStateCode(details?.placeOfSupply || client?.maanilam || '');
              const posName = client?.maanilam || pos;
              (items || []).forEach(item => {
                if (!interStateB2C[posName]) interStateB2C[posName] = { pos: posName, taxable: 0, igst: 0 };
                const split = computeItemTaxSplit(item, true);
                interStateB2C[posName].taxable += split.taxable;
                interStateB2C[posName].igst += split.igst;
              });
            });
            const rows = Object.values(interStateB2C);
            if (rows.length === 0) return null;
            return (
              <Paper elevation={0} sx={{ borderRadius: 3, border: '1px solid', borderColor: 'divider', overflow: 'hidden' }}>
                <Box sx={{ p: 2, borderBottom: '1px solid', borderColor: 'divider', bgcolor: 'background.paper' }}>
                  <Typography variant="h6" sx={{ fontWeight: "bold" ,  m: 0 }}>{t('hc_table32IntermaanilamSuppliesTo')}</Typography>
                </Box>
                <TableContainer>
                  <Table size="small">
                    <TableHead sx={{ bgcolor: 'rgba(0,0,0,0.02)' }}>
                      <TableRow>
                        <TableCell sx={{ fontWeight: 'bold' }}>{t('hc_placeOfSupply')}</TableCell>
                        <TableCell align="right" sx={{ fontWeight: 'bold' }}>{t('hc_taxableValue')}</TableCell>
                        <TableCell align="right" sx={{ fontWeight: 'bold' }}>{t('igstCol')}</TableCell>
                      </TableRow>
                    </TableHead>
                    <TableBody>
                      {rows.map((r, i) => (
                        <TableRow key={i} hover>
                          <TableCell sx={{ fontWeight: 500 }}>{(r as any).pos}</TableCell>
                          <TableCell align="right">{formatCurrency((r as any).taxable)}</TableCell>
                          <TableCell align="right">{formatCurrency((r as any).igst)}</TableCell>
                        </TableRow>
                      ))}
                      <TableRow sx={{ bgcolor: 'rgba(0,0,0,0.02)', '& td': { fontWeight: 'bold', borderTop: '2px solid rgba(224, 224, 224, 1)' } }}>
                        <TableCell>{t('totalCol')}</TableCell>
                        <TableCell align="right">{formatCurrency(rows.reduce((s: number, r: any) => s + (r as any).taxable, 0))}</TableCell>
                        <TableCell align="right">{formatCurrency(rows.reduce((s: number, r: any) => s + (r as any).igst, 0))}</TableCell>
                      </TableRow>
                    </TableBody>
                  </Table>
                </TableContainer>
              </Paper>
            );
          })()}

          {/* Table 4 — ITC */}
          <Paper elevation={0} sx={{ borderRadius: 3, border: '1px solid', borderColor: 'divider', overflow: 'hidden' }}>
            <Box sx={{ p: 2, borderBottom: '1px solid', borderColor: 'divider', bgcolor: 'background.paper' }}>
              <Typography variant="h6" sx={{ fontWeight: "bold" ,  m: 0 }}>Table 4 — Eligible ITC (from Expenses & Purchases)</Typography>
            </Box>
            <TableContainer>
              <Table size="small">
                <TableHead sx={{ bgcolor: 'rgba(0,0,0,0.02)' }}>
                  <TableRow>
                    <TableCell sx={{ fontWeight: 'bold' }}>{t('hc_details')}</TableCell>
                    <TableCell align="right" sx={{ fontWeight: 'bold' }}>{t('igstCol')}</TableCell>
                    <TableCell align="right" sx={{ fontWeight: 'bold' }}>{t('cgstCol')}</TableCell>
                    <TableCell align="right" sx={{ fontWeight: 'bold' }}>{t('sgstCol')}</TableCell>
                  </TableRow>
                </TableHead>
                <TableBody>
                  <TableRow hover>
                    <TableCell sx={{ fontWeight: 500 }}>(A) ITC Available — All other ITC</TableCell>
                    <TableCell align="right">{formatCurrency(itcFromExpenses.igst)}</TableCell>
                    <TableCell align="right">{formatCurrency(itcFromExpenses.cgst)}</TableCell>
                    <TableCell align="right">{formatCurrency(itcFromExpenses.sgst)}</TableCell>
                  </TableRow>
                  <TableRow sx={{ fontWeight: 'bold' }}>
                    <TableCell sx={{ fontWeight: 'bold' }}>{t('hc_netItcAvailable')}</TableCell>
                    <TableCell align="right">{formatCurrency(itcFromExpenses.igst)}</TableCell>
                    <TableCell align="right">{formatCurrency(itcFromExpenses.cgst)}</TableCell>
                    <TableCell align="right">{formatCurrency(itcFromExpenses.sgst)}</TableCell>
                  </TableRow>
                </TableBody>
              </Table>
            </TableContainer>
            <Typography variant="caption" color="text.secondary" sx={{ display: 'block', p: 2 }}>
              ITC calculated from Expense Tracker and Purchase Bills entries with GST. Verify against GSTR-2B on the GST portal for actual eligible ITC.
            </Typography>
          </Paper>

          {/* Table 6 — Tax Payment */}
          <Paper elevation={0} sx={{ borderRadius: 3, border: '1px solid', borderColor: 'divider', overflow: 'hidden' }}>
            <Box sx={{ p: 2, borderBottom: '1px solid', borderColor: 'divider', bgcolor: 'background.paper' }}>
              <Typography variant="h6" sx={{ fontWeight: "bold" ,  m: 0 }}>{t('hc_table6TaxPaymentSummary')}</Typography>
            </Box>
            <TableContainer>
              <Table size="small">
                <TableHead sx={{ bgcolor: 'rgba(0,0,0,0.02)' }}>
                  <TableRow>
                    <TableCell sx={{ fontWeight: 'bold' }}>{t('descriptionCol')}</TableCell>
                    <TableCell align="right" sx={{ fontWeight: 'bold' }}>{t('igstCol')}</TableCell>
                    <TableCell align="right" sx={{ fontWeight: 'bold' }}>{t('cgstCol')}</TableCell>
                    <TableCell align="right" sx={{ fontWeight: 'bold' }}>{t('sgstCol')}</TableCell>
                    <TableCell align="right" sx={{ fontWeight: 'bold' }}>{t('totalCol')}</TableCell>
                  </TableRow>
                </TableHead>
                <TableBody>
                  <TableRow hover>
                    <TableCell sx={{ fontWeight: 500 }}>{t('hc_outputTaxLiability')}</TableCell>
                    <TableCell align="right">{formatCurrency(outputTax.igst)}</TableCell>
                    <TableCell align="right">{formatCurrency(outputTax.cgst)}</TableCell>
                    <TableCell align="right">{formatCurrency(outputTax.sgst)}</TableCell>
                    <TableCell align="right" sx={{ fontWeight: 'bold' }}>{formatCurrency(outputTax.igst + outputTax.cgst + outputTax.sgst)}</TableCell>
                  </TableRow>
                  <TableRow hover>
                    <TableCell sx={{ fontWeight: 500, color: 'success.main' }}>{t('hc_lessItcClaimed')}</TableCell>
                    <TableCell align="right" sx={{ color: 'success.main' }}>-{formatCurrency(itcFromExpenses.igst)}</TableCell>
                    <TableCell align="right" sx={{ color: 'success.main' }}>-{formatCurrency(itcFromExpenses.cgst)}</TableCell>
                    <TableCell align="right" sx={{ color: 'success.main' }}>-{formatCurrency(itcFromExpenses.sgst)}</TableCell>
                    <TableCell align="right" sx={{ color: 'success.main' }}>-{formatCurrency(itcFromExpenses.igst + itcFromExpenses.cgst + itcFromExpenses.sgst)}</TableCell>
                  </TableRow>
                  <TableRow sx={{ bgcolor: 'rgba(0,0,0,0.02)', '& td': { fontWeight: 'bold', borderTop: '2px solid rgba(224, 224, 224, 1)' } }}>
                    <TableCell>{t('hc_netTaxPayable')}</TableCell>
                    <TableCell align="right">{formatCurrency(netTax.igst)}</TableCell>
                    <TableCell align="right">{formatCurrency(netTax.cgst)}</TableCell>
                    <TableCell align="right">{formatCurrency(netTax.sgst)}</TableCell>
                    <TableCell align="right" sx={{ color: 'primary.main', fontSize: '1.1rem' }}>{formatCurrency(netTax.igst + netTax.cgst + netTax.sgst)}</TableCell>
                  </TableRow>
                </TableBody>
              </Table>
            </TableContainer>
          </Paper>

          {/* Net Payable */}
          <Paper elevation={0} sx={{ p: 3, textAlign: 'center', borderRadius: 3, border: '1px solid', borderColor: 'divider', bgcolor: 'background.paper' }}>
            <Typography variant="caption" color="text.secondary" sx={{ display: 'block', mb: 1 }}>{t('hc_netGstPayable')}</Typography>
            <Typography variant="h4" color="primary.main" sx={{ fontWeight: 800 ,  m: 0 }}>
              {formatCurrency(netPayable)}
            </Typography>
            <Typography variant="caption" color="text.secondary" sx={{ display: 'block', mt: 1 }}>
              {netPayable === 0 ? 'ITC covers your liability' : 'Pay via Electronic Cash Ledger'}
            </Typography>
          </Paper>
        </Stack>
      )}

      {/* ===================== GSTR-2B RECONCILIATION TAB ===================== */}
      {activeTab === 'gstr2b' && (() => {
        const reconRows = buildReconciliation(gstr2bData, purchases);
        const stats = reconRows.reduce((acc, r) => {
          acc.total += 1;
          acc[r.status] = (acc[r.status] || 0) + 1;
          return acc;
        }, { total: 0, matched: 0, amount_mismatch: 0, book_only: 0, twob_only: 0 });
        const visibleRows = gstr2bFilter === 'all' ? reconRows : reconRows.filter((r: any) => r.status === gstr2bFilter);

        const exportReconCSV = () => {
          if (reconRows.length === 0) { thagaval('Nothing to export', 'warning'); return; }
          downloadCSV('GSTR2B_Reconciliation.csv',
            ['Status', 'Supplier GSTIN', 'Supplier Name', 'Invoice No.', 'Invoice Date', '2B Value', 'Books Value', 'Diff', '2B Taxable', 'Books Taxable', '2B IGST', '2B CGST', '2B SGST', 'ITC Available'],
            reconRows.map((r: any) => [
              r.status, r.ctin, r.supplier, r.invoiceNumber, r.date,
              r.twoBVal.toFixed(2), r.bookVal.toFixed(2), (r.twoBVal - r.bookVal).toFixed(2),
              r.twoBTaxable.toFixed(2), r.bookTaxable.toFixed(2),
              r.twoBIgst.toFixed(2), r.twoBCgst.toFixed(2), r.twoBSgst.toFixed(2),
              r.itcAvailable ? 'Y' : 'N',
            ])
          );
          thagaval('Reconciliation CSV downloaded', 'success');
        };

        const STATUS_BADGES = {
          matched:         { label: '✓ Matched',         color: 'success.main' },
          amount_mismatch: { label: '⚠ Amount mismatch', color: 'warning.main' },
          book_only:       { label: '⚠ Books only',      color: 'error.main' },
          twob_only:       { label: '⚠ 2B only',         color: 'secondary.main' },
        };

        return (
          <Stack spacing={3}>
            {/* Help banner */}
            <Paper elevation={0} sx={{ p: 2, borderLeft: '4px solid', borderColor: 'primary.main', bgcolor: 'background.paper' }}>
              <Typography variant="body2" color="text.secondary">
                <strong>{t('hc_howToUse')}</strong> Download your GSTR-2B JSON from the GST portal
                (<Link href="https://services.gst.gov.in/services/auth/dashboard" target="_blank" rel="noopener noreferrer">services.gst.gov.in</Link>
                {' '}→ Returns → GSTR-2B → Download JSON), then click <strong>{t('hc_import2bJson')}</strong> below.
                We match each 2B entry against your <em>{t('hc_purchaseBills')}</em> by supplier GSTIN + invoice number, and flag mismatches so you can claim ITC accurately.
              </Typography>
            </Paper>

            {/* Actions */}
            <Stack direction="row"   spacing={1} sx={{ alignItems: 'center', flexWrap: 'wrap' }}>
              <input ref={gstr2bInputRef} type="file" accept=".json,application/json" onChange={handleImport2B} style={{ display: 'none' }} />
              <Button variant="contained" onClick={() => gstr2bInputRef.current?.click()} startIcon={<UploadSimple size={20} weight="fill" sx={{ fontSize: 14 }} />} sx={{ borderRadius: 5, textTransform: 'none' }}>{t('hc_import2bJson')}</Button>
              {gstr2bData && (
                <>
                  <Button variant="outlined" onClick={exportReconCSV} startIcon={<DownloadSimple size={20} weight="fill" sx={{ fontSize: 14 }} />} sx={{ borderRadius: 5, textTransform: 'none' }}>
                    Export reconciliation CSV
                  </Button>
                  <Button variant="outlined" color="error" onClick={() => { setGstr2bData(null); setGstr2bFilter('all'); }} sx={{ borderRadius: 5, textTransform: 'none' }}>
                    Clear
                  </Button>
                  <Typography variant="caption" color="text.secondary" sx={{ ml: 'auto' }}>
                    Imported for {gstr2bData.gstin || '?'} · period {gstr2bData.rtnprd || gstr2bData.fp || '?'}
                  </Typography>
                </>
              )}
            </Stack>

            {!gstr2bData && (
              <Paper elevation={0} sx={{ p: 4, textAlign: 'center', bgcolor: 'background.paper', borderRadius: 3, border: '1px solid', borderColor: 'divider' }}>
                <CheckCircle size={20} weight="fill" sx={{ fontSize: 48 }} htmlColor="var(--mui-palette-text-disabled)" style={{ marginBottom: 16 }} />
                <Typography variant="body1" color="text.secondary">{t('hc_importYourGstr2bJsonTo')}</Typography>
                {purchases.length === 0 && (
                  <Typography variant="body2" color="warning.main" sx={{ mt: 2 }}>
                    ⚠ You have no purchase bills recorded yet. Add some in the Purchases view first, otherwise everything will show as "2B only".
                  </Typography>
                )}
              </Paper>
            )}

            {gstr2bData && (
              <>
                {/* Summary stats */}
                <Paper elevation={0} sx={{ p: 2, bgcolor: 'background.paper', borderRadius: 3, border: '1px solid', borderColor: 'divider' }}>
                  <Box sx={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(140px, 1fr))', gap: 2 }}>
                    {[
                      { k: 'all', label: 'Total entries', count: stats.total, color: 'text.primary' },
                      { k: 'matched', label: '✓ Matched', count: stats.matched, color: 'success.main' },
                      { k: 'amount_mismatch', label: '⚠ Mismatched', count: stats.amount_mismatch, color: 'warning.main' },
                      { k: 'book_only', label: '⚠ Books only', count: stats.book_only, color: 'error.main' },
                      { k: 'twob_only', label: '⚠ 2B only', count: stats.twob_only, color: 'secondary.main' },
                    ].map(s => (
                      <Paper key={s.k} component={ButtonBase} onClick={() => setGstr2bFilter(s.k)} elevation={0} sx={{ p: 1.5, display: 'flex', flexDirection: 'column', alignItems: 'flex-start', border: '1px solid', borderColor: gstr2bFilter === s.k ? s.color : 'divider', bgcolor: gstr2bFilter === s.k ? `${s.color}10` : 'transparent', borderRadius: 2 }}>
                        <Typography variant="caption" color="text.secondary">{s.label}</Typography>
                        <Typography variant="h6" color={s.color} sx={{ fontWeight: "bold" }}>{s.count}</Typography>
                      </Paper>
                    ))}
                  </Box>
                </Paper>

                {/* Table */}
                <Paper elevation={0} sx={{ borderRadius: 3, border: '1px solid', borderColor: 'divider', overflow: 'hidden' }}>
                  <TableContainer>
                    <Table size="small">
                      <TableHead sx={{ bgcolor: 'rgba(0,0,0,0.02)' }}>
                        <TableRow>
                          <TableCell sx={{ fontWeight: 'bold' }}>{t('hc_status')}</TableCell>
                          <TableCell sx={{ fontWeight: 'bold' }}>{t('hc_supplier')}</TableCell>
                          <TableCell sx={{ fontWeight: 'bold' }}>{t('hc_invoiceNo1')}</TableCell>
                          <TableCell sx={{ fontWeight: 'bold' }}>Date</TableCell>
                          <TableCell align="right" sx={{ fontWeight: 'bold' }}>{t('hc_2bValue')}</TableCell>
                          <TableCell align="right" sx={{ fontWeight: 'bold' }}>{t('hc_booksValue')}</TableCell>
                          <TableCell align="right" sx={{ fontWeight: 'bold' }}>{t('hc_diff')}</TableCell>
                          <TableCell sx={{ fontWeight: 'bold' }}>ITC</TableCell>
                        </TableRow>
                      </TableHead>
                      <TableBody>
                        {visibleRows.length === 0 ? (
                          <TableRow>
                            <TableCell colSpan={8} align="center" sx={{ py: 3, color: 'text.secondary' }}>{t('hc_noEntriesMatchThisFilter')}</TableCell>
                          </TableRow>
                        ) : visibleRows.map((r, i) => {
                          const badge = STATUS_BADGES[r.status as keyof typeof STATUS_BADGES];
                          const diff = r.twoBVal - r.bookVal;
                          return (
                            <TableRow key={i} hover>
                              <TableCell><Chip size="small" label={badge.label} sx={{ bgcolor: `${badge.color}15`, color: badge.color, fontWeight: 'bold' }} /></TableCell>
                              <TableCell>
                                <Typography variant="body2" sx={{ fontWeight: 500 }}>{r.supplier || '—'}</Typography>
                                <Typography variant="caption" color="text.secondary" sx={{ fontFamily: 'monospace' }}>{r.ctin}</Typography>
                              </TableCell>
                              <TableCell sx={{ fontFamily: 'monospace' }}>{r.invoiceNumber}</TableCell>
                              <TableCell sx={{ color: 'text.secondary' }}>{r.date || '—'}</TableCell>
                              <TableCell align="right">{r.twoBVal > 0 ? formatCurrency(r.twoBVal) : '—'}</TableCell>
                              <TableCell align="right">{r.bookVal > 0 ? formatCurrency(r.bookVal) : '—'}</TableCell>
                              <TableCell align="right" sx={{ color: Math.abs(diff) > 1 ? 'error.main' : 'text.secondary', fontWeight: Math.abs(diff) > 1 ? 'bold' : 'normal' }}>
                                {diff !== 0 ? (diff > 0 ? '+' : '') + formatCurrency(diff) : '—'}
                              </TableCell>
                              <TableCell sx={{ color: r.itcAvailable ? 'success.main' : 'text.disabled' }}>{r.itcAvailable ? '✓' : '✗'}</TableCell>
                            </TableRow>
                          );
                        })}
                      </TableBody>
                    </Table>
                  </TableContainer>
                </Paper>
              </>
            )}
          </Stack>
        );
      })()}

      {/* ===================== TDS / TCS REPORT TAB ===================== */}
      {activeTab === 'tds' && (() => {
        // Aggregate TDS (deducted by buyers) and TCS (collected by us) across all bills
        // in the filtered period, grouped by client + section + quarter. The data feeds
        // Form 26Q (TDS quarterly return) and Form 27EQ (TCS quarterly return) inputs.
        const tdsRows: any[] = []; // each row: { client, gstin, section, quarter, taxable, tds }
        const tcsRows: any[] = [];
        filteredBills.forEach(bill => {
          const t = bill.data?.totals || {};
          const opt = bill.data?.invoiceOptions || {};
          const client = bill.data?.client || {};
          const date = new Date(bill.invoiceDate);
          if (isNaN(date.getTime())) return;
          const m = date.getMonth(); // 0=Jan
          const fyQuarter = m >= 3 && m <= 5 ? 'Q1' : m >= 6 && m <= 8 ? 'Q2' : m >= 9 && m <= 11 ? 'Q3' : 'Q4';
          const taxable = t.taxableAmount ?? ((t.subtotal || 0) - (t.totalDiscount || 0));
          if (t.tdsAmount > 0) {
            tdsRows.push({
              clientName: client.name || bill.clientName || '—',
              clientGstin: client.gstin || '',
              clientPan: client.pan || '',
              section: opt.tdsSection || '194Q',
              rate: opt.tdsRate || 0,
              quarter: fyQuarter,
              invoiceNumber: bill.invoiceNumber,
              date: bill.invoiceDate,
              taxable,
              tds: t.tdsAmount,
            });
          }
          if (t.tcsAmount > 0) {
            tcsRows.push({
              clientName: client.name || bill.clientName || '—',
              clientGstin: client.gstin || '',
              clientPan: client.pan || '',
              section: opt.tcsSection || '206C(1H)',
              rate: opt.tcsRate || 0,
              quarter: fyQuarter,
              invoiceNumber: bill.invoiceNumber,
              date: bill.invoiceDate,
              taxable,
              tcs: t.tcsAmount,
            });
          }
        });

        const tdsTotal = tdsRows.reduce((acc, r) => ({ taxable: acc.taxable + (r as any).taxable, tds: acc.tds + r.tds }), { taxable: 0, tds: 0 });
        const tcsTotal = tcsRows.reduce((acc, r) => ({ taxable: acc.taxable + (r as any).taxable, tcs: acc.tcs + r.tcs }), { taxable: 0, tcs: 0 });

        // Section-wise summary for TDS: groups by section + quarter
        const tdsBySection: Record<string, any> = {};
        tdsRows.forEach(r => {
          const k = `${r.section}_${r.quarter}`;
          if (!tdsBySection[k]) tdsBySection[k] = { section: r.section, quarter: r.quarter, count: 0, taxable: 0, tds: 0 };
          tdsBySection[k].count += 1; tdsBySection[k].taxable += (r as any).taxable; tdsBySection[k].tds += r.tds;
        });
        const tcsBySection: Record<string, any> = {};
        tcsRows.forEach(r => {
          const k = `${r.section}_${r.quarter}`;
          if (!tcsBySection[k]) tcsBySection[k] = { section: r.section, quarter: r.quarter, count: 0, taxable: 0, tcs: 0 };
          tcsBySection[k].count += 1; tcsBySection[k].taxable += (r as any).taxable; tcsBySection[k].tcs += r.tcs;
        });

        const exportTDSCSV = () => {
          if (tdsRows.length === 0) { thagaval('No TDS entries in this period', 'warning'); return; }
          downloadCSV('TDS_Receivable_Report.csv',
            ['Quarter', 'Section', 'Rate %', 'Invoice No.', 'Date', 'Client', 'Client GSTIN', 'Client PAN', 'Taxable Value', 'TDS Amount'],
            tdsRows.map((r: any) => [r.quarter, r.section, r.rate, r.invoiceNumber, r.date, r.clientName, r.clientGstin, r.clientPan, (r as any).taxable.toFixed(2), r.tds.toFixed(2)])
          );
          thagaval('TDS report exported', 'success');
        };

        const exportTCSCSV = () => {
          if (tcsRows.length === 0) { thagaval('No TCS entries in this period', 'warning'); return; }
          downloadCSV('TCS_Collected_Report.csv',
            ['Quarter', 'Section', 'Rate %', 'Invoice No.', 'Date', 'Client', 'Client GSTIN', 'Client PAN', 'Taxable Value', 'TCS Amount'],
            tcsRows.map((r: any) => [r.quarter, r.section, r.rate, r.invoiceNumber, r.date, r.clientName, r.clientGstin, r.clientPan, (r as any).taxable.toFixed(2), r.tcs.toFixed(2)])
          );
          thagaval('TCS report exported', 'success');
        };

        return (
          <Stack spacing={3}>
            <Paper elevation={0} sx={{ p: 2, borderLeft: '4px solid', borderColor: 'primary.main', bgcolor: 'background.paper' }}>
              <Typography variant="body2" color="text.secondary">
                <strong>{t('hc_whatThisIs')}</strong> Aggregates TDS (tax deducted by your clients on payments to you — Section 194C/194J/194Q etc.)
                and TCS (tax collected by you from clients — Section 206C(1H)/52 etc.) across the selected period.
                Use the CSV exports as input for <strong>{t('hc_form26q')}</strong> (TDS) and <strong>{t('hc_form27eq')}</strong> (TCS) quarterly returns,
                or hand the file to your CA. Filed at <Link href="https://www.tin-nsdl.com" target="_blank" rel="noopener noreferrer">tin-nsdl.com</Link>.
              </Typography>
            </Paper>

            {/* TDS section */}
            <Paper elevation={0} sx={{ p: 3, borderRadius: 3, border: '1px solid', borderColor: 'divider', bgcolor: 'background.paper' }}>
              <Stack direction="row" spacing={2} sx={{ mb: 3, justifyContent: 'space-between', alignItems: 'flex-start', flexWrap: 'wrap' }}>
                <Box>
                  <Typography variant="h6" sx={{ fontWeight: "bold" ,  m: 0 }}>TDS Receivable (deducted by clients)</Typography>
                  <Typography variant="body2" color="text.secondary">
                    Total TDS your clients should have deducted. You can claim this as credit against your own income tax.
                  </Typography>
                </Box>
                <Button variant="outlined" onClick={exportTDSCSV} disabled={tdsRows.length === 0} startIcon={<DownloadSimple size={20} weight="fill" sx={{ fontSize: 14 }} />} sx={{ borderRadius: 5, textTransform: 'none' }}>
                  Export TDS CSV
                </Button>
              </Stack>
              <Box sx={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(160px, 1fr))', gap: 2, mb: 3 }}>
                <Paper elevation={0} sx={{ p: 2, bgcolor: 'background.default', borderRadius: 2 }}>
                  <Typography variant="caption" color="text.secondary">{t('hc_invoicesWithTds')}</Typography>
                  <Typography variant="h6" sx={{ fontWeight: "bold" }}>{tdsRows.length}</Typography>
                </Paper>
                <Paper elevation={0} sx={{ p: 2, bgcolor: 'background.default', borderRadius: 2 }}>
                  <Typography variant="caption" color="text.secondary">Taxable value</Typography>
                  <Typography variant="h6" sx={{ fontWeight: "bold" }}>{formatCurrency(tdsTotal.taxable)}</Typography>
                </Paper>
                <Paper elevation={0} sx={{ p: 2, bgcolor: 'background.default', borderRadius: 2 }}>
                  <Typography variant="caption" color="text.secondary">{t('hc_totalTdsReceivable')}</Typography>
                  <Typography variant="h6" color="success.main" sx={{ fontWeight: "bold" }}>{formatCurrency(tdsTotal.tds)}</Typography>
                </Paper>
              </Box>
              {Object.values(tdsBySection).length > 0 ? (
                <TableContainer>
                  <Table size="small">
                    <TableHead sx={{ bgcolor: 'rgba(0,0,0,0.02)' }}>
                      <TableRow>
                        <TableCell sx={{ fontWeight: 'bold' }}>{t('hc_quarter')}</TableCell>
                        <TableCell sx={{ fontWeight: 'bold' }}>{t('hc_section')}</TableCell>
                        <TableCell sx={{ fontWeight: 'bold' }}>Invoices</TableCell>
                        <TableCell align="right" sx={{ fontWeight: 'bold' }}>{t('taxableLabel')}</TableCell>
                        <TableCell align="right" sx={{ fontWeight: 'bold' }}>TDS</TableCell>
                      </TableRow>
                    </TableHead>
                    <TableBody>
                      {Object.values(tdsBySection).sort((a: any, b: any) => a.quarter.localeCompare(b.quarter) || a.section.localeCompare(b.section)).map((r: any, i) => (
                        <TableRow key={`tds-${i}`} hover>
                          <TableCell>{r.quarter}</TableCell>
                          <TableCell sx={{ fontFamily: 'monospace' }}>{r.section}</TableCell>
                          <TableCell>{r.count}</TableCell>
                          <TableCell align="right">{formatCurrency((r as any).taxable)}</TableCell>
                          <TableCell align="right" sx={{ color: 'success.main', fontWeight: 'bold' }}>{formatCurrency(r.tds)}</TableCell>
                        </TableRow>
                      ))}
                    </TableBody>
                  </Table>
                </TableContainer>
              ) : (
                <Typography variant="body2" color="text.secondary" align="center" sx={{ py: 2 }}>
                  No invoices with TDS in this period. Enable TDS on an invoice via <em>{t('hc_customizeTds')}</em>.
                </Typography>
              )}
            </Paper>

            {/* TCS section */}
            <Paper elevation={0} sx={{ p: 3, borderRadius: 3, border: '1px solid', borderColor: 'divider', bgcolor: 'background.paper' }}>
              <Stack direction="row" spacing={2} sx={{ mb: 3, justifyContent: 'space-between', alignItems: 'flex-start', flexWrap: 'wrap' }}>
                <Box>
                  <Typography variant="h6" sx={{ fontWeight: "bold" ,  m: 0 }}>TCS Collected (from clients)</Typography>
                  <Typography variant="body2" color="text.secondary">
                    TCS you collected from buyers. Must be deposited to the Income Tax Department and reported in Form 27EQ quarterly.
                  </Typography>
                </Box>
                <Button variant="outlined" onClick={exportTCSCSV} disabled={tcsRows.length === 0} startIcon={<DownloadSimple size={20} weight="fill" sx={{ fontSize: 14 }} />} sx={{ borderRadius: 5, textTransform: 'none' }}>
                  Export TCS CSV
                </Button>
              </Stack>
              <Box sx={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(160px, 1fr))', gap: 2, mb: 3 }}>
                <Paper elevation={0} sx={{ p: 2, bgcolor: 'background.default', borderRadius: 2 }}>
                  <Typography variant="caption" color="text.secondary">{t('hc_invoicesWithTcs')}</Typography>
                  <Typography variant="h6" sx={{ fontWeight: "bold" }}>{tcsRows.length}</Typography>
                </Paper>
                <Paper elevation={0} sx={{ p: 2, bgcolor: 'background.default', borderRadius: 2 }}>
                  <Typography variant="caption" color="text.secondary">Taxable value</Typography>
                  <Typography variant="h6" sx={{ fontWeight: "bold" }}>{formatCurrency(tcsTotal.taxable)}</Typography>
                </Paper>
                <Paper elevation={0} sx={{ p: 2, bgcolor: 'background.default', borderRadius: 2 }}>
                  <Typography variant="caption" color="text.secondary">{t('hc_totalTcsCollected')}</Typography>
                  <Typography variant="h6" color="warning.main" sx={{ fontWeight: "bold" }}>{formatCurrency(tcsTotal.tcs)}</Typography>
                </Paper>
              </Box>
              {Object.values(tcsBySection).length > 0 ? (
                <TableContainer>
                  <Table size="small">
                    <TableHead sx={{ bgcolor: 'rgba(0,0,0,0.02)' }}>
                      <TableRow>
                        <TableCell sx={{ fontWeight: 'bold' }}>{t('hc_quarter')}</TableCell>
                        <TableCell sx={{ fontWeight: 'bold' }}>{t('hc_section')}</TableCell>
                        <TableCell sx={{ fontWeight: 'bold' }}>Invoices</TableCell>
                        <TableCell align="right" sx={{ fontWeight: 'bold' }}>{t('taxableLabel')}</TableCell>
                        <TableCell align="right" sx={{ fontWeight: 'bold' }}>TCS</TableCell>
                      </TableRow>
                    </TableHead>
                    <TableBody>
                      {Object.values(tcsBySection).sort((a: any, b: any) => a.quarter.localeCompare(b.quarter) || a.section.localeCompare(b.section)).map((r: any, i) => (
                        <TableRow key={`tcs-${i}`} hover>
                          <TableCell>{r.quarter}</TableCell>
                          <TableCell sx={{ fontFamily: 'monospace' }}>{r.section}</TableCell>
                          <TableCell>{r.count}</TableCell>
                          <TableCell align="right">{formatCurrency((r as any).taxable)}</TableCell>
                          <TableCell align="right" sx={{ color: 'warning.main', fontWeight: 'bold' }}>{formatCurrency(r.tcs)}</TableCell>
                        </TableRow>
                      ))}
                    </TableBody>
                  </Table>
                </TableContainer>
              ) : (
                <Typography variant="body2" color="text.secondary" align="center" sx={{ py: 2 }}>
                  No invoices with TCS in this period. Enable TCS on an invoice via <em>{t('hc_customizeTcs')}</em>.
                </Typography>
              )}
            </Paper>
          </Stack>
        );
      })()}

      {/* ===================== FILING GUIDE TAB ===================== */}
      {activeTab === 'guide' && (
        <Stack spacing={3}>
          {/* Quick Start */}
          <Paper elevation={0} sx={{ p: 2, borderLeft: '4px solid', borderColor: 'primary.main', bgcolor: 'background.paper' }}>
            <Typography variant="body2" color="text.secondary">
              <strong>{t('hc_steps')}</strong> Review GSTR-1 & 3B tabs → Export JSON → Upload to gst.gov.in → File GSTR-1 first, then GSTR-3B.
              <Typography component="span" variant="caption" sx={{ color: 'text.disabled', ml: 1 }}>{t('hc_dueR1By11th3b')}</Typography>
            </Typography>
          </Paper>

          {/* Tab selector within guide */}
          <Box sx={{ display: 'flex', gap: 1, flexWrap: 'wrap', mb: 1 }}>
            {[
              { id: 'regular', label: 'Regular Filing (With Sales)' },
              { id: 'nil', label: 'NIL Return (No Sales)' },
              { id: 'errors', label: 'Common Errors & Fixes' },
            ].map(tab => (
              <Button key={tab.id} variant={guideTab === tab.id ? 'contained' : 'outlined'} onClick={() => setGuideTab(tab.id as any)} sx={{ borderRadius: 5, textTransform: 'none', boxShadow: 'none' }}>
                {tab.label}
              </Button>
            ))}
          </Box>

          {guideTab === 'regular' && (
            <Stack spacing={3}>
              <StepList steps={GSTR1_STEPS} title="GSTR-1 — Sales Return (File This First)" />

              <Paper elevation={0} sx={{ p: 3, bgcolor: '#f0fdf4', borderRadius: 3 }}>
                <Typography variant="subtitle2" color="#059669" sx={{ fontWeight: "bold" ,  mb: 1 }}>{t('hc_gstr1ProTips')}</Typography>
                <Box component="ul" sx={{ m: 0, pl: 2, typography: 'body2', color: '#047857', '& li': { mb: 0.5 } }}>
                  <li><strong>{t('hc_fastestMethod')}</strong>{t('hc_exportGstr1JsonFromThe')}</li>
                  <li>If turnover {'<'} ₹5 Cr, opt for QRMP scheme — file quarterly instead of monthly. Apply via Services → User Services → Opt-in for QRMP.</li>
                  <li>Amendments to previous period invoices: Use Table 9A (not 4A). You can amend within the September return of the following FY.</li>
                  <li>Export invoices (zero-rated): Report in Table 6A with shipping bill details.</li>
                  <li>Advances received: Report in Table 11A (tax on advance received) — adjust when invoice is issued (Table 11B).</li>
                </Box>
              </Paper>

              <StepList steps={GSTR3B_STEPS} title="GSTR-3B — Summary Return + Tax Payment (File After GSTR-1)" />

              <Paper elevation={0} sx={{ p: 3, bgcolor: '#eff6ff', borderRadius: 3 }}>
                <Typography variant="subtitle2" color="primary.main" sx={{ fontWeight: "bold" ,  mb: 1 }}>{t('hc_gstr3bProTips')}</Typography>
                <Box component="ul" sx={{ m: 0, pl: 2, typography: 'body2', color: '#1e40af', '& li': { mb: 0.5 } }}>
                  <li><strong>{t('hc_fromJuly2025')}</strong>{t('hc_table3AutopopulatesFromGstr1')}</li>
                  <li><strong>{t('hc_itcMatching')}</strong>{t('hc_alwaysCheckGstr2bStatementBefore')}</li>
                  <li><strong>ITC utilization order (Section 49):</strong> IGST credit first (against IGST → CGST → SGST), then CGST (against CGST → IGST), then SGST (against SGST → IGST).</li>
                  <li><strong>{t('hc_payment')}</strong> Use Electronic Credit Ledger (ITC) first. Pay remaining via Electronic Cash Ledger. Create challan via Services → Payments → Create Challan.</li>
                  <li><strong>{t('hc_interestCalculation')}</strong> If you file late, interest is 18% p.a. calculated on tax payable (not total liability). Interest starts from day after due date.</li>
                  <li><strong>{t('hc_reverseCharge')}</strong> If you paid RCM (restaurant/legal/GTA services), report in 3.1(d) AND claim ITC in Table 4(A)(3).</li>
                </Box>
              </Paper>
            </Stack>
          )}

          {guideTab === 'nil' && (
            <Stack spacing={3}>
              <Paper elevation={0} sx={{ p: 3, bgcolor: '#fffbeb', borderLeft: '4px solid #f59e0b', borderRadius: 3 }}>
                <Typography variant="subtitle2" color="#92400e" sx={{ fontWeight: "bold" ,  mb: 1 }}>{t('hc_whenToFileNilReturn')}</Typography>
                <Box component="ul" sx={{ m: 0, pl: 2, typography: 'body2', color: '#a16207', '& li': { mb: 0.5 } }}>
                  <li>{t('hc_youHad')}<strong>{t('hc_zeroOutwardSupplies')}</strong> (no sales/services) during the period</li>
                  <li>{t('hc_youHave')}<strong>{t('hc_noInputTaxCredit')}</strong>{t('hc_toClaim')}</li>
                  <li>{t('hc_youHave')}<strong>{t('hc_noTaxLiability')}</strong> (including reverse charge)</li>
                  <li>{t('hc_youHave')}<strong>{t('hc_noInwardSupplies')}</strong>{t('hc_liableToReverseCharge')}</li>
                  <li>{t('hc_ifAnyOfTheAbove')}</li>
                </Box>
                <Typography variant="body2" color="#92400e" sx={{ mt: 2, fontWeight: "bold" }}>
                  MANDATORY: You must file NIL returns every month/quarter even with zero activity. Non-filing for 6 continuous months can result in suo-motu GSTIN cancellation under Section 29(2)(c).
                </Typography>
              </Paper>

              <StepList steps={NIL_GSTR1_STEPS} title="NIL GSTR-1 — File First (Even with Zero Sales)" />
              <StepList steps={NIL_GSTR3B_STEPS} title={t('hc_nilGstr3bFileAfterNil')} />

              <Paper elevation={0} sx={{ p: 3, bgcolor: '#f0fdf4', borderRadius: 3 }}>
                <Typography variant="subtitle2" color="#059669" sx={{ fontWeight: "bold" ,  mb: 1 }}>{t('hc_nilReturnQuickSummary')}</Typography>
                <Box component="ul" sx={{ m: 0, pl: 2, typography: 'body2', color: '#047857', '& li': { mb: 0.5 } }}>
                  <li>{t('hc_nilGstr1AndNilGstr3b')}<strong>{t('hc_separateReturns')}</strong>{t('hc_fileBoth')}</li>
                  <li>{t('hc_nilFilingTakes23Minutes')}</li>
                  <li>Late fee for NIL: ₹20/day (₹10 CGST + ₹10 SGST), capped at ₹500 per return</li>
                  <li>{t('hc_youCanFileNilReturns')}<code>{t('hc_nilSpaceGstinSpaceReturn')}</code>{t('hc_to14409VerifyWithOtp')}</li>
                  <li>{t('hc_qrmpUsersFilingQuarterlyNil')}</li>
                  <li>Even if you had no sales but had purchases with GST → file REGULAR return (not NIL) to claim ITC</li>
                </Box>
              </Paper>
            </Stack>
          )}

          {guideTab === 'errors' && (
            <Stack spacing={3}>
              <Paper elevation={0} sx={{ borderRadius: 3, border: '1px solid', borderColor: 'divider', overflow: 'hidden', bgcolor: 'background.paper' }}>
                <Box sx={{ p: 2, borderBottom: '1px solid', borderColor: 'divider', bgcolor: 'background.paper' }}>
                  <Typography variant="h6" sx={{ fontWeight: "bold" ,  m: 0 }}>Common GST Portal Errors & How to Fix Them</Typography>
                </Box>
                <Box sx={{ p: 3 }}>
                  {[
                    { error: '"Invalid GSTIN" when adding B2B invoice', fix: 'Verify the client GSTIN on the portal: Services → User Services → Search Taxpayer. The GSTIN must be active. Cancelled/surrendered GSTINs are rejected. Also check for typos — GSTIN is 15 characters: 2 digits (maanilam) + 10 chars (PAN) + 1 entity code + 1 check digit.' },
                    { error: '"Invoice number already exists for this recipient"', fix: 'Each invoice number must be unique per GSTIN per period. If you\'re re-filing after amendment, use Table 9A for amendments, not Table 4A. If duplicate, check if invoice was already reported in a previous period.' },
                    { error: '"Place of Supply mismatch" or wrong tax type', fix: 'If supply is INTER-maanilam (different states), only IGST applies. If INTRA-maanilam (same maanilam), only CGST+SGST. POS must match the buyer\'s maanilam for inter-maanilam. Common mistake: Delhi business billing Delhi client but selecting different POS.' },
                    { error: '"Invoice date is not within the return period"', fix: 'Invoice date must fall within the filing period. E.g., for March 2026 return, dates must be 01/03/2026 to 31/03/2026. If you missed an invoice, report in the current period — it\'s allowed but must be before September of next FY.' },
                    { error: '"HSN code is invalid" in Table 12', fix: 'Use valid HSN codes from the official HSN Master (downloadable from cbic.gov.in). Services use SAC codes starting with 99. Common: 998314 (IT services), 9954 (construction), 9983 (professional services). The portal validates against the master list.' },
                    { error: '"GSTR-3B cannot be filed — GSTR-1 not filed"', fix: 'You MUST file GSTR-1 before GSTR-3B for the same period. Go back and file GSTR-1 first. This is a hard block — no workaround.' },
                    { error: '"ITC claimed exceeds GSTR-2B available ITC"', fix: 'You cannot claim more ITC than what\'s in your auto-populated GSTR-2B statement. Check Returns → GSTR-2B to see eligible ITC. If a supplier hasn\'t filed their GSTR-1, their invoice won\'t appear in your GSTR-2B and you can\'t claim that ITC yet.' },
                    { error: '"Previous period return not filed"', fix: 'GST returns must be filed sequentially. You cannot file March return if February is pending. File all pending returns in order starting from the earliest unfiled period.' },
                    { error: '"Taxable value and tax amount mismatch"', fix: 'The portal validates that tax = taxable value × rate. E.g., if taxable value is ₹10,000 at 18%, IGST must be ₹1,800 (or CGST ₹900 + SGST ₹900). Rounding differences up to ₹1 are allowed.' },
                    { error: '"EVC generation failed" or "OTP not received"', fix: 'Try after 5 minutes. Check registered mobile number is correct (Profile → Update). For companies, EVC is not available — use DSC only. If DSC fails, check USB token is inserted and emsigner utility is running.' },
                    { error: '"Challan amount does not match liability"', fix: 'Create challan AFTER submitting GSTR-3B, not before. The challan amount must match the "Tax payable in cash" column. If you overpaid, excess stays in Electronic Cash Ledger for future use or refund.' },
                  ].map((item, i, arr) => (
                    <Box key={i} sx={{ mb: i < arr.length - 1 ? 2 : 0, pb: i < arr.length - 1 ? 2 : 0, borderBottom: i < arr.length - 1 ? '1px solid' : 'none', borderColor: 'divider' }}>
                      <Typography variant="subtitle2" color="error.main" sx={{ fontWeight: "bold" ,  mb: 0.5 }}>Error: {item.error}</Typography>
                      <Typography variant="body2" color="text.secondary">Fix: {item.fix}</Typography>
                    </Box>
                  ))}
                </Box>
              </Paper>

              <Paper elevation={0} sx={{ p: 3, bgcolor: '#f8fafc', borderRadius: 3 }}>
                <Typography variant="subtitle2" sx={{ fontWeight: "bold" ,  mb: 1 }}>{t('hc_keyGstRulesToRemember')}</Typography>
                <Box component="ul" sx={{ m: 0, pl: 2, typography: 'body2', color: 'text.secondary', '& li': { mb: 0.5 } }}>
                  <li><strong>Section 16(4):</strong>{t('hc_itcForAnyInvoiceMust')}</li>
                  <li><strong>{t('hc_section34')}</strong>{t('hc_creditNotesMustBeIssued')}</li>
                  <li><strong>{t('hc_section31')}</strong>{t('hc_taxInvoiceMustBeIssued')}</li>
                  <li><strong>{t('hc_section49')}</strong> ITC utilization order is mandatory: IGST first (against IGST→CGST→SGST), then CGST (→CGST→IGST), then SGST (→SGST→IGST). Cross-utilization of CGST↔SGST is NOT allowed.</li>
                  <li><strong>Rule 36(4):</strong>{t('hc_itcCanOnlyBeClaimed')}</li>
                  <li><strong>{t('hc_section50')}</strong> Interest on late payment is 18% p.a. on NET tax payable (after ITC). Calculated from the day after due date to date of payment.</li>
                  <li><strong>{t('hc_section7374')}</strong> Tax department can issue notice for short payment within 3 years (73) or 5 years for fraud (74). Maintain all records for at least 6 years.</li>
                  <li><strong>Section 29(2)(c):</strong> GSTIN cancellation if returns not filed for 6+ continuous months (quarterly filers: 2 consecutive quarters).</li>
                  <li><strong>{t('hc_ewayBill')}</strong>{t('hc_cannotGenerateEwayBillsIf')}</li>
                </Box>
              </Paper>
            </Stack>
          )}
        </Stack>
      )}

      <Box sx={{ mt: 6, mb: 2, textAlign: 'center' }}>
        <Typography variant="caption" sx={{ opacity: 0.15, userSelect: 'none', transition: 'opacity 0.2s', '@media (hover: hover)': { '&:hover': { opacity: 0.5 } } }}>
          <a href="https://services.gst.gov.in/services/login" target="_blank" rel="noopener noreferrer" style={{ color: 'inherit', textDecoration: 'none' }}>{t('gstPortalBtn')}</a>
        </Typography>
      </Box>
    </Box>
  );
}
