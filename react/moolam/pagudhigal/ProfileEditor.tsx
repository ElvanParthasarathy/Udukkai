// @ts-nocheck
import { useState, useEffect, useRef } from 'react';
import { getProfile, saveProfile, exportAllData, importData, inspectBackup, getAllProfiles, saveBusinessProfile, deleteBusinessProfile, getInvoiceNumberSettings, saveInvoiceNumberSettings, getReceiptNumberSettings, saveReceiptNumberSettings, getInvoiceDisplayOptions, saveInvoiceDisplayOptions } from '../Avanam';
import { getCountryConfig, getStatesForCountry, getBilingualStateName, getBilingualCountryName, validateTaxId, detectCountryFromBrowser, getCountriesForRegion, getPaymentAccounts, createEmptyAccount, maskkanakkuEn, reorderAccounts, setDefaultAccount, isValidUpiId, tamilNaduDistricts } from '../Payanpadu';
import { thagaval } from './Thagaval';
import { useLanguage } from '../mozhi/LanguageContext';
import { motion, AnimatePresence } from 'framer-motion';
import '../styles/settings/shared.css';
import '../styles/settings/hub.css';

// MUI Imports
import { 
  Box, Paper, TextField, Button, Select, MenuItem, Checkbox,
  FormControlLabel, Typography, IconButton, Grid, Chip, Autocomplete,
  Divider, InputAdornment, FormControl, InputLabel,
  Dialog, DialogTitle, DialogContent, DialogActions, DialogContentText,
  List, ListItem, ListItemText, ListItemSecondaryAction, Tabs, Tab, Fab
} from '@mui/material';
import { Material3Switch } from './Amaippugal/Material3Switch';

const ACCENT_PRESETS = [
  { color: '#1e40af', label: 'Blue' },
  { color: '#7c3aed', label: 'Purple' },
  { color: '#0f766e', label: 'Teal' },
  { color: '#be123c', label: 'Red' },
  { color: '#c2410c', label: 'Orange' },
  { color: '#15803d', label: 'Green' },
  { color: '#0369a1', label: 'Sky' },
  { color: '#1e293b', label: 'Dark' },
];

const PDF_STYLES = [
  { id: 'classic', label: 'Classic', desc: 'Clean with top accent bar' },
  { id: 'modern', label: 'Modern', desc: 'Bold header with color block' },
  { id: 'minimal', label: 'Minimal', desc: 'Simple, borderless layout' },
];

export default function Amaippugal({ onSaved }) {
  const { language, setLanguage, t } = useLanguage();
  const [profile, setProfile] = useState<any>({
    niruvanathinPeyar: '', niruvanathinPeyarEn: '', mugavari: '', mugavariEn: '', oor: '', oorEn: '', maavattam: '', maavattamEn: '', maanilam: '', maanilamEn: '', gstin: '', pan: '',
    email: '', tholaipesi: '', mobileNumber: '', vangiPeyar: '', kanakkuEn: '', ifsc: '',
    logo: '', logoHeight: 48, signature: '', upiId: '',
    primaryDataLanguage: 'Tamil', secondaryDataLanguage: 'English', enableBilingual: true,
    invoiceTerms: '',
  });
  const [saving, setSaving] = useState(false);
  const [businessProfiles, setBusinessProfiles] = useState([]);
  const [invNumSettings, setInvNumSettings] = useState({
    format: 'branded', brandPrefix: '', separator: '/', showFinYear: true, startNumber: 1, padDigits: 4,
  });
  const [invNumSaving, setInvNumSaving] = useState(false);

  const [invoiceTemplate, setInvoiceTemplate] = useState<any>({});
  
  useEffect(() => {
    const savedLocal = localStorage.getItem('elvanniril_invoiceOptions');
    const local = savedLocal ? JSON.parse(savedLocal) : {};
    getInvoiceDisplayOptions().then(serverOpts => {
      setInvoiceTemplate({ ...local, ...(serverOpts || {}) });
    });
  }, []);

  const handleTemplateChange = (key, val) => {
    const newOpts = { ...invoiceTemplate, [key]: val };
    setInvoiceTemplate(newOpts);
    localStorage.setItem('elvanniril_invoiceOptions', JSON.stringify(newOpts));
    saveInvoiceDisplayOptions(newOpts);
  };


  const [rcpNumSettings, setRcpNumSettings] = useState({
    format: 'branded', brandPrefix: 'RCP', separator: '/', showFinYear: true, startNumber: 1, padDigits: 4,
  });
  const [rcpNumSaving, setRcpNumSaving] = useState(false);
  const [updateInfo, setUpdateInfo] = useState(null);
  const [checkingUpdate, setCheckingUpdate] = useState(false);
  const [editingLangSettings, setEditingLangSettings] = useState(false);

  const [isEditingCompany, setIsEditingCompany] = useState(false);
  const [showMobileField, setShowMobileField] = useState(false);
  const [activeTab, setActiveTab] = useState(0);
  const [isMobile, setIsMobile] = useState(window.innerWidth <= 768);
  const [direction, setDirection] = useState(0);
  const [currentView, setCurrentView] = useState(window.innerWidth <= 768 ? 'hub' : 0);
  
  useEffect(() => {
    const handleResize = () => {
        const mobile = window.innerWidth <= 768;
        setIsMobile(mobile);
        if (!mobile && currentView === 'hub') setCurrentView(0);
    };
    window.addEventListener('resize', handleResize);
    return () => window.removeEventListener('resize', handleResize);
  }, [currentView]);

  const handleNavigate = (viewId) => {
    setDirection(1);
    setCurrentView(viewId);
  };

  const goHub = () => {
    setDirection(-1);
    setCurrentView(isMobile ? 'hub' : 0);
  };

  const saveLangSettings = async () => {
    await saveProfile(profile);
    setEditingLangSettings(false);
    thagaval(t('languageSettingsSaved'), 'success');
  };


  const fileInputRef = useRef(null);
  const logoInputRef = useRef(null);
  const sigInputRef = useRef(null);
  const companyFormRef = useRef(null);
  const visibleCountries = getCountriesForRegion();

  useEffect(() => {
    getProfile().then(p => {
      setProfile({
        ...profile,
        ...p,
        primaryDataLanguage: p?.primaryDataLanguage || 'Tamil',
        secondaryDataLanguage: p?.secondaryDataLanguage || 'English',
        enableBilingual: p?.enableBilingual !== false
      });
    });
    loadBusinessProfiles();
    getInvoiceNumberSettings().then(setInvNumSettings);
    getReceiptNumberSettings().then(setRcpNumSettings);
  }, []);

  const loadBusinessProfiles = async () => setBusinessProfiles(await getAllProfiles(setBusinessProfiles));

  const handleChange = (e) => {
    const { name, value } = e.target;
    setProfile(prev => ({ ...prev, [name]: value }));
  };

  // ---- Payment Accounts manager ----
  // `editingAccount` = null ⇒ closed; an account object ⇒ form open for that
  // account; a fresh `createEmptyAccount()` ⇒ Add new flow. Saving merges back
  // into profile.paymentAccounts; profile.upiId/vangiPeyar/etc. are mirrored to
  // the DEFAULT account on save so legacy code paths and v1.4.x backups keep
  // working without a data migration.
  const [editingAccount, setEditingAccount] = useState(null);
  const [accountUpiWarning, setAccountUpiWarning] = useState('');

  const updateAccounts = (nextAccounts) => {
    // Mirror the default account's fields onto the profile's flat bank/UPI
    // fields. Means: a v1.4.x reader of the same profile.json still sees the
    // current default account's details, and the existing flat-field code
    // paths (e.g. legacy fallback in InvoicePreview) continue to work.
    const def = nextAccounts.find(a => a.isDefault) || nextAccounts[0];
    setProfile(prev => ({
      ...prev,
      paymentAccounts: nextAccounts,
      ...(def ? {
        vangiPeyar: def.vangiPeyar || '',
        kanakkuEn: def.kanakkuEn || '',
        ifsc: def.ifsc || '',
        swift: def.swift || '',
        upiId: def.upiId || '',
      } : {}),
    }));
  };

  const openAddAccount = () => {
    const fresh = createEmptyAccount();
    const existing = getPaymentAccounts(profile);
    // First account auto-marks Primary so the user never sees a "no default" maanilam.
    if (existing.length === 0) fresh.isDefault = true;
    setEditingAccount(fresh);
    setAccountUpiWarning('');
  };

  const openEditAccount = (acc) => {
    setEditingAccount({ ...acc });
    setAccountUpiWarning('');
  };

  const cancelAccount = () => { setEditingAccount(null); setAccountUpiWarning(''); };

  const saveAccountForm = () => {
    if (!editingAccount) return;
    const existing = getPaymentAccounts(profile).filter(a => a.id !== 'legacy');
    const idx = existing.findIndex(a => a.id === editingAccount.id);
    const next = idx >= 0 ? existing.map((a, i) => i === idx ? editingAccount : a) : [...existing, editingAccount];
    // Enforce: exactly one default (or zero if list empty).
    if (editingAccount.isDefault) {
      next.forEach(a => { if (a.id !== editingAccount.id) a.isDefault = false; });
    } else if (!next.some(a => a.isDefault) && next.length > 0) {
      next[0].isDefault = true;
    }
    updateAccounts(next);
    setEditingAccount(null);
    setAccountUpiWarning('');
    thagaval(idx >= 0 ? 'Account updated' : 'Account added', 'success');
  };

  const removeAccount = (acc) => {
    if (!confirm(`Delete payment account "${acc.label || acc.vangiPeyar || 'this account'}"? Existing invoices that used it keep their PDFs.`)) return;
    const next = getPaymentAccounts(profile).filter(a => a.id !== 'legacy' && a.id !== acc.id);
    if (next.length > 0 && !next.some(a => a.isDefault)) next[0].isDefault = true;
    updateAccounts(next);
    thagaval('Account removed', 'success');
  };

  const markDefault = (acc) => {
    const next = setDefaultAccount(getPaymentAccounts(profile).filter(a => a.id !== 'legacy'), acc.id);
    updateAccounts(next);
    thagaval(`Default → ${acc.label || acc.vangiPeyar || 'account'}`, 'success');
  };

  const toggleAccountActive = (acc) => {
    const next = getPaymentAccounts(profile)
      .filter(a => a.id !== 'legacy')
      .map(a => a.id === acc.id ? { ...a, isActive: !(a.isActive !== false) } : a);
    updateAccounts(next);
  };

  const moveAccountIdx = (idx, delta) => {
    const list = getPaymentAccounts(profile).filter(a => a.id !== 'legacy');
    const next = reorderAccounts(list, idx, idx + delta);
    updateAccounts(next);
  };

  const importLegacyAsAccount = () => {
    const fresh = createEmptyAccount(profile.vangiPeyar || 'Default account');
    fresh.vangiPeyar = profile.vangiPeyar || '';
    fresh.kanakkuEn = profile.kanakkuEn || '';
    fresh.ifsc = profile.ifsc || '';
    fresh.swift = profile.swift || '';
    fresh.upiId = profile.upiId || '';
    fresh.isDefault = true;
    updateAccounts([fresh]);
    thagaval('Imported existing bank details as your first account', 'success');
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

  const handleSave = async (e) => {
    if (e) e.preventDefault();
    try {
      setSaving(true);
      await saveProfile(profile);
      if (onSaved) onSaved(profile);
      setIsEditingCompany(false);
      thagaval('Profile saved!', 'success');
    } catch { thagaval('Failed to save profile', 'error'); }
    finally { setSaving(false); }
  };

  const handleCancelEditCompany = async () => {
    const p = await getProfile();
    setProfile({
      ...profile,
      ...p,
      primaryDataLanguage: p?.primaryDataLanguage || 'Tamil',
      secondaryDataLanguage: p?.secondaryDataLanguage || 'English',
      enableBilingual: p?.enableBilingual !== false
    });
    setIsEditingCompany(false);
    setShowMobileField(false);
  };

  // Invoice Number Settings
  const handleInvNumChange = (field, value) => {
    setInvNumSettings(prev => ({ ...prev, [field]: value }));
  };

  const handleSaveInvNumSettings = async () => {
    setInvNumSaving(true);
    try {
      await saveInvoiceNumberSettings(invNumSettings);
      thagaval('Invoice number settings saved!', 'success');
    } catch { thagaval('Failed to save settings', 'error'); }
    finally { setInvNumSaving(false); }
  };

  const getInvNumPreview = () => {
    const s = invNumSettings;
    const pfx = s.brandPrefix || 'INV';
    const sep = s.separator || '/';
    const padded = String(s.startNumber || 1).padStart(s.padDigits || 4, '0');
    if (s.format === 'random') {
      return `${pfx}${sep}A3X9K2`;
    }
    if (s.showFinYear) {
      const yr = new Date().getFullYear();
      const ny = (yr + 1).toString().slice(-2);
      return `${pfx}${sep}${yr}-${ny}${sep}${padded}`;
    }
    return `${pfx}${sep}${padded}`;
  };

  const handleRcpNumChange = (field, value) => {
    setRcpNumSettings(prev => ({ ...prev, [field]: value }));
  };

  const handleSaveRcpNumSettings = async () => {
    setRcpNumSaving(true);
    try {
      await saveReceiptNumberSettings(rcpNumSettings);
      thagaval('Receipt number settings saved!', 'success');
    } catch { thagaval('Failed to save settings', 'error'); }
    finally { setRcpNumSaving(false); }
  };

  const getRcpNumPreview = () => {
    const s = rcpNumSettings;
    const pfx = s.brandPrefix || 'RCP';
    const sep = s.separator || '/';
    const padded = String(s.startNumber || 1).padStart(s.padDigits || 4, '0');
    if (s.format === 'random') {
      return `${pfx}${sep}A3X9K2`;
    }
    if (s.showFinYear) {
      const yr = new Date().getFullYear();
      const ny = (yr + 1).toString().slice(-2);
      return `${pfx}${sep}${yr}-${ny}${sep}${padded}`;
    }
    return `${pfx}${sep}${padded}`;
  };

    thagaval('Disconnected from Google Drive', 'info');
  };

  // Export / Import (granular)
  const ALL_BACKUP_PARTS = [
    { id: 'profile',        label: 'Active business profile',  hint: 'Name, mugavari, GSTIN, bank, logo, signature' },
    { id: 'profiles',       label: 'All business profiles',    hint: 'Multi-business switcher entries' },
    { id: 'bills',          label: 'Invoices / bills',         hint: 'Tax invoices, proforma, credit notes, etc.' },
    { id: 'clients',        label: 'Clients',                  hint: 'Saved client directory' },
    { id: 'products',       label: 'Products / Inventory',     hint: 'Product catalog with HSN, rate, stock' },



    { id: 'receipts',       label: 'Receipts',                 hint: 'Payment receipts' },
    { id: 'meta',           label: 'App settings',             hint: 'Region, modules, invoice number format, display options' },
    { id: 'localStorage',   label: 'Local preferences',        hint: 'Custom units, theme, last region preference' },
  ];

  const [showExportModal, setShowExportModal] = useState(false);
  const [showImportModal, setShowImportModal] = useState(false);
  const [exportSel, setExportSel] = useState(() => Object.fromEntries(ALL_BACKUP_PARTS.map(p => [p.id, true])));
  const [importSel, setImportSel] = useState(() => Object.fromEntries(ALL_BACKUP_PARTS.map(p => [p.id, true])));
  const [importInspection, setImportInspection] = useState(null);
  const [importJsonText, setImportJsonText] = useState('');
  const [exportToDrive, setExportToDrive] = useState(false);
  const [drivePending, setDrivePending] = useState(false);

  const toggleExport = (id) => setExportSel(prev => ({ ...prev, [id]: !prev[id] }));
  const toggleImport = (id) => setImportSel(prev => ({ ...prev, [id]: !prev[id] }));
  const exportToggleAll = (val) => setExportSel(Object.fromEntries(ALL_BACKUP_PARTS.map(p => [p.id, val])));
  const importToggleAll = (val) => setImportSel(Object.fromEntries(ALL_BACKUP_PARTS.map(p => [p.id, val])));

  const runExport = async () => {
    try {
      const json = await exportAllData(exportSel);
      const fileName = `elvanniril-backup-${new Date().toISOString().split('T')[0]}.json`;

      // Local download (always)
      const blob = new Blob([json], { type: 'application/json' });
      const url = URL.createObjectURL(blob);
      const a = document.createElement('a');
      a.href = url; a.download = fileName; a.click();
      URL.revokeObjectURL(url);

      thagaval('Backup downloaded', 'success');
      setShowExportModal(false);
    } catch (err) {
      console.error(err);
      thagaval('Export failed: ' + (err.message || 'unknown error'), 'error');
    }
  };

  const handleImportPick = async (e) => {
    const file = e.target.files?.[0];
    if (!file) return;
    try {
      const text = await file.text();
      const inspection = inspectBackup(text);
      if (!inspection.valid) { thagaval("This file doesn't look like an Udukkai backup.", 'error'); return; }
      setImportInspection(inspection);
      setImportJsonText(text);
      // Auto-tick only the parts that actually have data in the file
      const auto = {};
      ALL_BACKUP_PARTS.forEach(p => { auto[p.id] = (inspection.counts[p.id] || 0) > 0; });
      setImportSel(auto);
      setShowImportModal(true);
    } catch { thagaval('Could not read the file', 'error'); }
    if (fileInputRef.current) fileInputRef.current.value = '';
  };

  const runImport = async () => {
    try {
      const result = await importData(importJsonText, importSel);
      const parts = [];
      if (result.billCount) parts.push(`${result.billCount} invoice(s)`);
      if (result.hasProfile) parts.push('profile');
      if (result.templateCount) parts.push(`${result.templateCount} template(s)`);
      if (result.clientCount) parts.push(`${result.clientCount} client(s)`);
      if (result.productCount) parts.push(`${result.productCount} product(s)`);
      thagaval(parts.length ? `Restored: ${parts.join(', ')}` : 'Restore complete', 'success');
      if (importSel.profile) { const p = await getProfile(); setProfile(p); if (onSaved) onSaved(p); }
      if (importSel.profiles) loadBusinessProfiles();
      setShowImportModal(false);
      setImportInspection(null);
      setImportJsonText('');
    } catch (err) {
      console.error(err);
      thagaval('Import failed: ' + (err.message || 'unknown error'), 'error');
    }
  };

  // Multi-business profiles
  const handleSaveAsProfile = async () => {
    if (!profile.niruvanathinPeyar.trim()) { thagaval('Business name required', 'warning'); return; }
    // Update existing profile with same name, or create new
    const existing = businessProfiles.find(bp => bp.niruvanathinPeyar.trim().toLowerCase() === profile.niruvanathinPeyar.trim().toLowerCase());
    await saveBusinessProfile({ ...profile, id: existing?.id || undefined });
    thagaval(existing ? 'Profile updated!' : 'Profile saved!', 'success');
    loadBusinessProfiles();
  };

  const handleLoadProfile = async (bp) => {
    // Auto-save current profile before switching (so it's not lost)
    if (profile.niruvanathinPeyar?.trim()) {
      const existing = businessProfiles.find(p => p.niruvanathinPeyar.trim().toLowerCase() === profile.niruvanathinPeyar.trim().toLowerCase());
      await saveBusinessProfile({ ...profile, id: existing?.id || undefined });
    }
    const loaded = { ...bp };
    delete loaded.id;
    setProfile(loaded);
    await saveProfile(loaded);
    if (onSaved) onSaved(loaded);
    thagaval(`Switched to ${bp.niruvanathinPeyar}`, 'success');
  };

  const handleDeleteProfile = async (id) => {
    if (confirm('Delete this saved business profile?')) {
      await deleteBusinessProfile(id);
      thagaval('Profile deleted', 'success');
      loadBusinessProfiles();
    }
  };

  const handleAddNewProfile = () => {
    setProfile({
      personName: '', personNameEn: '',
      niruvanathinPeyar: '', niruvanathinPeyarEn: '', mugavari: '', mugavariEn: '', oor: '', oorEn: '', maavattam: '', maavattamEn: '', maanilam: '', maanilamEn: '', pin: '', country: detectCountryFromBrowser(),
      gstin: '', pan: '', email: '', tholaipesi: '', mobileNumber: '', vangiPeyar: '', kanakkuEn: '', ifsc: '', swift: '',
      logo: '', logoHeight: 48, signature: '', upiId: '',
    });
    setTaxIdWarning('');
    companyFormRef.current?.scrollIntoView({ behavior: 'smooth', block: 'start' });
  };

  const [taxIdWarning, setTaxIdWarning] = useState('');
  const handleTaxIdBlur = () => {
    const result = validateTaxId(profile.country, profile.gstin);
    setTaxIdWarning(result.ok ? '' : result.message);
  };



  const variants = {
    enter: (direction) => ({ x: direction > 0 ? "100%" : (direction < 0 ? "-100%" : 0), opacity: 0, zIndex: 1 }),
    center: { x: 0, opacity: 1, zIndex: 0 },
    exit: (direction) => ({ x: direction < 0 ? "100%" : (direction > 0 ? "-100%" : 0), opacity: 0, zIndex: 0 })
  };
  const transition = { type: "spring", stiffness: 300, damping: 30 };

  const renderDetailView = () => {
    let content = null;
    let title = "";
    if (currentView === 0) { content = (<Box sx={{ display: 'flex', flexDirection: 'column', gap: 0 }}>
      <Paper className="s2-group" elevation={0} sx={{ p: { xs: 0, md: 0 }, mb: { xs: 4, md: 5 }, bgcolor: 'transparent', backgroundImage: 'none' }} component="form" onSubmit={handleSave} ref={companyFormRef}>
        <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 2 }}>
          <Typography variant="h6" style={{ fontWeight: 600 }} sx={{ m: 0 }}>
            {t('companyDetailsTitle')}
          </Typography>
          {!isEditingCompany ? (
            <Button variant="outlined" size="small" onClick={() => setIsEditingCompany(true)} startIcon={<Edit sx={{ fontSize: 16 }} />}>
              Edit Details
            </Button>
          ) : (
            <Box sx={{ display: 'flex', gap: 1 }}>
              <Button variant="outlined" size="small" color="inherit" onClick={handleCancelEditCompany}>
                Cancel
              </Button>
              <Button variant="contained" size="small" color="primary" onClick={() => setIsEditingCompany(false)}>
                Done
              </Button>
            </Box>
          )}
        </Box>
        {(() => {
          const cc = getCountryConfig(profile.country);
          return (
            <Grid container spacing={3}>
              <Grid size={{ xs: 12, sm: profile.enableBilingual !== false ? 6 : 12 }}>
                <TextField disabled={!isEditingCompany} required fullWidth size="small" label={`${t('businessNameLabel')} (${profile.primaryDataLanguage || 'Tamil'})`} name="niruvanathinPeyar" value={profile.niruvanathinPeyar} onChange={handleChange} />
              </Grid>
              {profile.enableBilingual !== false && (
                <Grid size={{ xs: 12, sm: 6 }}>
                  <TextField disabled={!isEditingCompany} fullWidth size="small" label={`Business Name in ${profile.secondaryDataLanguage || 'English'}`} name="niruvanathinPeyarEn" value={profile.niruvanathinPeyarEn || ''} onChange={handleChange} />
                </Grid>
              )}

              <Grid size={{ xs: 12, sm: 12 }}>
                <TextField disabled={!isEditingCompany} fullWidth size="small" label={t('shortBusinessNameLabel')} name="shortBusinessName" value={profile.shortBusinessName || ''} onChange={handleChange} placeholder="e.g. SJS" helperText={t('usedAsTheDefaultPrefixForYourI')} />
              </Grid>

              <Grid size={{ xs: 12, sm: profile.enableBilingual !== false ? 6 : 12 }}>
                <TextField disabled={!isEditingCompany} fullWidth multiline rows={2} size="small" label={`${t('mugavariLabel')} (${profile.primaryDataLanguage || 'Tamil'})`} name="mugavari" value={profile.mugavari} onChange={handleChange} />
              </Grid>
              {profile.enableBilingual !== false && (
                <Grid size={{ xs: 12, sm: 6 }}>
                  <TextField disabled={!isEditingCompany} fullWidth multiline rows={2} size="small" label={`Address in ${profile.secondaryDataLanguage || 'English'}`} name="mugavariEn" value={profile.mugavariEn || ''} onChange={handleChange} />
                </Grid>
              )}

              <Grid size={{ xs: 12, sm: profile.enableBilingual !== false ? 6 : 12 }}>
                <TextField disabled={!isEditingCompany} fullWidth size="small" label={`${t('oorLabel')} (${profile.primaryDataLanguage || 'Tamil'})`} name="oor" value={profile.oor || ''} onChange={handleChange} placeholder={t('e.g. Mumbai' as any)} />
              </Grid>
              {profile.enableBilingual !== false && (
                <Grid size={{ xs: 12, sm: 6 }}>
                  <TextField disabled={!isEditingCompany} fullWidth size="small" label={`City in ${profile.secondaryDataLanguage || 'English'}`} name="oorEn" value={profile.oorEn || ''} onChange={handleChange} />
                </Grid>
              )}

              <Grid size={{ xs: 12, sm: profile.enableBilingual !== false ? 6 : 12 }}>
                <TextField disabled={!isEditingCompany} fullWidth size="small" label={`மாவட்டம் (District) (${profile.primaryDataLanguage || 'Tamil'})`} name="maavattam" value={profile.maavattam || ''} onChange={handleChange} placeholder={`மாவட்டம்`} />
              </Grid>
              {profile.enableBilingual !== false && (
                <Grid size={{ xs: 12, sm: 6 }}>
                  <TextField disabled={!isEditingCompany} fullWidth size="small" label={`District in ${profile.secondaryDataLanguage || 'English'}`} name="maavattamEn" value={profile.maavattamEn || ''} onChange={handleChange} placeholder={`District`} />
                </Grid>
              )}

              <Grid size={{ xs: 12, sm: profile.enableBilingual !== false ? 6 : 12 }}>
                {(() => {
                  const stateOpts = getStatesForCountry(profile.country || 'India');
                  return stateOpts.length > 0 ? (
                    <FormControl fullWidth size="small" disabled={!isEditingCompany}>
                      <InputLabel>{t(cc.stateLabel as any)} ({profile.primaryDataLanguage || 'Tamil'})</InputLabel>
                      <Select MenuProps={{ disableScrollLock: true }} name="maanilam" value={profile.maanilam} onChange={handleChange} label={`${t(cc.stateLabel as any)} (${profile.primaryDataLanguage || 'Tamil'})`}>
                        <MenuItem value="">{t('Select maanilam' as any)}</MenuItem>
                        {stateOpts.map(s => <MenuItem key={s} value={s}>{getBilingualStateName(s, { ...profile, returnOnlyPrimary: true })}</MenuItem>)}
                      </Select>
                    </FormControl>
                  ) : (
                    <TextField disabled={!isEditingCompany} fullWidth size="small" label={`${t(cc.stateLabel as any)} (${profile.primaryDataLanguage || 'Tamil'})`} name="maanilam" value={profile.maanilam || ''} onChange={handleChange} placeholder={cc.stateLabel} />
                  );
                })()}
              </Grid>
              {profile.enableBilingual !== false && (
                <Grid size={{ xs: 12, sm: 6 }}>
                  <TextField fullWidth size="small" disabled={true} 
                    label={`${t(cc.stateLabel as any)} (${profile.secondaryDataLanguage || 'English'})`} 
                    value={profile.maanilam ? getBilingualStateName(profile.maanilam, { ...profile, returnOnlySecondary: true }) : ''} 
                    sx={{ '& .MuiInputBase-root': { bgcolor: 'action.hover' } }} />
                </Grid>
              )}

              <Grid size={{ xs: 12, sm: profile.enableBilingual !== false ? 6 : 12 }}>
                {(() => {
                  const isCustomCountry = profile.country === 'Other' || (profile.country && !visibleCountries.some(c => c.name === profile.country));
                  return (
                    <Box>
                      <FormControl fullWidth size="small" disabled={!isEditingCompany} sx={{ mb: isCustomCountry ? 2 : 0 }}>
                        <InputLabel>{t('countryLabel')} ({profile.primaryDataLanguage || 'Tamil'})</InputLabel>
                        <Select MenuProps={{ disableScrollLock: true }} name="country" 
                          value={isCustomCountry ? 'Other' : (profile.country || 'India')} 
                          onChange={(e) => {
                            if (e.target.value === 'Other') {
                              handleChange({ target: { name: 'country', value: 'Other' } } as any);
                              handleChange({ target: { name: 'countryEn', value: '' } } as any);
                            } else {
                              handleChange(e);
                              handleChange({ target: { name: 'countryEn', value: '' } } as any);
                            }
                          }} 
                          label={`${t('countryLabel')} (${profile.primaryDataLanguage || 'Tamil'})`}>
                          {visibleCountries.map(c => <MenuItem key={c.code} value={c.name}>{getBilingualCountryName(c.name, { ...profile, returnOnlyPrimary: true })}</MenuItem>)}
                        </Select>
                      </FormControl>
                      {isCustomCountry && (
                        <TextField fullWidth size="small" 
                          label={`Custom Country (${profile.primaryDataLanguage || 'Tamil'})`}
                          name="country" 
                          value={profile.country === 'Other' ? '' : profile.country} 
                          onChange={handleChange} 
                          placeholder="Enter country name" 
                          disabled={!isEditingCompany} />
                      )}
                    </Box>
                  );
                })()}
              </Grid>
              {profile.enableBilingual !== false && (
                <Grid size={{ xs: 12, sm: 6 }}>
                  {(() => {
                    const isCustomCountry = profile.country === 'Other' || (profile.country && !visibleCountries.some(c => c.name === profile.country));
                    return (
                      <Box>
                        {isCustomCountry && <Box sx={{ height: 40, mb: 2 }} />}
                        <TextField fullWidth size="small" disabled={!isCustomCountry || !isEditingCompany} 
                          name={isCustomCountry ? "countryEn" : undefined}
                          onChange={isCustomCountry ? handleChange : undefined}
                          label={`${t('countryLabel')} (${profile.secondaryDataLanguage || 'English'})`} 
                          value={isCustomCountry ? (profile.countryEn || '') : getBilingualCountryName(profile.country || 'India', { ...profile, returnOnlySecondary: true })} 
                          sx={!isCustomCountry ? { '& .MuiInputBase-root': { bgcolor: 'action.hover' } } : {}} />
                      </Box>
                    );
                  })()}
                </Grid>
              )}

              <Grid size={{ xs: 12, sm: 6 }}>
                <TextField disabled={!isEditingCompany} fullWidth size="small" label={t('tholaipesiLabel')} name="tholaipesi" value={profile.tholaipesi} onChange={handleChange} />
              </Grid>
              <Grid size={{ xs: 12, sm: 6 }}>
                <TextField disabled={!isEditingCompany} fullWidth size="small" label={t('email') || 'Email'} name="email" value={profile.email || ''} onChange={handleChange} />
              </Grid>

              {!showMobileField && !profile.mobileNumber ? (
                isEditingCompany ? (
                  <Grid size={{ xs: 12, sm: 6 }} sx={{ display: 'flex', alignItems: 'center' }}>
                    <Button size="small" variant="text" onClick={() => setShowMobileField(true)} startIcon={<Plus size={20} weight="fill" sx={{ fontSize: 16 }} />} sx={{ color: 'text.secondary', textTransform: 'none' }}>
                      {t('addAlternateMobile')}
                    </Button>
                  </Grid>
                ) : null
              ) : (
                <Grid size={{ xs: 12, sm: 6 }}>
                  <TextField 
                    disabled={!isEditingCompany} 
                    fullWidth 
                    size="small" 
                    label={t('mobileLabel')} 
                    name="mobileNumber" 
                    value={profile.mobileNumber || ''} 
                    onChange={handleChange} 
                    slotProps={{
                      input: {
                        endAdornment: isEditingCompany ? (
                          <InputAdornment position="end">
                            <IconButton 
                              size="small" 
                              onClick={() => {
                                setProfile(prev => ({ ...prev, mobileNumber: '' }));
                                setShowMobileField(false);
                              }}
                              edge="end"
                            >
                              <X size={20} weight="fill" sx={{ fontSize: 16 }} />
                            </IconButton>
                          </InputAdornment>
                        ) : null
                      }
                    }}
                  />
                </Grid>
              )}

              <Grid size={{ xs: 12, sm: 6 }}>
                <TextField 
                  disabled={!isEditingCompany} 
                  fullWidth 
                  size="small" 
                  label={cc.taxIdLabel || 'GSTIN / Tax ID'} 
                  name="gstin" 
                  value={profile.gstin || ''} 
                  onChange={handleChange} 
                  onBlur={handleTaxIdBlur} 
                  error={!!taxIdWarning} 
                  helperText={taxIdWarning} 
                />
              </Grid>
            </Grid>
          );
        })()}
        {/* Logo & Signature */}
        <Typography variant="h6" style={{ fontWeight: 600 }} gutterBottom sx={{ mt: 4, mb: 2 }}>
          {t('brandingTitle')}
        </Typography>
        <Grid container spacing={3}>
          <Grid size={{ xs: 12, sm: 6 }}>
            <Typography variant="subtitle2" gutterBottom>{t('businessLogo')}</Typography>
            <Paper className="s2-group" variant="outlined" sx={{ p: 2, display: 'flex', flexDirection: 'column', alignItems: 'center', gap: 2, borderStyle: 'dashed' }}>
              {profile.logo ? (
                <>
                  <Box sx={{ position: 'relative' }}>
                    <img src={profile.logo} alt="Logo" style={{ height: `${profile.logoHeight || 48}px`, maxWidth: '180px', objectFit: 'contain' }} />
                    <IconButton size="small" color="error" onClick={() => removeImage('logo')} sx={{ position: 'absolute', top: -10, right: -10, bgcolor: 'background.paper', '@media (hover: hover)': { '&:hover': { bgcolor: 'error.light' } } }}>
                      <Trash size={20} weight="fill" sx={{ fontSize: 14 }} />
                    </IconButton>
                  </Box>
                  <Box sx={{ width: '100%', px: 2 }}>
                    <Typography variant="caption" color="text.secondary" sx={{ display: 'block' }} gutterBottom>Logo Size on Invoice ({profile.logoHeight || 48}px)</Typography>
                    <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                      <Typography variant="caption" color="text.secondary">S</Typography>
                      <input type="range" min="24" max="80" value={profile.logoHeight || 48} onChange={(e) => setProfile(prev => ({ ...prev, logoHeight: Number(e.target.value) }))} style={{ flex: 1, accentColor: 'var(--primary)' }} />
                      <Typography variant="caption" color="text.secondary">L</Typography>
                    </Box>
                  </Box>
                  <Button size="small" variant="outlined" onClick={() => logoInputRef.current?.click()}>Change Logo</Button>
                </>
              ) : (
                <Button variant="outlined" onClick={() => logoInputRef.current?.click()} startIcon={<ImageSquare size={20} weight="fill" sx={{ fontSize: 20 }} />} sx={{ flexDirection: 'column', py: 3, gap: 1 }}>
                  {t('uploadLogo')}
                  <Typography variant="caption" color="text.secondary" sx={{ display: 'block' }}>{t('logoHint')}</Typography>
                </Button>
              )}
              <input ref={logoInputRef} type="file" accept="image/*" style={{ display: 'none' }} onChange={(e) => handleImageUpload('logo', e)} />
            </Paper>
          </Grid>
          <Grid size={{ xs: 12, sm: 6 }}>
            <Typography variant="subtitle2" gutterBottom>{t('signature')}</Typography>
            <Paper className="s2-group" variant="outlined" sx={{ p: 2, display: 'flex', flexDirection: 'column', alignItems: 'center', gap: 2, borderStyle: 'dashed', height: '100%' }}>
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
                <Button variant="outlined" onClick={() => sigInputRef.current?.click()} startIcon={<PencilSimple size={20} weight="fill" sx={{ fontSize: 20 }} />} sx={{ flexDirection: 'column', py: 3, gap: 1, height: '100%', width: '100%' }}>
                  {t('uploadSignature')}
                  <Typography variant="caption" color="text.secondary" sx={{ display: 'block' }}>{t('sigHint')}</Typography>
                </Button>
              )}
              <input ref={sigInputRef} type="file" accept="image/*" style={{ display: 'none' }} onChange={(e) => handleImageUpload('signature', e)} />
            </Paper>
          </Grid>
        </Grid>


      </Paper>

      {/* ---- Multi-Business Profiles ---- */}
      <Paper className="s2-group" elevation={0} sx={{ p: { xs: 0, md: 0 }, mb: { xs: 4, md: 5 }, bgcolor: 'transparent', backgroundImage: 'none' }}>
        <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 2 }}>
          <Typography variant="h6" style={{ fontWeight: 600 }} sx={{ m: 0 }}>
            {t('businessProfilesTitle')}
          </Typography>
          <Box sx={{ display: 'flex', gap: 1 }}>
            <Button variant="outlined" onClick={handleAddNewProfile} startIcon={<Plus size={20} weight="fill" sx={{ fontSize: 16 }} />}>
              {t('addNewProfile')}
            </Button>
            <Button variant="contained" onClick={handleSaveAsProfile} startIcon={<Buildings size={20} weight="fill" sx={{ fontSize: 16 }} />}>
              {t('saveAsProfile')}
            </Button>
          </Box>
        </Box>
        <Typography variant="body2" color="text.secondary" sx={{ mb: 2 }}>
          {t('businessProfilesDesc1')}
        </Typography>
        {businessProfiles.length === 0 ? (
          <Typography variant="body2" color="text.secondary">
            {t('businessProfilesDesc2')}
          </Typography>
        ) : (
          <Grid container spacing={2}>
            {businessProfiles.map(bp => {
              const isActive = bp.niruvanathinPeyar?.trim().toLowerCase() === profile.niruvanathinPeyar?.trim().toLowerCase();
              return (
              <Grid size={{ xs: 12, sm: 6 }} key={bp.id}>
                <Paper className="s2-group" variant="outlined" sx={{ p: 2, height: '100%', borderColor: isActive ? 'primary.main' : 'divider', borderWidth: isActive ? 2 : 1 }}>
                  <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', mb: 1 }}>
                    <Box>
                      <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                        <Typography variant="subtitle2" style={{ fontWeight: 'bold' }}>{bp.niruvanathinPeyar}</Typography>
                        {isActive && <Chip label="Active" size="small" color="primary" sx={{ height: 20, fontSize: '0.7rem' }} />}
                      </Box>
                      <Typography variant="caption" color="text.secondary" sx={{ display: 'block' }}>
                        {bp.maanilam}{bp.gstin ? ` | ${bp.gstin}` : ''}
                      </Typography>
                    </Box>
                    <Box sx={{ display: 'flex', gap: 0.5 }}>
                      <Button size="small" variant={isActive ? 'outlined' : 'contained'} onClick={() => handleLoadProfile(bp)} disabled={isActive}>
                        {isActive ? 'Current' : 'Switch'}
                      </Button>
                      <IconButton size="small" color="error" onClick={() => handleDeleteProfile(bp.id)} title="Delete">
                        <Trash size={20} weight="fill" sx={{ fontSize: 16 }} />
                      </IconButton>
                    </Box>
                  </Box>
                  {bp.mugavari && (
                    <Typography variant="caption" color="text.secondary" sx={{ mt: 1, whiteSpace: 'pre-line', overflow: 'hidden', display: '-webkit-box', WebkitLineClamp: 2, WebkitBoxOrient: 'vertical' }}>
                      {bp.mugavari}
                    </Typography>
                  )}
                </Paper>
              </Grid>
            );
            })}
          </Grid>
        )}
      </Paper>
</Box>); title = "Profile & Accounts"; }
    else if (currentView === 1) { content = (<Box sx={{ display: 'flex', flexDirection: 'column', gap: 0 }}>      {/* ---- Language Preference ---- */}
      <Paper className="s2-group" elevation={0} sx={{ p: { xs: 0, md: 0 }, mb: { xs: 4, md: 5 }, bgcolor: 'transparent', backgroundImage: 'none' }}>
        <Typography variant="h6" style={{ fontWeight: 600 }} gutterBottom sx={{ mt: 0, mb: 0.5 }}>
          {t('language')}
        </Typography>
        <Typography variant="body2" color="text.secondary" sx={{ mb: 2 }}>
          {t('changeTheLanguageOfTheApplicat')}
        </Typography>
        <Grid container spacing={2}>
          {[
            { id: 'ta', label: 'தமிழ்', desc: 'முழுக்க தமிழில்' },
            { id: 'en', label: 'English', desc: 'English only' },
          ].map(opt => (
            <Grid size={{ xs: 12, sm: 6 }} key={opt.id}>
              <Paper
                variant="outlined"
                component="button"
                onClick={() => {
                  setLanguage(opt.id as any);
                  thagaval(opt.id === 'ta' ? 'மொழி தமிழுக்கு மாற்றப்பட்டது' : 'Language changed to English', 'success');
                }}
                sx={{
                  p: 2,
                  width: '100%',
                  textAlign: 'left',
                  height: '100%',
                  cursor: 'pointer',
                  borderWidth: language === opt.id ? 2 : 1,
                  borderColor: language === opt.id ? 'primary.main' : 'divider',
                  bgcolor: language === opt.id ? 'primary.50' : 'background.paper',
                  '@media (hover: hover)': { '&:hover': { bgcolor: language === opt.id ? 'primary.50' : 'action.hover' } }
                }}
              >
                <Typography variant="subtitle2" style={{ fontWeight: 'bold' }} gutterBottom color={language === opt.id ? 'primary.main' : 'text.primary'}>
                  {opt.label}
                </Typography>
                <Typography variant="body2" color={language === opt.id ? 'primary.main' : 'text.secondary'}>
                  {opt.desc}
                </Typography>
              </Paper>
            </Grid>
          ))}
        </Grid>
      </Paper>

        {/* App User Name */}
        <Paper className="s2-group" elevation={0} sx={{ p: { xs: 0, md: 0 }, mb: { xs: 4, md: 5 }, bgcolor: 'transparent', backgroundImage: 'none' }}>
          <Typography variant="h6" style={{ fontWeight: 600 }} gutterBottom sx={{ mt: 0, mb: 0.5 }}>
            {t('appUserName')}
          </Typography>
          <Typography variant="body2" color="text.secondary" sx={{ mb: 3 }}>
            {t('thisNameWillAppearOnTheAppHome')}
          </Typography>
          <Grid container spacing={3}>
            <Grid size={{ xs: 12, sm: profile.enableBilingual !== false ? 6 : 12 }}>
              <TextField 
                fullWidth size="small" 
                label={`${t('userName')} (${profile.primaryDataLanguage || 'Tamil'})`} 
                name="personName" 
                value={profile.personName || ''} 
                onChange={handleChange} 
              />
            </Grid>
            {profile.enableBilingual !== false && (
              <Grid size={{ xs: 12, sm: 6 }}>
                <TextField 
                  fullWidth size="small" 
                  label={`${t('userName')} (${profile.secondaryDataLanguage || 'English'})`} 
                  name="personNameEn" 
                  value={profile.personNameEn || ''} 
                  onChange={handleChange} 
                />
              </Grid>
            )}
          </Grid>

        </Paper>

      {/* Data Language Settings */}
      <Paper className="s2-group" elevation={0} sx={{ p: { xs: 0, md: 0 }, mb: { xs: 4, md: 5 }, bgcolor: 'transparent', backgroundImage: 'none' }}>
        <Box sx={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', flexWrap: 'wrap', gap: 1, mb: 3 }}>
          <Box>
            <Typography variant="h6" style={{ fontWeight: 600 }} gutterBottom sx={{ mt: 0, mb: 0.5 }}>
              {t('dataEntryLanguages')}
            </Typography>
            <Typography variant="body2" color="text.secondary">
              {t('languagesUsedForBilling')}
            </Typography>
          </Box>
          {!editingLangSettings ? (
            <Button variant="outlined" size="small" onClick={() => setEditingLangSettings(true)} startIcon={<Edit sx={{ fontSize: 16 }} />}>
              {t('edit' as any) || 'Edit'}
            </Button>
          ) : (
            <Box sx={{ display: 'flex', gap: 1 }}>
              <Button variant="outlined" color="inherit" onClick={() => setEditingLangSettings(false)}>{t('cancel' as any) || 'Cancel'}</Button>
              <Button variant="contained" onClick={saveLangSettings} startIcon={<FloppyDisk size={20} weight="fill" sx={{ fontSize: 16 }} />}>{t('save' as any) || 'Save'}</Button>
            </Box>
          )}
        </Box>
        
        <Grid container spacing={3}>
          <Grid size={{ xs: 12, sm: 6 }}>
            {!editingLangSettings ? (
              <Box>
                <Typography variant="body2" color="text.secondary" gutterBottom>{t('primaryLanguage')}</Typography>
                <Typography variant="body1" sx={{ p: 1.5, bgcolor: 'action.hover', borderRadius: 1 }}>{profile.primaryDataLanguage || 'Tamil'}</Typography>
              </Box>
            ) : (
              <FormControl fullWidth size="small">
                <InputLabel>{t('primaryLanguage')}</InputLabel>
                <Select MenuProps={{ disableScrollLock: true }} name="primaryDataLanguage" value={profile.primaryDataLanguage || 'Tamil'} onChange={handleChange} label={t('primaryLanguage')}>
                  <MenuItem value="Tamil">{t('langTamil')}</MenuItem>
                  <MenuItem value="English">{t('langEnglish')}</MenuItem>
                  {/* Archived Languages:
                  <MenuItem value="Hindi">{t('langHindi')}</MenuItem>
                  <MenuItem value="Telugu">{t('langTelugu')}</MenuItem>
                  <MenuItem value="Kannada">{t('langKannada')}</MenuItem>
                  <MenuItem value="Malayalam">{t('langMalayalam')}</MenuItem>
                  <MenuItem value="Marathi">{t('langMarathi')}</MenuItem>
                  <MenuItem value="Gujarati">{t('langGujarati')}</MenuItem>
                  <MenuItem value="Bengali">{t('langBengali')}</MenuItem>
                  */}
                </Select>
              </FormControl>
            )}
          </Grid>
          <Grid size={{ xs: 12, sm: 6 }}>
            {!editingLangSettings ? (
              <Box>
                <Typography variant="body2" color="text.secondary" gutterBottom>{t('secondaryLanguage')}</Typography>
                <Typography variant="body1" sx={{ p: 1.5, bgcolor: 'action.hover', borderRadius: 1 }}>{profile.secondaryDataLanguage || 'English'}</Typography>
              </Box>
            ) : (
              <FormControl fullWidth size="small">
                <InputLabel>{t('secondaryLanguage')}</InputLabel>
                <Select MenuProps={{ disableScrollLock: true }} name="secondaryDataLanguage" value={profile.secondaryDataLanguage || 'English'} onChange={handleChange} label={t('secondaryLanguage')}>
                  <MenuItem value="English">{t('langEnglish')}</MenuItem>
                  <MenuItem value="Tamil">{t('langTamil')}</MenuItem>
                  {/* Archived Languages:
                  <MenuItem value="Hindi">{t('langHindi')}</MenuItem>
                  <MenuItem value="Telugu">{t('langTelugu')}</MenuItem>
                  <MenuItem value="Kannada">{t('langKannada')}</MenuItem>
                  <MenuItem value="Malayalam">{t('langMalayalam')}</MenuItem>
                  <MenuItem value="Marathi">{t('langMarathi')}</MenuItem>
                  <MenuItem value="Gujarati">{t('langGujarati')}</MenuItem>
                  <MenuItem value="Bengali">{t('langBengali')}</MenuItem>
                  */}
                </Select>
              </FormControl>
            )}
          </Grid>
          <Grid size={{ xs: 12 }}>
            <Paper className="s2-group" variant="outlined" sx={{ p: 2, display: 'flex', alignItems: 'center', opacity: editingLangSettings ? 1 : 0.7, pointerEvents: editingLangSettings ? 'auto' : 'none', bgcolor: 'action.hover' }}>
              <FormControlLabel
                control={
                  <Material3Switch 
                    checked={profile.enableBilingual !== false} 
                    onChange={e => setProfile(prev => ({ ...prev, enableBilingual: e.target.checked }))} 
                    disabled={!editingLangSettings} 
                  />
                }
                label={
                  <Box>
                    <Typography variant="body1" style={{ fontWeight: 500 }}>{t('enableBilingualBills')}</Typography>
                  </Box>
                }
                sx={{ m: 0, width: '100%' }}
              />
            </Paper>
          </Grid>
        </Grid>
      </Paper>
</Box>); title = "Display & Languages"; }
    else if (currentView === 2) { content = (<Box sx={{ display: 'flex', flexDirection: 'column', gap: 0 }}>
      <Paper className="s2-group" elevation={0} sx={{ p: { xs: 0, md: 0 }, mb: { xs: 4, md: 5 }, bgcolor: 'transparent', backgroundImage: 'none' }}>
        {/* ---- Bank Details (Simple — same as Coolie) ---- */}
        <Typography variant="h6" style={{ fontWeight: 600 }} gutterBottom sx={{ m: 0 }}>
          {t('paymentAccountsTitle')}
        </Typography>
        <Typography variant="body2" color="text.secondary" sx={{ mt: 0.5, mb: 3 }}>
          {t('bankDetailsShownOnYourInvoices')}
        </Typography>
        <Grid container spacing={3}>
          <Grid size={{ xs: 12, sm: 6 }}>
            <TextField fullWidth size="small" label={t('bankNameTamil')} name="vangiPeyar" value={profile.vangiPeyar || ''} onChange={handleChange} />
          </Grid>
          <Grid size={{ xs: 12, sm: 6 }}>
            <TextField fullWidth size="small" label={t('bankNameEnglish')} name="vangiPeyarEn" value={profile.vangiPeyarEn || ''} onChange={handleChange} />
          </Grid>
          <Grid size={{ xs: 12, sm: 6 }}>
            <TextField fullWidth size="small" label={t('branchNameTamil')} name="bankBranch" value={profile.bankBranch || ''} onChange={handleChange} />
          </Grid>
          <Grid size={{ xs: 12, sm: 6 }}>
            <TextField fullWidth size="small" label={t('branchNameEnglish')} name="bankBranchEn" value={profile.bankBranchEn || ''} onChange={handleChange} />
          </Grid>
          <Grid size={{ xs: 12, sm: 6 }}>
            <TextField fullWidth size="small" label={t('accountNumber')} name="kanakkuEn" value={profile.kanakkuEn || ''} onChange={handleChange} />
          </Grid>
          <Grid size={{ xs: 12, sm: 6 }}>
            <TextField fullWidth size="small" label="IFSC Code" name="ifsc" value={profile.ifsc || ''} onChange={handleChange} />
          </Grid>
          <Grid size={{ xs: 12, sm: 6 }}>
            <TextField fullWidth size="small" label="SWIFT / BIC (optional)" name="swift" value={profile.swift || ''} onChange={handleChange} placeholder="e.g. HDFCINBB" />
          </Grid>
          <Grid size={{ xs: 12, sm: 6 }}>
            <TextField fullWidth size="small" label={t('uPIIDOptional')} name="upiId" value={profile.upiId || ''} onChange={handleChange} placeholder="e.g. yourbusiness@hdfcbank" />
          </Grid>
          {((profile.country || 'India') === 'India') && (
            <Grid size={{ xs: 12, sm: 6 }}>
              <TextField fullWidth size="small" label="PAN Number" name="pan" value={profile.pan || ''} onChange={handleChange} placeholder="e.g. AAAAA1234A" slotProps={{ htmlInput: { maxLength: 10 } }} />
            </Grid>
          )}
        </Grid>
      </Paper>




      {/* ---- Invoice Terms & Conditions ---- */}
      <Paper className="s2-group" elevation={0} sx={{ p: { xs: 0, md: 0 }, mb: { xs: 4, md: 5 }, bgcolor: 'transparent', backgroundImage: 'none' }}>
        <Typography variant="h6" style={{ fontWeight: 600 }} gutterBottom sx={{ mt: 0 }}>
          {t('termsTemplatesTitle')}
        </Typography>
        <Typography variant="body2" color="text.secondary" sx={{ mb: 2 }}>
          Default Terms and Conditions that will appear on your invoices.
        </Typography>
        <TextField 
          fullWidth 
          multiline 
          rows={6} 
          size="small" 
          label="Terms & Conditions" 
          name="invoiceTerms" 
          value={profile.invoiceTerms || ''} 
          onChange={handleChange} 
          placeholder="Paste or type your terms & conditions..." 
        />

      </Paper>

      {/* ---- Invoice Template ---- */}
      <Paper className="s2-group" elevation={0} sx={{ p: { xs: 0, md: 0 }, mb: { xs: 4, md: 5 }, bgcolor: 'transparent', backgroundImage: 'none' }}>
        <Typography variant="h6" style={{ fontWeight: 600 }} gutterBottom sx={{ mt: 0 }}>
          {t('hc_invoiceTemplate')}
        </Typography>
        <Typography variant="body2" color="text.secondary" sx={{ mb: 3 }}>
          Configure default styling and optional fields for all your invoices.
        </Typography>

        {Array.from(new Map(getCountriesForRegion().map(c => [c.currency, c])).values()).length > 1 && (
        <Grid container spacing={3} sx={{ mb: 3 }}>
          <Grid size={{ xs: 12, md: invoiceTemplate.currency !== 'INR' ? 6 : 12 }}>
            <FormControl fullWidth size="small">
              <InputLabel shrink>{t('hc_currency')}</InputLabel>
              <Select value={invoiceTemplate.currency || 'INR'} label={t('hc_currency')} displayEmpty
                onChange={(e) => handleTemplateChange('currency', e.target.value)}>
                {Array.from(new Map(getCountriesForRegion().map(c => [c.currency, c])).values()).map(c => (
                  <MenuItem key={c.currency} value={c.currency}>{c.currency} ({c.currencySymbol === c.currency ? c.name : c.currencySymbol})</MenuItem>
                ))}
              </Select>
            </FormControl>
          </Grid>
          {invoiceTemplate.currency !== 'INR' && (
            <Grid size={{ xs: 12, md: 6 }}>
              <TextField fullWidth size="small" label="Exchange Rate (optional, snapshot)" 
                type="number" slotProps={{ htmlInput: { step: "any", min: 0 } }}
                value={invoiceTemplate.exchangeRate || ''}
                onChange={(e) => handleTemplateChange('exchangeRate', e.target.value)}
                placeholder={`1 ${invoiceTemplate.currency} = ? INR`}
                helperText="Stored on this invoice — historical reports stay accurate even if rates change."
              />
            </Grid>
          )}
        </Grid>
        )}
        
        <Grid container spacing={3} sx={{ mb: 3 }}>
          <Grid size={{ xs: 12, md: 6 }}>
            <Typography variant="body2" gutterBottom sx={{ fontWeight: 600 }}>{t('hc_pdfStyle')}</Typography>
            <Box sx={{ display: 'flex', gap: 1 }}>
              {PDF_STYLES.map(s => (
                <Chip key={s.id} 
                  label={s.label} 
                  color={(invoiceTemplate.pdfStyle || 'classic') === s.id ? "primary" : "default"} 
                  variant={(invoiceTemplate.pdfStyle || 'classic') === s.id ? "filled" : "outlined"} 
                  onClick={() => handleTemplateChange('pdfStyle', s.id)} 
                  clickable 
                  title={s.desc} 
                />
              ))}
            </Box>
          </Grid>
          <Grid size={{ xs: 12, md: 6 }}>
            <Typography variant="body2" gutterBottom sx={{ fontWeight: 600 }}>{t('hc_accentColor')}</Typography>
            <Box sx={{ display: 'flex', gap: 1, flexWrap: 'wrap', alignItems: 'center' }}>
              <Box component="button" type="button" title="Auto (match invoice type)"
                sx={{ width: 28, height: 28, borderRadius: '50%', border: !invoiceTemplate.accentColor ? '2.5px solid #334155' : '2px solid #cbd5e1', background: 'conic-gradient(#1e40af, #7c3aed, #0f766e, #be123c, #1e40af)', cursor: 'pointer', position: 'relative' }}
                onClick={() => handleTemplateChange('accentColor', '')}>
                {!invoiceTemplate.accentColor && <Box component="span" sx={{ position: 'absolute', inset: '3px', borderRadius: '50%', border: '2px solid white' }} />}
              </Box>
              {ACCENT_PRESETS.map(p => (
                <Box key={p.color} component="button" type="button" title={p.label}
                  sx={{ width: 28, height: 28, borderRadius: '50%', backgroundColor: p.color, border: invoiceTemplate.accentColor === p.color ? '2.5px solid #334155' : '2px solid #cbd5e1', cursor: 'pointer', position: 'relative' }}
                  onClick={() => handleTemplateChange('accentColor', p.color)}>
                  {invoiceTemplate.accentColor === p.color && <Box component="span" sx={{ position: 'absolute', inset: '3px', borderRadius: '50%', border: '2px solid white' }} />}
                </Box>
              ))}
            </Box>
          </Grid>
        </Grid>

        <Grid container spacing={3}>
          {[
            { group: 'Header & branding', items: [
              ['showLogo', 'Logo'],
              ['showBusinessAddress', 'Business mugavari'],
              ['showBusinessPhone', 'Business tholaipesi'],
              ['showBusinessEmail', 'Business email'],
              ['showState', 'Business maanilam'],
              ['showDistrict', 'Business district'],
              ['showCountry', 'Business country'],
              ['showGSTIN', 'Tax ID (GSTIN/VAT/etc.)'],
            ]},
            { group: 'Client / Bill-to', items: [
              ['showClientAddress', 'Client mugavari'],
              ['showClientPhone', 'Client tholaipesi'],
              ['showClientEmail', 'Client email'],
              ['showPlaceOfSupply', 'Place of Supply'],
            ]},
            { group: 'Items table', items: [
              ['showHSN', 'HSN/SAC column'],
              ['showItemUnit', 'Unit column'],
              ['showDiscount', 'Discount column'],
              ['showItemizedTax', 'Tax % and Amount columns'],
              ['showCess', 'GST Cess % column'],
            ]},
            { group: 'Totals', items: [
              ['showAmountWords', 'Amount in words'],
              ['showRoundOff', 'Round-off line'],
            ]},
            { group: 'Compliance', items: [
              ['reverseCharge', 'Reverse Charge applies'],
            ]},
            { group: 'Footer', items: [
              ['showBankDetails', 'Bank details'],
              ['showAccountLabel', 'Show Pay via label'],
              ['showUPI', 'UPI QR (India only)'],
              ['showSignature', 'Signature block'],
              ['showSignatoryText', 'Authorized Signatory caption'],
              ['showTerms', 'Terms & Conditions'],
              ['showNotes', 'Notes / Remarks'],
            ]},
          ].map(section => (
            <Grid size={{ xs: 12, sm: 6, md: 4 }} key={section.group}>
              <Typography variant="overline" color="text.secondary" sx={{ fontWeight: 700, display: 'block', mb: 1 }}>{section.group}</Typography>
              <Box sx={{ display: 'flex', flexDirection: 'column' }}>
                {section.items.map(([key, label]) => {
                  const offByDefault = key === 'showRoundOff' || key === 'showAccountLabel' || key === 'showCess' || key === 'reverseCharge' || key === 'showItemizedTax';
                  const checked = offByDefault ? !!invoiceTemplate[key] : invoiceTemplate[key] !== false;
                  return (
                    <FormControlLabel key={key} control={<Checkbox size="small" checked={checked} onChange={(e) => handleTemplateChange(key, !checked)} />} label={<Typography variant="body2">{label}</Typography>} />
                  );
                })}
              </Box>
            </Grid>
          ))}
        </Grid>
      </Paper>

</Box>); title = "Billing & Payments"; }
    else if (currentView === 3) { content = (<Box sx={{ display: 'flex', flexDirection: 'column', gap: 0 }}>      {/* ---- Advanced Settings ---- */}
      <Paper className="s2-group" elevation={2} sx={{ p: { xs: 2, md: 3 }, mb: { xs: 2, md: 3 }, borderRadius: { xs: 0, sm: 2 }, borderX: { xs: 0, sm: undefined } }}>
        <Typography variant="h6" style={{ fontWeight: 600 }} gutterBottom sx={{ mt: 0, mb: 0.5 }}>
          Advanced Settings
        </Typography>
        <Typography variant="body2" color="text.secondary" sx={{ mb: 2 }}>
          API Integrations and advanced configuration.
        </Typography>

      </Paper>

      <Paper className="s2-group" elevation={2} sx={{ p: { xs: 2, md: 3 }, mb: { xs: 2, md: 3 }, borderRadius: { xs: 0, sm: 2 }, borderX: { xs: 0, sm: undefined } }}>
        <Typography variant="h6" style={{ fontWeight: 600 }} gutterBottom sx={{ mt: 0 }}>
          {t('dataManagementTitle')}
        </Typography>

        <Paper className="s2-group" elevation={0} sx={{ p: 2, mb: 2, bgcolor: 'info.light', color: 'info.contrastText', display: 'flex', gap: 2 }}>
          <Typography variant="h6" sx={{ m: 0 }}>🔒</Typography>
          <Typography variant="body2" dangerouslySetInnerHTML={{ __html: t('dataNoticeText') as string }} />
        </Paper>

        <Typography variant="body2" color="text.secondary" sx={{ mb: 3 }}>
          {t('dataManagementDesc')}
        </Typography>
        
        <Box sx={{ display: 'flex', flexWrap: 'wrap', gap: 2 }}>
          <Button variant="contained" onClick={() => setShowExportModal(true)} startIcon={<DownloadSimple size={20} weight="fill" sx={{ fontSize: 18 }} />}>
            {t('exportBackup')}
          </Button>
          <Button variant="outlined" onClick={() => fileInputRef.current?.click()} startIcon={<UploadSimple size={20} weight="fill" sx={{ fontSize: 18 }} />}>
            {t('importBackup')}
          </Button>
          <input ref={fileInputRef} type="file" accept=".json" onChange={handleImportPick} style={{ display: 'none' }} />
        </Box>
      </Paper>
</Box>); title = "Storage & Cloud"; }
    else if (currentView === 4) { content = (<Box sx={{ display: 'flex', flexDirection: 'column', gap: 0 }}>      {/* ---- Data Management ---- */}
      <Paper className="s2-group" elevation={2} sx={{ p: { xs: 2, md: 3 }, mb: { xs: 2, md: 3 }, borderRadius: { xs: 0, sm: 2 }, borderX: { xs: 0, sm: undefined } }}>
        <Typography variant="h6" style={{ fontWeight: 600 }} gutterBottom sx={{ mt: 0, mb: 0.5 }}>
          {t('appUpdatesTitle')}
        </Typography>
        <Typography variant="body2" color="text.secondary" sx={{ mb: 2 }}>
          {t('appUpdatesDesc')}
        </Typography>
        <Box sx={{ display: 'flex', alignItems: 'center', gap: 2, mb: 2 }}>
          <Button variant="outlined" disabled={checkingUpdate} onClick={async () => {
            setCheckingUpdate(true);
            try {
              const res = await fetch('/api/check-update');
              const data = await res.json();
              setUpdateInfo(data);
              if (data.updateAvailable) {
                thagaval(`Update available: v${data.latest}`, 'info');
              } else if (data.error) {
                thagaval('Could not check for updates. Check internet connection.', 'warning');
              } else {
                thagaval('You are on the latest version!', 'success');
              }
            } catch {
              thagaval('Could not check for updates.', 'error');
            }
            setCheckingUpdate(false);
          }} startIcon={
            <Box component="span" sx={{ display: 'flex', animation: checkingUpdate ? 'spin 1s linear infinite' : 'none', '@keyframes spin': { '100%': { transform: 'rotate(360deg)' } } }}>
              <Refresh sx={{ fontSize: 18 }} />
            </Box>
          }>
            {checkingUpdate ? t('checkingUpdate') : t('checkForUpdates')}
          </Button>
          {updateInfo && (
            <Typography variant="body2" color="text.secondary">
              Current: v{updateInfo.current}{updateInfo.latest ? ` | Latest: v${updateInfo.latest}` : ''}
            </Typography>
          )}
        </Box>
        {updateInfo?.updateAvailable && (
          <Paper className="s2-group" elevation={0} sx={{ p: 2, bgcolor: 'primary.50', color: 'primary.900', borderRadius: 2 }}>
            <Typography variant="subtitle2" style={{ fontWeight: 'bold' }}>New version v{updateInfo.latest} is available!</Typography>
            <Typography variant="body2" sx={{ mb: 1 }}>Your data will not be affected. Click below to update:</Typography>
            <Button component="a" href="elvanniril-update://run" variant="contained" color="primary" startIcon={<DownloadSimple size={20} weight="fill" sx={{ fontSize: 18 }} />}>
              Update Now
            </Button>
          </Paper>
        )}
      </Paper>
</Box>); title = "System & Updates"; }

    return (
      <Box sx={{ width: '100%', pb: 10 }}>
        {isMobile && (
           <div className="s2-sub-header" style={{ display: 'flex', alignItems: 'center', marginBottom: 24, padding: '0 8px' }}>
              <button className="s2-back-btn" onClick={goHub} type="button">
                  <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                      <path d="M15 18l-6-6 6-6" strokeLinecap="round" strokeLinejoin="round"/>
                  </svg>
              </button>
              <div className="s2-sub-title" style={{ fontSize: 22, marginLeft: 8 }}>{title}</div>
           </div>
        )}
        {!isMobile && (
           <div className="s2-sub-header" style={{ display: 'none', alignItems: 'center', marginBottom: 24 }}>
              <div className="s2-sub-title">{title}</div>
           </div>
        )}
        {content}
      </Box>
    );
  };

  const detailContent = renderDetailView();

  const renderHub = () => (
    <div style={{ paddingBottom: 100 }}>
       <div className="s2-sub-header" style={{ display: 'none', marginBottom: 24, padding: '0 8px' }}>
           <div className="s2-sub-title">{t('settingsTitle')}</div>
       </div>

       <div className="s2-profile-card" onClick={() => handleNavigate(0)} style={{ marginBottom: 32 }}>
          <div className="s2-avatar">
              {profile.logo ? <img src={profile.logo} alt="Logo" /> : <Buildings size={20} weight="fill" className="s2-avatar-icon" />}
          </div>
          <div className="s2-profile-text">
              <div className="s2-profile-title">{profile.niruvanathinPeyar || 'Business Profile'}</div>
              <div className="s2-profile-sub">{profile.email || 'Configure your details'}</div>
          </div>
          <div style={{ color: 'var(--mac-text-secondary)', paddingRight: 8 }}>
              <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                  <path d="M9 18l6-6-6-6" strokeLinecap="round" strokeLinejoin="round"/>
              </svg>
          </div>
       </div>

       <div className="s2-section-label">Preferences</div>
       <div className="s2-group" style={{ marginBottom: 32 }}>
          <div className="s2-item" onClick={() => handleNavigate(1)}>
              <div className="s2-icon-circle blue"><Tag fontSize="small" /></div>
              <div className="s2-item-text">
                  <div className="s2-item-title">Display & Languages</div>
                  <div className="s2-item-desc">UI language, Data languages</div>
              </div>
          </div>
          <div className="s2-divider"></div>
          <div className="s2-item" onClick={() => handleNavigate(2)}>
              <div className="s2-icon-circle green"><Tag fontSize="small" /></div>
              <div className="s2-item-text">
                  <div className="s2-item-title">Billing & Payments</div>
                  <div className="s2-item-desc">Accounts, Invoice formats, Terms</div>
              </div>
          </div>
       </div>

       <div className="s2-section-label">Advanced</div>
       <div className="s2-group" style={{ marginBottom: 32 }}>
          <div className="s2-item" onClick={() => handleNavigate(3)}>
              <div className="s2-icon-circle purple"><Cloud fontSize="small" /></div>
              <div className="s2-item-text">
                  <div className="s2-item-title">Storage & Cloud</div>
                  <div className="s2-item-desc">Google Drive, Local Backups</div>
              </div>
          </div>
          <div className="s2-divider"></div>
          <div className="s2-item" onClick={() => handleNavigate(4)}>
              <div className="s2-icon-circle orange"><Refresh fontSize="small" /></div>
              <div className="s2-item-text">
                  <div className="s2-item-title">System & Updates</div>
                  <div className="s2-item-desc">App versions, Cache</div>
              </div>
          </div>
       </div>
    </div>
  );

  return (
    <div className="s2-page-view" style={isMobile ? { position: 'relative', overflow: 'hidden', height: 'calc(100vh - 60px)', paddingTop: 16, fontFamily: '"Elvan Sans", sans-serif' } : { paddingTop: 0, fontFamily: '"Elvan Sans", sans-serif' }}>
      {isMobile ? (
          <AnimatePresence mode="popLayout" custom={direction} initial={false}>
              {currentView === 'hub' ? (
                  <motion.div
                  key="hub"
                      custom={direction}
                      variants={variants}
                      initial="enter"
                      animate="center"
                      exit="exit"
                      transition={transition}
                      className="s2-col-left"
                      style={{ width: '100%', height: '100%' }}
                  >
                      {renderHub()}
                  </motion.div>
              ) : (
                  <motion.div
                      key="detail"
                      custom={direction}
                      variants={variants}
                      initial="enter"
                      animate="center"
                      exit="exit"
                      transition={transition}
                      className="s2-col-right"
                      style={{ width: '100%', height: '100%' }}
                  >
                      {detailContent}
                  </motion.div>
              )}
          </AnimatePresence>
      ) : (
          <Box sx={{ width: '100%', maxWidth: 1200, mx: 'auto', mt: 0, pt: { xs: 2, md: 4 }, pb: { xs: 2, md: 4 }, px: { xs: 0, md: 4 } }}>
              <Typography variant="h5" sx={{ mb: 4, fontWeight: 700, ml: { xs: 1.5, md: 2 } }}>{t('settingsTitle') || 'Settings'}</Typography>
              <Tabs 
                  value={currentView === 'hub' ? 0 : currentView} 
                  onChange={(_e, val) => handleNavigate(val)}
                  variant="scrollable"
                  scrollButtons="auto"
                  sx={{
                      mb: 4,
                      borderBottom: 1,
                      borderColor: 'divider',
                      '& .MuiTabs-indicator': {
                          height: 3,
                          borderTopLeftRadius: 3,
                          borderTopRightRadius: 3,
                      },
                      '& .MuiTab-root': {
                          textTransform: 'none',
                          fontWeight: 600,
                          fontSize: '0.95rem',
                          minHeight: 48,
                          px: 3
                      }
                  }}
              >
                  <Tab label="Profile" />
                  <Tab label="Display & Languages" />
                  <Tab label="Billing & Payments" />
                  <Tab label="Storage & Cloud" />
                  <Tab label="System & Updates" />
              </Tabs>
              <Box>
                  {detailContent || <div className="s2-welcome-card">Select a setting</div>}
              </Box>
            </Box>
        )}

        {/* Desktop Master Save Button */}
        <Box sx={{ display: { xs: 'none', md: 'flex' }, justifyContent: 'flex-end', gap: 2, mb: 6, mt: 5, px: { xs: 0, md: 4 } }}>
          <Button 
            variant="contained" 
            size="large"
            onClick={handleSave}
            disabled={saving}
            startIcon={<FloppyDisk size={20} weight="fill" />}
            sx={{ minWidth: 150, borderRadius: 2 }}
          >
            {saving ? t('saving') : t('saveProfile')}
          </Button>
        </Box>

        {/* Mobile FAB Save */}
        {isMobile && (
          <Fab 
            color="primary" 
            aria-label="save" 
            onClick={handleSave}
            disabled={saving}
            sx={{ position: 'fixed', bottom: 'calc(env(safe-area-inset-bottom, 0px) + 95px)', right: 20, zIndex: 1000 }}
          >
            <FloppyDisk size={20} weight="fill" />
          </Fab>
        )}

        {/* ----------------------- Export modal ----------------------- */}
      <Dialog open={showExportModal} onClose={() => !drivePending && setShowExportModal(false)} maxWidth="sm" fullWidth>
        <DialogTitle>Export Backup</DialogTitle>
        <DialogContent dividers>
          <Typography variant="body2" color="text.secondary" gutterBottom>
            Choose what to include. Everything is on by default — uncheck anything you don't want.
          </Typography>
          <Box sx={{ display: 'flex', gap: 1, mb: 2 }}>
            <Button size="small" variant="outlined" onClick={() => exportToggleAll(true)}>Select all</Button>
            <Button size="small" variant="outlined" onClick={() => exportToggleAll(false)}>Clear all</Button>
          </Box>
          <Box sx={{ display: 'flex', flexDirection: 'column', gap: 1 }}>
            {ALL_BACKUP_PARTS.map(p => (
              <FormControlLabel key={p.id} control={<Checkbox checked={!!exportSel[p.id]} onChange={() => toggleExport(p.id)} />} label={
                <Box>
                  <Typography variant="body2" style={{ fontWeight: 'bold' }}>{p.label}</Typography>
                  <Typography variant="caption" color="text.secondary">{p.hint}</Typography>
                </Box>
              } />
            ))}
          </Box>
        </DialogContent>
        <DialogActions>
          <Button color="inherit" onClick={() => setShowExportModal(false)}>{t('cancel')}</Button>
          <Button variant="contained" onClick={runExport} disabled={!Object.values(exportSel).some(Boolean)}>
            <><DownloadSimple size={20} weight="fill" sx={{ fontSize: 16 }} style={{ marginRight: 6 }} /> Download Backup</>
          </Button>
        </DialogActions>
      </Dialog>

      {/* ----------------------- Import modal ----------------------- */}
      {importInspection && (
        <Dialog open={showImportModal} onClose={() => setShowImportModal(false)} maxWidth="sm" fullWidth>
          <DialogTitle>Restore from Backup</DialogTitle>
          <DialogContent dividers>
            <Typography variant="body2" color="text.secondary" gutterBottom>
              File contents preview
              {importInspection.exportedAt && <span> — exported {new Date(importInspection.exportedAt).toLocaleString()}</span>}
              {importInspection.version && <span> · v{importInspection.version}</span>}
            </Typography>
            <Paper className="s2-group" elevation={0} sx={{ p: 1.5, bgcolor: 'warning.light', color: 'warning.contrastText', mb: 2 }}>
              <Typography variant="body2">
                ⚠ Restoring will <strong>overwrite matching records by ID</strong> in the categories you select. Records you didn't tick are untouched. We recommend exporting a fresh backup of your current data first.
              </Typography>
            </Paper>
            <Box sx={{ display: 'flex', gap: 1, mb: 2 }}>
              <Button size="small" variant="outlined" onClick={() => importToggleAll(true)}>Select all (with data)</Button>
              <Button size="small" variant="outlined" onClick={() => importToggleAll(false)}>Clear all</Button>
            </Box>
            <Box sx={{ display: 'flex', flexDirection: 'column', gap: 1 }}>
              {ALL_BACKUP_PARTS.map(p => {
                const count = importInspection.counts[p.id] || 0;
                return (
                  <Box key={p.id} sx={{ display: 'flex', alignItems: 'center', opacity: count === 0 ? 0.5 : 1 }}>
                    <FormControlLabel sx={{ flex: 1 }} control={<Checkbox checked={!!importSel[p.id]} disabled={count === 0} onChange={() => toggleImport(p.id)} />} label={
                      <Box>
                        <Typography variant="body2" style={{ fontWeight: 'bold' }}>{p.label}</Typography>
                        <Typography variant="caption" color="text.secondary">{p.hint}</Typography>
                      </Box>
                    } />
                    <Typography variant="caption" sx={{ fontWeight: count > 0 ? 'bold' : 'normal', color: count > 0 ? 'success.main' : 'text.secondary' }}>
                      {count > 0 ? `${count} item${count !== 1 ? 's' : ''}` : 'empty'}
                    </Typography>
                  </Box>
                );
              })}
            </Box>
          </DialogContent>
          <DialogActions>
            <Button color="inherit" onClick={() => setShowImportModal(false)}>{t('cancel')}</Button>
            <Button variant="contained" onClick={runImport} disabled={!Object.values(importSel).some(Boolean)} startIcon={<UploadSimple size={20} weight="fill" sx={{ fontSize: 16 }} />}>
              Restore selected
            </Button>
          </DialogActions>
        </Dialog>
      )}
    </div>
  );
// }

function EditIcon({ size }) {
  return (
    <svg width={size} height={size} viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
      <path d="M17 3a2.85 2.83 0 1 1 4 4L7.5 20.5 2 22l1.5-5.5Z" /><path d="m15 5 4 4" />
    </svg>
  );
}
