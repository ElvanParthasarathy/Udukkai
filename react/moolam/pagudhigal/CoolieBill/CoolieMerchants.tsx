import { Users, Trash, CheckSquare, Square } from '@phosphor-icons/react';
import { useState, useEffect } from 'react';
import { getAllCoolieClients, getAllCoolieBills, deleteCoolieClient, saveCoolieClient, getAllCoolieProfiles } from '../../Avanam';
import { thagaval } from '../Thagaval';
import { useLanguage } from '../../mozhi/LanguageContext';
import { getDynamicField } from '../../Payanpadu';
import { Typography, Box, Stack, IconButton, Tooltip, Dialog, DialogTitle, DialogContent, DialogContentText, DialogActions, Button } from '@mui/material';
import { useTheme } from '@mui/material/styles';
import ElvanCard from '../ElvanCard';
import ElvanListView from '../ElvanListView';

export default function CoolieMerchants({ onEditClient, onAddClient }) {
  const { t } = useLanguage();
  const theme = useTheme();
  const isDark = theme.palette.mode === 'dark';
  const [clients, setClients] = useState([]);
  const [isLoading, setIsLoading] = useState(true);
  const [bills, setBills] = useState([]);
  const [coolieProfile, setCoolieProfile] = useState<any>(null);
  const [isLoadingProfile, setIsLoadingProfile] = useState(true);
  const [confirmDialog, setConfirmDialog] = useState({ open: false, title: '', message: '', action: null as any });

  useEffect(() => {
    let unsubs = [];

    const initRealtime = async () => {
      setIsLoading(true);
      try {
        const c = await getAllCoolieClients((fresh) => setClients(fresh || []));
        if (c && c.unsubscribe) unsubs.push(c.unsubscribe);
        setClients(c || []);

        const b = await getAllCoolieBills((fresh) => setBills(fresh || []));
        if (b && b.unsubscribe) unsubs.push(b.unsubscribe);
        setBills(b || []);

        const p = await getAllCoolieProfiles((fresh) => {
          if (fresh && fresh.length > 0) setCoolieProfile(fresh[0]);
        });
        if (p && p.unsubscribe) unsubs.push(p.unsubscribe);
        if (p && p.length > 0) setCoolieProfile(p[0]);
      } catch {
        thagaval(t('errorLoadingCustomers') || 'Error loading customers', 'error');
      } finally {
        setIsLoading(false);
        setIsLoadingProfile(false);
      }
    };

    initRealtime();

    return () => {
      unsubs.forEach(unsub => unsub());
    };
  }, []);

  const activeProfile = coolieProfile || {};

  const filterFn = (c, search) => {
    if (!search.trim()) return true;
    const term = search.toLowerCase();
    const searchable = [
      getDynamicField(c, 'name', activeProfile, true), getDynamicField(c, 'name', activeProfile, false),
      getDynamicField(c, 'address', activeProfile, true), getDynamicField(c, 'address', activeProfile, false), getDynamicField(c, 'city', activeProfile, true), getDynamicField(c, 'city', activeProfile, false)
    ].filter(Boolean).join(' ').toLowerCase();
    return searchable.includes(term);
  };

  const handleBulkDelete = async (ids, onProgress) => {
    try {
      let count = 0;
      for (const id of ids) {
        await deleteCoolieClient(id);
        count++;
        if (onProgress) onProgress(count, ids.length);
      }
      thagaval(t('deletedSuccessfully') || 'Deleted successfully', 'success');
    } catch (e) {
      thagaval(t('errorDeleting') || 'Error deleting', 'error');
    }
  };

  const handleBulkDuplicate = async (ids, onProgress) => {
    try {
      const selected = clients.filter(c => ids.includes(c.id));
      let count = 0;
      for (const client of selected) {
        const { id, ...rest } = client;
        const primaryLang = activeProfile?.primaryDataLanguage || 'Tamil';
        const primaryField = `name_${primaryLang}`;
        const fallbackField = `name`;
        const currentName = rest[primaryField] || rest[fallbackField] || '';
        
        // We set both to ensure backwards compatibility 
        await saveCoolieClient({ ...rest, [primaryField]: `${currentName} (Copy)`, [fallbackField]: `${currentName} (Copy)` });
        count++;
        if (onProgress) onProgress(count, selected.length);
      }
      thagaval(t('customersDuplicatedSuccess') || 'Customers duplicated successfully', 'success');
    } catch (e) {
      thagaval(t('errorDuplicating') || 'Error duplicating customers', 'error');
    }
  };

  const handleDeleteSingle = (id) => {
    setConfirmDialog({
      open: true,
      title: t('delete') || 'Delete?',
      message: t('removeSavedClientConfirm') || 'Are you sure you want to remove this client?',
      action: async () => {
        await deleteCoolieClient(id);
        thagaval(t('clientRemoved') || 'Client removed', 'success');
        loadData();
      }
    });
  };

  const renderCard = (client, globalIndex, isSelectionMode, isSelected, toggleSelection) => {
    return (
      <Box sx={{ position: 'relative', height: '100%' }} key={client.id}>
        <ElvanCard 
          sx={{ 
            height: '100%',
            ...(isSelectionMode && isSelected ? { bgcolor: isDark ? 'rgba(255,255,255,0.06) !important' : 'rgba(0,0,0,0.04) !important' } : {})
          }}
          onClick={() => isSelectionMode ? toggleSelection(client.id) : onEditClient(client)}
        >
          <Box sx={{ display: 'flex', gap: 1.5, alignItems: 'flex-start' }}>
            {!isSelectionMode ? (
              <Box sx={{ 
                display: 'flex', alignItems: 'center', justifyContent: 'center', 
                width: 28, height: 28, mt: 0.15, 
                borderRadius: '50%',
                bgcolor: isDark ? 'rgba(255,255,255,0.12)' : 'rgba(0,0,0,0.08)',
                flexShrink: 0
              }}>
                <Typography variant="caption" sx={{ fontWeight: 800, color: isDark ? '#FFFFFF' : '#000000', fontSize: '0.7rem', lineHeight: 1, position: 'relative', top: '1px' }}>
                  {(globalIndex + 1).toString().padStart(2, '0')}
                </Typography>
              </Box>
            ) : (
              <Box sx={{ display: 'flex', alignItems: 'center', justifyContent: 'center', width: 28, height: 28, mt: 0.15, color: isSelected ? 'primary.main' : 'text.secondary', flexShrink: 0 }}>
                {isSelected ? <CheckSquare size={24} weight="fill" /> : <Square size={24} weight="regular" />}
              </Box>
            )}
            
            <Box sx={{ minWidth: 0, flex: 1, display: 'flex', flexDirection: 'column' }}>
              <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', gap: 1 }}>
                <Typography variant="subtitle1" noWrap sx={{ fontWeight: 700, fontSize: '0.95rem', flex: 1, minWidth: 0 }}>
                  {getDynamicField(client, 'name', activeProfile, true) || client.name || client.nameEn || client.companyName || 'Unknown'}
                </Typography>
                {isSelectionMode && (
                  <Box sx={{ width: 34, flexShrink: 0 }} />
                )}
              </Box>
              
              {(getDynamicField(client, 'name', activeProfile, false) || client.nameEn) && (
                <Typography variant="caption" noWrap sx={{ display: 'block', fontWeight: 500, color: 'text.secondary', mt: 0.25 }}>
                  {getDynamicField(client, 'name', activeProfile, false) || client.nameEn}
                </Typography>
              )}
              
              <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-end', mt: 0.5, gap: 1 }}>
                <Box sx={{ display: 'flex', flexDirection: 'column', flex: 1, minWidth: 0 }}>
                  {(getDynamicField(client, 'city', activeProfile, true) || client.city) && (
                    <Typography variant="body2" sx={{ fontSize: '0.85rem', color: 'text.secondary' }} noWrap>
                      {getDynamicField(client, 'city', activeProfile, true) || client.city}
                    </Typography>
                  )}
                  {(getDynamicField(client, 'city', activeProfile, false) || client.cityEn) && (
                    <Typography variant="body2" sx={{ fontSize: '0.8rem', color: 'text.secondary', opacity: 0.8, mt: 0.25 }} noWrap>
                      {getDynamicField(client, 'city', activeProfile, false) || client.cityEn}
                    </Typography>
                  )}
                </Box>
              </Box>
            </Box>
          </Box>
        </ElvanCard>
        {isSelectionMode && (
          <Tooltip title={t('delete') || 'Delete'}>
            <IconButton 
              color="error" 
              onClick={(e) => { e.stopPropagation(); handleDeleteSingle(client.id); }}
              sx={{ 
                position: 'absolute',
                top: '50%',
                right: '16px',
                transform: 'translateY(-50%)',
                zIndex: 10
              }}
            >
              <Trash size={20} weight="regular" />
            </IconButton>
          </Tooltip>
        )}
      </Box>
    );
  };


  return (
    <>
      <ElvanListView 
        title={t('merchants')}
        searchPlaceholder={t('search')}
        addButtonText={t('addClient')}
        onAdd={() => onAddClient(null)}
        items={clients}
        isLoading={isLoading || isLoadingProfile}
        filterFn={filterFn}
        renderCard={renderCard}
        emptyIcon={<Users size={48} weight="regular" style={{ opacity: 0.5 }} />}
        emptyText={t('noClientsFound')}
        onDeleteSelected={handleBulkDelete}
        onDuplicateSelected={handleBulkDuplicate}
        deleteConfirmTitle={t('deleteMerchantsTitle') || 'Delete Merchants?'}
        deleteConfirmMessage={(count) => (t('deleteMerchantsMessage') || 'Are you sure you want to delete {count} merchant(s)?').replace('{count}', count.toString())}
        duplicateConfirmTitle={t('duplicateCustomersTitle') || 'Duplicate Customers?'}
        duplicateConfirmMessage={(count) => (t('duplicateCustomersMessage') || 'Are you sure you want to create copies of the {count} selected customer(s)?').replace('{count}', count.toString())}
      />
      <Dialog open={confirmDialog.open} onClose={() => setConfirmDialog({ ...confirmDialog, open: false })} slotProps={{ paper: { elevation: 8, sx: { borderRadius: '24px', p: 1 } } }}>
        <DialogTitle sx={{ fontWeight: 800 }}>{confirmDialog.title}</DialogTitle>
        <DialogContent>
          <DialogContentText>{confirmDialog.message}</DialogContentText>
        </DialogContent>
        <DialogActions sx={{ px: 3, pb: 2 }}>
          <Button onClick={() => setConfirmDialog({ ...confirmDialog, open: false })} color="inherit" sx={{ borderRadius: '50px', textTransform: 'none', px: 3 }}>{t('cancel') || 'Cancel'}</Button>
          <Button onClick={async () => { try { await confirmDialog.action(); } catch(e){} setConfirmDialog({ ...confirmDialog, open: false }); }} variant="contained" color="primary" sx={{ borderRadius: '50px', textTransform: 'none', px: 3, boxShadow: 'none' }}>{t('delete') || 'Delete'}</Button>
        </DialogActions>
      </Dialog>
    </>
  );
}
