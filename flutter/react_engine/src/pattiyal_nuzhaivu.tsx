import React, { useEffect, useState } from 'react';
import { createRoot } from 'react-dom/client';
import { ThemeProvider, createTheme } from '@mui/material/styles';
import { LanguageProvider } from './mozhi/LanguageContext';
import CoolieInvoiceView from './pagudhigal/CoolieBill/CoolieInvoiceView';
import InvoiceView from './pagudhigal/GstBill/InvoiceView';
import './fonts.css';

// Minimal theme without CssBaseline to ensure transparency
const theme = createTheme({
  palette: {
    background: {
      default: 'transparent',
      paper: 'transparent'
    }
  }
});

function App() {
  const [bill, setBill] = useState({});
  const [isDark, setIsDark] = useState(false);
  const [invoiceType, setInvoiceType] = useState('COOLIE');

  useEffect(() => {
    // Read data safely from the Kotlin bridge
    if (typeof window !== 'undefined' && (window as any).FlutterBridge) {
      try {
        const invoiceData = (window as any).FlutterBridge.getInvoiceData();
        const profileData = (window as any).FlutterBridge.getProfileData();
        const parsedBill = JSON.parse(invoiceData);
        parsedBill._companyProfile = JSON.parse(profileData);
        
        const invType = (window as any).FlutterBridge.getInvoiceType ? (window as any).FlutterBridge.getInvoiceType() : 'COOLIE';
        setInvoiceType(invType);

        if (invType !== 'COOLIE') {
            // Transform Silk invoice flat format to nested React format expected by SjsTheme
            let items = [];
            try {
                items = typeof parsedBill.tharavugal === 'string' ? JSON.parse(parsedBill.tharavugal) : (parsedBill.tharavugal || []);
            } catch(e) {}
            
            let options = {};
            try {
                options = typeof parsedBill.sonthaViruppangal === 'string' ? JSON.parse(parsedBill.sonthaViruppangal) : (parsedBill.sonthaViruppangal || {});
            } catch(e) {}

            const mappedItems = items.map((it: any, i: number) => {
                const qty = Number(it.alavu) || 0;
                const rate = Number(it.vilai) || 0;
                const baseAmt = qty * rate;
                const thallupadi = Number(it.thallupadi) || 0;
                const discAmt = it.thallupadiVagai === '%' ? baseAmt * (thallupadi / 100) : thallupadi;

                return {
                    id: String(i),
                    name: it.porulPeyar || '',
                    name_Tamil: it.porulPeyar || '',
                    name_English: it.porulPeyarEn || '',
                    description: '',
                    qty: qty,
                    quantity: qty,
                    rate: rate,
                    unit: it.alagu || 'Nos',
                    hsn: it.hsnKuriyeedu || '',
                    taxPercent: Number(it.variVizhukkaadu) || 0,
                    discount: discAmt,
                };
            });

            // Reconstruct totals directly from Flutter's payload
            const total = Number(parsedBill.mothaThogai) || 0;
            const subtotal = Number(parsedBill.adippadaiMothangal) || 0;
            const totalDiscount = Number(parsedBill.thallupadiMothangal) || 0;
            const roundOff = Number(parsedBill.suttruOff) || 0;

            // Reconstruct client
            const client = parsedBill._client || {};

            parsedBill.data = {
                profile: parsedBill._companyProfile,
                client: client,
                details: {
                    invoiceNumber: parsedBill.patrucheettuEn || '',
                    invoiceDate: parsedBill.naal || '',
                    invoiceType: parsedBill.pattiyalVagai || 'tax-invoice',
                    placeOfSupply: options.placeOfSupply || client.maanilam || ''
                },
                items: mappedItems,
                totals: {
                    total: total,
                    subtotal: subtotal,
                    totalDiscount: totalDiscount,
                    roundOff: roundOff,
                    cgst: Number(parsedBill.cgst) || 0,
                    sgst: Number(parsedBill.sgst) || 0,
                    igst: Number(parsedBill.igst) || 0,
                    cess: Number(parsedBill.cess) || 0,
                    taxInclusive: false
                },
                invoiceType: parsedBill.pattiyalVagai || 'tax-invoice',
                customTerms: parsedBill.ullkurippu || '',
                invoiceOptions: options
            };
        } else {
            // Also handle the _client object that we attach from Flutter for Coolie if any
            if (parsedBill._client) {
               parsedBill.client = parsedBill._client;
            }
        }

        setBill(parsedBill);
        setIsDark((window as any).FlutterBridge.isDarkMode());
      } catch (e) {
        console.error("Failed to parse bridge data", e);
      }
    }
  }, []);

  return (
    <ThemeProvider theme={theme}>
      <LanguageProvider initialProfile={(bill as any)._companyProfile || {}}>
        <div style={{ padding: 0, margin: 0, background: 'transparent' }}>
          {invoiceType === 'COOLIE' ? (
            <CoolieInvoiceView 
              profile={bill?.data?.profile || {}}
              bill={bill} 
              onClose={() => {
                if (typeof window !== 'undefined' && (window as any).FlutterBridge && (window as any).FlutterBridge.closeInvoice) {
                  (window as any).FlutterBridge.closeInvoice();
                }
              }}
              onEdit={() => {}}
            />
          ) : (
            <InvoiceView 
              profile={bill?.data?.profile || {}}
              bill={bill} 
              onClose={() => {
                if (typeof window !== 'undefined' && (window as any).FlutterBridge && (window as any).FlutterBridge.closeInvoice) {
                  (window as any).FlutterBridge.closeInvoice();
                }
              }}
              onEdit={() => {}}
            />
          )}
        </div>
      </LanguageProvider>
    </ThemeProvider>
  );
}

const container = document.getElementById('root');
if (container) {
  const root = createRoot(container);
  root.render(<App />);
}
