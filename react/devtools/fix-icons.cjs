const fs = require('fs');
const path = require('path');

const map = {
  'IconPlus': 'Plus',
  'IconFiles': 'Files',
  'IconArrowLeft': 'ArrowLeft',
  'IconTrash': 'Trash',
  'IconSearch': 'Search',
  'IconUsers': 'Users',
  'IconBox': 'Package',
  'IconSettings': 'Settings',
  'IconEdit': 'Pencil',
  'IconX': 'X',
  'IconSave': 'Save',
  'IconPhone': 'Phone',
  'IconMail': 'Mail',
  'IconZoomIn': 'ZoomIn',
  'IconZoomOut': 'ZoomOut',
  'IconPrinter': 'Printer',
  'IconDatabase': 'Database',
  'IconRefresh': 'RefreshCw'
};

function traverse(dir) {
  for (const file of fs.readdirSync(dir)) {
    const fullPath = path.join(dir, file);
    if (fs.statSync(fullPath).isDirectory()) {
      traverse(fullPath);
    } else if (fullPath.endsWith('.jsx')) {
      let content = fs.readFileSync(fullPath, 'utf8');
      let original = content;

      // Replace import
      const regex = /import\s+\{([^}]+)\}\s+from\s+['\"](?:\.\.\/)*common\/Icons['\"];/g;
      content = content.replace(regex, (match, importsStr) => {
        const imports = importsStr.split(',').map(s => s.trim());
        const mapped = imports.map(i => map[i] || i);
        return 'import { ' + mapped.join(', ') + ' } from \'lucide-react\';';
      });

      // Replace usages
      for (const [oldName, newName] of Object.entries(map)) {
        const usageRegex = new RegExp('<' + oldName + '([^>]*)>', 'g');
        content = content.replace(usageRegex, '<' + newName + '$1>');
      }

      if (content !== original) {
        fs.writeFileSync(fullPath, content);
        console.log('Fixed', fullPath);
      }
    }
  }
}

traverse('src/components/Coolie');
console.log('Done');
