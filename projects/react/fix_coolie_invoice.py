import sys

with open('moolam/pagudhigal/CoolieBill/CoolieInvoiceView.tsx', 'r', encoding='utf-8') as f:
    content = f.read()

target1 = """                {isMobile ? (
                    <div ref={wrapperRef} style={{ width: "100%", overflow: "hidden", touchAction: "none", display: "flex", justifyContent: "center", padding: "0 16px", boxSizing: "border-box" }}>
            <div ref={contentRef} style={{ transformOrigin: "top center", width: "100%" }}>
              <Paper elevation={8} className="invoice-paper print-wrapper" sx={{ 
                                p: 0, overflow: 'hidden', minWidth: '210mm', width: '210mm', m: '0 auto',
                                zoom: 'none',
                                  transformOrigin: 'top left',
                                  transform: `scale(${initialScale})`,
                                  mb: `-${mbPercent}%`,
                                  borderRadius: '12px',
                            }}>

                    <div ref={printRef} className="a4-paper font-tamil" style={{ margin: '0 auto', background: 'white', ...themeStyles }}>"""

replacement1 = """        <div ref={isMobile ? wrapperRef : null} style={isMobile ? { width: "100%", overflow: "hidden", touchAction: "none", display: "flex", justifyContent: "center", padding: "0 16px", boxSizing: "border-box" } : { width: '100%', display: 'flex', justifyContent: 'center' }}>
          <div ref={isMobile ? contentRef : null} style={isMobile ? { transformOrigin: "top center", width: "100%" } : {}}>
            <Paper elevation={isMobile ? 8 : 3} className="invoice-paper print-wrapper" sx={{ 
              p: 0, overflow: 'hidden', minWidth: '210mm', width: '210mm', m: '0 auto', bgcolor: 'white', color: 'black',
              ...(isMobile ? {
                zoom: 'none',
                transformOrigin: 'top left',
                transform: `scale(${initialScale})`,
                mb: `-${mbPercent}%`,
                borderRadius: '12px'
              } : {
                zoom: { xs: 0.43, sm: 0.7, md: 0.85, lg: 1 },
                '@supports not (zoom: 1)': {
                  transformOrigin: 'top center',
                  transform: { xs: 'scale(0.43)', sm: 'scale(0.7)', md: 'scale(0.85)', lg: 'none' },
                  mb: { xs: '-55%', sm: '-25%', md: '-10%', lg: 0 }
                }
              }),
              '@media print': { 
                zoom: '1 !important', 
                transform: 'none !important',
                mb: '0 !important',
                boxShadow: 'none !important',
                bgcolor: 'white !important',
                color: 'black !important',
                borderRadius: '0 !important'
              }
            }}>
                    <div ref={printRef} className="a4-paper font-tamil" style={{ margin: '0 auto', background: 'white', ...themeStyles }}>"""

content = content.replace(target1, replacement1)

target2 = """                    </div>
                            </Paper>
                        </div>
          </div>
                ) : ("""

split_idx = content.find(target2)
if split_idx != -1:
    end_target = "                </Paper>\n                )}"
    end_idx = content.find(end_target, split_idx)
    if end_idx != -1:
        prefix = content[:split_idx]
        suffix = content[end_idx + len(end_target):]
        replacement2 = """                    </div>
            </Paper>
          </div>
        </div>"""
        content = prefix + replacement2 + suffix
        with open('moolam/pagudhigal/CoolieBill/CoolieInvoiceView.tsx', 'w', encoding='utf-8') as f:
            f.write(content)
        print("Success")
    else:
        print("End block not found")
else:
    print("Start block not found")

