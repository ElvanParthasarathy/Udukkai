const fs = require('fs');

function updateFile(filePath) {
    let content = fs.readFileSync(filePath, 'utf-8');

    const oldSx = `sx={{ 
                                p: 0, overflow: 'hidden', minWidth: '210mm', width: '210mm', m: '0 auto',
                                zoom: 'none',
                                '@supports not (zoom: 1)': {
                                    transformOrigin: 'top center',
                                    transform: 'none',
                                    mb: 0
                                }
                            }}`;

    const oldSxFallback = `sx={{ 
                p: 0, overflow: 'hidden', minWidth: '210mm', width: '210mm', m: '0 auto',
                zoom: 'none',
                '@supports not (zoom: 1)': {
                  transformOrigin: 'top center',
                  transform: 'none',
                  mb: 0
                }
              }}`;

    const newSx = `sx={{ 
                                p: 0, overflow: 'hidden', minWidth: '210mm', width: '210mm', m: '0 auto',
                                zoom: 'none',
                                transformOrigin: 'top center',
                                mb: '-57%'
                            }}`;

    if (content.includes(oldSx)) {
        content = content.replace(oldSx, newSx);
    } else if (content.includes(oldSxFallback)) {
        content = content.replace(oldSxFallback, newSx);
    } else {
        console.log('Could not find sx block in', filePath);
    }

    fs.writeFileSync(filePath, content, 'utf-8');
    console.log('Updated', filePath);
}

updateFile('moolam/pagudhigal/GstBill/InvoiceView.tsx');
updateFile('moolam/pagudhigal/CoolieBill/CoolieInvoiceView.tsx');
