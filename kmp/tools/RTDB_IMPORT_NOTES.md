# Old RTDB export -> new seed data

Source: `elvan-niril-default-rtdb-export (2).json`. Regenerate with `python -I kmp/tools/import_rtdb_export.py <export.json>`
(writes `data/mock/MockData.kt` and the gitignored `composeResources/files/seed/*.txt` images).
The floating `< >` dev menu ("Seed Textiles Data" / "Seed Factory Data") wipes the cloud copy and the local DB, then loads exactly this.

## Counts (export -> app)
| Table | Textiles (silk) | Factory (coolie) |
|---|---|---|
| businesses | `profile` 1 (+ `thannilai` 1, same business) -> 1 | `coolie_thannilai` 2 -> 2 (VRM, PVS) |
| items | `porulgal` 8 -> 8 | `coolie_porulgal` 4 -> 4 |
| customers | `vanigargal` 1 -> 1 | `coolie_vanigargal` 2 -> 2 |
| invoices | `pattiyalkal` 13 -> **12** | `coolie_pattiyalkal` 47 -> 47 (VRM 28, PVS 19) |
| receipts | `patrugal` 4 -> 4 | `coolie_patrugal` 7 -> 7 |

`ImportedSeedDataTest` checks these counts, and that the app's own calculation gives every bill the total the old app printed.

## Mapping
- **Business**: `niruvanathinPeyar_Tamil/English` -> businessName ta/en; `phone` split into phone1/phone2; mugavari/oor/maavattam/maanilam -> address/city/district/state; bank (`vangiPeyar*`, `bankBranch*`, `kanakkuEn`, `ifsc`) -> bank fields; `logo`/`wideLogo`/`signature` -> seed images; `billHeaderStyle` -> headerLayout; `authorizedSignatoryName` + `signatureName` -> signatory (ta + en) in print settings; `logoHeight`, `wideLogoX/Y/Scale` and `meta.invoiceDisplayOptions` (`showItemizedTax`) -> print settings. Factory: `receiptLanguage`/`defaultPrintLanguage` -> print settings, `themeColor` -> themeColor.
- **Silk business uses the top-level `profile`** (account 309150050800239, the one the 2026-07+ bills carry), not `thannilai.biz_…` (older account ...162).
- **Items**: `rate` -> price, `taxPercent` -> taxRate, `hsn`, `unit`, `measureType` -> quantityType.
- **Customers**: Tamil + English name, address (mugavari), city (oor), district, state, country, pin, GSTIN, phone. Values keep their trailing spaces.
- **Silk invoices**: number, date, lines (names ta/en, HSN, qty, unit, rate, tax %, discount: `percentage` -> `%` with the entered value, `amount` -> `₹`), invoice-level discount, tax, total (`totalAmount`, as the old app rounded it), taxInclusive/showHSN/showDiscountColumn/measureMode, terms, note. The `client` copy inside each bill -> the bill's customer snapshot (so SJPS-29/31 keep the older customer text). All point at the one customer (`cli_1` and `cli_1781436385668` are the same business).
- **Coolie bills**: `bill_no`, date, company (`company_id`), customer (by name + city), lines (`kg`, `coolie` rate, names as printed on that bill), `setharam_grams` -> wastageGrams, `courier_rs` -> postalCharges, `ahimsa_silk_rs` -> ahimsaSilkAmount, `grand_total`; totalWeight is recomputed (kg + wastage); bank text/account/IFSC/show flags -> the bill's bankDetailsJson. Item names stay as the old bill printed them (many differ from the current items, e.g. "ஜரி தடை செய்ய கூலி"); a line links to an item only when the Tamil name matches exactly.
- **Receipts**: number, date, amount, payment mode (Bank Transfer/Card/Cash -> bank_transfer/card/cash), reference, note, customer. **Links** are rebuilt from the old `againstInvoice` text: invoices are paid in the listed order, each up to its total (the app has no other record of what a receipt paid). Company = the number's prefix, else the company of the first listed bill.
- Financial year is worked out from each date (e.g. 21/02/2026 is FY 2025), like the editor does.

## Not in the new schema / decisions
- **Skipped**: `pattiyalkal/dup_1781375654449_u39qrvc`, an exact copy of SJPS-31 (same number, lines, date, total).
- **Counters** `meta.counter_INV` (100), `counter_EST` (3), `counter_VRM` (6): no table; the app numbers the next bill from the existing ones (next silk bill after SJPS-575 in FY 2026 is SJPS-576).
- Per-bill `status` / `paidAmount` / `currency`: the app derives paid from receipt links; `currency` has no field. All 13 old bills said "paid", but only the ones a receipt names are linked, so the others show unpaid.
- Receipts with no matching bill in the data (no link made, the receipt itself is kept): RCP/SJPS/04 (names PVS bills, which are coolie bills), RCP/SJS/02 and 03 (INV/2026-27/0001 copies, SJPS-25), and SJPS-27 in RCP/SJS/01 (52,543,303 against 52,504,936 linked). Receipts numbered `RCP/SJS/…` were given to the silk business (SJS is not a business in the data).
- Coolie receipts: RCP/PVS/08 exists three times plus one with no number (all 47,025 on PVS-31/32/33, two dated 2026-10-17). All four kept and linked, so those three bills show as paid four times; delete the extras in the app if they are copies.
- Per-bill display flags with no field in `InvoiceDisplayOptions`: showBank, showDueDate, showNotes, showPlaceOfSupply, showSignature, showTerms, showVehicle; `placeOfSupply` was empty (app default).
- Business fields with no field: `googleClientId`, `googleDriveFolder`, `invoiceTerms` (empty), `enableBilingual` (app default: bilingual), `mobileNumber`/`tholaipesi` (the `phone` text is used), the `_Gujarati/_Hindi/_Malayalam/_Marathi` copies of every text (only ta/en exist), `shortBusinessName*` variants, `upiId` (empty), `pan` (empty).
- Items: `stock` (always 0). Customers: `type: "coolie"`. Coolie customer `address`/`address1` are empty.
- Silk country is empty on the business (the export only has "India" under the other languages); customers carry "India" in Tamil only. Factory businesses have no state/country in the export.
- VRM has no logo or signature in the export (`""`), so none is seeded; the old local `vrm_logo/vrm_signature/react_companies` seed files were removed. PVS has a signature only.
- Unit stays "Nos" on the weight lines of the three weight bills (that is what the old bills hold); `measureMode: weight` is kept on them.
