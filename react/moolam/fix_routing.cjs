const fs = require('fs');
let content = fs.readFileSync('Seyali.tsx', 'utf8');

// 1. Update handleNewInvoice
content = content.replace(
  /setCurrentView\('invoice-editor'\);\s*\};/g,
  (match, offset) => {
    const before = content.slice(Math.max(0, offset - 150), offset);
    if (before.includes('handleNewInvoice =')) return "navigate('/dashboard/invoices/new');\n  };";
    if (before.includes('handleEditInvoice =')) return "navigate(`/dashboard/invoices/edit/${bill.id || 'draft'}`);\n  };";
    if (before.includes('handleDuplicateInvoice =')) return "navigate('/dashboard/invoices/new');\n  };";
    return match;
  }
);

// 2. Update handleViewInvoice
content = content.replace(
  /setCurrentView\('invoice-view'\);\s*\};/g,
  (match, offset) => {
    const before = content.slice(Math.max(0, offset - 150), offset);
    if (before.includes('handleViewInvoice =')) return "navigate(`/dashboard/invoices/view/${bill.id || 'draft'}`);\n  };";
    return match;
  }
);

// 3. Fix onBack in InvoiceEditor (GST)
content = content.replace(
  /onBack=\{.*?setCurrentView\('invoice-view'\);\s*\} else \{\s*setEditingBill\(null\);\s*handleBack[\s\S]*?\}\s*\}\s*\}\s*onSaved=/g,
  "onBack={() => handleBack(sessionStorage.getItem('gst_backTo') === 'dashboard' ? 'dashboard' : 'invoice-list')}\n                onSaved="
);

// 4. Fix onBack in CoolieInvoiceEditor
content = content.replace(
  /onBack=\{.*?setEditingBill\(null\);\s*handleBack[\s\S]*?\}\}\s*onSaved=/g,
  "onBack={() => handleBack(sessionStorage.getItem('gst_backTo') === 'dashboard' ? 'dashboard' : 'invoice-list')}\n                onSaved="
);

// 5. Fix onBack in InvoiceView (GST)
content = content.replace(
  /onBack=\{\(\) => \{ setEditingBill\(null\); handleBack\(sessionStorage/g,
  "onBack={() => handleBack(sessionStorage"
);

// 6. Fix onEdit in InvoiceView (GST)
content = content.replace(
  /onEdit=\{\(bill\) => \{ setEditingBill\(bill\); setCurrentView\('invoice-editor'\); \}\}/g,
  "onEdit={(bill) => { setEditingBill(bill); navigate(`/dashboard/invoices/edit/${bill.id || 'draft'}`); }}"
);

fs.writeFileSync('Seyali.tsx', content);
console.log('Fixed invoice routing!');
