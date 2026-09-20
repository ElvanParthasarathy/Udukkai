const fs = require('fs');

function updateDict(path, isTamil) {
  let content = fs.readFileSync(path, 'utf-8');
  
  const additions = isTamil ? `
  // Notifications
  notificationsTitle: 'அறிவிப்புகள்',
  allClearNotif: 'எல்லாம் சரி ✨ — தற்போது உங்கள் கவனம் தேவைப்படும் எதுவும் இல்லை.',
  recurringAutoFireNotif: 'இன்று தானியங்கியாக உருவாக்கப்பட்ட தொடர் பட்டியல்கள்',
  checkDashboardForNewBills: 'புதிய பட்டியல்களுக்கு முகப்பைப் பார்க்கவும் · தேவைப்பட்டால் PDF பதிவிறக்கவும்',
  overdueInvoicesNotif: 'காலதாமதமான பட்டியல்கள்',
  moreWord: 'மேலும்',
  dueIn3DaysNotif: 'அடுத்த 3 நாட்களில் கெடு தேதி கொண்ட பட்டியல்கள்',
  gstFilingsDueNotif: 'அடுத்த 10 நாட்களில் GST தாக்கல் நிலுவையில் உள்ளன',
  todayWord: 'இன்று',
  lowStockNotif: 'இருப்பு குறைவாக உள்ள பொருட்கள்',
  leftWord: 'மீதம்',
  ` : `
  // Notifications
  notificationsTitle: 'Notifications',
  allClearNotif: 'All clear ✨ — nothing needs your attention right now.',
  recurringAutoFireNotif: 'recurring invoices auto-generated today',
  checkDashboardForNewBills: 'Check the Dashboard for the new bills · review and download PDFs as needed',
  overdueInvoicesNotif: 'overdue invoices',
  moreWord: 'more',
  dueIn3DaysNotif: 'invoices due in next 3 days',
  gstFilingsDueNotif: 'GST filings due in next 10 days',
  todayWord: 'today',
  lowStockNotif: 'products low on stock',
  leftWord: 'left',
  `;

  if (!content.includes('notificationsTitle:')) {
    content = content.replace(/};\s*export type TranslationKey/, additions + '\n};\n\nexport type TranslationKey');
    fs.writeFileSync(path, content, 'utf-8');
  }
}

updateDict('moolam/mozhi/ta.ts', true);
updateDict('moolam/mozhi/en.ts', false);

let v = fs.readFileSync('moolam/Seyali.tsx', 'utf-8');

v = v.replace(/>Notifications<\/h3>/g, ">{t('notificationsTitle')}</h3>");
v = v.replace(/>\s*All clear ✨ — nothing needs your attention right now\.\s*<\/p>/g, ">\\n                {t('allClearNotif')}\\n              </p>");

// <strong>{notifications.autoFire.count} recurring invoice{notifications.autoFire.count !== 1 ? 's' : ''} auto-generated today</strong>
v = v.replace(/<strong>\{notifications\.autoFire\.count\} recurring invoice[^<]+<\/strong>/g, "<strong>{notifications.autoFire.count} {t('recurringAutoFireNotif')}</strong>");
v = v.replace(/>\s*Check the Dashboard for the new bills · review and download PDFs as needed\s*<\/div>/g, ">{t('checkDashboardForNewBills')}</div>");

// <strong>{notifications.overdue.length} overdue invoice{notifications.overdue.length !== 1 ? 's' : ''}</strong>
v = v.replace(/<strong>\{notifications\.overdue\.length\} overdue invoice[^<]+<\/strong>/g, "<strong>{notifications.overdue.length} {t('overdueInvoicesNotif')}</strong>");
v = v.replace(/ \+ \` more\`/g, " + ' ' + t('moreWord')");

// <strong>{notifications.dueSoon.length} invoice{notifications.dueSoon.length !== 1 ? 's' : ''} due in next 3 days</strong>
v = v.replace(/<strong>\{notifications\.dueSoon\.length\} invoice[^<]+due in next 3 days<\/strong>/g, "<strong>{notifications.dueSoon.length} {t('dueIn3DaysNotif')}</strong>");

// <strong>{notifications.filings.length} GST filing{notifications.filings.length !== 1 ? 's' : ''} due in next 10 days</strong>
v = v.replace(/<strong>\{notifications\.filings\.length\} GST filing[^<]+due in next 10 days<\/strong>/g, "<strong>{notifications.filings.length} {t('gstFilingsDueNotif')}</strong>");
v = v.replace(/'today'/g, "t('todayWord')");

// <strong>{notifications.lowStock.length} product{notifications.lowStock.length !== 1 ? 's' : ''} low on stock</strong>
v = v.replace(/<strong>\{notifications\.lowStock\.length\} product[^<]+low on stock<\/strong>/g, "<strong>{notifications.lowStock.length} {t('lowStockNotif')}</strong>");
v = v.replace(/' left\)'\)/g, "' ' + t('leftWord') + ')')");

fs.writeFileSync('moolam/Seyali.tsx', v, 'utf-8');
