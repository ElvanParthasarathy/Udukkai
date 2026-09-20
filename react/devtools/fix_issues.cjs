const fs = require('fs');

// Fix InputProps warning in Mugappu.tsx
let mugappu = fs.readFileSync('moolam/pagudhigal/Mugappu.tsx', 'utf-8');
mugappu = mugappu.replace(/InputProps=\{\{/g, 'slotProps={{ htmlInput: {');
mugappu = mugappu.replace(/slotProps=\{\{ htmlInput: \{\s*sx:/g, 'slotProps={{ input: { sx:'); // TextField uses slotProps={{ input: ... }} or InputProps. Wait!
// If I use slotProps={{ input: { ... } }} it's equivalent to InputProps. If I use slotProps={{ htmlInput: { ... } }} it's equivalent to inputProps.
// Wait, the easiest way to avoid "React does not recognize the InputProps prop on a DOM element" is just to rename InputProps to inputProps if it's actually applied to an HTML element. But wait, we are using MUI TextField! MUI TextField DOES NOT pass InputProps to a DOM element, it handles it. 
// WHY is it passed to a DOM element? Because `TextField` might be an html input? No, it's imported from @mui/material.
// Let's just rename `InputProps={{ sx: ... }}` to `slotProps={{ input: { sx: ... } }}` in Mugappu.tsx.
mugappu = mugappu.replace(/InputProps=\{\{\s*sx:\s*\{ borderRadius:\s*'999px'\s*\}\s*\}\}/g, "slotProps={{ input: { sx: { borderRadius: '999px' } } }}");

// Fix the one with InputAdornment
mugappu = mugappu.replace(/InputProps=\{\{([\s\S]*?)startAdornment:([\s\S]*?)\}\}/g, "slotProps={{ input: {$1startAdornment:$2} }}");

fs.writeFileSync('moolam/pagudhigal/Mugappu.tsx', mugappu, 'utf-8');

// Fix vite.config.js Workbox
let vite = fs.readFileSync('vite.config.js', 'utf-8');
vite = vite.replace(/\{\s*urlPattern:\s*\/\^https:\\\/\\\/fonts\\\.googleapis\\\.com\\\/.*?,\s*handler:\s*'CacheFirst'[\s\S]*?\},/gi, '');
vite = vite.replace(/\{\s*urlPattern:\s*\/\^https:\\\/\\\/fonts\\\.gstatic\\\.com\\\/.*?,\s*handler:\s*'CacheFirst'[\s\S]*?\},/gi, '');

fs.writeFileSync('vite.config.js', vite, 'utf-8');

// Remove favicon.svg from index.html
let indexHtml = fs.readFileSync('index.html', 'utf-8');
indexHtml = indexHtml.replace(/<link rel="icon".*favicon\.svg".*\n/g, '');
indexHtml = indexHtml.replace(/<link rel="apple-touch-icon".*favicon\.svg".*\n/g, '');
fs.writeFileSync('index.html', indexHtml, 'utf-8');
