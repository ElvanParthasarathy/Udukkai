const fs = require('fs');
const path = require('path');

const amaippugalPath = path.join(__dirname, '..', 'pagudhigal', 'Amaippugal.tsx');
let content = fs.readFileSync(amaippugalPath, 'utf8');

if (!content.includes('ElvanSettingsSection')) {
  content = content.replace(
    "import { useLanguage } from '../mozhi/LanguageContext';",
    "import { useLanguage } from '../mozhi/LanguageContext';\nimport ElvanSettingsSection from './ElvanSettingsSection';\nimport ElvanBilingualField from './ElvanBilingualField';"
  );
}

// 1. Company Details
content = content.replace(
  /<Paper className="s2-group" elevation=\{2\} sx=\{\{ p: \{ xs: 2, md: 3 \}, mb: \{ xs: 2, md: 3 \}, borderRadius: \{ xs: 0, sm: 2 \}, borderX: \{ xs: 0, sm: undefined \} \}\} component="form" onSubmit=\{handleSave\} ref=\{companyFormRef\}>\s*<Box sx=\{\{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 2 \}\}>\s*<Typography variant="h6" style=\{\{ fontWeight: 600 \}\} sx=\{\{ m: 0 \}\}>\s*\{t\('companyDetailsTitle'\)\}\s*<\/Typography>\s*\{!isEditingCompany \? \(\s*<Button variant="outlined" size="small" onClick=\{\(\) => setIsEditingCompany\(true\)\} startIcon=\{<Edit sx=\{\{ fontSize: 16 \}\} \/>\}>\s*Edit Details\s*<\/Button>\s*\) : \(\s*<Box sx=\{\{ display: 'flex', gap: 1 \}\}>\s*<Button variant="outlined" size="small" color="inherit" onClick=\{handleCancelEditCompany\}>\s*Cancel\s*<\/Button>\s*<Button variant="contained" size="small" color="primary" onClick=\{handleSave\} startIcon=\{<Save sx=\{\{ fontSize: 16 \}\} \/>\}>\s*Save Details\s*<\/Button>\s*<\/Box>\s*\)\}\s*<\/Box>/,
  `<ElvanSettingsSection
        title={t('companyDetailsTitle') as string}
        action={
          <>
            {!isEditingCompany ? (
              <Button variant="outlined" size="small" onClick={() => setIsEditingCompany(true)} startIcon={<Edit sx={{ fontSize: 16 }} />}>
                Edit Details
              </Button>
            ) : (
              <Box sx={{ display: 'flex', gap: 1 }}>
                <Button variant="outlined" size="small" color="inherit" onClick={handleCancelEditCompany}>
                  Cancel
                </Button>
                <Button variant="contained" size="small" color="primary" onClick={handleSave} startIcon={<Save sx={{ fontSize: 16 }} />}>
                  Save Details
                </Button>
              </Box>
            )}
          </>
        }
      >
        <form onSubmit={handleSave} ref={companyFormRef}>`
);

// Close Company Details form and open Multi-Business
content = content.replace(
  /<\/Paper>\s*\{\/\* ---- Multi-Business Profiles ---- \*\/\}/,
  `        </form>\n      </ElvanSettingsSection>\n\n      {/* ---- Multi-Business Profiles ---- */}`
);

// 2. Multi-Business Profiles
content = content.replace(
  /<Paper className="s2-group" elevation=\{2\} sx=\{\{ p: \{ xs: 2, md: 3 \}, mb: \{ xs: 2, md: 3 \}, borderRadius: \{ xs: 0, sm: 2 \}, borderX: \{ xs: 0, sm: undefined \} \}\}>\s*<Box sx=\{\{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 2 \}\}>\s*<Typography variant="h6" style=\{\{ fontWeight: 600 \}\} sx=\{\{ m: 0 \}\}>\s*\{t\('businessProfilesTitle'\)\}\s*<\/Typography>\s*<Box sx=\{\{ display: 'flex', gap: 1 \}\}>\s*<Button variant="outlined" onClick=\{handleAddNewProfile\} startIcon=\{<Add sx=\{\{ fontSize: 16 \}\} \/>\}>\s*\{t\('addNewProfile'\)\}\s*<\/Button>\s*<Button variant="contained" onClick=\{handleSaveAsProfile\} startIcon=\{<Business sx=\{\{ fontSize: 16 \}\} \/>\}>\s*\{t\('saveAsProfile'\)\}\s*<\/Button>\s*<\/Box>\s*<\/Box>/,
  `<ElvanSettingsSection
        title={t('businessProfilesTitle') as string}
        action={
          <>
            <Button variant="outlined" onClick={handleAddNewProfile} startIcon={<Add sx={{ fontSize: 16 }} />}>
              {t('addNewProfile')}
            </Button>
            <Button variant="contained" onClick={handleSaveAsProfile} startIcon={<Business sx={{ fontSize: 16 }} />}>
              {t('saveAsProfile')}
            </Button>
          </>
        }
      >`
);
content = content.replace(/<\/Paper>\s*\{currentView === 0 && isMobile && \(/, `</ElvanSettingsSection>\n      {currentView === 0 && isMobile && (`);


// 3. Payment Accounts
content = content.replace(
  /<Paper className="s2-group" elevation=\{2\} sx=\{\{ p: \{ xs: 2, md: 3 \}, mb: \{ xs: 2, md: 3 \}, borderRadius: \{ xs: 0, sm: 2 \}, borderX: \{ xs: 0, sm: undefined \} \}\}>\s*<Box sx=\{\{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 2 \}\}>\s*<Typography variant="h6" style=\{\{ fontWeight: 600 \}\} sx=\{\{ m: 0 \}\}>\s*\{t\('paymentAccountsTitle'\)\}\s*<\/Typography>\s*<Button variant="outlined" onClick=\{openAddAccount\} startIcon=\{<Add sx=\{\{ fontSize: 16 \}\} \/>\}>\s*\{t\('addAccountBtn'\)\}\s*<\/Button>\s*<\/Box>/,
  `<ElvanSettingsSection
        title={t('paymentAccountsTitle') as string}
        action={
          <Button variant="outlined" onClick={openAddAccount} startIcon={<Add sx={{ fontSize: 16 }} />}>
            {t('addAccountBtn')}
          </Button>
        }
      >`
);
content = content.replace(/<\/Paper>\s*\{currentView === 2 && \(/, `</ElvanSettingsSection>\n      {currentView === 2 && (`);


// 4. Backup & Sync
content = content.replace(
  /<Paper className="s2-group" elevation=\{2\} sx=\{\{ p: \{ xs: 2, md: 3 \}, mb: \{ xs: 2, md: 3 \}, borderRadius: \{ xs: 0, sm: 2 \}, borderX: \{ xs: 0, sm: undefined \} \}\}>\s*<Box sx=\{\{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 2 \}\}>\s*<Typography variant="h6" style=\{\{ fontWeight: 600 \}\} sx=\{\{ m: 0 \}\}>\s*\{t\('backupSyncTitle'\)\}\s*<\/Typography>\s*<\/Box>/,
  `<ElvanSettingsSection title={t('backupSyncTitle') as string}>`
);
content = content.replace(/<\/Paper>\s*<Paper className="s2-group" elevation=\{2\} sx=\{\{ p: \{ xs: 2, md: 3 \}, mb: \{ xs: 2, md: 3 \}, borderRadius: \{ xs: 0, sm: 2 \}, borderX: \{ xs: 0, sm: undefined \} \}\}>\s*<Typography variant="h6"/, `</ElvanSettingsSection>\n\n      <Paper className="s2-group" elevation={2} sx={{ p: { xs: 2, md: 3 }, mb: { xs: 2, md: 3 }, borderRadius: { xs: 0, sm: 2 }, borderX: { xs: 0, sm: undefined } }}>\n        <Typography variant="h6"`);

// 5. Data Reset
content = content.replace(
  /<Paper className="s2-group" elevation=\{2\} sx=\{\{ p: \{ xs: 2, md: 3 \}, mb: \{ xs: 2, md: 3 \}, borderRadius: \{ xs: 0, sm: 2 \}, borderX: \{ xs: 0, sm: undefined \} \}\}>\s*<Typography variant="h6" style=\{\{ fontWeight: 600 \}\} gutterBottom>\s*\{t\('dataResetTitle'\)\}\s*<\/Typography>/,
  `<ElvanSettingsSection title={t('dataResetTitle') as string}>`
);
content = content.replace(/<\/Paper>\s*\{currentView === 3 && \(/, `</ElvanSettingsSection>\n      {currentView === 3 && (`);

// 6. Invoice Format
content = content.replace(
  /<Paper className="s2-group" elevation=\{2\} sx=\{\{ p: \{ xs: 2, md: 3 \}, mb: \{ xs: 2, md: 3 \}, borderRadius: \{ xs: 0, sm: 2 \}, borderX: \{ xs: 0, sm: undefined \} \}\}>\s*<Box sx=\{\{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 2 \}\}>\s*<Typography variant="h6" style=\{\{ fontWeight: 600 \}\} sx=\{\{ m: 0 \}\}>\s*\{t\('invoiceNumberFormatTitle'\)\}\s*<\/Typography>\s*<Button variant="contained" onClick=\{handleSaveInvNumSettings\} disabled=\{invNumSaving\} startIcon=\{<Save sx=\{\{ fontSize: 16 \}\} \/>\}>\s*\{invNumSaving \? t\('saving'\) : t\('saveSettings'\)\}\s*<\/Button>\s*<\/Box>/,
  `<ElvanSettingsSection
        title={t('invoiceNumberFormatTitle') as string}
        action={
          <Button variant="contained" onClick={handleSaveInvNumSettings} disabled={invNumSaving} startIcon={<Save sx={{ fontSize: 16 }} />}>
            {invNumSaving ? t('saving') : t('saveSettings')}
          </Button>
        }
      >`
);
content = content.replace(/<\/Paper>\s*<Paper className="s2-group" elevation=\{2\} sx=\{\{ p: \{ xs: 2, md: 3 \}, mb: \{ xs: 2, md: 3 \}/, `</ElvanSettingsSection>\n\n      <Paper className="s2-group" elevation={2} sx={{ p: { xs: 2, md: 3 }`);


// 7. Receipt Format
content = content.replace(
  /<Paper className="s2-group" elevation=\{2\} sx=\{\{ p: \{ xs: 2, md: 3 \}, mb: \{ xs: 2, md: 3 \}, borderRadius: \{ xs: 0, sm: 2 \}, borderX: \{ xs: 0, sm: undefined \} \}\}>\s*<Box sx=\{\{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 2 \}\}>\s*<Typography variant="h6" style=\{\{ fontWeight: 600 \}\} sx=\{\{ m: 0 \}\}>\s*\{t\('receiptNumberFormatTitle'\)\}\s*<\/Typography>\s*<Button variant="contained" onClick=\{handleSaveRcpNumSettings\} disabled=\{rcpNumSaving\} startIcon=\{<Save sx=\{\{ fontSize: 16 \}\} \/>\}>\s*\{rcpNumSaving \? t\('saving'\) : t\('saveSettings'\)\}\s*<\/Button>\s*<\/Box>/,
  `<ElvanSettingsSection
        title={t('receiptNumberFormatTitle') as string}
        action={
          <Button variant="contained" onClick={handleSaveRcpNumSettings} disabled={rcpNumSaving} startIcon={<Save sx={{ fontSize: 16 }} />}>
            {rcpNumSaving ? t('saving') : t('saveSettings')}
          </Button>
        }
      >`
);
content = content.replace(/<\/Paper>\s*<Paper className="s2-group" elevation=\{2\} sx=\{\{ p: \{ xs: 2, md: 3 \}, mb: \{ xs: 2, md: 3 \}/, `</ElvanSettingsSection>\n\n      <Paper className="s2-group" elevation={2} sx={{ p: { xs: 2, md: 3 }`);

// 8. Terms
content = content.replace(
  /<Paper className="s2-group" elevation=\{2\} sx=\{\{ p: \{ xs: 2, md: 3 \}, mb: \{ xs: 2, md: 3 \}, borderRadius: \{ xs: 0, sm: 2 \}, borderX: \{ xs: 0, sm: undefined \} \}\}>\s*<Box sx=\{\{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 2 \}\}>\s*<Typography variant="h6" style=\{\{ fontWeight: 600 \}\} sx=\{\{ m: 0 \}\}>\s*Invoice Terms & Conditions\s*<\/Typography>\s*<Button variant="contained" onClick=\{handleSave\} disabled=\{saving\} startIcon=\{<Save sx=\{\{ fontSize: 16 \}\} \/>\}>\s*\{saving \? t\('saving'\) : t\('saveSettings'\)\}\s*<\/Button>\s*<\/Box>/,
  `<ElvanSettingsSection
        title="Invoice Terms & Conditions"
        action={
          <Button variant="contained" onClick={handleSave} disabled={saving} startIcon={<Save sx={{ fontSize: 16 }} />}>
            {saving ? t('saving') : t('saveSettings')}
          </Button>
        }
      >`
);
content = content.replace(/<\/Paper>\s*\{currentView === 4 && \(/, `</ElvanSettingsSection>\n      {currentView === 4 && (`);

// 9. Language Config
content = content.replace(
  /<Paper className="s2-group" elevation=\{2\} sx=\{\{ p: \{ xs: 2, md: 3 \}, mb: \{ xs: 2, md: 3 \}, borderRadius: \{ xs: 0, sm: 2 \}, borderX: \{ xs: 0, sm: undefined \} \}\}>\s*<Box sx=\{\{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 2 \}\}>\s*<Typography variant="h6" style=\{\{ fontWeight: 600 \}\} sx=\{\{ m: 0 \}\}>\s*Language \/ மொழி\s*<\/Typography>\s*\{!editingLangSettings \? \(\s*<Button variant="outlined" size="small" onClick=\{\(\) => setEditingLangSettings\(true\)\} startIcon=\{<Edit sx=\{\{ fontSize: 16 \}\} \/>\}>\s*Edit Configuration\s*<\/Button>\s*\) : \(\s*<Box sx=\{\{ display: 'flex', gap: 1 \}\}>\s*<Button variant="outlined" size="small" color="inherit" onClick=\{\(\) => setEditingLangSettings\(false\)\}>\s*Cancel\s*<\/Button>\s*<Button variant="contained" size="small" color="primary" onClick=\{saveLangSettings\} startIcon=\{<Save sx=\{\{ fontSize: 16 \}\} \/>\}>\s*Save Settings\s*<\/Button>\s*<\/Box>\s*\)\}\s*<\/Box>/,
  `<ElvanSettingsSection
        title="Language / மொழி"
        action={
          <>
            {!editingLangSettings ? (
              <Button variant="outlined" size="small" onClick={() => setEditingLangSettings(true)} startIcon={<Edit sx={{ fontSize: 16 }} />}>
                Edit Configuration
              </Button>
            ) : (
              <Box sx={{ display: 'flex', gap: 1 }}>
                <Button variant="outlined" size="small" color="inherit" onClick={() => setEditingLangSettings(false)}>
                  Cancel
                </Button>
                <Button variant="contained" size="small" color="primary" onClick={saveLangSettings} startIcon={<Save sx={{ fontSize: 16 }} />}>
                  Save Settings
                </Button>
              </Box>
            )}
          </>
        }
      >`
);
content = content.replace(/<\/Paper>\s*\{currentView === 5 && \(/, `</ElvanSettingsSection>\n      {currentView === 5 && (`);

// Replace Bilingual inputs
content = content.replace(
  /<Grid size=\{\{ xs: 12, sm: profile\.enableBilingual !== false \? 6 : 12 \}\}>\s*<TextField disabled=\{!isEditingCompany\} required fullWidth size="small" label=\{\`\$\{t\('businessNameLabel'\)\} \(\$\{profile\.primaryDataLanguage \|\| 'Tamil'\}\)\`\} name="niruvanathinPeyar" value=\{profile\.niruvanathinPeyar\} onChange=\{handleChange\} \/>\s*<\/Grid>\s*\{profile\.enableBilingual !== false && \(\s*<Grid size=\{\{ xs: 12, sm: 6 \}\}>\s*<TextField disabled=\{!isEditingCompany\} fullWidth size="small" label=\{\`Business Name in \$\{profile\.secondaryDataLanguage \|\| 'English'\}\`\} name="niruvanathinPeyarEn" value=\{profile\.niruvanathinPeyarEn \|\| ''\} onChange=\{handleChange\} \/>\s*<\/Grid>\s*\)\}/g,
  `<ElvanBilingualField
                  label={t('businessNameLabel') as string}
                  primaryLang={profile.primaryDataLanguage || 'Tamil'}
                  secondaryLang={profile.secondaryDataLanguage || 'English'}
                  isBilingual={profile.enableBilingual !== false}
                  disabled={!isEditingCompany}
                  required
                  primaryValue={profile.niruvanathinPeyar}
                  onPrimaryChange={(e) => handleChange({ target: { name: 'niruvanathinPeyar', value: e.target.value } } as any)}
                  secondaryValue={profile.niruvanathinPeyarEn || ''}
                  onSecondaryChange={(e) => handleChange({ target: { name: 'niruvanathinPeyarEn', value: e.target.value } } as any)}
                />`
);

content = content.replace(
  /<Grid size=\{\{ xs: 12, sm: profile\.enableBilingual !== false \? 6 : 12 \}\}>\s*<TextField disabled=\{!isEditingCompany\} fullWidth multiline rows=\{2\} size="small" label=\{\`\$\{t\('mugavariLabel'\)\} \(\$\{profile\.primaryDataLanguage \|\| 'Tamil'\}\)\`\} name="mugavari" value=\{profile\.mugavari\} onChange=\{handleChange\} \/>\s*<\/Grid>\s*\{profile\.enableBilingual !== false && \(\s*<Grid size=\{\{ xs: 12, sm: 6 \}\}>\s*<TextField disabled=\{!isEditingCompany\} fullWidth multiline rows=\{2\} size="small" label=\{\`Address in \$\{profile\.secondaryDataLanguage \|\| 'English'\}\`\} name="mugavariEn" value=\{profile\.mugavariEn \|\| ''\} onChange=\{handleChange\} \/>\s*<\/Grid>\s*\)\}/g,
  `<ElvanBilingualField
                  label={t('mugavariLabel') as string}
                  primaryLang={profile.primaryDataLanguage || 'Tamil'}
                  secondaryLang={profile.secondaryDataLanguage || 'English'}
                  isBilingual={profile.enableBilingual !== false}
                  disabled={!isEditingCompany}
                  multiline
                  rows={2}
                  primaryValue={profile.mugavari}
                  onPrimaryChange={(e) => handleChange({ target: { name: 'mugavari', value: e.target.value } } as any)}
                  secondaryValue={profile.mugavariEn || ''}
                  onSecondaryChange={(e) => handleChange({ target: { name: 'mugavariEn', value: e.target.value } } as any)}
                />`
);

content = content.replace(
  /<Grid size=\{\{ xs: 12, sm: profile\.enableBilingual !== false \? 6 : 12 \}\}>\s*<TextField disabled=\{!isEditingCompany\} fullWidth size="small" label=\{\`\$\{t\('oorLabel'\)\} \(\$\{profile\.primaryDataLanguage \|\| 'Tamil'\}\)\`\} name="oor" value=\{profile\.oor \|\| ''\} onChange=\{handleChange\} placeholder=\{t\('e\.g\. Mumbai' as any\)\} \/>\s*<\/Grid>\s*\{profile\.enableBilingual !== false && \(\s*<Grid size=\{\{ xs: 12, sm: 6 \}\}>\s*<TextField disabled=\{!isEditingCompany\} fullWidth size="small" label=\{\`City in \$\{profile\.secondaryDataLanguage \|\| 'English'\}\`\} name="oorEn" value=\{profile\.oorEn \|\| ''\} onChange=\{handleChange\} \/>\s*<\/Grid>\s*\)\}/g,
  `<ElvanBilingualField
                  label={t('oorLabel') as string}
                  primaryLang={profile.primaryDataLanguage || 'Tamil'}
                  secondaryLang={profile.secondaryDataLanguage || 'English'}
                  isBilingual={profile.enableBilingual !== false}
                  disabled={!isEditingCompany}
                  placeholder={t('e.g. Mumbai' as any) as string}
                  primaryValue={profile.oor || ''}
                  onPrimaryChange={(e) => handleChange({ target: { name: 'oor', value: e.target.value } } as any)}
                  secondaryValue={profile.oorEn || ''}
                  onSecondaryChange={(e) => handleChange({ target: { name: 'oorEn', value: e.target.value } } as any)}
                />`
);

content = content.replace(
  /<Grid size=\{\{ xs: 12, sm: profile\.enableBilingual !== false \? 6 : 12 \}\}>\s*<TextField disabled=\{!isEditingCompany\} fullWidth size="small" label=\{\`மாவட்டம் \(District\) \(\$\{profile\.primaryDataLanguage \|\| 'Tamil'\}\)\`\} name="maavattam" value=\{profile\.maavattam \|\| ''\} onChange=\{handleChange\} placeholder=\{\`மாவட்டம்\`\} \/>\s*<\/Grid>\s*\{profile\.enableBilingual !== false && \(\s*<Grid size=\{\{ xs: 12, sm: 6 \}\}>\s*<TextField disabled=\{!isEditingCompany\} fullWidth size="small" label=\{\`District in \$\{profile\.secondaryDataLanguage \|\| 'English'\}\`\} name="maavattamEn" value=\{profile\.maavattamEn \|\| ''\} onChange=\{handleChange\} placeholder=\{\`District\`\} \/>\s*<\/Grid>\s*\)\}/g,
  `<ElvanBilingualField
                  label="மாவட்டம் (District)"
                  primaryLang={profile.primaryDataLanguage || 'Tamil'}
                  secondaryLang={profile.secondaryDataLanguage || 'English'}
                  isBilingual={profile.enableBilingual !== false}
                  disabled={!isEditingCompany}
                  placeholder="மாவட்டம்"
                  primaryValue={profile.maavattam || ''}
                  onPrimaryChange={(e) => handleChange({ target: { name: 'maavattam', value: e.target.value } } as any)}
                  secondaryValue={profile.maavattamEn || ''}
                  onSecondaryChange={(e) => handleChange({ target: { name: 'maavattamEn', value: e.target.value } } as any)}
                />`
);

fs.writeFileSync(amaippugalPath, content);
console.log('Script completed successfully!');
