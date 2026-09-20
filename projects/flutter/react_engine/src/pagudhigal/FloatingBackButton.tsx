import React from 'react';
import { Button, IconButton, Box, useMediaQuery, useTheme } from '@mui/material';
import { CaretLeft } from '@phosphor-icons/react';
import { useLanguage } from '../mozhi/LanguageContext';

interface FloatingBackButtonProps {
    to?: string; 
    label?: string;
    className?: string;
    onBack?: () => void;
}

export const FloatingBackButton: React.FC<FloatingBackButtonProps> = ({ label, className, onBack }) => {
    const theme = useTheme();
    const isMobile = useMediaQuery(theme.breakpoints.down('md'));
    const { t } = useLanguage();
    const displayLabel = label || t('back');

    const handleClick = (e: React.MouseEvent) => {
        if (onBack) {
            onBack();
        } else if (window.history.state && window.history.state.idx > 0) {
            window.history.back();
        } else {
            window.location.search = '';
        }
    };

    if (isMobile) {
        return (
            <IconButton 
                className={className}
                onClick={handleClick}
                sx={{
                    bgcolor: 'background.paper',
                    color: 'text.primary',
                    p: 1.2,
                    '&:active svg': { transform: 'scale(0.85)' },
                    '& svg': { transition: 'transform 0.15s cubic-bezier(0.4, 0, 0.2, 1)' }
                }}
            >
                <CaretLeft weight="bold" size={22} />
            </IconButton>
        );
    }

    return (
        <Button 
            variant="contained"
            disableElevation
            className={className}
            startIcon={<CaretLeft weight="bold" size={16} />}
            onClick={handleClick}
            sx={{ 
                borderRadius: '50px', 
                textTransform: 'none',
                fontWeight: 700,
                px: 2.5,
                height: 40,
                minHeight: 40,
                maxHeight: 40,
                display: 'flex',
                alignItems: 'center',
                fontSize: '0.9rem',
                bgcolor: 'background.paper',
                color: 'text.primary',
                '@media (hover: hover)': {
                    '&:hover': {
                        bgcolor: 'action.hover',
                    }
                }
            }}
        >
            <Box component="span" sx={{ transform: 'translateY(2px)' }}>
                {displayLabel}
            </Box>
        </Button>
    );
};
