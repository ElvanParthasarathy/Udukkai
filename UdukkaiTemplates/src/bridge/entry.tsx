// @ts-nocheck
// The entry an app loads in a WebView. It renders one of the four templates from the app's payload and hands
// back the printable A4 document. The functions are the ones the Kotlin app calls today:
//   window.UdukkaiPrint.render(payload)   -> renders, then calls UdukkaiNative.onReady() (onError if nothing drew)
//   window.UdukkaiPrint.printHtml(opts)   -> the paper-only HTML document the host prints to a vector PDF
//   window.UdukkaiPrint.setTopInset(px)   -> kept for compatibility, the page is hidden so nothing uses it
//   window.UdukkaiPrint.buildPdf(opts)    -> no image fallback in this bundle: reports onError so the host fails fast
import { useEffect } from 'react';
import { createRoot } from 'react-dom/client';
import { GstInvoice, CoolieInvoice, GstReceipt, CoolieReceipt, LanguageProvider } from '../index';
import { toGstInvoiceProps, toGstReceiptProps, toCoolieBillProps, toCoolieReceiptProps } from './adapter';
import { TEMPLATE_VERSION } from './version';

const notify = (method, ...args) => {
  try {
    const n = window.UdukkaiNative;
    if (n && typeof n[method] === 'function') n[method](...args);
  } catch (e) {
    console.error(e);
  }
};

const wait = (ms) => new Promise((r) => setTimeout(r, ms));
// requestAnimationFrame pauses in hidden WebViews, so never wait on it alone.
const nextFrame = () => Promise.race([new Promise((r) => requestAnimationFrame(() => r())), wait(50)]);

const waitForImages = (root) =>
  Promise.all(
    Array.from(root.querySelectorAll('img')).map((img) =>
      img.complete ? Promise.resolve() : new Promise((r) => { img.onload = r; img.onerror = r; })
    )
  );

const waitFor = async (find, timeoutMs = 4000) => {
  const start = Date.now();
  let el = find();
  while (!el && Date.now() - start < timeoutMs) {
    await wait(30);
    el = find();
  }
  return el;
};

// Inline print rules each document adds when it is printed (copied from the old views' handlePrint).
const INVOICE_PRINT_RULES = `
              @page { size: A4; margin: 0; }
              html { font-size: 16px !important; }
              body, html { background-color: white !important; margin: 0; padding: 0; }
              .invoice-paper, .a4-paper { box-shadow: none !important; margin: 0 !important; border: none !important; width: 100% !important; }
              .no-print { display: none !important; }
              #invoice-preview { padding: 0 !important; }
              #invoice-preview .inv-classic-header { padding-left: 1.5rem !important; padding-right: 1.5rem !important; padding-top: 1rem !important; }
              #invoice-preview .inv-parties { margin-left: 1.5rem !important; margin-right: 1.5rem !important; }
              #invoice-preview .unified-table-box { margin-left: 1.5rem !important; margin-right: 1.5rem !important; }
              #invoice-preview .inv-footer { margin-left: 1.5rem !important; margin-right: 1.5rem !important; }
              #invoice-preview .inv-contact-block { padding-left: 1.5rem !important; padding-right: 1.5rem !important; }`;

const COOLIE_INVOICE_PRINT_RULES = `
                  body, html { background-color: white !important; margin: 0; padding: 0; }
                  .invoice-paper, .a4-paper { box-shadow: none !important; margin: 0 !important; border: none !important; width: 100% !important; }
                  .no-print { display: none !important; }`;
const RECEIPT_PRINT_RULES = `
              body, html { background-color: white !important; margin: 0; padding: 0; }
              .receipt-box, .invoice-paper, .a4-paper { box-shadow: none !important; margin: 0 !important; border: none !important; width: 100% !important; }
              .no-print { display: none !important; }`;

// The paper element of each template, its print rules and the print job name.
const DOC_TYPES = {
  'gst-invoice': {
    paper: '#invoice-preview', printRules: INVOICE_PRINT_RULES,
    title: (p) => (p?.details?.invoiceNumber ? `Invoice-${p.details.invoiceNumber}` : 'Print'),
  },
  'gst-receipt': {
    paper: '.print-area', printRules: RECEIPT_PRINT_RULES,
    title: (p) => (p?.receipt?.receiptNo ? `Receipt-${p.receipt.receiptNo}` : 'Print'),
  },
  'coolie-invoice': {
    paper: '.a4-paper', printRules: COOLIE_INVOICE_PRINT_RULES,
    title: (p) => (p?.bill?.bill_no ? `Invoice-${p.bill.bill_no}` : 'Print'),
  },
  'coolie-receipt': {
    paper: '.print-area', printRules: RECEIPT_PRINT_RULES,
    title: (p) => (p?.receipt?.receiptNo ? `Receipt-${p.receipt.receiptNo}` : 'Print'),
  },
};

const Mounted = ({ onMounted, children }) => {
  useEffect(() => { onMounted(); }, []);
  return children;
};

const renderDocument = (docType, props) => {
  switch (docType) {
    case 'gst-receipt': return <GstReceipt receipt={props.receipt} profile={props.profile} />;
    case 'coolie-invoice': return <CoolieInvoice bill={props.bill} />;
    case 'coolie-receipt': return <CoolieReceipt receipt={props.receipt} />;
    default: return <GstInvoice {...props} />;
  }
};

let root = null;
let renderToken = 0;
let current = { docType: 'gst-invoice', props: null };

const paperElement = () => document.querySelector(DOC_TYPES[current.docType].paper);

const toProps = (data) => {
  switch (data.docType) {
    case 'gst-receipt': return toGstReceiptProps(data);
    case 'coolie-invoice': return toCoolieBillProps(data);
    case 'coolie-receipt': return toCoolieReceiptProps(data);
    default: return toGstInvoiceProps(data);
  }
};

const render = async (payload) => {
  const data = typeof payload === 'string' ? JSON.parse(payload) : payload;
  return renderProps(toProps(data), data.appLanguage, data.docType || 'gst-invoice');
};

// Renders a template straight from template props (what the playground and the checks use).
const renderProps = async (props, appLanguage, docType = 'gst-invoice') => {
  const token = ++renderToken;
  current = { docType: DOC_TYPES[docType] ? docType : 'gst-invoice', props };

  // LanguageProvider reads the UI language from localStorage on mount.
  try { localStorage.setItem('elvanniril_language', appLanguage === 'ta' ? 'ta' : 'en'); } catch (e) { /* ignore */ }

  if (!root) root = createRoot(document.getElementById('root'));

  await new Promise((resolve) => {
    root.render(
      <LanguageProvider key={token}>
        <Mounted onMounted={resolve}>{renderDocument(current.docType, props)}</Mounted>
      </LanguageProvider>
    );
  });

  // Let the paper, fonts, images and the async UPI QR code settle before reporting ready.
  const paper = await waitFor(paperElement);
  try { await document.fonts.ready; } catch (e) { /* ignore */ }
  await nextFrame();
  await wait(250);
  if (paper) await waitForImages(paper);
  await nextFrame();
  await nextFrame();

  if (token === renderToken) notify(paper ? 'onReady' : 'onError', ...(paper ? [] : ['Template did not render']));
};

// Paper sizes in mm (portrait: width x height)
const PAPER_MM = { A4: [210, 297], A5: [148, 210], Letter: [215.9, 279.4], Legal: [215.9, 355.6] };
const DEFAULT_PDF_OPTIONS = { paper: 'A4', orientation: 'portrait', marginMm: 0, color: true, fit: 'width' };

// Converts an rgb()/rgba() colour to its grey equivalent (keeps alpha).
const toGray = (css) => {
  const m = /rgba?\(([^)]+)\)/.exec(css);
  if (!m) return css;
  const [r, g, b, a] = m[1].split(',').map(x => parseFloat(x));
  const y = Math.round(0.299 * r + 0.587 * g + 0.114 * b);
  return a !== undefined && a < 1 ? `rgba(${y},${y},${y},${a})` : `rgb(${y},${y},${y})`;
};
const GRAY_PROPS = ['color', 'background-color', 'border-top-color', 'border-right-color', 'border-bottom-color', 'border-left-color', 'outline-color', 'text-decoration-color', 'fill', 'stroke'];

// Black & white without rasterising: every colour on the clone becomes its grey value, so text
// stays real text. Only images (logo, signature, QR) get an image filter.
const grayscaleClone = (orig, clone) => {
  const o = [orig, ...orig.querySelectorAll('*')];
  const c = [clone, ...clone.querySelectorAll('*')];
  o.forEach((el, i) => {
    const cs = getComputedStyle(el);
    for (const prop of GRAY_PROPS) {
      const v = cs.getPropertyValue(prop);
      if (v && v.includes('rgb')) c[i].style.setProperty(prop, toGray(v), 'important');
    }
    if (el.tagName === 'IMG') c[i].style.setProperty('filter', 'grayscale(1)', 'important');
  });
};

const MM_PER_PX = 25.4 / 96;

// The contact band at the foot of each invoice: every page of a longer bill ends with it.
const CONTACT_BAND = { 'gst-invoice': '.inv-contact-block', 'coolie-invoice': '.contact-section' };
const PAGE_TOP_MM = 8; // the room above the lines when they carry on at the top of a later page
const PAGE_SAFE_MM = 4; // kept free above the contact band, in case a page prints a little taller than it measured

// Splits an invoice into pages: every page is the paper again (same look, band pinned at its foot) holding some of the
// lines. The lines are counted from their measured heights, the first page keeps the header, a later page starts with
// the table head, and the totals and the footer go with the last lines on the last page.
const paginate = (root, clone, pageMm) => {
  const bandSel = CONTACT_BAND[current.docType];
  const tb = root.querySelector('table tbody');
  const band = bandSel ? root.querySelector(bandSel) : null;
  if (!tb || !band || tb.children.length < 2) return null;
  const table = tb.closest('table');
  const rows = Array.from(tb.children);
  const box = (el) => el.getBoundingClientRect();

  root.style.setProperty('min-height', '0px', 'important'); // the natural height, not the page-high paper
  const rootBox = box(root), tbBox = box(tb), bandBox = box(band);
  const thead = table.querySelector('thead');
  const m = {
    before: tbBox.top - rootBox.top, // the header, the parties and the table head
    head: thead ? box(thead).height : 0,
    after: bandBox.top - tbBox.bottom, // the totals and the footer
    foot: rootBox.height - (bandBox.top - rootBox.top), // the contact band
    rows: rows.map((r) => box(r).height),
  };
  root.style.removeProperty('min-height');

  const pageH = pageMm / MM_PER_PX;
  const room = (first) => pageH - (first ? m.before : PAGE_TOP_MM / MM_PER_PX + m.head) - m.foot - PAGE_SAFE_MM / MM_PER_PX;
  const sum = (a, b) => m.rows.slice(a, b).reduce((x, y) => x + y, 0);
  const pack = (end) => { // the ranges of lines that fill each page, up to line [end)
    const out = [];
    let a = 0;
    while (a < end) {
      let b = a + 1;
      while (b < end && sum(a, b + 1) <= room(out.length === 0)) b++;
      out.push([a, b]);
      a = b;
    }
    return out;
  };
  let ranges = pack(rows.length);
  const last = ranges[ranges.length - 1];
  if (sum(last[0], last[1]) + m.after > room(ranges.length === 1)) {
    // The totals and the footer do not fit under the last lines: the last two lines go with them on a page of their own
    const keep = Math.min(2, rows.length - 1);
    ranges = [...pack(rows.length - keep), [rows.length - keep, rows.length]];
  }
  if (ranges.length < 2) return null;

  return ranges.map(([a, b], i) => {
    const page = clone.cloneNode(true);
    const pageBody = page.querySelector('table tbody');
    const pageTable = pageBody.closest('table');
    Array.from(pageBody.children).forEach((row, k) => { if (k < a || k >= b) row.remove(); });
    let holder = pageTable;
    while (holder.parentElement !== page) holder = holder.parentElement;
    if (i > 0) {
      for (const c of Array.from(page.children)) {
        if (c === holder) break;
        if (c.tagName !== 'STYLE') c.remove();
      }
      const gap = document.createElement('div');
      gap.style.cssText = `flex: none; height: ${PAGE_TOP_MM}mm`;
      page.insertBefore(gap, holder);
    }
    if (i < ranges.length - 1) {
      let n = pageTable.nextElementSibling;
      while (n) { const next = n.nextElementSibling; n.remove(); n = next; }
      const tfoot = pageTable.querySelector('tfoot');
      if (tfoot) tfoot.remove();
      let c = holder.nextElementSibling;
      while (c) {
        const next = c.nextElementSibling;
        const spacer = c.children.length === 0 && !c.textContent.trim();
        if (!c.matches(bandSel) && !spacer) c.remove();
        c = next;
      }
    }
    for (const [k, v] of Object.entries({ height: `${pageMm}mm`, 'min-height': `${pageMm}mm`, overflow: 'hidden' })) page.style.setProperty(k, v, 'important');
    return page.outerHTML;
  });
};

// The document the host prints: the page's <head> + print rules + the paper only, laid out for the chosen
// print options. The host prints it with Chromium itself, so the result is a real vector PDF.
//  - page size / margins / orientation come from the options (@page),
//  - the sheet keeps its A4 design width (210mm) and is zoomed to fit the page content width,
//    or to fit one whole page ("fit page"),
//  - black & white converts the colours to grey.
const printHtml = (options) => {
  const o = { ...DEFAULT_PDF_OPTIONS, ...(options || {}) };
  const printContent = paperElement();
  if (!printContent) return '';
  const cfg = DOC_TYPES[current.docType];
  const title = cfg.title(current.props);
  const headContent = document.head.innerHTML;

  const [shortSide, longSide] = PAPER_MM[o.paper] || PAPER_MM.A4;
  const landscape = o.orientation === 'landscape';
  const pageW = landscape ? longSide : shortSide;
  const pageH = landscape ? shortSide : longSide;
  const margin = Number(o.marginMm) || 0;
  const contentW = pageW - 2 * margin;
  const contentH = pageH - 2 * margin;

  const sheetW = 210; // mm: the width every sheet is designed for
  const sheetH = Math.max(1, printContent.scrollHeight * MM_PER_PX);
  let zoom = contentW / sheetW;
  if (o.fit === 'page') zoom = Math.min(zoom, contentH / sheetH);
  // A bill with more lines than one page holds runs on to the next page. A one-page bill keeps the paper as tall as
  // the page (the contact band sits at its foot); a longer one is as tall as its content, and the band follows it.
  const pages = Math.max(1, Math.ceil((sheetH * zoom) / contentH - 0.002));
  const paperH = pages === 1 ? contentH / zoom : 0;

  const clone = printContent.cloneNode(true);
  if (!o.color) grayscaleClone(printContent, clone);

  // More than one page: the lines are split over pages that each end with the contact band.
  let paper = clone.outerHTML;
  if (pages > 1 && o.fit !== 'page') {
    const split = paginate(printContent, clone, contentH / zoom);
    if (split) paper = split.join('');
  }

  return `
      <!DOCTYPE html>
      <html>
        <head>
          <title>${title}</title>
          ${headContent}
          <style>
            @media print {${cfg.printRules}
            }
          </style>
          <style>
            @page { size: ${pageW}mm ${pageH}mm; margin: ${margin}mm; }
            html, body { position: static !important; width: auto !important; height: auto !important; min-height: 0 !important;
                         overflow: visible !important; margin: 0 !important; padding: 0 !important; background: #fff !important; }
            .udk-sheet { width: ${sheetW}mm; margin: 0 auto; zoom: ${zoom}; }
            .udk-sheet #invoice-preview, .udk-sheet .a4-paper { height: auto !important; min-height: ${paperH}mm !important; }
            .udk-sheet #invoice-preview > *, .udk-sheet .a4-paper > * { flex-shrink: 0 !important; }
            .udk-sheet > #invoice-preview, .udk-sheet > .a4-paper { break-after: page; page-break-after: always; }
            .udk-sheet > #invoice-preview:last-child, .udk-sheet > .a4-paper:last-child { break-after: auto; page-break-after: auto; }
            .udk-sheet tr, .udk-sheet .inv-totals-section, .udk-sheet .inv-footer, .udk-sheet .udk-footer, .udk-sheet .inv-contact-block, .udk-sheet .contact-section { break-inside: avoid; page-break-inside: avoid; }
            * { -webkit-print-color-adjust: exact; print-color-adjust: exact; }
          </style>
        </head>
        <body style="background-color: white; margin: 0; padding: 0;">
          <div class="udk-sheet">${paper}</div>
        </body>
      </html>
    `;
};

// vadivam.css sizes html/body/#root with 100dvh, which an Android WebView created before it has a size
// resolves to 0 (everything clipped). Use 100% instead, and let the host's background show.
for (const el of [document.documentElement, document.body, document.getElementById('root')]) {
  if (el) {
    el.style.background = 'transparent';
    el.style.height = '100%';
  }
}

const setTopInset = () => {};

// No image-based PDF in this bundle: say so at once so the host shows its failure notice instead of waiting.
const buildPdf = async () => {
  notify('onError', 'Image PDF fallback is not part of this bundle');
  return null;
};

// The top of a bill for the logo adjustment screen: the real template, shrunk to the screen width and cut off after
// `cropPx` pixels of the paper. previewSettings() changes the print settings (logo size and place) and redraws at once.
let previewState = null;
const PREVIEW_PAPER_PX = 794;

const fitPreview = (cropPx) => {
  const paper = paperElement();
  if (!paper) return;
  const scale = window.innerWidth / PREVIEW_PAPER_PX;
  const style = (el, css) => Object.entries(css).forEach(([k, v]) => el.style.setProperty(k, v, 'important'));
  style(document.documentElement, { overflow: 'hidden', background: '#fff' });
  style(document.body, {
    margin: '0', background: '#fff', width: `${PREVIEW_PAPER_PX}px`, height: `${cropPx}px`, overflow: 'hidden',
    transform: `scale(${scale})`, 'transform-origin': '0 0',
  });
  style(paper, { margin: '0', 'box-shadow': 'none', width: `${PREVIEW_PAPER_PX}px`, border: 'none' });
};

const drawPreview = () => {
  const { data, docType, token } = previewState;
  root.render(
    <LanguageProvider key={token}>{renderDocument(docType, toProps(data))}</LanguageProvider>
  );
};

const preview = async (payload, cropPx = 300) => {
  const data = typeof payload === 'string' ? JSON.parse(payload) : payload;
  const docType = DOC_TYPES[data.docType] ? data.docType : 'gst-invoice';
  try { localStorage.setItem('elvanniril_language', data.appLanguage === 'ta' ? 'ta' : 'en'); } catch (e) { /* ignore */ }
  if (!root) root = createRoot(document.getElementById('root'));
  previewState = { data, docType, token: ++renderToken, cropPx };
  current = { docType, props: null };
  await new Promise((resolve) => {
    root.render(
      <LanguageProvider key={previewState.token}>
        <Mounted onMounted={resolve}>{renderDocument(docType, toProps(data))}</Mounted>
      </LanguageProvider>
    );
  });
  const paper = await waitFor(paperElement);
  try { await document.fonts.ready; } catch (e) { /* ignore */ }
  await nextFrame();
  if (paper) await waitForImages(paper);
  fitPreview(cropPx);
  notify(paper ? 'onReady' : 'onError', ...(paper ? [] : ['Template did not render']));
};

const previewSettings = (settings) => {
  if (!previewState) return;
  const s = typeof settings === 'string' ? JSON.parse(settings) : settings;
  previewState.data.profile.printSettings = { ...(previewState.data.profile.printSettings || {}), ...s };
  drawPreview();
};

window.UdukkaiPrint = { render, renderProps, buildPdf, printHtml, setTopInset, preview, previewSettings, version: TEMPLATE_VERSION };
notify('onLoaded');
