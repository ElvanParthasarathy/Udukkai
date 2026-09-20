// @ts-nocheck
import { X } from '@phosphor-icons/react';
import { useState, useEffect } from 'react';
import { getCountryConfig, getStatesForCountry, getBilingualStateName, getBilingualCountryName, validateTaxId, detectCountryFromBrowser, COUNTRIES } from '../../../Payanpadu';
import { useLanguage } from '../../../mozhi/LanguageContext';
import { Dialog, DialogTitle, DialogContent, DialogActions, TextField, MenuItem, Button, IconButton, Grid, Typography, useMediaQuery, useTheme, Divider } from '@mui/material';

export default function VanigarThirai({ show, onClose, onSave, client, isEditing, defaultCountry, profileSettings }) {
  const { t } = useLanguage();
  const theme = useTheme();
  const isMobile = useMediaQuery(theme.breakpoints.down('sm'));
  
  const primaryLang = profileSettings?.primaryDataLanguage || 'Tamil';
  const secondaryLang = profileSettings?.secondaryDataLanguage || 'English';
  const enableBilingual = profileSettings?.enableBilingual !== false;
  
  const fallbackCountry = 'India';

  const [form, setForm] = useState({});
  const [taxIdWarning, setTaxIdWarning] = useState('');

  useEffect(() => {
    if (show && client) {
      setForm({
        ...client,
        country: client.country || fallbackCountry,
      });
    } else if (show) {
      setForm({ country: fallbackCountry });
    }
    setTaxIdWarning('');
  }, [show, client, fallbackCountry]);

  if (!show) return null;

  const getField = (baseField, lang) => form[`${baseField}_${lang}`] || '';
  const setField = (baseField, lang, val) => setForm(prev => ({ ...prev, [`${baseField}_${lang}`]: val }));

  const cc = getCountryConfig(form.country);
  const stateOptions = getStatesForCountry(form.country);

  const handleTaxIdBlur = () => {
    const result = validateTaxId(form.country, form.gstin);
    setTaxIdWarning(result.ok ? '' : result.message);
  };

  const handleSave = () => {
    if (!getField('name', primaryLang).trim()) return;
    
    // Form already contains the language-tagged fields dynamically.
    const finalData = { ...form };
    finalData.maanilam = form.maanilam || '';
    finalData.maanilamEn = form.maanilamEn || '';
    finalData.country = form.country || '';
    finalData.countryEn = form.countryEn || '';
    
    onSave(finalData);
  };

  return (
    <Dialog fullScreen={isMobile} open={show} onClose={onClose} maxWidth="sm" fullWidth slotProps={{ paper: { sx: { borderRadius: isMobile ? 0 : 3 } } }}>
      <DialogTitle sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', pb: 2 }}>
        <Typography variant="h6" sx={{ fontWeight: "bold" }}>
          {isEditing ? t('editClientTitle') : t('addNewClientTitle')}
        </Typography>
        <IconButton onClick={onClose} size="small"><X size={20} weight="regular" /></IconButton>
      </DialogTitle>
      
      <DialogContent dividers sx={{ p: { xs: 2, sm: 3 } }}>
        <Typography variant="subtitle2" sx={{ mb: 2.5, fontWeight: 700, color: 'text.secondary', textTransform: 'uppercase', letterSpacing: 1 }}>{t('hc_businessDetails')}</Typography>
        <Grid container spacing={3}>
          <Grid item xs={12}>
            <TextField fullWidth size="medium" label={`${t('clientBusinessName')}${enableBilingual ? ` (${primaryLang})` : ''}`} slotProps={{ inputLabel: { shrink: true } }}
              value={getField('name', primaryLang)} onChange={e => setField('name', primaryLang, e.target.value)} placeholder={t('hc_egAcmeCorp')} />
          </Grid>
          
          {enableBilingual && (
            <Grid item xs={12}>
              <TextField fullWidth size="medium" label={`${t('clientBusinessName')} (${secondaryLang})`} slotProps={{ inputLabel: { shrink: true } }}
                value={getField('name', secondaryLang)} onChange={e => setField('name', secondaryLang, e.target.value)} placeholder="e.g. Acme Corp (English)" />
            </Grid>
          )}
        </Grid>

        <Divider sx={{ my: 4 }} />

        <Typography variant="subtitle2" sx={{ mb: 2.5, fontWeight: 700, color: 'text.secondary', textTransform: 'uppercase', letterSpacing: 1 }}>
          Address Information
        </Typography>
        <Grid container spacing={3}>
          <Grid item xs={12}>
            <TextField fullWidth size="medium" label={`${t('billingAddress')}${enableBilingual ? ` (${primaryLang})` : ''}`} slotProps={{ inputLabel: { shrink: true } }}
              value={getField('mugavari', primaryLang)} onChange={e => setField('mugavari', primaryLang, e.target.value)} placeholder={t('streetAddressPlaceholder')} />
          </Grid>

          {enableBilingual && (
            <Grid item xs={12}>
              <TextField fullWidth size="medium" label={`${t('billingAddress')} (${secondaryLang})`} slotProps={{ inputLabel: { shrink: true } }}
                value={getField('mugavari', secondaryLang)} onChange={e => setField('mugavari', secondaryLang, e.target.value)} placeholder={t('hc_addressInEnglish')} />
            </Grid>
          )}

          <Grid item xs={12} sm={enableBilingual ? 6 : 12}>
            <TextField fullWidth size="medium" label={`${t('city')}${enableBilingual ? ` (${primaryLang})` : ''}`} slotProps={{ inputLabel: { shrink: true } }}
              value={getField('oor', primaryLang)} onChange={e => setField('oor', primaryLang, e.target.value)} placeholder={t('cityPlaceholder')} />
          </Grid>

          {enableBilingual && (
            <Grid item xs={12} sm={6}>
              <TextField fullWidth size="medium" label={`${t('city')} (${secondaryLang})`} slotProps={{ inputLabel: { shrink: true } }}
                value={getField('oor', secondaryLang)} onChange={e => setField('oor', secondaryLang, e.target.value)} placeholder={t('hc_cityInEnglish')} />
            </Grid>
          )}

          <Grid item xs={12} sm={enableBilingual ? 6 : 12}>
            <TextField fullWidth size="medium" label={`மாவட்டம் (District)${enableBilingual ? ` (${primaryLang})` : ''}`} slotProps={{ inputLabel: { shrink: true } }}
              value={getField('maavattam', primaryLang)} onChange={e => setField('maavattam', primaryLang, e.target.value)} placeholder="மாவட்டம்" />
          </Grid>

          {enableBilingual && (
            <Grid item xs={12} sm={6}>
              <TextField fullWidth size="medium" label={`District (${secondaryLang})`} slotProps={{ inputLabel: { shrink: true } }}
                value={getField('maavattam', secondaryLang)} onChange={e => setField('maavattam', secondaryLang, e.target.value)} placeholder="District" />
            </Grid>
          )}

          <Grid item xs={12} sm={enableBilingual ? 6 : 12}>
            {stateOptions.length > 0 ? (
              <TextField select fullWidth size="medium" label={`${t(cc.stateLabel as any, { defaultValue: cc.stateLabel })}${enableBilingual ? ` (${primaryLang})` : ''}`} slotProps={{ inputLabel: { shrink: true } }}
                value={form.maanilam || ''} onChange={e => setForm(prev => ({ ...prev, maanilam: e.target.value }))}>
                <MenuItem value="">{t('selectLabel')} {t(cc.stateLabel as any, { defaultValue: cc.stateLabel })}</MenuItem>
                {stateOptions.map(s => <MenuItem key={s} value={s}>{getBilingualStateName(s, { ...profileSettings, returnOnlyPrimary: true })}</MenuItem>)}
              </TextField>
            ) : (
              <TextField fullWidth size="medium" label={`${t(cc.stateLabel as any, { defaultValue: cc.stateLabel })}${enableBilingual ? ` (${primaryLang})` : ''}`} slotProps={{ inputLabel: { shrink: true } }}
                value={form.maanilam || ''} onChange={e => setForm(prev => ({ ...prev, maanilam: e.target.value }))} placeholder={cc.stateLabel} />
            )}
          </Grid>

          {enableBilingual && (
            <Grid item xs={12} sm={6}>
              <TextField fullWidth size="medium" disabled label={`${t(cc.stateLabel as any, { defaultValue: cc.stateLabel })} (${secondaryLang})`} slotProps={{ inputLabel: { shrink: true } }}
                value={form.maanilam ? getBilingualStateName(form.maanilam, { ...profileSettings, returnOnlySecondary: true }) : ''} sx={{ '& .MuiInputBase-root': { bgcolor: 'action.hover' } }} />
            </Grid>
          )}

          {(() => {
            const visible = COUNTRIES;
            const isCustomCountry = form.country === 'Other' || (form.country && !visible.some(c => c.name === form.country));
            return (
              <>
                <Grid item xs={12} sm={enableBilingual ? 6 : 12}>
                  <TextField select fullWidth size="medium" label={`${t('country')}${enableBilingual ? ` (${primaryLang})` : ''}`} slotProps={{ inputLabel: { shrink: true } }}
                    value={isCustomCountry ? 'Other' : form.country} sx={{ mb: isCustomCountry ? 2 : 0 }}
                    onChange={e => {
                      if (e.target.value === 'Other') {
                        setForm(prev => ({ ...prev, country: 'Other', countryEn: '', maanilam: '', maanilamEn: '' }));
                      } else {
                        setForm(prev => ({ ...prev, country: e.target.value, countryEn: '', maanilam: '', maanilamEn: '' }));
                      }
                    }}>
                    {visible.map(c => <MenuItem key={c.code} value={c.name}>{getBilingualCountryName(c.name, { ...profileSettings, returnOnlyPrimary: true })}</MenuItem>)}
                    <MenuItem value="Other">Other (Custom)</MenuItem>
                  </TextField>
                  {isCustomCountry && (
                    <TextField fullWidth size="medium" value={form.country === 'Other' ? '' : form.country} onChange={e => setForm(prev => ({ ...prev, country: e.target.value }))} placeholder={t('hc_enterCountryName')} />
                  )}
                </Grid>
                {enableBilingual && (
                  <Grid item xs={12} sm={6}>
                    <TextField fullWidth size="medium" disabled={!isCustomCountry} label={`${t('country')} (${secondaryLang})`} slotProps={{ inputLabel: { shrink: true } }}
                      value={isCustomCountry ? (form.countryEn || '') : (form.country ? getBilingualCountryName(form.country, { ...profileSettings, returnOnlySecondary: true }) : '')}
                      onChange={e => isCustomCountry ? setForm(prev => ({ ...prev, countryEn: e.target.value })) : null}
                      sx={!isCustomCountry ? { '& .MuiInputBase-root': { bgcolor: 'action.hover' } } : {}}
                      placeholder={t('hc_countryInEnglish')} />
                  </Grid>
                )}
              </>
            );
          })()}

          <Grid item xs={12} sm={6}>
            <TextField fullWidth size="medium" label={t(cc.postalLabel as any, { defaultValue: cc.postalLabel })} slotProps={{ inputLabel: { shrink: true } }}
              value={form.pin || ''} onChange={e => setForm(prev => ({ ...prev, pin: e.target.value }))} placeholder={cc.postalLabel} />
          </Grid>
        </Grid>

        <Divider sx={{ my: 4 }} />

        <Typography variant="subtitle2" sx={{ mb: 2.5, fontWeight: 700, color: 'text.secondary', textTransform: 'uppercase', letterSpacing: 1 }}>
          Tax & Contact Information
        </Typography>
        <Grid container spacing={3}>
          <Grid item xs={12} sm={12}>
            <TextField fullWidth size="medium" label={t(cc.taxIdLabel as any, { defaultValue: cc.taxIdLabel })} 
              error={!!taxIdWarning} helperText={taxIdWarning || ' '}
              value={form.gstin || ''} onChange={e => { setForm(prev => ({ ...prev, gstin: e.target.value.toUpperCase() })); if (taxIdWarning) setTaxIdWarning(''); }}
              onBlur={handleTaxIdBlur} placeholder={cc.taxIdPlaceholder} slotProps={{ inputLabel: { shrink: true }, htmlInput: { maxLength: 20 } }} />
          </Grid>

          <Grid item xs={12} sm={6}>
            <TextField fullWidth size="medium" label={t('emailLabel')} slotProps={{ inputLabel: { shrink: true } }} type="email"
              value={form.email || ''} onChange={e => setForm(prev => ({ ...prev, email: e.target.value }))} placeholder={t('emailPlaceholder')} />
          </Grid>
          
          <Grid item xs={12} sm={6}>
            <TextField fullWidth size="medium" label={t('phoneLabel')} slotProps={{ inputLabel: { shrink: true } }} type="tel"
              value={form.tholaipesi || ''} onChange={e => setForm(prev => ({ ...prev, tholaipesi: e.target.value }))} placeholder={t('phonePlaceholder')} />
          </Grid>
        </Grid>
      </DialogContent>
      <DialogActions sx={{ p: 2, px: 3 }}>
        <Button variant="outlined" color="inherit" onClick={onClose} sx={{ height: 42, borderRadius: '50px', textTransform: 'none', px: 3, whiteSpace: 'nowrap' }}>
          {t('cancelModalBtn')}
        </Button>
        <Button variant="contained" onClick={handleSave} sx={{ height: 42, borderRadius: '50px', textTransform: 'none', px: 3, bgcolor: '#0f172a', color: 'white', '@media (hover: hover)': { '&:hover': { bgcolor: '#1e293b' } }, whiteSpace: 'nowrap' }}>
          {isEditing ? t('updateClientModalBtn') : t('saveClientModalBtn')}
        </Button>
      </DialogActions>
    </Dialog>
  );
}
