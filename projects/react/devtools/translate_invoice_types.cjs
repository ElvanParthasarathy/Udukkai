const fs = require('fs');

function updateDict(path, isTamil) {
  let content = fs.readFileSync(path, 'utf-8');
  
  const additions = isTamil ? `
  // New Invoice Additional
  billedTo: 'வாடிக்கையாளர்',
  clientName: 'வாடிக்கையாளர் பெயர்',
  billingAddress: 'பட்டியல் முகவரி',
  country: 'நாடு',
  city: 'நகரம்',
  pinCode: 'அஞ்சல் குறியீடு',
  invoiceTypes_tax_invoice: 'வரி பட்டியல்',
  invoiceTypes_proforma: 'Proforma / Estimate',
  invoiceTypes_bill_of_supply: 'Bill of Supply (No GST)',
  invoiceTypes_composition: 'Composition (Bill of Supply)',
  invoiceTypes_credit_note: 'Credit Note',
  invoiceTypes_delivery_challan: 'Delivery Challan',
  invoiceDesc_tax_invoice: 'Standard GST tax invoice',
  invoiceDesc_proforma: 'Quotation or estimate',
  invoiceDesc_bill_of_supply: 'For unregistered or exempt goods',
  invoiceDesc_composition: 'For composition scheme dealers',
  invoiceDesc_credit_note: 'Refunds or returns',
  invoiceDesc_delivery_challan: 'Transport of goods without sale',` : `
  // New Invoice Additional
  billedTo: 'BILL TO',
  clientName: 'Client Name',
  billingAddress: 'Billing Address',
  country: 'Country',
  city: 'CITY',
  pinCode: 'PIN CODE',
  invoiceTypes_tax_invoice: 'Tax Invoice',
  invoiceTypes_proforma: 'Proforma / Estimate',
  invoiceTypes_bill_of_supply: 'Bill of Supply (No GST)',
  invoiceTypes_composition: 'Composition (Bill of Supply)',
  invoiceTypes_credit_note: 'Credit Note',
  invoiceTypes_delivery_challan: 'Delivery Challan',
  invoiceDesc_tax_invoice: 'Standard GST tax invoice',
  invoiceDesc_proforma: 'Quotation or estimate',
  invoiceDesc_bill_of_supply: 'For unregistered or exempt goods',
  invoiceDesc_composition: 'For composition scheme dealers',
  invoiceDesc_credit_note: 'Refunds or returns',
  invoiceDesc_delivery_challan: 'Transport of goods without sale',`;

  if (!content.includes('invoiceTypes_tax_invoice')) {
    content = content.replace(/};\s*export type TranslationKey/, additions + '\n};\n\nexport type TranslationKey');
    fs.writeFileSync(path, content, 'utf-8');
  }
}

updateDict('moolam/mozhi/ta.ts', true);
updateDict('moolam/mozhi/en.ts', false);

// Now patch PattiyalUruvakkam.tsx again
let jsx = fs.readFileSync('moolam/pagudhigal/PattiyalUruvakkam.tsx', 'utf-8');

jsx = jsx.replace(/>{val\.label}</g, '>{t(\`invoiceTypes_\${key}\` as any, { defaultValue: val.label })}<');
jsx = jsx.replace(/>{typeConfig\?\.description}</g, '>{t(\`invoiceDesc_\${invoiceType}\` as any, { defaultValue: typeConfig?.description })}<');

fs.writeFileSync('moolam/pagudhigal/PattiyalUruvakkam.tsx', jsx, 'utf-8');
