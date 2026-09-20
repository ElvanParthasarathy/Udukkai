const fs = require('fs');

let code = fs.readFileSync('D:/Projects/Elvan Niril/moolam/Seyali.tsx', 'utf8');

// 1. Add back framer-motion import
if (!code.includes("from 'framer-motion'")) {
    code = code.replace(
      "import { useNavigate, useLocation, useNavigationType } from 'react-router-dom';",
      "import { useNavigate, useLocation, useNavigationType } from 'react-router-dom';\nimport { motion, AnimatePresence } from 'framer-motion';"
    );
}

// 2. Replace all CSS class overlays back to Framer Motion
// We are looking for: className={isMobile ? 'mobile-overlay-slide-in' : undefined}
code = code.replace(
  /className=\{isMobile \? 'mobile-overlay-slide-in' : undefined\}/g,
  `component={motion.div}
            initial={isMobile ? { x: '100%' } : false}
            animate={isMobile ? { x: 0 } : false}
            exit={isMobile ? { x: '100%' } : false}
            transition={isMobile ? { type: 'spring', stiffness: 400, damping: 40, mass: 0.8 } : undefined}`
);

// 3. Add <AnimatePresence> wrappers.
// We will wrap the `{currentView === 'X' && (...)}` block inside <AnimatePresence>.
// We need to be careful with regex replacement to correctly match the end of the block.
// Instead of replacing blindly, we will do it view by view.

const viewsToWrap = [
    "'invoice-editor'",
    "'invoice-editor' && inlineOverlay",
    "'invoice-view'",
    "'client-editor'",
    "'product-editor'",
    "'receipt-editor'",
    "'receipt-view'"
];

for (const view of viewsToWrap) {
    // Find the opening tag: {currentView === view && (
    // Find the matching closing tag: )}
    const searchString = `{currentView === ${view} && (`;
    let startIndex = 0;
    while ((startIndex = code.indexOf(searchString, startIndex)) !== -1) {
        // If it's already wrapped by AnimatePresence, skip
        const textBefore = code.substring(Math.max(0, startIndex - 20), startIndex);
        if (textBefore.includes('<AnimatePresence>')) {
            startIndex += searchString.length;
            continue;
        }

        let openBrackets = 1; // for the opening (
        let i = startIndex + searchString.length;
        let foundEnd = false;
        while (i < code.length) {
            if (code[i] === '(') openBrackets++;
            else if (code[i] === ')') openBrackets--;
            
            if (openBrackets === 0) {
                // We found the closing )
                // Now look for the closing }
                let j = i + 1;
                while (j < code.length && (code[j] === ' ' || code[j] === '\n' || code[j] === '\r')) {
                    j++;
                }
                if (code[j] === '}') {
                    // Wrap the block
                    const blockStart = startIndex;
                    const blockEnd = j + 1;
                    const blockContent = code.substring(blockStart, blockEnd);
                    const newContent = `<AnimatePresence>\n        ${blockContent}\n        </AnimatePresence>`;
                    code = code.substring(0, blockStart) + newContent + code.substring(blockEnd);
                    startIndex = blockStart + newContent.length;
                    foundEnd = true;
                    break;
                }
            }
            i++;
        }
        if (!foundEnd) {
             startIndex += searchString.length;
        }
    }
}

fs.writeFileSync('D:/Projects/Elvan Niril/moolam/Seyali.tsx', code);
console.log("Done - restored Framer Motion properly");
