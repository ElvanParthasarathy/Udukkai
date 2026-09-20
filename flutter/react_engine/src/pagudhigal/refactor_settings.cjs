const fs = require('fs');

function refactorFile(filePath) {
  let content = fs.readFileSync(filePath, 'utf-8');

  // Add import if missing
  if (!content.includes('SettingsSection')) {
    content = content.replace(
      /import \{ getCountryConfig/,
      "import { SettingsSection, SettingsRow } from './ElvanSettingsSection';\nimport { getCountryConfig"
    );
  }

  // Replace Top Profile Switcher
  // It's a Paper with Business icon
  content = content.replace(
    /<Paper className="s2-group" elevation={2} sx=\{\{ p: \{ xs: 2, md: 3 \}, mb: \{ xs: 2, md: 3 \}, borderRadius: \{ xs: 0, sm: 2 \}, borderX: \{ xs: 0, sm: undefined \}, display: 'flex', alignItems: 'center', gap: 2, flexWrap: 'wrap' \}\}>/g,
    '<SettingsSection sx={{ p: 2, display: "flex", alignItems: "center", gap: 2, flexWrap: "wrap" }}>'
  );

  // Replace standard Papers with SettingsSection
  content = content.replace(
    /<Paper className="s2-group" elevation=\{2\} sx=\{\{ p: \{ xs: 2, md: 3 \}, mb: \{ xs: 2, md: 3 \}, borderRadius: \{ xs: 0, sm: 2 \}, borderX: \{ xs: 0, sm: undefined \} \}\}([^>]*)>/g,
    '<SettingsSection$1>'
  );

  // Remove the old Paper closing tags that correspond to SettingsSection
  // We'll replace </Paper> with </SettingsSection> if it matches the sections we converted.
  // This is tricky with regex, so let's do a simple count or manual block replacement.
  
  // Actually, replacing <Paper> with <SettingsSection> everywhere might be easier if we just map them.
  // Let's just write the changes out.
  return content;
}

const f1 = 'd:/Projects/Elvan Niril/moolam/pagudhigal/GstSettings.tsx';
let c1 = refactorFile(f1);
// Replace </Paper> tags that match the sections. Since there are some inner Papers (like for language selector), we should be careful.
c1 = c1.replace(/<\/Paper>\s*\{appMode !== 'COOLIE' && \(/g, '</SettingsSection>\n      {appMode !== \'COOLIE\' && (');
c1 = c1.replace(/<\/Paper>\s*<\/>\)}/g, '</SettingsSection>\n      </>)}');
// It's too error prone. Let's do it manually via file rewrite.
fs.writeFileSync('d:/Projects/Elvan Niril/moolam/pagudhigal/GstSettings2.tsx', c1);
