const fs = require('fs');

let content = fs.readFileSync('Seyali.tsx', 'utf8');

// 1. Add imports
if (!content.includes('import { useNavigate, useLocation }')) {
  content = content.replace("import React, { useState, useEffect, useCallback, useMemo, useRef } from 'react';", "import React, { useState, useEffect, useCallback, useMemo, useRef } from 'react';\nimport { useNavigate, useLocation } from 'react-router-dom';");
}

// 2. Replace currentView state and popstate listeners with derived state
const routerLogic = `
  const navigate = useNavigate();
  const location = useLocation();

  const currentView = useMemo(() => {
    const path = location.pathname;
    if (path.startsWith('/dashboard/invoices/edit') || path.startsWith('/dashboard/invoices/new')) return 'invoice-editor';
    if (path.startsWith('/dashboard/invoices/view')) return 'invoice-view';
    if (path.startsWith('/dashboard/invoices')) return 'invoice-list';
    if (path.startsWith('/dashboard/clients/edit') || path.startsWith('/dashboard/clients/new')) return 'client-editor';
    if (path.startsWith('/dashboard/clients')) return 'clients';
    if (path.startsWith('/dashboard/inventory/edit') || path.startsWith('/dashboard/inventory/new')) return 'product-editor';
    if (path.startsWith('/dashboard/inventory')) return 'inventory';
    if (path.startsWith('/dashboard/receipts/edit') || path.startsWith('/dashboard/receipts/new')) return 'receipt-editor';
    if (path.startsWith('/dashboard/receipts/view')) return 'receipt-view';
    if (path.startsWith('/dashboard/receipts')) return 'receipts';
    if (path.startsWith('/dashboard/reports')) return 'reports';
    if (path.startsWith('/dashboard/gst-returns')) return 'gst-returns';
    if (path.startsWith('/dashboard/settings')) return 'settings';
    return 'dashboard';
  }, [location.pathname]);

  const setCurrentView = useCallback((v: string, replace = false) => {
    let path = '/dashboard';
    if (v === 'invoice-list') path = '/dashboard/invoices';
    else if (v === 'invoice-editor') path = '/dashboard/invoices/edit';
    else if (v === 'invoice-view') path = '/dashboard/invoices/view';
    else if (v === 'clients') path = '/dashboard/clients';
    else if (v === 'client-editor') path = '/dashboard/clients/edit';
    else if (v === 'inventory') path = '/dashboard/inventory';
    else if (v === 'product-editor') path = '/dashboard/inventory/edit';
    else if (v === 'receipts') path = '/dashboard/receipts';
    else if (v === 'receipt-editor') path = '/dashboard/receipts/edit';
    else if (v === 'receipt-view') path = '/dashboard/receipts/view';
    else if (v === 'reports') path = '/dashboard/reports';
    else if (v === 'gst-returns') path = '/dashboard/gst-returns';
    else if (v === 'settings') path = '/dashboard/settings';
    
    // Smart history management for top-level tabs
    const isTopLevelTab = ['invoice-list', 'clients', 'inventory', 'receipts', 'reports', 'gst-returns', 'settings'].includes(v);
    const isCurrentlyDashboard = location.pathname === '/dashboard' || location.pathname === '/';
    
    const shouldReplace = isTopLevelTab && isCurrentlyDashboard ? false : replace;

    navigate(path, { replace: shouldReplace, state: location.state });
  }, [navigate, location.pathname, location.state]);

  const handleBack = useCallback((fallbackView: string) => {
    if (window.history.state && window.history.state.idx > 0) {
      navigate(-1);
    } else {
      setCurrentView(fallbackView, true);
    }
  }, [navigate, setCurrentView]);

  useEffect(() => {
    const params = new URLSearchParams(location.search);
    const viewParam = params.get('view');
    if (viewParam) {
      setCurrentView(viewParam === 'new' ? 'invoice-editor' : viewParam, true);
    }
  }, [location.search, setCurrentView]);
`;

const stateRegex = /const \[currentView, setCurrentView\] = useState\(\(\) => \{[\s\S]*?\}\);[\s\S]*?window\.history\.pushState\(\{ view: currentView \}, '', newUrl\);\s*\}\s*\}, \[currentView\]\);/g;

if (stateRegex.test(content)) {
  content = content.replace(stateRegex, routerLogic.trim());
}

fs.writeFileSync('Seyali.tsx', content);
console.log('Restored React Router routing logic!');
