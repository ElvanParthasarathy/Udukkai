import React from 'react';
import { Box, Typography, Grid, TextField, FormControl, InputLabel, Select, MenuItem, Autocomplete } from '@mui/material';
import { useLanguage } from '../../../mozhi/LanguageContext';
import { InvoiceMetadataState } from './InvoiceTypes';
import { getBilingualStateName, getStatesForCountry, doesStateMatchSearch } from '../../../Payanpadu';
import ElvanPillAutocomplete from '../../ElvanPillAutocomplete';
import { DesktopDatePicker } from '@mui/x-date-pickers/DesktopDatePicker';
import { LocalizationProvider } from '@mui/x-date-pickers/LocalizationProvider';
import { AdapterDayjs } from '@mui/x-date-pickers/AdapterDayjs';
import dayjs from 'dayjs';
import { CalendarBlank } from '@phosphor-icons/react';

interface InvoiceMetadataProps {
  metadata: InvoiceMetadataState;
  setMetadata: (meta: InvoiceMetadataState) => void;
  isBilingual: boolean;
  primaryLang: string;
  profile: any;
  client: any;
  settings: any;
}

export default function InvoiceMetadata({
  metadata,
  setMetadata,
  isBilingual,
  primaryLang,
  profile,
  client,
  settings,
}: InvoiceMetadataProps) {
  const { t } = useLanguage();

  const updateMeta = (field: keyof InvoiceMetadataState, value: any) => {
    setMetadata({ ...metadata, [field]: value });
  };

  return (
    <Box sx={{ py: 3 }}>
      <Box sx={{ display: 'flex', alignItems: 'center', gap: 1.5, mb: 3, ml: 1.5 }}>
        <Box sx={{ width: 24, height: 24, borderRadius: '50%', bgcolor: 'primary.main', color: 'primary.contrastText', display: 'flex', alignItems: 'center', justifyContent: 'center', fontSize: '0.8rem', fontWeight: 'bold', lineHeight: 1, pt: '1px' }}>2</Box>
        <Typography variant="h6" sx={{ fontSize: '1.1rem', fontWeight: 600 }}>{t('invoiceDetailsForm') || 'பட்டியல் தரவுகள்'}</Typography>
      </Box>
      <Grid container spacing={2}>
        <Grid size={{ xs: 12, md: 6 }}>
          <TextField fullWidth size="small" label={t('invoiceNumber') || 'பட்டியல் எண்'} slotProps={{ inputLabel: { shrink: true } }}
            value={metadata.invoiceNumber} onChange={(e) => updateMeta('invoiceNumber', e.target.value)} />
        </Grid>
        <Grid size={{ xs: 12, md: 6 }}>
          <LocalizationProvider dateAdapter={AdapterDayjs}>
            <DesktopDatePicker
              label={t('invoiceDate') || 'பட்டியல் நாள்'}
              value={metadata.date ? dayjs(metadata.date) : null}
              onChange={(newValue) => updateMeta('date', newValue ? newValue.format('YYYY-MM-DD') : '')}
              slots={{ openPickerIcon: () => <CalendarBlank size={20} weight="regular" /> }}
              slotProps={{ textField: { fullWidth: true, size: 'small', slotProps: { inputLabel: { shrink: true } } } }}
              format="DD/MM/YYYY"
            />
          </LocalizationProvider>
        </Grid>
        {settings.showPlaceOfSupply && (() => {
          const posOpts = getStatesForCountry(profile?.country || 'India') || [];
          const defaultClientState = client?.maanilam_Tamil || client?.maanilam_English || client?.maanilam;
          const currentValue = metadata.placeOfSupply || defaultClientState;

          return (
            <>
              <Grid size={{ xs: 12, md: 6 }}>
                {posOpts.length > 0 ? (
                  <ElvanPillAutocomplete
                    options={posOpts}
                    value={currentValue || null}
                    onChange={(_: any, newValue: any) => updateMeta('placeOfSupply', newValue || '')}
                    filterOptions={(options: any[], { inputValue }: any) => {
                      return options.filter((option: any) => doesStateMatchSearch(option, inputValue));
                    }}
                    getOptionLabel={(option: any) => getBilingualStateName(option, { ...profile, returnOnlyPrimary: true }) || option}
                    label={`${t('placeOfSupply')}${profile?.enableBilingual !== false ? ` (${profile?.primaryDataLanguage || 'Tamil'})` : ''}`}
                    placeholder={`Defaults to ${defaultClientState || 'Client State'}`}
                    textFieldProps={{ autoComplete: "new-password" }}
                  />
                ) : (
                  <TextField fullWidth size="small" label={`${t('placeOfSupply')}${profile?.enableBilingual !== false ? ` (${profile?.primaryDataLanguage || 'Tamil'})` : ''}`} slotProps={{ inputLabel: { shrink: true } }}
                    value={currentValue || ''} onChange={(e) => updateMeta('placeOfSupply', e.target.value)} placeholder={t('hc_maanilamRegion')} />
                )}
              </Grid>
              {profile?.enableBilingual !== false && (
                <Grid size={{ xs: 12, md: 6 }}>
                  <TextField fullWidth size="small" label={`${t('placeOfSupply')} (${profile?.secondaryDataLanguage || 'English'})`} slotProps={{ inputLabel: { shrink: true } }}
                    value={currentValue ? getBilingualStateName(currentValue, { ...profile, returnOnlySecondary: true }) : ''} 
                    placeholder={`Place of Supply in ${profile?.secondaryDataLanguage || 'English'}`} 
                    disabled={true}
                    sx={{ '& .MuiInputBase-root': { bgcolor: 'action.hover' } }} />
                </Grid>
              )}
            </>
          );
        })()}
      </Grid>
    </Box>
  );
}
