import React, { useState } from 'react';
import { MapPin } from '@phosphor-icons/react';
import { Box, TextField } from '@mui/material';
import { SettingsPillContainer, SettingsPillRow } from '../../ElvanSettingsSection';

export default function AddressSettings({ formData, setFormData, t, handleSave, handleDiscard }) {
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
      <SettingsPillContainer title={t('addressDetails') || 'Address & Location'} icon={<MapPin size={20} weight="fill" />} iconColor="red">
        <SettingsPillRow
          label={t('addressLabel') || 'Address'}
          value={
            <Box>
              <Box>{formData.address}</Box>
              {formData.addressEn && <Box sx={{ fontSize: '13px', color: 'var(--mac-text-secondary, #aaaaaa)', mt: 0.5 }}>{formData.addressEn}</Box>}
            </Box>
          }
          isEditing={editingSection === 'address'}
          onEdit={() => setEditingSection('address')}
          onCancel={onPillCancel}
          onSave={onPillSave}
        >
          <Box sx={{ display: 'flex', flexDirection: 'column', gap: 2 }}>
            <TextField label={t('addressTamil') || 'Address (Tamil)'} value={formData.address} onChange={e => setFormData({ ...formData, address: e.target.value })} fullWidth multiline rows={2} size="small" />
            <TextField label={t('addressEnglish') || 'Address (English)'} value={formData.addressEn} onChange={e => setFormData({ ...formData, addressEn: e.target.value })} fullWidth multiline rows={2} size="small" />
          </Box>
        </SettingsPillRow>

        <SettingsPillRow
          label={t('cityLabel') || 'City'}
          value={
            <Box>
              <Box>{formData.city}</Box>
              {formData.cityEn && <Box sx={{ fontSize: '13px', color: 'var(--mac-text-secondary, #aaaaaa)', mt: 0.5 }}>{formData.cityEn}</Box>}
            </Box>
          }
          isEditing={editingSection === 'city'}
          onEdit={() => setEditingSection('city')}
          onCancel={onPillCancel}
          onSave={onPillSave}
        >
          <Box sx={{ display: 'flex', flexDirection: 'column', gap: 2 }}>
            <TextField label={t('cityTamil') || 'City (Tamil)'} value={formData.city} onChange={e => setFormData({ ...formData, city: e.target.value })} fullWidth size="small" />
            <TextField label={t('cityEnglish') || 'City (English)'} value={formData.cityEn} onChange={e => setFormData({ ...formData, cityEn: e.target.value })} fullWidth size="small" />
          </Box>
        </SettingsPillRow>

        <SettingsPillRow
          label={t('districtLabel') || 'District'}
          value={
            <Box>
              <Box>{formData.district}</Box>
              {formData.districtEn && <Box sx={{ fontSize: '13px', color: 'var(--mac-text-secondary, #aaaaaa)', mt: 0.5 }}>{formData.districtEn}</Box>}
            </Box>
          }
          isEditing={editingSection === 'district'}
          onEdit={() => setEditingSection('district')}
          onCancel={onPillCancel}
          onSave={onPillSave}
        >
          <Box sx={{ display: 'flex', flexDirection: 'column', gap: 2 }}>
            <TextField label={t('districtTamil') || 'District (Tamil)'} value={formData.district} onChange={e => setFormData({ ...formData, district: e.target.value })} fullWidth size="small" />
            <TextField label={t('districtEnglish') || 'District (English)'} value={formData.districtEn} onChange={e => setFormData({ ...formData, districtEn: e.target.value })} fullWidth size="small" />
          </Box>
        </SettingsPillRow>

        <SettingsPillRow
          label={t('pincode') || 'Pincode'}
          value={formData.pincode}
          isEditing={editingSection === 'pincode'}
          onEdit={() => setEditingSection('pincode')}
          onCancel={onPillCancel}
          onSave={onPillSave}
        >
          <TextField label={t('pincode') || 'Pincode'} value={formData.pincode} onChange={e => setFormData({ ...formData, pincode: e.target.value })} fullWidth size="small" />
        </SettingsPillRow>
      </SettingsPillContainer>
    </Box>
  );
}
