import os
import re

base_path = r'D:\Projects\Elvan Niril\flutter\lib\src\cheyalpaadugal\amaippugal\kaatchi\koorugal'
pagudhi_path = os.path.join(base_path, 'kanakku_paadhugaappu_pagudhi.dart')

with open(pagudhi_path, 'r', encoding='utf-8') as f:
    content = f.read()

# The methods were inserted after the final closing brace of AccountSecuritySection.
# Let's find the methods and move them inside.

methods_match = re.search(r'  void _showBackupFlow\(.*?\n  \}\n\n  void _showEraseDataFlow\(.*?\n  \}\n\n  void _showSyncFlow\(.*?\n  \}', content, re.MULTILINE | re.DOTALL)

if methods_match:
    methods = methods_match.group(0)
    # Remove from current location
    content = content.replace(methods, '')
    
    # Insert right before the last closing brace (assuming it's the class closing brace)
    # Since AccountSecuritySection is the ONLY class in this file. Wait, no, we have to find the end of AccountSecuritySection.
    end_index = content.rfind('}')
    content = content[:end_index] + methods + '\n}\n'

with open(pagudhi_path, 'w', encoding='utf-8') as f:
    f.write(content)

print("Fixed methods placement.")
