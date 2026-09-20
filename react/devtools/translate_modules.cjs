const fs = require('fs');

function updateDict(path, isTamil) {
  let content = fs.readFileSync(path, 'utf-8');
  
  const additions = isTamil ? `
  // Module Groups
  moduleGroup_sales: 'விற்பனை & பட்டியல்கள்',
  moduleDesc_sales: 'பட்டியல் உருவாக்கம், தொடர் பட்டியல்கள், கட்டண ரசீதுகள்',
  module_invoicing: 'வரி பட்டியல்கள், உத்தேச பட்டியல்கள், கடன் குறிப்புகள்',
  module_recurring: 'தொடர் பட்டியல்கள்',
  module_receipts: 'கட்டண ரசீதுகள்',

  moduleGroup_directory: 'அடைவு (Directory)',
  moduleDesc_directory: 'வாடிக்கையாளர்கள் மற்றும் பொருள் பட்டியல்',
  module_clients: 'வாடிக்கையாளர்கள்',
  module_inventory: 'பொருட்கள் & சேவைகள் (கையிருப்பு)',

  moduleGroup_purchases: 'கொள்முதல் & செலவுகள்',
  moduleDesc_purchases: 'விற்பனையாளர் பில்கள், செலவு கண்காணிப்பு, ITC',
  module_expenses: 'செலவு கண்காணிப்பு',
  module_purchases: 'கொள்முதல் பில்கள்',
  module_gstr2b: 'GSTR-2B பொருத்தம் (ITC பொருத்துதல்)',

  moduleGroup_gst: 'GST & வரி (இந்தியா)',
  moduleDesc_gst: 'GSTR தாக்கல், e-Way Bill, TDS/TCS, HSN சுருக்கம்',
  module_gstReturns: 'GSTR-1 / GSTR-3B ஏற்றுமதி & வழிகாட்டி',
  module_ewayBill: 'E-Way Bill JSON ஏற்றுமதி',
  module_tdsTcs: 'பட்டியல்களில் TDS / TCS',

  moduleGroup_reports: 'அறிக்கைகள்',
  moduleDesc_reports: 'முகப்பு பலகை மற்றும் நிதி அறிக்கைகள்',
  module_dashboard: 'முகப்பு பலகை',
  module_reports: 'அறிக்கைகள் பார்வை',

  moduleGroup_integrations: 'ஒருங்கிணைப்புகள்',
  moduleDesc_integrations: 'கிளவுட் காப்பு & QR குறியீடுகள்',
  module_googleDrive: 'Google Drive காப்பு',
  module_upiQr: 'பட்டியல்களில் UPI QR குறியீடு',
` : `
  // Module Groups
  moduleGroup_sales: 'Sales & Invoicing',
  moduleDesc_sales: 'Invoice creation, recurring invoices, payment receipts',
  module_invoicing: 'Tax invoices, proforma, credit notes',
  module_recurring: 'Recurring invoices',
  module_receipts: 'Payment receipts',

  moduleGroup_directory: 'Directory',
  moduleDesc_directory: 'Clients and product catalog',
  module_clients: 'Clients',
  module_inventory: 'Products & Services (inventory)',

  moduleGroup_purchases: 'Purchases & Expenses',
  moduleDesc_purchases: 'Vendor bills, expense tracking, ITC',
  module_expenses: 'Expense tracker',
  module_purchases: 'Purchase bills',
  module_gstr2b: 'GSTR-2B reconciliation (purchase ITC matching)',

  moduleGroup_gst: 'GST & Tax (India)',
  moduleDesc_gst: 'GSTR returns, e-Way Bill, TDS/TCS, HSN summaries',
  module_gstReturns: 'GSTR-1 / GSTR-3B exports + filing guide',
  module_ewayBill: 'E-Way Bill JSON export',
  module_tdsTcs: 'TDS / TCS on invoices',

  moduleGroup_reports: 'Reports',
  moduleDesc_reports: 'Dashboards and financial reports',
  module_dashboard: 'Dashboard',
  module_reports: 'Reports view',

  moduleGroup_integrations: 'Integrations',
  moduleDesc_integrations: 'Cloud backup and payment QR codes',
  module_googleDrive: 'Google Drive backup',
  module_upiQr: 'UPI QR code on invoices',
`;

  content = content.replace(/\n};/, additions + '\n};');
  fs.writeFileSync(path, content, 'utf-8');
}

updateDict('moolam/mozhi/ta.ts', true);
updateDict('moolam/mozhi/en.ts', false);

let uiContent = fs.readFileSync('moolam/pagudhigal/Amaippugal.tsx', 'utf-8');

const replacements = [
  ['{group.label}', "{t(('moduleGroup_' + group.id) as any)}"],
  ['{group.description}', "{t(('moduleDesc_' + group.id) as any)}"],
  ['{mod.label}', "{t(('module_' + mod.id) as any)}"]
];

for (const [search, replace] of replacements) {
  uiContent = uiContent.split(search).join(replace);
}

fs.writeFileSync('moolam/pagudhigal/Amaippugal.tsx', uiContent, 'utf-8');
console.log('Modules translated!');
