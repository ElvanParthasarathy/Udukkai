const fs = require('fs');
const filePath = 'moolam/pagudhigal/CoolieBill/CoolieInvoiceView.tsx';
let content = fs.readFileSync(filePath, 'utf-8');

if (!content.includes('TransformWrapper')) {
    content = content.replace(
        "import { Box, Button, IconButton, Paper, ToggleButtonGroup, ToggleButton } from '@mui/material';",
        "import { Box, Button, IconButton, Paper, ToggleButtonGroup, ToggleButton } from '@mui/material';\nimport { useTheme } from '@mui/material/styles';\nimport useMediaQuery from '@mui/material/useMediaQuery';\nimport { TransformWrapper, TransformComponent } from \"react-zoom-pan-pinch\";"
    );
}

if (!content.includes('const isMobile =')) {
    content = content.replace(
        "    const { t } = useLanguage();",
        "    const { t } = useLanguage();\n    const theme = useTheme();\n    const isMobile = useMediaQuery(theme.breakpoints.down('sm'));"
    );
}

const oldBox = '<Box className=\"print-wrapper\" sx={{ flexGrow: 1, display: \'flex\', justifyContent: \'center\', alignItems: \'flex-start\', overflowX: \'hidden\', pb: 4 }}>';
const newBox = '<Box className=\"print-wrapper\" sx={{ flexGrow: 1, display: \'flex\', justifyContent: \'center\', alignItems: \'flex-start\', overflowX: \'hidden\', pb: 4, width: \'100%\' }}>';
content = content.replace(oldBox, newBox);

const paperStart = `                <Paper elevation={3} className=\"invoice-paper print-wrapper\" sx={{ 
                    p: 0, overflow: 'hidden', minWidth: '210mm', width: '210mm', m: '0 auto',
                    zoom: { xs: 0.43, sm: 0.7, md: 0.85, lg: 1 },
                    '@supports not (zoom: 1)': {
                        transformOrigin: 'top center',
                        transform: { xs: 'scale(0.43)', sm: 'scale(0.7)', md: 'scale(0.85)', lg: 'none' },
                        mb: { xs: '-55%', sm: '-25%', md: '-10%', lg: 0 }
                    }
                }}>`;

const paperEnd = `                    </div>
                </Paper>
            </Box>
        </Box>`;

const startIdx = content.indexOf(paperStart) + paperStart.length;
const endIdx = content.indexOf(paperEnd);

if (startIdx > paperStart.length && endIdx > startIdx) {
    const innerContent = content.substring(startIdx, endIdx);
    
    const mobileWrapper = `                {isMobile ? (
                    <TransformWrapper
                        initialScale={0.43}
                        minScale={0.43}
                        maxScale={3}
                        centerOnInit={true}
                        limitToBounds={true}
                    >
                        <TransformComponent wrapperStyle={{ width: \"100%\", display: \"flex\", justifyContent: \"center\" }}>
                            <Paper elevation={3} className=\"invoice-paper print-wrapper\" sx={{ 
                                p: 0, overflow: 'hidden', minWidth: '210mm', width: '210mm', m: '0 auto',
                                zoom: 'none',
                                '@supports not (zoom: 1)': {
                                    transformOrigin: 'top center',
                                    transform: 'none',
                                    mb: 0
                                }
                            }}>
${innerContent}                    </div>
                            </Paper>
                        </TransformComponent>
                    </TransformWrapper>
                ) : (
${paperStart}
${innerContent}                    </div>
                </Paper>
                )}`;
                
    content = content.substring(0, content.indexOf(paperStart)) + mobileWrapper + '\n            </Box>\n        </Box>\n    );\n}';
    fs.writeFileSync(filePath, content, 'utf-8');
    console.log('Successfully updated CoolieInvoiceView.tsx');
} else {
    console.log('Could not find paperStart or paperEnd indices');
    console.log('startIdx:', startIdx, 'endIdx:', endIdx);
}
