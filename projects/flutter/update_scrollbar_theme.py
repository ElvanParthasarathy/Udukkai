import os

file_path = r'D:\Projects\Elvan Niril\flutter\lib\main.dart'

with open(file_path, 'r', encoding='utf-8') as f:
    content = f.read()

scrollbar_theme_light = '''          scrollbarTheme: ScrollbarThemeData(
            thickness: WidgetStateProperty.all(4.0),
            radius: const Radius.circular(10),
            thumbColor: WidgetStateProperty.resolveWith((states) {
              if (states.contains(WidgetState.dragged)) {
                return Colors.black.withValues(alpha: 0.5);
              }
              if (states.contains(WidgetState.hovered)) {
                return Colors.black.withValues(alpha: 0.3);
              }
              return Colors.black.withValues(alpha: 0.15);
            }),
            crossAxisMargin: 2,
            mainAxisMargin: 2,
          ),
'''

scrollbar_theme_dark = '''          scrollbarTheme: ScrollbarThemeData(
            thickness: WidgetStateProperty.all(4.0),
            radius: const Radius.circular(10),
            thumbColor: WidgetStateProperty.resolveWith((states) {
              if (states.contains(WidgetState.dragged)) {
                return Colors.white.withValues(alpha: 0.5);
              }
              if (states.contains(WidgetState.hovered)) {
                return Colors.white.withValues(alpha: 0.3);
              }
              return Colors.white.withValues(alpha: 0.15);
            }),
            crossAxisMargin: 2,
            mainAxisMargin: 2,
          ),
'''

# We insert into light theme
light_target = '''          snackBarTheme: SnackBarThemeData(
            behavior: SnackBarBehavior.floating,
            shape: RoundedRectangleBorder(
              borderRadius: BorderRadius.circular(100),
            ),
          ),'''

content = content.replace(light_target, light_target + '\n' + scrollbar_theme_light)

# We insert into dark theme
# Wait, the dark target is the same string, but the SECOND occurrence.
# Let's just do a manual replace looking for 'darkTheme: ThemeData(' block

dark_start = content.find('darkTheme: ThemeData(')
dark_target = content.find(light_target, dark_start)

content = content[:dark_target] + light_target + '\n' + scrollbar_theme_dark + content[dark_target + len(light_target):]

with open(file_path, 'w', encoding='utf-8') as f:
    f.write(content)

print("Scrollbar theme updated globally.")
