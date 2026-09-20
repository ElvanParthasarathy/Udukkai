import fs from 'fs';

let code = fs.readFileSync('src/App.jsx', 'utf8');

// 1. Rewrite the Drawer sx to exactly match Nirvaagi's Drawer paper
code = code.replace(
  /sx={{\s*width:\s*260,\s*flexShrink:\s*0,\s*\[`& \.MuiDrawer-paper`\]:\s*{[^}]*},\s*}}/g,
  `sx={{
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
        }}`
);

// 2. Rewrite the dynamic nav items inside navItems.map
code = code.replace(
  /<ListItem key=\{item\.id\} disablePadding sx=\{\{ mb: 0\.5 \}\}>\s*<ListItemButton[\s\S]*?<\/ListItemButton>\s*<\/ListItem>/g,
  `<ListItem key={item.id} disablePadding sx={{ mb: 0.25 }}>
                <ListItemButton
                  selected={currentView === item.id}
                  onClick={item.onClick || (() => setCurrentView(item.id))}
                  sx={{
                    borderRadius: '24px',
                    mx: 1,
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
                  <ListItemIcon sx={{ mr: 2, minWidth: 0, color: currentView === item.id ? '#312c46' : '#c9c4cf', transition: 'color 0.2s' }}>
                    <item.icon size={22} />
                  </ListItemIcon>
                  <ListItemText 
                    primary={item.label} 
                    primaryTypographyProps={{ fontSize: '0.95rem', fontWeight: currentView === item.id ? 700 : 500 }} 
                  />
                </ListItemButton>
              </ListItem>`
);

// 3. Rewrite the update banner ListItemButton (the warning one)
code = code.replace(
  /sx={{\s*borderRadius:\s*3,\s*margin:\s*'2px 0',\s*padding:\s*'8px 16px',\s*color:\s*'#c9c4cf',\s*transition:\s*'all 0\.2s cubic-bezier\(0\.2, 0, 0, 1\)',\s*'&:hover':\s*{\s*backgroundColor:\s*'rgba\(230, 225, 230, 0\.08\)',\s*color:\s*'#e6e1e6',\s*'& \.MuiListItemIcon-root':\s*{\s*color:\s*'#e6e1e6'\s*}\s*}\s*}}/g,
  `sx={{
                      borderRadius: '24px',
                      mx: 1,
                      px: 2,
                      py: 1,
                      minHeight: 40,
                      background: 'var(--info-bg)',
                      border: '1px solid var(--info-border)',
                      color: 'var(--info-text)',
                    }}`
); // Note: we restore the info-bg to the update banner since it's a special alert banner

// 4. Rewrite the static bottom Nav items
// First, find the specific static ones and apply the new shape
code = code.replace(
  /sx={{\s*borderRadius:\s*3,\s*margin:\s*'2px 0',\s*padding:\s*'8px 16px',\s*color:\s*'#c9c4cf',\s*transition:\s*'all 0\.2s cubic-bezier\(0\.2, 0, 0, 1\)',\s*'&:hover':\s*{\s*backgroundColor:\s*'rgba\(230, 225, 230, 0\.08\)',\s*color:\s*'#e6e1e6',\s*'& \.MuiListItemIcon-root':\s*{\s*color:\s*'#e6e1e6'\s*}\s*}\s*}}/g,
  `sx={{
                    borderRadius: '24px',
                    mx: 1,
                    px: 2,
                    py: 1,
                    minHeight: 40,
                    color: '#c9c4cf',
                    transition: 'all 0.2s cubic-bezier(0.2, 0, 0, 1)',
                    '&:hover': {
                      backgroundColor: 'rgba(230, 225, 230, 0.08)',
                      color: '#e6e1e6',
                      '& .MuiListItemIcon-root': { color: '#e6e1e6' }
                    }
                  }}`
);

// 5. Rewrite the "Settings" button at the very bottom since it has selected states
code = code.replace(
  /sx={{\s*borderRadius:\s*3,\s*margin:\s*'2px 0',\s*padding:\s*'8px 16px',\s*color:\s*'#c9c4cf',\s*transition:\s*'all 0\.2s cubic-bezier\(0\.2, 0, 0, 1\)',\s*'&:hover':\s*{\s*backgroundColor:\s*'rgba\(230, 225, 230, 0\.08\)',\s*color:\s*'#e6e1e6',\s*'& \.MuiListItemIcon-root':\s*{\s*color:\s*'#e6e1e6'\s*}\s*},\s*'&\.Mui-selected':\s*{\s*backgroundColor:\s*'#c9c3dc',\s*color:\s*'#312c46',\s*'&:hover':\s*{\s*backgroundColor:\s*'#c9c3dc',\s*opacity:\s*0\.85\s*},\s*'& \.MuiListItemIcon-root':\s*{\s*color:\s*'#312c46'\s*}\s*}\s*}}/g,
  `sx={{
                    borderRadius: '24px',
                    mx: 1,
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
                  }}`
);

// 6. Rewrite the fixed icons to use `mr: 2`
code = code.replace(
  /sx={{\s*minWidth:\s*40,\s*color:\s*'#c9c4cf',\s*transition:\s*'color 0\.2s'\s*}}/g,
  `sx={{ mr: 2, minWidth: 0, color: '#c9c4cf', transition: 'color 0.2s' }}`
);

// 7. Rewrite the selected Settings icon
code = code.replace(
  /sx={{\s*minWidth:\s*40,\s*color:\s*currentView === 'settings' \? '#312c46' : '#c9c4cf',\s*transition:\s*'color 0\.2s'\s*}}/g,
  `sx={{ mr: 2, minWidth: 0, color: currentView === 'settings' ? '#312c46' : '#c9c4cf', transition: 'color 0.2s' }}`
);

fs.writeFileSync('src/App.jsx', code);
console.log('Done rewriting Navil Nirvaagi style');
