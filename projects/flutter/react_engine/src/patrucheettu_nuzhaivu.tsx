import React, { useEffect, useState } from 'react';
import { createRoot } from 'react-dom/client';
import { ThemeProvider, createTheme } from '@mui/material/styles';
import { LanguageProvider } from './mozhi/LanguageContext';
import SharedReceiptView from './pagudhigal/Shared/SharedReceiptView';
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
  const [receipt, setReceipt] = useState({});
  const [isDark, setIsDark] = useState(false);
  const [receiptType, setReceiptType] = useState('GST');

  useEffect(() => {
    // Read data safely from the Kotlin bridge
    if (typeof window !== 'undefined' && (window as any).FlutterBridge) {
      try {
        const receiptData = (window as any).FlutterBridge.getReceiptData();
        const profileData = (window as any).FlutterBridge.getProfileData();
        const parsedReceipt = JSON.parse(receiptData);
        parsedReceipt._companyProfile = JSON.parse(profileData);
        
        const recType = (window as any).FlutterBridge.getReceiptType ? (window as any).FlutterBridge.getReceiptType() : 'GST';
        setReceiptType(recType);

        if (recType !== 'COOLIE') {
            // Map Silk receipt properties to the flat structure used by SharedReceiptView
            const client = parsedReceipt._client || {};
            
            parsedReceipt.receiptNo = parsedReceipt.patrucheettuEn || '';
            parsedReceipt.date = parsedReceipt.naal || '';
            parsedReceipt.clientName = client.peyar || client.name || '';
            parsedReceipt.clientNameEn = client.peyarEn || client.nameEn || client.name_English || '';
            parsedReceipt.amount = Number(parsedReceipt.mothaThogai) || Number(parsedReceipt.thogai) || Number(parsedReceipt.perappattaThogai) || 0;
            
            let options = {};
            try {
                options = typeof parsedReceipt.sonthaViruppangal === 'string' ? JSON.parse(parsedReceipt.sonthaViruppangal) : (parsedReceipt.sonthaViruppangal || {});
            } catch(e) {}
            
            parsedReceipt.paymentMode = options.paymentMode || parsedReceipt.seluthumMurai || 'Cash';
            parsedReceipt.referenceNo = options.referenceNo || '';
            parsedReceipt.againstInvoice = options.againstInvoice || parsedReceipt.pattiyalEn || '';
            parsedReceipt.note = parsedReceipt.ullkurippu || '';
            parsedReceipt.isSilk = true; // Flag for bilingual mode
        } else {
            if (parsedReceipt._client) {
               parsedReceipt.client = parsedReceipt._client;
            }
        }

        setReceipt(parsedReceipt);
        setIsDark((window as any).FlutterBridge.isDarkMode());
      } catch (e) {
        console.error("Failed to parse bridge data", e);
      }
    } else {
      console.warn("FlutterBridge not found.");
    }
  }, []);

  return (
    <ThemeProvider theme={theme}>
      <LanguageProvider initialProfile={(receipt as any)._companyProfile || {}}>
        <div style={{ padding: 0, margin: 0, background: 'transparent' }}>
          <SharedReceiptView
            receipt={receipt}
            profile={(receipt as any)._companyProfile || {}}
            isDark={isDark}
          />
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
