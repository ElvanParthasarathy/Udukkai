import os
import re

file_path = r'D:\Projects\Elvan Niril\flutter\lib\src\cheyalpaadugal\ulnuzhaivu\kaatchi\muraimai_thaervu_thirai.dart'

with open(file_path, 'r', encoding='utf-8') as f:
    content = f.read()

# Target strings
silk_card = '''                _NetflixProfileCard(
                  title: K.nirilPattu.tr(context, ref),
                  iconBuilder: (color, size) => SvgPicture.string(
                    AppSvgs.silkMode,
                    width: size,
                    height: size,
                    colorFilter: ColorFilter.mode(color, BlendMode.srcIn),
                  ),
                  boxColor: isDark ? const Color(0xFF222222) : Colors.white,
                  iconColor: isDark ? Colors.white : const Color(0xFF111111),
                  isDark: isDark,
                  onTap: () => onModeSelected(AppMode.silk),
                ),'''

coolie_card = '''                _NetflixProfileCard(
                  title: K.nirilKooli.tr(context, ref),
                  iconBuilder: (color, size) => SvgPicture.string(
                    AppSvgs.coolieMode,
                    width: size,
                    height: size,
                    colorFilter: ColorFilter.mode(color, BlendMode.srcIn),
                  ),
                  boxColor: isDark ? const Color(0xFF222222) : Colors.white,
                  iconColor: isDark ? Colors.white : const Color(0xFF111111),
                  isDark: isDark,
                  onTap: () => onModeSelected(AppMode.coolie),
                ),'''

# Find the block containing both cards
target_block = f"{silk_card}\n{coolie_card}"
replacement_block = f"{coolie_card}\n{silk_card}"

if target_block in content:
    content = content.replace(target_block, replacement_block)
    with open(file_path, 'w', encoding='utf-8') as f:
        f.write(content)
    print("Swapped Coolie and Silk successfully.")
else:
    print("Target block not found. Could not swap.")
