const fs = require('fs');
let content = fs.readFileSync('Seyali.tsx', 'utf8');

const replacement = `
  const inlineOverlay = useMemo(() => {
    if (location.pathname.endsWith('/add-client')) return { type: 'client-editor' };
    if (location.pathname.endsWith('/add-product')) return { type: 'product-editor' };
    return null;
  }, [location.pathname]);

  const setInlineOverlay = useCallback((overlay) => {
    if (overlay === null) {
      if (location.pathname.endsWith('/add-client') || location.pathname.endsWith('/add-product')) {
        navigate(-1);
      }
    } else if (overlay.type === 'client-editor') {
      navigate(location.pathname.replace(/\\/$/, '') + '/add-client');
    } else if (overlay.type === 'product-editor') {
      navigate(location.pathname.replace(/\\/$/, '') + '/add-product');
    }
  }, [navigate, location.pathname]);
`;

content = content.replace(
  /const \[inlineOverlay, setInlineOverlay\] = useState[\s\S]*?\(null\);/,
  replacement.trim()
);

fs.writeFileSync('Seyali.tsx', content);
console.log('Replaced inlineOverlay with URL state');
