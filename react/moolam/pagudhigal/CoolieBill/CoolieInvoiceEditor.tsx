import React, { useState, useEffect } from 'react';
import { Box, Typography, TextField, Button, Stack, IconButton, Divider, Select, MenuItem, FormControl, InputLabel, useTheme, Switch, FormControlLabel, createFilterOptions, Paper, ListItemButton, ListItemText, Autocomplete } from '@mui/material';
import ElvanPillAutocomplete from '../ElvanPillAutocomplete';
import { styled } from '@mui/material/styles';
import { DesktopDatePicker } from '@mui/x-date-pickers/DesktopDatePicker';
import { LocalizationProvider } from '@mui/x-date-pickers/LocalizationProvider';
import { AdapterDayjs } from '@mui/x-date-pickers/AdapterDayjs';
import dayjs from 'dayjs';
import customParseFormat from 'dayjs/plugin/customParseFormat';

dayjs.extend(customParseFormat);

const parseDateSafe = (dateStr) => {
  if (!dateStr) return null;
  const formats = ['DD/MM/YYYY', 'DD/MM/YY', 'YYYY-MM-DD'];
  for (const fmt of formats) {
    const d = dayjs(dateStr, fmt, true);
    if (d.isValid()) return d;
  }
  const fallback = dayjs(dateStr);
  return fallback.isValid() ? fallback : null;
};

const MD3Switch = styled(Switch)(({ theme }) => ({
  width: 52,
  height: 32,
  padding: 0,
  '& .MuiSwitch-switchBase': {
    padding: 0,
    margin: 4,
    transitionDuration: '300ms',
    '&.Mui-checked': {
      transform: 'translateX(20px)',
      color: theme.palette.primary.contrastText,
      '& + .MuiSwitch-track': {
        backgroundColor: theme.palette.primary.main,
        opacity: 1,
        border: 0,
      },
      '& .MuiSwitch-thumb': {
        width: 24,
        height: 24,
        margin: 0,
        backgroundColor: theme.palette.primary.contrastText,
      },
    },
  },
  '& .MuiSwitch-thumb': {
    boxSizing: 'border-box',
    width: 16,
    height: 16,
    margin: 4,
    boxShadow: 'none',
    backgroundColor: theme.palette.mode === 'dark' ? '#cfd8dc' : '#fff',
    transition: theme.transitions.create(['width', 'height', 'margin'], {
      duration: 200,
    }),
  },
  '& .MuiSwitch-track': {
    borderRadius: 32 / 2,
    backgroundColor: theme.palette.mode === 'dark' ? '#39393D' : '#E9E9EA',
    opacity: 1,
    transition: theme.transitions.create(['background-color', 'border'], {
      duration: 300,
    }),
    border: `2px solid ${theme.palette.mode === 'dark' ? '#8f9bb3' : '#8f9bb3'}`,
    boxSizing: 'border-box',
  },
  '& .MuiSwitch-switchBase.Mui-checked + .MuiSwitch-track': {
    border: '2px solid transparent',
  }
}));
import { X, Plus, FloppyDisk, CalendarBlank, Trash } from '@phosphor-icons/react';
import { getAllCoolieClients, getAllCoolieProducts, getAllCoolieProfiles, saveCoolieBill, getNextCoolieBillNumber } from '../../Avanam';
import { thagaval } from '../Thagaval';
import { useLanguage } from '../../mozhi/LanguageContext';
import ElvanCard from '../ElvanCard';
import ElvanEditorLayout from '../ElvanEditorLayout';
import { useDraftAndUnsaved } from '../../hooks/useDraftAndUnsaved';

const filter = createFilterOptions({
  stringify: (option: any) => {
    if (typeof option === 'string') return option;
    return `${option.name || ''} ${option.nameEn || ''}`;
  }
});

const clientFilter = createFilterOptions({
  stringify: (option: any) => {
    if (typeof option === 'string') return option;
    return `${option.name || ''} ${option.nameEn || ''} ${option.city || ''} ${option.cityEn || ''}`;
  }
});

export default function CoolieInvoiceEditor({ onBack, onSaved, existingBill, onRequestAddClient, onRequestAddProduct, dataVersion, hideHeaderPortals }) {
  const { t, language } = useLanguage();
  const theme = useTheme();
  const skipNextInputChangeRef = React.useRef(false);
  
  // State from Kananam Coolie Bill
  const [billNo, setBillNo] = useState('');
  const [date, setDate] = useState(new Date().toLocaleDateString('en-GB')); // DD/MM/YYYY
  const [companyId, setCompanyId] = useState('');
  const [customerName, setCustomerName] = useState('');
  const [customerNameEn, setCustomerNameEn] = useState('');
  const [address, setAddress] = useState('');
  const [addressEn, setAddressEn] = useState('');
  const [city, setCity] = useState('');
  const [cityEn, setCityEn] = useState('');
  const [items, setItems] = useState<any[]>([{ porul: '', kg: '', coolie: '', name_tamil: '', name_english: '' }]);
  const [setharamGrams, setSetharamGrams] = useState('');
  const [courierRs, setCourierRs] = useState('');
  const [ahimsaSilkRs, setAhimsaSilkRs] = useState('');
  const [otherCharges, setOtherCharges] = useState<{ name: string, amount: string }[]>([]);
  const [bankDetails, setBankDetails] = useState('');
  const [accountNo, setAccountNo] = useState('');
  const [ifsc, setIfsc] = useState('');
  const [showBankDetails, setShowBankDetails] = useState(true);
  const [showIfsc, setShowIfsc] = useState(true);
  
  // Options
  const [clientOptions, setClientOptions] = useState<any[]>([]);
  const [productOptions, setProductOptions] = useState<any[]>([]);
  const [profileOptions, setProfileOptions] = useState<any[]>([]);
  const [isLoading, setIsLoading] = useState(true);

  const formState = {
    billNo, date, companyId, customerName, customerNameEn,
    address, addressEn, city, cityEn, items, setharamGrams, courierRs, ahimsaSilkRs, otherCharges,
    bankDetails, accountNo, ifsc, showBankDetails, showIfsc
  };

  const setFormState = (s) => {
    if (s.billNo !== undefined) setBillNo(s.billNo);
    if (s.date !== undefined) setDate(s.date);
    if (s.companyId !== undefined) setCompanyId(s.companyId);
    if (s.customerName !== undefined) setCustomerName(s.customerName);
    if (s.customerNameEn !== undefined) setCustomerNameEn(s.customerNameEn);
    if (s.address !== undefined) setAddress(s.address);
    if (s.addressEn !== undefined) setAddressEn(s.addressEn);
    if (s.city !== undefined) setCity(s.city);
    if (s.cityEn !== undefined) setCityEn(s.cityEn);
    if (s.items !== undefined) setItems(s.items);
    if (s.setharamGrams !== undefined) setSetharamGrams(s.setharamGrams);
    if (s.courierRs !== undefined) setCourierRs(s.courierRs);
    if (s.ahimsaSilkRs !== undefined) setAhimsaSilkRs(s.ahimsaSilkRs);
    if (s.otherCharges !== undefined) setOtherCharges(s.otherCharges);
    // Backward compatibility for old local drafts
    if (s.customChargeName !== undefined && s.otherCharges === undefined) { 
      setOtherCharges([{ name: s.customChargeName, amount: s.customChargeRs || '' }]); 
    }
    if (s.bankDetails !== undefined) setBankDetails(s.bankDetails);
    if (s.accountNo !== undefined) setAccountNo(s.accountNo);
    if (s.ifsc !== undefined) setIfsc(s.ifsc);
    if (s.showBankDetails !== undefined) setShowBankDetails(s.showBankDetails);
    if (s.showIfsc !== undefined) setShowIfsc(s.showIfsc);
  };

  const getIsBlank = (f) => {
    return !f.customerName && f.items.length === 1 && !f.items[0].porul;
  };

  const [initialForm] = useState({ ...formState });
  
  const isEditMode = !!existingBill;
  const { hasUnsavedChanges, clearDraft } = useDraftAndUnsaved(
    'niril_draft_coolie_bill',
    initialForm,
    formState,
    setFormState,
    isEditMode,
    getIsBlank
  );

  useEffect(() => {
    async function loadData() {
      setIsLoading(true);
      try {
        const [clients, products, profiles] = await Promise.all([
          getAllCoolieClients(),
          getAllCoolieProducts(),
          getAllCoolieProfiles()
        ]);
        setClientOptions(clients || []);
        setProductOptions(products || []);
        setProfileOptions(profiles || []);
        
        if (existingBill) {
          const compProfile = (profiles || []).find(p => p.id === existingBill.company_id);
          let parsedOtherCharges = [];
          if (existingBill.custom_charge_name) {
            try {
              if (existingBill.custom_charge_name.startsWith('[')) {
                parsedOtherCharges = JSON.parse(existingBill.custom_charge_name);
              } else {
                parsedOtherCharges = [{ name: existingBill.custom_charge_name, amount: existingBill.custom_charge_rs || '' }];
              }
            } catch (e) {
              parsedOtherCharges = [{ name: existingBill.custom_charge_name, amount: existingBill.custom_charge_rs || '' }];
            }
          }

          setFormState({
            billNo: existingBill.bill_no,
            date: existingBill.date || new Date().toLocaleDateString('en-GB'),
            companyId: existingBill.company_id || (profiles && profiles.length > 0 ? profiles[0].id : ''),
            customerName: existingBill.customer_name || '',
            customerNameEn: existingBill.customer_name_en || '',
            address: existingBill.address || '',
            addressEn: existingBill.address_en || '',
            city: existingBill.city || '',
            cityEn: existingBill.city_en || '',
            items: Array.isArray(existingBill.items) && existingBill.items.length > 0 ? existingBill.items : [{ porul: '', coolie: '', kg: '', name_english: '', name_tamil: '' }],
            setharamGrams: existingBill.setharam_grams || '',
            courierRs: existingBill.courier_rs || '',
            ahimsaSilkRs: existingBill.ahimsa_silk_rs || '',
            otherCharges: parsedOtherCharges,
            bankDetails: existingBill.bank_details || '',
            accountNo: existingBill.account_no || '',
            ifsc: existingBill.ifsc || compProfile?.ifsc || '',
            showBankDetails: existingBill.show_bank_details !== false,
            showIfsc: existingBill.show_ifsc !== false
          });
        } else if (profiles && profiles.length > 0) {
          // Do not auto-select companyId by default. Wait for user selection.
        }
      } catch (e) {
        console.error(e);
      } finally {
        setIsLoading(false);
      }
    }
    loadData();
  }, []);

  // Refresh dropdown options when returning from inline editor overlay
  useEffect(() => {
    if (dataVersion > 0) {
      Promise.all([getAllCoolieClients(), getAllCoolieProducts()]).then(([clients, products]) => {
        setClientOptions(clients || []);
        setProductOptions(products || []);
      });
    }
  }, [dataVersion]);

  // Fix legacy dates automatically (e.g. 27/05/26 -> 27/05/2026)
  useEffect(() => {
    if (date) {
      const parsed = parseDateSafe(date);
      if (parsed && parsed.isValid() && date !== parsed.format('DD/MM/YYYY')) {
        setDate(parsed.format('DD/MM/YYYY'));
      }
    }
  }, [date]);

  useEffect(() => {
    if (!isEditMode) {
      if (companyId) {
        if (!billNo) {
          getNextCoolieBillNumber(companyId).then(nextNum => {
            setBillNo(nextNum);
          }).catch(() => {});
        }
      } else {
        setBillNo('');
        setBankDetails('');
        setAccountNo('');
        setIfsc('');
      }
    }
  }, [companyId, isEditMode]);

  const handleCompanyChange = (e) => {
    const pId = e.target.value;
    setCompanyId(pId);
    const profile = profileOptions.find(p => p.id === pId);
    if (profile) {
      const bankName = profile.bankNameTamil || profile.bankName || '';
      const branch = profile.branchTamil || profile.branch || '';
      setBankDetails([bankName, branch].filter(Boolean).join(', '));
      setAccountNo(profile.accountNo || '');
      setIfsc(profile.ifsc || '');
    } else {
      setBankDetails('');
      setAccountNo('');
      setIfsc('');
    }
    
    if (!isEditMode) {
      if (pId) {
        getNextCoolieBillNumber(pId).then(nextNum => setBillNo(nextNum));
      } else {
        setBillNo('');
      }
    }
  };

  // Calculations (Exact Logic Ported from Kananam)
  const calculateRowTotal = (item) => {
    if (!item) return '0';
    const qty = parseFloat(item.kg) || 0;
    const rate = parseFloat(item.coolie) || 0;
    return Math.floor(qty * rate);
  };

  const calculateSubtotal = () => {
    return items.reduce((sum, item) => sum + (parseFloat(calculateRowTotal(item).toString()) || 0), 0);
  };

  const calculateTotalKg = () => {
    const itemKg = items.reduce((sum, item) => sum + (parseFloat(item.kg) || 0), 0);
    const setharamKg = (parseFloat(setharamGrams) || 0) / 1000;
    return itemKg + setharamKg;
  };

  const calculateGrandTotal = () => {
    const totalItems = items.reduce((sum, item) => sum + calculateRowTotal(item), 0);
    const courier = parseFloat(courierRs) || 0;
    const ahimsaSilk = parseFloat(ahimsaSilkRs) || 0;
    const totalOtherCharges = otherCharges.reduce((sum, charge) => sum + (parseFloat(charge.amount) || 0), 0);
    return totalItems + courier + ahimsaSilk + totalOtherCharges;
  };

  const handleSave = async () => {
    if (!companyId) {
      alert("Company is required");
      return;
    }

    let finalBillNo = billNo;
    if (!finalBillNo) {
      finalBillNo = await getNextCoolieBillNumber(companyId);
      setBillNo(finalBillNo);
    }
    
    if (!customerName) {
      alert("Customer Name is required");
      return;
    }
    
    const grandTotal = calculateGrandTotal();
    
    const billData = {
      id: existingBill?.id || `cbill_${Date.now()}`,
      bill_no: finalBillNo,
      date,
      customer_name: customerName,
      customer_name_en: customerNameEn,
      address,
      address_en: addressEn,
      city,
      city_en: cityEn,
      items,
      setharam_grams: setharamGrams,
      courier_rs: courierRs,
      ahimsa_silk_rs: ahimsaSilkRs,
      custom_charge_name: otherCharges.length > 0 ? JSON.stringify(otherCharges) : '',
      custom_charge_rs: otherCharges.reduce((sum, charge) => sum + (parseFloat(charge.amount) || 0), 0),
      bank_details: bankDetails,
      account_no: accountNo,
      ifsc: ifsc,
      company_id: companyId,
      grand_total: grandTotal,
      show_bank_details: showBankDetails,
      show_ifsc: showIfsc
    };
    
    await saveCoolieBill(billData);
    clearDraft();
    if (onSaved) {
      onSaved(billData);
    } else {
      alert("Bill Saved Successfully!");
    }
  };

  const updateCustomerName = (customer) => {
    setCustomerName(customer.name || '');
  };

  const handleCustomerSelect = (e, val) => {
    if (val && val.isAddButton) {
      skipNextInputChangeRef.current = true;
      if (onRequestAddClient) { onRequestAddClient(); } else { window.location.href = window.location.pathname + '?view=client-editor'; }
      return;
    }
    if (!val) {
      setCustomerName('');
      setCustomerNameEn('');
      setAddress('');
      setAddressEn('');
      setCity('');
      setCityEn('');
      return;
    }
    
    if (typeof val === 'string') {
      setCustomerName(val);
      setCustomerNameEn('');
      return;
    }
    
    setCustomerName(val.name || '');
    setCustomerNameEn(val.nameEn || '');
    setAddress(val.address || '');
    setAddressEn(val.addressEn || '');
    setCity(val.city || '');
    setCityEn(val.cityEn || '');
  };


  return (
    <ElvanEditorLayout
      title={t('coolieBill') || 'Coolie Bill'}
      onBack={onBack}
      onSave={handleSave}
      saveButtonText={(!!existingBill ? t('hc_updateBillBtn') : t('hc_saveBillBtn')) as string}
      hasUnsavedChanges={hasUnsavedChanges}
      onDiscard={() => {
        clearDraft();
        if (onBack) onBack();
      }}
      hideHeaderPortals={hideHeaderPortals}
    >
            <Box sx={{ display: 'flex', flexDirection: 'column', gap: 3 }}>
        
        {/* Section 1: Customer */}
        <Box sx={{ py: 3, mb: 2 }}>
          <Box sx={{ display: 'flex', alignItems: 'center', mb: 3, ml: 1.5 }}>
            <Box sx={{ width: 24, height: 24, borderRadius: '50%', bgcolor: 'primary.main', color: 'primary.contrastText', display: 'flex', alignItems: 'center', justifyContent: 'center', fontSize: '0.8rem', fontWeight: 'bold', lineHeight: 1, pt: '1px', mr: 1.5 }}>1</Box>
            <Typography variant="h6" sx={{ fontSize: '1.1rem', fontWeight: 600 }}>{t('customer') || 'Customer'}</Typography>
          </Box>
          <Box sx={{ display: 'grid', gridTemplateColumns: { xs: '1fr', md: '5fr 7fr', lg: '4fr 8fr' }, gap: 3, mb: 2 }}>
            <Box sx={{ display: 'flex', flexDirection: 'column', gap: 1 }}>
              <Box>
                <Typography variant="caption" color="text.secondary" sx={{ display: 'block', ml: 1.5, mb: 0.5 }}>
                  {t('clientName') || 'Client Name'}
                </Typography>
                <ElvanPillAutocomplete
                  freeSolo
                options={clientOptions}
                filterOptions={(options, params) => {
                  const filtered = clientFilter(options, params) as any[];
                  filtered.push({ id: 'ADD_NEW', isAddButton: true, name: 'Add New' });
                  return filtered;
                }}
                getOptionLabel={(option) => {
                  if (option.isAddButton) return '';
                  return typeof option === 'string' ? option : (option.name || option.nameEn || '');
                }}
                isOptionEqualToValue={(option, value) => {
                  if (typeof option === 'string' && typeof value === 'string') return option === value;
                  if (typeof value === 'string') return option.name === value || option.nameEn === value;
                  return option.id === value.id;
                }}
                onChange={handleCustomerSelect}
                value={customerName || null}
                inputValue={customerName || ''}
                onInputChange={(e, val, reason) => {
                  if (skipNextInputChangeRef.current) { skipNextInputChangeRef.current = false; return; }
                  setCustomerName(val || '');
                  if (reason === 'input' || reason === 'clear') {
                    setCustomerNameEn('');
                    setAddress('');
                    setAddressEn('');
                    setCity('');
                    setCityEn('');
                  }
                }}
                renderOption={(props, option, { index }) => {
                    const { key, ...optionProps } = props as any;
                    
                    if (option.isAddButton) {
                      return (
                        <li key="add-new-client" {...optionProps} style={{ padding: 0 }}>
                          <Box sx={{ p: 1.5, width: '100%', color: 'primary.main', display: 'flex', alignItems: 'center', gap: 1 }}>
                            <Plus size={18} weight="bold" />
                            <Typography fontWeight={600}>{t('hc_addNewClient') || 'Add New Client'}</Typography>
                          </Box>
                        </li>
                      );
                    }

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
                placeholder={`${t("typeClientName") || 'Type Client Name'}...`}
                textFieldProps={{ required: true, sx: { m: 0, '& .MuiInputBase-root': { mt: 0 } } }}
              />
              </Box>


              {(address || city || customerNameEn) && (
                <ElvanCard sx={{ mt: 2 }} boxSx={{ p: 2 }}>
                  <Typography variant="overline" color="text.secondary" sx={{ fontWeight: 600, display: 'block', mb: 1 }}>
                    {t('hc_savedClientDetails') || 'Saved Details'}
                  </Typography>
                  {customerNameEn && (
                    <Typography variant="subtitle2" sx={{ fontWeight: 700, mb: 1 }}>
                      {customerNameEn}
                    </Typography>
                  )}
                  <Box sx={{ mb: 1 }}>
                    {address && <Typography variant="body2">{address}</Typography>}
                    {city && <Typography variant="body2">{city}</Typography>}
                  </Box>
                  {(addressEn || cityEn) && (
                    <Box sx={{ color: 'text.secondary' }}>
                      {addressEn && <Typography variant="body2">{addressEn}</Typography>}
                      {cityEn && <Typography variant="body2">{cityEn}</Typography>}
                    </Box>
                  )}
                </ElvanCard>
              )}

              <Box sx={{ mt: 2 }}>
                <Typography variant="caption" color="text.secondary" sx={{ display: 'block', ml: 1.5, mb: 0.5 }}>
                  {t('company') || 'company'}
                </Typography>
                <ElvanPillAutocomplete
                  options={profileOptions}
                  getOptionLabel={(option) => typeof option === 'string' ? option : (option.name || option.nameEn || '')}
                  onChange={(e, val) => {
                    const pId = val ? val.id : '';
                    handleCompanyChange({ target: { value: pId } });
                  }}
                  value={profileOptions.find(p => p.id === companyId) || null}
                  renderOption={(props, option, { index }) => {
                    const { key, ...optionProps } = props as any;
                    return (
                      <li key={`company-opt-${option.id}`} {...optionProps} style={{ display: 'flex', flexDirection: 'column', alignItems: 'flex-start', borderBottom: '1px solid rgba(128,128,128,0.1)' }}>
                        <Box sx={{ display: 'flex', flexDirection: 'column', gap: 0.5 }}>
                          <Typography variant="body1" color="primary" sx={{ fontWeight: 500 }}>{option.name}</Typography>
                          {option.nameEn && <Typography variant="body2" sx={{ color: 'text.secondary' }}>{option.nameEn}</Typography>}
                        </Box>
                      </li>
                    );
                  }}
                  placeholder={`${t('company') || 'Select Company'}...`}
                  textFieldProps={{ required: true, sx: { m: 0, '& .MuiInputBase-root': { mt: 0 } } }}
                />
              </Box>
            </Box>
          </Box>
        </Box>

        <Box sx={{ 
          pointerEvents: companyId ? 'auto' : 'none', 
          opacity: companyId ? 1 : 0.4, 
          transition: 'all 0.3s ease',
          position: 'relative'
        }}>
          {!companyId && (
            <Box sx={{
              position: 'absolute', top: 0, left: 0, right: 0, bottom: 0,
              zIndex: 10, cursor: 'not-allowed'
            }} />
          )}


        {/* Section 2: Metadata */}
        <Box sx={{ mb: 2, pb: 3 }}>
          <Box sx={{ display: 'flex', alignItems: 'center', mb: 3, ml: 1.5 }}>
            <Box sx={{ width: 24, height: 24, borderRadius: '50%', bgcolor: 'primary.main', color: 'primary.contrastText', display: 'flex', alignItems: 'center', justifyContent: 'center', fontSize: '0.8rem', fontWeight: 'bold', lineHeight: 1, pt: '1px', mr: 1.5 }}>2</Box>
            <Typography variant="h6" sx={{ fontSize: '1.1rem', fontWeight: 600 }}>{t('invoiceDetails') || 'Invoice Details'}</Typography>
          </Box>
          <Box sx={{ display: 'grid', gridTemplateColumns: { xs: '1fr', md: '1fr 1fr' }, gap: 3, mb: 2 }}>
            <TextField 
              label={t('billNo') || 'Bill No'} 
              value={billNo} 
              onChange={e => setBillNo(e.target.value)} 
              size="small" 
              fullWidth
            />
            <LocalizationProvider dateAdapter={AdapterDayjs}>
              <DesktopDatePicker
                label={t('date') || 'Date'}
                value={parseDateSafe(date)}
                onChange={(newValue) => setDate(newValue ? newValue.format('DD/MM/YYYY') : '')}
                slots={{ openPickerIcon: () => <CalendarBlank size={20} weight="regular" /> }}
                slotProps={{ textField: { fullWidth: true, size: 'small' } }}
                format="DD/MM/YYYY"
              />
            </LocalizationProvider>
          </Box>
        </Box>



        {/* Section 3: Items Table */}
        <Box sx={{ mb: 2, pb: 3 }}>
          <Box sx={{ display: 'flex', alignItems: 'center', mb: 3, ml: 1.5 }}>
            <Box sx={{ width: 24, height: 24, borderRadius: '50%', bgcolor: 'primary.main', color: 'primary.contrastText', display: 'flex', alignItems: 'center', justifyContent: 'center', fontSize: '0.8rem', fontWeight: 'bold', lineHeight: 1, pt: '1px', mr: 1.5 }}>3</Box>
            <Typography variant="h6" sx={{ fontSize: '1.1rem', fontWeight: 600 }}>{t('itemsList') || 'Items'}</Typography>
          </Box>
          {items.map((item, index) => (
            <Box key={index} sx={{ mb: 2 }}>
              <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 1, px: 1.5 }}>
                <Typography variant="subtitle2" color="text.secondary">
                  {t('item') || 'Item'} #{index + 1}
                </Typography>
                <IconButton 
                  onClick={() => setItems(items.length > 1 ? items.filter((_, i) => i !== index) : items)} 
                  title={t('hc_remove') || 'Remove'}
                  sx={{ 
                    bgcolor: (theme) => theme.palette.mode === 'dark' ? 'action.hover' : 'background.paper',
                    color: 'text.secondary',
                    '@media (hover: hover)': { '&:hover': { bgcolor: (theme) => theme.palette.mode === 'dark' ? 'action.selected' : 'error.light', color: (theme) => theme.palette.mode === 'dark' ? 'error.main' : 'error.main' } }
                  }}
                >
                  <Trash size={20} weight="regular" />
                </IconButton>
              </Box>
              <Box sx={{ display: 'flex', flexWrap: 'wrap', gap: 2, p: 2, bgcolor: (theme) => theme.palette.mode === 'dark' ? 'action.hover' : 'background.paper', borderRadius: '16px' }}>
                
                {/* Product Search */}
                <Box sx={{ flex: { xs: '1 1 100%', sm: '3 1 250px' }, minWidth: 0 }}>
                  <ElvanPillAutocomplete
                    freeSolo
                    options={productOptions}
                    filterOptions={(options, params) => {
                      const filtered = filter(options, params);
                      filtered.push({ isAddButton: true });
                      return filtered;
                    }}
                    getOptionLabel={(option) => {
                      if (option.isAddButton) return '';
                      return typeof option === 'string' ? option : (option.name || option.nameEn || '');
                    }}
                    isOptionEqualToValue={(option, value) => {
                      if (typeof option === 'string' && typeof value === 'string') return option === value;
                      if (typeof value === 'string') return option.name === value || option.nameEn === value;
                      return option.id === value.id;
                    }}
                    onChange={(e, val) => {
                      if (val && val.isAddButton) {
                        skipNextInputChangeRef.current = true;
                        if (onRequestAddProduct) { onRequestAddProduct(); } else { window.location.href = window.location.pathname + '?view=product-editor'; }
                        return;
                      }
                      setItems(prev => {
                        const newItems = [...prev];
                        const updatedItem = { ...newItems[index] };
                        if (typeof val === 'string') {
                          updatedItem.porul = val;
                        } else if (val) {
                          updatedItem.porul = val.name || val.nameEn || '';
                          updatedItem.name_tamil = val.name || '';
                          updatedItem.name_english = val.nameEn || '';
                          if (val.rate) updatedItem.coolie = val.rate;
                        } else {
                          updatedItem.porul = '';
                        }
                        newItems[index] = updatedItem;
                        return newItems;
                      });
                    }}
                    value={item.porul ? item.porul : null}
                    inputValue={item.porul || ''}
                    onInputChange={(e, val, reason) => {
                      if (skipNextInputChangeRef.current) { skipNextInputChangeRef.current = false; return; }
                      setItems(prev => {
                        const newItems = [...prev];
                        newItems[index] = { ...newItems[index], porul: val };
                        return newItems;
                      });
                    }}
                    renderOption={(props, option, { index }) => {
                        const { key, ...optionProps } = props as any;
                        if (option.isAddButton) {
                          return (
                            <li key={`item-opt-add-${index}`} {...optionProps} style={{...optionProps.style, padding: 0}}>
                              <Box sx={{ width: '100%' }}>
                                {index > 0 && <Divider />}
                                <ListItemButton sx={{ color: 'primary.main', width: '100%', py: 1.5 }}>
                                  <Plus size={18} weight="bold" style={{ marginRight: 8 }} />
                                  <Typography fontWeight={600}>{t('hc_addNewProduct') || 'Add New Product'}</Typography>
                                </ListItemButton>
                              </Box>
                            </li>
                          );
                        }
                        if (typeof option === 'string') {
                          return (
                            <li key={`item-opt-${index}`} {...optionProps}>
                              <Typography variant="body2">{option}</Typography>
                            </li>
                          );
                        }
                        return (
                          <li key={`item-opt-${index}`} {...optionProps} style={{ display: 'flex', flexDirection: 'column', alignItems: 'flex-start' }}>
                            <Typography variant="body2" sx={{ fontWeight: 500 }}>{option.name}</Typography>
                            {option.nameEn && <Typography variant="caption" sx={{ color: 'text.secondary' }}>{option.nameEn}</Typography>}
                          </li>
                        );
                    }}
                    label={t('itemName') || 'Item Name'}
                    placeholder={t('hc_searchSavedItems') || 'Search saved items'}
                    textFieldProps={{
                      inputProps: { autoComplete: 'new-password' }
                    }}
                  />
                </Box>

                {/* Qty */}
                <Box sx={{ flex: { xs: '1 1 40%', sm: '1 1 120px' } }}>
                  <TextField fullWidth size="small" label={t('quantity') || 'Weight'} type="number" slotProps={{ inputLabel: { shrink: true }, htmlInput: { min: 0, step: "any" } }} 
                    value={item.kg} onChange={e => { const i = [...items]; i[index].kg = e.target.value; setItems(i); }} />
                </Box>

                {/* Rate */}
                <Box sx={{ flex: { xs: '1 1 40%', sm: '1 1 120px' } }}>
                  <TextField fullWidth size="small" label={t('rate') || 'Rate'} type="number" slotProps={{ inputLabel: { shrink: true }, htmlInput: { min: 0, step: "any" } }} 
                    value={item.coolie} onChange={e => { const i = [...items]; i[index].coolie = e.target.value; setItems(i); }} />
                </Box>

                {/* Line Total */}
                <Box sx={{ flex: { xs: '1 1 40%', sm: '1 1 120px' } }}>
                  <TextField
                    fullWidth
                    size="small"
                    label={t('total') || 'Total'}
                    value={`₹ ${calculateRowTotal(item)}`}
                    slotProps={{ 
                      inputLabel: { shrink: true },
                      htmlInput: { readOnly: true, style: { fontWeight: 600 } }
                    }}
                  />
                </Box>

              </Box>
            </Box>
          ))}
          <Box sx={{ display: 'flex', gap: 2, flexWrap: 'wrap', mt: 1, mb: otherCharges.length > 0 ? 4 : 1 }}>
            <Button 
              variant="text" 
              startIcon={<Plus size={18} weight="regular" />} 
              onClick={() => setItems([...items, { porul: '', coolie: '', kg: '', name_english: '', name_tamil: '' }])}
              sx={{
                bgcolor: (theme) => theme.palette.mode === 'dark' ? 'action.hover' : 'background.paper',
                color: 'text.primary',
                borderRadius: '24px',
                px: 3,
                py: 1,
                boxShadow: 'none',
                border: 'none',
                '@media (hover: hover)': { '&:hover': { bgcolor: (theme) => theme.palette.mode === 'dark' ? 'action.selected' : 'rgba(0,0,0,0.04)' } }
              }}
            >
              {t('addAnotherItem') || 'Add Item'}
            </Button>
            
            {otherCharges.length === 0 && (
              <Button 
                variant="text" 
                startIcon={<Plus size={18} weight="regular" />} 
                onClick={() => setOtherCharges([...otherCharges, { name: '', amount: '' }])}
                sx={{
                  bgcolor: (theme) => theme.palette.mode === 'dark' ? 'action.hover' : 'background.paper',
                  color: 'text.primary',
                  borderRadius: '24px',
                  px: 3,
                  py: 1,
                  boxShadow: 'none',
                  border: 'none',
                  '@media (hover: hover)': { '&:hover': { bgcolor: (theme) => theme.palette.mode === 'dark' ? 'action.selected' : 'rgba(0,0,0,0.04)' } }
                }}
              >
                {t('addOtherCharges') || 'Add Other Charges'}
              </Button>
            )}
          </Box>

          {/* Other Charges Boxes */}
          {otherCharges.map((charge, index) => (
            <Box key={`oc-${index}`} sx={{ width: '100%', mb: 2 }}>
              <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 1, px: 1.5 }}>
                <Typography variant="subtitle2" color="text.secondary" fontWeight={600}>
                  {t('otherCharges') || 'Other Charges'} #{index + 1}
                </Typography>
                <IconButton 
                  size="small" 
                  onClick={() => setOtherCharges(otherCharges.filter((_, i) => i !== index))} 
                  title={t('hc_remove') || 'Remove'}
                  sx={{ 
                    bgcolor: (theme) => theme.palette.mode === 'dark' ? 'action.hover' : 'background.paper',
                    color: 'text.secondary',
                    '@media (hover: hover)': { '&:hover': { bgcolor: (theme) => theme.palette.mode === 'dark' ? 'action.selected' : 'error.light', color: (theme) => theme.palette.mode === 'dark' ? 'error.main' : 'error.main' } }
                  }}
                >
                  <Trash size={20} weight="regular" />
                </IconButton>
              </Box>
              <ElvanCard boxSx={{ p: 2 }} sx={{ borderRadius: '16px' }}>
                <Box sx={{ display: 'grid', gridTemplateColumns: { xs: '1fr', sm: '2fr 1fr' }, gap: 2 }}>
                  <TextField 
                    size="small" fullWidth 
                    label={t('chargeName') || 'Charge Name'}
                    slotProps={{ inputLabel: { shrink: true } }} 
                    value={charge.name} onChange={e => {
                      const newCharges = [...otherCharges];
                      newCharges[index].name = e.target.value;
                      setOtherCharges(newCharges);
                    }} 
                    sx={{ '& fieldset': { border: 'none' } }}
                  />
                  
                  <TextField 
                    size="small" fullWidth type="number" 
                    label={`${t('amount') || 'Amount'} (₹)`}
                    slotProps={{ inputLabel: { shrink: true } }} 
                    value={charge.amount} onChange={e => {
                      const newCharges = [...otherCharges];
                      newCharges[index].amount = e.target.value;
                      setOtherCharges(newCharges);
                    }} 
                    sx={{ '& fieldset': { border: 'none' } }}
                  />
                </Box>
              </ElvanCard>
            </Box>
          ))}

          {otherCharges.length > 0 && (
            <Box sx={{ display: 'flex', gap: 2, flexWrap: 'wrap', mt: 1 }}>
              <Button 
                variant="text" 
                startIcon={<Plus size={18} weight="regular" />} 
                onClick={() => setOtherCharges([...otherCharges, { name: '', amount: '' }])}
                sx={{
                  bgcolor: (theme) => theme.palette.mode === 'dark' ? 'action.hover' : 'background.paper',
                  color: 'text.primary',
                  borderRadius: '24px',
                  px: 3,
                  py: 1,
                  boxShadow: 'none',
                  border: 'none',
                  '@media (hover: hover)': { '&:hover': { bgcolor: (theme) => theme.palette.mode === 'dark' ? 'action.selected' : 'rgba(0,0,0,0.04)' } }
                }}
              >
                {t('addOtherCharges') || 'Add Other Charges'}
              </Button>
            </Box>
          )}
        </Box>

        {/* Extra Charges Section (Bento Style) */}
        <Box sx={{ display: 'flex', flexDirection: 'column', gap: 3, mb: 4 }}>
          {/* Bento Boxes for Extra Charges */}
          <Box sx={{ display: 'grid', gridTemplateColumns: { xs: '1fr 1fr', md: '1fr 1fr 1fr' }, gap: 3 }}>
            <ElvanCard boxSx={{ p: 2, display: 'flex', alignItems: 'center' }} sx={{ gridColumn: { xs: '1 / -1', md: 'auto' }, borderRadius: '16px' }}>
              <TextField 
                size="small" fullWidth type="number" 
                label={`${t('setharam') || 'Setharam'} (Grams)`}
                slotProps={{ inputLabel: { shrink: true }, htmlInput: { step: '0.001' } }} 
                value={setharamGrams} onChange={e => setSetharamGrams(e.target.value)} 
                sx={{ '& fieldset': { border: 'none' } }}
              />
            </ElvanCard>
            
            <ElvanCard boxSx={{ p: 2, display: 'flex', alignItems: 'center' }} sx={{ borderRadius: '16px' }}>
              <TextField 
                size="small" fullWidth type="number" 
                label={`${t('ahimsaSilk') || 'Ahimsa Silk'} (₹)`}
                slotProps={{ inputLabel: { shrink: true } }} 
                value={ahimsaSilkRs} onChange={e => setAhimsaSilkRs(e.target.value)} 
                sx={{ '& fieldset': { border: 'none' } }}
              />
            </ElvanCard>
            
            <ElvanCard boxSx={{ p: 2, display: 'flex', alignItems: 'center' }} sx={{ borderRadius: '16px' }}>
              <TextField 
                size="small" fullWidth type="number" 
                label={`${t('courier') || 'Courier Charge'} (₹)`}
                slotProps={{ inputLabel: { shrink: true } }} 
                value={courierRs} onChange={e => setCourierRs(e.target.value)} 
                sx={{ '& fieldset': { border: 'none' } }}
              />
            </ElvanCard>
          </Box>


        </Box>

        {/* Section 4: Totals and Bank */}
        <Box sx={{ display: 'flex', flexDirection: 'column', gap: 4, mb: 2 }}>
          {/* Totals Section */}
          <Box sx={{ display: 'flex', flexDirection: 'column', gap: 2, alignItems: { xs: 'stretch', md: 'flex-start' } }}>
            {/* Static Totals Box */}
            <Box sx={{ width: '100%', maxWidth: { xs: '100%', sm: 400 } }}>
              <ElvanCard boxSx={{ p: 3 }} sx={{ borderRadius: '24px' }}>
                <Stack spacing={1.5}>
                  <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                    <Typography variant="subtitle2" color="text.secondary" fontWeight={600}>{t('subTotal') || 'Sub Total'}</Typography>
                    <Typography variant="subtitle1" fontWeight={700}>₹ {calculateSubtotal().toLocaleString('en-IN')}</Typography>
                  </Box>

                  {(parseFloat(ahimsaSilkRs) || 0) > 0 && (
                    <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                      <Typography variant="subtitle2" color="text.secondary" fontWeight={500}>{t('ahimsaSilk') || 'Ahimsa Silk'}</Typography>
                      <Typography variant="subtitle2" fontWeight={600}>₹ {parseFloat(ahimsaSilkRs).toLocaleString('en-IN')}</Typography>
                    </Box>
                  )}
                  
                  {(parseFloat(courierRs) || 0) > 0 && (
                    <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                      <Typography variant="subtitle2" color="text.secondary" fontWeight={500}>{t('courier') || 'Courier'}</Typography>
                      <Typography variant="subtitle2" fontWeight={600}>₹ {parseFloat(courierRs).toLocaleString('en-IN')}</Typography>
                    </Box>
                  )}

                  {otherCharges.map((charge, i) => (parseFloat(charge.amount) || 0) > 0 && (
                    <Box key={i} sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                      <Typography variant="subtitle2" color="text.secondary" fontWeight={500}>{charge.name || 'Other Charge'}</Typography>
                      <Typography variant="subtitle2" fontWeight={600}>₹ {parseFloat(charge.amount).toLocaleString('en-IN')}</Typography>
                    </Box>
                  ))}
                  
                  <Box sx={{ mt: 1, display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                    <Typography variant="subtitle2" color="text.secondary" fontWeight={600}>{t('totalWeight') || 'Total Weight'}</Typography>
                    <Typography variant="subtitle1" fontWeight={700}>{calculateTotalKg().toFixed(3)} Kg</Typography>
                  </Box>

                  {(parseFloat(setharamGrams) || 0) > 0 && (
                    <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                      <Typography variant="subtitle2" color="text.secondary" fontWeight={500}>+ {t('setharam') || 'Setharam'}</Typography>
                      <Typography variant="subtitle2" fontWeight={600}>{setharamGrams} {t('grams') || 'g'}</Typography>
                    </Box>
                  )}
                  
                  <Divider sx={{ my: 0.5 }} />
                  
                  <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                    <Typography variant="h6" fontWeight={700} color="primary.main">{t('total') || 'Total'}</Typography>
                    <Typography variant="h5" color="primary" fontWeight={800}>₹ {calculateGrandTotal().toLocaleString('en-IN')}</Typography>
                  </Box>
                </Stack>
              </ElvanCard>
            </Box>
          </Box>


        </Box>

        </Box>
      </Box>
    </ElvanEditorLayout>
  );
}
