// @ts-nocheck
import React, { useRef, useState, useEffect } from 'react';
import { jsPDF } from 'jspdf';
import html2canvas from 'html2canvas';
import { useLanguage } from '../mozhi/LanguageContext';
import { en } from '../mozhi/en';
import { ta } from '../mozhi/ta';
import { 
  formatCurrency, 
  numberToWords, 
  getCountryConfig, 
  getDynamicField, 
  getBilingualStateName, 
  getBilingualCountryName, 
  getPrintHeadContent 
} from '../Payanpadu';
import { Box, Paper, useTheme, useMediaQuery } from '@mui/material';
import { ViewHeader } from './ViewHeader';
import NativeDocument from './NativeDocument';
import { Capacitor } from '@capacitor/core';
import { thagaval } from './Thagaval';
import './CoolieBill/print.css';
import { usePinchZoom } from '../hooks/usePinchZoom';

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
    '--bill-text-dark': `rgba(${Math.max(0, r - 30)}, ${Math.max(0, g - 30)}, ${Math.max(0, b - 30)}, 1)`,
    '--bill-row-alt': `rgba(${r}, ${g}, ${b}, 0.03)`
  };
};

export function normalizeReceiptProfile(p: any = {}) {
  const isBilingual = p.enableBilingual !== false;
  const isTamilPrimary = p.primaryDataLanguage === 'Tamil' || (!p.primaryDataLanguage && p.receiptLanguage !== 'en');
  const primaryLang = isBilingual 
    ? (p.primaryDataLanguage || 'Tamil') 
    : (p.receiptLanguage === 'en' ? 'English' : (p.primaryDataLanguage || 'Tamil'));
  const secondaryLang = isBilingual ? (p.secondaryDataLanguage || 'English') : primaryLang;

  // Business Name: English is primary, Tamil is subtitle (matching Coolie Bill)
  const englishName = 
    p.nameEn || 
    p.nameEnglish || 
    p.businessNameEn || 
    p.niruvanathinPeyarEn || 
    p.niruvanathinPeyar_English || 
    (p.businessName && typeof p.businessName === 'object' ? p.businessName.en : '') ||
    getDynamicField(p, 'niruvanathinPeyar', p, false) || 
    '';

  const tamilName = 
    p.name || 
    p.nameTamil || 
    p.businessNameTa || 
    p.niruvanathinPeyar || 
    p.niruvanathinPeyar_Tamil || 
    (p.businessName && typeof p.businessName === 'object' ? p.businessName.ta : '') ||
    getDynamicField(p, 'niruvanathinPeyar', p, true) || 
    (typeof p.businessName === 'string' ? p.businessName : '') || 
    '';

  const namePrimary = englishName || tamilName || 'Your Business';
  const nameSecondary = tamilName;

  // Address lines matching invoice contact lines:
  // Line 1: Street, City - PIN
  // Line 2: District
  const useEn = isBilingual || !isTamilPrimary;
  const line1_en = p.addressEn || getDynamicField(p, 'mugavari', p, false);
  const line1_ta = p.address || getDynamicField(p, 'mugavari', p, true);
  const city_en = p.cityEn || getDynamicField(p, 'oor', p, false);
  const city_ta = p.city || getDynamicField(p, 'oor', p, true);
  const dist_en = p.districtEn || getDynamicField(p, 'maavattam', p, false);
  const dist_ta = p.district || getDynamicField(p, 'maavattam', p, true);
  const pin = p.pin || p.pincode;

  const addrLines = (en: boolean) => {
    const l1_str = en ? (line1_en || line1_ta) : (line1_ta || line1_en);
    const c_str = en ? (city_en || city_ta) : (city_ta || city_en);
    const d_str = en ? (dist_en || dist_ta) : (dist_ta || dist_en);
    const cityPin = [c_str, pin].filter(Boolean).join(' - ');
    return {
      l1: [l1_str, cityPin].filter(Boolean).join(', ') || [l1_str, pin].filter(Boolean).join(', '),
      l2: d_str || ''
    };
  };

  const contact = addrLines(useEn);

  // Phone numbers: deduplicate across phone, tholaipesi, and mobileNumber, keeping up to 2 unique contact numbers
  const phoneArr: string[] = [];
  const rawList = [p.phone, p.tholaipesi, p.mobileNumber];
  for (const item of rawList) {
    if (!item) continue;
    const parts = Array.isArray(item) ? item : String(item).split(',');
    for (const raw of parts) {
      const clean = String(raw).trim();
      if (clean && !phoneArr.includes(clean)) {
        phoneArr.push(clean);
      }
    }
  }
  const phones = phoneArr.slice(0, 2);

  // Email
  const email = (p.email || p.minnanjal || '').trim();

  return {
    themeColor: p.themeColor || '#388e3c',
    primaryLang,
    secondaryLang,
    isBilingual,
    isTamilPrimary,
    namePrimary,
    nameSecondary,
    logo: p.logo,
    logoHeight: p.logoHeight || 120,
    wideLogo: p.wideLogo,
    wideLogoX: p.wideLogoX || 0,
    wideLogoY: p.wideLogoY || 0,
    wideLogoScale: p.wideLogoScale || 1,
    billHeaderStyle: p.billHeaderStyle,
    signature: p.signature,
    authorizedSignatoryName: p.authorizedSignatoryName || p.signatoryName,
    addr1: contact.l1,
    addr2: contact.l2,
    phones,
    email,
    country: p.country || 'India'
  };
}

export default function PodhuReceiptPaper({
  receipt: receiptProp,
  profile: profileProp,
  onBack,
  onEdit,
  appMode = 'Udukkai'
}: {
  receipt: any;
  profile?: any;
  onBack?: () => void;
  onEdit?: (receipt: any) => void;
  appMode?: string;
}) {
  const receipt = receiptProp || {};
  const profile = profileProp || {};
  const { t } = useLanguage();
  const printRef = useRef<HTMLDivElement>(null);
  const [sharing, setSharing] = useState(false);
  const [saving, setSaving] = useState(false);

  const theme = useTheme();
  const isMobile = useMediaQuery(theme.breakpoints.down('sm'));
  const initialScale = typeof window !== 'undefined' ? Math.min((window.innerWidth - 32) / 793.7, 1) : 0.43;
  const mbPercent = (1 - initialScale) * 141;

  const { wrapperRef, contentRef } = usePinchZoom({ minScale: 1, maxScale: 4 });

  const norm = normalizeReceiptProfile(profile);
  const profileCurrency = getCountryConfig(norm.country || 'India').currency;

  const RECEIPT_LABELS: Record<string, Record<string, string>> = {
    'English': { hc_paymentReceipt: 'Payment Receipt', receiptNoLabel: 'Receipt No:', dateLabel: 'Date:', receivedFromLabel: 'Received From:', paymentModeLabel: 'Payment Mode:', referenceNoLabel: 'Reference No:', againstInvoiceLabel: 'Against Invoice:', noteLabel: 'Note:', receivedBy: 'Received By:', authorizedSignatory: '(Authorized Signatory)', phoneLabel: 'Phone:', mobileLabel: 'Mobile:', contactUsLabel: 'Contact Us' },
    'Tamil': { hc_paymentReceipt: 'பெறுகைச் சான்று', receiptNoLabel: 'சான்று எண்:', dateLabel: 'நாள்:', receivedFromLabel: 'இவரிடமிருந்து பெறப்பட்டது:', paymentModeLabel: 'கட்டண முறை:', referenceNoLabel: 'குறிப்பு எண்:', againstInvoiceLabel: 'பட்டியல் எண்:', noteLabel: 'குறிப்பு:', receivedBy: 'பெற்றவர்', authorizedSignatory: '(கையொப்பம்)', phoneLabel: 'தொலைப்பேசி:', mobileLabel: 'கைப்பேசி:', contactUsLabel: 'தொடர்பு கொள்ள' },
    'Hindi': { hc_paymentReceipt: 'भुगतान रसीद', receiptNoLabel: 'रसीद संख्या:', dateLabel: 'दिनांक:', receivedFromLabel: 'से प्राप्त:', paymentModeLabel: 'भुगतान का प्रकार:', referenceNoLabel: 'संदर्भ संख्या:', againstInvoiceLabel: 'बिल के विरुद्ध:', noteLabel: 'नोट:', receivedBy: 'प्राप्तकर्ता', authorizedSignatory: 'अधिकृत हस्ताक्षरकर्ता', phoneLabel: 'फ़ोन:', mobileLabel: 'मोबाइल:', contactUsLabel: 'संपर्क करें' },
    'Telugu': { hc_paymentReceipt: 'చెల్లింపు రసీదు', receiptNoLabel: 'రసీదు నంబర్:', dateLabel: 'తేదీ:', receivedFromLabel: 'నుండి స్వీకరించబడింది:', paymentModeLabel: 'చెల్లింపు విధానం:', referenceNoLabel: 'సూచన సంఖ్య:', againstInvoiceLabel: 'ఇన్వాయిస్‌కు వ్యతిరేకంగా:', noteLabel: 'గమనిక:', receivedBy: 'స్వీకర్త', authorizedSignatory: 'అధికారిక సంతకం', phoneLabel: 'ఫోన్:', mobileLabel: 'మొబైల్:', contactUsLabel: 'సంప్రదించండి' },
    'Kannada': { hc_paymentReceipt: 'ಪಾವತಿ ರಶೀದಿ', receiptNoLabel: 'ರಶೀದಿ ಸಂಖ್ಯೆ:', dateLabel: 'ದಿನಾಂಕ:', receivedFromLabel: 'ಇವರಿಂದ ಸ್ವೀಕರಿಸಲಾಗಿದೆ:', paymentModeLabel: 'ಪಾವತಿ ವಿಧಾನ:', referenceNoLabel: 'ಉಲ್ಲೇಖ ಸಂಖ್ಯೆ:', againstInvoiceLabel: 'ಇನ್‌ವಾಯ್ಸ್ ವಿರುದ್ಧ:', noteLabel: 'ಸೂಚನೆ:', receivedBy: 'ಸ್ವೀಕರಿಸುವವರು', authorizedSignatory: 'ಅಧಿಕೃತ ಸಹಿದಾರರು', phoneLabel: 'ಫೋನ್:', mobileLabel: 'ಮೊಬೈಲ್:', contactUsLabel: 'ಸಂಪರ್ಕಿಸಿ' },
    'Malayalam': { hc_paymentReceipt: 'പേയ്‌മെന്റ് രസീത്', receiptNoLabel: 'രസീത് നമ്പർ:', dateLabel: 'തീയതി:', receivedFromLabel: 'ഇതിൽ നിന്നും ലഭിച്ചു:', paymentModeLabel: 'പേയ്‌മെന്റ് രീതി:', referenceNoLabel: 'റഫറൻസ് നമ്പർ:', againstInvoiceLabel: 'ഇൻവോയ്സിനെതിരെ:', noteLabel: 'കുറിപ്പ്:', receivedBy: 'സ്വീകർത്താവ്', authorizedSignatory: 'അംഗീകൃത ഒപ്പ്', phoneLabel: 'ഫോൺ:', mobileLabel: 'മൊബൈൽ:', contactUsLabel: 'ബന്ധപ്പെടുക' },
    'Marathi': { hc_paymentReceipt: 'पेमेंट पावती', receiptNoLabel: 'पावती क्रमांक:', dateLabel: 'दिनांक:', receivedFromLabel: 'कडून प्राप्त:', paymentModeLabel: 'पेमेंट पद्धत:', referenceNoLabel: 'संदर्भ क्रमांक:', againstInvoiceLabel: 'इनव्हॉइस विरुद्ध:', noteLabel: 'टीप:', receivedBy: 'प्राप्तकर्ता', authorizedSignatory: 'अधिकृत स्वाक्षरी', phoneLabel: 'फोन:', mobileLabel: 'मोबाईल:', contactUsLabel: 'संपर्क' },
    'Gujarati': { hc_paymentReceipt: 'ચુકવણી પહોંચ', receiptNoLabel: 'પહોંચ નંબર:', dateLabel: 'તારીખ:', receivedFromLabel: 'તરફથી પ્રાપ્ત:', paymentModeLabel: 'ચુકવણી પદ્ધતિ:', referenceNoLabel: 'સંદર્ભ નંબર:', againstInvoiceLabel: 'ઇન્વૉઇસ સામે:', noteLabel: 'નોંધ:', receivedBy: 'પ્રાપ્તકર્તા', authorizedSignatory: 'અધિકૃત સહી', phoneLabel: 'ફોન:', mobileLabel: 'મોબાઇલ:', contactUsLabel: 'સંપર્ક કરો' },
    'Bengali': { hc_paymentReceipt: 'পেমেন্ট রসিদ', receiptNoLabel: 'রসিদ নম্বর:', dateLabel: 'তারিখ:', receivedFromLabel: 'থেকে প্রাপ্ত:', paymentModeLabel: 'পেমেন্ট পদ্ধতি:', referenceNoLabel: 'রেফারেন্স নম্বর:', againstInvoiceLabel: 'ইনভয়েসের বিপরীতে:', noteLabel: 'নোট:', receivedBy: 'প্রাপক', authorizedSignatory: 'অনুমোদিত স্বাক্ষরকারী', phoneLabel: 'ফোন:', mobileLabel: 'মোবাইল:', contactUsLabel: 'যোগাযোগ' }
  };

  const getLangStr = (lang: string, key: string, enDefault: string, taDefault: string) => {
    if (RECEIPT_LABELS[lang]?.[key]) return RECEIPT_LABELS[lang][key];
    if (lang === 'Tamil') return ta[key] || taDefault || enDefault || key;
    if (lang === 'English') return en[key] || enDefault || taDefault || key;
    return '';
  };

  const renderKey = (key: string, enDefault: string, taDefault: string) => {
    const pStr = getLangStr(norm.primaryLang, key, enDefault, taDefault);
    const sStr = getLangStr(norm.secondaryLang, key, enDefault, taDefault);

    if (!norm.isBilingual) return pStr || getLangStr('English', key, enDefault, taDefault);

    if (pStr && sStr && pStr !== sStr) {
      return (
        <div style={{ display: 'inline-flex', flexDirection: 'column', alignItems: 'flex-start' }}>
          <span>{pStr}</span>
          <span style={{ fontSize: '0.65em', fontWeight: 500, color: '#64748b', letterSpacing: '0.02em', marginTop: '-1px' }}>{sStr}</span>
        </div>
      );
    }
    return pStr || sStr || '';
  };

  const renderPaymentMode = (mode: string) => {
    const dictionaries: Record<string, Record<string, string>> = {
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
    const pStr = (dictionaries[norm.primaryLang] || {})[mode] || mode;
    const sStr = norm.secondaryLang === 'English' ? mode : ((dictionaries[norm.secondaryLang] || {})[mode] || mode);

    if (norm.isBilingual && pStr !== sStr) {
      return (
        <div style={{ display: 'inline-flex', flexDirection: 'column', alignItems: 'flex-end' }}>
          <span>{pStr}</span>
          <span style={{ fontSize: '0.65em', fontWeight: 500, color: '#64748b', letterSpacing: '0.02em', marginTop: '-1px' }}>{sStr}</span>
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
        const fileName = `RCPT_${(receipt.receiptNo || 'RCPT').replace(/\//g, '-')}`;
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

    const doc = iframe.contentWindow?.document;
    if (doc) {
      doc.open();
      doc.write(htmlContent);
      doc.close();

      setTimeout(() => {
        iframe.contentWindow?.focus();
        iframe.contentWindow?.print();
        setTimeout(() => { document.body.removeChild(iframe); }, 1000);
      }, 500);
    }
  };

  const buildPDF = async () => {
    if (!printRef.current) return null;
    const paperEl = printRef.current.closest('.MuiPaper-root') as HTMLElement | null;
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

    const mainCanvas = await html2canvas(printRef.current, {
      scale: renderScale,
      useCORS: true,
      logging: false,
      letterRendering: true,
      backgroundColor: '#ffffff',
      imageTimeout: 0,
      width: printRef.current.scrollWidth,
      height: printRef.current.scrollHeight,
      onclone: (clonedDoc) => {
        clonedDoc.querySelectorAll('*').forEach((n: any) => { n.style.letterSpacing = '0px'; n.style.wordSpacing = '0px'; });
        const inv = clonedDoc.querySelector('.receipt-box') as HTMLElement | null;
        if (inv) { inv.style.width = '210mm'; inv.style.maxWidth = '210mm'; inv.style.overflow = 'visible'; inv.style.minHeight = 'unset'; inv.style.border = 'none'; inv.style.boxShadow = 'none'; inv.style.borderRadius = '0'; }
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
      while (heightLeft > 2) {
        position -= pdfPageHeight;
        pdf.addPage();
        pdf.addImage(mainImg, 'JPEG', 0, position, pdfWidth, mainImgHeight, undefined, 'MEDIUM');
        heightLeft -= pdfPageHeight;
      }
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
      if (!pdf) return;
      const fileName = `RCPT_${(receipt.receiptNo || 'RCPT').replace(/\//g, '-')}.pdf`;
      
      if (Capacitor.isNativePlatform()) {
        const pdfBase64 = pdf.output('datauristring').split(',')[1];
        await NativeDocument.downloadPdf({
          base64Data: pdfBase64,
          filename: fileName,
          appMode: appMode,
          category: 'Receipt'
        });
        thagaval(`Receipt downloaded to ${appMode}/Receipt/`, 'success');
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
      if (!pdf) return;
      const fileName = `RCPT_${(receipt.receiptNo || 'RCPT').replace(/\//g, '-')}.pdf`;
      
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
          .receipt-box { width: 210mm; min-height: 297mm; box-sizing: border-box; margin: 0 auto; border: 2px solid #e2e8f0; border-radius: 8px; background: white; display: flex; flex-direction: column; position: relative; text-align: left; }
          .receipt-content { padding: 8mm 12mm; flex: 1; display: flex; flex-direction: column; box-sizing: border-box; }
          .print-header-new { display: flex; justify-content: space-between; align-items: center; padding: 10px 0; margin-bottom: 12px; }
          .header-left { display: flex; align-items: center; gap: 15px; }
          .header-company-info { text-align: left; }
          .header-right { text-align: right; display: flex; flex-direction: column; align-items: flex-end; justify-content: center; }
          .receipt-row { display: flex; justify-content: space-between; align-items: flex-start; padding: 0.65rem 8px; font-size: 1.05rem; border-bottom: 1px solid #f1f5f9; }
          .receipt-label { color: var(--bill-primary, #1e3a8a); font-weight: 600; }
          .receipt-value { color: #242f3d; font-weight: 600; text-align: right; }
          .receipt-amount { font-size: 2rem; font-weight: 800; color: var(--bill-primary, #1e3a8a); text-align: center; margin: 1.25rem 0 0.75rem 0; padding: 1.1rem; background: var(--bill-primary-light, #eff6ff); border-radius: 8px; }
          .receipt-words { font-size: 1.05rem; color: #242f3d; text-align: center; margin-bottom: 1.25rem; }
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
            <div className="receipt-box" style={{ ...getThemeStyles(norm.themeColor) }}>
              <div className="receipt-content">
                {/* Top Greeting Row */}
                <div className="top-greeting-row" style={{ paddingLeft: '8px', paddingRight: '8px' }}>
                  <span className="greeting-left" style={{ color: norm.themeColor }}>{(!norm.isBilingual && norm.primaryLang === 'English') ? 'Vaazhga Vaiyagam' : 'வாழ்க வையகம்'}</span>
                  <span className="greeting-center" style={{ color: norm.themeColor }}>உ</span>
                  <span className="greeting-right" style={{ color: norm.themeColor }}>{(!norm.isBilingual && norm.primaryLang === 'English') ? 'Vaazhga Valamudan' : 'வாழ்க வளமுடன்'}</span>
                </div>

                {/* Header matching Coolie Bill layout */}
                <div className="print-header-new" style={{ paddingLeft: '8px', paddingRight: '8px', borderBottom: '1px solid #e2e8f0', paddingBottom: '10px', marginBottom: '12px' }}>
                  <div className="header-left">
                    {(norm.billHeaderStyle === 'wide' || (!norm.billHeaderStyle && norm.wideLogo)) && norm.wideLogo ? (
                      <img 
                        src={norm.wideLogo} 
                        alt="Wide Logo" 
                        style={{ 
                          height: '60px', 
                          maxWidth: '360px', 
                          objectFit: 'contain', 
                          objectPosition: 'left center',
                          transform: `translate(${norm.wideLogoX}px, ${norm.wideLogoY}px) scale(${norm.wideLogoScale})`,
                          transformOrigin: 'left center'
                        }} 
                      />
                    ) : (
                      <>
                        {norm.logo && (
                          <img 
                            src={norm.logo} 
                            alt="Logo" 
                            style={{ maxHeight: `${Math.min(norm.logoHeight || 60, 70)}px`, maxWidth: '150px', objectFit: 'contain' }} 
                          />
                        )}
                        <div className="header-company-info" style={{ display: 'flex', flexDirection: 'column', justifyContent: 'center' }}>
                          <div className="company-name font-display" style={{ color: norm.themeColor, whiteSpace: 'nowrap' }}>
                            {norm.namePrimary}
                          </div>
                          {norm.isBilingual && norm.nameSecondary && norm.nameSecondary !== norm.namePrimary && (
                            <div className="company-subtitle font-tamil" style={{ color: '#475569', whiteSpace: 'nowrap' }}>
                              {norm.nameSecondary}
                            </div>
                          )}
                        </div>
                      </>
                    )}
                  </div>
                  <div className="header-right" style={{ textAlign: 'right', display: 'flex', flexDirection: 'column', alignItems: 'flex-end', justifyContent: 'center' }}>
                    <div className="bill-type-badge font-tamil" style={{ color: norm.themeColor, fontSize: '17px', fontWeight: 800, lineHeight: 1.25, letterSpacing: '0.2px' }}>
                      {(!norm.isBilingual && norm.primaryLang === 'English') ? 'Payment Receipt' : 'பெறுகைச் சான்று'}
                    </div>
                    {norm.isBilingual && (
                      <div style={{ fontSize: '11px', fontWeight: 500, color: norm.themeColor, opacity: 0.85, marginTop: '3px', lineHeight: 1 }}>
                        Payment Receipt
                      </div>
                    )}
                  </div>
                </div>

                {/* Stacked Receipt Rows */}
                <div className="receipt-row">
                  <span className="receipt-label" style={{ color: norm.themeColor }}>{renderKey('receiptNoLabel', 'Receipt No:', 'சான்று எண்:')}</span>
                  <span className="receipt-value">{receipt.receiptNo}</span>
                </div>

                <div className="receipt-row">
                  <span className="receipt-label" style={{ color: norm.themeColor }}>{renderKey('dateLabel', 'Date:', 'நாள்:')}</span>
                  <span className="receipt-value">{receipt.date ? (new Date(receipt.date).toLocaleDateString('en-IN') !== 'Invalid Date' ? new Date(receipt.date).toLocaleDateString('en-IN') : receipt.date) : ''}</span>
                </div>

                <div className="receipt-row">
                  <span className="receipt-label" style={{ color: norm.themeColor }}>{renderKey('receivedFromLabel', 'Received From:', 'இவரிடமிருந்து பெறப்பட்டது:')}</span>
                  <span className="receipt-value">
                    {norm.isBilingual && receipt.clientNameEn && receipt.clientNameEn !== receipt.clientName ? (
                      <div style={{ display: 'inline-flex', flexDirection: 'column', alignItems: 'flex-end' }}>
                        <span>{receipt.clientName}</span>
                        <span style={{ fontSize: '0.65em', fontWeight: 500, color: '#64748b', letterSpacing: '0.02em', marginTop: '-1px' }}>{receipt.clientNameEn}</span>
                      </div>
                    ) : (receipt.clientName || receipt.clientNameEn || '-')}
                  </span>
                </div>

                <div className="receipt-row">
                  <span className="receipt-label" style={{ color: norm.themeColor }}>{renderKey('paymentModeLabel', 'Payment Mode:', 'கட்டண முறை:')}</span>
                  <span className="receipt-value">{renderPaymentMode(receipt.paymentMode)}</span>
                </div>

                {receipt.againstInvoice && (
                  <div className="receipt-row">
                    <span className="receipt-label" style={{ color: norm.themeColor }}>{renderKey('againstInvoiceLabel', 'Against Invoice:', 'பட்டியல் எண்:')}</span>
                    <span className="receipt-value">{receipt.againstInvoice}</span>
                  </div>
                )}

                {receipt.referenceNo && (
                  <div className="receipt-row">
                    <span className="receipt-label" style={{ color: norm.themeColor }}>{renderKey('referenceNoLabel', 'Reference No:', 'குறிப்பு எண்:')}</span>
                    <span className="receipt-value">{receipt.referenceNo}</span>
                  </div>
                )}

                {/* Center Amount and Words */}
                <div style={{ marginTop: 'auto', marginBottom: 'auto', paddingLeft: '8px', paddingRight: '8px' }}>
                  <div className="receipt-amount" style={{ color: norm.themeColor, backgroundColor: norm.themeColor ? `${norm.themeColor}15` : '#e8f5e9' }}>
                    {formatCurrency(receipt.amount, profileCurrency)}
                  </div>
                  <div className="receipt-words" style={{ display: 'flex', flexDirection: 'column', alignItems: 'center', gap: '3px' }}>
                    {(() => {
                      const words = numberToWords(receipt.amount, norm.primaryLang, norm.secondaryLang, norm.isBilingual);
                      if (words.includes(' / ')) {
                        const [pStr, sStr] = words.split(' / ');
                        return (
                          <>
                            <span style={{ fontWeight: 600, color: '#242f3d' }}>{pStr}</span>
                            <span style={{ fontSize: '0.85em', fontWeight: 500, color: '#64748b', opacity: 0.85 }}>{sStr}</span>
                          </>
                        );
                      }
                      return <span style={{ fontWeight: 600, color: '#242f3d' }}>{words}</span>;
                    })()}
                  </div>
                </div>

                {receipt.note && (
                  <p style={{ fontSize: '0.85rem', color: '#64748b', paddingLeft: '8px', paddingRight: '8px' }}>
                    {renderKey('noteLabel', 'Note:', 'குறிப்பு:')} {receipt.note}
                  </p>
                )}

                {/* Footer: Clean stationery layout matching invoice */}
                <div style={{ display: 'flex', flexDirection: 'column', marginTop: 'auto', marginBottom: '14px', paddingLeft: '8px', paddingRight: '8px' }}>
                  {/* Top Row: Business Name & Signature */}
                  <div style={{ display: 'flex', justifyContent: 'flex-end', alignItems: 'flex-start', width: '100%' }}>
                    <div style={{ textAlign: 'right', display: 'flex', flexDirection: 'column', alignItems: 'flex-end', minWidth: '220px' }}>
                      <div className="sign-company font-display" style={{ color: norm.themeColor, position: 'relative', zIndex: 10, fontSize: '14px', fontWeight: 700, marginBottom: '5px', lineHeight: 1.2 }}>
                        {norm.namePrimary}
                      </div>
                      <div style={{ position: 'relative', height: '55px', width: '100%', display: 'flex', justifyContent: 'flex-end', alignItems: 'center' }}>
                        {(norm.signature || receipt?.signature) && (
                          <img crossOrigin={(norm.signature || receipt?.signature)?.startsWith('http') ? 'anonymous' : undefined} src={norm.signature || receipt?.signature} alt="Signature" style={{ position: 'absolute', top: '50%', transform: 'translateY(-50%)', right: '-10px', maxHeight: '95px', maxWidth: '200px', objectFit: 'contain', pointerEvents: 'none' }} />
                        )}
                      </div>
                    </div>
                  </div>

                  {/* Bottom Row: Signatory Text */}
                  <div style={{ display: 'flex', justifyContent: 'flex-end', alignItems: 'flex-end', width: '100%', paddingTop: '12px' }}>
                    <div style={{ minWidth: '220px', textAlign: 'right' }}>
                      <div style={{ position: 'relative', zIndex: 10, fontSize: '0.75rem', fontWeight: 600, color: '#333', lineHeight: 1 }}>
                        {norm.authorizedSignatoryName ? `(${norm.authorizedSignatoryName})` : renderKey('authorizedSignatory', '(Authorized Signature)', '(கையொப்பம்)')}
                      </div>
                    </div>
                  </div>
                </div>

                {/* Contact Section: 1:1 match with invoice */}
                <div className="contact-section" style={{ padding: '24px calc(12mm + 8px)', background: norm.themeColor ? `${norm.themeColor}15` : '#e8f5e9' }}>
                  <div className="contact-row" style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', width: '100%' }}>
                    {/* Left Column: Title + Address Lines */}
                    <div className="contact-left" style={{ display: 'flex', flexDirection: 'column', gap: '6px' }}>
                      <div style={{ fontSize: '0.85rem', fontWeight: 700, color: norm.themeColor, display: 'flex', alignItems: 'center', height: '20px', lineHeight: 1.2 }}>
                        {!norm.isTamilPrimary ? 'Contact Us' : 'தொடர்பு கொள்ள'}
                      </div>
                      <div style={{ fontSize: '0.82rem', color: '#333', fontWeight: 500, display: 'flex', alignItems: 'center', height: '20px', lineHeight: 1.2 }}>
                        {norm.addr1}
                      </div>
                      {norm.addr2 && (
                        <div style={{ fontSize: '0.82rem', color: '#333', fontWeight: 500, display: 'flex', alignItems: 'center', height: '20px', lineHeight: 1.2 }}>
                          {norm.addr2}
                        </div>
                      )}
                    </div>

                    {/* Right Column: Phones & Email */}
                    <div className="contact-phones" style={{ display: 'flex', flexDirection: 'column', alignItems: 'flex-end', gap: '6px' }}>
                      {norm.phones.map((num: string, i: number) => (
                        <div key={i} className="phone-item-new" style={{ display: 'flex', alignItems: 'center', justifyContent: 'flex-end', gap: '8px', fontSize: '0.82rem', fontWeight: 600, color: '#444', height: '20px', lineHeight: 1.2 }}>
                          <div style={{ color: norm.themeColor, display: 'flex' }}><IconPhone size={14} /></div>
                          <span style={{ fontVariantNumeric: 'tabular-nums', letterSpacing: '0.02em' }}>{num}</span>
                        </div>
                      ))}
                      {norm.email && (
                        <div className="phone-item-new" style={{ display: 'flex', alignItems: 'center', justifyContent: 'flex-end', gap: '8px', fontSize: '0.82rem', fontWeight: 600, color: '#444', height: '20px', lineHeight: 1.2 }}>
                          <div style={{ color: norm.themeColor, display: 'flex' }}><IconMail size={14} /></div>
                          <span>{norm.email}</span>
                        </div>
                      )}
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>
            </Paper>
          </div>
        </div>
      </Box>
    </Box>
  );
}
