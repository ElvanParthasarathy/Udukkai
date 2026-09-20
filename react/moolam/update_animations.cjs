const fs = require('fs');

let code = fs.readFileSync('D:/Projects/Elvan Niril/moolam/Seyali.tsx', 'utf8');

// 1. Add isNewItem variable
const isNewItemStr = `\n  const isNewItem = location.pathname.endsWith('/new');\n\n  return (`;
code = code.replace(`\n  return (`, isNewItemStr);

// 2. Replace animation props
code = code.replace(/initial=\{isMobile \? \{ x: '100%' \} : false\}/g, `initial={isMobile ? (isNewItem ? { opacity: 0 } : { x: '100%' }) : false}`);
code = code.replace(/animate=\{isMobile \? \{ x: 0 \} : false\}/g, `animate={isMobile ? (isNewItem ? { opacity: 1 } : { x: 0 }) : false}`);
code = code.replace(/exit=\{isMobile \? \{ x: '100%' \} : false\}/g, `exit={isMobile ? (isNewItem ? { opacity: 0 } : { x: '100%' }) : false}`);
code = code.replace(/transition=\{isMobile \? \{ type: 'spring', stiffness: 400, damping: 40, mass: 0\.8 \} : undefined\}/g, `transition={isMobile ? (isNewItem ? { duration: 0.15 } : { type: 'spring', stiffness: 400, damping: 40, mass: 0.8 }) : undefined}`);

fs.writeFileSync('D:/Projects/Elvan Niril/moolam/Seyali.tsx', code);
console.log("Successfully updated Framer Motion props to use fade for new items.");
