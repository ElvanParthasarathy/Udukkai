import { Package, Trash, CheckSquare, Square, Scales, Hash, SquaresFour } from '@phosphor-icons/react';
import React, { useState, useEffect } from 'react';
import { useTheme } from '@mui/material/styles';
import { Box, Typography, IconButton, Tooltip, Stack, Dialog, DialogTitle, DialogContent, DialogContentText, DialogActions, Button, ToggleButton, ToggleButtonGroup } from '@mui/material';
import { getAllProducts, saveProduct, deleteProduct } from '../../../Avanam';
import { getCountryConfig, formatCurrency, getDynamicField } from '../../../Payanpadu';
import { thagaval } from '../../Thagaval';
import { useLanguage } from '../../../mozhi/LanguageContext';
import ElvanCard from '../../ElvanCard';
import ElvanListView from '../../ElvanListView';

export default function Porul({ onAddProduct, onEditProduct, profile }) {
  const { t } = useLanguage();
  const theme = useTheme();
  const isDark = theme.palette.mode === 'dark';
  const [products, setProducts] = useState([]);
  const [isLoading, setIsLoading] = useState(true);
  const [confirmDialog, setConfirmDialog] = useState({ open: false, title: '', message: '', action: null });
  const [activeTab, setActiveTab] = useState('all');
  const { language } = useLanguage();

  const profileCountry = profile?.country || 'India';
  const profileCurrency = getCountryConfig(profileCountry).currency;

  useEffect(() => {
    let unsubs = [];

    const initRealtime = async () => {
      setIsLoading(true);
      try {
        const data = await getAllProducts(setProducts);
        if (data && data.unsubscribe) unsubs.push(data.unsubscribe);
        setProducts(data || []);
      } catch {
        thagaval(t('failedToLoadProductsToast') || 'Failed to load products', 'error');
      } finally {
        setIsLoading(false);
      }
    };

    initRealtime();

    return () => {
      unsubs.forEach(unsub => unsub());
    };
  }, []);

  const filterFn = (p, search) => {
    if (!search.trim()) return true;
    const term = search.toLowerCase();
    const searchable = [
      getDynamicField(p, 'name', profile, true), getDynamicField(p, 'name', profile, false), p.hsn, p.rate?.toString(), p.taxPercent?.toString(),
      getDynamicField(p, 'description', profile, true), getDynamicField(p, 'description', profile, false), p.unit
    ].filter(Boolean).join(' ').toLowerCase();
    return searchable.includes(term);
  };

  const handleBulkDelete = async (ids, onProgress) => {
    try {
      let count = 0;
      for (const id of ids) {
        await deleteProduct(id);
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
        const primaryLang = profile?.primaryDataLanguage || 'Tamil';
        const primaryField = `name_${primaryLang}`;
        const currentName = rest[primaryField] || '';
        await saveProduct({ ...rest, [primaryField]: `${currentName} (Copy)` });
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
          await deleteProduct(id);
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
            cursor: 'pointer',
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
                  {getDynamicField(product, 'name', profile, true) || '-'}
                </Typography>
                {isSelectionMode && (
                  <Box sx={{ width: 34, flexShrink: 0 }} />
                )}
              </Box>
              
              {profile?.enableBilingual !== false && getDynamicField(product, 'name', profile, false) && (
                <Typography variant="caption" noWrap sx={{ display: 'block', fontWeight: 500, color: 'text.secondary', mt: 0.25 }}>
                  {getDynamicField(product, 'name', profile, false)}
                </Typography>
              )}
              
              <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-end', mt: 0.5, gap: 1 }}>
                <Box sx={{ display: 'flex', flexDirection: 'column', flex: 1, minWidth: 0 }}>
                  {(product.hsn || product.taxPercent) && (
                    <Typography variant="body2" sx={{ fontSize: '0.85rem', color: 'text.secondary' }} noWrap>
                      {product.hsn ? `HSN: ${product.hsn}` : ''}
                      {product.hsn && product.taxPercent ? <span style={{ opacity: 0.6, margin: '0 6px' }}>•</span> : ''}
                      {product.taxPercent ? `Tax: ${product.taxPercent}%` : ''}
                    </Typography>
                  )}
                  <Typography variant="body2" sx={{ fontSize: '0.8rem', mt: 0.25, color: 'primary.main', fontWeight: 500 }} noWrap>
                    {(product.measureType === 'weight') ? 'எடை • Weight' : 'அளவு • Quantity'}
                  </Typography>
                </Box>
                <Typography variant="subtitle2" noWrap sx={{ fontWeight: 800, color: 'primary.main', fontSize: (product.rate && formatCurrency(product.rate, profileCurrency).length > 11) ? '0.8rem' : '0.95rem', flexShrink: 0 }}>
                  {product.rate ? formatCurrency(product.rate, profileCurrency) : '-'}
                </Typography>
              </Box>
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
                top: '12px',
                right: '12px',
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

  const tabFilteredProducts = products.filter(p => {
    if (activeTab === 'all') return true;
    return (p.measureType || 'quantity') === activeTab;
  });

  const filterChips = (
    <Box sx={{ display: 'flex', width: '100%', justifyContent: 'flex-start', alignItems: 'center', gap: 1.5, mt: 3, mb: 1.5 }}>
      <ToggleButtonGroup
        value={activeTab}
        exclusive
        onChange={(e, newTab) => {
          if (newTab !== null) {
            setActiveTab(newTab);
          }
        }}
        aria-label="measure type filter"
        sx={{ 
          bgcolor: isDark ? 'rgba(255,255,255,0.05)' : 'rgba(0,0,0,0.04)',
          p: 0.5,
          borderRadius: '50px',
          display: 'flex',
          gap: 1,
          width: { xs: '100%', sm: '360px' },
          '& .MuiToggleButtonGroup-grouped': {
            flex: 1,
            whiteSpace: 'nowrap',
            justifyContent: 'center',
            border: 0,
            borderRadius: '50px !important',
            px: 2,
            py: 0.5,
            textTransform: 'uppercase',
            fontWeight: 600,
            fontSize: '0.85rem',
            color: 'text.secondary',
            '&.Mui-selected': {
              bgcolor: 'background.paper',
              color: 'text.primary',
              boxShadow: '0 2px 8px rgba(0,0,0,0.2)',
              '@media (hover: hover)': { '&:hover': {
                bgcolor: 'background.paper',
              } }
            }
          }
        }}
      >
        <ToggleButton value="all" sx={{ display: 'flex', gap: 0.75, alignItems: 'center' }}>
          <SquaresFour size={18} weight="duotone" style={{ marginBottom: '2px' }} />
          {t('all')}
        </ToggleButton>
        <ToggleButton value="quantity" sx={{ display: 'flex', gap: 0.75, alignItems: 'center' }}>
          <Hash size={18} weight="duotone" style={{ marginBottom: '2px' }} />
          {t('quantity')}
        </ToggleButton>
        <ToggleButton value="weight" sx={{ display: 'flex', gap: 0.75, alignItems: 'center' }}>
          <Scales size={18} weight="duotone" style={{ marginBottom: '2px' }} />
          {t('weight')}
        </ToggleButton>
      </ToggleButtonGroup>
    </Box>
  );

  return (
    <>
      <ElvanListView 
        title={t('inventoryTitle') || 'Products'}
        searchPlaceholder={t('searchProducts') || 'Search products...'}
        addButtonText={t('addProductBtn') || 'Add Product'}
        renderBelowSearch={filterChips}
        onAdd={() => onAddProduct(null)}
        items={tabFilteredProducts}
        isLoading={isLoading}
        filterFn={filterFn}
        renderCard={renderCard}
        emptyIcon={<Package size={48} weight="regular" style={{ opacity: 0.5 }} />}
        emptyText={products.length === 0 ? (t('noProductsYet') || 'No Products Found') : (t('noProductsMatch') || 'No matching products')}
        onDeleteSelected={handleBulkDelete}
        onDuplicateSelected={handleBulkDuplicate}
        deleteConfirmTitle={t('deleteProductsTitle') || 'Delete Products?'}
        deleteConfirmMessage={(count) => (t('deleteProductsMessage') || 'Are you sure you want to delete {count} product(s)?').replace('{count}', count.toString())}
        duplicateConfirmTitle={t('duplicateProductsTitle') || 'Duplicate Products?'}
        duplicateConfirmMessage={(count) => (t('duplicateProductsMessage') || 'Are you sure you want to create copies of the {count} selected product(s)?').replace('{count}', count.toString())}
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
