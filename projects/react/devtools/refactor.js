import fs from 'fs';
let code = fs.readFileSync('src/App.jsx', 'utf8');

// 1. Imports
code = code.replace(
  "import { List, ListItem, ListItemButton, ListItemIcon, ListItemText, Badge, Box } from '@mui/material';",
  "import { Drawer, List, ListItem, ListItemButton, ListItemIcon, ListItemText, Badge, Box } from '@mui/material';"
);

// 2. Start of Layout
code = code.replace(
  '<div className="app-layout">\n      <div className="sidebar">\n        <div className="sidebar-brand">',
  `<Box sx={{ display: 'flex', height: '100vh', overflow: 'hidden' }}>
      <Drawer
        variant="permanent"
        sx={{
          width: 260,
          flexShrink: 0,
          [\`& .MuiDrawer-paper\`]: { 
            width: 260, 
            boxSizing: 'border-box', 
            backgroundColor: '#0f172a', 
            color: 'white', 
            borderRight: '1px solid rgba(255,255,255,0.06)' 
          },
        }}
      >
        <Box sx={{ padding: '1.75rem 1.25rem 0' }}>
          <div className="sidebar-brand">`
);

// 3. Close the new Box before sidebar-nav
code = code.replace(
  '</div>\n          )}\n        </div>\n\n        <Box className="sidebar-nav"',
  '</div>\n          )}\n        </div>\n        </Box>\n\n        <Box className="sidebar-nav"'
);

// 4. End of Drawer and start of main-content
code = code.replace(
  '</Box>\n        </Box>\n      </div>\n\n      <div className="main-content" onScroll={handleScroll}>',
  '</Box>\n        </Box>\n      </Drawer>\n\n      <Box component="main" className="main-content" onScroll={handleScroll} sx={{ flexGrow: 1, overflowY: \'auto\' }}>'
);

// 5. End of layout
code = code.replace(
  '<ToastContainer />\n    </div>\n  );\n}',
  '<ToastContainer />\n    </Box>\n  );\n}'
);

// 6. Make the pills slimmer
code = code.replaceAll("padding: '10px 20px'", "padding: '6px 16px', minHeight: '36px'");

fs.writeFileSync('src/App.jsx', code);
console.log('App.jsx updated!');
