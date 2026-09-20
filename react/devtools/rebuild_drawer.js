import fs from 'fs';

let code = fs.readFileSync('src/App.jsx', 'utf8');

// The replacement Drawer block
const newDrawerBlock = `      <Drawer
        variant="permanent"
        sx={{
          width: 280,
          flexShrink: 0,
          display: { xs: 'none', md: 'block' },
          transition: 'width 0.3s cubic-bezier(0.2, 0, 0, 1)',
          [\`& .MuiDrawer-paper\`]: { 
            width: 280, 
            boxSizing: 'border-box', 
            backgroundColor: '#1c1a1f', 
            color: '#e6e1e6', 
            borderRight: 'none', 
            backgroundImage: 'none',
            transition: 'width 0.3s cubic-bezier(0.2, 0, 0, 1)',
            overflowX: 'hidden'
          },
        }}
      >
        <Box className="flex flex-col h-full">
          {/* Header */}
          <Box className="flex items-center h-16 shrink-0" sx={{ px: 3, justifyContent: 'space-between' }}>
            <Box sx={{ display: 'flex', alignItems: 'center', gap: 1.5 }}>
              <FileText size={22} color="#c9c3dc" />
              <Typography variant="h6" className="select-none" sx={{ letterSpacing: '-0.02em', fontWeight: 800 }}>
                GST Billing
              </Typography>
            </Box>
          </Box>

          <Divider sx={{ borderColor: 'rgba(255,255,255,0.08)' }} />

          {/* Nav */}
          <List className="flex-1 overflow-y-auto px-2 py-2" sx={{ '&::-webkit-scrollbar': { display: 'none' } }}>
            {navItems.map(item => (
              <ListItem key={item.id} disablePadding sx={{ mb: 0.25 }}>
                <ListItemButton
                  selected={currentView === item.id}
                  onClick={item.onClick || (() => setCurrentView(item.id))}
                  sx={{
                    borderRadius: '24px',
                    mx: 1,
                    justifyContent: 'flex-start',
                    px: 2,
                    py: 1,
                    minHeight: 40,
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
                  <ListItemIcon sx={{ mr: 2, minWidth: 0, justifyContent: 'center', color: currentView === item.id ? '#312c46' : '#c9c4cf', transition: 'color 0.2s' }}>
                    <item.icon size={22} />
                  </ListItemIcon>
                  <ListItemText
                    primary={<Typography variant="body2" fontWeight={currentView === item.id ? 700 : 500}>{item.label}</Typography>}
                  />
                </ListItemButton>
              </ListItem>
            ))}

            <Box className="h-2" />

            {/* Bottom tools directly mapped */}
            {updateBannerVisible && (
              <ListItem disablePadding sx={{ mb: 0.25 }}>
                <ListItemButton
                  onClick={() => setShowUpdateModal(true)}
                  title={\`v\${updateInfo.latest} is available\`}
                  sx={{
                    borderRadius: '24px',
                    mx: 1, px: 2, py: 1, minHeight: 40,
                    background: 'var(--info-bg)',
                    border: '1px solid var(--info-border)',
                    color: 'var(--info-text)',
                  }}
                >
                  <ListItemIcon sx={{ mr: 2, minWidth: 0, color: 'var(--info-text)' }}>
                    <Download size={22} />
                  </ListItemIcon>
                  <ListItemText
                    primary={<Typography variant="body2" fontWeight={600}>\`Update to v\${updateInfo.latest}\`</Typography>}
                  />
                  <Box sx={{ width: 8, height: 8, borderRadius: '50%', background: '#f59e0b', boxShadow: '0 0 0 3px rgba(245,158,11,0.25)', flexShrink: 0 }} />
                </ListItemButton>
              </ListItem>
            )}

            <ListItem disablePadding sx={{ mb: 0.25 }}>
              <ListItemButton
                onClick={() => setShowNotifs(s => !s)}
                sx={{
                  borderRadius: '24px', mx: 1, px: 2, py: 1, minHeight: 40, color: '#c9c4cf',
                  transition: 'all 0.2s cubic-bezier(0.2, 0, 0, 1)',
                  '&:hover': { backgroundColor: 'rgba(230, 225, 230, 0.08)', color: '#e6e1e6', '& .MuiListItemIcon-root': { color: '#e6e1e6' } }
                }}
              >
                <ListItemIcon sx={{ mr: 2, minWidth: 0, color: '#c9c4cf', transition: 'color 0.2s' }}>
                  <Badge badgeContent={notifTotal} color="error" max={99}>
                    <Bell size={22} />
                  </Badge>
                </ListItemIcon>
                <ListItemText primary={<Typography variant="body2" fontWeight={500}>Notifications</Typography>} />
              </ListItemButton>
            </ListItem>

            <ListItem disablePadding sx={{ mb: 0.25 }}>
              <ListItemButton
                onClick={() => setDarkMode(!darkMode)}
                sx={{
                  borderRadius: '24px', mx: 1, px: 2, py: 1, minHeight: 40, color: '#c9c4cf',
                  transition: 'all 0.2s cubic-bezier(0.2, 0, 0, 1)',
                  '&:hover': { backgroundColor: 'rgba(230, 225, 230, 0.08)', color: '#e6e1e6', '& .MuiListItemIcon-root': { color: '#e6e1e6' } }
                }}
              >
                <ListItemIcon sx={{ mr: 2, minWidth: 0, color: '#c9c4cf', transition: 'color 0.2s' }}>
                  {darkMode ? <Sun size={22} /> : <Moon size={22} />}
                </ListItemIcon>
                <ListItemText primary={<Typography variant="body2" fontWeight={500}>{darkMode ? 'Light Mode' : 'Dark Mode'}</Typography>} />
              </ListItemButton>
            </ListItem>

            <ListItem disablePadding sx={{ mb: 0.25 }}>
              <ListItemButton
                selected={currentView === 'settings'}
                onClick={() => setCurrentView('settings')}
                sx={{
                  borderRadius: '24px', mx: 1, px: 2, py: 1, minHeight: 40, color: '#c9c4cf',
                  transition: 'all 0.2s cubic-bezier(0.2, 0, 0, 1)',
                  '&:hover': { backgroundColor: 'rgba(230, 225, 230, 0.08)', color: '#e6e1e6', '& .MuiListItemIcon-root': { color: '#e6e1e6' } },
                  '&.Mui-selected': { backgroundColor: '#c9c3dc', color: '#312c46', '&:hover': { backgroundColor: '#c9c3dc', opacity: 0.85 }, '& .MuiListItemIcon-root': { color: '#312c46' } }
                }}
              >
                <ListItemIcon sx={{ mr: 2, minWidth: 0, color: currentView === 'settings' ? '#312c46' : '#c9c4cf', transition: 'color 0.2s' }}>
                  <Badge color="warning" variant="dot" invisible={!updateBannerVisible}>
                    <Settings size={22} />
                  </Badge>
                </ListItemIcon>
                <ListItemText primary={<Typography variant="body2" fontWeight={currentView === 'settings' ? 700 : 500}>Settings</Typography>} />
              </ListItemButton>
            </ListItem>
            <div className={\`server-status server-status-\${serverStatus}\`} style={{ marginTop: '1rem', marginLeft: '1.5rem', marginBottom: '1rem' }}>
              <span className="server-status-dot" />
              <span className="server-status-text">
                {serverStatus === 'online' ? 'Connected' : serverStatus === 'offline' ? 'Offline mode' : 'Connecting...'}
              </span>
            </div>
          </List>

          <Divider sx={{ borderColor: 'rgba(255,255,255,0.08)' }} />

          {/* Profile Zone */}
          <Box className="p-2 pb-3">
            <ListItemButton
              onClick={() => allProfiles.length > 1 && setShowProfileMenu(v => !v)}
              sx={{ borderRadius: '24px', mx: 1, justifyContent: 'flex-start', px: 2 }}
            >
              <Avatar
                sx={{
                  width: 32, height: 32,
                  bgcolor: '#312c46',
                  color: '#c9c3dc',
                  mr: 1.5,
                  fontSize: '0.85rem',
                  fontWeight: 700,
                }}
              >
                {profile?.businessName ? profile.businessName.charAt(0).toUpperCase() : 'M'}
              </Avatar>
              <ListItemText
                primary={<Typography variant="body2" sx={{ fontWeight: 600 }}>{profile?.businessName || 'My Business'}</Typography>}
                secondary={<Typography variant="caption" color="text.secondary">Business Profile</Typography>}
              />
              {allProfiles.length > 1 && <ChevronDown size={16} color="#c9c4cf" />}
            </ListItemButton>
            
            {showProfileMenu && (
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
          </Box>
        </Box>
      </Drawer>`;

// Regex to replace the entire <Drawer>...</Drawer> block
// Since the block contains many nested elements, we'll find the start and end carefully
const drawerStartRegex = /<Drawer\s+variant="permanent"[\s\S]*?sx=\{\{[\s\S]*?\}\}\s*>/;
const drawerEndStr = '      </Drawer>';

const startIndex = code.search(drawerStartRegex);
if (startIndex !== -1) {
    const startOfEnd = code.indexOf(drawerEndStr, startIndex);
    if (startOfEnd !== -1) {
        const endIndex = startOfEnd + drawerEndStr.length;
        code = code.substring(0, startIndex) + newDrawerBlock + code.substring(endIndex);
        
        fs.writeFileSync('src/App.jsx', code);
        console.log('Successfully replaced Drawer block with exact Nirvaagi structure.');
    } else {
        console.error('Could not find </Drawer>');
    }
} else {
    console.error('Could not find <Drawer variant="permanent"');
}
