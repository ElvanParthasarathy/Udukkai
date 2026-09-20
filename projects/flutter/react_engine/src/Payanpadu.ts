// @ts-nocheck
import { Capacitor } from '@capacitor/core';
import numberToWordsTamil from './mozhi/tamilNumbers';

export const getPrintHeadContent = async () => {
  let headHtml = document.head.innerHTML;
  
  if (Capacitor.isNativePlatform()) {
    let inlineStyles = '';
    const links = document.querySelectorAll('link[rel="stylesheet"]');
    for (const link of Array.from(links)) {
      if (link.href) {
        try {
          const response = await fetch(link.href);
          let cssText = await response.text();
          // Fix relative font URLs
          cssText = cssText.replace(/url\(\/([^)]+)\)/g, 'url(file:///android_asset/public/$1)');
          inlineStyles += cssText + '\n';
        } catch (e) {
          console.error('Failed to inline CSS', e);
        }
      }
    }
    
    const parser = new DOMParser();
    const doc = parser.parseFromString(`<html><head>${headHtml}</head></html>`, 'text/html');
    
    // Remove original links to prevent failure loading
    const headLinks = doc.querySelectorAll('link[rel="stylesheet"]');
    headLinks.forEach(l => l.remove());
    
    // Fix all absolute paths in HTML elements
    const elementsWithSrc = doc.querySelectorAll('[src^="/"], [href^="/"]');
    elementsWithSrc.forEach(el => {
      if (el.hasAttribute('src') && el.getAttribute('src').startsWith('/')) {
        el.setAttribute('src', 'file:///android_asset/public' + el.getAttribute('src'));
      }
      if (el.hasAttribute('href') && el.getAttribute('href').startsWith('/')) {
        el.setAttribute('href', 'file:///android_asset/public' + el.getAttribute('href'));
      }
    });

    return doc.head.innerHTML + `<style>${inlineStyles}</style>`;
  }
  
  return headHtml;
};


export const getDynamicField = (obj: any, fieldName: string, profile: any, isPrimary = true): string => {
  if (!obj) return '';
  const enableBilingual = profile?.enableBilingual !== false;
  
  // SHIELD: If bilingual mode is OFF, never return secondary fields.
  // This enforces strict single language across the entire bill rendering logic.
  if (!enableBilingual && !isPrimary) {
    return '';
  }

  const primaryLang = profile?.primaryDataLanguage || 'Tamil';
  const secondaryLang = profile?.secondaryDataLanguage || 'English';
  const lang = isPrimary ? primaryLang : secondaryLang;

  const exactVal = obj[`${fieldName}_${lang}`];
  const baseVal = obj[fieldName];

  // If a regional language field accidentally got saved with Tamil text (due to auto-save), 
  // suppress it so it correctly shows as a blank state like the user expects.
  if (exactVal) {
    if (lang !== 'Tamil' && /[\u0B80-\u0BFF]/.test(exactVal)) {
      return '';
    }
    return exactVal;
  }
  
  if (baseVal && typeof baseVal === 'string') {
    if (lang !== 'Tamil' && /[\u0B80-\u0BFF]/.test(baseVal)) {
      return ''; // baseVal has Tamil text, but we asked for English
    }
    return baseVal;
  }

  // CRITICAL FIX: If we still have NO data, but the user requested the primary language,
  // we should check if the secondary language has data! Otherwise, single-language mode 
  // causes data to magically disappear if they only filled out the secondary fields!
  if (!enableBilingual && isPrimary) {
    const fallbackVal = obj[`${fieldName}_${secondaryLang}`];
    if (fallbackVal) {
      return fallbackVal;
    }
  }
  
  return '';
};

export const numberToWords = (num, primaryLang = 'English', secondaryLang = 'English', enableBilingual = false) => {
  const isTamilPrimary = primaryLang === 'Tamil';
  const isTamilSecondary = secondaryLang === 'Tamil';

  const roundedNum = Math.round(num * 100) / 100;
  const rupees = Math.floor(roundedNum);
  const paise = Math.round((roundedNum - rupees) * 100);

  let enResult = '';
  if (num === 0) {
    enResult = 'Zero Rupees Only';
  } else {
    const a = ['', 'One ', 'Two ', 'Three ', 'Four ', 'Five ', 'Six ', 'Seven ', 'Eight ', 'Nine ', 'Ten ', 'Eleven ', 'Twelve ', 'Thirteen ', 'Fourteen ', 'Fifteen ', 'Sixteen ', 'Seventeen ', 'Eighteen ', 'Nineteen '];
    const b = ['', '', 'Twenty', 'Thirty', 'Forty', 'Fifty', 'Sixty', 'Seventy', 'Eighty', 'Ninety'];

    const convertToWords = (n) => {
      if (n < 20) return a[n];
      return b[Math.floor(n / 10)] + (n % 10 !== 0 ? ' ' + a[n % 10] : '');
    };

    const getIndianFormatString = (n) => {
      let res = '';
      const crore = Math.floor(n / 10000000);
      n -= crore * 10000000;
      const lakh = Math.floor(n / 100000);
      n -= lakh * 100000;
      const thousand = Math.floor(n / 1000);
      n -= thousand * 1000;
      const hundred = Math.floor(n / 100);
      n -= hundred * 100;

      if (crore > 0) res += convertToWords(crore) + ' Crore ';
      if (lakh > 0) res += convertToWords(lakh) + ' Lakh ';
      if (thousand > 0) res += convertToWords(thousand) + ' Thousand ';
      if (hundred > 0) res += convertToWords(hundred) + ' Hundred ';
      if (n > 0) res += (res !== '' ? 'and ' : '') + convertToWords(n);
      return res.trim();
    };

    enResult = getIndianFormatString(rupees) + ' Rupees';
    if (paise > 0) {
      enResult += ' and ' + getIndianFormatString(paise) + ' Paise';
    }
    enResult += ' Only';
  }

  let taResult = '';
  if (num === 0) {
    taResult = 'சுழியம் ரூபாய் மட்டும்';
  } else {
    taResult = numberToWordsTamil(rupees, 'ரூபாய்').trim();
    if (paise > 0) {
      taResult += ' மற்றும் ' + numberToWordsTamil(paise, 'காசுகள்').trim();
    }
    taResult += ' மட்டும்';
  }

  const getLanguageResult = (lang) => {
    if (lang === 'English') return enResult;
    if (lang === 'Tamil') return taResult;
    return '';
  };

  const primaryText = getLanguageResult(primaryLang);
  const secondaryText = getLanguageResult(secondaryLang);

  if (!enableBilingual) {
    return primaryText || enResult;
  }

  if (primaryText && secondaryText) return `${primaryText} / ${secondaryText}`;
  return primaryText || secondaryText || '';
};

export const formatCurrency = (amount, currency = 'INR') => {
  const locale = currency === 'INR' ? 'en-IN' : 'en-US';
  return new Intl.NumberFormat(locale, {
    style: 'currency',
    currency: currency || 'INR',
    minimumFractionDigits: 2
  }).format(amount || 0);
};

// Compute the per-item tax breakdown.
// `taxInclusive=true` means rate already includes tax (MRP-style) — back-calculate the
// taxable value. This matches the bill form's "Prices include tax" toggle.
//
// Inputs are defensively coerced: `Number(...)` turns strings (from CSV import) into
// numbers; `isFinite` filters out NaN/Infinity; `Math.max(0, ...)` clamps negatives.
// The form clamps these at input time via `clampNonNeg`, but anyone calling the
// helper directly (e.g. CSV import, recurring template materialisation) gets the
// same safety net here.
const finiteNonNeg = (n) => {
  const x = Number(n);
  return isFinite(x) && x > 0 ? x : 0;
};

export const calculateLineItemTax = (item: any = {}, taxInclusive = false) => {
  const qty = finiteNonNeg(item.qty || item.quantity);
  const rate = finiteNonNeg(item.rate);
  const discount = finiteNonNeg(item.discount);
  const taxRate = finiteNonNeg(item.taxPercent);
  const amount = qty * rate;
  const grossAfterDiscount = Math.max(0, amount - discount); // discount can't exceed line value
  if (taxInclusive && taxRate > 0) {
    const afterDiscount = grossAfterDiscount / (1 + taxRate / 100);
    const taxAmount = grossAfterDiscount - afterDiscount;
    return { amount, discount, afterDiscount, taxAmount, total: grossAfterDiscount };
  }
  const afterDiscount = grossAfterDiscount;
  const taxAmount = (afterDiscount * taxRate) / 100;
  return { amount, discount, afterDiscount, taxAmount, total: afterDiscount + taxAmount };
};

// Invoice type configuration
export const INVOICE_TYPES = {
  'tax-invoice': {
    label: 'Tax Invoice',
    prefix: 'INV',
    title: 'TAX INVOICE',
    showGST: true,
    description: 'Standard GST tax invoice',
  },
  'proforma': {
    label: 'Proforma / Estimate',
    prefix: 'EST',
    title: 'PROFORMA INVOICE',
    showGST: true,
    description: 'Quotation or estimate — not a legal tax document',
  },
};

export const tamilNaduDistricts = [
  { en: "Ariyalur", ta: "அரியலூர்" },
  { en: "Chengalpattu", ta: "செங்கல்பட்டு" },
  { en: "Chennai", ta: "சென்னை" },
  { en: "Coimbatore", ta: "கோயம்புத்தூர்" },
  { en: "Cuddalore", ta: "கடலூர்" },
  { en: "Dharmapuri", ta: "தருமபுரி" },
  { en: "Dindigul", ta: "திண்டுக்கல்" },
  { en: "Erode", ta: "ஈரோடு" },
  { en: "Kallakurichi", ta: "கள்ளக்குறிச்சி" },
  { en: "Kanchipuram", ta: "காஞ்சிபுரம்" },
  { en: "Kanyakumari", ta: "கன்னியாகுமரி" },
  { en: "Karur", ta: "கரூர்" },
  { en: "Krishnagiri", ta: "கிருஷ்ணகிரி" },
  { en: "Madurai", ta: "மதுரை" },
  { en: "Mayiladuthurai", ta: "மயிலாடுதுறை" },
  { en: "Nagapattinam", ta: "நாகப்பட்டினம்" },
  { en: "Namakkal", ta: "நாமக்கல்" },
  { en: "Nilgiris", ta: "நீலகிரி" },
  { en: "Perambalur", ta: "பெரம்பலூர்" },
  { en: "Pudukkottai", ta: "புதுக்கோட்டை" },
  { en: "Ramanathapuram", ta: "இராமநாதபுரம்" },
  { en: "Ranipet", ta: "இராணிப்பேட்டை" },
  { en: "Salem", ta: "சேலம்" },
  { en: "Sivaganga", ta: "சிவகங்கை" },
  { en: "Tenkasi", ta: "தென்காசி" },
  { en: "Thanjavur", ta: "தஞ்சாவூர்" },
  { en: "Theni", ta: "தேனி" },
  { en: "Thoothukudi", ta: "தூத்துக்குடி" },
  { en: "Tiruchirappalli", ta: "திருச்சிராப்பள்ளி" },
  { en: "Tirunelveli", ta: "திருநெல்வேலி" },
  { en: "Tirupattur", ta: "திருப்பத்தூர்" },
  { en: "Tiruppur", ta: "திருப்பூர்" },
  { en: "Tiruvallur", ta: "திருவள்ளூர்" },
  { en: "Tiruvannamalai", ta: "திருவண்ணாமலை" },
  { en: "Tiruvarur", ta: "திருவாரூர்" },
  { en: "Vellore", ta: "வேலூர்" },
  { en: "Viluppuram", ta: "விழுப்புரம்" },
  { en: "Virudhunagar", ta: "விருதுநகர்" }
];

// Indian states list for dropdowns
export const INDIAN_STATES = [
  'Andhra Pradesh', 'Arunachal Pradesh', 'Assam', 'Bihar', 'Chhattisgarh',
  'Goa', 'Gujarat', 'Haryana', 'Himachal Pradesh', 'Jharkhand',
  'Karnataka', 'Kerala', 'Madhya Pradesh', 'Maharashtra', 'Manipur',
  'Meghalaya', 'Mizoram', 'Nagaland', 'Odisha', 'Punjab',
  'Rajasthan', 'Sikkim', 'Tamil Nadu', 'Telangana', 'Tripura',
  'Uttar Pradesh', 'Uttarakhand', 'West Bengal',
  'Andaman and Nicobar Islands', 'Chandigarh', 'Dadra and Nagar Haveli and Daman and Diu',
  'Delhi', 'Jammu and Kashmir', 'Ladakh', 'Lakshadweep', 'Puducherry'
];

// US States + DC
export const US_STATES = [
  'Alabama','Alaska','Arizona','Arkansas','California','Colorado','Connecticut','Delaware',
  'Florida','Georgia','Hawaii','Idaho','Illinois','Indiana','Iowa','Kansas','Kentucky',
  'Louisiana','Maine','Maryland','Massachusetts','Michigan','Minnesota','Mississippi',
  'Missouri','Montana','Nebraska','Nevada','New Hampshire','New Jersey','New Mexico',
  'New York','North Carolina','North Dakota','Ohio','Oklahoma','Oregon','Pennsylvania',
  'Rhode Island','South Carolina','South Dakota','Tennessee','Texas','Utah','Vermont',
  'Virginia','Washington','West Virginia','Wisconsin','Wyoming','District of Columbia'
];

// Canada Provinces & Territories
export const CANADA_PROVINCES = [
  'Alberta','British Columbia','Manitoba','New Brunswick','Newfoundland and Labrador',
  'Northwest Territories','Nova Scotia','Nunavut','Ontario','Prince Edward Island',
  'Quebec','Saskatchewan','Yukon'
];

// Australia States & Territories
export const AUSTRALIA_STATES = [
  'New South Wales','Victoria','Queensland','South Australia',
  'Western Australia','Tasmania','Australian Capital Territory','Northern Territory'
];

// Returns states/provinces list for a country, or [] if free-text is better
export const getStatesForCountry = (countryName) => {
  switch (countryName) {
    case 'India': return INDIAN_STATES;
    case 'United States': return US_STATES;
    case 'Canada': return CANADA_PROVINCES;
    case 'Australia': return AUSTRALIA_STATES;
    default: return [];
  }
};

// Configurable array of supported country codes. 
// Change to ['ALL'] or add other codes like ['IN', 'US', 'AE'] to scale internationally.
export const SUPPORTED_REGIONS = ['IN'];

// ========== Country Configuration ==========
// Each entry: { name, code, currency, currencySymbol, taxLabel, taxIdLabel, taxIdPlaceholder, bankLabel, postalLabel, stateLabel, hasStates, taxRates, taxIdRegex }
// taxRates: common rates for that country's tax dropdown (always allow custom entry)
// taxIdRegex: optional pattern for soft validation (warning only, never blocks save)
export const COUNTRIES = [
  { name: 'India', code: 'IN', currency: 'INR', currencySymbol: '₹', taxLabel: 'GST', taxIdLabel: 'GSTIN', taxIdPlaceholder: '22AAAAA0000A1Z5', bankLabel: 'IFSC Code', postalLabel: 'PIN Code', stateLabel: 'maanilam', hasStates: true, taxRates: [0, 0.1, 0.25, 3, 5, 12, 18, 28], taxIdRegex: /^\d{2}[A-Z]{5}\d{4}[A-Z]\d[A-Z][A-Z\d]$/ },
  { name: 'United Arab Emirates', code: 'AE', currency: 'AED', currencySymbol: 'AED', taxLabel: 'VAT', taxIdLabel: 'TRN', taxIdPlaceholder: '100123456700003', bankLabel: 'IBAN', postalLabel: 'Postal Code', stateLabel: 'Emirate', hasStates: false, taxRates: [0, 5], taxIdRegex: /^\d{15}$/ },
  { name: 'United States', code: 'US', currency: 'USD', currencySymbol: '$', taxLabel: 'Sales Tax', taxIdLabel: 'EIN / TIN', taxIdPlaceholder: '12-3456789', bankLabel: 'Routing Number', postalLabel: 'ZIP Code', stateLabel: 'maanilam', hasStates: false, taxRates: [0, 4, 6, 7, 8, 9, 10], taxIdRegex: /^\d{2}-?\d{7}$/ },
  { name: 'United Kingdom', code: 'GB', currency: 'GBP', currencySymbol: '£', taxLabel: 'VAT', taxIdLabel: 'VAT Number', taxIdPlaceholder: 'GB123456789', bankLabel: 'Sort Code', postalLabel: 'Postcode', stateLabel: 'County', hasStates: false, taxRates: [0, 5, 20], taxIdRegex: /^GB\d{9}(\d{3})?$/i },
  { name: 'Australia', code: 'AU', currency: 'AUD', currencySymbol: 'A$', taxLabel: 'GST', taxIdLabel: 'ABN', taxIdPlaceholder: '12 345 678 901', bankLabel: 'BSB Number', postalLabel: 'Postcode', stateLabel: 'maanilam/Territory', hasStates: false, taxRates: [0, 10], taxIdRegex: /^\d{2}\s?\d{3}\s?\d{3}\s?\d{3}$/ },
  { name: 'Canada', code: 'CA', currency: 'CAD', currencySymbol: 'CA$', taxLabel: 'GST/HST', taxIdLabel: 'GST/HST Number', taxIdPlaceholder: '123456789 RT 0001', bankLabel: 'Transit Number', postalLabel: 'Postal Code', stateLabel: 'Province', hasStates: false, taxRates: [0, 5, 13, 15], taxIdRegex: /^\d{9}\s?(RT)\s?\d{4}$/i },
  { name: 'Singapore', code: 'SG', currency: 'SGD', currencySymbol: 'S$', taxLabel: 'GST', taxIdLabel: 'GST Reg No.', taxIdPlaceholder: 'M12345678X', bankLabel: 'Bank Code', postalLabel: 'Postal Code', stateLabel: 'Region', hasStates: false, taxRates: [0, 9], taxIdRegex: /^[MTFG]\d{7,8}[A-Z]$/i },
  { name: 'Malaysia', code: 'MY', currency: 'MYR', currencySymbol: 'RM', taxLabel: 'SST', taxIdLabel: 'SST No.', taxIdPlaceholder: 'W10-1234-56789012', bankLabel: 'Bank Code', postalLabel: 'Postcode', stateLabel: 'maanilam', hasStates: false, taxRates: [0, 6, 8, 10] },
  { name: 'Germany', code: 'DE', currency: 'EUR', currencySymbol: '€', taxLabel: 'MwSt', taxIdLabel: 'USt-IdNr.', taxIdPlaceholder: 'DE123456789', bankLabel: 'IBAN', postalLabel: 'PLZ', stateLabel: 'Bundesland', hasStates: false, taxRates: [0, 7, 19], taxIdRegex: /^DE\d{9}$/i },
  { name: 'France', code: 'FR', currency: 'EUR', currencySymbol: '€', taxLabel: 'TVA', taxIdLabel: 'N° TVA', taxIdPlaceholder: 'FR12345678901', bankLabel: 'IBAN', postalLabel: 'Code Postal', stateLabel: 'Région', hasStates: false, taxRates: [0, 5.5, 10, 20], taxIdRegex: /^FR[A-Z\d]{2}\d{9}$/i },
  { name: 'Netherlands', code: 'NL', currency: 'EUR', currencySymbol: '€', taxLabel: 'BTW', taxIdLabel: 'BTW-nummer', taxIdPlaceholder: 'NL123456789B01', bankLabel: 'IBAN', postalLabel: 'Postcode', stateLabel: 'Provincie', hasStates: false, taxRates: [0, 9, 21], taxIdRegex: /^NL\d{9}B\d{2}$/i },
  { name: 'South Africa', code: 'ZA', currency: 'ZAR', currencySymbol: 'R', taxLabel: 'VAT', taxIdLabel: 'VAT Number', taxIdPlaceholder: '4123456789', bankLabel: 'kilai Code', postalLabel: 'Postal Code', stateLabel: 'Province', hasStates: false, taxRates: [0, 15], taxIdRegex: /^4\d{9}$/ },
  { name: 'Nigeria', code: 'NG', currency: 'NGN', currencySymbol: '₦', taxLabel: 'VAT', taxIdLabel: 'TIN', taxIdPlaceholder: '12345678-0001', bankLabel: 'Bank Code', postalLabel: 'Postal Code', stateLabel: 'maanilam', hasStates: false, taxRates: [0, 7.5] },
  { name: 'Kenya', code: 'KE', currency: 'KES', currencySymbol: 'KSh', taxLabel: 'VAT', taxIdLabel: 'KRA PIN', taxIdPlaceholder: 'A123456789Z', bankLabel: 'Bank Code', postalLabel: 'Postal Code', stateLabel: 'County', hasStates: false, taxRates: [0, 8, 16], taxIdRegex: /^[A-Z]\d{9}[A-Z]$/i },
  { name: 'Saudi Arabia', code: 'SA', currency: 'SAR', currencySymbol: 'SAR', taxLabel: 'VAT', taxIdLabel: 'VAT Number', taxIdPlaceholder: '300012345600003', bankLabel: 'IBAN', postalLabel: 'Postal Code', stateLabel: 'Region', hasStates: false, taxRates: [0, 15], taxIdRegex: /^3\d{14}$/ },
  { name: 'Nepal', code: 'NP', currency: 'NPR', currencySymbol: 'Rs', taxLabel: 'VAT', taxIdLabel: 'PAN/VAT No.', taxIdPlaceholder: '123456789', bankLabel: 'Bank Code', postalLabel: 'Postal Code', stateLabel: 'Province', hasStates: false, taxRates: [0, 13] },
  { name: 'Bangladesh', code: 'BD', currency: 'BDT', currencySymbol: '৳', taxLabel: 'VAT', taxIdLabel: 'BIN', taxIdPlaceholder: '123456789-0101', bankLabel: 'Bank Code', postalLabel: 'Postal Code', stateLabel: 'Division', hasStates: false, taxRates: [0, 5, 7.5, 10, 15] },
  { name: 'Sri Lanka', code: 'LK', currency: 'LKR', currencySymbol: 'Rs', taxLabel: 'VAT', taxIdLabel: 'VAT Reg No.', taxIdPlaceholder: '123456789-7000', bankLabel: 'Bank Code', postalLabel: 'Postal Code', stateLabel: 'Province', hasStates: false, taxRates: [0, 18] },
  { name: 'Pakistan', code: 'PK', currency: 'PKR', currencySymbol: 'Rs', taxLabel: 'GST', taxIdLabel: 'NTN', taxIdPlaceholder: '1234567-8', bankLabel: 'Bank Code', postalLabel: 'Postal Code', stateLabel: 'Province', hasStates: false, taxRates: [0, 5, 10, 17, 18] },
  { name: 'Philippines', code: 'PH', currency: 'PHP', currencySymbol: '₱', taxLabel: 'VAT', taxIdLabel: 'TIN', taxIdPlaceholder: '123-456-789-000', bankLabel: 'Bank Code', postalLabel: 'ZIP Code', stateLabel: 'Region', hasStates: false, taxRates: [0, 12] },
  { name: 'Indonesia', code: 'ID', currency: 'IDR', currencySymbol: 'Rp', taxLabel: 'PPN', taxIdLabel: 'NPWP', taxIdPlaceholder: '12.345.678.9-012.000', bankLabel: 'Bank Code', postalLabel: 'Kode Pos', stateLabel: 'Provinsi', hasStates: false, taxRates: [0, 11, 12] },
  { name: 'New Zealand', code: 'NZ', currency: 'NZD', currencySymbol: 'NZ$', taxLabel: 'GST', taxIdLabel: 'GST Number', taxIdPlaceholder: '123-456-789', bankLabel: 'Bank kilai', postalLabel: 'Postcode', stateLabel: 'Region', hasStates: false, taxRates: [0, 15] },
  { name: 'Other', code: 'XX', currency: 'USD', currencySymbol: '$', taxLabel: 'Tax', taxIdLabel: 'Tax ID', taxIdPlaceholder: 'Your tax registration number', bankLabel: 'Bank Routing', postalLabel: 'Postal Code', stateLabel: 'maanilam/Region', hasStates: false, taxRates: [0, 5, 10, 15, 20] },
];

export const getCountryConfig = (countryName) => {
  const supported = getCountriesForRegion();
  if (!countryName) return supported[0] || COUNTRIES[0]; 
  return supported.find(c => c.name === countryName) || supported.find(c => c.code === countryName) || supported[0] || COUNTRIES[0];
};

export const getCountriesForRegion = () => {
  if (SUPPORTED_REGIONS.includes('ALL')) return COUNTRIES;
  return COUNTRIES.filter(c => SUPPORTED_REGIONS.includes(c.code));
};

// GST maanilam Codes (as per GST portal) — used in GSTR-1 JSON export
const GST_STATE_CODES = {
  'jammu and kashmir': '01', 'himachal pradesh': '02', 'punjab': '03',
  'chandigarh': '04', 'uttarakhand': '05', 'haryana': '06',
  'delhi': '07', 'rajasthan': '08', 'uttar pradesh': '09',
  'bihar': '10', 'sikkim': '11', 'arunachal pradesh': '12',
  'nagaland': '13', 'manipur': '14', 'mizoram': '15',
  'tripura': '16', 'meghalaya': '17', 'assam': '18',
  'west bengal': '19', 'jharkhand': '20', 'odisha': '21',
  'chhattisgarh': '22', 'madhya pradesh': '23', 'gujarat': '24',
  'dadra and nagar haveli and daman and diu': '26', 'maharashtra': '27',
  'andhra pradesh': '37', 'karnataka': '29', 'goa': '30',
  'lakshadweep': '31', 'kerala': '32', 'tamil nadu': '33',
  'puducherry': '34', 'andaman and nicobar islands': '35',
  'telangana': '36', 'ladakh': '38',
};

// Get 2-digit GST maanilam code from maanilam name or GSTIN
export const getStateCode = (stateOrGstin) => {
  if (!stateOrGstin) return '';
  const s = stateOrGstin.trim();
  // If it looks like a GSTIN (15 chars), extract first 2 digits
  if (/^\d{2}[A-Z0-9]{13}$/i.test(s)) return s.substring(0, 2);
  return GST_STATE_CODES[s.toLowerCase()] || '';
};

// Format date as DD-MM-YYYY (GST portal format).
// Guard against malformed input — `new Date("2026-13-45")` is an Invalid Date
// whose getDate() returns NaN, producing "NaN-NaN-NaN" in GSTR-1 export rows.
export const formatDateGST = (dateStr) => {
  if (!dateStr) return '';
  const d = new Date(dateStr);
  if (isNaN(d.getTime())) return '';
  const dd = String(d.getDate()).padStart(2, '0');
  const mm = String(d.getMonth() + 1).padStart(2, '0');
  const yyyy = d.getFullYear();
  return `${dd}-${mm}-${yyyy}`;
};

// Generate E-Way Bill JSON (NIC portal format).
// Throws a friendly error if the seller isn't registered in India — E-Way Bill is an Indian
// GST-portal artifact and the JSON schema (maanilam codes, CGST/SGST/IGST split) is meaningless
// for foreign invoices.
//
// Per the NIC schema, supplyType is the *direction* of the supply (O=Outward, I=Inward),
// NOT inter/intra-maanilam. Seller-issued bills are always 'O'. The intra/inter-maanilam
// distinction is captured by comparing fromStateCode and toStateCode.
export const generateEWayBillJSON = (profile, client, details, items, totals, invoiceType) => {
  if (profile?.country && profile.country !== 'India') {
    throw new Error('E-Way Bill is an Indian GST portal feature. Set business country to "India" in Settings to enable it.');
  }
  const fromStateCode = getStateCode(profile.maanilam || profile.gstin);
  const toStateCode = getStateCode(client.maanilam || client.gstin);
  const isInterstate = fromStateCode && toStateCode && fromStateCode !== toStateCode;

  // Pincodes are mandatory (the portal rejects 0). Try profile.pin / client.pin first,
  // then fall back to extracting digits from mugavari. If still missing, throw — the user
  // must fill the field rather than submit a guaranteed-rejected payload.
  const extractPin = (obj: any) => {
    const direct = String(obj?.pin || obj?.pincode || '').replace(/\D/g, '');
    if (direct.length === 6) return Number(direct);
    const fromAddr = String(obj?.mugavari || '').match(/\b(\d{6})\b/);
    return fromAddr ? Number(fromAddr[1]) : 0;
  };
  const fromPincode = extractPin(profile);
  const toPincode = extractPin(client);
  if (!fromPincode) throw new Error('Your business PIN code is required for the E-Way Bill. Set it in Settings → Company Details.');
  if (!toPincode) throw new Error("Client PIN code is required for the E-Way Bill. Add it in the client's mugavari.");

  const itemList = items.map((item: any, idx: number) => {
    const taxable = ((item.qty || item.quantity) * item.rate) - (item.discount || 0);
    const taxRate = item.taxPercent || 0;
    return {
      itemNo: idx + 1,
      productName: getDynamicField(item, 'name', profile, true) || '',
      productDesc: getDynamicField(item, 'name', profile, true) || '',
      hsnCode: Number(item.hsn) || 0,
      quantity: item.qty || item.quantity || 0,
      qtyUnit: getUnitUQC(item.unit),
      taxableAmount: Math.round(taxable * 100) / 100,
      cgstRate: isInterstate ? 0 : taxRate / 2,
      sgstRate: isInterstate ? 0 : taxRate / 2,
      igstRate: isInterstate ? taxRate : 0,
      cessRate: 0,
    };
  });

  return {
    version: '1.0.1221',
    billLists: [{
      userGstin: profile.gstin || '',
      supplyType: 'O', // Outward — seller-issued. Always O regardless of intra/inter-maanilam.
      subSupplyType: 1, // 1=Supply
      docType: invoiceType === 'delivery-challan' ? 'CHL' : 'INV',
      docNo: details.invoiceNumber || '',
      docDate: formatDateGST(details.invoiceDate),
      fromGstin: profile.gstin || '',
      fromAddr1: (profile.mugavari || '').substring(0, 120),
      fromPlace: profile.oor || profile.maanilam || '',
      fromPincode: fromPincode,
      fromStateCode: Number(fromStateCode) || 0,
      toGstin: client.gstin || 'URP',
      toAddr1: (client.mugavari || '').substring(0, 120),
      toPlace: client.oor || client.maanilam || '',
      toPincode: toPincode,
      toStateCode: Number(toStateCode) || 0,
      totalValue: Math.round((totals.subtotal - totals.totalDiscount) * 100) / 100,
      cgstValue: Math.round(totals.cgst * 100) / 100,
      sgstValue: Math.round(totals.sgst * 100) / 100,
      igstValue: Math.round(totals.igst * 100) / 100,
      cessValue: 0,
      totInvValue: Math.round(totals.total * 100) / 100,
      transMode: 1, // 1=Road
      transDistance: 0,
      transporterName: '',
      transporterId: '',
      transDocNo: '',
      transDocDate: '',
      vehicleNo: '',
      vehicleType: 'R', // R=Regular
      itemList: itemList,
    }]
  };
};

// Upcoming Indian GST / TDS filing due dates relative to `today`. Returns an
// ordered list capped at 60 days out so the notifications centre doesn't show
// 3-month-distant items as "due soon".
//
// Cadence (monthly filer; QRMP / quarterly users will see the same dates but
// only need to act on their schedule):
//   GSTR-1   — 11th of the following month
//   GSTR-3B  — 20th of the following month
//   Form 26Q — 31st of the month following quarter end (Jul / Oct / Jan / Apr)
//   Form 27EQ — 15th of the month following quarter end
export const getUpcomingFilings = (today = new Date()) => {
  const out: any[] = [];
  const t = new Date(today.getFullYear(), today.getMonth(), today.getDate());
  const push = (label: string, dueDate: Date) => {
    const diff = Math.round((dueDate.getTime() - t.getTime()) / 86400000);
    if (diff >= 0 && diff <= 60) out.push({ label, dueDate: dueDate.toISOString().split('T')[0], daysAway: diff });
  };
  // Iterate next 3 months so we catch upcoming month-end deadlines.
  for (let i = 0; i < 3; i++) {
    const nextMonth = new Date(t.getFullYear(), t.getMonth() + i, 1);
    const m = nextMonth.getMonth();
    const y = nextMonth.getFullYear();
    push(`GSTR-1 (${nextMonth.toLocaleString('en-IN', { month: 'short', year: 'numeric' })})`, new Date(y, m + 1, 11));
    push(`GSTR-3B (${nextMonth.toLocaleString('en-IN', { month: 'short', year: 'numeric' })})`, new Date(y, m + 1, 20));
    // Quarterly TDS / TCS — applies to the quarter the month falls into.
    const quarterEnd = m % 3 === 2; // Mar / Jun / Sep / Dec
    if (quarterEnd) {
      push(`Form 26Q (TDS Q ending ${nextMonth.toLocaleString('en-IN', { month: 'short' })})`, new Date(y, m + 2, 0)); // last day of next month after quarter
      push(`Form 27EQ (TCS Q ending ${nextMonth.toLocaleString('en-IN', { month: 'short' })})`, new Date(y, m + 1, 15));
    }
  }
  return out.sort((a, b) => a.daysAway - b.daysAway);
};

// Get filing period as MMYYYY from a date range
export const getFilingPeriod = (dateStr: string) => {
  if (!dateStr) return '';
  const d = new Date(dateStr);
  const mm = String(d.getMonth() + 1).padStart(2, '0');
  return `${mm}${d.getFullYear()}`;
};

// ========== Units of Measurement ==========
// label = display, uqc = GST portal Unit Quantity Code (used in GSTR-1 HSN summary)
// Each unit is tagged with the kind of supply it usually measures so the
// service-mode invoice can prioritise time-based units in the dropdown and
// pick a sensible default ("Hrs" instead of "Nos") for new line items.
// `kind: 'goods' | 'services' | 'both'`. `'both'` shows in either mode.
export const BUILTIN_UNITS = [
  { label: 'Pcs',     uqc: 'PCS', kind: 'goods' },
  { label: 'Nos',     uqc: 'NOS', kind: 'both' },
  { label: 'Kg',      uqc: 'KGS', kind: 'goods' },
  { label: 'g',       uqc: 'GMS', kind: 'goods' },
  { label: 'Tonne',   uqc: 'TON', kind: 'goods' },
  { label: 'Ltr',     uqc: 'LTR', kind: 'goods' },
  { label: 'ml',      uqc: 'MLT', kind: 'goods' },
  { label: 'Mtr',     uqc: 'MTR', kind: 'goods' },
  { label: 'cm',      uqc: 'CMS', kind: 'goods' },
  { label: 'Ft',      uqc: 'FTS', kind: 'goods' },
  { label: 'In',      uqc: 'INS', kind: 'goods' },
  { label: 'Sq.ft',   uqc: 'SQF', kind: 'both'  }, // construction services use this too
  { label: 'Sq.m',    uqc: 'SQM', kind: 'both'  },
  { label: 'Hrs',     uqc: 'HRS', kind: 'services' },
  { label: 'Day',     uqc: 'DAY', kind: 'services' },
  { label: 'Week',    uqc: 'OTH', kind: 'services' },
  { label: 'Month',   uqc: 'OTH', kind: 'services' },
  { label: 'Year',    uqc: 'OTH', kind: 'services' },
  { label: 'Visit',   uqc: 'OTH', kind: 'services' },
  { label: 'Session', uqc: 'OTH', kind: 'services' },
  { label: 'Project', uqc: 'OTH', kind: 'services' },
  { label: 'Word',    uqc: 'OTH', kind: 'services' }, // translators / writers
  { label: 'Page',    uqc: 'OTH', kind: 'services' },
  { label: 'Box',     uqc: 'BOX', kind: 'goods' },
  { label: 'Dozen',   uqc: 'DOZ', kind: 'goods' },
  { label: 'Pair',    uqc: 'PRS', kind: 'goods' },
  { label: 'Set',     uqc: 'SET', kind: 'goods' },
  { label: 'Bag',     uqc: 'BAG', kind: 'goods' },
  { label: 'Roll',    uqc: 'ROL', kind: 'goods' },
  { label: 'Bottle',  uqc: 'BTL', kind: 'goods' },
];

// Default unit per invoice mode — used when adding a new line item so the user
// doesn't have to flip the unit dropdown 90% of the time.
export const getDefaultUnitForMode = (mode: string) => {
  if (mode === 'services') return 'Hrs';
  if (mode === 'mixed') return 'Nos';
  return 'Nos'; // goods (default)
};

// Filter units by invoice mode for the dropdown. Service mode hides
// kg/ltr/box etc. (the user can still pick them via "Add custom…" if
// they truly need a goods unit on a service invoice — rare).
export const filterUnitsByMode = (units: any[], mode: string) => {
  if (mode === 'mixed' || !mode) return units;
  return units.filter(u => u.kind === mode || u.kind === 'both' || u.custom);
};

const CUSTOM_UNITS_KEY = 'gst_customUnits';

export const getCustomUnits = () => {
  try {
    const raw = localStorage.getItem(CUSTOM_UNITS_KEY);
    if (!raw) return [];
    const parsed = JSON.parse(raw);
    return Array.isArray(parsed) ? parsed.filter((u: any) => u && typeof u.label === 'string') : [];
  } catch { return []; }
};

export const addCustomUnit = (label: string) => {
  const trimmed = (label || '').trim();
  if (!trimmed || trimmed.length > 20) return false;
  const existing = getCustomUnits();
  if (existing.some((u: any) => u.label.toLowerCase() === trimmed.toLowerCase())) return false;
  if (BUILTIN_UNITS.some(u => u.label.toLowerCase() === trimmed.toLowerCase())) return false;
  const next = [...existing, { label: trimmed, uqc: 'OTH', custom: true }];
  try { localStorage.setItem(CUSTOM_UNITS_KEY, JSON.stringify(next)); } catch { return false; }
  return true;
};

export const removeCustomUnit = (label: string) => {
  const next = getCustomUnits().filter((u: any) => u.label !== label);
  try { localStorage.setItem(CUSTOM_UNITS_KEY, JSON.stringify(next)); } catch { /* ignore */ }
};

export const getAllUnits = () => [...BUILTIN_UNITS, ...getCustomUnits()];

export const getUnitUQC = (label: string) => {
  const u = getAllUnits().find(x => x.label === label);
  return u?.uqc || 'OTH';
};

// ========== Tax ID validation ==========
// Returns { ok: boolean, message: string }. Empty value is treated as ok (field is optional).
export const validateTaxId = (countryName: string, value: string) => {
  if (!value || !value.trim()) return { ok: true, message: '' };
  const cc = getCountryConfig(countryName);
  if (!cc.taxIdRegex) return { ok: true, message: '' };
  const ok = cc.taxIdRegex.test(value.trim().toUpperCase());
  return ok
    ? { ok: true, message: '' }
    : { ok: false, message: `${cc.taxIdLabel} format looks unusual. Expected like: ${cc.taxIdPlaceholder}` };
};

// ========== Country detection from browser locale ==========
// Maps Intl region code → COUNTRIES.name. Falls back to 'India' on no match.
export const detectCountryFromBrowser = () => {
  try {
    const locale = (navigator?.language || 'en-IN').split('-');
    const region = locale[1]?.toUpperCase() || '';
    if (region === 'GB' || region === 'US') return 'India';
    const match = COUNTRIES.find(c => c.code === region);
    return match?.name || 'India';
  } catch { return 'India'; }
};

// ========== Currency exchange rate snapshot ==========
// Stored on the invoice itself so historical reports stay accurate even if rates change.
// User enters rate manually; we don't fetch from the network (offline-first).
export const formatExchangeRateLine = (currency: string, rate: number, baseCurrency = 'INR') => {
  if (!rate || !currency || currency === baseCurrency) return '';
  return `1 ${currency} = ${Number(rate).toFixed(4)} ${baseCurrency}`;
};

// ========== Payment accounts ==========
// Profiles store an array `paymentAccounts: [{ id, label, vangiPeyar,
// kanakkuEn, ifsc, swift, upiId, isDefault, notes }]`. The legacy flat
// fields (profile.vangiPeyar / kanakkuEn / ifsc / swift / upiId) are kept
// in place for backwards compatibility — `getPaymentAccounts(profile)`
// transparently synthesises a single entry from them when no array exists,
// so old invoices and old profiles keep working without a data migration.

const newAccountId = () => `acc_${Date.now()}_${Math.random().toString(36).slice(2, 8)}`;

export const getPaymentAccounts = (profile: any) => {
  if (!profile) return [];
  if (Array.isArray(profile.paymentAccounts) && profile.paymentAccounts.length > 0) {
    return profile.paymentAccounts;
  }
  // Synthesise a single legacy entry if any flat bank/UPI field is set.
  const hasLegacy = profile.vangiPeyar || profile.kanakkuEn || profile.ifsc || profile.swift || profile.upiId;
  if (!hasLegacy) return [];
  return [{
    id: 'legacy',
    label: profile.vangiPeyar ? `${profile.vangiPeyar}` : 'Default account',
    vangiPeyar: profile.vangiPeyar || '',
    kanakkuEn: profile.kanakkuEn || '',
    ifsc: profile.ifsc || '',
    swift: profile.swift || '',
    upiId: profile.upiId || '',
    isDefault: true,
    notes: '',
  }];
};

export const getDefaultAccount = (profile: any) => {
  const accounts = getPaymentAccounts(profile);
  return accounts.find((a: any) => a.isDefault) || accounts[0] || null;
};

export const getAccountById = (profile: any, id: string) => {
  if (!id) return getDefaultAccount(profile);
  const accounts = getPaymentAccounts(profile);
  return accounts.find((a: any) => a.id === id) || getDefaultAccount(profile);
};

// Create a fresh empty account ready for the user to fill in.
export const createEmptyAccount = (label = 'New account') => ({
  id: newAccountId(),
  label,
  vangiPeyar: '',
  kanakkuEn: '',
  ifsc: '',
  swift: '',
  upiId: '',
  isDefault: false,
  isActive: true,
  notes: '',
});

// Active accounts only — used to populate the new-invoice dropdown so
// archived/disabled accounts don't appear, but they remain editable in
// Settings and still resolve for historical invoices that referenced them.
export const getActiveAccounts = (profile: any) =>
  getPaymentAccounts(profile).filter((a: any) => a.isActive !== false);

// Account number is sensitive — mask all but the last 4 digits for list rows.
// Preserves the visual cue of length without leaking the full number.
export const maskkanakkuEn = (n: any) => {
  const s = String(n || '').trim();
  if (s.length <= 4) return s; // too short to mask
  return '••••' + s.slice(-4);
};

// Returns a NEW accounts array with the entry at fromIdx moved to toIdx.
// Pure — caller writes the result back to profile.paymentAccounts.
export const reorderAccounts = (accounts: any[], fromIdx: number, toIdx: number) => {
  if (!Array.isArray(accounts)) return accounts;
  if (fromIdx === toIdx || fromIdx < 0 || fromIdx >= accounts.length) return accounts;
  if (toIdx < 0 || toIdx >= accounts.length) return accounts;
  const total = accounts.reduce((acc, curr: any) => acc + (parseFloat(curr.balance) || 0), 0);
  const next = accounts.slice();
  const [moved] = next.splice(fromIdx, 1);
  next.splice(toIdx, 0, moved);
  return next;
};

// Returns a NEW accounts array with exactly one default — the one matching
// `accountId`. Used by the Settings UI's ⭐ buttons. If no match, returns
// the input unchanged.
export const setDefaultAccount = (accounts, accountId) => {
  if (!Array.isArray(accounts) || !accountId) return accounts;
  if (!accounts.some(a => a.id === accountId)) return accounts;
  return accounts.map(a => ({ ...a, isDefault: a.id === accountId }));
};

// Soft UPI VPA format check (warning-only). Allows alphanumerics, dot, hyphen,
// underscore on either side of the @ — covers Indian VPAs like
// "9999999999@upi", "acme.corp@hdfcbank", "merchant-1@paytm".
export const isValidUpiId = (s) => /^[\w.\-]+@[\w.\-]+$/.test(String(s || '').trim());



// ========== Built-in Terms & Conditions presets ==========
// Drop-in starter T&C wording grouped by India-common business types. Users pick a
// preset, edit it however they want, and optionally save the result as one of their
// own reusable templates via the existing Terms Templates feature in Settings.
//
// Each entry is rich HTML so the in-invoice rich editor renders bullets and bold
// correctly. Every preset includes an India-relevant jurisdiction line and an
// "all disputes subject to <oor> jurisdiction" clause that the user can edit.
export const TERMS_PRESETS = [
  {
    id: 'generic-sme',
    label: 'Generic SME / Trader',
    region: 'IN',
    body: `<p><strong>Payment Terms</strong></p>
<ul>
  <li>Payment is due within <strong>15 days</strong> from the date of invoice.</li>
  <li>Goods once sold will not be taken back or exchanged.</li>
  <li>Interest @ <strong>18% p.a.</strong> will be charged on overdue payments.</li>
  <li>All cheques to be drawn in favour of the company name printed above.</li>
</ul>
<p><strong>Delivery & Title</strong></p>
<ul>
  <li>Goods remain the property of the seller until full payment is received.</li>
  <li>Risk passes to the buyer on dispatch from our premises.</li>
</ul>
<p><strong>Disputes:</strong> Subject to <em>[your oor]</em> jurisdiction only.</p>`,
  },
  {
    id: 'freelancer',
    label: 'Freelancer / Consultant',
    region: 'IN',
    body: `<p><strong>Scope</strong></p>
<ul>
  <li>This invoice is for professional services as agreed in our scope of work.</li>
  <li>Any work outside the agreed scope will be quoted separately.</li>
</ul>
<p><strong>Payment</strong></p>
<ul>
  <li>Payment is due within <strong>7 days</strong> from invoice date.</li>
  <li>Late payments accrue interest at <strong>1.5% per month</strong>.</li>
  <li>TDS, if applicable, may be deducted under Section 194J. Please share Form 16A.</li>
</ul>
<p><strong>Intellectual Property:</strong> Final deliverables transfer to client only after the full invoice value is settled.</p>
<p>Disputes subject to <em>[your oor]</em> jurisdiction.</p>`,
  },
  {
    id: 'manufacturer',
    label: 'Manufacturer / Wholesale',
    region: 'IN',
    body: `<p><strong>Order & Delivery</strong></p>
<ul>
  <li>Goods are dispatched ex-works unless otherwise agreed in writing.</li>
  <li>Delivery dates are estimates; we are not liable for delays caused by transporters or force majeure events.</li>
  <li>Buyer is responsible for inspection of goods at the time of delivery.</li>
</ul>
<p><strong>Payment</strong></p>
<ul>
  <li>Net <strong>30 days</strong> from date of invoice.</li>
  <li>Interest @ <strong>24% p.a.</strong> on overdue amounts.</li>
  <li>TDS under Section 194Q applicable for buyers with turnover &gt; ₹10 cr.</li>
</ul>
<p><strong>Returns:</strong> Goods sold are not returnable unless defective and notified within 7 days of delivery.</p>
<p>Subject to <em>[your oor]</em> jurisdiction.</p>`,
  },
  {
    id: 'retail-shop',
    label: 'Retail Shop',
    region: 'IN',
    body: `<ul>
  <li><strong>No exchange or refund</strong> on goods once sold, except in case of manufacturing defects within 7 days, with original bill.</li>
  <li>Discounted items are not eligible for return or exchange.</li>
  <li>Goods may be exchanged of equal value within 7 days, subject to availability.</li>
  <li>Cheques to be drawn in favour of <em>[shop name]</em>. Interest @ 24% p.a. on dishonoured cheques.</li>
  <li>All disputes subject to <em>[your oor]</em> jurisdiction only.</li>
</ul>`,
  },
  {
    id: 'restaurant',
    label: 'Restaurant / Café',
    region: 'IN',
    body: `<ul>
  <li>Service charge, where applicable, is at the discretion of the customer.</li>
  <li>GST as applicable is included as per current government rates.</li>
  <li>Cheques are not accepted. Card and UPI welcomed.</li>
  <li>We reserve the right of admission.</li>
  <li>Disputes subject to <em>[your oor]</em> jurisdiction.</li>
</ul>`,
  },
  {
    id: 'it-saas',
    label: 'IT / Software Services',
    region: 'IN',
    body: `<p><strong>Service Terms</strong></p>
<ul>
  <li>Services are billed on a project / monthly retainer basis as agreed.</li>
  <li>Software licenses, third-party services, and infrastructure costs are billed at actuals.</li>
</ul>
<p><strong>Payment</strong></p>
<ul>
  <li>Net <strong>15 days</strong> from invoice date.</li>
  <li>Late payments accrue interest at <strong>18% p.a.</strong></li>
  <li>TDS under Section 194J applicable.</li>
</ul>
<p><strong>SLA & Support:</strong> Support is provided per the agreed SLA. Outages caused by hosting providers, third-party APIs, or scheduled maintenance are excluded.</p>
<p><strong>IP & Confidentiality:</strong> Custom code is licensed to client on full settlement. Confidential information is protected per the signed NDA.</p>
<p>Subject to <em>[your oor]</em> jurisdiction.</p>`,
  },
  {
    id: 'construction',
    label: 'Construction / Contractor',
    region: 'IN',
    body: `<p><strong>Work & Materials</strong></p>
<ul>
  <li>Work is executed per the approved drawings and BOQ.</li>
  <li>Any change orders or additional work will be billed separately at agreed rates.</li>
  <li>Materials remain the property of the contractor until full payment is received.</li>
</ul>
<p><strong>Payment Schedule</strong></p>
<ul>
  <li>Payment as per agreed milestones in the contract.</li>
  <li>Final payment due within <strong>30 days</strong> of completion certificate.</li>
  <li>TDS under Section 194C applicable.</li>
  <li>Retention, if any, will be released as per the contract terms.</li>
</ul>
<p><strong>Defects Liability:</strong> 12 months from handover, covering workmanship only.</p>
<p>Subject to <em>[your oor]</em> jurisdiction.</p>`,
  },
  {
    id: 'medical',
    label: 'Medical / Healthcare',
    region: 'IN',
    body: `<ul>
  <li>Medicines and consumables once sold cannot be exchanged or returned (Drugs and Cosmetics Rules).</li>
  <li>Services rendered are non-refundable.</li>
  <li>Payment is due at the time of service unless covered by a pre-authorized insurance claim.</li>
  <li>Insurance reimbursement is between patient and insurer; we provide all documentation needed.</li>
  <li>Disputes subject to <em>[your oor]</em> jurisdiction.</li>
</ul>`,
  },
  {
    id: 'education',
    label: 'Educational Services / Coaching',
    region: 'IN',
    body: `<ul>
  <li>Fees are non-refundable once classes commence, except as per the published refund policy.</li>
  <li>Course material remains the property of the institute and may not be reproduced without permission.</li>
  <li>Late fee of ₹500 per month after the due date.</li>
  <li>The institute reserves the right to reschedule or cancel classes with prior notice.</li>
  <li>Disputes subject to <em>[your oor]</em> jurisdiction.</li>
</ul>`,
  },
  {
    id: 'transport',
    label: 'Transport / Logistics',
    region: 'IN',
    body: `<ul>
  <li>Goods are carried at <strong>owner's risk</strong> unless transit insurance is separately arranged and paid for.</li>
  <li>Delivery times are best-effort estimates and not guaranteed.</li>
  <li>Liability for loss or damage is limited to ₹100 per consignment unless declared value is paid.</li>
  <li>Demurrage / detention charges as per the schedule attached.</li>
  <li>Payment due within <strong>15 days</strong>; interest @ 24% p.a. on overdue amounts.</li>
  <li>Subject to <em>[your oor]</em> jurisdiction.</li>
</ul>`,
  },
  {
    id: 'real-estate-rent',
    label: 'Real Estate / Rental Invoice',
    region: 'IN',
    body: `<ul>
  <li>Rent is payable on or before the <strong>5th of every month</strong>.</li>
  <li>Late payments attract a penalty of ₹100 per day after grace period.</li>
  <li>TDS under Section 194I applicable for tenant if annual rent exceeds ₹2.4 lakh.</li>
  <li>Maintenance, electricity, and water charges are billed separately as per usage.</li>
  <li>Premises must be vacated in the same condition as handed over, normal wear and tear excepted.</li>
  <li>Disputes subject to <em>[your oor]</em> jurisdiction.</li>
</ul>`,
  },
  {
    id: 'ecommerce',
    label: 'E-commerce Seller',
    region: 'IN',
    body: `<ul>
  <li>Returns accepted within 7 days of delivery, in original packaging, subject to product category policy.</li>
  <li>Refunds are processed to the original payment mode within 7-10 working days of return receipt.</li>
  <li>Products with broken seals, used items, and clearance-sale items are not returnable.</li>
  <li>Shipping charges, where applicable, are non-refundable.</li>
  <li>For warranty, please contact the manufacturer's authorized service center.</li>
  <li>Disputes subject to <em>[your oor]</em> jurisdiction. Governed by the Consumer Protection Act, 2019.</li>
</ul>`,
  },
  {
    id: 'export',
    label: 'Export / International (LUT)',
    region: 'IN',
    body: `<p><strong>Tax Status:</strong> Supplied as a zero-rated export under <strong>LUT (Letter of Undertaking)</strong> — IGST not charged. Bond / LUT reference: <em>[insert LUT number]</em>.</p>
<p><strong>Payment</strong></p>
<ul>
  <li>Payable in <em>[USD/EUR/etc.]</em> by SWIFT wire transfer to the bank account printed above.</li>
  <li>All bank charges (sender + intermediary + beneficiary) are to be borne by the buyer.</li>
  <li>Payment due within <strong>30 days</strong> of invoice date.</li>
</ul>
<p><strong>Delivery:</strong> FOB / CIF as per Incoterms 2020 — see the contract for the agreed term.</p>
<p>Disputes subject to <em>[your oor]</em>, India jurisdiction.</p>`,
  },
  {
    id: 'custom-blank',
    label: '— Start from blank —',
    region: '*',
    body: '',
  },
];

export const getTermsPresets = (region) => {
  if (!region || region === '*') return TERMS_PRESETS;
  return TERMS_PRESETS.filter(p => p.region === region || p.region === '*');
};

// ========== TDS / TCS (Income Tax Act) ==========
// Common TDS sections that appear on invoices. The buyer deducts this from the
// payment made to us (the seller); we surface it as an informational line so
// the client knows what to deduct, and so our records can track receivable TDS.
// Rates here are the default — users can override per-invoice.
export const TDS_SECTIONS = [
  { code: '194Q', label: '194Q — Purchase of goods (buyer turnover > ₹10cr)', rate: 0.1 },
  { code: '194C', label: '194C — Contractor / sub-contractor', rate: 1 },
  { code: '194C-co', label: '194C — Contractor (company)', rate: 2 },
  { code: '194J', label: '194J — Professional / technical services', rate: 10 },
  { code: '194J-tech', label: '194J — Technical services (lower rate)', rate: 2 },
  { code: '194I', label: '194I — Rent (land / building)', rate: 10 },
  { code: '194I-pm', label: '194I — Rent (plant / machinery)', rate: 2 },
  { code: '194H', label: '194H — Commission / brokerage', rate: 5 },
  { code: '194O', label: '194O — E-commerce participant', rate: 1 },
  { code: '195',  label: '195 — Payments to non-residents (varies)', rate: 0 },
  { code: 'custom', label: 'Custom section / rate', rate: 0 },
];

// TCS (Section 206C) is collected BY the seller from the buyer and added to the
// invoice total. Common cases:
export const TCS_SECTIONS = [
  { code: '206C(1H)', label: '206C(1H) — Sale of goods (seller turnover > ₹10cr)', rate: 0.1 },
  { code: '52',       label: 'CGST 52 — E-commerce operator', rate: 1 },
  { code: '206C(1)',  label: '206C(1) — Tendu leaves / scrap / minerals (varies)', rate: 1 },
  { code: 'custom',   label: 'Custom rate', rate: 0 },
];

export const getTDSSection = (code) => TDS_SECTIONS.find(s => s.code === code) || TDS_SECTIONS[0];
export const getTCSSection = (code) => TCS_SECTIONS.find(s => s.code === code) || TCS_SECTIONS[0];

// ========== Round-off helper ==========
// Returns the delta needed to round the total to the nearest whole unit.
// e.g. 1234.67 → -0.67 (subtract); 1234.40 → +0.60 (add).
export const calculateRoundOff = (total) => {
  if (typeof total !== 'number' || isNaN(total)) return 0;
  const rounded = Math.round(total);
  return Math.round((rounded - total) * 100) / 100;
};


// ========== Currency name map (for amount-in-words) ==========
// Used by InvoicePreview when rendering "Amount in Words" footer for foreign currencies.
export const CURRENCY_NAMES = {
  INR: { major: 'Rupees',   minor: 'Paise' },
  USD: { major: 'Dollars',  minor: 'Cents' },
  EUR: { major: 'Euros',    minor: 'Cents' },
  GBP: { major: 'Pounds',   minor: 'Pence' },
  AUD: { major: 'Dollars',  minor: 'Cents' },
  CAD: { major: 'Dollars',  minor: 'Cents' },
  SGD: { major: 'Dollars',  minor: 'Cents' },
  AED: { major: 'Dirhams',  minor: 'Fils'  },
  SAR: { major: 'Riyals',   minor: 'Halalas' },
  MYR: { major: 'Ringgit',  minor: 'Sen'   },
  ZAR: { major: 'Rand',     minor: 'Cents' },
  NGN: { major: 'Naira',    minor: 'Kobo'  },
  KES: { major: 'Shillings',minor: 'Cents' },
  NPR: { major: 'Rupees',   minor: 'Paisa' },
  BDT: { major: 'Taka',     minor: 'Poisha'},
  LKR: { major: 'Rupees',   minor: 'Cents' },
  PKR: { major: 'Rupees',   minor: 'Paisa' },
  PHP: { major: 'Pesos',    minor: 'Centavos' },
  IDR: { major: 'Rupiah',   minor: 'Sen'   },
  NZD: { major: 'Dollars',  minor: 'Cents' },
};

const STATE_TRANSLATIONS: Record<string, Record<string, string>> = {
  'Tamil': {
    'Andhra Pradesh': 'ஆந்திரப் பிரதேசம்', 'Arunachal Pradesh': 'அருணாச்சலப் பிரதேசம்', 'Assam': 'அஸ்ஸாம்', 'Bihar': 'பீகார்',
    'Chhattisgarh': 'சத்தீஸ்கர்', 'Goa': 'கோவா', 'Gujarat': 'குஜராத்', 'Haryana': 'ஹரியானா', 'Himachal Pradesh': 'இமாச்சலப் பிரதேசம்',
    'Jharkhand': 'ஜார்க்கண்ட்', 'Karnataka': 'கர்நாடகா', 'Kerala': 'கேரளா', 'Madhya Pradesh': 'மத்தியப் பிரதேசம்', 'Maharashtra': 'மகாராஷ்டிரா',
    'Manipur': 'மணிப்பூர்', 'Meghalaya': 'மேகாலயா', 'Mizoram': 'மிசோரம்', 'Nagaland': 'நாகாலாந்து', 'Odisha': 'ஒடிசா', 'Punjab': 'பஞ்சாப்',
    'Rajasthan': 'ராஜஸ்தான்', 'Sikkim': 'சிக்கிம்', 'Tamil Nadu': 'தமிழ்நாடு', 'Telangana': 'தெலுங்கானா', 'Tripura': 'திரிபுரா',
    'Uttar Pradesh': 'உத்தரப் பிரதேசம்', 'Uttarakhand': 'உத்தராகண்ட்', 'West Bengal': 'மேற்கு வங்கம்',
    'Andaman and Nicobar Islands': 'அந்தமான் நிக்கோபார் தீவுகள்', 'Chandigarh': 'சண்டிகர்',
    'Dadra and Nagar Haveli and Daman and Diu': 'தாத்ரா நகர் ஹவேலி மற்றும் டாமன் டையூ', 'Delhi': 'டெல்லி',
    'Jammu and Kashmir': 'ஜம்மு காஷ்மீர்', 'Ladakh': 'லடாக்', 'Lakshadweep': 'லட்சத்தீவு', 'Puducherry': 'புதுச்சேரி'
  },
  'Hindi': {
    'Andhra Pradesh': 'आंध्र प्रदेश', 'Arunachal Pradesh': 'अरुणाचल प्रदेश', 'Assam': 'असम', 'Bihar': 'बिहार',
    'Chhattisgarh': 'छत्तीसगढ़', 'Goa': 'गोवा', 'Gujarat': 'गुजरात', 'Haryana': 'हरियाणा', 'Himachal Pradesh': 'हिमाचल प्रदेश',
    'Jharkhand': 'झारखण्ड', 'Karnataka': 'कर्नाटक', 'Kerala': 'केरल', 'Madhya Pradesh': 'मध्य प्रदेश', 'Maharashtra': 'महाराष्ट्र',
    'Manipur': 'मणिपुर', 'Meghalaya': 'मेघालय', 'Mizoram': 'मिज़ोरम', 'Nagaland': 'नागालैंड', 'Odisha': 'ओडिशा', 'Punjab': 'पंजाब',
    'Rajasthan': 'राजस्थान', 'Sikkim': 'सिक्किम', 'Tamil Nadu': 'तमिलनाडु', 'Telangana': 'तेलंगाना', 'Tripura': 'त्रिपुरा',
    'Uttar Pradesh': 'उत्तर प्रदेश', 'Uttarakhand': 'उत्तराखण्ड', 'West Bengal': 'पश्चिम बंगाल',
    'Andaman and Nicobar Islands': 'अंडमान और निकोबार द्वीपसमूह', 'Chandigarh': 'चंडीगढ़',
    'Dadra and Nagar Haveli and Daman and Diu': 'दादरा और नगर हवेली और दमन और दीव', 'Delhi': 'दिल्ली',
    'Jammu and Kashmir': 'जम्मू और कश्मीर', 'Ladakh': 'लद्दाख', 'Lakshadweep': 'लक्षद्वीप', 'Puducherry': 'पुदुच्चेरी'
  },
  'Telugu': {
    'Andhra Pradesh': 'ఆంధ్ర ప్రదేశ్', 'Arunachal Pradesh': 'అరుణాచల్ ప్రదేశ్', 'Assam': 'అస్సాం', 'Bihar': 'బీహార్',
    'Chhattisgarh': 'ఛత్తీస్గఢ్', 'Goa': 'గోవా', 'Gujarat': 'గుజరాత్', 'Haryana': 'హర్యానా', 'Himachal Pradesh': 'హిమాచల్ ప్రదేశ్',
    'Jharkhand': 'జార్ఖండ్', 'Karnataka': 'కర్ణాటక', 'Kerala': 'కేరళ', 'Madhya Pradesh': 'మధ్య ప్రదేశ్', 'Maharashtra': 'మహారాష్ట్ర',
    'Manipur': 'మణిపూర్', 'Meghalaya': 'మేఘాలయ', 'Mizoram': 'మిజోరం', 'Nagaland': 'నాగాలాండ్', 'Odisha': 'ఒడిశా', 'Punjab': 'పంజాబ్',
    'Rajasthan': 'రాజస్థాన్', 'Sikkim': 'సిక్కిం', 'Tamil Nadu': 'తమిళనాడు', 'Telangana': 'తెలంగాణ', 'Tripura': 'త్రిపుర',
    'Uttar Pradesh': 'ఉత్తర ప్రదేశ్', 'Uttarakhand': 'ఉత్తరాఖండ్', 'West Bengal': 'పశ్చిమ బెంగాల్',
    'Andaman and Nicobar Islands': 'అండమాన్ మరియు నికోబార్ దీవులు', 'Chandigarh': 'చండీగఢ్',
    'Dadra and Nagar Haveli and Daman and Diu': 'దాద్రా మరియు నగర్ హవేలీ మరియు డామన్ మరియు డయ్యూ', 'Delhi': 'ఢిల్లీ',
    'Jammu and Kashmir': 'జమ్మూ మరియు కాశ్మీర్', 'Ladakh': 'లడఖ్', 'Lakshadweep': 'లక్షద్వీప్', 'Puducherry': 'పుదుచ్చేరి'
  },
  'Kannada': {
    'Andhra Pradesh': 'ಆಂಧ್ರ ಪ್ರದೇಶ', 'Arunachal Pradesh': 'ಅರುಣಾಚಲ ಪ್ರದೇಶ', 'Assam': 'ಅಸ್ಸಾಂ', 'Bihar': 'ಬಿಹಾರ',
    'Chhattisgarh': 'ಛತ್ತೀಸ್ಗಢ', 'Goa': 'ಗೋವಾ', 'Gujarat': 'ಗುಜರಾತ್', 'Haryana': 'ಹರಿಯಾಣ', 'Himachal Pradesh': 'ಹಿಮಾಚಲ ಪ್ರದೇಶ',
    'Jharkhand': 'ಜಾರ್ಖಂಡ್', 'Karnataka': 'ಕರ್ನಾಟಕ', 'Kerala': 'ಕೇರಳ', 'Madhya Pradesh': 'ಮಧ್ಯ ಪ್ರದೇಶ', 'Maharashtra': 'ಮಹಾರಾಷ್ಟ್ರ',
    'Manipur': 'ಮಣಿಪುರ', 'Meghalaya': 'ಮೇಘಾಲಯ', 'Mizoram': 'ಮಿಜೋರಂ', 'Nagaland': 'ನಾಗಾಲ್ಯಾಂಡ್', 'Odisha': 'ಒಡಿಶಾ', 'Punjab': 'ಪಂಜಾಬ್',
    'Rajasthan': 'ರಾಜಸ್ಥಾನ', 'Sikkim': 'ಸಿಕ್ಕಿಂ', 'Tamil Nadu': 'ತಮಿಳುನಾಡು', 'Telangana': 'ತೆಲಂಗಾಣ', 'Tripura': 'ತ್ರಿಪುರ',
    'Uttar Pradesh': 'ಉತ್ತರ ಪ್ರದೇಶ', 'Uttarakhand': 'ಉತ್ತರಾಖಂಡ', 'West Bengal': 'ಪಶ್ಚಿಮ ಬಂಗಾಳ',
    'Andaman and Nicobar Islands': 'ಅಂಡಮಾನ್ ಮತ್ತು ನಿಕೋಬಾರ್ ದ್ವೀಪಗಳು', 'Chandigarh': 'ಚಂಡೀಗಡ',
    'Dadra and Nagar Haveli and Daman and Diu': 'ದಾದ್ರಾ ಮತ್ತು ನಗರ್ ಹವೇಲಿ ಮತ್ತು ಡಾಮನ್ ಮತ್ತು ಡಿಯು', 'Delhi': 'ದೆಹಲಿ',
    'Jammu and Kashmir': 'ಜಮ್ಮು ಮತ್ತು ಕಾಶ್ಮೀರ', 'Ladakh': 'ಲಡಾಖ್', 'Lakshadweep': 'ಲಕ್ಷದ್ವೀಪ', 'Puducherry': 'ಪುದುಚ್ಚೇರಿ'
  },
  'Malayalam': {
    'Andhra Pradesh': 'ആന്ധ്രാ പ്രദേശ്', 'Arunachal Pradesh': 'അരുണാചൽ പ്രദേശ്', 'Assam': 'അസം', 'Bihar': 'ബിഹാർ',
    'Chhattisgarh': 'ഛത്തീസ്ഗഢ്', 'Goa': 'ഗോവ', 'Gujarat': 'ഗുജറാത്ത്', 'Haryana': 'ഹരിയാന', 'Himachal Pradesh': 'ഹിമാചൽ പ്രദേശ്',
    'Jharkhand': 'ജാർഖണ്ഡ്', 'Karnataka': 'കർണാടക', 'Kerala': 'കേരളം', 'Madhya Pradesh': 'മധ്യ പ്രദേശ്', 'Maharashtra': 'മഹാരാഷ്ട്ര',
    'Manipur': 'മണിപ്പൂർ', 'Meghalaya': 'മേഘാലയ', 'Mizoram': 'മിസോറം', 'Nagaland': 'നാഗാലാൻഡ്', 'Odisha': 'ഒഡിഷ', 'Punjab': 'പഞ്ചാബ്',
    'Rajasthan': 'രാജസ്ഥാൻ', 'Sikkim': 'സിക്കിം', 'Tamil Nadu': 'തമിഴ്നാട്', 'Telangana': 'തെലങ്കാന', 'Tripura': 'ത്രിപുര',
    'Uttar Pradesh': 'ഉത്തർ പ്രദേശ്', 'Uttarakhand': 'ഉത്തരാഖണ്ഡ്', 'West Bengal': 'പശ്ചിമ ബംഗാൾ',
    'Andaman and Nicobar Islands': 'ആൻഡമാൻ നിക്കോബാർ ദ്വീപുകൾ', 'Chandigarh': 'ചണ്ഡീഗഢ്',
    'Dadra and Nagar Haveli and Daman and Diu': 'ദാദ്ര നഗർ ഹവേലി ദമൻ ദിയു', 'Delhi': 'ഡൽഹി',
    'Jammu and Kashmir': 'ജമ്മു കാശ്മീർ', 'Ladakh': 'ലഡാക്ക്', 'Lakshadweep': 'ലക്ഷദ്വീപ്', 'Puducherry': 'പുതുച്ചേരി'
  },
  'Marathi': {
    'Andhra Pradesh': 'आंध्र प्रदेश', 'Arunachal Pradesh': 'अरुणाचल प्रदेश', 'Assam': 'आसाम', 'Bihar': 'बिहार',
    'Chhattisgarh': 'छत्तीसगड', 'Goa': 'गोवा', 'Gujarat': 'गुजरात', 'Haryana': 'हरियाणा', 'Himachal Pradesh': 'हिमाचल प्रदेश',
    'Jharkhand': 'झारखंड', 'Karnataka': 'कर्नाटक', 'Kerala': 'केरळ', 'Madhya Pradesh': 'मध्य प्रदेश', 'Maharashtra': 'महाराष्ट्र',
    'Manipur': 'मणिपूर', 'Meghalaya': 'मेघालय', 'Mizoram': 'मिझोराम', 'Nagaland': 'नागालँड', 'Odisha': 'ओडिशा', 'Punjab': 'पंजाब',
    'Rajasthan': 'राजस्थान', 'Sikkim': 'सिक्कीम', 'Tamil Nadu': 'तमिळनाडू', 'Telangana': 'तेलंगणा', 'Tripura': 'त्रिपुरा',
    'Uttar Pradesh': 'उत्तर प्रदेश', 'Uttarakhand': 'उत्तराखंड', 'West Bengal': 'पश्चिम बंगाल',
    'Andaman and Nicobar Islands': 'अंदमान आणि निकोबार बेटे', 'Chandigarh': 'चंदीगड',
    'Dadra and Nagar Haveli and Daman and Diu': 'दादरा आणि नगर हवेली आणि दमण आणि दीव', 'Delhi': 'दिल्ली',
    'Jammu and Kashmir': 'जम्मू आणि काश्मीर', 'Ladakh': 'लडाख', 'Lakshadweep': 'लक्षद्वीप', 'Puducherry': 'पुद्दुचेरी'
  },
  'Gujarati': {
    'Andhra Pradesh': 'આંધ્ર પ્રદેશ', 'Arunachal Pradesh': 'અરુણાચલ પ્રદેશ', 'Assam': 'આસામ', 'Bihar': 'બિહાર',
    'Chhattisgarh': 'છત્તીસગઢ', 'Goa': 'ગોવા', 'Gujarat': 'ગુજરાત', 'Haryana': 'હરિયાણા', 'Himachal Pradesh': 'હિમાચલ પ્રદેશ',
    'Jharkhand': 'ઝારખંડ', 'Karnataka': 'કર્ણાટક', 'Kerala': 'કેરળ', 'Madhya Pradesh': 'મધ્ય પ્રદેશ', 'Maharashtra': 'મહારાષ્ટ્ર',
    'Manipur': 'મણિપુર', 'Meghalaya': 'મેઘાલય', 'Mizoram': 'મિઝોરમ', 'Nagaland': 'નાગાલેન્ડ', 'Odisha': 'ઓડિશા', 'Punjab': 'પંજાબ',
    'Rajasthan': 'રાજસ્થાન', 'Sikkim': 'સિક્કિમ', 'Tamil Nadu': 'તમિલનાડુ', 'Telangana': 'તેલંગાણા', 'Tripura': 'ત્રિપુરા',
    'Uttar Pradesh': 'ઉત્તર પ્રદેશ', 'Uttarakhand': 'ઉત્તરાખંડ', 'West Bengal': 'પશ્ચિમ બંગાળ',
    'Andaman and Nicobar Islands': 'આંદામાન અને નિકોબાર ટાપુઓ', 'Chandigarh': 'ચંદીગઢ',
    'Dadra and Nagar Haveli and Daman and Diu': 'દાદરા અને નગર હવેલી અને દમણ અને દીવ', 'Delhi': 'દિલ્હી',
    'Jammu and Kashmir': 'જમ્મુ અને કાશ્મીર', 'Ladakh': 'લદાખ', 'Lakshadweep': 'લક્ષદ્વીપ', 'Puducherry': 'પુડુચેરી'
  },
  'Bengali': {
    'Andhra Pradesh': 'অন্ধ্র প্রদেশ', 'Arunachal Pradesh': 'অরুণাচল প্রদেশ', 'Assam': 'অসম', 'Bihar': 'বিহার',
    'Chhattisgarh': 'ছত্তিশগড়', 'Goa': 'গোয়া', 'Gujarat': 'গুজরাট', 'Haryana': 'হরিয়ানা', 'Himachal Pradesh': 'হিমাচল প্রদেশ',
    'Jharkhand': 'ঝাড়খণ্ড', 'Karnataka': 'কর্ণাটক', 'Kerala': 'কেরল', 'Madhya Pradesh': 'মধ্য প্রদেশ', 'Maharashtra': 'মহারাষ্ট্র',
    'Manipur': 'মণিপুর', 'Meghalaya': 'মেঘালয়', 'Mizoram': 'মিজোরাম', 'Nagaland': 'নাগাল্যান্ড', 'Odisha': 'ওড়িশা', 'Punjab': 'পাঞ্জাব',
    'Rajasthan': 'রাজস্থান', 'Sikkim': 'সিকিম', 'Tamil Nadu': 'তামিলনাড়ু', 'Telangana': 'তেলেঙ্গানা', 'Tripura': 'ত্রিপুরা',
    'Uttar Pradesh': 'উত্তর প্রদেশ', 'Uttarakhand': 'উত্তরাখণ্ড', 'West Bengal': 'পশ্চিমবঙ্গ',
    'Andaman and Nicobar Islands': 'আন্দামান ও নিকোবর দ্বীপপুঞ্জ', 'Chandigarh': 'চণ্ডীগড়',
    'Dadra and Nagar Haveli and Daman and Diu': 'দাদরা ও নগর হাভেলি এবং দমন ও দিউ', 'Delhi': 'দিল্লি',
    'Jammu and Kashmir': 'জম্মু ও কাশ্মীর', 'Ladakh': 'লাদাখ', 'Lakshadweep': 'লাক্ষাদ্বীপ', 'Puducherry': 'পুদুচ্চেরি'
  },
};

const COUNTRY_TRANSLATIONS: Record<string, Record<string, string>> = {
  'Tamil': {
    'India': 'இந்தியா', 'United Arab Emirates': 'ஐக்கிய அரபு அமீரகம்', 'United States': 'அமெரிக்கா', 'United Kingdom': 'இங்கிலாந்து',
    'Australia': 'ஆஸ்திரேலியா', 'Canada': 'கனடா', 'Singapore': 'சிங்கப்பூர்', 'Malaysia': 'மலேசியா', 'Germany': 'ஜெர்மனி',
    'France': 'பிரான்ஸ்', 'Netherlands': 'நெதர்லாந்து', 'South Africa': 'தென்னாப்பிரிக்கா', 'Nigeria': 'நைஜீரியா', 'Kenya': 'கென்யா',
    'Saudi Arabia': 'சவூதி அரேபியா', 'Nepal': 'நேபாளம்', 'Bangladesh': 'வங்காளதேசம்', 'Sri Lanka': 'இலங்கை', 'Pakistan': 'பாகிஸ்தான்',
    'Philippines': 'பிலிப்பைன்ஸ்', 'Indonesia': 'இந்தோனேசியா', 'New Zealand': 'நியூசிலாந்து', 'Other': 'மற்றவை (Other)'
  },
  'Hindi': {
    'India': 'भारत', 'United Arab Emirates': 'संयुक्त अरब अमीरात', 'United States': 'संयुक्त राज्य अमेरिका', 'United Kingdom': 'यूनाइटेड किंगडम',
    'Australia': 'ऑस्ट्रेलिया', 'Canada': 'कनाडा', 'Singapore': 'सिंगापुर', 'Malaysia': 'मलेशिया', 'Germany': 'जर्मनी',
    'France': 'फ्रांस', 'Netherlands': 'नीदरलैंड', 'South Africa': 'दक्षिण अफ्रीका', 'Nigeria': 'नाइजीरिया', 'Kenya': 'केन्या',
    'Saudi Arabia': 'सऊदी अरब', 'Nepal': 'नेपाल', 'Bangladesh': 'बांग्लादेश', 'Sri Lanka': 'श्रीलंका', 'Pakistan': 'पाकिस्तान',
    'Philippines': 'फिलीपींस', 'Indonesia': 'इंडोनेशिया', 'New Zealand': 'न्यूज़ीलैंड', 'Other': 'अन्य (Other)'
  },
  'Telugu': {
    'India': 'భారతదేశం', 'United Arab Emirates': 'యునైటెడ్ అరబ్ ఎమిరేట్స్', 'United States': 'అమెరికా సంయుక్త రాష్ట్రాలు', 'United Kingdom': 'యునైటెడ్ కింగ్డమ్',
    'Australia': 'ఆస్ట్రేలియా', 'Canada': 'కెనడా', 'Singapore': 'సింగపూర్', 'Malaysia': 'మలేషియా', 'Germany': 'జర్మనీ',
    'France': 'ఫ్రాన్స్', 'Netherlands': 'నెదర్లాండ్స్', 'South Africa': 'దక్షిణ ఆఫ్రికా', 'Nigeria': 'నైజీరియా', 'Kenya': 'కెన్యా',
    'Saudi Arabia': 'సౌదీ అరేబియా', 'Nepal': 'నేపాల్', 'Bangladesh': 'బంగ్లాదేశ్', 'Sri Lanka': 'శ్రీలంక', 'Pakistan': 'పాకిస్తాన్',
    'Philippines': 'ఫిలిప్పీన్స్', 'Indonesia': 'ఇండోనేషియా', 'New Zealand': 'న్యూజిలాండ్', 'Other': 'ఇతర (Other)'
  },
  'Kannada': {
    'India': 'ಭಾರತ', 'United Arab Emirates': 'ಯುನೈಟೆಡ್ ಅರಬ್ ಎಮಿರೇಟ್ಸ್', 'United States': 'ಅಮೆರಿಕ ಸಂಯುಕ್ತ ಸಂಸ್ಥಾನ', 'United Kingdom': 'ಯುನೈಟೆಡ್ ಕಿಂಗ್ಡಮ್',
    'Australia': 'ಆಸ್ಟ್ರೇಲಿಯಾ', 'Canada': 'ಕೆನಡಾ', 'Singapore': 'ಸಿಂಗಾಪುರ', 'Malaysia': 'ಮಲೇಷ್ಯಾ', 'Germany': 'ಜರ್ಮನಿ',
    'France': 'ಫ್ರಾನ್ಸ್', 'Netherlands': 'ನೆದರ್ಲ್ಯಾಂಡ್ಸ್', 'South Africa': 'ದಕ್ಷಿಣ ಆಫ್ರಿಕಾ', 'Nigeria': 'ನೈಜೀರಿಯಾ', 'Kenya': 'ಕೀನ್ಯಾ',
    'Saudi Arabia': 'ಸೌದಿ ಅರೇಬಿಯಾ', 'Nepal': 'ನೇಪಾಳ', 'Bangladesh': 'ಬಾಂಗ್ಲಾದೇಶ', 'Sri Lanka': 'ಶ್ರೀಲಂಕಾ', 'Pakistan': 'ಪಾಕಿಸ್ತಾನ',
    'Philippines': 'ಫಿಲಿಪ್ಪೀನ್ಸ್', 'Indonesia': 'ಇಂಡೋನೇಷ್ಯಾ', 'New Zealand': 'ನ್ಯೂಜಿಲೆಂಡ್', 'Other': 'ಇತರ (Other)'
  },
  'Malayalam': {
    'India': 'ഇന്ത്യ', 'United Arab Emirates': 'ഐക്യ അറബ് എമിറേറ്റ്സ്', 'United States': 'അമേരിക്ക', 'United Kingdom': 'യുണൈറ്റഡ് കിങ്ഡം',
    'Australia': 'ഓസ്ട്രേലിയ', 'Canada': 'കാനഡ', 'Singapore': 'സിംഗപ്പൂർ', 'Malaysia': 'മലേഷ്യ', 'Germany': 'ജർമനി',
    'France': 'ഫ്രാൻസ്', 'Netherlands': 'നെതർലാൻഡ്സ്', 'South Africa': 'ദക്ഷിണാഫ്രിക്ക', 'Nigeria': 'നൈജീരിയ', 'Kenya': 'കെനിയ',
    'Saudi Arabia': 'സൗദി അറേബ്യ', 'Nepal': 'നേപ്പാൾ', 'Bangladesh': 'ബംഗ്ലാദേശ്', 'Sri Lanka': 'ശ്രീലങ്ക', 'Pakistan': 'പാകിസ്ഥാൻ',
    'Philippines': 'ഫിലിപ്പീൻസ്', 'Indonesia': 'ഇന്തോനേഷ്യ', 'New Zealand': 'ന്യൂസിലൻഡ്', 'Other': 'മറ്റുള്ളവ (Other)'
  },
  'Marathi': {
    'India': 'भारत', 'United Arab Emirates': 'संयुक्त अरब अमिराती', 'United States': 'अमेरिकेची संयुक्त संस्थाने', 'United Kingdom': 'युनायटेड किंगडम',
    'Australia': 'ऑस्ट्रेलिया', 'Canada': 'कॅनडा', 'Singapore': 'सिंगापूर', 'Malaysia': 'मलेशिया', 'Germany': 'जर्मनी',
    'France': 'फ्रान्स', 'Netherlands': 'नेदरलँड', 'South Africa': 'दक्षिण आफ्रिका', 'Nigeria': 'नायजेरिया', 'Kenya': 'केनिया',
    'Saudi Arabia': 'सौदी अरेबिया', 'Nepal': 'नेपाळ', 'Bangladesh': 'बांगलादेश', 'Sri Lanka': 'श्रीलंका', 'Pakistan': 'पाकिस्तान',
    'Philippines': 'फिलिपिन्स', 'Indonesia': 'इंडोनेशिया', 'New Zealand': 'न्यूझीलंड', 'Other': 'इतर (Other)'
  },
  'Gujarati': {
    'India': 'ભારત', 'United Arab Emirates': 'સંયુક્ત આરબ અમીરાત', 'United States': 'અમેરિકા', 'United Kingdom': 'યુનાઇટેડ કિંગડમ',
    'Australia': 'ઑસ્ટ્રેલિયા', 'Canada': 'કેનેડા', 'Singapore': 'સિંગાપોર', 'Malaysia': 'મલેશિયા', 'Germany': 'જર્મની',
    'France': 'ફ્રાંસ', 'Netherlands': 'નેધરલેન્ડ', 'South Africa': 'દક્ષિણ આફ્રિકા', 'Nigeria': 'નાઇજીરિયા', 'Kenya': 'કેન્યા',
    'Saudi Arabia': 'સાઉદી અરેબિયા', 'Nepal': 'નેપાળ', 'Bangladesh': 'બાંગ્લાદેશ', 'Sri Lanka': 'શ્રીલંકા', 'Pakistan': 'પાકિસ્તાન',
    'Philippines': 'ફિલિપાઇન્સ', 'Indonesia': 'ઇન્ડોનેશિયા', 'New Zealand': 'ન્યુઝીલેન્ડ', 'Other': 'અન્ય (Other)'
  },
  'Bengali': {
    'India': 'ভারত', 'United Arab Emirates': 'সংযুক্ত আরব আমিরাত', 'United States': 'মার্কিন যুক্তরাষ্ট্র', 'United Kingdom': 'যুক্তরাজ্য',
    'Australia': 'অস্ট্রেলিয়া', 'Canada': 'কানাডা', 'Singapore': 'সিঙ্গাপুর', 'Malaysia': 'মালয়েশিয়া', 'Germany': 'জার্মানি',
    'France': 'ফ্রান্স', 'Netherlands': 'নেদারল্যান্ডস', 'South Africa': 'দক্ষিণ আফ্রিকা', 'Nigeria': 'নাইজেরিয়া', 'Kenya': 'কেনিয়া',
    'Saudi Arabia': 'সৌদি আরব', 'Nepal': 'নেপাল', 'Bangladesh': 'বাংলাদেশ', 'Sri Lanka': 'শ্রীলঙ্কা', 'Pakistan': 'পাকিস্তান',
    'Philippines': 'ফিলিপাইন', 'Indonesia': 'ইন্দোনেশিয়া', 'New Zealand': 'নিউজিল্যান্ড', 'Other': 'অন্যান্য (Other)'
  },
};

export const getBilingualStateName = (state, opts) => {
  const primaryLang = opts?.primaryDataLanguage || 'Tamil';
  const secondaryLang = opts?.secondaryDataLanguage || 'English';

  // Resolve to English first
  let englishName = opts?.fallbackEnglishName || state?.toString().trim() || '';
  const lowerState = englishName.toLowerCase();

  const isEnglishKey = Object.keys(STATE_TRANSLATIONS['Tamil'] || {}).find(k => k.toLowerCase() === lowerState);
  if (isEnglishKey) {
    englishName = isEnglishKey;
  } else {
    for (const [, langMap] of Object.entries(STATE_TRANSLATIONS)) {
      const found = Object.entries(langMap).find(([, v]) => v.toLowerCase() === lowerState);
      if (found) { englishName = found[0]; break; }
    }
  }

  const getTranslated = (lang) => {
    if (lang === 'English') return englishName;
    const lowerEng = englishName.toLowerCase();
    const foundKey = Object.keys(STATE_TRANSLATIONS[lang] || {}).find(k => k.toLowerCase() === lowerEng);
    return foundKey ? STATE_TRANSLATIONS[lang][foundKey] : englishName;
  };

  if (opts?.returnOnlyPrimary || (opts?.enableBilingual === false && !opts?.returnOnlySecondary)) return getTranslated(primaryLang);
  
  // SHIELD: If bilingual mode is OFF and they ask for secondary, return empty string
  if (opts?.enableBilingual === false && opts?.returnOnlySecondary) return '';
  
  if (opts?.returnOnlySecondary) return getTranslated(secondaryLang);

  const primary = getTranslated(primaryLang);
  const secondary = getTranslated(secondaryLang);
  if (opts?.enableBilingual !== false) return `${primary} / ${secondary}`;
  return primary;
};

export const doesStateMatchSearch = (state: string, searchTerm: string): boolean => {
  if (!searchTerm) return true;
  const lowerSearch = searchTerm.toLowerCase();
  
  // Resolve to English first in case input state is already translated
  let englishName = state;
  for (const [, langMap] of Object.entries(STATE_TRANSLATIONS)) {
    const found = Object.entries(langMap).find(([, v]) => v === state);
    if (found) { englishName = found[0]; break; }
  }

  if (englishName.toLowerCase().includes(lowerSearch)) return true;
  
  for (const langMap of Object.values(STATE_TRANSLATIONS)) {
    if (langMap[englishName]?.toLowerCase().includes(lowerSearch)) return true;
  }
  return false;
};

export const doesCountryMatchSearch = (country: string, searchTerm: string): boolean => {
  if (!searchTerm) return true;
  const lowerSearch = searchTerm.toLowerCase();
  
  let englishName = country;
  for (const [, langMap] of Object.entries(COUNTRY_TRANSLATIONS)) {
    const found = Object.entries(langMap).find(([, v]) => v === country);
    if (found) { englishName = found[0]; break; }
  }

  if (englishName.toLowerCase().includes(lowerSearch)) return true;
  
  for (const langMap of Object.values(COUNTRY_TRANSLATIONS)) {
    if (langMap[englishName]?.toLowerCase().includes(lowerSearch)) return true;
  }
  return false;
};

export const getBilingualCountryName = (country, opts) => {
  const primaryLang = opts?.primaryDataLanguage || 'Tamil';
  const secondaryLang = opts?.secondaryDataLanguage || 'English';

  // Resolve English name (country might already be stored in a native script)
  let englishName = country;
  for (const [, langMap] of Object.entries(COUNTRY_TRANSLATIONS)) {
    const found = Object.entries(langMap).find(([, v]) => v === country);
    if (found) { englishName = found[0]; break; }
  }

  const getTranslated = (lang) => {
    if (lang === 'English') return englishName;
    return COUNTRY_TRANSLATIONS[lang]?.[englishName] || englishName;
  };

  if (opts?.returnOnlyPrimary || (opts?.enableBilingual === false && !opts?.returnOnlySecondary)) return getTranslated(primaryLang);
  
  // SHIELD: If bilingual mode is OFF and they ask for secondary, return empty string
  if (opts?.enableBilingual === false && opts?.returnOnlySecondary) return '';
  
  if (opts?.returnOnlySecondary) return getTranslated(secondaryLang);

  const primary = getTranslated(primaryLang);
  const secondary = getTranslated(secondaryLang);
  if (opts?.enableBilingual !== false) return `${primary} / ${secondary}`;
  return primary;
};
