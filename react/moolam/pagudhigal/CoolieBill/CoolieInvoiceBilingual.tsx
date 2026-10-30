// @ts-nocheck
import React from 'react';
import './print.css'; // Same stylesheet as the original Coolie bill: same sizes and positions
import { numberToWords } from '../../Payanpadu';

// Coolie (Factory) invoice, print only. The original Coolie bill, made bilingual the way the
// Textiles invoice is (Tamil line + smaller English line), without the "Motham" table footer: the
// total and the amount in words are laid out like the Textiles invoice instead.

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

const gramsToKg = (grams) => (grams ? parseFloat(grams) / 1000 : 0);
const calcItemAmount = (rate, kg) => Math.floor((parseFloat(rate) || 0) * (parseFloat(kg) || 0));
const formatWeight = (val) => Number(val).toFixed(3);
const formatCurrency = (val) => Number(val).toLocaleString('en-IN');

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

export default function CoolieInvoiceBilingual({ bill }) {
  const p = bill._companyProfile || {};
  const bi = p.enableBilingual !== false;
  // Single-language bills follow the business's primary language
  const primaryEng = p.primaryDataLanguage === 'English';
  const isEng = !bi && primaryEng;
  const primaryLang = bi ? (p.primaryDataLanguage || 'Tamil') : (isEng ? 'English' : 'Tamil');
  const secondaryLang = bi ? (p.secondaryDataLanguage || 'English') : primaryLang;

  // Same rules as the Textiles bill. Table headings: Tamil line + smaller English line when bilingual.
  // Bill number, date and "Bill To" read "Tamil / English"; the other labels, the address and the bank
  // details are in one language (labels in the primary language, values in English when bilingual).
  const lines = (ta, en) => (bi ? [ta, en] : [isEng ? en : ta]).map((text, i) => <div key={i}>{text}</div>);
  const slash = (ta, en) => (bi ? `${ta} / ${en}` : (isEng ? en : ta));
  const lbl = (ta, en) => (primaryEng ? en : ta);

  const {
    bill_no: displayBillNo,
    date,
    customer_name: customerNameTa,
    customer_name_en: customerNameEn,
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
    show_ifsc: showIfsc,
  } = bill;

  let otherCharges = [];
  if (customChargeName) {
    try {
      otherCharges = customChargeName.startsWith('[') ? JSON.parse(customChargeName) : [{ name: customChargeName, amount: customChargeRs }];
    } catch (e) {
      otherCharges = [{ name: customChargeName, amount: customChargeRs }];
    }
  }

  // Figures
  const setharamKg = gramsToKg(setharamGrams);
  const totalKg = items.reduce((sum, item) => sum + (parseFloat(item.kg) || 0), 0) + setharamKg;
  const subtotal = items.reduce((sum, item) => sum + calcItemAmount(item.coolie, item.kg), 0);
  const totalRs = Math.floor(subtotal + (parseFloat(courierRs) || 0) + (parseFloat(ahimsaSilkRs) || 0) + (parseFloat(customChargeRs) || 0));

  // Customer / place: the Tamil value, with the English one under it when bilingual
  const place = (a, c) => (a && c ? `${a}, ${c}` : (a || c || ''));
  const useEn = bi || isEng;
  const customerName = (useEn ? (customerNameEn || customerNameTa) : customerNameTa) || '';
  const customerPlace = (useEn ? (place(addressEn, cityEn) || place(addressTa, cityTa)) : place(addressTa, cityTa));

  // Business
  const name = { english: p.nameEn || p.nameEnglish || '', tamil: p.name || p.nameTamil || '' };
  const phone = p.phone ? (Array.isArray(p.phone) ? p.phone : String(p.phone).split(',')) : [];
  const contactLines = (en) => {
    const line1 = en ? p.addressEn : p.address;
    const city = en ? p.cityEn : p.city;
    const district = en ? p.districtEn : p.district;
    const cityPin = [city, p.pincode].filter(Boolean).join(' - ');
    return { l1: [line1, cityPin].filter(Boolean).join(', '), l2: district || '' };
  };
  const contact = contactLines(useEn);
  const ifsc = billIfsc || p.ifsc || '';

  // Amount in words, like the Textiles invoice (primary line, smaller secondary line)
  const wordsStr = numberToWords(totalRs, primaryLang, secondaryLang, bi);
  const [wordsPrimary, wordsSecondary] = wordsStr.includes(' / ') ? wordsStr.split(' / ') : [wordsStr, ''];
  const accent = p.themeColor || '#1e3a8a';

  // Rows after the items: Ahimsa silk, other charges, wastage, courier (as on the original bill)
  const extraRows = [
    ...(ahimsaSilkRs ? [{ key: 'ahimsa', ta: 'அகிம்சா பட்டு', en: 'Ahimsa Silk', amount: ahimsaSilkRs }] : []),
    ...otherCharges
      .filter((c) => c.amount && parseFloat(String(c.amount)) !== 0)
      .map((c, i) => ({ key: `oc-${i}`, ta: c.name || 'பிற விவரம்', en: c.name ? '' : 'Other Charges', amount: c.amount })),
    ...(setharamGrams ? [{ key: 'setharam', ta: 'சேதாரம்', en: 'Wastage', weight: setharamKg }] : []),
    ...(courierRs ? [{ key: 'courier', ta: 'கொரியர் கட்டணம்', en: 'Courier Charges', amount: courierRs }] : []),
  ];

  return (
    <div className={`a4-paper font-tamil${bi ? ' bilingual' : ''}`} style={{ margin: '0 auto', background: 'white', ...themeStyles(p.themeColor) }}>
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
            <div className="company-name font-display">{name.english || name.tamil}</div>
            {name.english && name.tamil && <div className="company-subtitle font-tamil">{name.tamil}</div>}
          </div>
        </div>
        <div className="header-right">
          <div className="bill-type-badge font-tamil">
            {isEng ? 'Coolie Bill' : 'கூலி பில்'}
            {bi && <div style={{ fontSize: '10px', fontWeight: 500, opacity: 0.8 }}>Coolie Bill</div>}
          </div>
        </div>
      </div>

      <div className="header-divider"></div>

      {/* Bill Info Section */}
      <div className="bill-info-section">
        <div className="bill-meta-row">
          <span className="bill-meta"><div style={{ fontSize: 'inherit', fontWeight: 'inherit', color: 'inherit' }}>{slash('பில் எண்', 'Bill No')} : <strong style={{ textTransform: 'uppercase' }}>{displayBillNo}</strong></div></span>
          <span className="bill-meta"><div style={{ fontSize: 'inherit', fontWeight: 'inherit', color: 'inherit' }}>{slash('நாள்', 'Date')} : <strong>{date}</strong></div></span>
        </div>
        {/* Bill To */}
        <div className="inv-party" style={{ marginTop: '6px', paddingLeft: '15px', paddingRight: '15px' }}>
          <h4 className="inv-section-label" style={{ color: 'var(--bill-primary)', fontWeight: 700, fontSize: '12px', marginBottom: '3px', textTransform: 'none' }}>{slash('பெறுநர்', 'Bill To')}</h4>
          <p className="inv-party-name" style={{ color: '#000', fontWeight: 700, fontSize: '15px', margin: 0, marginBottom: '2px' }}>{customerName || '-'}</p>
          {customerPlace && <div className="inv-party-details" style={{ color: '#333', fontSize: '13px' }}><p style={{ color: '#333', margin: 0 }}>{customerPlace}</p></div>}
        </div>
      </div>

      {/* One box: the table and, attached to it, the total with the amount in words */}
      <div style={{ border: '1px solid #ddd', borderRadius: '8px', overflow: 'hidden', marginTop: '5px', marginBottom: '10px' }}>
        <table className="bill-table-new" style={{ margin: 0, border: 'none', borderRadius: 0 }}>
          <thead>
            <tr>
              <th style={{ width: '15%' }}>{lines('கூலி', 'Coolie')}</th>
              <th style={{ width: '45%', textAlign: 'left', paddingLeft: '15px' }}>{lines('உருப்படி', 'Item')}</th>
              <th style={{ width: '15%' }}>{lines('எடை', 'Weight')}</th>
              <th style={{ width: '25%', textAlign: 'right', paddingRight: '16px' }}>{lines('தொகை', 'Amount')}</th>
            </tr>
          </thead>
          <tbody>
            {items.map((item, i) => (
              <tr key={i} className={i % 2 === 1 ? 'row-alt' : ''}>
                <td className="text-center">{item.coolie || ''}</td>
                <td className="text-left">
                  <div>{isEng && item.name_english ? item.name_english : item.porul}</div>
                  {bi && <div>{item.name_english || ''}</div>}
                </td>
                <td className="text-center">{item.kg ? `${formatWeight(item.kg)} Kg` : ''}</td>
                <td className="text-right" style={{ fontVariantNumeric: 'tabular-nums', paddingRight: '16px' }}>{item.kg ? `₹${formatCurrency(calcItemAmount(item.coolie, item.kg))}` : ''}</td>
              </tr>
            ))}
            {extraRows.map((row, i) => (
              <tr key={row.key} className={(items.length + i) % 2 === 1 ? 'row-alt' : ''}>
                <td className="text-center">-</td>
                <td className="text-left">
                  <div>{isEng && row.en ? row.en : row.ta}</div>
                  {bi && <div>{row.en || ''}</div>}
                </td>
                <td className="text-center">{row.weight ? `${formatWeight(row.weight)} Kg` : '-'}</td>
                <td className="text-right" style={{ fontVariantNumeric: 'tabular-nums', paddingRight: '16px' }}>{row.amount ? `₹${formatCurrency(row.amount)}` : '-'}</td>
              </tr>
            ))}
          </tbody>
        </table>

        {totalRs > 0 && (
          <div style={{ display: 'flex', alignItems: 'stretch', borderTop: '1px solid #ddd', background: '#fff' }}>
            {/* Left side: Amount in Words */}
            <div style={{ padding: '0.65rem 1rem', borderRight: '1px solid #ddd', flex: '0 0 60%', width: '60%', boxSizing: 'border-box', display: 'flex', flexDirection: 'column', justifyContent: 'center' }}>
              <span style={{ fontSize: '11px', fontWeight: 700, color: '#64748b', textTransform: 'uppercase', letterSpacing: '0.5px', marginBottom: '4px' }}>
                {bi ? 'எழுத்தில் மொத்தத் தொகை / In Words' : (isEng ? 'Amount in Words' : 'எழுத்தில் மொத்தத் தொகை')}
              </span>
              <span style={{ fontSize: '13px', fontWeight: 700, color: '#0f172a', lineHeight: 1.35 }}>
                {wordsPrimary}
              </span>
              {wordsSecondary && (
                <span style={{ fontSize: '11.5px', fontWeight: 500, color: '#64748b', lineHeight: 1.3, marginTop: '2px' }}>
                  {wordsSecondary}
                </span>
              )}
            </div>

            {/* Right side: Stacked Totals (Total Weight + Grand Total) */}
            <div style={{ padding: '0.65rem 16px', flex: '0 0 40%', width: '40%', boxSizing: 'border-box', minWidth: '240px', display: 'flex', flexDirection: 'column', justifyContent: 'center', gap: '6px', background: 'var(--bill-primary-light, #f8fafc)' }}>
              {/* Row 1: Total Weight */}
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', gap: '1rem' }}>
                <div style={{ display: 'flex', flexDirection: 'column', gap: '1px' }}>
                  <span style={{ fontSize: '12px', fontWeight: 700, color: '#334155' }}>
                    {lbl('மொத்த எடை', 'Total Weight')}
                  </span>
                  {bi && (
                    <span style={{ fontSize: '10px', fontWeight: 500, color: '#64748b' }}>
                      {primaryEng ? 'மொத்த எடை' : 'Total Weight'}
                    </span>
                  )}
                </div>
                <span style={{ fontSize: '13.5px', fontWeight: 700, color: '#1e293b', fontVariantNumeric: 'tabular-nums' }}>
                  {formatWeight(totalKg)} Kg
                </span>
              </div>

              {/* Subtle divider */}
              <div style={{ borderTop: '1px solid rgba(0, 0, 0, 0.08)', margin: '1px 0' }}></div>

              {/* Row 2: Grand Total */}
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', gap: '1rem' }}>
                <div style={{ display: 'flex', flexDirection: 'column', gap: '1px' }}>
                  <span style={{ fontSize: '14px', fontWeight: 800, color: accent, lineHeight: 1.1 }}>
                    {lbl('மொத்தம்', 'Total')}
                  </span>
                  {bi && (
                    <span style={{ fontSize: '10px', fontWeight: 600, color: accent, opacity: 0.85 }}>
                      {primaryEng ? 'மொத்தம்' : 'Grand Total'}
                    </span>
                  )}
                </div>
                <span style={{ fontSize: '1.25rem', fontWeight: 800, color: accent, fontVariantNumeric: 'tabular-nums' }}>
                  ₹{formatCurrency(totalRs)}
                </span>
              </div>
            </div>
          </div>
        )}
      </div>

      <div style={{ flexGrow: 1 }}></div>

      {/* Footer: bank details in the Textiles style (labels in the primary language, values in English when bilingual) */}
      <div className="bill-footer-new">
        <div className="footer-left">
          {((showBankDetails && (bankDetails || accountNo)) || (showIfsc && ifsc)) && (
            <div style={{ marginBottom: '10px', color: 'var(--bill-text-dark)', fontSize: '0.9rem', lineHeight: '1.4', display: 'flex', flexDirection: 'column', gap: '6px' }}>
              {showBankDetails && (
                <div style={{ display: 'flex', gap: '5px' }}>
                  <div style={{ fontWeight: 'bold', whiteSpace: 'nowrap' }}>{lbl('வங்கி விவரம்', 'Bank Details')} :</div>
                  <div style={{ color: '#000' }}>
                    {useEn && p.bankNameEn ? `${p.bankNameEn}${p.branchEn ? ', ' + p.branchEn : ''}` : bankDetails}
                  </div>
                </div>
              )}
              {showBankDetails && accountNo && (
                <div style={{ display: 'flex', gap: '5px' }}>
                  <div style={{ fontWeight: 'bold', whiteSpace: 'nowrap' }}>{lbl('கணக்கு எண்', 'A/C No')} :</div>
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
          <div className="thank-you font-tamil">{lbl('நன்றி', 'Thank You')}</div>
        </div>
        <div className="preview-footer-right">
          <div className="sign-company font-display" style={{ position: 'relative', zIndex: 10 }}>{name.english || name.tamil}</div>
          <div className="sign-space" style={{ position: 'relative' }}>
            {p.signature && (
              <img src={p.signature} alt="Signature" style={{ position: 'absolute', top: '50%', transform: 'translateY(-50%)', right: '-10px', maxHeight: '95px', maxWidth: '200px', objectFit: 'contain', pointerEvents: 'none' }} />
            )}
          </div>
          <div className="sign-label" style={{ position: 'relative', zIndex: 10 }}>
            {p.authorizedSignatoryName ? `(${p.authorizedSignatoryName})` : lbl('(கையொப்பம்)', '(Authorized Signature)')}
          </div>
        </div>
      </div>

      {/* Contact Section: address on the left, phone numbers and e-mail on the right */}
      <div className="contact-section">
        <div className="contact-title">{lbl('தொடர்பு கொள்ள', 'Contact Us')}</div>
        <div className="contact-row">
          <div className="contact-left">
            <div className="contact-address">
              <div>{contact.l1}</div>
              {contact.l2 && <div>{contact.l2}</div>}
            </div>
          </div>
          <div className="contact-phones">
            {phone.map((num, i) => (
              <div key={i} className="phone-item-new" style={{ display: 'flex', alignItems: 'center', gap: '6px' }}>
                <div style={{ display: 'flex', alignItems: 'center', marginTop: '-2px' }}><IconPhone size={14} /></div>
                <span>{num}</span>
              </div>
            ))}
            {p.email && (
              <div className="phone-item-new" style={{ display: 'flex', alignItems: 'center', gap: '6px' }}>
                <div style={{ display: 'flex', alignItems: 'center', marginTop: '-2px' }}><IconMail size={14} /></div>
                <span>{p.email}</span>
              </div>
            )}
          </div>
        </div>
      </div>
    </div>
  );
}
