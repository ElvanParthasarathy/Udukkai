import re

files = ['moolam/pagudhigal/GstBill/InvoiceView.tsx', 'moolam/pagudhigal/CoolieBill/CoolieInvoiceView.tsx']

for file_path in files:
    with open(file_path, 'r', encoding='utf-8') as f:
        content = f.read()

    # 1. Add import
    if 'usePinchZoom' not in content:
        content = content.replace('import { useTheme } from', "import { usePinchZoom } from '../../hooks/usePinchZoom';\nimport { useTheme } from")
    
    # Remove TransformWrapper import
    content = re.sub(r'import \{ TransformWrapper, TransformComponent \} from ["\']react-zoom-pan-pinch["\'];\n', '', content)

    # 2. Add hook call
    hook_str = '  const { wrapperRef, contentRef } = usePinchZoom({ minScale: 1, maxScale: 4 });\n\n  useEffect(() => {'
    if 'const { wrapperRef, contentRef }' not in content:
        content = content.replace('  useEffect(() => {', hook_str, 1)

    # Remove isZoomedOut state
    content = re.sub(r'\s*const \[isZoomedOut, setIsZoomedOut\] = useState\(true\);\n', '\n', content)

    # 3. Replace TransformWrapper markup
    wrapper_start = r'<TransformWrapper[^>]*>[\s\S]*?<TransformComponent[^>]*>'
    wrapper_end = r'</TransformComponent>\s*</TransformWrapper>'
    
    replacement_start = '<div ref={wrapperRef} style={{ width: "100%", overflowX: "hidden", touchAction: "pan-y" }}>\n            <div ref={contentRef} style={{ transformOrigin: "top center" }}>'
    replacement_end = '</div>\n          </div>'
    
    content = re.sub(wrapper_start, replacement_start, content)
    content = re.sub(wrapper_end, replacement_end, content)

    with open(file_path, 'w', encoding='utf-8') as f:
        f.write(content)
    print('Updated ' + file_path)
