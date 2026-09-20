const fs = require('fs');
let code = fs.readFileSync('moolam/Seyali.tsx', 'utf8');

code = code.replace(/\{\/\* Render Editor on top as a modal inner shell \*\/\}\s*\n\s*<AnimatePresence>/, '{/* Render Editor on top as a modal inner shell */}\n        <AnimatePresence>\n        {currentView === \'invoice-editor\' && (');
code = code.replace(/\{\/\* Inline overlay for Add Client \/ Add Product from inside invoice editor \*\/\}\s*\n\s*<AnimatePresence>/, '{/* Inline overlay for Add Client / Add Product from inside invoice editor */}\n        <AnimatePresence>\n        {currentView === \'invoice-editor\' && inlineOverlay && (');
code = code.replace(/\{\/\* Render View on top as a modal inner shell \*\/\}\s*\n\s*<AnimatePresence>/, '{/* Render View on top as a modal inner shell */}\n        <AnimatePresence>\n        {currentView === \'invoice-view\' && editingBill && (');
code = code.replace(/\{\/\* Client editor overlay \*\/\}\s*\n\s*<AnimatePresence>/, '{/* Client editor overlay */}\n        <AnimatePresence>\n        {currentView === \'client-editor\' && (');
code = code.replace(/\{\/\* Product editor overlay \*\/\}\s*\n\s*<AnimatePresence>/, '{/* Product editor overlay */}\n        <AnimatePresence>\n        {currentView === \'product-editor\' && (');
code = code.replace(/\{\/\* Receipt editor overlay \*\/\}\s*\n\s*<AnimatePresence>/, '{/* Receipt editor overlay */}\n        <AnimatePresence>\n        {currentView === \'receipt-editor\' && (');
code = code.replace(/\{\/\* Receipt view overlay \*\/\}\s*\n\s*<AnimatePresence>/, '{/* Receipt view overlay */}\n        <AnimatePresence>\n        {currentView === \'receipt-view\' && editingReceipt && (');

fs.writeFileSync('moolam/Seyali.tsx', code);
console.log('Fixed conditions');
