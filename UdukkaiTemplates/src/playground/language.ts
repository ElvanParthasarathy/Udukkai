// @ts-nocheck
// Language check: a bill set to one language must show only that language, and a bilingual bill must show both.
// Renders the four papers through the same bridge the app uses, in every language mode and with the app's screen
// language set both ways, and lists every line of the wrong language. Run: npm run dev, open /language.html
// (the title ends in PASS or FAIL).
import '../bridge/entry';

const T = { biz: 'ஸ்ரீ ஜெய்பிரியா சில்க்ஸ்', tag: 'கைத்தறி பட்டு சேலைகள்', addr: '12 பெரிய தெரு', city: 'ஆரணி', dist: 'திருவண்ணாமலை', state: 'தமிழ்நாடு', country: 'இந்தியா', cust: 'ஸ்ரீ சிவராம் சில்க் சாரீஸ்', caddr: '45 காந்தி சாலை', ccity: 'காஞ்சிபுரம்', cdist: 'காஞ்சிபுரம்', item: 'பட்டுப்புடவை', bank: 'இந்தியன் வங்கி', br: 'ஆரணி கிளை' };
const E = { biz: 'Sri Jaipriya Silks', tag: 'Handloom Silk Sarees', addr: '12 Periya Street', city: 'Arani', dist: 'Tiruvannamalai', state: 'Tamil Nadu', country: 'India', cust: 'Sri Sivaram Silk Sarees', caddr: '45 Gandhi Road', ccity: 'Kancheepuram', cdist: 'Kancheepuram', item: 'Silk Saree', bank: 'Indian Bank', br: 'Arani Branch' };
const m = (k) => ({ ta: T[k], en: E[k] });

// mode: 'bi' = bilingual, 'ta' / 'en' = that language only
const profile = (mode) => ({
  id: '1', shortName: 'SJPS', primaryLanguage: mode === 'en' ? 'en' : 'ta', secondaryLanguage: mode === 'en' ? 'ta' : 'en', isBilingual: mode === 'bi',
  businessName: m('biz'), tagline: m('tag'), address: m('addr'), city: m('city'), district: m('dist'), state: m('state'), country: m('country'),
  pinCode: '632301', phone1: '9000000001', phone2: '9000000002', email: 'shop@example.com', gstin: '33AAAAA0000A1Z5', pan: 'AAAAA0000A',
  logo: '', wideLogo: '', headerLayout: 'small', signature: '', signatoryName: 'R. Owner', themeColor: '#388e3c', upiId: 'shop@upi',
  bankName: m('bank'), branch: m('br'), accountNumber: '123456789012', ifsc: 'IDIB000A001', printSettings: {}, paymentAccounts: [],
});
const customer = (state) => ({ name: m('cust'), address: m('caddr'), city: m('ccity'), district: m('cdist'), state: state || m('state'), country: m('country'), pinCode: '631501', gstin: '33BBBBB1111B1Z5', email: 'cust@example.com', phone: '9876543210' });

const gst = (mode, ui, v = {}) => ({
  docType: 'gst-invoice', appLanguage: ui, profile: profile(mode), customer: customer(v.cstate),
  invoice: {
    invoiceNumber: 'SJPS-007', invoiceType: v.type || 'tax-invoice', invoiceDate: '2026-11-02', customerName: m('cust'), customerAddress: m('ccity'),
    placeOfSupply: v.pos || 'Tamil Nadu', placeOfSupplyTa: v.posTa || 'தமிழ்நாடு',
    options: { taxInclusive: false, showHSN: true, showDiscountColumn: true, ...(v.options || {}) },
    items: [{ id: '1', name: m('item'), hsnCode: '50072010', quantity: 2, unit: 'Nos', price: 1500, taxRate: 12, discountAmount: v.disc || 0 }],
    totals: v.totals || { subtotal: 3000, totalDiscount: 0, cgst: 180, sgst: 180, igst: 0, roundOff: 0, total: 3360 },
  },
});
const coolie = (mode, ui) => ({
  docType: 'coolie-invoice', appLanguage: ui, profile: profile(mode), customer: customer(),
  invoice: {
    id: '1', invoiceNumber: 'VRM-007', invoiceDate: '2026-11-02', customerName: m('cust'), customerAddress: m('caddr'),
    items: [{ name: { ta: 'ஜரி தடை பணிக்கட்டணம்', en: 'Jari Thadai Job Work Charges' }, weight: 12.345, price: 150 }],
    wastageGrams: 15, postalCharges: 40, ahimsaSilkAmount: 55, charges: [{ name: 'Loading', amount: 77 }], grandTotal: 2023,
  },
});
const receipt = (docType) => (mode, ui) => ({
  docType, appLanguage: ui, profile: profile(mode),
  receipt: { receiptNumber: 'RC-007', receiptDate: '2026-11-02', amount: 3360, paymentMode: 'bank_transfer', transactionNumber: 'TXN12345', notes: '', customerName: m('cust'), invoiceNumbers: ['SJPS-007'] },
});

const DOCS = { gst, coolie, 'gst-receipt': receipt('gst-receipt'), 'coolie-receipt': receipt('coolie-receipt') };
const GST_VARIANTS = {
  estimate: { type: 'proforma' },
  'other state': { cstate: { ta: 'கேரளா', en: 'Kerala' }, pos: 'Kerala', posTa: 'கேரளா', totals: { subtotal: 3000, totalDiscount: 0, cgst: 0, sgst: 0, igst: 360, roundOff: 0, total: 3360 } },
  discount: { disc: 300, totals: { subtotal: 2700, totalDiscount: 300, cgst: 162, sgst: 162, igst: 0, roundOff: 0, total: 3024 } },
  'reverse charge': { options: { reverseCharge: true } },
};

// Acronyms and the data typed into the test business are not "language"
const ACRONYM = /^(GSTIN|CGST|SGST|IGST|IFSC|PAN|UPI|HSN|SWIFT|BIC|INR|GST|QR|SAC|NEFT|RTGS|IMPS|ID|No)$/i;
const DATA = ['R. Owner', 'Loading', 'shop@example.com', 'shop@upi', 'cust@example.com', 'IDIB000A001', 'AAAAA0000A', 'RC-007', 'SJPS-007', 'VRM-007', 'TXN12345', '33BBBBB1111B1Z5', '33AAAAA0000A1Z5'];
const hasTamil = (s) => /[஀-௿]/.test(s);
const hasEnglishWord = (s) => { let x = s; DATA.forEach((d) => { x = x.split(d).join(' '); }); return (x.match(/[A-Za-z]{2,}/g) || []).some((w) => !ACRONYM.test(w)); };

const paper = () => document.querySelector('.a4-paper, #invoice-preview, .print-area');
const linesOf = async (payload) => {
  await window.UdukkaiPrint.render(payload);
  return (paper()?.innerText || '').split('\n').map((s) => s.trim()).filter(Boolean);
};

const run = async () => {
  const fails = [];
  for (const [doc, make] of Object.entries(DOCS)) {
    const variants = doc === 'gst' ? { plain: {}, ...GST_VARIANTS } : { plain: {} };
    for (const [vn, v] of Object.entries(variants)) {
      for (const ui of ['ta', 'en']) {
        // Tamil only: no English words. English only: no Tamil (the "உ" blessing mark is a symbol, not language).
        for (const [mode, wrong] of [['ta', hasEnglishWord], ['en', (l) => hasTamil(l.replace(/^உ$/, ''))]]) {
          for (const l of (await linesOf(make(mode, ui, v))).filter(wrong)) fails.push(`${doc} / ${vn} / ${mode} only / screen ${ui}: "${l}"`);
        }
      }
      // Bilingual: both languages are on the paper
      const bi = (await linesOf(make('bi', 'ta', v))).join('\n');
      if (!hasTamil(bi) || !/[A-Za-z]{3,}/.test(bi)) fails.push(`${doc} / ${vn} / bilingual: not both languages`);
    }
  }
  return fails;
};

run().then((fails) => {
  document.getElementById('out').textContent = fails.length ? fails.join('\n') : 'All language checks passed.';
  document.title = `Language check: ${fails.length ? 'FAIL' : 'PASS'}`;
}).catch((e) => {
  document.getElementById('out').textContent = String(e && e.stack || e);
  document.title = 'Language check: FAIL';
});
