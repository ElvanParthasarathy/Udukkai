import React, { useState, useEffect } from 'react';
import { useNavigate, useLocation } from 'react-router-dom';
import { Box, Dialog, DialogTitle, DialogContent, DialogActions, TextField, Typography, Button, ButtonBase, Skeleton } from '@mui/material';
import { Buildings, Hash, Cloud, ArrowsClockwise, CaretRight, Invoice, HandCoins, Translate, MapPin, Bank, Palette, PaintBrush, Trash } from '@phosphor-icons/react';
import { LockKeyhole } from 'lucide-react';
import { signOut } from 'firebase/auth';
import { auth } from '../../firebase';
import { motion, AnimatePresence } from 'framer-motion';

import { useLanguage } from '../../mozhi/LanguageContext';
import { getProfile, saveProfile, getAllProfiles, saveBusinessProfile } from '../../Avanam';
import { thagaval } from '../Thagaval';

import GstSettings from '../GstBill/GstSettings';
import CoolieSettings from '../CoolieBill/CoolieSettings';
import SystemUpdates from './SystemUpdates';
import DevTools from './DevTools';
import AppPreferences from './AppPreferences';
import { SubHeader, SettingsGroup, SettingsItem, SettingsDivider } from './SettingsShared';
import { SettingsSection, SettingsRow } from '../ElvanSettingsSection';

import '../../styles/settings/shared.css';
import '../../styles/settings/hub.css';

export default function Amaippugal({ onSaved, appMode, onSwitchModeRequest, darkMode, setDarkMode, themeMode, setThemeMode }: any) {
  const { language, setLanguage, t } = useLanguage();
  const navigate = useNavigate();
  const location = useLocation();
  
  const [profile, setProfile] = useState<any>({});
  const [profileLoaded, setProfileLoaded] = useState(false);
  const [businessProfiles, setBusinessProfiles] = useState([]);
  const [saving, setSaving] = useState(false);
  
  // Storage & Cloud connection states
  const [driveConnected, setDriveConnected] = useState(false);
  const [connecting, setConnecting] = useState(false);

  // New Profile state
  const [showNewProfileModal, setShowNewProfileModal] = useState(false);
  const [newProfileData, setNewProfileData] = useState({ niruvanathinPeyar: '', gstin: '', mugavari: '' });
  const [creatingProfile, setCreatingProfile] = useState(false);

  // Navigation state
  const [isMobile, setIsMobile] = useState(window.innerWidth <= 768);
  const [currentView, setCurrentView] = useState<any>(() => window.innerWidth > 768 ? 0 : 'hub');
  const [activeTab, setActiveTab] = useState<any>('business');

  const showAppVersion = import.meta.env.DEV && !(window as any).Capacitor;

  useEffect(() => {
    const handleResize = () => setIsMobile(window.innerWidth <= 768);
    window.addEventListener('resize', handleResize);
    return () => window.removeEventListener('resize', handleResize);
  }, []);

  useEffect(() => {
    const path = location.pathname;
    if (!path.startsWith('/dashboard/settings')) return;

    const parts = path.split('/');
    const subRoute = parts[3];

    if (!subRoute) {
      if (isMobile) {
        setCurrentView('hub');
      } else {
        setCurrentView(0);
        setActiveTab('business');
      }
      return;
    }

    if (subRoute === 'app-preferences') setCurrentView(1);
    else if (subRoute === 'cloud') setCurrentView(2);
    else if (subRoute === 'updates') setCurrentView(3);
    else if (subRoute === 'devtools') setCurrentView(4);
    else {
      setCurrentView(0);
      if (appMode === 'COOLIE') {
        if (subRoute === 'address') setActiveTab(1);
        else if (subRoute === 'branding') setActiveTab(2);
        else if (subRoute === 'bank') setActiveTab(3);
        else if (subRoute === 'theme') setActiveTab(4);
        else setActiveTab('business');
      } else {
        setActiveTab(subRoute);
      }
    }
  }, [location.pathname, isMobile, appMode]);

  useEffect(() => {
    getProfile((p) => {
      setProfile({
        ...p,
        primaryDataLanguage: p?.primaryDataLanguage || 'Tamil',
        secondaryDataLanguage: p?.secondaryDataLanguage || 'English',
        enableBilingual: p?.enableBilingual !== false
      });
      setProfileLoaded(true);
    }).then(p => {
      setProfile({
        ...p,
        primaryDataLanguage: p?.primaryDataLanguage || 'Tamil',
        secondaryDataLanguage: p?.secondaryDataLanguage || 'English',
        enableBilingual: p?.enableBilingual !== false
      });
      setProfileLoaded(true);
    });
    loadBusinessProfiles();
  }, []);

  const loadBusinessProfiles = async () => setBusinessProfiles(await getAllProfiles(setBusinessProfiles));

  const handleNavigate = (viewId, tab = 'business') => {
    let path = '/dashboard/settings';
    
    if (viewId === 1) path = '/dashboard/settings/app-preferences';
    else if (viewId === 2) path = '/dashboard/settings/cloud';
    else if (viewId === 3) path = '/dashboard/settings/updates';
    else if (viewId === 4) path = '/dashboard/settings/devtools';
    else if (viewId === 0) {
      if (appMode === 'COOLIE') {
        if (tab === 1) path = '/dashboard/settings/address';
        else if (tab === 2) path = '/dashboard/settings/branding';
        else if (tab === 3) path = '/dashboard/settings/bank';
        else if (tab === 4) path = '/dashboard/settings/theme';
        else path = '/dashboard/settings/business';
      } else {
        path = `/dashboard/settings/${tab}`;
      }
    }
    
    // If already inside a sub-setting, replace so we don't stack up history.
    // Only push when navigating from the settings hub into a sub-setting.
    const isAlreadyInSubSetting = location.pathname !== '/dashboard/settings' && location.pathname.startsWith('/dashboard/settings/');
    navigate(path, { replace: isAlreadyInSubSetting });
  };

  const goHub = () => {
    navigate('/dashboard/settings', { replace: true });
  };

  const handleCreateNewProfile = async () => {
    if (!newProfileData.niruvanathinPeyar.trim()) return;
    setCreatingProfile(true);
    try {
      const freshProfile = {
        niruvanathinPeyar: newProfileData.niruvanathinPeyar, niruvanathinPeyarEn: '',
        mugavari: newProfileData.mugavari, mugavariEn: '', oor: '', oorEn: '', maavattam: '', maavattamEn: '', maanilam: '', maanilamEn: '',
        gstin: newProfileData.gstin, pan: '', email: '', tholaipesi: '', mobileNumber: '', vangiPeyar: '', kanakkuEn: '', ifsc: '', swift: '',
        logo: '', wideLogo: '', logoHeight: 48, signature: '', upiId: '',
      };
      
      const saved = await saveBusinessProfile(freshProfile);
      
      // Auto-save current profile before switching
      if (profile.niruvanathinPeyar?.trim()) {
        const existing = businessProfiles.find((p: any) => p.niruvanathinPeyar.trim().toLowerCase() === profile.niruvanathinPeyar.trim().toLowerCase());
        await saveBusinessProfile({ ...profile, id: existing?.id || undefined });
      }
      
      const loaded = { ...saved };
      delete loaded.id;
      setProfile(loaded);
      await saveProfile(loaded);
      if (onSaved) onSaved(loaded);
      
      await loadBusinessProfiles();
      setShowNewProfileModal(false);
      setNewProfileData({ niruvanathinPeyar: '', gstin: '', mugavari: '' });
      thagaval(`Created and switched to ${saved.niruvanathinPeyar}`, 'success');
    } catch (err) {
      thagaval('Failed to create profile', 'error');
    } finally {
      setCreatingProfile(false);
    }
  };

  const variants = {
    enter: (direction: number) => ({ x: direction > 0 ? "100%" : (direction < 0 ? "-100%" : 0), opacity: 0, zIndex: 1 }),
    center: { x: 0, opacity: 1, zIndex: 0 },
    exit: (direction: number) => ({ x: direction < 0 ? "100%" : (direction > 0 ? "-100%" : 0), opacity: 0, zIndex: 0 })
  };
  const transition = { type: "spring", stiffness: 300, damping: 30 };

  const SettingsShimmer = () => (
    <Box sx={{ p: { xs: 2, md: 3 } }}>
      <Skeleton variant="rectangular" height={80} sx={{ borderRadius: 3, mb: 2 }} />
      <Skeleton variant="rectangular" height={80} sx={{ borderRadius: 3, mb: 2 }} />
      <Skeleton variant="rectangular" height={80} sx={{ borderRadius: 3, mb: 2 }} />
    </Box>
  );

  const renderDetailView = () => {
    let content = null;
    let title = "";
    
    if (currentView === 0) { 
      if (appMode === 'COOLIE') {
        if (activeTab === 1) title = t('address');
        else if (activeTab === 2) title = t('contactBank');
        else if (activeTab === 3) title = t('settings');
        else title = t('businessSettings');
      } else {
        if (activeTab === 'bank') title = t('bank');
        else if (activeTab === 'address') title = t('address');
        else if (activeTab === 'branding') title = t('branding');
        else if (activeTab === 'invoice') title = t('invoiceSettings');
        else if (activeTab === 'languages') title = t('languageSettings');
        else title = t('businessSettings');
      }
      content = (
        <Box sx={{ display: 'flex', flexDirection: 'column', gap: 0 }}>
          {appMode === 'COOLIE' ? (
            profileLoaded ? <CoolieSettings activeTab={activeTab} /> : <SettingsShimmer />
          ) : (
            profileLoaded ? (
              <GstSettings 
                profile={profile} 
                setProfile={setProfile} 
                onSaved={onSaved} 
                businessProfiles={businessProfiles} 
                loadBusinessProfiles={loadBusinessProfiles} 
                saving={saving} 
                setSaving={setSaving} 
                activeTab={activeTab}
              />
            ) : <SettingsShimmer />
          )}
        </Box>
      ); 
    }
    else if (currentView === 1) { 
      title = t('appPreferences') || "App Preferences";
      content = <AppPreferences thagaval={thagaval} darkMode={darkMode} setDarkMode={setDarkMode} themeMode={themeMode} setThemeMode={setThemeMode} />;
    }
    else if (currentView === 3) { 
      title = t('systemAndSecurity') || "System & Security";
      content = (
        <Box sx={{ display: 'flex', flexDirection: 'column', gap: 2 }}>
          <SystemUpdates t={t} />
        </Box>
      );
    }
    else if (currentView === 4) {
      title = "Developer Tools";
      content = (
        <Box sx={{ display: 'flex', flexDirection: 'column', gap: 2 }}>
          <DevTools t={t} profile={profile} driveConnected={driveConnected} setDriveConnected={setDriveConnected} connecting={connecting} setConnecting={setConnecting} />
        </Box>
      );
    }

    return (
      <Box sx={{ width: '100%', pb: 0 }}>
        <SubHeader title={title} onBack={goHub} />
        {content}
      </Box>
    );
  };

  const renderHub = () => (
    <div>
       <div className="s2-sub-header" style={{ display: 'none', marginBottom: 24, padding: '0 8px' }}>
           <div className="s2-sub-title">{t('settingsTitle') as string || 'Settings'}</div>
       </div>

       <SettingsSection sx={{ mb: 3 }} paperSx={{ borderRadius: '999px', bgcolor: 'var(--mac-card-bg, #1c1c1e)', position: 'relative', '& > *:not(:last-child)::after': { display: 'none' } }}>
          <ButtonBase 
             onClick={() => handleNavigate(0, 'business')} 
             sx={{ 
                display: 'flex', 
                alignItems: 'center', 
                width: '100%', 
                p: '14px 32px 14px 98px', 
                justifyContent: 'flex-start',
                textAlign: 'left',
                '@media (hover: hover)': {
                  '&:hover': { bgcolor: (theme) => theme.palette.mode === 'dark' ? 'var(--mac-selection-hover, rgba(255, 255, 255, 0.05))' : 'action.hover' },
                },
                transition: 'background-color 0.2s',
             }}
          >
             <Box sx={{ flexGrow: 1, minWidth: 0, pr: 2, height: 64, display: 'flex', flexDirection: 'column', justifyContent: 'center' }}>
                <Typography variant="h6" sx={{ fontWeight: 600 }}>
                   {t('businessSettings')}
                 </Typography>
                <Typography sx={{ fontWeight: 600, fontSize: '0.875rem', color: 'var(--mac-text-secondary)' }}>
                   {appMode === 'COOLIE' ? t('nirilCoolie') : t('nirilSilk')}
                </Typography>
             </Box>
          </ButtonBase>

          <Box 
             onClick={(e) => {
                e.stopPropagation();
                if (onSwitchModeRequest) onSwitchModeRequest('settings');
             }}
             sx={{ 
             position: 'absolute',
             top: 14,
             left: 14,
             zIndex: 2,
             width: 64, height: 64, borderRadius: '50%', overflow: 'hidden',
             display: 'flex', alignItems: 'center', justifyContent: 'center', bgcolor: 'var(--mac-selection-hover)',
             cursor: 'pointer',
             transition: 'transform 0.2s, background-color 0.2s',
             '@media (hover: hover)': { '&:hover': { transform: 'scale(1.05)', bgcolor: (theme) => theme.palette.mode === 'dark' ? 'rgba(255, 255, 255, 0.12)' : 'rgba(0, 0, 0, 0.08)' } },
             '&:active': { transform: 'scale(0.95)' }
          }}>
             {appMode === 'COOLIE' ? (
                <HandCoins weight="fill" size={32} color="var(--mac-text)" />
             ) : (
                <Invoice weight="fill" size={32} color="var(--mac-text)" />
             )}
          </Box>
       </SettingsSection>

        {appMode === 'GST' ? (
          <>
            <SettingsSection>
               <SettingsRow 
                 icon={<MapPin size={20} weight="fill" />} 
                 iconColor="monochrome"
                 title={t('address')} 
                 description={t('companyAddressDetails')} 
                 onClick={() => handleNavigate(0, 'address')} 
              />
              <SettingsRow 
                 icon={<PaintBrush size={20} weight="fill" />} 
                 iconColor="monochrome"
                 title={t('branding')} 
                 description={t('logoSignatures')} 
                 onClick={() => handleNavigate(0, 'branding')} 
              />
              <SettingsRow 
                 icon={<Bank size={20} weight="fill" />} 
                 iconColor="monochrome"
                 title={t('bank')} 
                 description={t('accountNumberIfsc')} 
                 onClick={() => handleNavigate(0, 'bank')} 
              />
            </SettingsSection>
            <SettingsSection>
              <SettingsRow 
                 icon={<Invoice size={20} weight="fill" />} 
                 iconColor="monochrome"
                 title={t('invoiceTab')} 
                 description={t('invoiceStylingOptions')} 
                 onClick={() => handleNavigate(0, 'invoice')} 
              />
              <SettingsRow 
                 icon={<Translate size={20} weight="fill" />} 
                 iconColor="monochrome"
                 title={t('languages')} 
                 description={t('dataEntryLanguages')} 
                 onClick={() => handleNavigate(0, 'languages')} 
              />
            </SettingsSection>
          </>
        ) : (
          <>
            <SettingsSection>
               <SettingsRow 
                  icon={<MapPin size={20} weight="fill" />} 
                  iconColor="monochrome"
                  title={t('address')} 
                  description={t('companyAddressDetails')} 
                  onClick={() => handleNavigate(0, 1)} 
               />
               <SettingsRow 
                  icon={<PaintBrush size={20} weight="fill" />} 
                  iconColor="monochrome"
                  title={t('branding')} 
                  description={t('logoSignatures')} 
                  onClick={() => handleNavigate(0, 2)} 
               />
               <SettingsRow 
                  icon={<Bank size={20} weight="fill" />} 
                  iconColor="monochrome"
                  title={t('bank')} 
                  description={t('accountNumberIfsc')} 
                  onClick={() => handleNavigate(0, 3)} 
               />
            </SettingsSection>
            
            <SettingsSection>
               <SettingsRow 
                  icon={<Palette size={20} weight="fill" />} 
                  iconColor="monochrome"
                  title={t('settings')} 
                  description={t('colorsTheme')} 
                  onClick={() => handleNavigate(0, 4)} 
               />
            </SettingsSection>
          </>
        )}



        <SettingsSection>
          <SettingsRow 
             icon={<Hash size={20} weight="fill" />} 
             iconColor="monochrome"
             title={t('appPreferences')} 
             description={t('uiLanguageTheme')} 
             onClick={() => handleNavigate(1)} 
          />
          <SettingsRow 
             icon={<LockKeyhole size={20} weight="fill" />} 
             iconColor="monochrome"
             title={t('systemAndSecurity') !== 'systemAndSecurity' ? t('systemAndSecurity') : 'System & Security'} 
             description={t('systemSecurityDesc') !== 'systemSecurityDesc' ? t('systemSecurityDesc') : 'Clear cache, Account security, Updates'} 
             onClick={() => handleNavigate(3)} 
          />
        </SettingsSection>

        {showAppVersion && (
          <SettingsSection>
            <SettingsRow 
               icon={<ArrowsClockwise size={20} weight="fill" />} 
               iconColor="blue"
               title="Developer Tools" 
               description="Import/Export and App Version" 
               onClick={() => handleNavigate(4)} 
            />
          </SettingsSection>
        )}
    </div>
  );

  return (
    <div className="s2-page-view">
      <div className="s2-content-grid">
        {/* LEFT: Hub navigation */}
        <div className={`s2-col-left`} style={{ visibility: currentView !== 'hub' && isMobile ? 'hidden' : 'visible' }}>
          {renderHub()}
        </div>

        {/* RIGHT: Detail view */}
        <div className="s2-col-right">
          {isMobile ? (
            <AnimatePresence>
              {currentView !== 'hub' && (
                <Box
                  component={motion.div}
                  initial={{ x: "100vw" }}
                  animate={{ x: 0 }}
                  exit={{ x: "100vw" }}
                  transition={{ duration: 0.45, ease: [0.16, 1, 0.3, 1] }}
                  style={{ willChange: "transform" }}
                  key={currentView}
                  sx={{
                    position: 'fixed',
                    top: 'calc(64px + env(safe-area-inset-top, 0px))',
                    left: 0,
                    right: 0,
                    bottom: 0,
                    borderRadius: 0,
                    bgcolor: (theme) => theme.palette.mode === 'dark' ? '#000000' : '#F3F4F6',
                    zIndex: 50,
                    overflowY: 'auto',
                    overscrollBehavior: 'contain',
                    padding: '8px 12px calc(80px + env(safe-area-inset-bottom, 0px)) 12px',
                    boxShadow: 'none'
                  }}
                >
                  {renderDetailView()}
                </Box>
              )}
            </AnimatePresence>
          ) : (
            currentView !== 'hub' ? renderDetailView() : null
          )}
        </div>
      </div>

      {/* New Profile Modal */}
      <Dialog 
        open={showNewProfileModal} 
        onClose={() => setShowNewProfileModal(false)} 
        maxWidth="sm" 
        fullWidth 
        disableScrollLock
        PaperProps={{
          sx: {
            borderRadius: isMobile ? 0 : 3,
            bgcolor: 'var(--mac-window-bg, #1e1e1e)',
            backgroundImage: 'none'
          }
        }}
      >
        <DialogTitle sx={{ p: 3, pb: 1, fontWeight: 600 }}>{t('createNewBusinessProfile')}</DialogTitle>
        <DialogContent sx={{ p: 3 }}>
          <Box sx={{ display: 'flex', flexDirection: 'column', gap: 3, py: 1 }}>
            <TextField 
              fullWidth autoFocus required
              label={t('businessName')} 
              value={newProfileData.niruvanathinPeyar} 
              onChange={(e) => setNewProfileData(prev => ({ ...prev, niruvanathinPeyar: e.target.value }))} 
            />
          </Box>
        </DialogContent>
        <DialogActions sx={{ p: 3, pt: 1, gap: 1 }}>
          <Button 
            color="inherit" 
            onClick={() => setShowNewProfileModal(false)} 
            disabled={creatingProfile}
            sx={{ px: 3, py: 1, borderRadius: 2 }}
          >
            {t('cancel' as any) || 'Cancel'}
          </Button>
          <Button 
            variant="contained" 
            onClick={handleCreateNewProfile} 
            disabled={creatingProfile || !newProfileData.niruvanathinPeyar.trim()}
            sx={{ px: 4, py: 1, borderRadius: 2 }}
          >
            {creatingProfile ? t('creating') : t('create')}
          </Button>
        </DialogActions>
      </Dialog>
    </div>
  );
}
