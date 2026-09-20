import React, { useState, useRef } from 'react';
import { Box, Typography, Button, FormControlLabel, Checkbox, Dialog, DialogTitle, DialogContent, DialogActions } from '@mui/material';
import { DownloadSimple, UploadSimple, Cloud, CloudSlash, ArrowsClockwise } from '@phosphor-icons/react';
import { LockKeyhole } from 'lucide-react';
import { signOut } from 'firebase/auth';
import { auth } from '../../firebase';
import { SettingsPillContainer, SettingsRow } from '../ElvanSettingsSection';
import { exportAllData, importData, inspectBackup } from '../../Avanam';
import { thagaval } from '../Thagaval';

const ALL_BACKUP_PARTS = [
  { id: 'profile',        label: 'Active business profile',  hint: 'Name, address, GSTIN, bank, logo, signature' },
  { id: 'profiles',       label: 'All business profiles',    hint: 'Multi-business switcher entries' },
  { id: 'bills',          label: 'GST Invoices / bills',     hint: 'Tax invoices, proforma, credit notes, etc.' },
  { id: 'clients',        label: 'GST Clients',              hint: 'Saved client directory' },
  { id: 'products',       label: 'GST Products / Inventory', hint: 'Product catalog with HSN, rate, stock' },
  { id: 'receipts',       label: 'GST Receipts',             hint: 'Payment receipts' },
  { id: 'coolieBills',    label: 'Coolie Bills',             hint: 'Non-GST / Coolie bills' },
  { id: 'coolieProfiles', label: 'Coolie Profiles',          hint: 'Coolie business settings' },
  { id: 'coolieClients',  label: 'Coolie Clients',           hint: 'Saved coolie clients' },
  { id: 'coolieProducts', label: 'Coolie Products',          hint: 'Coolie product catalog' },
  { id: 'coolieReceipts', label: 'Coolie Receipts',          hint: 'Coolie payment receipts' },
  { id: 'termsTemplates', label: 'Terms Templates',          hint: 'Custom terms and conditions' },
  { id: 'meta',           label: 'App settings',             hint: 'Region, modules, invoice number format, display options' },
  { id: 'localStorage',   label: 'Local preferences',        hint: 'Custom units, theme, last region preference' },
];

export default function DevTools({ profile, driveConnected, setDriveConnected, connecting, setConnecting, t }) {
  const fileInputRef = useRef(null);

  const [showExportModal, setShowExportModal] = useState(false);
  const [showImportModal, setShowImportModal] = useState(false);
  const [exportSel, setExportSel] = useState(() => Object.fromEntries(ALL_BACKUP_PARTS.map(p => [p.id, true])));
  const [importSel, setImportSel] = useState(() => Object.fromEntries(ALL_BACKUP_PARTS.map(p => [p.id, true])));
  const [importInspection, setImportInspection] = useState<any>(null);
  const [importJsonText, setImportJsonText] = useState('');

  const [updateInfo, setUpdateInfo] = useState<any>(null);
  const [checkingUpdate, setCheckingUpdate] = useState(false);

  const toggleExport = (id) => setExportSel(prev => ({ ...prev, [id]: !prev[id] }));
  const toggleImport = (id) => setImportSel(prev => ({ ...prev, [id]: !prev[id] }));
  const exportToggleAll = (val) => setExportSel(Object.fromEntries(ALL_BACKUP_PARTS.map(p => [p.id, val])));
  const importToggleAll = (val) => setImportSel(Object.fromEntries(ALL_BACKUP_PARTS.map(p => [p.id, val])));

  const runExport = async () => {
    try {
      const json = await exportAllData(exportSel);
      const fileName = `elvanniril-backup-${new Date().toISOString().split('T')[0]}.json`;

      const blob = new Blob([json], { type: 'application/json' });
      const url = URL.createObjectURL(blob);
      const a = document.createElement('a');
      a.href = url; a.download = fileName; a.click();
      URL.revokeObjectURL(url);

      thagaval('Backup downloaded', 'success');
      setShowExportModal(false);
    } catch (err: any) {
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
      const auto: any = {};
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
      ALL_BACKUP_PARTS.forEach(p => {
        if (p.id === 'profile' || p.id === 'meta' || p.id === 'localStorage') return; // Handled specially or hidden from numbers
        if (importSel[p.id] && result.counts[p.id] > 0) parts.push(`${result.counts[p.id]} ${p.label.toLowerCase()}`);
      });
      if (importSel.profile && result.counts.profile > 0) parts.push('Active Profile');
      thagaval(`Imported: ${parts.join(', ')}`, 'success');
      setShowImportModal(false);
      setTimeout(() => window.location.reload(), 1500);
    } catch (err: any) {
      thagaval('Import failed: ' + (err.message || 'unknown error'), 'error');
    }
  };

  return (
    <Box sx={{ display: 'flex', flexDirection: 'column', gap: 2 }}>
      <SettingsPillContainer>
        <SettingsRow
          icon={<DownloadSimple size={20} weight="fill" />}
          iconColor="blue"
          title={t('exportBackup') || 'Export Backup'}
          description="Save all your profiles and settings to a JSON file."
          control={
            <Button variant="outlined" size="small" sx={{ borderRadius: 8 }} onClick={() => setShowExportModal(true)}>
              Export
            </Button>
          }
        />
        <SettingsRow
          icon={<UploadSimple size={20} weight="fill" />}
          iconColor="orange"
          title={t('importBackup') || 'Import Backup'}
          description="Restore your data from a previous backup file."
          control={
            <>
              <Button variant="outlined" size="small" sx={{ borderRadius: 8 }} onClick={() => (fileInputRef.current as any)?.click()}>
                Import
              </Button>
              <input ref={fileInputRef} type="file" accept=".json" onChange={handleImportPick} style={{ display: 'none' }} />
            </>
          }
        />
      </SettingsPillContainer>
      
      <SettingsPillContainer>
        <SettingsRow
          icon={<ArrowsClockwise size={20} weight="fill" />}
          iconColor="blue"
          title="App Version"
          description={updateInfo ? `Current: v${updateInfo.current}${updateInfo.latest ? ` | Latest: v${updateInfo.latest}` : ''}` : "Checking for updates manually."}
          control={
            <Button variant="outlined" size="small" disabled={checkingUpdate} onClick={async () => {
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
            }}>
              {checkingUpdate ? (t('checkingUpdate') || 'Checking...') : (t('checkForUpdates') || 'Check for Updates')}
            </Button>
          }
        />

        {updateInfo?.updateAvailable && (
          <SettingsRow
            title="New Version Available"
            description={`v${updateInfo.latest} is ready to install.`}
            control={
              <Button component="a" href="elvanniril-update://run" variant="contained" color="primary" size="small">
                Update Now
              </Button>
            }
          />
        )}
      </SettingsPillContainer>

      {/* Export modal */}
      <Dialog open={showExportModal} onClose={() => setShowExportModal(false)} maxWidth="sm" fullWidth disableScrollLock>
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
        <DialogActions sx={{ px: 3, py: 2 }}>
          <Button onClick={() => setShowExportModal(false)}>Cancel</Button>
          <Button variant="contained" onClick={runExport} disabled={!Object.values(exportSel).some(Boolean)}>
            Download .json
          </Button>
        </DialogActions>
      </Dialog>

      {/* Import modal */}
      <Dialog open={showImportModal} onClose={() => setShowImportModal(false)} maxWidth="sm" fullWidth disableScrollLock>
        <DialogTitle>Import Backup</DialogTitle>
        <DialogContent dividers>
          <Typography variant="body2" color="text.secondary" gutterBottom>
            Select what you want to restore from this file. Existing data might be overwritten!
          </Typography>
          <Box sx={{ display: 'flex', gap: 1, mb: 2 }}>
            <Button size="small" variant="outlined" onClick={() => importToggleAll(true)}>Select all</Button>
            <Button size="small" variant="outlined" onClick={() => importToggleAll(false)}>Clear all</Button>
          </Box>
          <Box sx={{ display: 'flex', flexDirection: 'column', gap: 1 }}>
            {importInspection && ALL_BACKUP_PARTS.map(p => {
              const count = importInspection.counts[p.id] || 0;
              const hasData = count > 0 || (p.id === 'localStorage' && importInspection.counts[p.id] > 0) || (p.id === 'meta' && count > 0);
              return (
                <FormControlLabel key={p.id} disabled={!hasData} control={<Checkbox checked={!!importSel[p.id]} onChange={() => toggleImport(p.id)} />} label={
                  <Box sx={{ opacity: hasData ? 1 : 0.5 }}>
                    <Typography variant="body2" style={{ fontWeight: 'bold' }}>{p.label}</Typography>
                    <Typography variant="caption" color="text.secondary">
                      {hasData ? (p.id === 'profile' || p.id === 'meta' || p.id === 'localStorage' ? 'Included' : `${count} items found`) : 'None found in file'}
                    </Typography>
                  </Box>
                } />
              );
            })}
          </Box>
        </DialogContent>
        <DialogActions sx={{ px: 3, py: 2 }}>
          <Button onClick={() => setShowImportModal(false)}>Cancel</Button>
          <Button variant="contained" color="warning" onClick={runImport} disabled={!Object.values(importSel).some(Boolean)}>
            Import Selected
          </Button>
        </DialogActions>
      </Dialog>
    </Box>
  );
}
