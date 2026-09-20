const fs = require('fs');

let lines = fs.readFileSync('pagudhigal/Amaippugal.tsx', 'utf8').split('\n');

const getLines = (start, end) => lines.slice(start - 1, end).join('\n');

// Extract sections
const dataLang = getLines(488, 581);
const uiLang = getLines(584, 628);

const busProfileHeader = getLines(631, 649); // Company Details Title and Edit buttons
const busProfileFields = getLines(650, 829);
const paymentAccs = getLines(831, 983);
const invFormat = getLines(985, 1055);
const rcpFormat = getLines(1056, 1126);
const branding = getLines(1127, 1184);
const saveButtonBox = getLines(1185, 1189);

const advSettings = getLines(1192, 1240);
const terms = getLines(1242, 1267);
const multiBus = getLines(1268, 1328);
const appUpdates = getLines(1329, 1378);
const dataMgmt = getLines(1379, 1403);
const dialogsAndEnd = getLines(1404, lines.length);

const headerAndState = getLines(1, 487);

// 1. Inject activeTab state if not present
let newHeader = headerAndState;
if (!newHeader.includes('const [activeTab, setActiveTab]')) {
  newHeader = newHeader.replace(
    'const [showMobileField, setShowMobileField] = useState(false);',
    'const [showMobileField, setShowMobileField] = useState(false);\n  const [activeTab, setActiveTab] = useState(0);'
  );
}

// 2. Change the return box to the new tabbed layout
newHeader = newHeader.replace(
  `  return (
    <Box sx={{ maxWidth: 1200, mx: 'auto', p: { xs: 2, md: 4 } }}>
      <Box sx={{ mb: 4 }}>
        <Typography variant="h4" style={{ fontWeight: 'bold' }} color="text.primary" gutterBottom>
          {t('settingsTitle')}
        </Typography>
        <Typography variant="body1" color="text.secondary">
          {t('settingsSubtitle')}
        </Typography>
      </Box>`,
  `  return (
    <Box sx={{ maxWidth: 1200, mx: 'auto', p: { xs: 0, md: 4 } }}>
      <Box sx={{ px: { xs: 2, md: 0 }, pt: { xs: 2, md: 0 }, mb: { xs: 2, md: 4 } }}>
        <Typography variant="h4" style={{ fontWeight: 'bold' }} color="text.primary" gutterBottom>
          {t('settingsTitle')}
        </Typography>
        <Typography variant="body1" color="text.secondary">
          {t('settingsSubtitle')}
        </Typography>
      </Box>

      <Grid container spacing={3}>
        {/* Left Sidebar / Top Tabs */}
        <Grid size={{ xs: 12, md: 3 }}>
          <Paper elevation={0} sx={{ 
            display: { xs: 'flex', md: 'block' }, 
            overflowX: 'auto', 
            bgcolor: 'transparent',
            borderRadius: { xs: 0, md: 2 }, 
            borderBottom: { xs: 1, md: 0 }, 
            borderColor: 'divider',
            mb: { xs: 2, md: 0 },
            '&::-webkit-scrollbar': { display: 'none' },
            msOverflowStyle: 'none',
            scrollbarWidth: 'none',
          }}>
            {[
              { id: 0, label: 'Profile & Accounts', icon: <Business fontSize="small" /> },
              { id: 1, label: 'Display & Languages', icon: <Tag fontSize="small" /> },
              { id: 2, label: 'Billing & Payments', icon: <Tag fontSize="small" /> },
              { id: 3, label: 'Storage & Cloud', icon: <Cloud fontSize="small" /> },
              { id: 4, label: 'System & Updates', icon: <Refresh fontSize="small" /> },
            ].map(item => (
              <Button
                key={item.id}
                onClick={() => setActiveTab(item.id)}
                fullWidth
                variant={activeTab === item.id ? 'contained' : 'text'}
                color={activeTab === item.id ? 'primary' : 'inherit'}
                startIcon={item.icon}
                sx={{
                  justifyContent: 'flex-start',
                  px: 2, py: 1.5,
                  mb: { xs: 0, md: 0.5 },
                  mr: { xs: 1, md: 0 },
                  borderRadius: 50,
                  flexShrink: 0,
                  whiteSpace: 'nowrap',
                  fontWeight: activeTab === item.id ? 700 : 500,
                  color: activeTab === item.id ? 'primary.contrastText' : 'text.secondary',
                  bgcolor: activeTab === item.id ? 'primary.main' : 'transparent',
                  '&:hover': {
                    bgcolor: activeTab === item.id ? 'primary.dark' : 'action.hover',
                  }
                }}
              >
                {item.label}
              </Button>
            ))}
          </Paper>
        </Grid>

        {/* Right Content Area */}
        <Grid size={{ xs: 12, md: 9 }}>
          <Box sx={{ px: { xs: 2, md: 0 }, pb: 4 }}>`
);


// 3. Assemble the tabs
const paperWrapper = (content) => `
      <Paper elevation={2} sx={{ p: { xs: 2, md: 3 }, mb: { xs: 2, md: 3 }, borderRadius: { xs: 0, sm: 2 }, borderX: { xs: 0, sm: undefined } }}>
${content}
      </Paper>
`;

const formWrapper = (content) => `
      <Paper elevation={2} sx={{ p: { xs: 2, md: 3 }, mb: { xs: 2, md: 3 }, borderRadius: { xs: 0, sm: 2 }, borderX: { xs: 0, sm: undefined } }} component="form" onSubmit={handleSave} ref={companyFormRef}>
${content}
      </Paper>
`;


const tab0 = `
            {activeTab === 0 && (
              <Box sx={{ display: 'flex', flexDirection: 'column', gap: 0 }}>
${formWrapper(busProfileHeader + '\\n' + busProfileFields + '\\n' + branding + '\\n' + saveButtonBox)}
${multiBus}
              </Box>
            )}
`;

const tab1 = `
            {activeTab === 1 && (
              <Box sx={{ display: 'flex', flexDirection: 'column', gap: 0 }}>
${uiLang}
${dataLang}
              </Box>
            )}
`;

const tab2 = `
            {activeTab === 2 && (
              <Box sx={{ display: 'flex', flexDirection: 'column', gap: 0 }}>
${paperWrapper(paymentAccs)}
${paperWrapper(invFormat + '\\n\\n' + rcpFormat)}
${terms}
              </Box>
            )}
`;

const tab3 = `
            {activeTab === 3 && (
              <Box sx={{ display: 'flex', flexDirection: 'column', gap: 0 }}>
${advSettings}
${dataMgmt}
              </Box>
            )}
`;

const tab4 = `
            {activeTab === 4 && (
              <Box sx={{ display: 'flex', flexDirection: 'column', gap: 0 }}>
${appUpdates}
              </Box>
            )}
`;

const endTags = `
          </Box>
        </Grid>
      </Grid>
`;

const finalFile = newHeader + '\n' + tab0 + tab1 + tab2 + tab3 + tab4 + endTags + dialogsAndEnd;

fs.writeFileSync('pagudhigal/Amaippugal.tsx', finalFile);
console.log("Success! File rewritten.");
