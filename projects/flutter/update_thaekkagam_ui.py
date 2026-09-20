import os
import re

base_path = r'D:\Projects\Elvan Niril\flutter\lib\src\cheyalpaadugal\amaippugal\kaatchi\koorugal'
amaippu_pagudhi_path = os.path.join(base_path, 'elvan_amaippu_pagudhi.dart')
thaekkagam_path = os.path.join(base_path, 'thaekkagam_pagudhi.dart')

# 1. Update ElvanSettingsRow in elvan_amaippu_pagudhi.dart
with open(amaippu_pagudhi_path, 'r', encoding='utf-8') as f:
    amaippu_content = f.read()

# Add customDescription field
amaippu_content = amaippu_content.replace(
    'this.description,\n    this.onTap,\n  })',
    'this.description,\n    this.customDescription,\n    this.onTap,\n  })'
)
amaippu_content = amaippu_content.replace(
    'final String? description;\n  final VoidCallback? onTap;',
    'final String? description;\n  final Widget? customDescription;\n  final VoidCallback? onTap;'
)

# Render customDescription
custom_desc_widget = '''                  if (description != null)
                    Padding(
                      padding: const EdgeInsets.only(top: 2),
                      child: Text(
                        description!,
                        style: TextStyle(
                          fontSize: 12,
                          color: Theme.of(context)
                              .colorScheme
                              .onSurface
                              .withValues(alpha: 0.5),
                        ),
                      ),
                    ),
                  if (customDescription != null)
                    Padding(
                      padding: const EdgeInsets.only(top: 8),
                      child: customDescription!,
                    ),'''

amaippu_content = re.sub(
    r'                  if \(description != null\)[\s\S]*?\)\),[\s\n]*\],',
    custom_desc_widget + '\n                ],',
    amaippu_content
)

with open(amaippu_pagudhi_path, 'w', encoding='utf-8') as f:
    f.write(amaippu_content)

# 2. Update thaekkagam_pagudhi.dart
with open(thaekkagam_path, 'r', encoding='utf-8') as f:
    thaekkagam_content = f.read()

# Add StorageProjectionPill class
pill_class = '''
class StorageProjectionPill extends ConsumerWidget {
  final Map<String, dynamic> stats;
  const StorageProjectionPill({super.key, required this.stats});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final theme = Theme.of(context);
    final dbSize = stats['totalDbSize'] as int;
    final totalItems = (stats['kooliInvoices'] as int) + 
                       (stats['kooliReceipts'] as int) + 
                       (stats['kooliCustomers'] as int) + 
                       (stats['kooliProducts'] as int) + 
                       (stats['silkInvoices'] as int) + 
                       (stats['silkReceipts'] as int) + 
                       (stats['silkCustomers'] as int) + 
                       (stats['silkProducts'] as int);
    
    final avgSize = totalItems > 0 ? (dbSize / totalItems) : 4096.0;
    final maxSpace = 1024.0 * 1024.0 * 1024.0; // 1 GB
    final maxItems = (maxSpace / avgSize).floor();
    final remainingItems = (maxItems - totalItems).clamp(0, maxItems);
    final percent = dbSize / maxSpace;

    // Formatting sizes
    String formatBytes(double bytes) {
      if (bytes <= 0) return "0 B";
      const suffixes = ["B", "KB", "MB", "GB", "TB"];
      var i = 0;
      double size = bytes;
      while (size > 1024 && i < suffixes.length - 1) {
        size /= 1024;
        i++;
      }
      return '${size.toStringAsFixed(2)} ${suffixes[i]}';
    }

    return Container(
      margin: const EdgeInsets.only(bottom: 24),
      padding: const EdgeInsets.all(20),
      decoration: BoxDecoration(
        color: theme.colorScheme.onSurface.withValues(alpha: 0.03),
        borderRadius: BorderRadius.circular(24),
        border: Border.all(
          color: theme.colorScheme.onSurface.withValues(alpha: 0.05),
        ),
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Row(
            mainAxisAlignment: MainAxisAlignment.spaceBetween,
            children: [
              Text(
                'Cloud Storage (1GB Limit)',
                style: TextStyle(
                  fontSize: 14,
                  fontWeight: FontWeight.w600,
                  color: theme.colorScheme.onSurface,
                ),
              ),
              Text(
                '${(percent * 100).toStringAsFixed(4)}%',
                style: TextStyle(
                  fontSize: 13,
                  fontWeight: FontWeight.w600,
                  color: theme.colorScheme.onSurface.withValues(alpha: 0.5),
                ),
              ),
            ],
          ),
          const SizedBox(height: 16),
          ClipRRect(
            borderRadius: BorderRadius.circular(100),
            child: LinearProgressIndicator(
              value: percent,
              minHeight: 12,
              backgroundColor: theme.colorScheme.onSurface.withValues(alpha: 0.1),
              valueColor: AlwaysStoppedAnimation<Color>(theme.colorScheme.onSurface),
            ),
          ),
          const SizedBox(height: 16),
          Text(
            '${formatBytes(dbSize.toDouble())} used. At your current usage rate, you can store approximately ${NumberFormat.decimalPattern().format(remainingItems)} more items before hitting the free cloud limit.',
            style: TextStyle(
              fontSize: 12,
              color: theme.colorScheme.onSurface.withValues(alpha: 0.6),
              height: 1.4,
            ),
          ),
        ],
      ),
    );
  }
}
'''

# Replace description strings with customDescription grid calls
thaekkagam_content = thaekkagam_content.replace(
    '''                  title: 'Coolie Data',
                  description: '${stats['kooliInvoices']} Invoices  •  ${stats['kooliReceipts']} Receipts  •  ${stats['kooliCustomers']} Customers  •  ${stats['kooliProducts']} Products',
                  onTap: () {},''',
    '''                  title: 'Coolie Data',
                  customDescription: _buildDataGrid(context, stats['kooliInvoices'], stats['kooliReceipts'], stats['kooliCustomers'], stats['kooliProducts']),
                  onTap: () {},'''
)

thaekkagam_content = thaekkagam_content.replace(
    '''                  title: 'Silk Data',
                  description: '${stats['silkInvoices']} Invoices  •  ${stats['silkReceipts']} Receipts  •  ${stats['silkCustomers']} Customers  •  ${stats['silkProducts']} Products',
                  onTap: () {},''',
    '''                  title: 'Silk Data',
                  customDescription: _buildDataGrid(context, stats['silkInvoices'], stats['silkReceipts'], stats['silkCustomers'], stats['silkProducts']),
                  onTap: () {},'''
)

# Add the pill right after statsAsync.when( data: (stats) { ... return Column(children: [StorageProjectionPill(stats: stats), ElvanSettingsSection(...)])
# Wait, currently it returns ElvanSettingsSection directy.
# Let's replace `return ElvanSettingsSection(` with `return Column(children: [StorageProjectionPill(stats: stats), ElvanSettingsSection(`
# And add closing bracket at the end.
thaekkagam_content = thaekkagam_content.replace(
    'return ElvanSettingsSection(',
    'return Column(children: [\n                StorageProjectionPill(stats: stats),\n                ElvanSettingsSection('
)
thaekkagam_content = thaekkagam_content.replace(
    '              ];\n            );\n          },\n          loading',
    '              ],\n            ),\n              ],);\n          },\n          loading'
)

# And add `_buildDataGrid` method into `StorageManagerSection`
grid_method = '''
  Widget _buildDataGrid(BuildContext context, int invoices, int receipts, int customers, int products) {
    final color = Theme.of(context).colorScheme.onSurface.withValues(alpha: 0.5);
    final style = TextStyle(fontSize: 12, color: color);
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        Row(
          children: [
            Expanded(child: Text('$invoices Invoices', style: style)),
            Expanded(child: Text('$receipts Receipts', style: style)),
          ],
        ),
        const SizedBox(height: 6),
        Row(
          children: [
            Expanded(child: Text('$customers Customers', style: style)),
            Expanded(child: Text('$products Products', style: style)),
          ],
        ),
      ],
    );
  }
'''

thaekkagam_content = thaekkagam_content.replace(
    '  String _formatBytes(int bytes) {',
    grid_method + '\n  String _formatBytes(int bytes) {'
)

# Insert the pill class at the bottom
thaekkagam_content += pill_class

with open(thaekkagam_path, 'w', encoding='utf-8') as f:
    f.write(thaekkagam_content)

print("Updated UI with Pill and Grid.")
