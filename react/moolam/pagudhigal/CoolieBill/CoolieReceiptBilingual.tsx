// @ts-nocheck
import React from 'react';
import './print.css';
import { numberToWords, formatCurrency, getCountryConfig } from '../../Payanpadu';

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

export default function CoolieReceiptBilingual({ receipt }) {
  const profile = receipt._companyProfile || receipt.companyProfile || receipt.profile || {};
  const isBilingual = profile?.enableBilingual !== false;
  const primaryLang = isBilingual ? (profile?.primaryDataLanguage || 'Tamil') : ((profile?.receiptLanguage || 'ta') === 'en' ? 'English' : 'Tamil');
  const secondaryLang = isBilingual ? (profile?.secondaryDataLanguage || 'English') : primaryLang;

  const profileCurrency = getCountryConfig(profile?.country || 'India').currency;
  const accent = profile.themeColor || '#1e3a8a';

  const RECEIPT_LABELS = {
    'English': { hc_paymentReceipt: 'Payment Receipt', receiptNoLabel: 'Receipt No:', dateLabel: 'Date:', receivedFromLabel: 'Received From:', paymentModeLabel: 'Payment Mode:', referenceNoLabel: 'Reference No:', againstInvoiceLabel: 'Against Invoice:', noteLabel: 'Note:', receivedBy: 'Received By:', authorizedSignatory: '(Authorized Signature)', phoneLabel: 'Phone:', mobileLabel: 'Mobile:', contactUsLabel: 'Contact Us' },
    'Tamil': { hc_paymentReceipt: 'பெறுகைச் சான்று', receiptNoLabel: 'சான்று எண்:', dateLabel: 'நாள்:', receivedFromLabel: 'இவரிடமிருந்து பெறப்பட்டது:', paymentModeLabel: 'கட்டண முறை:', referenceNoLabel: 'குறிப்பு எண்:', againstInvoiceLabel: 'பட்டியல் எண்:', noteLabel: 'குறிப்பு:', receivedBy: 'பெற்றவர்', authorizedSignatory: '(கையொப்பம்)', phoneLabel: 'தொலைப்பேசி:', mobileLabel: 'கைப்பேசி:', contactUsLabel: 'தொடர்பு கொள்ள' },
  };

  const getLangStr = (lang, key, enDefault, taDefault) => {
    if (RECEIPT_LABELS[lang]?.[key]) return RECEIPT_LABELS[lang][key];
    if (lang === 'Tamil') return taDefault || enDefault || key;
    if (lang === 'English') return enDefault || taDefault || key;
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

  const formatDate = (val) => {
    if (!val) return '';
    if (typeof val === 'string' && val.includes('/')) return val;
    try {
      const d = new Date(val);
      if (!isNaN(d.getTime())) {
        const day = String(d.getDate()).padStart(2, '0');
        const month = String(d.getMonth() + 1).padStart(2, '0');
        const year = d.getFullYear();
        return `${day}/${month}/${year}`;
      }
    } catch (e) {}
    return String(val);
  };

  const primaryEng = profile?.primaryDataLanguage === 'English';
  const useEn = isBilingual || primaryEng;
  const contactLines = (en) => {
    const line1 = en ? profile?.addressEn : profile?.address;
    const city = en ? profile?.cityEn : profile?.city;
    const district = en ? profile?.districtEn : profile?.district;
    const cityPin = [city, profile?.pincode].filter(Boolean).join(' - ');
    return { l1: [line1, cityPin].filter(Boolean).join(', ') || [profile?.address, profile?.pincode].filter(Boolean).join(', '), l2: district || '' };
  };
  const contact = contactLines(useEn);
  
  const email = (profile?.email || profile?.minnanjal || '').trim();
  const phone = profile?.phone ? (Array.isArray(profile.phone) ? profile.phone : String(profile.phone).split(',')) : [];

  return (
    <div className="receipt-box" style={{ ...getThemeStyles(accent) }}>
      <div className="receipt-content">
        {/* Top Greeting Row */}
        <div className="top-greeting-row">
          <span className="greeting-left" style={{ color: accent }}>{(!isBilingual && primaryLang === 'English') ? 'Vaazhga Vaiyagam' : 'வாழ்க வையகம்'}</span>
          <span className="greeting-center" style={{ color: accent }}>உ</span>
          <span className="greeting-right" style={{ color: accent }}>{(!isBilingual && primaryLang === 'English') ? 'Vaazhga Valamudan' : 'வாழ்க வளமுடன்'}</span>
        </div>

        {/* Header matching Silk layout */}
        <div className="print-header-new" style={{ borderBottom: '2px solid #e2e8f0', paddingBottom: '1rem', marginBottom: '1.5rem' }}>
          <div className="header-left">
            {profile.logo && <img src={profile.logo} alt="Logo" style={{ maxHeight: '80px', maxWidth: '120px', objectFit: 'contain' }} />}
            <div className="header-company-info">
              <div className="company-name font-display" style={{ color: accent }}>
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
            <div className="font-tamil" style={{ color: accent, fontSize: '17px', fontWeight: 800, lineHeight: 1.25 }}>
              {(!isBilingual && primaryLang === 'English') ? 'Payment Receipt' : 'பெறுகைச் சான்று'}
            </div>
            {isBilingual && (
              <div style={{ fontSize: '11px', fontWeight: 500, color: accent, opacity: 0.85, marginTop: '3px', lineHeight: 1 }}>
                Payment Receipt
              </div>
            )}
          </div>
        </div>

        {/* Stacked Receipt Rows - 1:1 with Silk Layout */}
        <div className="receipt-row">
          <span className="receipt-label" style={{ color: accent }}>{renderKey('receiptNoLabel', 'Receipt No:', 'சான்று எண்:')}</span>
          <span className="receipt-value">{receipt.receiptNo}</span>
        </div>

        <div className="receipt-row">
          <span className="receipt-label" style={{ color: accent }}>{renderKey('dateLabel', 'Date:', 'நாள்:')}</span>
          <span className="receipt-value">{formatDate(receipt.date)}</span>
        </div>

        <div className="receipt-row">
          <span className="receipt-label" style={{ color: accent }}>{renderKey('receivedFromLabel', 'Received From:', 'இவரிடமிருந்து பெறப்பட்டது:')}</span>
          <span className="receipt-value">
            {isBilingual && receipt.clientNameEn && receipt.clientNameEn !== receipt.clientName ? (
              <div style={{ display: 'inline-flex', flexDirection: 'column', alignItems: 'flex-end' }}>
                <span>{receipt.clientName}</span>
                <span style={{ fontSize: '0.65em', fontWeight: 500, opacity: 0.85, letterSpacing: '0.02em', marginTop: '-1px' }}>{receipt.clientNameEn}</span>
              </div>
            ) : (receipt.clientName || receipt.clientNameEn || '-')}
          </span>
        </div>

        <div className="receipt-row">
          <span className="receipt-label" style={{ color: accent }}>{renderKey('paymentModeLabel', 'Payment Mode:', 'கட்டண முறை:')}</span>
          <span className="receipt-value">{renderPaymentMode(receipt.paymentMode)}</span>
        </div>

        {receipt.againstInvoice && (
          <div className="receipt-row">
            <span className="receipt-label" style={{ color: accent }}>{renderKey('againstInvoiceLabel', 'Against Invoice:', 'பட்டியல் எண்:')}</span>
            <span className="receipt-value">{receipt.againstInvoice}</span>
          </div>
        )}

        {receipt.referenceNo && (
          <div className="receipt-row">
            <span className="receipt-label" style={{ color: accent }}>{renderKey('referenceNoLabel', 'Reference No:', 'குறிப்பு எண்:')}</span>
            <span className="receipt-value">{receipt.referenceNo}</span>
          </div>
        )}

        {/* Center Amount and Words */}
        <div style={{ marginTop: 'auto', marginBottom: 'auto' }}>
          <div className="receipt-amount" style={{ color: accent, backgroundColor: profile.themeColor ? `${profile.themeColor}15` : '#f0f9ff' }}>
            {formatCurrency(receipt.amount, profileCurrency)}
          </div>
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

        {receipt.note && (
          <p style={{ fontSize: '0.85rem', color: '#64748b' }}>
            {renderKey('noteLabel', 'Note:', 'குறிப்பு:')} {receipt.note}
          </p>
        )}

        {/* Footer: Clean stationery layout matching invoice */}
        <div style={{ display: 'flex', flexDirection: 'column', marginTop: 'auto', marginBottom: '16px', paddingLeft: 0, paddingRight: 0 }}>
          {/* Top Row: Business Name & Signature */}
          <div style={{ display: 'flex', justifyContent: 'flex-end', alignItems: 'flex-start', width: '100%' }}>
            <div style={{ textAlign: 'right', display: 'flex', flexDirection: 'column', alignItems: 'flex-end', minWidth: '220px' }}>
              <div className="sign-company font-display" style={{ color: accent, position: 'relative', zIndex: 10, fontSize: '14px', fontWeight: 700, marginBottom: '5px', lineHeight: 1.2 }}>
                {profile?.nameEn || profile?.name}
              </div>
              <div style={{ position: 'relative', height: '55px', width: '100%', display: 'flex', justifyContent: 'flex-end', alignItems: 'center' }}>
                {(profile?.signature || receipt?.signature) && (
                  <img src={profile?.signature || receipt?.signature} alt="Signature" style={{ position: 'absolute', top: '50%', transform: 'translateY(-50%)', right: '-10px', maxHeight: '95px', maxWidth: '200px', objectFit: 'contain', pointerEvents: 'none' }} />
                )}
              </div>
            </div>
          </div>

          {/* Bottom Row: Signatory Text */}
          <div style={{ display: 'flex', justifyContent: 'flex-end', alignItems: 'flex-end', width: '100%', paddingTop: '12px' }}>
            <div style={{ minWidth: '220px', textAlign: 'right' }}>
              <div style={{ position: 'relative', zIndex: 10, fontSize: '0.75rem', fontWeight: 600, color: '#333', lineHeight: 1 }}>
                {profile?.authorizedSignatoryName ? `(${profile.authorizedSignatoryName})` : renderKey('authorizedSignatory', '(Authorized Signature)', '(கையொப்பம்)')}
              </div>
            </div>
          </div>
        </div>

        {/* Contact Section: 1:1 match with invoice */}
        <div className="contact-section" style={{ padding: '24px 12mm' }}>
          <div className="contact-row" style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', width: '100%' }}>
            {/* Left Column: Title + Address Lines */}
            <div className="contact-left" style={{ display: 'flex', flexDirection: 'column', gap: '6px' }}>
              <div style={{ fontSize: '0.85rem', fontWeight: 700, color: accent, display: 'flex', alignItems: 'center', height: '20px', lineHeight: 1.2 }}>
                {primaryEng ? 'Contact Us' : 'தொடர்பு கொள்ள'}
              </div>
              <div style={{ fontSize: '0.82rem', color: '#333', fontWeight: 500, display: 'flex', alignItems: 'center', height: '20px', lineHeight: 1.2 }}>
                {contact.l1}
              </div>
              {contact.l2 && (
                <div style={{ fontSize: '0.82rem', color: '#333', fontWeight: 500, display: 'flex', alignItems: 'center', height: '20px', lineHeight: 1.2 }}>
                  {contact.l2}
                </div>
              )}
            </div>

            {/* Right Column: Phones & Email */}
            <div className="contact-phones" style={{ display: 'flex', flexDirection: 'column', alignItems: 'flex-end', gap: '6px' }}>
              {phone.map((num, i) => (
                <div key={i} className="phone-item-new" style={{ display: 'flex', alignItems: 'center', justifyContent: 'flex-end', gap: '8px', fontSize: '0.82rem', fontWeight: 600, color: '#444', height: '20px', lineHeight: 1.2 }}>
                  <div style={{ color: accent, display: 'flex' }}><IconPhone size={14} /></div>
                  <span style={{ fontVariantNumeric: 'tabular-nums', letterSpacing: '0.02em' }}>{num}</span>
                </div>
              ))}
              {email && (
                <div className="phone-item-new" style={{ display: 'flex', alignItems: 'center', justifyContent: 'flex-end', gap: '8px', fontSize: '0.82rem', fontWeight: 600, color: '#444', height: '20px', lineHeight: 1.2 }}>
                  <div style={{ color: accent, display: 'flex' }}><IconMail size={14} /></div>
                  <span>{email}</span>
                </div>
              )}
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}
