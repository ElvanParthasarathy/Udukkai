import fs from 'fs';

let code = fs.readFileSync('src/App.jsx', 'utf8');

// 1. State
code = code.replace(
  /const \[darkMode, setDarkMode\] = useState\(\(\) => \{\n    return localStorage\.getItem\('elvanniril_theme'\) === 'dark';\n  \}\);/,
  `const [darkMode, setDarkMode] = useState(() => {
    return localStorage.getItem('elvanniril_theme') === 'dark';
  });
  const [isCollapsed, setIsCollapsed] = useState(() => localStorage.getItem('elvanniril_sidebar_collapsed') === 'true');
  const [isHovered, setIsHovered] = useState(false);`
);

// 2. Icons & Imports
code = code.replace(
  /import { Home, FileText, Settings/g,
  `import { PanelLeftClose, PanelLeftOpen, Home, FileText, Settings`
);

code = code.replace(
  /import { Drawer, List, ListItem, ListItemButton, ListItemIcon, ListItemText, Badge, Box, Typography, Avatar, Divider } from '@mui\/material';/,
  `import { Drawer, List, ListItem, ListItemButton, ListItemIcon, ListItemText, Badge, Box, Typography, Avatar, Divider, Tooltip, IconButton } from '@mui/material';`
);

// 3. New Drawer width var
const newDrawerWidth = `const drawerWidth = (isCollapsed && !isHovered) ? 80 : 280;
  const isCompact = isCollapsed && !isHovered;`;

code = code.replace(
  /return \(\n    <Box sx=\{\{ display: 'flex', height: '100vh', overflow: 'hidden' \}\}>\n      <Drawer\n        variant="permanent"/,
  `${newDrawerWidth}\n  return (
    <Box sx={{ display: 'flex', height: '100vh', overflow: 'hidden' }}>
      <Drawer
        variant="permanent"
        onMouseEnter={() => setIsHovered(true)}
        onMouseLeave={() => setIsHovered(false)}`
);

// 4. Update width properties in Drawer sx
code = code.replace(
  /width: 280,\n          flexShrink: 0,/g,
  `width: drawerWidth,
          flexShrink: 0,`
);
code = code.replace(
  /width: 280, \n            boxSizing: 'border-box',/g,
  `width: drawerWidth, 
            boxSizing: 'border-box',`
);

// 5. Header replacement
code = code.replace(
  /<Box className="flex items-center h-16 shrink-0" sx=\{\{ px: 3, justifyContent: 'space-between' \}\}>\n            <Box sx=\{\{ display: 'flex', alignItems: 'center', gap: 1\.5 \}\}>\n              <FileText size=\{22\} color="#c9c3dc" \/>\n              <Typography variant="h6" className="select-none" sx=\{\{ letterSpacing: '-0\.02em', fontWeight: 800 \}\}>\n                GST Billing\n              <\/Typography>\n            <\/Box>\n          <\/Box>/g,
  `<Box className="flex items-center h-16 shrink-0" sx={{ px: isCompact ? 0 : 3, justifyContent: isCompact ? 'center' : 'space-between' }}>
            {isCompact ? (
              <FileText size={24} color="#c9c3dc" />
            ) : (
              <Box sx={{ display: 'flex', alignItems: 'center', gap: 1.5 }}>
                <FileText size={24} color="#c9c3dc" />
                <Typography variant="h6" className="select-none" sx={{ letterSpacing: '-0.02em', fontWeight: 800 }}>
                  GST Billing
                </Typography>
              </Box>
            )}
            {!isCompact && (
              <IconButton 
                onClick={() => {
                  const next = !isCollapsed;
                  setIsCollapsed(next);
                  localStorage.setItem('elvanniril_sidebar_collapsed', String(next));
                }} 
                size="small" 
                sx={{ color: 'rgba(255,255,255,0.5)', '&:hover': { color: '#fff' } }}
              >
                {isCollapsed ? <PanelLeftOpen size={18} /> : <PanelLeftClose size={18} />}
              </IconButton>
            )}
          </Box>`
);

// 6. Dynamic nav items
const navItemsMapRegex = /\{navItems\.map\(item => \([\s\S]*?<\/ListItem>\n            \)\)\}/;
const navItemsReplacement = `{navItems.map(item => (
              <ListItem key={item.id} disablePadding sx={{ mb: 0.75 }}>
                <Tooltip title={isCompact ? item.label : ''} placement="right" arrow>
                  <ListItemButton
                    selected={currentView === item.id}
                    onClick={item.onClick || (() => setCurrentView(item.id))}
                    sx={{
                      borderRadius: '24px',
                      mx: isCompact ? 1 : 1.5,
                      justifyContent: isCompact ? 'center' : 'flex-start',
                      px: isCompact ? 0 : 2,
                      py: 1,
                      minHeight: 44,
                      color: '#c9c4cf',
                      transition: 'all 0.2s cubic-bezier(0.2, 0, 0, 1)',
                      '&:hover': {
                        backgroundColor: 'rgba(230, 225, 230, 0.08)',
                        color: '#e6e1e6',
                        '& .MuiListItemIcon-root': { color: '#e6e1e6' }
                      },
                      '&.Mui-selected': {
                        backgroundColor: '#c9c3dc',
                        color: '#312c46',
                        '&:hover': {
                          backgroundColor: '#c9c3dc',
                          opacity: 0.85
                        },
                        '& .MuiListItemIcon-root': { color: '#312c46' }
                      }
                    }}
                  >
                    <ListItemIcon sx={{ mr: isCompact ? 0 : 2, minWidth: 0, justifyContent: 'center', color: currentView === item.id ? '#312c46' : '#c9c4cf', transition: 'color 0.2s' }}>
                      <item.icon size={isCompact ? 24 : 22} />
                    </ListItemIcon>
                    {!isCompact && (
                      <ListItemText
                        primary={<Typography variant="body2" fontWeight={currentView === item.id ? 700 : 500}>{item.label}</Typography>}
                      />
                    )}
                  </ListItemButton>
                </Tooltip>
              </ListItem>
            ))}`;

code = code.replace(navItemsMapRegex, navItemsReplacement);

// 7. UpdateBanner replacement
const updateBannerRegex = /\{updateBannerVisible && \(\n              <ListItem disablePadding sx=\{\{ mb: 0\.25 \}\}>\n                <ListItemButton[\s\S]*?<\/ListItemButton>\n              <\/ListItem>\n            \)\}/;
const updateBannerReplacement = `{updateBannerVisible && (
              <ListItem disablePadding sx={{ mb: 0.75 }}>
                <Tooltip title={isCompact ? \`Update to v\${updateInfo.latest}\` : ''} placement="right" arrow>
                  <ListItemButton
                    onClick={() => setShowUpdateModal(true)}
                    sx={{
                      borderRadius: '24px', mx: isCompact ? 1 : 1.5, px: isCompact ? 0 : 2, py: 1, minHeight: 44,
                      justifyContent: isCompact ? 'center' : 'flex-start',
                      background: 'var(--info-bg)', border: '1px solid var(--info-border)', color: 'var(--info-text)',
                    }}
                  >
                    <ListItemIcon sx={{ mr: isCompact ? 0 : 2, minWidth: 0, justifyContent: 'center', color: 'var(--info-text)' }}>
                      <Download size={isCompact ? 24 : 22} />
                    </ListItemIcon>
                    {!isCompact && (
                      <>
                        <ListItemText primary={<Typography variant="body2" fontWeight={600}>\`Update to v\${updateInfo.latest}\`</Typography>} />
                        <Box sx={{ width: 8, height: 8, borderRadius: '50%', background: '#f59e0b', boxShadow: '0 0 0 3px rgba(245,158,11,0.25)', flexShrink: 0 }} />
                      </>
                    )}
                    {isCompact && <Box sx={{ position: 'absolute', top: 6, right: 6, width: 8, height: 8, borderRadius: '50%', background: '#f59e0b' }} />}
                  </ListItemButton>
                </Tooltip>
              </ListItem>
            )}`;

code = code.replace(updateBannerRegex, updateBannerReplacement);

// 8. Notifications, Dark Mode, Settings
const notifRegex = /<ListItem disablePadding sx=\{\{ mb: 0\.25 \}\}>\n              <ListItemButton\n                onClick=\{\(\) => setShowNotifs\(s => !s\)\}[\s\S]*?<\/ListItemButton>\n            <\/ListItem>/;
const notifReplacement = `<ListItem disablePadding sx={{ mb: 0.75 }}>
              <Tooltip title={isCompact ? 'Notifications' : ''} placement="right" arrow>
                <ListItemButton
                  onClick={() => setShowNotifs(s => !s)}
                  sx={{
                    borderRadius: '24px', mx: isCompact ? 1 : 1.5, justifyContent: isCompact ? 'center' : 'flex-start', px: isCompact ? 0 : 2, py: 1, minHeight: 44, color: '#c9c4cf',
                    transition: 'all 0.2s cubic-bezier(0.2, 0, 0, 1)',
                    '&:hover': { backgroundColor: 'rgba(230, 225, 230, 0.08)', color: '#e6e1e6', '& .MuiListItemIcon-root': { color: '#e6e1e6' } }
                  }}
                >
                  <ListItemIcon sx={{ mr: isCompact ? 0 : 2, minWidth: 0, justifyContent: 'center', color: '#c9c4cf', transition: 'color 0.2s' }}>
                    <Badge badgeContent={notifTotal} color="error" max={99}>
                      <Bell size={isCompact ? 24 : 22} />
                    </Badge>
                  </ListItemIcon>
                  {!isCompact && <ListItemText primary={<Typography variant="body2" fontWeight={500}>Notifications</Typography>} />}
                </ListItemButton>
              </Tooltip>
            </ListItem>`;
code = code.replace(notifRegex, notifReplacement);

const darkRegex = /<ListItem disablePadding sx=\{\{ mb: 0\.25 \}\}>\n              <ListItemButton\n                onClick=\{\(\) => setDarkMode\(!darkMode\)\}[\s\S]*?<\/ListItemButton>\n            <\/ListItem>/;
const darkReplacement = `<ListItem disablePadding sx={{ mb: 0.75 }}>
              <Tooltip title={isCompact ? (darkMode ? 'Light Mode' : 'Dark Mode') : ''} placement="right" arrow>
                <ListItemButton
                  onClick={() => setDarkMode(!darkMode)}
                  sx={{
                    borderRadius: '24px', mx: isCompact ? 1 : 1.5, justifyContent: isCompact ? 'center' : 'flex-start', px: isCompact ? 0 : 2, py: 1, minHeight: 44, color: '#c9c4cf',
                    transition: 'all 0.2s cubic-bezier(0.2, 0, 0, 1)',
                    '&:hover': { backgroundColor: 'rgba(230, 225, 230, 0.08)', color: '#e6e1e6', '& .MuiListItemIcon-root': { color: '#e6e1e6' } }
                  }}
                >
                  <ListItemIcon sx={{ mr: isCompact ? 0 : 2, minWidth: 0, justifyContent: 'center', color: '#c9c4cf', transition: 'color 0.2s' }}>
                    {darkMode ? <Sun size={isCompact ? 24 : 22} /> : <Moon size={isCompact ? 24 : 22} />}
                  </ListItemIcon>
                  {!isCompact && <ListItemText primary={<Typography variant="body2" fontWeight={500}>{darkMode ? 'Light Mode' : 'Dark Mode'}</Typography>} />}
                </ListItemButton>
              </Tooltip>
            </ListItem>`;
code = code.replace(darkRegex, darkReplacement);

const settingsRegex = /<ListItem disablePadding sx=\{\{ mb: 0\.25 \}\}>\n              <ListItemButton\n                selected=\{currentView === 'settings'\}[\s\S]*?<\/ListItemButton>\n            <\/ListItem>/;
const settingsReplacement = `<ListItem disablePadding sx={{ mb: 0.75 }}>
              <Tooltip title={isCompact ? 'Settings' : ''} placement="right" arrow>
                <ListItemButton
                  selected={currentView === 'settings'}
                  onClick={() => setCurrentView('settings')}
                  sx={{
                    borderRadius: '24px', mx: isCompact ? 1 : 1.5, justifyContent: isCompact ? 'center' : 'flex-start', px: isCompact ? 0 : 2, py: 1, minHeight: 44, color: '#c9c4cf',
                    transition: 'all 0.2s cubic-bezier(0.2, 0, 0, 1)',
                    '&:hover': { backgroundColor: 'rgba(230, 225, 230, 0.08)', color: '#e6e1e6', '& .MuiListItemIcon-root': { color: '#e6e1e6' } },
                    '&.Mui-selected': { backgroundColor: '#c9c3dc', color: '#312c46', '&:hover': { backgroundColor: '#c9c3dc', opacity: 0.85 }, '& .MuiListItemIcon-root': { color: '#312c46' } }
                  }}
                >
                  <ListItemIcon sx={{ mr: isCompact ? 0 : 2, minWidth: 0, justifyContent: 'center', color: currentView === 'settings' ? '#312c46' : '#c9c4cf', transition: 'color 0.2s' }}>
                    <Badge color="warning" variant="dot" invisible={!updateBannerVisible}>
                      <Settings size={isCompact ? 24 : 22} />
                    </Badge>
                  </ListItemIcon>
                  {!isCompact && <ListItemText primary={<Typography variant="body2" fontWeight={currentView === 'settings' ? 700 : 500}>Settings</Typography>} />}
                </ListItemButton>
              </Tooltip>
            </ListItem>`;
code = code.replace(settingsRegex, settingsReplacement);

// 9. Server status hide when compact
code = code.replace(
  /<div className=\{\`server-status server-status-\$\{serverStatus\}\`\} style=\{\{ marginTop: '1rem', marginLeft: '1\.5rem', marginBottom: '1rem' \}\}>\n              <span className="server-status-dot" \/>\n              <span className="server-status-text">\n                \{serverStatus === 'online' \? 'Connected' : serverStatus === 'offline' \? 'Offline mode' : 'Connecting\.\.\.'\}\n              <\/span>\n            <\/div>/g,
  `{!isCompact && (
              <div className={\`server-status server-status-\${serverStatus}\`} style={{ marginTop: '1rem', marginLeft: '1.5rem', marginBottom: '1rem' }}>
                <span className="server-status-dot" />
                <span className="server-status-text">
                  {serverStatus === 'online' ? 'Connected' : serverStatus === 'offline' ? 'Offline mode' : 'Connecting...'}
                </span>
              </div>
            )}`
);

// 10. Profile Zone hide when compact
const profileZoneRegex = /<Box className="p-2 pb-3">[\s\S]*?<\/Box>/;
const profileZoneReplacement = `<Box className="p-2 pb-3">
            <ListItemButton
              onClick={() => allProfiles.length > 1 && setShowProfileMenu(v => !v)}
              sx={{ borderRadius: '24px', mx: isCompact ? 0 : 1, justifyContent: isCompact ? 'center' : 'flex-start', px: isCompact ? 0 : 2, py: isCompact ? 1 : undefined, minHeight: isCompact ? 44 : undefined }}
            >
              <Avatar
                sx={{
                  width: 32, height: 32,
                  bgcolor: '#312c46', color: '#c9c3dc',
                  mr: isCompact ? 0 : 1.5,
                  fontSize: '0.85rem', fontWeight: 700,
                }}
              >
                {profile?.businessName ? profile.businessName.charAt(0).toUpperCase() : 'M'}
              </Avatar>
              {!isCompact && (
                <>
                  <ListItemText
                    primary={<Typography variant="body2" sx={{ fontWeight: 600 }}>{profile?.businessName || 'My Business'}</Typography>}
                    secondary={<Typography variant="caption" color="text.secondary">Business Profile</Typography>}
                  />
                  {allProfiles.length > 1 && <ChevronDown size={16} color="#c9c4cf" />}
                </>
              )}
            </ListItemButton>
            
            {(showProfileMenu && !isCompact) && (
              <Box sx={{ mt: 1, mx: 1, p: 1, bgcolor: '#2b2931', borderRadius: '16px', border: '1px solid rgba(255,255,255,0.05)' }}>
                {allProfiles.map(bp => (
                  <ListItemButton
                    key={bp.id || bp.businessName}
                    onClick={() => handleSwitchProfile(bp)}
                    sx={{ borderRadius: '12px', py: 0.5, px: 1.5, mb: 0.5 }}
                  >
                    <Typography variant="body2" color={bp.businessName?.trim().toLowerCase() === profile?.businessName?.trim().toLowerCase() ? '#c9c3dc' : '#e6e1e6'}>
                      {bp.businessName}
                    </Typography>
                  </ListItemButton>
                ))}
                <Divider sx={{ my: 0.5, borderColor: 'rgba(255,255,255,0.05)' }} />
                <ListItemButton
                  onClick={() => { setShowProfileMenu(false); setCurrentView('settings'); }}
                  sx={{ borderRadius: '12px', py: 0.5, px: 1.5 }}
                >
                  <Typography variant="body2" color="text.secondary">Manage profiles...</Typography>
                </ListItemButton>
              </Box>
            )}
          </Box>`;
code = code.replace(profileZoneRegex, profileZoneReplacement);

fs.writeFileSync('src/App.jsx', code);
console.log('Successfully made sidebar collapsible with hover and toggle, and improved spacing.');
