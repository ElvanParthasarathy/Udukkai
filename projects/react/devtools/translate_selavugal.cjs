const fs = require('fs');

function updateDict(path, isTamil) {
  let content = fs.readFileSync(path, 'utf-8');
  
  const additions = isTamil ? `
  // Expenses Page
  expensesSubtitle: 'P&L மற்றும் ITC கோரிக்கைகளுக்கான வணிக செலவுகளைக் கண்காணிக்கவும்',
  exportCsvBtn: 'CSV ஏற்றுமதி செய்',
  addExpenseBtn: 'செலவைச் சேர்',
  totalExpensesCard: 'மொத்த செலவுகள்',
  gstPaidCard: 'செலுத்திய GST (ITC)',
  entriesCard: 'பதிவுகள்',
  searchExpenses: 'செலவுகளைத் தேட...',
  allCategoriesFilter: 'அனைத்து பிரிவுகளும்',
  expenseRecordsTable: 'செலவு பதிவுகள்',
  noExpensesYet: 'இன்னும் செலவுகள் பதிவு செய்யப்படவில்லை.',
  noExpensesMatch: 'உங்கள் வடிகட்டல்களுக்கு எந்த செலவும் பொருந்தவில்லை.',
  editExpenseTitle: 'செலவைத் திருத்து',
  addExpenseTitle: 'செலவைச் சேர்',
  dateLabelStar: 'தேதி *',
  categoryLabel: 'பிரிவு (Category)',
  descriptionLabelStar: 'விவரம் *',
  descriptionPlaceholderExp: 'உ.ம். இணையதளப் பராமரிப்பு - ஏப்ரல்',
  amountGstLabelStar: 'தொகை (GST உட்பட) *',
  gstPercentItcLabel: 'GST % (ITC-க்காக)',
  vendorNameLabel: 'விற்பனையாளர் பெயர்',
  vendorNameOptional: 'விருப்பத்திற்குரியது',
  vendorGstinLabel: 'விற்பனையாளர் GSTIN',
  vendorGstinHint: 'ITC கோரிக்கைக்கு',
  invoiceBillNoLabel: 'பட்டியல் / பில் எண்',
  descriptionRequiredToast: 'விவரம் அவசியம்',
  enterValidAmountToastExp: 'சரியான தொகையை உள்ளிடவும்',
  expenseUpdatedToast: 'செலவு புதுப்பிக்கப்பட்டது',
  expenseAddedToast: 'செலவு சேர்க்கப்பட்டது',
  failedToSaveExpenseToast: 'செலவைச் சேமிக்க முடியவில்லை',
  deleteExpenseConfirmMsg: 'இந்தச் செலவை நீக்க வேண்டுமா?',
  expenseDeletedToast: 'செலவு நீக்கப்பட்டது',
  noExpensesToExportToast: 'ஏற்றுமதி செய்ய எந்த செலவும் இல்லை',
  expensesCsvDownloadedToast: 'செலவுகள் CSV பதிவிறக்கப்பட்டது',
  gstCol: 'GST',
  vendorCol: 'விற்பனையாளர்',
  ` : `
  // Expenses Page
  expensesSubtitle: 'Track business expenses for P&L and ITC claims',
  exportCsvBtn: 'Export CSV',
  addExpenseBtn: 'Add Expense',
  totalExpensesCard: 'Total Expenses',
  gstPaidCard: 'GST Paid (ITC)',
  entriesCard: 'Entries',
  searchExpenses: 'Search expenses...',
  allCategoriesFilter: 'All Categories',
  expenseRecordsTable: 'Expense Records',
  noExpensesYet: 'No expenses recorded yet.',
  noExpensesMatch: 'No expenses match your filters.',
  editExpenseTitle: 'Edit Expense',
  addExpenseTitle: 'Add Expense',
  dateLabelStar: 'Date *',
  categoryLabel: 'Category',
  descriptionLabelStar: 'Description *',
  descriptionPlaceholderExp: 'e.g. AWS Hosting - March',
  amountGstLabelStar: 'Amount (incl. GST) *',
  gstPercentItcLabel: 'GST % (for ITC)',
  vendorNameLabel: 'Vendor Name',
  vendorNameOptional: 'Optional',
  vendorGstinLabel: 'Vendor GSTIN',
  vendorGstinHint: 'For ITC claim',
  invoiceBillNoLabel: 'Invoice / Bill No',
  descriptionRequiredToast: 'Description is required',
  enterValidAmountToastExp: 'Enter a valid amount',
  expenseUpdatedToast: 'Expense updated',
  expenseAddedToast: 'Expense added',
  failedToSaveExpenseToast: 'Failed to save expense',
  deleteExpenseConfirmMsg: 'Delete this expense?',
  expenseDeletedToast: 'Expense deleted',
  noExpensesToExportToast: 'No expenses to export',
  expensesCsvDownloadedToast: 'Expenses CSV downloaded',
  gstCol: 'GST',
  vendorCol: 'Vendor',
  `;

  if (!content.includes('expensesSubtitle:')) {
    content = content.replace(/};\s*export type TranslationKey/, additions + '\n};\n\nexport type TranslationKey');
    fs.writeFileSync(path, content, 'utf-8');
  }
}

updateDict('moolam/mozhi/ta.ts', true);
updateDict('moolam/mozhi/en.ts', false);

let v = fs.readFileSync('moolam/pagudhigal/Selavugal.tsx', 'utf-8');
if (!v.includes('useLanguage')) {
  v = v.replace(/import \{ thagaval \} from '\.\/Thagaval';/, "import { thagaval } from './Thagaval';\nimport { useLanguage } from '../mozhi/LanguageContext';");
  v = v.replace(/export default function Selavugal\(\) \{/, "$&\n  const { t } = useLanguage();");
}

v = v.replace(/'Failed to load expenses'/g, "t('failedToLoadProductsToast')"); // Reuse
v = v.replace(/'Description is required'/g, "t('descriptionRequiredToast')");
v = v.replace(/'Enter a valid amount'/g, "t('enterValidAmountToastExp')");
v = v.replace(/'Expense updated'/g, "t('expenseUpdatedToast')");
v = v.replace(/'Expense added'/g, "t('expenseAddedToast')");
v = v.replace(/'Failed to save expense'/g, "t('failedToSaveExpenseToast')");
v = v.replace(/'Delete this expense\?'/g, "t('deleteExpenseConfirmMsg')");
v = v.replace(/'Expense deleted'/g, "t('expenseDeletedToast')");
v = v.replace(/'No expenses to export'/g, "t('noExpensesToExportToast')");
v = v.replace(/'Expenses CSV downloaded'/g, "t('expensesCsvDownloadedToast')");
v = v.replace(/'Failed to delete'/g, "t('failedToDelete')");

v = v.replace(/>Expenses</g, ">{t('expenses')}<");
v = v.replace(/>Track business expenses for P&L and ITC claims</g, ">{t('expensesSubtitle')}<");
v = v.replace(/<Download size=\{16\} \/> Export CSV/g, "<Download size={16} /> {t('exportCsvBtn')}");
v = v.replace(/<Plus size=\{18\} \/> Add Expense/g, "<Plus size={18} /> {t('addExpenseBtn')}");

v = v.replace(/>Total Expenses</g, ">{t('totalExpensesCard')}<");
v = v.replace(/>GST Paid \(ITC\)</g, ">{t('gstPaidCard')}<");
v = v.replace(/>Entries</g, ">{t('entriesCard')}<");
v = v.replace(/placeholder="Search expenses\.\.\."/g, "placeholder={t('searchExpenses')}");
v = v.replace(/>All Categories</g, ">{t('allCategoriesFilter')}<");
v = v.replace(/>Expense Records</g, ">{t('expenseRecordsTable')}<");

v = v.replace(/expenses\.length === 0 \? 'No expenses recorded yet\.' : 'No expenses match your filters\.'/g, "expenses.length === 0 ? t('noExpensesYet') : t('noExpensesMatch')");

v = v.replace(/\{editingId \? 'Edit Expense' : 'Add Expense'\}/g, "{editingId ? t('editExpenseTitle') : t('addExpenseTitle')}");
v = v.replace(/>Date \*</g, ">{t('dateLabelStar')}<");
v = v.replace(/>Category</g, ">{t('categoryLabel')}<");
v = v.replace(/>Description \*</g, ">{t('descriptionLabelStar')}<");
v = v.replace(/placeholder="e\.g\. AWS Hosting - March"/g, "placeholder={t('descriptionPlaceholderExp')}");
v = v.replace(/>Amount \(incl\. GST\) \*</g, ">{t('amountGstLabelStar')}<");
v = v.replace(/>GST % \(for ITC\)</g, ">{t('gstPercentItcLabel')}<");
v = v.replace(/>Vendor Name</g, ">{t('vendorNameLabel')}<");
v = v.replace(/placeholder="Optional"/g, "placeholder={t('vendorNameOptional')}");
v = v.replace(/>Vendor GSTIN</g, ">{t('vendorGstinLabel')}<");
v = v.replace(/placeholder="For ITC claim"/g, "placeholder={t('vendorGstinHint')}");
v = v.replace(/>Invoice \/ Bill No</g, ">{t('invoiceBillNoLabel')}<");

v = v.replace(/>Payment Mode</g, ">{t('paymentModeLabel')}<"); // Re-using from Raseedhu
v = v.replace(/>Note \(optional\)</g, ">{t('noteOptionalLabel')}<");

v = v.replace(/>Cancel</g, ">{t('cancelModalBtn')}<");
v = v.replace(/\{editingId \? 'Update' : 'Save'\}/g, "{editingId ? t('updateClientModalBtn') : t('saveClientModalBtn')}");

v = v.replace(/>Description</g, ">{t('descriptionCol')}<");
v = v.replace(/>Vendor</g, ">{t('vendorCol')}<");
v = v.replace(/>GST</g, ">{t('gstCol')}<");
v = v.replace(/>Mode</g, ">{t('modeCol')}<");
v = v.replace(/>Actions</g, ">{t('actionsCol')}<");
v = v.replace(/>Date</g, ">{t('dateLabel')}<");
v = v.replace(/>Amount</g, ">{t('amountCol')}<");
v = v.replace(/>Total</g, ">{t('totalLabel')}<");

fs.writeFileSync('moolam/pagudhigal/Selavugal.tsx', v, 'utf-8');
