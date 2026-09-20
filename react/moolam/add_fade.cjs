const fs = require('fs');

let code = fs.readFileSync('D:/Projects/Elvan Niril/moolam/Seyali.tsx', 'utf8');

const targetStr = `          <Box id="main-scroll-container" className={['invoice-editor', 'invoice-view', 'receipt-editor', 'receipt-view'].includes(currentView as string) ? 'no-print' : ''} sx={{ \n            flexGrow: 1, \n            overflowY: 'scroll',\n            overflowX: 'hidden',`;

const replacementStr = `          <Box id="main-scroll-container" className={['invoice-editor', 'invoice-view', 'receipt-editor', 'receipt-view'].includes(currentView as string) ? 'no-print' : ''} sx={{ \n            flexGrow: 1, \n            overflowY: 'scroll',\n            overflowX: 'hidden',\n            opacity: isEditorView && isMobile ? 0 : 1,\n            transition: 'opacity 0.2s ease-out',\n            pointerEvents: isEditorView && isMobile ? 'none' : 'auto',`;

if (code.includes(targetStr)) {
  code = code.replace(targetStr, replacementStr);
  fs.writeFileSync('D:/Projects/Elvan Niril/moolam/Seyali.tsx', code);
  console.log("Successfully added fade to main-scroll-container.");
} else {
  console.log("Could not find target string.");
}
