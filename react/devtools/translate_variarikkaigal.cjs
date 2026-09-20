const fs = require('fs');

function updateDict(path, isTamil) {
  let content = fs.readFileSync(path, 'utf-8');
  
  const additions = isTamil ? `
  // GST Returns Page
  gstReturnsTitle: 'GST அறிக்கைகள்',
  gstReturnsSubtitle: 'GST அறிக்கைகள் மற்றும் தாக்கல் நிலையை நிர்வகிக்கவும்',
  monthlyTab: 'மாதாந்திரம்',
  quarterlyTab: 'காலாண்டு',
  r1Pending: 'R1 நிலுவை',
  b3Pending: '3B நிலுவை',
  gstPortalBtn: 'GST தளம்',
  gstinNotSet: 'உங்கள் வணிக GSTIN அமைக்கப்படவில்லை. அதைச் சேர்க்க அமைப்புகள் → நிறுவன விவரங்களுக்குச் செல்லவும்.',
  invoicesCountLabel: 'பட்டியல்கள்',
  taxableLabel: 'வரிக்குட்பட்டது',
  taxLabel: 'வரி',
  netPayableLabel: 'நிகர செலுத்த வேண்டியவை',
  gstr1Tab: 'GSTR-1',
  gstr3bTab: 'GSTR-3B',
  gstr2bReconTab: 'GSTR-2B ஒப்பீடு',
  tdsTcsTab: 'TDS / TCS அறிக்கை',
  filingGuideTab: 'தாக்கல் வழிகாட்டி',
  jsonExportBtn: 'JSON ஏற்றுமதி',
  docsBtn: 'ஆவணங்கள்',
  markFiledBtn: 'தாக்கல் செய்யப்பட்டதாகக் குறி',
  b2bSalesTable: 'B2B விற்பனை — அட்டவணை 4A',
  b2cSalesTable: 'B2C விற்பனை — அட்டவணை 7',
  hsnSummaryTable: 'HSN சுருக்கம் — அட்டவணை 12',
  docSummaryTable: 'ஆவண சுருக்கம் — அட்டவணை 13',
  gstr1SummaryTotals: 'GSTR-1 சுருக்க மொத்தங்கள்',
  noB2bInvoices: 'இந்த காலத்திற்கு B2B பட்டியல்கள் எதுவும் இல்லை.',
  noB2cInvoices: 'இந்த காலத்திற்கு B2C பட்டியல்கள் எதுவும் இல்லை.',
  rateCol: 'விகிதம் %',
  cgstCol: 'CGST',
  sgstCol: 'SGST',
  igstCol: 'IGST',
  totalCol: 'மொத்தம்',
  b2cTotal: 'B2C மொத்தம்',
  hsnCol: 'HSN',
  descriptionCol: 'விளக்கம்',
  qtyCol: 'அளவு',
  totalTaxCol: 'மொத்த வரி',
  docTypeCol: 'ஆவண வகை',
  fromCol: 'இருந்து',
  toCol: 'வரை',
  totalIssuedCol: 'மொத்தம் வழங்கப்பட்டது',
  categoryCol: 'வகை',
  grandTotal: 'பெரு மொத்தம்',
  b2bSalesCategory: 'B2B விற்பனை',
  b2cSalesCategory: 'B2C விற்பனை',
  invoiceWord: 'பட்டியல்',
  invoicesWord: 'பட்டியல்கள்',
  codeWord: 'குறியீடு',
  codesWord: 'குறியீடுகள்',
  ` : `
  // GST Returns Page
  gstReturnsTitle: 'GST Returns',
  gstReturnsSubtitle: 'GST Returns and filing status management',
  monthlyTab: 'Monthly',
  quarterlyTab: 'Quarterly',
  r1Pending: 'R1 Pending',
  b3Pending: '3B Pending',
  gstPortalBtn: 'GST Portal',
  gstinNotSet: 'Your business GSTIN is not set. Go to Settings → Company Details to add it.',
  invoicesCountLabel: 'Invoices',
  taxableLabel: 'Taxable',
  taxLabel: 'Tax',
  netPayableLabel: 'Net Payable',
  gstr1Tab: 'GSTR-1',
  gstr3bTab: 'GSTR-3B',
  gstr2bReconTab: 'GSTR-2B Reconciliation',
  tdsTcsTab: 'TDS / TCS Report',
  filingGuideTab: 'Filing Guide',
  jsonExportBtn: 'JSON Export',
  docsBtn: 'Docs',
  markFiledBtn: 'Mark Filed',
  b2bSalesTable: 'B2B Sales — Table 4A',
  b2cSalesTable: 'B2C Sales — Table 7',
  hsnSummaryTable: 'HSN Summary — Table 12',
  docSummaryTable: 'Document Summary — Table 13',
  gstr1SummaryTotals: 'GSTR-1 Summary Totals',
  noB2bInvoices: 'No B2B invoices for this period.',
  noB2cInvoices: 'No B2C invoices for this period.',
  rateCol: 'Rate %',
  cgstCol: 'CGST',
  sgstCol: 'SGST',
  igstCol: 'IGST',
  totalCol: 'Total',
  b2cTotal: 'B2C Total',
  hsnCol: 'HSN',
  descriptionCol: 'Description',
  qtyCol: 'Qty',
  totalTaxCol: 'Total Tax',
  docTypeCol: 'Document Type',
  fromCol: 'From',
  toCol: 'To',
  totalIssuedCol: 'Total Issued',
  categoryCol: 'Category',
  grandTotal: 'Grand Total',
  b2bSalesCategory: 'B2B Sales',
  b2cSalesCategory: 'B2C Sales',
  invoiceWord: 'invoice',
  invoicesWord: 'invoices',
  codeWord: 'code',
  codesWord: 'codes',
  `;

  if (!content.includes('gstReturnsTitle:')) {
    content = content.replace(/};\s*export type TranslationKey/, additions + '\n};\n\nexport type TranslationKey');
    fs.writeFileSync(path, content, 'utf-8');
  }
}

updateDict('moolam/mozhi/ta.ts', true);
updateDict('moolam/mozhi/en.ts', false);

let v = fs.readFileSync('moolam/pagudhigal/VariArikkaigal.tsx', 'utf-8');

if (!v.includes('useLanguage')) {
  v = v.replace(/import \{ thagaval \} from '\.\/Thagaval';/, "import { thagaval } from './Thagaval';\nimport { useLanguage } from '../mozhi/LanguageContext';");
}

if (!v.includes('const { t } = useLanguage();')) {
  v = v.replace(/export default function VariArikkaigal\(\) \{/, "$&\n  const { t } = useLanguage();\n");
}

// Translations replacements
v = v.replace(/>GST Returns<\/h1>/g, ">{t('gstReturnsTitle')}</h1>");
v = v.replace(/>GST Returns<\/h3>/g, ">{t('gstReturnsTitle')}</h3>"); // fallback just in case
v = v.replace(/<p className="page-subtitle">[^<]+<\/p>/g, '<p className="page-subtitle">{t("gstReturnsSubtitle")}</p>');

v = v.replace(/>Monthly</g, ">{t('monthlyTab')}<");
v = v.replace(/>Quarterly</g, ">{t('quarterlyTab')}<");
v = v.replace(/>R1 Pending</g, ">{t('r1Pending')}<");
v = v.replace(/>3B Pending</g, ">{t('b3Pending')}<");
v = v.replace(/>GST Portal</g, ">{t('gstPortalBtn')}<");

v = v.replace(/Your business GSTIN is not set\..*add it\./g, "{t('gstinNotSet')}");
v = v.replace(/>Invoices<\/p>/g, ">{t('invoicesCountLabel')}</p>");
v = v.replace(/>Taxable<\/p>/g, ">{t('taxableLabel')}</p>");
v = v.replace(/>Tax<\/p>/g, ">{t('taxLabel')}</p>");
v = v.replace(/>Net Payable<\/p>/g, ">{t('netPayableLabel')}</p>");

v = v.replace(/>GSTR-1</g, ">{t('gstr1Tab')}<");
v = v.replace(/>GSTR-3B</g, ">{t('gstr3bTab')}<");
v = v.replace(/>GSTR-2B Reconciliation</g, ">{t('gstr2bReconTab')}<");
v = v.replace(/>TDS \/ TCS Report</g, ">{t('tdsTcsTab')}<");
v = v.replace(/>Filing Guide</g, ">{t('filingGuideTab')}<");

v = v.replace(/>JSON Export</g, ">{t('jsonExportBtn')}<");
v = v.replace(/>Docs</g, ">{t('docsBtn')}<");
v = v.replace(/>Mark Filed</g, ">{t('markFiledBtn')}<");

v = v.replace(/>B2B Sales — Table 4A</g, ">{t('b2bSalesTable')}<");
v = v.replace(/>No B2B invoices for this period\.</g, ">{t('noB2bInvoices')}<");

v = v.replace(/>B2C Sales — Table 7</g, ">{t('b2cSalesTable')}<");
v = v.replace(/>No B2C invoices for this period\.</g, ">{t('noB2cInvoices')}<");

v = v.replace(/>HSN Summary — Table 12</g, ">{t('hsnSummaryTable')}<");
v = v.replace(/>Document Summary — Table 13</g, ">{t('docSummaryTable')}<");
v = v.replace(/>GSTR-1 Summary Totals</g, ">{t('gstr1SummaryTotals')}<");

v = v.replace(/>Rate %</g, ">{t('rateCol')}<");
v = v.replace(/>Taxable</g, ">{t('taxableLabel')}<");
v = v.replace(/>CGST</g, ">{t('cgstCol')}<");
v = v.replace(/>SGST</g, ">{t('sgstCol')}<");
v = v.replace(/>IGST</g, ">{t('igstCol')}<");
v = v.replace(/>Total</g, ">{t('totalCol')}<");
v = v.replace(/>B2C Total</g, ">{t('b2cTotal')}<");

v = v.replace(/>HSN</g, ">{t('hsnCol')}<");
v = v.replace(/>Description</g, ">{t('descriptionCol')}<");
v = v.replace(/>Qty</g, ">{t('qtyCol')}<");
v = v.replace(/>Total Tax</g, ">{t('totalTaxCol')}<");

v = v.replace(/>Document Type</g, ">{t('docTypeCol')}<");
v = v.replace(/>From</g, ">{t('fromCol')}<");
v = v.replace(/>To</g, ">{t('toCol')}<");
v = v.replace(/>Total Issued</g, ">{t('totalIssuedCol')}<");

v = v.replace(/>Category</g, ">{t('categoryCol')}<");
v = v.replace(/>B2B Sales</g, ">{t('b2bSalesCategory')}<");
v = v.replace(/>B2C Sales</g, ">{t('b2cSalesCategory')}<");
v = v.replace(/>Grand Total</g, ">{t('grandTotal')}<");

// 0 invoices
v = v.replace(/\{b2bRows\.length\} invoice\{b2bRows\.length !== 1 \? 's' : ''\}/g, "{b2bRows.length} {b2bRows.length !== 1 ? t('invoicesWord') : t('invoiceWord')}");
v = v.replace(/\{b2cBills\.length\} invoice\{b2cBills\.length !== 1 \? 's' : ''\}/g, "{b2cBills.length} {b2cBills.length !== 1 ? t('invoicesWord') : t('invoiceWord')}");
v = v.replace(/\{hsnRows\.length\} code\{hsnRows\.length !== 1 \? 's' : ''\}/g, "{hsnRows.length} {hsnRows.length !== 1 ? t('codesWord') : t('codeWord')}");

// Month names in dropdown
v = v.replace(/MONTHS\.map/g, "(MONTHS as string[]).map");
// Fix month names array
v = v.replace(/const MONTHS = \['January','February','March','April','May','June','July','August','September','October','November','December'\];/g, "");
if (!v.includes("const MONTHS = [t('january'), t('february'),")) {
  v = v.replace(/const \{ t \} = useLanguage\(\);/, "const { t } = useLanguage();\n  const MONTHS = [t('january'), t('february'), t('march'), t('april'), t('may'), t('june'), t('july'), t('august'), t('september'), t('october'), t('november'), t('december')];\n");
}


fs.writeFileSync('moolam/pagudhigal/VariArikkaigal.tsx', v, 'utf-8');
