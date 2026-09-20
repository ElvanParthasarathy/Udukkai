import { Theme, SxProps } from '@mui/material/styles';

export const getSearchPaperSx = (isDark: boolean): SxProps<Theme> => ({
  flex: 1,
  minWidth: 0,
  maxWidth: { xs: 'none', md: 400 },
  display: 'flex',
  alignItems: 'center',
  height: 48,
  gap: 1.25,
  bgcolor: isDark ? 'rgba(255,255,255,0.03)' : '#FFFFFF',
  borderRadius: 100,
  px: 2.5,
  boxShadow: 'none',
  border: 'none',
  transition: 'background 0.3s ease',
});

export const searchInputStyle = {
  flex: 1,
  minWidth: 0,
  padding: '12px 0',
  fontSize: '0.95rem',
  background: 'transparent',
  border: 'none',
  outline: 'none',
  color: 'inherit',
  fontFamily: 'inherit',
};

export const getEditPaperSx = (isDark: boolean, isSelectionMode: boolean): SxProps<Theme> => ({
  borderRadius: '50%',
  bgcolor: isSelectionMode ? (isDark ? 'rgba(255,255,255,0.1)' : 'rgba(0,0,0,0.1)') : (isDark ? 'rgba(255,255,255,0.03)' : '#FFFFFF'), 
  boxShadow: 'none',
  flexShrink: 0,
});

export const getEditIconButtonSx = (isDark: boolean): SxProps<Theme> => ({
  width: 48, height: 48, 
  '@media (hover: hover)': { '&:hover': { bgcolor: isDark ? 'rgba(255,255,255,0.05)' : 'rgba(0,0,0,0.05)' } },
  '& .MuiTouchRipple-child': {
    backgroundColor: isDark ? 'rgba(255, 255, 255, 0.12)' : 'rgba(0, 0, 0, 0.2)',
  }
});

export const getAddButtonSx = (isDark: boolean): SxProps<Theme> => ({
  display: { xs: 'none', md: 'inline-flex' }, 
  flexShrink: 0, 
  height: 48, 
  px: 3, 
  borderRadius: '999px', 
  textTransform: 'none', 
  whiteSpace: 'nowrap', 
  ml: 'auto', 
  bgcolor: isDark ? 'white' : 'black', 
  color: isDark ? 'black' : 'white', 
  fontWeight: 700, 
  boxShadow: 'none', 
  '@media (hover: hover)': { '&:hover': { 
    bgcolor: isDark ? '#e5e5e5' : '#333', 
    boxShadow: '0 4px 12px rgba(0,0,0,0.1)' 
  } },
  '@media (hover: none)': {
    '@media (hover: hover)': { '&:hover': {
      bgcolor: isDark ? 'white' : 'black',
      boxShadow: 'none',
    } }
  }
});
