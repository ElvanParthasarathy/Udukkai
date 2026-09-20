const fs = require('fs');

function updateDict(path, isTamil) {
  let content = fs.readFileSync(path, 'utf-8');
  
  const additions = isTamil ? `
  // Purchases Page
  purchasesSubtitle: 'GSTR-3B இல் ITC கோருவதற்கு சப்ளையர் பில்களைக் கண்காணிக்கவும்',
  addPurchaseBtn: 'கொள்முதலைச் சேர்',
  totalPurchasesCard: 'மொத்த கொள்முதல்',
  gstItcEligibleCard: 'GST (ITC தகுதியுடையது)',
  searchSupplierInvoice: 'சப்ளையர், பில் தேட...',
  purchaseRecordsTable: 'கொள்முதல் பதிவுகள்',
  noPurchaseBillsYet: 'இன்னும் கொள்முதல் பில்கள் பதிவு செய்யப்படவில்லை.',
  noPurchasesMatch: 'உங்கள் வடிகட்டல்களுக்கு எந்த கொள்முதலும் பொருந்தவில்லை.',
  editPurchaseBillTitle: 'கொள்முதல் பில்லைத் திருத்து',
  addPurchaseBillTitle: 'கொள்முதல் பில்லைச் சேர்',
  paymentStatusLabel: 'பணம் செலுத்தும் நிலை',
  supplierNameLabel: 'சப்ளையர் பெயர் *',
  supplierNamePlaceholder: 'விற்பனையாளர் / சப்ளையர் பெயர்',
  supplierGstinLabel: 'சப்ளையர் GSTIN',
  supplierGstinPlaceholder: '15-இலக்க GSTIN',
  invoiceNumberLabel: 'பட்டியல் எண் (Invoice No) *',
  invoiceNumberPlaceholder: 'சப்ளையர் பட்டியல் எண்',
  interstatePurchaseLabel: 'மாநிலங்களுக்கு இடையேயான கொள்முதல்',
  interstatePurchaseDesc: 'சப்ளையர் IGST வசூலித்துள்ளார் (வேறு மாநிலம்)',
  interstatePurchaseTip: 'CGST + SGST-க்கு பதிலாக GSTR-3B இல் ITC-ஐ IGST-க்கு மாற்றுகிறது. குறிப்பு: சப்ளையர் GSTIN இன் முதல் 2 இலக்கங்கள் = அவர்களின் மாநிலக் குறியீடு.',
  itemsTitle: 'பொருட்கள்',
  qtyCol: 'அளவு',
  taxPercentCol: 'வரி %',
  taxableLabelText: 'வரிக்குட்பட்டது:',
  taxLabelText: 'வரி:',
  totalLabelText: 'மொத்தம்:',
  supplierNameRequiredToast: 'சப்ளையர் பெயர் அவசியம்',
  invoiceNumberRequiredToast: 'பட்டியல் எண் அவசியம்',
  purchaseUpdatedToast: 'கொள்முதல் புதுப்பிக்கப்பட்டது',
  purchaseAddedToast: 'கொள்முதல் சேர்க்கப்பட்டது',
  failedToSavePurchaseToast: 'கொள்முதலைச் சேமிக்க முடியவில்லை',
  failedToLoadPurchasesToast: 'கொள்முதலை ஏற்ற முடியவில்லை',
  deletePurchaseConfirmMsg: 'இந்த கொள்முதல் பில்லை நீக்க வேண்டுமா?',
  purchaseDeletedToast: 'கொள்முதல் நீக்கப்பட்டது',
  noPurchasesToExportToast: 'ஏற்றுமதி செய்ய எந்த கொள்முதலும் இல்லை',
  purchasesCsvDownloadedToast: 'கொள்முதல் CSV பதிவிறக்கப்பட்டது',
  supplierCol: 'சப்ளையர்',
  gstinCol: 'GSTIN',
  taxableCol: 'வரிக்குட்பட்டது',
  statusCol: 'நிலை',
  ` : `
  // Purchases Page
  purchasesSubtitle: 'Track supplier invoices for ITC claims in GSTR-3B',
  addPurchaseBtn: 'Add Purchase',
  totalPurchasesCard: 'Total Purchases',
  gstItcEligibleCard: 'GST (ITC Eligible)',
  searchSupplierInvoice: 'Search supplier, invoice...',
  purchaseRecordsTable: 'Purchase Records',
  noPurchaseBillsYet: 'No purchase bills recorded yet.',
  noPurchasesMatch: 'No purchases match your filters.',
  editPurchaseBillTitle: 'Edit Purchase Bill',
  addPurchaseBillTitle: 'Add Purchase Bill',
  paymentStatusLabel: 'Payment Status',
  supplierNameLabel: 'Supplier Name *',
  supplierNamePlaceholder: 'Vendor / Supplier name',
  supplierGstinLabel: 'Supplier GSTIN',
  supplierGstinPlaceholder: '15-digit GSTIN',
  invoiceNumberLabel: 'Invoice Number *',
  invoiceNumberPlaceholder: 'Supplier invoice no.',
  interstatePurchaseLabel: 'Inter-maanilam purchase',
  interstatePurchaseDesc: 'supplier charged IGST (different maanilam)',
  interstatePurchaseTip: 'Routes ITC to IGST in GSTR-3B instead of CGST + SGST. Tip: first 2 digits of supplier GSTIN = their maanilam code.',
  itemsTitle: 'Items',
  qtyCol: 'Qty',
  taxPercentCol: 'Tax %',
  taxableLabelText: 'Taxable:',
  taxLabelText: 'Tax:',
  totalLabelText: 'Total:',
  supplierNameRequiredToast: 'Supplier name is required',
  invoiceNumberRequiredToast: 'Invoice number is required',
  purchaseUpdatedToast: 'Purchase updated',
  purchaseAddedToast: 'Purchase added',
  failedToSavePurchaseToast: 'Failed to save purchase',
  failedToLoadPurchasesToast: 'Failed to load purchases',
  deletePurchaseConfirmMsg: 'Delete this purchase bill?',
  purchaseDeletedToast: 'Purchase deleted',
  noPurchasesToExportToast: 'No purchases to export',
  purchasesCsvDownloadedToast: 'Purchases CSV downloaded',
  supplierCol: 'Supplier',
  gstinCol: 'GSTIN',
  taxableCol: 'Taxable',
  statusCol: 'Status',
  `;

  if (!content.includes('purchasesSubtitle:')) {
    content = content.replace(/};\s*export type TranslationKey/, additions + '\n};\n\nexport type TranslationKey');
    fs.writeFileSync(path, content, 'utf-8');
  }
}

updateDict('moolam/mozhi/ta.ts', true);
updateDict('moolam/mozhi/en.ts', false);

let v = fs.readFileSync('moolam/pagudhigal/Kolmudhal.tsx', 'utf-8');
if (!v.includes('useLanguage')) {
  v = v.replace(/import \{ thagaval \} from '\.\/Thagaval';/, "import { thagaval } from './Thagaval';\nimport { useLanguage } from '../mozhi/LanguageContext';");
  v = v.replace(/export default function Kolmudhal\(\) \{/, "$&\n  const { t } = useLanguage();");
}

v = v.replace(/'Failed to load purchases'/g, "t('failedToLoadPurchasesToast')");
v = v.replace(/'Supplier name is required'/g, "t('supplierNameRequiredToast')");
v = v.replace(/'Invoice number is required'/g, "t('invoiceNumberRequiredToast')");
v = v.replace(/'Purchase updated'/g, "t('purchaseUpdatedToast')");
v = v.replace(/'Purchase added'/g, "t('purchaseAddedToast')");
v = v.replace(/'Failed to save purchase'/g, "t('failedToSavePurchaseToast')");
v = v.replace(/'Delete this purchase bill\?'/g, "t('deletePurchaseConfirmMsg')");
v = v.replace(/'Purchase deleted'/g, "t('purchaseDeletedToast')");
v = v.replace(/'Failed to delete'/g, "t('failedToDelete')");
v = v.replace(/'No purchases to export'/g, "t('noPurchasesToExportToast')");
v = v.replace(/'Purchases CSV downloaded'/g, "t('purchasesCsvDownloadedToast')");

v = v.replace(/>Purchase Bills</g, ">{t('purchases')}<");
v = v.replace(/>Track supplier invoices for ITC claims in GSTR-3B</g, ">{t('purchasesSubtitle')}<");
v = v.replace(/<Download size=\{16\} \/> Export CSV/g, "<Download size={16} /> {t('exportCsvBtn')}");
v = v.replace(/<Plus size=\{18\} \/> Add Purchase/g, "<Plus size={18} /> {t('addPurchaseBtn')}");

v = v.replace(/>Total Purchases</g, ">{t('totalPurchasesCard')}<");
v = v.replace(/>GST \(ITC Eligible\)</g, ">{t('gstItcEligibleCard')}<");
v = v.replace(/>Entries</g, ">{t('entriesCard')}<");
v = v.replace(/placeholder="Search supplier, invoice\.\.\."/g, "placeholder={t('searchSupplierInvoice')}");

v = v.replace(/>Purchase Records</g, ">{t('purchaseRecordsTable')}<");
v = v.replace(/purchases\.length === 0 \? 'No purchase bills recorded yet\.' : 'No purchases match your filters\.'/g, "purchases.length === 0 ? t('noPurchaseBillsYet') : t('noPurchasesMatch')");

v = v.replace(/\{editingId \? 'Edit Purchase Bill' : 'Add Purchase Bill'\}/g, "{editingId ? t('editPurchaseBillTitle') : t('addPurchaseBillTitle')}");
v = v.replace(/>Date \*</g, ">{t('dateLabelStar')}<");
v = v.replace(/>Payment Status</g, ">{t('paymentStatusLabel')}<");
v = v.replace(/>Supplier Name \*</g, ">{t('supplierNameLabel')}<");
v = v.replace(/placeholder="Vendor \/ Supplier name"/g, "placeholder={t('supplierNamePlaceholder')}");
v = v.replace(/>Supplier GSTIN</g, ">{t('supplierGstinLabel')}<");
v = v.replace(/placeholder="15-digit GSTIN"/g, "placeholder={t('supplierGstinPlaceholder')}");
v = v.replace(/>Invoice Number \*</g, ">{t('invoiceNumberLabel')}<");
v = v.replace(/placeholder="Supplier invoice no\."/g, "placeholder={t('invoiceNumberPlaceholder')}");
v = v.replace(/>Note \(optional\)</g, ">{t('noteOptionalLabel')}<");
v = v.replace(/placeholder="Any note\.\.\."/g, "placeholder={t('noteOptionalLabel')}");

v = v.replace(/>\s*Inter-maanilam purchase\s*</g, "> {t('interstatePurchaseLabel')} <");
v = v.replace(/>\s*— supplier charged IGST \(different maanilam\)\s*</g, "> — {t('interstatePurchaseDesc')} <");
v = v.replace(/>\s*Routes ITC to IGST in GSTR-3B instead of CGST \+ SGST\. Tip: first 2 digits of supplier GSTIN = their maanilam code\.\s*</g, ">{t('interstatePurchaseTip')}<");

v = v.replace(/>Items</g, ">{t('itemsTitle')}<");
v = v.replace(/>Name</g, ">{t('nameCol')}<");
v = v.replace(/>HSN</g, ">{t('hsnCol')}<");
v = v.replace(/>Qty</g, ">{t('qtyCol')}<");
v = v.replace(/>Rate</g, ">{t('rateCol')}<");
v = v.replace(/>Tax %</g, ">{t('taxPercentCol')}<");
v = v.replace(/<Plus size=\{14\} \/> Add Item/g, "<Plus size={14} /> {t('addItemBtn')}");

v = v.replace(/>Taxable: /g, ">{t('taxableLabelText')} ");
v = v.replace(/>Tax: /g, ">{t('taxLabelText')} ");
v = v.replace(/>Total: /g, ">{t('totalLabelText')} ");

v = v.replace(/>Cancel</g, ">{t('cancelModalBtn')}<");
v = v.replace(/\{editingId \? 'Update' : 'Save'\}/g, "{editingId ? t('updateClientModalBtn') : t('saveClientModalBtn')}");

v = v.replace(/>Date</g, ">{t('dateLabel')}<");
v = v.replace(/>Supplier</g, ">{t('supplierCol')}<");
v = v.replace(/>GSTIN</g, ">{t('gstinCol')}<");
v = v.replace(/>Taxable</g, ">{t('taxableCol')}<");
v = v.replace(/>Tax</g, ">{t('taxLabelText')}<");
v = v.replace(/>Total</g, ">{t('totalLabel')}<");
v = v.replace(/>Status</g, ">{t('statusCol')}<");
v = v.replace(/>Actions</g, ">{t('actionsCol')}<");

fs.writeFileSync('moolam/pagudhigal/Kolmudhal.tsx', v, 'utf-8');
