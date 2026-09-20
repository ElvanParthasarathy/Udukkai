import React, { useState } from 'react';
import { Palette } from '@phosphor-icons/react';
import { Box, Typography } from '@mui/material';
import { SettingsPillContainer, SettingsPillRow } from '../../ElvanSettingsSection';
import ElvanPillAutocomplete from '../../ElvanPillAutocomplete';

export default function AppearanceSettings({ formData, setFormData, handleAutoSaveSetting, t, language, handleSave, handleDiscard }) {
  const [editingSection, setEditingSection] = useState<string | null>(null);

  const getThemeName = (val) => {
    if (val === '#388e3c') return t('green') || 'Green';
    if (val === '#6a1b9a') return t('violet') || 'Violet';
    return val;
  };

  const getLangName = (val) => {
    if (val === 'ta') return t('tamil') || 'Tamil';
    if (val === 'en') return t('english') || 'English';
    return val;
  };

  const onPillSave = async () => {
    if (handleSave) await handleSave();
    setEditingSection(null);
  };

  const onPillCancel = async () => {
    if (handleDiscard) await handleDiscard();
    setEditingSection(null);
  };

  const langOptions = [
    { id: 'ta', label: t('tamil') || 'Tamil' },
    { id: 'en', label: t('english') || 'English' }
  ];

  return (
    <Box sx={{ display: 'flex', flexDirection: 'column', gap: 0 }}>
      <SettingsPillContainer 
        title={t('appearanceDescription') || "Configure bill theme color and print languages."} 
        icon={<Palette size={20} weight="fill" />} 
        iconColor="teal"
      >
        <SettingsPillRow
          label={t('billTheme') || 'Bill Theme Color'}
          value={
            <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
              <Box sx={{ width: 16, height: 16, borderRadius: '50%', bgcolor: formData.themeColor || '#388e3c' }} />
              <Box>{getThemeName(formData.themeColor || '#388e3c')}</Box>
            </Box>
          }
          isEditing={editingSection === 'theme'}
          onEdit={() => setEditingSection('theme')}
          onCancel={onPillCancel}
          onSave={onPillSave}
        >
          <Box sx={{ display: 'flex', flexDirection: 'column', gap: 1.5 }}>
            <Typography sx={{ fontSize: '13px', color: 'var(--mac-text-secondary, #aaaaaa)', fontWeight: 500 }}>
              {t('billTheme') || 'Bill Theme Color'}
            </Typography>
            <Box sx={{ display: 'flex', gap: 2 }}>
              {[
                { name: t('green') || 'Green', val: '#388e3c' },
                { name: t('violet') || 'Violet', val: '#6a1b9a' }
              ].map(themeOpt => (
                <Box 
                  key={themeOpt.val}
                  onClick={() => setFormData({ ...formData, themeColor: themeOpt.val })}
                  sx={{ 
                    width: 40, height: 40, borderRadius: '50%', bgcolor: themeOpt.val, 
                    cursor: 'pointer', border: formData.themeColor === themeOpt.val ? '3px solid' : '3px solid transparent',
                    borderColor: formData.themeColor === themeOpt.val ? '#ffffff' : 'transparent',
                    transition: 'all 0.2s', boxShadow: 2
                  }}
                />
              ))}
            </Box>
          </Box>
        </SettingsPillRow>

        <SettingsPillRow
          label={t('defaultPrintLanguage') || 'Default Print Language'}
          value={getLangName(formData.defaultPrintLanguage || 'ta')}
          isEditing={editingSection === 'printLang'}
          onEdit={() => setEditingSection('printLang')}
          onCancel={onPillCancel}
          onSave={onPillSave}
        >
          <Box sx={{ display: 'flex', flexDirection: 'column', gap: 1.5 }}>
            <Typography sx={{ fontSize: '13px', color: 'var(--mac-text-secondary, #aaaaaa)', fontWeight: 500 }}>
              {t('defaultPrintLanguage') || 'Default Print Language'}
            </Typography>
            <ElvanPillAutocomplete
              options={langOptions}
              value={langOptions.find(o => o.id === (formData.defaultPrintLanguage || 'ta')) || null}
              disableClearable
              onChange={(e, val: any) => setFormData({ 
                ...formData, 
                defaultPrintLanguage: val?.id || 'ta',
                receiptLanguage: val?.id || 'ta'
              })}
            />
          </Box>
        </SettingsPillRow>


      </SettingsPillContainer>
    </Box>
  );
}
