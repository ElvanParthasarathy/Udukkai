const fs = require('fs');

function updateDict(path, isTamil) {
  let content = fs.readFileSync(path, 'utf-8');
  
  const additions = isTamil ? `
  // Inventory (Porul) Page
  inventoryTitle: 'பொருள்',
  inventorySubtitle: 'உங்கள் பொருட்கள் மற்றும் சேவைகளை நிர்வகிக்கவும்',
  addProductBtn: 'பொருளைச் சேர்',
  searchProducts: 'பெயர் அல்லது HSN மூலம் தேடுக...',
  editProductTitle: 'பொருளைத் திருத்து',
  addProductTitle: 'புதிய பொருளைச் சேர்',
  productNameLabel: 'பொருள் / சேவை பெயர் *',
  productNamePlaceholder: 'உ.ம். இணையதளம் உருவாக்குதல்',
  hsnCodeLabel: 'HSN / SAC குறியீடு',
  hsnCodePlaceholder: 'உ.ம். 998314',
  rateLabel: 'விலை (அடிப்படை விலை)',
  gstPercentLabel: 'GST %',
  unitLabel: 'அலகு (Unit)',
  stockQuantityLabel: 'கையிருப்பு அளவு',
  descriptionLabelOptional: 'விவரம் (விரும்பினால்)',
  descriptionPlaceholder: 'சுருக்கமான விவரம்...',
  productsAndServices: 'பொருட்கள் & சேவைகள்',
  noProductsYet: 'இன்னும் பொருட்கள் இல்லை. உங்கள் முதல் பொருளைச் சேர்க்கவும்.',
  noProductsMatch: 'உங்கள் தேடலுக்கு எந்தப் பொருளும் பொருந்தவில்லை.',
  outOfStock: 'கையிருப்பு இல்லை',
  productUpdatedToast: 'பொருள் புதுப்பிக்கப்பட்டது',
  productAddedToast: 'பொருள் சேர்க்கப்பட்டது',
  productNameRequiredToast: 'பொருளின் பெயர் அவசியம்',
  deleteProductConfirmMsg: 'இந்தப் பொருளை நீக்க வேண்டுமா? இதை மாற்ற இயலாது.',
  productDeletedToast: 'பொருள் நீக்கப்பட்டது',
  failedToSaveProductToast: 'பொருளைச் சேமிக்க முடியவில்லை',
  failedToLoadProductsToast: 'பொருட்களை ஏற்ற முடியவில்லை',
  emptyCsvWarningToast: 'CSV கோப்பு காலியாக உள்ளது',
  failedToParseCsvToast: 'CSV கோப்பைப் படிக்க முடியவில்லை',
  nameCol: 'பெயர்',
  hsnCol: 'HSN/SAC',
  stockCol: 'கையிருப்பு',
  ` : `
  // Inventory (Porul) Page
  inventoryTitle: 'Inventory',
  inventorySubtitle: 'Manage your products and services catalog',
  addProductBtn: 'Add Product',
  searchProducts: 'Search by name or HSN...',
  editProductTitle: 'Edit Product',
  addProductTitle: 'Add Product',
  productNameLabel: 'Product / Service Name *',
  productNamePlaceholder: 'e.g. Web Development',
  hsnCodeLabel: 'HSN / SAC Code',
  hsnCodePlaceholder: 'e.g. 998314',
  rateLabel: 'Rate (Default Price)',
  gstPercentLabel: 'GST %',
  unitLabel: 'Unit',
  stockQuantityLabel: 'Stock Quantity',
  descriptionLabelOptional: 'Description (optional)',
  descriptionPlaceholder: 'Brief description...',
  productsAndServices: 'Products & Services',
  noProductsYet: 'No products yet. Add your first product.',
  noProductsMatch: 'No products match your search.',
  outOfStock: 'Out of Stock',
  productUpdatedToast: 'Product updated',
  productAddedToast: 'Product added',
  productNameRequiredToast: 'Product name is required',
  deleteProductConfirmMsg: 'Delete this product? This cannot be undone.',
  productDeletedToast: 'Product deleted',
  failedToSaveProductToast: 'Failed to save product',
  failedToLoadProductsToast: 'Failed to load products',
  emptyCsvWarningToast: 'CSV file is empty or has no data rows',
  failedToParseCsvToast: 'Failed to parse CSV file',
  nameCol: 'Name',
  hsnCol: 'HSN/SAC',
  stockCol: 'Stock',
  `;

  if (!content.includes('inventoryTitle:')) {
    content = content.replace(/};\s*export type TranslationKey/, additions + '\n};\n\nexport type TranslationKey');
    fs.writeFileSync(path, content, 'utf-8');
  }
}

updateDict('moolam/mozhi/ta.ts', true);
updateDict('moolam/mozhi/en.ts', false);

// Patch Porul.tsx
let v = fs.readFileSync('moolam/pagudhigal/Porul.tsx', 'utf-8');
if (!v.includes('useLanguage')) {
  v = v.replace(/import \{ thagaval \} from '\.\/Thagaval';/, "import { thagaval } from './Thagaval';\nimport { useLanguage } from '../mozhi/LanguageContext';");
  v = v.replace(/export default function Porul\(\) \{/, "$&\n  const { t } = useLanguage();");
}

v = v.replace(/'Failed to load products'/g, "t('failedToLoadProductsToast')");
v = v.replace(/'Product name is required'/g, "t('productNameRequiredToast')");
v = v.replace(/'Product updated'/g, "t('productUpdatedToast')");
v = v.replace(/'Product added'/g, "t('productAddedToast')");
v = v.replace(/'Failed to save product'/g, "t('failedToSaveProductToast')");
v = v.replace(/'Delete this product\? This cannot be undone\.'/g, "t('deleteProductConfirmMsg')");
v = v.replace(/'Product deleted'/g, "t('productDeletedToast')");
v = v.replace(/'CSV file is empty or has no data rows'/g, "t('emptyCsvWarningToast')");
v = v.replace(/'Failed to parse CSV file'/g, "t('failedToParseCsvToast')");

v = v.replace(/>Inventory</g, ">{t('inventoryTitle')}<");
v = v.replace(/>Manage your products and services catalog</g, ">{t('inventorySubtitle')}<");
v = v.replace(/<Upload size=\{16\} \/> Import CSV/g, "<Upload size={16} /> {t('importCSV')}");
v = v.replace(/<Plus size=\{18\} \/> Add Product/g, "<Plus size={18} /> {t('addProductBtn')}");
v = v.replace(/placeholder="Search by name or HSN\.\.\."/g, "placeholder={t('searchProducts')}");
v = v.replace(/\{editingId \? 'Edit Product' : 'Add Product'\}/g, "{editingId ? t('editProductTitle') : t('addProductTitle')}");
v = v.replace(/>Product \/ Service Name \*</g, ">{t('productNameLabel')}<");
v = v.replace(/placeholder="e\.g\. Web Development"/g, "placeholder={t('productNamePlaceholder')}");
v = v.replace(/>HSN \/ SAC Code</g, ">{t('hsnCodeLabel')}<");
v = v.replace(/placeholder="e\.g\. 998314"/g, "placeholder={t('hsnCodePlaceholder')}");
v = v.replace(/>Rate \(Default Price\)</g, ">{t('rateLabel')}<");
v = v.replace(/>GST %</g, ">{t('gstPercentLabel')}<");
v = v.replace(/>Unit</g, ">{t('unitLabel')}<");
v = v.replace(/>Stock Quantity</g, ">{t('stockQuantityLabel')}<");
v = v.replace(/>Description \(optional\)</g, ">{t('descriptionLabelOptional')}<");
v = v.replace(/placeholder="Brief description\.\.\."/g, "placeholder={t('descriptionPlaceholder')}");
v = v.replace(/>Products \& Services</g, ">{t('productsAndServices')}<");
v = v.replace(/products\.length === 0 \? 'No products yet\. Add your first product\.' : 'No products match your search\.'/g, "products.length === 0 ? t('noProductsYet') : t('noProductsMatch')");
v = v.replace(/>Name</g, ">{t('nameCol')}<");
v = v.replace(/>HSN\/SAC</g, ">{t('hsnCol')}<");
v = v.replace(/>Rate</g, ">{t('rateCol')}<");
v = v.replace(/>Stock</g, ">{t('stockCol')}<");
v = v.replace(/>Out of Stock</g, ">{t('outOfStock')}<");
v = v.replace(/>Cancel</g, ">{t('cancelModalBtn')}<");
v = v.replace(/\{editingId \? 'Update' : 'Save'\}/g, "{editingId ? t('updateClientModalBtn') : t('saveClientModalBtn')}");

fs.writeFileSync('moolam/pagudhigal/Porul.tsx', v, 'utf-8');
