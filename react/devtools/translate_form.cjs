const fs = require('fs');

function updateDict(path, isTamil) {
  let content = fs.readFileSync(path, 'utf-8');
  
  const additions = isTamil ? `
  // Settings Form
  businessNameLabel: 'நிறுவனத்தின் பெயர் *',
  countryLabel: 'நாடு',
  mugavariLabel: 'முகவரி',
  oorLabel: 'ஊர் / நகரம்',
  emailLabel: 'மின்னஞ்சல்',
  tholaipesiLabel: 'தொலைபேசி',
  'maanilam': 'மாநிலம்',
  'PIN Code': 'அஞ்சல் குறியீடு',
  'GSTIN': 'GSTIN',
  'Select maanilam': 'மாநிலத்தைத் தேர்ந்தெடுக்கவும்',
  'e.g. Mumbai': 'உ.ம். சென்னை',
` : `
  // Settings Form
  businessNameLabel: 'Business Name *',
  countryLabel: 'Country',
  mugavariLabel: 'Address',
  oorLabel: 'City / Town',
  emailLabel: 'Email',
  tholaipesiLabel: 'Phone',
  'maanilam': 'State',
  'PIN Code': 'PIN Code',
  'GSTIN': 'GSTIN',
  'Select maanilam': 'Select State',
  'e.g. Mumbai': 'e.g. Mumbai',
`;

  content = content.replace(/\n};/, additions + '\n};');
  fs.writeFileSync(path, content, 'utf-8');
}

updateDict('moolam/mozhi/ta.ts', true);
updateDict('moolam/mozhi/en.ts', false);

let uiContent = fs.readFileSync('moolam/pagudhigal/Amaippugal.tsx', 'utf-8');

const replacements = [
  ['<label className="form-label">Business Name *</label>', '<label className="form-label">{t(\'businessNameLabel\')}</label>'],
  ['<label className="form-label">Country</label>', '<label className="form-label">{t(\'countryLabel\')}</label>'],
  ['<label className="form-label">mugavari</label>', '<label className="form-label">{t(\'mugavariLabel\')}</label>'],
  ['<label className="form-label">oor</label>', '<label className="form-label">{t(\'oorLabel\')}</label>'],
  ['<label className="form-label">Email</label>', '<label className="form-label">{t(\'emailLabel\')}</label>'],
  ['<label className="form-label">tholaipesi</label>', '<label className="form-label">{t(\'tholaipesiLabel\')}</label>'],
  ['<label className="form-label">{cc.postalLabel}</label>', '<label className="form-label">{t(cc.postalLabel as any)}</label>'],
  ['<label className="form-label">{cc.stateLabel}</label>', '<label className="form-label">{t(cc.stateLabel as any)}</label>'],
  ['<label className="form-label">{cc.taxIdLabel}</label>', '<label className="form-label">{t(cc.taxIdLabel as any)}</label>'],
  ['placeholder="e.g. Mumbai"', 'placeholder={t(\'e.g. Mumbai\' as any)}'],
  ['placeholder={cc.postalLabel}', 'placeholder={t(cc.postalLabel as any)}'],
  ['Select {cc.stateLabel}', '{t(\'Select maanilam\' as any)}']
];

for (const [search, replace] of replacements) {
  uiContent = uiContent.split(search).join(replace);
}

fs.writeFileSync('moolam/pagudhigal/Amaippugal.tsx', uiContent, 'utf-8');
console.log('Form translated!');
