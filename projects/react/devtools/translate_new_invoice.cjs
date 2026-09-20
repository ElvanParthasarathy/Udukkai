const fs = require('fs');

function updateDict(path, isTamil) {
  let content = fs.readFileSync(path, 'utf-8');
  
  const additions = isTamil ? `
  // New Invoice
  draftOnly: 'வரைவு மட்டுமே — இன்னும் சேமிக்கப்படவில்லை',
  downloadPdf: 'PDF பதிவிறக்கு',
  whatsapp: 'வாட்ஸ்அப்',
  ewayBill: 'E-Way Bill',
  invoiceType: 'பட்டியல் வகை',
  customize: 'தனிப்பயனாக்கு',
  thisInvoiceIsFor: 'இந்த பட்டியல் இதற்கானது:',
  goods: 'பொருட்கள்',
  services: 'சேவைகள்',
  mixed: 'இரண்டும்',
  typeClientName: 'வாடிக்கையாளர் பெயரைத் தேட அல்லது புதிதாகச் சேர்க்க தட்டச்சு செய்க',
  egMumbai: 'உ.ம். சென்னை',
  selectState: 'மாநிலத்தைத் தேர்ந்தெடுக்கவும்',
  optionalLabel: 'விருப்பத்திற்குரியது',
  pdfPreviewHeader: 'PDF முன்னோட்டம் — உங்கள் பட்டியல் இப்படித்தான் இருக்கும்',
  termsAndConditionsLabel: 'விதிமுறைகள் & நிபந்தனைகள்',
  totalDueLabel: 'செலுத்த வேண்டிய மொத்தத் தொகை',` : `
  // New Invoice
  draftOnly: 'Draft only — not saved yet',
  downloadPdf: 'Download PDF',
  whatsapp: 'WhatsApp',
  ewayBill: 'E-Way Bill',
  invoiceType: 'Invoice Type',
  customize: 'Customize',
  thisInvoiceIsFor: 'This invoice is for:',
  goods: 'Goods',
  services: 'Services',
  mixed: 'Mixed',
  typeClientName: 'Type client name to search or add new',
  egMumbai: 'e.g. Mumbai',
  selectState: 'Select State',
  optionalLabel: 'Optional',
  pdfPreviewHeader: 'PDF PREVIEW — THIS IS HOW YOUR INVOICE WILL LOOK',
  termsAndConditionsLabel: 'TERMS & CONDITIONS',
  totalDueLabel: 'Total Due',`;

  if (!content.includes('pdfPreviewHeader')) {
    content = content.replace(/};\s*export type TranslationKey/, additions + '\n};\n\nexport type TranslationKey');
    fs.writeFileSync(path, content, 'utf-8');
  }
}

updateDict('moolam/mozhi/ta.ts', true);
updateDict('moolam/mozhi/en.ts', false);

// Now patch PattiyalUruvakkam.tsx
let jsx = fs.readFileSync('moolam/pagudhigal/PattiyalUruvakkam.tsx', 'utf-8');

// Replacements using template literals for safe replacement strings
jsx = jsx.replace(/>Draft only — not saved yet</g, '>{t("draftOnly")}<');
jsx = jsx.replace(/>Download PDF</g, '>{t("downloadPdf")}<');
jsx = jsx.replace(/>WhatsApp</g, '>{t("whatsapp")}<');
jsx = jsx.replace(/>E-Way Bill</g, '>{t("ewayBill")}<');
jsx = jsx.replace(/>Invoice Type</g, '>{t("invoiceType")}<');
jsx = jsx.replace(/>Customize</g, '>{t("customize")}<');
jsx = jsx.replace(/>This invoice is for:</g, '>{t("thisInvoiceIsFor")}<');
jsx = jsx.replace(/>Goods</g, '>{t("goods")}<');
jsx = jsx.replace(/>Services</g, '>{t("services")}<');
jsx = jsx.replace(/>Mixed</g, '>{t("mixed")}<');
jsx = jsx.replace(/"Type client name to search or add new"/g, '{t("typeClientName")}');
jsx = jsx.replace(/"e\.g\. Mumbai"/g, '{t("egMumbai")}');
jsx = jsx.replace(/"Optional"/g, '{t("optionalLabel")}');
jsx = jsx.replace(/>PDF PREVIEW — THIS IS HOW YOUR INVOICE WILL LOOK</g, '>{t("pdfPreviewHeader")}<');
jsx = jsx.replace(/>TERMS &amp; CONDITIONS</g, '>{t("termsAndConditionsLabel")}<'); // JSX encoded
jsx = jsx.replace(/>TERMS & CONDITIONS</g, '>{t("termsAndConditionsLabel")}<'); // just in case
jsx = jsx.replace(/>Total Due</g, '>{t("totalDueLabel")}<');
jsx = jsx.replace(/{cc\.postalLabel}/g, '{t(cc.postalLabel as any, { defaultValue: cc.postalLabel })}');
jsx = jsx.replace(/{cc\.stateLabel}/g, '{t(cc.stateLabel as any, { defaultValue: cc.stateLabel })}');
jsx = jsx.replace(/Select {cc\.stateLabel}/g, '{t("selectState")}');
jsx = jsx.replace(/{cc\.taxIdLabel}/g, '{t(cc.taxIdLabel as any, { defaultValue: cc.taxIdLabel })}');

fs.writeFileSync('moolam/pagudhigal/PattiyalUruvakkam.tsx', jsx, 'utf-8');
