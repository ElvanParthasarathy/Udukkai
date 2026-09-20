const fs = require('fs');

function updateFile(filePath) {
    let content = fs.readFileSync(filePath, 'utf-8');

    // Remove the conflicting wrapperStyle flexbox centering
    content = content.replace(
        'wrapperStyle={{ width: "100%", display: "flex", justifyContent: "center" }}',
        'wrapperStyle={{ width: "100%" }}'
    );
    
    // Add alignmentAnimation and centerZoomedOut properties to TransformWrapper to ensure it always stays centered when small
    const oldProps = 'centerOnInit={true}';
    const newProps = 'centerOnInit={true}\n            centerZoomedOut={true}\n            alignmentAnimation={{ sizeX: 0, sizeY: 0 }}';
    
    if (content.includes(oldProps)) {
        content = content.replace(oldProps, newProps);
    }

    fs.writeFileSync(filePath, content, 'utf-8');
    console.log('Updated', filePath);
}

updateFile('moolam/pagudhigal/GstBill/InvoiceView.tsx');
updateFile('moolam/pagudhigal/CoolieBill/CoolieInvoiceView.tsx');
