import re

files = ['moolam/pagudhigal/GstBill/InvoiceView.tsx', 'moolam/pagudhigal/CoolieBill/CoolieInvoiceView.tsx']

for file_path in files:
    with open(file_path, 'r', encoding='utf-8') as f:
        content = f.read()

    # Replace the wrapper open tags
    old_code = '<div ref={wrapperRef} style={{ width: "100%", overflowX: "hidden", touchAction: "pan-y", display: "flex", justifyContent: "center" }}>\n              <div ref={contentRef} style={{ transformOrigin: "top center" }}>'
    
    new_code = '<div ref={wrapperRef} style={{ width: "100%", overflowX: "hidden", touchAction: "pan-y" }}>\n              <div ref={contentRef} style={{ transformOrigin: "top center", width: "100%" }}>\n                <div style={{ position: "relative", left: "50%", marginLeft: "-105mm", width: "210mm" }}>'
    
    content = content.replace(old_code, new_code)

    # Replace the wrapper close tags
    old_close = '</Paper>\n              </div>\n            </div>\n          ) : ('
    
    new_close = '</Paper>\n                </div>\n              </div>\n            </div>\n          ) : ('
    
    content = content.replace(old_close, new_close)

    with open(file_path, 'w', encoding='utf-8') as f:
        f.write(content)
    print('Updated ' + file_path)
