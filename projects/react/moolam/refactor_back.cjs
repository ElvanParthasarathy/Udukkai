const fs = require('fs');
let content = fs.readFileSync('Seyali.tsx', 'utf8');

const handleBackFn = `
  const handleBack = useCallback((fallbackView: string) => {
    if (window.history.state && window.history.state.idx > 0) {
      navigate(-1);
    } else {
      setCurrentView(fallbackView, true);
    }
  }, [navigate, setCurrentView]);
`;

if (!content.includes('const handleBack = useCallback')) {
  content = content.replace('  const setCurrentView = useCallback((v: string, replace = false) => {', handleBackFn + '\n  const setCurrentView = useCallback((v: string, replace = false) => {');
}

// Replace all setCurrentView('...', true) inside onBack
content = content.replace(/setCurrentView\('([^']+)',\s*true\)/g, "handleBack('$1')");

// Replace the gst_backTo logic
content = content.replace(/setCurrentView\(sessionStorage\.getItem\('gst_backTo'\)\s*===\s*'dashboard'\s*\?\s*'dashboard'\s*:\s*'invoice-list',\s*true\)/g, "handleBack(sessionStorage.getItem('gst_backTo') === 'dashboard' ? 'dashboard' : 'invoice-list')");

fs.writeFileSync('Seyali.tsx', content);
console.log('Refactored Seyali.tsx back handlers');
