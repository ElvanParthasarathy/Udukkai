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
import { Box, Paper, Button, useTheme, useMediaQuery } from '@mui/material';
import { ViewHeader } from '../ViewHeader';
import { thagaval } from '../Thagaval';
import NativeDocument from '../NativeDocument';
import { Capacitor } from '@capacitor/core';
import { getAllCoolieProfiles } from '../../Avanam';
import CoolieReceiptBilingual from './CoolieReceiptBilingual';
import './print.css';

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

const getThemeStyles = (hexColor) => {
    if (!hexColor) return {};
    let r = 0, g = 0, b = 0;
    if (hexColor.length === 4) {
      r = parseInt(hexColor[1] + hexColor[1], 16);
      g = parseInt(hexColor[2] + hexColor[2], 16);
      b = parseInt(hexColor[3] + hexColor[3], 16);
    } else if (hexColor.length === 7) {
      r = parseInt(hexColor.slice(1, 3), 16);
      g = parseInt(hexColor.slice(3, 5), 16);
      b = parseInt(hexColor.slice(5, 7), 16);
    } else {
        return {};
    }
    return {
        '--bill-primary': hexColor,
        '--bill-primary-light': `rgba(${r}, ${g}, ${b}, 0.1)`,
        '--bill-primary-dark': hexColor,
        '--bill-accent': hexColor,
        '--bill-border': hexColor,
        '--bill-text': hexColor,
        '--bill-text-dark': `rgba(${Math.max(0, r-30)}, ${Math.max(0, g-30)}, ${Math.max(0, b-30)}, 1)`,
        '--bill-row-alt': `rgba(${r}, ${g}, ${b}, 0.03)`
    };
};

export default function CoolieReceiptView({ receipt: receiptProp, onBack, onEdit }) {
  const receipt = receiptProp || {};
  const { t } = useLanguage();
  const printRef = useRef(null);
  const [sharing, setSharing] = useState(false);
  const [saving, setSaving] = useState(false);
  const [themeVariant, setThemeVariant] = useState(() => localStorage.getItem('coolie_receipt_theme') || 'bilingual');
  const [coolieProfile, setCoolieProfile] = useState<any>(receipt._companyProfile || null);
  const [isLoadingProfile, setIsLoadingProfile] = useState(!receipt._companyProfile);

  const theme = useTheme();
  const isMobile = useMediaQuery(theme.breakpoints.down('sm'));
  const initialScale = typeof window !== 'undefined' ? Math.min((window.innerWidth - 32) / 793.7, 1) : 0.43;
  const mbPercent = (1 - initialScale) * 141;

  const { wrapperRef, contentRef, scale } = usePinchZoom({ minScale: 1, maxScale: 4 });

  React.useEffect(() => {
    getAllCoolieProfiles().then(profiles => {
      if (profiles && profiles.length > 0) {
        const p = profiles.find(pr => receipt.company_id && pr.id === receipt.company_id) 
               || profiles.find(pr => pr.shortBusinessName && receipt.receiptNo?.includes(pr.shortBusinessName)) 
               || profiles[0];
        setCoolieProfile(p);
      }
      setIsLoadingProfile(false);
    });
  }, [receipt.company_id, receipt.receiptNo]);

  const profile = coolieProfile || {};

  const profileCurrency = getCountryConfig(profile?.country || 'India').currency;

  const isBilingual = profile?.enableBilingual !== false;
  const primaryLang = isBilingual ? (profile?.primaryDataLanguage || 'Tamil') : ((profile?.receiptLanguage || 'ta') === 'en' ? 'English' : 'Tamil');
  const secondaryLang = isBilingual ? (profile?.secondaryDataLanguage || 'English') : primaryLang;

  const RECEIPT_LABELS = {
    'English': { hc_paymentReceipt: 'Payment Receipt', receiptNoLabel: 'Receipt No:', dateLabel: 'Date:', receivedFromLabel: 'Received From:', paymentModeLabel: 'Payment Mode:', referenceNoLabel: 'Reference No:', againstInvoiceLabel: 'Against Invoice:', noteLabel: 'Note:', receivedBy: 'Received By:', authorizedSignatory: '(Authorized Signature)', phoneLabel: 'Phone:', mobileLabel: 'Mobile:', contactUsLabel: 'Contact Us' },
    'Tamil': { hc_paymentReceipt: 'பெறுகைச் சான்று', receiptNoLabel: 'சான்று எண்:', dateLabel: 'நாள்:', receivedFromLabel: 'இவரிடமிருந்து பெறப்பட்டது:', paymentModeLabel: 'கட்டண முறை:', referenceNoLabel: 'குறிப்பு எண்:', againstInvoiceLabel: 'பட்டியல் எண்:', noteLabel: 'குறிப்பு:', receivedBy: 'பெற்றவர்', authorizedSignatory: '(கையொப்பம்)', phoneLabel: 'தொலைப்பேசி:', mobileLabel: 'கைப்பேசி:', contactUsLabel: 'தொடர்பு கொள்ள' },
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

  const renderKey = (key, enDefault, taDefault) => {
    const pStr = getLangStr(primaryLang, key, enDefault, taDefault);
    const sStr = getLangStr(secondaryLang, key, enDefault, taDefault);

    if (!isBilingual) return pStr || getLangStr('English', key, enDefault, taDefault);

    if (pStr && sStr && pStr !== sStr) {
      return (
        <div style={{ display: 'inline-flex', flexDirection: 'column', alignItems: 'flex-start' }}>
          <span>{pStr}</span>
          <span style={{ fontSize: '0.65em', fontWeight: 500, opacity: 0.85, letterSpacing: '0.02em', marginTop: '-1px' }}>{sStr}</span>
        </div>
      );
    }
    return pStr || sStr || '';
  };
  
  const renderPaymentMode = (mode) => {
    const dictionaries = {
      'Tamil': { 'Cash': 'பணம்', 'UPI': 'UPI', 'Bank Transfer': 'வங்கிப் பரிமாற்றம்', 'Cheque': 'காசோலை', 'Card': 'கார்டு', 'Other': 'மற்றவை' },
      'English': { 'Cash': 'Cash', 'UPI': 'UPI', 'Bank Transfer': 'Bank Transfer', 'Cheque': 'Cheque', 'Card': 'Card', 'Other': 'Other' },
      'Hindi': { 'Cash': 'नकद', 'UPI': 'UPI', 'Bank Transfer': 'बैंक ट्रांसफर', 'Cheque': 'चेक', 'Card': 'कार्ड', 'Other': 'अन्य' },
      'Telugu': { 'Cash': 'నగదు', 'UPI': 'UPI', 'Bank Transfer': 'బ్యాంక్ బదిలీ', 'Cheque': 'చెక్', 'Card': 'కార్డు', 'Other': 'ఇతర' },
      'Kannada': { 'Cash': 'ನಗದು', 'UPI': 'UPI', 'Bank Transfer': 'ಬ್ಯಾಂಕ್ ವರ್ಗಾವಣೆ', 'Cheque': 'ಚೆಕ್', 'Card': 'ಕಾರ್ಡ್', 'Other': 'ಇತರ' },
      'Malayalam': { 'Cash': 'പണം', 'UPI': 'UPI', 'Bank Transfer': 'ബാങ്ക് ട്രാൻസ്ഫർ', 'Cheque': 'ചെക്ക്', 'Card': 'കാർഡ്', 'Other': 'മറ്റ്' },
      'Marathi': { 'Cash': 'रोख', 'UPI': 'UPI', 'Bank Transfer': 'बँक ट्रान्सफर', 'Cheque': 'चेक', 'Card': 'कार्ड', 'Other': 'इतर' },
      'Gujarati': { 'Cash': 'રોકડ', 'UPI': 'UPI', 'Bank Transfer': 'બેંક ટ્રાન્સફર', 'Cheque': 'ચેક', 'Card': 'કાર્ડ', 'Other': 'અન્ય' },
      'Bengali': { 'Cash': 'নগদ', 'UPI': 'UPI', 'Bank Transfer': 'ব্যাঙ্ক ট্রান্সফার', 'Cheque': 'চেক', 'Card': 'কার্ড', 'Other': 'অন্য' }
    };
    const pStr = (dictionaries[primaryLang] || {})[mode] || mode;
    const sStr = (dictionaries[secondaryLang] || {})[mode] || mode;

    if (isBilingual && pStr !== sStr) {
      return (
        <div style={{ display: 'inline-flex', flexDirection: 'column', alignItems: 'flex-end' }}>
          <span>{pStr}</span>
          <span style={{ fontSize: '0.65em', fontWeight: 500, opacity: 0.85, letterSpacing: '0.02em', marginTop: '-1px' }}>{sStr}</span>
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
              .receipt-box, .invoice-paper, .a4-paper { box-shadow: none !important; margin: 0 !important; border: none !important; width: 100% !important; }
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
        const fileName = `RCPT_${receipt.receiptNo.replace(/\//g, '-')}`;
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
        const inv = clonedDoc.querySelector('.receipt-box') || clonedDoc.querySelector('.a4-paper');
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
      const fileName = `RCPT_${receipt.receiptNo.replace(/\//g, '-')}.pdf`;
      
      if (Capacitor.isNativePlatform()) {
        const pdfBase64 = pdf.output('datauristring').split(',')[1];
        await NativeDocument.downloadPdf({
          base64Data: pdfBase64,
          filename: fileName,
          appMode: 'Udukkai Coolie',
          category: 'Receipt'
        });
        thagaval(`Receipt downloaded to Udukkai Coolie/Receipt/`, 'success');
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
      const fileName = `RCPT_${receipt.receiptNo.replace(/\//g, '-')}.pdf`;
      
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

      {/* Theme / Template Shifter */}
      <Box className="no-print" sx={{ display: 'flex', justifyContent: 'center', mb: 2 }}>
        <Paper elevation={2} sx={{ p: 0.5, borderRadius: '999px', bgcolor: 'background.paper', display: 'flex', gap: 0.5, border: '1px solid', borderColor: 'divider' }}>
          <Button 
            size="small"
            onClick={() => { setThemeVariant('bilingual'); localStorage.setItem('coolie_receipt_theme', 'bilingual'); }}
            variant={themeVariant === 'bilingual' ? 'contained' : 'text'}
            sx={{ borderRadius: '999px', textTransform: 'none', fontWeight: 700, fontSize: '12px', px: 2 }}
          >
            ✨ Bilingual Template (KMP)
          </Button>
          <Button 
            size="small"
            onClick={() => { setThemeVariant('classic'); localStorage.setItem('coolie_receipt_theme', 'classic'); }}
            variant={themeVariant === 'classic' ? 'contained' : 'text'}
            sx={{ borderRadius: '999px', textTransform: 'none', fontWeight: 600, fontSize: '12px', px: 2 }}
          >
            📜 Classic Receipt
          </Button>
        </Paper>
      </Box>

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
            {themeVariant === 'bilingual' ? (
              <CoolieReceiptBilingual receipt={{ ...receipt, _companyProfile: profile }} />
            ) : (
              <div className="receipt-box" style={{ ...getThemeStyles(profile.themeColor || '#1e3a8a') }}>
                <div className="receipt-content">
                  <div className="top-greeting-row">
                    <span className="greeting-left" style={{ color: profile.themeColor || '#1e3a8a' }}>{(!isBilingual && primaryLang === 'English') ? 'Vaazhga Vaiyagam' : 'வாழ்க வையகம்'}</span>
                    <span className="greeting-center" style={{ color: profile.themeColor || '#1e3a8a' }}>உ</span>
                    <span className="greeting-right" style={{ color: profile.themeColor || '#1e3a8a' }}>{(!isBilingual && primaryLang === 'English') ? 'Vaazhga Valamudan' : 'வாழ்க வளமுடன்'}</span>
                </div>
                <div className="print-header-new" style={{ borderBottom: '2px solid #e2e8f0', paddingBottom: '1rem', marginBottom: '1.5rem' }}>
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
                  <div className="header-right" style={{ textAlign: 'right', display: 'flex', flexDirection: 'column', alignItems: 'flex-end', justifyContent: 'center' }}>
                    <div className="bill-type-badge font-tamil" style={{ color: profile.themeColor || '#1e3a8a', fontSize: '17px', fontWeight: 800, lineHeight: 1.25 }}>
                      {getLangStr(primaryLang, 'hc_paymentReceipt', 'Payment Receipt', 'பெறுகைச் சான்று')}
                    </div>
                    {isBilingual && getLangStr(primaryLang, 'hc_paymentReceipt', 'Payment Receipt', 'பெறுகைச் சான்று') !== getLangStr(secondaryLang, 'hc_paymentReceipt', 'Payment Receipt', 'பெறுகைச் சான்று') && (
                      <div style={{ fontSize: '11px', fontWeight: 500, color: profile.themeColor || '#1e3a8a', opacity: 0.85, marginTop: '3px', lineHeight: 1 }}>
                        {getLangStr(secondaryLang, 'hc_paymentReceipt', 'Payment Receipt', 'பெறுகைச் சான்று')}
                      </div>
                    )}
                  </div>
                </div>
              <div className="receipt-row"><span className="receipt-label" style={{ color: profile.themeColor || '#1e3a8a' }}>{renderKey('receiptNoLabel', 'Receipt No:', 'சான்று எண்:')}</span><span className="receipt-value">{receipt.receiptNo}</span></div>
              <div className="receipt-row"><span className="receipt-label" style={{ color: profile.themeColor || '#1e3a8a' }}>{renderKey('dateLabel', 'Date:', 'நாள்:')}</span><span className="receipt-value">{new Date(receipt.date).toLocaleDateString('en-IN')}</span></div>
              <div className="receipt-row">
                <span className="receipt-label" style={{ color: profile.themeColor || '#1e3a8a' }}>{renderKey('receivedFromLabel', 'Received From:', 'இவரிடமிருந்து பெறப்பட்டது:')}</span>
                <span className="receipt-value">
                  {isBilingual && receipt.clientNameEn && receipt.clientNameEn !== receipt.clientName ? (
                    <div style={{ display: 'inline-flex', flexDirection: 'column', alignItems: 'flex-end' }}>
                      <span>{receipt.clientName}</span>
                      <span style={{ fontSize: '0.65em', fontWeight: 500, opacity: 0.85, letterSpacing: '0.02em', marginTop: '-1px' }}>{receipt.clientNameEn}</span>
                    </div>
                  ) : (receipt.clientName || receipt.clientNameEn || '-')}
                </span>
              </div>
              <div className="receipt-row"><span className="receipt-label" style={{ color: profile.themeColor || '#1e3a8a' }}>{renderKey('paymentModeLabel', 'Payment Mode:', 'கட்டண முறை:')}</span><span className="receipt-value">{renderPaymentMode(receipt.paymentMode)}</span></div>

              {receipt.againstInvoice && <div className="receipt-row"><span className="receipt-label" style={{ color: profile.themeColor || '#1e3a8a' }}>{renderKey('againstInvoiceLabel', 'Against Invoice:', 'பட்டியல் எண்:')}</span><span className="receipt-value">{receipt.againstInvoice}</span></div>}
              <div style={{ marginTop: 'auto', marginBottom: 'auto' }}>
                <div className="receipt-amount" style={{ color: profile.themeColor || '#1e3a8a', backgroundColor: profile.themeColor ? `${profile.themeColor}15` : '#f0f9ff' }}>{formatCurrency(receipt.amount, profileCurrency)}</div>
                <div className="receipt-words" style={{ display: 'flex', flexDirection: 'column', alignItems: 'center', gap: '3px' }}>
                  {(() => {
                    const words = numberToWords(receipt.amount, primaryLang, secondaryLang, isBilingual);
                    if (words.includes(' / ')) {
                      const [pStr, sStr] = words.split(' / ');
                      return (
                        <>
                          <span style={{ fontWeight: 700, color: '#0f172a' }}>{pStr}</span>
                          <span style={{ fontSize: '0.85em', fontWeight: 500, color: '#64748b', opacity: 0.85 }}>{sStr}</span>
                        </>
                      );
                    }
                    return <span style={{ fontWeight: 600 }}>{words}</span>;
                  })()}
                </div>
              </div>
              {receipt.note && <p style={{ fontSize: '0.85rem', color: '#64748b' }}>{renderKey('noteLabel', 'Note:', 'குறிப்பு:')} {receipt.note}</p>}
              {/* Footer: Clean stationery layout matching invoice */}
              <div style={{ display: 'flex', flexDirection: 'column', marginTop: 'auto', marginBottom: '16px', paddingLeft: '8px', paddingRight: '8px' }}>
                {/* Top Row: Business Name & Signature */}
                <div style={{ display: 'flex', justifyContent: 'flex-end', alignItems: 'flex-start', width: '100%' }}>
                  <div style={{ textAlign: 'right', display: 'flex', flexDirection: 'column', alignItems: 'flex-end', minWidth: '220px' }}>
                    <div className="sign-company font-display" style={{ color: profile.themeColor || '#1e3a8a', position: 'relative', zIndex: 10, fontSize: '14px', fontWeight: 700, marginBottom: '5px', lineHeight: 1.2 }}>
                      {profile?.nameEn || profile?.name}
                    </div>
                    <div style={{ position: 'relative', height: '55px', width: '100%', display: 'flex', justifyContent: 'flex-end', alignItems: 'center' }}>
                      {(profile?.signature || receipt?.signature) && (
                        <img src={profile?.signature || receipt?.signature} alt="Signature" style={{ position: 'absolute', top: '50%', transform: 'translateY(-50%)', right: '-10px', maxHeight: '95px', maxWidth: '200px', objectFit: 'contain', pointerEvents: 'none' }} />
                      )}
                    </div>
                  </div>
                </div>

                {/* Bottom Row: Nandri & Signatory Text baseline aligned */}
                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-end', width: '100%', paddingTop: '12px' }}>
                  <div style={{ fontSize: '0.85rem', fontWeight: 700, color: profile.themeColor || '#1e3a8a', lineHeight: 1 }}>
                    {(!isBilingual && primaryLang === 'English') ? 'Thank You' : 'நன்றி'}
                    {isBilingual && <span style={{ marginLeft: '6px' }}>• {primaryLang === 'English' ? 'நன்றி' : 'Thank You'}</span>}
                  </div>
                  <div style={{ minWidth: '220px', textAlign: 'right' }}>
                    <div style={{ position: 'relative', zIndex: 10, fontSize: '0.75rem', fontWeight: 600, color: '#333', lineHeight: 1 }}>
                      {profile?.authorizedSignatoryName ? `(${profile.authorizedSignatoryName})` : renderKey('authorizedSignatory', '(Authorized Signature)', '(கையொப்பம்)')}
                    </div>
                  </div>
                </div>
              </div>

              {/* Contact Section: 1:1 match with invoice */}
              {(() => {
                const isTa = (profile?.receiptLanguage || 'ta') === 'ta';
                const addr1 = isTa 
                  ? [profile?.address, profile?.city].filter(Boolean).join(', ') || [profile?.addressEn, profile?.cityEn].filter(Boolean).join(', ')
                  : [profile?.addressEn, profile?.cityEn].filter(Boolean).join(', ') || [profile?.address, profile?.city].filter(Boolean).join(', ');
                
                const dist = isTa ? (profile?.district || profile?.districtEn) : (profile?.districtEn || profile?.district);
                const addr2 = dist ? `${dist}${profile?.pincode ? ` - ${profile.pincode}` : ''}` : (profile?.pincode ? `PIN: ${profile.pincode}` : '');
                
                const email = (profile?.email || profile?.minnanjal || '').trim();
                const phone = profile?.phone ? (Array.isArray(profile.phone) ? profile.phone : String(profile.phone).split(',')) : [];

                return (
                  <div className="contact-section">
                    <div className="contact-row">
                      <div className="contact-left">
                        <div className="contact-title" style={{ display: 'flex', flexDirection: 'column', gap: '2px', marginBottom: '8px' }}>
                          <span style={{ fontSize: '13px', fontWeight: 700, color: profile.themeColor || '#1e3a8a' }}>{(!isBilingual && primaryLang === 'English') ? 'Contact Us' : 'தொடர்பு கொள்ள'}</span>
                          {isBilingual && (
                            <span style={{ fontSize: '10px', fontWeight: 500, color: profile.themeColor || '#1e3a8a', opacity: 0.85 }}>{primaryLang === 'English' ? 'தொடர்பு கொள்ள' : 'Contact Us'}</span>
                          )}
                        </div>
                        <div className="contact-address">
                          <div>{addr1}</div>
                          {addr2 && <div>{addr2}</div>}
                        </div>
                        {email && (
                          <div className="contact-email" style={{ display: 'flex', alignItems: 'center', gap: '6px' }}>
                            <div style={{ display: 'flex', alignItems: 'center', marginTop: '-2px', color: profile.themeColor || '#1e3a8a' }}>
                              <IconMail size={14} />
                            </div>
                            <span>{email}</span>
                          </div>
                        )}
                      </div>
                      <div className="contact-phones">
                        {phone.map((num, i) => (
                          <div key={i} className="phone-item-new" style={{ display: 'flex', alignItems: 'center', gap: '6px' }}>
                            <div style={{ display: 'flex', alignItems: 'center', marginTop: '-2px', color: profile.themeColor || '#1e3a8a' }}>
                              <IconPhone size={14} />
                            </div>
                            <span style={{ fontVariantNumeric: 'tabular-nums', letterSpacing: '0.02em' }}>{num}</span>
                          </div>
                        ))}
                      </div>
                    </div>
                  </div>
                );
              })()}
            </div>
          </div>

          )}
          </div>
            </Paper>
          </div>
        </div>
      </Box>
    </Box>
  );
}
