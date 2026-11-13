// @ts-nocheck
import React from 'react';

const PinIcon = () => <svg viewBox="0 0 24 24" aria-hidden="true"><path fill="currentColor" d="M12 2a7 7 0 0 0-7 7c0 5.25 7 13 7 13s7-7.75 7-13a7 7 0 0 0-7-7Zm0 9.5A2.5 2.5 0 1 1 12 6.5a2.5 2.5 0 0 1 0 5Z"/></svg>;
const PhoneIcon = () => <svg viewBox="0 0 24 24" aria-hidden="true"><path fill="currentColor" d="m6.6 10.8 2.2-2.2a1 1 0 0 0 .25-1C8.7 6.45 8.5 5.25 8.5 4a1 1 0 0 0-1-1H4a1 1 0 0 0-1 1c0 9.4 7.6 17 17 17a1 1 0 0 0 1-1v-3.5a1 1 0 0 0-1-1c-1.25 0-2.46-.2-3.58-.56a1 1 0 0 0-1 .24l-2.2 2.2a15 15 0 0 1-6.62-6.58Z"/></svg>;

const value = (v, code) => (v && typeof v === 'object' ? v[code] : (code === 'en' ? v : '')) || '';
const languageCode = (v, fallback) => String(v || '').toLowerCase().startsWith('en') ? 'en' : String(v || '').toLowerCase().startsWith('ta') ? 'ta' : fallback;
const hexRgb = (hex) => {
  const h = String(hex || '').replace('#', '');
  const n = parseInt(h.length === 3 ? h.split('').map(x => x + x).join('') : h, 16);
  return Number.isFinite(n) ? [(n >> 16) & 255, (n >> 8) & 255, n & 255] : [122, 11, 148];
};

export default function Letterhead({ profile }) {
  const p = profile || {};
  const settings = p.printSettings || {};
  const primary = languageCode(p.primaryLanguage, 'ta');
  const secondary = languageCode(p.secondaryLanguage, primary === 'ta' ? 'en' : 'ta');
  const bilingual = p.isBilingual !== false;
  const first = languageCode(settings.langRules?.businessName, primary);
  const second = first === 'ta' ? 'en' : 'ta';
  const names = [value(p.businessName, first), bilingual ? value(p.businessName, second) : ''].filter(Boolean);
  const theme = p.themeColor || '#7a0b94';
  const [r, g, b] = hexRgb(theme);
  const tint = `rgba(${r},${g},${b},.09)`;
  // A wide logo often contains the company name baked into a small bitmap. It looks
  // acceptable at its source size but becomes visibly soft in PDF page-fit preview.
  // Letterheads therefore keep the emblem as an image and draw both names as PDF text.
  const usesWideFallback = !p.logo && !!p.wideLogo;
  const logo = p.logo || p.wideLogo;
  const scale = Number(usesWideFallback ? settings.wideLogoScale : settings.logoScale) || 1;
  const x = Number(usesWideFallback ? settings.wideLogoX : settings.logoX) || 0;
  const y = Number(usesWideFallback ? settings.wideLogoY : settings.logoY) || 0;
  const addrCode = value(p.address, primary) ? primary : secondary;
  const address = [value(p.address, addrCode), value(p.city, addrCode), p.pinCode].filter(Boolean).join(', ');
  const district = [value(p.district, addrCode), value(p.state, addrCode)].filter(Boolean).join(', ');
  const phones = [p.phone1 || p.phone, p.phone2 || p.altPhone].filter(Boolean);
  const greetingEnglish = primary === 'en' && !bilingual;

  return <div className="letterhead-paper" style={{ width: '210mm', height: '297mm', boxSizing: 'border-box', background: '#fff', color: '#151019', overflow: 'hidden', position: 'relative', margin: '0 auto', fontFamily: 'Arial, "Noto Sans Tamil", "Nirmala UI", sans-serif' }}>
    <div style={{ height: '8mm', padding: '2.6mm 8mm 0', display: 'flex', justifyContent: 'space-between', color: theme, fontSize: '3mm', fontWeight: 700 }}>
      <span>{greetingEnglish ? 'Vaazhga Vaiyagam' : 'வாழ்க வையகம்'}</span><span>உ</span><span>{greetingEnglish ? 'Vaazhga Valamudan' : 'வாழ்க வளமுடன்'}</span>
    </div>
    <div style={{ minHeight: '27mm', margin: '0 8mm', padding: '1mm 0 3mm', borderBottom: '0.35mm solid #e2e8f0', display: 'flex', alignItems: 'center', gap: '5mm' }}>
      {logo && <img src={logo} crossOrigin={String(logo).startsWith('http') ? 'anonymous' : undefined} alt="Logo" style={{ width: usesWideFallback ? '30mm' : '23mm', height: '20mm', flex: 'none', objectFit: 'contain', objectPosition: 'left center', transform: `translate(${x}px, ${y}px) scale(${scale})`, transformOrigin: 'left center' }}/>}
      <div style={{ minWidth: 0, flex: 1 }}>
        <div lang={first} style={{ color: theme, fontSize: '5.5mm', lineHeight: 1.2, fontWeight: 750, whiteSpace: 'nowrap' }}>{names[0]}</div>
        {names[1] && <div lang={second} style={{ color: '#475569', fontSize: '3.8mm', lineHeight: 1.35, fontWeight: 650, whiteSpace: 'nowrap' }}>{names[1]}</div>}
      </div>
      <div style={{ textAlign: 'right', color: theme, flex: 'none', marginLeft: 'auto' }}><div lang="ta" style={{ fontSize: '4mm', fontWeight: 750 }}>கடிதத்தலை</div><div style={{ fontSize: '2.8mm', color: '#475569' }}>Letterhead</div></div>
    </div>
    <div style={{ margin: '4mm 8mm 0', minHeight: '18mm', borderRadius: '3mm', background: tint, padding: '2.8mm 4mm', display: 'flex', alignItems: 'center', justifyContent: 'space-between', gap: '6mm', boxSizing: 'border-box' }}>
      <div style={{ display: 'flex', alignItems: 'center', gap: '2.5mm', minWidth: 0 }}><span style={{ width: '5mm', height: '5mm', color: theme, flex: 'none' }}><PinIcon/></span><div style={{ fontSize: '3mm', lineHeight: 1.4 }}><div>{address}</div>{district && <div>{district}</div>}</div></div>
      <div style={{ flex: 'none', fontSize: '3.1mm', lineHeight: 1.5 }}>{phones.map((phone, i) => <div key={i} style={{ display: 'flex', alignItems: 'center', gap: '1.8mm' }}><span style={{ width: '3.5mm', height: '3.5mm', color: theme }}><PhoneIcon/></span>{phone}</div>)}</div>
    </div>
    <div style={{ position: 'absolute', top: '61mm', right: '8mm', width: '72mm', height: '12mm', borderRadius: '2.5mm', background: tint, color: theme, display: 'flex', alignItems: 'center', padding: '0 4mm', boxSizing: 'border-box', fontSize: '3.6mm', fontWeight: 700 }}><span lang="ta">தேதி</span>&nbsp;/ Date :</div>
    <div style={{ position: 'absolute', right: '8mm', bottom: '14mm', color: theme, fontSize: '3.4mm', fontWeight: 700 }}><span lang="ta">கையொப்பம்</span> / Signature</div>
    <div style={{ position: 'absolute', left: 0, right: 0, bottom: 0, height: '7mm', background: tint }}/>
  </div>;
}
