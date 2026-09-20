const fs = require('fs');
let mugappu = fs.readFileSync('moolam/pagudhigal/Mugappu.tsx', 'utf-8');
mugappu = mugappu.replace(/slotProps=\{\{ input: \{ sx: \{ borderRadius: '999px' \} \}\}/g, "slotProps={{ htmlInput: { sx: { borderRadius: '999px' } } }}");
fs.writeFileSync('moolam/pagudhigal/Mugappu.tsx', mugappu, 'utf-8');
