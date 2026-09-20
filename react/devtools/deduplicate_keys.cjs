const fs = require('fs');

function deduplicate(path) {
  let content = fs.readFileSync(path, 'utf-8');
  const lines = content.split('\\n');
  const seenKeys = new Set();
  const newLines = [];
  
  for (let i = 0; i < lines.length; i++) {
    const line = lines[i];
    const match = line.match(/^\\s*([a-zA-Z0-9_]+)\\s*:/);
    if (match) {
      const key = match[1];
      if (seenKeys.has(key)) {
        console.log('Removing duplicate key: ' + key);
        continue;
      }
      seenKeys.add(key);
    }
    newLines.push(line);
  }
  
  fs.writeFileSync(path, newLines.join('\\n'), 'utf-8');
}

deduplicate('moolam/mozhi/ta.ts');
deduplicate('moolam/mozhi/en.ts');
