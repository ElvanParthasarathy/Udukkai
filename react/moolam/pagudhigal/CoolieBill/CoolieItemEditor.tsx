import React, { useState, useEffect } from 'react';
import { Box, Typography } from '@mui/material';
import { saveCoolieProduct, getAllCoolieProfiles } from '../../Avanam';
import { thagaval } from '../Thagaval';
import { useLanguage } from '../../mozhi/LanguageContext';
import ElvanEditorLayout from '../ElvanEditorLayout';
import ElvanBilingualField from '../ElvanBilingualField';
import { useDraftAndUnsaved } from '../../hooks/useDraftAndUnsaved';

export default function CoolieItemEditor({ onBack, onSaved, product }) {
  const { t } = useLanguage();
  const [form, setForm] = useState({});
  const isEditing = !!product?.id;
  
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

  // Coolie is always bilingual per user's earlier instructions
  const isBilingual = true;
  const primaryLang = activeProfile?.primaryDataLanguage || 'Tamil';
  const secondaryLang = activeProfile?.secondaryDataLanguage || 'English';

  const primaryLangSuffix = isBilingual ? ` (${t(primaryLang.toLowerCase() as any) || primaryLang})` : '';
  const secondaryLangSuffix = isBilingual ? ` (${t(secondaryLang.toLowerCase() as any) || secondaryLang})` : '';

  useEffect(() => {
    if (product) {
      setForm({ ...product });
    } else if (!localStorage.getItem('niril_coolie_draft_product')) {
      setForm({});
    }
  }, [product]);

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
    return !f[`name_${primaryLang}`] && !f[`name_${secondaryLang}`];
  };

  const { hasUnsavedChanges, clearDraft } = useDraftAndUnsaved(
    'niril_coolie_draft_product',
    product || {},
    form,
    setForm,
    isEditing,
    getIsBlank
  );

  const handleSave = async () => {
    const primaryName = getField('name', primaryLang);
    if (!primaryName.trim()) {
      thagaval(t('productNameRequiredToast') || 'Name is required', 'warning');
      return;
    }
    try {
      const productData = {
        ...form,
        ...(isEditing ? { id: product.id } : {}),
      };
      
      // Backward compatibility
      productData.name = primaryName;
      productData.nameEn = getField('name', secondaryLang);

      const savedProduct = await saveCoolieProduct(productData);
      clearDraft();
      thagaval(isEditing ? (t('productUpdatedToast') || 'Updated!') : (t('productAddedToast') || 'Added!'), 'success');
      onSaved(savedProduct);
    } catch {
      thagaval(t('failedToSaveProductToast') || 'Error saving', 'error');
    }
  };

  if (isLoadingProfile) {
    return <Box sx={{ minHeight: '100vh', display: 'flex', justifyContent: 'center', alignItems: 'center' }}></Box>;
  }

  return (
    <ElvanEditorLayout
      title={(isEditing ? (t('editProductTitle') || 'Edit Product') : (t('addProductTitle') || 'New Product')) as string}
      onBack={onBack}
      onSave={handleSave}
      saveButtonText={(isEditing ? (t('updateClientModalBtn') || 'Save Changes') : (t('saveClientModalBtn') || 'Create Product')) as string}
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
              {t('productDetails') || 'Product Details'}
            </Typography>
          </Box>
          <Box sx={{ display: 'grid', gridTemplateColumns: { xs: '1fr', sm: 'repeat(2, 1fr)' }, gap: 3 }}>
            <Box sx={{ display: 'flex', flexDirection: 'column', gap: 3 }}>
              <ElvanBilingualField
                label={(t('productNameLabel') || 'Name') as string}
                primaryLang={primaryLang}
                secondaryLang={secondaryLang}
                isBilingual={isBilingual}
                primaryValue={getField('name', primaryLang)}
                onPrimaryChange={e => updateField('name', primaryLang, e.target.value)}
                secondaryValue={getField('name', secondaryLang)}
                onSecondaryChange={e => updateField('name', secondaryLang, e.target.value)}
                required
                placeholder={(t('productNameLabel') || 'Product Name') as string}
              />
            </Box>
          </Box>
        </Box>
      </Box>
    </ElvanEditorLayout>
  );
}
