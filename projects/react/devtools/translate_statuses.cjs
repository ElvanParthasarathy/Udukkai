const fs = require('fs');

function updateDict(path, isTamil) {
  let content = fs.readFileSync(path, 'utf-8');
  
  const additions = isTamil ? `
  // Statuses
  status_unpaid: 'செலுத்தப்படாதவை',
  status_partial: 'பகுதி செலுத்தியவை',
  status_paid: 'செலுத்தியவை',
  status_overdue: 'காலக்கெடு முடிந்தது',` : `
  // Statuses
  status_unpaid: 'Unpaid',
  status_partial: 'Partial',
  status_paid: 'Paid',
  status_overdue: 'Overdue',`;

  content = content.replace(/};\s*export default/, additions + '\n};\nexport default');
  fs.writeFileSync(path, content, 'utf-8');
}

updateDict('moolam/mozhi/ta.ts', true);
updateDict('moolam/mozhi/en.ts', false);
