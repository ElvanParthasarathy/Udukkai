import re

files = ['moolam/pagudhigal/GstBill/InvoiceView.tsx', 'moolam/pagudhigal/CoolieBill/CoolieInvoiceView.tsx']

for file_path in files:
    with open(file_path, 'r', encoding='utf-8') as f:
        content = f.read()

    # Calculate initial scale inside the component
    if 'const initialScale' not in content:
        content = content.replace("const isMobile = useMediaQuery(theme.breakpoints.down('sm'));", 
                                  "const isMobile = useMediaQuery(theme.breakpoints.down('sm'));\n    const initialScale = typeof window !== 'undefined' ? Math.min(window.innerWidth / 793.7, 1) : 0.43;\n    const mbPercent = (1 - initialScale) * 141;")

    # Update Paper styles
    old_paper = r"zoom: 'none',\s*transformOrigin: 'top center',\s*transform: 'scale\(0.43\)',\s*mb: '-57%'"
    new_paper = "zoom: 'none',\n                                  transformOrigin: 'top center',\n                                  transform: `scale(${initialScale})`,\n                                  mb: `-${mbPercent}%`"
    
    content = re.sub(old_paper, new_paper, content)

    # For CoolieInvoiceView
    old_paper_coolie = r"zoom: 'none',\s*transformOrigin: 'top center',\s*transform: 'scale\(0.43\)',\s*mb: '-50%'"
    new_paper_coolie = "zoom: 'none',\n                                  transformOrigin: 'top center',\n                                  transform: `scale(${initialScale})`,\n                                  mb: `-${mbPercent}%`"
    content = re.sub(old_paper_coolie, new_paper_coolie, content)

    with open(file_path, 'w', encoding='utf-8') as f:
        f.write(content)
    print('Updated ' + file_path)
