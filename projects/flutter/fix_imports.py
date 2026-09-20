import os, glob

base_dir = r'D:\Projects\Elvan Niril\flutter\lib\src\features'
files = glob.glob(base_dir + r'\niril_coolie\presentation\pages\*.dart') + \
        glob.glob(base_dir + r'\niril_silk\presentation\pages\*.dart')

for file in files:
    with open(file, 'r', encoding='utf-8') as f:
        content = f.read()
    
    old_import = "import '../../../../shell/presentation/widgets/elvan_responsive_grid.dart';"
    new_import = "import '../../../shell/presentation/widgets/elvan_responsive_grid.dart';"
    content = content.replace(old_import, new_import)
    
    with open(file, 'w', encoding='utf-8') as f:
        f.write(content)

print('Done fixing imports')
