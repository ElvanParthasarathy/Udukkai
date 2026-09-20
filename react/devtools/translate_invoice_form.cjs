const fs = require('fs');

function updateDict(path, isTamil) {
  let content = fs.readFileSync(path, 'utf-8');
  
  const additions = isTamil ? `
  // New Invoice Line Items & Extras
  descriptionCol: 'விவரம்',
  hsnSacCol: 'HSN/SAC',
  rateCol: 'விலை (Rate)',
  addItemBtn: 'பொருளைச் சேர் (Add Item)',
  termsHeading: 'விதிமுறைகள் & நிபந்தனைகள்',
  insertPreset: 'முன்-அமைப்பைச் செருகு (Insert preset)',
  pickBusinessType: '— வணிக வகையைத் தேர்ந்தெடுக்கவும் —',
  indiaSpecificStarter: 'இந்திய வணிகங்களுக்கான மாதிரி வரிகள். தேவைக்கேற்ப திருத்தலாம்.',
  loadSavedTemplate: 'சேமிக்கப்பட்ட வார்ப்புருவை ஏற்று (Load saved template)',
  termsAppearsOnInvoice: 'விதிமுறைகள் (பட்டியலில் தோன்றும் — rich formatting ஆதரவு)',
  privateNote: 'தனிப்பட்ட குறிப்பு (பட்டியலில் தோன்றாது)',
  privateNoteEg: 'உ.ம். வாடிக்கையாளர் 15 நாள் கடன் கேட்டார், ரவியால் பரிந்துரைக்கப்பட்டது...',
  additionalPages: 'கூடுதல் பக்கங்கள் / பிரிவுகள்',
  addSectionBtn: 'பிரிவைச் சேர் (Add Section)',
  addExtraSectionsDesc: 'பட்டியல் அடிக்குறிப்புக்குப் பிறகு தோன்றும் கூடுதல் பிரிவுகளைச் சேர்க்கவும். (HTML உள்ளடக்கத்தை ஒட்டலாம்).',
  noExtraSections: 'கூடுதல் பிரிவுகள் இல்லை. உருவாக்க "பிரிவைச் சேர்" என்பதைக் கிளிக் செய்யவும்.',` : `
  // New Invoice Line Items & Extras
  descriptionCol: 'Description',
  hsnSacCol: 'HSN/SAC',
  rateCol: 'Rate',
  addItemBtn: 'Add Item',
  termsHeading: 'Terms & Conditions',
  insertPreset: 'Insert preset (by business type)',
  pickBusinessType: '— Pick a business type —',
  indiaSpecificStarter: 'India-specific starter wording. Edit freely.',
  loadSavedTemplate: 'Load saved template',
  termsAppearsOnInvoice: 'Terms (appears on invoice — supports rich formatting)',
  privateNote: 'Private Note (not shown on invoice)',
  privateNoteEg: 'e.g. Client asked for 15-day credit, follow up on 20th, referred by Ravi...',
  additionalPages: 'Additional Pages / Sections',
  addSectionBtn: 'Add Section',
  addExtraSectionsDesc: 'Add extra sections that appear after the invoice footer. You can paste formatted HTML content (bold, lists, tables, etc.).',
  noExtraSections: 'No extra sections. Click "Add Section" to create one.',`;

  if (!content.includes('descriptionCol:')) {
    content = content.replace(/};\s*export type TranslationKey/, additions + '\n};\n\nexport type TranslationKey');
    fs.writeFileSync(path, content, 'utf-8');
  }
}

updateDict('moolam/mozhi/ta.ts', true);
updateDict('moolam/mozhi/en.ts', false);

let jsx = fs.readFileSync('moolam/pagudhigal/PattiyalUruvakkam.tsx', 'utf-8');

jsx = jsx.replace(/>Description</g, '>{t("descriptionCol")}<');
jsx = jsx.replace(/>HSN\/SAC</g, '>{t("hsnSacCol")}<');
jsx = jsx.replace(/>Rate</g, '>{t("rateCol")}<');
jsx = jsx.replace(/>Add Item</g, '>{t("addItemBtn")}<');
jsx = jsx.replace(/>Terms &amp; Conditions</g, '>{t("termsHeading")}<'); // JSX encoded
jsx = jsx.replace(/>Terms & Conditions</g, '>{t("termsHeading")}<');
jsx = jsx.replace(/>Insert preset \(by business type\)</g, '>{t("insertPreset")}<');
jsx = jsx.replace(/>— Pick a business type —</g, '>{t("pickBusinessType")}<');
jsx = jsx.replace(/>India-specific starter wording\. Edit freely\.</g, '>{t("indiaSpecificStarter")}<');
jsx = jsx.replace(/>Load saved template</g, '>{t("loadSavedTemplate")}<');
jsx = jsx.replace(/>Terms \(appears on invoice — supports rich formatting\)</g, '>{t("termsAppearsOnInvoice")}<');
jsx = jsx.replace(/>Private Note \(not shown on invoice\)</g, '>{t("privateNote")}<');
jsx = jsx.replace(/placeholder="e\.g\. Client asked for 15-day credit, follow up on 20th, referred by Ravi\.\.\."/g, 'placeholder={t("privateNoteEg")}');
jsx = jsx.replace(/>Additional Pages \/ Sections</g, '>{t("additionalPages")}<');
jsx = jsx.replace(/>Add Section</g, '>{t("addSectionBtn")}<');
jsx = jsx.replace(/>Add extra sections that appear after the invoice footer\. You can paste formatted HTML content \(bold, lists, tables, etc\.\)\.</g, '>{t("addExtraSectionsDesc")}<');
jsx = jsx.replace(/>No extra sections\. Click "Add Section" to create one\.</g, '>{t("noExtraSections")}<');

fs.writeFileSync('moolam/pagudhigal/PattiyalUruvakkam.tsx', jsx, 'utf-8');
