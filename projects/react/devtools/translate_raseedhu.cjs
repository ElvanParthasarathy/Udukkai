const fs = require('fs');

function updateDict(path, isTamil) {
  let content = fs.readFileSync(path, 'utf-8');
  
  const additions = isTamil ? `
  // Receipts Page
  receiptsSubtitle: 'வாடிக்கையாளர்களுக்கான கட்டண ரசீதுகளை உருவாக்கவும்',
  newReceiptBtn: 'புதிய ரசீது',
  newPaymentReceiptTitle: 'புதிய கட்டண ரசீது',
  quickSelectUnpaid: 'விரைவான தேர்வு — நிலுவையில் உள்ள பட்டியல்கள்',
  receiptNoLabel: 'ரசீது எண்',
  dateLabel: 'தேதி',
  receivedFromLabel: 'பெறப்பட்டது (வாடிக்கையாளர் பெயர்) *',
  amountLabelStar: 'தொகை *',
  paymentModeLabel: 'கட்டண முறை',
  referenceNoLabel: 'பரிவர்த்தனை எண்',
  againstInvoiceLabel: 'எந்தப் பட்டியலுக்கு (Against Invoice)',
  againstInvoicePlaceholder: 'உ.ம். INV/2025-26/0001',
  noteOptionalLabel: 'குறிப்பு (விரும்பினால்)',
  saveReceiptBtn: 'ரசீதைச் சேமி',
  searchReceipts: 'ரசீதுகளைத் தேட...',
  paymentReceiptsTableTitle: 'கட்டண ரசீதுகள்',
  noReceiptsYet: 'இன்னும் ரசீதுகள் உருவாக்கப்படவில்லை.',
  noReceiptsMatch: 'உங்கள் தேடலுக்கு எந்த ரசீதும் பொருந்தவில்லை.',
  createReceiptBtn: 'ரசீதை உருவாக்கு',
  clientCol: 'வாடிக்கையாளர்',
  againstInvoiceCol: 'எந்தப் பட்டியலுக்கு',
  modeCol: 'கட்டண முறை',
  clientNameRequiredToast: 'வாடிக்கையாளர் பெயர் அவசியம்',
  enterValidAmountToast: 'சரியான தொகையை உள்ளிடவும்',
  receiptSavedToast: 'ரசீது சேமிக்கப்பட்டது',
  deleteReceiptConfirmMsg: 'இந்த ரசீதை நீக்க வேண்டுமா?',
  deletedToast: 'நீக்கப்பட்டது',
  ` : `
  // Receipts Page
  receiptsSubtitle: 'Generate payment receipts for clients',
  newReceiptBtn: 'New Receipt',
  newPaymentReceiptTitle: 'New Payment Receipt',
  quickSelectUnpaid: 'Quick Select — Unpaid Invoices',
  receiptNoLabel: 'Receipt No',
  dateLabel: 'Date',
  receivedFromLabel: 'Received From (Client Name) *',
  amountLabelStar: 'Amount *',
  paymentModeLabel: 'Payment Mode',
  referenceNoLabel: 'Reference / Transaction No',
  againstInvoiceLabel: 'Against Invoice',
  againstInvoicePlaceholder: 'e.g. INV/2025-26/0001',
  noteOptionalLabel: 'Note (optional)',
  saveReceiptBtn: 'Save Receipt',
  searchReceipts: 'Search receipts...',
  paymentReceiptsTableTitle: 'Payment Receipts',
  noReceiptsYet: 'No receipts generated yet.',
  noReceiptsMatch: 'No receipts match your search.',
  createReceiptBtn: 'Create Receipt',
  clientCol: 'Client',
  againstInvoiceCol: 'Against Invoice',
  modeCol: 'Mode',
  clientNameRequiredToast: 'Client name required',
  enterValidAmountToast: 'Enter valid amount',
  receiptSavedToast: 'Receipt saved',
  deleteReceiptConfirmMsg: 'Delete this receipt?',
  deletedToast: 'Deleted',
  `;

  if (!content.includes('receiptsSubtitle:')) {
    content = content.replace(/};\s*export type TranslationKey/, additions + '\n};\n\nexport type TranslationKey');
    fs.writeFileSync(path, content, 'utf-8');
  }
}

updateDict('moolam/mozhi/ta.ts', true);
updateDict('moolam/mozhi/en.ts', false);

let v = fs.readFileSync('moolam/pagudhigal/Raseedhu.tsx', 'utf-8');
if (!v.includes('useLanguage')) {
  v = v.replace(/import \{ thagaval \} from '\.\/Thagaval';/, "import { thagaval } from './Thagaval';\nimport { useLanguage } from '../mozhi/LanguageContext';");
  v = v.replace(/export default function Raseedhu\(\) \{/, "$&\n  const { t } = useLanguage();");
}

v = v.replace(/'Client name required'/g, "t('clientNameRequiredToast')");
v = v.replace(/'Enter valid amount'/g, "t('enterValidAmountToast')");
v = v.replace(/'Receipt saved'/g, "t('receiptSavedToast')");
v = v.replace(/'Delete this receipt\?'/g, "t('deleteReceiptConfirmMsg')");
v = v.replace(/'Deleted'/g, "t('deletedToast')");

v = v.replace(/>Receipts</g, ">{t('receipts')}<");
v = v.replace(/>Generate payment receipts for clients</g, ">{t('receiptsSubtitle')}<");
v = v.replace(/<Plus size=\{18\} \/> New Receipt/g, "<Plus size={18} /> {t('newReceiptBtn')}");
v = v.replace(/>New Payment Receipt</g, ">{t('newPaymentReceiptTitle')}<");
v = v.replace(/>Quick Select — Unpaid Invoices</g, ">{t('quickSelectUnpaid')}<");
v = v.replace(/>Receipt No</g, ">{t('receiptNoLabel')}<");
v = v.replace(/>Date</g, ">{t('dateLabel')}<");
v = v.replace(/>Received From \(Client Name\) \*</g, ">{t('receivedFromLabel')}<");
v = v.replace(/>Amount \*</g, ">{t('amountLabelStar')}<");
v = v.replace(/>Payment Mode</g, ">{t('paymentModeLabel')}<");
v = v.replace(/>Reference \/ Transaction No</g, ">{t('referenceNoLabel')}<");
v = v.replace(/>Against Invoice</g, ">{t('againstInvoiceLabel')}<");
v = v.replace(/placeholder="e\.g\. INV\/2025-26\/0001"/g, "placeholder={t('againstInvoicePlaceholder')}");
v = v.replace(/>Note \(optional\)</g, ">{t('noteOptionalLabel')}<");
v = v.replace(/>Cancel</g, ">{t('cancelModalBtn')}<");
v = v.replace(/>Save Receipt</g, ">{t('saveReceiptBtn')}<");
v = v.replace(/placeholder="Search receipts\.\.\."/g, "placeholder={t('searchReceipts')}");
v = v.replace(/>Payment Receipts</g, ">{t('paymentReceiptsTableTitle')}<");
v = v.replace(/receipts\.length === 0 \? 'No receipts generated yet\.' : 'No receipts match your search\.'/g, "receipts.length === 0 ? t('noReceiptsYet') : t('noReceiptsMatch')");
v = v.replace(/<Plus size=\{18\} \/> Create Receipt/g, "<Plus size={18} /> {t('createReceiptBtn')}");
v = v.replace(/>Client</g, ">{t('clientCol')}<");
v = v.replace(/>Against Invoice</g, ">{t('againstInvoiceCol')}<");
v = v.replace(/>Amount</g, ">{t('amountCol')}<");
v = v.replace(/>Mode</g, ">{t('modeCol')}<");
v = v.replace(/>Actions</g, ">{t('actionsCol')}<");

fs.writeFileSync('moolam/pagudhigal/Raseedhu.tsx', v, 'utf-8');
