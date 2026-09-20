import React, { useState, useEffect, useRef } from 'react';
import { Trash, FloppyDisk, ImageSquare, Buildings, Gear, PencilSimple, Plus, Storefront, Palette, MapPin, Bank, X, AddressBook, ArrowLeft } from '@phosphor-icons/react';
import { 
  Box, Typography, Button, TextField, Select, MenuItem, useTheme, useMediaQuery, 
  Stack, FormControl, InputLabel, Divider, Chip, Dialog, DialogTitle, 
  DialogContent, DialogActions, List, ListItem, ListItemText, ListItemSecondaryAction, 
  IconButton, Paper, Grid, Card, CardActionArea, CardContent, ButtonBase, Collapse
} from '@mui/material';

import { getAllCoolieProfiles, saveCoolieProfile, deleteCoolieProfile } from '../../../Avanam';
import { thagaval } from '../../Thagaval';
import { useLanguage } from '../../../mozhi/LanguageContext';

import BusinessInfoSettings from './BusinessInfoSettings';
import AddressSettings from './AddressSettings';
import BrandingSettings from './BrandingSettings';
import ContactBankSettings from './ContactBankSettings';
import AppearanceSettings from './AppearanceSettings';

// ─── Helpers ───
function deepEqual(a: any, b: any) {
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

// Fields tracked for "Save" button (excludes auto-saved settings)
const FORM_FIELDS = [
  'name', 'nameEn', 'shortBusinessName',
  'address', 'addressEn', 'city', 'cityEn', 'district', 'districtEn', 'pincode',
  'bankName', 'bankNameEn', 'accountNo', 'ifsc', 'branch', 'branchEn',
  'email', 'logo', 'signature', 'authorizedSignatoryName', 'phone'
];

function pickSnapshot(data: any, phonesArray: string[]) {
  const out: any = {};
  FORM_FIELDS.forEach(k => {
    if (k === 'phone') {
      out[k] = phonesArray.filter(x => x.trim() !== '').join(',');
    } else {
      out[k] = data[k] ?? '';
    }
  });
  return out;
}

const DEFAULT_FORM_DATA = {
  name: '', nameEn: '', shortBusinessName: '',
  address: '', addressEn: '', city: '', cityEn: '', district: '', districtEn: '', pincode: '',
  bankName: '', bankNameEn: '', accountNo: '', ifsc: '', branch: '', branchEn: '',
  email: '', logo: '', signature: '', authorizedSignatoryName: '',
  themeColor: '#388e3c', defaultPrintLanguage: 'ta', receiptLanguage: 'ta'
};

export default function CoolieSettings({ activeTab = 'business' }: any) {
  const { t, language } = useLanguage();
  const theme = useTheme();
  const isMobile = useMediaQuery(theme.breakpoints.down('md'));

  const activeSubTab = activeTab === 'business' ? 0 : Number(activeTab);

  // ─── Profile Management ───
  const [profiles, setProfiles] = useState<any[]>([]);
  const [selectedId, setSelectedId] = useState(() => localStorage.getItem('coolie_last_selected_profile') || 'new');
  const [isLoading, setIsLoading] = useState(true);
  const [showManageProfilesModal, setShowManageProfilesModal] = useState(false);
  const [showNewProfileModal, setShowNewProfileModal] = useState(false);
  const [creatingProfile, setCreatingProfile] = useState(false);
  const [newProfileData, setNewProfileData] = useState({ name: '', nameEn: '' });
  const [isProfileEditing, setIsProfileEditing] = useState(false);
  const [deleteConfirmId, setDeleteConfirmId] = useState<string | null>(null);

  // ─── Form State ───
  const [phones, setPhones] = useState(['']);
  const [formData, setFormData] = useState({ ...DEFAULT_FORM_DATA });
  const [saving, setSaving] = useState(false);

  // ─── Snapshot for dirty detection ───
  const [savedSnapshot, setSavedSnapshot] = useState<any>(null);

  const isDirty = savedSnapshot ? !deepEqual(pickSnapshot(formData, phones), savedSnapshot) : false;

  const formDataRef = useRef(formData);
  const phonesRef = useRef(phones);
  const snapshotRef = useRef(savedSnapshot);

  useEffect(() => {
    formDataRef.current = formData;
    phonesRef.current = phones;
    snapshotRef.current = savedSnapshot;
  }, [formData, phones, savedSnapshot]);

  // ─── Data Loading ───
  const loadProfiles = async (idToSelect = null) => {
    setIsLoading(true);
    try {
      const handleFreshData = (data: any[]) => {
        setProfiles(data);
        if (data.length === 0) {
          handleAddNew();
        } else {
          const targetId = idToSelect || (selectedId !== 'new' ? selectedId : data[0].id);
          const current = data.find(p => p.id === targetId) || data[0];
          
          const currentForm = formDataRef.current;
          const currentSnapshot = snapshotRef.current;
          const currentPhones = phonesRef.current;
          
          // Only update form if it was not dirty, OR if it's the initial load
          const isCurrentlyDirty = currentSnapshot ? !deepEqual(pickSnapshot(currentForm, currentPhones), currentSnapshot) : false;
          
          if (!isCurrentlyDirty) {
            const loadedData = { ...DEFAULT_FORM_DATA, ...current };
            const loadedPhones = current.phone ? current.phone.split(',').filter(Boolean) : [''];
            if (loadedPhones.length === 0) loadedPhones.push('');
            
            setFormData(loadedData);
            setPhones(loadedPhones);
            setSavedSnapshot(pickSnapshot(loadedData, loadedPhones));
          }
        }
      };

      const data = await getAllCoolieProfiles(handleFreshData);
      setProfiles(data);
      if (data.length === 0) {
        handleAddNew();
      } else {
        const targetId = idToSelect || (selectedId !== 'new' ? selectedId : data[0].id);
        const current = data.find(p => p.id === targetId) || data[0];
        selectProfile(current);
      }
    } catch {
      thagaval(t('error') || 'Error loading profiles', 'error');
    } finally {
      setIsLoading(false);
    }
  };

  useEffect(() => {
    loadProfiles();
    
    const handleUpdate = (e: any) => {
      setProfiles(e.detail);
      const currentSelected = e.detail.find((p: any) => p.id === selectedId);
      if (currentSelected) {
         const currentForm = formDataRef.current;
         const currentSnapshot = snapshotRef.current;
         const currentPhones = phonesRef.current;

         const isCurrentlyDirty = currentSnapshot ? !deepEqual(pickSnapshot(currentForm, currentPhones), currentSnapshot) : false;
         if (!isCurrentlyDirty) {
            const loadedData = { ...DEFAULT_FORM_DATA, ...currentSelected };
            const loadedPhones = currentSelected.phone ? currentSelected.phone.split(',').filter(Boolean) : [''];
            if (loadedPhones.length === 0) loadedPhones.push('');
            
            setFormData(loadedData);
            setPhones(loadedPhones);
            setSavedSnapshot(pickSnapshot(loadedData, loadedPhones));
         }
      }
    };
    
    window.addEventListener('elvan_update_coolie_profiles', handleUpdate);
    return () => {
      window.removeEventListener('elvan_update_coolie_profiles', handleUpdate);
    };
  }, [selectedId]);

  const selectProfile = (p: any) => {
    setSelectedId(p.id);
    localStorage.setItem('coolie_last_selected_profile', p.id);
    const loadedData = { ...DEFAULT_FORM_DATA, ...p };
    const loadedPhones = p.phone ? p.phone.split(',').filter(Boolean) : [''];
    if (loadedPhones.length === 0) loadedPhones.push('');
    
    setFormData(loadedData);
    setPhones(loadedPhones);
    setSavedSnapshot(pickSnapshot(loadedData, loadedPhones));
  };

  const handleAddNew = () => {
    setSelectedId('new');
    setFormData({ ...DEFAULT_FORM_DATA });
    setPhones(['']);
    setSavedSnapshot(pickSnapshot(DEFAULT_FORM_DATA, ['']));
  };

  // ─── Handlers ───
  const handleCreateNewProfile = async () => {
    if (!newProfileData.name.trim()) { thagaval('Business name required', 'warning'); return; }
    setCreatingProfile(true);
    try {
      const freshProfile = { ...DEFAULT_FORM_DATA, name: newProfileData.name, nameEn: newProfileData.nameEn };
      
      // Auto-save current if dirty and valid (though typically we prompt first)
      if (isDirty && (formData.name || formData.nameEn) && selectedId !== 'new') {
         await saveCoolieProfile({ ...formData, phone: phones.filter(x => x.trim() !== '').join(','), id: selectedId });
      }

      const saved = await saveCoolieProfile(freshProfile);
      await loadProfiles(saved.id);
      
      setShowNewProfileModal(false);
      setNewProfileData({ name: '' });
      thagaval(`Created and switched to ${saved.name}`, 'success');
    } catch (err) {
      thagaval('Failed to create profile', 'error');
    } finally {
      setCreatingProfile(false);
    }
  };

  const handleLoadProfile = async (bp: any) => {
    if (isDirty) {
      if (!confirm(t('unsavedChangesContinue'))) return;
    }
    selectProfile(bp);
  };

  const handleDeleteProfile = (id: string) => {
    setDeleteConfirmId(id);
  };

  const confirmDeleteProfile = async () => {
    if (!deleteConfirmId) return;
    await deleteCoolieProfile(deleteConfirmId);
    thagaval(t('profileDeleted'), 'success');
    loadProfiles(profiles.find(p => p.id !== deleteConfirmId)?.id || 'new');
    setDeleteConfirmId(null);
  };

  const handleSave = async () => {
    if (!formData.name && !formData.nameEn) {
      thagaval(t('orgNameRequired') || 'Organization Name is required', 'warning');
      return;
    }

    try {
      setSaving(true);
      const payload = {
        ...formData,
        phone: phones.filter(x => x.trim() !== '').join(','),
        ...(selectedId !== 'new' ? { id: selectedId } : {})
      };
      
      const saved = await saveCoolieProfile(payload);
      setSelectedId(saved.id);
      setSavedSnapshot(pickSnapshot(saved, phones));
      
      // Refresh the profiles list to update the dropdown names if they changed
      const data = await getAllCoolieProfiles();
      setProfiles(data);

      thagaval(t('profileSaved') || 'Profile Saved successfully', 'success');
    } catch (error: any) {
      thagaval(`Error: ${error.message}`, 'error');
    } finally {
      setSaving(false);
    }
  };

  const handleDiscard = async () => {
    if (!savedSnapshot) return;
    if (selectedId !== 'new') {
      const data = await getAllCoolieProfiles();
      setProfiles(data);
      const current = data.find(p => p.id === selectedId);
      if (current) selectProfile(current);
    } else {
      handleAddNew();
    }
  };

  const handleAutoSaveSetting = async (key: string, val: any) => {
    const updatedData = { ...formData, [key]: val };
    setFormData(updatedData);

    if (selectedId !== 'new') {
       try {
         const payload = {
            ...updatedData,
            phone: phones.filter(x => x.trim() !== '').join(','),
            id: selectedId
         };
         await saveCoolieProfile(payload);
         thagaval(t('settingsSaved'), 'success');
       } catch (err) {
         thagaval('Failed to save settings', 'error');
       }
    }
  };

  const handleLogoChange = (e: any) => {
    const file = e.target.files[0];
    if (file) {
      const reader = new FileReader();
      reader.onloadend = () => setFormData(prev => ({ ...prev, logo: reader.result as string }));
      reader.readAsDataURL(file);
    }
  };

  const handleSignatureChange = (e: any) => {
    const file = e.target.files[0];
    if (file) {
      const reader = new FileReader();
      reader.onloadend = () => setFormData(prev => ({ ...prev, signature: reader.result as string }));
      reader.readAsDataURL(file);
    }
  };

  const logoInputRef = useRef<HTMLInputElement>(null);
  const sigInputRef = useRef<HTMLInputElement>(null);

  // ─── Rendering ───
  const useTamilFirst = language === 'ta';

  if (isLoading) return <Box sx={{ minHeight: '100vh' }}></Box>;

  return (
    <Box sx={{ display: 'flex', flexDirection: 'column', gap: 0, pb: 4 }}>


      {/* Drill Down Views */}
      {activeSubTab === 0 && (
        <>
          {/* Profile Switcher */}
          <Paper elevation={0} sx={{ mb: 3, borderRadius: '24px', bgcolor: 'var(--mac-card-bg, #1c1c1e)', border: 'none', overflow: 'hidden' }}>
            <Box sx={{ p: '16px 20px', display: 'flex', flexDirection: 'column', bgcolor: 'transparent' }}>
              <Typography sx={{ fontSize: '13px', color: 'var(--mac-text-secondary, #aaaaaa)', mb: '4px', fontWeight: 500, ml: 2 }}>
                {t('activeProfile')}
              </Typography>
              <Box sx={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', width: '100%' }}>
                <Select 
                  fullWidth size="small"
                  value={selectedId} 
                  onChange={(e) => {
                    const val = e.target.value;
                    if (val === 'new') handleAddNew();
                    else if (val === 'manage') setShowManageProfilesModal(true);
                    else {
                      const bp = profiles.find(p => p.id === val);
                      if (bp) handleLoadProfile(bp);
                    }
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
                  {profiles.map(p => (
                    <MenuItem key={p.id} value={p.id}>
                      {useTamilFirst ? (p.name || p.nameEn || 'Unnamed') : (p.nameEn || p.name || 'Unnamed')}
                    </MenuItem>
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
                      {t('manageBtn')}
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
                      {t('addNewBtn')}
                    </ButtonBase>
                  </Box>
                </Box>
              </Collapse>
              </Box>
          </Paper>
          <BusinessInfoSettings formData={formData} setFormData={setFormData} phones={phones} setPhones={setPhones} t={t} handleSave={handleSave} handleDiscard={handleDiscard} />
        </>
      )}

      {activeSubTab === 1 && (
        <>
          <AddressSettings formData={formData} setFormData={setFormData} t={t} handleSave={handleSave} handleDiscard={handleDiscard} />
        </>
      )}

      {activeSubTab === 2 && (
        <BrandingSettings formData={formData} setFormData={setFormData} logoInputRef={logoInputRef} sigInputRef={sigInputRef} handleLogoChange={handleLogoChange} handleSignatureChange={handleSignatureChange} t={t} handleSave={handleSave} handleDiscard={handleDiscard} />
      )}

      {activeSubTab === 3 && (
        <>
          <ContactBankSettings formData={formData} setFormData={setFormData} phones={phones} setPhones={setPhones} t={t} handleSave={handleSave} handleDiscard={handleDiscard} />
        </>
      )}

      {activeSubTab === 4 && (
        <AppearanceSettings formData={formData} setFormData={setFormData} handleAutoSaveSetting={handleAutoSaveSetting} t={t} language={language} handleSave={handleSave} handleDiscard={handleDiscard} />
      )}

      {/* ─── Modals ─── */}

      {/* Create New Profile */}
      <Dialog open={showNewProfileModal} onClose={() => setShowNewProfileModal(false)} disableScrollLock fullScreen={isMobile}>
        <DialogTitle>{t('createNewProfileModalTitle')}</DialogTitle>
        <DialogContent sx={{ minWidth: { sm: 400 } }}>
          <TextField autoFocus margin="dense" label={t('businessNameStar')} fullWidth value={newProfileData.name} onChange={(e) => setNewProfileData({ ...newProfileData, name: e.target.value })} />
          <TextField margin="dense" label={t('businessNameEnglish')} fullWidth value={newProfileData.nameEn} onChange={(e) => setNewProfileData({ ...newProfileData, nameEn: e.target.value })} />
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setShowNewProfileModal(false)}>{t('cancelBtn')}</Button>
          <Button variant="contained" onClick={handleCreateNewProfile} disabled={creatingProfile || !newProfileData.name.trim()}>
            {creatingProfile ? '...' : t('createBtn')}
          </Button>
        </DialogActions>
      </Dialog>

      {/* Manage Profiles Dialog */}
      <Dialog open={showManageProfilesModal} onClose={() => setShowManageProfilesModal(false)} disableScrollLock maxWidth="sm" fullWidth fullScreen={isMobile}>
        <DialogTitle>{t('manageBusinessProfilesModalTitle')}</DialogTitle>
        <DialogContent dividers>
          {profiles.length === 0 ? (
            <Typography variant="body2" color="text.secondary" sx={{ py: 4, textAlign: 'center' }}>
              {t('noSavedProfilesYet')}
            </Typography>
          ) : (
            <List disablePadding>
              {profiles.map((bp, idx) => {
                const isActive = bp.id === selectedId;
                return (
                  <React.Fragment key={bp.id}>
                    {idx > 0 && <Divider />}
                    <ListItem sx={{ py: 1.5 }}>
                      <ListItemText 
                        primary={
                          <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                            {useTamilFirst ? (bp.name || bp.nameEn) : (bp.nameEn || bp.name)}
                          </Box>
                        }
                        secondary={
                          isActive ? (
                            <Box sx={{ mt: 0.5 }}>
                              <Typography variant="caption" sx={{ color: 'text.secondary' }}>
                                {t('currentlyEditing')}
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
        <DialogTitle>{t('confirmDeleteModalTitle')}</DialogTitle>
        <DialogContent>
          <Typography>{t('permanentlyDeleteProfile')}</Typography>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setDeleteConfirmId(null)}>{t('cancelBtn')}</Button>
          <Button color="error" variant="contained" onClick={confirmDeleteProfile}>{t('deleteBtn')}</Button>
        </DialogActions>
      </Dialog>
    </Box>
  );
}
