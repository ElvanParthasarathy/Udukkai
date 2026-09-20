import React from 'react';
import { Paper, Box, useTheme, PaperProps, BoxProps, CardActionArea } from '@mui/material';

interface ElvanCardProps extends PaperProps {
  onClick?: () => void;
  boxSx?: BoxProps['sx'];
  children: React.ReactNode;
}

export default function ElvanCard({ children, onClick, sx = {}, boxSx = {}, ...props }: ElvanCardProps) {
  const theme = useTheme();
  const isDark = theme.palette.mode === 'dark';

  const content = (
    <Box sx={{ p: { xs: 2, sm: 2.5 }, height: '100%', ...boxSx }}>
      {children}
    </Box>
  );

  return (
    <Paper 
      elevation={1}
      sx={{
        borderRadius: '24px', 
        bgcolor: isDark ? 'rgba(255,255,255,0.03)' : '#FFFFFF', 
        overflow: 'hidden',
        boxShadow: 'none',
        border: 'none',
        transition: 'transform 0.2s cubic-bezier(0.4, 0, 0.2, 1), background-color 0.2s',
        ...(onClick ? {
          '@media (hover: hover)': {
            '&:hover': { bgcolor: isDark ? 'rgba(255,255,255,0.05)' : 'rgba(0,0,0,0.02)' },
          },
          '&:active': { 
            transform: 'scale(0.985)',
            transitionDuration: '0.1s'
          }
        } : {}),
        ...sx
      }}
      {...props}
    >
      {onClick ? (
        <CardActionArea component="div" onClick={onClick} sx={{ height: '100%' }}>
          {content}
        </CardActionArea>
      ) : content}
    </Paper>
  );
}
