import React, { useState, useEffect } from 'react';
import { Box, TextField, InputAdornment, Typography, ToggleButton, ToggleButtonGroup } from '@mui/material';
import { saveProduct } from '../../../Avanam';
import { getCountryConfig } from '../../../Payanpadu';
import { thagaval } from '../../Thagaval';
import { useLanguage } from '../../../mozhi/LanguageContext';
import ElvanEditorLayout from '../../ElvanEditorLayout';
import ElvanBilingualField from '../../ElvanBilingualField';
import { useDraftAndUnsaved } from '../../../hooks/useDraftAndUnsaved';

export default function PorulThoguppu({ onBack, onSaved, product, profileSettings, defaultCountry }) {
  const { t, language } = useLanguage();
  const [form, setForm] = useState<any>({});
  const profileCountry = defaultCountry || 'India';
  const isEditing = !!product?.id;

  const isBilingual = profileSettings?.enableBilingual !== false;
  const primaryLang = profileSettings?.primaryDataLanguage || 'Tamil';
  const secondaryLang = profileSettings?.secondaryDataLanguage || 'English';

  const primaryLangSuffix = isBilingual ? ` (${t(primaryLang.toLowerCase() as any) || primaryLang})` : '';
  const secondaryLangSuffix = isBilingual ? ` (${t(secondaryLang.toLowerCase() as any) || secondaryLang})` : '';

  useEffect(() => {
    if (product) {
      setForm({ ...product });
    } else if (!localStorage.getItem('niril_draft_product')) {
      setForm({ hsn: '50072010', taxPercent: '5', measureType: 'quantity', unit: 'Nos', rate: '', stock: '' });
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
    return !f[`name_${primaryLang}`] && !f[`name_${secondaryLang}`] && (!f.rate || f.rate === '');
  };

  const { hasUnsavedChanges, clearDraft } = useDraftAndUnsaved(
    'niril_draft_product',
    product || { hsn: '50072010', taxPercent: '5', measureType: 'quantity', unit: 'Nos', rate: '', stock: '' },
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
        hsn: form.hsn?.trim() || '',
        rate: form.rate ? parseFloat(form.rate as any) : 0,
        taxPercent: form.taxPercent ? parseFloat(form.taxPercent as any) : 0,
        unit: form.measureType === 'weight' ? 'kg' : (form.unit || 'Nos'),
        measureType: form.measureType || 'quantity',
        stock: form.stock ? parseFloat(form.stock as any) : 0,
      };
      const savedProduct = await saveProduct(productData);
      clearDraft();
      thagaval(isEditing ? (t('productUpdatedToast') || 'Updated!') : (t('productAddedToast') || 'Added!'), 'success');
      onSaved(savedProduct);
    } catch {
      thagaval(t('failedToSaveProductToast') || 'Error saving', 'error');
    }
  };

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
      <datalist id="hsn-list"><option value="50072010" /></datalist>
      <datalist id="gst-list">
        <option value="0" />
        <option value="5" />
        <option value="12" />
        <option value="18" />
        <option value="28" />
      </datalist>

      <Box sx={{ display: 'flex', flexDirection: 'column', gap: 4, mt: 2 }}>
        <Box>
          <Box sx={{ display: 'flex', alignItems: 'center', mb: 2, ml: 1 }}>
            <Box sx={{ width: 24, height: 24, borderRadius: '50%', bgcolor: 'primary.main', color: 'primary.contrastText', display: 'flex', alignItems: 'center', justifyContent: 'center', mr: 1.5, fontSize: '0.8rem', fontWeight: 'bold' }}>1</Box>
            <Typography variant="subtitle2" color="primary.main" sx={{ fontWeight: 800, letterSpacing: '0.5px' }}>
              {t('productDetails') || 'Product Details'}
            </Typography>
          </Box>
          <Box sx={{ display: 'grid', gridTemplateColumns: { xs: '1fr', sm: 'repeat(2, 1fr)' }, gap: 3 }}>
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
            <Box sx={{ display: 'flex', flexDirection: 'column' }}>
              <Typography variant="caption" sx={{ color: 'text.secondary', mb: 1, fontWeight: 500, ml: 2.5 }}>
                {t('measureType')}
              </Typography>
              <ToggleButtonGroup
                color="primary"
                value={form.measureType || 'quantity'}
                exclusive
                onChange={(e, newVal) => {
                  if (newVal) {
                    updateField('measureType', null, newVal);
                  }
                }}
                sx={{ 
                  height: '40px',
                  bgcolor: 'action.hover',
                  borderRadius: '50px',
                  p: 0.5,
                  '.MuiToggleButton-root': {
                    border: 'none',
                    borderRadius: '50px !important',
                    color: 'text.secondary',
                    mx: 0.25,
                    '&.Mui-selected': {
                      bgcolor: 'primary.main',
                      color: 'primary.contrastText',
                      boxShadow: '0 2px 8px rgba(0,0,0,0.2)',
                      '@media (hover: hover)': { '&:hover': {
                        bgcolor: 'primary.dark',
                      } }
                    }
                  }
                }}
              >
                <ToggleButton value="quantity" sx={{ flex: 1, textTransform: 'none', fontWeight: 600 }}>
                  {t('quantity')}
                </ToggleButton>
                <ToggleButton value="weight" sx={{ flex: 1, textTransform: 'none', fontWeight: 600 }}>
                  {t('weight')}
                </ToggleButton>
              </ToggleButtonGroup>
            </Box>
          </Box>
        </Box>

        <Box>
          <Box sx={{ display: 'flex', alignItems: 'center', mb: 2, ml: 1 }}>
            <Box sx={{ width: 24, height: 24, borderRadius: '50%', bgcolor: 'primary.main', color: 'primary.contrastText', display: 'flex', alignItems: 'center', justifyContent: 'center', mr: 1.5, fontSize: '0.8rem', fontWeight: 'bold' }}>2</Box>
            <Typography variant="subtitle2" color="primary.main" sx={{ fontWeight: 800, letterSpacing: '0.5px' }}>
              {t('pricingAndTax') || 'Pricing & Tax'}
            </Typography>
          </Box>
          <Box sx={{ display: 'grid', gridTemplateColumns: { xs: '1fr', sm: 'repeat(2, 1fr)' }, gap: 3 }}>
        <TextField fullWidth size="medium" label={(t('hsnCodeLabel') || 'HSN / SAC Code') as string} 
          slotProps={{ 
            inputLabel: { shrink: true }, 
            htmlInput: { list: "hsn-list" },
            input: {
              sx: { 
                '& input::-webkit-calendar-picker-indicator': { 
                  position: 'absolute', 
                  right: 16, 
                  top: '50%', 
                  transform: 'translateY(-50%)',
                  cursor: 'pointer',
                  opacity: 0.6
                } 
              }
            }
          }}
          value={form.hsn || ''} onChange={e => updateField('hsn', null, e.target.value)} placeholder="50072010" />

        <TextField fullWidth size="medium" label={(t('rateLabel') || 'Selling Rate') as string} type="number" slotProps={{ inputLabel: { shrink: true }, htmlInput: { min: 0 }, input: { startAdornment: <InputAdornment position="start" sx={{ display: 'flex', alignItems: 'center', justifyContent: 'center', minWidth: 24, mt: '0 !important' }}>{getCountryConfig(profileCountry).currencySymbol || getCountryConfig(profileCountry).currency}</InputAdornment> } }}
          value={form.rate || ''} onChange={e => updateField('rate', null, e.target.value)} placeholder="0.00" />

          <TextField fullWidth size="medium" label={(t('gstPercentLabel') || 'GST Rate') as string} type="number" 
            slotProps={{ 
              inputLabel: { shrink: true }, 
              htmlInput: { min: 0, list: "gst-list" }, 
              input: { 
                endAdornment: <InputAdornment position="end" sx={{ mt: '0 !important', mr: 1.5, color: 'text.secondary' }}>%</InputAdornment>,
                sx: { 
                  '& input::-webkit-calendar-picker-indicator': { 
                    position: 'absolute', 
                    right: 36, 
                    top: '50%', 
                    transform: 'translateY(-50%)',
                    cursor: 'pointer',
                    opacity: 0.6
                  } 
                }
              } 
            }}
            value={form.taxPercent || ''} onChange={e => updateField('taxPercent', null, e.target.value)} placeholder="0" />
          </Box>
        </Box>
      </Box>
    </ElvanEditorLayout>
  );
}
