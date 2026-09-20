import React, { useState } from 'react';
import { SettingsPillContainer, SettingsRow } from '../ElvanSettingsSection';
import { ArrowsClockwise, Trash, WarningCircle, SignOut } from '@phosphor-icons/react';
import { Box, Dialog, DialogTitle, DialogContent, DialogActions, TextField, Typography, CircularProgress, Button } from '@mui/material';
import { thagaval } from '../Thagaval';
import { signOut, EmailAuthProvider, reauthenticateWithCredential } from 'firebase/auth';
import { auth, rtdb } from '../../firebase';
import { ref, remove } from 'firebase/database';

export default function SystemUpdates({ t }: { t: (key: string) => string }) {
  const [eraseDialogOpen, setEraseDialogOpen] = useState(false);
  const [signOutDialogOpen, setSignOutDialogOpen] = useState(false);
  const [confirmEmailInput, setConfirmEmailInput] = useState('');
  const [erasePassword, setErasePassword] = useState('');
  const [erasing, setErasing] = useState(false);

  const currentUserEmail = auth.currentUser?.email || '';

  const handleEraseApp = async () => {
    if (confirmEmailInput.trim().toLowerCase() !== currentUserEmail.toLowerCase()) {
      thagaval(t('incorrectEmail') || 'Email does not match.', 'warning');
      return;
    }
    if (!erasePassword) {
      thagaval(t('enterPassword') || 'Please enter your password', 'warning');
      return;
    }
    setErasing(true);
    try {
      if (auth.currentUser?.email) {
        const credential = EmailAuthProvider.credential(auth.currentUser.email, erasePassword);
        await reauthenticateWithCredential(auth.currentUser, credential);
      } else {
        throw new Error("No user email found.");
      }

      // Wiping the entire database (safely by deleting top-level nodes instead of root if root is blocked)
      const pathsToClear = [
        'pattiyalkal', 'vanigargal', 'porulgal', 'patrugal', 'thannilai', 'mathirigal',
        'coolie_pattiyalkal', 'coolie_vanigargal', 'coolie_porulgal', 'coolie_patrugal', 'coolie_thannilai',
        'profile', 'meta'
      ];
      await Promise.all(pathsToClear.map(p => remove(ref(rtdb, p))));
      
      thagaval(t('dataErasedSuccess') || 'App data erased.', 'success');
      
      // Clear all local states to ensure a true fresh start
      localStorage.clear();
      sessionStorage.clear();
      
      // Attempt to clear Firebase Auth IndexedDB directly to prevent ghost sessions
      try {
        const dbs = await window.indexedDB.databases();
        dbs.forEach(db => {
          if (db.name && db.name.includes('firebaseLocalStorageDb')) {
            window.indexedDB.deleteDatabase(db.name);
          }
        });
      } catch (e) {
        // ignore error if indexedDB API is not fully supported
      }
      
      try {
        await signOut(auth);
      } catch (e) {
        // ignore sign out error so it doesn't block the reload
      }
      
      // Force hard reload
      window.location.href = '/';
    } catch (error: any) {
      console.error(error);
      if (error.code === 'auth/wrong-password' || error.code === 'auth/invalid-credential') {
        thagaval(t('incorrectPassword') || 'Incorrect password.', 'error');
      } else if (error.code === 'auth/too-many-requests') {
        thagaval(t('tooManyRequests') || 'Too many attempts. Try later.', 'error');
      } else {
        thagaval(t('errorOccurred') || 'Failed to erase data.', 'error');
      }
    }
    setErasing(false);
  };

  const handleSignOut = async () => {
    await signOut(auth);
    window.location.replace('/');
  };

  return (
    <Box sx={{ display: 'flex', flexDirection: 'column', gap: 2 }}>

      <SettingsPillContainer>
        <SettingsRow 
          icon={<SignOut size={20} weight="fill" />} 
          iconColor="monochrome"
          title={t('signOutBtn') !== 'signOutBtn' ? t('signOutBtn') : 'Sign Out'}
          description={t('accountSecurityDesc') !== 'accountSecurityDesc' ? t('accountSecurityDesc') : 'Lock app access'}
          onClick={() => setSignOutDialogOpen(true)}
        />
        <SettingsRow 
          icon={<WarningCircle size={20} weight="fill" />} 
          iconColor="red"
          title={t('eraseAppDataTitle') !== 'eraseAppDataTitle' ? t('eraseAppDataTitle') : 'Erase App Data'}
          description={t('eraseAppDataDesc') !== 'eraseAppDataDesc' ? t('eraseAppDataDesc') : 'Permanently wipe all records'}
          onClick={() => setEraseDialogOpen(true)}
        />
      </SettingsPillContainer>


      {/* Sign Out Dialog */}
      <Dialog open={signOutDialogOpen} onClose={() => setSignOutDialogOpen(false)} maxWidth="xs" fullWidth slotProps={{ paper: { sx: { borderRadius: 3 } } }}>
        <DialogTitle sx={{ fontWeight: 600, pb: 1, fontSize: '1.125rem' }}>{t('signOutConfirmTitle') !== 'signOutConfirmTitle' ? t('signOutConfirmTitle') : 'Sign Out?'}</DialogTitle>
        <DialogContent sx={{ color: 'text.secondary', fontSize: '0.875rem' }}>
          {t('signOutConfirmDesc') !== 'signOutConfirmDesc' ? t('signOutConfirmDesc') : 'Are you sure you want to sign out? You will need your password to access your data again.'}
        </DialogContent>
        <DialogActions sx={{ p: 2, pt: 0 }}>
          <Button onClick={() => setSignOutDialogOpen(false)} sx={{ color: 'text.secondary', fontWeight: 600, borderRadius: 2, fontSize: '0.875rem', textTransform: 'none' }}>
            {t('cancel') !== 'cancel' ? t('cancel') : 'Cancel'}
          </Button>
          <Button onClick={handleSignOut} variant="contained" disableElevation sx={{ bgcolor: 'black', color: 'white', borderRadius: 2, px: 3, fontWeight: 600, fontSize: '0.875rem', textTransform: 'none', '@media (hover: hover)': { '&:hover': { bgcolor: '#333' } } }}>
            {t('signOutBtn') !== 'signOutBtn' ? t('signOutBtn') : 'Sign Out'}
          </Button>
        </DialogActions>
      </Dialog>

      {/* Erase App Data Dialog */}
      <Dialog open={eraseDialogOpen} onClose={() => !erasing && setEraseDialogOpen(false)} maxWidth="xs" fullWidth slotProps={{ paper: { sx: { borderRadius: 3 } } }}>
        <DialogTitle sx={{ fontWeight: 600, pb: 1, fontSize: '1.125rem' }}>
          {t('eraseAppDataTitle') !== 'eraseAppDataTitle' ? t('eraseAppDataTitle') : 'Erase All Data?'}
        </DialogTitle>
        <DialogContent sx={{ pb: 1 }}>
          <TextField
            fullWidth
            type="email"
            size="small"
            variant="outlined"
            disabled={erasing}
            value={confirmEmailInput}
            onChange={(e) => setConfirmEmailInput(e.target.value)}
            placeholder={t('confirmEmailLabel') !== 'confirmEmailLabel' ? t('confirmEmailLabel') : 'Confirm email'}
            sx={{ 
              mt: 1, mb: 2, 
              '& .MuiOutlinedInput-root': { 
                borderRadius: '50px', 
                fontSize: '0.875rem', 
                bgcolor: 'action.selected' 
              }, 
              '& .MuiOutlinedInput-notchedOutline': { 
                border: 'none' 
              } 
            }}
          />

          <TextField
            fullWidth
            type="password"
            size="small"
            variant="outlined"
            disabled={erasing}
            value={erasePassword}
            onChange={(e) => setErasePassword(e.target.value)}
            placeholder={t('password') !== 'password' ? t('password') : 'Password'}
            sx={{ 
              '& .MuiOutlinedInput-root': { 
                borderRadius: '50px', 
                fontSize: '0.875rem', 
                bgcolor: 'action.selected' 
              }, 
              '& .MuiOutlinedInput-notchedOutline': { 
                border: 'none' 
              } 
            }}
          />
        </DialogContent>
        <DialogActions sx={{ p: 2, pt: 0, gap: 1 }}>
          <Button disabled={erasing} onClick={() => setEraseDialogOpen(false)} sx={{ color: 'text.secondary', fontWeight: 600, borderRadius: '50px', px: 3, fontSize: '0.875rem', textTransform: 'none' }}>
            {t('cancel') !== 'cancel' ? t('cancel') : 'Cancel'}
          </Button>
          <Button 
            disabled={erasing || !erasePassword || confirmEmailInput.trim().toLowerCase() !== currentUserEmail.toLowerCase()} 
            onClick={handleEraseApp} 
            variant="contained" 
            disableElevation
            sx={{ bgcolor: 'black', color: 'white', borderRadius: '50px', px: 4, fontWeight: 600, fontSize: '0.875rem', textTransform: 'none', '@media (hover: hover)': { '&:hover': { bgcolor: '#333' } } }}
            startIcon={erasing ? <CircularProgress size={16} color="inherit" /> : undefined}
          >
            {erasing ? (t('erasing') !== 'erasing' ? t('erasing') : 'Erasing...') : (t('eraseDataBtn') !== 'eraseDataBtn' ? t('eraseDataBtn') : 'Erase')}
          </Button>
        </DialogActions>
      </Dialog>
    </Box>
  );
}
