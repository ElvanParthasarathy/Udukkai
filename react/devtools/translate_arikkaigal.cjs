const fs = require('fs');

function updateDict(path, isTamil) {
  let content = fs.readFileSync(path, 'utf-8');
  
  const additions = isTamil ? `
  // Reports Page
  reportsSubtitle: 'நிதி அறிக்கைகள் மற்றும் பெற வேண்டியவைகளின் பகுப்பாய்வு',
  profitAndLossTab: 'லாபம் & நஷ்டம்',
  outstandingAndAgingTab: 'நிலுவையில் உள்ளவை & பழமை',
  filterByLabel: 'வடிகட்டி',
  fiscalYearLabel: 'நிதியாண்டு (Fiscal Year)',
  monthYearLabel: 'மாதம் / ஆண்டு',
  monthLabel: 'மாதம்',
  yearLabel: 'ஆண்டு',
  currencyLabel: 'நாணயம்',
  revenueExTax: 'வருவாய் (வரி தவிர்த்து)',
  expensesExGst: 'செலவுகள் (GST தவிர்த்து)',
  netProfitLabel: 'நிகர லாபம்',
  netLossLabel: 'நிகர நஷ்டம்',
  marginLabel: 'லாப விகிதம் (Margin)',
  plStatementTitle: 'லாப நஷ்ட அறிக்கை',
  showingCurrencyInvoicesOnly: 'பட்டியல்களை மட்டுமே காட்டுகிறது',
  totalRevenueLabel: 'மொத்த வருவாய்',
  lessGstCollectedLabel: 'கழிக்க: வசூலிக்கப்பட்ட GST',
  netRevenueLabel: 'நிகர வருவாய்',
  totalExpensesLabel: 'மொத்த செலவுகள்',
  lessGstOnExpensesLabel: 'கழிக்க: செலவுகள் மீதான GST (ITC)',
  netExpensesLabel: 'நிகர செலவுகள்',
  monthlyBreakdownTitle: 'மாதாந்திர அறிக்கை',
  revenueCol: 'வருவாய்',
  expensesCol: 'செலவுகள்',
  profitLossCol: 'லாபம்/நஷ்டம்',
  totalOutstandingLabel: 'மொத்த நிலுவை',
  current0to30d: 'நடப்பு (0-30 நாட்கள்)',
  days31to60: '31-60 நாட்கள்',
  days61to90: '61-90 நாட்கள்',
  days90plus: '90+ நாட்கள்',
  filterByClientName: 'வாடிக்கையாளர் பெயர் மூலம் வடிகட்டவும்...',
  outstandingReceivablesTitle: 'நிலுவையில் உள்ள பெற வேண்டியவைகள்',
  noOutstandingFound: 'நிலுவையில் உள்ள பெற வேண்டியவைகள் எதுவும் காணப்படவில்லை.',
  dueDateCol: 'கெடு தேதி',
  paidCol: 'செலுத்தப்பட்டது',
  outstandingCol: 'நிலுவை',
  daysOverdueCol: 'காலதாமதமான நாட்கள்',
  currentStatus: 'நடப்பு',
  january: 'ஜனவரி',
  february: 'பிப்ரவரி',
  march: 'மார்ச்',
  april: 'ஏப்ரல்',
  may: 'மே',
  june: 'ஜூன்',
  july: 'ஜூலை',
  august: 'ஆகஸ்ட்',
  september: 'செப்டம்பர்',
  october: 'அக்டோபர்',
  november: 'நவம்பர்',
  december: 'டிசம்பர்',
  ` : `
  // Reports Page
  reportsSubtitle: 'Financial reports and receivables analysis',
  profitAndLossTab: 'Profit & Loss',
  outstandingAndAgingTab: 'Outstanding & Aging',
  filterByLabel: 'Filter By',
  fiscalYearLabel: 'Fiscal Year',
  monthYearLabel: 'Month / Year',
  monthLabel: 'Month',
  yearLabel: 'Year',
  currencyLabel: 'Currency',
  revenueExTax: 'Revenue (ex. tax)',
  expensesExGst: 'Expenses (ex. GST)',
  netProfitLabel: 'Net Profit',
  netLossLabel: 'Net Loss',
  marginLabel: 'Margin',
  plStatementTitle: 'Profit & Loss Statement',
  showingCurrencyInvoicesOnly: 'invoices only',
  totalRevenueLabel: 'Total Revenue',
  lessGstCollectedLabel: 'Less: GST Collected',
  netRevenueLabel: 'Net Revenue',
  totalExpensesLabel: 'Total Expenses',
  lessGstOnExpensesLabel: 'Less: GST on Expenses (ITC)',
  netExpensesLabel: 'Net Expenses',
  monthlyBreakdownTitle: 'Monthly Breakdown',
  revenueCol: 'Revenue',
  expensesCol: 'Expenses',
  profitLossCol: 'Profit/Loss',
  totalOutstandingLabel: 'Total Outstanding',
  current0to30d: 'Current (0-30d)',
  days31to60: '31-60 days',
  days61to90: '61-90 days',
  days90plus: '90+ days',
  filterByClientName: 'Filter by client name...',
  outstandingReceivablesTitle: 'Outstanding Receivables',
  noOutstandingFound: 'No outstanding receivables found.',
  dueDateCol: 'Due Date',
  paidCol: 'Paid',
  outstandingCol: 'Outstanding',
  daysOverdueCol: 'Days Overdue',
  currentStatus: 'Current',
  january: 'January',
  february: 'February',
  march: 'March',
  april: 'April',
  may: 'May',
  june: 'June',
  july: 'July',
  august: 'August',
  september: 'September',
  october: 'October',
  november: 'November',
  december: 'December',
  `;

  if (!content.includes('reportsSubtitle:')) {
    content = content.replace(/};\s*export type TranslationKey/, additions + '\n};\n\nexport type TranslationKey');
    fs.writeFileSync(path, content, 'utf-8');
  }
}

updateDict('moolam/mozhi/ta.ts', true);
updateDict('moolam/mozhi/en.ts', false);

let v = fs.readFileSync('moolam/pagudhigal/Arikkaigal.tsx', 'utf-8');
if (!v.includes('useLanguage')) {
  v = v.replace(/import \{ thagaval \} from '\.\/Thagaval';/, "import { thagaval } from './Thagaval';\nimport { useLanguage } from '../mozhi/LanguageContext';");
}

v = v.replace(/const MONTHS = \[[^\]]+\];/, ""); // Will dynamically replace below

if (!v.includes('const { t } = useLanguage();')) {
  v = v.replace(/export default function Arikkaigal\(\) \{/, "$&\n  const { t } = useLanguage();\n  const MONTHS = [t('january'), t('february'), t('march'), t('april'), t('may'), t('june'), t('july'), t('august'), t('september'), t('october'), t('november'), t('december')];\n");
}

v = v.replace(/'Failed to load data'/g, "t('failedToLoadProductsToast')"); // Reuse

v = v.replace(/>Reports</g, ">{t('reports')}<");
v = v.replace(/>Financial reports and receivables analysis</g, ">{t('reportsSubtitle')}<");
v = v.replace(/<BarChart3 size=\{16\} \/> Profit & Loss/g, "<BarChart3 size={16} /> {t('profitAndLossTab')}");
v = v.replace(/<Clock size=\{16\} \/> Outstanding & Aging/g, "<Clock size={16} /> {t('outstandingAndAgingTab')}");

v = v.replace(/>Filter By</g, ">{t('filterByLabel')}<");
v = v.replace(/>Fiscal Year<\/option>/g, ">{t('fiscalYearLabel')}</option>");
v = v.replace(/>Month \/ Year<\/option>/g, ">{t('monthYearLabel')}</option>");
v = v.replace(/>Fiscal Year<\/label>/g, ">{t('fiscalYearLabel')}</label>");
v = v.replace(/>Month<\/label>/g, ">{t('monthLabel')}</label>");
v = v.replace(/>Year<\/label>/g, ">{t('yearLabel')}</label>");
v = v.replace(/>Currency<\/label>/g, ">{t('currencyLabel')}</label>");

v = v.replace(/>Revenue \(ex\. tax\)</g, ">{t('revenueExTax')}<");
v = v.replace(/>Expenses \(ex\. GST\)</g, ">{t('expensesExGst')}<");
v = v.replace(/Net \{netProfit >= 0 \? 'Profit' : 'Loss'\}/g, "{netProfit >= 0 ? t('netProfitLabel') : t('netLossLabel')}");
v = v.replace(/>Margin</g, ">{t('marginLabel')}<");

v = v.replace(/>Profit & Loss Statement</g, ">{t('plStatementTitle')}<");
v = v.replace(/Showing \{currencyFilter\} invoices only/g, "Showing {currencyFilter} {t('showingCurrencyInvoicesOnly')}");

v = v.replace(/>Total Revenue</g, ">{t('totalRevenueLabel')}<");
v = v.replace(/>Less: GST Collected</g, ">{t('lessGstCollectedLabel')}<");
v = v.replace(/>Net Revenue</g, ">{t('netRevenueLabel')}<");
v = v.replace(/>Total Expenses</g, ">{t('totalExpensesLabel')}<");
v = v.replace(/>Less: GST on Expenses \(ITC\)</g, ">{t('lessGstOnExpensesLabel')}<");
v = v.replace(/>Net Expenses</g, ">{t('netExpensesLabel')}<");

v = v.replace(/>Monthly Breakdown</g, ">{t('monthlyBreakdownTitle')}<");
v = v.replace(/>Month<\/th>/g, ">{t('monthLabel')}</th>");
v = v.replace(/>Revenue<\/th>/g, ">{t('revenueCol')}</th>");
v = v.replace(/>Expenses<\/th>/g, ">{t('expensesCol')}</th>");
v = v.replace(/>Profit\/Loss<\/th>/g, ">{t('profitLossCol')}</th>");

v = v.replace(/>Total Outstanding</g, ">{t('totalOutstandingLabel')}<");
v = v.replace(/>Current \(0-30d\)</g, ">{t('current0to30d')}<");
v = v.replace(/>31-60 days</g, ">{t('days31to60')}<");
v = v.replace(/>61-90 days</g, ">{t('days61to90')}<");
v = v.replace(/>90\+ days</g, ">{t('days90plus')}<");
v = v.replace(/'Total Outstanding', 'Current \(0-30d\)', '31-60 days', '61-90 days', '90\+ days'/g, "t('totalOutstandingLabel'), t('current0to30d'), t('days31to60'), t('days61to90'), t('days90plus')");

v = v.replace(/placeholder="Filter by client name\.\.\."/g, "placeholder={t('filterByClientName')}");

v = v.replace(/>Outstanding Receivables</g, ">{t('outstandingReceivablesTitle')}<");
v = v.replace(/>No outstanding receivables found\.</g, ">{t('noOutstandingFound')}<");

v = v.replace(/>Client</g, ">{t('clientCol')}<");
v = v.replace(/>Invoice No</g, ">{t('invoiceNoCol') || 'Invoice No'}<");
v = v.replace(/>Date</g, ">{t('dateLabel')}<");
v = v.replace(/>Due Date</g, ">{t('dueDateCol')}<");
v = v.replace(/>Amount</g, ">{t('amountCol')}<");
v = v.replace(/>Paid</g, ">{t('paidCol')}<");
v = v.replace(/>Outstanding</g, ">{t('outstandingCol')}<");
v = v.replace(/>Days Overdue</g, ">{t('daysOverdueCol')}<");

v = v.replace(/\{r\.daysOverdue === 0 \? 'Current' : `\$\{r\.daysOverdue\}d`\}/g, "{r.daysOverdue === 0 ? t('currentStatus') : `${r.daysOverdue}d`}");

fs.writeFileSync('moolam/pagudhigal/Arikkaigal.tsx', v, 'utf-8');
