// @ts-nocheck
// How a bilingual bill picks its languages (a single-language bill is never touched by any of this):
//   business name   English, with the Tamil name under it          item name   the main language, then the second
//   business address, customer name, customer address   ONE language: English, or Tamil when the business's rule
//   (profile.langRules[field] === 'ta') says so. The other language fills in if the chosen one has no value.
const has = (x) => (x && typeof x === 'object' ? Object.values(x).some(Boolean) : !!x);
const tamilMain = (profile) => (profile?.primaryDataLanguage || 'Tamil') === 'Tamil';

export const langRule = (profile, field) => (profile?.enableBilingual !== false && profile?.langRules?.[field] === 'ta' ? 'ta' : 'en');

// The (main language, second language) pair as [English, Tamil]
export const enTa = (profile, pri, sec) => (tamilMain(profile) ? [sec, pri] : [pri, sec]);

// The values that have text, the first one first, without a repeat of the same text
export const both = (list) => {
  const out = list.filter(has);
  return out.filter((x, i) => i === 0 || JSON.stringify(x).toLowerCase() !== JSON.stringify(out[0]).toLowerCase());
};

// The one value a one-language field prints
export const oneLang = (profile, field, pri, sec) => {
  const [en, ta] = enTa(profile, pri, sec);
  const list = langRule(profile, field) === 'ta' ? [ta, en] : [en, ta];
  return list.find(has) ?? list[0];
};
