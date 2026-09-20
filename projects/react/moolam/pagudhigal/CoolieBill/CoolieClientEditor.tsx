import { useState, useEffect } from 'react';

import { useLanguage } from '../../mozhi/LanguageContext';
import { TextField, Box, Autocomplete, Typography } from '@mui/material';
import { saveCoolieClient, getAllCoolieProfiles } from '../../Avanam';
import { thagaval } from '../Thagaval';
import ElvanEditorLayout from '../ElvanEditorLayout';
import ElvanBilingualField from '../ElvanBilingualField';
import { useDraftAndUnsaved } from '../../hooks/useDraftAndUnsaved';

export default function CoolieClientEditor({ onBack, onSaved, client }) {
  const { t } = useLanguage();
  const [form, setForm] = useState({});
  const isEditing = !!client?.id;

  const [coolieProfile, setCoolieProfile] = useState<any>(null);
  const [isLoadingProfile, setIsLoadingProfile] = useState(true);

  useEffect(() => {
    getAllCoolieProfiles().then(profiles => {
      if (profiles && profiles.length > 0) {
        setCoolieProfile(profiles[0]);
      }
      setIsLoadingProfile(false);
    });
  }, []);

  const activeProfile = coolieProfile || {};

  const isBilingual = true; // Coolie merchants always use bilingual fields
  const primaryLang = activeProfile?.primaryDataLanguage || 'Tamil';
  const secondaryLang = activeProfile?.secondaryDataLanguage || 'English';

  const primaryLangSuffix = isBilingual ? ` (${t(primaryLang.toLowerCase() as any) || primaryLang})` : '';
  const secondaryLangSuffix = isBilingual ? ` (${t(secondaryLang.toLowerCase() as any) || secondaryLang})` : '';

  useEffect(() => {
    if (client) {
      setForm({ ...client });
    } else if (!localStorage.getItem('niril_coolie_draft_client')) {
      setForm({});
    }
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



  const getIsBlank = (f: any) => {
    return !f[`name_${primaryLang}`] && !f[`name_${secondaryLang}`] && !f.address && !f.city;
  };

  const { hasUnsavedChanges, clearDraft } = useDraftAndUnsaved(
    'niril_coolie_draft_client',
    client || {},
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

      };
      
      const savedClient = await saveCoolieClient(data);
      clearDraft();
      thagaval(isEditing ? 'Client updated' : 'Client added', 'success');
      onSaved(savedClient);
    } catch {
      thagaval('Failed to save client', 'error');
    }
  };

  if (isLoadingProfile) {
    return <Box sx={{ minHeight: '100vh', display: 'flex', justifyContent: 'center', alignItems: 'center' }}></Box>;
  }

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
          label={t('city') as string}
          primaryLang={primaryLang}
          secondaryLang={secondaryLang}
          isBilingual={isBilingual}
          primaryValue={getField('city', primaryLang)}
          onPrimaryChange={e => updateField('city', primaryLang, e.target.value)}
          secondaryValue={getField('city', secondaryLang)}
          onSecondaryChange={e => updateField('city', secondaryLang, e.target.value)}
          placeholder={t('cityPlaceholder') as string}
        />

        <ElvanBilingualField
          label={t('billingAddress') as string}
          primaryLang={primaryLang}
          secondaryLang={secondaryLang}
          isBilingual={isBilingual}
          multiline
          rows={3}
          primaryValue={getField('address', primaryLang)}
          onPrimaryChange={e => updateField('address', primaryLang, e.target.value)}
          secondaryValue={getField('address', secondaryLang)}
          onSecondaryChange={e => updateField('address', secondaryLang, e.target.value)}
          placeholder={t('streetAddressPlaceholder') as string}
        />

          </Box>
        </Box>
      </Box>
    </ElvanEditorLayout>
  );
}
