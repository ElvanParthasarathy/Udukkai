// @ts-nocheck
// Parity check. Every case is rendered twice IN THIS PAGE: by the new code (window.UdukkaiPrint from the bridge entry)
// and by the frozen copy of the old shipped bundle (verify/reference, loaded in an iframe). The printed A4 document of
// each is measured element by element and compared. Both run in the same browser state, so display scaling cancels out.
import '../bridge/entry';
import gstInvoice from './samples/gst-invoice.json';
import gstReceipt from './samples/gst-receipt.json';
import coolieInvoice from './samples/coolie-invoice.json';
import coolieReceipt from './samples/coolie-receipt.json';

const SAMPLES = { 'gst-invoice': gstInvoice, 'gst-receipt': gstReceipt, 'coolie-invoice': coolieInvoice, 'coolie-receipt': coolieReceipt };
const ALL = ['gst-invoice', 'gst-receipt', 'coolie-invoice', 'coolie-receipt'];
const svg = 'data:image/svg+xml;utf8,' + encodeURIComponent('<svg xmlns="http://www.w3.org/2000/svg" width="400" height="140"><rect width="400" height="140" fill="#cde"/><text x="20" y="80" font-size="40" fill="#234">SAMPLE LOGO</text></svg>');

const CASES = {
  base: [ALL, 'ta', (p) => p],
  A_en_ui: [ALL, 'en', (p) => p],
  B_single_lang: [['gst-invoice'], 'ta', (p) => { Object.assign(p.profile, { enableBilingual: false, primaryDataLanguage: 'English', secondaryDataLanguage: 'English' }); return p; }],
  C_options: [['gst-invoice'], 'ta', (p) => { Object.assign(p.options, { showHSN: false, showDiscountColumn: false, taxInclusive: true }); p.totals.taxInclusive = true; return p; }],
  D_many_items: [['gst-invoice'], 'ta', (p) => { const base = p.items; p.items = []; for (let i = 0; i < 8; i++) base.forEach((it, j) => p.items.push({ ...it, id: 'x' + i + j })); return p; }],
  E_coolie_many: [['coolie-invoice'], 'ta', (p) => { const base = p.bill.items; p.bill.items = []; for (let i = 0; i < 10; i++) base.forEach((it) => p.bill.items.push({ ...it })); p.bill.custom_charge_name = JSON.stringify([{ name: 'Packing', amount: 50 }]); p.bill.custom_charge_rs = 50; return p; }],
  F_wide_header: [['gst-invoice', 'gst-receipt'], 'ta', (p) => { Object.assign(p.profile, { billHeaderStyle: 'wide', wideLogo: svg, wideLogoX: 10, wideLogoY: 5, wideLogoScale: 0.8, themeColor: '#7b1fa2' }); return p; }],
  G_receipt_single_en: [['gst-receipt'], 'en', (p) => { Object.assign(p.profile, { enableBilingual: false, primaryDataLanguage: 'English', secondaryDataLanguage: 'English' }); return p; }],
  H_long_note: [['gst-receipt', 'coolie-receipt'], 'ta', (p) => { p.receipt.note = 'Long note. '.repeat(60); return p; }],
};

// Two measured lines match when every field is equal; numbers (with or without px) may differ by 0.05.
const NUM = /^-?\d+(\.\d+)?(px)?$/;
const sameField = (x, y) => x === y || (NUM.test(x) && NUM.test(y) && Math.abs(parseFloat(x) - parseFloat(y)) <= 0.05);
const compare = (got, want) => {
  if (got.length !== want.length) return `element count ${got.length}, reference ${want.length}`;
  for (let i = 0; i < got.length; i++) {
    const a = got[i].split('|'), b = want[i].split('|');
    for (let k = 0; k < Math.max(a.length, b.length); k++) {
      if (!sameField(a[k] ?? '', b[k] ?? '')) return `element ${i} (${a[0]} ${a[1].slice(0, 30)}) field ${k}: ${a[k]} vs ${b[k]}`;
    }
  }
  return '';
};

const measure = async (html) => {
  const f = document.createElement('iframe');
  f.style.cssText = 'position:fixed;left:0;top:0;width:900px;height:1300px;border:0;visibility:hidden';
  document.body.appendChild(f);
  await new Promise((r) => { f.onload = r; f.srcdoc = html; });
  // Wait until the fonts have really loaded (stable for 300ms), not just for the first 'ready'.
  const fonts = f.contentWindow.document.fonts;
  await fonts.ready;
  for (let i = 0, calm = 0; i < 100 && calm < 3; i++) {
    await new Promise((r) => setTimeout(r, 100));
    calm = fonts.status === 'loaded' ? calm + 1 : 0;
  }
  await new Promise((r) => setTimeout(r, 200));
  const d = f.contentDocument, w = d.defaultView;
  const props = ['font-family', 'font-size', 'font-weight', 'color', 'background-color', 'padding-top', 'padding-left', 'margin-top', 'border-top-width', 'line-height', 'display'];
  const lines = [...d.body.querySelectorAll('*')].map((e) => {
    const r = e.getBoundingClientRect(), cs = w.getComputedStyle(e);
    const cls = typeof e.className === 'string' ? e.className : '';
    return [e.tagName, cls, ...[r.x, r.y, r.width, r.height].map((n) => Math.round(n * 100) / 100), ...props.map((p) => cs.getPropertyValue(p))].join('|');
  });
  f.remove();
  return lines;
};

const out = document.getElementById('out');
const ref = document.createElement('iframe');
ref.style.cssText = 'position:fixed;left:0;top:0;width:900px;height:1300px;border:0;visibility:hidden';
document.body.appendChild(ref);
await new Promise((r) => { ref.onload = r; ref.src = '/reference/index.html'; });
for (let i = 0; i < 100 && !ref.contentWindow.UdukkaiPrint; i++) await new Promise((r) => setTimeout(r, 100));
const OLD = ref.contentWindow.UdukkaiPrint;
if (!OLD) { out.textContent = 'The reference bundle did not load (verify/reference).'; document.title = 'FAIL'; throw new Error('no reference'); }

const rows = [];
let pass = 0, total = 0;
for (const [name, [docs, lang, fn]] of Object.entries(CASES)) {
  for (const dt of docs) {
    const props = fn(JSON.parse(JSON.stringify(SAMPLES[dt].props)));
    await window.UdukkaiPrint.renderProps(props, lang, dt);
    const got = await measure(window.UdukkaiPrint.printHtml({}));
    await OLD.renderProps(JSON.parse(JSON.stringify(props)), lang, dt);
    // the reference's document points at its own assets under /reference
    const want = await measure(OLD.printHtml({}).replaceAll('"/assets/', '"/reference/assets/'));
    const problem = compare(got, want);
    total++; if (!problem) pass++;
    rows.push((problem ? 'FAIL ' : 'PASS ') + (name + ':' + dt).padEnd(36) + got.length + ' elements' + (problem ? '   ' + problem : ''));
    out.textContent = rows.join('\n');
  }
}
out.textContent = (pass === total ? 'ALL PASS ' : 'FAILED ') + pass + '/' + total + ' (dpr ' + devicePixelRatio + ')\n\n' + rows.join('\n');
document.title = (pass === total ? 'PASS ' : 'FAIL ') + pass + '/' + total;
