import { useLanguage } from '../../../mozhi/LanguageContext';
import React, { useState } from 'react';
import { PaintBrush, Trash, ImageSquare, PencilSimple } from '@phosphor-icons/react';
import { Box, Paper, Typography, Button, IconButton, TextField, FormControl, Select, MenuItem } from '@mui/material';
import { SettingsPillContainer, SettingsPillRow } from '../../ElvanSettingsSection';

export default function BrandingSettings({ 
  profile, handleChange, language, handleSave, handleDiscard, 
  logoInputRef, wideLogoInputRef, sigInputRef, removeImage, handleImageUpload 
}) {
  const { t } = useLanguage();

  const [editingSection, setEditingSection] = useState<string | null>(null);

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
      <SettingsPillContainer title={t('brandingTitle') || 'Branding & Signatures'} icon={<PaintBrush size={20} weight="fill" />} iconColor="purple">
        <SettingsPillRow
          label={t('businessLogo') || 'Logo'}
          value={profile.logo ? <img src={profile.logo} alt="Logo" style={{ height: 40, objectFit: 'contain' }} /> : 'No Logo'}
          isEditing={editingSection === 'logo'}
          onEdit={() => setEditingSection('logo')}
          onCancel={onPillCancel}
          onSave={onPillSave}
          saveLabel={t('saveBtn')} cancelLabel={t('cancelBtn')}
        >
          <Paper variant="outlined" sx={{ p: 2, display: 'flex', flexDirection: 'column', alignItems: 'center', gap: 2, borderStyle: 'dashed', bgcolor: (theme) => theme.palette.mode === 'dark' ? 'rgba(0,0,0,0.2)' : 'rgba(0,0,0,0.03)' }}>
            {profile.logo ? (
              <>
                <Box sx={{ position: 'relative' }}>
                  <img src={profile.logo} alt="Logo" style={{ maxHeight: '100px', maxWidth: '180px', objectFit: 'contain' }} />
                  <IconButton size="small" color="error" onClick={() => removeImage('logo')} sx={{ position: 'absolute', top: -10, right: -10, bgcolor: 'background.paper', '@media (hover: hover)': { '&:hover': { bgcolor: 'error.light' } } }}>
                    <Trash size={20} weight="fill" sx={{ fontSize: 14 }} />
                  </IconButton>
                </Box>
                <Button size="small" variant="outlined" onClick={() => logoInputRef.current?.click()}>Change Logo</Button>
              </>
            ) : (
              <Button variant="outlined" onClick={() => logoInputRef.current?.click()} startIcon={<ImageSquare size={20} weight="fill" sx={{ fontSize: 20 }} />} sx={{ flexDirection: 'column', py: 3, gap: 1, width: '100%' }}>
                {t('uploadLogo') || 'Upload Logo'}
                <Typography variant="caption" color="text.secondary" sx={{ display: 'block' }}>{t('logoHint') || 'Square or circle logo'}</Typography>
              </Button>
            )}
            <input ref={logoInputRef} type="file" accept="image/*" style={{ display: 'none' }} onChange={(e) => handleImageUpload('logo', e)} />
          </Paper>
        </SettingsPillRow>

        <SettingsPillRow
          label="Vertical / Wide Logo"
          value={profile.wideLogo ? <img src={profile.wideLogo} alt="Wide Logo" style={{ height: 40, objectFit: 'contain' }} /> : 'No Wide Logo'}
          isEditing={editingSection === 'wideLogo'}
          onEdit={() => setEditingSection('wideLogo')}
          onCancel={onPillCancel}
          onSave={onPillSave}
          saveLabel={t('saveBtn')} cancelLabel={t('cancelBtn')}
        >
          <Paper variant="outlined" sx={{ p: 2, display: 'flex', flexDirection: 'column', alignItems: 'center', gap: 2, borderStyle: 'dashed', bgcolor: (theme) => theme.palette.mode === 'dark' ? 'rgba(0,0,0,0.2)' : 'rgba(0,0,0,0.03)' }}>
            {profile.wideLogo ? (
              <>
                <Box sx={{ position: 'relative', width: '100%', display: 'flex', justifyContent: 'center' }}>
                  <img src={profile.wideLogo} alt="Wide Logo" style={{ height: '100px', width: '100%', objectFit: 'contain' }} />
                  <IconButton size="small" color="error" onClick={() => removeImage('wideLogo')} sx={{ position: 'absolute', top: -10, right: -10, bgcolor: 'background.paper', '@media (hover: hover)': { '&:hover': { bgcolor: 'error.light' } } }}>
                    <Trash size={20} weight="fill" sx={{ fontSize: 14 }} />
                  </IconButton>
                </Box>
                <Button size="small" variant="outlined" sx={{ mt: 1 }} onClick={() => wideLogoInputRef.current?.click()}>Change Logo</Button>
              </>
            ) : (
              <Button variant="outlined" onClick={() => wideLogoInputRef.current?.click()} startIcon={<ImageSquare size={20} weight="fill" sx={{ fontSize: 20 }} />} sx={{ flexDirection: 'column', py: 3, gap: 1, width: '100%' }}>
                Upload Wide Logo
                <Typography variant="caption" color="text.secondary" sx={{ display: 'block' }}>Replaces company name in header</Typography>
              </Button>
            )}
            <input ref={wideLogoInputRef} type="file" accept="image/*" style={{ display: 'none' }} onChange={(e) => handleImageUpload('wideLogo', e)} />
          </Paper>
        </SettingsPillRow>

        <SettingsPillRow
          label="Bill Header Style"
          value={profile.billHeaderStyle === 'wide' ? 'Wide Logo Only' : 'Small Logo + Business Name'}
          isEditing={editingSection === 'headerStyle'}
          onEdit={() => setEditingSection('headerStyle')}
          onCancel={onPillCancel}
          onSave={onPillSave}
          saveLabel={t('saveBtn')} cancelLabel={t('cancelBtn')}
        >
          <FormControl fullWidth size="small">
            <Select 
              value={profile.billHeaderStyle || 'small'}
              onChange={(e) => handleChange({ target: { name: 'billHeaderStyle', value: e.target.value } })}
              sx={{ 
                bgcolor: 'action.hover',
                borderRadius: '100px',
                '& .MuiOutlinedInput-notchedOutline': { border: 'none' },
                '&:hover .MuiOutlinedInput-notchedOutline': { border: 'none' },
                '&.Mui-focused .MuiOutlinedInput-notchedOutline': { border: 'none' },
                '& .MuiSelect-select': { py: 1.5, px: 2 }
              }}
            >
              <MenuItem value="small">Small Logo + Business Name</MenuItem>
              <MenuItem value="wide">Wide Logo Only</MenuItem>
            </Select>
          </FormControl>
        </SettingsPillRow>

        <SettingsPillRow
          label={t('signature') || 'Signature'}
          value={
            <Box sx={{ display: 'flex', flexDirection: 'column', gap: 0.5 }}>
              {profile.signature ? <img src={profile.signature} alt="Signature" style={{ height: 40, objectFit: 'contain' }} /> : 'No Signature'}
              <Typography sx={{ fontSize: '12px', color: 'text.secondary' }}>{profile.authorizedSignatoryName}</Typography>
            </Box>
          }
          isEditing={editingSection === 'signature'}
          onEdit={() => setEditingSection('signature')}
          onCancel={onPillCancel}
          onSave={onPillSave}
          saveLabel={t('saveBtn')} cancelLabel={t('cancelBtn')}
        >
          <Paper variant="outlined" sx={{ p: 2, display: 'flex', flexDirection: 'column', alignItems: 'center', gap: 2, borderStyle: 'dashed', bgcolor: (theme) => theme.palette.mode === 'dark' ? 'rgba(0,0,0,0.2)' : 'rgba(0,0,0,0.03)' }}>
            {profile.signature ? (
              <>
                <Box sx={{ position: 'relative' }}>
                  <img src={profile.signature} alt="Signature" style={{ maxHeight: '100px', maxWidth: '200px', objectFit: 'contain' }} />
                  <IconButton size="small" color="error" onClick={() => removeImage('signature')} sx={{ position: 'absolute', top: -10, right: -10, bgcolor: 'background.paper', '@media (hover: hover)': { '&:hover': { bgcolor: 'error.light' } } }}>
                    <Trash size={20} weight="fill" sx={{ fontSize: 14 }} />
                  </IconButton>
                </Box>
              </>
            ) : (
              <Button variant="outlined" onClick={() => sigInputRef.current?.click()} startIcon={<PencilSimple size={20} weight="fill" sx={{ fontSize: 20 }} />} sx={{ flexDirection: 'column', py: 3, gap: 1, width: '100%' }}>
                {t('uploadSignature') || 'Upload Signature'}
                <Typography variant="caption" color="text.secondary" sx={{ display: 'block' }}>{t('sigHint') || 'For bottom of invoices'}</Typography>
              </Button>
            )}
            <input ref={sigInputRef} type="file" accept="image/*" style={{ display: 'none' }} onChange={(e) => handleImageUpload('signature', e)} />
          </Paper>
          <TextField fullWidth size="small" sx={{ mt: 4 }} label={t('authorizedSignatoryName')} name="authorizedSignatoryName" value={profile.authorizedSignatoryName || ''} onChange={handleChange} placeholder="e.g. V.R.M. Elvan" />
        </SettingsPillRow>
      </SettingsPillContainer>
    </Box>
  );
}
