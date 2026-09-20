const fs = require('fs');
const path = require('path');

function walk(dir, callback) {
    fs.readdirSync(dir).forEach(f => {
        const dirPath = path.join(dir, f);
        const isDirectory = fs.statSync(dirPath).isDirectory();
        if (isDirectory) {
            walk(dirPath, callback);
        } else {
            callback(path.join(dir, f));
        }
    });
}

function processFile(filePath) {
    if (!filePath.endsWith('.tsx') && !filePath.endsWith('.ts')) return;
    
    let content = fs.readFileSync(filePath, 'utf8');
    let original = content;

    // Simple regex to replace '&:hover': { ... }
    // Since objects can be multi-line, we have to be careful.
    // Actually, a safer approach is to replace:
    // '&:hover': {
    // with:
    // '@media (hover: hover)': { '&:hover': {
    // AND then add an extra closing brace. But this requires AST or bracket matching.
    
    // Instead of regex, let's use a simple bracket matcher.
    let index = 0;
    while ((index = content.indexOf(`'&:hover':`, index)) !== -1) {
        // Check if it's already inside a @media query (heuristically check previous lines)
        let beforeStr = content.substring(Math.max(0, index - 50), index);
        if (beforeStr.includes('@media (hover: hover)')) {
            index += 10;
            continue;
        }

        // Find the opening brace after '&:hover':
        let openBrace = content.indexOf('{', index);
        if (openBrace === -1) {
            index += 10;
            continue;
        }

        let braceCount = 1;
        let closeBrace = openBrace + 1;
        while (braceCount > 0 && closeBrace < content.length) {
            if (content[closeBrace] === '{') braceCount++;
            else if (content[closeBrace] === '}') braceCount--;
            closeBrace++;
        }

        if (braceCount === 0) {
            // We found the matching closing brace at `closeBrace - 1`
            let before = content.substring(0, index);
            let inside = content.substring(index, closeBrace);
            let after = content.substring(closeBrace);
            
            content = before + `'@media (hover: hover)': { ` + inside + ` }` + after;
            index = closeBrace + `'@media (hover: hover)': { `.length + ` }`.length;
        } else {
            index += 10;
        }
    }

    if (content !== original) {
        fs.writeFileSync(filePath, content, 'utf8');
        console.log("Updated: " + filePath);
    }
}

walk(path.join(__dirname, 'pagudhigal'), processFile);
processFile(path.join(__dirname, 'Seyali.tsx'));

console.log("Done");
