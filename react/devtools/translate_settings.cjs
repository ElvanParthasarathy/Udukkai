const fs = require('fs');

function updateDict(path, isTamil) {
  let content = fs.readFileSync(path, 'utf-8');
  
  const additions = isTamil ? `
  // Settings Page
  settingsTitle: 'அமைப்புகள்',
  settingsSubtitle: 'நிறுவன விவரங்கள், பிராண்டிங் & தரவுகள்',
  modulesTitle: 'தொகுதிகள் (Modules)',
  modulesDesc: 'உங்களுக்குத் தேவையில்லாத அம்சங்களை முடக்கலாம்.',
  regionTitle: 'பகுதி முன்னுரிமை',
  regionDesc: 'பயன்பாடு எவ்வாறு செயல்பட வேண்டும் என்பதைத் தேர்ந்தெடுக்கவும்.',
  companyDetailsTitle: 'நிறுவன விவரங்கள்',
  paymentAccountsTitle: 'கட்டணக் கணக்குகள்',
  paymentAccountsDesc: 'ஒரு சுயவிவரத்திற்கு பல வங்கி/UPI கணக்குகள்.',
  invoiceNumberFormatTitle: 'பட்டியல் எண் வடிவம்',
  brandingTitle: 'பிராண்டிங்',
  businessLogo: 'நிறுவனத்தின் லோகோ',
  signature: 'கையொப்பம்',
  cloudBackupTitle: 'கிளவுட் காப்பு (Google Drive)',
  cloudBackupDesc: 'உங்கள் பட்டியல்களை Google Drive உடன் தானாக ஒத்திசைக்கலாம்.',
  exportImportTitle: 'தரவு ஏற்றுமதி & இறக்குமதி',
  exportImportDesc: 'உங்கள் தரவை உள்ளூரில் பாதுகாப்பாக காப்பு எடுக்கலாம்.',
` : `
  // Settings Page
  settingsTitle: 'Settings',
  settingsSubtitle: 'Business profile, branding, integrations & data',
  modulesTitle: 'Modules',
  modulesDesc: 'Turn off the features you don\\'t need. They disappear from the sidebar and forms — your data stays untouched.',
  regionTitle: 'Region Preference',
  regionDesc: 'Choose how the app behaves. You can change this any time without losing data.',
  companyDetailsTitle: 'Company Details',
  paymentAccountsTitle: 'Payment Accounts',
  paymentAccountsDesc: 'Multiple bank / UPI accounts per profile. Pick one per invoice in the Customize panel. The ⭐ default account is preselected on new invoices.',
  invoiceNumberFormatTitle: 'Invoice Number Format',
  brandingTitle: 'Branding',
  businessLogo: 'Business Logo',
  signature: 'Signature / Stamp',
  cloudBackupTitle: 'Cloud Backup (Google Drive)',
  cloudBackupDesc: 'Auto-sync your invoices to Google Drive — no coding or API setup needed.',
  exportImportTitle: 'Export & Import Data',
  exportImportDesc: 'Backup your data locally or restore from a previous backup file.',
`;

  content = content.replace(/\n};/, additions + '\n};');
  fs.writeFileSync(path, content, 'utf-8');
}

updateDict('moolam/mozhi/ta.ts', true);
updateDict('moolam/mozhi/en.ts', false);

let uiContent = fs.readFileSync('moolam/pagudhigal/Amaippugal.tsx', 'utf-8');

const replacements = [
  ['<h1 className="page-title">Settings</h1>', '<h1 className="page-title">{t(\'settingsTitle\')}</h1>'],
  ['<p className="page-subtitle">Business profile, branding, integrations & data</p>', '<p className="page-subtitle">{t(\'settingsSubtitle\')}</p>'],
  ['<h3 className="section-title" style={{ marginTop: 0, marginBottom: \'0.25rem\' }}>Modules</h3>', '<h3 className="section-title" style={{ marginTop: 0, marginBottom: \'0.25rem\' }}>{t(\'modulesTitle\')}</h3>'],
  ['Turn off the features you don\'t need. They disappear from the sidebar and forms — your data stays untouched.', '{t(\'modulesDesc\')}'],
  ['<h3 className="section-title" style={{ marginTop: 0 }}>Region Preference</h3>', '<h3 className="section-title" style={{ marginTop: 0 }}>{t(\'regionTitle\')}</h3>'],
  ['Choose how the app behaves. You can change this any time without losing data.', '{t(\'regionDesc\')}'],
  ['<h3 className="section-title">Company Details</h3>', '<h3 className="section-title">{t(\'companyDetailsTitle\')}</h3>'],
  ['<h3 className="section-title" style={{ margin: 0 }}>Payment Accounts</h3>', '<h3 className="section-title" style={{ margin: 0 }}>{t(\'paymentAccountsTitle\')}</h3>'],
  ['Multiple bank / UPI accounts per profile. Pick one per invoice in the Customize panel.\n                    The ⭐ default account is preselected on new invoices.', '{t(\'paymentAccountsDesc\')}'],
  ['Invoice Number Format</h3>', '{t(\'invoiceNumberFormatTitle\')}</h3>'],
  ['<h3 className="section-title mt-8">Branding</h3>', '<h3 className="section-title mt-8">{t(\'brandingTitle\')}</h3>'],
  ['<label className="form-label">Business Logo</label>', '<label className="form-label">{t(\'businessLogo\')}</label>'],
  ['<label className="form-label">Signature / Stamp</label>', '<label className="form-label">{t(\'signature\')}</label>'],
  ['<h3 className="section-title">Cloud Backup (Google Drive)</h3>', '<h3 className="section-title">{t(\'cloudBackupTitle\')}</h3>'],
  ['Auto-sync your invoices to Google Drive — no coding or API setup needed.', '{t(\'cloudBackupDesc\')}'],
  ['<h3 className="section-title">Export & Import Data</h3>', '<h3 className="section-title">{t(\'exportImportTitle\')}</h3>'],
  ['<p className="page-subtitle">Backup your data locally or restore from a previous backup file.</p>', '<p className="page-subtitle">{t(\'exportImportDesc\')}</p>']
];

for (const [search, replace] of replacements) {
  uiContent = uiContent.split(search).join(replace);
}

fs.writeFileSync('moolam/pagudhigal/Amaippugal.tsx', uiContent, 'utf-8');
console.log('Settings translated!');
