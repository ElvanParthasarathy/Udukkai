const fs = require('fs');

let css = fs.readFileSync('moolam/vadivam.css', 'utf-8');
css = css.replace(/Google Sans/g, 'Elvan Sans');
fs.writeFileSync('moolam/vadivam.css', css, 'utf-8');

let seyali = fs.readFileSync('moolam/Seyali.tsx', 'utf-8');
seyali = seyali.replace(/Google Sans/g, 'Elvan Sans');
fs.writeFileSync('moolam/Seyali.tsx', seyali, 'utf-8');
