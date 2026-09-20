import os
import re

base_path = r'D:\Projects\Elvan Niril\flutter\lib\src\cheyalpaadugal\amaippugal\kaatchi\koorugal'
pagudhi_path = os.path.join(base_path, 'kanakku_paadhugaappu_pagudhi.dart')

with open(pagudhi_path, 'r', encoding='utf-8') as f:
    content = f.read()

# The methods are at the bottom of the file after the `}` of AccountSecuritySection.
# So we have `}\n  void _showBackupFlow...`
methods_match = re.search(r'\}\n\n(  void _showBackupFlow.*)', content, re.MULTILINE | re.DOTALL)

if methods_match:
    methods = methods_match.group(1)
    
    # Remove it from the end of the file
    content = content.replace(methods_match.group(0), '}\n')
    
    # The last `}` is now the closing of AccountSecuritySection. Let's find it.
    end_index = content.rfind('}')
    content = content[:end_index] + methods + '\n}\n'

with open(pagudhi_path, 'w', encoding='utf-8') as f:
    f.write(content)

print("Fixed methods placement again.")
