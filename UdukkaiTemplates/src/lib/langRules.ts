// @ts-nocheck
// Per-field language rules from the business's print settings (profile.langRules), for a bilingual bill.
//   both      main language, then the second one      both-rev  second language, then the main one
//   pri       main language only                      sec       second language only
// A missing or "auto" rule keeps the bill's built-in layout for that field. A single-language bill ignores every rule.
const RULES = ['both', 'both-rev', 'pri', 'sec'];

export const langRule = (profile, field) => {
  if (profile?.enableBilingual === false) return 'auto';
  const r = profile?.langRules?.[field];
  return RULES.includes(r) ? r : 'auto';
};

const has = (x) => (x && typeof x === 'object' ? Object.values(x).some(Boolean) : !!x);
const same = (a, b) => JSON.stringify(a).toLowerCase() === JSON.stringify(b).toLowerCase();

// The values (strings, or blocks of lines) in print order. `auto` is what the bill shows without a rule.
// The chosen language having no value never blanks the field: the other language is shown instead.
export const orderBy = (rule, pri, sec, auto) => {
  const list = rule === 'both' ? [pri, sec] : rule === 'both-rev' ? [sec, pri] : rule === 'pri' ? [pri] : rule === 'sec' ? [sec] : auto;
  let out = list.filter(has);
  if (!out.length) out = [pri, sec].filter(has).slice(0, 1);
  return out.filter((x, i) => i === 0 || !same(x, out[0]));
};
