import React, { useState, useEffect } from 'react';
import { createPortal } from 'react-dom';
import { Box, Typography, IconButton, Tooltip, useMediaQuery, useTheme, Button } from '@mui/material';
import { PencilSimple, Printer as PrintIcon, DownloadSimple, ShareNetwork, Spinner } from '@phosphor-icons/react';
import { FloatingBackButton } from './FloatingBackButton';
import { useLanguage } from '../mozhi/LanguageContext';
import { Capacitor } from '@capacitor/core';

export const ViewHeader = ({ onEdit, onPrint, onPDF, onShare, saving, sharing = false, title, onBack, className = '' }: any) => {
  const { t } = useLanguage();
  const theme = useTheme();
  const isMobile = useMediaQuery(theme.breakpoints.down('md'));
  const [mounted, setMounted] = useState(false);

  useEffect(() => { setMounted(true); }, []);

  const iconBtnSx = { 
    color: 'text.secondary',
    width: { xs: 44, md: 34 },
    height: { xs: 44, md: 34 },
    '&:active svg': { transform: 'scale(0.85)' },
    '& svg': { transition: 'transform 0.15s cubic-bezier(0.4, 0, 0.2, 1)' }
  };

  const toolsPill = (
    <Box sx={{ ml: {xs: 0, md: 2}, display: 'flex', gap: { xs: 1, md: 0.5 }, alignItems: 'center', bgcolor: { xs: 'transparent', md: 'background.paper' }, borderRadius: '999px', px: { xs: 0, md: 1 }, height: { xs: 48, md: 40 }, boxSizing: 'border-box', border: 'none' }}>
      {onEdit && (
        <Tooltip title="Edit">
          <IconButton onClick={onEdit} sx={{ ...iconBtnSx, display: { xs: 'none', md: 'inline-flex' } }}>
            <PencilSimple size={22} />
          </IconButton>
        </Tooltip>
      )}
      <Tooltip title="Print">
        <IconButton onClick={onPrint} sx={iconBtnSx}>
          <PrintIcon size={22} />
        </IconButton>
      </Tooltip>
      <Tooltip title="PDF">
        <IconButton onClick={onPDF} disabled={saving} sx={iconBtnSx}>
          <DownloadSimple size={22} />
        </IconButton>
      </Tooltip>
      <Tooltip title="Share">
        <IconButton onClick={onShare} disabled={sharing || saving} sx={iconBtnSx}>
          {sharing ? <Spinner size={22} className="animate-spin" /> : <ShareNetwork size={22} />}
        </IconButton>
      </Tooltip>
    </Box>
  );

  const backBtn = <FloatingBackButton onBack={onBack} label={(t('back') || 'Back') as string} className="back-pill" />;

  const mobileEditFab = isMobile && onEdit ? (
    <Button
      variant="contained"
      color="primary"
      onClick={onEdit}
      sx={{
        display: { xs: 'flex', md: 'none' },
        position: 'fixed',
        bottom: 'calc(env(safe-area-inset-bottom, 0px) + 95px)',
        right: 20,
        minWidth: 56,
        width: 56,
        height: 56,
        borderRadius: '20px',
        zIndex: 1100,
        boxShadow: '0 8px 24px rgba(0,0,0,0.2)',
        p: 0,
      }}
    >
      <PencilSimple size={24} weight="fill" />
    </Button>
  ) : null;

  if (isMobile && mounted) {
    const leftTarget = document.getElementById('mobile-topbar-left');
    const rightTarget = document.getElementById('mobile-topbar-right');

    if (leftTarget && rightTarget) {
      return (
        <>
          {createPortal(backBtn, leftTarget)}
          {createPortal(toolsPill, rightTarget)}
          {mobileEditFab}
        </>
      );
    }
  }

  return (
    <Box className={className} sx={{ px: { xs: 2, md: 0 }, mb: 4, display: 'flex', justifyContent: 'space-between', alignItems: 'center', flexWrap: 'wrap', gap: 2, position: 'relative' }}>
      {toolsPill}
      
      {/* Centered Title (Desktop Only) */}
      <Typography variant="h6" sx={{ display: { xs: 'none', md: 'block' }, position: 'absolute', left: '50%', transform: 'translateX(-50%)', fontWeight: 700, color: 'text.primary', letterSpacing: '-0.5px' }}>
        {title}
      </Typography>

      {backBtn}
      {mobileEditFab}
    </Box>
  );
};
