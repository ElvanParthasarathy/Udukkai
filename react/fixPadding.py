import re

files = ['moolam/pagudhigal/GstBill/SjsTheme.tsx', 'moolam/pagudhigal/GstBill/InvoicePreview.tsx', 'moolam/pagudhigal/CoolieBill/CoolieTheme.tsx', 'moolam/pagudhigal/CoolieBill/CooliePreview.tsx']

for file_path in files:
    try:
        with open(file_path, 'r', encoding='utf-8') as f:
            content = f.read()

        # Remove 1.5rem left/right padding and margins inside #invoice-preview
        content = content.replace('padding-left: 1.5rem;', 'padding-left: 0;')
        content = content.replace('padding-right: 1.5rem;', 'padding-right: 0;')
        content = content.replace('margin: 0 1.5rem 0.5rem 1.5rem !important;', 'margin: 0 0 0.5rem 0 !important;')
        content = content.replace('margin: 0 1.5rem 0.5rem !important;', 'margin: 0 0 0.5rem !important;')
        content = content.replace('padding: 0 1.5rem !important;', 'padding: 0 !important;')
        content = content.replace('margin: 1.5rem 1.5rem 0.5rem !important;', 'margin: 1.5rem 0 0.5rem !important;')
        content = content.replace('padding-left: 1.5rem !important;', 'padding-left: 0 !important;')
        content = content.replace('padding-right: 1.5rem !important;', 'padding-right: 0 !important;')
        content = content.replace('padding: 10px 1.5rem 14px 1.5rem', 'padding: 10px 0 14px 0')

        with open(file_path, 'w', encoding='utf-8') as f:
            f.write(content)
        print('Updated ' + file_path)
    except FileNotFoundError:
        pass
