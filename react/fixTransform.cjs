const fs = require('fs');

function updateFile(filePath) {
    let content = fs.readFileSync(filePath, 'utf-8');

    // 1. Update TransformWrapper settings
    const oldWrapper = `<TransformWrapper
            initialScale={0.43}
            minScale={0.43}
            maxScale={3}
            centerOnInit={true}
            centerZoomedOut={true}
            alignmentAnimation={{ sizeX: 0, sizeY: 0 }}
            panning={{ disabled: isZoomedOut }}
            onTransformed={(ref) => {
                const zoomedOut = ref.state.scale <= 0.45;
                if (zoomedOut !== isZoomedOut) {
                    setIsZoomedOut(zoomedOut);
                }
            }}
            onInit={(ref) => {
                const zoomedOut = ref.state.scale <= 0.45;
                if (zoomedOut !== isZoomedOut) {
                    setIsZoomedOut(zoomedOut);
                }
            }}
          >`;

    const newWrapper = `<TransformWrapper
            initialScale={1}
            minScale={1}
            maxScale={5}
            centerOnInit={true}
            panning={{ disabled: isZoomedOut }}
            onTransformed={(ref) => {
                const zoomedOut = ref.state.scale <= 1.05;
                if (zoomedOut !== isZoomedOut) {
                    setIsZoomedOut(zoomedOut);
                }
            }}
            onInit={(ref) => {
                const zoomedOut = ref.state.scale <= 1.05;
                if (zoomedOut !== isZoomedOut) {
                    setIsZoomedOut(zoomedOut);
                }
            }}
          >`;

    if (content.includes(oldWrapper)) {
        content = content.replace(oldWrapper, newWrapper);
    } else {
        console.log('Could not find TransformWrapper block in', filePath);
    }

    // 2. Update Paper sx
    const oldSx = `sx={{ 
                                p: 0, overflow: 'hidden', minWidth: '210mm', width: '210mm', m: '0 auto',
                                zoom: 'none',
                                transformOrigin: 'top center',
                                mb: '-57%'
                            }}`;

    const newSx = `sx={{ 
                                p: 0, overflow: 'hidden', minWidth: '210mm', width: '210mm', m: '0 auto',
                                zoom: 'none',
                                transformOrigin: 'top center',
                                transform: 'scale(0.43)',
                                mb: '-57%'
                            }}`;

    if (content.includes(oldSx)) {
        content = content.replace(oldSx, newSx);
    } else {
        console.log('Could not find sx block in', filePath);
    }

    fs.writeFileSync(filePath, content, 'utf-8');
    console.log('Updated', filePath);
}

updateFile('moolam/pagudhigal/GstBill/InvoiceView.tsx');
updateFile('moolam/pagudhigal/CoolieBill/CoolieInvoiceView.tsx');
