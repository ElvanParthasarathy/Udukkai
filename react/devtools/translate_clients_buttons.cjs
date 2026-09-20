const fs = require('fs');

let v = fs.readFileSync('moolam/pagudhigal/Vanigargal.tsx', 'utf-8');

v = v.replace(/<Upload size=\{16\} \/> Import CSV/g, "<Upload size={16} /> {t('importCSV')}");
v = v.replace(/<Plus size=\{18\} \/> Add Client/g, "<Plus size={18} /> {t('addClient')}");
v = v.replace(/<FileText size=\{18\} \/> New Invoice/g, "<FileText size={18} /> {t('newInvoiceBtn')}");
v = v.replace(/<Plus size=\{16\} \/> Add Your First Client/g, "<Plus size={16} /> {t('addFirstClient')}");

fs.writeFileSync('moolam/pagudhigal/Vanigargal.tsx', v, 'utf-8');
