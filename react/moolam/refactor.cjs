const fs = require('fs');
let code = fs.readFileSync('d:/Projects/Elvan Niril/moolam/pagudhigal/Amaippugal.tsx', 'utf8');

if (!code.includes('ProfileEditor')) {
    code = code.replace("import ElvanBilingualField from './ElvanBilingualField';", "import ElvanBilingualField from './ElvanBilingualField';\nimport ProfileEditor from './ProfileEditor';\nimport { Avatar } from '@mui/material';");
}

if (!code.includes('isEditingProfile')) {
    code = code.replace('const [saving, setSaving] = useState(false);', 'const [saving, setSaving] = useState(false);\n    const [isEditingProfile, setIsEditingProfile] = useState(false);');
}

const startMarker = `if (currentView === 0) { content = (<Box sx={{ display: 'flex', flexDirection: 'column', gap: 0 }}>`;
const endMarker = `} else if (currentView === 1) {`;

const startIdx = code.indexOf(startMarker);
const endIdx = code.indexOf(endMarker);

if (startIdx !== -1 && endIdx !== -1) {
    const newContent = `if (currentView === 0) { 
        content = (
            <Box sx={{ p: 0, mb: 4 }}>
                <Paper elevation={0} sx={{ p: 3, border: '1px solid', borderColor: 'divider', borderRadius: 2, cursor: 'pointer', '&:hover': { bgcolor: 'action.hover' } }} onClick={() => setIsEditingProfile(true)}>
                    <Box sx={{ display: 'flex', alignItems: 'center', gap: 2 }}>
                        {profile.logo ? (
                            <img src={profile.logo} alt="Logo" style={{ maxHeight: 60, objectFit: 'contain' }} />
                        ) : (
                            <Avatar sx={{ width: 60, height: 60, bgcolor: 'primary.main', fontSize: 24 }}>{profile.niruvanathinPeyar?.charAt(0) || 'B'}</Avatar>
                        )}
                        <Box sx={{ flexGrow: 1 }}>
                            <Typography variant="h6" sx={{ fontWeight: 600 }}>{profile.niruvanathinPeyar || 'New Business Profile'}</Typography>
                            <Typography variant="body2" color="text.secondary">{profile.personName} {profile.mobileNumber ? \`| \${profile.mobileNumber}\` : ''}</Typography>
                            <Typography variant="body2" color="text.secondary">{profile.email}</Typography>
                        </Box>
                        <Button variant="outlined" size="small" onClick={(e) => { e.stopPropagation(); setIsEditingProfile(true); }}>Edit Profile</Button>
                    </Box>
                </Paper>
            </Box>
        );
    `;
    code = code.substring(0, startIdx) + newContent + "    " + code.substring(endIdx);
    
    code = code.replace(`return (
    <Box sx={{ flexGrow: 1, bgcolor: '#f9fafb', minHeight: '100vh', pb: 10, fontFamily: "'Inter', sans-serif" }}>`,
    `if (isEditingProfile) {
        return (
            <ProfileEditor 
                profileSettings={profile} 
                onSaved={(newProf) => { setProfile(newProf); setIsEditingProfile(false); }} 
                onBack={() => setIsEditingProfile(false)} 
            />
        );
    }

    return (
    <Box sx={{ flexGrow: 1, bgcolor: '#f9fafb', minHeight: '100vh', pb: 10, fontFamily: "'Inter', sans-serif" }}>`);

    fs.writeFileSync('d:/Projects/Elvan Niril/moolam/pagudhigal/Amaippugal.tsx', code);
    console.log('Refactored Amaippugal.tsx successfully!');
} else {
    console.log('Could not find markers', startIdx, endIdx);
}
