import React, { useState, useEffect } from 'react';
import { useLanguage } from '../../mozhi/LanguageContext';
import { Box, Typography, Card, CardContent, Stack, useTheme, Avatar, Skeleton } from '@mui/material';
import { CurrencyInr, Receipt, TrendUp, CaretRight, Storefront, HandCoins } from '@phosphor-icons/react';
import { getAllCoolieBills, getAllCoolieProfiles } from '../../Avanam';
import { formatCurrency } from '../../Payanpadu';
import { thagaval } from '../Thagaval';
import MugappuLayout from '../MugappuLayout';
import ElvanCard from '../ElvanCard';

export default function CoolieDashboard({ onViewAll, onNew, onView, onSwitchModeRequest }: any) {
  const { t, language } = useLanguage();
  const theme = useTheme();
  const isDark = theme.palette.mode === 'dark';

  const [bills, setBills] = useState([]);
  const [profiles, setProfiles] = useState([]);
  const [stats, setStats] = useState({ overallTotal: 0, byCompany: {}, count: 0 });
  const [isLoading, setIsLoading] = useState(true);

  useEffect(() => {
    let unsubs = [];
    
    const initRealtime = async () => {
      setIsLoading(true);
      try {
        let currentBills = [];
        let currentProfiles = [];

        const computeStats = (allBills, allProfiles) => {
          let overallTotal = 0;
          const byCompany = {};
          for (const p of allProfiles || []) {
            byCompany[p.id] = { name: p.shortBusinessName || p.name, total: 0, count: 0 };
          }
          for (const b of allBills || []) {
            const amount = Number(b.grand_total || 0);
            overallTotal += amount;
            if (b.company_id && byCompany[b.company_id]) {
              byCompany[b.company_id].total += amount;
              byCompany[b.company_id].count += 1;
            } else if (b.company_id) {
              byCompany[b.company_id] = { name: 'Unknown', total: amount, count: 1 };
            }
          }
          setStats({ overallTotal, byCompany, count: (allBills || []).length });
        };

        const b = await getAllCoolieBills((fresh) => {
          currentBills = fresh || [];
          setBills(currentBills);
          computeStats(currentBills, currentProfiles);
        });
        if (b && b.unsubscribe) unsubs.push(b.unsubscribe);
        currentBills = b || [];
        setBills(currentBills);

        const p = await getAllCoolieProfiles((fresh) => {
          currentProfiles = fresh || [];
          setProfiles(currentProfiles);
          computeStats(currentBills, currentProfiles);
        });
        if (p && p.unsubscribe) unsubs.push(p.unsubscribe);
        currentProfiles = p || [];
        setProfiles(currentProfiles);

        computeStats(currentBills, currentProfiles);
      } catch {
        thagaval('Failed to load coolie dashboard', 'error');
      } finally {
        setIsLoading(false);
      }
    };

    initRealtime();

    return () => {
      unsubs.forEach(unsub => unsub());
    };
  }, []);

  const handleView = (bill) => {
    const p = profiles.find(pr => pr.id === bill.company_id);
    if (p) bill._companyProfile = p;
    onEdit(bill);
  };

  const parseDate = (dString: string) => {
    if (!dString) return 0;
    const parts = dString.split('/');
    if (parts.length === 3) {
      return new Date(`${parts[2]}-${parts[1]}-${parts[0]}`).getTime();
    }
    return new Date(dString).getTime() || 0;
  };

  const recentBills = [...bills]
    .sort((a, b) => parseDate(b.date) - parseDate(a.date))
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
      {t('greeting')}!{customSparkle}
    </Box>
  );
  const greetingSubtitle = t('nirilCoolie');
  const profileInitial = <HandCoins weight="fill" size={24} />;

  const statsCards = (
    <>
      <Card sx={{ borderRadius: '24px', boxShadow: 'none', border: 'none', display: 'flex', flexDirection: 'column', bgcolor: isDark ? 'rgba(255,255,255,0.03)' : '#FFFFFF', userSelect: 'none' }}>
        <CardContent sx={{ display: 'flex', flexDirection: { xs: 'column', sm: 'row' }, alignItems: 'flex-start', p: { xs: 2, sm: 3 }, flexGrow: 1 }}>
          <Box sx={{ width: 48, height: 48, display: 'flex', alignItems: 'center', justifyContent: 'center', flexShrink: 0, borderRadius: '16px', bgcolor: isDark ? 'rgba(255,255,255,0.05)' : '#f5f5f5', color: 'text.primary', mr: { xs: 0, sm: 2 }, mb: { xs: 1.5, sm: 0 } }}>
            <CurrencyInr size={24} weight="regular" />
          </Box>
          <Box>
            <Typography variant="body2" color="text.secondary" mb={0.5} sx={{ fontWeight: 600 }}>{t('totalInvoiced') || 'Total Amount'}</Typography>
            {isLoading ? (
              <Skeleton variant="text" width={120} height={32} />
            ) : (
              <Typography variant="h5" color="text.primary" sx={{ fontWeight: 800, fontSize: { xs: formatCurrency(stats.overallTotal, 'INR').length > 14 ? '0.95rem' : formatCurrency(stats.overallTotal, 'INR').length > 11 ? '1.15rem' : '1.5rem', sm: '1.5rem' }, whiteSpace: 'nowrap', overflow: 'hidden', textOverflow: 'ellipsis', lineHeight: 1.2, width: '100%', display: 'block' }}>
                {formatCurrency(stats.overallTotal, 'INR')}
              </Typography>
            )}
          </Box>
        </CardContent>
      </Card>
      
      <Card sx={{ borderRadius: '24px', boxShadow: 'none', border: 'none', display: 'flex', flexDirection: 'column', bgcolor: isDark ? 'rgba(255,255,255,0.03)' : '#FFFFFF', userSelect: 'none' }}>
        <CardContent sx={{ display: 'flex', flexDirection: { xs: 'column', sm: 'row' }, alignItems: 'flex-start', p: { xs: 2, sm: 3 }, flexGrow: 1 }}>
          <Box sx={{ width: 48, height: 48, display: 'flex', alignItems: 'center', justifyContent: 'center', flexShrink: 0, borderRadius: '16px', bgcolor: isDark ? 'rgba(255,255,255,0.05)' : '#f5f5f5', color: 'text.primary', mr: { xs: 0, sm: 2 }, mb: { xs: 1.5, sm: 0 } }}>
            <Storefront size={24} weight="regular" />
          </Box>
          <Box sx={{ width: '100%' }}>
            <Typography variant="body2" color="text.secondary" mb={0.5} sx={{ fontWeight: 600 }}>
              {t('companies')}
            </Typography>
            {isLoading ? (
              <Skeleton variant="text" width="80%" height={32} />
            ) : (
              <Typography variant="h6" color="text.primary" sx={{ fontWeight: 800, lineHeight: 1.4 }}>
                {Object.entries(stats.byCompany)
                  .filter(([id, data]: any) => data.total > 0)
                  .map(([id, data]: any) => data.name)
                  .join(', ') || '—'}
              </Typography>
            )}
          </Box>
        </CardContent>
      </Card>

      {/* Third Card: Invoice Count */}
      <Card sx={{ display: 'flex', gridColumn: { xs: 'span 2', md: 'auto' }, borderRadius: '24px', boxShadow: 'none', border: 'none', flexDirection: 'column', bgcolor: isDark ? 'rgba(255,255,255,0.03)' : '#FFFFFF', userSelect: 'none' }}>
        <CardContent sx={{ display: 'flex', flexDirection: 'row', alignItems: 'center', p: { xs: 2, sm: 3 }, flexGrow: 1 }}>
          <Box sx={{ width: 48, height: 48, display: 'flex', alignItems: 'center', justifyContent: 'center', flexShrink: 0, borderRadius: '16px', bgcolor: isDark ? 'rgba(255,255,255,0.05)' : '#f5f5f5', color: 'text.primary', mr: 2 }}>
            <Receipt size={24} weight="regular" />
          </Box>
          <Box>
            <Typography variant="body2" color="text.secondary" mb={0.5} sx={{ fontWeight: 600 }}>
              {t('totalBills')}
            </Typography>
            {isLoading ? (
              <Skeleton variant="text" width={60} height={32} />
            ) : (
              <Typography variant="h6" color="text.primary" sx={{ fontWeight: 800 }}>
                {(() => {
                  const activeCompanies = Object.values(stats.byCompany).filter((c: any) => c.count > 0);
                  if (activeCompanies.length === 1) return `${activeCompanies[0].count} · ${activeCompanies[0].name}`;
                  if (activeCompanies.length > 1) return activeCompanies.map((c: any) => `${c.count} · ${c.name}`).join('  +  ') + `  =  ${stats.count}`;
                  return stats.count;
                })()}
              </Typography>
            )}
          </Box>
        </CardContent>
      </Card>
    </>
  );

  const renderRecentItem = (bill, index) => {
    return (
      <ElvanCard key={bill.id} onClick={() => onView && onView(bill)} sx={{ cursor: 'pointer', '@media (hover: hover)': { '&:hover': { bgcolor: 'action.hover' } } }}>
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
                {bill.customer_name ? (bill.customer_name.includes('/') ? bill.customer_name.split('/')[0].trim() : bill.customer_name) : '-'}
              </Typography>
              <CaretRight size={18} weight="regular" color={isDark ? "#555" : "#aaa"} style={{ marginTop: '2px', flexShrink: 0 }} />
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
              <Typography variant="subtitle2" noWrap sx={{ fontWeight: 800, color: 'primary.main', fontSize: formatCurrency(Number(bill.grand_total || 0), 'INR').length > 11 ? '0.8rem' : '0.95rem', flexShrink: 0 }}>
                {formatCurrency(Number(bill.grand_total || 0), 'INR')}
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
      profileAvatar={profiles[0]?.logo}
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
