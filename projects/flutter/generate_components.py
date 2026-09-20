import os

pages = {
    'mugappu': 'Mugappu',
    'pattiyal': 'Pattiyal',
    'vanigar': 'Vanigar',
    'porul': 'Porul',
    'chaandru': 'Chaandru'
}

modes = ['Gst', 'Coolie']

base_path = r'd:\Projects\Elvan Niril\flutter\lib\src\features\pages\components'

template = '''import 'package:flutter/material.dart';

class {className} extends StatelessWidget {{
  const {className}({{super.key}});

  @override
  Widget build(BuildContext context) {{
    return SliverPadding(
      padding: const EdgeInsets.only(
        left: 12,
        right: 12,
        top: 32,
        bottom: 120, // clearance for the floating pill
      ),
      sliver: SliverList(
        delegate: SliverChildBuilderDelegate(
          (context, index) {{
            return Container(
              margin: const EdgeInsets.only(bottom: 16),
              height: 80,
              decoration: BoxDecoration(
                color: Colors.{color}.withValues(alpha: 0.1),
                borderRadius: BorderRadius.circular(16),
              ),
              child: Center(
                child: Text(
                  '{className} Item \',
                  style: TextStyle(
                    color: Colors.{color}.withValues(alpha: 0.8),
                    fontSize: 16,
                    fontWeight: FontWeight.w500,
                  ),
                ),
              ),
            );
          }},
          childCount: 50,
        ),
      ),
    );
  }}
}}
'''

for folder, prefix in pages.items():
    for mode in modes:
        class_name = f'{prefix}{mode}'
        color = 'blue' if mode == 'Gst' else 'green'
        content = template.format(className=class_name, color=color)
        file_path = os.path.join(base_path, folder, f'{folder}_{mode.lower()}.dart')
        
        with open(file_path, 'w') as f:
            f.write(content)

print("Generated all placeholder components.")
