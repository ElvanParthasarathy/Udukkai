// @ts-nocheck
import { CurrencyInr, Receipt, TrendUp, Package, CaretRight, Invoice } from '@phosphor-icons/react';
import { useState, useEffect } from 'react';
import { getAllBills, getAllProducts, getAllClients } from '../../Avanam';
import { formatCurrency, INVOICE_TYPES, getDynamicField } from '../../Payanpadu';
import { thagaval } from '../Thagaval';
import { useLanguage } from '../../mozhi/LanguageContext';
import {
  Box, Typography, Button, Card, CardContent,
  Table, TableBody, TableCell, TableContainer,
  TableHead, TableRow, Paper, Chip, Stack,
  useTheme, Divider, Avatar, Skeleton
} from '@mui/material';
import ElvanCard from '../ElvanCard';

import MugappuLayout from '../MugappuLayout';

export default function Mugappu({ onViewAll, onNew, onEdit, onDuplicate, onConvert, profile, onSwitchModeRequest }: any) {
  const { t, language } = useLanguage();
  const theme = useTheme();
  const isDark = theme.palette.mode === 'dark';

  const [bills, setBills] = useState([]);
  const [stats, setStats] = useState({ byCurrency: {}, count: 0 });
  const [isLoading, setIsLoading] = useState(true);

  useEffect(() => {
    let unsub = null;

    const initRealtime = async () => {
      setIsLoading(true);
      try {
        const handleSetData = (data) => {
          setBills(data);
          const byCurrency = {};
          for (const b of data || []) {
            const cur = b.currency || b.data?.invoiceOptions?.currency || 'INR';
            if (!byCurrency[cur]) byCurrency[cur] = { total: 0, tax: 0 };
            byCurrency[cur].total += b.totalAmount || 0;
            byCurrency[cur].tax += b.totalTaxAmount || 0;
          }
          setStats({ byCurrency, count: (data || []).length });
        };
        
        const data = await getAllBills(handleSetData);
        if (data && data.unsubscribe) unsub = data.unsubscribe;
        handleSetData(data || []);
      } catch {
        thagaval('Failed to load invoices', 'error');
      } finally {
        setIsLoading(false);
      }
    };

    initRealtime();

    return () => {
      if (unsub) unsub();
    };
  }, []);

  const handleView = (bill) => {
    if (bill.data) onEdit(bill);
    else thagaval('No editable data saved for this invoice', 'warning');
  };

  const recentBills = [...bills]
    .sort((a, b) => new Date(b.invoiceDate).getTime() - new Date(a.invoiceDate).getTime())
    .slice(0, 6);

  const customSparkle = (
    <Box component="span" sx={{ display: 'inline-flex', alignItems: 'center', ml: 0.5, mt: -0.5 }}>
      <svg xmlns="http://www.w3.org/2000/svg" height="20px" viewBox="0 0 24 24" width="20px" fill="#FFC107" style={{ transform: 'scaleX(-1)' }}>
        <path d="M0 0h24v24H0z" fill="none"/>
        <path d="M19 9l1.25-2.75L23 5l-2.75-1.25L19 1l-1.25 2.75L15 5l2.75 1.25L19 9zm-7.5.5L9 4 6.5 9.5 1 12l5.5 2.5L9 20l2.5-5.5L17 12l-5.5-2.5zM19 15l-1.25 2.75L15 19l2.75 1.25L19 23l1.25-2.75L23 19l-2.75-1.25L19 15z"/>
      </svg>
    </Box>
  );

  const greetingTitle = (
    <Box component="span" sx={{ display: 'inline-flex', alignItems: 'center' }}>
      {t('vanakkamTitle')}{customSparkle}
    </Box>
  );
  const greetingSubtitle = t('nirilSilk');
  const profileInitial = <Invoice weight="fill" size={24} />;

  const statsCards = (
    <>
      <Card sx={{ borderRadius: '24px', boxShadow: 'none', border: 'none', display: 'flex', flexDirection: 'column', bgcolor: isDark ? 'rgba(255,255,255,0.03)' : '#FFFFFF', userSelect: 'none' }}>
        <CardContent sx={{ display: 'flex', flexDirection: { xs: 'column', sm: 'row' }, alignItems: 'flex-start', p: { xs: 2, sm: 3 }, flexGrow: 1 }}>
          <Box sx={{ width: 48, height: 48, display: 'flex', alignItems: 'center', justifyContent: 'center', flexShrink: 0, borderRadius: '16px', bgcolor: isDark ? 'rgba(255,255,255,0.05)' : '#f5f5f5', color: 'text.primary', mr: { xs: 0, sm: 2 }, mb: { xs: 1.5, sm: 0 } }}>
            <CurrencyInr size={24} weight="regular" />
          </Box>
          <Box>
            <Typography variant="body2" color="text.secondary" mb={0.5} sx={{ fontWeight: 600 }}>{t('totalInvoiced')}</Typography>
            {isLoading ? (
              <Skeleton variant="text" width={120} height={32} />
            ) : Object.keys(stats.byCurrency).length === 0 ? (
              <Typography variant="h5" color="text.secondary" sx={{ fontWeight: 800 }}>—</Typography>
            ) : (
              Object.entries(stats.byCurrency).map(([cur, v]) => (
                <Typography key={cur} variant="h5" color="text.primary" sx={{ fontWeight: 800, fontSize: { xs: formatCurrency(v.total, cur).length > 14 ? '0.95rem' : formatCurrency(v.total, cur).length > 11 ? '1.15rem' : '1.5rem', sm: '1.5rem' }, whiteSpace: 'nowrap', overflow: 'hidden', textOverflow: 'ellipsis', lineHeight: 1.2, width: '100%', display: 'block' }}>
                  {formatCurrency(v.total, cur)}
                </Typography>
              ))
            )}
          </Box>
        </CardContent>
      </Card>
      
      <Card sx={{ borderRadius: '24px', boxShadow: 'none', border: 'none', display: 'flex', flexDirection: 'column', bgcolor: isDark ? 'rgba(255,255,255,0.03)' : '#FFFFFF', userSelect: 'none' }}>
        <CardContent sx={{ display: 'flex', flexDirection: { xs: 'column', sm: 'row' }, alignItems: 'flex-start', p: { xs: 2, sm: 3 }, flexGrow: 1 }}>
          <Box sx={{ width: 48, height: 48, display: 'flex', alignItems: 'center', justifyContent: 'center', flexShrink: 0, borderRadius: '16px', bgcolor: isDark ? 'rgba(255,255,255,0.05)' : '#f5f5f5', color: 'text.primary', mr: { xs: 0, sm: 2 }, mb: { xs: 1.5, sm: 0 } }}>
            <TrendUp size={24} weight="regular" />
          </Box>
          <Box>
            <Typography variant="body2" color="text.secondary" mb={0.5} sx={{ fontWeight: 600 }}>{t('taxCollected')}</Typography>
            {isLoading ? (
              <Skeleton variant="text" width={120} height={32} />
            ) : Object.keys(stats.byCurrency).length === 0 ? (
              <Typography variant="h5" color="text.secondary" sx={{ fontWeight: 800 }}>—</Typography>
            ) : (
              Object.entries(stats.byCurrency).map(([cur, v]) => (
                <Typography key={cur} variant="h5" color="text.primary" sx={{ fontWeight: 800, fontSize: { xs: formatCurrency(v.tax, cur).length > 14 ? '0.95rem' : formatCurrency(v.tax, cur).length > 11 ? '1.15rem' : '1.5rem', sm: '1.5rem' }, whiteSpace: 'nowrap', overflow: 'hidden', textOverflow: 'ellipsis', lineHeight: 1.2, width: '100%', display: 'block' }}>
                  {formatCurrency(v.tax, cur)}
                </Typography>
              ))
            )}
          </Box>
        </CardContent>
      </Card>

      {/* Third Card: Invoice Count (Desktop Only) */}
      <Card sx={{ display: { xs: 'none', md: 'flex' }, borderRadius: '24px', boxShadow: 'none', border: 'none', flexDirection: 'column', bgcolor: isDark ? 'rgba(255,255,255,0.03)' : '#FFFFFF', userSelect: 'none' }}>
        <CardContent sx={{ display: 'flex', flexDirection: { xs: 'column', sm: 'row' }, alignItems: 'flex-start', p: { xs: 2, sm: 3 }, flexGrow: 1 }}>
          <Box sx={{ width: 48, height: 48, display: 'flex', alignItems: 'center', justifyContent: 'center', flexShrink: 0, borderRadius: '16px', bgcolor: isDark ? 'rgba(255,255,255,0.05)' : '#f5f5f5', color: 'text.primary', mr: { xs: 0, sm: 2 }, mb: { xs: 1.5, sm: 0 } }}>
            <Receipt size={24} weight="regular" />
          </Box>
          <Box>
            <Typography variant="body2" color="text.secondary" mb={0.5} sx={{ fontWeight: 600 }}>
              {t('invoiceCount')}
            </Typography>
            {isLoading ? (
              <Skeleton variant="text" width={60} height={32} />
            ) : (
              <Typography variant="h5" color="text.primary" sx={{ fontWeight: 800 }}>
                {stats.count}
              </Typography>
            )}
          </Box>
        </CardContent>
      </Card>
    </>
  );

  const renderRecentItem = (bill, index) => {
    const billCurrency = bill.currency || bill.data?.invoiceOptions?.currency || 'INR';
    return (
      <ElvanCard key={bill.id} onClick={() => handleView(bill)}>
        <Box sx={{ display: 'flex', gap: 1.5, alignItems: 'flex-start' }}>
          <Box sx={{ 
            display: 'flex', alignItems: 'center', justifyContent: 'center', 
            width: 28, height: 28, mt: 0.15, 
            borderRadius: '50%',
            bgcolor: isDark ? 'rgba(255,255,255,0.12)' : 'rgba(0,0,0,0.08)',
            flexShrink: 0
          }}>
            <Typography variant="caption" sx={{ fontWeight: 800, color: isDark ? '#FFFFFF' : '#000000', fontSize: '0.7rem', lineHeight: 1, position: 'relative', top: '1px' }}>
              {(index + 1).toString().padStart(2, '0')}
            </Typography>
          </Box>
          <Box sx={{ minWidth: 0, flex: 1, display: 'flex', flexDirection: 'column' }}>
            <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', gap: 1 }}>
              <Typography variant="subtitle1" noWrap sx={{ fontWeight: 700, fontSize: '0.95rem', flex: 1, minWidth: 0 }}>
                {bill.clientName || '-'}
              </Typography>
              <CaretRight size={18} weight="regular" color={isDark ? "#555" : "#aaa"} style={{ marginTop: '2px', flexShrink: 0 }} />
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
                {(INVOICE_TYPES[bill.invoiceType || 'tax-invoice'])?.label}
              </Typography>
              <Typography variant="subtitle2" noWrap sx={{ fontWeight: 800, color: 'primary.main', fontSize: formatCurrency(bill.totalAmount, billCurrency).length > 11 ? '0.8rem' : '0.95rem', flexShrink: 0 }}>
                {formatCurrency(bill.totalAmount, billCurrency)}
              </Typography>
            </Box>
          </Box>
        </Box>
      </ElvanCard>
    );
  };

  return (
    <MugappuLayout 
      title={t('dashboard')}
      greetingTitle={greetingTitle}
      greetingSubtitle={greetingSubtitle}
      profileAvatar={profile?.logo}
      profileInitial={profileInitial}
      onNew={onNew}
      onViewAll={onViewAll}
      statsCards={statsCards}
      recentActivityTitle={t('recentActivity')}
      recentItems={recentBills}
      isLoading={isLoading}
      emptyStateText={t('noInvoicesYet')}
      renderRecentItem={renderRecentItem}
      onProfileClick={onSwitchModeRequest}
    />
  );
}
