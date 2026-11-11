type Address = {
  street?: string;
  town?: string;
  pin?: string;
  district?: string;
  state?: string;
  country?: string;
};

export function invoiceAddressLines({ street, town, pin, district, state, country }: Address): string[] {
  const streetText = street?.trim() || '';
  const townText = town?.trim() || '';
  const streetKey = streetText.toLowerCase();
  const townKey = townText.toLowerCase();
  let locality = [streetText, townText].filter(Boolean).join(', ');
  if (streetKey && townKey.startsWith(`${streetKey},`)) locality = townText;
  else if (streetKey === townKey || (townKey && streetKey.endsWith(`, ${townKey}`))) locality = streetText;
  const districtLabel = district && !/district|மாவட்டம்/i.test(district)
    ? `${district} ${/[\u0B80-\u0BFF]/.test(district) ? 'மாவட்டம்' : 'District'}`
    : district;
  return [[locality, pin].filter(Boolean).join(' - '), [districtLabel, state, country].filter(Boolean).join(', ')].filter(Boolean);
}
