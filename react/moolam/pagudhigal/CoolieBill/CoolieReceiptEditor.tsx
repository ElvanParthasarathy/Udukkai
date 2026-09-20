import React, { useState, useEffect } from 'react';
import { 
  Box, Typography, Paper, TextField, InputAdornment, 
  Grid, MenuItem, Stack, Chip, Button, Dialog, DialogTitle, 
  DialogContent, DialogActions, Checkbox, IconButton, Divider, useTheme, useMediaQuery 
} from '@mui/material';
import ElvanPillAutocomplete from '../ElvanPillAutocomplete';
import { saveCoolieReceipt, getAllCoolieBills, getAllCoolieReceipts, getAllCoolieProfiles, getAllCoolieClients } from '../../Avanam';
import { createFilterOptions } from '@mui/material';
import { Plus, X, Receipt, MagnifyingGlass } from '@phosphor-icons/react';
import { formatCurrency, getCountryConfig, getDynamicField } from '../../Payanpadu';
import { thagaval } from '../Thagaval';
import { useLanguage } from '../../mozhi/LanguageContext';
import ElvanEditorLayout from '../ElvanEditorLayout';
import ElvanCard from '../ElvanCard';
import ElvanBilingualField from '../ElvanBilingualField';
import { useDraftAndUnsaved } from '../../hooks/useDraftAndUnsaved';
import { DatePicker } from '@mui/x-date-pickers/DatePicker';
import { LocalizationProvider } from '@mui/x-date-pickers/LocalizationProvider';
import { AdapterDayjs } from '@mui/x-date-pickers/AdapterDayjs';
import dayjs from 'dayjs';

const PAYMENT_MODES = ['Bank Transfer', 'UPI', 'Cash', 'Cheque', 'Card', 'Other'];

export default function CoolieReceiptEditor({ onBack, onSaved, editingReceipt }: { onBack: () => void, onSaved: () => void, editingReceipt?: any }) {
  const { t } = useLanguage();
  const theme = useTheme();
  const fullScreen = useMediaQuery(theme.breakpoints.down('sm'));
  const [coolieProfile, setCoolieProfile] = useState<any>(null);
  const [isLoadingProfile, setIsLoadingProfile] = useState(true);
  const activeProfile = coolieProfile || {};

  const profileCurrency = 'INR';
  const currencySymbol = '₹';

  const primaryLang = activeProfile?.primaryDataLanguage || 'Tamil';
  const secondaryLang = activeProfile?.secondaryDataLanguage || 'English';
  
  const [form, setForm] = useState({
    id: editingReceipt?.id || '',
    date: editingReceipt?.date || new Date().toISOString().split('T')[0],
    receiptNo: editingReceipt?.receiptNo || '',
    [`clientName_${primaryLang}`]: editingReceipt?.clientName || '',
    [`clientName_${secondaryLang}`]: editingReceipt?.clientNameEn || '',
    clientAddress: editingReceipt?.clientAddress || '',
    amount: editingReceipt?.amount?.toString() || '',
    paymentMode: editingReceipt?.paymentMode || '',
    referenceNo: editingReceipt?.referenceNo || '',
    againstInvoice: editingReceipt?.againstInvoice || '',
    note: editingReceipt?.note || '',
  });
  const [initialForm, setInitialForm] = useState<any>(editingReceipt ? { ...form } : { ...form });

  const [bills, setBills] = useState<any[]>([]);
  const unpaidBills = bills.filter(b => b.status !== 'paid');
  const [invoiceDialogOpen, setInvoiceDialogOpen] = useState(false);

  // Scroll masking state
  const [showLeftFade, setShowLeftFade] = useState(false);
  const [showRightFade, setShowRightFade] = useState(true);

  const handleChipScroll = (e: React.UIEvent<HTMLElement>) => {
    const target = e.currentTarget;
    const isAtStart = target.scrollLeft <= 0;
    const isAtEnd = target.scrollWidth - target.clientWidth <= target.scrollLeft + 1;
    
    setShowLeftFade(!isAtStart);
    setShowRightFade(!isAtEnd);
  };

  const calculateMask = () => {
    if (showLeftFade && showRightFade) {
      return 'linear-gradient(to right, transparent, black 8px, black calc(100% - 8px), transparent)';
    } else if (showLeftFade) {
      return 'linear-gradient(to right, transparent, black 8px, black 100%)';
    } else if (showRightFade) {
      return 'linear-gradient(to left, transparent, black 8px, black 100%)';
    }
    return 'none';
  };

  const [dialogSearch, setDialogSearch] = useState('');
  
  const [allProfiles, setAllProfiles] = useState<any[]>([]);
  const [allReceipts, setAllReceipts] = useState<any[]>([]);
  const [clientOptions, setClientOptions] = useState<any[]>([]);
  const clientFilter = createFilterOptions({
    stringify: (option: any) => {
      if (typeof option === 'string') return option;
      return `${option.name || ''} ${option.nameEn || ''} ${option.city || ''} ${option.cityEn || ''}`;
    }
  });

  const generateReceiptNo = (profileToUse: any, allRecs: any[]) => {
    let bizInitialsStr = profileToUse?.shortBusinessName?.trim()?.toUpperCase() || '';
    let bizInitials = bizInitialsStr;
    
    if (!bizInitials) {
      const bizName = profileToUse?.name || getDynamicField(profileToUse, 'niruvanathinPeyar', profileToUse, false) || getDynamicField(profileToUse, 'niruvanathinPeyar', profileToUse, true) || 'BIZ';
      bizInitials = bizName.split(' ').filter((w: string) => w.trim().length > 0).map((w: string) => w[0]).join('').toUpperCase().slice(0, 4) || 'BIZ';
    }

    const pfx = 'RCP';
    const sep = '/';
    const prefixStr = `${pfx}${sep}${bizInitials}${sep}`;
    
    let maxFound = 0;
    const bizUpper = bizInitials.toUpperCase();
    const relevantRecs = allRecs.filter(r => 
      r.company_id === profileToUse?.id || 
      (r.receiptNo || '').toUpperCase().includes(`/${bizUpper}/`) ||
      (r.receiptNo || '').toUpperCase().includes(`${bizUpper}`)
    );
    
    for (const rcp of relevantRecs) {
      if (rcp.receiptNo) {
        const match = rcp.receiptNo.match(/\d+$/);
        if (match) {
          const numPart = parseInt(match[0], 10);
          if (!isNaN(numPart) && numPart > maxFound) {
            maxFound = numPart;
          }
        }
      }
    }

    const next = maxFound + 1;
    const padded = String(next).padStart(2, '0');
    return `${prefixStr}${padded}`;
  };

  useEffect(() => {
    const init = async () => {
      try {
        const [recs, bls, coolieProfiles, clients] = await Promise.all([
          getAllCoolieReceipts(),
          getAllCoolieBills(),
          getAllCoolieProfiles(),
          getAllCoolieClients()
        ]);
        setBills(bls || []);
        setAllProfiles(coolieProfiles || []);
        setAllReceipts(recs || []);
        setClientOptions(clients || []);
        
        if (!editingReceipt) {
          const coolieProf = coolieProfiles && coolieProfiles.length > 0 ? coolieProfiles[0] : null;
          if (coolieProf) setCoolieProfile(coolieProf);
          
          setForm(prev => {
            const newForm = { ...prev, receiptNo: '' };
            return newForm;
          });
          setInitialForm((prev: any) => ({ ...prev, receiptNo: '' }));
        }
      } catch (err) {
        console.error(err);
      } finally {
        setIsLoadingProfile(false);
      }
    };
    init();
  }, []);

  const updateField = (field: string, value: any) => setForm(prev => ({ ...prev, [field]: value }));

  const selectInvoices = (selectedItems: any[]) => {
    let nextNo = form.receiptNo;
    
    if (selectedItems.length === 0) {
      setForm(prev => ({
        ...prev,
        againstInvoice: '',
        [`clientName_${primaryLang}`]: '',
        [`clientName_${secondaryLang}`]: '',
        clientAddress: '',
        amount: '',
        receiptNo: ''
      }));
      return;
    }

    const invoicesStr = selectedItems.map(b => typeof b === 'string' ? b : b.bill_no).join(', ');
    const actualBills = selectedItems.filter(b => typeof b !== 'string');

    if (actualBills.length === 0) {
      setForm(prev => ({ ...prev, againstInvoice: invoicesStr, receiptNo: '' }));
      return;
    }

    const firstBill = actualBills[0];
    if (!editingReceipt) {
      const selectedProfile = allProfiles.find(p => p.id === firstBill.company_id) || allProfiles[0];
      nextNo = generateReceiptNo(selectedProfile, allReceipts);
    }

    const totalAmount = actualBills.reduce((sum, bill) => {
      return sum + (bill.status === 'paid' ? (bill.paidAmount || bill.grand_total) : Math.max(0, (bill.grand_total || 0) - (bill.paidAmount || 0)));
    }, 0);

    setForm(prev => ({
      ...prev,
      receiptNo: nextNo,
      [`clientName_${primaryLang}`]: firstBill.customer_name || '',
      [`clientName_${secondaryLang}`]: firstBill.customer_name_en || '',
      clientAddress: [firstBill.address || firstBill.address_en, firstBill.city || firstBill.city_en].filter(Boolean).join(', ') || '',
      amount: String(totalAmount),
      againstInvoice: invoicesStr,
    }));
  };

  const handleCustomerSelect = (e: any, val: any) => {
    if (val && val.isAddButton) {
      window.location.href = window.location.pathname + '?view=client-editor';
      return;
    }
    if (!val) {
      setForm(prev => ({ ...prev, [`clientName_${primaryLang}`]: '', [`clientName_${secondaryLang}`]: '', clientAddress: '' }));
      return;
    }
    if (typeof val === 'string') {
      setForm(prev => ({ ...prev, [`clientName_${primaryLang}`]: val, [`clientName_${secondaryLang}`]: '' }));
      return;
    }
    setForm(prev => ({
      ...prev,
      [`clientName_${primaryLang}`]: val.name || '',
      [`clientName_${secondaryLang}`]: val.nameEn || '',
      clientAddress: [val.address || val.addressEn, val.city || val.cityEn].filter(Boolean).join(', ')
    }));
  };

  const getIsBlank = (f: any) => {
    return !f[`clientName_${primaryLang}`] && !f[`clientName_${secondaryLang}`] && !f.clientAddress && !f.amount && !f.note && !f.paymentMode && !f.againstInvoice;
  };

  const { hasUnsavedChanges, clearDraft } = useDraftAndUnsaved(
    'niril_draft_receipt',
    initialForm,
    form,
    setForm,
    !!editingReceipt,
    getIsBlank
  );

  const handleSave = async () => {
    if (!form[`clientName_${primaryLang}`]?.trim()) { thagaval(t('clientNameRequiredToast') || 'Client name is required', 'warning'); return; }
    if (!form.amount || parseFloat(form.amount) <= 0) { thagaval(t('enterValidAmountToast') || 'Enter valid amount', 'warning'); return; }
    if (!form.paymentMode) { thagaval(t('paymentModeLabel') + ' is required', 'warning'); return; }
    try {
      const receipt = {
        ...form,
        clientName: form[`clientName_${primaryLang}`] || '',
        clientNameEn: form[`clientName_${secondaryLang}`] || '',
        referenceNo: form.paymentMode === 'Cash' ? '' : form.referenceNo,
        amount: parseFloat(form.amount),
      };
      delete receipt[`clientName_${primaryLang}`];
      delete receipt[`clientName_${secondaryLang}`];
      
      await saveCoolieReceipt(receipt);
      clearDraft();
      thagaval(t('receiptSavedToast') || 'Receipt Saved!', 'success');
      onSaved();
    } catch {
      thagaval('Failed to save', 'error');
    }
  };

  const renderPaymentModeOption = (mode: string, inDropdown = true) => {
    if (mode === 'UPI' || mode === 'Card') return mode;

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
    
    const primaryLang = activeProfile?.primaryDataLanguage || 'Tamil';
    const secondaryLang = activeProfile?.secondaryDataLanguage || 'English';
    const enableBilingual = activeProfile?.enableBilingual !== false;
    
    const primaryVal = (dictionaries[primaryLang] || {})[mode] || mode;
    const secondaryVal = (dictionaries[secondaryLang] || {})[mode] || mode;
    
    if (inDropdown && enableBilingual && (mode !== 'UPI')) {
      return (
        <Box>
          <Typography variant="body1" sx={{ lineHeight: 1.2 }}>{primaryVal}</Typography>
          <Typography variant="body2" sx={{ display: 'block', lineHeight: 1.2, mt: 0.2, opacity: 0.6, fontSize: '0.75rem' }}>{secondaryVal}</Typography>
        </Box>
      );
    }
    
    return primaryVal;
  };

  if (isLoadingProfile) {
    return <Box sx={{ minHeight: '100vh', display: 'flex', justifyContent: 'center', alignItems: 'center' }}></Box>;
  }

  return (
    <ElvanEditorLayout
      title={(editingReceipt ? (t('editReceiptTitle') || 'Edit Receipt') : t('newPaymentReceiptTitle')) as string}
      onBack={onBack}
      onSave={handleSave}
      saveButtonText={(t('saveReceiptBtn') || 'Save Receipt') as string}
      hasUnsavedChanges={hasUnsavedChanges}
      onDiscard={() => {
        clearDraft();
        onBack();
      }}
    >


      <Grid container spacing={3}>
        <Grid size={{ xs: 12 }}>
          <Box sx={{ display: 'flex', alignItems: 'center', mb: 0, ml: 1 }}>
            <Box sx={{ width: 24, height: 24, borderRadius: '50%', bgcolor: 'primary.main', color: 'primary.contrastText', display: 'flex', alignItems: 'center', justifyContent: 'center', fontSize: '0.8rem', fontWeight: 'bold', lineHeight: 1, pt: '1px', mr: 1.5 }}>1</Box>
            <Typography variant="h6" sx={{ fontSize: '1.1rem', fontWeight: 600 }}>{t('invoiceData') || 'பட்டியல் தரவுகள்'}</Typography>
          </Box>
        </Grid>
        <Grid size={{ xs: 12, sm: 6 }}>
          <Typography variant="caption" color="text.secondary" sx={{ display: 'block', ml: 1.5, mb: 0.5 }}>
            {t('againstInvoiceLabel') || 'எந்தப் பட்டியலுக்கு'}
          </Typography>
          <Button
            variant="contained"
            disableElevation
            fullWidth
            onClick={() => { setDialogSearch(''); setInvoiceDialogOpen(true); }}
            sx={{
              justifyContent: 'flex-start',
              textTransform: 'none',
              py: 1.5,
              px: 2,
              bgcolor: 'action.hover', // Darker filled look matching the subtle inputs below it
              color: 'text.primary',
              borderRadius: 50,
              '@media (hover: hover)': { '&:hover': { bgcolor: 'action.selected' } }
            }}
            startIcon={<Receipt size={20} />}
          >
            {form.againstInvoice
              ? `${form.againstInvoice.split(', ').filter(Boolean).length} ${t('invoicesSelected') || 'பட்டியல்கள்'}`
              : (t('selectInvoices') || 'பட்டியலைத் தேர்ந்தெடு')}
          </Button>

          {/* Selected Invoices - Chips */}
          <Box sx={{ minHeight: 52 }}>
            {form.againstInvoice && form.againstInvoice.split(', ').filter(Boolean).length > 0 && (
              <Box 
              onScroll={handleChipScroll}
              sx={{ 
                mt: 1.5, 
                display: 'flex', 
                gap: 1, 
                overflowX: 'auto',
                pb: 1, // slightly more padding for the scrollbar
                scrollbarWidth: 'none', // Hide by default (Firefox mobile)
                '&::-webkit-scrollbar': { display: 'none' }, // Hide by default (Webkit mobile)
                '@media (pointer: fine)': {
                  scrollbarWidth: 'thin',
                  scrollbarColor: 'transparent transparent', // Invisible by default
                  '@media (hover: hover)': { '&:hover': {
                    scrollbarColor: 'rgba(128,128,128,0.3) transparent', // Show on hover for Firefox
                  } },
                  '&::-webkit-scrollbar': {
                    display: 'block',
                    height: 4, // Very thin
                  },
                  '&::-webkit-scrollbar-track': {
                    background: 'transparent',
                  },
                  '&::-webkit-scrollbar-thumb': {
                    background: 'transparent', // Invisible by default
                    borderRadius: 4,
                  },
                  '&:hover::-webkit-scrollbar-thumb': {
                    background: 'rgba(128,128,128,0.3)', // Show when mouse enters container
                  },
                  '&::-webkit-scrollbar-thumb:hover': {
                    background: 'rgba(128,128,128,0.5)', // Darker when hovering the scrollbar itself
                  }
                },
                WebkitMaskImage: calculateMask(),
                maskImage: calculateMask(),
                transition: 'mask-image 0.3s ease-in-out, -webkit-mask-image 0.3s ease-in-out'
              }}
            >
              {form.againstInvoice.split(', ').filter(Boolean).map(invNo => {
                return (
                  <Chip
                    key={invNo}
                    label={invNo}
                    onDelete={() => {
                      const updatedArr = form.againstInvoice.split(', ').filter(Boolean).filter(v => v !== invNo);
                      const newItems = updatedArr.map(n => bills.find(b => b.bill_no === n) || n);
                      selectInvoices(newItems);
                    }}
                    color="default"
                    sx={{ fontWeight: 600 }}
                  />
                );
              })}
            </Box>
          )}
          </Box>

          {/* Invoice Picker Dialog */}
          <Dialog
            open={invoiceDialogOpen}
            onClose={() => setInvoiceDialogOpen(false)}
            fullWidth
            fullScreen={fullScreen}
            maxWidth="sm"
            slotProps={{ paper: { sx: { borderRadius: fullScreen ? 0 : 3, maxHeight: fullScreen ? '100dvh' : '80vh' } } }}
          >
            <DialogTitle sx={{ pb: 1, display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
              <Typography variant="h6" sx={{ fontWeight: 600 }}>{t('selectInvoices') || 'Select Invoices'}</Typography>
              <IconButton 
                onClick={() => setInvoiceDialogOpen(false)} 
                sx={{ bgcolor: 'action.hover', width: 40, height: 40 }}
              >
                <X size={20} />
              </IconButton>
            </DialogTitle>
            <Box sx={{ px: 3, pb: 1.5 }}>
              <TextField
                fullWidth
                size="small"
                placeholder={t('searchInvoices') || 'Search by invoice no. or customer...'}
                value={dialogSearch}
                onChange={e => setDialogSearch(e.target.value)}
                InputProps={{ startAdornment: <InputAdornment position="start"><MagnifyingGlass size={18} /></InputAdornment> }}
                autoComplete="off"
              />
            </Box>
            <Divider />
            <DialogContent sx={{ p: 0 }}>
              {(() => {
                const selectedBillNos = form.againstInvoice ? form.againstInvoice.split(', ').filter(Boolean) : [];
                const selectedBills = selectedBillNos.map(invNo => bills.find(b => b.bill_no === invNo)).filter(Boolean);
                const firstSelected = selectedBills.length > 0 ? selectedBills[0] : null;

                const availableBills = firstSelected
                  ? unpaidBills.filter(b =>
                      b.company_id === firstSelected.company_id &&
                      b.customer_name === firstSelected.customer_name &&
                      (b.city || '') === (firstSelected.city || '') &&
                      (b.address || '') === (firstSelected.address || '')
                    )
                  : unpaidBills;

                const searchVal = dialogSearch.toLowerCase().replace(/[^a-z0-9]/g, '');
                const filtered = searchVal
                  ? availableBills.filter(b => {
                      const invNo = (b.bill_no || '').toLowerCase().replace(/[^a-z0-9]/g, '');
                      const pName = (b.customer_name || '').toLowerCase().replace(/[^a-z0-9]/g, '');
                      const sName = (b.customer_name_en || '').toLowerCase().replace(/[^a-z0-9]/g, '');
                      return invNo.includes(searchVal) || pName.includes(searchVal) || sName.includes(searchVal);
                    })
                  : availableBills;

                if (filtered.length === 0) {
                  return (
                    <Box sx={{ py: 6, textAlign: 'center' }}>
                      <Typography color="text.secondary">{t('noInvoicesFound') || 'No unpaid invoices found'}</Typography>
                    </Box>
                  );
                }

                return filtered.map((bill, idx) => {
                  const isSelected = selectedBillNos.includes(bill.bill_no);
                  const pendingAmt = bill.status === 'paid' ? (bill.paidAmount || bill.grand_total) : Math.max(0, (bill.grand_total || 0) - (bill.paidAmount || 0));
                  return (
                    <Box
                      key={bill.id || bill.bill_no}
                      onClick={() => {
                        let newItems;
                        if (isSelected) {
                          const updatedArr = selectedBillNos.filter(v => v !== bill.bill_no);
                          newItems = updatedArr.map(n => bills.find(b => b.bill_no === n) || n);
                        } else {
                          newItems = [...selectedBills, bill];
                        }
                        selectInvoices(newItems);
                      }}
                      sx={{
                        display: 'flex',
                        alignItems: 'center',
                        gap: 1,
                        px: 2,
                        py: 1.25,
                        cursor: 'pointer',
                        borderBottom: idx < filtered.length - 1 ? '1px solid' : 'none',
                        borderColor: 'divider',
                        bgcolor: isSelected ? 'action.selected' : 'transparent',
                        '@media (hover: hover)': { '&:hover': { bgcolor: isSelected ? 'action.selected' : 'action.hover' } },
                        transition: 'background-color 0.15s'
                      }}
                    >
                      <Checkbox checked={isSelected} size="small" sx={{ p: 0.5 }} />
                      <Box sx={{ flex: 1, minWidth: 0 }}>
                        <Box sx={{ display: 'flex', flexDirection: 'column', gap: 0.25 }}>
                          <Typography variant="body2" sx={{ fontWeight: 600 }}>
                            {bill.bill_no} <Box component="span" sx={{ opacity: 0.6, fontWeight: 400, ml: 1, fontSize: '0.75rem' }}>{bill.date || ''}</Box>
                          </Typography>
                          {(bill.customer_name || bill.customer_name_en) && (
                            <Typography variant="caption" color="text.primary" sx={{ opacity: 0.9 }} noWrap>
                              {bill.customer_name || bill.customer_name_en}
                            </Typography>
                          )}
                          {(bill.city || bill.city_en) && (
                            <Typography variant="caption" color="text.secondary" sx={{ fontSize: '0.7rem' }} noWrap>
                              {bill.city || bill.city_en}
                            </Typography>
                          )}
                        </Box>
                      </Box>
                      <Typography variant="body2" sx={{ fontWeight: 500, color: 'primary.main', whiteSpace: 'nowrap' }}>
                        {formatCurrency(pendingAmt, profileCurrency)}
                      </Typography>
                    </Box>
                  );
                });
              })()}
            </DialogContent>
            <Divider />
            <DialogActions sx={{ px: 3, pt: 2.5, pb: 2.5 }}>
              <Button 
                onClick={() => setInvoiceDialogOpen(false)} 
                variant="contained" 
                size="small" 
                sx={{ borderRadius: 50, textTransform: 'none', minHeight: '36px !important', px: 3 }}
              >
                {t('done') || 'Done'}
              </Button>
            </DialogActions>
          </Dialog>
        </Grid>
        <Grid size={{ xs: 12, sm: 6 }} sx={{ display: { xs: 'none', sm: 'block' } }} />
        <Grid size={{ xs: 12 }}>
          <Box sx={{ display: 'flex', alignItems: 'center', mb: 0, ml: 1, mt: 1 }}>
            <Box sx={{ width: 24, height: 24, borderRadius: '50%', bgcolor: 'primary.main', color: 'primary.contrastText', display: 'flex', alignItems: 'center', justifyContent: 'center', fontSize: '0.8rem', fontWeight: 'bold', lineHeight: 1, pt: '1px', mr: 1.5 }}>2</Box>
            <Typography variant="h6" sx={{ fontSize: '1.1rem', fontWeight: 600 }}>{t('receiptData') || 'பற்றுச்சீட்டு தரவுகள்'}</Typography>
          </Box>
        </Grid>
        <Grid size={{ xs: 12, sm: 6 }}>
          <LocalizationProvider dateAdapter={AdapterDayjs}>
            <DatePicker
              label={t('dateLabel') as string}
              value={form.date ? dayjs(form.date) : null}
              onChange={(newValue) => updateField('date', newValue ? newValue.format('YYYY-MM-DD') : '')}
              slotProps={{ textField: { fullWidth: true, size: 'medium' } }}
              format="DD/MM/YYYY"
            />
          </LocalizationProvider>
        </Grid>
        <Grid size={{ xs: 12, sm: 6 }}>
          <ElvanPillAutocomplete
            freeSolo
            options={clientOptions}
            filterOptions={clientFilter}
            getOptionLabel={(option) => {
              return typeof option === 'string' ? option : (option.name || option.nameEn || '');
            }}
            onChange={handleCustomerSelect}
            value={form[`clientName_${primaryLang}`] || null}
            inputValue={form[`clientName_${primaryLang}`] || ''}
            onInputChange={(e, val, reason) => {
              updateField(`clientName_${primaryLang}`, val || '');
              if (reason === 'input' || reason === 'clear') {
                updateField(`clientName_${secondaryLang}`, '');
                updateField('clientAddress', '');
              }
            }}
            renderOption={(props, option, { index }) => {
              const { key, ...optionProps } = props as any;
                            if (typeof option === 'string') {
                return <li key={`client-opt-${index}`} {...optionProps}><Typography variant="body1">{option}</Typography></li>;
              }
              return (
                <li key={`client-opt-${index}`} {...optionProps} style={{ display: 'flex', flexDirection: 'column', alignItems: 'flex-start', borderBottom: '1px solid rgba(128,128,128,0.1)' }}>
                  <Box sx={{ display: 'flex', alignItems: 'baseline', gap: 1 }}>
                    <Typography variant="body1" color="primary" sx={{ fontWeight: 500 }}>{option.name}</Typography>
                    {option.nameEn && <Typography variant="body2" sx={{ color: 'text.secondary' }}>{option.nameEn}</Typography>}
                  </Box>
                  {(option.city || option.cityEn) && (
                    <Box sx={{ display: 'flex', alignItems: 'baseline', gap: 0.5, mt: 0.5 }}>
                      {option.city && <Typography variant="caption" sx={{ color: 'text.secondary' }}>{option.city}</Typography>}
                      {option.cityEn && <Typography variant="caption" sx={{ color: 'text.secondary' }}>({option.cityEn})</Typography>}
                    </Box>
                  )}
                </li>
              );
            }}
            label={t('clientName') as string}
          />


        </Grid>
        <Grid size={{ xs: 12, sm: 6 }}>
          <TextField 
            fullWidth size="medium" label={t('receiptNoLabel') as string} 
            value={form.receiptNo} onChange={e => updateField('receiptNo', e.target.value)} 
            slotProps={{ inputLabel: { shrink: true } }}
          />
        </Grid>
        <Grid size={{ xs: 12 }}>
          <Box sx={{ display: 'flex', alignItems: 'center', mb: 0, ml: 1, mt: 1 }}>
            <Box sx={{ width: 24, height: 24, borderRadius: '50%', bgcolor: 'primary.main', color: 'primary.contrastText', display: 'flex', alignItems: 'center', justifyContent: 'center', fontSize: '0.8rem', fontWeight: 'bold', lineHeight: 1, pt: '1px', mr: 1.5 }}>3</Box>
            <Typography variant="h6" sx={{ fontSize: '1.1rem', fontWeight: 600 }}>{t('paymentDetails') || 'Payment Details'}</Typography>
          </Box>
        </Grid>
        <Grid size={{ xs: 12, sm: 6 }}>
          <TextField 
            fullWidth size="medium" label={t('amountLabelStar') as string} type="number"
            value={form.amount} onChange={e => updateField('amount', e.target.value)}
            slotProps={{
              input: { startAdornment: <InputAdornment position="start" sx={{ display: 'flex', alignItems: 'center', justifyContent: 'center', minWidth: 24, mt: '0 !important' }}>{currencySymbol}</InputAdornment> },
              inputLabel: { shrink: true }
            }}
          />
        </Grid>
        <Grid size={{ xs: 12, sm: 6 }}>
          <TextField 
            select
            fullWidth size="medium" 
            label={t('paymentModeLabel') as string} 
            value={form.paymentMode} 
            onChange={e => updateField('paymentMode', e.target.value)} 
            slotProps={{ 
              inputLabel: { shrink: true }, 
              select: { 
                displayEmpty: true,
                renderValue: (selected) => selected ? renderPaymentModeOption(selected as string, false) : <em>{t('paymentModeLabel')}...</em>
              } 
            }}
          >
            <MenuItem value=""><em>{t('paymentModeLabel')}...</em></MenuItem>
            {PAYMENT_MODES.map(m => <MenuItem key={m} value={m}>{renderPaymentModeOption(m)}</MenuItem>)}
          </TextField>
        </Grid>


      </Grid>
    </ElvanEditorLayout>
  );
}
