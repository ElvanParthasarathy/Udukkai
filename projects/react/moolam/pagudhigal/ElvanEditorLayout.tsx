import React, { useState, useEffect } from 'react';
import { createPortal } from 'react-dom';
import { Box, Typography, Button, useMediaQuery, useTheme, Dialog, DialogTitle, DialogContent, DialogActions } from '@mui/material';
import { FloppyDisk } from '@phosphor-icons/react';
import { FloatingBackButton } from './FloatingBackButton';
import { useLanguage } from '../mozhi/LanguageContext';

export interface ElvanEditorLayoutProps {
  title: string;
  onBack: () => void;
  onSave: () => void;
  saveButtonText: string;
  saveButtonIcon?: React.ReactNode;
  children: React.ReactNode;
  hasUnsavedChanges?: boolean;
  onDiscard?: () => void;
  hideHeaderPortals?: boolean;
}

export default function ElvanEditorLayout({
  title,
  onBack,
  onSave,
  saveButtonText,
  saveButtonIcon = <FloppyDisk size={20} weight="bold" />,
  children,
  hasUnsavedChanges = false,
  onDiscard,
  hideHeaderPortals = false
}: ElvanEditorLayoutProps) {
  const { t } = useLanguage();
  const theme = useTheme();
  const isMobile = useMediaQuery(theme.breakpoints.down('md'));
  const [mounted, setMounted] = useState(false);
  const [showUnsavedDialog, setShowUnsavedDialog] = useState(false);

  useEffect(() => { setMounted(true); }, []);

  const handleBackClick = () => {
    if (hasUnsavedChanges) {
      setShowUnsavedDialog(true);
    } else {
      onBack();
    }
  };

  const isEditingTitle = title.includes(t('edit')) || title.toLowerCase().includes('edit') || title.includes('திருத்து');
  const displayTitle = isMobile && isEditingTitle ? (t('edit') || 'Edit') : title;

  const titleElement = (
    <Typography variant="h5" sx={{ 
      ml: { xs: 1.5, md: 2 }, 
      fontWeight: 800, 
      letterSpacing: '-0.5px', 
      color: 'text.primary',
      fontSize: { xs: '1.1rem', md: '1.5rem' }
    }}>
      {displayTitle}
    </Typography>
  );

  const backButtonElement = (
    <FloatingBackButton onBack={handleBackClick} label={(t('back') || 'Back') as string} className="back-pill" />
  );

  let renderHeader = null;
  if (!hideHeaderPortals && isMobile && mounted) {
    const leftTarget = document.getElementById('mobile-topbar-left');
    if (leftTarget) {
      renderHeader = (
        <>
          {createPortal(
            <Box sx={{ display: 'flex', alignItems: 'center' }}>
              {backButtonElement}
              {titleElement}
            </Box>, 
            leftTarget
          )}
        </>
      );
    }
  } else if (!isMobile) {
    renderHeader = (
      <Box sx={{ 
        px: 0, 
        mb: 4, 
        display: 'flex', 
        justifyContent: 'space-between', 
        alignItems: 'center'
      }}>
        {titleElement}
        {backButtonElement}
      </Box>
    );
  }

  return (
    <Box sx={{ pt: { xs: 1.5, md: 4 }, pb: { xs: '180px', md: 4 }, px: { xs: 0, md: 4 }, maxWidth: 1200, mx: 'auto', position: 'relative' }}>
      {renderHeader}

      <Box sx={{ px: 0 }}>
        {children}
      </Box>

      <Box sx={{ display: { xs: 'none', md: 'flex' }, justifyContent: 'flex-end', gap: 2, mb: 6, mt: 5, px: 0 }}>
        <Button 
          variant="contained" 
          disableElevation 
          onClick={handleBackClick} 
          sx={{ height: 40, minHeight: 40, maxHeight: 40, px: 3, borderRadius: '50px', bgcolor: 'background.paper', color: 'text.primary', '@media (hover: hover)': { '&:hover': { bgcolor: 'action.hover' } } }}
        >
          {t('cancelModalBtn') || 'Cancel'}
        </Button>
        <Button 
          variant="contained" 
          color="primary" 
          disableElevation 
          onClick={onSave} 
          startIcon={saveButtonIcon} 
          sx={{ height: 40, minHeight: 40, maxHeight: 40, px: 3, borderRadius: '50px' }}
        >
          {saveButtonText}
        </Button>
      </Box>

      {/* Mobile Squircle FAB for Save */}
      {isMobile && (
        <Button
          variant="contained"
          color="primary"
          onClick={onSave}
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
          {saveButtonIcon || <FloppyDisk size={24} weight="fill" />}
        </Button>
      )}

      {/* Unsaved Changes Dialog */}
      <Dialog 
        open={showUnsavedDialog} 
        onClose={() => setShowUnsavedDialog(false)}
        slotProps={{ paper: { sx: { borderRadius: 3, p: 1 } } }}
      >
        <DialogTitle sx={{ fontWeight: 'bold' }}>
          {t('hc_discardChanges') || 'Discard Changes'}
        </DialogTitle>
        <DialogContent>
          <Typography variant="body1">
            {t('hc_discardWarning') || 'Are you sure you want to discard your unsaved changes?'}
          </Typography>
        </DialogContent>
        <DialogActions sx={{ p: 2, pt: 1 }}>
          <Button 
            onClick={() => setShowUnsavedDialog(false)} 
            color="inherit" 
            sx={{ borderRadius: '50px', px: 3, textTransform: 'none' }}
          >
            {t('cancelModalBtn') || 'Cancel'}
          </Button>
          <Button 
            onClick={() => {
              setShowUnsavedDialog(false);
              if (onDiscard) onDiscard();
              else onBack();
            }} 
            color="error" 
            sx={{ borderRadius: '50px', px: 3, textTransform: 'none' }}
          >
            {t('hc_discard') || 'Discard'}
          </Button>
          <Button 
            onClick={() => {
              setShowUnsavedDialog(false);
              onSave();
            }} 
            variant="contained" 
            color="primary" 
            disableElevation
            sx={{ borderRadius: '50px', px: 3, textTransform: 'none' }}
          >
            {t('save') || 'Save'}
          </Button>
        </DialogActions>
      </Dialog>
    </Box>
  );
}
