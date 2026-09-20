// @ts-nocheck
// File-based storage via local Express API server
// All data persists as JSON files in the ./data/ folder

const API = '/api';

import { rtdb } from './firebase';
import { ref, get, set, remove, update, onValue, runTransaction } from 'firebase/database';

import { withCache, clearCache } from './CacheManager';

const activeListeners = {};

async function apiFetch(url, options = {}, onFreshData = null) {
  const isGet = !options.method || options.method === 'GET';
  const isPost = options.method === 'POST';
  const isDelete = options.method === 'DELETE';
  
  let path = url.replace('/api/', '').split('?')[0];

  if (path === 'export') {
    const snapshot = await get(ref(rtdb, '/'));
    return snapshot.exists() ? snapshot.val() : {};
  }
  if (path === 'version') {
     return { version: '2.0.0' };
  }

  const collections = ['pattiyalkal', 'vanigargal', 'mathirigal', 'porulgal', 'patrugal', 'thannilai', 'coolie_pattiyalkal', 'coolie_vanigargal', 'coolie_porulgal', 'coolie_patrugal', 'coolie_thannilai'];

  if (path === 'import') {
    const payload = JSON.parse(options.body);
    const updates = {};
    const keyMap = {
      bills: 'pattiyalkal', clients: 'vanigargal', products: 'porulgal', receipts: 'patrugal',
      profiles: 'thannilai', termsTemplates: 'mathirigal', coolieBills: 'coolie_pattiyalkal',
      coolieClients: 'coolie_vanigargal', coolieProducts: 'coolie_porulgal', coolieReceipts: 'coolie_patrugal',
      coolieProfiles: 'coolie_thannilai'
    };

    if (payload.profile) updates['profile'] = payload.profile;
    if (payload.meta) updates['meta'] = payload.meta;
    
    for (const [key, dbPath] of Object.entries(keyMap)) {
      if (payload[key] && Array.isArray(payload[key])) {
        for (const item of payload[key]) {
           if (item.id) updates[`${dbPath}/${item.id}`] = item;
        }
      }
    }
    
    await update(ref(rtdb), updates);
    return { success: true };
  }

  try {
    if (isGet) {
      if (onFreshData && !activeListeners[path]) {
          activeListeners[path] = true;
          const dbRef = ref(rtdb, path);
          onValue(dbRef, (snapshot) => {
              const val = snapshot.exists() ? snapshot.val() : null;
              const data = val ? (collections.includes(path) ? Object.values(val) : val) : (collections.includes(path) ? [] : {});
              
              const cacheKey = `elvan_cache_${path.replace(/\//g, '_')}`;
              const oldCache = localStorage.getItem(cacheKey);
              const newCache = JSON.stringify(data);
              
              if (oldCache !== newCache) {
                 try {
                     localStorage.setItem(cacheKey, newCache);
                 } catch (e) {
                     console.warn('[Avanam] Error saving to cache in onValue', e);
                 }
                 window.dispatchEvent(new CustomEvent(`elvan_update_${path}`, { detail: data }));
              }
          });
      }

      const fetchFn = async () => {
        const snapshot = await get(ref(rtdb, path));
        if (snapshot.exists()) {
          const val = snapshot.val();
          if (collections.includes(path)) return Object.values(val);
          return val;
        }
        if (collections.includes(path)) return [];
        return {};
      };

      const handler = (e) => onFreshData(e.detail);
      if (onFreshData) {
          window.addEventListener(`elvan_update_${path}`, handler);
      }

      const result = await withCache(path, fetchFn, onFreshData);

      if (onFreshData && result && typeof result === 'object') {
          result.unsubscribe = () => window.removeEventListener(`elvan_update_${path}`, handler);
      }
      return result;
    }
    
    if (isPost) {
      const body = JSON.parse(options.body);
      
      if (path.endsWith('/increment')) {
        const counterPath = path.replace(/\/increment$/, '');
        const counterRef = ref(rtdb, counterPath);
        let finalVal = 1;
        
        const result = await runTransaction(counterRef, (currentData) => {
          if (currentData === null || typeof currentData !== 'object') {
            return { value: 1 };
          }
          return { ...currentData, value: (currentData.value || 0) + 1 };
        });
        
        if (result.committed) {
           finalVal = result.snapshot.val().value;
        }
        
        clearCache(counterPath);
        return { value: finalVal };
      }
      
      if (path === 'profile' || path.startsWith('meta/')) {
         await set(ref(rtdb, path), body);
         clearCache(path);
         return body;
      }
      const id = body.id || `doc_${Date.now()}`;
      await set(ref(rtdb, `${path}/${id}`), body);
      clearCache(path);
      return body;
    }

    if (isDelete) {
      await remove(ref(rtdb, path));
      clearCache(path);
      return { success: true };
    }
  } catch (error) {
    console.error(`[Firebase API Error] Failed to ${options.method || 'GET'} ${path}:`, error);
    if (isGet) {
      return collections.includes(path) ? [] : {};
    }
    throw error;
  }
}

// ---- Invoice Number Settings ----
const DEFAULT_INV_SETTINGS = {
  format: 'branded',      // 'branded' | 'sequential' | 'random'
  brandPrefix: '',         // e.g. 'ACME' — empty means use type prefix (INV/EST/CN/BOS)
  separator: '/',          // '/' | '-' | '#'
  showFinYear: true,       // include 2026-27 financial year
  startNumber: 1,          // starting counter value
  padDigits: 4,            // zero-pad to this many digits
};

export const getInvoiceNumberSettings = async () => {
  const { value } = await apiFetch(`${API}/meta/invoiceNumberSettings`);
  return { ...DEFAULT_INV_SETTINGS, ...(value || {}) };
};

export const saveInvoiceNumberSettings = async (settings) => {
  await apiFetch(`${API}/meta/invoiceNumberSettings`, {
    method: 'POST',
    body: JSON.stringify({ value: settings }),
  });
};

const DEFAULT_RCP_SETTINGS = {
  format: 'branded',
  brandPrefix: 'RCP',
  separator: '/',
  showFinYear: true,
  startNumber: 1,
  padDigits: 4,
};

export const getReceiptNumberSettings = async () => {
  const { value } = await apiFetch(`${API}/meta/receiptNumberSettings`);
  return { ...DEFAULT_RCP_SETTINGS, ...(value || {}) };
};

export const saveReceiptNumberSettings = async (settings) => {
  await apiFetch(`${API}/meta/receiptNumberSettings`, {
    method: 'POST',
    body: JSON.stringify({ value: settings }),
  });
};

// ---- Language-Tagged Data Storage Helpers ----
const isBlankState = () => typeof window !== 'undefined' && window.sessionStorage.getItem('__DEV_BLANK_STATE__') === 'true';

const getLangConfig = async () => {
  try {
    const profile = await apiFetch(`${API}/profile`);
    return {
      primary: profile?.primaryDataLanguage || 'Tamil',
      secondary: profile?.secondaryDataLanguage || 'English'
    };
  } catch {
    return { primary: 'Tamil', secondary: 'English' };
  }
};

const prepareForStorage = async (item, fields) => {
  const { primary, secondary } = await getLangConfig();
  const out = { ...item };
  fields.forEach(f => {
    if (out[f] !== undefined) out[`${f}_${primary}`] = out[f];
    if (out[`${f}En`] !== undefined) out[`${f}_${secondary}`] = out[`${f}En`];
  });
  return out;
};

const restoreFromStorage = async (items, fields) => {
  const { primary, secondary } = await getLangConfig();
  return items.map(item => {
    const out = { ...item };
    fields.forEach(f => {
      const hasAnyTag = Object.keys(item).some(k => k.startsWith(`${f}_`));
      if (hasAnyTag) {
        out[f] = out[`${f}_${primary}`] !== undefined ? out[`${f}_${primary}`] : '';
        out[`${f}En`] = out[`${f}_${secondary}`] !== undefined ? out[`${f}_${secondary}`] : '';
      } else {
        if (out[`${f}_${primary}`] !== undefined) out[f] = out[`${f}_${primary}`];
        if (out[`${f}_${secondary}`] !== undefined) out[`${f}En`] = out[`${f}_${secondary}`];
      }
    });
    return out;
  });
};

const restoreSingleFromStorage = async (item, fields) => {
  if (!item) return item;
  const items = await restoreFromStorage([item], fields);
  return items[0];
};

// ---- Invoice Display Options (checkboxes like showGST, showLogo etc.) ----
export const getInvoiceDisplayOptions = async (onFreshData = null) => {
  const result = await apiFetch(`${API}/meta/invoiceDisplayOptions`, {}, onFreshData ? async (fresh) => {
      onFreshData(fresh?.value || null);
  } : null);
  const val = result?.value || {};
  if (onFreshData && result && result.unsubscribe) {
    val.unsubscribe = result.unsubscribe;
  }
  return val;
};

export const saveInvoiceDisplayOptions = async (options) => {
  await apiFetch(`${API}/meta/invoiceDisplayOptions`, {
    method: 'POST',
    body: JSON.stringify({ value: options }),
  });
};





// ---- Invoice counter ----
// Uses the atomic /meta/:key/increment endpoint so two concurrent saves can't both
// read 5 and both write 6 (= duplicate invoice numbers, which is a GST audit failure).
export const getNextInvoiceNumber = async (prefix = 'INV') => {
  const profile = await getProfile();
  const pfx = profile.shortBusinessName || prefix;

  const allGstBills = await getAllBills();
  const allCoolieBills = await getAllCoolieBills();
  const allBills = [...(allGstBills || []), ...(allCoolieBills || [])];
  
  let maxFound = 0;
  for (const bill of allBills) {
    if (bill && bill.invoiceNumber && bill.invoiceNumber.startsWith(pfx + '-')) {
      const numPart = parseInt(bill.invoiceNumber.split('-')[1], 10);
      if (!isNaN(numPart) && numPart > maxFound) {
        maxFound = numPart;
      }
    }
  }
  
  const next = maxFound + 1;
  const padded = String(next).padStart(2, '0');

  return `${pfx}-${padded}`;
};

// ---- Bills ----
export const saveBill = async (bill) => {
  const taggedBill = { ...bill };
  if (taggedBill.client) taggedBill.client = await prepareForStorage(taggedBill.client, ['name', 'mugavari', 'oor', 'maavattam', 'maanilam', 'country']);
  if (taggedBill.items && Array.isArray(taggedBill.items)) {
    taggedBill.items = await Promise.all(taggedBill.items.map(async item => prepareForStorage(item, ['name', 'description'])));
  }
  return apiFetch(`${API}/pattiyalkal`, { method: 'POST', body: JSON.stringify(taggedBill) });
};

const processBills = async (bills) => {
  return Promise.all(bills.map(async bill => {
    const b = { ...bill };
    if (b.client) b.client = await restoreSingleFromStorage(b.client, ['name', 'mugavari', 'oor', 'maavattam', 'maanilam', 'country']);
    if (b.items && Array.isArray(b.items)) {
      b.items = await restoreFromStorage(b.items, ['name', 'description']);
    }
    return b;
  }));
};

export const getAllBills = async (onFreshData = null) => {
  if (isBlankState()) return [];
  const bills = await apiFetch(`${API}/pattiyalkal`, {}, onFreshData ? async (fresh) => {
      onFreshData(await processBills(fresh));
  } : null);
  return processBills(bills);
};

export const deleteBill = async (id) => {
  return apiFetch(`${API}/pattiyalkal/${encodeURIComponent(id)}`, { method: 'DELETE' });
};

// ---- Profile ----
export const saveProfile = async (profile) => {
  const taggedProfile = await prepareForStorage(profile, ['niruvanathinPeyar', 'shortBusinessName', 'mugavari', 'oor', 'maavattam', 'maanilam', 'country', 'bankName', 'branch', 'terms']);
  const result = await apiFetch(`${API}/profile`, { method: 'POST', body: JSON.stringify(taggedProfile) });
  window.dispatchEvent(new Event('profileUpdated'));
  return result;
};

export const getProfile = async (onFreshData = null) => {
  if (isBlankState()) return {};
  const profile = await apiFetch(`${API}/profile`, {}, onFreshData ? async (fresh) => {
      onFreshData(await restoreSingleFromStorage(fresh, ['niruvanathinPeyar', 'shortBusinessName', 'mugavari', 'oor', 'maavattam', 'maanilam', 'country', 'bankName', 'branch', 'terms']));
  } : null);
  return restoreSingleFromStorage(profile, ['niruvanathinPeyar', 'shortBusinessName', 'mugavari', 'oor', 'maavattam', 'maanilam', 'country', 'bankName', 'branch', 'terms']);
};

// ---- Saved Clients ----
export const saveClient = async (client) => {
  const taggedClient = await prepareForStorage(client, ['name', 'mugavari', 'oor', 'maavattam', 'maanilam', 'country']);
  const res = await apiFetch(`${API}/vanigargal`, { method: 'POST', body: JSON.stringify(taggedClient) });
  if (res.id) client.id = res.id;
  return client;
};

export const getAllClients = async (onFreshData = null) => {
  if (isBlankState()) return [];
  const clients = await apiFetch(`${API}/vanigargal`, {}, onFreshData ? async (fresh) => {
      onFreshData(await restoreFromStorage(fresh, ['name', 'mugavari', 'oor', 'maavattam', 'maanilam', 'country']));
  } : null);
  return restoreFromStorage(clients, ['name', 'mugavari', 'oor', 'maavattam', 'maanilam', 'country']);
};

export const deleteClient = async (id) => {
  return apiFetch(`${API}/vanigargal/${encodeURIComponent(id)}`, { method: 'DELETE' });
};

// ---- Coolie Clients / Merchants ----
export const getAllCoolieClients = async (onFreshData = null) => {
  if (isBlankState()) return [];
  const clients = await apiFetch(`${API}/coolie_vanigargal`, {}, onFreshData ? async (fresh) => {
      onFreshData(await restoreFromStorage(fresh, ['name', 'city', 'address1']));
  } : null);
  return restoreFromStorage(clients, ['name', 'city', 'address1']);
};

export const saveCoolieClient = async (client) => {
  const taggedClient = await prepareForStorage(client, ['name', 'city', 'address1']);
  const res = await apiFetch(`${API}/coolie_vanigargal`, { method: 'POST', body: JSON.stringify(taggedClient) });
  if (res.id) client.id = res.id;
  return client;
};

export const deleteCoolieClient = async (id) => {
  return apiFetch(`${API}/coolie_vanigargal/${encodeURIComponent(id)}`, { method: 'DELETE' });
};

// ---- Coolie Profiles / Settings ----
export const getAllCoolieProfiles = async (onFreshData = null) => {
  if (isBlankState()) return [];
  const profiles = await apiFetch(`${API}/coolie_thannilai`, {}, onFreshData ? async (fresh) => {
      onFreshData(await restoreFromStorage(fresh, ['name', 'address', 'city', 'district', 'bankName', 'branch', 'email']));
  } : null);
  return restoreFromStorage(profiles, [
    'name', 'address', 'city', 'district', 'bankName', 'branch', 'email'
  ]);
};

export const saveCoolieProfile = async (profile) => {
  const taggedProfile = await prepareForStorage(profile, [
    'name', 'address', 'city', 'district', 'bankName', 'branch', 'email'
  ]);
  const res = await apiFetch(`${API}/coolie_thannilai`, { method: 'POST', body: JSON.stringify(taggedProfile) });
  if (res.id) profile.id = res.id;
  return profile;
};

export const deleteCoolieProfile = async (id) => {
  return apiFetch(`${API}/coolie_thannilai/${encodeURIComponent(id)}`, { method: 'DELETE' });
};


// ---- Products / Inventory ----
export const getAllProducts = async (onFreshData = null) => {
  if (isBlankState()) return [];
  const products = await apiFetch(`${API}/porulgal`, {}, onFreshData ? async (fresh) => {
      onFreshData(await restoreFromStorage(fresh, ['name', 'description']));
  } : null);
  return restoreFromStorage(products, ['name', 'description']);
};

export const saveProduct = async (product) => {
  const taggedProduct = await prepareForStorage(product, ['name', 'description']);
  const res = await apiFetch(`${API}/porulgal`, { method: 'POST', body: JSON.stringify(taggedProduct) });
  if (res.id) product.id = res.id;
  return product;
};

export const deleteProduct = async (id) => {
  return apiFetch(`${API}/porulgal/${encodeURIComponent(id)}`, { method: 'DELETE' });
};

// ---- Coolie Products / Inventory ----
export const getAllCoolieProducts = async (onFreshData = null) => {
  if (isBlankState()) return [];
  const products = await apiFetch(`${API}/coolie_porulgal`, {}, onFreshData ? async (fresh) => {
      onFreshData(await restoreFromStorage(fresh, ['name', 'description']));
  } : null);
  return restoreFromStorage(products, ['name', 'description']);
};

export const saveCoolieProduct = async (product) => {
  const taggedProduct = await prepareForStorage(product, ['name', 'description']);
  const res = await apiFetch(`${API}/coolie_porulgal`, { method: 'POST', body: JSON.stringify(taggedProduct) });
  if (res.id) product.id = res.id;
  return product;
};

export const deleteCoolieProduct = async (id) => {
  return apiFetch(`${API}/coolie_porulgal/${encodeURIComponent(id)}`, { method: 'DELETE' });
};

// ---- Coolie Bills ----
export const getAllCoolieBills = async (onFreshData = null) => {
  if (isBlankState()) return [];
  const bills = await apiFetch(`${API}/coolie_pattiyalkal`, {}, onFreshData ? async (fresh) => {
      onFreshData(fresh || []);
  } : null);
  return bills || [];
};

export const getNextCoolieBillNumber = async (companyId: string) => {
  if (isBlankState()) return '1';
  try {
    const profiles = await getAllCoolieProfiles();
    const profile = profiles.find((p: any) => p.id === companyId);
    const prefix = profile?.shortBusinessName || profile?.name || 'CB';

    const bills = await apiFetch(`${API}/coolie_pattiyalkal`);
    if (!bills || !Array.isArray(bills)) {
      return `${prefix}-01`;
    }

    const companyBills = bills.filter((b: any) => b.company_id === companyId);
    if (companyBills.length === 0) {
      return `${prefix}-01`;
    }

    const maxNo = Math.max(...companyBills.map((b: any) => {
      const match = String(b.bill_no).match(/\d+/);
      return match ? parseInt(match[0], 10) : 0;
    }));

    const next = maxNo + 1;
    const paddedNext = next < 10 ? `0${next}` : `${next}`;
    
    return `${prefix}-${paddedNext}`;
  } catch (e) {
    return '1';
  }
};


export const saveCoolieBill = async (bill) => {
  const res = await apiFetch(`${API}/coolie_pattiyalkal`, { method: 'POST', body: JSON.stringify(bill) });
  return res;
};

export const deleteCoolieBill = async (id) => {
  const res = await apiFetch(`${API}/coolie_pattiyalkal/${encodeURIComponent(id)}`, { method: 'DELETE' });
  return res;
};


// ---- Receipts / Payment Vouchers ----
export const getAllReceipts = async (onFreshData = null) => {
  if (isBlankState()) return [];
  return apiFetch(`${API}/patrugal`, {}, onFreshData);
};

export const saveReceipt = async (receipt) => {
  const res = await apiFetch(`${API}/patrugal`, { method: 'POST', body: JSON.stringify(receipt) });
  if (res.id) receipt.id = res.id;
  return receipt;
};

export const deleteReceipt = async (id) => {
  return apiFetch(`${API}/patrugal/${encodeURIComponent(id)}`, { method: 'DELETE' });
};

// ---- Coolie Receipts ----
export const getAllCoolieReceipts = async (onFreshData = null) => {
  if (isBlankState()) return [];
  return apiFetch(`${API}/coolie_patrugal`, {}, onFreshData);
};

export const saveCoolieReceipt = async (receipt) => {
  const res = await apiFetch(`${API}/coolie_patrugal`, { method: 'POST', body: JSON.stringify(receipt) });
  if (res.id) receipt.id = res.id;
  return receipt;
};

export const deleteCoolieReceipt = async (id) => {
  return apiFetch(`${API}/coolie_patrugal/${encodeURIComponent(id)}`, { method: 'DELETE' });
};

// ---- Business Profiles (multi-business) ----
export const getAllProfiles = async (onFreshData = null) => {
  if (isBlankState()) return [];
  return apiFetch(`${API}/thannilai`, {}, onFreshData);
};

export const saveBusinessProfile = async (profile) => {
  const res = await apiFetch(`${API}/thannilai`, { method: 'POST', body: JSON.stringify(profile) });
  if (res.id) profile.id = res.id;
  return profile;
};

export const deleteBusinessProfile = async (id) => {
  return apiFetch(`${API}/thannilai/${encodeURIComponent(id)}`, { method: 'DELETE' });
};

// ---- Export / Import ----
// localStorage keys that are part of the "user's data" and should ride along in any
// backup. Each key is documented with what it stores and whether losing it matters.
const EXPORTABLE_LOCALSTORAGE_KEYS = [
  'gst_customUnits',          // user-defined units (e.g. Carat, Bundle) for line items
  'gst_enabledModules',        // map of disabled feature toggles
  'freegstbill_invoiceOptions',// per-invoice display preference defaults
  'theme',                     // light/dark
  'freegstbill_onboarded',     // skip welcome wizard on next launch
];

const collectLocalStorage = () => {
  const out = {};
  EXPORTABLE_LOCALSTORAGE_KEYS.forEach(k => {
    try { const v = localStorage.getItem(k); if (v !== null) out[k] = v; } catch { /* sandboxed */ }
  });
  return out;
};

const restoreLocalStorage = (map) => {
  if (!map || typeof map !== 'object') return;
  Object.entries(map).forEach(([k, v]) => {
    if (!EXPORTABLE_LOCALSTORAGE_KEYS.includes(k)) return; // ignore foreign keys
    try { localStorage.setItem(k, v); } catch { /* ignore */ }
  });
};

// Cached app version — pulled from server once per session via /api/version so the
// frontend doesn't have to ship its own copy of package.json. Falls back to 'unknown'
// only if the server is unreachable, which only happens during the brief startup
// window before the user opens the app.
let cachedAppVersion = null;
const getAppVersion = async () => {
  if (cachedAppVersion) return cachedAppVersion;
  try {
    const { current } = await apiFetch(`${API}/version`);
    if (current) { cachedAppVersion = current; return current; }
  } catch { /* server down — best effort */ }
  return 'unknown';
};

// Full export. Returns the JSON-serialised bundle (server data + localStorage).
// Pass `selection` to limit what's included — undefined ⇒ everything.
//
// `selection` shape: { profile, profiles, bills, clients, products,
//   receipts, meta, localStorage } — each bool.
export const exportAllData = async (selection) => {
  const [all, version] = await Promise.all([apiFetch(`${API}/export`), getAppVersion()]);
  const sel = selection || { profile: true, profiles: true, bills: true, clients: true, products: true, receipts: true, meta: true, localStorage: true, coolieClients: true, termsTemplates: true, coolieProducts: true, coolieBills: true, coolieReceipts: true, coolieProfiles: true };

  const data: any = { exportedAt: new Date().toISOString(), version, __freegstbill_backup: true };
  if (sel.profile)        data.profile = all.profile;
  if (sel.profiles)       data.profiles = all.profiles;
  if (sel.bills)          data.bills = all.bills;
  if (sel.clients)        data.clients = all.clients;
  if (sel.products)       data.products = all.products;
  if (sel.receipts)       data.receipts = all.receipts;
  if (sel.coolieClients)  data.coolieClients = all.coolieClients;
  if (sel.termsTemplates) data.termsTemplates = all.termsTemplates;
  if (sel.coolieProducts) data.coolieProducts = all.coolieProducts;
  if (sel.coolieProfiles) data.coolieProfiles = all.coolieProfiles;
  if (sel.coolieBills)    data.coolieBills = all.coolieBills;
  if (sel.coolieReceipts) data.coolieReceipts = all.coolieReceipts;

  if (sel.meta)           data.meta = all.meta; // includes enabledModules, etc. on server
  if (sel.localStorage)   data.localStorage = collectLocalStorage();

  return JSON.stringify(data, null, 2);
};

// Inspect a backup file without committing — returns counts so the UI can show
// what's in it before the user picks what to restore.
export const inspectBackup = (jsonString) => {
  let data;
  try { data = JSON.parse(jsonString); }
  catch { throw new Error('Not a valid JSON file'); }
  return {
    valid: !!data && (data.__freegstbill_backup || data.bills || data.profile),
    exportedAt: data.exportedAt || null,
    version: data.version || null,
    counts: {
      profile: data.profile && Object.keys(data.profile).length > 0 ? 1 : 0,
      profiles: Array.isArray(data.profiles) ? data.profiles.length : 0,
      bills: Array.isArray(data.bills) ? data.bills.length : 0,
      clients: Array.isArray(data.clients) ? data.clients.length : 0,
      products: Array.isArray(data.products) ? data.products.length : 0,
      receipts: Array.isArray(data.receipts) ? data.receipts.length : 0,
      coolieClients: Array.isArray(data.coolieClients) ? data.coolieClients.length : 0,
      termsTemplates: Array.isArray(data.termsTemplates) ? data.termsTemplates.length : 0,
      coolieProducts: Array.isArray(data.coolieProducts) ? data.coolieProducts.length : 0,
      coolieProfiles: Array.isArray(data.coolieProfiles) ? data.coolieProfiles.length : 0,
      coolieBills: Array.isArray(data.coolieBills) ? data.coolieBills.length : 0,
      coolieReceipts: Array.isArray(data.coolieReceipts) ? data.coolieReceipts.length : 0,
      meta: data.meta ? Object.keys(data.meta).length : 0,
      localStorage: data.localStorage ? Object.keys(data.localStorage).length : 0,
    },
    raw: data,
  };
};

// Selective import. `selection` is the same shape as for exportAllData.
export const importData = async (jsonString, selection) => {
  const inspected = typeof jsonString === 'string' ? inspectBackup(jsonString) : { raw: jsonString };
  const data = inspected.raw;
  const sel = selection || { profile: true, profiles: true, bills: true, clients: true, products: true, receipts: true, meta: true, localStorage: true, coolieClients: true, termsTemplates: true, coolieProducts: true, coolieBills: true, coolieReceipts: true, coolieProfiles: true };

  // Build a filtered payload — never touch collections the user didn't tick.
  const payload: any = {};
  if (sel.profile && data.profile)               payload.profile = data.profile;
  if (sel.profiles && data.profiles)             payload.profiles = data.profiles;
  if (sel.bills && data.bills)                   payload.bills = data.bills;
  if (sel.clients && data.clients)               payload.clients = data.clients;
  if (sel.products && data.products)             payload.products = data.products;
  if (sel.receipts && data.receipts)             payload.receipts = data.receipts;
  if (sel.coolieClients && data.coolieClients)   payload.coolieClients = data.coolieClients;
  if (sel.termsTemplates && data.termsTemplates) payload.termsTemplates = data.termsTemplates;
  if (sel.coolieProducts && data.coolieProducts) payload.coolieProducts = data.coolieProducts;
  if (sel.coolieProfiles && data.coolieProfiles) payload.coolieProfiles = data.coolieProfiles;
  if (sel.coolieBills && data.coolieBills)       payload.coolieBills = data.coolieBills;
  if (sel.coolieReceipts && data.coolieReceipts) payload.coolieReceipts = data.coolieReceipts;

  if (sel.meta && data.meta)                     payload.meta = data.meta;

  const result = await apiFetch(`${API}/import`, { method: 'POST', body: JSON.stringify(payload) });

  if (sel.localStorage && data.localStorage) restoreLocalStorage(data.localStorage);

  return { ...result, counts: inspected.counts || {} };
};

// Dummy exports for deleted features to keep VariArikkaigal compiling
export const getAllExpenses = async () => [];
export const getAllPurchases = async () => [];
