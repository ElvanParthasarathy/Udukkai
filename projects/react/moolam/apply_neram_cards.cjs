const fs = require('fs');

// 1. Update shared.css with Dark Mode tokens for Neram styles
const cssPath = 'styles/settings/shared.css';
let css = fs.readFileSync(cssPath, 'utf8');

const tokens = `
/* Moolam Theme Overrides for Neram Settings */
:root {
   --mac-card-bg: #1c1c1e;
   --mac-card-border: 1px solid rgba(255, 255, 255, 0.05);
   --mac-selection-hover: rgba(255, 255, 255, 0.08);
   --mac-divider: rgba(255, 255, 255, 0.08);
   --mac-text: #ffffff;
   --mac-text-secondary: rgba(255, 255, 255, 0.5);
   --mac-blue: #0A84FF;
   --mac-green: #32D74B;
   --mac-purple: #BF5AF2;
   --mac-orange: #FF9F0A;
   --mac-red: #FF453A;
}

/* Ensure paper backgrounds don't override our s2-group */
.s2-group {
    background: var(--mac-card-bg) !important;
    border: var(--mac-card-border) !important;
    border-radius: 24px !important;
    overflow: hidden;
}

/* Override MUI text colors inside settings to use Neram colors if needed */
.s2-col-right * {
    /* We don't force color to allow MUI to work, but we set the base */
}
`;

if (!css.includes('--mac-card-bg: #1c1c1e')) {
    fs.writeFileSync(cssPath, tokens + '\n' + css);
}

// 2. Replace <Paper> with s2-group in Amaippugal.tsx
const tsxPath = 'pagudhigal/Amaippugal.tsx';
let tsx = fs.readFileSync(tsxPath, 'utf8');

// We want to replace <Paper ...> with <div className="s2-group" style={{ padding: 24, marginBottom: 24 }}>
// But ONLY in the renderDetailView part. Actually, there are no Papers in the Hub.
// There is one paper in the export modal, but we can just regex replace all Papers inside the content tabs.

// Let's replace:
// <Paper elevation={2} sx={{ p: 3, mb: 3, borderRadius: 2 }}>
// <Paper elevation={2} sx={{ p: 3, mb: 3, borderRadius: 2, bgcolor: 'background.paper' }}>
// <Paper elevation={0} sx={{ p: 2, bgcolor: 'primary.50', color: 'primary.900', borderRadius: 2 }}>
// with `<div className="s2-group" style={{ padding: 24, marginBottom: 24 }}>`

tsx = tsx.replace(/<Paper elevation=\{2\} sx=\{\{ p: 3, mb: 3, borderRadius: 2 \}\}>/g, '<div className="s2-group" style={{ padding: 24, marginBottom: 24 }}>');
tsx = tsx.replace(/<Paper elevation=\{2\} sx=\{\{ p: 3, mb: 3, borderRadius: 2, bgcolor: 'background\.paper' \}\}>/g, '<div className="s2-group" style={{ padding: 24, marginBottom: 24 }}>');

// Specifically handle the system update Paper
tsx = tsx.replace(/<Paper elevation=\{0\} sx=\{\{ p: 2, bgcolor: 'primary\.50', color: 'primary\.900', borderRadius: 2 \}\}>/g, '<div className="s2-group" style={{ padding: 16, backgroundColor: "rgba(10, 132, 255, 0.15)", border: "1px solid rgba(10, 132, 255, 0.3)" }}>');

// Replace closing tags
// To be safe, we will just replace </Paper> with </div> everywhere BEFORE the Export modal
const uiEnd = tsx.lastIndexOf(`{/* ----------------------- Export modal`);
let beforeModal = tsx.substring(0, uiEnd);
let afterModal = tsx.substring(uiEnd);

beforeModal = beforeModal.replace(/<\/Paper>/g, '</div>');

fs.writeFileSync(tsxPath, beforeModal + afterModal);

console.log("Updated cards to use Neram s2-group styling!");
