const fs = require('fs');
const path = require('path');

const dataDir = path.resolve(__dirname, '..', 'tharavu', 'pattiyalkal');

if (!fs.existsSync(dataDir)) {
  console.log(`No directory found at ${dataDir}`);
  process.exit(0);
}

const files = fs.readdirSync(dataDir).filter(f => f.endsWith('.json'));
let migratedCount = 0;

for (const file of files) {
  const filePath = path.join(dataDir, file);
    try {
      const raw = fs.readFileSync(filePath, 'utf-8');
      const bill = JSON.parse(raw);
      let changed = false;

      // Migrate options
      if (bill.data && bill.data.invoiceOptions) {
        if (bill.data.invoiceOptions.showDiscount !== undefined) {
          bill.data.invoiceOptions.showDiscountColumn = bill.data.invoiceOptions.showDiscount;
          delete bill.data.invoiceOptions.showDiscount;
          changed = true;
        }
      }

      // Helper to migrate bilingual fields
      const migrateBilingual = (obj, fields) => {
        for (const field of fields) {
          // If the field exists but it's not an object (meaning it's the old flat string format)
          if (obj[field] !== undefined && typeof obj[field] !== 'object') {
            const primary = obj[`${field}_Tamil`] || obj[field] || '';
            const secondary = obj[`${field}_English`] || obj[`${field}En`] || '';
            
            // Reassign as object
            obj[field] = { primary, secondary };
            changed = true;
          }
          
          // Clean up old flat fields to enforce the new format strictly
          delete obj[`${field}En`];
          delete obj[`${field}_Tamil`];
          delete obj[`${field}_English`];
        }
      };

      // Migrate client
      if (bill.data && bill.data.client) {
        migrateBilingual(bill.data.client, ['name', 'mugavari', 'oor', 'maavattam', 'maanilam', 'country']);
      }

      // Migrate items
      if (bill.data && Array.isArray(bill.data.items)) {
        for (const item of bill.data.items) {
          if (item.quantity !== undefined) {
            item.qty = item.quantity;
            delete item.quantity;
            changed = true;
          }
          
          if (!item.discountType) {
            item.discountType = 'amount';
            changed = true;
          }

          migrateBilingual(item, ['name', 'description']);
        }
      }

      if (changed) {
        fs.writeFileSync(filePath, JSON.stringify(bill, null, 2));
        console.log(`Migrated: ${file}`);
        migratedCount++;
      }

    } catch (e) {
      console.error(`Error migrating ${filePath}:`, e);
    }
}

console.log(`Migration complete. Updated ${migratedCount} files.`);
