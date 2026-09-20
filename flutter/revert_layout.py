import os

replacements = {
    'isDesktopLayoutContext(context)': 'MediaQuery.sizeOf(context).width >= 800',
    'isDesktopLayout(constraints.maxWidth)': 'constraints.maxWidth >= 800'
}

files_to_revert = [
    'lib/src/cheyalpaadugal/niril_kooli/kaatchi/koorugal/elvan_kooli_irumozhi_pulan.dart',
    'lib/src/cheyalpaadugal/niril_kooli/kaatchi/thiraigal/kooli_mugappu_thirai.dart',
    'lib/src/cheyalpaadugal/niril_kooli/kaatchi/thiraigal/amaippugal/kooli_uruvakku_amaippu.dart',
    'lib/src/cheyalpaadugal/niril_pattu/kaatchi/thiraigal/pattu_mugappu_thirai.dart',
    'lib/src/cheyalpaadugal/niril_podhu/kaatchi/thiruthi/elvan_thiruthi_oadu.dart',
    'lib/src/cheyalpaadugal/niril_podhu/kaatchi/thiruthi/koorugal/elvan_thiruthi_paguthi.dart',
    'lib/src/cheyalpaadugal/niril_podhu/kaatchi/thiruthi/koorugal/patru_thiruthi_paguthigal.dart',
    'lib/src/koorugal/podhu_koorugal/elvan_mithakkum_pin_pothan.dart',
    'lib/src/koorugal/pulan_koorugal/elvan_irumozhi_autocomplete.dart',
    'lib/src/koorugal/pulan_koorugal/elvan_irumozhi_pulan.dart',
    'lib/src/cheyalpaadugal/amaippugal/kaatchi/amaippugal_thirai.dart'
]

for file_path in files_to_revert:
    file_path = os.path.normpath(file_path)
    if os.path.exists(file_path):
        with open(file_path, 'r', encoding='utf-8') as f:
            content = f.read()
            
        for old, new in replacements.items():
            if old in content:
                content = content.replace(old, new)
                
        with open(file_path, 'w', encoding='utf-8') as f:
            f.write(content)
        print(f'Reverted {file_path}')
