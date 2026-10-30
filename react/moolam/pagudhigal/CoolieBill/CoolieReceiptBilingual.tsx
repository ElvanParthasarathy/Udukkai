// @ts-nocheck
import React from 'react';
import './print.css'; // Same stylesheet as CoolieInvoiceBilingual: exact sizes, fonts, and positions
import { numberToWords, formatCurrency } from '../../Payanpadu';

const IconPhone = ({ size = 14 }) => (
  <svg xmlns="http://www.w3.org/2000/svg" width={size} height={size} viewBox="0 0 24 24" fill="currentColor">
    <path d="M6.62 10.79c1.44 2.83 3.76 5.14 6.59 6.59l2.2-2.2c.27-.27.67-.36 1.02-.24 1.12.37 2.33.57 3.57.57.55 0 1 .45 1 1V20c0 .55-.45 1-1 1-9.39 0-17-7.61-17-17 0-.55.45-1 1-1h3.5c.55 0 1 .45 1 1 0 1.25.2 2.45.57 3.57.11.35.03.74-.25 1.02l-2.2 2.2z" />
  </svg>
);

const IconMail = ({ size = 14 }) => (
  <svg xmlns="http://www.w3.org/2000/svg" width={size} height={size} viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
    <path d="M4 4h16c1.1 0 2 .9 2 2v12c0 1.1-.9 2-2 2H4c-1.1 0-2-.9-2-2V6c0-1.1.9-2 2-2z"></path>
    <polyline points="22,6 12,13 2,6"></polyline>
  </svg>
);

const themeStyles = (hex) => {
  if (!hex || (hex.length !== 4 && hex.length !== 7)) return {};
  const full = hex.length === 4 ? `#${hex[1]}${hex[1]}${hex[2]}${hex[2]}${hex[3]}${hex[3]}` : hex;
  const r = parseInt(full.slice(1, 3), 16);
  const g = parseInt(full.slice(3, 5), 16);
  const b = parseInt(full.slice(5, 7), 16);
  return {
    '--bill-primary': hex,
    '--bill-primary-light': `rgba(${r}, ${g}, ${b}, 0.1)`,
    '--bill-primary-dark': hex,
    '--bill-accent': hex,
    '--bill-border': hex,
    '--bill-text': hex,
    '--bill-text-dark': `rgba(${Math.max(0, r - 30)}, ${Math.max(0, g - 30)}, ${Math.max(0, b - 30)}, 1)`,
    '--bill-row-alt': `rgba(${r}, ${g}, ${b}, 0.03)`,
  };
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

const PAYMENT_MODE_TAMIL = {
  Cash: 'பணம்',
  UPI: 'UPI',
  'Bank Transfer': 'வங்கிப் பரிமாற்றம்',
  Cheque: 'காசோலை',
  Card: 'கார்டு',
  Other: 'மற்றவை',
};

export default function CoolieReceiptBilingual({ receipt }) {
  const p = receipt._companyProfile || receipt.companyProfile || receipt.profile || {};
  const bi = p.enableBilingual !== false;
  const primaryEng = p.primaryDataLanguage === 'English';
  const isEng = !bi && primaryEng;
  const primaryLang = bi ? (p.primaryDataLanguage || 'Tamil') : (isEng ? 'English' : 'Tamil');
  const secondaryLang = bi ? (p.secondaryDataLanguage || 'English') : primaryLang;

  const slash = (ta, en) => (bi ? `${ta} / ${en}` : (isEng ? en : ta));
  const lbl = (ta, en) => (primaryEng ? en : ta);

  const displayReceiptNo = receipt.receiptNo || '-';
  const displayDate = formatDate(receipt.date);

  // Business info
  const name = { english: p.nameEn || p.nameEnglish || '', tamil: p.name || p.nameTamil || '' };
  const phone = p.phone ? (Array.isArray(p.phone) ? p.phone : String(p.phone).split(',')) : [];
  const contactLines = (en) => {
    const line1 = en ? p.addressEn : p.address;
    const city = en ? p.cityEn : p.city;
    const district = en ? p.districtEn : p.district;
    const cityPin = [city, p.pincode].filter(Boolean).join(' - ');
    return { l1: [line1, cityPin].filter(Boolean).join(', '), l2: district || '' };
  };
  const contact = contactLines(bi || isEng);
  const accent = p.themeColor || '#1e3a8a';

  // Customer info
  const customerNameTa = receipt.clientName || '';
  const customerNameEn = receipt.clientNameEn || '';
  const customerPrimary = (isEng && customerNameEn) ? customerNameEn : (customerNameTa || customerNameEn);
  const customerSecondary = bi && customerNameEn && customerNameEn !== customerNameTa ? customerNameEn : '';
  const customerAddress = receipt.address || receipt.city ? [receipt.address, receipt.city].filter(Boolean).join(', ') : '';

  // Payment mode translations
  const rawMode = receipt.paymentMode || 'Cash';
  const modeTa = PAYMENT_MODE_TAMIL[rawMode] || rawMode;
  const modeEn = rawMode;
  const modePrimary = primaryEng ? modeEn : modeTa;
  const modeSecondary = primaryEng ? modeTa : modeEn;

  // Amount in words
  const wordsStr = numberToWords(receipt.amount || 0, primaryLang, secondaryLang, bi);
  const [wordsPrimary, wordsSecondary] = wordsStr.includes(' / ') ? wordsStr.split(' / ') : [wordsStr, ''];

  return (
    <div className={`a4-paper font-tamil${bi ? ' bilingual' : ''}`} style={{ margin: '0 auto', background: 'white', ...themeStyles(p.themeColor) }}>
      {/* Top Greeting Row */}
      <div className="top-greeting-row" style={{ paddingLeft: '8px', paddingRight: '8px' }}>
        <span className="greeting-left">{isEng ? 'Vaazhga Vaiyagam' : 'வாழ்க வையகம்'}</span>
        <span className="greeting-center">உ</span>
        <span className="greeting-right">{isEng ? 'Vaazhga Valamudan' : 'வாழ்க வளமுடன்'}</span>
      </div>

      {/* Header */}
      <div className="print-header-new" style={{ paddingLeft: '8px', paddingRight: '8px' }}>
        <div className="header-left">
          {p.logo && <img src={p.logo} alt="Logo" style={{ maxHeight: '80px', maxWidth: '120px', objectFit: 'contain' }} />}
          <div className="header-company-info">
            <div className="company-name font-display">{name.english || name.tamil}</div>
            {name.english && name.tamil && <div className="company-subtitle font-tamil">{name.tamil}</div>}
          </div>
        </div>
        <div className="header-right" style={{ textAlign: 'right', display: 'flex', flexDirection: 'column', alignItems: 'flex-end', justifyContent: 'center' }}>
          <div className="font-tamil" style={{ fontSize: '17px', fontWeight: 800, color: accent, lineHeight: 1.25, letterSpacing: '0.2px' }}>
            {lbl('பெறுகைச் சான்று', 'Payment Receipt')}
          </div>
          {bi && (
            <div style={{ fontSize: '11px', fontWeight: 500, color: accent, opacity: 0.85, marginTop: '3px', lineHeight: 1 }}>
              Payment Receipt
            </div>
          )}
        </div>
      </div>

      {/* Receipt Info Section */}
      <div className="bill-info-section">
        <div className="bill-meta-row">
          <span className="bill-meta">
            <div style={{ fontSize: 'inherit', fontWeight: 'inherit', color: 'inherit' }}>
              {slash('சான்று எண்', 'Receipt No')} : <strong style={{ textTransform: 'uppercase' }}>{displayReceiptNo}</strong>
            </div>
          </span>
          <span className="bill-meta">
            <div style={{ fontSize: 'inherit', fontWeight: 'inherit', color: 'inherit' }}>
              {slash('நாள்', 'Date')} : <strong>{displayDate}</strong>
            </div>
          </span>
        </div>

        {/* Received From */}
        <div className="inv-party" style={{ marginTop: '6px', paddingLeft: '8px', paddingRight: '8px' }}>
          <h4 className="inv-section-label" style={{ color: 'var(--bill-primary)', fontWeight: 700, fontSize: '12px', marginBottom: '3px', textTransform: 'none' }}>
            {slash('இவரிடமிருந்து பெறப்பட்டது', 'Received From')}
          </h4>
          <p className="inv-party-name" style={{ color: '#000', fontWeight: 700, fontSize: '15px', margin: 0, marginBottom: '2px' }}>
            {customerPrimary || '-'}
          </p>
          {customerSecondary && (
            <p style={{ color: '#64748b', fontSize: '12px', fontWeight: 500, margin: 0 }}>
              {customerSecondary}
            </p>
          )}
          {customerAddress && (
            <div className="inv-party-details" style={{ color: '#333', fontSize: '13px', marginTop: '2px' }}>
              <p style={{ color: '#333', margin: 0 }}>{customerAddress}</p>
            </div>
          )}
        </div>
      </div>

      {/* Receipt Content - Original Row Items */}
      <div style={{ paddingLeft: '8px', paddingRight: '8px', marginTop: '10px' }}>
        {/* Payment Mode Row */}
        <div className="receipt-row">
          <span className="receipt-label" style={{ color: accent }}>
            <div style={{ display: 'inline-flex', flexDirection: 'column', alignItems: 'flex-start' }}>
              <span>{lbl('கட்டண முறை:', 'Payment Mode:')}</span>
              {bi && <span style={{ fontSize: '0.65em', fontWeight: 500, opacity: 0.85, marginTop: '-1px' }}>{primaryEng ? 'கட்டண முறை:' : 'Payment Mode:'}</span>}
            </div>
          </span>
          <span className="receipt-value">
            <div style={{ display: 'inline-flex', flexDirection: 'column', alignItems: 'flex-end' }}>
              <span>{modePrimary}</span>
              {bi && modeSecondary && modeSecondary !== modePrimary && (
                <span style={{ fontSize: '0.65em', fontWeight: 500, opacity: 0.85, marginTop: '-1px' }}>{modeSecondary}</span>
              )}
            </div>
          </span>
        </div>

        {/* Against Invoice Row (if present) */}
        {receipt.againstInvoice && (
          <div className="receipt-row">
            <span className="receipt-label" style={{ color: accent }}>
              <div style={{ display: 'inline-flex', flexDirection: 'column', alignItems: 'flex-start' }}>
                <span>{lbl('பட்டியல் எண்:', 'Against Bill:')}</span>
                {bi && <span style={{ fontSize: '0.65em', fontWeight: 500, opacity: 0.85, marginTop: '-1px' }}>{primaryEng ? 'பட்டியல் எண்:' : 'Against Bill:'}</span>}
              </div>
            </span>
            <span className="receipt-value">{receipt.againstInvoice}</span>
          </div>
        )}

        {/* Reference / Transaction No (if present) */}
        {receipt.referenceNo && (
          <div className="receipt-row">
            <span className="receipt-label" style={{ color: accent }}>
              <div style={{ display: 'inline-flex', flexDirection: 'column', alignItems: 'flex-start' }}>
                <span>{lbl('குறிப்பு எண்:', 'Reference No:')}</span>
                {bi && <span style={{ fontSize: '0.65em', fontWeight: 500, opacity: 0.85, marginTop: '-1px' }}>{primaryEng ? 'குறிப்பு எண்:' : 'Reference No:'}</span>}
              </div>
            </span>
            <span className="receipt-value">{receipt.referenceNo}</span>
          </div>
        )}
      </div>

      {/* Center Amount & Words */}
      <div style={{ marginTop: 'auto', marginBottom: 'auto', padding: '1rem 8px' }}>
        <div className="receipt-amount" style={{ color: accent, backgroundColor: p.themeColor ? `${p.themeColor}15` : '#eff6ff' }}>
          ₹{formatCurrency(receipt.amount || 0)}
        </div>
        <div className="receipt-words" style={{ display: 'flex', flexDirection: 'column', alignItems: 'center', gap: '3px' }}>
          <span style={{ fontWeight: 700, color: '#0f172a', fontSize: '1.05rem' }}>{wordsPrimary}</span>
          {wordsSecondary && (
            <span style={{ fontSize: '0.85em', fontWeight: 500, color: '#64748b', opacity: 0.85 }}>{wordsSecondary}</span>
          )}
        </div>
        {receipt.note && (
          <p style={{ fontSize: '0.85rem', color: '#64748b', textAlign: 'center', margin: '0.5rem 0 0 0' }}>
            <strong style={{ color: accent }}>{lbl('குறிப்பு:', 'Note:')}</strong> {receipt.note}
          </p>
        )}
      </div>

      <div style={{ flexGrow: 1 }}></div>

      {/* Footer: Clean open stationery layout (aligned signature and baseline Nandri / Signatory) */}
      <div style={{ display: 'flex', flexDirection: 'column', marginTop: 'auto', marginBottom: '16px', paddingLeft: '8px', paddingRight: '8px' }}>
        {/* Top Row: Business Name & Signature space on Right */}
        <div style={{ display: 'flex', justifyContent: 'flex-end', alignItems: 'flex-start', width: '100%' }}>
          <div style={{ textAlign: 'right', display: 'flex', flexDirection: 'column', alignItems: 'flex-end', minWidth: '220px' }}>
            <div className="sign-company font-display" style={{ color: accent, position: 'relative', zIndex: 10, fontSize: '14px', fontWeight: 700, marginBottom: '5px', lineHeight: 1.2 }}>
              {name.english || name.tamil}
            </div>
            <div style={{ position: 'relative', height: '55px', width: '100%', display: 'flex', justifyContent: 'flex-end', alignItems: 'center' }}>
              {(p.signature || receipt.signature) && (
                <img src={p.signature || receipt.signature} alt="Signature" style={{ position: 'absolute', top: '50%', transform: 'translateY(-50%)', right: '-10px', maxHeight: '95px', maxWidth: '200px', objectFit: 'contain', pointerEvents: 'none' }} />
              )}
            </div>
          </div>
        </div>

        {/* Bottom Row: Nandri & Signatory Text perfectly baseline aligned */}
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-end', width: '100%', paddingTop: '12px' }}>
          <div style={{ fontSize: '0.85rem', fontWeight: 700, color: accent, lineHeight: 1 }}>
            {lbl('நன்றி', 'Thank You')}
            {bi && <span style={{ marginLeft: '6px' }}>• {primaryEng ? 'நன்றி' : 'Thank You'}</span>}
          </div>
          <div style={{ minWidth: '220px', textAlign: 'right' }}>
            <div style={{ position: 'relative', zIndex: 10, fontSize: '0.75rem', fontWeight: 600, color: '#333', lineHeight: 1 }}>
              {p.authorizedSignatoryName ? `(${p.authorizedSignatoryName})` : lbl('(கையொப்பம்)', '(Authorized Signature)')}
            </div>
          </div>
        </div>
      </div>

      {/* Contact Section: address on the left, phone numbers and e-mail on the right */}
      <div className="contact-section">
        <div className="contact-row" style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', width: '100%' }}>
          {/* Left Column: Title + Address Lines */}
          <div className="contact-left" style={{ display: 'flex', flexDirection: 'column', gap: '6px' }}>
            <div style={{ fontSize: '0.85rem', fontWeight: 700, color: accent, display: 'flex', alignItems: 'center', height: '20px', lineHeight: 1.2 }}>
              {lbl('தொடர்பு கொள்ள', 'Contact Us')}
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
            {p.email && (
              <div className="phone-item-new" style={{ display: 'flex', alignItems: 'center', justifyContent: 'flex-end', gap: '8px', fontSize: '0.82rem', fontWeight: 600, color: '#444', height: '20px', lineHeight: 1.2 }}>
                <div style={{ color: accent, display: 'flex' }}><IconMail size={14} /></div>
                <span>{p.email}</span>
              </div>
            )}
          </div>
        </div>
      </div>
    </div>
  );
}
