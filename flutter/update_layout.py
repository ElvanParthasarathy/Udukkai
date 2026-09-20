import os

replacements = {
    'MediaQuery.sizeOf(context).width >= 800': 'isDesktopLayoutContext(context)',
    'constraints.maxWidth >= 800': 'isDesktopLayout(constraints.maxWidth)'
}

files_to_check = []
for root, dirs, files in os.walk('lib/src'):
    for file in files:
        if file.endswith('.dart'):
            files_to_check.append(os.path.join(root, file))

modified_count = 0
for file_path in files_to_check:
    with open(file_path, 'r', encoding='utf-8') as f:
        content = f.read()
        
    original = content
    for old, new in replacements.items():
        if old in content:
            content = content.replace(old, new)
            
    if content != original:
        import_stmt = "import 'package:elvan_niril/src/adippadai/vazhikaattal/niril_nav.dart';\n"
        # add import if not exists
        if 'vazhikaattal/niril_nav.dart' not in content:
            # find first import
            lines = content.split('\n')
            for i, line in enumerate(lines):
                if line.startswith('import '):
                    lines.insert(i, import_stmt.strip())
                    content = '\n'.join(lines)
                    break
                    
        with open(file_path, 'w', encoding='utf-8') as f:
            f.write(content)
        modified_count += 1
        print(f'Updated {file_path}')
        
print(f'Total files updated: {modified_count}')
