import React, { useState, useEffect } from 'react';
import { Box, Typography, FormControl, InputLabel, Select, MenuItem, TextField } from '@mui/material';
import { Receipt } from '@phosphor-icons/react';
import { SettingsPillContainer, SettingsPillRow } from '../../ElvanSettingsSection';
import { Material3Switch } from '../../Amaippugal/Material3Switch';
import { getCountriesForRegion } from '../../../Payanpadu';
import { getInvoiceDisplayOptions, saveInvoiceDisplayOptions } from '../../../Avanam';

export default function InvoiceSettings({ language, t }) {
  const [invoiceTemplate, setInvoiceTemplate] = useState<any>(() => {
    const savedLocal = localStorage.getItem('elvanniril_invoiceOptions');
    return savedLocal ? JSON.parse(savedLocal) : {};
  });
  const [editingSection, setEditingSection] = useState<string | null>(null);

  const [loaded, setLoaded] = useState(false);

  useEffect(() => {
    let unsub = null;
    getInvoiceDisplayOptions((fresh) => {
      if (fresh && Object.keys(fresh).length > 0) {
        setInvoiceTemplate(prev => ({ ...prev, ...fresh }));
      }
    }).then(serverOpts => {
      if (serverOpts && Object.keys(serverOpts).length > 0) {
        setInvoiceTemplate(prev => ({ ...prev, ...serverOpts }));
      }
      if (serverOpts && serverOpts.unsubscribe) unsub = serverOpts.unsubscribe;
      setLoaded(true);
    });

    return () => {
      if (unsub) unsub();
    };
  }, []);

  const handleTemplateChange = (key, val) => {
    const newOpts = { ...invoiceTemplate, [key]: val };
    setInvoiceTemplate(newOpts);
    localStorage.setItem('elvanniril_invoiceOptions', JSON.stringify(newOpts));
    saveInvoiceDisplayOptions(newOpts);
  };

  return (
    <Box sx={{ display: 'flex', flexDirection: 'column', gap: 0 }}>
      <SettingsPillContainer title={t('hc_invoiceTemplate') || 'Invoice Template'} icon={<Receipt size={20} weight="fill" />} iconColor="orange">
        {Array.from(new Map(getCountriesForRegion().map(c => [c.currency, c])).values()).length > 1 && (
          <SettingsPillRow
            label={t('hc_currency') || 'Currency'}
            value={invoiceTemplate.currency || 'INR'}
            isEditing={editingSection === 'currency'}
            onEdit={() => setEditingSection('currency')}
            onCancel={() => setEditingSection(null)}
            onSave={() => setEditingSection(null)}
            saveLabel={t('closeBtn')}
            cancelLabel=""
          >
            <FormControl fullWidth size="small">
              <InputLabel shrink>{t('hc_currency') || 'Currency'}</InputLabel>
              <Select value={invoiceTemplate.currency || 'INR'} label={t('hc_currency') || 'Currency'} displayEmpty
                onChange={(e) => handleTemplateChange('currency', e.target.value)}>
                {Array.from(new Map(getCountriesForRegion().map(c => [c.currency, c])).values()).map(c => (
                  <MenuItem key={c.currency} value={c.currency}>{c.currency} ({c.currencySymbol === c.currency ? c.name : c.currencySymbol})</MenuItem>
                ))}
              </Select>
            </FormControl>
            {invoiceTemplate.currency !== 'INR' && (
              <Box sx={{ mt: 2 }}>
                <TextField fullWidth size="small" label="Exchange Rate (optional, snapshot)" 
                  type="number" slotProps={{ htmlInput: { step: "any", min: 0 } }}
                  value={invoiceTemplate.exchangeRate || ''}
                  onChange={(e) => handleTemplateChange('exchangeRate', e.target.value)}
                  placeholder={`1 ${invoiceTemplate.currency} = ? INR`}
                  helperText="Stored on this invoice — historical reports stay accurate even if rates change."
                />
              </Box>
            )}
          </SettingsPillRow>
        )}

        <Box sx={{ p: '16px 20px', display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
          <Box sx={{ display: 'flex', flexDirection: 'column', pr: 2 }}>
            <Typography sx={{ fontSize: '15px', color: 'var(--mac-text, #ffffff)', fontWeight: 500 }}>
              {t('showItemizedGstTable')}
            </Typography>
          </Box>
          {loaded && (
            <Material3Switch 
              checked={!!invoiceTemplate.showItemizedTax}
              onChange={(e) => handleTemplateChange('showItemizedTax', e.target.checked)}
            />
          )}
        </Box>
      </SettingsPillContainer>
    </Box>
  );
}
