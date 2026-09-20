const fs = require('fs');

// 1. Update Avanam.ts
let avanam = fs.readFileSync('Avanam.ts', 'utf8');
const blankCheck = `const isBlankState = () => typeof window !== 'undefined' && window.sessionStorage.getItem('__DEV_BLANK_STATE__') === 'true';\n\n`;

if (!avanam.includes('isBlankState()')) {
    // Insert helper at the top after imports
    avanam = avanam.replace('const getLangConfig =', blankCheck + 'const getLangConfig =');

    // Intercept getters
    avanam = avanam.replace(
        'export const getAllBills = async () => {', 
        'export const getAllBills = async () => {\n  if (isBlankState()) return [];'
    );
    avanam = avanam.replace(
        'export const getProfile = async () => {', 
        'export const getProfile = async () => {\n  if (isBlankState()) return {};'
    );
    avanam = avanam.replace(
        'export const getAllClients = async () => {', 
        'export const getAllClients = async () => {\n  if (isBlankState()) return [];'
    );
    avanam = avanam.replace(
        'export const getAllProducts = async () => {', 
        'export const getAllProducts = async () => {\n  if (isBlankState()) return [];'
    );
    avanam = avanam.replace(
        'export const getAllReceipts = async () => {', 
        'export const getAllReceipts = async () => {\n  if (isBlankState()) return [];'
    );
    avanam = avanam.replace(
        'export const getAllProfiles = async () => {', 
        'export const getAllProfiles = async () => {\n  if (isBlankState()) return [];'
    );

    fs.writeFileSync('Avanam.ts', avanam);
}

// 2. Update Seyali.tsx
let seyali = fs.readFileSync('Seyali.tsx', 'utf8');

const devButton = `
      {/* Dev-only floating blank state toggle */}
      <IconButton 
        onClick={() => {
          const isBlank = sessionStorage.getItem('__DEV_BLANK_STATE__') === 'true';
          sessionStorage.setItem('__DEV_BLANK_STATE__', isBlank ? 'false' : 'true');
          window.location.reload();
        }}
        sx={{ 
          position: 'fixed', top: 12, right: 56, zIndex: 9999, 
          bgcolor: sessionStorage.getItem('__DEV_BLANK_STATE__') === 'true' ? 'rgba(255,69,58,0.8)' : (darkMode ? 'rgba(255,255,255,0.1)' : 'rgba(0,0,0,0.05)'),
          backdropFilter: 'blur(8px)', width: 32, height: 32,
          '&:hover': { bgcolor: sessionStorage.getItem('__DEV_BLANK_STATE__') === 'true' ? 'rgba(255,69,58,1)' : (darkMode ? 'rgba(255,255,255,0.2)' : 'rgba(0,0,0,0.1)') }
        }}
        title="Toggle Blank State (Dev Only)"
      >
        <div style={{ width: 12, height: 12, borderRadius: '50%', border: '2px solid', borderColor: darkMode ? "#ffffff" : "#000000" }} />
      </IconButton>
`;

if (!seyali.includes('__DEV_BLANK_STATE__')) {
    seyali = seyali.replace(
        '{/* Dev-only floating dark mode toggle */}',
        devButton + '\n      {/* Dev-only floating dark mode toggle */}'
    );
    fs.writeFileSync('Seyali.tsx', seyali);
}

console.log("Success! Added Dev Blank State toggle.");
