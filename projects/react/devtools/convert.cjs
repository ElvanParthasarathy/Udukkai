const fs = require('fs');
const ttf2woff2 = require('ttf2woff2');

try {
  const input = fs.readFileSync('ElvanSans/fonts/ttf/ElvanSans-Variable.ttf');
  const output = ttf2woff2(input);
  fs.writeFileSync('../podhu/fonts/ElvanSans-Variable.woff2', output);
  console.log('Conversion successful');
} catch (e) {
  console.error('Conversion failed', e);
}
