import React, { useState } from 'react';
import { Box, Typography, Select, MenuItem, FormControl, Collapse, Fade } from '@mui/material';
import { Material3Switch } from '../../Amaippugal/Material3Switch';
import { Translate } from '@phosphor-icons/react';
import { SettingsPillContainer, SettingsPillRow } from '../../ElvanSettingsSection';
import { saveProfile } from '../../../Avanam';

export default function LanguageSettings({ profile, setProfile, setSavedSnapshot, pickProfileFields, onSaved, language, t, handleSave, handleDiscard }) {
  const [editingSection, setEditingSection] = useState<string | null>(null);

  const onPillSave = async () => {
    if (handleSave) await handleSave();
    setEditingSection(null);
  };

  const onPillCancel = async () => {
    if (handleDiscard) await handleDiscard();
    setEditingSection(null);
  };

  // We reuse setProfile to update state
  const handleCustomChange = (name, value) => {
    if (setProfile) {
      setProfile({ ...profile, [name]: value });
    }
  };

  // Auto-save for standalone toggles
  const handleToggleBilingual = async (checked) => {
    const newProfile = { ...profile, enableBilingual: checked };
    if (setProfile) setProfile(newProfile);
    
    try {
      await saveProfile(newProfile);
      if (setSavedSnapshot && pickProfileFields) {
        setSavedSnapshot(pickProfileFields(newProfile));
      }
      if (onSaved) onSaved(newProfile);
    } catch (e) {
      console.error('Failed to save toggle', e);
    }
  };

  return (
    <Box sx={{ display: 'flex', flexDirection: 'column', gap: 0 }}>
      <SettingsPillContainer title={t('dataEntryLanguages')} icon={<Translate size={20} weight="fill" />} iconColor="blue">
        
        <Collapse in={editingSection === 'languages'} unmountOnExit timeout={300}>
          <Fade in={editingSection === 'languages'} timeout={300}>
            <Box sx={{ p: '24px 20px 20px 20px', bgcolor: 'transparent' }}>
              <Box sx={{ display: 'flex', flexDirection: 'column', gap: 3 }}>
                <Box>
                  <Typography sx={{ fontSize: '13px', color: 'var(--mac-text)', fontWeight: 500, mb: 1, ml: 1.5 }}>
                    {t('primaryLanguage')}
                  </Typography>
                  <FormControl fullWidth size="small">
                    <Select
                      value={profile.primaryDataLanguage || 'Tamil'}
                      onChange={(e) => handleCustomChange('primaryDataLanguage', e.target.value)}
                      sx={{ 
                        bgcolor: 'action.hover',
                        borderRadius: '100px',
                        '& .MuiOutlinedInput-notchedOutline': { border: 'none' },
                        '&:hover .MuiOutlinedInput-notchedOutline': { border: 'none' },
                        '&.Mui-focused .MuiOutlinedInput-notchedOutline': { border: 'none' },
                        '& .MuiSelect-select': { py: 1.5, px: 2 }
                      }}
                      MenuProps={{ disableScrollLock: true }}
                    >
                      <MenuItem value="Tamil">Tamil</MenuItem>
                      <MenuItem value="English">English</MenuItem>
                    </Select>
                  </FormControl>
                </Box>
  
                <Box>
                  <Typography sx={{ fontSize: '13px', color: 'var(--mac-text)', fontWeight: 500, mb: 1, ml: 1.5 }}>
                    {t('secondaryLanguage')}
                  </Typography>
                  <FormControl fullWidth size="small">
                    <Select
                      value={profile.secondaryDataLanguage || 'English'}
                      onChange={(e) => handleCustomChange('secondaryDataLanguage', e.target.value)}
                      sx={{ 
                        bgcolor: 'action.hover',
                        borderRadius: '100px',
                        '& .MuiOutlinedInput-notchedOutline': { border: 'none' },
                        '&:hover .MuiOutlinedInput-notchedOutline': { border: 'none' },
                        '&.Mui-focused .MuiOutlinedInput-notchedOutline': { border: 'none' },
                        '& .MuiSelect-select': { py: 1.5, px: 2 }
                      }}
                      MenuProps={{ disableScrollLock: true }}
                    >
                      <MenuItem value="Tamil">Tamil</MenuItem>
                      <MenuItem value="English">English</MenuItem>
                    </Select>
                  </FormControl>
                </Box>
              </Box>
  
              <Box sx={{ mt: 3, display: 'flex', justifyContent: 'flex-end', gap: 2 }}>
                <Typography 
                  component="div"
                  onClick={onPillCancel} 
                  sx={{ 
                    px: 2, py: 1, borderRadius: '500px', fontWeight: 600, fontSize: '14px',
                    color: 'text.secondary', cursor: 'pointer',
                    '@media (hover: hover)': { '&:hover': { bgcolor: 'rgba(255,255,255,0.05)' } }
                  }}
                >
                  {t('cancel') || 'Cancel'}
                </Typography>
                <Typography 
                  component="div"
                  onClick={onPillSave} 
                  sx={{ 
                    px: 3, py: 1, borderRadius: '500px', fontWeight: 600, fontSize: '14px',
                    bgcolor: 'primary.main', color: 'primary.contrastText', cursor: 'pointer',
                    '@media (hover: hover)': { '&:hover': { bgcolor: 'primary.dark' } }
                  }}
                >
                  {t('save') || 'Save'}
                </Typography>
              </Box>
            </Box>
          </Fade>
        </Collapse>

        <Collapse in={editingSection !== 'languages'} timeout={300}>
          <Fade in={editingSection !== 'languages'} timeout={300}>
            <Box>
              <SettingsPillRow
                label={t('primaryLanguage')}
                value={profile.primaryDataLanguage || 'Tamil'}
                isEditing={false}
                onEdit={() => setEditingSection('languages')}
                onCancel={() => {}}
                onSave={() => {}}
              >
                {null}
              </SettingsPillRow>
  
              <SettingsPillRow
                label={t('secondaryLanguage')}
                value={profile.secondaryDataLanguage || 'English'}
                isEditing={false}
                onEdit={() => {}}
                onCancel={() => {}}
                onSave={() => {}}
                disableEdit={true}
              >
                {null}
              </SettingsPillRow>
            </Box>
          </Fade>
        </Collapse>
      </SettingsPillContainer>

      {/* Standalone Bilingual Toggle in its own container */}
      <SettingsPillContainer>
        <Box sx={{ p: '16px 20px', display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
          <Typography sx={{ fontSize: '15px', color: 'var(--mac-text, #ffffff)', fontWeight: 500 }}>
            {t('enableBilingualBills')}
          </Typography>
          <Material3Switch 
            checked={profile.enableBilingual !== false} 
            onChange={(e) => handleToggleBilingual(e.target.checked)} 
          />
        </Box>
      </SettingsPillContainer>
    </Box>
  );
}
