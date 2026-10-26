# React Print View — Exact Reference

What the React app does when it shows, prints, saves or shares a document.
KMP must reproduce **every** item below to look 100% the same.
Source of truth: `react/moolam/pagudhigal/**`. Nothing here is a suggestion — it is what React does today.

---

## 1. The four templates (each one is different)

| Document | View component | Paper element | Styles |
|---|---|---|---|
| GST / Silk invoice | `GstBill/InvoiceView.tsx` → `GstBill/SjsTheme.tsx` | `#invoice-preview.invoice-preview-container` | `vadivam.css` + inline `<style>` inside SjsTheme |
| GST / Silk receipt | `GstBill/Receipts/ReceiptView.tsx` | `.receipt-box` inside `.print-area` | inline `<style>` + `CoolieBill/print.css` |
| Coolie invoice | `CoolieBill/CoolieInvoiceView.tsx` | `.a4-paper.font-tamil` | `CoolieBill/print.css` |
| Coolie receipt | `CoolieBill/CoolieReceiptView.tsx` | `.receipt-box` inside `.print-area` | inline `<style>` + `CoolieBill/print.css` |

- `InvoicePreview.tsx` is only a wrapper that renders `SjsTheme`, so every GST invoice uses SjsTheme (`profile.invoiceTheme` does not change the look).
- `options.pdfStyle` is read in SjsTheme (line 216) but **unused** — only the classic header is drawn.
- All four views are imported eagerly in `Seyali.tsx`, so `vadivam.css` **and** `print.css` are global for every document (print.css `@media print` rules also hit `#invoice-preview`).

---

## 2. A4 lock — paper size

| Template | Width | Height | Inner padding | Overflow |
|---|---|---|---|---|
| SjsTheme | `210mm` (`.invoice-preview-container`) | `min-height: 297mm` | header `5mm` top, sections `1.5rem` sides | hidden |
| Coolie invoice | `210mm` (`.a4-paper`) | `min-height: 297mm` | `8mm 12mm` | — |
| Receipts (both) | `210mm` | **fixed** `height: 297mm; max-height: 297mm` | `.receipt-content { padding: 8mm 12mm }` | hidden |

Base fonts: SjsTheme `font-size: 12.5px; line-height: 1.45; color: #1a1a2e`, family `'Elvan Sans'` (woff, weights 400/500/600/700 + italics, `font-display: block`).

---

## 3. Screen preview — the "view modes"

All four views use the **same** frame (MUI `Paper`, class `invoice-paper print-wrapper`, `width/minWidth: 210mm`, `overflow: hidden`, `m: 0 auto`, `bgcolor: white`).

### Phone (`theme.breakpoints.down('sm')` → width < 600px)
- `initialScale = min((window.innerWidth − 32) / 793.7, 1)` (793.7px = 210mm)
- Paper: `transform: scale(initialScale)`, `transform-origin: top left`, `zoom: none`, `margin-bottom: −((1 − initialScale) × 141)%`, `border-radius: 12px`, `elevation 8`
- Wrapper: `padding: 0 16px`, `display:flex; justify-content:center`, `overflow: hidden`, `touch-action: none`
- **Pinch zoom** (`hooks/usePinchZoom.ts`): min 1×, max 4×, rubber-band below 1× at 80% speed, one-finger pan only when zoomed, applied as `translate3d(x,y,0) scale(s)` on the inner content div.

### Tablet / desktop (≥ 600px)
- `zoom` by breakpoint: **xs 0.43 · sm 0.7 · md 0.85 · lg 1** (`elevation 3`)
- Browsers without `zoom`: same values as `transform: scale(..)` with `margin-bottom` **xs −55% · sm −25% · md −10% · lg 0**

### Page around the paper
`Box.print-wrapper`: `py: xs 1.5 / md 4`, `px: xs 0 / md 4`, `maxWidth: 1200`, `bgcolor: background.default` (dark in dark mode — the paper stays white).
Dark mode never recolours the document (`vadivam.css` 180-218 force `.invoice-preview-container` light).

---

## 4. Print (system print dialog)

`handlePrint` in each view builds a **fresh HTML document** — not the app page:

```
<head>  document.head (all app CSS)  +  <style> @media print rules </style>  </head>
<body style="background:white;margin:0;padding:0">  printRef.current.outerHTML  </body>
```

Inline print rules (InvoiceView 214-226, same in the other views):
- `@page { size: A4; margin: 0; }`, `html { font-size: 16px !important }`
- `.invoice-paper, .a4-paper` → no shadow, no border, no margin, `width: 100%`
- `.no-print { display: none }`, `#invoice-preview { padding: 0 }`
- SjsTheme section gutters forced to `1.5rem` (header also `padding-top: 1rem`)

Global `print.css @media print` (757-811) also applies:
- `html, body { width: 210mm; height: 297mm; overflow: hidden }` → **exactly one A4 page**
- `.a4-paper, #invoice-preview { width:100%; height:297mm; padding: 8mm 12mm; transform:none; border-radius:0 }`
- `.print-wrapper` stripped (no zoom/transform/padding/shadow)
- `-webkit-print-color-adjust: exact` (colour backgrounds print)
- `.bill-footer-new { margin-top: auto }` (Coolie footer pinned to page bottom)
- Receipts add `.receipt-box { height: 297mm; border: none; width: 100% }`

Android (Capacitor): `NativeDocumentPlugin.printHtml` loads that HTML with `loadDataWithBaseURL("file:///android_asset/public/", …)` into a new WebView, and on `onPageFinished` calls `printManager.print(filename, createPrintDocumentAdapter(filename), PrintAttributes.Builder().build())` — **default attributes**; A4 and zero margin come from the CSS `@page` rule.
Web: same HTML written into a hidden iframe, `print()` after 500 ms.
Job name: `<prefix>_<invoiceNumber with / → ->` (prefix from `INVOICE_TYPES`: `INV`, `EST`, …).

---

## 5. PDF (Download / Share) — `buildPDF`

Identical pattern in all four views:
1. Temporarily set paper `transform: none; zoom: 1` (undo the screen scaling).
2. `jsPDF({ orientation:'portrait', unit:'mm', format:'a4', compress:true })`.
3. `html2canvas(printRef, { scale: max(3, round(devicePixelRatio × 2)), useCORS, letterRendering, backgroundColor:'#ffffff', imageTimeout:0, width/height = scrollWidth/scrollHeight })`.
4. `onclone`: every node `letterSpacing: 0; wordSpacing: 0`; paper forced `width 210mm`, `overflow visible`, no border / shadow / radius
   (SjsTheme & Coolie invoice: `minHeight 297mm`; receipts: `maxWidth 210mm`, `minHeight unset`).
5. JPEG quality **0.95**, `addImage(..., 'MEDIUM')`; taller than A4 (+2 mm tolerance) → sliced across pages.
6. `[data-pdf-page]` blocks rendered as separate extra pages.
7. Restore transform/zoom.

Native: `NativeDocument.downloadPdf` (folder `Udukkai Silk/Invoice/`) and `NativeDocument.sharePdf`. Share text: `*Invoice: <no>*\nClient: <name>\nAmount: <amount>\nDate: <date>`.

---

## 6. Settings that change the document

### Global invoice display options (`meta.json → invoiceDisplayOptions`, merged under per-bill `invoiceOptions`)
`showGST, showState, showDistrict, showCountry, showGSTIN, showPlaceOfSupply, showHSN, showDiscount, showBankDetails, showUPI, showLogo, showSignature, showTerms, showNotes, showAmountWords, showItemQty, showRoundOff, showCess, showItemizedTax, showDueDate, showniruvanathinPeyar, showBusinessAddress, showAccountLabel, selectedAccountId, customTitle, currency, exchangeRate, reverseCharge, showTDS/tdsSection/tdsRate, showTCS/tcsSection/tcsRate, accentColor, pdfStyle, invoiceMode`.
Every `show*` defaults to **ON** when missing; `showItemizedTax` defaults **OFF**.

Current React values: `showItemizedTax: true`, `showRoundOff: false`, `pdfStyle: 'minimal'`, `accentColor: '#0369a1'`, `showDueDate: true`.

### Per-bill options (`bill.data.invoiceOptions`)
`taxInclusive, showDiscountColumn, showHSN, showPlaceOfSupply, showVehicle, showDueDate, showTerms, showBank, showNotes, showSignature, measureMode`.
`measureMode: 'weight'` → column title **எடை / Weight**, values suffixed **" Kg"**, "Total Items" line hidden. (SJPS-29 and SJPS-31 use weight.)

### Profile (company) fields that change the look
- `themeColor` → accent for headings, table header (`accent + 1A`), even rows (`accent + 14`), totals. Default `#388e3c`. Coolie views turn it into CSS variables via `getThemeStyles`; receipts default `#1e3a8a`.
- `logo` + `logoHeight` (default 120, max width 160px); `wideLogo` + `wideLogoX/Y/Scale`; `billHeaderStyle` (`'wide'`, or wide when unset and a wide logo exists).
- `signature` image (max height 95px, positioned right −10px), `authorizedSignatoryName`.
- Bank: `vangiPeyar/En`, `bankBranch/En`, `kanakkuEn`, `ifsc`, `swift`, `pan`, `paymentAccounts[]`.
- **UPI QR** only when `showUPI` **and** an account/profile `upiId` exists **and** total > 0 **and** currency INR. QR = `upi://pay?pa=…&pn=<name>&am=<total>&cu=INR&tn=Payment for <no>`, 120px, margin 1, level M.
- Bilingual: `enableBilingual`, `primaryDataLanguage`, `secondaryDataLanguage`; receipts also `receiptLanguage`.

### UI language (`t()` from `LanguageContext`, `localStorage.elvanniril_language`)
Only a few labels (e.g. "This is not a tax invoice", "Against invoice"). Everything else follows the profile data languages.

---

## 7. KMP status

| # | Item | Status |
|---|---|---|
| 1 | Print = fresh paper-only document (React `handlePrint` HTML + rules per template) | ✅ `UdukkaiPrint.printHtml()` + Android `printHtml` (new WebView, `loadDataWithBaseURL`) |
| 2 | Default `PrintAttributes` (A4 / margins from `@page`) | ✅ |
| 3 | Global display options per company | ✅ stored in `BusinessProfile.printSettingsJson` (`invoiceDisplayOptions`), merged `{...global, ...bill}` like React |
| 4 | Per-bill `measureMode` | ✅ `InvoiceDisplayOptions.measureMode` (seeded "weight" for Kg bills). No editor toggle yet |
| 5 | Profile images / logo position / colours / bank / UPI | ✅ sample data matches React (`kmp/tools/export_react_seed.py` → `composeResources/files/seed`). Existing installs must re-seed |
| 6 | Screen preview | Receipt & Coolie views run React's own frame (incl. pinch zoom). GST invoice uses React's phone frame; no pinch zoom yet |
| 7 | Blank preview on phone | ✅ fixed (`100dvh` → 0 in WebView; Samsung force-dark disabled) |
| 8 | Four templates | ✅ GST invoice, GST receipt (Textiles); Coolie invoice, Coolie receipt (Factory) |
| — | Desktop | ❌ still placeholder preview |
