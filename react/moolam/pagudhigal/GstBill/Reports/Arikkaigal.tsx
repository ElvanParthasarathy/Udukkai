// @ts-nocheck
import { TrendUp, TrendDown, Wallet, ChartBar, Clock, MagnifyingGlass, X } from '@phosphor-icons/react';
import { useState, useEffect } from 'react';
import { Box, Typography, Paper, Grid, TextField, InputAdornment, MenuItem, Table, TableBody, TableCell, TableContainer, TableHead, TableRow, Stack, Skeleton, useTheme } from '@mui/material';
import { getAllBills } from '../../../Avanam';
import { formatCurrency } from '../../../Payanpadu';
import ElvanCard from '../../ElvanCard';
import { thagaval } from '../../Thagaval';
import { useLanguage } from '../../../mozhi/LanguageContext';



function getFYOptions() {
  const now = new Date();
  const currentYear = now.getMonth() >= 3 ? now.getFullYear() : now.getFullYear() - 1;
  const options = [];
  for (let i = 0; i < 5; i++) {
    const y = currentYear - i;
    options.push({ value: `${y}-${y + 1}`, label: `FY ${y}-${String(y + 1).slice(-2)}`, from: `${y}-04-01`, to: `${y + 1}-03-31` });
  }
  return options;
}

const getBillCurrency = (b) => b.currency || b.data?.invoiceOptions?.currency || 'INR';

export default function Arikkaigal() {
  const { t } = useLanguage();
  const theme = useTheme();
  const isDark = theme.palette.mode === 'dark';
  const MONTHS = [t('january'), t('february'), t('march'), t('april'), t('may'), t('june'), t('july'), t('august'), t('september'), t('october'), t('november'), t('december')];

  const [bills, setBills] = useState([]);
  const [isLoading, setIsLoading] = useState(true);
  const [filterMode, setFilterMode] = useState('fy');
  const [fyFilter, setFyFilter] = useState('');
  const [monthFilter, setMonthFilter] = useState('');
  const [yearFilter, setYearFilter] = useState('');
  const [currencyFilter, setCurrencyFilter] = useState('INR');

  const fyOptions = getFYOptions();
  const currentYear = new Date().getFullYear();
  const yearOptions = [];
  for (let y = currentYear; y >= currentYear - 5; y--) yearOptions.push(y);

  useEffect(() => {
    let unsubs = [];

    const now = new Date();
    const fy = fyOptions[0];
    if (fy) setFyFilter(fy.value);
    setYearFilter(String(now.getFullYear()));
    setMonthFilter(String(now.getMonth()));

    const initRealtime = async () => {
      setIsLoading(true);
      try {
        const billData = await getAllBills((fresh) => setBills(fresh || []));
        if (billData && billData.unsubscribe) unsubs.push(billData.unsubscribe);
        setBills(billData || []);
      } catch {
        thagaval(t('failedToLoadProductsToast'), 'error');
      } finally {
        setIsLoading(false);
      }
    };

    initRealtime();

    return () => {
      unsubs.forEach(unsub => unsub());
    };
  }, []);

  const filterByPeriod = (date) => {
    if (!date) return false;
    if (filterMode === 'fy') {
      const fy = fyOptions.find(f => f.value === fyFilter);
      return fy ? date >= fy.from && date <= fy.to : true;
    } else {
      const d = new Date(date);
      return d.getFullYear() === parseInt(yearFilter) && d.getMonth() === parseInt(monthFilter);
    }
  };

  const allFilteredBills = bills.filter(bill => bill.data && filterByPeriod(bill.invoiceDate));

  // All distinct currencies in the filtered period
  const allCurrencies = [...new Set(allFilteredBills.map(getBillCurrency))].sort();
  // Set currency filter to first available if current selection not in list
  useEffect(() => {
    if (allCurrencies.length > 0 && !allCurrencies.includes(currencyFilter)) {
      setCurrencyFilter(allCurrencies[0]);
    }
  }, [allCurrencies.join(',')]);

  // Bills for P&L — only selected currency
  const plBills = allFilteredBills.filter(b => getBillCurrency(b) === currencyFilter);

  // P&L
  const totalRevenue = plBills.reduce((s, b) => s + (b.totalAmount || 0), 0);
  const totalTaxCollected = plBills.reduce((s, b) => s + (b.totalTaxAmount || 0), 0);
  const revenueExTax = totalRevenue - totalTaxCollected;

  // Monthly breakdown — per selected currency
  const monthlyPL = {};
  plBills.forEach(b => {
    if (!b.invoiceDate) return;
    const key = b.invoiceDate.substring(0, 7);
    if (!monthlyPL[key]) monthlyPL[key] = { revenue: 0, tax: 0 };
    monthlyPL[key].revenue += b.totalAmount || 0;
    monthlyPL[key].tax += b.totalTaxAmount || 0;
  });
  const monthlyKeys = Object.keys(monthlyPL).sort();


  return (
    <Box sx={{ pt: { xs: 3, md: 4 }, pb: { xs: 12, md: 4 }, px: { xs: 0, md: 4 }, maxWidth: 1200, mx: 'auto' }}>
      {/* Page Header (Hidden on Mobile) */}
      <Box sx={{ mb: 4, display: { xs: 'none', md: 'flex' }, justifyContent: 'space-between', alignItems: 'flex-start' }}>
        <Box>
          <Typography variant="h4" sx={{ fontWeight: 800, mb: 1 }}>
            {t('reportsTitle')}
          </Typography>
          <Typography variant="body1" color="text.secondary">
            {t('reportsSubtitle')}
          </Typography>
        </Box>
      </Box>

      {/* Period + Currency Selector - MOBILE (Unified Card) */}
      <Box sx={{ display: { xs: 'block', sm: 'none' }, mb: 4 }}>
        <ElvanCard boxSx={{ px: 3, pt: 2, pb: 2 }}>
          <Grid container spacing={2} sx={{ alignItems: 'center' }}>
            <Grid size={12}>
              <TextField 
                select 
                fullWidth
                label={t('filterByLabel') as string} 
                value={filterMode} 
                onChange={e => setFilterMode(e.target.value)}
                sx={{ '& .MuiFilledInput-root': { backgroundColor: isDark ? 'rgba(255,255,255,0.08)' : '#FFFFFF' } }}
              >
                <MenuItem value="fy">{t('fiscalYearLabel')}</MenuItem>
                <MenuItem value="month">{t('monthYearLabel')}</MenuItem>
              </TextField>
            </Grid>
            
            {filterMode === 'fy' ? (
              <Grid size={12}>
                <TextField 
                  select 
                  fullWidth
                  label={t('fiscalYearLabel') as string} 
                  value={fyFilter} 
                  onChange={e => setFyFilter(e.target.value)}
                  sx={{ '& .MuiFilledInput-root': { backgroundColor: isDark ? 'rgba(255,255,255,0.08)' : '#FFFFFF' } }}
                >
                  {fyOptions.map(fy => <MenuItem key={fy.value} value={fy.value}>{fy.label}</MenuItem>)}
                </TextField>
              </Grid>
            ) : (
              <>
                <Grid size={12}>
                  <TextField 
                    select 
                    fullWidth
                    label={t('monthLabel') as string} 
                    value={monthFilter} 
                    onChange={e => setMonthFilter(e.target.value)}
                    sx={{ '& .MuiFilledInput-root': { backgroundColor: isDark ? 'rgba(255,255,255,0.08)' : '#FFFFFF' } }}
                  >
                    {MONTHS.map((m, i) => <MenuItem key={i} value={i}>{m}</MenuItem>)}
                  </TextField>
                </Grid>
                <Grid size={12}>
                  <TextField 
                    select 
                    fullWidth
                    label={t('yearLabel') as string} 
                    value={yearFilter} 
                    onChange={e => setYearFilter(e.target.value)}
                    sx={{ '& .MuiFilledInput-root': { backgroundColor: isDark ? 'rgba(255,255,255,0.08)' : '#FFFFFF' } }}
                  >
                    {yearOptions.map(y => <MenuItem key={y} value={y}>{y}</MenuItem>)}
                  </TextField>
                </Grid>
              </>
            )}
            
            {allCurrencies.length > 1 && (
              <Grid size={12}>
                <TextField 
                  select 
                  fullWidth
                  label={t('currencyLabel') as string} 
                  value={currencyFilter} 
                  onChange={e => setCurrencyFilter(e.target.value)}
                  sx={{ '& .MuiFilledInput-root': { backgroundColor: isDark ? 'rgba(255,255,255,0.08)' : '#FFFFFF' } }}
                >
                  {allCurrencies.map(c => <MenuItem key={c} value={c}>{c}</MenuItem>)}
                </TextField>
              </Grid>
            )}
          </Grid>
        </ElvanCard>
      </Box>

      {/* Period + Currency Selector - DESKTOP (Split Cards) */}
      <Box sx={{ display: { xs: 'none', sm: 'flex' }, gap: 2, mb: 4, flexWrap: 'wrap', alignItems: 'center' }}>
        <TextField 
          select 
          label={t('filterByLabel') as string} 
          value={filterMode} 
          onChange={e => setFilterMode(e.target.value)}
          sx={{ width: 200 }}
        >
          <MenuItem value="fy">{t('fiscalYearLabel')}</MenuItem>
          <MenuItem value="month">{t('monthYearLabel')}</MenuItem>
        </TextField>
        
        {filterMode === 'fy' ? (
          <TextField 
            select 
            label={t('fiscalYearLabel') as string} 
            value={fyFilter} 
            onChange={e => setFyFilter(e.target.value)}
            sx={{ width: 200 }}
          >
            {fyOptions.map(fy => <MenuItem key={fy.value} value={fy.value}>{fy.label}</MenuItem>)}
          </TextField>
        ) : (
          <Box sx={{ display: 'flex', gap: 2 }}>
            <TextField 
              select 
              label={t('monthLabel') as string} 
              value={monthFilter} 
              onChange={e => setMonthFilter(e.target.value)}
              sx={{ width: 200 }}
            >
              {MONTHS.map((m, i) => <MenuItem key={i} value={i}>{m}</MenuItem>)}
            </TextField>
            <TextField 
              select 
              label={t('yearLabel') as string} 
              value={yearFilter} 
              onChange={e => setYearFilter(e.target.value)}
              sx={{ width: 200 }}
            >
              {yearOptions.map(y => <MenuItem key={y} value={y}>{y}</MenuItem>)}
            </TextField>
          </Box>
        )}
        
        {allCurrencies.length > 1 && (
          <TextField 
            select 
            label={t('currencyLabel') as string} 
            value={currencyFilter} 
            onChange={e => setCurrencyFilter(e.target.value)}
            sx={{ width: 200 }}
          >
            {allCurrencies.map(c => <MenuItem key={c} value={c}>{c}</MenuItem>)}
          </TextField>
        )}
      </Box>

      {/* P&L Metric Cards */}
      <Box sx={{ mb: 2, display: 'flex', justifyContent: 'space-between', alignItems: 'center', px: 2 }}>
        <Typography variant="subtitle1" sx={{ fontWeight: 700 }}>
          {t('salesSummaryTitle') === 'salesSummaryTitle' ? 'Sales Summary' : t('salesSummaryTitle')}
        </Typography>
        {allCurrencies.length > 1 && (
          <Typography variant="caption" color="text.secondary" sx={{ bgcolor: 'action.hover', px: 1.5, py: 0.5, borderRadius: '16px', fontWeight: 600 }}>
            {currencyFilter}
          </Typography>
        )}
      </Box>

      <Grid container spacing={2} sx={{ mb: 4 }}>
        <Grid size={{ xs: 12, sm: 4 }}>
          <ElvanCard sx={{ height: '100%' }} boxSx={{ display: 'flex', flexDirection: 'row', alignItems: 'center', gap: 2 }}>
            <Box sx={{ width: 48, height: 48, borderRadius: '16px', bgcolor: 'action.hover', color: 'text.primary', display: 'flex', alignItems: 'center', justifyContent: 'center', flexShrink: 0 }}>
              <Wallet size={24} weight="regular" />
            </Box>
            <Box>
              <Typography variant="body2" color="text.secondary" mb={0.5} sx={{ fontWeight: 600 }}>{t('totalRevenueLabel') || 'Gross Revenue'}</Typography>
              {isLoading ? (
                <Skeleton variant="text" width={100} height={32} />
              ) : (
                <Typography variant="h6" sx={{ fontWeight: 800, color: 'text.primary' }}>
                  {formatCurrency(totalRevenue, currencyFilter)}
                </Typography>
              )}
            </Box>
          </ElvanCard>
        </Grid>
        
        <Grid size={{ xs: 12, sm: 4 }}>
          <ElvanCard sx={{ height: '100%' }} boxSx={{ display: 'flex', flexDirection: 'row', alignItems: 'center', gap: 2 }}>
            <Box sx={{ width: 48, height: 48, borderRadius: '16px', bgcolor: 'action.hover', color: 'text.primary', display: 'flex', alignItems: 'center', justifyContent: 'center', flexShrink: 0 }}>
              <TrendDown size={24} weight="regular" />
            </Box>
            <Box>
              <Typography variant="body2" color="text.secondary" mb={0.5} sx={{ fontWeight: 600 }}>{t('lessGstCollectedLabel') || 'Tax Collected'}</Typography>
              {isLoading ? (
                <Skeleton variant="text" width={100} height={32} />
              ) : (
                <Typography variant="h6" sx={{ fontWeight: 800, color: 'text.primary' }}>
                  {formatCurrency(totalTaxCollected, currencyFilter)}
                </Typography>
              )}
            </Box>
          </ElvanCard>
        </Grid>

        <Grid size={{ xs: 12, sm: 4 }}>
          <ElvanCard sx={{ height: '100%' }} boxSx={{ display: 'flex', flexDirection: 'row', alignItems: 'center', gap: 2 }}>
            <Box sx={{ width: 48, height: 48, borderRadius: '16px', bgcolor: 'action.hover', color: 'text.primary', display: 'flex', alignItems: 'center', justifyContent: 'center', flexShrink: 0 }}>
              <TrendUp size={24} weight="regular" />
            </Box>
            <Box>
              <Typography variant="body2" color="text.secondary" mb={0.5} sx={{ fontWeight: 600 }}>{t('netRevenueLabel') || 'Net Revenue'}</Typography>
              {isLoading ? (
                <Skeleton variant="text" width={100} height={32} />
              ) : (
                <Typography variant="h6" sx={{ fontWeight: 800, color: 'text.primary' }}>
                  {formatCurrency(revenueExTax, currencyFilter)}
                </Typography>
              )}
            </Box>
          </ElvanCard>
        </Grid>
      </Grid>

      {/* Monthly Breakdown Cards */}
      {monthlyKeys.length > 0 && (
        <>
          <Box sx={{ mb: 2, px: 2 }}>
            <Typography variant="subtitle1" sx={{ fontWeight: 700 }}>
              {t('monthlySalesTitle') === 'monthlySalesTitle' ? 'Monthly Breakdown' : t('monthlySalesTitle')}
            </Typography>
          </Box>
          <Grid container spacing={2}>
            {monthlyKeys.map(key => {
              const m = monthlyPL[key];
              const rev = m.revenue - m.tax;
              const [y, mo] = key.split('-');
              return (
                <Grid size={{ xs: 12, sm: 6, md: 4 }} key={key}>
                  <ElvanCard sx={{ height: '100%' }} boxSx={{ display: 'flex', flexDirection: 'row', alignItems: 'center', gap: 2 }}>
                    <Box sx={{ width: 48, height: 48, borderRadius: '16px', bgcolor: 'action.hover', color: 'text.primary', display: 'flex', alignItems: 'center', justifyContent: 'center', flexShrink: 0 }}>
                      <ChartBar size={24} weight="regular" />
                    </Box>
                    <Box>
                      <Typography variant="body2" color="text.secondary" mb={0.5} sx={{ fontWeight: 600 }}>
                        {MONTHS[parseInt(mo) - 1]} {y}
                      </Typography>
                      <Typography variant="h6" sx={{ fontWeight: 800, color: 'text.primary' }}>
                        {formatCurrency(rev, currencyFilter)}
                      </Typography>
                    </Box>
                  </ElvanCard>
                </Grid>
              );
            })}
          </Grid>
        </>
      )}

    </Box>
  );
}
