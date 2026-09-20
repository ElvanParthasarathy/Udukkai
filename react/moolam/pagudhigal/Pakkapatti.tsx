// @ts-nocheck
import { SidebarSimple, FileText, GearSix, DownloadSimple, Moon, Sun, CaretDown, CaretRight, Question, Bell, SignOut, CaretUpDown, Invoice, HandCoins } from '@phosphor-icons/react';
import { useState } from 'react';
import { Drawer, List, ListItem, ListItemButton, ListItemIcon, ListItemText, Badge, Box, Typography, Avatar, Divider, Tooltip, IconButton, Collapse, Popover, Switch, Fade } from '@mui/material';
import { useLanguage } from '../mozhi/LanguageContext';

export default function Pakkapatti({
  mobileOpen,
  handleDrawerToggle,
  isCollapsed,
  setIsCollapsed,
  appMode,
  setAppMode,
  onSwitchModeRequest,
  currentView,
  setCurrentView,
  mainNavItems,
  accountingItems,
  reportsItems,
  updateBannerVisible,
  updateInfo,
  setShowUpdateModal,
  darkMode,
  setDarkMode,
  themeMode,
  setThemeMode,
  serverStatus,
  profile,
  allProfiles,
  showProfileMenu,
  setShowProfileMenu,
  handleSwitchProfile
}) {

  const { t, language } = useLanguage();
  const [expandedGroups, setExpandedGroups] = useState({ accounting: false, reports: false });
  const [profileAnchorEl, setProfileAnchorEl] = useState(null);
  const isProfileMenuOpen = Boolean(profileAnchorEl);
  const handleProfileClose = () => setProfileAnchorEl(null);

  const handleThemeChange = (mode: any) => {
    if (setThemeMode) setThemeMode(mode);
  };

  const handleNavClick = (id: any, defaultOnClick?: any) => {
    if (defaultOnClick) defaultOnClick();
    else setCurrentView(id);
    if (mobileOpen && handleDrawerToggle) {
      handleDrawerToggle();
    }
  };

  const drawerContent = (
    <Box sx={{ display: 'flex', flexDirection: 'column', height: '100%' }}>
        {/* Header */}
        <Box sx={{ display: 'flex', alignItems: 'center', flexShrink: 0, minHeight: 80, pt: 3, pb: 1, pl: isCollapsed ? 0 : 4, pr: isCollapsed ? 0 : 3, justifyContent: isCollapsed ? 'center' : 'space-between' }}>
          {isCollapsed ? (
            <Tooltip title={t('hc_openSidebar')} placement="right" arrow>
              <IconButton 
                onClick={() => {
                  setIsCollapsed(false);
                  localStorage.setItem('elvanniril_sidebar_collapsed', 'false');
                }} 
                size="medium" 
                sx={{ 
                  display: { xs: 'none', md: 'inline-flex' }, 
                  color: 'primary.main', 
                  '@media (hover: hover)': { '&:hover': { 
                    backgroundColor: darkMode ? 'rgba(255,255,255,0.08)' : 'rgba(0,0,0,0.04)',
                  } } 
                }}
              >
                <svg style={{ transform: 'scaleX(-1)' }} width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.25" strokeLinecap="round" strokeLinejoin="round">
                  <path d="M10 6L5 9L10 12" />
                  <path d="M14 6H19" />
                  <path d="M14 12H19" />
                  <path d="M5 18H19" />
                </svg>
              </IconButton>
            </Tooltip>
          ) : (
            <Box sx={{ display: 'flex', alignItems: 'center', gap: 1.5 }}>
              <Typography variant="h6" sx={{ userSelect: 'none', letterSpacing: '-0.02em', fontWeight: 800, color: 'primary.main', display: 'flex', alignItems: 'center', lineHeight: 1 }}>
                {t('appName')}
              </Typography>
            </Box>
          )}
          {!isCollapsed && (
            <Tooltip title={t('hc_closeSidebar')} placement="right" arrow>
              <IconButton 
                onClick={() => {
                  const next = !isCollapsed;
                  setIsCollapsed(next);
                  localStorage.setItem('elvanniril_sidebar_collapsed', String(next));
                }} 
                size="medium" 
                sx={{ 
                  display: { xs: 'none', md: 'inline-flex' }, 
                  color: 'primary.main', 
                  '@media (hover: hover)': { '&:hover': { 
                    backgroundColor: darkMode ? 'rgba(255,255,255,0.08)' : 'rgba(0,0,0,0.04)',
                  } } 
                }}
              >
                <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.25" strokeLinecap="round" strokeLinejoin="round">
                  <path d="M10 6L5 9L10 12" />
                  <path d="M14 6H19" />
                  <path d="M14 12H19" />
                  <path d="M5 18H19" />
                </svg>
              </IconButton>
            </Tooltip>
          )}
        </Box>


        {/* Nav */}
        <List sx={{ flex: 1, overflowY: 'auto', px: 1.5, py: 1, '&::-webkit-scrollbar': { display: 'none' } }}>
          {(() => {
            const renderNavItem = (item) => {
              const isSelected = currentView === item.id || 
                (item.id === 'clients' && currentView === 'client-editor') ||
                (item.id === 'inventory' && currentView === 'product-editor') ||
                (item.id === 'invoice-list' && (currentView === 'invoice-editor' || currentView === 'invoice-view')) ||
                (item.id === 'receipts' && currentView === 'receipt-editor');

              return (
              <ListItem key={item.id} disablePadding sx={{ mb: isCollapsed ? 1.5 : 1.25 }}>
                <Tooltip title={isCollapsed ? item.label : ''} placement="right" arrow disableHoverListener={true}>
                  <ListItemButton
                    disableRipple={isCollapsed}
                    selected={isSelected}
                    onClick={() => handleNavClick(item.id, item.onClick)}
                    sx={{
                      borderRadius: isCollapsed ? '16px' : '100px',
                      mx: 0,
                      justifyContent: isCollapsed ? 'center' : 'flex-start',
                      alignItems: 'center',
                      flexDirection: isCollapsed ? 'column' : 'row',
                      px: isCollapsed ? 1 : '18px',
                      py: isCollapsed ? 1.5 : '10px',
                      minHeight: 40,
                      color: darkMode ? '#aaaaaa' : '#666666',
                      transition: 'all 0.2s cubic-bezier(0.2, 0, 0, 1)',
                      '@media (hover: hover)': { '&:hover': {
                        backgroundColor: isCollapsed ? 'transparent' : (darkMode ? 'rgba(255, 255, 255, 0.08)' : 'rgba(0,0,0,0.04)'),
                        color: darkMode ? '#ffffff' : '#000000',
                        '& .MuiListItemIcon-root': { 
                          color: darkMode ? '#ffffff' : '#000000',
                          backgroundColor: isCollapsed ? (isSelected ? (darkMode ? 'rgba(255, 255, 255, 0.2)' : 'rgba(0,0,0,0.12)') : (darkMode ? 'rgba(255, 255, 255, 0.08)' : 'rgba(0,0,0,0.04)')) : 'transparent'
                        }
                      } },
                      '&.Mui-selected': {
                        backgroundColor: isCollapsed ? 'transparent' : (darkMode ? 'rgba(255, 255, 255, 0.1)' : 'rgba(0,0,0,0.06)'),
                        color: darkMode ? '#ffffff' : '#000000',
                        '@media (hover: hover)': { '&:hover': {
                          backgroundColor: isCollapsed ? 'transparent' : (darkMode ? 'rgba(255, 255, 255, 0.15)' : 'rgba(0,0,0,0.09)'),
                          opacity: 1
                        } },
                        '& .MuiListItemIcon-root': { color: darkMode ? '#ffffff' : '#000000' }
                      },
                      '&:active svg': {
                        transform: 'scale(0.85)',
                      },
                      '& svg': {
                        transition: 'transform 0.15s cubic-bezier(0.4, 0, 0.2, 1)',
                      },
                      '& .MuiTouchRipple-child': {
                        backgroundColor: darkMode ? 'rgba(255, 255, 255, 0.12)' : 'rgba(0, 0, 0, 0.2)',
                      }
                    }}
                  >
                    <ListItemIcon sx={{ 
                      mr: isCollapsed ? 0 : 1.25, 
                      mb: isCollapsed ? 1 : 0, 
                      minWidth: 0, 
                      justifyContent: 'center', 
                      alignItems: 'center',
                      color: isSelected ? (darkMode ? '#ffffff' : '#000000') : (darkMode ? '#aaaaaa' : '#666666'), 
                      transition: 'all 0.2s',
                      ...(isCollapsed && {
                        width: 56,
                        height: 32,
                        borderRadius: '16px',
                        backgroundColor: isSelected ? (darkMode ? 'rgba(255, 255, 255, 0.12)' : 'rgba(0,0,0,0.06)') : 'transparent',
                      })
                    }}>
                      <item.icon size={isCollapsed ? 24 : 20} weight={isSelected ? "fill" : "regular"} />
                    </ListItemIcon>
                    {(!isCollapsed || true) && (
                      <ListItemText
                        sx={{ 
                          m: 0, 
                          display: (!isCollapsed || isCollapsed) ? 'block' : 'none',
                          overflow: 'visible'
                        }}
                        primary={
                          <Typography variant="body2" sx={{ 
                            fontWeight: isSelected ? 700 : 500,
                            fontSize: isCollapsed ? '0.65rem' : '0.875rem',
                            textAlign: isCollapsed ? 'center' : 'left',
                            lineHeight: 1.2,
                            position: 'relative',
                            overflow: 'visible',
                            top: isCollapsed ? 0 : '1px', // Push text down a tiny bit to visually center with icon
                            letterSpacing: isCollapsed ? '0.02em' : 'normal',
                          }}>
                            {item.label}
                          </Typography>
                        }
                      />
                    )}
                  </ListItemButton>
                </Tooltip>
              </ListItem>
              );
            };

            return (
              <>
                {mainNavItems.map(renderNavItem)}

                {!isCollapsed && accountingItems.length > 0 && (
                  <>
                    <Box sx={{ mx: 0, px: '18px', py: '6px', mb: 0.5, mt: 1.5 }}>
                      <Typography sx={{ fontSize: '11.5px', fontWeight: 700, color: darkMode ? 'rgba(255,255,255,0.5)' : 'rgba(0,0,0,0.5)', textTransform: 'none', letterSpacing: '-0.1px' }}>
                        {t('accountingGroup')}
                      </Typography>
                    </Box>
                    {accountingItems.map(renderNavItem)}
                  </>
                )}
                {isCollapsed && accountingItems.map(renderNavItem)}

                {appMode === 'GST' && !isCollapsed && reportsItems.length > 0 && (
                  <>
                    <Box sx={{ mx: 0, px: '18px', py: '6px', mb: 0.5, mt: 1.5 }}>
                      <Typography sx={{ fontSize: '11.5px', fontWeight: 700, color: darkMode ? 'rgba(255,255,255,0.5)' : 'rgba(0,0,0,0.5)', textTransform: 'none', letterSpacing: '-0.1px' }}>
                        {t('reportsGroup')}
                      </Typography>
                    </Box>
                    {reportsItems.map(renderNavItem)}
                  </>
                )}
                {appMode === 'GST' && isCollapsed && reportsItems.map(renderNavItem)}
                
              </>
            );
          })()}

          <Box sx={{ height: 16 }} />

          {/* Bottom tools */}
          {updateBannerVisible && (
            <ListItem disablePadding sx={{ mb: 1.25 }}>
              <Tooltip title={isCollapsed ? `Update to v${updateInfo.latest}` : ''} placement="right" arrow>
                <ListItemButton
                  onClick={() => setShowUpdateModal(true)}
                  sx={{
                    borderRadius: isCollapsed ? '16px' : '100px', mx: 0, px: isCollapsed ? 1 : '18px', py: isCollapsed ? 1.5 : '10px', minHeight: 40,
                    justifyContent: isCollapsed ? 'center' : 'flex-start',
                    background: 'var(--info-bg)', border: '1px solid var(--info-border)', color: 'var(--info-text)',
                    '& .MuiTouchRipple-child': {
                      backgroundColor: darkMode ? 'rgba(255, 255, 255, 0.12)' : 'rgba(0, 0, 0, 0.2)',
                    }
                  }}
                >
                  <ListItemIcon sx={{ mr: isCollapsed ? 0 : 1.25, minWidth: 0, justifyContent: 'center', color: 'var(--info-text)' }}>
                    <DownloadSimple size={isCollapsed ? 24 : 20} weight="regular" />
                  </ListItemIcon>
                  {!isCollapsed && (
                    <>
                      <ListItemText primary={<Typography variant="body2" sx={{ fontWeight: 600 }}>`Update to v${updateInfo.latest}`</Typography>} />
                      <Box sx={{ width: 8, height: 8, borderRadius: '50%', background: '#f59e0b', boxShadow: '0 0 0 3px rgba(245,158,11,0.25)', flexShrink: 0 }} />
                    </>
                  )}
                  {isCollapsed && <Box sx={{ position: 'absolute', top: 6, right: 6, width: 8, height: 8, borderRadius: '50%', background: '#f59e0b' }} />}
                </ListItemButton>
              </Tooltip>
            </ListItem>
          )}


        </List>

          {/* Profile & Settings Zone */}
          <Box sx={{ px: 1.5, pb: 2, display: 'flex', flexDirection: 'column', gap: 1 }}>
            {isCollapsed ? (
              <Box 
                sx={{ 
                  borderRadius: '16px', 
                  mx: 'auto',
                  width: 48,
                  display: 'flex',
                  justifyContent: 'center',
                  alignItems: 'center',
                  minHeight: 48,
                }}
              >
                <Tooltip title={t('settings') || 'Settings'} placement="right" arrow slots={{ transition: Fade }}>
                  <IconButton 
                    onClick={() => handleNavClick('settings')}
                    sx={{ 
                      width: 48, 
                      height: 48, 
                      color: currentView === 'settings' ? 'primary.main' : (darkMode ? '#aaaaaa' : '#666666'),
                      '@media (hover: hover)': { '&:hover': {
                        backgroundColor: darkMode ? 'rgba(255,255,255,0.08)' : 'rgba(0,0,0,0.04)',
                        color: darkMode ? '#ffffff' : '#000000'
                      } }
                    }}
                  >
                    <GearSix size={24} weight={currentView === 'settings' ? "fill" : "regular"} />
                  </IconButton>
                </Tooltip>
              </Box>
            ) : (
              <Box sx={{ position: 'relative', width: '100%' }}>
                <ListItemButton
                  component="div"
                  disableRipple
                  onClick={() => { if(onSwitchModeRequest) onSwitchModeRequest(); }}
                  sx={{ 
                    borderRadius: '16px', 
                    width: 'calc(100% - 48px)',
                    display: 'flex',
                    justifyContent: 'flex-start',
                    alignItems: 'center',
                    pl: '18px',
                    pr: '12px',
                    py: '8px',
                    minHeight: 52,
                    userSelect: 'none',
                    transition: 'all 0.2s cubic-bezier(0.2, 0, 0, 1)',
                    '@media (hover: hover)': { '&:hover': {
                      backgroundColor: 'transparent'
                    } }
                  }}
                >
                  <Box 
                    sx={{ 
                      display: 'flex', 
                      alignItems: 'center', 
                      flex: 1, 
                      minWidth: 0, 
                      gap: 1.25,
                      alignSelf: 'stretch',
                      '&:hover .profile-text, &:hover .profile-circle': {
                        color: darkMode ? '#ffffff' : '#000000'
                      }
                    }}
                  >
                    <Box 
                      className="profile-circle"
                      sx={{ 
                      width: 32, 
                      height: 32, 
                      borderRadius: '50%', 
                      bgcolor: darkMode ? 'rgba(255,255,255,0.1)' : 'rgba(0,0,0,0.05)',
                      display: 'flex',
                      alignItems: 'center',
                      justifyContent: 'center',
                      flexShrink: 0,
                      color: darkMode ? '#aaaaaa' : '#666666',
                      transition: 'color 0.2s ease'
                    }}>
                      {appMode === 'COOLIE' ? <HandCoins weight="fill" size={16} /> : <Invoice weight="fill" size={16} />}
                    </Box>

                    <Box sx={{ flex: 1, minWidth: 0, display: 'flex', flexDirection: 'column', justifyContent: 'center' }}>
                      <Typography 
                        className="profile-text"
                        noWrap 
                        variant="body2" 
                        sx={{ 
                        fontWeight: 500, 
                        fontSize: '0.875rem', 
                        lineHeight: 1.2, 
                        color: darkMode ? '#aaaaaa' : '#666666',
                        transition: 'color 0.2s ease',
                        position: 'relative',
                        top: '1px'
                      }}>
                        {appMode === 'COOLIE' ? t('nirilCoolie') : t('nirilSilk')}
                      </Typography>
                    </Box>
                  </Box>
                </ListItemButton>

                <Box sx={{ position: 'absolute', right: '8px', top: '50%', transform: 'translateY(-50%)' }}>
                  <Tooltip title={t('settings') || 'Settings'} placement="top" arrow slots={{ transition: Fade }}>
                    <IconButton 
                      onClick={(e) => { e.stopPropagation(); handleNavClick('settings'); }}
                      size="small"
                      sx={{ 
                        color: 'text.secondary',
                        bgcolor: 'transparent',
                        '@media (hover: hover)': { '&:hover': {
                           bgcolor: darkMode ? 'rgba(255,255,255,0.15)' : 'rgba(0,0,0,0.08)',
                           color: darkMode ? '#ffffff' : '#000000'
                        } }
                      }}
                    >
                      <GearSix size={18} weight="regular" />
                    </IconButton>
                  </Tooltip>
                </Box>
              </Box>
            )}
          </Box>
      </Box>
  );

  return (
    <Box component="nav" sx={{ width: { md: isCollapsed ? 80 : 260 }, flexShrink: { md: 0 } }}>
      <Drawer
        variant="temporary"
        open={mobileOpen}
        onClose={handleDrawerToggle}
        ModalProps={{ keepMounted: true }}
        sx={{
          display: 'none', // Hidden completely on mobile in favor of bottom nav
          '& .MuiDrawer-paper': { 
            boxSizing: 'border-box', 
            width: '85%', 
            maxWidth: 320, 
            backgroundColor: darkMode ? '#111111' : '#FFFFFF', 
            color: darkMode ? '#ffffff' : '#000000', 
            backgroundImage: 'none',
            pt: 'env(safe-area-inset-top, 0px)',
            pb: 'env(safe-area-inset-bottom, 0px)',
          },
          '& .MuiBackdrop-root': { backdropFilter: 'blur(4px)' },
        }}
      >
        {drawerContent}
      </Drawer>
      <Drawer
        variant="permanent"
        sx={{
          width: isCollapsed ? 80 : 260,
          flexShrink: 0,
          display: { xs: 'none', md: 'block' },
          transition: 'width 0.3s cubic-bezier(0.2, 0, 0, 1)',
          [`& .MuiDrawer-paper`]: { 
            width: isCollapsed ? 80 : 260, 
            boxSizing: 'border-box', 
            backgroundColor: darkMode ? '#111111' : '#FFFFFF', 
            color: darkMode ? '#ffffff' : '#000000', 
            borderRight: darkMode ? 'none' : '1px solid #F3F4F6', 
            backgroundImage: 'none',
            transition: 'width 0.3s cubic-bezier(0.2, 0, 0, 1), background-color 0.3s',
            overflowX: 'hidden'
          },
        }}
      >
        {drawerContent}
      </Drawer>
    </Box>
  );
}
