const fs = require('fs');

function updateFile(path, isTamil) {
  let content = fs.readFileSync(path, 'utf-8');
  
  const additions = isTamil ? `
  // Invoice Form
  billedTo: 'வாடிக்கையாளர்',
  clientName: 'வாடிக்கையாளர் பெயர்',
  billingAddress: 'பட்டியல் முகவரி',
  country: 'நாடு',
  pinCode: 'அஞ்சல் குறியீடு',
  maanilam: 'மாநிலம்',
  gstin: 'GSTIN',
  invoiceDetailsForm: 'பட்டியல் விவரங்கள்',
  invoiceNumber: 'பட்டியல் எண்',
  invoiceDate: 'பட்டியல் தேதி',
  dueDate: 'கடைசி தேதி',
  placeOfSupply: 'வழங்கும் இடம்',
  lineItems: 'பொருட்கள்',
  pricesIncludeTax: 'வரிகள் அடங்கும்',
  hsnSac: 'HSN/SAC',
  qty: 'அளவு',
  unit: 'அலகு',
  discount: 'தள்ளுபடி',
  cess: 'செஸ் %',
` : `
  // Invoice Form
  billedTo: 'Billed To',
  clientName: 'Client Name',
  billingAddress: 'Billing Address',
  country: 'Country',
  pinCode: 'PIN Code',
  maanilam: 'State',
  gstin: 'GSTIN',
  invoiceDetailsForm: 'Invoice Details',
  invoiceNumber: 'Invoice Number',
  invoiceDate: 'Invoice Date',
  dueDate: 'Due Date',
  placeOfSupply: 'Place of Supply',
  lineItems: 'Line Items',
  pricesIncludeTax: 'Prices include tax',
  hsnSac: 'HSN/SAC',
  qty: 'Qty',
  unit: 'Unit',
  discount: 'Discount',
  cess: 'Cess %',
`;

  content = content.replace(/\n};/, additions + '\n};');
  
  if (!isTamil) {
     content = content.replace("mugavari: 'mugavari'", "mugavari: 'Address'");
     content = content.replace("oor: 'oor'", "oor: 'City'");
     content = content.replace("tholaipesi: 'tholaipesi'", "tholaipesi: 'Phone'");
     content = content.replace("kilai: 'kilai'", "kilai: 'Branch'");
  }

  fs.writeFileSync(path, content, 'utf-8');
}

updateFile('moolam/mozhi/ta.ts', true);
updateFile('moolam/mozhi/en.ts', false);
console.log('Dictionaries updated!');
