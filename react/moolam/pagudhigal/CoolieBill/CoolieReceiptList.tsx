import { X, Receipt as PhosphorReceipt, CheckSquare, Square, Trash } from '@phosphor-icons/react';
import React, { useState, useEffect, useRef } from 'react';
import { useTheme } from '@mui/material/styles';
import { Box, Typography, Button, IconButton, Tooltip, Stack, Dialog, DialogTitle, DialogContent, DialogActions } from '@mui/material';
import { getAllCoolieReceipts, saveCoolieReceipt, deleteCoolieReceipt, getAllCoolieBills, getAllCoolieProfiles } from '../../Avanam';
import { formatCurrency, numberToWords, getCountryConfig, getDynamicField } from '../../Payanpadu';
import { thagaval } from '../Thagaval';
import { useLanguage } from '../../mozhi/LanguageContext';
import ElvanCard from '../ElvanCard';
import ElvanListView from '../ElvanListView';

export default function CoolieReceiptList({ onAddReceipt, onEditReceipt, onViewReceipt }: { onAddReceipt?: () => void, onEditReceipt?: (rcp: any) => void, onViewReceipt?: (rcp: any) => void } = {}) {
  const { t } = useLanguage();
  const [receipts, setReceipts] = useState<any[]>([]);
  const [bills, setBills] = useState<any[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [receiptToDelete, setReceiptToDelete] = useState<any>(null);
  const [localProfile, setLocalProfile] = useState<any>({});
  const profile = localProfile;
  const theme = useTheme();
  const isDark = theme.palette.mode === 'dark';
  
  const profileCurrency = getCountryConfig(profile?.country || 'India').currency;

  const renderLabel = (enLabel: string, taLabel: string) => profile?.enableBilingual !== false ? `${taLabel} / ${enLabel}` : enLabel;
  const renderPaymentMode = (mode: string) => {
    const dictionaries: Record<string, Record<string, string>> = {
      'Tamil': { 'Cash': 'பணம்', 'UPI': 'UPI', 'Bank Transfer': 'வங்கிப் பரிமாற்றம்', 'Cheque': 'காசோலை', 'Card': 'கார்டு', 'Other': 'மற்றவை' },
      'Hindi': { 'Cash': 'नकद', 'UPI': 'UPI', 'Bank Transfer': 'बैंक ट्रांसफर', 'Cheque': 'चेक', 'Card': 'कार्ड', 'Other': 'अन्य' },
      'Telugu': { 'Cash': 'నగదు', 'UPI': 'UPI', 'Bank Transfer': 'బ్యాంక్ బదిలీ', 'Cheque': 'చెక్', 'Card': 'కార్డు', 'Other': 'ఇతర' },
      'Kannada': { 'Cash': 'ನಗದು', 'UPI': 'UPI', 'Bank Transfer': 'ಬ್ಯಾಂಕ್ ವರ್ಗಾವಣೆ', 'Cheque': 'ಚೆಕ್', 'Card': 'ಕಾರ್ಡ್', 'Other': 'ಇತರ' },
      'Malayalam': { 'Cash': 'പണം', 'UPI': 'UPI', 'Bank Transfer': 'ബാങ്ക് ട്രാൻസ്ഫർ', 'Cheque': 'ചെക്ക്', 'Card': 'കാർഡ്', 'Other': 'മറ്റ്' },
      'Marathi': { 'Cash': 'रोख', 'UPI': 'UPI', 'Bank Transfer': 'बँक ट्रान्सफर', 'Cheque': 'चेक', 'Card': 'कार्ड', 'Other': 'इतर' },
      'Gujarati': { 'Cash': 'રોકડ', 'UPI': 'UPI', 'Bank Transfer': 'બેંક ટ્રાન્સફર', 'Cheque': 'ચેક', 'Card': 'કાર્ડ', 'Other': 'અન્ય' },
      'Bengali': { 'Cash': 'নগদ', 'UPI': 'UPI', 'Bank Transfer': 'ব্যাঙ্ক ট্রান্সফার', 'Cheque': 'চেক', 'Card': 'কার্ড', 'Other': 'অন্য' }
    };
    const primaryLang = profile?.primaryDataLanguage || 'Tamil';
    const primaryVal = (dictionaries[primaryLang] || {})[mode] || mode;
    return primaryVal;
  };

  const getDisplayClientNameEn = (rcp: any) => {
    if (!rcp) return '';
    if (rcp.clientNameEn) return rcp.clientNameEn;
    if (rcp.againstInvoice) {
      const linkedBill = bills.find(b => b.invoiceNumber === rcp.againstInvoice);
      if (linkedBill) {
        return linkedBill.clientNameEn || getDynamicField(linkedBill.data?.client, 'name', profile, false) || linkedBill.data?.client?.nameEn || linkedBill.data?.client?.peyarEn || '';
      }
    }
    return '';
  };

  useEffect(() => {
    let unsubs = [];

    const initRealtime = async () => {
      setIsLoading(true);
      try {
        const recs = await getAllCoolieReceipts((fresh) => setReceipts(fresh || []));
        if (recs && recs.unsubscribe) unsubs.push(recs.unsubscribe);
        setReceipts(recs || []);

        const bls = await getAllCoolieBills((fresh) => setBills(fresh || []));
        if (bls && bls.unsubscribe) unsubs.push(bls.unsubscribe);
        setBills(bls || []);

        const profs = await getAllCoolieProfiles((fresh) => {
          if (Array.isArray(fresh) && fresh.length > 0) setLocalProfile(fresh[0]);
        });
        if (profs && profs.unsubscribe) unsubs.push(profs.unsubscribe);
        if (Array.isArray(profs) && profs.length > 0) setLocalProfile(profs[0]);
      } catch {
        thagaval('Failed to load data', 'error');
      } finally {
        setIsLoading(false);
      }
    };

    initRealtime();

    const handleRefresh = () => initRealtime();
    window.addEventListener('refreshReceipts', handleRefresh);
    
    return () => {
      window.removeEventListener('refreshReceipts', handleRefresh);
      unsubs.forEach(unsub => unsub());
    };
  }, []);

  const filterFn = (r, search) => {
    if (!search.trim()) return true;
    const term = search.toLowerCase();
    const searchable = [
      r.receiptNo, r.clientName, r.clientNameEn, r.paymentMode, 
      r.referenceNo, r.againstInvoice, r.note, r.amount?.toString()
    ].filter(Boolean).join(' ').toLowerCase();
    return searchable.includes(term);
  };

  const handleBulkDelete = async (ids, onProgress) => {
    try {
      let count = 0;
      for (const id of ids) {
        await deleteCoolieReceipt(id);
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
      const selected = receipts.filter(r => ids.includes(r.id));
      let count = 0;
      for (const r of selected) {
        const { id, ...rest } = r;
        await saveCoolieReceipt({ ...rest, receiptNo: `${r.receiptNo} (Copy)` });
        count++;
        if (onProgress) onProgress(count, selected.length);
      }
      thagaval(t('invoicesDuplicatedSuccess') || 'Invoices duplicated successfully', 'success');
    } catch (e) {
      thagaval(t('errorDuplicating') || 'Error duplicating', 'error');
    }
  };

  const renderCard = (rcp, globalIndex, isSelectionMode, isSelected, toggleSelection) => {
    return (
      <ElvanCard 
        key={rcp.id} 
        sx={{ 
          height: '100%', 
          cursor: 'pointer',
          ...(isSelectionMode && isSelected ? { bgcolor: isDark ? 'rgba(255,255,255,0.06) !important' : 'rgba(0,0,0,0.04) !important' } : {})
        }} 
        onClick={() => isSelectionMode ? toggleSelection(rcp.id) : (onViewReceipt && onViewReceipt(rcp))}
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
                  {rcp.clientName || '-'}
                </Typography>
                {isSelectionMode && (
                  <Box sx={{ display: 'flex', gap: 0.5, mt: -0.5, mr: -0.5, alignItems: 'center', flexShrink: 0 }}>
                    <IconButton 
                      size="small" 
                      color="error"
                      onClick={(e) => {
                        e.stopPropagation();
                        setReceiptToDelete(rcp);
                      }}
                      sx={{ p: 0.5 }}
                    >
                      <Trash size={18} />
                    </IconButton>
                  </Box>
                )}
              </Box>
              
              {profile?.enableBilingual !== false && getDisplayClientNameEn(rcp) && (
                <Typography variant="caption" noWrap sx={{ display: 'block', fontWeight: 500, color: 'text.secondary', mt: 0.25 }}>
                  {getDisplayClientNameEn(rcp)}
                </Typography>
              )}
              
              <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-end', mt: 0.5, gap: 1 }}>
                <Typography variant="body2" sx={{ fontSize: '0.85rem', color: 'text.secondary', flex: 1, minWidth: 0 }} noWrap>
                  {rcp.receiptNo} <span style={{ opacity: 0.6, margin: '0 6px' }}>•</span> {rcp.date ? new Date(rcp.date).toLocaleDateString('en-IN') : '-'}
                </Typography>
                <Typography variant="subtitle2" noWrap sx={{ fontWeight: 800, color: 'primary.main', fontSize: formatCurrency(rcp.amount, profileCurrency).length > 11 ? '0.8rem' : '0.95rem', flexShrink: 0 }}>
                  {formatCurrency(rcp.amount, profileCurrency)}
                </Typography>
              </Box>
            </Box>
          </Box>
      </ElvanCard>
    );
  };

  const sortedReceipts = [...receipts].sort((a, b) => {
    const dateA = new Date(a.date || 0).getTime();
    const dateB = new Date(b.date || 0).getTime();
    if (dateB !== dateA) return dateB - dateA;
    
    const numA = parseInt((a.receiptNo || '').replace(/\D/g, ''), 10) || 0;
    const numB = parseInt((b.receiptNo || '').replace(/\D/g, ''), 10) || 0;
    return numB - numA;
  });

  return (
    <>
      <ElvanListView 
        title={t('receipts') || 'Receipts'}
        searchPlaceholder={t('search') || 'Search...'}
        addButtonText={t('newReceiptBtn') || 'Add Receipt'}
        onAdd={onAddReceipt ? () => onAddReceipt() : undefined}
        items={sortedReceipts}
        isLoading={isLoading}
        filterFn={filterFn}
        renderCard={renderCard}
        emptyIcon={<PhosphorReceipt size={48} weight="regular" style={{ opacity: 0.5 }} />}
        emptyText={receipts.length === 0 ? (t('noReceiptsYet') || 'No receipts yet') : (t('noReceiptsMatch') || 'No receipts match')}
        onDeleteSelected={handleBulkDelete}
        onDuplicateSelected={handleBulkDuplicate}
        deleteConfirmTitle={t('delete') || 'Delete'}
        deleteConfirmMessage={() => t('deleteReceiptConfirmMsg') || 'Are you sure you want to delete the selected receipt(s)?'}
        duplicateConfirmTitle={t('duplicateProductsTitle') || 'Duplicate Receipts?'}
        duplicateConfirmMessage={() => t('duplicateProductsMessage') || 'Are you sure you want to create copies of the selected receipt(s)?'}
      />

      <Dialog open={!!receiptToDelete} onClose={() => setReceiptToDelete(null)}>
        <DialogTitle>{t('delete') || 'Delete'}</DialogTitle>
        <DialogContent>
          <Typography>{t('deleteReceiptConfirmMsg') || 'Are you sure you want to delete this receipt?'}</Typography>
        </DialogContent>
        <DialogActions sx={{ p: 2, pt: 1 }}>
          <Button onClick={() => setReceiptToDelete(null)} color="inherit">
            {t('hc_cancel') || 'Cancel'}
          </Button>
          <Button 
            onClick={() => {
              if (receiptToDelete) {
                handleBulkDelete([receiptToDelete.id]);
                setReceiptToDelete(null);
              }
            }} 
            color="error" 
            variant="contained"
            disableElevation
            sx={{ borderRadius: 8 }}
          >
            {t('delete') || 'Delete'}
          </Button>
        </DialogActions>
      </Dialog>
    </>
  );
}
