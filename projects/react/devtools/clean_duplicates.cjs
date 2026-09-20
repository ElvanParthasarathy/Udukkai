const fs = require('fs');

function cleanFile(path) {
  let content = fs.readFileSync(path, 'utf-8');
  
  // Find the block starting from "// New Invoice Additional" to just before "// New Invoice Line Items & Extras"
  const startIdx = content.indexOf('  // New Invoice Additional');
  const endIdx = content.indexOf('  // New Invoice Line Items & Extras');
  
  if (startIdx !== -1 && endIdx !== -1) {
    const chunkToRemove = content.substring(startIdx, endIdx);
    content = content.replace(chunkToRemove, '');
    fs.writeFileSync(path, content, 'utf-8');
    console.log(`Cleaned ${path}`);
  }
}

cleanFile('moolam/mozhi/ta.ts');
cleanFile('moolam/mozhi/en.ts');
