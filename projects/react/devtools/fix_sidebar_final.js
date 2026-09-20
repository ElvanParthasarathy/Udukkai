import fs from 'fs';

let code = fs.readFileSync('src/App.jsx', 'utf8');

const drawerStartRegex = /<Drawer\s+variant="permanent"[\s\S]*?<\/Drawer>/;

const newDrawerBlock = `<Drawer
        variant="permanent"
        sx={{
          width: isCollapsed ? 80 : 280,
          flexShrink: 0,
          display: { xs: 'none', md: 'block' },
          transition: 'width 0.3s cubic-bezier(0.2, 0, 0, 1)',
          [\`& .MuiDrawer-paper\`]: { 
            width: isCollapsed ? 80 : 280, 
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
          <Box className="flex items-center h-16 shrink-0" sx={{ px: isCollapsed ? 0 : 3, justifyContent: isCollapsed ? 'center' : 'space-between' }}>
            {isCollapsed ? (
              <FileText size={24} color="#c9c3dc" />
            ) : (
              <Box sx={{ display: 'flex', alignItems: 'center', gap: 1.5 }}>
                <FileText size={24} color="#c9c3dc" />
                <Typography variant="h6" className="select-none" sx={{ letterSpacing: '-0.02em', fontWeight: 800 }}>
                  GST Billing
                </Typography>
              </Box>
            )}
            {!isCollapsed && (
              <IconButton 
                onClick={() => {
                  const next = !isCollapsed;
                  setIsCollapsed(next);
                  localStorage.setItem('elvanniril_sidebar_collapsed', String(next));
                }} 
                size="small" 
                sx={{ color: 'rgba(255,255,255,0.5)', '&:hover': { color: '#fff' } }}
              >
                <PanelLeftClose size={18} />
              </IconButton>
            )}
          </Box>

          <Divider sx={{ borderColor: 'rgba(255,255,255,0.08)' }} />

          {/* Nav */}
          <List className="flex-1 overflow-y-auto px-2 py-2" sx={{ '&::-webkit-scrollbar': { display: 'none' } }}>
            {isCollapsed && (
              <ListItem disablePadding sx={{ mb: 1, justifyContent: 'center' }}>
                <IconButton 
                  onClick={() => {
                    setIsCollapsed(false);
                    localStorage.setItem('elvanniril_sidebar_collapsed', 'false');
                  }} 
                  size="small" 
                  sx={{ color: 'rgba(255,255,255,0.5)', '&:hover': { color: '#fff', backgroundColor: 'rgba(255,255,255,0.08)' } }}
                >
                  <PanelLeftOpen size={20} />
                </IconButton>
              </ListItem>
            )}

            {navItems.map(item => (
              <ListItem key={item.id} disablePadding sx={{ mb: 0.25 }}>
                <Tooltip title={isCollapsed ? item.label : ''} placement="right" arrow>
                  <ListItemButton
                    selected={currentView === item.id}
                    onClick={item.onClick || (() => setCurrentView(item.id))}
                    sx={{
                      borderRadius: '24px',
                      mx: isCollapsed ? 1 : 1,
                      justifyContent: isCollapsed ? 'center' : 'flex-start',
                      px: isCollapsed ? 0 : 2,
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
                    <ListItemIcon sx={{ mr: isCollapsed ? 0 : 2, minWidth: 0, justifyContent: 'center', color: currentView === item.id ? '#312c46' : '#c9c4cf', transition: 'color 0.2s' }}>
                      <item.icon size={isCollapsed ? 22 : 22} />
                    </ListItemIcon>
                    {!isCollapsed && (
                      <ListItemText
                        primary={<Typography variant="body2" fontWeight={currentView === item.id ? 700 : 500}>{item.label}</Typography>}
                      />
                    )}
                  </ListItemButton>
                </Tooltip>
              </ListItem>
            ))}

            <Box className="h-4" />

            {/* Bottom tools */}
            {updateBannerVisible && (
              <ListItem disablePadding sx={{ mb: 0.25 }}>
                <Tooltip title={isCollapsed ? \`Update to v\${updateInfo.latest}\` : ''} placement="right" arrow>
                  <ListItemButton
                    onClick={() => setShowUpdateModal(true)}
                    sx={{
                      borderRadius: '24px', mx: isCollapsed ? 1 : 1, px: isCollapsed ? 0 : 2, py: 1, minHeight: 40,
                      justifyContent: isCollapsed ? 'center' : 'flex-start',
                      background: 'var(--info-bg)', border: '1px solid var(--info-border)', color: 'var(--info-text)',
                    }}
                  >
                    <ListItemIcon sx={{ mr: isCollapsed ? 0 : 2, minWidth: 0, justifyContent: 'center', color: 'var(--info-text)' }}>
                      <Download size={isCollapsed ? 22 : 22} />
                    </ListItemIcon>
                    {!isCollapsed && (
                      <>
                        <ListItemText primary={<Typography variant="body2" fontWeight={600}>\`Update to v\${updateInfo.latest}\`</Typography>} />
                        <Box sx={{ width: 8, height: 8, borderRadius: '50%', background: '#f59e0b', boxShadow: '0 0 0 3px rgba(245,158,11,0.25)', flexShrink: 0 }} />
                      </>
                    )}
                    {isCollapsed && <Box sx={{ position: 'absolute', top: 6, right: 6, width: 8, height: 8, borderRadius: '50%', background: '#f59e0b' }} />}
                  </ListItemButton>
                </Tooltip>
              </ListItem>
            )}

            <ListItem disablePadding sx={{ mb: 0.25 }}>
              <Tooltip title={isCollapsed ? 'Notifications' : ''} placement="right" arrow>
                <ListItemButton
                  onClick={() => setShowNotifs(s => !s)}
                  sx={{
                    borderRadius: '24px', mx: isCollapsed ? 1 : 1, justifyContent: isCollapsed ? 'center' : 'flex-start', px: isCollapsed ? 0 : 2, py: 1, minHeight: 40, color: '#c9c4cf',
                    transition: 'all 0.2s cubic-bezier(0.2, 0, 0, 1)',
                    '&:hover': { backgroundColor: 'rgba(230, 225, 230, 0.08)', color: '#e6e1e6', '& .MuiListItemIcon-root': { color: '#e6e1e6' } }
                  }}
                >
                  <ListItemIcon sx={{ mr: isCollapsed ? 0 : 2, minWidth: 0, justifyContent: 'center', color: '#c9c4cf', transition: 'color 0.2s' }}>
                    <Badge badgeContent={notifTotal} color="error" max={99}>
                      <Bell size={isCollapsed ? 22 : 22} />
                    </Badge>
                  </ListItemIcon>
                  {!isCollapsed && <ListItemText primary={<Typography variant="body2" fontWeight={500}>Notifications</Typography>} />}
                </ListItemButton>
              </Tooltip>
            </ListItem>

            <ListItem disablePadding sx={{ mb: 0.25 }}>
              <Tooltip title={isCollapsed ? (darkMode ? 'Light Mode' : 'Dark Mode') : ''} placement="right" arrow>
                <ListItemButton
                  onClick={() => setDarkMode(!darkMode)}
                  sx={{
                    borderRadius: '24px', mx: isCollapsed ? 1 : 1, justifyContent: isCollapsed ? 'center' : 'flex-start', px: isCollapsed ? 0 : 2, py: 1, minHeight: 40, color: '#c9c4cf',
                    transition: 'all 0.2s cubic-bezier(0.2, 0, 0, 1)',
                    '&:hover': { backgroundColor: 'rgba(230, 225, 230, 0.08)', color: '#e6e1e6', '& .MuiListItemIcon-root': { color: '#e6e1e6' } }
                  }}
                >
                  <ListItemIcon sx={{ mr: isCollapsed ? 0 : 2, minWidth: 0, justifyContent: 'center', color: '#c9c4cf', transition: 'color 0.2s' }}>
                    {darkMode ? <Sun size={isCollapsed ? 22 : 22} /> : <Moon size={isCollapsed ? 22 : 22} />}
                  </ListItemIcon>
                  {!isCollapsed && <ListItemText primary={<Typography variant="body2" fontWeight={500}>{darkMode ? 'Light Mode' : 'Dark Mode'}</Typography>} />}
                </ListItemButton>
              </Tooltip>
            </ListItem>

            <ListItem disablePadding sx={{ mb: 0.25 }}>
              <Tooltip title={isCollapsed ? 'Settings' : ''} placement="right" arrow>
                <ListItemButton
                  selected={currentView === 'settings'}
                  onClick={() => setCurrentView('settings')}
                  sx={{
                    borderRadius: '24px', mx: isCollapsed ? 1 : 1, justifyContent: isCollapsed ? 'center' : 'flex-start', px: isCollapsed ? 0 : 2, py: 1, minHeight: 40, color: '#c9c4cf',
                    transition: 'all 0.2s cubic-bezier(0.2, 0, 0, 1)',
                    '&:hover': { backgroundColor: 'rgba(230, 225, 230, 0.08)', color: '#e6e1e6', '& .MuiListItemIcon-root': { color: '#e6e1e6' } },
                    '&.Mui-selected': { backgroundColor: '#c9c3dc', color: '#312c46', '&:hover': { backgroundColor: '#c9c3dc', opacity: 0.85 }, '& .MuiListItemIcon-root': { color: '#312c46' } }
                  }}
                >
                  <ListItemIcon sx={{ mr: isCollapsed ? 0 : 2, minWidth: 0, justifyContent: 'center', color: currentView === 'settings' ? '#312c46' : '#c9c4cf', transition: 'color 0.2s' }}>
                    <Badge color="warning" variant="dot" invisible={!updateBannerVisible}>
                      <Settings size={isCollapsed ? 22 : 22} />
                    </Badge>
                  </ListItemIcon>
                  {!isCollapsed && <ListItemText primary={<Typography variant="body2" fontWeight={currentView === 'settings' ? 700 : 500}>Settings</Typography>} />}
                </ListItemButton>
              </Tooltip>
            </ListItem>

            {!isCollapsed && (
              <div className={\`server-status server-status-\${serverStatus}\`} style={{ marginTop: '1rem', marginLeft: '1.5rem', marginBottom: '1rem' }}>
                <span className="server-status-dot" />
                <span className="server-status-text">
                  {serverStatus === 'online' ? 'Connected' : serverStatus === 'offline' ? 'Offline mode' : 'Connecting...'}
                </span>
              </div>
            )}
          </List>

          <Divider sx={{ borderColor: 'rgba(255,255,255,0.08)' }} />

          {/* Profile Zone */}
          <Box className="p-2 pb-3">
            <ListItemButton
              onClick={() => allProfiles.length > 1 && setShowProfileMenu(v => !v)}
              sx={{ borderRadius: '24px', mx: isCollapsed ? 0 : 1, justifyContent: isCollapsed ? 'center' : 'flex-start', px: isCollapsed ? 0 : 2, py: isCollapsed ? 1 : undefined, minHeight: isCollapsed ? 44 : undefined }}
            >
              <Avatar
                sx={{
                  width: 32, height: 32,
                  bgcolor: '#312c46', color: '#c9c3dc',
                  mr: isCollapsed ? 0 : 1.5,
                  fontSize: '0.85rem', fontWeight: 700,
                }}
              >
                {profile?.businessName ? profile.businessName.charAt(0).toUpperCase() : 'M'}
              </Avatar>
              {!isCollapsed && (
                <>
                  <ListItemText
                    primary={<Typography variant="body2" sx={{ fontWeight: 600, whiteSpace: 'nowrap', overflow: 'hidden', textOverflow: 'ellipsis' }}>{profile?.businessName || 'My Business'}</Typography>}
                    secondary={<Typography variant="caption" color="text.secondary">Business Profile</Typography>}
                  />
                  {allProfiles.length > 1 && <ChevronDown size={16} color="#c9c4cf" />}
                </>
              )}
            </ListItemButton>
            
            {(showProfileMenu && !isCollapsed) && (
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

if (drawerStartRegex.test(code)) {
    code = code.replace(drawerStartRegex, newDrawerBlock);
    
    // Remove unused isCompact and drawerWidth variables
    code = code.replace(/const drawerWidth = \([\s\S]*?;\n/g, '');
    code = code.replace(/const isCompact = [\s\S]*?;\n/g, '');
    
    fs.writeFileSync('src/App.jsx', code);
    console.log('Successfully fixed drawer formatting and collapse behavior!');
} else {
    console.error('Could not find drawer block!');
}
