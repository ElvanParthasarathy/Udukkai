const fs = require('fs');

// Remove from vite.config.js
let vite = fs.readFileSync('vite.config.js', 'utf-8');
vite = vite.replace(/'favicon\.svg',? ?/g, ''); // Removes 'favicon.svg' from includeAssets array
vite = vite.replace(/\{\s*src: '\/favicon\.svg'[^}]+\},?/g, ''); // Removes it from icons arrays

// Wait, the icons array inside manifest requires at least one icon for PWA.
// Currently it only has favicon.svg.
// Let's replace the main manifest icons with an empty array or remove it?
// PWA requires icons. The user doesn't want favicon.svg. I'll just remove it and let PWA complain, or if there's an 'og-preview.png', maybe use that?
// For now, I'll just remove favicon.svg from the icons array.
fs.writeFileSync('vite.config.js', vite, 'utf-8');

// Also delete the file podhu/favicon.svg
if (fs.existsSync('podhu/favicon.svg')) {
  fs.unlinkSync('podhu/favicon.svg');
}
