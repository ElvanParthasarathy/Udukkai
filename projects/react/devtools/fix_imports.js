import fs from 'fs';

let code = fs.readFileSync('src/App.jsx', 'utf8');

code = code.replace(
  /import { Drawer, List, ListItem, ListItemButton, ListItemIcon, ListItemText, Badge, Box } from '@mui\/material';/,
  `import { Drawer, List, ListItem, ListItemButton, ListItemIcon, ListItemText, Badge, Box, Typography, Avatar, Divider } from '@mui/material';`
);

fs.writeFileSync('src/App.jsx', code);
console.log('Added Typography, Avatar, Divider to MUI imports');
