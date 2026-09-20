// @ts-nocheck
import React, { useState, useEffect, useCallback } from 'react';
import { Snackbar, Alert, Stack } from '@mui/material';

let toastId = 0;
let addToastFn: any = null;

export function thagaval(message: string, type: 'success' | 'error' | 'warning' | 'info' = 'info', duration = 3500) {
  if (addToastFn) {
    addToastFn({ id: ++toastId, message, type, duration });
  }
}

export default function Thagaval() {
  const [toasts, setToasts] = useState<any[]>([]);

  const addToast = useCallback((t: any) => {
    setToasts(prev => [...prev, t]);
    setTimeout(() => {
      setToasts(prev => prev.filter(x => x.id !== t.id));
    }, t.duration);
  }, []);

  useEffect(() => {
    addToastFn = addToast;
    return () => { addToastFn = null; };
  }, [addToast]);

  const dismiss = (id: number) => {
    setToasts(prev => prev.filter(x => x.id !== id));
  };

  return (
    <Stack spacing={2} sx={{ position: 'fixed', bottom: 24, right: 24, zIndex: 9999 }}>
      {toasts.map(t => (
        <Alert
          key={t.id}
          severity={t.type}
          onClose={() => dismiss(t.id)}
          variant="filled"
          sx={{ boxShadow: 3, display: 'flex', alignItems: 'center', borderRadius: '50px', px: 3 }}
        >
          {t.message}
        </Alert>
      ))}
    </Stack>
  );
}

