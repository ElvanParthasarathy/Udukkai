const fs = require('fs');

function updateDict(path, isTamil) {
  let content = fs.readFileSync(path, 'utf-8');
  
  const additions = isTamil ? `
  // Recurring Invoices Page
  recurringInvoicesSubtitle: 'நிலையான வாடிக்கையாளர்களுக்கான பட்டியல்களை தானியங்கியாக உருவாக்குங்கள்',
  newTemplateBtn: 'புதிய வார்ப்புரு',
  dueForGeneration: 'உருவாக்குவதற்கான பட்டியல்கள்',
  editTemplateTitle: 'வார்ப்புருவைத் திருத்து',
  newRecurringInvoiceTitle: 'புதிய தொடர் பட்டியல்',
  quickSelectClient: 'வாடிக்கையாளரைத் விரைவாகத் தேர்ந்தெடு',
  frequencyLabel: 'அதிர்வெண் (Frequency)',
  nextDueDateLabel: 'அடுத்த தேதி',
  invoiceTypeLabel: 'பட்டியல் வகை',
  lineItemsTitle: 'பொருட்கள்',
  recurringTemplatesTable: 'தொடர் வார்ப்புருக்கள்',
  noRecurringYet: 'இன்னும் தொடர் பட்டியல்கள் அமைக்கப்படவில்லை.',
  createTemplateBtn: 'வார்ப்புருவை உருவாக்கு',
  frequencyCol: 'அதிர்வெண்',
  typeCol: 'வகை',
  nextDueCol: 'அடுத்த தேதி',
  addAtLeastOneItemToast: 'விவரம் மற்றும் விலையுடன் குறைந்தது ஒரு பொருளையாவது சேர்க்கவும்',
  templateUpdatedToast: 'வார்ப்புரு புதுப்பிக்கப்பட்டது',
  recurringCreatedToast: 'தொடர் பட்டியல் உருவாக்கப்பட்டது',
  deleteTemplateConfirmMsg: 'இந்தத் தொடர் பட்டியல் வார்ப்புருவை நீக்க வேண்டுமா?',
  pausedToast: 'இடைநிறுத்தப்பட்டது',
  activatedToast: 'செயல்படுத்தப்பட்டது',
  failedToGenerateToast: 'உருவாக்க முடியவில்லை:',
  invoiceGeneratedFor: 'இவருக்காக பட்டியல் உருவாக்கப்பட்டது:',
  freqWeekly: 'வாரம்',
  freqMonthly: 'மாதம்',
  freqQuarterly: 'காலாண்டு',
  freqYearly: 'ஆண்டு',
  statusActive: 'செயலில்',
  statusPaused: 'இடைநிறுத்தம்',
  generateNowTooltip: 'இப்போதே உருவாக்கு',
  pauseTooltip: 'இடைநிறுத்து',
  activateTooltip: 'செயல்படுத்து',
  ` : `
  // Recurring Invoices Page
  recurringInvoicesSubtitle: 'Auto-generate invoices for retainer clients',
  newTemplateBtn: 'New Template',
  dueForGeneration: 'due for generation',
  editTemplateTitle: 'Edit Template',
  newRecurringInvoiceTitle: 'New Recurring Invoice',
  quickSelectClient: 'Quick Select Client',
  frequencyLabel: 'Frequency',
  nextDueDateLabel: 'Next Due Date',
  invoiceTypeLabel: 'Invoice Type',
  lineItemsTitle: 'Line Items',
  recurringTemplatesTable: 'Recurring Templates',
  noRecurringYet: 'No recurring invoices set up yet.',
  createTemplateBtn: 'Create Template',
  frequencyCol: 'Frequency',
  typeCol: 'Type',
  nextDueCol: 'Next Due',
  addAtLeastOneItemToast: 'Add at least one item with description and rate',
  templateUpdatedToast: 'Template updated',
  recurringCreatedToast: 'Recurring invoice created',
  deleteTemplateConfirmMsg: 'Delete this recurring invoice template?',
  pausedToast: 'Paused',
  activatedToast: 'Activated',
  failedToGenerateToast: 'Failed to generate:',
  invoiceGeneratedFor: 'Invoice generated for:',
  freqWeekly: 'Weekly',
  freqMonthly: 'Monthly',
  freqQuarterly: 'Quarterly',
  freqYearly: 'Yearly',
  statusActive: 'Active',
  statusPaused: 'Paused',
  generateNowTooltip: 'Generate Now',
  pauseTooltip: 'Pause',
  activateTooltip: 'Activate',
  `;

  if (!content.includes('recurringInvoicesSubtitle:')) {
    content = content.replace(/};\s*export type TranslationKey/, additions + '\n};\n\nexport type TranslationKey');
    fs.writeFileSync(path, content, 'utf-8');
  }
}

updateDict('moolam/mozhi/ta.ts', true);
updateDict('moolam/mozhi/en.ts', false);

let v = fs.readFileSync('moolam/pagudhigal/ThodarPattiyalkal.tsx', 'utf-8');
if (!v.includes('useLanguage')) {
  v = v.replace(/import \{ thagaval \} from '\.\/Thagaval';/, "import { thagaval } from './Thagaval';\nimport { useLanguage } from '../mozhi/LanguageContext';");
  v = v.replace(/export default function ThodarPattiyalkal\(\) \{/, "$&\n  const { t } = useLanguage();");
}

v = v.replace(/'Failed to load data'/g, "t('failedToLoadProductsToast')"); // Reuse
v = v.replace(/'Client name required'/g, "t('clientNameRequiredToast')");
v = v.replace(/'Add at least one item with description and rate'/g, "t('addAtLeastOneItemToast')");
v = v.replace(/'Template updated'/g, "t('templateUpdatedToast')");
v = v.replace(/'Recurring invoice created'/g, "t('recurringCreatedToast')");
v = v.replace(/'Failed to save'/g, "t('failedToSaveExpenseToast')");
v = v.replace(/'Delete this recurring invoice template\?'/g, "t('deleteTemplateConfirmMsg')");
v = v.replace(/'Deleted'/g, "t('deletedToast')");
v = v.replace(/'Failed to delete'/g, "t('failedToDelete')");
v = v.replace(/'Paused'/g, "t('pausedToast')");
v = v.replace(/'Activated'/g, "t('activatedToast')");
v = v.replace(/'Failed to generate: ' \+ err.message/g, "t('failedToGenerateToast') + ' ' + err.message");
v = v.replace(/`Invoice \$\{invoiceNumber\} generated for \$\{tpl.clientName\}`/g, "`\$\{invoiceNumber\} ${t('invoiceGeneratedFor')} ${tpl.clientName}`");

v = v.replace(/>Recurring Invoices</g, ">{t('recurringInvoices')}<");
v = v.replace(/>Auto-generate invoices for retainer clients</g, ">{t('recurringInvoicesSubtitle')}<");
v = v.replace(/<Plus size=\{18\} \/> New Template/g, "<Plus size={18} /> {t('newTemplateBtn')}");

v = v.replace(/\{dueTemplates.length\} invoice\{dueTemplates.length > 1 \? 's' : ''\} due for generation/g, "{dueTemplates.length} {t('dueForGeneration')}");

v = v.replace(/\{editingId \? 'Edit Template' : 'New Recurring Invoice'\}/g, "{editingId ? t('editTemplateTitle') : t('newRecurringInvoiceTitle')}");
v = v.replace(/>Quick Select Client</g, ">{t('quickSelectClient')}<");
v = v.replace(/>Client Name \*</g, ">{t('clientNameLabel')}<"); // Already there in ta.ts (clientNameLabel exists from Kolmudhal?) wait Kolmudhal had supplierNameLabel. clientName is clientName in ta.ts from invoices.
v = v.replace(/>Client GSTIN</g, ">{t('clientGstinLabel')}<"); // Wait I will just replace Client Name * with >{t('clientName')} *<
v = v.replace(/>Frequency</g, ">{t('frequencyLabel')}<");
v = v.replace(/>Next Due Date</g, ">{t('nextDueDateLabel')}<");
v = v.replace(/>Invoice Type</g, ">{t('invoiceTypeLabel')}<");

v = v.replace(/>Line Items</g, ">{t('lineItemsTitle')}<");
v = v.replace(/>Description</g, ">{t('descriptionCol')}<");
v = v.replace(/>Qty</g, ">{t('qtyCol')}<");
v = v.replace(/>Rate</g, ">{t('rateCol')}<");
v = v.replace(/>GST%</g, ">{t('gstPercentCol') || 'GST%'}<");

v = v.replace(/<Plus size=\{14\} \/> Add Item/g, "<Plus size={14} /> {t('addItemBtn')}");

v = v.replace(/>Cancel</g, ">{t('cancelModalBtn')}<");
v = v.replace(/\{editingId \? 'Update' : 'Save'\}/g, "{editingId ? t('updateClientModalBtn') : t('saveClientModalBtn')}");

v = v.replace(/>Recurring Templates</g, ">{t('recurringTemplatesTable')}<");
v = v.replace(/>No recurring invoices set up yet\.</g, ">{t('noRecurringYet')}<");
v = v.replace(/<Plus size=\{18\} \/> Create Template/g, "<Plus size={18} /> {t('createTemplateBtn')}");

v = v.replace(/>Client</g, ">{t('clientCol')}<");
v = v.replace(/>Type</g, ">{t('typeCol')}<");
v = v.replace(/>Amount</g, ">{t('amountCol')}<");
v = v.replace(/>Status</g, ">{t('statusCol')}<");
v = v.replace(/>Actions</g, ">{t('actionsCol')}<");

// Replace headers that aren't mapped
v = v.replace(/>Frequency<\/th>/g, ">{t('frequencyCol')}</th>");
v = v.replace(/>Next Due<\/th>/g, ">{t('nextDueCol')}</th>");

// Frequencies translations
v = v.replace(/\{ value: 'weekly', label: 'Weekly' \}/g, "{ value: 'weekly', label: t('freqWeekly') || 'Weekly' }");
v = v.replace(/\{ value: 'monthly', label: 'Monthly' \}/g, "{ value: 'monthly', label: t('freqMonthly') || 'Monthly' }");
v = v.replace(/\{ value: 'quarterly', label: 'Quarterly' \}/g, "{ value: 'quarterly', label: t('freqQuarterly') || 'Quarterly' }");
v = v.replace(/\{ value: 'yearly', label: 'Yearly' \}/g, "{ value: 'yearly', label: t('freqYearly') || 'Yearly' }");
// We need to pass t to FREQUENCIES. 
// Wait, FREQUENCIES is defined outside the component.
// We must move it inside the component or translate it dynamically.
// Let's replace the top level const with a function inside component.
v = v.replace(/const FREQUENCIES = \\[[\\s\\S]*?\\];/, "");
v = v.replace(/const emptyForm = \{/, "const emptyForm = {");

v = v.replace(/const load = async \(\) => \{/, "const FREQUENCIES = [\n    { value: 'weekly', label: t('freqWeekly') },\n    { value: 'monthly', label: t('freqMonthly') },\n    { value: 'quarterly', label: t('freqQuarterly') },\n    { value: 'yearly', label: t('freqYearly') },\n  ];\n\n  const load = async () => {");

// Status tags
v = v.replace(/tpl\.active !== false \? 'Active' : 'Paused'/g, "tpl.active !== false ? t('statusActive') : t('statusPaused')");
v = v.replace(/title="Generate Now"/g, "title={t('generateNowTooltip')}");
v = v.replace(/title=\{tpl\.active !== false \? 'Pause' : 'Activate'\}/g, "title={tpl.active !== false ? t('pauseTooltip') : t('activateTooltip')}");

fs.writeFileSync('moolam/pagudhigal/ThodarPattiyalkal.tsx', v, 'utf-8');
