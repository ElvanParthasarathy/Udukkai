const fs = require('fs');
let code = fs.readFileSync('moolam/Seyali.tsx', 'utf8');

const overlays = [
  { matchStr: "{currentView === 'invoice-editor' && (" },
  { matchStr: "{currentView === 'invoice-editor' && inlineOverlay && (" },
  { matchStr: "{currentView === 'invoice-view' && editingBill && (" },
  { matchStr: "{currentView === 'client-editor' && (" },
  { matchStr: "{currentView === 'product-editor' && (" },
  { matchStr: "{currentView === 'receipt-editor' && (" },
  { matchStr: "{currentView === 'receipt-view' && editingReceipt && (" }
];

let replacedCount = 0;

overlays.forEach(overlay => {
  let searchStr = overlay.matchStr + '\n          <Box \n            \n            sx={{ \n            position: { xs: \'fixed\', md: \'absolute\' }';
  let replaceStr = '<AnimatePresence>\n        ' + overlay.matchStr + '\n          <Box \n            component={motion.div}\n            initial={{ opacity: 0 }}\n            animate={{ opacity: 1 }}\n            exit={{ opacity: 0 }}\n            transition={{ duration: 0.15, ease: \'easeOut\' }}\n            sx={{ \n            position: { xs: \'fixed\', md: \'absolute\' }';

  if (code.includes(searchStr)) {
    code = code.replace(searchStr, replaceStr);
    replacedCount++;
    
    let startIndex = code.indexOf(replaceStr);
    let closeIndex = code.indexOf(')}', startIndex + replaceStr.length);
    if (closeIndex !== -1) {
      code = code.substring(0, closeIndex + 2) + '\n        </AnimatePresence>' + code.substring(closeIndex + 2);
    }
  }
});

// Also need to make sure AnimatePresence is imported!
if (!code.includes('AnimatePresence')) {
  code = code.replace("import { motion } from 'framer-motion';", "import { motion, AnimatePresence } from 'framer-motion';");
}

fs.writeFileSync('moolam/Seyali.tsx', code);
console.log('Added AnimatePresence to ' + replacedCount + ' overlays.');
