// @ts-nocheck
// Language rules sheet: one paper and one field, the same bill printed once for every rule and cropped to the part of
// the bill that field controls, so each option can be checked by eye.
//   npm run dev, then open /rules-sheet.html?paper=gst&field=customerName
//   paper: gst (Textiles) or coolie (Factory). field: businessName, businessAddress, customerName, customerAddress, itemName.
import '../bridge/entry';

const T = { biz: 'ஸ்ரீ ஜெய்பிரியா சில்க்ஸ்', tag: 'கைத்தறி பட்டு சேலைகள்', addr: '12 பெரிய தெரு', city: 'ஆரணி', dist: 'திருவண்ணாமலை', state: 'தமிழ்நாடு', country: 'இந்தியா', cust: 'ஸ்ரீ சிவராம் சில்க் சாரீஸ்', caddr: '45 காந்தி சாலை', ccity: 'காஞ்சிபுரம்', cdist: 'காஞ்சிபுரம்', item: 'பட்டுப்புடவை', bank: 'இந்தியன் வங்கி', br: 'ஆரணி கிளை' };
const E = { biz: 'Sri Jaipriya Silks', tag: 'Handloom Silk Sarees', addr: '12 Periya Street', city: 'Arani', dist: 'Tiruvannamalai', state: 'Tamil Nadu', country: 'India', cust: 'Sri Sivaram Silk Sarees', caddr: '45 Gandhi Road', ccity: 'Kancheepuram', cdist: 'Kancheepuram', item: 'Zari Pattu Saree', bank: 'Indian Bank', br: 'Arani Branch' };
const m = (k) => ({ ta: T[k], en: E[k] });

const profile = (rules) => ({
  id: '1', shortName: 'SJPS', primaryLanguage: 'ta', secondaryLanguage: 'en', isBilingual: true,
  businessName: m('biz'), tagline: m('tag'), address: m('addr'), city: m('city'), district: m('dist'), state: m('state'), country: m('country'),
  pinCode: '632301', phone1: '9000000001', phone2: '9000000002', email: 'shop@example.com', gstin: '33AAAAA0000A1Z5', pan: 'AAAAA0000A',
  logo: '', wideLogo: '', headerLayout: 'small', signature: '', signatoryName: 'R. Owner', themeColor: '#388e3c', upiId: '',
  bankName: m('bank'), branch: m('br'), accountNumber: '123456789012', ifsc: 'IDIB000A001', printSettings: rules ? { langRules: rules } : {}, paymentAccounts: [],
});
const customer = { name: m('cust'), address: m('caddr'), city: m('ccity'), district: m('cdist'), state: m('state'), country: m('country'), pinCode: '631501', gstin: '33BBBBB1111B1Z5', email: 'cust@example.com', phone: '9876543210' };

const PAPERS = {
  gst: (rules) => ({
    docType: 'gst-invoice', appLanguage: 'ta', profile: profile(rules), customer,
    invoice: {
      invoiceNumber: 'SJPS-007', invoiceType: 'tax-invoice', invoiceDate: '2026-11-02', customerName: m('cust'), customerAddress: m('ccity'),
      placeOfSupply: 'Tamil Nadu', placeOfSupplyTa: 'தமிழ்நாடு', options: { taxInclusive: false, showHSN: true, showDiscountColumn: true },
      items: [{ id: '1', name: m('item'), hsnCode: '50072010', quantity: 2, unit: 'Nos', price: 1500, taxRate: 12, discountAmount: 0 }],
      totals: { subtotal: 3000, totalDiscount: 0, cgst: 180, sgst: 180, igst: 0, roundOff: 0, total: 3360 },
    },
  }),
  coolie: (rules) => ({
    docType: 'coolie-invoice', appLanguage: 'ta', profile: profile(rules), customer,
    invoice: {
      id: '1', invoiceNumber: 'VRM-007', invoiceDate: '2026-11-02', customerName: m('cust'), customerAddress: m('caddr'),
      items: [{ name: { ta: 'ஜரி தடை பணிக்கட்டணம்', en: 'Jari Thadai Job Work Charges' }, weight: 12.345, price: 150 }],
      wastageGrams: 15, postalCharges: 40, ahimsaSilkAmount: 55, charges: [], grandTotal: 2023,
    },
  }),
};

// The part of the bill each field controls
const REGION = {
  gst: { businessName: '.inv-classic-header', businessAddress: '.inv-contact-block', customerName: '.inv-parties', customerAddress: '.inv-parties', itemName: '.unified-table-box' },
  coolie: { businessName: '.print-header-new', businessAddress: '.contact-section', customerName: '.bill-info-section', customerAddress: '.bill-info-section', itemName: '.bill-table-new' },
};
const TITLE = { businessName: 'Business name', businessAddress: 'Business address', customerName: 'Customer name', customerAddress: 'Customer address', itemName: 'Item name' };
const RULES = [['auto', 'Default (no rule)'], ['both', 'Tamil → English'], ['both-rev', 'English → Tamil'], ['pri', 'Tamil only'], ['sec', 'English only']];

const q = new URLSearchParams(location.search);
const paper = q.get('paper') === 'coolie' ? 'coolie' : 'gst';
const field = REGION.gst[q.get('field')] ? q.get('field') : 'customerName';
// The box to show: the part of the bill a field controls (for the Factory item name: the header and the first row)
const rectOf = (el) => {
  if (paper === 'coolie' && field === 'itemName') {
    const a = el.querySelector('.bill-table-new thead').getBoundingClientRect();
    const b = el.querySelector('.bill-table-new tbody tr').getBoundingClientRect();
    return { top: a.top, height: b.bottom - a.top - 4 };
  }
  return el.querySelector(REGION[paper][field]).getBoundingClientRect();
};

const cell = (el, label, n, SCALE) => {
  const pr = el.getBoundingClientRect();
  const k = pr.width / el.offsetWidth;
  const tr = rectOf(el);
  const top = (tr.top - pr.top) / k, height = tr.height / k;
  const box = document.createElement('div');
  box.style.cssText = `margin:0 0 14px;`;
  const cap = document.createElement('div');
  cap.style.cssText = 'font:600 15px system-ui;margin:0 0 4px;color:#111';
  cap.textContent = `${n}. ${label}`;
  const crop = document.createElement('div');
  crop.style.cssText = `position:relative;overflow:hidden;width:${el.offsetWidth * SCALE}px;height:${height * SCALE}px;border:1px solid #bbb;background:#fff`;
  const inner = el.cloneNode(true);
  inner.style.cssText += `;position:absolute;left:0;top:${-top * SCALE}px;width:${el.offsetWidth}px;margin:0;transform:scale(${SCALE});transform-origin:0 0;`;
  crop.appendChild(inner);
  box.append(cap, crop);
  return box;
};

const run = async () => {
  const out = document.getElementById('out');
  out.innerHTML = '';
  const head = document.createElement('div');
  head.style.cssText = 'font:700 18px system-ui;margin:0 0 12px';
  head.textContent = `${paper === 'gst' ? 'Textiles bill' : 'Factory bill'} · ${TITLE[field]} · bilingual, main language Tamil, second language English`;
  out.appendChild(head);
  const paperEl = () => document.querySelector('#root .a4-paper, #root #invoice-preview');
  const regionHeight = (el) => rectOf(el).height / (el.getBoundingClientRect().width / el.offsetWidth);
  // Five cells must fit on one screen: shrink the taller regions
  await window.UdukkaiPrint.render(PAPERS[paper](null));
  const scale = Math.min(0.95, 166 / regionHeight(paperEl()));
  for (const [i, [rule, label]] of RULES.entries()) {
    await window.UdukkaiPrint.render(PAPERS[paper](rule === 'auto' ? null : { [field]: rule }));
    out.appendChild(cell(paperEl(), label, i + 1, scale));
  }
  document.title = 'Rules sheet: ready';
};
run().catch((e) => { document.getElementById('out').textContent = String((e && e.stack) || e); document.title = 'Rules sheet: error'; });
