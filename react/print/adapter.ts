// @ts-nocheck
// Translates the KMP app's payload (English field names, language-code maps
// like { ta: '...', en: '...' }) into the exact props the React print
// templates receive in the React app. The templates themselves are imported
// untouched — every naming difference is resolved here, in one place.

const LANG_NAMES = {
  ta: 'Tamil',
  en: 'English',
  hi: 'Hindi',
  te: 'Telugu',
  kn: 'Kannada',
  ml: 'Malayalam',
};

const langName = (code, fallback) => LANG_NAMES[code] || fallback;

const round2 = (n) => Math.round((Number(n) || 0) * 100) / 100;

const str = (v) => (v === null || v === undefined ? '' : String(v));

// React stores every bilingual field four ways (see Avanam.prepareForStorage):
//   field      -> primary language value
//   fieldEn    -> secondary language value
//   field_<Primary>, field_<Secondary> -> language-tagged copies
const putBilingual = (out, key, map, lang) => {
  const primary = str(map?.[lang.primaryCode]);
  const secondary = str(map?.[lang.secondaryCode]);
  out[key] = primary;
  out[`${key}En`] = secondary;
  out[`${key}_${lang.primary}`] = primary;
  out[`${key}_${lang.secondary}`] = secondary;
};

const resolveLang = (profile) => {
  const primaryCode = profile?.primaryLanguage || 'ta';
  const secondaryCode = profile?.secondaryLanguage || 'en';
  return {
    primaryCode,
    secondaryCode,
    primary: langName(primaryCode, 'Tamil'),
    secondary: langName(secondaryCode, 'English'),
  };
};

const toPaymentAccount = (acc, lang) => ({
  id: str(acc.id),
  label: str(acc.label),
  vangiPeyar: str(acc.bankName?.[lang.primaryCode]),
  vangiPeyarEn: str(acc.bankName?.[lang.secondaryCode]),
  bankBranch: str(acc.branch?.[lang.primaryCode]),
  bankBranchEn: str(acc.branch?.[lang.secondaryCode]),
  kanakkuEn: str(acc.accountNumber),
  ifsc: str(acc.ifsc),
  swift: '',
  upiId: str(acc.upiId),
  isDefault: acc.isDefault === true,
  isActive: acc.isActive !== false,
  notes: '',
});

export const toReactProfile = (p, lang) => {
  const out = {
    primaryDataLanguage: lang.primary,
    secondaryDataLanguage: lang.secondary,
    enableBilingual: p.isBilingual !== false,
  };

  putBilingual(out, 'niruvanathinPeyar', p.businessName, lang);
  putBilingual(out, 'mugavari', p.address, lang);
  putBilingual(out, 'oor', p.city, lang);
  putBilingual(out, 'maavattam', p.district, lang);
  putBilingual(out, 'maanilam', p.state, lang);
  putBilingual(out, 'adaimozhi', p.tagline, lang);

  // Country drives tax labels via getCountryConfig(), which is keyed by the
  // English country name — so the base value must be English.
  const countryEn = str(p.country?.en) || str(p.country?.[lang.secondaryCode]) || 'India';
  putBilingual(out, 'country', p.country, lang);
  out.country = countryEn;
  out.country_English = countryEn;

  out.pin = str(p.pinCode);
  out.tholaipesi = [p.phone1, p.phone2].map(str).filter(Boolean).join(',');
  out.minnanjal = str(p.email);
  out.email = str(p.email);
  out.gstin = str(p.gstin);

  const ps = p.printSettings || {};
  out.logo = str(p.logo);
  if (ps.logoHeight !== undefined) out.logoHeight = Number(ps.logoHeight);
  out.wideLogo = str(p.wideLogo);
  if (ps.wideLogoX !== undefined) out.wideLogoX = Number(ps.wideLogoX);
  if (ps.wideLogoY !== undefined) out.wideLogoY = Number(ps.wideLogoY);
  if (ps.wideLogoScale !== undefined) out.wideLogoScale = Number(ps.wideLogoScale);
  if (ps.receiptLanguage) out.receiptLanguage = ps.receiptLanguage;
  out.billHeaderStyle = p.headerLayout === 'wide' ? 'wide' : 'small';
  out.signature = str(p.signature);
  out.authorizedSignatoryName = str(p.signatoryName);
  out.themeColor = str(p.themeColor) || '#388e3c';
  out.invoiceTheme = 'sjs';

  // Legacy flat bank fields (React synthesises an account from these when
  // paymentAccounts is empty).
  out.vangiPeyar = str(p.bankName?.[lang.primaryCode]);
  out.vangiPeyarEn = str(p.bankName?.[lang.secondaryCode]);
  out.bankBranch = str(p.branch?.[lang.primaryCode]);
  out.bankBranchEn = str(p.branch?.[lang.secondaryCode]);
  out.kanakkuEn = str(p.accountNumber);
  out.ifsc = str(p.ifsc);
  out.upiId = str(p.upiId);

  const accounts = Array.isArray(p.paymentAccounts) ? p.paymentAccounts : [];
  out.paymentAccounts = accounts.map((a) => toPaymentAccount(a, lang));

  return out;
};

export const toReactClient = (c, invoice, lang) => {
  const out = {};
  const name = c?.name && Object.keys(c.name).length ? c.name : invoice.customerName;
  const city = c?.city && Object.keys(c.city).length ? c.city : invoice.customerAddress;

  putBilingual(out, 'name', name, lang);
  putBilingual(out, 'mugavari', c?.address, lang);
  putBilingual(out, 'oor', city, lang);
  putBilingual(out, 'maavattam', c?.district, lang);
  putBilingual(out, 'maanilam', c?.state, lang);
  putBilingual(out, 'country', c?.country, lang);

  out.pin = str(c?.pinCode);
  out.gstin = str(c?.gstin);
  out.email = str(c?.email);
  out.tholaipesi = str(c?.phone);
  return out;
};

export const toReactItems = (items, lang) =>
  (items || []).map((it, index) => {
    const out = {
      id: str(it.id) || `item_${index}`,
      hsn: str(it.hsnCode),
      qty: Number(it.quantity) || 0,
      quantity: Number(it.quantity) || 0,
      rate: Number(it.price) || 0,
      // React keeps the per-line discount as an absolute amount.
      discount: round2(it.discountAmount),
      taxPercent: Number(it.taxRate) || 0,
      unit: str(it.unit),
    };
    putBilingual(out, 'name', it.name, lang);
    putBilingual(out, 'description', it.description, lang);
    return out;
  });

export const toReactTotals = (t, taxInclusive) => ({
  subtotal: round2(t?.subtotal),
  totalDiscount: round2(t?.totalDiscount),
  cgst: round2(t?.cgst),
  sgst: round2(t?.sgst),
  igst: round2(t?.igst),
  cess: 0,
  roundOff: round2(t?.roundOff),
  tcsAmount: 0,
  tdsAmount: 0,
  total: round2(t?.total),
  netReceivable: round2(t?.total),
  amountPaid: 0,
  taxInclusive: taxInclusive === true,
});

// Same keys React keeps per bill in bill.data.invoiceOptions.
const BILL_OPTION_KEYS = ['taxInclusive', 'showDiscountColumn', 'showHSN', 'showPlaceOfSupply', 'showVehicle', 'showDueDate', 'measureMode'];

// React: options = { ...global invoiceDisplayOptions, ...bill.data.invoiceOptions }
export const toReactOptions = (globalOptions, billOptions) => {
  const options = { ...(globalOptions || {}) };
  const bill = billOptions || {};
  for (const key of BILL_OPTION_KEYS) {
    if (bill[key] !== undefined && bill[key] !== '') options[key] = bill[key];
  }
  if (bill.selectedAccountId) options.selectedAccountId = bill.selectedAccountId;
  if (!options.currency) options.currency = 'INR';
  return options;
};

// Full GST invoice payload -> SjsTheme props.
export const toGstInvoiceProps = (payload) => {
  const p = payload.profile || {};
  const inv = payload.invoice || {};
  const lang = resolveLang(p);

  return {
    profile: toReactProfile(p, lang),
    client: toReactClient(payload.customer, inv, lang),
    details: {
      invoiceNumber: str(inv.invoiceNumber),
      invoiceDate: str(inv.invoiceDate),
      placeOfSupply: str(inv.placeOfSupply),
    },
    items: toReactItems(inv.items, lang),
    totals: toReactTotals(inv.totals, inv.options?.taxInclusive === true),
    invoiceType: inv.invoiceType || 'tax-invoice',
    customTerms: undefined,
    options: toReactOptions(p.printSettings?.invoiceDisplayOptions, inv.options),
  };
};

// ---------------------------------------------------------------------------
// Receipts (GST + Coolie) and Coolie invoices
// ---------------------------------------------------------------------------

// KMP payment mode codes -> the labels React stores (ReceiptView.renderPaymentMode keys).
const PAYMENT_MODES = {
  cash: 'Cash',
  upi: 'UPI',
  bank_transfer: 'Bank Transfer',
  bank: 'Bank Transfer',
  cheque: 'Cheque',
  card: 'Card',
  other: 'Other',
};

const toReactReceipt = (r, lang) => ({
  receiptNo: str(r.receiptNumber),
  date: str(r.receiptDate),
  amount: Number(r.amount) || 0,
  paymentMode: PAYMENT_MODES[str(r.paymentMode).toLowerCase()] || str(r.paymentMode),
  referenceNo: str(r.transactionNumber),
  againstInvoice: (r.invoiceNumbers || []).join(', '),
  note: str(r.notes),
  clientName: str(r.customerName?.[lang.primaryCode]),
  clientNameEn: str(r.customerName?.[lang.secondaryCode]),
});

// GST receipt payload -> ReceiptView props.
export const toGstReceiptProps = (payload) => {
  const p = payload.profile || {};
  const lang = resolveLang(p);
  return {
    profile: toReactProfile(p, lang),
    receipt: toReactReceipt(payload.receipt || {}, lang),
  };
};

// KMP profile -> React Coolie profile (coolie_thannilai record). Coolie views
// always read Tamil from the base field and English from the `En` field.
export const toCoolieProfile = (p) => {
  const ps = p.printSettings || {};
  return {
    id: str(p.id),
    name: str(p.businessName?.ta),
    nameEn: str(p.businessName?.en),
    shortBusinessName: str(p.shortName),
    address: str(p.address?.ta),
    addressEn: str(p.address?.en),
    city: str(p.city?.ta),
    cityEn: str(p.city?.en),
    district: str(p.district?.ta),
    districtEn: str(p.district?.en),
    pincode: str(p.pinCode),
    bankName: str(p.bankName?.ta),
    bankNameEn: str(p.bankName?.en),
    accountNo: str(p.accountNumber),
    ifsc: str(p.ifsc),
    branch: str(p.branch?.ta),
    branchEn: str(p.branch?.en),
    email: str(p.email),
    logo: str(p.logo),
    signature: str(p.signature),
    authorizedSignatoryName: str(p.signatoryName),
    themeColor: str(p.themeColor),
    defaultPrintLanguage: ps.defaultPrintLanguage || 'ta',
    receiptLanguage: ps.receiptLanguage || 'ta',
    phone: [p.phone1, p.phone2].map(str).filter(Boolean).join(','),
  };
};

// "2026-02-21" -> "21/02/2026" (React Coolie bills store dd/MM/yyyy).
const toDmy = (iso) => {
  const m = /^(\d{4})-(\d{2})-(\d{2})/.exec(str(iso));
  return m ? `${m[3]}/${m[2]}/${m[1]}` : str(iso);
};

// React keeps unused Coolie amounts as '' (not 0).
const amountOrBlank = (n) => (Number(n) ? Number(n) : '');

// Coolie invoice payload -> CoolieTheme props (the Textiles invoice paper without GST).
export const toCoolieThemeProps = (payload) => {
  const p = payload.profile || {};
  const inv = payload.invoice || {};
  const lang = resolveLang(p);
  const items = (inv.items || []).map((it, index) => {
    const out = { id: `item_${index}`, qty: Number(it.weight) || 0, rate: Number(it.price) || 0 };
    putBilingual(out, 'name', it.name, lang);
    return out;
  });
  return {
    profile: toReactProfile(p, lang),
    client: toReactClient(payload.customer, inv, lang),
    details: {
      invoiceNumber: str(inv.invoiceNumber),
      invoiceDate: str(inv.invoiceDate),
      wastageGrams: Number(inv.wastageGrams) || 0,
      courier: Number(inv.postalCharges) || 0,
      ahimsaSilk: Number(inv.ahimsaSilkAmount) || 0,
    },
    items,
    totals: { total: round2(inv.grandTotal) },
    charges: (inv.charges || [])
      .filter((ch) => str(ch.name) || Number(ch.amount))
      .map((ch) => ({ name: str(ch.name), amount: Number(ch.amount) || 0 })),
    options: toReactOptions(p.printSettings?.invoiceDisplayOptions, {}),
  };
};

// Coolie receipt payload -> CoolieReceiptView `receipt` (+ profile for the Avanam shim).
export const toCoolieReceiptProps = (payload) => {
  const p = payload.profile || {};
  const profile = toCoolieProfile(p);
  const receipt = toReactReceipt(payload.receipt || {}, { primaryCode: 'ta', secondaryCode: 'en' });
  return {
    coolieProfiles: [profile],
    receipt: { ...receipt, company_id: profile.id },
  };
};
