import React, { useState } from 'react';
import { Bank } from '@phosphor-icons/react';
import { Box, TextField } from '@mui/material';
import { SettingsPillContainer, SettingsPillRow } from '../../ElvanSettingsSection';

export default function ContactBankSettings({ formData, setFormData, phones, setPhones, t, handleSave, handleDiscard }) {
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
      <SettingsPillContainer title={t('bankAndContact') || 'Bank Details'} icon={<Bank size={20} weight="fill" />} iconColor="green">
        <SettingsPillRow
          label={t('bankName') || 'Bank Name'}
          value={
            <Box>
              <Box>{formData.bankName}</Box>
              {formData.bankNameEn && <Box sx={{ fontSize: '13px', color: 'var(--mac-text-secondary, #aaaaaa)', mt: 0.5 }}>{formData.bankNameEn}</Box>}
            </Box>
          }
          isEditing={editingSection === 'bankName'}
          onEdit={() => setEditingSection('bankName')}
          onCancel={onPillCancel}
          onSave={onPillSave}
        >
          <Box sx={{ display: 'flex', flexDirection: 'column', gap: 2 }}>
            <TextField label={t('bankNameTamil') || 'Bank Name (Tamil)'} value={formData.bankName} onChange={e => setFormData({ ...formData, bankName: e.target.value })} fullWidth size="small" />
            <TextField label={t('bankNameEnglish') || 'Bank Name (English)'} value={formData.bankNameEn} onChange={e => setFormData({ ...formData, bankNameEn: e.target.value })} fullWidth size="small" />
          </Box>
        </SettingsPillRow>

        <SettingsPillRow
          label={t('branch') || 'Branch Name'}
          value={
            <Box>
              <Box>{formData.branch}</Box>
              {formData.branchEn && <Box sx={{ fontSize: '13px', color: 'var(--mac-text-secondary, #aaaaaa)', mt: 0.5 }}>{formData.branchEn}</Box>}
            </Box>
          }
          isEditing={editingSection === 'branch'}
          onEdit={() => setEditingSection('branch')}
          onCancel={onPillCancel}
          onSave={onPillSave}
        >
          <Box sx={{ display: 'flex', flexDirection: 'column', gap: 2 }}>
            <TextField label={t('branchTamil') || 'Branch Name (Tamil)'} value={formData.branch} onChange={e => setFormData({ ...formData, branch: e.target.value })} fullWidth size="small" />
            <TextField label={t('branchEnglish') || 'Branch Name (English)'} value={formData.branchEn} onChange={e => setFormData({ ...formData, branchEn: e.target.value })} fullWidth size="small" />
          </Box>
        </SettingsPillRow>

        <SettingsPillRow
          label={t('accountNo') || 'Account Number'}
          value={formData.accountNo}
          isEditing={editingSection === 'accountNo'}
          onEdit={() => setEditingSection('accountNo')}
          onCancel={onPillCancel}
          onSave={onPillSave}
        >
          <TextField label={t('accountNo') || 'Account Number'} value={formData.accountNo} onChange={e => setFormData({ ...formData, accountNo: e.target.value })} fullWidth size="small" />
        </SettingsPillRow>

        <SettingsPillRow
          label={t('ifscCode') || 'IFSC Code'}
          value={formData.ifsc}
          isEditing={editingSection === 'ifsc'}
          onEdit={() => setEditingSection('ifsc')}
          onCancel={onPillCancel}
          onSave={onPillSave}
        >
          <TextField label={t('ifscCode') || 'IFSC Code'} value={formData.ifsc} onChange={e => setFormData({ ...formData, ifsc: e.target.value })} fullWidth size="small" />
        </SettingsPillRow>
      </SettingsPillContainer>
    </Box>
  );
}
