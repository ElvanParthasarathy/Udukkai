import React, { useState } from 'react';
import { PaintBrush, Trash, ImageSquare, PencilSimple } from '@phosphor-icons/react';
import { Box, Paper, Typography, Button, IconButton, TextField } from '@mui/material';
import { SettingsPillContainer, SettingsPillRow } from '../../ElvanSettingsSection';

export default function BrandingSettings({ 
  formData, setFormData, 
  logoInputRef, sigInputRef, 
  handleLogoChange, handleSignatureChange, 
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
      <SettingsPillContainer title={t('appearance') || 'Branding & Signatures'} icon={<PaintBrush size={20} weight="fill"Icon />} iconColor="purple">
        <SettingsPillRow
          label={t('organizationLogo') || 'Organization Logo'}
          value={
            <Box sx={{ display: 'inline-flex', alignItems: 'center', bgcolor: 'rgba(255,255,255,0.08)', px: 1.5, py: 0.5, borderRadius: 1, fontSize: '13px', color: 'var(--mac-text-secondary)', fontWeight: 500 }}>
              {t('upcomingFeature') || 'Upcoming Feature'}
            </Box>
          }
          isEditing={false}
          disableEdit={true}
          onEdit={() => {}}
          onCancel={onPillCancel}
          onSave={onPillSave}
        >
          <Box />
        </SettingsPillRow>

        <SettingsPillRow
          label={t('signature') || 'Signature'}
          value={
            <Box sx={{ display: 'flex', flexDirection: 'column', gap: 0.5 }}>
              {formData.signature ? <img src={formData.signature} alt="Signature" style={{ height: 40, objectFit: 'contain' }} /> : 'No Signature'}
              <Typography sx={{ fontSize: '12px', color: 'text.secondary' }}>{formData.authorizedSignatoryName}</Typography>
            </Box>
          }
          isEditing={editingSection === 'signature'}
          onEdit={() => setEditingSection('signature')}
          onCancel={onPillCancel}
          onSave={onPillSave}
        >
          <Paper variant="outlined" sx={{ p: 2, display: 'flex', flexDirection: 'column', alignItems: 'center', gap: 2, borderStyle: 'dashed', bgcolor: 'rgba(0,0,0,0.2)' }}>
            {formData.signature ? (
              <>
                <Box sx={{ position: 'relative' }}>
                  <img src={formData.signature} alt="Signature" style={{ maxHeight: '100px', maxWidth: '200px', objectFit: 'contain' }} />
                  <IconButton size="small" color="error" onClick={() => setFormData(prev => ({ ...prev, signature: '' }))} sx={{ position: 'absolute', top: -10, right: -10, bgcolor: 'background.paper', '@media (hover: hover)': { '&:hover': { bgcolor: 'error.light' } } }}>
                    <Trash size={20} weight="fill" sx={{ fontSize: 14 }} />
                  </IconButton>
                </Box>
              </>
            ) : (
              <Button variant="outlined" onClick={() => sigInputRef.current?.click()} startIcon={<PencilSimple size={20} weight="fill" sx={{ fontSize: 20 }} />} sx={{ flexDirection: 'column', py: 3, gap: 1, width: '100%' }}>
                Upload Signature
              </Button>
            )}
            <input ref={sigInputRef} type="file" accept="image/*" style={{ display: 'none' }} onChange={handleSignatureChange} />
          </Paper>
          <TextField fullWidth size="small" sx={{ mt: 4 }} label={t('authorizedSignatoryName') || 'Authorized Signatory Name'} name="authorizedSignatoryName" value={formData.authorizedSignatoryName || ''} onChange={e => setFormData(prev => ({ ...prev, authorizedSignatoryName: e.target.value }))} placeholder="e.g. V.R.M. Elvan" />
        </SettingsPillRow>
      </SettingsPillContainer>
    </Box>
  );
}
