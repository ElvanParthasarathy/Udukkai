// @ts-nocheck
import { useLanguage } from '../../mozhi/LanguageContext';
import React, { useState, useEffect } from 'react';
import QRCode from 'qrcode';
import DOMPurify from 'dompurify';

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
import { numberToWords, formatCurrency, INVOICE_TYPES, getCountryConfig, CURRENCY_NAMES, formatExchangeRateLine, getAccountById, getBilingualStateName, getBilingualCountryName, getDynamicField } from '../../Payanpadu';
import { en } from '../../mozhi/en';
import { ta } from '../../mozhi/ta';

// Coolie (Factory) invoice: the Textiles invoice paper without any GST. Items are weight x coolie
// rate; Ahimsa silk, other charges, wastage and courier come as extra lines.
const CoolieTheme = React.forwardRef(({ profile = {}, client = {}, details = {}, items = [], totals = {}, charges = [], options = {} }, ref) => {
  const invoiceType = 'coolie-invoice';
  const { t } = useLanguage();
  

  // Resolve the payment account once per render. Falls back to the synthesised
  // legacy account (built from flat profile bank fields) when no array exists,
  // so v1.4.3 invoices render byte-identical with no `selectedAccountId`.
  const account = getAccountById(profile, options.selectedAccountId);
  const showAccountLabel = options.showAccountLabel === true;

  // Options with defaults. Each toggle defaults to ON so old invoices keep rendering as
  // before; users opt INTO hiding fields via Customize.
  const opt = (key, fallback = true) => options[key] !== undefined ? options[key] : fallback;
  const showState = opt('showState');
  const showDistrict = opt('showDistrict');
  const showCountry = opt('showCountry');
  
  const showDiscount = opt('showDiscount');
  const showBankDetails = opt('showBankDetails');
  const showUPI = opt('showUPI');
  const showLogo = opt('showLogo');
  const showSignature = opt('showSignature');
  const showSignatoryText = opt('showSignatoryText');
  const showTerms = opt('showTerms');
  const showNotes = opt('showNotes');
  const showAmountWords = opt('showAmountWords');
  const showItemQty = opt('showItemQty');
  const showItemUnit = opt('showItemUnit');
  const showRateColumn = opt('showRateColumn');
  const showSubtotal = opt('showSubtotal');
  // Header / client meta — default ON
  const showniruvanathinPeyar = opt('showniruvanathinPeyar');
  const showBusinessAddress = opt('showBusinessAddress');
  const showBusinessPhone = opt('showBusinessPhone');
  const showBusinessEmail = opt('showBusinessEmail');
  const showClientAddress = opt('showClientAddress');
  const showClientPhone = opt('showClientPhone');
  const showClientEmail = opt('showClientEmail');
  const showInvoiceNumber = opt('showInvoiceNumber');
  const showInvoiceDate = opt('showInvoiceDate');
  const customTitle = (profile?.primaryDataLanguage || 'Tamil') === 'Tamil' ? 'கூலி பில்' : 'Coolie Bill';
  const currencySymbol = options.currency || 'INR';

  const renderLabel = (enLabel, taLabel) => {
    const p = profile?.primaryDataLanguage || 'Tamil';
    const s = profile?.secondaryDataLanguage || 'English';
    const b = profile?.enableBilingual !== false;

    const getLabel = (lang) => {
      if (lang === 'Tamil') return taLabel;
      if (lang === 'English') return enLabel;
      return '';
    };

    const pStr = getLabel(p);
    const sStr = getLabel(s);

    if (!b) return pStr || getLabel('English');

    if (pStr && sStr) return `${pStr} / ${sStr}`;
    return pStr || sStr || '';
  };

  const renderSubtitleLabel = (enLabel, taLabel, align = 'flex-start') => {
    const p = profile?.primaryDataLanguage || 'Tamil';
    const s = profile?.secondaryDataLanguage || 'English';
    const b = profile?.enableBilingual !== false;

    const getLabel = (lang) => {
      if (lang === 'Tamil') return taLabel;
      if (lang === 'English') return enLabel;
      return '';
    };

    const pStr = getLabel(p);
    const sStr = getLabel(s);

    if (!b) return pStr || getLabel('English');

    if (pStr && sStr) {
      return (
        <div style={{ display: 'flex', flexDirection: 'column', alignItems: align, justifyContent: 'center' }}>
          <div style={{ fontWeight: 700, marginBottom: '2px', lineHeight: 1.2, textTransform: 'none' }}>{pStr}</div>
          <div style={{ fontWeight: 400, fontSize: '10.5px', color: 'inherit', opacity: 0.9, textTransform: 'none', lineHeight: 1 }}>{sStr}</div>
        </div>
      );
    }
    return pStr || sStr || '';
  };

  const renderKey = (key) => {
    const p = profile?.primaryDataLanguage || 'Tamil';
    const s = profile?.secondaryDataLanguage || 'English';
    const b = profile?.enableBilingual !== false;

    const getStr = (lang) => {
      if (lang === 'Tamil') return ta[key] || en[key] || key;
      if (lang === 'English') return en[key] || ta[key] || key;
      return '';
    };

    const pStr = getStr(p);
    const sStr = getStr(s);

    if (!b) return pStr || getStr('English');

    if (pStr && sStr) return `${pStr} / ${sStr}`;
    return pStr || sStr || '';
  };

  const renderState = (val, customEn) => {
    if (!val) return '';
    return getBilingualStateName(val, { ...profile, fallbackEnglishName: customEn });
  };

  const renderCountry = (val, customEn) => {
    if (!val) return '';
    return getBilingualCountryName(val, { ...profile, fallbackEnglishName: customEn });
  };

  const fmt = (amount) => {
    if (currencySymbol === 'INR') return formatCurrency(amount);
    return new Intl.NumberFormat('en-US', { style: 'currency', currency: currencySymbol, minimumFractionDigits: 2 }).format(amount || 0);
  };

  const amountInWords = (num) => {
    if (currencySymbol === 'INR') return numberToWords(num, profile?.primaryDataLanguage || 'English', profile?.secondaryDataLanguage || 'English', profile?.enableBilingual !== false);
    const a = ['', 'One', 'Two', 'Three', 'Four', 'Five', 'Six', 'Seven', 'Eight', 'Nine', 'Ten', 'Eleven', 'Twelve', 'Thirteen', 'Fourteen', 'Fifteen', 'Sixteen', 'Seventeen', 'Eighteen', 'Nineteen'];
    const b = ['', '', 'Twenty', 'Thirty', 'Forty', 'Fifty', 'Sixty', 'Seventy', 'Eighty', 'Ninety'];
    const convert = (n) => {
      if (n === 0) return 'Zero';
      let result = '';
      if (n >= 1000000) { result += convert(Math.floor(n / 1000000)) + ' Million '; n %= 1000000; }
      if (n >= 1000) { result += convert(Math.floor(n / 1000)) + ' Thousand '; n %= 1000; }
      if (n >= 100) { result += a[Math.floor(n / 100)] + ' Hundred '; n %= 100; }
      if (n >= 20) { result += b[Math.floor(n / 10)] + ' '; if (n % 10) result += a[n % 10] + ' '; }
      else if (n > 0) { result += a[n] + ' '; }
      return result.trim();
    };
    const names = CURRENCY_NAMES[currencySymbol] || { major: currencySymbol, minor: 'Cents' };
    const rounded = Math.round(num * 100) / 100;
    const whole = Math.floor(rounded);
    const cents = Math.round((rounded - whole) * 100);
    let result = convert(whole) + ' ' + names.major;
    if (cents > 0) result += ' and ' + convert(cents) + ' ' + names.minor;
    return result + ' Only';
  };

  const [qrDataUrl, setQrDataUrl] = useState('');

  // Generate UPI QR code. The VPA now comes from the selected payment account
  // (so switching account swaps both bank block + QR), with fallback to the
  // legacy flat profile.upiId for invoices that pre-date multi-account support.
  const upiId = account?.upiId || profile?.upiId || '';
  useEffect(() => {
    if (!showUPI || !upiId || !totals?.total || currencySymbol !== 'INR') {
      setQrDataUrl('');
      return;
    }
    const upiUrl = `upi://pay?pa=${encodeURIComponent(upiId)}&pn=${encodeURIComponent(profile?.niruvanathinPeyar || '')}&am=${totals.total.toFixed(2)}&cu=INR&tn=${encodeURIComponent(`Payment for ${details?.invoiceNumber || 'Invoice'}`)}`;
    QRCode.toDataURL(upiUrl, { width: 120, margin: 1, errorCorrectionLevel: 'M' })
      .then(setQrDataUrl)
      .catch(() => setQrDataUrl(''));
  }, [showUPI, upiId, profile?.niruvanathinPeyar, totals?.total, details?.invoiceNumber, currencySymbol]);

  if (!profile) return <div ref={ref} />;

  const accentColors = {
    'tax-invoice': '#1e40af',
    'proforma': '#7c3aed',
    'bill-of-supply': '#0f766e',
    'credit-note': '#be123c',
  };
  const accent = profile?.themeColor || '#388e3c';
  const pdfStyle = options.pdfStyle || 'classic';

  // Check if any item has discount

  // Header renderers per style


  const renderClassicHeader = () => (
    <div className="inv-classic-header" style={{ paddingTop: '5mm' }}>
      <div style={{ padding: '0 0.5rem' }}>
        {/* Greeting Row - identical to receipt */}
        <div style={{ display: 'flex', justifyContent: 'space-between', padding: '0', marginBottom: '4px' }}>
          <span style={{ fontSize: '13px', fontWeight: 700, color: accent, letterSpacing: '0.5px' }}>
            {(profile?.primaryDataLanguage || 'Tamil') === 'English' ? 'Vaazhga Vaiyagam' : 'வாழ்க வையகம்'}
          </span>
          <span style={{ fontSize: '13px', fontWeight: 700, color: accent, letterSpacing: '0.5px' }}>உ</span>
          <span style={{ fontSize: '13px', fontWeight: 700, color: accent, letterSpacing: '0.5px' }}>
            {(profile?.primaryDataLanguage || 'Tamil') === 'English' ? 'Vaazhga Valamudan' : 'வாழ்க வளமுடன்'}
          </span>
        </div>

        {/* Header Row - identical to receipt: padding 15px 0, borderBottom, paddingBottom 1.5rem, marginBottom 1.5rem */}
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', position: 'relative', minHeight: '70px', borderBottom: 'none', padding: '15px 0', marginBottom: '15px' }}>
        <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'flex-start', gap: '1rem', position: 'relative' }}>
          {showLogo && (profile?.billHeaderStyle === 'wide' || (!profile?.billHeaderStyle && profile?.wideLogo)) && profile?.wideLogo ? (
            <img
              crossOrigin={profile.wideLogo?.startsWith('http') ? 'anonymous' : undefined}
              src={profile.wideLogo}
              alt="Wide Logo"
              width={400}
              height={140}
              style={{
                position: 'absolute',
                left: 0,
                top: -20,
                height: '140px',
                width: '400px',
                objectFit: 'contain',
                objectPosition: 'left center',
                transform: `translate(${profile?.wideLogoX || 0}px, ${profile?.wideLogoY || 0}px) scale(${profile?.wideLogoScale || 1})`,
                transformOrigin: 'left center'
              }}
            />
          ) : (
            <div style={{
              position: 'absolute',
              left: 0,
              top: -40,
              display: 'flex',
              alignItems: 'center',
              gap: '1rem',
              height: '140px'
            }}>
              {showLogo && profile?.logo && <img crossOrigin={profile.logo?.startsWith('http') ? 'anonymous' : undefined} src={profile.logo} alt="Logo" style={{ maxHeight: `${profile.logoHeight || 120}px`, maxWidth: '160px', objectFit: 'contain' }} />}
              <div style={{ display: 'flex', flexDirection: 'column', justifyContent: 'center' }}>
                {showniruvanathinPeyar && (
                  <div style={{ fontSize: '1.35rem', fontWeight: 700, color: accent, whiteSpace: 'nowrap' }}>
                    {getDynamicField(profile, 'niruvanathinPeyar', profile, true) || 'Your Business'}
                  </div>
                )}
                {showniruvanathinPeyar && profile?.enableBilingual !== false && getDynamicField(profile, 'niruvanathinPeyar', profile, false) && (
                  <div style={{ fontSize: '1rem', fontWeight: 500, color: '#475569', whiteSpace: 'nowrap' }}>
                    {getDynamicField(profile, 'niruvanathinPeyar', profile, false)}
                  </div>
                )}
              </div>
            </div>
          )}
        </div>
        <div style={{ textAlign: 'right', zIndex: 5, position: 'relative' }}>
          <div style={{ color: accent, display: 'flex', flexDirection: 'column', alignItems: 'flex-end', fontWeight: 700, fontSize: '1.05rem', textTransform: 'capitalize', transform: 'translateY(3px)' }}>
            <div>{String(customTitle || '').toLowerCase()}</div>
          </div>
        </div>
      </div>
      </div>

      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', backgroundColor: accent + '1A', borderRadius: '8px', padding: '8px 0.5rem', marginBottom: '15px' }}>
        <div style={{ fontSize: '14px', fontWeight: 600, color: accent }}>
          {showInvoiceNumber && <>{renderLabel('Bill No', 'பில் எண்')} : <strong style={{ color: '#000', fontWeight: 700 }}>{details?.invoiceNumber}</strong></>}
        </div>
        <div style={{ fontSize: '14px', fontWeight: 600, color: accent }}>
          {showInvoiceDate && <>{renderLabel('Date', 'நாள்')} : <strong style={{ color: '#000', fontWeight: 700 }}>{details?.invoiceDate ? new Date(details.invoiceDate).toLocaleDateString('en-GB', { day: '2-digit', month: '2-digit', year: 'numeric' }) : ''}</strong></>}
        </div>
      </div>
    </div>
  );

  // Billing parties section
  const renderParties = () => {
    return (
      <div className="inv-parties" style={{ padding: '0 0.5rem' }}>
        <div className="inv-party">
          <h4 className="inv-section-label">{renderLabel('Bill To', 'பெறுநர்')}</h4>
          <p className="inv-party-name">{getDynamicField(client, 'name', profile, false) || getDynamicField(client, 'name', profile, true) || 'Client Name'}</p>
          <div className="inv-party-details">
            {showClientAddress && (() => {
              const mugavari = getDynamicField(client, 'mugavari', profile, false) || getDynamicField(client, 'mugavari', profile, true);
              const oor = getDynamicField(client, 'oor', profile, false) || getDynamicField(client, 'oor', profile, true);
              const addr1 = [mugavari, oor].filter(Boolean).join(', ') + (client?.pin ? ` - ${client.pin}` : '');
              
              const maavattam = showDistrict ? (getDynamicField(client, 'maavattam', profile, false) || getDynamicField(client, 'maavattam', profile, true)) : '';
              const stateRaw = getDynamicField(client, 'maanilam', profile, true) || getDynamicField(client, 'maanilam', profile, false);
              const maanilam = showState ? (
                profile?.enableBilingual === false
                  ? (getBilingualStateName(stateRaw, { ...profile, fallbackEnglishName: getDynamicField(client, 'maanilam', profile, false), returnOnlyPrimary: true }) || getDynamicField(client, 'maanilam', profile, true))
                  : (getBilingualStateName(stateRaw, { ...profile, fallbackEnglishName: getDynamicField(client, 'maanilam', profile, false), returnOnlySecondary: true }) || getDynamicField(client, 'maanilam', profile, false) || getDynamicField(client, 'maanilam', profile, true))
              ) : '';
              
              const isTamil = maavattam && /[\u0B80-\u0BFF]/.test(maavattam);
              const distLabel = profile?.enableBilingual === false ? (isTamil ? 'மாவட்டம்' : 'District') : 'District';
              const distString = maavattam ? (maavattam.toLowerCase().includes('district') || maavattam.includes('மாவட்டம்') ? maavattam : `${maavattam} ${distLabel}`) : '';
              
              const countryRaw = getDynamicField(client, 'country', profile, true) || getDynamicField(client, 'country', profile, false);
              const country = showCountry ? (
                profile?.enableBilingual === false
                  ? (getBilingualCountryName(countryRaw, { ...profile, fallbackEnglishName: getDynamicField(client, 'country', profile, false), returnOnlyPrimary: true }) || getDynamicField(client, 'country', profile, true))
                  : (getBilingualCountryName(countryRaw, { ...profile, fallbackEnglishName: getDynamicField(client, 'country', profile, false), returnOnlySecondary: true }) || getDynamicField(client, 'country', profile, false) || getDynamicField(client, 'country', profile, true))
              ) : '';

              const addr2 = [distString, maanilam, country].filter(Boolean).join(', ');

              return (
                <>
                  {addr1 && <p>{addr1}</p>}
                  {addr2 && <p>{addr2}</p>}
                </>
              );
            })()}
            {showClientEmail && client?.email && <p>{client.email}</p>}
            {showClientPhone && client?.tholaipesi && <p>Ph: {client.tholaipesi}</p>}
          </div>
        </div>
      </div>
    );
  };

  // Coolie bill figures
  const formatKg = (n) => parseFloat((Number(n) || 0).toFixed(3));
  const itemAmount = (item) => Math.floor((Number(item.qty) || 0) * (Number(item.rate) || 0));
  const subtotal = items.reduce((sum, item) => sum + itemAmount(item), 0);
  const wastageKg = (Number(details.wastageGrams) || 0) / 1000;
  const totalKg = (items.reduce((sum, item) => sum + (Number(item.qty) || 0), 0) + wastageKg);
  const extraRows = [
    ...(Number(details.ahimsaSilk) > 0 ? [{ key: 'ahimsa', en: 'Ahimsa Silk', ta: 'அகிம்சா பட்டு', amount: Number(details.ahimsaSilk) }] : []),
    ...(charges || []).filter((ch) => ch.name || Number(ch.amount)).map((ch, i) => ({ key: `charge-${i}`, name: ch.name, en: 'Other Charges', ta: 'பிற விவரம்', amount: Number(ch.amount) || 0 })),
    ...(wastageKg > 0 ? [{ key: 'wastage', en: 'Wastage', ta: 'சேதாரம்', kg: wastageKg }] : []),
    ...(Number(details.courier) > 0 ? [{ key: 'courier', en: 'Courier Charges', ta: 'கொரியர் கட்டணம்', amount: Number(details.courier) }] : []),
  ];
  const hasExtraAmounts = extraRows.some((row) => row.amount);
  const totalWeightLabel = profile?.enableBilingual !== false
    ? ((profile?.primaryDataLanguage || 'Tamil') === 'Tamil' ? 'மொத்த எடை • Total Weight' : 'Total Weight • மொத்த எடை')
    : ((profile?.primaryDataLanguage || 'Tamil') === 'Tamil' ? 'மொத்த எடை' : 'Total Weight');

  return (
    <div className="invoice-preview-container" ref={ref} id="invoice-preview" style={{ display: 'flex', flexDirection: 'column', position: 'relative', minHeight: '297mm', backgroundColor: '#ffffff' }}>
      <style>{`
        #invoice-preview .unified-table-box {
          border: 1px solid #e2e8f0 !important;
          border-radius: 8px !important;
          overflow: hidden !important;
          margin: 0 0 0.5rem !important;
        }
        #invoice-preview .inv-table {
          border-collapse: separate !important;
          border-spacing: 0 !important;
          border: none !important;
          border-radius: 0 !important;
          margin: 0 !important;
          width: 100% !important;
        }
        #invoice-preview .inv-table th {
          background-color: ${accent}1A !important;
          color: ${accent} !important;
          font-weight: 700 !important;
          border-bottom: 1px solid rgba(0,0,0,0.05) !important;
          border-right: 1px solid rgba(0,0,0,0.08) !important;
          border-radius: 0 !important;
          padding: 0.5rem 0.5rem !important;
        }
        #invoice-preview .inv-table th:last-child {
          border-right: none !important;
        }
        #invoice-preview .inv-table td {
          border-right: 1px solid rgba(0,0,0,0.08) !important;
          padding: ${profile?.enableBilingual !== false ? '0.25rem 0.5rem' : '0.65rem 0.5rem'} !important;
        }
        #invoice-preview .inv-table td:last-child {
          border-right: none !important;
        }
        #invoice-preview .inv-table tbody tr:nth-child(even) {
          background-color: ${accent}14 !important;
        }
        #invoice-preview .inv-footer {
          border-top: none !important;
          padding: 0.5rem !important;
        }
        #invoice-preview .grand-total-row span {
          color: ${accent} !important;
        }
        #invoice-preview .inv-total-row {
          gap: 1.5rem !important;
        }
        #invoice-preview .inv-classic-header {
          padding-left: 1.5rem !important;
          padding-right: 1.5rem !important;
        }
        #invoice-preview .inv-parties {
          margin: 0 1.5rem 0.5rem 1.5rem !important;
        }
        #invoice-preview .unified-table-box {
          margin: 0 1.5rem 0.5rem !important;
        }
        #invoice-preview .invoice-preview-signatures {
          padding: 0 1.5rem !important;
        }
        #invoice-preview .inv-footer {
          margin: 1.5rem 1.5rem 0.5rem !important;
        }
        #invoice-preview .inv-contact-block {
          padding-left: 1.5rem !important;
          padding-right: 1.5rem !important;
        }
      `}</style>
      {renderClassicHeader()}

      {renderParties()}

      {/* Items table & Totals Unified Box */}
      <div className="unified-table-box">
        <table className="inv-table" style={{ tableLayout: 'auto' }}>
          <thead>
            <tr>
              <th className="inv-th">#</th>
              <th className="inv-th" style={{ width: '100%' }}>{renderSubtitleLabel('Product Name', 'பொருள் பெயர்')}</th>
              <th className="inv-th inv-th-center">{renderSubtitleLabel('Weight', 'எடை', 'center')}</th>
              <th className="inv-th inv-th-right">{renderSubtitleLabel('Coolie', 'கூலி', 'flex-end')}</th>
              <th className="inv-th inv-th-right">{renderSubtitleLabel('Amount', 'தொகை', 'flex-end')}</th>
            </tr>
          </thead>
          <tbody>
            {items.map((item, index) => (
              <tr key={item.id} className={index % 2 === 0 ? 'inv-tr-even' : ''}>
                <td className="inv-td inv-td-muted">{index + 1}</td>
                <td className="inv-td inv-td-name">
                  <div style={{ fontWeight: 500 }}>{getDynamicField(item, 'name', profile, true) || '-'}</div>
                  {profile?.enableBilingual !== false && getDynamicField(item, 'name', profile, false) && <div style={{ fontSize: '0.85em', color: '#64748b', marginTop: '2px' }}>{getDynamicField(item, 'name', profile, false)}</div>}
                </td>
                <td className="inv-td inv-td-center">{formatKg(item.qty)} Kg</td>
                <td className="inv-td inv-td-right">{fmt(item.rate)}</td>
                <td className="inv-td inv-td-right inv-td-amount">{fmt(itemAmount(item))}</td>
              </tr>
            ))}
            {extraRows.map((row, index) => (
              <tr key={row.key} className={(items.length + index) % 2 === 0 ? 'inv-tr-even' : ''}>
                <td className="inv-td inv-td-muted">-</td>
                <td className="inv-td inv-td-name">
                  <div style={{ fontWeight: 500 }}>{row.name || renderSubtitleLabel(row.en, row.ta)}</div>
                </td>
                <td className="inv-td inv-td-center">{row.kg ? `${formatKg(row.kg)} Kg` : '-'}</td>
                <td className="inv-td inv-td-right">-</td>
                <td className="inv-td inv-td-right inv-td-amount">{row.amount ? fmt(row.amount) : '-'}</td>
              </tr>
            ))}
          </tbody>
        </table>

        {/* Totals section */}
        <div className="inv-totals-section" style={{ margin: 0, padding: 0, alignItems: 'stretch', borderTop: '1px solid #e2e8f0', background: '#fff' }}>
          <div className="inv-words" style={{ padding: '0.75rem 0.5rem', borderRight: '1px solid #e2e8f0', flex: 1, display: 'flex', flexDirection: 'column' }}>
            <div style={{ marginBottom: '0.2rem', color: '#64748b', fontWeight: 400, fontSize: '0.8rem' }}>
              {totalWeightLabel}: <span style={{ fontWeight: 600, color: '#334155' }}>{formatKg(totalKg)} Kg</span>
            </div>
          {showAmountWords && (
            <>
              <div className="inv-words-text" style={{ fontStyle: 'normal', fontWeight: 600, color: '#0f172a', fontSize: '0.75rem', margin: 0, marginTop: '1.25rem', display: 'flex', flexDirection: 'column', gap: '0' }}>
                {(() => {
                   const wordsStr = amountInWords(totals.total);
                   if (wordsStr.includes(' / ')) {
                     const [pStr, sStr] = wordsStr.split(' / ');
                     return (
                       <>
                         <span>{pStr}</span>
                         <span style={{ fontWeight: 400, fontSize: '0.7rem', color: '#64748b' }}>{sStr}</span>
                       </>
                     );
                   }
                   return wordsStr;
                })()}
              </div>
            </>
          )}
          {/* UPI QR Code */}
          {qrDataUrl && (
            <div style={{ marginTop: '1.25rem' }}>
              <h4 className="inv-section-label">{renderLabel('SCAN TO PAY (UPI)', 'ஸ்கேன் செய்து செலுத்த')}</h4>
              <div style={{ display: 'flex', alignItems: 'center', gap: '0.75rem' }}>
                <img src={qrDataUrl} alt="UPI QR" style={{ width: '90px', height: '90px', borderRadius: '6px', border: '1px solid #e2e8f0' }} />
                <div style={{ fontSize: '0.7rem', color: '#94a3b8', lineHeight: 1.5 }}>
                  <p style={{ margin: 0, color: '#94a3b8' }}>UPI ID:</p>
                  <p style={{ margin: 0, color: '#334155', fontWeight: 600, fontSize: '0.75rem' }}>{upiId}</p>
                  <p style={{ margin: '0.25rem 0 0', color: '#94a3b8' }}>{fmt(totals.total)}</p>
                </div>
              </div>
            </div>
          )}
          </div>

          <div className="inv-totals" style={{ padding: '0.75rem 0.5rem', alignSelf: 'center', minWidth: '220px', width: 'auto', whiteSpace: 'nowrap' }}>
            {hasExtraAmounts && showSubtotal && (
              <div className="inv-total-row">
                <span>{profile?.enableBilingual === false && profile?.primaryDataLanguage === 'Tamil' ? 'உபமொத்தம்' : 'Subtotal'}</span>
                <span>{fmt(subtotal)}</span>
              </div>
            )}
            <div className="inv-total-row grand-total-row" style={{ fontWeight: 700, color: accent, marginTop: '0.35rem', paddingTop: '0', alignItems: 'center' }}>
              <div style={{ display: 'flex', flexDirection: 'column', gap: '1px' }}>
                <span style={{ fontSize: '0.85rem', color: accent, lineHeight: 1 }}>{profile?.primaryDataLanguage === 'Tamil' || !profile?.primaryDataLanguage ? 'மொத்தம்' : 'Grand Total'}</span>
                {profile?.enableBilingual !== false && <span style={{ fontSize: '0.55rem', color: accent, opacity: 0.8, fontWeight: 500 }}>{profile?.primaryDataLanguage === 'Tamil' || !profile?.primaryDataLanguage ? 'Grand Total' : 'மொத்தம்'}</span>}
              </div>
              <span style={{ color: accent, fontSize: '1.05rem', fontWeight: 800 }}>{fmt(totals.total)}</span>
            </div>
          </div>
        </div>
      </div>

      {/* Footer */}
      <div className="inv-footer" style={{ display: 'flex', flexDirection: 'column', marginTop: '1.5rem', paddingLeft: '0.5rem', paddingRight: '0.5rem' }}>
        
        {/* Top Row: Bank Details & Signature Image */}
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', width: '100%' }}>
          {/* Left: Bank Details */}
          <div style={{ display: 'flex', flexDirection: 'column', paddingTop: '5px' }}>
            {(() => {
              const isTamilPrimary = profile?.enableBilingual === false && profile?.primaryDataLanguage === 'Tamil';
              const bankNameTa = account?.vangiPeyar || profile?.vangiPeyar;
              const bankNameEn = account?.vangiPeyarEn || profile?.vangiPeyarEn;
              const bankName = isTamilPrimary ? (bankNameTa || bankNameEn) : (bankNameEn || bankNameTa);
              
              const branchTa = account?.bankBranch || profile?.bankBranch;
              const branchEn = account?.bankBranchEn || profile?.bankBranchEn;
              const branch = isTamilPrimary ? (branchTa || branchEn) : (branchEn || branchTa);

              return showBankDetails && (bankName) && (
              <div style={{ marginBottom: '15px', color: '#475569', fontSize: '0.85rem', lineHeight: '1.45', display: 'flex', flexDirection: 'column', gap: '3px' }}>
                {showAccountLabel && account?.label && (
                  <div style={{ fontSize: '0.75rem', fontStyle: 'italic', marginBottom: '1px' }}>
                    Pay via: <strong style={{ color: '#334155' }}>{account.label}</strong>
                  </div>
                )}
                <div style={{ display: 'flex', gap: '4px' }}>
                  <div style={{ fontWeight: 700, whiteSpace: 'nowrap', fontSize: '0.82rem', color: accent }}>{profile?.primaryDataLanguage === 'English' ? 'Bank Details' : 'வங்கி விவரம்'} :</div>
                  <div style={{ color: '#334155', fontWeight: 600 }}>
                    {bankName}
                    {branch ? `, ${branch}` : ''}
                  </div>
                </div>
                <div style={{ display: 'flex', gap: '4px' }}>
                  <div style={{ fontWeight: 700, whiteSpace: 'nowrap', fontSize: '0.82rem', color: accent }}>{profile?.primaryDataLanguage === 'English' ? 'Account No' : 'கணக்கு எண்'} :</div>
                  <div style={{ color: '#334155', fontWeight: 600 }}>{account?.kanakkuEn || profile.kanakkuEn}</div>
                </div>
                {(account?.ifsc || profile.ifsc) && (
                  <div style={{ display: 'flex', gap: '4px' }}>
                    <div style={{ fontWeight: 700, whiteSpace: 'nowrap', fontSize: '0.82rem', color: accent }}>IFSC :</div>
                    <div style={{ color: '#334155', fontWeight: 600 }}>{account?.ifsc || profile.ifsc}</div>
                  </div>
                )}
                {(account?.swift || profile.swift) && (
                  <div style={{ display: 'flex', gap: '4px' }}>
                    <div style={{ fontWeight: 700, whiteSpace: 'nowrap', fontSize: '0.82rem', color: accent }}>SWIFT/BIC :</div>
                    <div style={{ color: '#334155', fontWeight: 600 }}>{account?.swift || profile.swift}</div>
                  </div>
                )}
                {(account?.upiId || profile.upiId) && (
                  <div style={{ display: 'flex', gap: '4px' }}>
                    <div style={{ fontWeight: 700, whiteSpace: 'nowrap', fontSize: '0.82rem', color: accent }}>UPI :</div>
                    <div style={{ color: '#334155', fontWeight: 600 }}>{account?.upiId || profile.upiId}</div>
                  </div>
                )}
              </div>
            );})()}
          </div>

          {/* Right: Business Name & Image */}
          {showSignature && (
            <div style={{ textAlign: 'right', display: 'flex', flexDirection: 'column', alignItems: 'flex-end', minWidth: '220px' }}>
              <div style={{ color: accent, position: 'relative', zIndex: 10, fontSize: '1.05rem', fontWeight: 700, marginBottom: '5px', lineHeight: 1 }}>
                {getDynamicField(profile, 'niruvanathinPeyar', profile, false) || getDynamicField(profile, 'niruvanathinPeyar', profile, true) || 'Your Business'}
              </div>
              <div style={{ position: 'relative', height: '55px', width: '100%', display: 'flex', justifyContent: 'flex-end' }}>
                {profile?.signature && (
                  <img crossOrigin={profile.signature?.startsWith('http') ? 'anonymous' : undefined} src={profile.signature} alt="Signature" style={{ position: 'absolute', top: '50%', transform: 'translateY(-50%)', right: '-10px', maxHeight: '95px', maxWidth: '200px', objectFit: 'contain', pointerEvents: 'none' }} />
                )}
              </div>
            </div>
          )}
        </div>

        {/* Bottom Row: Nandri & Signatory Text */}
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-end', width: '100%', marginTop: 'auto', paddingTop: '10px' }}>
          <div style={{ fontFamily: 'inherit', fontSize: '0.85rem', fontWeight: 700, color: accent, lineHeight: 1 }}>
            {profile?.primaryDataLanguage === 'English' ? 'Thank You' : 'நன்றி'}
          </div>
          
          {showSignature && (
            <div style={{ minWidth: '220px', textAlign: 'right' }}>
              {showSignatoryText && (
                <div style={{ position: 'relative', zIndex: 10, fontSize: '0.75rem', fontWeight: 600, color: '#555', lineHeight: 1 }}>
                  {profile?.authorizedSignatoryName ? `(${profile.authorizedSignatoryName})` : `(${profile?.primaryDataLanguage === 'English' ? 'Signature' : 'கையொப்பம்'})`}
                </div>
              )}
            </div>
          )}
        </div>
      </div>

      {/* Contact Section */}
      {(() => {
        const addr1_eng = [getDynamicField(profile, 'mugavari', profile, false), getDynamicField(profile, 'oor', profile, false)].filter(Boolean).join(', ');
        const addr1_tam = [getDynamicField(profile, 'mugavari', profile, true), getDynamicField(profile, 'oor', profile, true)].filter(Boolean).join(', ');
        const addr1 = (addr1_eng || addr1_tam) + (profile?.pin ? ` - ${profile.pin}` : '');
        
        const rawCountry = profile?.country || 'India';
        const country_eng = profile?.enableBilingual === false ? '' : (getBilingualCountryName(rawCountry, { ...profile, returnOnlySecondary: true, fallbackEnglishName: profile?.country_English }) || profile?.country_English || rawCountry);
        const countryPrimary = getBilingualCountryName(rawCountry, { ...profile, returnOnlyPrimary: true });
        const country_tam = countryPrimary || (rawCountry === 'India' ? (profile?.primaryDataLanguage === 'English' ? 'India' : 'இந்தியா') : rawCountry);

        const dist_eng = [getDynamicField(profile, 'maavattam', profile, false), getBilingualStateName(profile.maanilam, { ...profile, returnOnlySecondary: true, fallbackEnglishName: profile.maanilamEn }), country_eng].filter(Boolean).join(', ');
        const dist_tam = [getDynamicField(profile, 'maavattam', profile, true), getBilingualStateName(profile.maanilam, { ...profile, returnOnlyPrimary: true }), country_tam].filter(Boolean).join(', ');
        const dist = dist_eng || dist_tam;
        const addr2 = dist;
        
        const email = profile?.email || profile?.minnanjal || '';
        const phoneArr = [];
        if (profile?.tholaipesi) phoneArr.push(...(Array.isArray(profile.tholaipesi) ? profile.tholaipesi : String(profile.tholaipesi).split(',')));
        if (profile?.mobileNumber) phoneArr.push(...(Array.isArray(profile.mobileNumber) ? profile.mobileNumber : String(profile.mobileNumber).split(',')));
        const phone = phoneArr.map(p => p.trim()).filter(Boolean);

        return (
          <div className="inv-contact-block" style={{ marginTop: 'auto', padding: '10px 1.5rem 14px 1.5rem', background: accent ? `${accent}15` : '#e8f5e9', marginBottom: 0 }}>
            <div style={{ padding: '0 0.5rem' }}>
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start' }}>
                <div style={{ display: 'flex', flexDirection: 'column', gap: '4px' }}>
                  <div style={{ fontSize: '0.85rem', fontWeight: 700, color: accent, display: 'flex', alignItems: 'center', minHeight: '20px' }}>
                    {profile?.primaryDataLanguage === 'Tamil' || !profile?.primaryDataLanguage ? 'தொடர்பு கொள்ள' : 'Contact Us'}
                  </div>
                  <div style={{ fontSize: '0.8rem', color: '#333', fontWeight: 500, display: 'flex', alignItems: 'center', minHeight: '20px' }}>
                    {addr1}
                  </div>
                  {addr2 && (
                    <div style={{ fontSize: '0.8rem', color: '#333', fontWeight: 500, display: 'flex', alignItems: 'center', minHeight: '20px' }}>
                      {addr2}
                    </div>
                  )}
                </div>
                {(phone.length > 0 || email) && (
                  <div style={{ display: 'flex', flexDirection: 'column', gap: '4px' }}>
                    {phone.map((num, i) => (
                      <div key={i} style={{ display: 'flex', alignItems: 'center', gap: '8px', fontSize: '0.8rem', fontWeight: 600, color: '#444', justifyContent: 'flex-end', minHeight: '20px' }}>
                        <div style={{ color: accent, display: 'flex' }}><IconPhone size={14} /></div>
                        <span style={{ fontVariantNumeric: 'tabular-nums', letterSpacing: '0.02em' }}>{num}</span>
                      </div>
                    ))}
                    {email && (
                      <div style={{ display: 'flex', alignItems: 'center', gap: '8px', fontSize: '0.8rem', color: '#444', justifyContent: 'flex-end', minHeight: '20px' }}>
                        <div style={{ color: accent, display: 'flex' }}><IconMail size={14} /></div>
                        <span>{email}</span>
                      </div>
                    )}
                  </div>
                )}
              </div>
            </div>
          </div>
        );
      })()}

      {/* Watermark for proforma */}
      {invoiceType === 'proforma' && (
        <div style={{
          position: 'absolute', top: '50%', left: '50%',
          transform: 'translate(-50%, -50%) rotate(-35deg)',
          fontSize: '5rem', fontWeight: 800, color: `${accent}0a`,
          pointerEvents: 'none', whiteSpace: 'nowrap'
        }}>
          ESTIMATE
        </div>
      )}


    </div>
  );
});

export default CoolieTheme;
