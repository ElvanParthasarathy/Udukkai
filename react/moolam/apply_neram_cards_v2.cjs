const fs = require('fs');
const { execSync } = require('child_process');

// Restore clean state
execSync('git checkout pagudhigal/Amaippugal.tsx');
execSync('node refactor_settings.cjs');
execSync('node apply_neram_v3.cjs');

let tsx = fs.readFileSync('pagudhigal/Amaippugal.tsx', 'utf8');

// Safely inject the class name into all Papers
tsx = tsx.replace(/<Paper /g, '<Paper className="s2-group" ');

fs.writeFileSync('pagudhigal/Amaippugal.tsx', tsx);
console.log("Success! Applied s2-group safely.");
