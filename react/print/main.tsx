// @ts-nocheck
// Print-only entry used by the KMP app. It renders the React print templates
// exactly as the React app does (same components, same CSS, same fonts), prints
// the same paper-only document and builds the PDF with the same html2canvas +
// jsPDF pipeline as each React view.
//
// Bridge:
//   window.UdukkaiPrint.render(payload)  -> renders, then calls UdukkaiNative.onReady()
//   window.UdukkaiPrint.printHtml()      -> the document React's handlePrint builds
//   window.UdukkaiPrint.buildPdf()       -> calls UdukkaiNative.onPdf(base64) or onError(msg)
import { useEffect } from 'react';
import { createRoot } from 'react-dom/client';
import { Paper } from '@mui/material';
import { jsPDF } from 'jspdf';
import html2canvas from 'html2canvas';
// Same global stylesheets the React app loads (both are global there too).
import '../moolam/vadivam.css';
import '../moolam/pagudhigal/CoolieBill/print.css';
import { LanguageProvider } from '../moolam/mozhi/LanguageContext';
import SjsTheme from '../moolam/pagudhigal/GstBill/SjsTheme';
import ReceiptView from '../moolam/pagudhigal/GstBill/Receipts/ReceiptView';
import CoolieInvoiceBilingual from '../moolam/pagudhigal/CoolieBill/CoolieInvoiceBilingual';
import CoolieReceiptView from '../moolam/pagudhigal/CoolieBill/CoolieReceiptView';
import { setCoolieProfiles } from './shims/Avanam';
import { toGstInvoiceProps, toGstReceiptProps, toCoolieBillProps, toCoolieReceiptProps } from './adapter';

const native = () => window.UdukkaiNative;

const notify = (method, ...args) => {
  try {
    const n = native();
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

const noop = () => {};

// Inline print rules each React view adds in handlePrint.
const INVOICE_PRINT_RULES = `
              @page { size: A4; margin: 0; }
              html { font-size: 16px !important; }
              body, html { background-color: white !important; margin: 0; padding: 0; }
              .invoice-paper, .a4-paper { box-shadow: none !important; margin: 0 !important; border: none !important; width: 100% !important; }
              .no-print { display: none !important; }
              #invoice-preview { padding: 0 !important; }
              #invoice-preview .inv-classic-header { padding-left: 1.5rem !important; padding-right: 1.5rem !important; padding-top: 1rem !important; }
              #invoice-preview .inv-parties { padding-left: 1.5rem !important; padding-right: 1.5rem !important; }
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

const PAPER_RESET = { width: '210mm', overflow: 'visible', minHeight: '297mm', border: 'none', boxShadow: 'none', borderRadius: '0' };

// Per-template settings, copied from each React view's handlePrint / buildPDF.
const DOC_TYPES = {
  'gst-invoice': {
    paper: '#invoice-preview', frame: '.invoice-paper', clone: '#invoice-preview', cloneStyle: PAPER_RESET,
    extraPages: true, printRules: INVOICE_PRINT_RULES,
    title: (p) => (p?.details?.invoiceNumber ? `Invoice-${p.details.invoiceNumber}` : 'Print'),
  },
  'gst-receipt': {
    paper: '.print-area', frame: '.MuiPaper-root', clone: '.receipt-box',
    cloneStyle: { width: '210mm', maxWidth: '210mm', overflow: 'visible', minHeight: 'unset', border: 'none', boxShadow: 'none', borderRadius: '0' },
    extraPages: false, printRules: RECEIPT_PRINT_RULES,
    title: (p) => (p?.receipt?.receiptNo ? `Receipt-${p.receipt.receiptNo}` : 'Print'),
  },
  'coolie-invoice': {
    paper: '.a4-paper', frame: '.invoice-paper', clone: '.a4-paper', cloneStyle: PAPER_RESET,
    extraPages: true, printRules: COOLIE_INVOICE_PRINT_RULES,
    title: (p) => (p?.bill?.bill_no ? `Invoice-${p.bill.bill_no}` : 'Print'),
  },
  'coolie-receipt': {
    paper: '.print-area', frame: '.invoice-paper', clone: '.receipt-box, .a4-paper', cloneStyle: PAPER_RESET,
    extraPages: false, printRules: RECEIPT_PRINT_RULES,
    title: (p) => (p?.receipt?.receiptNo ? `Receipt-${p.receipt.receiptNo}` : 'Print'),
  },
};

// Mirrors the mobile preview frame in React's InvoiceView (Paper elevation 8,
// 12px radius, scaled to fit the screen width with 16px side gutters, pinch to
// zoom 1x-4x with React's own usePinchZoom). The receipt and Coolie views draw
// this frame themselves.
const PreviewFrame = ({ children }) => {
  const initialScale = Math.min((window.innerWidth - 32) / 793.7, 1);
  const mbPercent = (1 - initialScale) * 141;
  return (
    <div style={{ width: '100%', overflow: 'hidden', display: 'flex', justifyContent: 'center', padding: '0 16px', boxSizing: 'border-box' }}>
      <div style={{ transformOrigin: 'top center', width: '100%' }}>
        <Paper
          elevation={8}
          className="invoice-paper print-wrapper"
          sx={{
            p: 0, overflow: 'hidden', minWidth: '210mm', width: '210mm', m: '0 auto',
            zoom: 'none',
            transformOrigin: 'top left',
            transform: `scale(${initialScale})`,
            mb: `-${mbPercent}%`,
            borderRadius: '12px',
          }}
        >
          {children}
        </Paper>
      </div>
    </div>
  );
};

const Mounted = ({ onMounted, children }) => {
  useEffect(() => { onMounted(); }, []);
  return children;
};

const renderDocument = (docType, props) => {
  switch (docType) {
    case 'gst-receipt':
      return <ReceiptView receipt={props.receipt} profile={props.profile} onBack={noop} onEdit={noop} />;
    case 'coolie-invoice':
      return <PreviewFrame><CoolieInvoiceBilingual bill={props.bill} /></PreviewFrame>;
    case 'coolie-receipt':
      return <CoolieReceiptView receipt={props.receipt} onBack={noop} onEdit={noop} />;
    default:
      return <PreviewFrame><SjsTheme {...props} /></PreviewFrame>;
  }
};

let root = null;
let renderToken = 0;
let topInset = 0;
const setTopInset = (px) => { topInset = Number(px) || 0; };
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

// Renders a template straight from React-shaped props (also used to compare
// KMP's output against a bill exactly as the React app stores it).
const renderProps = async (props, appLanguage, docType = 'gst-invoice') => {
  const token = ++renderToken;
  current = { docType: DOC_TYPES[docType] ? docType : 'gst-invoice', props };
  if (props.coolieProfiles) setCoolieProfiles(props.coolieProfiles);

  // LanguageContext reads the UI language from localStorage on mount.
  try { localStorage.setItem('elvanniril_language', appLanguage === 'ta' ? 'ta' : 'en'); } catch (e) { /* ignore */ }

  if (!root) root = createRoot(document.getElementById('root'));

  await new Promise((resolve) => {
    root.render(
      <LanguageProvider key={token}>
        <Mounted onMounted={resolve}>
          <div className="print-scroll" style={{ width: '100%', height: '100%', overflow: 'hidden', padding: `${12 + topInset}px 0 24px` }}>
            {renderDocument(current.docType, props)}
          </div>
        </Mounted>
      </LanguageProvider>
    );
  });

  // Views that load their company profile asynchronously render the paper a tick later.
  const paper = await waitFor(paperElement);

  // Let fonts, images and the async UPI QR code settle before reporting ready.
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

// Copies a canvas region, optionally converted to black & white.
const cropCanvas = (src, y, h, grayscale) => {
  const out = document.createElement('canvas');
  out.width = src.width;
  out.height = h;
  const ctx = out.getContext('2d');
  ctx.fillStyle = '#ffffff';
  ctx.fillRect(0, 0, out.width, out.height);
  if (grayscale) ctx.filter = 'grayscale(1)';
  ctx.drawImage(src, 0, y, src.width, h, 0, 0, src.width, h);
  return out;
};

// Same capture settings as each React view's buildPDF (html2canvas + jsPDF), but the page is laid
// out from the chosen print options instead of a fixed A4.
const buildPdfDocument = async (options) => {
  const o = { ...DEFAULT_PDF_OPTIONS, ...(options || {}) };
  const cfg = DOC_TYPES[current.docType];
  const paper = paperElement();
  const frameEl = paper.closest(cfg.frame);
  const origTransform = frameEl ? frameEl.style.transform : '';
  const origZoom = frameEl ? frameEl.style.zoom : '';
  if (frameEl) {
    frameEl.style.transform = 'none';
    frameEl.style.zoom = '1';
  }

  const [shortSide, longSide] = PAPER_MM[o.paper] || PAPER_MM.A4;
  const landscape = o.orientation === 'landscape';
  const pageW = landscape ? longSide : shortSide;
  const pageH = landscape ? shortSide : longSide;
  const margin = Number(o.marginMm) || 0;
  const contentW = pageW - 2 * margin;
  const contentH = pageH - 2 * margin;

  const pdf = new jsPDF({ orientation: landscape ? 'landscape' : 'portrait', unit: 'mm', format: [shortSide, longSide], compress: true });
  const extraPages = cfg.extraPages ? paper.querySelectorAll('[data-pdf-page]') : [];
  const renderScale = Math.max(3, Math.round((window.devicePixelRatio || 1) * 2));

  const captureOptions = (el) => ({
    scale: renderScale,
    useCORS: true,
    logging: false,
    letterRendering: true,
    backgroundColor: '#ffffff',
    imageTimeout: 0,
    width: el.scrollWidth,
    height: el.scrollHeight,
  });

  let pageCount = 0;
  const addSheet = (canvas) => {
    const gray = !o.color;
    if (o.fit === 'page') {
      // whole sheet on one page, centred horizontally
      const s = Math.min(contentW / canvas.width, contentH / canvas.height);
      const w = canvas.width * s;
      const h = canvas.height * s;
      if (pageCount++) pdf.addPage();
      pdf.addImage(cropCanvas(canvas, 0, canvas.height, gray).toDataURL('image/jpeg', 0.95), 'JPEG', margin + (contentW - w) / 2, margin, w, h, undefined, 'MEDIUM');
    } else {
      // fit to the page width, continue on more pages when the sheet is taller
      const s = contentW / canvas.width;
      const slicePx = Math.max(1, Math.floor(contentH / s));
      // up to 2mm of overhang still counts as one page (as before), otherwise start a new page
      for (let y = 0; (canvas.height - y) * s > 2; y += slicePx) {
        const remaining = canvas.height - y;
        const h = remaining * s <= contentH + 2 ? remaining : slicePx;
        if (pageCount++) pdf.addPage();
        pdf.addImage(cropCanvas(canvas, y, h, gray).toDataURL('image/jpeg', 0.95), 'JPEG', margin, margin, contentW, h * s, undefined, 'MEDIUM');
      }
    }
  };

  extraPages.forEach(el => el.style.display = 'none');
  const mainCanvas = await html2canvas(paper, {
    ...captureOptions(paper),
    onclone: (clonedDoc) => {
      clonedDoc.querySelectorAll('*').forEach(n => { n.style.letterSpacing = '0px'; n.style.wordSpacing = '0px'; });
      const inv = clonedDoc.querySelector(cfg.clone);
      if (inv) Object.assign(inv.style, cfg.cloneStyle);
      if (cfg.extraPages) clonedDoc.querySelectorAll('[data-pdf-page]').forEach(el => el.style.display = 'none');
    }
  });
  extraPages.forEach(el => el.style.display = '');
  addSheet(mainCanvas);

  for (const pageEl of extraPages) {
    const c = await html2canvas(pageEl, {
      ...captureOptions(pageEl),
      onclone: (cd) => { cd.querySelectorAll('*').forEach(n => { n.style.letterSpacing = '0px'; n.style.wordSpacing = '0px'; }); }
    });
    addSheet(c);
  }

  if (frameEl) {
    frameEl.style.transform = origTransform;
    frameEl.style.zoom = origZoom;
  }
  return { pdf, pageCount };
};

// Renders the sheet (the paper only, never the page around it) to a sharp JPEG with the same
// html2canvas settings as the PDF, and hands it to the host app. The host shows this image in a
// gallery-style zoom viewer, so panning is bounded by the sheet and the theme colour shows around it.
const captureSheet = async () => {
  try {
    const cfg = DOC_TYPES[current.docType];
    const paper = paperElement();
    if (!paper) throw new Error('Nothing rendered yet');
    const frameEl = paper.closest(cfg.frame);
    const origTransform = frameEl ? frameEl.style.transform : '';
    const origZoom = frameEl ? frameEl.style.zoom : '';
    if (frameEl) {
      frameEl.style.transform = 'none';
      frameEl.style.zoom = '1';
    }
    const extraPages = cfg.extraPages ? paper.querySelectorAll('[data-pdf-page]') : [];
    extraPages.forEach(el => el.style.display = 'none');
    const width = paper.scrollWidth;
    const height = paper.scrollHeight;
    // 3x is sharp up to ~4x zoom; keep the longest side within a safe GPU texture size.
    const scale = Math.min(3, 4096 / Math.max(width, height));
    const canvas = await html2canvas(paper, {
      scale,
      useCORS: true,
      logging: false,
      letterRendering: true,
      backgroundColor: '#ffffff',
      imageTimeout: 0,
      width,
      height,
      onclone: (clonedDoc) => {
        clonedDoc.querySelectorAll('*').forEach(n => { n.style.letterSpacing = '0px'; n.style.wordSpacing = '0px'; });
        const inv = clonedDoc.querySelector(cfg.clone);
        if (inv) Object.assign(inv.style, cfg.cloneStyle);
        if (cfg.extraPages) clonedDoc.querySelectorAll('[data-pdf-page]').forEach(el => el.style.display = 'none');
      }
    });
    extraPages.forEach(el => el.style.display = '');
    if (frameEl) {
      frameEl.style.transform = origTransform;
      frameEl.style.zoom = origZoom;
    }
    notify('onSheet', canvas.toDataURL('image/jpeg', 0.95).split(',')[1]);
  } catch (e) {
    console.error(e);
    notify('onError', String(e?.message || e));
  }
};

const buildPdf = async (options) => {
  try {
    if (!paperElement()) throw new Error('Nothing rendered yet');
    const { pdf, pageCount } = await buildPdfDocument(options);
    const base64 = pdf.output('datauristring').split(',')[1];
    notify('onPdf', base64, pageCount);
    return base64;
  } catch (e) {
    console.error(e);
    notify('onError', String(e?.message || e));
    return null;
  }
};

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

// The document each React view's handlePrint builds (app <head> + print rules + the paper only),
// laid out for the chosen print options. It is printed by Chromium itself, so the result is a real
// vector PDF: text stays selectable and copyable.
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

  const clone = printContent.cloneNode(true);
  if (!o.color) grayscaleClone(printContent, clone);

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
            * { -webkit-print-color-adjust: exact; print-color-adjust: exact; }
          </style>
        </head>
        <body style="background-color: white; margin: 0; padding: 0;">
          <div class="udk-sheet">${clone.outerHTML}</div>
        </body>
      </html>
    `;
};

// Let the host app's background show around the paper (React shows the paper on
// the app's background). Inline styles so the print <head> stays identical.
// vadivam.css sizes html/body/#root with 100dvh, which an Android WebView created
// before it has a size resolves to 0 (everything clipped). Use 100% instead.
for (const el of [document.documentElement, document.body, document.getElementById('root')]) {
  if (el) {
    el.style.background = 'transparent';
    el.style.height = '100%';
  }
}

// The React views' own toolbars (back / edit / print / PDF / share) sit next to
// the paper inside the view's root .print-wrapper; KMP shows its own buttons, so
// hide them on screen. (The print document only contains the paper.)
const hideToolbars = document.createElement('style');
hideToolbars.textContent = '.no-print, .print-scroll > .print-wrapper > :not(.print-wrapper) { display: none !important; }';
document.head.appendChild(hideToolbars);

// Native pinch-zoom: React's views lock touch-action to 'none' for their own zoom code.
const allowNativeZoom = document.createElement('style');
allowNativeZoom.textContent = 'html, body, #root, #root * { touch-action: auto !important; }';
document.head.appendChild(allowNativeZoom);

// Tell the host app when the page is zoomed (it hides the title bar text while zoomed in).
if (window.visualViewport) {
  window.visualViewport.addEventListener('resize', () => notify('onZoom', window.visualViewport.scale));
}

window.UdukkaiPrint = { render, renderProps, buildPdf, printHtml, setTopInset, captureSheet };
notify('onLoaded');
