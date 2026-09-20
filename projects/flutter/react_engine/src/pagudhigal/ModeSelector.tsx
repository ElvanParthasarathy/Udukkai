import { Box, Typography, Card, CardActionArea, CardContent, useTheme, Fade, Zoom, useMediaQuery } from '@mui/material';
import { Invoice, HandCoins } from '@phosphor-icons/react';
import { useState, useEffect } from 'react';
import { useLanguage } from '../mozhi/LanguageContext';

export default function ModeSelector({ onSelect, currentMode }) {
  const { t } = useLanguage();

  const { language } = useLanguage();
  const theme = useTheme();
  const darkMode = true; // Always lock to dark mode for a cinematic, premium entry screen
  const isMobile = useMediaQuery(theme.breakpoints.down('sm'));
  const [mounted, setMounted] = useState(false);
  const [isFadingOut, setIsFadingOut] = useState(false);

  useEffect(() => {
    setMounted(true);
  }, []);

  const handleSelect = (mode) => {
    setIsFadingOut(true);
    setTimeout(() => {
      onSelect(mode);
    }, 400); // Wait for fade out animation
  };

  return (
    <>
      <Box
        sx={{
          position: 'fixed',
          top: 0,
          left: 0,
          width: '100vw',
          height: '100vh',
          bgcolor: darkMode ? '#0a0a0a' : '#f0f2f5',
          display: 'flex',
          flexDirection: 'column',
          alignItems: 'center',
          justifyContent: 'center',
          overflow: 'hidden',
          px: 2,
          zIndex: 99999,
          opacity: isFadingOut ? 0 : 1,
          transition: 'opacity 0.4s ease-out',
        }}
      >
        <Zoom in={mounted} timeout={1000}>
          <Box sx={{ textAlign: 'center', mb: { xs: 3, sm: 8 } }}>
            <Typography
              variant="h3"
              sx={{
                fontSize: { xs: '1.6rem', sm: '3rem' },
                fontWeight: 800,
                color: darkMode ? '#ffffff' : '#111827',
                letterSpacing: '-0.02em',
                lineHeight: 1.2,
                mb: 1,
              }}
            >
              {t('whoIsWorkingToday')}
            </Typography>
            <Typography
              variant="subtitle1"
              sx={{
                fontSize: { xs: '0.85rem', sm: '1.25rem' },
                fontWeight: 500,
                color: darkMode ? '#9ca3af' : '#6b7280',
                px: 2,
              }}
            >
              {t('selectYourOperatingMode')}
            </Typography>
          </Box>
        </Zoom>

        <Box sx={{ display: 'flex', flexDirection: 'row', gap: { xs: 2, sm: 8 }, alignItems: 'center', justifyContent: 'center', flexWrap: 'nowrap' }}>
          {/* GST Mode Card */}
          <Zoom in={mounted} timeout={1200}>
            <Card
              elevation={0}
              sx={{
                width: { xs: 130, sm: 240 },
                height: { xs: 160, sm: 280 },
                bgcolor: 'transparent',
                borderRadius: 4,
                overflow: 'visible',
              }}
            >
              <CardActionArea
                onClick={() => handleSelect('GST')}
                sx={{
                  height: '100%',
                  borderRadius: 4,
                  display: 'flex',
                  flexDirection: 'column',
                  alignItems: 'center',
                  justifyContent: 'center',
                  transition: 'all 0.3s cubic-bezier(0.4, 0, 0.2, 1)',
                  '@media (hover: hover)': { '&:hover': {
                    transform: 'scale(1.05) translateY(-8px)',
                    '& .icon-box': {
                      boxShadow: darkMode 
                        ? '0 20px 40px rgba(255, 255, 255, 0.1)' 
                        : '0 20px 40px rgba(0, 0, 0, 0.1)',
                      border: `2px solid ${darkMode ? '#ffffff' : '#000000'}`,
                    },
                    '& .card-text': {
                      color: darkMode ? '#ffffff' : '#111827',
                    }
                  } },
                }}
              >
                <Box
                  className="icon-box"
                  sx={{
                    width: { xs: 80, sm: 140 },
                    height: { xs: 80, sm: 140 },
                    borderRadius: '50%',
                    bgcolor: darkMode ? '#333333' : '#eeeeee',
                    display: 'flex',
                    alignItems: 'center',
                    justifyContent: 'center',
                    mb: 3,
                    border: '2px solid transparent',
                    transition: 'all 0.3s ease',
                    boxShadow: darkMode ? '0 8px 16px rgba(0,0,0,0.4)' : '0 8px 16px rgba(0,0,0,0.05)',
                  }}
                >
                  <Invoice size={isMobile ? 40 : 64} weight="fill" color={darkMode ? '#ffffff' : '#000000'} />
                </Box>
                <CardContent sx={{ p: 0, textAlign: 'center' }}>
                  <Typography
                    className="card-text"
                    variant="h6"
                    sx={{
                      fontSize: { xs: '1rem', sm: '1.5rem' },
                      fontWeight: 700,
                      color: darkMode ? '#9ca3af' : '#4b5563',
                      transition: 'color 0.3s ease',
                    }}
                  >
                    {t('nirilSilk')}
                  </Typography>
                </CardContent>
              </CardActionArea>
            </Card>
          </Zoom>

          {/* Coolie Mode Card */}
          <Zoom in={mounted} timeout={1400}>
            <Card
              elevation={0}
              sx={{
                width: { xs: 130, sm: 240 },
                height: { xs: 160, sm: 280 },
                bgcolor: 'transparent',
                borderRadius: 4,
                overflow: 'visible',
              }}
            >
              <CardActionArea
                onClick={() => handleSelect('COOLIE')}
                sx={{
                  height: '100%',
                  borderRadius: 4,
                  display: 'flex',
                  flexDirection: 'column',
                  alignItems: 'center',
                  justifyContent: 'center',
                  transition: 'all 0.3s cubic-bezier(0.4, 0, 0.2, 1)',
                  '@media (hover: hover)': { '&:hover': {
                    transform: 'scale(1.05) translateY(-8px)',
                    '& .icon-box': {
                      boxShadow: darkMode 
                        ? '0 20px 40px rgba(255, 255, 255, 0.1)' 
                        : '0 20px 40px rgba(0, 0, 0, 0.1)',
                      border: `2px solid ${darkMode ? '#ffffff' : '#000000'}`,
                    },
                    '& .card-text': {
                      color: darkMode ? '#ffffff' : '#111827',
                    }
                  } },
                }}
              >
                <Box
                  className="icon-box"
                  sx={{
                    width: { xs: 80, sm: 140 },
                    height: { xs: 80, sm: 140 },
                    borderRadius: '50%',
                    bgcolor: darkMode ? '#333333' : '#eeeeee',
                    display: 'flex',
                    alignItems: 'center',
                    justifyContent: 'center',
                    mb: 3,
                    border: '2px solid transparent',
                    transition: 'all 0.3s ease',
                    boxShadow: darkMode ? '0 8px 16px rgba(0,0,0,0.4)' : '0 8px 16px rgba(0,0,0,0.05)',
                  }}
                >
                  <HandCoins size={isMobile ? 40 : 64} weight="fill" color={darkMode ? '#ffffff' : '#000000'} />
                </Box>
                <CardContent sx={{ p: 0, textAlign: 'center' }}>
                  <Typography
                    className="card-text"
                    variant="h6"
                    sx={{
                      fontSize: { xs: '1rem', sm: '1.5rem' },
                      fontWeight: 700,
                      color: darkMode ? '#9ca3af' : '#4b5563',
                      transition: 'color 0.3s ease',
                    }}
                  >
                    {t('nirilCoolie')}
                  </Typography>
                </CardContent>
              </CardActionArea>
            </Card>
          </Zoom>
        </Box>
      </Box>
    </>
  );
}
