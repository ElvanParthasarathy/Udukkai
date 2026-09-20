const fs = require('fs');
let content = fs.readFileSync('Seyali.tsx', 'utf8');

// Clients List
content = content.replace(
  /onAddClient=\{\(\) => \{ setEditingClient\(null\); setCurrentView\('client-editor'\); \}\}/g,
  "onAddClient={() => { setEditingClient(null); navigate('/dashboard/clients/new'); }}"
);

content = content.replace(
  /onEditClient=\{\(c\) => \{ setEditingClient\(c\); setCurrentView\('client-editor'\); \}\}/g,
  "onEditClient={(c) => { setEditingClient(c); navigate(`/dashboard/clients/edit/${c.id || 'draft'}`); }}"
);

// Client Editor Back/Saved
content = content.replace(
  /onBack=\{\(\) => \{ setEditingClient\(null\); setCurrentView\('clients'\); \}\}/g,
  "onBack={() => navigate(-1)}"
);

content = content.replace(
  /onSaved=\{\(c\) => \{ setEditingClient\(null\); setCurrentView\('clients'\); setRefreshKey\(k => k \+ 1\); \}\}/g,
  "onSaved={() => { setEditingClient(null); navigate('/dashboard/clients', { replace: true }); setRefreshKey(k => k + 1); }}"
);

// Inventory List
content = content.replace(
  /onAddProduct=\{\(\) => \{ setEditingProduct\(null\); setCurrentView\('product-editor'\); \}\}/g,
  "onAddProduct={() => { setEditingProduct(null); navigate('/dashboard/inventory/new'); }}"
);

content = content.replace(
  /onEditProduct=\{\(p\) => \{ setEditingProduct\(p\); setCurrentView\('product-editor'\); \}\}/g,
  "onEditProduct={(p) => { setEditingProduct(p); navigate(`/dashboard/inventory/edit/${p.id || 'draft'}`); }}"
);

// Product Editor Back/Saved
content = content.replace(
  /onBack=\{\(\) => \{ setEditingProduct\(null\); setCurrentView\('inventory'\); \}\}/g,
  "onBack={() => navigate(-1)}"
);

content = content.replace(
  /onSaved=\{\(p\) => \{ setEditingProduct\(null\); setCurrentView\('inventory'\); setRefreshKey\(k => k \+ 1\); \}\}/g,
  "onSaved={() => { setEditingProduct(null); navigate('/dashboard/inventory', { replace: true }); setRefreshKey(k => k + 1); }}"
);

// Floating Action Button Quick Add Logic
content = content.replace(
  /setEditingClient\(null\);\s*setCurrentView\('client-editor'\);/g,
  "setEditingClient(null);\n                navigate('/dashboard/clients/new');"
);

content = content.replace(
  /setEditingProduct\(null\);\s*setCurrentView\('product-editor'\);/g,
  "setEditingProduct(null);\n                navigate('/dashboard/inventory/new');"
);

fs.writeFileSync('Seyali.tsx', content);
console.log('Fixed clients and inventory routing!');
