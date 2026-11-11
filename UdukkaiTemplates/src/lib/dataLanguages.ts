/** Language metadata belongs here; templates select data by configured codes. */
export const languageNames: Record<string, string> = {
  ta: 'Tamil', en: 'English', hi: 'Hindi', te: 'Telugu', kn: 'Kannada', ml: 'Malayalam',
};

export const languageName = (code: string) => languageNames[code] || code;

export const visibleDataLanguages = (profile: any) =>
  [profile.primaryLanguage, ...(profile.isBilingual ? [profile.secondaryLanguage] : [])]
    .filter((code, index, all) => code && all.indexOf(code) === index);

export const visibleDataValues = (values: Record<string, string>, profile: any) =>
  visibleDataLanguages(profile).map(code => values?.[code] || '').filter(Boolean);
