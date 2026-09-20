import { FileText, CheckSquare, Square, Trash, Factory } from '@phosphor-icons/react';
import React, { useState, useEffect } from 'react';
import { useTheme } from '@mui/material/styles';
import { Box, Typography, IconButton, Stack, Dialog, DialogTitle, DialogContent, DialogContentText, DialogActions, Button, ToggleButton, ToggleButtonGroup } from '@mui/material';
import { getAllCoolieBills, deleteCoolieBill, saveCoolieBill, getAllCoolieProfiles } from '../../Avanam';
import { thagaval } from '../Thagaval';
import { useLanguage } from '../../mozhi/LanguageContext';
import ElvanCard from '../ElvanCard';
import ElvanListView from '../ElvanListView';

export default function CoolieInvoiceList({ onView, onNew }) {
  const { t } = useLanguage();
  const theme = useTheme();
  const isDark = theme.palette.mode === 'dark';
  const [bills, setBills] = useState([]);
  const [profiles, setProfiles] = useState([]);
  const [isLoading, setIsLoading] = useState(true);
  const [invoiceToDelete, setInvoiceToDelete] = useState(null);
  const [activeTab, setActiveTab] = useState('all');

  const loadData = async () => {
    setIsLoading(true);
    try {
      const b = await getAllCoolieBills();
      const p = await getAllCoolieProfiles();

      const sortBills = (billsArr) => {
        return (billsArr || []).sort((a, b) => {
          const aMatch = String(a.bill_no || '').match(/\d+/);
          const bMatch = String(b.bill_no || '').match(/\d+/);
          const aNo = aMatch ? parseInt(aMatch[0], 10) : 0;
          const bNo = bMatch ? parseInt(bMatch[0], 10) : 0;
          return bNo - aNo;
        });
      };

      setBills(sortBills(b));
      setProfiles(p || []);
    } catch {
      thagaval(t('errorLoadingInvoices') || 'Failed to load invoices', 'error');
    } finally {
      setIsLoading(false);
    }
  };

  useEffect(() => {
    let unsubs = [];

    const sortBills = (billsArr) => {
      return (billsArr || []).sort((a, b) => {
        const aMatch = String(a.bill_no || '').match(/\d+/);
        const bMatch = String(b.bill_no || '').match(/\d+/);
        const aNo = aMatch ? parseInt(aMatch[0], 10) : 0;
        const bNo = bMatch ? parseInt(bMatch[0], 10) : 0;
        return bNo - aNo;
      });
    };

    const initRealtime = async () => {
      setIsLoading(true);
      try {
        const b = await getAllCoolieBills((fresh) => setBills(sortBills(fresh)));
        if (b && b.unsubscribe) unsubs.push(b.unsubscribe);
        setBills(sortBills(b));

        const p = await getAllCoolieProfiles((fresh) => setProfiles(fresh || []));
        if (p && p.unsubscribe) unsubs.push(p.unsubscribe);
        setProfiles(p || []);
      } catch (e) {
        thagaval(t('errorLoadingInvoices') || 'Failed to load invoices', 'error');
      } finally {
        setIsLoading(false);
      }
    };

    initRealtime();

    return () => {
      unsubs.forEach(unsub => unsub());
    };
  }, []);

  // Tanglish Normalization Logic (from Kananam)
  const normalizeBasic = (text) => String(text || '').toLowerCase().replace(/[\s\-_/.,]+/g, '');
  const normalizeTanglish = (text) => {
      let s = normalizeBasic(text);
      if (!s) return s;
      s = s.replace(/aa/g, 'a').replace(/ee/g, 'e').replace(/ii/g, 'i').replace(/oo/g, 'o').replace(/uu/g, 'u')
           .replace(/ph/g, 'f').replace(/bh/g, 'b').replace(/dh/g, 'd').replace(/th/g, 't').replace(/kh/g, 'k')
           .replace(/gh/g, 'g').replace(/ch/g, 's').replace(/sh/g, 's').replace(/zh/g, 'l').replace(/j/g, 's')
           .replace(/c/g, 's').replace(/z/g, 's').replace(/ng/g, 'n').replace(/nj/g, 'n').replace(/rr/g, 'r')
           .replace(/ll/g, 'l').replace(/nn/g, 'n').replace(/bb/g, 'b').replace(/pp/g, 'p').replace(/ff/g, 'f')
           .replace(/b/g, 'p').replace(/f/g, 'p').replace(/d/g, 't').replace(/g/g, 'k').replace(/v/g, 'w');
      return s.replace(/(.)\1+/g, '$1');
  };
  const consonantSkeleton = (text) => normalizeTanglish(text).replace(/[aeiou]/g, '');

  const buildHaystack = (b) => {
      const parts = [
        b.customer_name, b.customer_name_en, b.bill_no, b.city, b.city_en, b.date, b.grand_total, b.total_weight,
        b.sub_total, b.ahimsa_silk_rs, b.courier_rs, b.setharam_grams,
        b.other_charges_name, b.other_charges_amount, b.notes, b.terms
      ];
      if (b.items && Array.isArray(b.items)) {
        b.items.forEach(item => {
            parts.push(item.porul, item.name_tamil, item.name_english, item.kg, item.coolie);
        });
      }
      const raw = parts.filter(Boolean).join(' ');
      return [raw.toLowerCase(), normalizeBasic(raw), normalizeTanglish(raw), consonantSkeleton(raw)].filter(Boolean);
  };

  const buildNeedles = (input) => {
      const raw = String(input || '').toLowerCase().trim();
      if (!raw) return [];
      return [raw, normalizeBasic(raw), normalizeTanglish(raw), consonantSkeleton(raw)].filter(Boolean);
  };

  const filterFn = (b, search) => {
      const needles = buildNeedles(search);
      if (needles.length === 0) return true;
      const haystack = buildHaystack(b);
      return needles.some(n => haystack.some(h => h.includes(n)));
  };

  const handleBulkDelete = async (ids, onProgress) => {
    try {
      let count = 0;
      for (const id of ids) {
        await deleteCoolieBill(id);
        count++;
        if (onProgress) onProgress(count, ids.length);
      }
      thagaval(t('deletedSuccessfully') || 'Deleted successfully', 'success');
      loadData();
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
        dup.bill_no = `${bill.bill_no || ''} (Copy)`;
        
        await saveCoolieBill(dup);
        count++;
        if (onProgress) onProgress(count, selected.length);
      }
      loadData();
      thagaval(t('invoicesDuplicatedSuccess') || 'Invoices duplicated successfully', 'success');
    } catch (e) {
      thagaval(t('errorDuplicating') || 'Error duplicating invoices', 'error');
    }
  };

  const renderCard = (bill, globalIndex, isSelectionMode, isSelected, toggleSelection) => {
    const companyProfile = profiles.find(p => p.id === bill.company_id);
    const companyShortName = bill.company_id === 'legacy' ? 'Legacy' : (companyProfile?.shortBusinessName || companyProfile?.name || 'Company');

    
    return (
      <Box sx={{ position: 'relative', height: '100%' }} key={bill.id}>
        <ElvanCard
          sx={{
            height: '100%',
            cursor: 'pointer',
            ...(isSelectionMode && isSelected ? { bgcolor: isDark ? 'rgba(255,255,255,0.06) !important' : 'rgba(0,0,0,0.04) !important' } : {})
          }}
          onClick={(e) => {
            if (isSelectionMode) {
              toggleSelection(bill.id);
            } else if (onView) {
              const p = profiles.find(pr => pr.id === bill.company_id);
              if (p) bill._companyProfile = p;
              onView(bill);
            }
          }}
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
                    {bill.customer_name ? (bill.customer_name.includes('/') ? bill.customer_name.split('/')[0].trim() : bill.customer_name) : '-'}
                  </Typography>
                  {isSelectionMode && (
                    <Box sx={{ width: 34, flexShrink: 0 }} />
                  )}
                </Box>
                
                {bill.customer_name_en && (
                  <Typography variant="caption" noWrap sx={{ display: 'block', fontWeight: 500, color: 'text.secondary', mt: 0.25 }}>
                    {bill.customer_name_en}
                  </Typography>
                )}
                
                <Typography variant="body2" sx={{ fontSize: '0.85rem', color: 'text.secondary', mt: 0.5 }} noWrap>
                  {bill.bill_no} <span style={{ opacity: 0.6, margin: '0 6px' }}>•</span> {bill.date ? bill.date : '-'}
                </Typography>
                
                <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-end', mt: 0.25, gap: 1 }}>
                  <Typography variant="body2" sx={{ fontSize: '0.82rem', color: 'text.secondary', flex: 1, minWidth: 0 }} noWrap>
                    {bill.city || ''}
                  </Typography>
                  <Typography variant="subtitle2" noWrap sx={{ fontWeight: 800, color: 'primary.main', fontSize: `₹ ${Number(bill.grand_total || 0).toLocaleString('en-IN')}`.length > 11 ? '0.8rem' : '0.95rem', flexShrink: 0 }}>
                    ₹ {Number(bill.grand_total || 0).toLocaleString('en-IN')}
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

  const parseDate = (dString: string) => {
    if (!dString) return 0;
    const parts = dString.split('/');
    if (parts.length === 3) {
      return new Date(`${parts[2]}-${parts[1]}-${parts[0]}`).getTime();
    }
    return new Date(dString).getTime() || 0;
  };

  const tabFilteredBills = bills.filter(b => {
    if (activeTab === 'all') return true;
    return b.company_id === activeTab;
  }).sort((a, b) => {
    const timeA = parseDate(a.date);
    const timeB = parseDate(b.date);
    if (timeA !== timeB) return timeB - timeA;
    
    // Tie-breaker: bill number descending
    const getNum = (n: string) => {
      const match = String(n || '').match(/\d+/);
      return match ? parseInt(match[0], 10) : 0;
    };
    return getNum(b.bill_no) - getNum(a.bill_no);
  });

  const legacyExists = bills.some(b => b.company_id === 'legacy');

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
        aria-label="company filter"
        sx={{ 
          bgcolor: isDark ? 'rgba(255,255,255,0.05)' : 'rgba(0,0,0,0.04)',
          p: 0.5,
          borderRadius: '50px',
          display: { xs: 'flex', sm: 'inline-flex' },
          gap: 1,
          flexWrap: { xs: 'nowrap', sm: 'wrap' },
          overflowX: { xs: 'auto', sm: 'visible' },
          width: { xs: '100%', sm: 'fit-content' },
          scrollbarWidth: 'none',
          '&::-webkit-scrollbar': { display: 'none' },
          '& .MuiToggleButtonGroup-grouped': {
            flex: { xs: 1, sm: '0 0 auto' },
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
          <Factory size={18} weight="duotone" style={{ marginBottom: '2px' }} />
          {t('all') || 'All'}
        </ToggleButton>
        {profiles.map(p => (
          <ToggleButton key={p.id} value={p.id} sx={{ display: 'flex', gap: 0.75, alignItems: 'center' }}>
            <Factory size={18} weight="duotone" style={{ marginBottom: '2px' }} />
            {p.shortBusinessName || p.name}
          </ToggleButton>
        ))}
        {legacyExists && (
          <ToggleButton value="legacy" sx={{ display: 'flex', gap: 0.75, alignItems: 'center' }}>
            <Factory size={18} weight="duotone" style={{ marginBottom: '2px' }} />
            Legacy Bills
          </ToggleButton>
        )}
      </ToggleButtonGroup>
    </Box>
  );



  return (
    <Box sx={{ display: 'flex', flexDirection: 'column', height: '100%', width: '100%' }}>
      <Box sx={{ flex: 1 }}>
        <ElvanListView 
          key={activeTab}
          title={t('invoicesCount') || 'Invoices'}
          renderBelowSearch={filterChips}
          searchPlaceholder={t('searchBillsPlaceholder') || 'Search anything...'}
          addButtonText={t('newBill') || 'New Bill'}
          onAdd={() => onNew()}
          items={tabFilteredBills}
      isLoading={isLoading}
      filterFn={filterFn}
      renderCard={renderCard}
      emptyIcon={<FileText size={48} weight="regular" style={{ opacity: 0.5 }} />}
      emptyText={bills.length === 0 ? (t('noInvoicesYet') || 'No bills yet') : (t('noInvoicesMatch') || 'No bills match your search')}
      onDeleteSelected={handleBulkDelete}
      onDuplicateSelected={handleBulkDuplicate}
      deleteConfirmTitle={t('deleteProductsTitle') || 'Delete Bills?'}
      deleteConfirmMessage={() => t('deleteProductsMessage') || 'Are you sure you want to delete the selected bill(s)? This action cannot be undone.'}
      duplicateConfirmTitle={t('duplicateProductsTitle') || 'Duplicate Bills?'}
      duplicateConfirmMessage={() => t('duplicateProductsMessage') || 'Are you sure you want to create copies of the selected bill(s)?'}
    />
    </Box>

      <Dialog
        open={Boolean(invoiceToDelete)}
        onClose={() => setInvoiceToDelete(null)}
        slotProps={{ paper: { sx: { bgcolor: 'background.paper', backgroundImage: 'none' } } }}
      >
        <DialogTitle sx={{ pb: 1, color: 'error.main' }}>{t('delete') || 'Delete'}</DialogTitle>
        <DialogContent>
          <DialogContentText>{t('deleteInvoiceConfirmMsg') || 'Are you sure you want to delete this bill?'}</DialogContentText>
        </DialogContent>
        <DialogActions sx={{ px: 3, pb: 3, pt: 1 }}>
          <Button onClick={() => setInvoiceToDelete(null)} sx={{ color: 'text.secondary' }}>{t('hc_cancel') || 'Cancel'}</Button>
          <Button 
            variant="contained" 
            color="error"
            onClick={async () => {
              if (invoiceToDelete) {
                try {
                  await deleteCoolieBill(invoiceToDelete.id);
                  thagaval(t('deletedSuccessfully') || 'Deleted successfully', 'success');
                  loadData();
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
    </Box>
  );
}
