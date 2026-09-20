const fs = require('fs');

function updateDict(path, isTamil) {
  let content = fs.readFileSync(path, 'utf-8');
  
  const additions = isTamil ? `
  // Clients Page
  clientsTitle: 'வாடிக்கையாளர்கள்',
  clientsSubtitle: 'வாடிக்கையாளர் வாரியான பட்டியல் மற்றும் நிலுவை',
  importCSV: 'CSV ஐ இறக்குமதி செய்',
  addClient: 'வாடிக்கையாளரைச் சேர்',
  newInvoiceBtn: 'புதிய பட்டியல்',
  searchClients: 'வாடிக்கையாளர்களைத் தேட...',
  noClientsFound: 'வாடிக்கையாளர்கள் யாரும் இல்லை.',
  addFirstClient: 'உங்கள் முதல் வாடிக்கையாளரைச் சேர்க்கவும்',
  invoicesCount: 'பட்டியல்கள்',
  invoiceCountSingular: 'பட்டியல்',
  totalLabel: 'மொத்தம்',
  paidLabel: 'செலுத்தப்பட்டது',
  outstandingLabel: 'நிலுவை',
  hideLabel: 'மறை',
  viewLabel: 'காண்பி',
  noInvoicesForClient: 'இந்த வாடிக்கையாளருக்கு இன்னும் பட்டியல்கள் இல்லை.',
  createInvoiceBtn: 'பட்டியலை உருவாக்கு',
  removeSavedClientConfirm: 'இந்த வாடிக்கையாளரை அகற்ற வேண்டுமா?',
  clientRemoved: 'வாடிக்கையாளர் அகற்றப்பட்டார்',
  deleteInvoiceConfirm: 'இந்த பட்டியலை நீக்க வேண்டுமா? இதை மாற்ற இயலாது.',
  invoiceDeleted: 'பட்டியல் நீக்கப்பட்டது',
  failedToDelete: 'நீக்க முடியவில்லை',
  editClientBtn: 'திருத்து',
  deleteClientBtn: 'நீக்கு',
  saveAsClientBtn: 'வாடிக்கையாளராகச் சேமி',
  editClientTitle: 'வாடிக்கையாளரைத் திருத்து',
  addNewClientTitle: 'புதிய வாடிக்கையாளரைச் சேர்',
  clientBusinessName: 'வாடிக்கையாளர் / நிறுவனத்தின் பெயர் *',
  streetAddressPlaceholder: 'தெரு முகவரி, பகுதி',
  cityPlaceholder: 'உ.ம். சென்னை',
  selectLabel: 'தேர்ந்தெடு',
  emailLabel: 'மின்னஞ்சல்',
  emailPlaceholder: 'client@example.com',
  phoneLabel: 'தொலைபேசி',
  phonePlaceholder: '+91 98765 43210',
  sezUnitTitle: 'SEZ / Developer — மாநிலத்தைப் பொருட்படுத்தாமல் IGST விதிக்கப்படும் (பிரிவு 16, IGST சட்டம்).',
  updateClientModalBtn: 'புதுப்பி',
  saveClientModalBtn: 'சேமி',
  cancelModalBtn: 'ரத்து',
  ` : `
  // Clients Page
  clientsTitle: 'Clients',
  clientsSubtitle: 'Client-wise invoice ledger and outstanding',
  importCSV: 'Import CSV',
  addClient: 'Add Client',
  newInvoiceBtn: 'New Invoice',
  searchClients: 'Search clients...',
  noClientsFound: 'No clients found.',
  addFirstClient: 'Add Your First Client',
  invoicesCount: 'invoices',
  invoiceCountSingular: 'invoice',
  totalLabel: 'Total',
  paidLabel: 'Paid',
  outstandingLabel: 'Outstanding',
  hideLabel: 'Hide',
  viewLabel: 'View',
  noInvoicesForClient: 'No invoices for this client yet.',
  createInvoiceBtn: 'Create Invoice',
  removeSavedClientConfirm: 'Remove this saved client?',
  clientRemoved: 'Client removed',
  deleteInvoiceConfirm: 'Delete this invoice? This cannot be undone.',
  invoiceDeleted: 'Invoice deleted',
  failedToDelete: 'Failed to delete',
  editClientBtn: 'Edit Client',
  deleteClientBtn: 'Delete Client',
  saveAsClientBtn: 'Save as Client',
  editClientTitle: 'Edit Client',
  addNewClientTitle: 'Add New Client',
  clientBusinessName: 'Client / Business Name *',
  streetAddressPlaceholder: 'Street address, locality',
  cityPlaceholder: 'e.g. Mumbai',
  selectLabel: 'Select',
  emailLabel: 'Email',
  emailPlaceholder: 'client@example.com',
  phoneLabel: 'Phone',
  phonePlaceholder: '+91 98765 43210',
  sezUnitTitle: 'SEZ unit / Developer — supplies will be charged IGST regardless of state (Section 16, IGST Act).',
  updateClientModalBtn: 'Update Client',
  saveClientModalBtn: 'Save Client',
  cancelModalBtn: 'Cancel',
  `;

  if (!content.includes('clientsTitle:')) {
    content = content.replace(/};\s*export type TranslationKey/, additions + '\n};\n\nexport type TranslationKey');
    fs.writeFileSync(path, content, 'utf-8');
  }
}

updateDict('moolam/mozhi/ta.ts', true);
updateDict('moolam/mozhi/en.ts', false);

// 1. Patch Vanigargal.tsx
let v = fs.readFileSync('moolam/pagudhigal/Vanigargal.tsx', 'utf-8');
// add useLanguage import if not present
if (!v.includes('useLanguage')) {
  v = v.replace(/import \{ thagaval \} from '\.\/Thagaval';/, "import { thagaval } from './Thagaval';\nimport { useLanguage } from '../mozhi/LanguageContext';");
  v = v.replace(/export default function Vanigargal\(\{[^}]+\}\) \{/, "$&\n  const { t } = useLanguage();");
}

v = v.replace(/>Clients</g, ">{t('clientsTitle')}<");
v = v.replace(/>Client-wise invoice ledger and outstanding</g, ">{t('clientsSubtitle')}<");
v = v.replace(/> Import CSV</g, "> {t('importCSV')}<");
v = v.replace(/> Add Client</g, "> {t('addClient')}<");
v = v.replace(/> New Invoice</g, "> {t('newInvoiceBtn')}<");
v = v.replace(/placeholder="Search clients\.\.\."/g, "placeholder={t('searchClients')}");
v = v.replace(/>No clients found\.</g, ">{t('noClientsFound')}<");
v = v.replace(/> Add Your First Client</g, "> {t('addFirstClient')}<");
v = v.replace(/invoice\{stats\.count !== 1 \? 's' : ''\}/g, "{stats.count !== 1 ? t('invoicesCount') : t('invoiceCountSingular')}");
v = v.replace(/>Total</g, ">{t('totalLabel')}<");
v = v.replace(/>Paid</g, ">{t('paidLabel')}<");
v = v.replace(/>Outstanding</g, ">{t('outstandingLabel')}<");
v = v.replace(/\{isExpanded \? 'Hide' : 'View'\}/g, "{isExpanded ? t('hideLabel') : t('viewLabel')}");
v = v.replace(/>No invoices for this client yet\.</g, ">{t('noInvoicesForClient')}<");
v = v.replace(/> Create Invoice</g, "> {t('createInvoiceBtn')}<");
v = v.replace(/> Edit Client</g, "> {t('editClientBtn')}<");
v = v.replace(/> Delete Client</g, "> {t('deleteClientBtn')}<");
v = v.replace(/> Save as Client</g, "> {t('saveAsClientBtn')}<");
v = v.replace(/confirm\('Remove this saved client\?'\)/g, "confirm(t('removeSavedClientConfirm'))");
v = v.replace(/'Client removed'/g, "t('clientRemoved')");
v = v.replace(/confirm\('Delete this invoice\? This cannot be undone\.'\)/g, "confirm(t('deleteInvoiceConfirm'))");
v = v.replace(/'Invoice deleted'/g, "t('invoiceDeleted')");
v = v.replace(/'Failed to delete'/g, "t('failedToDelete')");
// Table headers
v = v.replace(/>Date</g, ">{t('dateCol')}<");
v = v.replace(/>Invoice No\.</g, ">{t('invoiceNoCol')}<");
v = v.replace(/>Type</g, ">{t('typeCol')}<");
v = v.replace(/>Amount</g, ">{t('amountCol')}<");
v = v.replace(/>Status</g, ">{t('statusCol')}<");
v = v.replace(/>Actions</g, ">{t('actionsCol')}<");
v = v.replace(/>\{\(INVOICE_TYPES\[bill\.invoiceType \|\| 'tax-invoice'\]\)\?\.label\}</g, ">{t(`invoiceTypes_${(bill.invoiceType || 'tax-invoice').replace(/-/g, '_')}` as any, { defaultValue: INVOICE_TYPES[bill.invoiceType || 'tax-invoice']?.label })}<");

fs.writeFileSync('moolam/pagudhigal/Vanigargal.tsx', v, 'utf-8');

// 2. Patch VanigarThirai.tsx
let vt = fs.readFileSync('moolam/pagudhigal/VanigarThirai.tsx', 'utf-8');
if (!vt.includes('useLanguage')) {
  vt = vt.replace(/import \{ getRegionMode \} from '\.\.\/Avanam';/, "import { getRegionMode } from '../Avanam';\nimport { useLanguage } from '../mozhi/LanguageContext';");
  vt = vt.replace(/export default function VanigarThirai\(\{[^}]+\}\) \{/, "$&\n  const { t } = useLanguage();");
}

vt = vt.replace(/\{isEditing \? 'Edit Client' : 'Add New Client'\}/g, "{isEditing ? t('editClientTitle') : t('addNewClientTitle')}");
vt = vt.replace(/>Client \/ Business Name \*</g, ">{t('clientBusinessName')}<");
vt = vt.replace(/>mugavari</g, ">{t('billingAddress')}<");
vt = vt.replace(/placeholder="Street mugavari, locality"/g, "placeholder={t('streetAddressPlaceholder')}");
vt = vt.replace(/>Country</g, ">{t('country')}<");
vt = vt.replace(/>oor</g, ">{t('city')}<");
vt = vt.replace(/placeholder="e\.g\. Mumbai"/g, "placeholder={t('cityPlaceholder')}");
vt = vt.replace(/>\{cc\.postalLabel\}</g, ">{t(cc.postalLabel as any, { defaultValue: cc.postalLabel })}<");
vt = vt.replace(/>\{cc\.stateLabel\}</g, ">{t(cc.stateLabel as any, { defaultValue: cc.stateLabel })}<");
vt = vt.replace(/Select \{cc\.stateLabel\}/g, "{t('selectLabel')} {t(cc.stateLabel as any, { defaultValue: cc.stateLabel })}");
vt = vt.replace(/>\{cc\.taxIdLabel\}</g, ">{t(cc.taxIdLabel as any, { defaultValue: cc.taxIdLabel })}<");
vt = vt.replace(/>Email</g, ">{t('emailLabel')}<");
vt = vt.replace(/placeholder="client@example\.com"/g, "placeholder={t('emailPlaceholder')}");
vt = vt.replace(/>tholaipesi</g, ">{t('phoneLabel')}<");
vt = vt.replace(/placeholder="\+91 98765 43210"/g, "placeholder={t('phonePlaceholder')}");
vt = vt.replace(/>Cancel</g, ">{t('cancelModalBtn')}<");
vt = vt.replace(/\{isEditing \? 'Update Client' : 'Save Client'\}/g, "{isEditing ? t('updateClientModalBtn') : t('saveClientModalBtn')}");
// Also replace the SEZ text
vt = vt.replace(/>SEZ unit \/ Developer — supplies will be charged IGST regardless of maanilam \(Section 16, IGST Act\)\.</g, ">{t('sezUnitTitle')}<");

fs.writeFileSync('moolam/pagudhigal/VanigarThirai.tsx', vt, 'utf-8');
