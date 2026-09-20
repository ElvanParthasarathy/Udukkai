const fs = require('fs');

let code = fs.readFileSync('D:/Projects/Elvan Niril/moolam/Seyali.tsx', 'utf8');

// 1. Remove isNewItem and add useFade
const isNewItemStr = `  const isNewItem = location.pathname.endsWith('/new');`;
const useFadeStr = `  const useFade = !['invoice-view', 'receipt-view'].includes(currentView as string);`;

code = code.replace(isNewItemStr, useFadeStr);

// 2. Replace isNewItem with useFade in all animation props
code = code.replace(/isNewItem \?/g, `useFade ?`);

fs.writeFileSync('D:/Projects/Elvan Niril/moolam/Seyali.tsx', code);
console.log("Successfully updated Framer Motion props to fade for all editors, slide for views only.");
