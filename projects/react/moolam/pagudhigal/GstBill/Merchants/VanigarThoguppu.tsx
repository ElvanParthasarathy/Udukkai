import { useState, useEffect } from 'react';
import { getCountryConfig, getStatesForCountry, getBilingualStateName, getBilingualCountryName, validateTaxId, detectCountryFromBrowser, COUNTRIES, doesStateMatchSearch, doesCountryMatchSearch } from '../../../Payanpadu';
import { useLanguage } from '../../../mozhi/LanguageContext';
import ElvanPillAutocomplete from '../../ElvanPillAutocomplete';
import { TextField, Box, Typography } from '@mui/material';
import { saveClient } from '../../../Avanam';
import { thagaval } from '../../Thagaval';
import ElvanEditorLayout from '../../ElvanEditorLayout';
import ElvanBilingualField from '../../ElvanBilingualField';
import { useDraftAndUnsaved } from '../../../hooks/useDraftAndUnsaved';

export default function VanigarThoguppu({ onBack, onSaved, client, profileSettings, defaultCountry }) {
  const { t } = useLanguage();
  const fallbackCountry = 'India';
  const [form, setForm] = useState<any>({});
  const [taxIdWarning, setTaxIdWarning] = useState('');
  const isEditing = !!client?.id;

  const isBilingual = profileSettings?.enableBilingual !== false;
  const primaryLang = profileSettings?.primaryDataLanguage || 'Tamil';
  const secondaryLang = profileSettings?.secondaryDataLanguage || 'English';

  const primaryLangSuffix = isBilingual ? ` (${t(primaryLang.toLowerCase() as any) || primaryLang})` : '';
  const secondaryLangSuffix = isBilingual ? ` (${t(secondaryLang.toLowerCase() as any) || secondaryLang})` : '';

  useEffect(() => {
    if (client) {
      setForm({ ...client });
    } else if (!localStorage.getItem('niril_draft_client')) {
      setForm({ country: fallbackCountry, isSEZ: false });
    }
    setTaxIdWarning('');
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [client]);

  const updateField = (field, lang, value) => {
    if (lang) {
      setForm(prev => {
        const next = { ...prev, [`${field}_${lang}`]: value };
        if (lang === primaryLang) next[field] = value;
        if (lang === secondaryLang) next[`${field}En`] = value;
        return next;
      });
    } else {
      setForm(prev => ({ ...prev, [field]: value }));
    }
  };

  const getField = (field, lang) => {
    return form[`${field}_${lang}`] || '';
  };

  const formCountry = form.country || fallbackCountry;
  const cc = getCountryConfig(formCountry);
  const stateOptions = getStatesForCountry(formCountry);
  const visibleCountries = COUNTRIES;
  const isCustomCountry = formCountry === 'Other' || (formCountry && !visibleCountries.some(c => c.name === formCountry));

  const handleTaxIdBlur = () => {
    const result = validateTaxId(formCountry, form.gstin);
    setTaxIdWarning(result.ok ? '' : result.message);
  };

  const getIsBlank = (f: any) => {
    return !f[`name_${primaryLang}`] && !f[`name_${secondaryLang}`] && !f.gstin && !f.email && !f.tholaipesi && !f.mugavari;
  };

  const { hasUnsavedChanges, clearDraft } = useDraftAndUnsaved(
    'niril_draft_client',
    client || { country: fallbackCountry, isSEZ: false },
    form,
    setForm,
    isEditing,
    getIsBlank
  );

  const handleSave = async () => {
    const primaryName = getField('name', primaryLang);
    if (!primaryName.trim()) { thagaval('Client name is required', 'warning'); return; }
    try {
      const data = { 
        ...form,
        ...(isEditing ? { id: client.id } : {}),
        country: isCustomCountry ? getField('country', primaryLang).trim() : formCountry,
        pin: form.pin || '',
        gstin: form.gstin || '',
        email: form.email || '',
        tholaipesi: form.tholaipesi || '',
      };
      
      const savedClient = await saveClient(data);
      clearDraft();
      thagaval(isEditing ? 'Client updated' : 'Client added', 'success');
      onSaved(savedClient);
    } catch {
      thagaval('Failed to save client', 'error');
    }
  };

  return (
    <ElvanEditorLayout
      title={(isEditing ? t('editClientTitle') : t('addNewClientTitle')) as string}
      onBack={onBack}
      onSave={handleSave}
      saveButtonText={(isEditing ? t('updateClientModalBtn') : t('saveClientModalBtn')) as string}
      hasUnsavedChanges={hasUnsavedChanges}
      onDiscard={() => {
        clearDraft();
        onBack();
      }}
    >
      <Box sx={{ display: 'flex', flexDirection: 'column', gap: 4, mt: 2 }}>
        <Box>
          <Box sx={{ display: 'flex', alignItems: 'center', mb: 2, ml: 1 }}>
            <Box sx={{ width: 24, height: 24, borderRadius: '50%', bgcolor: 'primary.main', color: 'primary.contrastText', display: 'flex', alignItems: 'center', justifyContent: 'center', mr: 1.5, fontSize: '0.8rem', fontWeight: 'bold' }}>1</Box>
            <Typography variant="subtitle2" color="primary.main" sx={{ fontWeight: 800, letterSpacing: '0.5px' }}>
              {t('clientDetails') || 'Client Details'}
            </Typography>
          </Box>
          <Box sx={{ display: 'grid', gridTemplateColumns: { xs: '1fr', sm: 'repeat(2, 1fr)' }, gap: 3 }}>
            <ElvanBilingualField
              label={t('clientBusinessName') as string}
              primaryLang={primaryLang}
              secondaryLang={secondaryLang}
              isBilingual={isBilingual}
              primaryValue={getField('name', primaryLang)}
              onPrimaryChange={e => updateField('name', primaryLang, e.target.value)}
              secondaryValue={getField('name', secondaryLang)}
              onSecondaryChange={e => updateField('name', secondaryLang, e.target.value)}
              placeholder={t('clientNamePlaceholder') as string}
            />
          </Box>
        </Box>

        <Box>
          <Box sx={{ display: 'flex', alignItems: 'center', mb: 2, ml: 1 }}>
            <Box sx={{ width: 24, height: 24, borderRadius: '50%', bgcolor: 'primary.main', color: 'primary.contrastText', display: 'flex', alignItems: 'center', justifyContent: 'center', mr: 1.5, fontSize: '0.8rem', fontWeight: 'bold' }}>2</Box>
            <Typography variant="subtitle2" color="primary.main" sx={{ fontWeight: 800, letterSpacing: '0.5px' }}>
              {t('addressDetails') || 'Address Details'}
            </Typography>
          </Box>
          <Box sx={{ display: 'grid', gridTemplateColumns: { xs: '1fr', sm: 'repeat(2, 1fr)' }, gap: 3 }}>
        <ElvanBilingualField
          label={t('billingAddress') as string}
          primaryLang={primaryLang}
          secondaryLang={secondaryLang}
          isBilingual={isBilingual}
          primaryValue={getField('mugavari', primaryLang)}
          onPrimaryChange={e => updateField('mugavari', primaryLang, e.target.value)}
          secondaryValue={getField('mugavari', secondaryLang)}
          onSecondaryChange={e => updateField('mugavari', secondaryLang, e.target.value)}
          placeholder={t('streetAddressPlaceholder') as string}
        />

        <ElvanBilingualField
          label={t('city') as string}
          primaryLang={primaryLang}
          secondaryLang={secondaryLang}
          isBilingual={isBilingual}
          primaryValue={getField('oor', primaryLang)}
          onPrimaryChange={e => updateField('oor', primaryLang, e.target.value)}
          secondaryValue={getField('oor', secondaryLang)}
          onSecondaryChange={e => updateField('oor', secondaryLang, e.target.value)}
          placeholder={t('cityPlaceholder') as string}
        />

        <ElvanBilingualField
          label={t('district') as string}
          primaryLang={primaryLang}
          secondaryLang={secondaryLang}
          isBilingual={isBilingual}
          primaryValue={getField('maavattam', primaryLang)}
          onPrimaryChange={e => updateField('maavattam', primaryLang, e.target.value)}
          secondaryValue={getField('maavattam', secondaryLang)}
          onSecondaryChange={e => updateField('maavattam', secondaryLang, e.target.value)}
          placeholder={t('districtPlaceholder') as string}
        />

        <Box sx={{ gridColumn: !isBilingual ? { sm: '1 / -1' } : undefined }}>
          {stateOptions.length > 0 ? (
            <ElvanPillAutocomplete
              options={stateOptions}
              filterOptions={(options: any[], { inputValue }: any) => options.filter((option: any) => doesStateMatchSearch(option, inputValue))}
              getOptionLabel={(s: any) => getBilingualStateName(s, { ...profileSettings, returnOnlyPrimary: true }) || s}
              value={getField('maanilam', primaryLang) || null}
              onChange={(_e: any, newValue: any) => updateField('maanilam', primaryLang, newValue || '')}
              label={`${t(cc.stateLabel as any, { defaultValue: cc.stateLabel })}${primaryLangSuffix}`}
              placeholder={`${t('selectLabel')} ${t(cc.stateLabel as any, { defaultValue: cc.stateLabel })}`}
              textFieldProps={{ autoComplete: "new-password" }}
            />
          ) : (
            <TextField fullWidth size="small" label={`${t(cc.stateLabel as any, { defaultValue: cc.stateLabel })}${primaryLangSuffix}`} slotProps={{ inputLabel: { shrink: true } }}
              value={getField('maanilam', primaryLang)} onChange={e => updateField('maanilam', primaryLang, e.target.value)} placeholder={t(cc.stateLabel as any, { defaultValue: cc.stateLabel }) as string} />
          )}
        </Box>{isBilingual && (
          <TextField fullWidth size="small" disabled label={`${t(cc.stateLabel as any, { defaultValue: cc.stateLabel })}${secondaryLangSuffix}`} slotProps={{ inputLabel: { shrink: true } }}
            value={getField('maanilam', primaryLang) ? getBilingualStateName(getField('maanilam', primaryLang), { ...profileSettings, returnOnlySecondary: true }) : ''} sx={{ '& .MuiInputBase-root': { bgcolor: 'action.hover' } }} />
        )}

        <Box sx={!isBilingual ? { gridColumn: { sm: '1 / -1' } } : undefined}>
          <ElvanPillAutocomplete
            options={Array.from(new Set([...visibleCountries.map((c: any) => c.name), 'Other']))}
            filterOptions={(options: any[], { inputValue }: any) => options.filter((option: any) => option === 'Other' ? 'Other (Custom)'.toLowerCase().includes(inputValue.toLowerCase()) : doesCountryMatchSearch(option, inputValue))}
            getOptionLabel={(c: any) => c === 'Other' ? 'Other (Custom)' : (getBilingualCountryName(c, { ...profileSettings, returnOnlyPrimary: true }) || c)}
            value={isCustomCountry ? 'Other' : (formCountry || null)}
            onChange={(_e: any, newValue: any) => {
              const val = newValue || '';
              if (val === 'Other') {
                updateField('country', null, 'Other');
                updateField('country', primaryLang, '');
                updateField('country', secondaryLang, '');
                updateField('maanilam', primaryLang, '');
                updateField('maanilam', secondaryLang, '');
              } else {
                updateField('country', null, val);
                updateField('country', primaryLang, val);
                updateField('country', secondaryLang, '');
                updateField('maanilam', primaryLang, '');
                updateField('maanilam', secondaryLang, '');
              }
            }}
            label={`${t('country')}${primaryLangSuffix}`}
            textFieldProps={{ autoComplete: "new-password" }}
            textFieldSx={{ mb: isCustomCountry ? 2 : 0 }}
          />
          {isCustomCountry && (
            <TextField fullWidth size="small" 
              value={getField('country', primaryLang)} 
              onChange={e => updateField('country', primaryLang, e.target.value)} 
              placeholder={t('hc_enterCountryName') as string} />
          )}
        </Box>

        {isBilingual && (
          <Box>
            <TextField fullWidth size="small" disabled label={`${t('country')}${secondaryLangSuffix}`} slotProps={{ inputLabel: { shrink: true } }}
              value={formCountry ? getBilingualCountryName(formCountry, { ...profileSettings, returnOnlySecondary: true }) || (formCountry === 'Other' ? 'Other (Custom)' : formCountry) : ''}
              sx={{ '& .MuiInputBase-root': { bgcolor: 'action.hover' }, mb: isCustomCountry ? 2 : 0 }} />
            
            {isCustomCountry && (
              <TextField fullWidth size="small" label={`${t('country')}${secondaryLangSuffix}`} slotProps={{ inputLabel: { shrink: true } }}
                value={getField('country', secondaryLang)}
                onChange={e => updateField('country', secondaryLang, e.target.value)}
                placeholder={t('hc_enterCountryName') as string} />
            )}
          </Box>
        )}

          <TextField fullWidth size="small" label={t(cc.postalLabel as any, { defaultValue: cc.postalLabel }) as string} slotProps={{ inputLabel: { shrink: true } }}
            value={form.pin || ''} onChange={e => updateField('pin', null, e.target.value)} placeholder={cc.postalLabel} />
          </Box>
        </Box>

        <Box>
          <Box sx={{ display: 'flex', alignItems: 'center', mb: 2, ml: 1 }}>
            <Box sx={{ width: 24, height: 24, borderRadius: '50%', bgcolor: 'primary.main', color: 'primary.contrastText', display: 'flex', alignItems: 'center', justifyContent: 'center', mr: 1.5, fontSize: '0.8rem', fontWeight: 'bold' }}>3</Box>
            <Typography variant="subtitle2" color="primary.main" sx={{ fontWeight: 800, letterSpacing: '0.5px' }}>
              {t('contactAndTaxDetails') || 'Contact & Tax Details'}
            </Typography>
          </Box>
          <Box sx={{ display: 'grid', gridTemplateColumns: { xs: '1fr', sm: 'repeat(2, 1fr)' }, gap: 3 }}>
        <TextField fullWidth size="medium" label={t(cc.taxIdLabel as any) || cc.taxIdLabel}
          error={!!taxIdWarning} helperText={taxIdWarning || ' '}
          value={form.gstin || ''} onChange={e => { updateField('gstin', null, e.target.value.toUpperCase()); if (taxIdWarning) setTaxIdWarning(''); }}
          onBlur={handleTaxIdBlur} placeholder={cc.taxIdPlaceholder} slotProps={{ inputLabel: { shrink: true }, htmlInput: { maxLength: 20 } }} />

        <TextField fullWidth size="medium" label={t('emailLabel') as string} slotProps={{ inputLabel: { shrink: true } }} type="email"
          value={form.email || ''} onChange={e => updateField('email', null, e.target.value)} placeholder={t('emailPlaceholder') as string} />
        
          <TextField fullWidth size="medium" label={t('phoneLabel') as string} slotProps={{ inputLabel: { shrink: true } }} type="tel"
            value={form.tholaipesi || ''} onChange={e => updateField('tholaipesi', null, e.target.value)} placeholder={t('phonePlaceholder') as string} />
          </Box>
        </Box>
      </Box>
    </ElvanEditorLayout>
  );
}
