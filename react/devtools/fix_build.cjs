const fs = require('fs');

// Fix the syntax error in VariArikkaigal.tsx
let variArikkaigal = fs.readFileSync('moolam/pagudhigal/VariArikkaigal.tsx', 'utf-8');
variArikkaigal = variArikkaigal.replace(/msg: '\{t\('gstinNotSet'\)\}'/g, "msg: t('gstinNotSet')");
fs.writeFileSync('moolam/pagudhigal/VariArikkaigal.tsx', variArikkaigal, 'utf-8');

// Deduplicate ta.ts
function deduplicate(path) {
  const content = fs.readFileSync(path, 'utf-8');
  const lines = content.split('\n');
  const uniqueKeys = new Set();
  const outputLines = [];
  
  // A simple approach: we want to keep the FIRST occurrence (because earlier I appended things but maybe we want to keep the LATEST occurrence? Let's just keep the FIRST occurrence that is valid, actually, wait. If a key is duplicated, the latest one might be the intended one if I appended it. So let's iterate backwards. Or just use a map).
  
  const keyMap = new Map();
  const order = [];
  
  let inObject = false;
  
  for (let i = 0; i < lines.length; i++) {
    const line = lines[i];
    
    if (line.includes('export const ta = {') || line.includes('export const en = {') || line.includes('export const defaultLang =')) {
      inObject = true;
      outputLines.push(line);
      continue;
    }
    
    if (inObject && line.match(/^};\s*$/) || line.includes('export type TranslationKey')) {
      inObject = false;
      
      // Flush the map
      for (const k of order) {
        if (keyMap.has(k)) {
          outputLines.push(keyMap.get(k));
          keyMap.delete(k); // So we don't output twice if order has duplicates
        }
      }
      outputLines.push(line);
      continue;
    }
    
    if (inObject) {
      // Try to match a key-value pair. Example: `  keyName: 'value',`
      const match = line.match(/^\s*([a-zA-Z0-9_]+)\s*:\s*(['"`].*?['"`]),?\s*$/);
      if (match) {
        const key = match[1];
        if (!keyMap.has(key)) {
          order.push(key);
        }
        // Always overwrite with latest value
        keyMap.set(key, line);
      } else {
        // It could be a comment or blank line
        if (line.trim() !== '' && !line.trim().startsWith('//')) {
           // Not a standard key-value, maybe multi-line? Keep it
           outputLines.push(line);
        } else {
           // Keep comments and blank lines as they are
           // But wait, they aren't part of the keyMap. So they will appear BEFORE the flushed keys if we push them now.
           // To keep them in place, we can store them in keyMap with a special key
           const specialKey = '___line_' + i;
           order.push(specialKey);
           keyMap.set(specialKey, line);
        }
      }
    } else {
      outputLines.push(line);
    }
  }
  
  fs.writeFileSync(path, outputLines.join('\n'), 'utf-8');
}

deduplicate('moolam/mozhi/ta.ts');
deduplicate('moolam/mozhi/en.ts');
