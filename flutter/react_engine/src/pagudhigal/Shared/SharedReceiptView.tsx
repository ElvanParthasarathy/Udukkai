import React, { useRef, useState } from 'react';
import { usePinchZoom } from '../../hooks/usePinchZoom';
import { ArrowLeft, Printer as PrintIcon, ShareNetwork, Spinner, DownloadSimple, PencilSimple, DotsThreeVertical } from '@phosphor-icons/react';
import { FloatingBackButton } from '../FloatingBackButton';
import { jsPDF } from 'jspdf';
import html2canvas from 'html2canvas';
import { useLanguage } from '../../mozhi/LanguageContext';
import { en } from '../../mozhi/en';
import { ta } from '../../mozhi/ta';
import { formatCurrency, numberToWords, getCountryConfig, getDynamicField, getBilingualStateName, getPrintHeadContent } from '../../Payanpadu';
import { Box, Paper, useTheme, useMediaQuery } from '@mui/material';
import { ViewHeader } from '../ViewHeader';
import { thagaval } from '../Thagaval';
import NativeDocument from '../NativeDocument';
import { Capacitor } from '@capacitor/core';
// import removedimport './print.css';

const IconPhone = ({ size = 14, className = '', style = {} }) => (
    <svg xmlns="http://www.w3.org/2000/svg" width={size} height={size} viewBox="0 0 24 24" fill="currentColor" className={className} style={style}>
        <path d="M6.62 10.79c1.44 2.83 3.76 5.14 6.59 6.59l2.2-2.2c.27-.27.67-.36 1.02-.24 1.12.37 2.33.57 3.57.57.55 0 1 .45 1 1V20c0 .55-.45 1-1 1-9.39 0-17-7.61-17-17 0-.55.45-1 1-1h3.5c.55 0 1 .45 1 1 0 1.25.2 2.45.57 3.57.11.35.03.74-.25 1.02l-2.2 2.2z" />
    </svg>
);

const IconMail = ({ size = 14, className = '', style = {} }) => (
    <svg xmlns="http://www.w3.org/2000/svg" width={size} height={size} viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" className={className} style={style}>
        <path d="M4 4h16c1.1 0 2 .9 2 2v12c0 1.1-.9 2-2 2H4c-1.1 0-2-.9-2-2V6c0-1.1.9-2 2-2z"></path>
        <polyline points="22,6 12,13 2,6"></polyline>
    </svg>
);

export default function SharedReceiptView({ receipt: receiptProp, profile: profileProp, onBack, onEdit }) {
  const receipt = receiptProp || {};
  const { t } = useLanguage();
  const printRef = useRef(null);
  const [sharing, setSharing] = useState(false);
  const [saving, setSaving] = useState(false);
  const [coolieProfile, setCoolieProfile] = useState<any>(null);
  const [isLoadingProfile, setIsLoadingProfile] = useState(true);

  const theme = useTheme();
  const isNative = typeof window !== 'undefined' && (window as any).FlutterBridge && (window as any).FlutterBridge.isNativeApp && (window as any).FlutterBridge.isNativeApp();
  const isMobile = useMediaQuery(theme.breakpoints.down('sm')) || isNative;
  const initialScale = typeof window !== 'undefined' ? Math.min((window.innerWidth - 32) / 793.7, 1) : 0.43;
  const mbPercent = (1 - initialScale) * 141;

  const { wrapperRef, contentRef, scale } = usePinchZoom({ minScale: 1, maxScale: 4 });

  React.useEffect(() => {
    setCoolieProfile(profileProp || {}); setIsLoadingProfile(false);
  }, [receipt.company_id, receipt.receiptNo]);

  const profile = coolieProfile || {};

  const profileCurrency = getCountryConfig(profile?.country || 'India').currency;

  const renderKey = (key, enDefault, taDefault) => {
    const p = profile?.primaryDataLanguage || 'Tamil';
    const s = profile?.secondaryDataLanguage || 'English';
    const b = profile?.enableBilingual !== false && receipt.isSilk;

    const pStr = getLangStr(receipt.isSilk ? p : ((profile?.receiptLanguage || 'ta') === 'ta' ? 'Tamil' : 'English'), key, enDefault, taDefault);
    const sStr = getLangStr(s, key, enDefault, taDefault);

    if (b && pStr !== sStr) {
      return (
        <div style={{ display: 'flex', flexDirection: 'column' }}>
          <span>{pStr}</span>
          <span style={{ fontSize: '0.65em', fontWeight: 500, opacity: 0.85, letterSpacing: '0.02em', marginTop: '-1px' }}>{sStr}</span>
        </div>
      );
    }
    return pStr;
  };

  const RECEIPT_LABELS = {
    'English': { hc_paymentReceipt: 'PAYMENT RECEIPT', receiptNoLabel: 'Receipt No:', dateLabel: 'Date:', receivedFromLabel: 'Received From:', paymentModeLabel: 'Payment Mode:', referenceNoLabel: 'Reference No:', againstInvoiceLabel: 'Against Invoice:', noteLabel: 'Note:', receivedBy: 'Received By:', authorizedSignatory: '(Authorized Signature)', phoneLabel: 'Phone:', mobileLabel: 'Mobile:' },
    'Tamil': { hc_paymentReceipt: 'பண ரசீது', receiptNoLabel: 'ரசீது எண்:', dateLabel: 'நாள்:', receivedFromLabel: 'இவரிடமிருந்து பெறப்பட்டது:', paymentModeLabel: 'கட்டண முறை:', referenceNoLabel: 'குறிப்பு எண்:', againstInvoiceLabel: 'பில் எண்:', noteLabel: 'குறிப்பு:', receivedBy: 'பெற்றவர்', authorizedSignatory: '(கையொப்பம்)', phoneLabel: 'தொலைபேசி:', mobileLabel: 'கைபேசி:' },
    'Hindi': { hc_paymentReceipt: 'भुगतान रसीद', receiptNoLabel: 'रसीद संख्या:', dateLabel: 'दिनांक:', receivedFromLabel: 'से प्राप्त:', paymentModeLabel: 'भुगतान का प्रकार:', referenceNoLabel: 'संदर्भ संख्या:', againstInvoiceLabel: 'बिल के विरुद्ध:', noteLabel: 'नोट:', receivedBy: 'प्राप्तकर्ता', authorizedSignatory: 'अधिकृत हस्ताक्षरकर्ता', phoneLabel: 'फ़ोन:', mobileLabel: 'मोबाइल:' },
    'Telugu': { hc_paymentReceipt: 'చెల్లింపు రసీదు', receiptNoLabel: 'రసీదు నంబర్:', dateLabel: 'తేదీ:', receivedFromLabel: 'నుండి స్వీకరించబడింది:', paymentModeLabel: 'చెల్లింపు విధానం:', referenceNoLabel: 'సూచన సంఖ్య:', againstInvoiceLabel: 'ఇన్వాయిస్‌కు వ్యతిరేకంగా:', noteLabel: 'గమనిక:', receivedBy: 'స్వీకర్త', authorizedSignatory: 'అధికారిక సంతకం', phoneLabel: 'ఫోన్:', mobileLabel: 'మొబైల్:' },
    'Kannada': { hc_paymentReceipt: 'ಪಾವತಿ ರಶೀದಿ', receiptNoLabel: 'ರಶೀದಿ ಸಂಖ್ಯೆ:', dateLabel: 'ದಿನಾಂಕ:', receivedFromLabel: 'ಇವರಿಂದ ಸ್ವೀಕರಿಸಲಾಗಿದೆ:', paymentModeLabel: 'ಪಾವತಿ ವಿಧಾನ:', referenceNoLabel: 'ಉಲ್ಲೇಖ ಸಂಖ್ಯೆ:', againstInvoiceLabel: 'ಇನ್‌ವಾಯ್ಸ್ ವಿರುದ್ಧ:', noteLabel: 'ಸೂಚನೆ:', receivedBy: 'ಸ್ವೀಕರಿಸುವವರು', authorizedSignatory: 'ಅಧಿಕೃತ ಸಹಿದಾರರು', phoneLabel: 'ಫೋನ್:', mobileLabel: 'ಮೊಬೈಲ್:' },
    'Malayalam': { hc_paymentReceipt: 'പേയ്‌മെന്റ് രസീത്', receiptNoLabel: 'രസീത് നമ്പർ:', dateLabel: 'തീയതി:', receivedFromLabel: 'ഇതിൽ നിന്നും ലഭിച്ചു:', paymentModeLabel: 'പേയ്‌മെന്റ് രീതി:', referenceNoLabel: 'റഫറൻസ് നമ്പർ:', againstInvoiceLabel: 'ഇൻവോയ്സിനെതിരെ:', noteLabel: 'കുറിപ്പ്:', receivedBy: 'സ്വീകർത്താവ്', authorizedSignatory: 'അംഗീകൃത ഒപ്പ്', phoneLabel: 'ഫോൺ:', mobileLabel: 'മൊബൈൽ:' },
    'Marathi': { hc_paymentReceipt: 'पेमेंट पावती', receiptNoLabel: 'पावती क्रमांक:', dateLabel: 'दिनांक:', receivedFromLabel: 'कडून प्राप्त:', paymentModeLabel: 'पेमेंट पद्धत:', referenceNoLabel: 'संदर्भ क्रमांक:', againstInvoiceLabel: 'इनव्हॉइस विरुद्ध:', noteLabel: 'टीप:', receivedBy: 'प्राप्तकर्ता', authorizedSignatory: 'अधिकृत स्वाक्षरी', phoneLabel: 'फोन:', mobileLabel: 'मोबाईल:' },
    'Gujarati': { hc_paymentReceipt: 'ચુકવણી પહોંચ', receiptNoLabel: 'પહોંચ નંબર:', dateLabel: 'તારીખ:', receivedFromLabel: 'તરફથી પ્રાપ્ત:', paymentModeLabel: 'ચુકવણી પદ્ધતિ:', referenceNoLabel: 'સંદર્ભ નંબર:', againstInvoiceLabel: 'ઇન્વૉઇસ સામે:', noteLabel: 'નોંધ:', receivedBy: 'પ્રાપ્તકર્તા', authorizedSignatory: 'અધિકૃત સહી', phoneLabel: 'ફોન:', mobileLabel: 'મોબાઇલ:' },
    'Bengali': { hc_paymentReceipt: 'পেমেন্ট রসিদ', receiptNoLabel: 'রসিদ নম্বর:', dateLabel: 'তারিখ:', receivedFromLabel: 'থেকে প্রাপ্ত:', paymentModeLabel: 'পেমেন্ট পদ্ধতি:', referenceNoLabel: 'রেফারেন্স নম্বর:', againstInvoiceLabel: 'ইনভয়েসের বিপরীতে:', noteLabel: 'নোট:', receivedBy: 'প্রাপক', authorizedSignatory: 'অনুমোদিত স্বাক্ষরকারী', phoneLabel: 'ফোন:', mobileLabel: 'মোবাইল:' }
  };

  const getLangStr = (lang, key, enDefault, taDefault) => {
    if (RECEIPT_LABELS[lang]?.[key]) return RECEIPT_LABELS[lang][key];
    if (lang === 'Tamil') return ta[key] || taDefault || enDefault || key;
    if (lang === 'English') return en[key] || enDefault || taDefault || key;
    return '';
  };
  
  const renderPaymentMode = (mode) => {
    const dictionaries = {
      'Tamil': { 'Cash': 'பணம்', 'UPI': 'UPI', 'Bank Transfer': 'வங்கிப் பரிமாற்றம்', 'Cheque': 'காசோலை', 'Card': 'கார்டு', 'Other': 'மற்றவை' },
      'Hindi': { 'Cash': 'नकद', 'UPI': 'UPI', 'Bank Transfer': 'बैंक ट्रांसफर', 'Cheque': 'चेक', 'Card': 'कार्ड', 'Other': 'अन्य' },
      'Telugu': { 'Cash': 'నగదు', 'UPI': 'UPI', 'Bank Transfer': 'బ్యాంక్ బదిలీ', 'Cheque': 'చెక్', 'Card': 'కార్డు', 'Other': 'ఇతర' },
      'Kannada': { 'Cash': 'ನಗದು', 'UPI': 'UPI', 'Bank Transfer': 'ಬ್ಯಾಂಕ್ ವರ್ಗಾವಣೆ', 'Cheque': 'ಚೆಕ್', 'Card': 'ಕಾರ್ಡ್', 'Other': 'ಇತರ' },
      'Malayalam': { 'Cash': 'പണം', 'UPI': 'UPI', 'Bank Transfer': 'ബാങ്ക് ട്രാൻസ്ഫർ', 'Cheque': 'ചെക്ക്', 'Card': 'കാർഡ്', 'Other': 'മറ്റ്' },
      'Marathi': { 'Cash': 'रोख', 'UPI': 'UPI', 'Bank Transfer': 'बँक ट्रान्सफर', 'Cheque': 'चेक', 'Card': 'कार्ड', 'Other': 'इतर' },
      'Gujarati': { 'Cash': 'રોકડ', 'UPI': 'UPI', 'Bank Transfer': 'બેંક ટ્રાન્સફર', 'Cheque': 'ચેક', 'Card': 'કાર્ડ', 'Other': 'અન્ય' },
      'Bengali': { 'Cash': 'নগদ', 'UPI': 'UPI', 'Bank Transfer': 'ব্যাঙ্ক ট্রান্সফার', 'Cheque': 'চেক', 'Card': 'কার্ড', 'Other': 'অন্য' }
    };
    const primaryLang = receipt.isSilk ? (profile?.primaryDataLanguage || 'Tamil') : ((profile?.receiptLanguage || 'ta') === 'ta' ? 'Tamil' : 'English');
    const secondaryLang = profile?.secondaryDataLanguage || 'English';
    
    const pStr = (dictionaries[primaryLang] || {})[mode] || mode;
    const sStr = secondaryLang === 'English' ? mode : ((dictionaries[secondaryLang] || {})[mode] || mode);

    if (profile?.enableBilingual !== false && receipt.isSilk && pStr !== sStr) {
      return (
        <div style={{ display: 'flex', flexDirection: 'column', alignItems: 'flex-end' }}>
          <span>{pStr}</span>
          <span style={{ fontSize: '0.6em', fontWeight: 500, opacity: 0.85, letterSpacing: '0.02em', marginTop: '-1px' }}>{sStr}</span>
        </div>
      );
    }
    return pStr;
  };

  const executePrint = async () => {
    const printContent = printRef.current;
    if (!printContent) return;

    const title = receipt?.receiptNo ? `Receipt-${receipt.receiptNo}` : 'Print';
    const headContent = await getPrintHeadContent();

    const htmlContent = `
      <!DOCTYPE html>
      <html>
        <head>
          <title>${title}</title>
          ${headContent}
          <style>
            @media print {
              body, html { background-color: white !important; margin: 0; padding: 0; }
              .receipt-box, .invoice-paper { box-shadow: none !important; margin: 0 !important; border: none !important; width: 100% !important; }
              .no-print { display: none !important; }
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
        const fileName = `RCPT_${(receipt.receiptNo || '').replace(/\//g, '-')}`;
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
      window.FlutterBridge.printReceipt();
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

  const buildPDF = async () => {
    const paperEl = printRef.current?.closest('.invoice-paper');
    const origTransform = paperEl ? paperEl.style.transform : '';
    const origZoom = paperEl ? paperEl.style.zoom : '';
    if (paperEl) {
      paperEl.style.transform = 'none';
      paperEl.style.zoom = '1';
    }

    const pdf = new jsPDF({ orientation: 'portrait', unit: 'mm', format: 'a4', compress: true });
    const pdfWidth = pdf.internal.pageSize.getWidth();
    const pdfPageHeight = pdf.internal.pageSize.getHeight();
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

    const mainCanvas = await html2canvas(printRef.current, {
      ...captureOptions(printRef.current),
      onclone: (clonedDoc) => {
        clonedDoc.querySelectorAll('*').forEach(n => { n.style.letterSpacing = '0px'; n.style.wordSpacing = '0px'; });
        const inv = clonedDoc.querySelector('.receipt-box');
        if (inv) { inv.style.width = '210mm'; inv.style.overflow = 'visible'; inv.style.minHeight = '297mm'; inv.style.border = 'none'; inv.style.boxShadow = 'none'; inv.style.borderRadius = '0'; }
      }
    });

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
      const fileName = `RCPT_${(receipt.receiptNo || '').replace(/\//g, '-')}.pdf`;
      
      if (Capacitor.isNativePlatform()) {
        const pdfBase64 = pdf.output('datauristring').split(',')[1];
        await NativeDocument.downloadPdf({
          base64Data: pdfBase64,
          filename: fileName,
          appMode: 'Niril Coolie',
          category: 'Receipt'
        });
        thagaval(`Receipt downloaded to Niril Coolie/Receipt/`, 'success');
      } else {
        pdf.save(fileName);
        const pdfBlob = pdf.output('blob');
        const clientName = receipt.clientName || 'General';
        const rcpDate = receipt.date ? new Date(receipt.date) : new Date();
        const monthName = rcpDate.toLocaleString('en-IN', { month: 'long', year: 'numeric' });
        const params = new URLSearchParams({ name: fileName, client: clientName, month: monthName });
        fetch(`/api/save-pdf?${params}`, { method: 'POST', headers: { 'Content-Type': 'application/pdf' }, body: pdfBlob }).catch(() => {});
        thagaval(`Receipt downloaded & saved to Saved Invoices/${clientName}/`, 'success');
      }
    } catch (err) {
      console.error(err);
      thagaval('Failed to generate PDF.', 'error');
    } finally {
      setSaving(false);
    }
  };

  const handleNativeShare = async () => {
    if (sharing) return;
    const amount = formatCurrency(receipt.amount, profileCurrency);
    const msg = `*Receipt: ${receipt.receiptNo}*\nReceived From: ${receipt.clientName}\nAmount: ${amount}\nDate: ${new Date(receipt.date).toLocaleDateString('en-IN')}`;
    
    setSharing(true);
    try {
      const pdf = await buildPDF();
      const fileName = `RCPT_${(receipt.receiptNo || '').replace(/\//g, '-')}.pdf`;
      
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
      setTimeout(() => setSharing(false), 2000);
    }
  };

  if (isLoadingProfile) {
    return <Box sx={{ minHeight: '100vh', display: 'flex', justifyContent: 'center', alignItems: 'center' }}></Box>;
  }

  return (
    <Box className="print-wrapper" sx={{ py: { xs: 1.5, md: 4 }, px: { xs: 0, md: 4 }, maxWidth: 1200, mx: 'auto', width: '100%', position: 'relative', bgcolor: 'background.default', minHeight: '100vh', display: 'flex', flexDirection: 'column', '@media print': { bgcolor: 'white !important', minHeight: 'auto', py: 0, px: 0 } }}>
      {!isNative && (
        <ViewHeader 
          onEdit={onEdit ? () => onEdit(receipt) : undefined}
          onPrint={executePrint}
          onPDF={generatePDF}
          onShare={handleNativeShare}
          saving={saving}
          sharing={sharing}
          title={receipt?.receiptNo}
          onBack={onBack}
        />
      )}

      <Box className="print-wrapper" sx={{ flexGrow: 1, display: 'flex', justifyContent: 'center', alignItems: 'flex-start', overflowX: 'hidden', pb: 4, width: '100%' }}>
        <div ref={isMobile ? wrapperRef : null} style={isMobile ? { width: "100%", overflow: "hidden", touchAction: "none", display: "flex", justifyContent: "center", padding: "0 16px", boxSizing: "border-box" } : { width: '100%', display: 'flex', justifyContent: 'center' }}>
          <div ref={isMobile ? contentRef : null} style={isMobile ? { transformOrigin: "top center", width: "100%" } : {}}>
            <Paper elevation={isMobile ? 8 : 3} className="invoice-paper print-wrapper" sx={{ 
              p: 0, overflow: 'hidden', minWidth: '210mm', width: '210mm', m: '0 auto', bgcolor: 'white', color: 'black',
              ...(isMobile ? {
                zoom: 'none',
                transformOrigin: 'top left',
                transform: `scale(${initialScale})`,
                mb: `-${mbPercent}%`,
                borderRadius: '12px'
              } : {
                zoom: { xs: 0.43, sm: 0.7, md: 0.85, lg: 1 },
                '@supports not (zoom: 1)': {
                  transformOrigin: 'top center',
                  transform: { xs: 'scale(0.43)', sm: 'scale(0.7)', md: 'scale(0.85)', lg: 'none' },
                  mb: { xs: '-55%', sm: '-25%', md: '-10%', lg: 0 }
                }
              }),
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
          <div ref={printRef} className="print-area">
          <style>{`
          .receipt-box { width: 210mm; height: 297mm; max-height: 297mm; box-sizing: border-box; margin: 0 auto; border: 2px solid #e2e8f0; border-radius: 8px; background: white; display: flex; flex-direction: column; overflow: hidden; }
          .receipt-content { padding: 8mm 12mm; flex: 1; display: flex; flex-direction: column; }
          .receipt-header { text-align: center; margin-bottom: 2.5rem; border-bottom: 2px solid #e2e8f0; padding-bottom: 1.5rem; }
          .receipt-title { font-size: 2rem; font-weight: 800; color: #0f172a; margin: 0; }
          .receipt-row { display: flex; justify-content: space-between; padding: 0.85rem 0; font-size: 1.1rem; border-bottom: 1px solid #f1f5f9; }
          .receipt-label { color: #64748b; font-weight: 600; }
          .receipt-value { color: #1e293b; font-weight: 700; text-align: right; }
          .receipt-amount { font-size: 2rem; font-weight: 800; color: #1e40af; text-align: center; margin: 2rem 0; padding: 1.25rem; background: #eff6ff; border-radius: 8px; }
          .receipt-words { font-size: 1.05rem; color: #334155; text-align: center; margin-bottom: 2rem; }
          .receipt-footer { display: flex; justify-content: flex-end; padding-top: 2rem; padding-bottom: 1.5rem; }
          .receipt-sig { text-align: center; }
          .receipt-sig-line { width: 220px; border-bottom: 1.5px solid #1e293b; margin-bottom: 0.35rem; }
          .receipt-sig-label { font-size: 0.95rem; color: #64748b; }
          .business-name { font-size: 1.35rem; font-weight: 700; margin-bottom: 0.35rem; color: #000; }
          .rcpt-contact-section { padding: 1rem 12mm; }
          .rcpt-contact-title { font-size: 0.95rem; font-weight: 700; margin-bottom: 0.4rem; }
          .rcpt-contact-row { display: flex; justify-content: space-between; align-items: flex-start; }
          .rcpt-contact-left { display: flex; flex-direction: column; gap: 4px; }
          .rcpt-contact-address { font-size: 0.85rem; color: #334155; font-weight: 500; }
          .rcpt-contact-email, .rcpt-phone-item-new { display: flex; align-items: center; gap: 6px; font-size: 0.85rem; font-weight: 500; color: #334155; }
          @media print {
            .receipt-box {
              border: none !important;
              border-radius: 0 !important;
              box-shadow: none !important;
              width: 100% !important;
              height: 297mm !important;
              margin: 0 !important;
            }
          }
        `}</style>
            <div className="receipt-box">
              <div className="receipt-content">
                <div className="top-greeting-row">
                    <span className="greeting-left" style={{ color: profile.themeColor || '#1e3a8a' }}>{(profile?.receiptLanguage || 'ta') === 'en' ? 'Vaazhga Vaiyagam' : 'வாழ்க வையகம்'}</span>
                    <span className="greeting-center" style={{ color: profile.themeColor || '#1e3a8a' }}>உ</span>
                    <span className="greeting-right" style={{ color: profile.themeColor || '#1e3a8a' }}>{(profile?.receiptLanguage || 'ta') === 'en' ? 'Vaazhga Valamudan' : 'வாழ்க வளமுடன்'}</span>
                </div>
                <div className="print-header-new" style={{ borderBottom: '2px solid #e2e8f0', paddingBottom: '1.5rem', marginBottom: '3rem' }}>
                  <div className="header-left">
                    {profile.logo && <img src={profile.logo} alt="Logo" style={{ maxHeight: '80px' }} />}
                    <div className="header-company-info">
                      <div className="company-name font-display" style={{ color: profile.themeColor || '#1e3a8a' }}>
                        {profile?.nameEn || profile?.name || 'Your Business'}
                      </div>
                      {profile?.name && profile?.nameEn && profile.name !== profile.nameEn && (
                        <div className="company-subtitle font-tamil" style={{ color: '#475569' }}>
                          {profile.name}
                        </div>
                      )}
                    </div>
                  </div>
                  <div className="header-right">
                    <div className="font-tamil" style={{ color: profile.themeColor || '#1e3a8a', display: 'flex', flexDirection: 'column', alignItems: 'flex-end', fontWeight: 700, fontSize: '1.2rem' }}>
                      {profile.enableBilingual !== false && receipt.isSilk && getLangStr(profile?.primaryDataLanguage || 'Tamil', 'hc_paymentReceipt', 'PAYMENT RECEIPT', 'பண ரசீது') !== getLangStr(profile?.secondaryDataLanguage || 'English', 'hc_paymentReceipt', 'PAYMENT RECEIPT', 'பண ரசீது') ? (
                        <>
                          <div>{getLangStr(profile?.primaryDataLanguage || 'Tamil', 'hc_paymentReceipt', 'PAYMENT RECEIPT', 'பண ரசீது')}</div>
                          <div style={{ fontSize: '0.55em', fontWeight: 500, opacity: 0.85, letterSpacing: '0.02em', marginTop: '2px' }}>{getLangStr(profile?.secondaryDataLanguage || 'English', 'hc_paymentReceipt', 'PAYMENT RECEIPT', 'பண ரசீது')}</div>
                        </>
                      ) : (
                        getLangStr(receipt.isSilk ? (profile?.primaryDataLanguage || 'Tamil') : ((profile?.receiptLanguage || 'ta') === 'ta' ? 'Tamil' : 'English'), 'hc_paymentReceipt', 'PAYMENT RECEIPT', 'பண ரசீது')
                      )}
                    </div>
                  </div>
                </div>
              <div className="receipt-row"><span className="receipt-label" style={{ color: profile.themeColor || '#1e3a8a' }}>{renderKey('receiptNoLabel', 'Receipt No:', 'ரசீது எண்:')}</span><span className="receipt-value">{receipt.receiptNo}</span></div>
              <div className="receipt-row"><span className="receipt-label" style={{ color: profile.themeColor || '#1e3a8a' }}>{renderKey('dateLabel', 'Date:', 'தேதி:')}</span><span className="receipt-value">{new Date(receipt.date).toLocaleDateString('en-IN')}</span></div>
              <div className="receipt-row"><span className="receipt-label" style={{ color: profile.themeColor || '#1e3a8a' }}>{renderKey('receivedFromLabel', 'Received From:', 'பெறப்பட்டது:')}</span><span className="receipt-value">
                {profile?.enableBilingual !== false && receipt.isSilk && receipt.clientNameEn ? (
                  <div style={{ display: 'flex', flexDirection: 'column', alignItems: 'flex-end' }}>
                    <span>{receipt.clientName}</span>
                    <span style={{ fontSize: '0.6em', fontWeight: 500, opacity: 0.85, letterSpacing: '0.02em', marginTop: '-1px' }}>{receipt.clientNameEn}</span>
                  </div>
                ) : receipt.clientName}
              </span></div>
              <div className="receipt-row"><span className="receipt-label" style={{ color: profile.themeColor || '#1e3a8a' }}>{renderKey('paymentModeLabel', 'Payment Mode:', 'செலுத்தும் முறை:')}</span><span className="receipt-value">{renderPaymentMode(receipt.paymentMode)}</span></div>

              {receipt.againstInvoice && <div className="receipt-row"><span className="receipt-label" style={{ color: profile.themeColor || '#1e3a8a' }}>{renderKey('againstInvoiceLabel', 'Against Invoice:', 'விலைப்பட்டியலுக்கு எதிராக:')}</span><span className="receipt-value">{receipt.againstInvoice}</span></div>}
              <div style={{ marginTop: 'auto', marginBottom: 'auto' }}>
                <div className="receipt-amount" style={{ color: profile.themeColor || '#1e3a8a', backgroundColor: profile.themeColor ? `${profile.themeColor}15` : '#f0f9ff' }}>{formatCurrency(receipt.amount, profileCurrency)}</div>
                <div className="receipt-words" style={{ display: 'flex', flexDirection: 'column', alignItems: 'center', color: '#64748b', fontStyle: 'italic', marginBottom: '2rem', fontSize: '0.9rem' }}>
                  {(() => {
                    const primaryLang = receipt.isSilk ? (profile?.primaryDataLanguage || 'Tamil') : ((profile?.receiptLanguage || 'ta') === 'ta' ? 'Tamil' : 'English');
                    const isBilingual = profile?.enableBilingual !== false && receipt.isSilk;
                    const words = numberToWords(receipt.amount || 0, primaryLang, profile?.secondaryDataLanguage || 'English', isBilingual);
                    if (words.includes(' / ')) {
                      const [pStr, sStr] = words.split(' / ');
                      return (
                        <>
                          <span>{pStr}</span>
                          <span style={{ fontSize: '0.65em', fontWeight: 400, opacity: 0.85, letterSpacing: '0.02em', marginTop: '3px' }}>{sStr}</span>
                        </>
                      );
                    }
                    return words;
                  })()}
                </div>
              </div>
              {receipt.note && <p style={{ fontSize: '0.85rem', color: '#64748b' }}>{renderKey('noteLabel', 'Note:', 'குறிப்பு:')} {receipt.note}</p>}
              <div className="receipt-footer">
                <div className="preview-footer-right">
                  <div className="sign-company font-display" style={{ color: profile.themeColor || '#1e3a8a', position: 'relative', zIndex: 10 }}>{profile?.nameEn || profile?.name}</div>
                  <div className="sign-space" style={{ position: 'relative' }}>
                    {profile?.signature && (
                        <img src={profile.signature} alt="Signature" style={{ position: 'absolute', top: '50%', transform: 'translateY(-50%)', right: '-10px', maxHeight: '95px', maxWidth: '200px', objectFit: 'contain', pointerEvents: 'none' }} />
                    )}
                  </div>
                  <div className="sign-label" style={{ position: 'relative', zIndex: 10 }}>{profile?.authorizedSignatoryName ? `(${profile.authorizedSignatoryName})` : renderKey('authorizedSignatory', '(Authorized Signature)', '(கையொப்பம்)')}</div>
                </div>
              </div>
              </div>

              {/* Contact Section */}
              {(() => {
                const isTa = (profile?.receiptLanguage || 'ta') === 'ta';
                const addr1 = isTa 
                  ? [profile?.address, profile?.city].filter(Boolean).join(', ') || [profile?.addressEn, profile?.cityEn].filter(Boolean).join(', ')
                  : [profile?.addressEn, profile?.cityEn].filter(Boolean).join(', ') || [profile?.address, profile?.city].filter(Boolean).join(', ');
                
                const dist = isTa ? (profile?.district || profile?.districtEn) : (profile?.districtEn || profile?.district);
                const addr2 = dist ? `${dist}${profile?.pincode ? ` - ${profile.pincode}` : ''}` : (profile?.pincode ? `PIN: ${profile.pincode}` : '');
                
                const email = profile?.email || '';
                const phone = profile?.phone ? (Array.isArray(profile.phone) ? profile.phone : String(profile.phone).split(',')) : [];

                return (
                  <div className="rcpt-contact-section" style={{ background: profile.themeColor ? `${profile.themeColor}15` : '#f8fafc' }}>
                    <div className="rcpt-contact-title" style={{ color: profile.themeColor || '#1e293b' }}>{renderKey('contactUsLabel', 'Contact Us', 'தொடர்பு கொள்ள')}</div>
                    <div className="rcpt-contact-row">
                      <div className="rcpt-contact-left">
                        <div className="rcpt-contact-address">
                          <div>{addr1}</div>
                          {addr2 && <div>{addr2}</div>}
                        </div>
                        {email && (
                          <div className="rcpt-contact-email">
                            <div style={{ display: 'flex', alignItems: 'center', color: profile.themeColor || '#3b82f6' }}>
                              <IconMail size={14} />
                            </div>
                            <span>{email}</span>
                          </div>
                        )}
                      </div>
                      {phone.length > 0 && (
                        <div className="rcpt-contact-phones">
                          {phone.map((num, i) => (
                            <div key={i} className="rcpt-phone-item-new">
                              <div style={{ display: 'flex', alignItems: 'center', color: profile.themeColor || '#3b82f6' }}>
                                <IconPhone size={14} />
                              </div>
                              <span>{num}</span>
                            </div>
                          ))}
                        </div>
                      )}
                    </div>
                  </div>
                );
              })()}
            </div>
          </div>
            </Paper>
          </div>
        </div>
      </Box>
    </Box>
  );
}



