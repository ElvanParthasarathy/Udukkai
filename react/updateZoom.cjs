const fs = require('fs');

function updateFile(filePath) {
    let content = fs.readFileSync(filePath, 'utf-8');

    // Add state variable
    if (!content.includes('const [isZoomedOut, setIsZoomedOut]')) {
        content = content.replace(
            "const isMobile = useMediaQuery(theme.breakpoints.down('sm'));",
            "const isMobile = useMediaQuery(theme.breakpoints.down('sm'));\n    const [isZoomedOut, setIsZoomedOut] = useState(true);"
        );
    }

    // Update TransformWrapper
    const oldWrapper = `<TransformWrapper
                        initialScale={0.43}
                        minScale={0.43}
                        maxScale={3}
                        centerOnInit={true}
                        limitToBounds={true}
                    >`;
    
    // Fallback for InvoiceView without indentation
    const oldWrapper2 = `<TransformWrapper
            initialScale={0.43}
            minScale={0.43}
            maxScale={3}
            centerOnInit={true}
            limitToBounds={true}
          >`;

    const newWrapper = `<TransformWrapper
            initialScale={0.43}
            minScale={0.43}
            maxScale={3}
            centerOnInit={true}
            limitToBounds={true}
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

    if (content.includes(oldWrapper)) {
        content = content.replace(oldWrapper, newWrapper);
    } else if (content.includes(oldWrapper2)) {
        content = content.replace(oldWrapper2, newWrapper);
    } else {
        console.log('Could not find TransformWrapper block in', filePath);
    }

    fs.writeFileSync(filePath, content, 'utf-8');
    console.log('Updated', filePath);
}

updateFile('moolam/pagudhigal/GstBill/InvoiceView.tsx');
updateFile('moolam/pagudhigal/CoolieBill/CoolieInvoiceView.tsx');
