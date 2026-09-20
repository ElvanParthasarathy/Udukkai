import { useLanguage } from '../../../mozhi/LanguageContext';
import React, { useState } from 'react';
import { Bank } from '@phosphor-icons/react';
import { Box, TextField, Typography } from '@mui/material';
import { SettingsPillContainer, SettingsPillRow } from '../../ElvanSettingsSection';

export default function BankSettings({ profile, handleChange, language, handleSave, handleDiscard }) {
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
      <SettingsPillContainer title={t('paymentAccountsTitle') || 'Bank Details'} icon={<Bank size={20} weight="fill"Icon />} iconColor="green">
        <SettingsPillRow
          label={t('bankName') || 'Bank Name'}
          value={
            <Box sx={{ display: 'flex', flexDirection: 'column', gap: 0.5 }}>
              {(profile?.enableBilingual !== false || profile?.primaryDataLanguage !== 'English') && (
                <Typography variant="body2" sx={{ lineHeight: 1.2 }}>{profile.vangiPeyar || (profile?.enableBilingual !== false ? '-' : profile.vangiPeyarEn || '-')}</Typography>
              )}
              {(profile?.enableBilingual !== false || profile?.primaryDataLanguage === 'English') && profile.vangiPeyarEn && (
                <Typography variant={profile?.enableBilingual !== false ? "caption" : "body2"} color={profile?.enableBilingual !== false ? "text.secondary" : "text.primary"} sx={{ lineHeight: 1.2 }}>{profile.vangiPeyarEn}</Typography>
              )}
            </Box>
          }
          isEditing={editingSection === 'bankName'}
          onEdit={() => setEditingSection('bankName')}
          onCancel={onPillCancel}
          onSave={onPillSave}
          saveLabel={t('saveBtn')} cancelLabel={t('cancelBtn')}
        >
          <Box sx={{ display: 'flex', flexDirection: 'column', gap: 2 }}>
            {(profile?.enableBilingual !== false || profile?.primaryDataLanguage !== 'English') && (
              <TextField fullWidth size="small" label={profile?.enableBilingual !== false ? t('bankNameTamil') : t('bankName')} name="vangiPeyar" value={profile.vangiPeyar || ''} onChange={handleChange} />
            )}
            {(profile?.enableBilingual !== false || profile?.primaryDataLanguage === 'English') && (
              <TextField fullWidth size="small" label={profile?.enableBilingual !== false ? t('bankNameEnglish') : t('bankName')} name="vangiPeyarEn" value={profile.vangiPeyarEn || ''} onChange={handleChange} />
            )}
          </Box>
        </SettingsPillRow>

        <SettingsPillRow
          label={t('branchName') || 'Branch Name'}
          value={
            <Box sx={{ display: 'flex', flexDirection: 'column', gap: 0.5 }}>
              {(profile?.enableBilingual !== false || profile?.primaryDataLanguage !== 'English') && (
                <Typography variant="body2" sx={{ lineHeight: 1.2 }}>{profile.bankBranch || (profile?.enableBilingual !== false ? '-' : profile.bankBranchEn || '-')}</Typography>
              )}
              {(profile?.enableBilingual !== false || profile?.primaryDataLanguage === 'English') && profile.bankBranchEn && (
                <Typography variant={profile?.enableBilingual !== false ? "caption" : "body2"} color={profile?.enableBilingual !== false ? "text.secondary" : "text.primary"} sx={{ lineHeight: 1.2 }}>{profile.bankBranchEn}</Typography>
              )}
            </Box>
          }
          isEditing={editingSection === 'branchName'}
          onEdit={() => setEditingSection('branchName')}
          onCancel={onPillCancel}
          onSave={onPillSave}
          saveLabel={t('saveBtn')} cancelLabel={t('cancelBtn')}
        >
          <Box sx={{ display: 'flex', flexDirection: 'column', gap: 2 }}>
            {(profile?.enableBilingual !== false || profile?.primaryDataLanguage !== 'English') && (
              <TextField fullWidth size="small" label={profile?.enableBilingual !== false ? t('branchNameTamil') : t('branchName')} name="bankBranch" value={profile.bankBranch || ''} onChange={handleChange} />
            )}
            {(profile?.enableBilingual !== false || profile?.primaryDataLanguage === 'English') && (
              <TextField fullWidth size="small" label={profile?.enableBilingual !== false ? t('branchNameEnglish') : t('branchName')} name="bankBranchEn" value={profile.bankBranchEn || ''} onChange={handleChange} />
            )}
          </Box>
        </SettingsPillRow>

        <SettingsPillRow
          label={t('accountNumber')}
          value={profile.kanakkuEn}
          isEditing={editingSection === 'kanakkuEn'}
          onEdit={() => setEditingSection('kanakkuEn')}
          onCancel={onPillCancel}
          onSave={onPillSave}
          saveLabel={t('saveBtn')} cancelLabel={t('cancelBtn')}
        >
          <TextField fullWidth size="small" label={t('accountNumber')} name="kanakkuEn" value={profile.kanakkuEn || ''} onChange={handleChange} />
        </SettingsPillRow>

        <SettingsPillRow
          label="IFSC Code"
          value={profile.ifsc}
          isEditing={editingSection === 'ifsc'}
          onEdit={() => setEditingSection('ifsc')}
          onCancel={onPillCancel}
          onSave={onPillSave}
          saveLabel={t('saveBtn')} cancelLabel={t('cancelBtn')}
        >
          <TextField fullWidth size="small" label="IFSC Code" name="ifsc" value={profile.ifsc || ''} onChange={handleChange} />
        </SettingsPillRow>
      </SettingsPillContainer>
    </Box>
  );
}
