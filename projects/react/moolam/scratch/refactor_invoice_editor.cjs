const fs = require('fs');
const path = require('path');

const file = path.join(__dirname, '../pagudhigal/invoice/InvoiceEditor.tsx');
let content = fs.readFileSync(file, 'utf8');

// Insert helpers at the top of InvoiceEditor
const helperCode = `
  const primaryLang = profile?.primaryDataLanguage || 'Tamil';
  const secondaryLang = profile?.secondaryDataLanguage || 'English';
  const enableBilingual = profile?.enableBilingual !== false;

  const clientFields = ['name', 'mugavari', 'oor', 'maavattam', 'maanilam', 'country'];
  const itemFields = ['name', 'description'];

  const convertFromSnapshot = (obj, fields) => {
    if (!obj) return {};
    const result = { ...obj };
    fields.forEach(f => {
       if (result[f] !== undefined) result[\`\${f}_\${primaryLang}\`] = result[f];
       if (result[\`\${f}En\`] !== undefined) result[\`\${f}_\${secondaryLang}\`] = result[\`\${f}En\`];
       delete result[f];
       delete result[\`\${f}En\`];
    });
    return result;
  };

  const convertToSnapshot = (obj, fields) => {
    if (!obj) return {};
    const result = { ...obj };
    fields.forEach(f => {
       result[f] = result[\`\${f}_\${primaryLang}\`] || '';
       result[\`\${f}En\`] = result[\`\${f}_\${secondaryLang}\`] || '';
    });
    return result;
  };

  const getClientField = (f, l) => client[\`\${f}_\${l}\`] || '';
  const setClientField = (f, l, v) => setClient(prev => ({...prev, [\`\${f}_\${l}\`]: v}));
  
  const getItemField = (item, f, l) => item[\`\${f}_\${l}\`] || '';
`;

// Insert after "const profile = IS_TESTING_MODE ? ..."
content = content.replace(
  /const profile = IS_TESTING_MODE \? \{[\s\S]*?\} : \(activeProfile \|\| profileProp\);/,
  match => match + '\n' + helperCode
);

// Replace state initializations
content = content.replace(
  /const \[client, setClient\] = useState\(draft\?.client \|\| \(IS_TESTING_MODE \? TEST_CLIENT : \{.*?\}\)\);/,
  `const [client, setClient] = useState(() => convertFromSnapshot(draft?.client || (IS_TESTING_MODE ? TEST_CLIENT : { name: '', nameEn: '', mugavari: '', mugavariEn: '', oor: '', oorEn: '', maavattam: '', maavattamEn: '', pin: '', maanilam: '', maanilamEn: '', gstin: '', country: '', countryEn: '' }), clientFields));`
);

content = content.replace(
  /const \[items, setItems\] = useState\(draft\?.items \|\| \(IS_TESTING_MODE \? TEST_ITEMS\.map\(i => \(\{\.\.\.i, isTemp: true\}\)\) : \[\s*\{ id: Date\.now\(\)\.toString\(\), name: '', nameEn: '', hsn: '50072010', quantity: 1, unit: 'Nos', rate: 0, discount: 0, taxPercent: 5, cessPercent: 0, isTemp: false \}\s*\]\)\);/,
  `const [items, setItems] = useState(() => {
    const defaultItems = IS_TESTING_MODE ? TEST_ITEMS.map(i => ({...i, isTemp: true})) : [ { id: Date.now().toString(), name: '', nameEn: '', hsn: '50072010', quantity: 1, unit: 'Nos', rate: 0, discount: 0, taxPercent: 5, cessPercent: 0, isTemp: false } ];
    return (draft?.items || defaultItems).map(i => convertFromSnapshot(i, itemFields));
  });`
);

// Fix useEffect mapping for editingBill
content = content.replace(
  /setClient\(d\.client\);/g,
  `setClient(convertFromSnapshot(d.client, clientFields));`
);

content = content.replace(
  /setItems\(d\.items\.map\(i => \(\{ \.\.\.i, isTemp: i\.isTemp \?\? \(i\.productId \? false : true\) \}\)\)\);/g,
  `setItems(d.items.map(i => convertFromSnapshot({ ...i, isTemp: i.isTemp ?? (i.productId ? false : true) }, itemFields)));`
);

// Fix selectProduct
content = content.replace(
  /name: product\.name,\s*nameEn: product\.nameEn \|\| '',\s*description: product\.description \|\| '',\s*descriptionEn: product\.descriptionEn \|\| '',/g,
  `[\`name_\${primaryLang}\`]: product.name,
      [\`name_\${secondaryLang}\`]: product.nameEn || '',
      [\`description_\${primaryLang}\`]: product.description || '',
      [\`description_\${secondaryLang}\`]: product.descriptionEn || '',`
);

// Fix getProductSuggestions query
content = content.replace(
  /p\.name\?\.toLowerCase\(\)\.includes\(q\) \|\| p\.hsn\?\.toLowerCase\(\)\.includes\(q\)/g,
  `(p.name?.toLowerCase().includes(q) || p.nameEn?.toLowerCase().includes(q) || p.hsn?.toLowerCase().includes(q))`
);

// Fix selectSavedClient
content = content.replace(
  /setClient\(\{ name: cli\.name, nameEn: cli\.nameEn \|\| '', mugavari: cli\.mugavari \|\| '', mugavariEn: cli\.mugavariEn \|\| '', oor: cli\.oor \|\| '', oorEn: cli\.oorEn \|\| '', maavattam: cli\.maavattam \|\| '', maavattamEn: cli\.maavattamEn \|\| '', pin: cli\.pin \|\| '', maanilam: cli\.maanilam \|\| '', maanilamEn: cli\.maanilamEn \|\| '', gstin: cli\.gstin \|\| '', country: cli\.country \|\| '', countryEn: cli\.countryEn \|\| '' \}\);/,
  `setClient(convertFromSnapshot(cli, clientFields));`
);

// Fix handleClientModalSave updates
content = content.replace(
  /setClient\(\{ name: data\.name, nameEn: data\.nameEn \|\| '', mugavari: data\.mugavari, mugavariEn: data\.mugavariEn \|\| '', oor: data\.oor \|\| '', oorEn: data\.oorEn \|\| '', maavattam: data\.maavattam \|\| '', maavattamEn: data\.maavattamEn \|\| '', pin: data\.pin \|\| '', maanilam: data\.maanilam, maanilamEn: data\.maanilamEn \|\| '', gstin: data\.gstin, country: data\.country \|\| '', countryEn: data\.countryEn \|\| '' \}\);/,
  `setClient(convertFromSnapshot(data, clientFields));`
);

// Fix saveInvoiceToDB
content = content.replace(
  /const bill = \{\s*id: details\.invoiceNumber,\s*clientName: client\.name,/g,
  `const snapClient = convertToSnapshot(client, clientFields);
    const snapItems = items.map(i => convertToSnapshot(i, itemFields));
    const bill = {
      id: details.invoiceNumber,
      clientName: snapClient.name,`
);
content = content.replace(
  /data: \{ profile, client, details, items, totals, invoiceType, customTerms, internalNote, invoiceOptions \}/g,
  `data: { profile, client: snapClient, details, items: snapItems, totals, invoiceType, customTerms, internalNote, invoiceOptions }`
);


const uiReplacements = [
  // isMeaningfulInvoice
  [/client\?\.name/g, "getClientField('name', primaryLang)"],
  [/item\.name/g, "getItemField(item, 'name', primaryLang)"],

  // modalClient init
  [/\{ name: client\.name \|\| '', mugavari: client\.mugavari \|\| '', oor: client\.oor \|\| '', pin: client\.pin \|\| '', maanilam: client\.maanilam \|\| '', gstin: client\.gstin \|\| '' \}/g, 
   `{ name: getClientField('name', primaryLang), nameEn: getClientField('name', secondaryLang), mugavari: getClientField('mugavari', primaryLang), mugavariEn: getClientField('mugavari', secondaryLang), oor: getClientField('oor', primaryLang), oorEn: getClientField('oor', secondaryLang), maavattam: getClientField('maavattam', primaryLang), maavattamEn: getClientField('maavattam', secondaryLang), pin: client.pin || '', maanilam: client.maanilam || '', maanilamEn: client.maanilamEn || '', gstin: client.gstin || '', country: client.country || '', countryEn: client.countryEn || '' }`],

  // filteredClients
  [/client\.name\.trim\(\)/g, "getClientField('name', primaryLang).trim()"],

  // TextField for temp client name
  [/\<TextField fullWidth size="small" value=\{client\.name\} inputRef=\{clientNameRef\}\s*onChange=\{\(e\) => \{\s*const newName = e\.target\.value;\s*if \(\!isTempClient\) \{\s*setClient\(\{ \s*name: newName, nameEn: '', mugavari: '', mugavariEn: '', \s*oor: '', oorEn: '', maavattam: '', maavattamEn: '', \s*pin: '', maanilam: '', maanilamEn: '', gstin: '', \s*country: '', countryEn: '' \s*\}\);\s*setSelectedClientId\(null\);\s*setShowClientSuggestions\(true\);\s*\} else \{\s*setClient\(\{ \.\.\.client, name: newName \}\);\s*\}\s*\}\}/g, 
  `<TextField fullWidth size="small" value={getClientField('name', primaryLang)} inputRef={clientNameRef}
                    onChange={(e) => {
                      const newName = e.target.value;
                      if (!isTempClient) {
                        setClient({ [\`name_\${primaryLang}\`]: newName });
                        setSelectedClientId(null);
                        setShowClientSuggestions(true);
                      } else {
                        setClientField('name', primaryLang, newName);
                      }
                    }}`],

  [/<TextField fullWidth size="small" value=\{client\.nameEn \|\| ''\}\s*onChange=\{\(e\) => setClient\(\{ \.\.\.client, nameEn: e\.target\.value \}\)\}/g,
   `<TextField fullWidth size="small" value={getClientField('name', secondaryLang)}
                      onChange={(e) => setClientField('name', secondaryLang, e.target.value)}`],

  // No saved clients text
  [/client\.name\.trim\(\) \? "No saved clients found\." : "Type to search clients"/g,
   "getClientField('name', primaryLang).trim() ? \"No saved clients found.\" : \"Type to search clients\""],

  // En fields MUST be replaced before base fields to avoid nested replacement errors!
  [/client\.mugavariEn/g, "getClientField('mugavari', secondaryLang)"],
  [/client\.oorEn/g, "getClientField('oor', secondaryLang)"],
  [/client\.maavattamEn/g, "getClientField('maavattam', secondaryLang)"],
  [/client\.nameEn/g, "getClientField('name', secondaryLang)"],
  
  // Now base fields
  [/client\.mugavari/g, "getClientField('mugavari', primaryLang)"],
  [/client\.oor/g, "getClientField('oor', primaryLang)"],
  [/client\.maavattam/g, "getClientField('maavattam', primaryLang)"],

  // For setting mugavari, oor, maavattam
  [/setClient\(\{ \.\.\.client, mugavariEn: e\.target\.value \}\)/g, "setClientField('mugavari', secondaryLang, e.target.value)"],
  [/setClient\(\{ \.\.\.client, oorEn: e\.target\.value \}\)/g, "setClientField('oor', secondaryLang, e.target.value)"],
  [/setClient\(\{ \.\.\.client, maavattamEn: e\.target\.value \}\)/g, "setClientField('maavattam', secondaryLang, e.target.value)"],

  [/setClient\(\{ \.\.\.client, mugavari: e\.target\.value \}\)/g, "setClientField('mugavari', primaryLang, e.target.value)"],
  [/setClient\(\{ \.\.\.client, oor: e\.target\.value \}\)/g, "setClientField('oor', primaryLang, e.target.value)"],
  [/setClient\(\{ \.\.\.client, maavattam: e\.target\.value \}\)/g, "setClientField('maavattam', primaryLang, e.target.value)"],

  // Items
  // item.name -> getItemField(item, 'name', primaryLang)
  [/value=\{item\.name\}\s*onChange=\{\(e\) => handleItemChange\(item\.id, 'name', e\.target\.value\)\}/g,
   `value={getItemField(item, 'name', primaryLang)} onChange={(e) => handleItemChange(item.id, \`name_\${primaryLang}\`, e.target.value)}`],

  // handleItemChange inside product selection fallback
  [/handleItemChange\(item\.id, 'nameEn', ''\);/g, "handleItemChange(item.id, \`name_\${secondaryLang}\`, '');"],
  [/handleItemChange\(item\.id, 'name', ''\);/g, "handleItemChange(item.id, \`name_\${primaryLang}\`, '');"],

  [/value=\{item\.nameEn \|\| ''\}\s*onChange=\{\(e\) => handleItemChange\(item\.id, 'nameEn', e\.target\.value\)\}/g,
   `value={getItemField(item, 'name', secondaryLang)} onChange={(e) => handleItemChange(item.id, \`name_\${secondaryLang}\`, e.target.value)}`],

  [/item\.name \? \{ name: item\.name \} : null/g, "getItemField(item, 'name', primaryLang) ? { name: getItemField(item, 'name', primaryLang) } : null"],
  [/item\.nameEn/g, "getItemField(item, 'name', secondaryLang)"],
];

uiReplacements.forEach(([regex, repl]) => {
  content = content.replace(regex, repl);
});

fs.writeFileSync(file, content);
console.log('Refactored InvoiceEditor.tsx successfully.');
