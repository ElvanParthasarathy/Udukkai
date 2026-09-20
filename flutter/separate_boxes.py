import os
import re

file_path = r'D:\Projects\Elvan Niril\flutter\lib\src\cheyalpaadugal\amaippugal\kaatchi\koorugal\thaekkagam_pagudhi.dart'

with open(file_path, 'r', encoding='utf-8') as f:
    content = f.read()

# Replace the single list of children with separated sections
# We'll split before the 'Coolie Data' row and before the 'Silk Data' row

target_str_coolie = '''                    ElvanSettingsRow(
                      iconWidget: Icon(
                        CupertinoIcons.doc_text_fill,'''

replacement_coolie = '''                  ],
                ),
                const SizedBox(height: 24),
                ElvanSettingsSection(
                  children: [
                    ElvanSettingsRow(
                      iconWidget: Icon(
                        CupertinoIcons.doc_text_fill,'''

target_str_silk = '''                    ElvanSettingsRow(
                      iconWidget: Icon(
                        CupertinoIcons.cube_box_fill,'''

replacement_silk = '''                  ],
                ),
                const SizedBox(height: 24),
                ElvanSettingsSection(
                  children: [
                    ElvanSettingsRow(
                      iconWidget: Icon(
                        CupertinoIcons.cube_box_fill,'''

content = content.replace(target_str_coolie, replacement_coolie)
content = content.replace(target_str_silk, replacement_silk)

with open(file_path, 'w', encoding='utf-8') as f:
    f.write(content)

print("Separated the boxes!")
