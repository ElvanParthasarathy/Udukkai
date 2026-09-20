import { FileText, CheckSquare, Square, Trash } from '@phosphor-icons/react';
import React, { useState, useEffect } from 'react';
import { useTheme } from '@mui/material/styles';
import { Box, Typography, IconButton, Tooltip, Stack, Dialog, DialogTitle, DialogContent, DialogContentText, DialogActions, Button } from '@mui/material';
import { getAllBills, deleteBill, saveBill } from '../../Avanam';
import { formatCurrency, INVOICE_TYPES, getCountryConfig, getDynamicField } from '../../Payanpadu';
import { thagaval } from '../Thagaval';
import { useLanguage } from '../../mozhi/LanguageContext';
import ElvanCard from '../ElvanCard';
import ElvanListView from '../ElvanListView';

export default function InvoiceList({ onView, onDuplicate, onNew, profile }) {
  const { t } = useLanguage();
  const theme = useTheme();
  const isDark = theme.palette.mode === 'dark';
  const [bills, setBills] = useState([]);
  const [isLoading, setIsLoading] = useState(true);
  const [invoiceToDelete, setInvoiceToDelete] = useState(null);

  const profileCurrency = getCountryConfig(profile?.country || 'India').currency;

  useEffect(() => {
    let unsub = null;

    const loadData = async () => {
      setIsLoading(true);
      try {
        const handleSetBills = (newBills) => {
          setBills(newBills.sort((a, b) => {
            const timeDiff = new Date(b.invoiceDate).getTime() - new Date(a.invoiceDate).getTime();
            if (timeDiff !== 0) return timeDiff;
            const getNum = (inv) => {
              if (!inv) return 0;
              const match = inv.match(/\d+/g);
              return match ? parseInt(match[match.length - 1], 10) : 0;
            };
            return getNum(b.invoiceNumber) - getNum(a.invoiceNumber);
          }));
        };
        const b = await getAllBills(handleSetBills);
        if (b && b.unsubscribe) unsub = b.unsubscribe;
        handleSetBills(b);
      } catch {
        thagaval(t('errorLoadingInvoices') || 'Failed to load invoices', 'error');
      } finally {
        setIsLoading(false);
      }
    };

    loadData();

    return () => {
      if (unsub) unsub();
    };
  }, []);



  const filterFn = (b, search) => {
    if (!search.trim()) return true;
    const term = search.toLowerCase();
    const itemsText = b.items ? b.items.map((i) => `${i.name || ''} ${i.nameEn || ''}`).join(' ') : '';
    const searchable = [
      b.invoiceNumber, b.clientName, b.clientNameEn, b.clientPhone, b.clientEmail, b.clientGstin,
      b.totalAmount?.toString(), b.status, b.invoiceType, b.poNumber, itemsText
    ].filter(Boolean).join(' ').toLowerCase();
    return searchable.includes(term);
  };

  const handleBulkDelete = async (ids, onProgress) => {
    try {
      let count = 0;
      for (const id of ids) {
        await deleteBill(id);
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
      const selected = bills.filter(b => ids.includes(b.id));
      let count = 0;
      for (const bill of selected) {
        const { id, ...rest } = bill;
        const dup = { ...rest };
        
        dup.id = `dup_${Date.now()}_${Math.random().toString(36).substring(2, 9)}`;
        dup.invoiceNumber = `${bill.invoiceNumber || ''} (Copy)`;
        
        await saveBill(dup);
        count++;
        if (onProgress) onProgress(count, selected.length);
      }
      thagaval(t('invoicesDuplicatedSuccess') || 'Invoices duplicated successfully', 'success');
    } catch (e) {
      thagaval(t('errorDuplicating') || 'Error duplicating invoices', 'error');
    }
  };

  const renderCard = (bill, globalIndex, isSelectionMode, isSelected, toggleSelection) => {
    const invoiceTypeKey = (bill.invoiceType || 'tax-invoice').replace(/-/g, '_');
    const invoiceTypeLabel = t(`invoiceTypes_${invoiceTypeKey}`, { defaultValue: INVOICE_TYPES[bill.invoiceType || 'tax-invoice']?.label });
    
    return (
      <Box sx={{ position: 'relative', height: '100%' }} key={bill.id}>
        <ElvanCard
          sx={{
            height: '100%',
            cursor: 'pointer',
            ...(isSelectionMode && isSelected ? { bgcolor: isDark ? 'rgba(255,255,255,0.06) !important' : 'rgba(0,0,0,0.04) !important' } : {})
          }}
          onClick={() => isSelectionMode ? toggleSelection(bill.id) : (onView && onView(bill))}
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
                    {bill.clientName || '-'}
                  </Typography>
                  {isSelectionMode && (
                    <Box sx={{ width: 34, flexShrink: 0 }} />
                  )}
                </Box>
                
                {profile?.enableBilingual !== false && (bill.clientNameEn || getDynamicField(bill.data?.client, 'name', profile, false) || bill.data?.client?.nameEn) && (
                  <Typography variant="caption" noWrap sx={{ display: 'block', fontWeight: 500, color: 'text.secondary', mt: 0.25 }}>
                    {bill.clientNameEn || getDynamicField(bill.data?.client, 'name', profile, false) || bill.data?.client?.nameEn}
                  </Typography>
                )}
                
                <Typography variant="body2" sx={{ fontSize: '0.85rem', color: 'text.secondary', mt: 0.5 }} noWrap>
                  {bill.invoiceNumber} <span style={{ opacity: 0.6, margin: '0 6px' }}>•</span> {bill.invoiceDate ? new Date(bill.invoiceDate).toLocaleDateString('en-IN') : '-'}
                </Typography>
                
                <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-end', mt: 0.25, gap: 1 }}>
                  <Typography variant="body2" sx={{ fontSize: '0.85rem', color: 'text.secondary', flex: 1, minWidth: 0 }} noWrap>
                    {invoiceTypeLabel}
                  </Typography>
                  <Typography variant="subtitle2" noWrap sx={{ fontWeight: 800, color: 'primary.main', fontSize: formatCurrency(bill.totalAmount, profileCurrency).length > 11 ? '0.8rem' : '0.95rem', flexShrink: 0 }}>
                    {formatCurrency(bill.totalAmount, profileCurrency)}
                  </Typography>
                </Box>
              </Box>
          </Box>
        </ElvanCard>
        {isSelectionMode && (
          <IconButton 
            size="small" 
            onClick={(e) => { e.stopPropagation(); setInvoiceToDelete(bill); }} 
            sx={{ 
              position: 'absolute',
              top: '12px',
              right: '12px',
              zIndex: 10,
              color: 'error.main'
            }}
          >
            <Trash size={20} />
          </IconButton>
        )}
      </Box>
    );
  };

  return (
    <>
    <ElvanListView 
      title={t('invoicesCount') || 'Invoices'}
      searchPlaceholder={t('search') || 'Search...'}
      addButtonText={t('newInvoiceBtn') || 'New Invoice'}
      onAdd={() => onNew()}
      items={bills}
      isLoading={isLoading}
      filterFn={filterFn}
      renderCard={renderCard}
      emptyIcon={<FileText size={48} weight="regular" style={{ opacity: 0.5 }} />}
      emptyText={bills.length === 0 ? (t('noInvoicesYet') || 'No invoices yet') : (t('noInvoicesMatch') || 'No invoices match your search')}
      onDeleteSelected={handleBulkDelete}
      onDuplicateSelected={handleBulkDuplicate}
      deleteConfirmTitle={t('deleteProductsTitle') || 'Delete Invoices?'}
      deleteConfirmMessage={() => t('deleteProductsMessage') || 'Are you sure you want to delete the selected invoice(s)? This action cannot be undone.'}
      duplicateConfirmTitle={t('duplicateProductsTitle') || 'Duplicate Invoices?'}
      duplicateConfirmMessage={() => t('duplicateProductsMessage') || 'Are you sure you want to create copies of the selected invoice(s)?'}
    />

      <Dialog
        open={Boolean(invoiceToDelete)}
        onClose={() => setInvoiceToDelete(null)}
        slotProps={{ paper: { sx: { bgcolor: 'background.paper', backgroundImage: 'none' } } }}
      >
        <DialogTitle sx={{ pb: 1, color: 'error.main' }}>{t('delete') || 'Delete'}</DialogTitle>
        <DialogContent>
          <DialogContentText>{t('deleteInvoiceConfirmMsg') || 'Are you sure you want to delete this invoice?'}</DialogContentText>
        </DialogContent>
        <DialogActions sx={{ px: 3, pb: 3, pt: 1 }}>
          <Button onClick={() => setInvoiceToDelete(null)} sx={{ color: 'text.secondary' }}>{t('hc_cancel') || 'Cancel'}</Button>
          <Button 
            variant="contained" 
            color="error"
            onClick={async () => {
              if (invoiceToDelete) {
                try {
                  await deleteBill(invoiceToDelete.id);
                  thagaval(t('deletedSuccessfully') || 'Deleted successfully', 'success');
                } catch (e) {
                  thagaval(t('errorDeleting') || 'Error deleting', 'error');
                }
                setInvoiceToDelete(null);
              }
            }}
          >
            {t('delete') || 'Delete'}
          </Button>
        </DialogActions>
      </Dialog>
    </>
  );
}
