import React, { useState, useRef, useEffect } from 'react';
import { FloppyDisk, Trash, X, ArrowLeft } from '@phosphor-icons/react';
import { 
  Box, Paper, TextField, Button, Typography, IconButton, Grid,
  Dialog, DialogTitle, DialogContent, DialogActions,
  Chip, List, ListItem, ListItemText, ListItemSecondaryAction, Divider,
  useTheme, useMediaQuery
} from '@mui/material';

import { 
  Business as BusinessIcon,
  AccountBalance as BankIcon,
  Receipt as InvoiceIcon,
  Translate as LanguageIcon
} from '@mui/icons-material';

import { useLanguage } from '../../../mozhi/LanguageContext';
import { saveProfile, getProfile, saveBusinessProfile, deleteBusinessProfile } from '../../../Avanam';
import { validateTaxId, detectCountryFromBrowser } from '../../../Payanpadu';
import { thagaval } from '../../Thagaval';

import BusinessSettings from './BusinessSettings';
import AddressSettings from './AddressSettings';
import BrandingSettings from './BrandingSettings';
import BankSettings from './BankSettings';
import InvoiceSettings from './InvoiceSettings';
import LanguageSettings from './LanguageSettings';
import { SettingsGroup, SettingsItem, SettingsDivider } from '../../Amaippugal/SettingsShared';

// ─── Helpers ───
function deepEqual(a: any, b: any): boolean {
  if (a === b) return true;
  if (!a || !b) return a === b;
  const keysA = Object.keys(a);
  const keysB = Object.keys(b);
  if (keysA.length !== keysB.length) return false;
  return keysA.every(k => {
    if (typeof a[k] === 'object' && typeof b[k] === 'object') return deepEqual(a[k], b[k]);
    return a[k] === b[k];
  });
}

// Profile fields that matter for dirty-checking
const PROFILE_FIELDS = [
  'niruvanathinPeyar', 'niruvanathinPeyarEn', 'shortBusinessName',
  'mugavari', 'mugavariEn', 'oor', 'oorEn', 'maavattam', 'maavattamEn',
  'maanilam', 'maanilamEn', 'country', 'countryEn', 'pin',
  'tholaipesi', 'email', 'mobileNumber', 'gstin', 'pan',
  'logo', 'wideLogo', 'logoHeight', 'signature', 'authorizedSignatoryName',
  'vangiPeyar', 'vangiPeyarEn', 'bankBranch', 'bankBranchEn', 'kanakkuEn', 'ifsc',
  'primaryDataLanguage', 'secondaryDataLanguage', 'enableBilingual',
];

export function pickProfileFields(p: any) {
  const out: any = {};
  PROFILE_FIELDS.forEach(k => { out[k] = p?.[k] ?? ''; });
  return out;
}

export default function GstSettings({ 
  profile, setProfile, onSaved, businessProfiles, loadBusinessProfiles, saving, setSaving, activeTab = 'business'
}) {
  const { language, t } = useLanguage();
  const theme = useTheme();
  const isMobile = useMediaQuery(theme.breakpoints.down('md'));

  // ─── Profile management ───
  const [showNewProfileModal, setShowNewProfileModal] = useState(false);
  const [showManageProfilesModal, setShowManageProfilesModal] = useState(false);
  const [newProfileData, setNewProfileData] = useState({ niruvanathinPeyar: '', gstin: '', mugavari: '' });
  const [creatingProfile, setCreatingProfile] = useState(false);
  const [taxIdWarning, setTaxIdWarning] = useState('');
  const [deleteConfirmId, setDeleteConfirmId] = useState<string | null>(null);

  // ─── Snapshot for dirty detection & cancel revert ───
  const [savedSnapshot, setSavedSnapshot] = useState<any>(null);
  
  useEffect(() => {
    // Take a snapshot when profile loads / changes externally
    if (profile && !savedSnapshot) {
      setSavedSnapshot(pickProfileFields(profile));
    }
  }, [profile]);

  const isDirty = savedSnapshot ? !deepEqual(pickProfileFields(profile), savedSnapshot) : false;

  // ─── Refs ───
  const logoInputRef = useRef(null);
  const wideLogoInputRef = useRef(null);
  const sigInputRef = useRef(null);

  // ─── Handlers ───
  const handleChange = (e) => {
    const { name, value } = e.target;
    setProfile(prev => ({ ...prev, [name]: value }));
  };

  const handleImageUpload = (field, e) => {
    const file = e.target.files?.[0];
    if (!file) return;
    if (file.size > 500 * 1024) { thagaval('Image must be under 500KB', 'warning'); return; }
    const reader = new FileReader();
    reader.onload = (ev) => setProfile(prev => ({ ...prev, [field]: ev.target.result }));
    reader.readAsDataURL(file);
  };

  const removeImage = (field) => setProfile(prev => ({ ...prev, [field]: '' }));

  const handleTaxIdBlur = () => {
    const result = validateTaxId(profile.country, profile.gstin);
    setTaxIdWarning(result.ok ? '' : result.message);
  };

  // ─── Save (unified for all text-field tabs) ───
  const handleSave = async () => {
    try {
      setSaving(true);
      await saveProfile(profile);
      if (onSaved) onSaved(profile);
      setSavedSnapshot(pickProfileFields(profile));
      thagaval(t('profileSaved'), 'success');
    } catch { thagaval('Failed to save profile', 'error'); }
    finally { setSaving(false); }
  };

  // ─── Discard unsaved changes ───
  const handleDiscard = async () => {
    if (!savedSnapshot) return;
    const fresh = await getProfile();
    setProfile(prev => ({ ...prev, ...fresh }));
    setSavedSnapshot(pickProfileFields(fresh));
  };

  // ─── Profile switching ───
  const handleLoadProfile = async (bp) => {
    if (isDirty) {
      if (!confirm(t('unsavedChangesContinue'))) return;
    }
    if (profile.niruvanathinPeyar?.trim()) {
      const existing = businessProfiles.find(p => (p.niruvanathinPeyar || '').trim().toLowerCase() === (profile.niruvanathinPeyar || '').trim().toLowerCase());
      await saveBusinessProfile({ ...profile, id: existing?.id || undefined });
    }
    const loaded = { ...bp };
    delete loaded.id;
    setProfile(loaded);
    await saveProfile(loaded);
    setSavedSnapshot(pickProfileFields(loaded));
    if (onSaved) onSaved(loaded);
    thagaval(`Switched to ${bp.niruvanathinPeyar}`, 'success');
  };

  const handleDeleteProfile = (id) => {
    setDeleteConfirmId(id);
  };

  const confirmDeleteProfile = async () => {
    if (!deleteConfirmId) return;
    const isActive = businessProfiles.find(p => p.id === deleteConfirmId)?.niruvanathinPeyar?.trim().toLowerCase() === (profile.niruvanathinPeyar || '').trim().toLowerCase();
    await deleteBusinessProfile(deleteConfirmId);
    thagaval(t('profileDeleted'), 'success');
    
    const remaining = businessProfiles.filter(p => p.id !== deleteConfirmId);
    if (isActive) {
      if (remaining.length > 0) {
        handleLoadProfile(remaining[0]);
      } else {
        setProfile({ niruvanathinPeyar: '' });
        setSavedSnapshot(null);
      }
    }
    loadBusinessProfiles();
    setDeleteConfirmId(null);
  };

  const handleCreateNewProfile = async () => {
    if (!newProfileData.niruvanathinPeyar.trim()) { thagaval('Business name required', 'warning'); return; }
    setCreatingProfile(true);
    try {
      const freshProfile = {
        niruvanathinPeyar: newProfileData.niruvanathinPeyar,
        niruvanathinPeyarEn: '', mugavari: newProfileData.mugavari, mugavariEn: '', oor: '', oorEn: '', maavattam: '', maavattamEn: '', maanilam: '', maanilamEn: '', pin: '', country: detectCountryFromBrowser(),
        gstin: newProfileData.gstin, pan: '', email: '', tholaipesi: '', mobileNumber: '', vangiPeyar: '', kanakkuEn: '', ifsc: '', swift: '',
        logo: '', wideLogo: '', logoHeight: 48, signature: '', upiId: '',
      };
      
      const saved = await saveBusinessProfile(freshProfile);
      
      if (profile.niruvanathinPeyar?.trim()) {
        const existing = businessProfiles.find(p => (p.niruvanathinPeyar || '').trim().toLowerCase() === (profile.niruvanathinPeyar || '').trim().toLowerCase());
        await saveBusinessProfile({ ...profile, id: existing?.id || undefined });
      }
      
      const loaded = { ...saved };
      delete loaded.id;
      setProfile(loaded);
      await saveProfile(loaded);
      setSavedSnapshot(pickProfileFields(loaded));
      if (onSaved) onSaved(loaded);
      
      await loadBusinessProfiles();
      setShowNewProfileModal(false);
      setNewProfileData({ niruvanathinPeyar: '', gstin: '', mugavari: '' });
      thagaval(`Created and switched to ${saved.niruvanathinPeyar}`, 'success');
    } catch (err) {
      thagaval('Failed to create profile', 'error');
    } finally {
      setCreatingProfile(false);
    }
  };

  const renderSaveBar = () => (
    <Box sx={{ 
      mt: 4, display: 'flex', justifyContent: 'flex-end', alignItems: 'center', gap: 2,
      position: 'sticky', bottom: 0, py: 2, px: 1,
      bgcolor: 'background.paper',
      borderTop: isDirty ? '1px solid' : 'none',
      borderColor: 'divider',
      zIndex: 10,
    }}>
      {isDirty && (
        <>
          <Chip 
            label={t('unsavedChanges')} 
            color="warning" size="small" variant="outlined"
            sx={{ fontWeight: 600 }}
          />
          <Button variant="text" size="small" color="inherit" onClick={handleDiscard}>
            {t('discardBtn')}
          </Button>
        </>
      )}
      <Button 
        variant="contained" color="primary" 
        disabled={saving || !isDirty} 
        onClick={handleSave} 
        startIcon={<FloppyDisk size={20} weight="fill" sx={{ fontSize: 18 }} />}
      >
        {saving ? (t('savingStatus')) : (t('saveBtn'))}
      </Button>
    </Box>
  );

  return (
    <>


      {activeTab === 'business' && (
        <BusinessSettings
          profile={profile}
          handleChange={handleChange}
          language={language}
          t={t}
          taxIdWarning={taxIdWarning}
          handleTaxIdBlur={handleTaxIdBlur}
          logoInputRef={logoInputRef}
          wideLogoInputRef={wideLogoInputRef}
          sigInputRef={sigInputRef}
          removeImage={removeImage}
          handleImageUpload={handleImageUpload}
          businessProfiles={businessProfiles}
          handleLoadProfile={handleLoadProfile}
          setShowManageProfilesModal={setShowManageProfilesModal}
          setShowNewProfileModal={setShowNewProfileModal}
          renderSaveBar={renderSaveBar}
          handleSave={handleSave}
          handleDiscard={handleDiscard}
        />
      )}
      {activeTab === 'address' && (
        <AddressSettings
          profile={profile}
          handleChange={handleChange}
          language={language}
          t={t}
          handleSave={handleSave}
          handleDiscard={handleDiscard}
        />
      )}
      {activeTab === 'branding' && (
        <BrandingSettings
          profile={profile}
          handleChange={handleChange}
          language={language}
          t={t}
          handleSave={handleSave}
          handleDiscard={handleDiscard}
          logoInputRef={logoInputRef}
          wideLogoInputRef={wideLogoInputRef}
          sigInputRef={sigInputRef}
          removeImage={removeImage}
          handleImageUpload={handleImageUpload}
        />
      )}
      {activeTab === 'bank' && (
        <BankSettings
          profile={profile}
          handleChange={handleChange}
          language={language}
          t={t}
          handleSave={handleSave}
          handleDiscard={handleDiscard}
        />
      )}
      {activeTab === 'invoice' && (
        <InvoiceSettings
          language={language}
          t={t}
          handleSave={handleSave}
          handleDiscard={handleDiscard}
        />
      )}
      {activeTab === 'languages' && (
        <LanguageSettings
          profile={profile}
          setProfile={setProfile}
          setSavedSnapshot={setSavedSnapshot}
          pickProfileFields={pickProfileFields}
          onSaved={onSaved}
          language={language}
          t={t}
          handleSave={handleSave}
          handleDiscard={handleDiscard}
        />
      )}
      {/* ─── Modals ─── */}

      {/* Create New Profile */}
      <Dialog 
        open={showNewProfileModal} 
        onClose={() => setShowNewProfileModal(false)} 
        disableScrollLock 
        fullScreen={isMobile}
        PaperProps={{
          sx: {
            borderRadius: isMobile ? 0 : 3,
            bgcolor: 'var(--mac-window-bg, #1e1e1e)',
            backgroundImage: 'none'
          }
        }}
      >
        <DialogTitle sx={{ p: 3, pb: 1, fontWeight: 600 }}>{t('createNewProfile')}</DialogTitle>
        <DialogContent sx={{ minWidth: { sm: 400 }, p: 3 }}>
          <Box sx={{ py: 1 }}>
            <TextField 
              autoFocus 
              label={t('businessNameAsterisk')} 
              fullWidth 
              value={newProfileData.niruvanathinPeyar} 
              onChange={(e) => setNewProfileData({ ...newProfileData, niruvanathinPeyar: e.target.value })} 
            />
          </Box>
        </DialogContent>
        <DialogActions sx={{ p: 3, pt: 1, gap: 1 }}>
          <Button 
            color="inherit" 
            onClick={() => setShowNewProfileModal(false)}
            sx={{ px: 3, py: 1, borderRadius: 2 }}
          >
            {t('cancelBtn')}
          </Button>
          <Button 
            onClick={handleCreateNewProfile} 
            variant="contained" 
            disabled={creatingProfile || !newProfileData.niruvanathinPeyar.trim()}
            sx={{ px: 4, py: 1, borderRadius: 2 }}
          >
            {creatingProfile ? '...' : (t('createBtn'))}
          </Button>
        </DialogActions>
      </Dialog>

      {/* Manage Profiles Dialog */}
      <Dialog open={showManageProfilesModal} onClose={() => setShowManageProfilesModal(false)} disableScrollLock maxWidth="sm" fullWidth fullScreen={isMobile}>
        <DialogTitle>{t('manageBusinessProfiles')}</DialogTitle>
        <DialogContent dividers>
          {businessProfiles.length === 0 ? (
            <Typography variant="body2" color="text.secondary" sx={{ py: 4, textAlign: 'center' }}>
              {t('noSavedProfiles')}
            </Typography>
          ) : (
            <List disablePadding>
              {businessProfiles.map((bp, idx) => {
                const isActive = (bp.niruvanathinPeyar || '').trim().toLowerCase() === (profile.niruvanathinPeyar || '').trim().toLowerCase();
                return (
                  <React.Fragment key={bp.id}>
                    {idx > 0 && <Divider />}
                    <ListItem sx={{ py: 1.5 }}>
                      <ListItemText 
                        primary={
                          <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                            {bp.niruvanathinPeyar}
                          </Box>
                        }
                        secondary={
                          isActive ? (
                            <Box sx={{ mt: 0.5 }}>
                              <Typography variant="caption" sx={{ color: 'primary.main' }}>
                                {t('activeProfile')}
                              </Typography>
                            </Box>
                          ) : null
                        }
                      />
                      <ListItemSecondaryAction>
                        <IconButton 
                          edge="end" 
                          color="error" 
                          size="small"
                          onClick={() => handleDeleteProfile(bp.id)}
                          title={t('deleteBtn')}
                        >
                          <Trash size={20} weight="fill" sx={{ fontSize: 18 }} />
                        </IconButton>
                      </ListItemSecondaryAction>
                    </ListItem>
                  </React.Fragment>
                );
              })}
            </List>
          )}
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setShowManageProfilesModal(false)}>{t('closeBtn')}</Button>
        </DialogActions>
      </Dialog>

      {/* Delete Confirmation Modal */}
      <Dialog open={!!deleteConfirmId} onClose={() => setDeleteConfirmId(null)} disableScrollLock>
        <DialogTitle>{t('confirmDeleteTitle')}</DialogTitle>
        <DialogContent>
          <Typography>{t('permanentlyDeleteProfile')}</Typography>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setDeleteConfirmId(null)}>{t('cancelBtn')}</Button>
          <Button color="error" variant="contained" onClick={confirmDeleteProfile}>{t('deleteBtn')}</Button>
        </DialogActions>
      </Dialog>
    </>
  );
}
