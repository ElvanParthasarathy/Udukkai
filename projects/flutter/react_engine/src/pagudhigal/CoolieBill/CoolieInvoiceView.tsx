import React, { useEffect, useState, useRef } from 'react';
import { Box, Button, IconButton, Paper, ToggleButtonGroup, ToggleButton } from '@mui/material';
import { usePinchZoom } from '../../hooks/usePinchZoom';
import { useTheme } from '@mui/material/styles';
import useMediaQuery from '@mui/material/useMediaQuery';
import { Printer, X, PencilSimple } from '@phosphor-icons/react';
import { jsPDF } from 'jspdf';
import html2canvas from 'html2canvas';
import { ViewHeader } from '../ViewHeader';
import { thagaval } from '../Thagaval';
import NativeDocument from '../NativeDocument';
import { Capacitor } from '@capacitor/core';
import { useLanguage } from '../../mozhi/LanguageContext';
import './print.css'; // The exact print.css copied from Kananam
import { getPrintHeadContent, numberToWords, getDynamicField } from '../../Payanpadu';

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

// Helper functions (Ported from Kananam calculations)
const gramsToKg = (grams) => {
    if (!grams) return 0;
    return parseFloat(grams) / 1000;
};

const calcTotalKg = (items, setharamGrams) => {
    const setharamKg = gramsToKg(setharamGrams);
    const itemKg = items.reduce((sum, item) => sum + (parseFloat(item.kg) || 0), 0);
    return itemKg + setharamKg;
};

const calcItemAmount = (rate, kg) => {
    const r = parseFloat(rate) || 0;
    const k = parseFloat(kg) || 0;
    return Math.floor(r * k);
};

const calcTotalRs = (items, courier, ahimsa, other) => {
    const sub = items.reduce((sum, item) => sum + calcItemAmount(item.coolie, item.kg), 0);
    return Math.floor(
        sub +
        (parseFloat(courier) || 0) +
        (parseFloat(ahimsa) || 0) +
        (parseFloat(other) || 0)
    );
};

const formatWeight = (val) => Number(val).toFixed(3);
const formatCurrency = (val) => Number(val).toLocaleString('en-IN');

const pickTamilPart = (text) => {
    if (!text) return text;
    const str = String(text);
    if (str.includes('/')) {
        const parts = str.split('/').map(p => p.trim()).filter(Boolean);
        return parts[0] || str;
    }
    return str;
};

export default function CoolieInvoiceView({ bill, onClose, onEdit }) {
    const { t } = useLanguage();
    const theme = useTheme();
    const isNative = typeof window !== 'undefined' && (window as any).FlutterBridge && (window as any).FlutterBridge.isNativeApp && (window as any).FlutterBridge.isNativeApp();
    const isMobile = useMediaQuery(theme.breakpoints.down('sm')) || isNative;
    const initialScale = typeof window !== 'undefined' ? Math.min((window.innerWidth - 32) / 793.7, 1) : 0.43;
    const mbPercent = (1 - initialScale) * 141;
    const printRef = useRef(null);
    const [companyProfile, setCompanyProfile] = useState(bill._companyProfile || null);
    const [isLoadingProfile, setIsLoadingProfile] = useState(!bill._companyProfile);
    const [printLang, setPrintLang] = useState('ta');
    const [saving, setSaving] = useState(false);

    const { wrapperRef, contentRef, scale } = usePinchZoom({ minScale: 1, maxScale: 4 });

  useEffect(() => {
        if (!companyProfile && bill?.company_id) {
            setCompanyProfile(bill._companyProfile || {}); if (bill._companyProfile?.defaultPrintLanguage) setPrintLang(bill._companyProfile.defaultPrintLanguage); setIsLoadingProfile(false);
        } else {
            setIsLoadingProfile(false);
        }
    }, [bill?.company_id, companyProfile]);

    if (isLoadingProfile || !bill) {
        return <Box sx={{ minHeight: '100vh', display: 'flex', justifyContent: 'center', alignItems: 'center' }}></Box>;
    }

    const activeProfile = companyProfile || {};
    const displayBillNo = bill.bill_no;

    const handlePrint = async () => {
        const printContent = printRef.current;
        if (!printContent) return;

        const title = displayBillNo ? `Invoice-${displayBillNo}` : 'Print';
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
                  .invoice-paper, .a4-paper { box-shadow: none !important; margin: 0 !important; border: none !important; width: 100% !important; }
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
                const fileName = `${(displayBillNo || '').replace(/\//g, '-')}`;
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
      const extraPages = printRef.current?.querySelectorAll('[data-pdf-page]') || [];
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
          const inv = clonedDoc.querySelector('.a4-paper');
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
        const fileName = `${(displayBillNo || '').replace(/\//g, '-')}.pdf`;
        
        if (Capacitor.isNativePlatform()) {
          const pdfBase64 = pdf.output('datauristring').split(',')[1];
          await NativeDocument.downloadPdf({
            base64Data: pdfBase64,
            filename: fileName,
            appMode: 'Niril Coolie',
            category: 'Invoice'
          });
          thagaval('Invoice downloaded to Niril Coolie/Invoice/', 'success');
        } else {
          pdf.save(fileName);
          const pdfBlob = pdf.output('blob');
          const invoiceDate = bill.date ? new Date(bill.date.split('/').reverse().join('-')) : new Date();
          const monthName = invoiceDate.toLocaleString('en-IN', { month: 'long', year: 'numeric' });
          const clientName = bill.customer_name || 'General';
          const params = new URLSearchParams({ name: fileName, client: clientName, month: monthName });
          fetch(`/api/save-pdf?${params}`, { method: 'POST', headers: { 'Content-Type': 'application/pdf' }, body: pdfBlob }).catch(() => {});
          thagaval('Invoice saved', 'success');
        }
      } catch (err) {
        console.error(err);
        thagaval('Failed to generate PDF.', 'error');
      } finally {
        setSaving(false);
      }
    };
  
    const handleNativeShare = async () => {
      const amount = formatCurrency(totalRs);
      const msg = `*${displayBillNo}*\nClient: ${bill.customer_name || ''}\nAmount: ${amount}\nDate: ${bill.date}`;
      
      setSaving(true);
      try {
        const pdf = await buildPDF();
        const fileName = `${(displayBillNo || '').replace(/\//g, '-')}.pdf`;
        
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

    // Extract Bill Data
    const {
        bill_no: billNo,
        date,
        customer_name: customerNameTa,
        customer_name_en: customerNameEn,
        contact_person: contactPerson,
        address: addressTa,
        address_en: addressEn,
        city: cityTa,
        city_en: cityEn,
        items = [],
        setharam_grams: setharamGrams,
        courier_rs: courierRs,
        ahimsa_silk_rs: ahimsaSilkRs,
        custom_charge_name: customChargeName,
        custom_charge_rs: customChargeRs,
        bank_details: bankDetails,
        account_no: accountNo,
        ifsc: billIfsc,
        show_bank_details: showBankDetails = true,
        show_ifsc: showIfsc
    } = bill;

    let otherCharges: { name: string, amount: string | number }[] = [];
    if (customChargeName) {
      try {
        if (customChargeName.startsWith('[')) {
          otherCharges = JSON.parse(customChargeName);
        } else {
          otherCharges = [{ name: customChargeName, amount: customChargeRs }];
        }
      } catch (e) {
        otherCharges = [{ name: customChargeName, amount: customChargeRs }];
      }
    }

    const isEng = printLang === 'en';
    const customerName = isEng && customerNameEn ? customerNameEn : customerNameTa;
    const address = isEng && addressEn ? addressEn : addressTa;
    const city = isEng && cityEn ? cityEn : cityTa;

    const ifsc = billIfsc || activeProfile?.ifsc || '';

    // Calculations
    const setharamKg = gramsToKg(setharamGrams);
    const totalKg = calcTotalKg(items, setharamGrams);
    const totalRs = calcTotalRs(items, courierRs, ahimsaSilkRs, customChargeRs);

    // Profile Fallbacks (Kananam config format)
    const p = activeProfile;
    const name = { english: p.nameEn || p.nameEnglish || 'ELVAN', tamil: p.name || p.nameTamil || 'எல்வன்' };
    const email = p.email || 'info@elvan.com';
    const phone = p.phone ? (Array.isArray(p.phone) ? p.phone : String(p.phone).split(',')) : ['9999999999'];
    const contactAddress = {
        line1: isEng && p.addressEn ? p.addressEn : (p.address || p.addressLine1 || ''),
        line2: isEng && p.districtEn ? p.districtEn : (p.district || p.addressLine2 || ''),
        line3: isEng && p.cityEn ? p.cityEn : (p.city || ''),
        pincode: p.pincode || ''
    };

    let cityPin = contactAddress.line3;
    if (contactAddress.pincode) {
        cityPin = cityPin ? `${cityPin} - ${contactAddress.pincode}` : contactAddress.pincode;
    }
    const addressLine1 = [contactAddress.line1, cityPin].filter(Boolean).join(', ');
    const addressLine2 = contactAddress.line2;

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

    const themeStyles = getThemeStyles(p.themeColor);

    return (
        <Box className="print-wrapper" sx={{ py: { xs: 1.5, md: 4 }, px: { xs: 0, md: 4 }, maxWidth: 1200, mx: 'auto', width: '100%', position: 'relative', bgcolor: 'background.default', minHeight: '100vh', display: 'flex', flexDirection: 'column' }}>
            {!isNative && (
                <ViewHeader 
                    className="no-print"
                    onEdit={() => onEdit(bill)}
                    onPrint={handlePrint}
                    onPDF={generatePDF}
                    onShare={handleNativeShare}
                    saving={saving}
                    title={displayBillNo}
                    onBack={onClose}
                />
            )}

            {/* Centered Preview Container */}
            <Box className="print-wrapper" sx={{ flexGrow: 1, display: 'flex', justifyContent: 'center', alignItems: 'flex-start', overflowX: 'hidden', pb: 4, width: '100%' }}>
        <div ref={isMobile ? wrapperRef : null} style={isMobile ? { width: "100%", overflow: "hidden", touchAction: "none", display: "flex", justifyContent: "center", padding: "0 16px", boxSizing: "border-box" } : { width: '100%', display: 'flex', justifyContent: 'center' }}>
          <div ref={isMobile ? contentRef : null} style={isMobile ? { transformOrigin: "top center", width: "100%" } : {}}>            <Paper elevation={isMobile ? 8 : 3} className="invoice-paper print-wrapper" sx={{ 
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
                    <div ref={printRef} className="a4-paper font-tamil" style={{ margin: '0 auto', background: 'white', ...themeStyles }}>
                
                {/* Top Greeting Row */}
                <div className="top-greeting-row">
                    <span className="greeting-left">{isEng ? 'Vaazhga Vaiyagam' : 'வாழ்க வையகம்'}</span>
                    <span className="greeting-center">உ</span>
                    <span className="greeting-right">{isEng ? 'Vaazhga Valamudan' : 'வாழ்க வளமுடன்'}</span>
                </div>

                {/* Header */}
                <div className="print-header-new">
                    <div className="header-left">
                        <div className="header-company-info">
                            <div className="company-name font-display">
                                {name.english}
                            </div>
                            <div className="company-subtitle font-tamil">{name.tamil}</div>
                        </div>
                    </div>
                    <div className="header-right">
                        <div className="bill-type-badge font-tamil">{isEng ? 'Coolie Bill' : 'கூலி பில்'}</div>
                    </div>
                </div>

                {/* Divider Line */}
                <div className="header-divider"></div>

                {/* Bill Info Section */}
                <div className="bill-info-section">
                    <div className="bill-meta-row">
                        <span className="bill-meta">{isEng ? 'Bill No:' : 'பில் எண் :'} <strong style={{ textTransform: 'uppercase' }}>{displayBillNo}</strong></span>
                        <span className="bill-meta">{isEng ? 'Date:' : 'நாள் :'} <strong>{date}</strong></span>
                    </div>
                    <div className="customer-info">
                        <div className="customer-name-simple">
                            <span className="customer-label">{isEng ? 'Thiru:' : 'திரு:'}</span>
                            <span style={{ fontWeight: '600' }}>
                                {contactPerson ? `${pickTamilPart(contactPerson)}, ${customerName}` : customerName}
                            </span>
                        </div>
                        <div className="customer-city-simple">
                            <span className="customer-label">{isEng ? 'Place:' : 'ஊர்:'}</span>
                            <span>{address && city ? `${address}, ${city}` : (address || city || '')}</span>
                        </div>
                    </div>
                </div>

                {/* Table */}
                <table className="bill-table-new">
                    <thead>
                        <tr>
                            <th style={{ width: '15%' }}><div>{isEng ? 'Coolie' : 'கூலி'}</div></th>
                            <th style={{ width: '45%', textAlign: 'left', paddingLeft: '15px' }}><div>{isEng ? 'Item Name' : 'பொருள் பெயர்'}</div></th>
                            <th style={{ width: '15%' }}><div>{isEng ? 'Weight (Kg)' : 'எடை (Kg)'}</div></th>
                            <th style={{ width: '25%' }}><div>{isEng ? 'Amount' : 'தொகை'}</div></th>
                        </tr>
                    </thead>
                    <tbody>
                        {items.map((item, i) => (
                            <tr key={i} className={i % 2 === 1 ? 'row-alt' : ''}>
                                <td className="text-center">{item.coolie || ''}</td>
                                <td className="text-left">{isEng && item.name_english ? item.name_english : pickTamilPart(item.porul)}</td>
                                <td className="text-center">{item.kg ? formatWeight(item.kg) : ''}</td>
                                <td className="text-center">{item.kg ? formatCurrency(calcItemAmount(item.coolie, item.kg)) : ''}</td>
                            </tr>
                        ))}

                        {ahimsaSilkRs && (
                            <tr>
                                <td className="text-center">-</td>
                                <td className="text-left"><div>{isEng ? 'Ahimsa Silk' : 'அகிம்சா பட்டு'}</div></td>
                                <td className="text-center">-</td>
                                <td className="text-center">{formatCurrency(ahimsaSilkRs)}</td>
                            </tr>
                        )}

                        {otherCharges.map((charge, i) => {
                            if (!charge.amount || parseFloat(String(charge.amount)) === 0) return null;
                            return (
                                <tr key={`oc-${i}`}>
                                    <td className="text-center">-</td>
                                    <td className="text-left"><div>{charge.name || (isEng ? 'Custom Charges' : 'பிற விவரம்')}</div></td>
                                    <td className="text-center">-</td>
                                    <td className="text-center">{formatCurrency(charge.amount)}</td>
                                </tr>
                            );
                        })}

                        {setharamGrams && (
                            <tr className={items.length % 2 === 1 ? 'row-alt' : ''}>
                                <td className="text-center">-</td>
                                <td className="text-left"><div>{isEng ? 'Wastage' : 'சேதாரம்'}</div></td>
                                <td className="text-center">{formatWeight(setharamKg)}</td>
                                <td className="text-center">-</td>
                            </tr>
                        )}

                        {courierRs && (
                            <tr>
                                <td className="text-center">-</td>
                                <td className="text-left"><div>{isEng ? 'Courier Charges' : 'கொரியர் கட்டணம்'}</div></td>
                                <td className="text-center">-</td>
                                <td className="text-center">{formatCurrency(courierRs)}</td>
                            </tr>
                        )}
                    </tbody>
                    <tfoot>
                        <tr className="total-footer-row">
                            <td colSpan="2" className="text-right total-label-cell">{isEng ? 'Total' : 'மொத்தம்'}</td>
                            <td className="text-center total-weight-cell">{formatWeight(totalKg)} Kg</td>
                            <td className="text-center total-amount-cell">₹ {formatCurrency(totalRs)}</td>
                        </tr>
                    </tfoot>
                </table>

                {totalRs > 0 && (
                    <div className="amount-in-words font-tamil mt-3 text-center">
                        <span className="words-label" style={{ fontSize: '12px', fontWeight: '600', color: 'var(--bill-text)', marginRight: '6px' }}>
                            {getDynamicField(p, 'Amount in words:', 'எழுத்தில் மொத்தத் தொகை:', 'எழுத்தில் மொத்தத் தொகை / Amount in words:')}
                        </span>
                        <span className="words-line">
                            {numberToWords(totalRs, isEng ? 'English' : 'Tamil', isEng ? 'English' : 'Tamil', false)}
                        </span>
                    </div>
                )}  <div style={{ flexGrow: 1 }}></div>

                {/* Footer */}
                <div className="bill-footer-new">
                    <div className="footer-left">
                        {( (showBankDetails && (bankDetails || accountNo)) || (showIfsc && ifsc) ) && (
                            <div style={{ marginBottom: '10px', color: 'var(--bill-text-dark)', fontSize: '0.9rem', lineHeight: '1.4', display: 'flex', flexDirection: 'column', gap: '6px' }}>
                                {showBankDetails && (
                                    <div style={{ display: 'flex', gap: '5px' }}>
                                        <div style={{ fontWeight: 'bold', whiteSpace: 'nowrap' }}>{isEng ? 'Bank Details :' : 'வங்கி விவரம் :'}</div>
                                        <div style={{ color: '#000' }}>
                                            {isEng && p.bankNameEn ? `${p.bankNameEn}${p.branchEn ? ', ' + p.branchEn : ''}` : bankDetails}
                                        </div>
                                    </div>
                                )}
                                {showBankDetails && accountNo && (
                                    <div style={{ display: 'flex', gap: '5px' }}>
                                        <div style={{ fontWeight: 'bold', whiteSpace: 'nowrap' }}>{isEng ? 'A/C No :' : 'கணக்கு எண் :'}</div>
                                        <div style={{ color: '#000' }}>{accountNo}</div>
                                    </div>
                                )}
                                {showIfsc && ifsc && (
                                    <div style={{ display: 'flex', gap: '5px' }}>
                                        <div style={{ fontWeight: 'bold', whiteSpace: 'nowrap' }}>IFSC :</div>
                                        <div style={{ color: '#000' }}>{ifsc}</div>
                                    </div>
                                )}
                            </div>
                        )}
                        <div className="thank-you font-tamil">{isEng ? 'Thank You' : 'நன்றி'}</div>
                    </div>
                    <div className="preview-footer-right">
                        <div className="sign-company font-display" style={{ position: 'relative', zIndex: 10 }}>{name.english}</div>
                        <div className="sign-space" style={{ position: 'relative' }}>
                            {p.signature && (
                                <img src={p.signature} alt="Signature" style={{ position: 'absolute', top: '50%', transform: 'translateY(-50%)', right: '-10px', maxHeight: '95px', maxWidth: '200px', objectFit: 'contain', pointerEvents: 'none' }} />
                            )}
                        </div>
                        <div className="sign-label" style={{ position: 'relative', zIndex: 10 }}>{p.authorizedSignatoryName ? `(${p.authorizedSignatoryName})` : isEng ? '(Authorized Signature)' : '(கையொப்பம்)'}</div>
                    </div>
                </div>

                {/* Contact Section */}
                <div className="contact-section">
                    <div className="contact-title">{isEng ? 'Contact Us' : 'தொடர்பு கொள்ள'}</div>
                    <div className="contact-row">
                        <div className="contact-left">
                            <div className="contact-address">
                                <div>{addressLine1}</div>
                                {addressLine2 && <div>{addressLine2}</div>}
                            </div>
                            <div className="contact-email" style={{ display: 'flex', alignItems: 'center', gap: '6px' }}>
                                <div style={{ display: 'flex', alignItems: 'center', marginTop: '-2px' }}>
                                    <IconMail size={14} />
                                </div>
                                <span>{email}</span>
                            </div>
                        </div>
                        <div className="contact-phones">
                            {phone.map((num, i) => (
                                <div key={i} className="phone-item-new" style={{ display: 'flex', alignItems: 'center', gap: '6px' }}>
                                    <div style={{ display: 'flex', alignItems: 'center', marginTop: '-2px' }}>
                                        <IconPhone size={14} />
                                    </div>
                                    <span>{num}</span>
                                </div>
                            ))}
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

