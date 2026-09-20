const fs = require('fs');
const path = require('path');

const files = [
  'GstSettings/index.tsx', 'GstSettings/BusinessSettings.tsx', 'GstSettings/BankSettings.tsx', 'GstSettings/InvoiceSettings.tsx', 'GstSettings/LanguageSettings.tsx',
  'Merchants/Vanigargal.tsx', 'Merchants/VanigarThirai.tsx', 'Merchants/VanigarThoguppu.tsx',
  'Items/Porul.tsx', 'Items/PorulThoguppu.tsx',
  'Receipts/Raseedhu.tsx', 'Receipts/ReceiptEditor.tsx', 'Receipts/ReceiptView.tsx',
  'Reports/Arikkaigal.tsx', 'Reports/VariArikkaigal.tsx',
  'Mugappu.tsx'
].map(f => path.join('d:/Projects/Elvan Niril/moolam/pagudhigal/GstBill', f));

files.forEach(f => {
  if (!fs.existsSync(f)) return;
  let code = fs.readFileSync(f, 'utf8');
  let newCode = code.replace(/from\s+['\"](\.\.?\/[^'\"]+)['\"]/g, (match, p1) => {
    // If original import was like '../Avanam' from pagudhigal/Porul.tsx
    // original directory: moolam/pagudhigal
    const originalDir = 'd:/Projects/Elvan Niril/moolam/pagudhigal';
    const originalTarget = path.resolve(originalDir, p1);
    
    // new file location
    const newDir = path.dirname(f);
    let newRelative = path.relative(newDir, originalTarget).replace(/\\/g, '/');
    if (!newRelative.startsWith('.')) newRelative = './' + newRelative;
    return 'from \'' + newRelative + '\'';
  });
  if(code !== newCode) {
    fs.writeFileSync(f, newCode);
    console.log('Updated imports in', f);
  }
});
