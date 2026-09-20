const fs = require('fs');

let code = fs.readFileSync('D:/Projects/Elvan Niril/moolam/Seyali.tsx', 'utf8');

// 1. Add back framer-motion import
code = code.replace(
  "import { useNavigate, useLocation, useNavigationType } from 'react-router-dom';",
  "import { useNavigate, useLocation, useNavigationType } from 'react-router-dom';\nimport { motion, AnimatePresence } from 'framer-motion';"
);

// 2. Replace all CSS class overlays back to Framer Motion
code = code.replace(
  /className=\{isMobile \? 'mobile-overlay-slide-in' : undefined\}/g,
  `component={motion.div}
            initial={isMobile ? { x: '100%' } : false}
            animate={isMobile ? { x: 0 } : false}
            exit={isMobile ? { x: '100%' } : false}
            transition={isMobile ? { type: 'spring', stiffness: 400, damping: 40, mass: 0.8 } : undefined}`
);

// 3. Wrap each overlay block with AnimatePresence
// Invoice editor
code = code.replace(
  `{/* Render Editor on top as a modal inner shell */}\n{currentView === 'invoice-editor'`,
  `{/* Render Editor on top as a modal inner shell */}\n<AnimatePresence>\n{currentView === 'invoice-editor'`
);
// Find closing of invoice editor overlay and add </AnimatePresence>
code = code.replace(
  `</Box>\n        )}\n\n        {/* Inline overlay for Add Client`,
  `</Box>\n        )}\n        </AnimatePresence>\n\n        {/* Inline overlay for Add Client`
);

// Inline overlay
code = code.replace(
  `{currentView === 'invoice-editor' && inlineOverlay`,
  `<AnimatePresence>\n        {currentView === 'invoice-editor' && inlineOverlay`
);
// Close inline overlay AnimatePresence - find next section
code = code.replace(
  `</Box>\n        )}\n\n        {/* Render View on top`,
  `</Box>\n        )}\n        </AnimatePresence>\n\n        {/* Render View on top`
);

// Invoice view
code = code.replace(
  `{/* Render View on top as a modal inner shell */}\n{currentView === 'invoice-view'`,
  `{/* Render View on top as a modal inner shell */}\n<AnimatePresence>\n{currentView === 'invoice-view'`
);
code = code.replace(
  `</Box>\n        )}\n\n        {/* Client editor overlay`,
  `</Box>\n        )}\n        </AnimatePresence>\n\n        {/* Client editor overlay`
);

// Client editor
code = code.replace(
  `{/* Client editor overlay */}\n{currentView === 'client-editor'`,
  `{/* Client editor overlay */}\n<AnimatePresence>\n{currentView === 'client-editor'`
);
code = code.replace(
  `</Box>\n        )}\n\n        {/* Product editor overlay`,
  `</Box>\n        )}\n        </AnimatePresence>\n\n        {/* Product editor overlay`
);

// Product editor  
code = code.replace(
  `{/* Product editor overlay */}\n{currentView === 'product-editor'`,
  `{/* Product editor overlay */}\n<AnimatePresence>\n{currentView === 'product-editor'`
);
code = code.replace(
  `</Box>\n        )}\n\n        {/* Receipt editor overlay`,
  `</Box>\n        )}\n        </AnimatePresence>\n\n        {/* Receipt editor overlay`
);

// Receipt editor
code = code.replace(
  `{/* Receipt editor overlay */}\n{currentView === 'receipt-editor'`,
  `{/* Receipt editor overlay */}\n<AnimatePresence>\n{currentView === 'receipt-editor'`
);
// Receipt editor closes before receipt view
code = code.replace(
  `</Box>\n        )}\n{currentView === 'receipt-view'`,
  `</Box>\n        )}\n        </AnimatePresence>\n<AnimatePresence>\n{currentView === 'receipt-view'`
);

// Receipt view - last overlay before </Box> closing the shell
code = code.replace(
  `</Box>\n        )}\n        </Box>`,
  `</Box>\n        )}\n        </AnimatePresence>\n        </Box>`
);

fs.writeFileSync('D:/Projects/Elvan Niril/moolam/Seyali.tsx', code);
console.log("Done - restored Framer Motion with tuned spring physics");
