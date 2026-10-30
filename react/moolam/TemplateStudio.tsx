// @ts-nocheck
import React, { useState } from 'react';
import { Box, Paper, Typography, Button, ButtonGroup, Switch, FormControlLabel, Slider } from '@mui/material';
import SjsTheme from './pagudhigal/GstBill/SjsTheme';
import CoolieInvoiceBilingual from './pagudhigal/CoolieBill/CoolieInvoiceBilingual';
import ReceiptView from './pagudhigal/GstBill/Receipts/ReceiptView';
import CoolieReceiptView from './pagudhigal/CoolieBill/CoolieReceiptView';
import './vadivam.css';
import './pagudhigal/CoolieBill/print.css';

// Realistic sample data for Silk / GST Invoice (Textiles)
const sampleGstData = {
  profile: {
    niruvanathinPeyar: 'ஸ்ரீ ஜெய்பிரியா சில்க்ஸ்',
    niruvanathinPeyarEn: 'Sri Jaipriya Silks',
    tagline_Tamil: 'கைத்தறி பட்டு சேலைகள் & ராசில்க்',
    tagline_English: 'Handloom Silk Sarees & Rawsilk',
    mugavari: '4/606, முதல் தெரு, சிவசக்தி நகர்',
    mugavariEn: '4/606, First Street, Sivasakthi Nagar',
    oor: 'ஆரணி',
    oorEn: 'Arani',
    maavattam: 'திருவண்ணாமலை மாவட்டம்',
    maavattamEn: 'Tiruvannamalai District',
    maanilam: 'Tamil Nadu',
    maanilamEn: 'Tamil Nadu',
    country: 'India',
    gstin: '33ASSPV0378E1ZD',
    email: 'srijaipriyasilks@gmail.com',
    tholaipesi: '9360779191',
    mobileNumber: '8144604797',
    themeColor: '#388e3c',
    vangiPeyar: 'தமிழ்நாடு மெர்கன்டைல் வங்கி',
    vangiPeyarEn: 'Tamilnad Mercantile Bank',
    bankBranch: 'ஆரணி',
    bankBranchEn: 'Arani Branch',
    kanakkuEn: '309150050800162',
    ifsc: 'TMBL0000309',
    upiId: '9360779191@okbizaxis',
    authorizedSignatoryName: 'S. Parthasarathy',
    enableBilingual: true,
    primaryDataLanguage: 'Tamil',
    secondaryDataLanguage: 'English',
    billHeaderStyle: 'small',
  },
  client: {
    name: 'ஸ்ரீ சிவராம் சில்க் சாரீஸ்',
    nameEn: 'Sri Sivaram Silk Sarees',
    mugavari: '12, காந்தி ரோடு',
    mugavariEn: '12, Gandhi Road',
    oor: 'காஞ்சிபுரம்',
    oorEn: 'Kanchipuram',
    maanilam: 'Tamil Nadu',
    maanilamEn: 'Tamil Nadu',
    country: 'India',
    gstin: '33AABCS1234H1Z5',
    tholaipesi: '9842312345',
  },
  details: {
    invoiceNumber: 'SJPS-29',
    invoiceDate: '2026-06-11',
    invoiceType: 'tax-invoice',
    placeOfSupply: 'Tamil Nadu',
  },
  items: [
    { id: '1', name: 'ஆரணி பட்டு புடவை', name_Tamil: 'ஆரணி பட்டு புடவை', name_English: 'Arani Pure Silk Saree', hsn: '5007', quantity: 15, qty: 15, unit: 'Nos', rate: 5200, price: 5200, taxPercent: 5, discount: 0 },
    { id: '2', name: 'காஞ்சிபுரம் திருமண பட்டு', name_Tamil: 'காஞ்சிபுரம் திருமண பட்டு', name_English: 'Kanchipuram Bridal Silk', hsn: '5007', quantity: 8, qty: 8, unit: 'Nos', rate: 7400, price: 7400, taxPercent: 5, discount: 500 },
    { id: '3', name: 'சாஃப்ட் சில்க் சேலை', name_Tamil: 'சாஃப்ட் சில்க் சேலை', name_English: 'Soft Silk Lightweight', hsn: '5007', quantity: 10, qty: 10, unit: 'Nos', rate: 3100, price: 3100, taxPercent: 5, discount: 0 },
  ],
  totals: {
    subtotal: 167700,
    totalDiscount: 500,
    cgst: 4180,
    sgst: 4180,
    igst: 0,
    roundOff: 0,
    total: 175560,
  },
  invoiceType: 'tax-invoice',
  options: {
    showGST: true,
    showHSN: true,
    showBankDetails: true,
    showUPI: true,
    showSignature: true,
    showTerms: true,
    showState: true,
    showGSTIN: true,
  }
};

// Realistic sample data for Coolie / Factory Invoice
const sampleCoolieData = {
  _companyProfile: {
    niruvanathinPeyar: 'பி.வி.எஸ். சில்க் டுவிஸ்டிங்',
    niruvanathinPeyarEn: 'P.V.S. Silk Twisting',
    tagline_Tamil: 'பட்டு நூல் முறுக்கு ஆலை',
    tagline_English: 'Silk Yarn Twisting Factory',
    mugavari: '15, பாரதியார் தெரு',
    mugavariEn: '15, Bharathiyar Street',
    oor: 'ஆரணி',
    oorEn: 'Arani',
    tholaipesi: '9842100000',
    themeColor: '#0369a1',
    vangiPeyar: 'இந்தியன் வங்கி',
    bankNameEn: 'Indian Bank',
    branchEn: 'Arani Branch',
    account_no: '50012345678',
    ifsc: 'IDIB000A012',
    authorizedSignatoryName: 'P. V. Sampath',
    enableBilingual: true,
    primaryDataLanguage: 'Tamil',
    secondaryDataLanguage: 'English',
  },
  bill_no: 'PVS-24',
  date: '12/06/2026',
  customer_name: 'சுந்தரி சில்க்ஸ் இந்தியா',
  customer_name_en: 'Sundari Silks India',
  address: '24, தியாகராய நகர்',
  address_en: '24, T. Nagar',
  city: 'சென்னை',
  city_en: 'Chennai',
  items: [
    { id: '1', porul: 'ரா சில்க் 20/22 வார்ப்', item_name_en: 'Raw Silk 20/22 Warp', coolie: 320, kg: '45.250' },
    { id: '2', porul: 'கோரா சில்க் வெஃப்ட்', item_name_en: 'Kora Silk Weft', coolie: 280, kg: '32.100' },
  ],
  setharam_grams: '450',
  courier_rs: '150',
  ahimsa_silk_rs: '0',
  custom_charge_name: 'பாக்கிங் கட்டணம் / Packing',
  custom_charge_rs: '200',
  show_bank_details: true,
  show_ifsc: true,
};

// Realistic sample receipt data
const sampleReceiptData = {
  receipt: {
    receiptNo: 'RCP-104',
    date: '16/06/2026',
    clientName: 'சுந்தரி சில்க்ஸ் இந்தியா',
    clientNameEn: 'Sundari Silks India',
    amount: 50000,
    paymentMode: 'Bank Transfer (NEFT)',
    againstInvoice: 'PVS-24',
    notes: 'Part payment for June delivery',
  },
  profile: {
    niruvanathinPeyar: 'பி.வி.எஸ். சில்க் டுவிஸ்டிங்',
    niruvanathinPeyarEn: 'P.V.S. Silk Twisting',
    themeColor: '#0369a1',
    primaryDataLanguage: 'Tamil',
    secondaryDataLanguage: 'English',
    enableBilingual: true,
    tholaipesi: '9842100000',
    email: 'pvssilktwisting@gmail.com',
    mugavari: '15, பாரதியார் தெரு',
    mugavariEn: '15, Bharathiyar Street',
    oor: 'ஆரணி',
    oorEn: 'Arani',
    authorizedSignatoryName: 'P. V. Sampath',
  }
};

export default function TemplateStudio() {
  const [template, setTemplate] = useState('gst-invoice');
  const [isBilingual, setIsBilingual] = useState(true);
  const [scale, setScale] = useState(1);
  const [accentColor, setAccentColor] = useState('#388e3c');

  // Compute live data with bilingual switch
  const gstProps = {
    ...sampleGstData,
    profile: {
      ...sampleGstData.profile,
      themeColor: accentColor,
      enableBilingual: isBilingual,
    }
  };

  const coolieProps = {
    bill: {
      ...sampleCoolieData,
      _companyProfile: {
        ...sampleCoolieData._companyProfile,
        themeColor: accentColor,
        enableBilingual: isBilingual,
      }
    }
  };

  const receiptProps = {
    receipt: sampleReceiptData.receipt,
    profile: {
      ...sampleReceiptData.profile,
      themeColor: accentColor,
      enableBilingual: isBilingual,
    },
    onBack: () => {},
    onEdit: () => {}
  };

  return (
    <Box sx={{ minHeight: '100vh', bgcolor: '#121212', color: '#fff', display: 'flex', flexDirection: 'column' }}>
      {/* Top Floating Control Bar */}
      <Paper
        elevation={8}
        sx={{
          position: 'sticky',
          top: 0,
          zIndex: 9999,
          bgcolor: 'rgba(24, 24, 27, 0.95)',
          backdropFilter: 'blur(16px)',
          borderBottom: '1px solid rgba(255,255,255,0.1)',
          p: 1.5,
          display: 'flex',
          flexWrap: 'wrap',
          gap: 2,
          alignItems: 'center',
          justifyContent: 'space-between',
        }}
      >
        <Box sx={{ display: 'flex', alignItems: 'center', gap: 2 }}>
          <Typography sx={{ fontWeight: 800, fontSize: '15px', color: '#fff', letterSpacing: '0.5px' }}>
            🎨 TEMPLATE STUDIO (Live Watcher)
          </Typography>

          <ButtonGroup variant="outlined" size="small">
            <Button
              onClick={() => { setTemplate('gst-invoice'); setAccentColor('#388e3c'); }}
              variant={template === 'gst-invoice' ? 'contained' : 'outlined'}
              sx={{ textTransform: 'none', fontWeight: 600 }}
            >
              👘 Silk / GST Invoice (SjsTheme)
            </Button>
            <Button
              onClick={() => { setTemplate('coolie-invoice'); setAccentColor('#0369a1'); }}
              variant={template === 'coolie-invoice' ? 'contained' : 'outlined'}
              sx={{ textTransform: 'none', fontWeight: 600 }}
            >
              🧵 Coolie Invoice (Bilingual)
            </Button>
            <Button
              onClick={() => { setTemplate('gst-receipt'); setAccentColor('#388e3c'); }}
              variant={template === 'gst-receipt' ? 'contained' : 'outlined'}
              sx={{ textTransform: 'none', fontWeight: 600 }}
            >
              🧾 GST Receipt
            </Button>
            <Button
              onClick={() => { setTemplate('coolie-receipt'); setAccentColor('#0369a1'); }}
              variant={template === 'coolie-receipt' ? 'contained' : 'outlined'}
              sx={{ textTransform: 'none', fontWeight: 600 }}
            >
              📑 Coolie Receipt
            </Button>
          </ButtonGroup>
        </Box>

        <Box sx={{ display: 'flex', alignItems: 'center', gap: 2 }}>
          <FormControlLabel
            control={
              <Switch
                checked={isBilingual}
                onChange={(e) => setIsBilingual(e.target.checked)}
                color="primary"
                size="small"
              />
            }
            label={<Typography sx={{ fontSize: '13px', color: '#ccc' }}>Bilingual Mode</Typography>}
          />

          <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
            <Typography sx={{ fontSize: '12px', color: '#aaa' }}>Accent:</Typography>
            {['#388e3c', '#0369a1', '#7c3aed', '#b91c1c', '#d97706'].map((c) => (
              <Box
                key={c}
                onClick={() => setAccentColor(c)}
                sx={{
                  width: 20,
                  height: 20,
                  borderRadius: '50%',
                  bgcolor: c,
                  cursor: 'pointer',
                  border: accentColor === c ? '2px solid #fff' : '2px solid transparent',
                  transition: 'transform 0.15s',
                  '&:hover': { transform: 'scale(1.2)' }
                }}
              />
            ))}
          </Box>

          <Box sx={{ display: 'flex', alignItems: 'center', gap: 1, minWidth: 140 }}>
            <Typography sx={{ fontSize: '12px', color: '#aaa' }}>Zoom:</Typography>
            <Slider
              value={scale}
              min={0.5}
              max={1.3}
              step={0.05}
              onChange={(_, v) => setScale(v as number)}
              size="small"
              sx={{ width: 80 }}
            />
            <Typography sx={{ fontSize: '11px', color: '#fff', width: 32 }}>{Math.round(scale * 100)}%</Typography>
          </Box>
        </Box>
      </Paper>

      {/* Main Canvas with A4 Paper */}
      <Box
        sx={{
          flexGrow: 1,
          p: 4,
          overflow: 'auto',
          display: 'flex',
          justifyContent: 'center',
          alignItems: 'flex-start',
          bgcolor: '#18181b',
        }}
      >
        <Box
          sx={{
            transform: `scale(${scale})`,
            transformOrigin: 'top center',
            transition: 'transform 0.1s ease-out',
            boxShadow: '0 25px 50px -12px rgba(0, 0, 0, 0.7)',
            borderRadius: '4px',
            bgcolor: '#ffffff',
          }}
        >
          {template === 'gst-invoice' && (
            <Paper elevation={0} className="invoice-paper" sx={{ width: '210mm', minHeight: '297mm', bgcolor: '#fff', p: 0, m: 0 }}>
              <SjsTheme {...gstProps} />
            </Paper>
          )}

          {template === 'coolie-invoice' && (
            <Paper elevation={0} className="invoice-paper" sx={{ width: '210mm', minHeight: '297mm', bgcolor: '#fff', p: 0, m: 0 }}>
              <CoolieInvoiceBilingual {...coolieProps} />
            </Paper>
          )}

          {template === 'gst-receipt' && (
            <Paper elevation={0} className="invoice-paper" sx={{ width: '210mm', bgcolor: '#fff', p: 0, m: 0 }}>
              <ReceiptView {...receiptProps} />
            </Paper>
          )}

          {template === 'coolie-receipt' && (
            <Paper elevation={0} className="invoice-paper" sx={{ width: '210mm', bgcolor: '#fff', p: 0, m: 0 }}>
              <CoolieReceiptView {...receiptProps} />
            </Paper>
          )}
        </Box>
      </Box>
    </Box>
  );
}
