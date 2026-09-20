const fs = require('fs');
const path = require('path');

const amaippugalPath = path.join(__dirname, '..', 'pagudhigal', 'Amaippugal.tsx');
let content = fs.readFileSync(amaippugalPath, 'utf8');

// Add imports
if (!content.includes('ElvanSettingsSection')) {
  content = content.replace(
    "import { useLanguage } from '../mozhi/LanguageContext';",
    "import { useLanguage } from '../mozhi/LanguageContext';\nimport ElvanSettingsSection from './ElvanSettingsSection';\nimport ElvanBilingualField from './ElvanBilingualField';"
  );
}

// 1. Refactor ElvanSettingsSection
// Find:
// <Paper className="s2-group" elevation={2} sx={{ p: { xs: 2, md: 3 }, mb: { xs: 2, md: 3 }, borderRadius: { xs: 0, sm: 2 }, borderX: { xs: 0, sm: undefined } }} component="form" onSubmit={handleSave} ref={companyFormRef}>
// <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 2 }}>
//   <Typography variant="h6" style={{ fontWeight: 600 }} sx={{ m: 0 }}>
//     {t('companyDetailsTitle')}
//   </Typography>
//   {!isEditingCompany ? ... }
// </Box>
//
// Change to:
// <ElvanSettingsSection
//    title={t('companyDetailsTitle')}
//    action={!isEditingCompany ? ... }
//    sx={{ ... }} // if needed
// >
// Note: This is complex to do purely via regex due to nested tags.
// Let's do a more robust string replacement for the exact headers.

// For now, let's just do it manually via a smart replace function
const sectionsToReplace = [
  {
    regex: /<Paper className="s2-group" elevation=\{2\} sx={{ p: { xs: 2, md: 3 }, mb: { xs: 2, md: 3 }, borderRadius: { xs: 0, sm: 2 }, borderX: { xs: 0, sm: undefined } }} component="form" onSubmit=\{handleSave\} ref=\{companyFormRef\}>\s*<Box sx=\{\{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 2 \}\}>\s*<Typography variant="h6" style=\{\{ fontWeight: 600 \}\} sx=\{\{ m: 0 \}\}>\s*\{t\('companyDetailsTitle'\)\}\s*<\/Typography>([\s\S]*?)<\/Box>/g,
    replace: (match, p1) => {
      return `<ElvanSettingsSection\n        title={t('companyDetailsTitle') as string}\n        action={<>{${p1.trim()}}</>}\n        sx={{ mb: { xs: 2, md: 3 } }}\n      >\n        <form onSubmit={handleSave} ref={companyFormRef}>`;
    },
    closingForm: true
  },
  {
    regex: /<Paper className="s2-group" elevation=\{2\} sx={{ p: { xs: 2, md: 3 }, mb: { xs: 2, md: 3 }, borderRadius: { xs: 0, sm: 2 }, borderX: { xs: 0, sm: undefined } }}>\s*<Box sx=\{\{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 2 \}\}>\s*<Typography variant="h6" style=\{\{ fontWeight: 600 \}\} sx=\{\{ m: 0 \}\}>\s*\{t\('businessProfilesTitle'\)\}\s*<\/Typography>\s*<Box sx=\{\{ display: 'flex', gap: 1 \}\}>([\s\S]*?)<\/Box>\s*<\/Box>/g,
    replace: (match, p1) => {
      return `<ElvanSettingsSection\n        title={t('businessProfilesTitle') as string}\n        action={<>${p1.trim()}</>}\n      >`;
    }
  },
  {
    regex: /<Paper className="s2-group" elevation=\{2\} sx={{ p: { xs: 2, md: 3 }, mb: { xs: 2, md: 3 }, borderRadius: { xs: 0, sm: 2 }, borderX: { xs: 0, sm: undefined } }}>\s*<Box sx=\{\{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 2 \}\}>\s*<Typography variant="h6" style=\{\{ fontWeight: 600 \}\} sx=\{\{ m: 0 \}\}>\s*\{t\('paymentAccountsTitle'\)\}\s*<\/Typography>\s*<Button variant="outlined" onClick=\{openAddAccount\} startIcon=\{<Add sx=\{\{ fontSize: 16 \}\} \/>\}>\s*\{t\('addAccountBtn'\)\}\s*<\/Button>\s*<\/Box>/g,
    replace: (match) => {
      return `<ElvanSettingsSection\n        title={t('paymentAccountsTitle') as string}\n        action={<Button variant="outlined" onClick={openAddAccount} startIcon={<Add sx={{ fontSize: 16 }} />}>{t('addAccountBtn')}</Button>}\n      >`;
    }
  },
  {
    regex: /<Paper className="s2-group" elevation=\{2\} sx={{ p: { xs: 2, md: 3 }, mb: { xs: 2, md: 3 }, borderRadius: { xs: 0, sm: 2 }, borderX: { xs: 0, sm: undefined } }}>\s*<Box sx=\{\{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 2 \}\}>\s*<Typography variant="h6" style=\{\{ fontWeight: 600 \}\} sx=\{\{ m: 0 \}\}>\s*\{t\('backupSyncTitle'\)\}\s*<\/Typography>\s*<\/Box>/g,
    replace: (match) => {
      return `<ElvanSettingsSection\n        title={t('backupSyncTitle') as string}\n      >`;
    }
  },
  {
    regex: /<Paper className="s2-group" elevation=\{2\} sx={{ p: { xs: 2, md: 3 }, mb: { xs: 2, md: 3 }, borderRadius: { xs: 0, sm: 2 }, borderX: { xs: 0, sm: undefined } }}>\s*<Typography variant="h6" style=\{\{ fontWeight: 600 \}\} gutterBottom>\s*\{t\('dataResetTitle'\)\}\s*<\/Typography>/g,
    replace: (match) => {
      return `<ElvanSettingsSection\n        title={t('dataResetTitle') as string}\n      >`;
    }
  },
  {
    regex: /<Paper className="s2-group" elevation=\{2\} sx={{ p: { xs: 2, md: 3 }, mb: { xs: 2, md: 3 }, borderRadius: { xs: 0, sm: 2 }, borderX: { xs: 0, sm: undefined } }}>\s*<Box sx=\{\{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 2 \}\}>\s*<Typography variant="h6" style=\{\{ fontWeight: 600 \}\} sx=\{\{ m: 0 \}\}>\s*\{t\('invoiceNumberFormatTitle'\)\}\s*<\/Typography>\s*<Button variant="contained" onClick=\{handleSaveInvNumSettings\} disabled=\{invNumSaving\} startIcon=\{<Save sx=\{\{ fontSize: 16 \}\} \/>\}>\s*\{invNumSaving \? t\('saving'\) : t\('saveSettings'\)\}\s*<\/Button>\s*<\/Box>/g,
    replace: (match) => {
      return `<ElvanSettingsSection\n        title={t('invoiceNumberFormatTitle') as string}\n        action={<Button variant="contained" onClick={handleSaveInvNumSettings} disabled={invNumSaving} startIcon={<Save sx={{ fontSize: 16 }} />}>{invNumSaving ? t('saving') : t('saveSettings')}</Button>}\n      >`;
    }
  },
  {
    regex: /<Paper className="s2-group" elevation=\{2\} sx={{ p: { xs: 2, md: 3 }, mb: { xs: 2, md: 3 }, borderRadius: { xs: 0, sm: 2 }, borderX: { xs: 0, sm: undefined } }}>\s*<Box sx=\{\{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 2 \}\}>\s*<Typography variant="h6" style=\{\{ fontWeight: 600 \}\} sx=\{\{ m: 0 \}\}>\s*\{t\('receiptNumberFormatTitle'\)\}\s*<\/Typography>\s*<Button variant="contained" onClick=\{handleSaveRcpNumSettings\} disabled=\{rcpNumSaving\} startIcon=\{<Save sx=\{\{ fontSize: 16 \}\} \/>\}>\s*\{rcpNumSaving \? t\('saving'\) : t\('saveSettings'\)\}\s*<\/Button>\s*<\/Box>/g,
    replace: (match) => {
      return `<ElvanSettingsSection\n        title={t('receiptNumberFormatTitle') as string}\n        action={<Button variant="contained" onClick={handleSaveRcpNumSettings} disabled={rcpNumSaving} startIcon={<Save sx={{ fontSize: 16 }} />}>{rcpNumSaving ? t('saving') : t('saveSettings')}</Button>}\n      >`;
    }
  },
  {
    regex: /<Paper className="s2-group" elevation=\{2\} sx={{ p: { xs: 2, md: 3 }, mb: { xs: 2, md: 3 }, borderRadius: { xs: 0, sm: 2 }, borderX: { xs: 0, sm: undefined } }}>\s*<Box sx=\{\{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 2 \}\}>\s*<Typography variant="h6" style=\{\{ fontWeight: 600 \}\} sx=\{\{ m: 0 \}\}>\s*Invoice Terms & Conditions\s*<\/Typography>\s*<Button variant="contained" onClick=\{handleSave\} disabled=\{saving\} startIcon=\{<Save sx=\{\{ fontSize: 16 \}\} \/>\}>\s*\{saving \? t\('saving'\) : t\('saveSettings'\)\}\s*<\/Button>\s*<\/Box>/g,
    replace: (match) => {
      return `<ElvanSettingsSection\n        title="Invoice Terms & Conditions"\n        action={<Button variant="contained" onClick={handleSave} disabled={saving} startIcon={<Save sx={{ fontSize: 16 }} />}>{saving ? t('saving') : t('saveSettings')}</Button>}\n      >`;
    }
  },
  {
    regex: /<Paper className="s2-group" elevation=\{2\} sx={{ p: { xs: 2, md: 3 }, mb: { xs: 2, md: 3 }, borderRadius: { xs: 0, sm: 2 }, borderX: { xs: 0, sm: undefined } }}>\s*<Box sx=\{\{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 2 \}\}>\s*<Typography variant="h6" style=\{\{ fontWeight: 600 \}\} sx=\{\{ m: 0 \}\}>\s*Language \/ மொழி\s*<\/Typography>\s*\{!editingLangSettings \? \(\s*<Button variant="outlined" size="small" onClick=\{\(\) => setEditingLangSettings\(true\)\} startIcon=\{<Edit sx=\{\{ fontSize: 16 \}\} \/>\}>\s*Edit Configuration\s*<\/Button>\s*\) : \(\s*<Box sx=\{\{ display: 'flex', gap: 1 \}\}>\s*<Button variant="outlined" size="small" color="inherit" onClick=\{\(\) => setEditingLangSettings\(false\)\}>\s*Cancel\s*<\/Button>\s*<Button variant="contained" size="small" color="primary" onClick=\{saveLangSettings\} startIcon=\{<Save sx=\{\{ fontSize: 16 \}\} \/>\}>\s*Save Settings\s*<\/Button>\s*<\/Box>\s*\)\}\s*<\/Box>/g,
    replace: (match) => {
      return `<ElvanSettingsSection\n        title="Language / மொழி"\n        action={!editingLangSettings ? (\n            <Button variant="outlined" size="small" onClick={() => setEditingLangSettings(true)} startIcon={<Edit sx={{ fontSize: 16 }} />}>\n              Edit Configuration\n            </Button>\n          ) : (\n            <Box sx={{ display: 'flex', gap: 1 }}>\n              <Button variant="outlined" size="small" color="inherit" onClick={() => setEditingLangSettings(false)}>\n                Cancel\n              </Button>\n              <Button variant="contained" size="small" color="primary" onClick={saveLangSettings} startIcon={<Save sx={{ fontSize: 16 }} />}>\n                Save Settings\n              </Button>\n            </Box>\n          )}\n      >`;
    }
  }
];

sectionsToReplace.forEach(r => {
  content = content.replace(r.regex, r.replace);
});

// Since we opened a <form> for the very first Paper we replaced, we must close it where the first paper ended
content = content.replace(
  /<\/Paper>\s*\{\/\* ---- Multi-Business Profiles ---- \*\/\}/g,
  "        </form>\n      </ElvanSettingsSection>\n\n      {/* ---- Multi-Business Profiles ---- */}"
);

// Close all the other </Paper> elements that we opened as <ElvanSettingsSection>
// We count how many <ElvanSettingsSection> we have and close them safely by relying on the fact that they end with </Paper>.
// But wait, there are other <Paper className="s2-group" variant="outlined"> inside the code. We didn't replace them with ElvanSettingsSection!
// Let's replace the EXACT </Paper> closures for the 9 sections we modified.
// Since we used ElvanSettingsSection, we must change </Paper> to </ElvanSettingsSection> for those specific root papers.
// Actually, it's easier to just do a smart regex:
content = content.replace(
  /<ElvanSettingsSection([\s\S]*?)<\/Paper>/g,
  (match, p1) => {
    // If there is another ElvanSettingsSection inside, this regex fails. But there isn't.
    // If there is another </Paper> inside, this regex captures too little.
    // So let's NOT do regex for closing tags. Let's do it manually via a simple pass.
    return match; // NO-OP, just safety check
  }
);

// Better way to close:
content = content.replace(/<\/Paper>\s*\{currentView === 0 && isMobile && \(/g, "</ElvanSettingsSection>\n      {currentView === 0 && isMobile && (");
content = content.replace(/<\/Paper>\s*\{currentView === 2 && \(/g, "</ElvanSettingsSection>\n      {currentView === 2 && (");
content = content.replace(/<\/Paper>\s*\{currentView === 3 && \(/g, "</ElvanSettingsSection>\n      {currentView === 3 && (");
content = content.replace(/<\/Paper>\s*\{currentView === 4 && \(/g, "</ElvanSettingsSection>\n      {currentView === 4 && (");
content = content.replace(/<\/Paper>\s*\{currentView === 5 && \(/g, "</ElvanSettingsSection>\n      {currentView === 5 && (");
content = content.replace(/<\/Paper>\s*\{currentView === 6 && \(/g, "</ElvanSettingsSection>\n      {currentView === 6 && (");

// For the rest of the <Paper> closures, we can just replace them based on the text following them.
// "Data Management" ->
content = content.replace(/<\/Paper>\s*<Paper className="s2-group" elevation=\{2\} sx=\{\{ p: \{ xs: 2, md: 3 \}/g, "</ElvanSettingsSection>\n\n      <Paper className=\"s2-group\" elevation={2} sx={{ p: { xs: 2, md: 3 }");
content = content.replace(/<\/Paper>\s*<ElvanSettingsSection/g, "</ElvanSettingsSection>\n\n      <ElvanSettingsSection");

// Fix the Bilingual Text fields in Amaippugal.tsx!
// e.g. Business Name
// <Grid size={{ xs: 12, sm: profile.enableBilingual !== false ? 6 : 12 }}>
//   <TextField disabled={!isEditingCompany} required fullWidth size="small" label={`${t('businessNameLabel')} (${profile.primaryDataLanguage || 'Tamil'})`} name="niruvanathinPeyar" value={profile.niruvanathinPeyar} onChange={handleChange} />
// </Grid>
// {profile.enableBilingual !== false && (
//   <Grid size={{ xs: 12, sm: 6 }}>
//     <TextField disabled={!isEditingCompany} fullWidth size="small" label={`Business Name in ${profile.secondaryDataLanguage || 'English'}`} name="niruvanathinPeyarEn" value={profile.niruvanathinPeyarEn || ''} onChange={handleChange} />
//   </Grid>
// )}

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
console.log('Done refactoring Amaippugal.tsx via script!');
