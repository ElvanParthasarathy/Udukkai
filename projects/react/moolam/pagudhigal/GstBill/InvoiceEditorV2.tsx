import React, { useState, useEffect } from 'react';
import { Box, Typography, Paper, Button, Grid, FormControl, Select, MenuItem, Divider, Dialog, DialogTitle, DialogContent, DialogContentText, DialogActions } from '@mui/material';
import { ArrowLeft, FloppyDisk } from '@phosphor-icons/react';
import { ClientState, InvoiceMetadataState, LineItemState, InvoiceSettingsState, InvoiceTotalsState, createEmptyLineItem, createEmptyClient } from './v2/InvoiceTypes';
import ClientSelection from './v2/ClientSelection';
import InvoiceMetadata from './v2/InvoiceMetadata';
import LineItemsTable from './v2/LineItemsTable';
import InvoiceTotals from './v2/InvoiceTotals';
import { saveBill, getNextInvoiceNumber } from '../../Avanam';
import { thagaval } from '../Thagaval';
import { useLanguage } from '../../mozhi/LanguageContext';
import { INVOICE_TYPES } from '../../Payanpadu';
import ElvanEditorLayout from '../ElvanEditorLayout';
import { useDraftAndUnsaved } from '../../hooks/useDraftAndUnsaved';

export default function InvoiceEditorV2({ onBack, onSaved, profile: profileProp, editingBill, onRequestAddClient, onRequestAddProduct, dataVersion, hideHeaderPortals }: any) {
  const { t } = useLanguage();
  const [client, setClient] = useState<ClientState>(createEmptyClient());
  const [items, setItems] = useState<LineItemState[]>([createEmptyLineItem()]);
  const [totals, setTotals] = useState<InvoiceTotalsState>({
    subtotal: 0,
    totalDiscount: 0,
    cgst: 0,
    sgst: 0,
    igst: 0,
    roundOff: 0,
    tcsAmount: 0,
    tdsAmount: 0,
    total: 0,
    netReceivable: 0,
    amountPaid: 0,
    globalDiscountValue: 0,
    globalDiscountType: 'percentage',
  });
  
  const [customTerms, setCustomTerms] = useState(profileProp?.invoiceTerms || '');
  const [internalNote, setInternalNote] = useState('');
  const [saving, setSaving] = useState(false);
  
  const [settings, setSettings] = useState<InvoiceSettingsState>({
    taxInclusive: false,
    showDiscountColumn: true,
    showHSN: true,
    showPlaceOfSupply: true,
    showVehicle: false,
    showDueDate: false,
    showTerms: true,
    showBank: true,
    showNotes: true,
    showSignature: true,
    ...(profileProp?.invoiceOptions || {})
  });
  
  const [metadata, setMetadata] = useState<InvoiceMetadataState>({
    invoiceType: 'tax-invoice',
    invoiceNumber: '',
    date: new Date().toISOString().split('T')[0],
    placeOfSupply: '',
  });

  const isBilingual = profileProp?.enableBilingual !== false;
  const primaryLang = profileProp?.primaryDataLanguage || 'Tamil';
  const secondaryLang = profileProp?.secondaryDataLanguage || 'English';

  // Initialize from editing bill or generate new number — exact same logic as v1
  useEffect(() => {
    if (editingBill?.data) {
      const d = editingBill.data;
      
      // Load common data
      if (d.client) setClient(d.client);
      if (d.items?.length) setItems(d.items);
      if (d.totals) setTotals(d.totals);
      if (d.invoiceOptions) {
        setSettings({
          ...settings,
          ...d.invoiceOptions,
          showDiscountColumn: d.invoiceOptions.showDiscountColumn ?? d.invoiceOptions.showDiscount ?? true,
        });
      }
      if (d.customTerms !== undefined) setCustomTerms(d.customTerms);
      if (d.internalNote !== undefined) setInternalNote(d.internalNote);

      if (editingBill._isDuplicate) {
        const convertType = editingBill._convertToType;
        const type = convertType || d.invoiceType || 'tax-invoice';
        setMetadata(prev => ({ ...prev, invoiceType: type, placeOfSupply: d.details?.placeOfSupply || '' }));
        const prefix = (INVOICE_TYPES as any)[type]?.prefix || 'INV';
        getNextInvoiceNumber(prefix).then((num: string) => {
          setMetadata(prev => ({ ...prev, invoiceNumber: num, date: new Date().toISOString().split('T')[0] }));
        });
      } else {
        setMetadata(prev => ({
          ...prev,
          invoiceType: d.invoiceType || 'tax-invoice',
          invoiceNumber: d.details?.invoiceNumber || prev.invoiceNumber,
          date: d.details?.invoiceDate || prev.date,
          placeOfSupply: d.details?.placeOfSupply || '',
        }));
      }
    } else if (!metadata.invoiceNumber) {
      const prefix = (INVOICE_TYPES as any)[metadata.invoiceType]?.prefix || 'INV';
      getNextInvoiceNumber(prefix).then((num: string) => {
        setMetadata(prev => ({ ...prev, invoiceNumber: num }));
      });
    }
  }, [editingBill]);

  const handleTypeChange = async (e: any) => {
    const type = e.target.value as string;
    const config = (INVOICE_TYPES as any)[type];
    const prefix = config?.prefix || 'INV';
    const num = await getNextInvoiceNumber(prefix);
    setMetadata(prev => ({ ...prev, invoiceType: type, invoiceNumber: num }));

    // Auto-set options based on type — same as v1
    if (type === 'bill-of-supply') {
      setSettings(prev => ({ ...prev, showPlaceOfSupply: false }));
    } else {
      setSettings(prev => ({ ...prev, showPlaceOfSupply: config?.showGST ?? true }));
    }
  };

  const formState = { client, items, totals, customTerms, internalNote, settings, metadata };
  const [initialForm, setInitialForm] = useState<any>(editingBill ? formState : { ...formState });

  useEffect(() => {
    if (!editingBill && metadata.invoiceNumber) {
      setInitialForm((prev: any) => ({ ...prev, metadata: { ...prev.metadata, invoiceNumber: metadata.invoiceNumber } }));
    }
  }, [metadata.invoiceNumber]);

  const setFormState = (parsed: any) => {
    if (parsed.client) setClient(parsed.client);
    if (parsed.items) setItems(parsed.items);
    if (parsed.totals) setTotals(parsed.totals);
    if (parsed.customTerms !== undefined) setCustomTerms(parsed.customTerms);
    if (parsed.internalNote !== undefined) setInternalNote(parsed.internalNote);
    if (parsed.settings) setSettings(parsed.settings);
    if (parsed.metadata) setMetadata(parsed.metadata);
  };

  const getIsBlank = (f: any) => {
    if (!f) return true;
    return f.items.length === 1 && !f.items[0].itemName && !f.client.name;
  };

  const { hasUnsavedChanges, clearDraft } = useDraftAndUnsaved(
    'niril_draft_invoice',
    initialForm,
    formState,
    setFormState,
    !!editingBill,
    getIsBlank
  );

  const handleSave = async () => {
    try {
      setSaving(true);
      const snapClient = { ...client };
      const snapItems = items.map(item => {
        const calculatedDiscountAmt = item.discountType === 'percentage' 
          ? (item.qty * item.rate) * ((item.discount || 0) / 100) 
          : (item.discount || 0);
        return {
          ...item,
          discount: calculatedDiscountAmt,
          rawDiscountValue: item.discount,
        };
      });
      
      // Need a valid invoice number. If empty, securely fetch the next valid one from template.
      let invNum = metadata.invoiceNumber;
      if (!invNum) {
        const prefix = (INVOICE_TYPES as any)[metadata.invoiceType]?.prefix || 'INV';
        invNum = await getNextInvoiceNumber(prefix);
      }
      
      const bill = {
        id: editingBill?.id || invNum,
        clientName: client[`name_${primaryLang}`] || '',
        clientNameEn: client[`name_${secondaryLang}`] || '',
        invoiceNumber: invNum,
        invoiceDate: metadata.date,
        invoiceType: metadata.invoiceType,
        currency: 'INR',
        totalAmount: totals.total,
        totalTaxAmount: totals.cgst + totals.sgst + totals.igst,
        status: 'paid', // Default to paid as per Legacy behavior
        paidAmount: Math.round(Number(totals.total)) || 0,
        payments: editingBill?.payments || [],
        data: { 
          profile: profileProp, 
          client: snapClient, 
          details: { ...metadata, invoiceNumber: invNum, invoiceDate: metadata.date }, 
          items: snapItems, 
          totals, 
          invoiceType: metadata.invoiceType, 
          customTerms, 
          internalNote, 
          invoiceOptions: settings
        }
      };
      
      await saveBill(bill);
      clearDraft();
      thagaval(t('hc_savedSuccessfully') || 'Invoice saved successfully!', 'success');
      if (onSaved) onSaved(bill);
    } catch (err) {
      console.error(err);
      thagaval('Failed to save invoice.', 'error');
    } finally {
      setSaving(false);
    }
  };

  return (
    <ElvanEditorLayout
      title={(editingBill ? (t('editInvoice') || 'Edit Invoice') : (t('newInvoice') || 'New Invoice')) as string}
      onBack={onBack}
      onSave={handleSave}
      saveButtonText={saving ? (t('saving') || 'Saving...') : (t('save') || 'Save')}
      hasUnsavedChanges={hasUnsavedChanges}
      onDiscard={() => {
        clearDraft();
        onBack();
      }}
      hideHeaderPortals={hideHeaderPortals}
    >
      <Box sx={{ display: 'flex', flexDirection: 'column', gap: 3 }}>
        
        {/* Client Selection Section */}
      <ClientSelection 
        client={client} 
        setClient={setClient} 
        isBilingual={isBilingual} 
        primaryLang={primaryLang} 
        secondaryLang={secondaryLang}
        profile={profileProp}
        invoiceOptions={settings}
        onRequestAddClient={onRequestAddClient}
        dataVersion={dataVersion}
      />

      {/* Metadata Section */}
      <InvoiceMetadata 
        metadata={metadata} 
        setMetadata={setMetadata} 
        isBilingual={isBilingual} 
        primaryLang={primaryLang}
        profile={profileProp}
        client={client}
        settings={settings}
      />

      {/* Line Items Section */}
      <LineItemsTable 
        items={items}
        setItems={setItems}
        settings={settings}
        setSettings={setSettings}
        isBilingual={isBilingual}
        primaryLang={primaryLang}
        secondaryLang={secondaryLang}
        profile={profileProp}
        onRequestAddProduct={onRequestAddProduct}
        dataVersion={dataVersion}
      />

      {/* Totals Section */}
        <InvoiceTotals
          items={items}
          totals={totals}
          setTotals={setTotals}
          settings={settings}
          client={client}
          profileState={profileProp?.maanilam}
          country={profileProp?.country}
        />



      {/* Invoice Type */}
      <Box sx={{ mb: 2 }}>
        <Box sx={{ display: 'flex', alignItems: 'center', mb: 2, ml: 2 }}>
          <Box sx={{ width: 24, height: 24, borderRadius: '50%', bgcolor: 'primary.main', color: 'primary.contrastText', display: 'flex', alignItems: 'center', justifyContent: 'center', fontSize: '0.8rem', fontWeight: 'bold', lineHeight: 1, pt: '1px', mr: 1.5 }}>5</Box>
          <Typography variant="h6" sx={{ fontSize: '1.1rem', fontWeight: 600 }}>{t("invoiceType")}</Typography>
        </Box>
        <Box sx={{ mb: 2 }}>
          <FormControl variant="filled" fullWidth size="small" sx={{ maxWidth: { sm: 400 } }}>
            <Select 
              value={metadata.invoiceType} 
              onChange={handleTypeChange}
            >
              {Object.entries(INVOICE_TYPES).map(([key, val]) => (
                <MenuItem key={key} value={key}>{t(`invoiceTypes_${key.replace(/-/g, '_')}` as any, { defaultValue: val.label })}</MenuItem>
              ))}
            </Select>
          </FormControl>
        </Box>
      </Box>

      </Box>
    </ElvanEditorLayout>
  );
}
