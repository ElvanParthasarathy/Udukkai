import { transliterate } from './transliterate';
import * as fs from 'fs';
import * as path from 'path';
import { fileURLToPath } from 'url';
const __filename = fileURLToPath(import.meta.url);
const __dirname = path.dirname(__filename);

const langDir = path.resolve(__dirname, '../../..', 'adippadai/mozhiyaakkam/language_keys');

// Read ta.dart and extract key→Tamil value pairs
const taContent = fs.readFileSync(path.join(langDir, 'ta.dart'), 'utf-8');
const tgContent = fs.readFileSync(path.join(langDir, 'tg.dart'), 'utf-8');

// Parse 'key': 'value' patterns
const keyValueRegex = /'([^']+)'\s*:\s*'([^']+)'/g;

const taMap = new Map<string, string>();
let match;
while ((match = keyValueRegex.exec(taContent)) !== null) {
  taMap.set(match[1], match[2]);
}

// Reset regex
keyValueRegex.lastIndex = 0;
const tgMap = new Map<string, string>();
while ((match = keyValueRegex.exec(tgContent)) !== null) {
  tgMap.set(match[1], match[2]);
}

// ─── PART 1: Audit keys against Tamil values ───
console.log("=== PART 1: KEY vs TAMIL VALUE AUDIT (extended++) ===\n");
console.log("Key".padEnd(40) + "Tamil".padEnd(25) + "Engine Output".padEnd(35) + "Match?");
console.log("─".repeat(110));

let keyMismatches: {key: string, tamil: string, engine: string}[] = [];

for (const [key, tamilValue] of taMap) {
  // Skip multi-word sentences (only check 1-3 word keys)
  const words = tamilValue.split(/\s+/);
  if (words.length > 3) continue;
  
  // Transliterate the Tamil value
  const engineOut = transliterate(tamilValue, "extended++");
  // Remove spaces and make camelCase for comparison
  const engineWords = engineOut.split(/\s+/);
  const engineCamel = engineWords[0] + engineWords.slice(1).map(w => w.charAt(0).toUpperCase() + w.slice(1)).join('');
  
  // Compare (case-insensitive, ignore Ptn suffix)
  const keyClean = key.replace(/Ptn$/, '');
  const engineClean = engineCamel.replace(/Ptn$/, '');
  
  const isMatch = keyClean.toLowerCase() === engineClean.toLowerCase();
  
  if (!isMatch) {
    keyMismatches.push({ key, tamil: tamilValue, engine: engineCamel });
    console.log(`${key.padEnd(40)} ${tamilValue.padEnd(25)} ${engineCamel.padEnd(35)} ❌`);
  }
}

console.log(`\n${keyMismatches.length} key mismatches found out of ${taMap.size} checked.\n`);

// ─── PART 2: Check TG values against engine ───
console.log("\n=== PART 2: TG VALUE AUDIT (Tanglish values in tg.dart) ===\n");

// For each key in tg.dart, check if ta.dart has the Tamil, transliterate it, compare with tg value
let tgMismatches: {key: string, tgValue: string, tamil: string, engine: string}[] = [];

for (const [key, tgValue] of tgMap) {
  const tamilValue = taMap.get(key);
  if (!tamilValue) continue;
  
  const words = tamilValue.split(/\s+/);
  if (words.length > 3) continue;
  
  const engineOut = transliterate(tamilValue, "extended++");
  // TG values are space-separated capitalized words
  const engineCapitalized = engineOut.split(/\s+/).map(w => w.charAt(0).toUpperCase() + w.slice(1)).join(' ');
  
  const isMatch = tgValue.toLowerCase() === engineCapitalized.toLowerCase();
  
  if (!isMatch) {
    tgMismatches.push({ key, tgValue, tamil: tamilValue, engine: engineCapitalized });
    console.log(`${key.padEnd(35)} TG: "${tgValue.padEnd(30)}" Engine: "${engineCapitalized.padEnd(30)}" Tamil: ${tamilValue}`);
  }
}

console.log(`\n${tgMismatches.length} TG value mismatches found.\n`);

// ─── PART 3: Scan all .dart filenames for transliteration issues ───
console.log("\n=== PART 3: FILE NAME AUDIT ===\n");

function scanDir(dir: string, results: string[] = []): string[] {
  for (const entry of fs.readdirSync(dir, { withFileTypes: true })) {
    const fullPath = path.join(dir, entry.name);
    if (entry.isDirectory()) {
      // Check directory name for known wrong patterns
      const name = entry.name;
      if (name.includes('meladukku') && !name.includes('maeladukku')) {
        results.push(`DIR: ${fullPath} — "meladukku" should be "maeladukku" (ே=ae)`);
      }
      if (name.includes('kaipaesi') && !name.includes('kaippaesi')) {
        results.push(`DIR: ${fullPath} — "kaipaesi" should be "kaippaesi" (ப doubles)`);
      }
      if (name === 'ullnuzhaivu') {
        results.push(`DIR: ${fullPath} — "ullnuzhaivu" should be "ulnuzhaivu" (single ள)`);
      }
      if (name.includes('uthavi') && !name.includes('udhavi')) {
        results.push(`FILE: ${fullPath} — "uthavi" should be "udhavi" (த softens→dh)`);
      }
      scanDir(fullPath, results);
    } else if (entry.name.endsWith('.dart')) {
      const name = entry.name;
      if (name.includes('meladukku') && !name.includes('maeladukku')) {
        results.push(`FILE: ${fullPath} — "meladukku" should be "maeladukku"`);
      }
      if (name.includes('kaipaesi') && !name.includes('kaippaesi')) {
        results.push(`FILE: ${fullPath} — "kaipaesi" should be "kaippaesi"`);
      }
      if (name.includes('uthavi') && !name.includes('udhavi')) {
        results.push(`FILE: ${fullPath} — "uthavi" should be "udhavi"`);
      }
    }
  }
  return results;
}

const libRoot = path.resolve(__dirname, '../../../..');
const fileIssues = scanDir(libRoot);

if (fileIssues.length === 0) {
  console.log("✅ All file/folder names are correct!");
} else {
  fileIssues.forEach(issue => console.log(issue));
  console.log(`\n${fileIssues.length} file/folder name issues found.`);
}
