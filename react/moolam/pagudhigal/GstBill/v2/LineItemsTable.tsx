import React, { useState, useEffect } from 'react';
import { Box, Typography, Paper, TextField, IconButton, Button, Divider, List, ListItem, ListItemButton, ListItemText, InputAdornment, Select, MenuItem, ToggleButton, ToggleButtonGroup } from '@mui/material';
import { Trash, Plus, X } from '@phosphor-icons/react';
import { useLanguage } from '../../../mozhi/LanguageContext';
import { getDynamicField } from '../../../Payanpadu';
import ElvanBilingualField from '../../ElvanBilingualField';
import { LineItemState, InvoiceSettingsState, createEmptyLineItem } from './InvoiceTypes';
import { getAllProducts } from '../../../Avanam';
import { formatCurrency } from '../../../Payanpadu';

interface LineItemsTableProps {
  items: LineItemState[];
  setItems: (items: LineItemState[] | ((prev: LineItemState[]) => LineItemState[])) => void;
  settings: InvoiceSettingsState;
  setSettings?: React.Dispatch<React.SetStateAction<InvoiceSettingsState>>;
  isBilingual: boolean;
  primaryLang: string;
  secondaryLang: string;
  profile: any;
  onRequestAddProduct?: () => void;
  dataVersion?: number;
}

export default function LineItemsTable({
  items,
  setItems,
  settings,
  setSettings,
  isBilingual,
  primaryLang,
  secondaryLang,
  profile,
  onRequestAddProduct,
  dataVersion,
}: LineItemsTableProps) {
  const { t, language } = useLanguage();
  
  const [products, setProducts] = useState<any[]>([]);
  const [activeSuggestionRow, setActiveSuggestionRow] = useState<string | null>(null);

  useEffect(() => {
    getAllProducts(setProducts).then(setProducts);
  }, []);

  useEffect(() => {
    if (dataVersion && dataVersion > 0) {
      getAllProducts(setProducts).then(setProducts);
    }
  }, [dataVersion]);

  useEffect(() => {
    const handleClickOutside = (e: MouseEvent) => {
      const target = e.target as HTMLElement;
      if (!target.closest('.suggestion-dropdown') && !target.closest('.suggestion-input')) {
        setActiveSuggestionRow(null);
      }
    };
    document.addEventListener('mousedown', handleClickOutside);
    return () => document.removeEventListener('mousedown', handleClickOutside);
  }, []);

  const getItemField = (item: LineItemState, field: string, lang: string) => {
    if (lang === primaryLang) {
      return (item[`${field}_${lang}` as keyof LineItemState] || (item as any)[field] || '') as string;
    }
    return (item[`${field}_${lang}` as keyof LineItemState] || '') as string;
  };

  const handleItemChange = (id: string, field: string, value: any) => {
    setItems(prev => prev.map(item => {
      if (item.id === id) {
        if (field === 'quantity') {
          return { ...item, qty: value };
        }
        return { ...item, [field]: value };
      }
      return item;
    }));
  };

  const selectProduct = (id: string, product: any) => {
    setItems(prev => prev.map(item => {
      if (item.id === id) {
        return {
          ...item,
          productId: product.id,
          // Copy all language keys directly
          ...Object.keys(product).filter(k => k.startsWith('name_') || k.startsWith('description_')).reduce((acc: any, key) => { acc[key] = product[key]; return acc; }, {}),
          hsn: product.hsn || '',
          rate: product.rate || 0,
          unit: product.unit || item.unit || 'Nos',
          taxPercent: product.taxPercent || item.taxPercent || 0,
          cessPercent: product.cessPercent || 0,
        };
      }
      return item;
    }));
    setActiveSuggestionRow(null);
  };

  const removeItem = (id: string) => {
    setItems(prev => prev.length > 1 ? prev.filter(i => i.id !== id) : prev);
  };

  const addItem = () => {
    setItems(prev => [...prev, createEmptyLineItem()]);
  };

  const clampNonNeg = (v: any) => {
    if (v === '' || v === null || v === undefined) return '';
    const n = parseFloat(v);
    if (!isFinite(n) || n < 0) return 0;
    return n;
  };

  const currency = settings.currency || 'INR';

  return (
    <Box sx={{ py: 3 }}>
      <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 3, ml: 1.5 }}>
        <Box sx={{ display: 'flex', alignItems: 'center', gap: 1.5 }}>
          <Box sx={{ width: 24, height: 24, borderRadius: '50%', bgcolor: 'primary.main', color: 'primary.contrastText', display: 'flex', alignItems: 'center', justifyContent: 'center', fontSize: '0.8rem', fontWeight: 'bold', lineHeight: 1, pt: '1px' }}>3</Box>
          <Typography variant="h6" sx={{ fontSize: '1.1rem', fontWeight: 600 }}>{t('lineItems')}</Typography>
        </Box>
        {setSettings && (
          <ToggleButtonGroup
            color="primary"
            value={settings.measureMode || 'quantity'}
            exclusive
            onChange={(e, newVal) => {
              if (newVal) {
                setSettings(prev => ({ ...prev, measureMode: newVal }));
              }
            }}
            sx={{ 
              height: '36px',
              bgcolor: (theme) => theme.palette.mode === 'dark' ? 'action.hover' : 'background.paper',
              borderRadius: '50px',
              p: 0.5,
              '.MuiToggleButton-root': {
                border: 'none',
                borderRadius: '50px !important',
                color: 'text.secondary',
                mx: 0.25,
                px: 2,
                '&.Mui-selected': {
                  bgcolor: 'primary.main',
                  color: 'primary.contrastText',
                  boxShadow: (theme) => theme.palette.mode === 'dark' ? '0 2px 8px rgba(0,0,0,0.2)' : 'none',
                  '@media (hover: hover)': { '&:hover': {
                    bgcolor: 'primary.dark',
                  } }
                }
              }
            }}
          >
            <ToggleButton value="quantity" sx={{ textTransform: 'none', fontWeight: 600 }}>
              {t('quantity')}
            </ToggleButton>
            <ToggleButton value="weight" sx={{ textTransform: 'none', fontWeight: 600 }}>
              {t('weight')}
            </ToggleButton>
          </ToggleButtonGroup>
        )}
      </Box>
      
      {items.map((item: any, index) => (
        <Box key={item.id} sx={{ mb: 2 }}>
          <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 1, px: 1.5 }}>
            <Typography variant="subtitle2" color="text.secondary">
              {t('item')} #{index + 1}
            </Typography>
            <IconButton 
              onClick={() => removeItem(item.id)} 
              title={t('hc_remove')}
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
            <Box sx={{ flex: { xs: '1 1 100%', sm: '3 1 250px' }, position: 'relative' }}>
              <TextField 
                fullWidth size="small" 
                label={t("descriptionCol")} 
                placeholder={t('hc_searchSavedItems') || 'Search saved items'}
                value={getItemField(item, 'name', primaryLang)}
                className="suggestion-input"
                onChange={(e) => {
                  handleItemChange(item.id, 'productId', null);
                  handleItemChange(item.id, 'name', e.target.value);
                  setActiveSuggestionRow(item.id);
                }}
                onFocus={() => setActiveSuggestionRow(item.id)}
                autoComplete="off"
                helperText={item.productId ? [
                  profile?.enableBilingual !== false ? getItemField(item, 'name', secondaryLang) : null,
                  item.taxPercent ? `GST ${item.taxPercent}%` : '',
                ].filter(Boolean).join(' · ') : undefined}
                slotProps={{ 
                  inputLabel: { shrink: true },
                  formHelperText: { sx: { mt: 0.5, ml: 2, lineHeight: 1.2 } },
                  input: getItemField(item, 'name', primaryLang) ? {
                    endAdornment: (
                      <InputAdornment position="end">
                        <IconButton
                          size="small"
                          onClick={(e) => {
                            e.stopPropagation();
                            handleItemChange(item.id, 'productId', null);
                            handleItemChange(item.id, `name_${primaryLang}`, '');
                            handleItemChange(item.id, `name_${secondaryLang}`, '');
                            handleItemChange(item.id, 'name', ''); // Clear generic name too just in case
                            setActiveSuggestionRow(item.id);
                          }}
                          edge="end"
                          sx={{ color: 'text.secondary' }}
                        >
                          <X size={18} weight="bold" />
                        </IconButton>
                      </InputAdornment>
                    ),
                  } : undefined
                }}
              />

              {activeSuggestionRow === item.id && (
                <Paper className="suggestion-dropdown" elevation={4} sx={{ position: 'absolute', top: '100%', left: 0, right: 0, zIndex: 10, mt: 0.5, maxHeight: 300, overflow: 'auto' }}>
                  <List disablePadding>
                    {(() => {
                      const q = (getItemField(item, 'name', primaryLang) || '').toLowerCase();
                      const getProdName = (p: any, lang: string) => {
                        return getDynamicField(p, 'name', { primaryDataLanguage: lang, secondaryDataLanguage: '' }, true);
                      };
                      
                      const filtered = products.filter(p => {
                        const pName = getProdName(p, primaryLang) || '';
                        const pNameSec = getProdName(p, secondaryLang) || '';
                        const matchesQuery = !q || (pName.toLowerCase().includes(q) || pNameSec.toLowerCase().includes(q) || p.hsn?.toLowerCase().includes(q));
                        const matchesMeasureType = (settings.measureMode || 'quantity') === (p.measureType || 'quantity');
                        return matchesQuery && matchesMeasureType;
                      });
                      
                      if (filtered.length === 0) {
                        return (
                          <ListItem>
                            <ListItemText primary={q ? "No saved items found." : "Type to search items"} />
                          </ListItem>
                        );
                      }
                      
                      const mappedProducts = filtered.map(p => {
                        const pName = getProdName(p, primaryLang);
                        const pNameSec = getProdName(p, secondaryLang);
                        return (
                          <ListItem key={p.id} disablePadding>
                            <ListItemButton onClick={() => selectProduct(item.id, p)}>
                              <ListItemText 
                                primary={pName}
                                secondary={[
                                  profile?.enableBilingual !== false ? pNameSec : null,
                                  p.rate ? formatCurrency(p.rate, currency) : '',
                                ].filter(Boolean).join(' · ')}
                              />
                            </ListItemButton>
                          </ListItem>
                        );
                      });

                      return (
                        <>
                          {mappedProducts}
                          <Divider />
                          <ListItem disablePadding>
                            <ListItemButton 
                              onClick={() => {
                                setActiveSuggestionRow(null);
                                if (onRequestAddProduct) { onRequestAddProduct(); } else { window.location.href = window.location.pathname + '?view=product-editor'; }
                              }} 
                              sx={{ color: 'primary.main' }}
                            >
                              <Plus size={18} weight="bold" style={{ marginRight: 8 }} />
                              <ListItemText primary={<Typography sx={{ fontWeight: 600 }}>{t('hc_addNewProduct') || 'Add New Product'}</Typography>} />
                            </ListItemButton>
                          </ListItem>
                        </>
                      );
                    })()}
                  </List>
                </Paper>
              )}

            </Box>

            {/* Qty */}
            <Box sx={{ flex: { xs: '1 1 40%', sm: '1 1 120px' } }}>
              <TextField 
                fullWidth 
                size="small" 
                label={settings.measureMode === 'weight' ? (t('weight')) : (t('quantity'))} 
                type="number" 
                slotProps={{ 
                  inputLabel: { shrink: true }, 
                  htmlInput: { min: 0, step: "any" },
                  input: settings.measureMode === 'weight' ? { endAdornment: <InputAdornment position="end" sx={{ mt: '0 !important' }}>kg</InputAdornment> } : undefined
                }} 
                value={item.qty} 
                onChange={(e) => handleItemChange(item.id, 'quantity', clampNonNeg(e.target.value))} 
                onBlur={(e) => {
                  if (settings.measureMode === 'weight' && item.qty) {
                    const parsed = parseFloat(item.qty);
                    if (!isNaN(parsed)) {
                      handleItemChange(item.id, 'quantity', parsed.toFixed(3));
                    }
                  }
                }}
              />
            </Box>

            {/* Rate */}
            <Box sx={{ flex: { xs: '1 1 40%', sm: '1 1 120px' } }}>
              <TextField fullWidth size="small" label={t("rateCol")} type="number" slotProps={{ inputLabel: { shrink: true }, htmlInput: { min: 0, step: "any" } }} 
                value={item.rate} onChange={(e) => handleItemChange(item.id, 'rate', clampNonNeg(e.target.value))} />
            </Box>

            {/* Discount */}
            {settings.showDiscountColumn && (
              <Box sx={{ flex: { xs: '1 1 40%', sm: '1 1 120px' } }}>
                <TextField fullWidth size="small" label={t('discount')} type="number" 
                  slotProps={{ 
                    inputLabel: { shrink: true }, 
                    htmlInput: { min: 0, step: "any" },
                    input: {
                      endAdornment: (
                        <InputAdornment position="end">
                          <Select
                            variant="standard"
                            disableUnderline
                            size="small"
                            value={item.discountType || 'amount'}
                            onChange={(e) => handleItemChange(item.id, 'discountType', e.target.value)}
                            sx={{ minWidth: 45, px: 1, py: 0.5, borderTopRightRadius: 4, borderBottomRightRadius: 4, height: '100%', '& .MuiSelect-select': { py: 0 } }}
                          >
                            <MenuItem value="amount">₹</MenuItem>
                            <MenuItem value="percentage">%</MenuItem>
                          </Select>
                        </InputAdornment>
                      )
                    }
                  }} 
                  value={item.discount} onChange={(e) => handleItemChange(item.id, 'discount', clampNonNeg(e.target.value))} />
              </Box>
            )}

            {/* Line Total */}
            <Box sx={{ flex: { xs: '1 1 40%', sm: '1 1 120px' } }}>
              {(() => {
                 const qty = Number(item.qty) || 0;
                 const rate = Number(item.rate) || 0;
                 const rawDiscount = Number(item.discount) || 0;
                 const discountAmt = item.discountType === 'percentage' ? (qty * rate) * (rawDiscount / 100) : rawDiscount;
                 const taxable = (qty * rate) - discountAmt;
                 const taxAmt = (taxable > 0 ? taxable : 0) * ((item.taxPercent || 0) / 100);
                 const cessAmt = settings.showCess ? (taxable > 0 ? taxable : 0) * ((item.cessPercent || 0) / 100) : 0;
                 const total = (taxable > 0 ? taxable : 0) + taxAmt + cessAmt;
                 return (
                   <TextField
                     fullWidth
                     size="small"
                     label={t('total') || 'Total'}
                     value={formatCurrency(total, currency)}
                     slotProps={{ 
                       inputLabel: { shrink: true },
                       htmlInput: { readOnly: true, style: { fontWeight: 600 } }
                     }}
                   />
                 );
              })()}
            </Box>

          </Box>
        </Box>
      ))}
      <Button 
        variant="text" 
        startIcon={<Plus size={18} weight="regular" />} 
        onClick={addItem} 
        sx={{ 
          mt: 1,
          bgcolor: (theme) => theme.palette.mode === 'dark' ? 'action.hover' : 'background.paper',
          color: 'text.primary',
          borderRadius: '24px',
          px: 3,
          py: 1,
          boxShadow: 'none',
          '@media (hover: hover)': { '&:hover': { 
            bgcolor: 'action.hover'
          } }
        }}
      >
        {t('addItemBtn') as string}
      </Button>
    </Box>
  );
}
