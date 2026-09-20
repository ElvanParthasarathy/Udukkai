const fs = require('fs');
const file = 'd:\\Projects\\Elvan Niril\\moolam\\Seyali.tsx';
let content = fs.readFileSync(file, 'utf8');

// The regex matches the common overlay block
const regex = /position:\s*\{\s*xs:\s*'fixed',\s*md:\s*'absolute'\s*\},\s*top:\s*\{\s*xs:\s*[^,]+,\s*md:\s*0\s*\},\s*left:\s*\{\s*xs:\s*'12px',\s*md:\s*0\s*\},\s*right:\s*\{\s*xs:\s*'12px',\s*md:\s*0\s*\},\s*bottom:\s*\{\s*xs:\s*'12px',\s*md:\s*0\s*\},\s*pt:\s*\{\s*xs:\s*1,\s*md:\s*0\s*\},\s*borderRadius:\s*\{\s*xs:\s*'24px',\s*md:\s*0\s*\},\s*bgcolor:\s*'background\.default',/g;

content = content.replace(regex, `position: 'absolute', \n            top: 0, \n            left: 0, \n            right: 0, \n            bottom: 0, \n            pt: { xs: 1, md: 0 },\n            bgcolor: 'background.default',`);

const regexSettings = /position:\s*\{\s*xs:\s*'fixed',\s*md:\s*'static'\s*\},\s*top:\s*\{\s*xs:\s*[^,]+,\s*md:\s*'auto'\s*\},\s*left:\s*\{\s*xs:\s*'12px',\s*md:\s*'auto'\s*\},\s*right:\s*\{\s*xs:\s*'12px',\s*md:\s*'auto'\s*\},\s*bottom:\s*\{\s*xs:\s*'12px',\s*md:\s*'auto'\s*\},\s*pt:\s*\{\s*xs:\s*1,\s*md:\s*0\s*\},\s*borderRadius:\s*\{\s*xs:\s*'24px',\s*md:\s*0\s*\},\s*bgcolor:\s*'background\.default',/g;

content = content.replace(regexSettings, `position: { xs: 'absolute', md: 'static' }, \n            top: 0, \n            left: 0, \n            right: 0, \n            bottom: 0, \n            pt: { xs: 1, md: 0 },\n            bgcolor: 'background.default',`);

fs.writeFileSync(file, content);
console.log('Replaced successfully');
