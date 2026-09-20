// @ts-nocheck
import React from 'react';
import { CaretRight, Receipt } from '@phosphor-icons/react';
import { useLanguage } from '../mozhi/LanguageContext';
import {
  Box, Typography, Button, Paper, Avatar, Skeleton, Stack, useTheme
} from '@mui/material';
import ElvanCard from './ElvanCard';

export default function MugappuLayout({
  title,
  greetingTitle,
  greetingSubtitle,
  profileAvatar,
  profileInitial,
  onNew,
  onViewAll,
  statsCards,
  recentActivityTitle,
  recentItems,
  isLoading,
  emptyStateText,
  renderRecentItem,
  onProfileClick
}: any) {
  const { t, language } = useLanguage();
  const theme = useTheme();
  const isDark = theme.palette.mode === 'dark';

  return (
    <Box sx={{ pt: { xs: 2, md: 4 }, px: { xs: 0, md: 4 }, pb: { xs: 2, md: 4 }, maxWidth: 1200, mx: 'auto' }}>
      {/* Desktop Page Title Area (Creates space at top for future buttons) */}
      <Box sx={{ display: { xs: 'none', md: 'flex' }, justifyContent: 'space-between', alignItems: 'center', mb: 4 }}>
        <Typography variant="h5" sx={{ fontWeight: 800, letterSpacing: '-0.5px', color: 'text.primary', ml: 2 }}>
          {title}
        </Typography>
        {/* Empty Box here ensures future right-aligned items push to the edge */}
        <Box />
      </Box>

      {/* Header */}
      <Box sx={{ display: 'flex', flexDirection: { xs: 'column', sm: 'row' }, justifyContent: 'space-between', alignItems: { xs: 'flex-start', sm: 'center' }, gap: 2, mb: 4 }}>
        <Paper elevation={1} sx={{ 
          display: 'flex', 
          alignItems: 'center',
          bgcolor: isDark ? 'rgba(255,255,255,0.03)' : '#FFFFFF', 
          color: isDark ? '#FFFFFF' : '#000000', 
          borderRadius: '999px', 
          p: { xs: 2, sm: 1.5 },
          pr: { xs: 4, sm: 4 },
          boxShadow: 'none',
          border: 'none',
          width: { xs: '100%', sm: 'auto' }
        }}>
          <Avatar 
            onClick={onProfileClick}
            sx={{ 
              width: { xs: 60, sm: 48 }, height: { xs: 60, sm: 48 }, mr: { xs: 2.5, sm: 2 }, 
              bgcolor: isDark ? '#333' : '#eee', color: isDark ? '#fff' : '#000', 
              fontWeight: 'bold', cursor: 'pointer', fontSize: { xs: '1.5rem', sm: '1.25rem' },
              transition: 'all 0.2s ease',
              '@media (hover: hover)': { '&:hover': { transform: 'scale(1.05)', filter: isDark ? 'brightness(1.1)' : 'brightness(0.95)' } },
              '&:active': { transform: 'scale(0.95)' }
            }}
          >
            {profileInitial}
          </Avatar>
          <Box sx={{ display: 'flex', flexDirection: 'column' }}>
            <Typography variant="h6" sx={{ fontWeight: 800, letterSpacing: '-0.02em', lineHeight: 1.2, fontSize: { xs: '1.4rem', sm: '1.25rem' } }}>
              {greetingTitle}
            </Typography>
            <Typography variant="body2" sx={{ fontWeight: 600, color: isDark ? '#9BA1A6' : '#666', mt: 0.3, fontSize: { xs: '1.05rem', sm: '0.875rem' } }}>
              {greetingSubtitle}
            </Typography>
          </Box>
        </Paper>
        <Button 
          variant="contained" 
          onClick={onNew} 
          sx={{ 
            display: { xs: 'none', md: 'inline-flex' },
            borderRadius: '999px', 
            px: 3, 
            py: 1.2, 
            bgcolor: isDark ? 'white' : 'black', 
            color: isDark ? 'black' : 'white',
            fontWeight: 700,
            textTransform: 'none',
            boxShadow: 'none',
            '@media (hover: hover)': { '&:hover': { bgcolor: isDark ? '#e5e5e5' : '#333', boxShadow: '0 4px 12px rgba(0,0,0,0.1)' } } 
          }}>
          + {t('newInvoice')}
        </Button>
      </Box>

      {/* Stats Grid - Bento Box Layout on Mobile */}
      <Box sx={{ display: 'grid', gridTemplateColumns: { xs: 'repeat(2, 1fr)', md: 'repeat(3, 1fr)' }, gap: { xs: 2, md: 3 }, mb: { xs: 1.5, md: 3 } }}>
        {statsCards}
      </Box>

      {/* Recent Activity Area */}
      <Box sx={{ borderRadius: '24px', overflow: 'hidden' }}>
        <Box sx={{ px: 0, pt: { xs: 1, md: 2 }, pb: 2, display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
          <Typography variant="h6" sx={{ fontWeight: 700, display: 'flex', alignItems: 'center', ml: 1.5 }}>
            {recentActivityTitle}
          </Typography>
          <Button 
            onClick={onViewAll} 
            endIcon={<CaretRight size={16} weight="regular" />}
            sx={{ fontWeight: 600, color: 'text.secondary', textTransform: 'none', '@media (hover: hover)': { '&:hover': { color: 'text.primary', bgcolor: 'transparent' } }, mr: 1.5 }}
            disableRipple
          >
            {t('seeAll')}
          </Button>
        </Box>

        {isLoading ? (
          <Box sx={{ display: 'grid', gridTemplateColumns: { xs: '1fr', md: 'repeat(2, 1fr)' }, gap: 2, pb: { xs: 12, md: 3 }, px: 0 }}>
            {[1, 2, 3, 4].map((i) => (
              <ElvanCard key={i} sx={{ height: '100%', p: 2 }}>
                <Stack direction="row" spacing={2} sx={{ alignItems: 'center', height: '100%' }}>
                  <Skeleton variant="circular" width={28} height={28} />
                  <Box sx={{ flex: 1 }}>
                    <Skeleton variant="text" width="60%" height={24} />
                    <Skeleton variant="text" width="40%" height={16} sx={{ mt: 0.5 }} />
                  </Box>
                  <Skeleton variant="rectangular" width={60} height={24} sx={{ borderRadius: 1 }} />
                </Stack>
              </ElvanCard>
            ))}
          </Box>
        ) : recentItems.length === 0 ? (
          <Box sx={{ py: 10, textAlign: 'center' }}>
            <Receipt size={48} weight="regular" color={isDark ? '#475569' : '#cbd5e1'} style={{ margin: '0 auto', marginBottom: '16px' }} />
            <Typography color="text.secondary" mb={2}>
              {emptyStateText || t('noInvoicesYet')}
            </Typography>
          </Box>
        ) : (
          <Box sx={{ display: 'grid', gridTemplateColumns: { xs: '1fr', md: 'repeat(2, 1fr)' }, gap: 2, pb: { xs: 12, md: 3 }, px: 0 }}>
            {recentItems.map((item, index) => renderRecentItem(item, index))}
          </Box>
        )}
      </Box>
    </Box>
  );
}
