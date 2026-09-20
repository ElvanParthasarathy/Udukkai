const fs = require('fs');

function updateDict(path, isTamil) {
  let content = fs.readFileSync(path, 'utf-8');
  
  const additions = isTamil ? `
  // Misc Settings
  includeFinancialYear: 'நிதியாண்டை சேர்க்கவா',
  yesFinYear: 'ஆம் (2026-27)',
  no: 'இல்லை',
  numberPadding: 'எண் இலக்கங்கள் (Number Padding)',
  digits3: '3 இலக்கங்கள் (001)',
  digits4: '4 இலக்கங்கள் (0001)',
  digits5: '5 இலக்கங்கள் (00001)',
  digits6: '6 இலக்கங்கள் (000001)',
  saving: 'சேமிக்கப்படுகிறது...',
  saveNumberFormat: 'எண் வடிவத்தை சேமி',
  uploadLogo: 'லோகோவை பதிவேற்று',
  logoHint: 'PNG அல்லது JPG, (அதிகபட்சம் 500KB)',
  uploadSignature: 'கையொப்பத்தை பதிவேற்று',
  sigHint: 'PNG, JPG (அதிகபட்சம் 500KB)',
  saveProfile: 'சுயவிவரத்தை சேமி',
  
  termsTemplatesTitle: 'விதிமுறைகள் & நிபந்தனைகள் வார்ப்புருக்கள்',
  newTemplate: 'புதிய வார்ப்புரு',
  termsTemplatesDesc: 'மீண்டும் பயன்படுத்தக்கூடிய வார்ப்புருக்களை உருவாக்கவும் அல்லது கீழேயுள்ளவற்றிலிருந்து தேர்ந்தெடுக்கவும்.',
  quickStartTemplate: 'விரைவான தொடக்கம் — உங்கள் வணிகத்திற்கான வார்ப்புருவைத் தேர்ந்தெடுக்கவும்:',
  templateName: 'வார்ப்புரு பெயர்',
  templateContent: 'உள்ளடக்கம் (உங்கள் விதிமுறைகளை இங்கே ஒட்டவும்)',
  cancel: 'ரத்துசெய்',
  saveTemplate: 'வார்ப்புருவை சேமி',
  noTemplates: 'இன்னும் வார்ப்புருக்கள் இல்லை.',

  businessProfilesTitle: 'வணிக சுயவிவரங்கள்',
  addNewProfile: 'புதிய சுயவிவரம் சேர்',
  saveAsProfile: 'சுயவிவரமாக சேமி',
  businessProfilesDesc1: 'பல வணிக சுயவிவரங்களைச் சேமித்து, அவற்றை உடனடியாக மாற்றிக் கொள்ளலாம்.',
  businessProfilesDesc2: 'இன்னும் சேமிக்கப்பட்ட சுயவிவரங்கள் இல்லை. உங்கள் வணிக விவரங்களை மேலே பூர்த்தி செய்து "சுயவிவரமாக சேமி" என்பதைக் கிளிக் செய்யவும்.',

  appUpdatesTitle: 'பயன்பாட்டு புதுப்பிப்புகள்',
  appUpdatesDesc: 'புதிய பதிப்பு உள்ளதா என சரிபார்க்கவும்.',
  checkingUpdate: 'சரிபார்க்கப்படுகிறது...',
  checkForUpdates: 'புதுப்பிப்புகளை சரிபார்',

  dataManagementTitle: 'தரவு மேலாண்மை (Data Management)',
  dataNoticeText: '<strong>உங்கள் தரவு இந்தக் கணினியில் மட்டுமே உள்ளது.</strong> எங்களுக்கு, எங்கள் சேவையகங்களுக்கு அல்லது எந்த மூன்றாம் தரப்பினருக்கும் எதுவும் பதிவேற்றப்படவில்லை. நீங்கள் வெளிப்படையாகக் கோரும்போது மட்டுமே தரவு உங்கள் சொந்த Google Drive-க்குச் செல்லும்.',
  dataManagementDesc: 'எதை காப்புப் பிரதி எடுக்க வேண்டும் அல்லது மீட்டெடுக்க வேண்டும் என்பதைத் தேர்வுசெய்க. காப்பு கோப்புகளை USB டிரைவ் அல்லது Google Drive-ல் பாதுகாப்பாக வைக்கலாம்.',
  exportBackup: 'காப்புப் பிரதியை ஏற்றுமதி செய்...',
  importBackup: 'காப்புப் பிரதியை இறக்குமதி செய்...',
  
  googleDriveEasyWay: 'எளிதான வழி — Google Drive for Desktop (பரிந்துரைக்கப்படுகிறது)',
` : `
  // Misc Settings
  includeFinancialYear: 'Include Financial Year',
  yesFinYear: 'Yes (2026-27)',
  no: 'No',
  numberPadding: 'Number Padding',
  digits3: '3 digits (001)',
  digits4: '4 digits (0001)',
  digits5: '5 digits (00001)',
  digits6: '6 digits (000001)',
  saving: 'Saving...',
  saveNumberFormat: 'Save Number Format',
  uploadLogo: 'Upload Logo',
  logoHint: 'PNG or JPG, square or wide (max 500KB)',
  uploadSignature: 'Upload Signature',
  sigHint: 'PNG, JPG (max 500KB)',
  saveProfile: 'Save Profile',
  
  termsTemplatesTitle: 'Terms & Conditions Templates',
  newTemplate: 'New Template',
  termsTemplatesDesc: 'Create reusable templates or pick from ready-made ones below.',
  quickStartTemplate: 'QUICK START — PICK A TEMPLATE FOR YOUR BUSINESS:',
  templateName: 'Template Name',
  templateContent: 'Content (paste your terms here)',
  cancel: 'Cancel',
  saveTemplate: 'Save Template',
  noTemplates: 'No templates yet.',

  businessProfilesTitle: 'Business Profiles',
  addNewProfile: 'Add New Profile',
  saveAsProfile: 'Save as Profile',
  businessProfilesDesc1: 'Save multiple business profiles and switch between them instantly. Switching auto-saves your current profile first.',
  businessProfilesDesc2: 'No saved profiles yet. Fill in your business details above and click "Save as Profile".',

  appUpdatesTitle: 'App Updates',
  appUpdatesDesc: 'Check if a newer version is available.',
  checkingUpdate: 'Checking...',
  checkForUpdates: 'Check for Updates',

  dataManagementTitle: 'Data Management',
  dataNoticeText: '<strong>Your data is on this computer only.</strong> Nothing is uploaded to us, our servers, or any third party — not invoices, not clients, not settings. The only time anything leaves your machine is if you explicitly click <em>Save to Drive</em> below (uploads to <strong>your own</strong> Google Drive account). Files live under <code>data/</code> and <code>Saved Invoices/</code> next to the app.',
  dataManagementDesc: 'Choose what to back up or restore — invoices, clients, products, settings, custom units, or just specific parts. Backup files are plain JSON you can keep on a USB drive, OneDrive, or your own Google Drive.',
  exportBackup: 'Export Backup...',
  importBackup: 'Import Backup...',
  
  googleDriveEasyWay: 'Easiest Way — Google Drive for Desktop (Recommended)',
`;

  content = content.replace(/\n};/, additions + '\n};');
  fs.writeFileSync(path, content, 'utf-8');
}

updateDict('moolam/mozhi/ta.ts', true);
updateDict('moolam/mozhi/en.ts', false);

let uiContent = fs.readFileSync('moolam/pagudhigal/Amaippugal.tsx', 'utf-8');

const replacements = [
  ['<label className="form-label">Include Financial Year</label>', '<label className="form-label">{t(\'includeFinancialYear\')}</label>'],
  ['>Yes (2026-27)</button>', '>{t(\'yesFinYear\')}</button>'],
  ['>No</button>', '>{t(\'no\')}</button>'],
  ['<label className="form-label">Number Padding</label>', '<label className="form-label">{t(\'numberPadding\')}</label>'],
  ['>3 digits (001)</option>', '>{t(\'digits3\')}</option>'],
  ['>4 digits (0001)</option>', '>{t(\'digits4\')}</option>'],
  ['>5 digits (00001)</option>', '>{t(\'digits5\')}</option>'],
  ['>6 digits (000001)</option>', '>{t(\'digits6\')}</option>'],
  ['{invNumSaving ? \'Saving...\' : \'Save Number Format\'}', '{invNumSaving ? t(\'saving\') : t(\'saveNumberFormat\')}'],
  ['<span>Upload Logo</span>', '<span>{t(\'uploadLogo\')}</span>'],
  ['<span className="upload-hint">PNG or JPG, square or wide (max 500KB)</span>', '<span className="upload-hint">{t(\'logoHint\')}</span>'],
  ['<span>Upload Signature</span>', '<span>{t(\'uploadSignature\')}</span>'],
  ['<span className="upload-hint">PNG, JPG (max 500KB)</span>', '<span className="upload-hint">{t(\'sigHint\')}</span>'],
  ['{saving ? \'Saving...\' : \'Save Profile\'}', '{saving ? t(\'saving\') : t(\'saveProfile\')}'],
  
  ['<h3 className="section-title" style={{ margin: 0 }}>Terms & Conditions Templates</h3>', '<h3 className="section-title" style={{ margin: 0 }}>{t(\'termsTemplatesTitle\')}</h3>'],
  ['<Plus size={16} /> New Template', '<Plus size={16} /> {t(\'newTemplate\')}'],
  ['<p className="page-subtitle mb-4">Create reusable templates or pick from ready-made ones below.</p>', '<p className="page-subtitle mb-4">{t(\'termsTemplatesDesc\')}</p>'],
  ['<p className="form-label" style={{ marginBottom: \'0.5rem\' }}>Quick Start — Pick a template for your business:</p>', '<p className="form-label" style={{ marginBottom: \'0.5rem\' }}>{t(\'quickStartTemplate\')}</p>'],
  ['<label className="form-label">Template Name</label>', '<label className="form-label">{t(\'templateName\')}</label>'],
  ['<label className="form-label">Content (paste your terms here)</label>', '<label className="form-label">{t(\'templateContent\')}</label>'],
  ['>Cancel</button>', '>{t(\'cancel\')}</button>'],
  ['<Save size={16} /> Save Template', '<Save size={16} /> {t(\'saveTemplate\')}'],
  ['No templates yet.', '{t(\'noTemplates\')}'],

  ['<h3 className="section-title" style={{ margin: 0 }}>Business Profiles</h3>', '<h3 className="section-title" style={{ margin: 0 }}>{t(\'businessProfilesTitle\')}</h3>'],
  ['<Plus size={16} /> Add New Profile', '<Plus size={16} /> {t(\'addNewProfile\')}'],
  ['<Building2 size={16} /> Save as Profile', '<Building2 size={16} /> {t(\'saveAsProfile\')}'],
  ['Save multiple business profiles and switch between them instantly. Switching auto-saves your current profile first.', '{t(\'businessProfilesDesc1\')}'],
  ['No saved profiles yet. Fill in your business details above and click "Save as Profile".', '{t(\'businessProfilesDesc2\')}'],

  ['<h3 className="section-title">App Updates</h3>', '<h3 className="section-title">{t(\'appUpdatesTitle\')}</h3>'],
  ['<p className="page-subtitle mb-4">Check if a newer version is available.</p>', '<p className="page-subtitle mb-4">{t(\'appUpdatesDesc\')}</p>'],
  ['{checkingUpdate ? \'Checking...\' : \'Check for Updates\'}', '{checkingUpdate ? t(\'checkingUpdate\') : t(\'checkForUpdates\')}'],

  ['<h3 className="section-title">Data Management</h3>', '<h3 className="section-title">{t(\'dataManagementTitle\')}</h3>'],
  ['<Download size={18} /> Export Backup…</button>', '<Download size={18} /> {t(\'exportBackup\')}</button>'],
  ['<Upload size={18} /> Import Backup…</button>', '<Upload size={18} /> {t(\'importBackup\')}</button>'],
  ['<Cloud size={18} color="var(--primary)" /> Easiest Way — Google Drive for Desktop (Recommended)', '<Cloud size={18} color="var(--primary)" /> {t(\'googleDriveEasyWay\')}']
];

for (const [search, replace] of replacements) {
  uiContent = uiContent.split(search).join(replace);
}

// Special multiline replacements
uiContent = uiContent.replace(
  /<strong>Your data is on this computer only\.<\/strong> Nothing is uploaded to[\s\S]*?Files live under <code>data\/<\/code> and <code>Saved Invoices\/<\/code> next to the app\./m,
  '<div dangerouslySetInnerHTML={{ __html: t(\'dataNoticeText\') as string }} />'
);

uiContent = uiContent.replace(
  /Choose what to back up or restore — invoices, clients, products, settings, custom units, or just specific parts\.\s*Backup files are plain JSON you can keep on a USB drive, OneDrive, or your own Google Drive\./m,
  "{t('dataManagementDesc')}"
);

fs.writeFileSync('moolam/pagudhigal/Amaippugal.tsx', uiContent, 'utf-8');
console.log('Misc translations applied!');
