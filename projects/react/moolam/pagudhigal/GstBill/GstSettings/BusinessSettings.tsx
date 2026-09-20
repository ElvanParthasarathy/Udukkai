import { useLanguage } from '../../../mozhi/LanguageContext';
import React, { useState } from 'react';
import { Buildings, Plus, Gear, Trash, ImageSquare, PencilSimple, MapPin, PaintBrush } from '@phosphor-icons/react';
import { Box, Paper, TextField, Button, Select, MenuItem, Typography, IconButton, Grid, FormControl, InputLabel, ButtonBase, Collapse } from '@mui/material';

import { getCountryConfig, getStatesForCountry, getBilingualStateName, getBilingualCountryName, getCountriesForRegion } from '../../../Payanpadu';
import { SettingsPillContainer, SettingsPillRow } from '../../ElvanSettingsSection';
import BankSettings from './BankSettings';

export default function BusinessSettings({
  profile,
  handleChange,
  language,
  taxIdWarning,
  handleTaxIdBlur,
  logoInputRef,
  wideLogoInputRef,
  sigInputRef,
  removeImage,
  handleImageUpload,
  businessProfiles,
  handleLoadProfile,
  setShowManageProfilesModal,
  setShowNewProfileModal,
  renderSaveBar,
  handleSave,
  handleDiscard
}) {
  const { t } = useLanguage();

  const [editingSection, setEditingSection] = useState<string | null>(null);
  const [isProfileEditing, setIsProfileEditing] = useState(false);
  
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
      {/* Profile Switcher */}
      <Paper elevation={0} sx={{ mb: 3, borderRadius: '24px', bgcolor: 'var(--mac-card-bg, #1c1c1e)', border: 'none', overflow: 'hidden' }}>
        <Box sx={{ p: '16px 20px', display: 'flex', flexDirection: 'column', bgcolor: 'transparent' }}>
          <Typography sx={{ fontSize: '13px', color: 'var(--mac-text-secondary, #aaaaaa)', mb: '4px', fontWeight: 500, ml: 2 }}>
            {t('activeProfile')}
          </Typography>
          <Box sx={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', width: '100%' }}>
            <Select 
              fullWidth size="small"
              value={businessProfiles.find(bp => (bp.niruvanathinPeyar || '').trim().toLowerCase() === (profile.niruvanathinPeyar || '').trim().toLowerCase())?.id || ''} 
              onChange={(e) => {
                const bp = businessProfiles.find(p => p.id === e.target.value);
                if (bp) handleLoadProfile(bp);
              }}
              displayEmpty
              MenuProps={{ disableScrollLock: true }}
              sx={{ 
                flex: 1, minWidth: 0,
                bgcolor: (theme) => theme.palette.mode === 'dark' ? 'rgba(255,255,255,0.05)' : 'rgba(0,0,0,0.04)',
                borderRadius: '100px',
                fontSize: '15px', color: 'var(--mac-text, #ffffff)', fontWeight: 500,
                '& .MuiOutlinedInput-notchedOutline': { border: 'none' },
                '&:hover .MuiOutlinedInput-notchedOutline': { border: 'none' },
                '&.Mui-focused .MuiOutlinedInput-notchedOutline': { border: 'none' },
                '& .MuiSelect-select': { py: 1, px: 2, overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap' }
              }}
            >
              <MenuItem value="" disabled><em>{profile.niruvanathinPeyar || 'Select Profile'}</em></MenuItem>
              {businessProfiles.map(bp => (
                <MenuItem key={bp.id} value={bp.id}>{bp.niruvanathinPeyar}</MenuItem>
              ))}
            </Select>
            {!isProfileEditing && (
              <ButtonBase 
                onClick={() => setIsProfileEditing(true)} 
                sx={{ 
                  width: 42, height: 42, borderRadius: '50%', 
                  color: 'text.secondary', flexShrink: 0, ml: 2,
                  bgcolor: (theme) => theme.palette.mode === 'dark' ? 'var(--mac-selection-hover, rgba(255,255,255,0.05))' : 'rgba(0,0,0,0.04)',
                  '@media (hover: hover)': { '&:hover': { bgcolor: (theme) => theme.palette.mode === 'dark' ? 'rgba(255,255,255,0.1)' : 'rgba(0,0,0,0.08)', color: 'var(--mac-text)' } }
                }}
              >
                <PencilSimple size={18} weight="bold" />
              </ButtonBase>
            )}
          </Box>
          
          <Collapse in={isProfileEditing} sx={{ width: '100%' }} unmountOnExit timeout={300}>
            <Box sx={{ pt: 3 }}>
              <Box sx={{ display: 'flex', gap: 1, justifyContent: 'flex-end', width: '100%', flexWrap: 'nowrap' }}>
                <ButtonBase 
                  onClick={() => setIsProfileEditing(false)} 
                  sx={{ 
                    px: 2, py: 1, borderRadius: '500px', fontWeight: 600, fontSize: '13px', fontFamily: '"Elvan Sans", sans-serif',
                    color: 'text.secondary', whiteSpace: 'nowrap',
                    '@media (hover: hover)': { '&:hover': { bgcolor: 'rgba(255,255,255,0.05)' } }
                  }}
                >
                  {t('closeBtn')}
                </ButtonBase>
                <ButtonBase 
                  onClick={() => {
                    setShowManageProfilesModal(true);
                  }} 
                  sx={{ 
                    px: 2, py: 1, borderRadius: '500px', fontWeight: 600, fontSize: '13px', fontFamily: '"Elvan Sans", sans-serif',
                    color: 'text.primary', whiteSpace: 'nowrap',
                    '@media (hover: hover)': { '&:hover': { bgcolor: 'rgba(255,255,255,0.05)' } }
                  }}
                >
                  {t('manage')}
                </ButtonBase>
                <ButtonBase 
                  onClick={() => {
                    setShowNewProfileModal(true);
                  }} 
                  sx={{ 
                    px: 2, py: 1, borderRadius: '500px', fontWeight: 600, fontSize: '13px', fontFamily: '"Elvan Sans", sans-serif',
                    bgcolor: 'primary.main', color: 'primary.contrastText', whiteSpace: 'nowrap',
                    '@media (hover: hover)': { '&:hover': { bgcolor: 'primary.dark' } }
                  }}
                >
                  {t('addNew')}
                </ButtonBase>
              </Box>
            </Box>
          </Collapse>
        </Box>
      </Paper>

      {/* Basic Info */}
      <SettingsPillContainer>
        <SettingsPillRow
          label={t('businessNameLabel') || 'Business Name'}
          value={
            <Box>
              <Box>{profile.niruvanathinPeyar}</Box>
              {profile.enableBilingual !== false && profile.niruvanathinPeyarEn && (
                <Box sx={{ fontSize: '13px', color: 'var(--mac-text-secondary, #aaaaaa)', mt: 0.5 }}>
                  {profile.niruvanathinPeyarEn}
                </Box>
              )}
            </Box>
          }
          isEditing={editingSection === 'niruvanathinPeyar'}
          onEdit={() => setEditingSection('niruvanathinPeyar')}
          onCancel={onPillCancel}
          onSave={onPillSave}
          saveLabel={t('saveBtn')} cancelLabel={t('cancelBtn')}
        >
          <Box sx={{ display: 'flex', flexDirection: 'column', gap: 2 }}>
            <TextField required fullWidth size="small" label={`${t('businessNameLabel') || 'Business Name'} (${profile.primaryDataLanguage || 'Tamil'})`} name="niruvanathinPeyar" value={profile.niruvanathinPeyar || ''} onChange={handleChange} />
            {profile.enableBilingual !== false && (
              <TextField fullWidth size="small" label={`Business Name in ${profile.secondaryDataLanguage || 'English'}`} name="niruvanathinPeyarEn" value={profile.niruvanathinPeyarEn || ''} onChange={handleChange} />
            )}
          </Box>
        </SettingsPillRow>

        <SettingsPillRow
          label={t('shortBusinessNameLabel')}
          value={profile.shortBusinessName}
          isEditing={editingSection === 'shortBusinessName'}
          onEdit={() => setEditingSection('shortBusinessName')}
          onCancel={onPillCancel}
          onSave={onPillSave}
          saveLabel={t('saveBtn')} cancelLabel={t('cancelBtn')}
        >
          <TextField fullWidth size="small" label={t('shortBusinessName')} name="shortBusinessName" value={profile.shortBusinessName || ''} onChange={handleChange} placeholder="e.g. SJS" helperText={t('shortBusinessNameHelper')} />
        </SettingsPillRow>

        <SettingsPillRow
          label={t('tholaipesiLabel') || 'Phone'}
          value={profile.tholaipesi}
          isEditing={editingSection === 'tholaipesi'}
          onEdit={() => setEditingSection('tholaipesi')}
          onCancel={onPillCancel}
          onSave={onPillSave}
          saveLabel={t('saveBtn')} cancelLabel={t('cancelBtn')}
        >
          <TextField fullWidth size="small" label={t('tholaipesiLabel') || 'Phone'} name="tholaipesi" value={profile.tholaipesi || ''} onChange={handleChange} />
        </SettingsPillRow>

        <SettingsPillRow
          label={t('mobileLabel') || 'Mobile'}
          value={profile.mobileNumber}
          isEditing={editingSection === 'mobileNumber'}
          onEdit={() => setEditingSection('mobileNumber')}
          onCancel={onPillCancel}
          onSave={onPillSave}
          saveLabel={t('saveBtn')} cancelLabel={t('cancelBtn')}
        >
          <TextField fullWidth size="small" label={t('mobileLabel') || 'Mobile'} name="mobileNumber" value={profile.mobileNumber || ''} onChange={handleChange} />
        </SettingsPillRow>

        <SettingsPillRow
          label={t('email') || 'Email'}
          value={profile.email}
          isEditing={editingSection === 'email'}
          onEdit={() => setEditingSection('email')}
          onCancel={onPillCancel}
          onSave={onPillSave}
          saveLabel={t('saveBtn')} cancelLabel={t('cancelBtn')}
        >
          <TextField fullWidth size="small" label={t('email') || 'Email'} name="email" value={profile.email || ''} onChange={handleChange} />
        </SettingsPillRow>

        <SettingsPillRow
          label={cc.taxIdLabel || 'GSTIN / Tax ID'}
          value={profile.gstin}
          isEditing={editingSection === 'gstin'}
          onEdit={() => setEditingSection('gstin')}
          onCancel={onPillCancel}
          onSave={onPillSave}
          saveLabel={t('saveBtn')} cancelLabel={t('cancelBtn')}
        >
          <TextField 
            fullWidth size="small" 
            label={cc.taxIdLabel || 'GSTIN / Tax ID'} 
            name="gstin" 
            value={profile.gstin || ''} 
            onChange={handleChange} 
            onBlur={handleTaxIdBlur} 
            error={!!taxIdWarning} 
            helperText={taxIdWarning} 
          />
        </SettingsPillRow>
      </SettingsPillContainer>

    </Box>
  );
}
