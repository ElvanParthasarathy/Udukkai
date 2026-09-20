import os

file_path = r'D:\Projects\Elvan Niril\flutter\lib\src\cheyalpaadugal\amaippugal\kaatchi\koorugal\thaekkagam_pagudhi.dart'

with open(file_path, 'r', encoding='utf-8') as f:
    content = f.read()

# 1. Add imports for flutter_svg and AppSvgs
imports_to_add = """
import 'package:flutter_svg/flutter_svg.dart';
import '../../../../adippadai/panigal/seyali_oaviyangal.dart';
"""

if 'package:flutter_svg/flutter_svg.dart' not in content:
    content = content.replace("import 'package:flutter/material.dart';", "import 'package:flutter/material.dart';" + imports_to_add)

# 2. Combine the Coolie and Silk boxes and replace the icons
target_str = '''                ElvanSettingsSection(
                  children: [
                    ElvanSettingsRow(
                      iconWidget: Icon(
                        CupertinoIcons.doc_text_fill,
                        color: theme.colorScheme.onSurface,
                        size: 20,
                      ),
                      iconBgColor: theme.colorScheme.onSurface.withValues(alpha: 0.05),
                      title: 'Coolie Data',
                      customDescription: _buildDataGrid(context, stats['kooliInvoices'], stats['kooliReceipts'], stats['kooliCustomers'], stats['kooliProducts']),
                      onTap: () {},
                    ),
                  ],
                ),
                const SizedBox(height: 24),
                ElvanSettingsSection(
                  children: [
                    ElvanSettingsRow(
                      iconWidget: Icon(
                        CupertinoIcons.cube_box_fill,
                        color: theme.colorScheme.onSurface,
                        size: 20,
                      ),
                      iconBgColor: theme.colorScheme.onSurface.withValues(alpha: 0.05),
                      title: 'Silk Data',
                      customDescription: _buildDataGrid(context, stats['silkInvoices'], stats['silkReceipts'], stats['silkCustomers'], stats['silkProducts']),
                      onTap: () {},
                    ),
                  ],
                ),'''

replacement_str = '''                ElvanSettingsSection(
                  children: [
                    ElvanSettingsRow(
                      iconWidget: SvgPicture.string(
                        AppSvgs.coolieMode,
                        width: 20,
                        height: 20,
                        colorFilter: ColorFilter.mode(theme.colorScheme.onSurface, BlendMode.srcIn),
                      ),
                      iconBgColor: theme.colorScheme.onSurface.withValues(alpha: 0.05),
                      title: 'Coolie Data',
                      customDescription: _buildDataGrid(context, stats['kooliInvoices'], stats['kooliReceipts'], stats['kooliCustomers'], stats['kooliProducts']),
                      onTap: () {},
                    ),
                    ElvanSettingsRow(
                      iconWidget: SvgPicture.string(
                        AppSvgs.silkMode,
                        width: 20,
                        height: 20,
                        colorFilter: ColorFilter.mode(theme.colorScheme.onSurface, BlendMode.srcIn),
                      ),
                      iconBgColor: theme.colorScheme.onSurface.withValues(alpha: 0.05),
                      title: 'Silk Data',
                      customDescription: _buildDataGrid(context, stats['silkInvoices'], stats['silkReceipts'], stats['silkCustomers'], stats['silkProducts']),
                      onTap: () {},
                    ),
                  ],
                ),'''

content = content.replace(target_str, replacement_str)

with open(file_path, 'w', encoding='utf-8') as f:
    f.write(content)

print("Combined boxes and updated icons successfully.")
