// @ts-nocheck
// PrintableViewWrapper — extracted from the working InvoiceView.tsx
// Provides: A4 paper shell, pinch-zoom, PDF generation, share, print
// Usage:  <PrintableViewWrapper data={bill.data} typeConfig={...} onBack={...} onEdit={...}>
//           {(printRef, mergedOptions) => <YourTheme ref={printRef} ... options={mergedOptions} />}
//         </PrintableViewWrapper>

import { useRef, useState, useEffect } from 'react';
import { jsPDF } from 'jspdf';
import html2canvas from 'html2canvas';
import { thagaval } from './Thagaval';
import { useLanguage } from '../mozhi/LanguageContext';
import { formatCurrency, INVOICE_TYPES, getPrintHeadContent } from '../Payanpadu';
import { Box, Paper } from '@mui/material';
import { usePinchZoom } from '../hooks/usePinchZoom';
import { useTheme } from '@mui/material/styles';
import useMediaQuery from '@mui/material/useMediaQuery';
import { ViewHeader } from './ViewHeader';
import NativeDocument from './NativeDocument';
import { Capacitor } from '@capacitor/core';

export function PrintableViewWrapper({ data, typeConfig, onBack, onEdit, children }) {
  const { t } = useLanguage();
  const printRef = useRef(null);
  const [saving, setSaving] = useState(false);
  const [displayOptions, setDisplayOptions] = useState({});
  
  const theme = useTheme();
  const isNative = typeof window !== 'undefined' && (window).FlutterBridge && (window).FlutterBridge.isNativeApp && (window).FlutterBridge.isNativeApp();
  const isMobile = useMediaQuery(theme.breakpoints.down('sm')) || isNative;
  const initialScale = typeof window !== 'undefined' ? Math.min((window.innerWidth - 32) / 793.7, 1) : 0.43;
  const mbPercent = (1 - initialScale) * 141;

  // Only use JS pinch-zoom for web; native WebView has its own builtInZoomControls
  const { wrapperRef, contentRef, scale } = usePinchZoom({ minScale: 1, maxScale: 4 });

  useEffect(() => {
    const savedLocal = localStorage.getItem('elvanniril_invoiceOptions');
    const local = savedLocal ? JSON.parse(savedLocal) : {};
    setDisplayOptions(local);
    if (!isNative) {
      try {
        if (typeof getInvoiceDisplayOptions === 'function') {
          getInvoiceDisplayOptions().then(serverOpts => {
            if (serverOpts) setDisplayOptions(prev => ({ ...prev, ...serverOpts }));
          });
        }
      } catch (e) { /* not available in native */ }
    }
  }, [isNative]);

  if (!data) return null;

  const {
    client, details, items, totals, invoiceType = 'tax-invoice',
    customTerms, invoiceOptions
  } = data;

  const mergedOptions = { ...displayOptions, ...invoiceOptions };

  const buildPDF = async () => {
    const paperEl = printRef.current.closest('.invoice-paper');
    const origTransform = paperEl ? paperEl.style.transform : '';
    const origZoom = paperEl ? paperEl.style.zoom : '';
    if (paperEl) {
      paperEl.style.transform = 'none';
      paperEl.style.zoom = '1';
    }

    const pdf = new jsPDF({ orientation: 'portrait', unit: 'mm', format: 'a4', compress: true });
    const pdfWidth = pdf.internal.pageSize.getWidth();
    const pdfPageHeight = pdf.internal.pageSize.getHeight();
    const extraPages = printRef.current.querySelectorAll('[data-pdf-page]');
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
    const mainCanvas = await html2canvas(printRef.current, {
      ...captureOptions(printRef.current),
      onclone: (clonedDoc) => {
        clonedDoc.querySelectorAll('*').forEach(n => { n.style.letterSpacing = '0px'; n.style.wordSpacing = '0px'; });
        const inv = clonedDoc.getElementById('invoice-preview');
        if (inv) { inv.style.width = '210mm'; inv.style.overflow = 'visible'; inv.style.minHeight = '297mm'; inv.style.border = 'none'; inv.style.boxShadow = 'none'; inv.style.borderRadius = '0'; }
        clonedDoc.querySelectorAll('[data-pdf-page]').forEach(el => el.style.display = 'none');
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

    if (paperEl) {
      paperEl.style.transform = origTransform;
      paperEl.style.zoom = origZoom;
    }
    return pdf;
  };

  const generatePDF = async () => {
    if (!printRef.current) return;
    try {
      setSaving(true);
      const pdf = await buildPDF();
      const fileName = `${typeConfig.prefix}_${(details.invoiceNumber || '').replace(/\//g, '-')}.pdf`;
      
      if (Capacitor.isNativePlatform()) {
        const pdfBase64 = pdf.output('datauristring').split(',')[1];
        await NativeDocument.downloadPdf({
          base64Data: pdfBase64,
          filename: fileName,
          appMode: 'Niril Silk',
          category: typeConfig.label || 'Invoice'
        });
        thagaval(`Downloaded to Niril Silk/${typeConfig.label || 'Invoice'}/`, 'success');
      } else {
        pdf.save(fileName);
        const pdfBlob = pdf.output('blob');
        const invoiceDate = details.invoiceDate ? new Date(details.invoiceDate) : new Date();
        const monthName = invoiceDate.toLocaleString('en-IN', { month: 'long', year: 'numeric' });
        const clientName = client?.name || 'General';
        const params = new URLSearchParams({ name: fileName, client: clientName, month: monthName });
        fetch(`/api/save-pdf?${params}`, { method: 'POST', headers: { 'Content-Type': 'application/pdf' }, body: pdfBlob }).catch(() => {});
        thagaval(`Downloaded & saved to Saved Invoices/${clientName}/`, 'success');
      }
    } catch (err) {
      console.error(err);
      thagaval('Failed to generate PDF.', 'error');
    } finally {
      setSaving(false);
    }
  };

  const handleNativeShare = async () => {
    const amount = formatCurrency(items.reduce((s, i) => s + ((i.qty || i.quantity) * i.rate), 0));
    const msg = `*${typeConfig.label || 'Invoice'}: ${details.invoiceNumber}*\nClient: ${client?.name || ''}\nAmount: ${amount}\nDate: ${details.invoiceDate}`;
    
    setSaving(true);
    try {
      const pdf = await buildPDF();
      const fileName = `${typeConfig.prefix}_${(details.invoiceNumber || '').replace(/\//g, '-')}.pdf`;
      
      if (Capacitor.isNativePlatform()) {
        const pdfBase64 = pdf.output('datauristring').split(',')[1];
        await NativeDocument.sharePdf({
          base64Data: pdfBase64,
          filename: fileName
        });
      } else if (navigator.share) {
        const pdfBlob = pdf.output('blob');
        const file = new File([pdfBlob], fileName, { type: 'application/pdf' });
        
        if (navigator.canShare && navigator.canShare({ files: [file] })) {
          navigator.share({ files: [file], title: fileName, text: msg }).catch((err) => {
            if (err.name === 'InvalidStateError') {
              thagaval('Share menu is already open! Please check your taskbar or behind your browser window.', 'warning');
            } else if (err.name !== 'AbortError') {
              console.error('Share failed', err);
            }
          });
        } else {
          navigator.share({ title: fileName, text: msg }).catch((err) => {
            if (err.name === 'InvalidStateError') {
              thagaval('Share menu is already open! Please check your taskbar or behind your browser window.', 'warning');
            } else if (err.name !== 'AbortError') {
              console.error('Share failed', err);
            }
          });
          pdf.save(fileName);
        }
      } else {
        thagaval(t('featureNotSupported') || 'Native sharing not supported on this device.', 'warning');
      }
    } catch (error) {
      console.error('PDF Generation failed', error);
      thagaval('Failed to generate PDF for sharing', 'error');
    } finally {
      setTimeout(() => setSaving(false), 2000);
    }
  };

  const handlePrint = async () => {
    const printContent = printRef.current;
    if (!printContent) return;

    const title = details?.invoiceNumber ? `${typeConfig.label || 'Invoice'}-${details.invoiceNumber}` : 'Print';
    const headContent = await getPrintHeadContent();

    const htmlContent = `
      <!DOCTYPE html>
      <html>
        <head>
          <title>${title}</title>
          ${headContent}
          <style>
            @media print {
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
              #invoice-preview .inv-contact-block { padding-left: 1.5rem !important; padding-right: 1.5rem !important; }
            }
          </style>
        </head>
        <body style="background-color: white; margin: 0; padding: 0;">
          ${printContent.outerHTML}
        </body>
      </html>
    `;

    if (Capacitor.isNativePlatform()) {
      try {
        setSaving(true);
        const fileName = `${typeConfig.prefix}_${(details.invoiceNumber || '').replace(/\//g, '-')}`;
        await NativeDocument.printHtml({
          html: htmlContent,
          baseUrl: "file:///android_asset/public/",
          filename: fileName
        });
      } catch (err) {
        console.error(err);
        thagaval('Failed to print document.', 'error');
      } finally {
        setSaving(false);
      }
      return;
    }

    // @ts-ignore
    if (typeof window !== 'undefined' && window.FlutterBridge) {
        // @ts-ignore
        if (window.FlutterBridge.printInvoice) {
            // @ts-ignore
            window.FlutterBridge.printInvoice();
        }
        return;
    }

    const iframe = document.createElement('iframe');
    iframe.style.position = 'fixed';
    iframe.style.right = '0';
    iframe.style.bottom = '0';
    iframe.style.width = '0';
    iframe.style.height = '0';
    iframe.style.border = '0';
    document.body.appendChild(iframe);

    const doc = iframe.contentWindow.document;
    doc.open();
    doc.write(htmlContent);
    doc.close();

    setTimeout(() => {
      iframe.contentWindow.focus();
      iframe.contentWindow.print();
      setTimeout(() => { document.body.removeChild(iframe); }, 1000);
    }, 500);
  };

  // Render the theme child via render-prop pattern: children(printRef, mergedOptions)
  const themeContent = typeof children === 'function' ? children(printRef, mergedOptions) : children;

  return (
    <Box className="print-wrapper" sx={{ py: { xs: 1.5, md: 4 }, px: { xs: 0, md: 4 }, maxWidth: 1200, mx: 'auto', width: '100%', position: 'relative', bgcolor: 'background.default', minHeight: '100vh', display: 'flex', flexDirection: 'column' }}>
      {!isNative && (
        <ViewHeader 
          className="no-print"
          onEdit={onEdit}
          onPrint={handlePrint}
          onPDF={generatePDF}
          onShare={handleNativeShare}
          saving={saving}
          title={details?.invoiceNumber}
          onBack={onBack}
        />
      )}

      {/* Centered Preview Container */}
      <Box className="print-wrapper" sx={{ flexGrow: 1, display: 'flex', justifyContent: 'center', alignItems: 'flex-start', overflowX: 'hidden', pb: 4, width: '100%' }}>
        {isMobile ? (
          <div ref={wrapperRef} style={{ width: "100%", overflow: "hidden", touchAction: "none", display: "flex", justifyContent: "center", padding: "0 16px", boxSizing: "border-box" }}>
            <div ref={contentRef} style={{ transformOrigin: "top center", width: "100%" }}>
              <Paper elevation={isNative ? 3 : 8} className="invoice-paper print-wrapper" sx={{ 
                                p: 0, overflow: 'hidden', minWidth: '210mm', width: '210mm', m: '0 auto', bgcolor: 'white', color: 'black',
                                zoom: 'none',
                                  transformOrigin: 'top left',
                                  transform: `scale(${initialScale})`,
                                  mb: `-${mbPercent}%`,
                                  borderRadius: isNative ? '0' : '12px',
                                  '@media print': { 
                                    zoom: '1 !important', 
                                    transform: 'none !important',
                                    mb: '0 !important',
                                    boxShadow: 'none !important',
                                    bgcolor: 'white !important',
                                    color: 'black !important',
                                    borderRadius: '0 !important'
                                  }
                            }}>
                {themeContent}
              </Paper>
            </div>
          </div>
        ) : (
          <Paper elevation={3} className="invoice-paper print-wrapper" sx={{ 
            p: 0, overflow: 'hidden', minWidth: '210mm', width: '210mm', m: '0 auto',
            zoom: { xs: 0.43, sm: 0.7, md: 0.85, lg: 1 },
            '@supports not (zoom: 1)': {
              transformOrigin: 'top center',
              transform: { xs: 'scale(0.43)', sm: 'scale(0.7)', md: 'scale(0.85)', lg: 'none' },
              mb: { xs: '-55%', sm: '-25%', md: '-10%', lg: 0 }
            }
          }}>
            {themeContent}
          </Paper>
        )}
      </Box>
    </Box>
  );
}
