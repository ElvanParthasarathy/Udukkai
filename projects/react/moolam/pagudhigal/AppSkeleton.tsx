import { Box, CircularProgress } from '@mui/material';
import { useState, useEffect } from 'react';

export default function AppSkeleton({ darkMode }: { darkMode: boolean }) {
  const [isFadingOut, setIsFadingOut] = useState(false);
  const [hidden, setHidden] = useState(false);

  useEffect(() => {
    // Show spinner fully for 600ms, then fade out
    const t1 = setTimeout(() => setIsFadingOut(true), 600);
    const t2 = setTimeout(() => setHidden(true), 1200);
    return () => { clearTimeout(t1); clearTimeout(t2); };
  }, []);

  if (hidden) return null;

  return (
    <Box sx={{ 
      position: 'fixed', top: 0, left: 0, width: '100vw', height: '100vh', 
      zIndex: 9998, 
      bgcolor: darkMode ? '#0a0a0a' : '#f0f2f5',
      opacity: isFadingOut ? 0 : 1, 
      transition: 'opacity 0.6s cubic-bezier(0.4, 0, 0.2, 1)',
      display: 'flex',
      alignItems: 'center',
      justifyContent: 'center',
      pointerEvents: 'none' // Don't block clicks while fading
    }}>
      <CircularProgress color="primary" />
    </Box>
  );
}
