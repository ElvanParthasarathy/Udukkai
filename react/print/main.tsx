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
import CoolieInvoiceView from '../moolam/pagudhigal/CoolieBill/CoolieInvoiceView';
import CoolieReceiptView from '../moolam/pagudhigal/CoolieBill/CoolieReceiptView';
import { setCoolieProfiles } from './shims/Avanam';
import { toGstInvoiceProps, toGstReceiptProps, toCoolieInvoiceProps, toCoolieReceiptProps } from './adapter';

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
              .receipt-box, .invoice-paper { box-shadow: none !important; margin: 0 !important; border: none !important; width: 100% !important; }
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
    paper: '.print-area', frame: '.invoice-paper', clone: '.receipt-box', cloneStyle: PAPER_RESET,
    extraPages: false, printRules: RECEIPT_PRINT_RULES,
    title: (p) => (p?.receipt?.receiptNo ? `Receipt-${p.receipt.receiptNo}` : 'Print'),
  },
};

// Mirrors the mobile preview frame in React's InvoiceView (Paper elevation 8,
// 12px radius, scaled to fit the screen width with 16px side gutters).
// The receipt and Coolie views draw this frame themselves.
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
      return <CoolieInvoiceView bill={props.bill} onClose={noop} onEdit={noop} />;
    case 'coolie-receipt':
      return <CoolieReceiptView receipt={props.receipt} onBack={noop} onEdit={noop} />;
    default:
      return <PreviewFrame><SjsTheme {...props} /></PreviewFrame>;
  }
};

let root = null;
let renderToken = 0;
let current = { docType: 'gst-invoice', props: null };

const paperElement = () => document.querySelector(DOC_TYPES[current.docType].paper);

const toProps = (data) => {
  switch (data.docType) {
    case 'gst-receipt': return toGstReceiptProps(data);
    case 'coolie-invoice': return toCoolieInvoiceProps(data);
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
          <div className="print-scroll" style={{ width: '100%', height: '100%', overflowY: 'auto', overflowX: 'hidden', padding: '12px 0 24px' }}>
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

// Same pipeline as buildPDF in each React view.
const buildPdfDocument = async () => {
  const cfg = DOC_TYPES[current.docType];
  const paper = paperElement();
  const frameEl = paper.closest(cfg.frame);
  const origTransform = frameEl ? frameEl.style.transform : '';
  const origZoom = frameEl ? frameEl.style.zoom : '';
  if (frameEl) {
    frameEl.style.transform = 'none';
    frameEl.style.zoom = '1';
  }

  const pdf = new jsPDF({ orientation: 'portrait', unit: 'mm', format: 'a4', compress: true });
  const pdfWidth = pdf.internal.pageSize.getWidth();
  const pdfPageHeight = pdf.internal.pageSize.getHeight();
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

  const mainImg = mainCanvas.toDataURL('image/jpeg', 0.95);
  const mainImgHeight = (mainCanvas.height * pdfWidth) / mainCanvas.width;
  if (mainImgHeight <= pdfPageHeight + 2) {
    pdf.addImage(mainImg, 'JPEG', 0, 0, pdfWidth, Math.min(mainImgHeight, pdfPageHeight), undefined, 'MEDIUM');
  } else {
    let heightLeft = mainImgHeight, position = 0;
    pdf.addImage(mainImg, 'JPEG', 0, position, pdfWidth, mainImgHeight, undefined, 'MEDIUM');
    heightLeft -= pdfPageHeight;
    while (heightLeft > 2) { position -= pdfPageHeight; pdf.addPage(); pdf.addImage(mainImg, 'JPEG', 0, position, pdfWidth, mainImgHeight, undefined, 'MEDIUM'); heightLeft -= pdfPageHeight; }
  }

  for (const pageEl of extraPages) {
    const c = await html2canvas(pageEl, {
      ...captureOptions(pageEl),
      onclone: (cd) => { cd.querySelectorAll('*').forEach(n => { n.style.letterSpacing = '0px'; n.style.wordSpacing = '0px'; }); }
    });
    pdf.addPage();
    pdf.addImage(c.toDataURL('image/jpeg', 0.95), 'JPEG', 0, 0, pdfWidth, Math.min((c.height * pdfWidth) / c.width, pdfPageHeight), undefined, 'MEDIUM');
  }

  if (frameEl) {
    frameEl.style.transform = origTransform;
    frameEl.style.zoom = origZoom;
  }
  return pdf;
};

const buildPdf = async () => {
  try {
    if (!paperElement()) throw new Error('Nothing rendered yet');
    const pdf = await buildPdfDocument();
    const base64 = pdf.output('datauristring').split(',')[1];
    notify('onPdf', base64);
    return base64;
  } catch (e) {
    console.error(e);
    notify('onError', String(e?.message || e));
    return null;
  }
};

// Same document each React view's handlePrint builds: the app <head> + print
// rules, and a body holding only the paper (not the preview frame).
const printHtml = () => {
  const printContent = paperElement();
  if (!printContent) return '';
  const cfg = DOC_TYPES[current.docType];
  const title = cfg.title(current.props);
  const headContent = document.head.innerHTML;
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
        </head>
        <body style="background-color: white; margin: 0; padding: 0;">
          ${printContent.outerHTML}
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

window.UdukkaiPrint = { render, renderProps, buildPdf, printHtml };
notify('onLoaded');
