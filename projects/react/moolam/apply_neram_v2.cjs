const fs = require('fs');

let code = fs.readFileSync('pagudhigal/Amaippugal.tsx', 'utf8');

// 1. Extract the inner contents of each tab
function extractTab(tabId) {
    const startMarker = `{activeTab === ${tabId} && (\n              <Box sx={{ display: 'flex', flexDirection: 'column', gap: 0 }}>\n`;
    const endMarker = `\n              </Box>\n            )}`;
    const start = code.indexOf(startMarker);
    const end = code.indexOf(endMarker, start);
    if (start === -1 || end === -1) {
        throw new Error(`Could not find tab ${tabId}`);
    }
    return code.substring(start + startMarker.length, end);
}

const t0 = extractTab(0);
const t1 = extractTab(1);
const t2 = extractTab(2);
const t3 = extractTab(3);
const t4 = extractTab(4);

// 2. Remove the old Grid layout and return block entirely
const uiStart = code.indexOf(`  return (\n    <Box sx={{ maxWidth: 1200,`);
const uiEnd = code.indexOf(`      </Grid>\n\n      {/* ----------------------- Export modal`);

if (uiStart === -1 || uiEnd === -1) throw new Error("Could not find uiStart/End");

const dialogsAndEnd = code.substring(uiEnd + `      </Grid>\n\n`.length);

// 3. Inject new header imports and state
let newHeader = code.substring(0, uiStart);
if (!newHeader.includes('framer-motion')) {
  newHeader = newHeader.replace(
    "import { useLanguage } from '../mozhi/LanguageContext';",
    "import { useLanguage } from '../mozhi/LanguageContext';\nimport { motion, AnimatePresence } from 'framer-motion';\nimport '../styles/settings/shared.css';\nimport '../styles/settings/hub.css';"
  );
}

newHeader = newHeader.replace(
  'const [showMobileField, setShowMobileField] = useState(false);\n  const [activeTab, setActiveTab] = useState(0);',
  `const [showMobileField, setShowMobileField] = useState(false);
  const [activeTab, setActiveTab] = useState(0);
  const [isMobile, setIsMobile] = useState(window.innerWidth <= 768);
  const [direction, setDirection] = useState(0);
  const [currentView, setCurrentView] = useState(window.innerWidth <= 768 ? 'hub' : 0);
  
  useEffect(() => {
    const handleResize = () => {
        const mobile = window.innerWidth <= 768;
        setIsMobile(mobile);
        if (!mobile && currentView === 'hub') setCurrentView(0);
    };
    window.addEventListener('resize', handleResize);
    return () => window.removeEventListener('resize', handleResize);
  }, [currentView]);

  const handleNavigate = (viewId) => {
    setDirection(1);
    setCurrentView(viewId);
  };

  const goHub = () => {
    setDirection(-1);
    setCurrentView(isMobile ? 'hub' : 0);
  };`
);


// 4. Build the new render block
const neramReturn = `
  const variants = {
    enter: (direction) => ({ x: direction > 0 ? "100%" : (direction < 0 ? "-100%" : 0), opacity: 0, zIndex: 1 }),
    center: { x: 0, opacity: 1, zIndex: 0 },
    exit: (direction) => ({ x: direction < 0 ? "100%" : (direction > 0 ? "-100%" : 0), opacity: 0, zIndex: 0 })
  };
  const transition = { type: "spring", stiffness: 300, damping: 30 };

  const renderDetailView = () => {
    let content = null;
    let title = "";
    if (currentView === 0) { content = (<Box sx={{ display: 'flex', flexDirection: 'column', gap: 0 }}>${t0}</Box>); title = "Profile & Accounts"; }
    else if (currentView === 1) { content = (<Box sx={{ display: 'flex', flexDirection: 'column', gap: 0 }}>${t1}</Box>); title = "Display & Languages"; }
    else if (currentView === 2) { content = (<Box sx={{ display: 'flex', flexDirection: 'column', gap: 0 }}>${t2}</Box>); title = "Billing & Payments"; }
    else if (currentView === 3) { content = (<Box sx={{ display: 'flex', flexDirection: 'column', gap: 0 }}>${t3}</Box>); title = "Storage & Cloud"; }
    else if (currentView === 4) { content = (<Box sx={{ display: 'flex', flexDirection: 'column', gap: 0 }}>${t4}</Box>); title = "System & Updates"; }

    return (
      <Box sx={{ width: '100%', pb: 10 }}>
        {isMobile && (
           <div className="s2-sub-header" style={{ display: 'flex', alignItems: 'center', marginBottom: 24, padding: '0 8px' }}>
              <button className="s2-back-btn" onClick={goHub} type="button">
                  <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                      <path d="M15 18l-6-6 6-6" strokeLinecap="round" strokeLinejoin="round"/>
                  </svg>
              </button>
              <div className="s2-sub-title" style={{ fontSize: 22, marginLeft: 8 }}>{title}</div>
           </div>
        )}
        {!isMobile && (
           <div className="s2-sub-header" style={{ display: 'flex', alignItems: 'center', marginBottom: 24 }}>
              <div className="s2-sub-title">{title}</div>
           </div>
        )}
        {content}
      </Box>
    );
  };

  const detailContent = renderDetailView();

  const renderHub = () => (
    <div style={{ paddingBottom: 100 }}>
       <div className="s2-sub-header" style={{ display: isMobile ? 'flex' : 'none', marginBottom: 24, padding: '0 8px' }}>
           <div className="s2-sub-title">{t('settingsTitle')}</div>
       </div>

       <div className="s2-profile-card" onClick={() => handleNavigate(0)} style={{ marginBottom: 32 }}>
          <div className="s2-avatar">
              {profile.logo ? <img src={profile.logo} alt="Logo" /> : <Business className="s2-avatar-icon" />}
          </div>
          <div className="s2-profile-text">
              <div className="s2-profile-title">{profile.niruvanathinPeyar || 'Business Profile'}</div>
              <div className="s2-profile-sub">{profile.email || 'Configure your details'}</div>
          </div>
          <div style={{ color: 'var(--mac-text-secondary)', paddingRight: 8 }}>
              <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                  <path d="M9 18l6-6-6-6" strokeLinecap="round" strokeLinejoin="round"/>
              </svg>
          </div>
       </div>

       <div className="s2-section-label">Preferences</div>
       <div className="s2-group" style={{ marginBottom: 32 }}>
          <div className="s2-item" onClick={() => handleNavigate(1)}>
              <div className="s2-icon-circle blue"><Tag fontSize="small" /></div>
              <div className="s2-item-text">
                  <div className="s2-item-title">Display & Languages</div>
                  <div className="s2-item-desc">UI language, Data languages</div>
              </div>
          </div>
          <div className="s2-divider"></div>
          <div className="s2-item" onClick={() => handleNavigate(2)}>
              <div className="s2-icon-circle green"><Tag fontSize="small" /></div>
              <div className="s2-item-text">
                  <div className="s2-item-title">Billing & Payments</div>
                  <div className="s2-item-desc">Accounts, Invoice formats, Terms</div>
              </div>
          </div>
       </div>

       <div className="s2-section-label">Advanced</div>
       <div className="s2-group" style={{ marginBottom: 32 }}>
          <div className="s2-item" onClick={() => handleNavigate(3)}>
              <div className="s2-icon-circle purple"><Cloud fontSize="small" /></div>
              <div className="s2-item-text">
                  <div className="s2-item-title">Storage & Cloud</div>
                  <div className="s2-item-desc">Google Drive, Local Backups</div>
              </div>
          </div>
          <div className="s2-divider"></div>
          <div className="s2-item" onClick={() => handleNavigate(4)}>
              <div className="s2-icon-circle orange"><Refresh fontSize="small" /></div>
              <div className="s2-item-text">
                  <div className="s2-item-title">System & Updates</div>
                  <div className="s2-item-desc">App versions, Cache</div>
              </div>
          </div>
       </div>
    </div>
  );

  return (
    <div className="s2-page-view" style={isMobile ? { position: 'relative', overflow: 'hidden', height: 'calc(100vh - 60px)', paddingTop: 16 } : { paddingTop: 24 }}>
      {isMobile ? (
          <AnimatePresence mode="popLayout" custom={direction} initial={false}>
              {currentView === 'hub' ? (
                  <motion.div
                      key="hub"
                      custom={direction}
                      variants={variants}
                      initial="enter"
                      animate="center"
                      exit="exit"
                      transition={transition}
                      className="s2-col-left"
                      style={{ width: '100%', height: '100%' }}
                  >
                      {renderHub()}
                  </motion.div>
              ) : (
                  <motion.div
                      key="detail"
                      custom={direction}
                      variants={variants}
                      initial="enter"
                      animate="center"
                      exit="exit"
                      transition={transition}
                      className="s2-col-right"
                      style={{ width: '100%', height: '100%' }}
                  >
                      {detailContent}
                  </motion.div>
              )}
          </AnimatePresence>
      ) : (
          <div className="s2-content-grid">
              <div className="s2-col-left" style={{ paddingRight: 16 }}>
                  {renderHub()}
              </div>
              <div className="s2-col-right" style={{ paddingLeft: 16, borderLeft: '1px solid var(--mac-divider)' }}>
                  {detailContent || <div className="s2-welcome-card">Select a setting</div>}
              </div>
          </div>
      )}

${dialogsAndEnd}`;

fs.writeFileSync('pagudhigal/Amaippugal.tsx', newHeader + neramReturn);
console.log("Success! Applied Neram styling v2.");
