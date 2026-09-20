import React, { useState } from 'react';
import { Box, TextField, Stack, Button, IconButton, InputAdornment, ButtonBase } from '@mui/material';
import { Plus, X } from '@phosphor-icons/react';
import { SettingsPillContainer, SettingsPillRow } from '../../ElvanSettingsSection';
import { useLanguage } from '../../../mozhi/LanguageContext';

export default function BusinessInfoSettings({ 
  formData, setFormData, 
  phones, setPhones,
  t, handleSave, handleDiscard 
}) {
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
      {/* Basic Info */}
      <SettingsPillContainer>
        <SettingsPillRow
          label={t('businessNameLabel')}
          value={
            <Box>
              <Box>{formData.name}</Box>
              {formData.nameEn && <Box sx={{ fontSize: '13px', color: 'var(--mac-text-secondary, #aaaaaa)', mt: 0.5 }}>{formData.nameEn}</Box>}
            </Box>
          }
          isEditing={editingSection === 'name'}
          onEdit={() => setEditingSection('name')}
          onCancel={onPillCancel}
          onSave={onPillSave}
        >
          <Box sx={{ display: 'flex', flexDirection: 'column', gap: 2 }}>
            <TextField required fullWidth size="small" label={t('businessNameLabel')} name="name" value={formData.name} onChange={e => setFormData({ ...formData, name: e.target.value })} />
            <TextField fullWidth size="small" label={t('orgNameEn')} name="nameEn" value={formData.nameEn} onChange={e => setFormData({ ...formData, nameEn: e.target.value })} />
          </Box>
        </SettingsPillRow>

        <SettingsPillRow
          label={t('shortBusinessName')}
          value={formData.shortBusinessName}
          isEditing={editingSection === 'shortBusinessName'}
          onEdit={() => setEditingSection('shortBusinessName')}
          onCancel={onPillCancel}
          onSave={onPillSave}
        >
          <TextField fullWidth size="small" label={t('shortBusinessName')} name="shortBusinessName" value={formData.shortBusinessName} onChange={e => setFormData({ ...formData, shortBusinessName: e.target.value })} placeholder="e.g. SJS" helperText={t('shortBusinessNameHint')} />
        </SettingsPillRow>

        <SettingsPillRow
          label={t('phoneNumbers')}
          value={phones.filter(p => p.trim() !== '').join(', ')}
          isEditing={editingSection === 'phones'}
          onEdit={() => setEditingSection('phones')}
          onCancel={onPillCancel}
          onSave={onPillSave}
          extraAction={
            phones.length < 2 && (
              <ButtonBase 
                onClick={() => setPhones([...phones, ''])} 
                sx={{ 
                  px: 2, py: 1, borderRadius: '500px', fontWeight: 600, fontSize: '14px', fontFamily: '"Elvan Sans", sans-serif',
                  color: 'text.secondary',
                  '@media (hover: hover)': { '&:hover': { bgcolor: 'rgba(255,255,255,0.05)' } }
                }}
              >
                {t('add')}
              </ButtonBase>
            )
          }
        >
          <Stack spacing={2}>
            {phones.map((phone, index) => (
              <TextField 
                key={index}
                hiddenLabel margin="none"
                fullWidth size="small" value={phone} 
                onChange={(e) => {
                  const newPhones = [...phones];
                  newPhones[index] = e.target.value;
                  setPhones(newPhones);
                }}
                slotProps={{
                  input: {
                    endAdornment: (
                      <InputAdornment position="end">
                        <IconButton 
                          size="small"
                          edge="end"
                          sx={{ 
                            color: 'text.secondary',
                            '@media (hover: hover)': { '&:hover': { color: 'error.main' } }
                          }} 
                          onClick={() => setPhones(phones.filter((_, i) => i !== index))}
                        >
                          <X size={16} weight="bold" />
                        </IconButton>
                      </InputAdornment>
                    )
                  }
                }}
              />
            ))}
          </Stack>
        </SettingsPillRow>

        <SettingsPillRow
          label={t('email') || 'Email'}
          value={formData.email}
          isEditing={editingSection === 'email'}
          onEdit={() => setEditingSection('email')}
          onCancel={onPillCancel}
          onSave={onPillSave}
        >
          <TextField fullWidth size="small" value={formData.email} onChange={e => setFormData({ ...formData, email: e.target.value })} />
        </SettingsPillRow>
      </SettingsPillContainer>
    </Box>
  );
}
