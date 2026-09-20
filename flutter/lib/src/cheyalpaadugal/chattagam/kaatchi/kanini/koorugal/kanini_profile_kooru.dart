import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:flutter_svg/flutter_svg.dart';

import 'package:elvan_niril/src/adippadai/mozhiyaakkam/k.dart';
import '../../../../../adippadai/mozhiyaakkam/mozhi_vazhanguthi.dart';
import '../../../../../adippadai/tharavuru/seyali_murai.dart';
import '../../../../../adippadai/nilaimai/seyali_nilaimai.dart';
import '../../../../../adippadai/panigal/seyali_oaviyangal.dart';
import '../../../../ulnuzhaivu/kaatchi/muraimai_thaervu_thirai.dart';

/// SVG assets for settings icon states.
const String settingsOutlineSvg =
    '<svg xmlns="http://www.w3.org/2000/svg" width="32" height="32" fill="#000000" viewBox="0 0 256 256"><path d="M128,80a48,48,0,1,0,48,48A48.05,48.05,0,0,0,128,80Zm0,80a32,32,0,1,1,32-32A32,32,0,0,1,128,160Zm109.94-52.79a8,8,0,0,0-3.89-5.4l-29.83-17-.12-33.62a8,8,0,0,0-2.83-6.08,111.91,111.91,0,0,0-36.72-20.67,8,8,0,0,0-6.46.59L128,41.85,97.88,25a8,8,0,0,0-6.47-.6A112.1,112.1,0,0,0,54.73,45.15a8,8,0,0,0-2.83,6.07l-.15,33.65-29.83,17a8,8,0,0,0-3.89,5.4,106.47,106.47,0,0,0,0,41.56,8,8,0,0,0,3.89,5.4l29.83,17,.12,33.62a8,8,0,0,0,2.83,6.08,111.91,111.91,0,0,0,36.72,20.67,8,8,0,0,0,6.46-.59L128,214.15,158.12,231a7.91,7.91,0,0,0,3.9,1,8.09,8.09,0,0,0,2.57-.42,112.1,112.1,0,0,0,36.68-20.73,8,8,0,0,0,2.83-6.07l.15-33.65,29.83-17a8,8,0,0,0,3.89-5.4A106.47,106.47,0,0,0,237.94,107.21Zm-15,34.91-28.57,16.25a8,8,0,0,0-3,3c-.58,1-1.19,2.06-1.81,3.06a7.94,7.94,0,0,0-1.22,4.21l-.15,32.25a95.89,95.89,0,0,1-25.37,14.3L134,199.13a8,8,0,0,0-3.91-1h-.19c-1.21,0-2.43,0-3.64,0a8.08,8.08,0,0,0-4.1,1l-28.84,16.1A96,96,0,0,1,67.88,201l-.11-32.2a8,8,0,0,0-1.22-4.22c-.62-1-1.23-2-1.8-3.06a8.09,8.09,0,0,0-3-3.06l-28.6-16.29a90.49,90.49,0,0,1,0-28.26L61.67,97.63a8,8,0,0,0,3-3c.58-1,1.19-2.06,1.81-3.06a7.94,7.94,0,0,0,1.22-4.21l.15-32.25a95.89,95.89,0,0,1,25.37-14.3L122,56.87a8,8,0,0,0,4.1,1c1.21,0,2.43,0,3.64,0a8.08,8.08,0,0,0,4.1-1l28.84-16.1A96,96,0,0,1,188.12,55l.11,32.2a8,8,0,0,0,1.22,4.22c.62,1,1.23,2,1.8,3.06a8.09,8.09,0,0,0,3,3.06l28.6,16.29A90.49,90.49,0,0,1,222.9,142.12Z"></path></svg>';
const String settingsFilledSvg =
    '<svg xmlns="http://www.w3.org/2000/svg" width="32" height="32" fill="#000000" viewBox="0 0 256 256"><path d="M237.94,107.21a8,8,0,0,0-3.89-5.4l-29.83-17-.12-33.62a8,8,0,0,0-2.83-6.08,111.91,111.91,0,0,0-36.72-20.67,8,8,0,0,0-6.46.59L128,41.85,97.88,25a8,8,0,0,0-6.47-.6A111.92,111.92,0,0,0,54.73,45.15a8,8,0,0,0-2.83,6.07l-.15,33.65-29.83,17a8,8,0,0,0-3.89,5.4,106.47,106.47,0,0,0,0,41.56,8,8,0,0,0,3.89,5.4l29.83,17,.12,33.63a8,8,0,0,0,2.83,6.08,111.91,111.91,0,0,0,36.72,20.67,8,8,0,0,0,6.46-.59L128,214.15,158.12,231a7.91,7.91,0,0,0,3.9,1,8.09,8.09,0,0,0,2.57-.42,112.1,112.1,0,0,0,36.68-20.73,8,8,0,0,0,2.83-6.07l.15-33.65,29.83-17a8,8,0,0,0,3.89-5.4A106.47,106.47,0,0,0,237.94,107.21ZM128,168a40,40,0,1,1,40-40A40,40,0,0,1,128,168Z"></path></svg>';

/// Collapsed sidebar profile — just a settings gear icon.
class KaniniCollapsedProfileKooru extends ConsumerStatefulWidget {
  final bool isDark;
  final VoidCallback onSettingsPressed;
  final bool isSettingsActive;

  const KaniniCollapsedProfileKooru({
    super.key,
    required this.isDark,
    required this.onSettingsPressed,
    this.isSettingsActive = false,
  });

  @override
  ConsumerState<KaniniCollapsedProfileKooru> createState() =>
      _KaniniCollapsedProfileKooruState();
}

class _KaniniCollapsedProfileKooruState
    extends ConsumerState<KaniniCollapsedProfileKooru> {
  bool _isSettingsHovered = false;
  bool _isSettingsPressed = false;

  @override
  Widget build(BuildContext context) {
    return Container(
      width: 48,
      height: 48,
      alignment: Alignment.center,
      child: MouseRegion(
        onEnter: (_) => setState(() => _isSettingsHovered = true),
        onExit: (_) => setState(() => _isSettingsHovered = false),
        child: Material(
          color: Colors.transparent,
          child: InkWell(
            customBorder: const CircleBorder(),
            onTap: widget.onSettingsPressed,
            onHighlightChanged: (isPressed) =>
                setState(() => _isSettingsPressed = isPressed),
            hoverColor: widget.isDark
                ? Colors.white.withValues(alpha: 0.08)
                : Colors.black.withValues(alpha: 0.04),
            splashColor: widget.isDark
                ? Colors.white.withValues(alpha: 0.12)
                : Colors.black.withValues(alpha: 0.12),
            child: SizedBox(
              width: 40,
              height: 40,
              child: Center(
                child: SvgPicture.string(
                  _isSettingsPressed ? settingsFilledSvg : settingsOutlineSvg,
                  width: 20,
                  height: 20,
                  colorFilter: ColorFilter.mode(
                    _isSettingsHovered
                        ? (widget.isDark ? Colors.white : Colors.black)
                        : (widget.isDark
                            ? const Color(0xFFAAAAAA)
                            : const Color(0xFF666666)),
                    BlendMode.srcIn,
                  ),
                ),
              ),
            ),
          ),
        ),
      ),
    );
  }
}

/// Expanded sidebar profile — mode switcher + settings gear.
class KaniniExpandedProfileKooru extends ConsumerStatefulWidget {
  final bool isDark;
  final AppMode appMode;
  final VoidCallback onSettingsPressed;
  final VoidCallback? onModeSwitched;

  const KaniniExpandedProfileKooru({
    super.key,
    required this.isDark,
    required this.appMode,
    required this.onSettingsPressed,
    this.onModeSwitched,
  });

  @override
  ConsumerState<KaniniExpandedProfileKooru> createState() =>
      _KaniniExpandedProfileKooruState();
}

class _KaniniExpandedProfileKooruState
    extends ConsumerState<KaniniExpandedProfileKooru> {
  bool _isModeHovered = false;
  bool _isSettingsHovered = false;
  bool _isSettingsPressed = false;

  void _openModeSelector() {
    Navigator.of(context, rootNavigator: true).push(
      PageRouteBuilder(
        pageBuilder: (context, animation, secondaryAnimation) =>
            ModeSelectorScreen(
          onModeSelected: (mode) {
            ref.read(appModeProvider.notifier).setMode(mode);
            widget.onModeSwitched?.call();
            Navigator.of(context, rootNavigator: true).pop();
          },
        ),
        transitionsBuilder: (context, animation, secondaryAnimation, child) {
          return FadeTransition(opacity: animation, child: child);
        },
        fullscreenDialog: true,
        transitionDuration: const Duration(milliseconds: 300),
      ),
    );
  }

  @override
  Widget build(BuildContext context) {
    final modeFgColor = _isModeHovered
        ? (widget.isDark ? Colors.white : Colors.black)
        : (widget.isDark ? const Color(0xFFAAAAAA) : const Color(0xFF666666));

    return Row(
      mainAxisAlignment: MainAxisAlignment.center,
      children: [
        Expanded(
          child: MouseRegion(
            onEnter: (_) => setState(() => _isModeHovered = true),
            onExit: (_) => setState(() => _isModeHovered = false),
            cursor: SystemMouseCursors.click,
            child: GestureDetector(
              onTap: _openModeSelector,
              behavior: HitTestBehavior.opaque,
              child: Container(
                padding: const EdgeInsets.only(left: 18, right: 12),
                height: 52,
                decoration: BoxDecoration(
                  borderRadius: BorderRadius.circular(16),
                  color: Colors.transparent,
                ),
                child: Row(
                  children: [
                    Container(
                      width: 32,
                      height: 32,
                      decoration: BoxDecoration(
                        shape: BoxShape.circle,
                        color: widget.isDark
                            ? Colors.white.withValues(alpha: 0.1)
                            : Colors.black.withValues(alpha: 0.05),
                      ),
                      child: Center(
                        child: AnimatedContainer(
                          duration: const Duration(milliseconds: 200),
                          child: SvgPicture.string(
                            widget.appMode == AppMode.coolie
                                ? AppSvgs.coolieMode
                                : AppSvgs.silkMode,
                            width: 16,
                            height: 16,
                            colorFilter:
                                ColorFilter.mode(modeFgColor, BlendMode.srcIn),
                          ),
                        ),
                      ),
                    ),
                    const SizedBox(width: 10),
                    Expanded(
                      child: Padding(
                        padding: const EdgeInsets.only(top: 1),
                        child: AnimatedDefaultTextStyle(
                          duration: const Duration(milliseconds: 200),
                          style: TextStyle(
                            fontSize: 14,
                            fontWeight: FontWeight.w500,
                            color: modeFgColor,
                            fontFamily:
                                DefaultTextStyle.of(context).style.fontFamily,
                          ),
                          maxLines: 1,
                          overflow: TextOverflow.ellipsis,
                          child: Text(
                            widget.appMode == AppMode.coolie
                                ? K.nirilKooli.tr(context, ref)
                                : K.nirilPattu.tr(context, ref),
                          ),
                        ),
                      ),
                    ),
                  ],
                ),
              ),
            ),
          ),
        ),
        const SizedBox(width: 6),
        MouseRegion(
          onEnter: (_) => setState(() => _isSettingsHovered = true),
          onExit: (_) => setState(() => _isSettingsHovered = false),
          cursor: SystemMouseCursors.click,
          child: Material(
            color: Colors.transparent,
            child: InkWell(
              customBorder: const CircleBorder(),
              onTap: widget.onSettingsPressed,
              onHighlightChanged: (isPressed) =>
                  setState(() => _isSettingsPressed = isPressed),
              hoverColor: widget.isDark
                  ? Colors.white.withValues(alpha: 0.08)
                  : Colors.black.withValues(alpha: 0.04),
              splashColor: widget.isDark
                  ? Colors.white.withValues(alpha: 0.12)
                  : Colors.black.withValues(alpha: 0.12),
              child: Container(
                width: 34,
                height: 34,
                decoration: const BoxDecoration(
                  shape: BoxShape.circle,
                ),
                alignment: Alignment.center,
                child: SvgPicture.string(
                  _isSettingsPressed ? settingsFilledSvg : settingsOutlineSvg,
                  width: 18,
                  height: 18,
                  colorFilter: ColorFilter.mode(
                    _isSettingsHovered
                        ? (widget.isDark ? Colors.white : Colors.black)
                        : (widget.isDark
                            ? const Color(0xFFAAAAAA)
                            : const Color(0xFF666666)),
                    BlendMode.srcIn,
                  ),
                ),
              ),
            ),
          ),
        ),
        const SizedBox(width: 8),
      ],
    );
  }
}
