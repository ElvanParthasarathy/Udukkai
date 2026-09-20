const fs = require('fs');

function updateDict(path, isTamil) {
  let content = fs.readFileSync(path, 'utf-8');
  
  const additions = isTamil ? `
  // Dashboard
  dashboard: 'முகப்பு',
  overviewOfInvoices: 'உங்கள் இன்வாய்ஸ்களின் சுருக்கம்',
  totalInvoiced: 'மொத்த இன்வாய்ஸ்',
  taxCollected: 'வசூலித்த வரி',
  outstanding: 'நிலுவை',
  invoicesCount: 'இன்வாய்ஸ்கள்',
  searchClientOrInvoice: 'வாடிக்கையாளர் அல்லது இன்வாய்ஸைத் தேடுக...',
  allYears: 'அனைத்து ஆண்டுகளும்',
  allTypes: 'அனைத்து வகைகளும்',
  allStatus: 'அனைத்து நிலைகளும்',
  noInvoicesYet: 'இன்னும் இன்வாய்ஸ்கள் இல்லை.',
  noInvoicesMatch: 'உங்கள் தேடலுக்குப் பொருத்தமான இன்வாய்ஸ்கள் இல்லை.',
  createInvoiceBtn: 'இன்வாய்ஸை உருவாக்கு',
  dateCol: 'தேதி',
  invoiceNoCol: 'இன்வாய்ஸ் எண்',
  typeCol: 'வகை',
  clientCol: 'வாடிக்கையாளர்',
  amountCol: 'தொகை',
  paidCol: 'செலுத்தியது',
  statusCol: 'நிலை',
  actionsCol: 'செயல்கள்',` : `
  // Dashboard
  dashboard: 'Dashboard',
  overviewOfInvoices: 'Overview of your invoices',
  totalInvoiced: 'Total Invoiced',
  taxCollected: 'Tax Collected',
  outstanding: 'Outstanding',
  invoicesCount: 'Invoices',
  searchClientOrInvoice: 'Search client or invoice...',
  allYears: 'All Years',
  allTypes: 'All Types',
  allStatus: 'All Status',
  noInvoicesYet: 'No invoices yet.',
  noInvoicesMatch: 'No invoices match your filters.',
  createInvoiceBtn: 'Create Invoice',
  dateCol: 'Date',
  invoiceNoCol: 'Invoice No.',
  typeCol: 'Type',
  clientCol: 'Client',
  amountCol: 'Amount',
  paidCol: 'Paid',
  statusCol: 'Status',
  actionsCol: 'Actions',`;

  content = content.replace(/};\s*export default/, additions + '\n};\nexport default');
  fs.writeFileSync(path, content, 'utf-8');
}

updateDict('moolam/mozhi/ta.ts', true);
updateDict('moolam/mozhi/en.ts', false);
