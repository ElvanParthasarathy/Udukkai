import { Package, Trash, CheckSquare, Square } from '@phosphor-icons/react';
import React, { useState, useEffect } from 'react';
import { useTheme } from '@mui/material/styles';
import { Box, Typography, IconButton, Tooltip, Stack, Dialog, DialogTitle, DialogContent, DialogContentText, DialogActions, Button } from '@mui/material';
import { getAllCoolieProducts, saveCoolieProduct, deleteCoolieProduct, getAllCoolieProfiles } from '../../Avanam';
import { getDynamicField } from '../../Payanpadu';
import { thagaval } from '../Thagaval';
import { useLanguage } from '../../mozhi/LanguageContext';
import ElvanCard from '../ElvanCard';
import ElvanListView from '../ElvanListView';

export default function CoolieItems({ onAddProduct, onEditProduct }) {
  const { t } = useLanguage();
  const theme = useTheme();
  const isDark = theme.palette.mode === 'dark';
  const [products, setProducts] = useState([]);
  const [isLoading, setIsLoading] = useState(true);
  const [coolieProfile, setCoolieProfile] = useState<any>(null);
  const [isLoadingProfile, setIsLoadingProfile] = useState(true);
  const [confirmDialog, setConfirmDialog] = useState({ open: false, title: '', message: '', action: null as any });

  useEffect(() => {
    let unsubs = [];

    const initRealtime = async () => {
      setIsLoading(true);
      try {
        const data = await getAllCoolieProducts((fresh) => setProducts(fresh || []));
        if (data && data.unsubscribe) unsubs.push(data.unsubscribe);
        setProducts(data || []);

        const p = await getAllCoolieProfiles((fresh) => {
          if (fresh && fresh.length > 0) setCoolieProfile(fresh[0]);
        });
        if (p && p.unsubscribe) unsubs.push(p.unsubscribe);
        if (p && p.length > 0) setCoolieProfile(p[0]);
      } catch {
        thagaval(t('failedToLoadProductsToast') || 'Failed to load products', 'error');
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

  const filterFn = (p, search) => {
    if (!search.trim()) return true;
    const term = search.toLowerCase();
    const searchable = [
      getDynamicField(p, 'name', activeProfile, true) || p.name, 
      getDynamicField(p, 'name', activeProfile, false) || p.nameEn
    ].filter(Boolean).join(' ').toLowerCase();
    return searchable.includes(term);
  };

  const handleBulkDelete = async (ids, onProgress) => {
    try {
      let count = 0;
      for (const id of ids) {
        await deleteCoolieProduct(id);
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
      const selected = products.filter(p => ids.includes(p.id));
      let count = 0;
      for (const product of selected) {
        const { id, ...rest } = product;
        const primaryLang = activeProfile?.primaryDataLanguage || 'Tamil';
        const primaryField = `name_${primaryLang}`;
        const fallbackField = `name`;
        const currentName = rest[primaryField] || rest[fallbackField] || '';
        
        await saveCoolieProduct({ ...rest, [primaryField]: `${currentName} (Copy)`, [fallbackField]: `${currentName} (Copy)` });
        count++;
        if (onProgress) onProgress(count, selected.length);
      }
      thagaval(t('productsDuplicatedSuccess') || 'Products duplicated successfully', 'success');
    } catch (e) {
      thagaval(t('errorDuplicating') || 'Error duplicating', 'error');
    }
  };

  const handleDeleteSingle = (id) => {
    setConfirmDialog({
      open: true,
      title: t('delete') || 'Delete?',
      message: t('deleteProductConfirmMsg') || 'Are you sure you want to delete this product?',
      action: async () => {
        try {
          await deleteCoolieProduct(id);
          thagaval(t('productDeletedToast') || 'Deleted successfully', 'success');
          loadProducts();
        } catch {
          thagaval('Failed to delete', 'error');
        }
      }
    });
  };

  const renderCard = (product, globalIndex, isSelectionMode, isSelected, toggleSelection) => {
    return (
      <Box sx={{ position: 'relative', height: '100%' }} key={product.id}>
        <ElvanCard 
          sx={{ 
            height: '100%',
            ...(isSelectionMode && isSelected ? { bgcolor: isDark ? 'rgba(255,255,255,0.06) !important' : 'rgba(0,0,0,0.04) !important' } : {})
          }}
          onClick={() => isSelectionMode ? toggleSelection(product.id) : onEditProduct(product)}
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
                  {getDynamicField(product, 'name', activeProfile, true) || product.name || '-'}
                </Typography>
                {isSelectionMode && (
                  <Box sx={{ width: 34, flexShrink: 0 }} />
                )}
              </Box>
              
              {(getDynamicField(product, 'name', activeProfile, false) || product.nameEn) && (
                <Typography variant="caption" noWrap sx={{ display: 'block', fontWeight: 500, color: 'text.secondary', mt: 0.25 }}>
                  {getDynamicField(product, 'name', activeProfile, false) || product.nameEn}
                </Typography>
              )}
            </Box>
          </Box>
        </ElvanCard>
        {isSelectionMode && (
          <Tooltip title={t('delete') || 'Delete'}>
            <IconButton 
              color="error" 
              size="small" 
              onClick={(e) => { e.stopPropagation(); handleDeleteSingle(product.id); }} 
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
        title={t('itemsTitle') || 'Coolie Items'}
        searchPlaceholder={t('searchProducts') || 'Search items...'}
        addButtonText={t('addProductBtn') || 'Add Item'}
        onAdd={() => onAddProduct(null)}
        items={products}
        isLoading={isLoading || isLoadingProfile}
        filterFn={filterFn}
        renderCard={renderCard}
        emptyIcon={<Package size={48} weight="regular" style={{ opacity: 0.5 }} />}
        emptyText={products.length === 0 ? (t('noProductsYet') || 'No Items Found') : (t('noProductsMatch') || 'No matching items')}
        onDeleteSelected={handleBulkDelete}
        onDuplicateSelected={handleBulkDuplicate}
        deleteConfirmTitle={t('deleteProductsTitle') || 'Delete Items?'}
        deleteConfirmMessage={(count) => (t('deleteProductsMessage') || 'Are you sure you want to delete {count} item(s)?').replace('{count}', count.toString())}
        duplicateConfirmTitle={t('duplicateProductsTitle') || 'Duplicate Items?'}
        duplicateConfirmMessage={(count) => (t('duplicateProductsMessage') || 'Are you sure you want to create copies of the {count} selected item(s)?').replace('{count}', count.toString())}
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
