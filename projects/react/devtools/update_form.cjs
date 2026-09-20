const fs = require('fs');

let content = fs.readFileSync('moolam/pagudhigal/PattiyalUruvakkam.tsx', 'utf-8');

if (!content.includes('useLanguage')) {
    content = content.replace("import { thagaval } from './Thagaval';", "import { thagaval } from './Thagaval';\nimport { useLanguage } from '../LanguageContext';");
}

if (!content.includes('const { t } = useLanguage();')) {
    content = content.replace("export default function PattiyalUruvakkam({ onBack, profile: profileProp, editingBill }) {", "export default function PattiyalUruvakkam({ onBack, profile: profileProp, editingBill }) {\n  const { t } = useLanguage();");
}

// Replacements based on the screenshot and user's complaint
const replacements = [
  ['<h3 className="section-title" style={{ margin: 0 }}>Billed To</h3>', '<h3 className="section-title" style={{ margin: 0 }}>{t(\'billedTo\')}</h3>'],
  ['<label className="form-label">Client Name</label>', '<label className="form-label">{t(\'clientName\')}</label>'],
  ['<label className="form-label">Billing mugavari</label>', '<label className="form-label">{t(\'billingAddress\')}</label>'],
  ['<label className="form-label">Country</label>', '<label className="form-label">{t(\'country\')}</label>'],
  ['<label className="form-label">oor</label>', '<label className="form-label">{t(\'city\')}</label>'],
  ['placeholder="Street mugavari, locality"', 'placeholder={t(\'billingAddress\')}'],
  ['placeholder="Postal / PIN code"', 'placeholder={t(\'pinCode\')}'],
  ['<h3 className="section-title">Invoice Details</h3>', '<h3 className="section-title">{t(\'invoiceDetailsForm\')}</h3>'],
  ['<label className="form-label">Invoice Number</label>', '<label className="form-label">{t(\'invoiceNumber\')}</label>'],
  ['<label className="form-label">Invoice Date</label>', '<label className="form-label">{t(\'invoiceDate\')}</label>'],
  ['<label className="form-label">Due Date</label>', '<label className="form-label">{t(\'dueDate\')}</label>'],
  ['<label className="form-label">Place of Supply</label>', '<label className="form-label">{t(\'placeOfSupply\')}</label>'],
  ['<h3 className="section-title" style={{ margin: 0 }}>Line Items</h3>', '<h3 className="section-title" style={{ margin: 0 }}>{t(\'lineItems\')}</h3>'],
  ['<span style={{ fontWeight: 500 }}>Prices include tax</span>', '<span style={{ fontWeight: 500 }}>{t(\'pricesIncludeTax\')}</span>'],
  ['<label className="form-label">HSN/SAC</label>', '<label className="form-label">{t(\'hsnSac\')}</label>'],
  ['<label className="form-label">Qty</label>', '<label className="form-label">{t(\'qty\')}</label>'],
  ['<label className="form-label">Unit</label>', '<label className="form-label">{t(\'unit\')}</label>'],
  ['<label className="form-label">Discount</label>', '<label className="form-label">{t(\'discount\')}</label>'],
  ['<label className="form-label" title="GST Compensation Cess (tobacco / auto / coal etc.)">Cess %</label>', '<label className="form-label" title="GST Compensation Cess (tobacco / auto / coal etc.)">{t(\'cess\')}</label>']
];

for (const [search, replace] of replacements) {
    content = content.split(search).join(replace);
}

fs.writeFileSync('moolam/pagudhigal/PattiyalUruvakkam.tsx', content, 'utf-8');
console.log('Form updated!');
