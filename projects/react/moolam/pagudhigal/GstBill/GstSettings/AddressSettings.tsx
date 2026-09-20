import { useLanguage } from '../../../mozhi/LanguageContext';
import React, { useState } from 'react';
import { MapPin } from '@phosphor-icons/react';
import { Box, TextField, FormControl, InputLabel, Select, MenuItem, Typography } from '@mui/material';
import { getCountryConfig, getStatesForCountry, getBilingualStateName, getBilingualCountryName, getCountriesForRegion, doesStateMatchSearch } from '../../../Payanpadu';
import { SettingsPillContainer, SettingsPillRow } from '../../ElvanSettingsSection';
import ElvanPillAutocomplete from '../../ElvanPillAutocomplete';

export default function AddressSettings({ profile, handleChange, language, handleSave, handleDiscard }) {
  const { t } = useLanguage();

  const [editingSection, setEditingSection] = useState<string | null>(null);
  
  const cc = getCountryConfig(profile.country);
  const visibleCountries = getCountriesForRegion();

  const onPillSave = async () => {
    if (handleSave) await handleSave();
    setEditingSection(null);
  };

  const onPillCancel = async () => {
    if (handleDiscard) await handleDiscard();
    setEditingSection(null);
  };

  return (
    <Box sx={{ display: 'flex', flexDirection: 'column', gap: 0 }}>
      <SettingsPillContainer title={t('mugavariLabel') || 'Location & Address'} icon={<MapPin size={20} weight="fill" />} iconColor="orange">
        <SettingsPillRow
          label={`${t('mugavariLabel') || 'Address'}`}
          value={
            <Box>
              <Box>{profile.mugavari}</Box>
              {profile.enableBilingual !== false && profile.mugavariEn && <Box sx={{ fontSize: '13px', color: 'var(--mac-text-secondary, #aaaaaa)', mt: 0.5 }}>{profile.mugavariEn}</Box>}
            </Box>
          }
          isEditing={editingSection === 'mugavari'}
          onEdit={() => setEditingSection('mugavari')}
          onCancel={onPillCancel}
          onSave={onPillSave}
          saveLabel={t('saveBtn')} cancelLabel={t('cancelBtn')}
        >
          <Box sx={{ display: 'flex', flexDirection: 'column', gap: 2 }}>
            <TextField fullWidth multiline rows={2} size="small" label={`${t('mugavariLabel') || 'Address'} (${profile.primaryDataLanguage || 'Tamil'})`} name="mugavari" value={profile.mugavari || ''} onChange={handleChange} />
            {profile.enableBilingual !== false && (
              <TextField fullWidth multiline rows={2} size="small" label={`Address in ${profile.secondaryDataLanguage || 'English'}`} name="mugavariEn" value={profile.mugavariEn || ''} onChange={handleChange} />
            )}
          </Box>
        </SettingsPillRow>

        <SettingsPillRow
          label={`${t('oorLabel') || 'City'}`}
          value={
            <Box>
              <Box>{profile.oor}</Box>
              {profile.enableBilingual !== false && profile.oorEn && <Box sx={{ fontSize: '13px', color: 'var(--mac-text-secondary, #aaaaaa)', mt: 0.5 }}>{profile.oorEn}</Box>}
            </Box>
          }
          isEditing={editingSection === 'oor'}
          onEdit={() => setEditingSection('oor')}
          onCancel={onPillCancel}
          onSave={onPillSave}
          saveLabel={t('saveBtn')} cancelLabel={t('cancelBtn')}
        >
          <Box sx={{ display: 'flex', flexDirection: 'column', gap: 2 }}>
            <TextField fullWidth size="small" label={`${t('oorLabel') || 'City'} (${profile.primaryDataLanguage || 'Tamil'})`} name="oor" value={profile.oor || ''} onChange={handleChange} />
            {profile.enableBilingual !== false && (
              <TextField fullWidth size="small" label={`City in ${profile.secondaryDataLanguage || 'English'}`} name="oorEn" value={profile.oorEn || ''} onChange={handleChange} />
            )}
          </Box>
        </SettingsPillRow>

        <SettingsPillRow
          label={`மாவட்டம் (District)`}
          value={
            <Box>
              <Box>{profile.maavattam}</Box>
              {profile.enableBilingual !== false && profile.maavattamEn && <Box sx={{ fontSize: '13px', color: 'var(--mac-text-secondary, #aaaaaa)', mt: 0.5 }}>{profile.maavattamEn}</Box>}
            </Box>
          }
          isEditing={editingSection === 'maavattam'}
          onEdit={() => setEditingSection('maavattam')}
          onCancel={onPillCancel}
          onSave={onPillSave}
          saveLabel={t('saveBtn')} cancelLabel={t('cancelBtn')}
        >
          <Box sx={{ display: 'flex', flexDirection: 'column', gap: 2 }}>
            <TextField fullWidth size="small" label={`மாவட்டம் (District) (${profile.primaryDataLanguage || 'Tamil'})`} name="maavattam" value={profile.maavattam || ''} onChange={handleChange} />
            {profile.enableBilingual !== false && (
              <TextField fullWidth size="small" label={`District in ${profile.secondaryDataLanguage || 'English'}`} name="maavattamEn" value={profile.maavattamEn || ''} onChange={handleChange} />
            )}
          </Box>
        </SettingsPillRow>

        <SettingsPillRow
          label={`${(t(cc.stateLabel as any) || cc.stateLabel)}`}
          value={
            <Box>
              {(() => {
                const primaryStr = profile.maanilam ? getBilingualStateName(profile.maanilam, { ...profile, returnOnlyPrimary: true }) : '';
                const secondaryStr = profile.maanilam ? getBilingualStateName(profile.maanilam, { ...profile, returnOnlySecondary: true }) : '';
                return (
                  <>
                    <Box>{primaryStr}</Box>
                    {profile.enableBilingual !== false && secondaryStr && (
                      <Box sx={{ fontSize: '13px', color: 'var(--mac-text-secondary, #aaaaaa)', mt: 0.5 }}>{secondaryStr}</Box>
                    )}
                  </>
                );
              })()}
            </Box>
          }
          isEditing={editingSection === 'maanilam'}
          onEdit={() => setEditingSection('maanilam')}
          onCancel={onPillCancel}
          onSave={onPillSave}
          saveLabel={t('saveBtn')} cancelLabel={t('cancelBtn')}
        >
          {(() => {
            const stateOpts = getStatesForCountry(profile.country || 'India');
            return (
              <Box sx={{ display: 'flex', flexDirection: 'column', gap: 2 }}>
                {stateOpts.length > 0 ? (
                  <ElvanPillAutocomplete
                    options={stateOpts.map(s => ({ id: s, label: getBilingualStateName(s, { ...profile, returnOnlyPrimary: true }) }))}
                    value={stateOpts.find(s => s === profile.maanilam) ? { id: profile.maanilam, label: getBilingualStateName(profile.maanilam, { ...profile, returnOnlyPrimary: true }) } : null}
                    onChange={(e, val) => {
                      handleChange({ target: { name: 'maanilam', value: val ? val.id : '' } });
                    }}
                    label={`${(t(cc.stateLabel as any) || cc.stateLabel)} (${profile.primaryDataLanguage || 'Tamil'})`}
                    isOptionEqualToValue={(option, value) => option.id === value?.id}
                    filterOptions={(options: any[], { inputValue }: any) => {
                      return options.filter((option: any) => doesStateMatchSearch(option.id, inputValue));
                    }}
                    disableClearable={false}
                  />
                ) : (
                  <TextField fullWidth size="small" label={`${(t(cc.stateLabel as any) || cc.stateLabel)} (${profile.primaryDataLanguage || 'Tamil'})`} name="maanilam" value={profile.maanilam || ''} onChange={handleChange} />
                )}
                {profile.enableBilingual !== false && (
                  <TextField 
                    fullWidth size="small" 
                    label={`${(t(cc.stateLabel as any) || cc.stateLabel)} (${profile.secondaryDataLanguage || 'English'})`} 
                    value={profile.maanilam ? getBilingualStateName(profile.maanilam, { ...profile, returnOnlySecondary: true }) : ''} 
                    disabled={true} 
                    sx={{ '& .MuiInputBase-root': { bgcolor: 'action.hover' } }}
                  />
                )}
              </Box>
            );
          })()}
        </SettingsPillRow>

        <SettingsPillRow
          label={`${t('countryLabel') || 'Country'}`}
          value={
            <Box>
              {(() => {
                const isCustomCountry = profile.country === 'Other' || (profile.country && !visibleCountries.some(c => c.name === profile.country));
                const primaryStr = isCustomCountry ? profile.country : getBilingualCountryName(profile.country || 'India', { ...profile, returnOnlyPrimary: true });
                const secondaryStr = isCustomCountry ? profile.countryEn : getBilingualCountryName(profile.country || 'India', { ...profile, returnOnlySecondary: true });
                return (
                  <>
                    <Box>{primaryStr}</Box>
                    {profile.enableBilingual !== false && secondaryStr && (
                      <Box sx={{ fontSize: '13px', color: 'var(--mac-text-secondary, #aaaaaa)', mt: 0.5 }}>{secondaryStr}</Box>
                    )}
                  </>
                );
              })()}
            </Box>
          }
          isEditing={editingSection === 'country'}
          onEdit={() => setEditingSection('country')}
          onCancel={onPillCancel}
          onSave={onPillSave}
          saveLabel={t('saveBtn')} cancelLabel={t('cancelBtn')}
        >
          {(() => {
            return (
              <Box sx={{ display: 'flex', flexDirection: 'column', gap: 2 }}>
                <ElvanPillAutocomplete
                  options={visibleCountries.map(c => ({ id: c.name, label: getBilingualCountryName(c.name, { ...profile, returnOnlyPrimary: true }) }))}
                  value={{ id: 'India', label: getBilingualCountryName('India', { ...profile, returnOnlyPrimary: true }) }}
                  disabled={true}
                  onChange={() => {}}
                  label={`${t('countryLabel') || 'Country'} (${profile.primaryDataLanguage || 'Tamil'})`}
                  isOptionEqualToValue={(option, value) => option.id === value?.id}
                  disableClearable={true}
                />
                {profile.enableBilingual !== false && (
                  <TextField 
                    fullWidth size="small" 
                    label={`${t('countryLabel') || 'Country'} (${profile.secondaryDataLanguage || 'English'})`} 
                    value={getBilingualCountryName('India', { ...profile, returnOnlySecondary: true })} 
                    disabled={true} 
                    sx={{ '& .MuiInputBase-root': { bgcolor: 'action.hover' } }}
                  />
                )}
              </Box>
            );
          })()}
        </SettingsPillRow>
      </SettingsPillContainer>
    </Box>
  );
}
