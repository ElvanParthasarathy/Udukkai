const fs = require('fs');
let content = fs.readFileSync('Seyali.tsx', 'utf8');

// 1. Update list handlers (onAddReceipt, onEditReceipt, onViewReceipt)
content = content.replace(
  /onAddReceipt=\{\(\) => \{ setEditingReceipt\(null\); setCurrentView\('receipt-editor'\); \}\}/g,
  "onAddReceipt={() => { setEditingReceipt(null); navigate('/dashboard/receipts/new'); }}"
);

content = content.replace(
  /onEditReceipt=\{\(rcp\) => \{ setEditingReceipt\(rcp\); setCurrentView\('receipt-editor'\); \}\}/g,
  "onEditReceipt={(rcp) => { setEditingReceipt(rcp); navigate(`/dashboard/receipts/edit/${rcp.id || 'draft'}`); }}"
);

content = content.replace(
  /onViewReceipt=\{\(rcp\) => \{ setEditingReceipt\(rcp\); setCurrentView\('receipt-view'\); \}\}/g,
  "onViewReceipt={(rcp) => { setEditingReceipt(rcp); navigate(`/dashboard/receipts/view/${rcp.id || 'draft'}`); }}"
);

// 2. Update Editor onBack
content = content.replace(
  /onBack=\{\(\) => \{ setEditingReceipt\(null\); setCurrentView\('receipts'\); \}\}/g,
  "onBack={() => navigate(-1)}"
);

// 3. Update Editor onSaved
content = content.replace(
  /onSaved=\{\(\) => \{ setEditingReceipt\(null\); setCurrentView\('receipts'\); setRefreshKey\(k => k \+ 1\); \}\}/g,
  "onSaved={() => { setEditingReceipt(null); navigate('/dashboard/receipts', { replace: true }); setRefreshKey(k => k + 1); }}"
);

// 4. Update View onEdit
content = content.replace(
  /onEdit=\{\(rcp\) => \{ setEditingReceipt\(rcp\); setCurrentView\('receipt-editor'\); \}\}/g,
  "onEdit={(rcp) => { setEditingReceipt(rcp); navigate(`/dashboard/receipts/edit/${rcp.id || 'draft'}`); }}"
);

fs.writeFileSync('Seyali.tsx', content);
console.log('Fixed receipt routing!');
