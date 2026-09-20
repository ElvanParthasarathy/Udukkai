const fs = require('fs');
const path = require('path');

const targetPath = path.join(__dirname, 'InvoiceEditor.tsx');
let content = fs.readFileSync(targetPath, 'utf8');

if (!content.includes('ElvanBilingualField')) {
  content = content.replace(
    "import ElvanCard from '../ElvanCard';",
    "import ElvanCard from '../ElvanCard';\nimport ElvanBilingualField from '../ElvanBilingualField';"
  );
}

// 1. Mugavari (Address)
content = content.replace(
  /<TextField fullWidth size="small" label=\{\`\$\{t\('billingAddress'\)\}\$\{profile\?\.enableBilingual !== false \? \` \(\$\{profile\?\.primaryDataLanguage \|\| 'Tamil'\}\)\` : ''\}\`\} slotProps=\{\{ inputLabel: \{ shrink: true \} \}\}\s*value=\{getClientField\('mugavari', primaryLang\)\} onChange=\{e => setClientField\('mugavari', primaryLang, e\.target\.value\)\} placeholder="e\.g\. 123 Main St" \/>\s*\{profile\?\.enableBilingual !== false && \(\s*<TextField fullWidth size="small" label=\{\`\$\{t\('billingAddress'\)\} \(\$\{profile\?\.secondaryDataLanguage \|\| 'English'\}\)\`\} slotProps=\{\{ inputLabel: \{ shrink: true \} \}\}\s*value=\{getClientField\('mugavari', secondaryLang\)\} onChange=\{e => setClientField\('mugavari', secondaryLang, e\.target\.value\)\} placeholder="e\.g\. 123 Main St" \/>\s*\)\}/g,
  `<ElvanBilingualField
                      label={t('billingAddress') as string}
                      primaryLang={primaryLang}
                      secondaryLang={secondaryLang}
                      isBilingual={profile?.enableBilingual !== false}
                      primaryValue={getClientField('mugavari', primaryLang)}
                      onPrimaryChange={e => setClientField('mugavari', primaryLang, e.target.value)}
                      secondaryValue={getClientField('mugavari', secondaryLang)}
                      onSecondaryChange={e => setClientField('mugavari', secondaryLang, e.target.value)}
                      placeholder="e.g. 123 Main St"
                    />`
);

// 2. Oor (City)
content = content.replace(
  /<TextField fullWidth size="small" label=\{\`\$\{t\('city'\)\}\$\{profile\?\.enableBilingual !== false \? \` \(\$\{profile\?\.primaryDataLanguage \|\| 'Tamil'\}\)\` : ''\}\`\} slotProps=\{\{ inputLabel: \{ shrink: true \} \}\}\s*value=\{getClientField\('oor', primaryLang\)\} onChange=\{e => setClientField\('oor', primaryLang, e\.target\.value\)\} placeholder="e\.g\. Chennai" \/>\s*\{profile\?\.enableBilingual !== false && \(\s*<TextField fullWidth size="small" label=\{\`\$\{t\('city'\)\} \(\$\{profile\?\.secondaryDataLanguage \|\| 'English'\}\)\`\} slotProps=\{\{ inputLabel: \{ shrink: true \} \}\}\s*value=\{getClientField\('oor', secondaryLang\)\} onChange=\{e => setClientField\('oor', secondaryLang, e\.target\.value\)\} placeholder="e\.g\. Chennai" \/>\s*\)\}/g,
  `<ElvanBilingualField
                      label={t('city') as string}
                      primaryLang={primaryLang}
                      secondaryLang={secondaryLang}
                      isBilingual={profile?.enableBilingual !== false}
                      primaryValue={getClientField('oor', primaryLang)}
                      onPrimaryChange={e => setClientField('oor', primaryLang, e.target.value)}
                      secondaryValue={getClientField('oor', secondaryLang)}
                      onSecondaryChange={e => setClientField('oor', secondaryLang, e.target.value)}
                      placeholder="e.g. Chennai"
                    />`
);

// 3. Maavattam (District)
content = content.replace(
  /<TextField fullWidth size="small" label=\{\`மாவட்டம் \(District\)\$\{profile\?\.enableBilingual !== false \? \` \(\$\{profile\?\.primaryDataLanguage \|\| 'Tamil'\}\)\` : ''\}\`\} slotProps=\{\{ inputLabel: \{ shrink: true \} \}\}\s*value=\{getClientField\('maavattam', primaryLang\)\} onChange=\{e => setClientField\('maavattam', primaryLang, e\.target\.value\)\} \/>\s*\{profile\?\.enableBilingual !== false && \(\s*<TextField fullWidth size="small" label=\{\`District \(\$\{profile\?\.secondaryDataLanguage \|\| 'English'\}\)\`\} slotProps=\{\{ inputLabel: \{ shrink: true \} \}\}\s*value=\{getClientField\('maavattam', secondaryLang\)\} onChange=\{e => setClientField\('maavattam', secondaryLang, e\.target\.value\)\} \/>\s*\)\}/g,
  `<ElvanBilingualField
                      label="மாவட்டம் (District)"
                      primaryLang={primaryLang}
                      secondaryLang={secondaryLang}
                      isBilingual={profile?.enableBilingual !== false}
                      primaryValue={getClientField('maavattam', primaryLang)}
                      onPrimaryChange={e => setClientField('maavattam', primaryLang, e.target.value)}
                      secondaryValue={getClientField('maavattam', secondaryLang)}
                      onSecondaryChange={e => setClientField('maavattam', secondaryLang, e.target.value)}
                    />`
);

// 4. Custom Country
content = content.replace(
  /<TextField fullWidth size="small" label=\{\`Custom Country\$\{profile\?\.enableBilingual !== false \? \` \(\$\{profile\?\.primaryDataLanguage \|\| 'Tamil'\}\)\` : ''\}\`\} slotProps=\{\{ inputLabel: \{ shrink: true \} \}\}\s*value=\{getClientField\('country', primaryLang\)\} onChange=\{e => setClientField\('country', primaryLang, e\.target\.value\)\} placeholder="Enter custom country name" \/>\s*\{profile\?\.enableBilingual !== false && \(\s*<Box>\s*<Box sx=\{\{ height: 40, mb: 2 \}\} \/>\s*<TextField fullWidth size="small" disabled=\{!isCustomCountry\} \s*label=\{\`\$\{t\('country'\)\} \(\$\{profile\?\.secondaryDataLanguage \|\| 'English'\}\)\`\} slotProps=\{\{ inputLabel: \{ shrink: true \} \}\}\s*value=\{isCustomCountry \? getClientField\('country', secondaryLang\) : \(clientCountry \? getBilingualCountryName\(clientCountry, \{ \.\.\.profile, returnOnlySecondary: true \}\) : ''\)\}\s*onChange=\{e => isCustomCountry \? setClientField\('country', secondaryLang, e\.target\.value\) : null\}\s*sx=\{!isCustomCountry \? \{ '& \.MuiInputBase-root': \{ bgcolor: 'action\.hover' \} \} : \{\}\}\s*placeholder=\{t\('country'\) as string\} \/>\s*<\/Box>\s*\)\}/g,
  `<ElvanBilingualField
                                label="Custom Country"
                                primaryLang={primaryLang}
                                secondaryLang={secondaryLang}
                                isBilingual={profile?.enableBilingual !== false}
                                disabled={!isCustomCountry}
                                primaryValue={getClientField('country', primaryLang)}
                                onPrimaryChange={e => setClientField('country', primaryLang, e.target.value)}
                                secondaryValue={isCustomCountry ? getClientField('country', secondaryLang) : (clientCountry ? getBilingualCountryName(clientCountry, { ...profile, returnOnlySecondary: true }) : '')}
                                onSecondaryChange={e => isCustomCountry ? setClientField('country', secondaryLang, e.target.value) : null}
                                placeholder="Enter custom country name"
                              />`
);

// 5. Place of Supply
content = content.replace(
  /<TextField fullWidth size="small" label=\{\`\$\{t\('placeOfSupply'\)\}\$\{profile\?\.enableBilingual !== false \? \` \(\$\{profile\?\.primaryDataLanguage \|\| 'Tamil'\}\)\` : ''\}\`\} slotProps=\{\{ inputLabel: \{ shrink: true \} \}\}\s*value=\{details\.placeOfSupply\} onChange=\{e => setDetails\(prev => \(\{ \.\.\.prev, placeOfSupply: e\.target\.value \}\)\)\} placeholder=\{t\('stateNameOnlyPlaceholder'\) as string\} \/>\s*\{profile\?\.enableBilingual !== false && \(\s*<TextField fullWidth size="small" label=\{\`\$\{t\('placeOfSupply'\)\} \(\$\{profile\?\.secondaryDataLanguage \|\| 'English'\}\)\`\} slotProps=\{\{ inputLabel: \{ shrink: true \} \}\}\s*value=\{getBilingualStateName\(details\.placeOfSupply, \{ \.\.\.profile, returnOnlySecondary: true \}\)\} disabled\s*sx=\{\{ '& \.MuiInputBase-root': \{ bgcolor: 'action\.hover' \} \}\} \/>\s*\)\}/g,
  `<ElvanBilingualField
                          label={t('placeOfSupply') as string}
                          primaryLang={primaryLang}
                          secondaryLang={secondaryLang}
                          isBilingual={profile?.enableBilingual !== false}
                          primaryValue={details.placeOfSupply}
                          onPrimaryChange={e => setDetails(prev => ({ ...prev, placeOfSupply: e.target.value }))}
                          secondaryValue={getBilingualStateName(details.placeOfSupply, { ...profile, returnOnlySecondary: true }) as string}
                          placeholder={t('stateNameOnlyPlaceholder') as string}
                          renderSecondary={(lang, suffix) => (
                            <TextField fullWidth size="small" label={\`\${t('placeOfSupply')} \${suffix}\`} slotProps={{ inputLabel: { shrink: true } }}
                              value={getBilingualStateName(details.placeOfSupply, { ...profile, returnOnlySecondary: true })} disabled
                              sx={{ '& .MuiInputBase-root': { bgcolor: 'action.hover' } }} />
                          )}
                        />`
);

// 6. Item Description
content = content.replace(
  /<TextField fullWidth size="small" label=\{t\("descriptionCol"\)\} slotProps=\{\{ inputLabel: \{ shrink: true \} \}\}\s*value=\{getItemField\(item, 'description', primaryLang\) \|\| getItemField\(item, 'name', primaryLang\) \|\| ''\}\s*onChange=\{e => handleItemChange\(item\.id, \`description_\$\{primaryLang\}\`, e\.target\.value\)\}\s*\/>\s*\{enableBilingual && \(\s*<TextField fullWidth size="small" value=\{getItemField\(item, 'description', secondaryLang\) \|\| getItemField\(item, 'name', secondaryLang\) \|\| ''\}\s*onChange=\{e => handleItemChange\(item\.id, \`description_\$\{secondaryLang\}\`, e\.target\.value\)\}\s*placeholder=\{\`\$\{t\("descriptionCol"\)\} \(\$\{secondaryLang\}\)\`\} \/>\s*\)\}/g,
  `<ElvanBilingualField
                        label={t("descriptionCol") as string}
                        primaryLang={primaryLang}
                        secondaryLang={secondaryLang}
                        isBilingual={enableBilingual}
                        primaryValue={getItemField(item, 'description', primaryLang) || getItemField(item, 'name', primaryLang) || ''}
                        onPrimaryChange={e => handleItemChange(item.id, \`description_\${primaryLang}\`, e.target.value)}
                        secondaryValue={getItemField(item, 'description', secondaryLang) || getItemField(item, 'name', secondaryLang) || ''}
                        onSecondaryChange={e => handleItemChange(item.id, \`description_\${secondaryLang}\`, e.target.value)}
                      />`
);

fs.writeFileSync(targetPath, content);
console.log('InvoiceEditor refactored!');
